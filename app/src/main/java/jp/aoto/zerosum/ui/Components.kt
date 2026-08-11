package jp.aoto.zerosum.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import jp.aoto.zerosum.R
import jp.aoto.zerosum.core.content.GameCatalog
import jp.aoto.zerosum.core.model.CardClass
import jp.aoto.zerosum.core.model.CardInstance
import jp.aoto.zerosum.core.model.Effect
import jp.aoto.zerosum.core.model.EffectKind
import jp.aoto.zerosum.core.model.GameState
import kotlin.math.roundToInt

/** Compact top status strip; navigation remains in the reachable lower area. */
@Composable
public fun RunHud(state: GameState) {
    Row(
        Modifier.fillMaxWidth().background(Palette.Surface).padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(stringResource(R.string.run_hud, state.act, state.nodeIndex), color = Palette.Cyan, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.hp_hud, state.player.hp, state.player.maxHp), color = Palette.Red)
        Text(stringResource(R.string.gold_hud, state.gold), color = Palette.Amber)
    }
}

/** Large reusable action surface. */
@Composable
public fun NeonButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Color = Palette.Cyan,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .height(52.dp)
            .background(if (enabled) Palette.Surface else Palette.Surface.copy(alpha = .45f), RoundedCornerShape(14.dp))
            .border(1.dp, if (enabled) accent else Palette.Muted, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (enabled) Palette.Text else Palette.Muted, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

/** Canvas-backed card that can optionally be dragged upward to play. */
@Composable
public fun ProceduralCard(
    card: CardInstance,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    onDragChanged: ((Boolean) -> Unit)? = null,
    onPlay: (() -> Unit)? = null,
) {
    val definition = GameCatalog.card(card.definitionId)
    var drag by remember(card.instanceId) { mutableStateOf(Offset.Zero) }
    val color = when (definition.cardClass) {
        CardClass.COMMON -> Palette.Cyan
        CardClass.CONDUCTOR -> Palette.Amber
        CardClass.BREAKER -> Palette.Magenta
        CardClass.RESOLVER -> Palette.Green
        CardClass.ENEMY -> Palette.Red
    }
    val dragModifier = if (onPlay == null) Modifier else Modifier.pointerInput(card.instanceId, enabled) {
        detectDragGestures(
            onDragStart = { if (enabled) onDragChanged?.invoke(true) },
            onDragCancel = { drag = Offset.Zero; onDragChanged?.invoke(false) },
            onDragEnd = {
                val shouldPlay = enabled && drag.y < -size.height * .45f
                drag = Offset.Zero
                onDragChanged?.invoke(false)
                if (shouldPlay) onPlay?.invoke()
            },
        ) { change, amount ->
            if (enabled) {
                change.consume()
                drag += amount
            }
        }
    }
    Box(
        modifier
            .offset { IntOffset(drag.x.roundToInt(), drag.y.roundToInt()) }
            .then(dragModifier)
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier),
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawRoundRect(Palette.Surface, cornerRadius = CornerRadius(18f, 18f))
            drawRoundRect(color.copy(alpha = if (enabled) .95f else .35f), style = Stroke(3f), cornerRadius = CornerRadius(18f, 18f))
            val path = Path().apply {
                moveTo(size.width * .12f, size.height * .35f)
                lineTo(size.width * .88f, size.height * .18f)
                lineTo(size.width * .72f, size.height * .48f)
                lineTo(size.width * .28f, size.height * .62f)
                close()
            }
            drawPath(path, color.copy(alpha = .13f))
            repeat(3) { index ->
                drawCircle(color.copy(alpha = .25f), 3f + index * 2f, Offset(size.width * (.2f + index * .3f), size.height * .78f))
            }
        }
        Column(Modifier.padding(if (compact) 7.dp else 10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (card.upgraded) "+ ${pretty(card.definitionId)}" else pretty(card.definitionId), color = Palette.Text, fontSize = if (compact) 10.sp else 13.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                Text((if (card.upgraded) definition.upgradedCost else definition.cost).toString(), color = color, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.weight(1f))
            Text(effectSummary(if (card.upgraded) definition.upgradedEffects else definition.effects), color = Palette.Text.copy(alpha = .9f), fontSize = if (compact) 8.sp else 10.sp, lineHeight = if (compact) 9.sp else 12.sp, maxLines = 4)
        }
        if (card.faceDown) Box(Modifier.matchParentSize().background(Palette.Background.copy(alpha = .9f), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
            Text("?", color = Palette.Magenta, fontSize = 32.sp, fontWeight = FontWeight.Black)
        }
    }
}

/** Converts stable content IDs to compact fallback labels without game logic. */
public fun pretty(id: String): String = id.split('_').joinToString(" ") { word ->
    word.replaceFirstChar { it.uppercase() }
}

private fun effectSummary(effects: List<Effect>): String = effects.joinToString(" • ") { effect ->
    val verb = when (effect.kind) {
        EffectKind.DAMAGE -> "DMG"
        EffectKind.BLOCK -> "BLOCK"
        EffectKind.DRAW -> "DRAW"
        EffectKind.APPLY_STATUS -> effect.status?.name ?: "STATUS"
        EffectKind.GAIN_ENERGY -> "ENERGY"
        EffectKind.HEAL -> "HEAL"
        EffectKind.SEIZE -> "SEIZE"
        EffectKind.DISCARD -> "DISCARD"
        EffectKind.RETURN_DISCARD -> "RETURN"
        EffectKind.COPY_LAST -> "COPY"
        EffectKind.GAIN_MAX_HP -> "MAX HP"
    }
    "$verb ${effect.amount}${if (effect.hits > 1) "×${effect.hits}" else ""}"
}
