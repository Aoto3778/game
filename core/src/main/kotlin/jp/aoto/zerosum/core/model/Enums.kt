package jp.aoto.zerosum.core.model

import kotlinx.serialization.Serializable

/** Playable character archetypes. */
@Serializable
public enum class HeroClass { CONDUCTOR, BREAKER, RESOLVER }

/** Core-owned navigation state rendered by the Android app. */
@Serializable
public enum class Screen { TITLE, MAP, COMBAT, DRAFT, DECK, EVENT, RESULT, SETTINGS, STATS }

/** Lifetime state of the current run. */
@Serializable
public enum class RunStatus { NOT_STARTED, ACTIVE, WON, LOST, ABANDONED }

/** Acting side used by the effect resolver. */
@Serializable
public enum class Side { PLAYER, ENEMY }

/** Card ownership restriction. */
@Serializable
public enum class CardClass { COMMON, CONDUCTOR, BREAKER, RESOLVER, ENEMY }

/** Draft rarity tier. */
@Serializable
public enum class CardRarity { STARTER, COMMON, UNCOMMON, RARE, SPECIAL }

/** Declarative card traits understood by the reducer. */
@Serializable
public enum class CardTag { ATTACK, SKILL, POWER, EXHAUST, RETAIN, SEIZE, ENEMY_ONLY }

/** The complete set of combat statuses. */
@Serializable
public enum class Status { OUTPUT, OVERLOAD, SHOCK, INTERRUPT }

/** Primitive operations available to card content. */
@Serializable
public enum class EffectKind {
    DAMAGE,
    BLOCK,
    DRAW,
    APPLY_STATUS,
    GAIN_ENERGY,
    HEAL,
    SEIZE,
    DISCARD,
    RETURN_DISCARD,
    COPY_LAST,
    GAIN_MAX_HP,
}

/** Recipient of an effect relative to the acting side. */
@Serializable
public enum class EffectTarget { SELF, OPPONENT }

/** Conditions supported by the generic effect resolver. */
@Serializable
public enum class EffectCondition {
    ALWAYS,
    IF_BLOCKED,
    IF_SHOCKED,
    IF_LOW_HP,
    IF_HAND_AT_LEAST,
    IF_ENEMY_POOL_AT_LEAST,
}

/** Optional value source added to an effect's base amount. */
@Serializable
public enum class Scaling {
    NONE,
    OUTPUT,
    SELF_BLOCK,
    OPPONENT_SHOCK,
    ENEMY_POOL,
    HAND_SIZE,
    MISSING_HP,
}

/** Encounter difficulty category. */
@Serializable
public enum class EnemyTier { NORMAL, ELITE, BOSS }

/** Boss-specific rule override. */
@Serializable
public enum class BossRule { NONE, SYNCHRONIZER, ACCUMULATOR, MIRROR, ZERO }

/** Relic drop rarity. */
@Serializable
public enum class RelicRarity { COMMON, UNCOMMON, RARE, BOSS }

/** Reducer hook represented by a relic definition. */
@Serializable
public enum class RelicEffect {
    START_BLOCK,
    START_ENERGY,
    DRAW_BONUS,
    DAMAGE_BONUS,
    HEAL_AFTER_COMBAT,
    MAX_HP,
    SEIZE_AFTER_DRAFT,
    GOLD_GAIN,
    FIRST_TURN_OUTPUT,
    START_INTERRUPT,
    SHOCK_BONUS,
    LOW_HP_BLOCK,
    RETAIN_BLOCK,
    COST_REDUCTION,
    POOL_TAKE_REDUCTION,
    REVIVE,
    UPGRADE_REWARD,
    EVENT_HEAL,
    BOSS_DAMAGE,
    TURN_HEAL,
}

/** Atomic result applied by an event choice. */
@Serializable
public enum class EventOutcomeKind {
    HP_DELTA,
    MAX_HP_DELTA,
    GOLD_DELTA,
    UPGRADE_RANDOM_CARD,
    REMOVE_RANDOM_CARD,
    ADD_CARD,
    BURN_POOL,
    ADD_RELIC,
    NOTHING,
}

/** Optional gate controlling whether an event choice is legal. */
@Serializable
public enum class EventRequirement { NONE, MIN_HP, MIN_GOLD, MIN_POOL, MAX_POOL }

/** Stable event kinds for animation and replay inspection. */
@Serializable
public enum class GameEventKind {
    CARD_PLAYED,
    DAMAGE,
    BLOCK,
    STATUS,
    DRAW,
    DISCARD,
    SEIZE,
    HEAL,
    TURN_STARTED,
    VICTORY,
    DEFEAT,
    INVALID_ACTION,
    EVENT_RESOLVED,
}
