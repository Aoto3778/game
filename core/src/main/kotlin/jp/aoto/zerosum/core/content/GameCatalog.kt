package jp.aoto.zerosum.core.content

import jp.aoto.zerosum.core.model.CardClass
import jp.aoto.zerosum.core.model.CardDefinition
import jp.aoto.zerosum.core.model.CardRarity
import jp.aoto.zerosum.core.model.CardTag
import jp.aoto.zerosum.core.model.Effect
import jp.aoto.zerosum.core.model.EffectKind
import jp.aoto.zerosum.core.model.EffectTarget
import jp.aoto.zerosum.core.model.EnemyDefinition
import jp.aoto.zerosum.core.model.EnemyTier
import jp.aoto.zerosum.core.model.HeroClass
import jp.aoto.zerosum.core.model.Scaling
import jp.aoto.zerosum.core.model.Status

/** Static, side-effect-free registry for all game content. */
public object GameCatalog {
    private fun card(
        id: String,
        cardClass: CardClass,
        rarity: CardRarity,
        cost: Int,
        vararg effects: Effect,
        upgradedCost: Int = cost,
        upgraded: List<Effect> = effects.toList(),
        tags: Set<CardTag> = setOf(CardTag.SKILL),
    ): CardDefinition = CardDefinition(
        id = id,
        nameKey = "card_${id}_name",
        descriptionKey = "card_${id}_description",
        cardClass = cardClass,
        rarity = rarity,
        cost = cost,
        upgradedCost = upgradedCost,
        effects = effects.toList(),
        upgradedEffects = upgraded,
        tags = tags,
    )

    private val cards: List<CardDefinition> = listOf(
        card(
            "strike", CardClass.COMMON, CardRarity.STARTER, 1,
            Effect(EffectKind.DAMAGE, 6),
            upgraded = listOf(Effect(EffectKind.DAMAGE, 9)),
            tags = setOf(CardTag.ATTACK),
        ),
        card(
            "guard", CardClass.COMMON, CardRarity.STARTER, 1,
            Effect(EffectKind.BLOCK, 5, target = EffectTarget.SELF),
            upgraded = listOf(Effect(EffectKind.BLOCK, 8, target = EffectTarget.SELF)),
        ),
        card(
            "cycle", CardClass.COMMON, CardRarity.STARTER, 1,
            Effect(EffectKind.DRAW, 2, target = EffectTarget.SELF),
            upgraded = listOf(Effect(EffectKind.DRAW, 3, target = EffectTarget.SELF)),
        ),
        card(
            "spark", CardClass.CONDUCTOR, CardRarity.COMMON, 1,
            Effect(EffectKind.DAMAGE, 4),
            Effect(EffectKind.APPLY_STATUS, 2, status = Status.SHOCK),
            upgraded = listOf(
                Effect(EffectKind.DAMAGE, 5),
                Effect(EffectKind.APPLY_STATUS, 3, status = Status.SHOCK),
            ),
            tags = setOf(CardTag.ATTACK),
        ),
        card(
            "surge", CardClass.CONDUCTOR, CardRarity.UNCOMMON, 1,
            Effect(EffectKind.APPLY_STATUS, 2, target = EffectTarget.SELF, status = Status.OUTPUT),
            upgraded = listOf(Effect(EffectKind.APPLY_STATUS, 3, target = EffectTarget.SELF, status = Status.OUTPUT)),
            tags = setOf(CardTag.POWER),
        ),
        card(
            "grounding", CardClass.BREAKER, CardRarity.UNCOMMON, 1,
            Effect(EffectKind.BLOCK, 7, target = EffectTarget.SELF),
            Effect(EffectKind.APPLY_STATUS, 1, target = EffectTarget.SELF, status = Status.INTERRUPT),
            upgraded = listOf(
                Effect(EffectKind.BLOCK, 10, target = EffectTarget.SELF),
                Effect(EffectKind.APPLY_STATUS, 1, target = EffectTarget.SELF, status = Status.INTERRUPT),
            ),
        ),
        card(
            "seizure", CardClass.BREAKER, CardRarity.RARE, 1,
            Effect(EffectKind.DAMAGE, 3),
            Effect(EffectKind.SEIZE, 1, target = EffectTarget.SELF),
            upgraded = listOf(Effect(EffectKind.DAMAGE, 6), Effect(EffectKind.SEIZE, 1, target = EffectTarget.SELF)),
            tags = setOf(CardTag.ATTACK, CardTag.SEIZE, CardTag.EXHAUST),
        ),
        card(
            "overclock", CardClass.RESOLVER, CardRarity.UNCOMMON, 0,
            Effect(EffectKind.GAIN_ENERGY, 1, target = EffectTarget.SELF),
            Effect(EffectKind.DRAW, 2, target = EffectTarget.SELF),
            upgraded = listOf(
                Effect(EffectKind.GAIN_ENERGY, 2, target = EffectTarget.SELF),
                Effect(EffectKind.DRAW, 2, target = EffectTarget.SELF),
            ),
            tags = setOf(CardTag.SKILL, CardTag.EXHAUST),
        ),
        card(
            "reclaim", CardClass.RESOLVER, CardRarity.COMMON, 1,
            Effect(EffectKind.RETURN_DISCARD, 1, target = EffectTarget.SELF),
            upgradedCost = 0,
        ),
        card(
            "pulse", CardClass.CONDUCTOR, CardRarity.UNCOMMON, 2,
            Effect(EffectKind.DAMAGE, 2, hits = 3, scaling = Scaling.OPPONENT_SHOCK),
            upgraded = listOf(Effect(EffectKind.DAMAGE, 2, hits = 4, scaling = Scaling.OPPONENT_SHOCK)),
            tags = setOf(CardTag.ATTACK),
        ),
        card(
            "triage", CardClass.COMMON, CardRarity.RARE, 1,
            Effect(EffectKind.HEAL, 5, target = EffectTarget.SELF),
            upgraded = listOf(Effect(EffectKind.HEAL, 8, target = EffectTarget.SELF)),
            tags = setOf(CardTag.SKILL, CardTag.EXHAUST),
        ),
        card(
            "echo", CardClass.RESOLVER, CardRarity.RARE, 1,
            Effect(EffectKind.COPY_LAST, 1, target = EffectTarget.SELF),
            upgradedCost = 0,
            tags = setOf(CardTag.SKILL, CardTag.EXHAUST),
        ),
        card(
            "scramble", CardClass.RESOLVER, CardRarity.UNCOMMON, 0,
            Effect(EffectKind.DRAW, 2, target = EffectTarget.SELF),
            Effect(EffectKind.DISCARD, 1, target = EffectTarget.SELF),
            upgraded = listOf(
                Effect(EffectKind.DRAW, 3, target = EffectTarget.SELF),
                Effect(EffectKind.DISCARD, 1, target = EffectTarget.SELF),
            ),
        ),
        card(
            "reinforce", CardClass.BREAKER, CardRarity.RARE, 1,
            Effect(EffectKind.GAIN_MAX_HP, 3, target = EffectTarget.SELF),
            upgraded = listOf(Effect(EffectKind.GAIN_MAX_HP, 5, target = EffectTarget.SELF)),
            tags = setOf(CardTag.SKILL, CardTag.EXHAUST),
        ),
        card(
            "hold_current", CardClass.COMMON, CardRarity.COMMON, 1,
            Effect(EffectKind.BLOCK, 3, target = EffectTarget.SELF),
            upgraded = listOf(Effect(EffectKind.BLOCK, 6, target = EffectTarget.SELF)),
            tags = setOf(CardTag.SKILL, CardTag.RETAIN),
        ),
        card("enemy_jab", CardClass.ENEMY, CardRarity.SPECIAL, 0, Effect(EffectKind.DAMAGE, 6), tags = setOf(CardTag.ATTACK, CardTag.ENEMY_ONLY)),
        card("enemy_guard", CardClass.ENEMY, CardRarity.SPECIAL, 0, Effect(EffectKind.BLOCK, 6, target = EffectTarget.SELF), tags = setOf(CardTag.SKILL, CardTag.ENEMY_ONLY)),
        card("enemy_overload", CardClass.ENEMY, CardRarity.SPECIAL, 0, Effect(EffectKind.APPLY_STATUS, 1, status = Status.OVERLOAD), tags = setOf(CardTag.SKILL, CardTag.ENEMY_ONLY)),
        card("enemy_shock", CardClass.ENEMY, CardRarity.SPECIAL, 0, Effect(EffectKind.APPLY_STATUS, 3, status = Status.SHOCK), tags = setOf(CardTag.SKILL, CardTag.ENEMY_ONLY)),
        card("enemy_heavy", CardClass.ENEMY, CardRarity.SPECIAL, 0, Effect(EffectKind.DAMAGE, 12), tags = setOf(CardTag.ATTACK, CardTag.ENEMY_ONLY)),
    )

