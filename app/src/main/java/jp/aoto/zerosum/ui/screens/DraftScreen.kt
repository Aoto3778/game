package jp.aoto.zerosum.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jp.aoto.zerosum.core.content.GameCatalog
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.CardInstance
import jp.aoto.zerosum.core.model.EffectKind
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.ui.Palette
import jp.aoto.zerosum.ui.ProceduralCard
import jp.aoto.zerosum.ui.RunHud
import androidx.compose.ui.res.stringResource
import jp.aoto.zerosum.R

/** Five-way zero-sum draft: one card joins the deck and four arm enemies. */
@Composable
public fun DraftScreen(state: GameState, dispatch: (Action) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        RunHud(state)
        Text(stringResource(R.string.draft_title), Modifier.fillMaxWidth().padding(top = 18.dp), color = Palette.Text, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Text(stringResource(R.string.draft_hint), Modifier.fillMaxWidth().padding(6.dp), color = Palette.Muted, textAlign = TextAlign.Center)
        Spacer(Modifier.weight(1f))
        Row(
            Modifier.fillMaxWidth().height(310.dp).horizontalScroll(rememberScrollState()).padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            state.draft.forEach { card -> DraftChoice(card) { dispatch(Action.ChooseDraft(card.instanceId)) } }
        }
        Text(stringResource(R.string.draft_pool, state.enemyPool.size, state.enemyPool.size + 4), Modifier.fillMaxWidth().padding(18.dp), color = Palette.Red, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DraftChoice(card: CardInstance, choose: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ProceduralCard(card, Modifier.size(142.dp, 202.dp), onClick = choose)
        Text(enemyPreview(card), Modifier.padding(top = 8.dp).size(142.dp, 52.dp), color = Palette.Red, textAlign = TextAlign.Center, maxLines = 3)
    }
}

private fun enemyPreview(card: CardInstance): String {
    val effects = GameCatalog.card(card.definitionId).effects
    val danger = when {
        effects.any { it.kind == EffectKind.DAMAGE } -> "Enemy: converts this into pressure"
        effects.any { it.kind == EffectKind.BLOCK } -> "Enemy: extends the fight"
        effects.any { it.kind == EffectKind.APPLY_STATUS } -> "Enemy: disrupts your next turn"
        else -> "Enemy: accelerates its cycle"
    }
    return danger
}
