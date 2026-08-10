package jp.aoto.zerosum.core

import jp.aoto.zerosum.core.content.GameCatalog
import jp.aoto.zerosum.core.engine.Balance
import jp.aoto.zerosum.core.engine.EnemyAi
import jp.aoto.zerosum.core.engine.reduce
import jp.aoto.zerosum.core.engine.replay
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.CardInstance
import jp.aoto.zerosum.core.model.CombatantState
import jp.aoto.zerosum.core.model.GameEventKind
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.HeroClass
import jp.aoto.zerosum.core.model.RunStatus
import jp.aoto.zerosum.core.model.Screen
import jp.aoto.zerosum.core.model.Status
import jp.aoto.zerosum.core.save.GameStateJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleEngineTest {
    @Test
    fun conductorStartsAt72Hp() {
        assertEquals(72, newRun(HeroClass.CONDUCTOR).player.hp)
    }

    @Test
    fun breakerStartsAt84Hp() {
        assertEquals(84, newRun(HeroClass.BREAKER).player.hp)
    }

    @Test
    fun resolverStartsAt66Hp() {
        assertEquals(66, newRun(HeroClass.RESOLVER).player.hp)
    }

    @Test
    fun starterDeckHasTenSpecifiedCards() {
        val deck = newRun().playerDeck
        assertEquals(10, deck.size)
        assertEquals(5, deck.count { it.definitionId == "strike" })
        assertEquals(4, deck.count { it.definitionId == "guard" })
        assertEquals(1, deck.count { it.definitionId == "cycle" })
    }

    @Test
    fun newRunOpensMapAsActive() {
        val state = newRun()
        assertEquals(Screen.MAP, state.screen)
        assertEquals(RunStatus.ACTIVE, state.runStatus)
        assertEquals(80, state.gold)
    }

    @Test
    fun invalidAscensionDoesNotStartRun() {
        val result = reduce(GameState(), Action.StartRun(HeroClass.CONDUCTOR, 1L, 21))
        assertEquals(RunStatus.NOT_STARTED, result.runStatus)
        assertEquals(GameEventKind.INVALID_ACTION, result.events.single().kind)
    }

    @Test
    fun combatDrawsFiveAndPublishesIntent() {
        val state = combatState()
        assertEquals(5, state.hand.size)
        assertEquals(3, state.energy)
        assertEquals(1, state.turn)
        assertEquals("enemy_jab", requireNotNull(state.enemy).intent.definitionId)
    }

    @Test
    fun normalEnemyTakesTwoPoolCards() {
        val run = newRun().copy(enemyPool = poolCards(6))
        val combat = reduce(run, Action.BeginCombat("training_drone"))
        assertEquals(10, requireNotNull(combat.enemy).deck.size)
    }

    @Test
    fun eliteTakesFourPoolCards() {
        val run = newRun().copy(enemyPool = poolCards(6))
        val combat = reduce(run, Action.BeginCombat("breaker_drone"))
        assertEquals(12, requireNotNull(combat.enemy).deck.size)
    }

    @Test
    fun ascensionTenAddsOnePoolCard() {
        val run = newRun(ascension = 10).copy(enemyPool = poolCards(6))
        val combat = reduce(run, Action.BeginCombat("training_drone"))
        assertEquals(11, requireNotNull(combat.enemy).deck.size)
    }

    @Test
    fun ascensionTwentyAddsTwoPoolCards() {
        assertEquals(4, Balance.enemyPoolTake(jp.aoto.zerosum.core.model.EnemyTier.NORMAL, 20))
    }

    @Test
    fun replayIsDeterministic() {
        val actions = listOf(
            Action.StartRun(HeroClass.CONDUCTOR, 91L),
            Action.BeginCombat("training_drone"),
            Action.EndTurn,
            Action.EndTurn,
        )
        assertEquals(replay(GameState(), actions), replay(GameState(), actions))
    }

    @Test
    fun differentSeedsChangeInitialHandOrder() {
        val first = combatState(10L).hand.map(CardInstance::instanceId)
        val second = combatState(11L).hand.map(CardInstance::instanceId)
        assertNotEquals(first, second)
    }

    @Test
    fun strikeCostsOneEnergy() {
        val (state, card) = stateWithCard("strike")
        assertEquals(98, reduce(state, Action.PlayCard(card.instanceId)).energy)
    }

    @Test
    fun unknownHandCardIsRejected() {
        val state = combatState()
        val result = reduce(state, Action.PlayCard(Long.MAX_VALUE))
        assertEquals(state.copy(events = emptyList()), result.copy(events = emptyList()))
        assertEquals(GameEventKind.INVALID_ACTION, result.events.single().kind)
    }

    @Test
    fun faceDownCardCannotBePlayed() {
        val (state, card) = stateWithCard("strike")
        val hidden = card.copy(faceDown = true)
        val result = reduce(state.copy(hand = listOf(hidden)), Action.PlayCard(hidden.instanceId))
        assertEquals(200, requireNotNull(result.enemy).actor.hp)
        assertEquals(GameEventKind.INVALID_ACTION, result.events.single().kind)
    }

    @Test
    fun insufficientEnergyRejectsCard() {
        val (state, card) = stateWithCard("pulse")
        val result = reduce(state.copy(energy = 1), Action.PlayCard(card.instanceId))
        assertEquals(200, requireNotNull(result.enemy).actor.hp)
        assertTrue(result.hand.contains(card))
    }

    @Test
    fun strikeDealsSixDamage() {
        assertEquals(194, enemyHpAfter("strike"))
    }

    @Test
    fun upgradedStrikeDealsNineDamage() {
        assertEquals(191, enemyHpAfter("strike", upgraded = true))
    }

    @Test
    fun guardAddsFiveBlock() {
        val result = play("guard")
        assertEquals(5, result.player.block)
    }

    @Test
    fun outputAddsDamagePerHit() {
        val (state, card) = stateWithCard("strike")
        val boosted = state.copy(player = state.player.copy(statuses = mapOf(Status.OUTPUT to 2)))
        assertEquals(192, requireNotNull(reduce(boosted, Action.PlayCard(card.instanceId)).enemy).actor.hp)
    }

    @Test
    fun overloadAmplifiesDamageByTwentyFivePercent() {
        val (state, card) = stateWithCard("strike")
        val enemy = requireNotNull(state.enemy)
        val exposed = state.copy(enemy = enemy.copy(actor = enemy.actor.copy(statuses = mapOf(Status.OVERLOAD to 1))))
        assertEquals(192, requireNotNull(reduce(exposed, Action.PlayCard(card.instanceId)).enemy).actor.hp)
    }

    @Test
    fun blockAbsorbsBeforeHp() {
        val (state, card) = stateWithCard("strike")
        val enemy = requireNotNull(state.enemy)
        val blocked = state.copy(enemy = enemy.copy(actor = enemy.actor.copy(block = 5)))
        val result = reduce(blocked, Action.PlayCard(card.instanceId))
        assertEquals(199, requireNotNull(result.enemy).actor.hp)
        assertEquals(0, requireNotNull(result.enemy).actor.block)
    }

    @Test
    fun multiHitConsumesBlockHitByHit() {
        val (state, card) = stateWithCard("pulse")
        val enemy = requireNotNull(state.enemy)
        val result = reduce(state.copy(enemy = enemy.copy(actor = enemy.actor.copy(block = 5))), Action.PlayCard(card.instanceId))
        assertEquals(199, requireNotNull(result.enemy).actor.hp)
    }

    @Test
    fun shockTicksThenHalvesAtTurnStart() {
        val state = combatState()
        val enemy = requireNotNull(state.enemy)
        val shocked = state.copy(enemy = enemy.copy(actor = enemy.actor.copy(statuses = mapOf(Status.SHOCK to 5))))
        val result = reduce(shocked, Action.EndTurn)
        assertEquals(33, requireNotNull(result.enemy).actor.hp)
        assertEquals(2, requireNotNull(result.enemy).actor.status(Status.SHOCK))
    }

    @Test
    fun interruptCancelsOnlyNextIncomingPrimitive() {
        val (state, card) = stateWithCard("spark")
        val enemy = requireNotNull(state.enemy)
        val protected = state.copy(enemy = enemy.copy(actor = enemy.actor.copy(statuses = mapOf(Status.INTERRUPT to 1))))
        val result = reduce(protected, Action.PlayCard(card.instanceId))
        assertEquals(200, requireNotNull(result.enemy).actor.hp)
        assertEquals(2, requireNotNull(result.enemy).actor.status(Status.SHOCK))
        assertEquals(0, requireNotNull(result.enemy).actor.status(Status.INTERRUPT))
    }

    @Test
    fun seizeBurnsExactlyOneSharedPoolCard() {
        val (state, card) = stateWithCard("seizure", seed = 8L)
        val result = reduce(state.copy(enemyPool = poolCards(3)), Action.PlayCard(card.instanceId))
        assertEquals(2, result.enemyPool.size)
        assertEquals(1, result.stats.cardsSeized)
    }

    @Test
    fun exhaustCardMovesToExhaustPile() {
        val (state, card) = stateWithCard("seizure")
        val result = reduce(state, Action.PlayCard(card.instanceId))
        assertTrue(result.exhaustPile.contains(card))
        assertFalse(result.discardPile.contains(card))
    }

    @Test
    fun normalCardMovesToDiscardPile() {
        val (state, card) = stateWithCard("strike")
        assertTrue(reduce(state, Action.PlayCard(card.instanceId)).discardPile.contains(card))
    }

    @Test
    fun enemyPublishedIntentDealsDamageOnEndTurn() {
        val result = reduce(combatState(), Action.EndTurn)
        assertEquals(66, result.player.hp)
    }

    @Test
    fun endTurnDrawsBackToFive() {
        assertEquals(5, reduce(combatState(), Action.EndTurn).hand.size)
    }

    @Test
    fun retainCardStaysInHand() {
        val state = combatState()
        val retained = CardInstance(9_001, "hold_current")
        val result = reduce(state.copy(hand = listOf(retained)), Action.EndTurn)
        assertTrue(result.hand.contains(retained))
    }

    @Test
    fun killingEnemyGeneratesFiveCardDraft() {
        val result = killTrainingEnemy()
        assertEquals(Screen.DRAFT, result.screen)
        assertEquals(5, result.draft.size)
        assertEquals(1, result.stats.combatsWon)
    }

    @Test
    fun draftChoiceIsStrictlyZeroSum() {
        val draft = killTrainingEnemy()
        val choice = draft.draft.first()
        val result = reduce(draft, Action.ChooseDraft(choice.instanceId))
        assertTrue(result.playerDeck.contains(choice))
        assertEquals(4, result.enemyPool.size)
        assertEquals(Screen.MAP, result.screen)
    }

    @Test
    fun invalidDraftChoiceDoesNotMoveCards() {
        val draft = killTrainingEnemy()
        val result = reduce(draft, Action.ChooseDraft(-1L))
        assertEquals(draft.draft, result.draft)
        assertTrue(result.enemyPool.isEmpty())
    }

    @Test
    fun serializedCombatRoundTripsExactly() {
        val state = reduce(combatState(73L), Action.EndTurn)
        assertEquals(state, GameStateJson.decode(GameStateJson.encode(state)))
    }

    @Test
    fun enemyIntentAdvancesAfterUse() {
        val result = reduce(combatState(), Action.EndTurn)
        assertEquals("enemy_guard", requireNotNull(result.enemy).intent.definitionId)
        assertEquals(1, requireNotNull(result.enemy).intentCursor)
    }

    @Test
    fun healCannotExceedMaximumHp() {
        val (state, card) = stateWithCard("triage")
        val injured = state.copy(player = CombatantState(70, 72))
        assertEquals(72, reduce(injured, Action.PlayCard(card.instanceId)).player.hp)
    }

    @Test
    fun returnDiscardMovesTopCardToHand() {
        val (state, card) = stateWithCard("reclaim")
        val strike = CardInstance(500L, "strike")
        val result = reduce(state.copy(discardPile = listOf(strike)), Action.PlayCard(card.instanceId))
        assertTrue(result.hand.contains(strike))
    }

    @Test
    fun copyLastCreatesNewInstance() {
        val (state, card) = stateWithCard("echo")
        val result = reduce(state.copy(lastPlayedDefinitionId = "strike"), Action.PlayCard(card.instanceId))
        assertTrue(result.hand.any { it.definitionId == "strike" })
        assertTrue(result.nextInstanceId > state.nextInstanceId)
    }

    @Test
    fun maxHpEffectHealsAndRaisesCap() {
        val (state, card) = stateWithCard("reinforce")
        val result = reduce(state.copy(player = CombatantState(60, 72)), Action.PlayCard(card.instanceId))
        assertEquals(63, result.player.hp)
        assertEquals(75, result.player.maxHp)
    }

    @Test
    fun playerOutputDecaysAtEndOfTurn() {
        val state = combatState().copy(player = combatState().player.copy(statuses = mapOf(Status.OUTPUT to 2)))
        assertEquals(1, reduce(state, Action.EndTurn).player.status(Status.OUTPUT))
    }

    @Test
    fun playerBlockProtectsThenClears() {
        val state = combatState().copy(player = combatState().player.copy(block = 5))
        val result = reduce(state, Action.EndTurn)
        assertEquals(71, result.player.hp)
        assertEquals(0, result.player.block)
    }

    @Test
    fun enemyAiSelectsHighestScoringPoolCard() {
        val enemy = GameCatalog.enemy("training_drone")
        val pool = listOf(CardInstance(1, "guard"), CardInstance(2, "pulse"), CardInstance(3, "triage"))
        val selected = EnemyAi.selectPoolCards(pool, enemy, CombatantState(72, 72), 1)
        assertEquals("pulse", selected.single().definitionId)
    }

    @Test
    fun deckScreenNavigationDoesNotChangeRulesState() {
        val run = newRun()
        val result = reduce(run, Action.Navigate(Screen.DECK))
        assertEquals(Screen.DECK, result.screen)
        assertEquals(run.playerDeck, result.playerDeck)
    }

    @Test
    fun abandoningActiveRunOpensResult() {
        val result = reduce(newRun(), Action.AbandonRun)
        assertEquals(RunStatus.ABANDONED, result.runStatus)
        assertEquals(Screen.RESULT, result.screen)
    }

    @Test
    fun eachActionReplacesTransientEventList() {
        val invalid = reduce(newRun(), Action.PlayCard(-1L))
        val navigated = reduce(invalid, Action.Navigate(Screen.DECK))
        assertTrue(navigated.events.isEmpty())
    }

    private fun newRun(
        heroClass: HeroClass = HeroClass.CONDUCTOR,
        ascension: Int = 0,
    ): GameState = reduce(GameState(), Action.StartRun(heroClass, 1L, ascension))

    private fun play(cardId: String, upgraded: Boolean = false): GameState {
        val (state, card) = stateWithCard(cardId, upgraded)
        return reduce(state, Action.PlayCard(card.instanceId))
    }

    private fun enemyHpAfter(cardId: String, upgraded: Boolean = false): Int =
        requireNotNull(play(cardId, upgraded).enemy).actor.hp

    private fun poolCards(count: Int): List<CardInstance> {
        val ids = listOf("pulse", "triage", "surge", "seizure", "grounding", "overclock")
        return ids.take(count).mapIndexed { index, id -> CardInstance(100L + index, id) }
    }

    private fun killTrainingEnemy(): GameState {
        val (state, card) = stateWithCard("strike", upgraded = true)
        val enemy = requireNotNull(state.enemy)
        val fragile = state.copy(enemy = enemy.copy(actor = CombatantState(1, 38)))
        return reduce(fragile, Action.PlayCard(card.instanceId))
    }
}
