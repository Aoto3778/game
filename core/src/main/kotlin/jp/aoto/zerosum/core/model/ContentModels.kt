package jp.aoto.zerosum.core.model

import kotlinx.serialization.Serializable

/** A single declarative operation resolved in list order. */
@Serializable
public data class Effect(
    val kind: EffectKind,
    val amount: Int = 0,
    val hits: Int = 1,
    val target: EffectTarget = EffectTarget.OPPONENT,
    val status: Status? = null,
    val condition: EffectCondition = EffectCondition.ALWAYS,
    val threshold: Int = 0,
    val scaling: Scaling = Scaling.NONE,
)

/** Static card data; user-facing copy is referenced by Android resource key. */
@Serializable
public data class CardDefinition(
    val id: String,
    val nameKey: String,
    val descriptionKey: String,
    val cardClass: CardClass,
    val rarity: CardRarity,
    val cost: Int,
    val upgradedCost: Int = cost,
    val effects: List<Effect>,
    val upgradedEffects: List<Effect> = effects,
    val tags: Set<CardTag>,
    val upgradedTags: Set<CardTag> = tags,
)

/** A concrete, uniquely addressable card in a deck or pile. */
@Serializable
public data class CardInstance(
    val instanceId: Long,
    val definitionId: String,
    val upgraded: Boolean = false,
    val faceDown: Boolean = false,
)

/** Static enemy data and its fixed eight-card base deck. */
@Serializable
public data class EnemyDefinition(
    val id: String,
    val nameKey: String,
    val tier: EnemyTier,
    val baseHp: Int,
    val baseDeck: List<String>,
    val bossRule: BossRule = BossRule.NONE,
    val aiProfile: String = "balanced",
)

