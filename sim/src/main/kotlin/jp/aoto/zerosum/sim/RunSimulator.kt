package jp.aoto.zerosum.sim

import jp.aoto.zerosum.core.content.GameCatalog
import jp.aoto.zerosum.core.engine.reduce
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.CardInstance
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.HeroClass
import jp.aoto.zerosum.core.model.RunStatus
import jp.aoto.zerosum.core.model.Screen
import jp.aoto.zerosum.core.rng.SplitMix64

internal object RunSimulator {
    private val normalByAct = mapOf(
        1 to listOf("training_drone", "stray_coil", "copper_wasp", "relay_rat", "static_hound", "fuse_mite", "arc_monk", "battery_thief"),
        2 to listOf("rusted_guard", "pulse_turret", "cable_serpent", "grid_leech", "echo_unit", "breaker_adept"),
        3 to listOf("charge_mimic", "volt_vulture", "null_technician", "phase_stalker"),
    )
    private val elitesByAct = mapOf(
        1 to listOf("breaker_drone", "tesla_knight"),
        2 to listOf("audit_engine", "storm_colossus"),
        3 to listOf("blacksite_warden", "recursion_beast"),
    )
    private val bossByAct = mapOf(1 to "synchronizer", 2 to "accumulator", 3 to "zero")

    internal fun run(
        bot: BotPolicy,
        seed: Long,
        ascension: Int,
        denialEnabled: Boolean = true,
    ): RunResult {
        val hero = HeroClass.entries[Math.floorMod(seed, HeroClass.entries.size.toLong()).toInt()]
        var state = reduce(GameState(), Action.StartRun(hero, seed, ascension))
        val drafts = mutableListOf<DraftRecord>()
        var stuck = false
        for (act in 1..3) {
            state = state.copy(act = act)
            val normal = choose(normalByAct.getValue(act), seed + act * 31L)
            val normalResult = combat(state, normal, bot)
            state = normalResult.first
            stuck = stuck || normalResult.second
            if (state.runStatus != RunStatus.ACTIVE || stuck) return result(state, drafts, stuck)
            state = draft(state, bot, denialEnabled, drafts)
            state = maybeTreat(state)

            val takeElite = Math.floorMod(seed + act * 17L, 100L) < 56L
            if (takeElite) {
                val elite = choose(elitesByAct.getValue(act), seed + act * 47L)
                val eliteResult = combat(state, elite, bot)
                state = eliteResult.first
                stuck = stuck || eliteResult.second
                if (state.runStatus != RunStatus.ACTIVE || stuck) return result(state, drafts, stuck)
                state = draft(state, bot, denialEnabled, drafts)
            }
            state = reduce(state, Action.Rest)

            val bossResult = combat(state, bossByAct.getValue(act), bot)
            state = bossResult.first
            stuck = stuck || bossResult.second
            if (state.runStatus != RunStatus.ACTIVE || stuck) return result(state, drafts, stuck)
            state = draft(state, bot, denialEnabled, drafts)
            state = reduce(state, Action.CompleteAct)
        }
        return result(state, drafts, stuck)
    }

    private fun combat(
        initial: GameState,
        enemyId: String,
        bot: BotPolicy,
    ): Pair<GameState, Boolean> {
        var state = reduce(initial, Action.BeginCombat(enemyId))
        var safety = 0
        while (state.screen == Screen.COMBAT && state.runStatus == RunStatus.ACTIVE && safety < 50) {
            var plays = 0
            while (state.screen == Screen.COMBAT && plays < 40) {
                val playable = state.hand.filter { card ->
                    val definition = GameCatalog.card(card.definitionId)
                    val cost = if (card.upgraded) definition.upgradedCost else definition.cost
                    !card.faceDown && cost <= state.energy
                }
                if (playable.isEmpty()) break
                val chosen = playable.maxWithOrNull(
                    compareBy<CardInstance> { bot.playScore(it, state) }
                        .thenByDescending(CardInstance::definitionId),
                ) ?: break
                val next = reduce(state, Action.PlayCard(chosen.instanceId))
                if (next == state) break
                state = next
                plays++
            }
            if (state.screen == Screen.COMBAT) state = reduce(state, Action.EndTurn)
            safety++
        }
        return state to (state.screen == Screen.COMBAT && state.runStatus == RunStatus.ACTIVE)
    }

    private fun draft(
        state: GameState,
        bot: BotPolicy,
        denialEnabled: Boolean,
        records: MutableList<DraftRecord>,
    ): GameState {
        if (state.screen != Screen.DRAFT || state.draft.isEmpty()) return state
        val scores = draftScores(bot, state, state.draft, denialEnabled)
        val ranked = scores.sortedWith(compareByDescending<Pair<CardInstance, Double>> { it.second }.thenBy { it.first.definitionId })
        val exploreRoll = SplitMix64.nextInt(state.rngState xor bot.name.hashCode().toLong(), 100)
        val chosen = if (exploreRoll.value < 10) {
            val option = SplitMix64.nextInt(exploreRoll.state, state.draft.size)
            state.draft[option.value]
        } else {
            ranked.firstOrNull()?.first ?: return state
        }
        val selfBest = state.draft.maxOf { bot.selfDraftScore(GameCatalog.card(it.definitionId), state) }
        val chosenSelf = bot.selfDraftScore(GameCatalog.card(chosen.definitionId), state)
        val threatBest = state.draft.maxOf { instance ->
            val enemy = GameCatalog.enemy(if (state.act == 1) "synchronizer" else if (state.act == 2) "accumulator" else "zero")
            jp.aoto.zerosum.core.engine.EnemyAi.poolScore(GameCatalog.card(instance.definitionId), enemy, state.player)
        }
        val chosenThreat = run {
            val enemy = GameCatalog.enemy(if (state.act == 1) "synchronizer" else if (state.act == 2) "accumulator" else "zero")
            jp.aoto.zerosum.core.engine.EnemyAi.poolScore(GameCatalog.card(chosen.definitionId), enemy, state.player)
        }
        records += DraftRecord(
            offeredIds = state.draft.map(CardInstance::definitionId),
            chosenId = chosen.definitionId,
            denialDecision = denialEnabled && chosenSelf + 0.01 < selfBest && chosenThreat == threatBest,
        )
        return reduce(state, Action.ChooseDraft(chosen.instanceId))
    }

    private fun maybeTreat(state: GameState): GameState {
        if (state.player.hp * 4 >= state.player.maxHp * 3 || state.gold < 35) return state
        val opened = reduce(state, Action.BeginEvent("broken_medic"))
        return reduce(opened, Action.ChooseEvent("medic_treatment"))
    }

    private fun choose(options: List<String>, seed: Long): String {
        val random = SplitMix64.nextInt(seed, options.size)
        return options[random.value]
    }

    private fun result(state: GameState, drafts: List<DraftRecord>, stuck: Boolean): RunResult = RunResult(
        won = state.runStatus == RunStatus.WON,
        turns = state.stats.turns,
        stuck = stuck,
        drafts = drafts.toList(),
        reachedAct = state.act,
        remainingHp = state.player.hp,
    )
}
