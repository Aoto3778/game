package jp.aoto.zerosum.core.content

import jp.aoto.zerosum.core.model.CardClass
import jp.aoto.zerosum.core.model.CardDefinition
import jp.aoto.zerosum.core.model.CardRarity
import jp.aoto.zerosum.core.model.CardTag
import jp.aoto.zerosum.core.model.Effect
import jp.aoto.zerosum.core.model.EffectCondition
import jp.aoto.zerosum.core.model.EffectKind
import jp.aoto.zerosum.core.model.EffectTarget
import jp.aoto.zerosum.core.model.Scaling
import jp.aoto.zerosum.core.model.Status

internal fun card(
    id: String,
    cardClass: CardClass,
    rarity: CardRarity,
    cost: Int,
    effects: List<Effect>,
    upgraded: List<Effect>,
    tags: Set<CardTag> = setOf(CardTag.SKILL),
    upgradedCost: Int = cost,
    upgradedTags: Set<CardTag> = tags,
): CardDefinition = CardDefinition(
    id = id,
    nameKey = "card_${id}_name",
    descriptionKey = "card_${id}_description",
    cardClass = cardClass,
    rarity = rarity,
    cost = cost,
    upgradedCost = upgradedCost,
    effects = effects,
    upgradedEffects = upgraded,
    tags = tags,
    upgradedTags = upgradedTags,
)

internal fun damage(
    amount: Int,
    hits: Int = 1,
    scaling: Scaling = Scaling.NONE,
    condition: EffectCondition = EffectCondition.ALWAYS,
    threshold: Int = 0,
): Effect = Effect(EffectKind.DAMAGE, amount, hits, scaling = scaling, condition = condition, threshold = threshold)

internal fun block(
    amount: Int,
    scaling: Scaling = Scaling.NONE,
    condition: EffectCondition = EffectCondition.ALWAYS,
    threshold: Int = 0,
): Effect = Effect(
    EffectKind.BLOCK,
    amount,
    target = EffectTarget.SELF,
    scaling = scaling,
    condition = condition,
    threshold = threshold,
)

internal fun draw(amount: Int): Effect = Effect(EffectKind.DRAW, amount, target = EffectTarget.SELF)
internal fun energy(amount: Int): Effect = Effect(EffectKind.GAIN_ENERGY, amount, target = EffectTarget.SELF)
internal fun heal(amount: Int): Effect = Effect(EffectKind.HEAL, amount, target = EffectTarget.SELF)
internal fun seize(amount: Int): Effect = Effect(EffectKind.SEIZE, amount, target = EffectTarget.SELF)
internal fun discard(amount: Int): Effect = Effect(EffectKind.DISCARD, amount, target = EffectTarget.SELF)
internal fun returnDiscard(amount: Int): Effect = Effect(EffectKind.RETURN_DISCARD, amount, target = EffectTarget.SELF)
internal fun copyLast(amount: Int): Effect = Effect(EffectKind.COPY_LAST, amount, target = EffectTarget.SELF)
internal fun maxHp(amount: Int): Effect = Effect(EffectKind.GAIN_MAX_HP, amount, target = EffectTarget.SELF)
internal fun status(status: Status, amount: Int, self: Boolean = false): Effect = Effect(
    EffectKind.APPLY_STATUS,
    amount,
    target = if (self) EffectTarget.SELF else EffectTarget.OPPONENT,
    status = status,
)
