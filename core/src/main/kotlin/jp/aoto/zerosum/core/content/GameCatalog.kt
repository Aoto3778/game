package jp.aoto.zerosum.core.content

import jp.aoto.zerosum.core.model.CardClass
import jp.aoto.zerosum.core.model.CardDefinition
import jp.aoto.zerosum.core.model.EnemyDefinition
import jp.aoto.zerosum.core.model.EventDefinition
import jp.aoto.zerosum.core.model.HeroClass
import jp.aoto.zerosum.core.model.RelicDefinition

/** Static, side-effect-free registry for all game content. */
public object GameCatalog {
    private val cards: List<CardDefinition> =
        CommonCards.cards + ConductorCards.cards + BreakerCards.cards + ResolverCards.cards + EnemyCards.cards
    private val enemies: List<EnemyDefinition> = Enemies.all
    private val relics: List<RelicDefinition> = Relics.all
    private val events: List<EventDefinition> = Events.all
    private val cardById = cards.associateBy(CardDefinition::id)
    private val enemyById = enemies.associateBy(EnemyDefinition::id)
    private val relicById = relics.associateBy(RelicDefinition::id)
    private val eventById = events.associateBy(EventDefinition::id)

    /** Returns a card or fails with a content-authoring message. */
    public fun card(id: String): CardDefinition = requireNotNull(cardById[id]) { "Unknown card id: $id" }

    /** Returns an enemy or fails with a content-authoring message. */
    public fun enemy(id: String): EnemyDefinition = requireNotNull(enemyById[id]) { "Unknown enemy id: $id" }

    /** Returns a relic or fails with a content-authoring message. */
    public fun relic(id: String): RelicDefinition = requireNotNull(relicById[id]) { "Unknown relic id: $id" }

    /** Returns an event or fails with a content-authoring message. */
    public fun event(id: String): EventDefinition = requireNotNull(eventById[id]) { "Unknown event id: $id" }

    /** All registered player and enemy cards. */
    public fun allCards(): List<CardDefinition> = cards

    /** All registered enemies. */
    public fun allEnemies(): List<EnemyDefinition> = enemies

    /** All registered relics. */
    public fun allRelics(): List<RelicDefinition> = relics

    /** All registered branching events. */
    public fun allEvents(): List<EventDefinition> = events

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
