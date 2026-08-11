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
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.HeroClass
import jp.aoto.zerosum.core.model.Screen
import jp.aoto.zerosum.ui.NeonButton
import jp.aoto.zerosum.ui.Palette

/** Title and class selection screen. */
@Composable
public fun TitleScreen(dispatch: (Action) -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            repeat(9) { index ->
                val radius = size.minDimension * (.08f + index * .045f)
                drawCircle(Palette.Cyan.copy(alpha = .04f + index * .006f), radius, Offset(size.width * .5f, size.height * .21f), style = Stroke(2f))
            }
            drawLine(Palette.Magenta.copy(alpha = .35f), Offset(0f, size.height * .42f), Offset(size.width, size.height * .35f), 3f)
        }
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(76.dp))
            Text("ZERO SUM", color = Palette.Cyan, fontSize = 42.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
            Text("SEIZE THE CARD. DENY THE ENEMY.", color = Palette.Muted, textAlign = TextAlign.Center, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text("CHOOSE YOUR CIRCUIT", color = Palette.Text, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroClass.entries.forEach { hero ->
                    NeonButton(
                        text = when (hero) {
                            HeroClass.CONDUCTOR -> "CONDUCTOR  •  Shock & tempo"
                            HeroClass.BREAKER -> "BREAKER  •  Block & rupture"
                            HeroClass.RESOLVER -> "RESOLVER  •  Recursion & control"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        accent = when (hero) {
                            HeroClass.CONDUCTOR -> Palette.Amber
                            HeroClass.BREAKER -> Palette.Magenta
                            HeroClass.RESOLVER -> Palette.Green
                        },
                    ) { dispatch(Action.StartRun(hero, System.currentTimeMillis())) }
                }
            }
            Spacer(Modifier.height(16.dp))
            NeonButton("SETTINGS", Modifier.fillMaxWidth(), accent = Palette.Muted) { dispatch(Action.Navigate(Screen.SETTINGS)) }
            Spacer(Modifier.height(22.dp))
        }
    }
}
