package jp.aoto.zerosum.core

import jp.aoto.zerosum.core.content.GameCatalog
import jp.aoto.zerosum.core.model.CardClass
import jp.aoto.zerosum.core.model.EffectKind
import jp.aoto.zerosum.core.model.EnemyTier
import jp.aoto.zerosum.core.model.EventOutcomeKind
import jp.aoto.zerosum.core.model.Status
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentValidationTest {
    @Test
    fun playerCardCountIsOneHundred() {
        assertEquals(100, playerCards().size)
    }

    @Test
    fun eachClassHasTwentyFiveExclusiveCards() {
        assertEquals(25, playerCards().count { it.cardClass == CardClass.CONDUCTOR })
        assertEquals(25, playerCards().count { it.cardClass == CardClass.BREAKER })
        assertEquals(25, playerCards().count { it.cardClass == CardClass.RESOLVER })
    }

    @Test
    fun commonShareIsExactlyTwentyFivePercent() {
        assertEquals(25, playerCards().count { it.cardClass == CardClass.COMMON })
    }

    @Test
    fun everyCardIdIsUnique() {
        val ids = GameCatalog.allCards().map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun everyPlayerCardHasAMeaningfulUpgrade() {
        playerCards().forEach { card ->
            val changed = card.effects != card.upgradedEffects ||
                card.cost != card.upgradedCost ||
                card.tags != card.upgradedTags
            assertTrue("No upgrade difference for ${card.id}", changed)
        }
    }

    @Test
    fun everyStatusEffectReferencesDefinedStatus() {
        GameCatalog.allCards().flatMap { it.effects + it.upgradedEffects }.forEach { effect ->
            if (effect.kind == EffectKind.APPLY_STATUS) {
                assertNotNull(effect.status)
                assertTrue(effect.status in Status.entries)
            } else {
                assertEquals(null, effect.status)
            }
        }
    }

    @Test
    fun cardCostsStayInsidePlayableRange() {
        playerCards().forEach { card ->
            assertTrue("base cost ${card.id}", card.cost in 0..3)
            assertTrue("upgrade cost ${card.id}", card.upgradedCost in 0..3)
        }
    }

    @Test
    fun enemyCountAndTierMixMatchDesign() {
        val enemies = GameCatalog.allEnemies()
        assertEquals(28, enemies.size)
        assertEquals(18, enemies.count { it.tier == EnemyTier.NORMAL })
        assertEquals(6, enemies.count { it.tier == EnemyTier.ELITE })
        assertEquals(4, enemies.count { it.tier == EnemyTier.BOSS })
    }

    @Test
    fun everyEnemyHasEightValidBaseCards() {
        val cardIds = GameCatalog.allCards().map { it.id }.toSet()
        GameCatalog.allEnemies().forEach { enemy ->
            assertEquals("base deck ${enemy.id}", 8, enemy.baseDeck.size)
            assertTrue("unknown base card ${enemy.id}", enemy.baseDeck.all(cardIds::contains))
        }
    }

    @Test
    fun fourBossesUseFourDifferentRuleOverrides() {
        val rules = GameCatalog.allEnemies().filter { it.tier == EnemyTier.BOSS }.map { it.bossRule }
        assertEquals(4, rules.distinct().size)
    }

    @Test
    fun relicCountIsFortyAndIdsAreUnique() {
        val ids = GameCatalog.allRelics().map { it.id }
        assertEquals(40, ids.size)
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun eventCountIsTwentyFiveAndEachBranches() {
        val events = GameCatalog.allEvents()
        assertEquals(25, events.size)
        assertEquals(events.size, events.map { it.id }.distinct().size)
        assertTrue(events.all { it.choices.size >= 2 })
    }

    @Test
    fun allEventContentReferencesResolve() {
        val cards = GameCatalog.allCards().map { it.id }.toSet()
        val relics = GameCatalog.allRelics().map { it.id }.toSet()
        GameCatalog.allEvents().flatMap { it.choices }.flatMap { it.outcomes }.forEach { outcome ->
            when (outcome.kind) {
                EventOutcomeKind.ADD_CARD -> assertTrue(outcome.contentId in cards)
                EventOutcomeKind.ADD_RELIC -> assertTrue(outcome.contentId in relics)
                else -> assertEquals(null, outcome.contentId)
            }
        }
    }

    @Test
    fun everyCardVariantHasAtLeastOneImplementedEffect() {
        GameCatalog.allCards().forEach { card ->
            assertFalse("base effects ${card.id}", card.effects.isEmpty())
            assertFalse("upgrade effects ${card.id}", card.upgradedEffects.isEmpty())
            card.effects.forEach { assertTrue(it.kind in EffectKind.entries) }
            card.upgradedEffects.forEach { assertTrue(it.kind in EffectKind.entries) }
        }
    }

    @Test
    fun resourceKeysAreStableAndNotUserFacingCopy() {
        GameCatalog.allCards().forEach { card ->
            assertEquals("card_${card.id}_name", card.nameKey)
            assertEquals("card_${card.id}_description", card.descriptionKey)
            assertNotEquals(card.id, card.nameKey)
        }
    }

    private fun playerCards() = GameCatalog.allCards().filter { it.cardClass != CardClass.ENEMY }
}

