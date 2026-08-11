package jp.aoto.zerosum.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import jp.aoto.zerosum.core.progress.DailyChallenge
import java.time.LocalDate
import androidx.compose.ui.res.stringResource
import jp.aoto.zerosum.R

/** Title and class selection screen. */
@Composable
public fun TitleScreen(dispatch: (Action) -> Unit) {
    var ascension by remember { mutableIntStateOf(0) }
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
            Text(stringResource(R.string.app_name), color = Palette.Cyan, fontSize = 42.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
            Text(stringResource(R.string.tagline), color = Palette.Muted, textAlign = TextAlign.Center, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.choose_circuit), color = Palette.Text, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NeonButton("−", Modifier.weight(1f), enabled = ascension > 0) { ascension-- }
                Box(Modifier.weight(2f).height(52.dp), contentAlignment = Alignment.Center) { Text(stringResource(R.string.ascension, ascension), color = Palette.Amber, fontWeight = FontWeight.Bold) }
                NeonButton("+", Modifier.weight(1f), enabled = ascension < 20) { ascension++ }
            }
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroClass.entries.forEach { hero ->
                    NeonButton(
                        text = when (hero) {
                            HeroClass.CONDUCTOR -> stringResource(R.string.hero_conductor)
                            HeroClass.BREAKER -> stringResource(R.string.hero_breaker)
                            HeroClass.RESOLVER -> stringResource(R.string.hero_resolver)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        accent = when (hero) {
                            HeroClass.CONDUCTOR -> Palette.Amber
                            HeroClass.BREAKER -> Palette.Magenta
                            HeroClass.RESOLVER -> Palette.Green
                        },
                    ) { dispatch(Action.StartRun(hero, System.currentTimeMillis(), ascension)) }
                }
            }
            Spacer(Modifier.height(10.dp))
            NeonButton(stringResource(R.string.daily_challenge), Modifier.fillMaxWidth(), accent = Palette.Amber) {
                dispatch(Action.StartRun(HeroClass.CONDUCTOR, DailyChallenge.seed(LocalDate.now().toString()), 0, dailyChallenge = true))
            }
            Spacer(Modifier.height(16.dp))
            NeonButton(stringResource(R.string.settings), Modifier.fillMaxWidth(), accent = Palette.Muted) { dispatch(Action.Navigate(Screen.SETTINGS)) }
            Spacer(Modifier.height(22.dp))
        }
    }
}
