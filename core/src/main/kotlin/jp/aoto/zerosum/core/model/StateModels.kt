package jp.aoto.zerosum.core.model

import kotlinx.serialization.Serializable

/** Immutable health, block, and status values for one combatant. */
@Serializable
public data class CombatantState(
    val hp: Int,
    val maxHp: Int,
    val block: Int = 0,
    val statuses: Map<Status, Int> = emptyMap(),
) {
    /** Returns a non-negative stack count. */
    public fun status(status: Status): Int = statuses[status] ?: 0
}

/** Immutable runtime state for the current enemy. */
@Serializable
public data class EnemyState(
    val definitionId: String,
    val tier: EnemyTier,
    val actor: CombatantState,
    val deck: List<CardInstance>,
    val intent: CardInstance,
    val intentCursor: Int = 0,
    val bossRule: BossRule = BossRule.NONE,
    val turn: Int = 1,
)

/** Compact event record consumed by UI animation and run summaries. */
@Serializable
public data class GameEvent(
    val kind: GameEventKind,
    val sourceId: String? = null,
    val amount: Int = 0,
    val side: Side? = null,
)

/** Aggregated local-only statistics for a single run. */
@Serializable
public data class RunStats(
    val cardsPlayed: Int = 0,
    val damageDealt: Int = 0,
    val damageTaken: Int = 0,
    val cardsSeized: Int = 0,
    val combatsWon: Int = 0,
    val draftsCompleted: Int = 0,
    val turns: Int = 0,
)

/** Entire serializable game state; every field participates in replay equality. */
@Serializable
public data class GameState(
    val version: Int = 1,
    val seed: Long = 1L,
    val rngState: Long = seed,
    val nextInstanceId: Long = 1L,
    val heroClass: HeroClass = HeroClass.CONDUCTOR,
    val ascension: Int = 0,
    val screen: Screen = Screen.TITLE,
    val runStatus: RunStatus = RunStatus.NOT_STARTED,
    val act: Int = 1,
    val nodeIndex: Int = 0,
    val gold: Int = 80,
    val player: CombatantState = CombatantState(hp = 72, maxHp = 72),
    val energy: Int = 3,
    val turn: Int = 0,
    val playerDeck: List<CardInstance> = emptyList(),
    val enemyPool: List<CardInstance> = emptyList(),
    val drawPile: List<CardInstance> = emptyList(),
    val discardPile: List<CardInstance> = emptyList(),
    val exhaustPile: List<CardInstance> = emptyList(),
    val hand: List<CardInstance> = emptyList(),
    val relicIds: List<String> = emptyList(),
    val enemy: EnemyState? = null,
    val draft: List<CardInstance> = emptyList(),
    val lastPlayedDefinitionId: String? = null,
    val events: List<GameEvent> = emptyList(),
    val stats: RunStats = RunStats(),
)