    private val enemies: List<EnemyDefinition> = listOf(
        EnemyDefinition(
            id = "training_drone",
            nameKey = "enemy_training_drone",
            tier = EnemyTier.NORMAL,
            baseHp = 38,
            baseDeck = listOf("enemy_jab", "enemy_guard", "enemy_jab", "enemy_overload", "enemy_jab", "enemy_guard", "enemy_shock", "enemy_heavy"),
        ),
        EnemyDefinition(
            id = "breaker_drone",
            nameKey = "enemy_breaker_drone",
            tier = EnemyTier.ELITE,
            baseHp = 74,
            baseDeck = listOf("enemy_heavy", "enemy_guard", "enemy_overload", "enemy_jab", "enemy_shock", "enemy_guard", "enemy_heavy", "enemy_jab"),
        ),
    )

    private val cardById: Map<String, CardDefinition> = cards.associateBy(CardDefinition::id)
    private val enemyById: Map<String, EnemyDefinition> = enemies.associateBy(EnemyDefinition::id)

    /** Returns a card or fails with a content-authoring message. */
    public fun card(id: String): CardDefinition = requireNotNull(cardById[id]) { "Unknown card id: $id" }

    /** Returns an enemy or fails with a content-authoring message. */
    public fun enemy(id: String): EnemyDefinition = requireNotNull(enemyById[id]) { "Unknown enemy id: $id" }

    /** All currently registered cards. */
    public fun allCards(): List<CardDefinition> = cards

    /** All currently registered enemies. */
    public fun allEnemies(): List<EnemyDefinition> = enemies

    /** Cards eligible for one hero's draft. */
    public fun playerCards(heroClass: HeroClass): List<CardDefinition> {
        val classRestriction = when (heroClass) {
            HeroClass.CONDUCTOR -> CardClass.CONDUCTOR
            HeroClass.BREAKER -> CardClass.BREAKER
            HeroClass.RESOLVER -> CardClass.RESOLVER
        }
        return cards.filter { it.cardClass == CardClass.COMMON || it.cardClass == classRestriction }
    }
}
