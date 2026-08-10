package jp.aoto.zerosum.core.engine

import jp.aoto.zerosum.core.content.GameCatalog
import jp.aoto.zerosum.core.model.CardDefinition
import jp.aoto.zerosum.core.model.CardInstance
import jp.aoto.zerosum.core.model.CardTag
import jp.aoto.zerosum.core.model.CombatantState
import jp.aoto.zerosum.core.model.EffectKind
import jp.aoto.zerosum.core.model.EnemyDefinition

/** Deterministic enemy drafting and intent evaluation. */
public object EnemyAi {
    /** Scores how well a pool card helps a particular enemy. */
    public fun poolScore(
        card: CardDefinition,
        enemy: EnemyDefinition,
        player: CombatantState,
    ): Int {
        val effectScore = card.effects.sumOf { effect ->
            when (effect.kind) {
                EffectKind.DAMAGE -> effect.amount * effect.hits * 4
                EffectKind.BLOCK -> effect.amount * 2
                EffectKind.APPLY_STATUS -> effect.amount * 7
                EffectKind.HEAL -> effect.amount * 2
                EffectKind.DRAW -> effect.amount * 3
                EffectKind.GAIN_ENERGY -> effect.amount * 4
                EffectKind.SEIZE -> 0
                EffectKind.DISCARD -> effect.amount * 5
                EffectKind.RETURN_DISCARD -> effect.amount * 2
                EffectKind.COPY_LAST -> effect.amount * 3
                EffectKind.GAIN_MAX_HP -> effect.amount
            }
        }
        val attackBias = if (CardTag.ATTACK in card.tags && enemy.aiProfile == "aggressive") 8 else 0
        val executeBias = if (player.hp * 3 <= player.maxHp && CardTag.ATTACK in card.tags) 6 else 0
        return effectScore + attackBias + executeBias - card.cost * 2
    }

    /** Chooses the strongest distinct cards without consuming the shared pool. */
    public fun selectPoolCards(
        pool: List<CardInstance>,
        enemy: EnemyDefinition,
        player: CombatantState,
        count: Int,
    ): List<CardInstance> = pool
        .sortedWith(
            compareByDescending<CardInstance> { poolCard ->
                poolScore(GameCatalog.card(poolCard.definitionId), enemy, player)
            }.thenBy(CardInstance::definitionId).thenBy(CardInstance::instanceId),
        )
        .take(count)

    /** Returns the prepublished card at the deterministic deck cursor. */
    public fun intent(deck: List<CardInstance>, cursor: Int): CardInstance {
        require(deck.isNotEmpty()) { "Enemy deck must not be empty" }
        return deck[Math.floorMod(cursor, deck.size)]
    }
}

