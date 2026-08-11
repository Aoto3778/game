package jp.aoto.zerosum.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.MapNode
import jp.aoto.zerosum.core.model.MapNodeType
import jp.aoto.zerosum.core.model.Screen
import jp.aoto.zerosum.ui.NeonButton
import jp.aoto.zerosum.ui.Palette
import jp.aoto.zerosum.ui.RunHud
import androidx.compose.ui.res.stringResource
import jp.aoto.zerosum.R

/** Branching act map with procedural paths and tappable nodes. */
@Composable
public fun MapScreen(state: GameState, dispatch: (Action) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        RunHud(state)
        Text(
            stringResource(R.string.grid_title, state.enemyPool.size),
            Modifier.fillMaxWidth().padding(12.dp),
            color = Palette.Text,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        MapCanvas(state, Modifier.fillMaxWidth().weight(1f)) { dispatch(Action.SelectMapNode(it)) }
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NeonButton(stringResource(R.string.deck), Modifier.weight(1f), accent = Palette.Green) { dispatch(Action.Navigate(Screen.DECK)) }
            NeonButton(stringResource(R.string.settings), Modifier.weight(1f), accent = Palette.Muted) { dispatch(Action.Navigate(Screen.SETTINGS)) }
        }
    }
}

@Composable
private fun MapCanvas(state: GameState, modifier: Modifier, onSelect: (String) -> Unit) {
    Canvas(modifier.pointerInput(state.availableNodeIds, state.mapNodes) {
        detectTapGestures { tap ->
            state.mapNodes
                .filter { it.id in state.availableNodeIds }
                .minByOrNull { node -> (position(node, size.width.toFloat(), size.height.toFloat()) - tap).getDistanceSquared() }
                ?.takeIf { (position(it, size.width.toFloat(), size.height.toFloat()) - tap).getDistance() < 52f }
                ?.let { onSelect(it.id) }
        }
    }) {
        state.mapNodes.forEach { node ->
            val start = position(node, size.width, size.height)
            node.connections.forEach { targetId ->
                state.mapNodes.firstOrNull { it.id == targetId }?.let { target ->
                    drawLine(Palette.Muted.copy(alpha = .35f), start, position(target, size.width, size.height), 4f)
                }
            }
        }
        state.mapNodes.forEach { node ->
            val center = position(node, size.width, size.height)
            val available = node.id in state.availableNodeIds
            val color = nodeColor(node.type)
            drawCircle(if (available) color.copy(alpha = .22f) else Palette.Surface, if (available) 35f else 28f, center)
            drawCircle(if (available) color else Palette.Muted.copy(alpha = .45f), if (available) 35f else 28f, center, style = Stroke(if (available) 5f else 2f))
            when (node.type) {
                MapNodeType.NORMAL -> drawLine(color, center - Offset(10f, 0f), center + Offset(10f, 0f), 4f)
                MapNodeType.ELITE -> {
                    drawLine(color, center - Offset(12f, 10f), center + Offset(12f, 10f), 4f)
                    drawLine(color, center - Offset(12f, -10f), center + Offset(12f, -10f), 4f)
                }
                MapNodeType.EVENT -> drawCircle(color, 5f, center)
                MapNodeType.REST -> drawArc(color, 200f, 280f, false, center - Offset(13f, 13f), androidx.compose.ui.geometry.Size(26f, 26f), style = Stroke(4f))
                MapNodeType.MERCHANT -> drawRect(color, center - Offset(9f, 9f), androidx.compose.ui.geometry.Size(18f, 18f), style = Stroke(4f))
                MapNodeType.BOSS -> drawCircle(color, 13f, center, style = Stroke(5f))
            }
        }
    }
}

private fun position(node: MapNode, width: Float, height: Float): Offset {
    val laneCount = if (node.row == 0) 2 else if (node.type == MapNodeType.BOSS) 1 else 3
    val x = width * (node.lane + 1f) / (laneCount + 1f)
    val maxRow = if (node.id.startsWith("a3_")) 5f else 4f
    return Offset(x, height * (1f - (node.row + .5f) / (maxRow + 1f)))
}

private fun nodeColor(type: MapNodeType): Color = when (type) {
    MapNodeType.NORMAL -> Palette.Cyan
    MapNodeType.ELITE -> Palette.Magenta
    MapNodeType.EVENT -> Palette.Amber
    MapNodeType.REST -> Palette.Green
    MapNodeType.MERCHANT -> Palette.Amber
    MapNodeType.BOSS -> Palette.Red
}
