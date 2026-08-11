package jp.aoto.zerosum.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.RunStatus
import jp.aoto.zerosum.core.model.Screen
import jp.aoto.zerosum.ui.NeonButton
import jp.aoto.zerosum.ui.Palette

/** Local presentation settings; persistence is added in Phase 5. */
@Composable
public fun SettingsScreen(state: GameState, dispatch: (Action) -> Unit) {
    var sound by remember { mutableStateOf(true) }
    var haptics by remember { mutableStateOf(true) }
    var reducedMotion by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(22.dp)) {
        Canvas(Modifier.fillMaxWidth().height(160.dp)) {
            repeat(12) { index ->
                val x = size.width * index / 11f
                val y = size.height * (.5f + kotlin.math.sin(index.toFloat()) * .18f)
                drawCircle(if (index % 2 == 0) Palette.Cyan else Palette.Magenta, 7f, Offset(x, y))
                if (index > 0) drawLine(Palette.Muted.copy(alpha = .3f), Offset(size.width * (index - 1) / 11f, size.height * .5f), Offset(x, y), 2f)
            }
        }
        Text("SETTINGS", color = Palette.Text, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Text("Dark theme is fixed. The grid works offline.", color = Palette.Muted)
        Spacer(Modifier.height(30.dp))
        Toggle("SYNTH SOUND", "Realtime waveforms only", sound) { sound = it }
        Toggle("HAPTICS", "Critical hits and selections", haptics) { haptics = it }
        Toggle("REDUCED MOTION", "Shorten screen shake and trails", reducedMotion) { reducedMotion = it }
        Spacer(Modifier.weight(1f))
        if (state.runStatus == RunStatus.ACTIVE) {
            NeonButton("ABANDON RUN", Modifier.fillMaxWidth(), accent = Palette.Red) { dispatch(Action.AbandonRun) }
            Spacer(Modifier.height(10.dp))
        }
        NeonButton("BACK", Modifier.fillMaxWidth()) {
            dispatch(Action.Navigate(if (state.runStatus == RunStatus.NOT_STARTED) Screen.TITLE else if (state.enemy == null) Screen.MAP else Screen.COMBAT))
        }
    }
}

@Composable
private fun Toggle(title: String, subtitle: String, checked: Boolean, change: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Palette.Text, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Palette.Muted, fontSize = 12.sp)
        }
        Switch(checked = checked, onCheckedChange = change)
    }
}
