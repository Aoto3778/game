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
    val act: Int = 1,
    val bossRule: BossRule = BossRule.NONE,
    val aiProfile: String = "balanced",
)

/** Static relic data; one reducer hook plus a tunable amount. */
@Serializable
public data class RelicDefinition(
    val id: String,
    val nameKey: String,
    val descriptionKey: String,
    val rarity: RelicRarity,
    val effect: RelicEffect,
    val amount: Int,
)

/** One atomic consequence of selecting an event branch. */
@Serializable
public data class EventOutcome(
    val kind: EventOutcomeKind,
    val amount: Int = 0,
    val contentId: String? = null,
)

/** One selectable event branch and its explicit tradeoff. */
@Serializable
public data class EventChoice(
    val id: String,
    val labelKey: String,
    val outcomes: List<EventOutcome>,
    val requirement: EventRequirement = EventRequirement.NONE,
    val requirementAmount: Int = 0,
)

/** Static branching event content. */
@Serializable
public data class EventDefinition(
    val id: String,
    val titleKey: String,
    val bodyKey: String,
    val choices: List<EventChoice>,
    val actMin: Int = 1,
    val actMax: Int = 3,
)
