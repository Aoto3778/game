package jp.aoto.zerosum.sim

import jp.aoto.zerosum.core.content.GameCatalog
import jp.aoto.zerosum.core.engine.EnemyAi
import jp.aoto.zerosum.core.model.CardDefinition
import jp.aoto.zerosum.core.model.CardInstance
import jp.aoto.zerosum.core.model.CardTag
import jp.aoto.zerosum.core.model.EffectKind
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.HeroClass
import jp.aoto.zerosum.core.model.Scaling
import jp.aoto.zerosum.core.model.Status

internal interface BotPolicy {
    val name: String
    val denialWeight: Double
    fun playScore(card: CardInstance, state: GameState): Double
    fun selfDraftScore(card: CardDefinition, state: GameState): Double
}

internal abstract class WeightedBot(
    override val name: String,
    override val denialWeight: Double,
    private val offense: Double,
    private val defense: Double,
    private val utility: Double,
) : BotPolicy {
    override fun playScore(card: CardInstance, state: GameState): Double {
        val definition = GameCatalog.card(card.definitionId)
        val effects = if (card.upgraded) definition.upgradedEffects else definition.effects
        val energyCost = if (card.upgraded) definition.upgradedCost else definition.cost
        val lethalBonus = effects.filter { it.kind == EffectKind.DAMAGE }.sumOf { it.amount * it.hits }
            .takeIf { damage -> damage >= (state.enemy?.actor?.hp ?: Int.MAX_VALUE) }?.let { 100.0 } ?: 0.0
        return effects.sumOf { effectValue(it.kind, it.amount, effect = it, state = state) } - energyCost + lethalBonus
    }

    open override fun selfDraftScore(card: CardDefinition, state: GameState): Double {
        val base = card.effects.sumOf { effectValue(it.kind, it.amount, effect = it, state = state) }
        val classBonus = if (card.cardClass.name == state.heroClass.name) 3.0 else 0.0
        val rarityBonus = card.rarity.ordinal * 0.35
        return base + classBonus + rarityBonus - card.cost * 0.8
    }

    private fun effectValue(
        kind: EffectKind,
        amount: Int,
        effect: jp.aoto.zerosum.core.model.Effect,
        state: GameState,
    ): Double {
        val scalingBonus = when (effect.scaling) {
            Scaling.NONE -> 0.0
            Scaling.OUTPUT -> state.player.status(Status.OUTPUT).toDouble()
            Scaling.SELF_BLOCK -> state.player.block * 0.45
            Scaling.OPPONENT_SHOCK -> (state.enemy?.actor?.status(Status.SHOCK) ?: 2) * 0.7
            Scaling.ENEMY_POOL -> state.enemyPool.size * 0.25
            Scaling.HAND_SIZE -> state.hand.size * 0.45
            Scaling.MISSING_HP -> (state.player.maxHp - state.player.hp) * 0.15
        }
        return when (kind) {
            EffectKind.DAMAGE -> (amount + scalingBonus) * effect.hits * offense
            EffectKind.BLOCK -> (amount + scalingBonus) * defense
            EffectKind.DRAW -> amount * 3.2 * utility
            EffectKind.GAIN_ENERGY -> amount * 4.0 * utility
            EffectKind.HEAL -> amount * 2.4 * defense
            EffectKind.SEIZE -> amount * 8.0 * utility
            EffectKind.DISCARD -> -amount * 1.5
            EffectKind.RETURN_DISCARD -> amount * 3.5 * utility
            EffectKind.COPY_LAST -> amount * 5.0 * utility
            EffectKind.GAIN_MAX_HP -> amount * 3.0 * defense
            EffectKind.APPLY_STATUS -> when (effect.status) {
                Status.OUTPUT -> amount * 4.5 * offense
                Status.SHOCK -> amount * 2.2 * offense
                Status.OVERLOAD -> amount * 3.5 * offense
                Status.INTERRUPT -> amount * 6.0 * defense
                null -> 0.0
            }
        }
    }
}

internal object GreedyBot : WeightedBot("greedy", 0.55, offense = 1.45, defense = 0.55, utility = 0.8)
internal object SaverBot : WeightedBot("saver", 1.0, offense = 1.2, defense = 1.0, utility = 1.1)
internal object SynergyBot : WeightedBot("synergy", 0.8, offense = 1.05, defense = 0.9, utility = 1.5) {
    override fun selfDraftScore(card: CardDefinition, state: GameState): Double {
        val base = super.selfDraftScore(card, state)
        val synergy = when (state.heroClass) {
            HeroClass.CONDUCTOR -> card.effects.count { it.status in setOf(Status.SHOCK, Status.OUTPUT) } * 4.0
            HeroClass.BREAKER -> card.tags.count { it in setOf(CardTag.SEIZE, CardTag.RETAIN) } * 4.0
            HeroClass.RESOLVER -> card.effects.count { it.kind in setOf(EffectKind.DRAW, EffectKind.RETURN_DISCARD, EffectKind.COPY_LAST) } * 4.0
        }
        return base + synergy
    }
}

internal val standardBots: List<BotPolicy> = listOf(GreedyBot, SaverBot, SynergyBot)

internal fun draftScores(
    bot: BotPolicy,
    state: GameState,
    offered: List<CardInstance>,
    denialEnabled: Boolean,
): List<Pair<CardInstance, Double>> {
    val enemy = GameCatalog.enemy(if (state.act == 1) "synchronizer" else if (state.act == 2) "accumulator" else "zero")
    return offered.map { instance ->
        val definition = GameCatalog.card(instance.definitionId)
        val self = bot.selfDraftScore(definition, state)
        val denial = if (denialEnabled) {
            EnemyAi.poolScore(definition, enemy, state.player) * bot.denialWeight * 0.25
        } else {
            0.0
        }
        instance to self + denial
    }
}
