package jp.aoto.zerosum.core.engine

import jp.aoto.zerosum.core.content.GameCatalog
import jp.aoto.zerosum.core.model.CardInstance
import jp.aoto.zerosum.core.model.BossRule
import jp.aoto.zerosum.core.model.CombatantState
import jp.aoto.zerosum.core.model.EnemyState
import jp.aoto.zerosum.core.model.GameEvent
import jp.aoto.zerosum.core.model.GameEventKind
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.HeroClass
import jp.aoto.zerosum.core.model.RunStatus
import jp.aoto.zerosum.core.model.Screen
import jp.aoto.zerosum.core.model.Side
import jp.aoto.zerosum.core.rng.SplitMix64

/** Deterministic constructors for run and encounter state. */
public object StateFactory {
    /** Creates the ten-card starter deck specified by the design. */
    public fun newRun(heroClass: HeroClass, seed: Long, ascension: Int): GameState {
        require(ascension in 0..20) { "Ascension must be in 0..20: $ascension" }
        val hp = Balance.startingHp(heroClass)
        val ids = List(5) { "strike" } + List(4) { "guard" } + "cycle"
        val deck = ids.mapIndexed { index, id -> CardInstance(index + 1L, id) }
        return GameState(
            seed = seed,
            rngState = seed,
            nextInstanceId = deck.size + 1L,
            heroClass = heroClass,
            ascension = ascension,
            screen = Screen.MAP,
            runStatus = RunStatus.ACTIVE,
            gold = Balance.STARTING_GOLD,
            player = CombatantState(hp = hp, maxHp = hp),
            playerDeck = deck,
        )
    }

    /** Builds an encounter, selects enemy-pool cards, shuffles, and draws five. */
    public fun beginCombat(state: GameState, enemyId: String): GameState {
        val enemyDefinition = GameCatalog.enemy(enemyId)
        var nextId = state.nextInstanceId
        val baseCards = if (enemyDefinition.bossRule == BossRule.MIRROR) {
            state.playerDeck.map { original ->
                CardInstance(nextId++, original.definitionId, original.upgraded)
            }
        } else {
            enemyDefinition.baseDeck.map { definitionId -> CardInstance(nextId++, definitionId) }
        }
        val selected = EnemyAi.selectPoolCards(
            pool = state.enemyPool,
            enemy = enemyDefinition,
            player = state.player,
            count = Balance.enemyPoolTake(enemyDefinition.tier, state.ascension),
        )
        val enemyDeck = baseCards + selected
        val hpBonus = (enemyDefinition.baseHp * state.ascension) / 50
        val enemy = EnemyState(
            definitionId = enemyId,
            tier = enemyDefinition.tier,
            actor = CombatantState(enemyDefinition.baseHp + hpBonus, enemyDefinition.baseHp + hpBonus),
            deck = enemyDeck,
            intent = EnemyAi.intent(enemyDeck, 0),
            bossRule = enemyDefinition.bossRule,
        )
        val shuffled = SplitMix64.shuffle(state.rngState, state.playerDeck)
        val drawCount = minOf(Balance.HAND_SIZE, shuffled.value.size)
        val combat = state.copy(
            rngState = shuffled.state,
            nextInstanceId = nextId,
            screen = Screen.COMBAT,
            energy = Balance.STARTING_ENERGY,
            turn = 1,
            drawPile = shuffled.value.drop(drawCount),
            discardPile = emptyList(),
            exhaustPile = emptyList(),
            hand = shuffled.value.take(drawCount),
            enemy = enemy,
            draft = emptyList(),
            events = listOf(GameEvent(GameEventKind.TURN_STARTED, amount = 1, side = Side.PLAYER)),
        )
        return BossRules.applyStartOfPlayerTurn(combat)
    }
}
