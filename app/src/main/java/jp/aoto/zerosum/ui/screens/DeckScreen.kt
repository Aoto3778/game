package jp.aoto.zerosum.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.Screen
import jp.aoto.zerosum.ui.NeonButton
import jp.aoto.zerosum.ui.Palette
import jp.aoto.zerosum.ui.ProceduralCard
import jp.aoto.zerosum.ui.RunHud
import androidx.compose.ui.res.stringResource
import jp.aoto.zerosum.R

/** Read-only deck and denied-card pool browser. */
@Composable
public fun DeckScreen(state: GameState, dispatch: (Action) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        RunHud(state)
        Text(stringResource(R.string.your_deck, state.playerDeck.size), Modifier.padding(12.dp), color = Palette.Green, fontWeight = FontWeight.Black)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.playerDeck, key = { it.instanceId }) { card ->
                ProceduralCard(card, Modifier.size(104.dp, 146.dp), compact = true)
            }
        }
        Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NeonButton(stringResource(R.string.enemy_pool, state.enemyPool.size), Modifier.weight(1f), accent = Palette.Red) {}
            NeonButton(stringResource(R.string.back), Modifier.weight(1f)) { dispatch(Action.Navigate(if (state.enemy == null) Screen.MAP else Screen.COMBAT)) }
        }
    }
}
