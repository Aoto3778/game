package jp.aoto.zerosum.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.aoto.zerosum.core.content.GameCatalog
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.CardInstance
import jp.aoto.zerosum.core.model.GameEventKind
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.ui.NeonButton
import jp.aoto.zerosum.ui.Palette
import jp.aoto.zerosum.ui.ProceduralCard
import jp.aoto.zerosum.ui.RunHud
import jp.aoto.zerosum.ui.pretty
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource
import jp.aoto.zerosum.R

/** Combat screen with drag-to-target play, damage pop, shake, and discard trail. */
@Composable
public fun CombatScreen(state: GameState, dispatch: (Action) -> Unit, reducedMotion: Boolean = false) {
    val shake = remember { Animatable(0f) }
    val popAlpha = remember { Animatable(0f) }
    val popY = remember { Animatable(0f) }
    var dragging by remember { mutableStateOf(false) }
    var popText by remember { mutableStateOf("") }
    var discarded by remember { mutableStateOf<CardInstance?>(null) }
    var tutorialStep by remember(state.seed) { mutableIntStateOf(if (state.stats.combatsWon == 0 && state.turn == 1) 0 else -1) }
    val discardAlpha = remember { Animatable(0f) }
    LaunchedEffect(state.events) {
        val damage = state.events.lastOrNull { it.kind == GameEventKind.DAMAGE }
        if (damage != null) {
            popText = "-${damage.amount}"
            popAlpha.snapTo(1f)
            popY.snapTo(0f)
            popY.animateTo(-54f, tween(420))
            popAlpha.animateTo(0f, tween(180))
            if (damage.side?.name == "PLAYER") {
                repeat(if (reducedMotion) 1 else 3) {
                    shake.animateTo(12f, tween(28))
                    shake.animateTo(-12f, tween(28))
                }
                shake.animateTo(0f, tween(35))
            }
        }
        state.events.lastOrNull { it.kind == GameEventKind.CARD_PLAYED }?.sourceId?.let { definitionId ->
            discarded = CardInstance(-1L, definitionId)
            discardAlpha.snapTo(1f)
            discardAlpha.animateTo(0f, tween(280))
            delay(40)
            discarded = null
        }
    }
    Column(
        Modifier.fillMaxSize().graphicsLayer { translationX = shake.value },
    ) {
        RunHud(state)
        EnemyPanel(state, dragging, popText, popAlpha.value, popY.value)
        CombatReadout(state)
        Spacer(Modifier.weight(1f))
        HandRow(state, onDragging = { dragging = it }, dispatch = dispatch)
        Row(
            Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.energy, state.energy), Modifier.weight(.8f).align(Alignment.CenterVertically), color = Palette.Amber, fontSize = 18.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            NeonButton(stringResource(R.string.end_turn), Modifier.weight(1.2f), accent = Palette.Magenta) { dispatch(Action.EndTurn) }
        }
    }
    discarded?.let { card ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
            ProceduralCard(card, Modifier.padding(24.dp).size(74.dp, 104.dp).alpha(discardAlpha.value).graphicsLayer { rotationZ = 18f * (1f - discardAlpha.value) }, compact = true)
        }
    }
    if (tutorialStep >= 0) TutorialOverlay(tutorialStep) {
        tutorialStep = if (tutorialStep >= 2) -1 else tutorialStep + 1
    }
}

@Composable
private fun TutorialOverlay(step: Int, next: () -> Unit) {
    val copy = when (step) {
        0 -> stringResource(R.string.guide_drag)
        1 -> stringResource(R.string.guide_intent)
        else -> stringResource(R.string.guide_draft)
    }
    Box(
        Modifier.fillMaxSize().background(Palette.Background.copy(alpha = .76f)).padding(24.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(Modifier.fillMaxWidth().background(Palette.Surface).padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.quick_guide, step + 1), color = Palette.Cyan, fontWeight = FontWeight.Black)
            Text(copy, Modifier.padding(vertical = 14.dp), color = Palette.Text, textAlign = TextAlign.Center)
            NeonButton(stringResource(if (step == 2) R.string.play else R.string.next), Modifier.fillMaxWidth(), onClick = next)
        }
    }
}

@Composable
private fun EnemyPanel(state: GameState, targeted: Boolean, popText: String, popAlpha: Float, popY: Float) {
    val enemy = state.enemy ?: return
    val definition = GameCatalog.enemy(enemy.definitionId)
    Box(
        Modifier.fillMaxWidth().height(230.dp).background(if (targeted) Palette.Red.copy(alpha = .09f) else Palette.Background),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width * .5f, size.height * .52f)
            repeat(4) { ring -> drawCircle(Palette.Red.copy(alpha = .08f + ring * .025f), 42f + ring * 22f, center) }
            drawLine(Palette.Red, center - Offset(50f, 0f), center + Offset(50f, 0f), 5f)
            drawLine(Palette.Magenta, center - Offset(0f, 42f), center + Offset(0f, 42f), 5f)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(pretty(definition.id), color = Palette.Text, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text("${enemy.actor.hp}/${enemy.actor.maxHp} HP  •  ${enemy.actor.block} BLOCK", color = Palette.Red, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.intent, pretty(enemy.intent.definitionId)), color = Palette.Amber, fontSize = 12.sp)
        }
        if (popText.isNotEmpty()) Text(
            popText,
            Modifier.offset(y = popY.dp).alpha(popAlpha),
            color = Palette.Red,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
        )
        if (targeted) Text(stringResource(R.string.release_target), Modifier.align(Alignment.BottomCenter).padding(8.dp), color = Palette.Red, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CombatReadout(state: GameState) {
    Row(
        Modifier.fillMaxWidth().background(Palette.Surface).padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Text("BLOCK ${state.player.block}", color = Palette.Cyan)
        Text("TURN ${state.turn}", color = Palette.Muted)
        val statuses = state.player.statuses.filterValues { it > 0 }.entries.joinToString { "${it.key.name}:${it.value}" }
        Text(if (statuses.isEmpty()) "STABLE" else statuses, color = Palette.Green, fontSize = 11.sp)
    }
}

@Composable
private fun HandRow(state: GameState, onDragging: (Boolean) -> Unit, dispatch: (Action) -> Unit) {
    val scroll = rememberScrollState()
    Row(
        Modifier.fillMaxWidth().height(180.dp).horizontalScroll(scroll).padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        state.hand.forEach { card ->
            val definition = GameCatalog.card(card.definitionId)
            val cost = if (card.upgraded) definition.upgradedCost else definition.cost
            DrawnCard(card, cost <= state.energy && !card.faceDown, onDragging) {
                dispatch(Action.PlayCard(card.instanceId))
            }
        }
    }
}

@Composable
private fun DrawnCard(card: CardInstance, enabled: Boolean, onDragging: (Boolean) -> Unit, onPlay: () -> Unit) {
    val entrance = remember(card.instanceId) { Animatable(.65f) }
    LaunchedEffect(card.instanceId) { entrance.animateTo(1f, tween(220)) }
    ProceduralCard(
        card,
        Modifier.size(112.dp, 158.dp).graphicsLayer { scaleX = entrance.value; scaleY = entrance.value },
        enabled = enabled,
        onDragChanged = onDragging,
        onPlay = onPlay,
    )
}
