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
}

