package jp.aoto.zerosum.core

import jp.aoto.zerosum.core.content.GameCatalog
import jp.aoto.zerosum.core.engine.reduce
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.CardClass
import jp.aoto.zerosum.core.model.GameEventKind
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class CardResolutionTest(
    private val cardId: String,
    private val upgraded: Boolean,
) {
    @Test
    fun everyCardVariantResolvesThroughGenericEffects() {
        val (state, card) = stateWithCard(cardId, upgraded)
        val result = reduce(state, Action.PlayCard(card.instanceId))
        assertTrue(result.events.any { it.kind == GameEventKind.CARD_PLAYED && it.sourceId == cardId })
        assertFalse(result.events.any { it.kind == GameEventKind.INVALID_ACTION })
        assertTrue(result.energy >= 0)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} upgraded={1}")
        fun variants(): List<Array<Any>> = GameCatalog.allCards()
            .filter { it.cardClass != CardClass.ENEMY }
            .flatMap { card -> listOf(arrayOf(card.id, false), arrayOf(card.id, true)) }
    }
}
