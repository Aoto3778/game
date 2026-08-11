package jp.aoto.zerosum.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.aoto.zerosum.core.content.GameCatalog
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.EventChoice
import jp.aoto.zerosum.core.model.EventOutcomeKind
import jp.aoto.zerosum.core.model.EventRequirement
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.ui.NeonButton
import jp.aoto.zerosum.ui.Palette
import jp.aoto.zerosum.ui.RunHud
import jp.aoto.zerosum.ui.pretty
import androidx.compose.ui.res.stringResource
import jp.aoto.zerosum.R

/** Branching event screen with explicit costs and consequences. */
@Composable
public fun EventScreen(state: GameState, dispatch: (Action) -> Unit) {
    val event = state.currentEventId?.let(GameCatalog::event) ?: return
    Column(Modifier.fillMaxSize()) {
        RunHud(state)
        Box(Modifier.fillMaxWidth().height(230.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                repeat(6) { drawCircle(Palette.Amber.copy(alpha = .05f), 28f + it * 18f, center, style = Stroke(3f)) }
                drawLine(Palette.Amber, center - Offset(70f, 0f), center + Offset(70f, 0f), 4f)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(pretty(event.id), color = Palette.Amber, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(stringResource(R.string.every_branch_price), color = Palette.Muted)
            }
        }
        Spacer(Modifier.weight(1f))
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            event.choices.forEach { choice ->
                NeonButton(
                    text = "${pretty(choice.id)}\n${outcomeText(choice)}",
                    modifier = Modifier.fillMaxWidth().height(68.dp),
                    enabled = requirementMet(state, choice),
                    accent = Palette.Amber,
                ) { dispatch(Action.ChooseEvent(choice.id)) }
            }
        }
        Text(stringResource(R.string.event_warning), Modifier.fillMaxWidth().padding(18.dp), color = Palette.Muted, textAlign = TextAlign.Center)
    }
}

private fun requirementMet(state: GameState, choice: EventChoice): Boolean = when (choice.requirement) {
    EventRequirement.NONE -> true
    EventRequirement.MIN_HP -> state.player.hp >= choice.requirementAmount
    EventRequirement.MIN_GOLD -> state.gold >= choice.requirementAmount
    EventRequirement.MIN_POOL -> state.enemyPool.size >= choice.requirementAmount
    EventRequirement.MAX_POOL -> state.enemyPool.size <= choice.requirementAmount
}

private fun outcomeText(choice: EventChoice): String = choice.outcomes.joinToString("  •  ") { outcome ->
    when (outcome.kind) {
        EventOutcomeKind.HP_DELTA -> "HP ${signed(outcome.amount)}"
        EventOutcomeKind.MAX_HP_DELTA -> "MAX HP ${signed(outcome.amount)}"
        EventOutcomeKind.GOLD_DELTA -> "GOLD ${signed(outcome.amount)}"
        EventOutcomeKind.UPGRADE_RANDOM_CARD -> "UPGRADE ${outcome.amount}"
        EventOutcomeKind.REMOVE_RANDOM_CARD -> "REMOVE ${outcome.amount}"
        EventOutcomeKind.ADD_CARD -> "GAIN ${outcome.contentId?.let(::pretty)}"
        EventOutcomeKind.BURN_POOL -> "BURN ${outcome.amount} ENEMY CARDS"
        EventOutcomeKind.ADD_RELIC -> "RELIC ${outcome.contentId?.let(::pretty)}"
        EventOutcomeKind.NOTHING -> "LEAVE"
    }
}

private fun signed(value: Int): String = if (value >= 0) "+$value" else value.toString()
