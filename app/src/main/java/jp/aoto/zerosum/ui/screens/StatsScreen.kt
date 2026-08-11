package jp.aoto.zerosum.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.Screen
import jp.aoto.zerosum.core.progress.AchievementCatalog
import jp.aoto.zerosum.core.progress.LifetimeStats
import jp.aoto.zerosum.ui.NeonButton
import jp.aoto.zerosum.ui.Palette
import jp.aoto.zerosum.ui.pretty
import androidx.compose.ui.res.stringResource
import jp.aoto.zerosum.R

/** Lifetime statistics and the complete thirty-achievement list. */
@Composable
public fun StatsScreen(state: GameState, stats: LifetimeStats, dispatch: (Action) -> Unit) {
    val unlocked = AchievementCatalog.unlocked(stats)
    Column(Modifier.fillMaxSize()) {
        Text(stringResource(R.string.statistics), Modifier.padding(18.dp), color = Palette.Text, fontWeight = FontWeight.Black)
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Metric("RUNS", stats.runs)
            Metric("WINS", stats.wins)
            Metric("TURNS", stats.totalTurns)
            Metric("DAMAGE", stats.damageDealt)
        }
        Text(stringResource(R.string.achievements, unlocked.size), Modifier.padding(18.dp), color = Palette.Cyan, fontWeight = FontWeight.Bold)
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(AchievementCatalog.all(), key = { it.id }) { achievement ->
                val earned = achievement.id in unlocked
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp).background(if (earned) Palette.Green.copy(alpha = .12f) else Palette.Surface).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(if (earned) "◆ ${pretty(achievement.id)}" else "◇ ${pretty(achievement.id)}", color = if (earned) Palette.Green else Palette.Muted)
                    Text(stringResource(if (earned) R.string.unlocked else R.string.locked), color = if (earned) Palette.Green else Palette.Muted)
                }
            }
        }
        NeonButton(stringResource(R.string.back), Modifier.fillMaxWidth().padding(12.dp)) { dispatch(Action.Navigate(if (state.runStatus.name == "NOT_STARTED") Screen.TITLE else if (state.enemy == null) Screen.MAP else Screen.COMBAT)) }
    }
}

@Composable
private fun Metric(label: String, value: Int) {
    Column {
        Text(value.toString(), color = Palette.Text, fontWeight = FontWeight.Black)
        Text(label, color = Palette.Muted)
    }
}
