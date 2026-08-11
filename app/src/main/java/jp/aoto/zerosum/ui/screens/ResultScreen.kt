package jp.aoto.zerosum.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.RunStatus
import jp.aoto.zerosum.ui.NeonButton
import jp.aoto.zerosum.ui.Palette
import androidx.compose.ui.res.stringResource
import jp.aoto.zerosum.R

/** End-of-run summary and immediate replay entrypoint. */
@Composable
public fun ResultScreen(state: GameState, dispatch: (Action) -> Unit) {
    val won = state.runStatus == RunStatus.WON
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Canvas(Modifier.fillMaxWidth().weight(1f)) {
            val center = Offset(size.width / 2f, size.height * .58f)
            repeat(7) { index -> drawCircle((if (won) Palette.Cyan else Palette.Red).copy(alpha = .06f), 30f + index * 23f, center, style = Stroke(5f)) }
            if (!won) {
                drawLine(Palette.Red, center - Offset(80f, 80f), center + Offset(80f, 80f), 8f)
                drawLine(Palette.Red, center - Offset(80f, -80f), center + Offset(80f, -80f), 8f)
            }
        }
        Text(stringResource(if (won) R.string.result_win else R.string.result_loss), Modifier.fillMaxWidth(), color = if (won) Palette.Cyan else Palette.Red, fontSize = 30.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Text(stringResource(if (won) R.string.result_win_copy else R.string.result_loss_copy), Modifier.fillMaxWidth().padding(12.dp), color = Palette.Muted, textAlign = TextAlign.Center)
        Row(Modifier.fillMaxWidth().padding(vertical = 22.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Stat("TURNS", state.stats.turns)
            Stat("DAMAGE", state.stats.damageDealt)
            Stat("DRAFTS", state.stats.draftsCompleted)
            Stat("ACT", state.act)
        }
        NeonButton(stringResource(R.string.run_again), Modifier.fillMaxWidth(), accent = if (won) Palette.Cyan else Palette.Red) {
            dispatch(Action.StartRun(state.heroClass, System.currentTimeMillis(), state.ascension))
        }
        Spacer(Modifier.padding(8.dp))
    }
}

@Composable
private fun Stat(label: String, value: Int) {
    Column {
        Text(value.toString(), color = Palette.Text, fontSize = 22.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Text(label, color = Palette.Muted, fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}
