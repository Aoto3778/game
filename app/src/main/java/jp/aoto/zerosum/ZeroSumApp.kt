package jp.aoto.zerosum

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.Screen
import jp.aoto.zerosum.ui.AppTheme
import jp.aoto.zerosum.ui.Palette
import jp.aoto.zerosum.ui.screens.CombatScreen
import jp.aoto.zerosum.ui.screens.DeckScreen
import jp.aoto.zerosum.ui.screens.DraftScreen
import jp.aoto.zerosum.ui.screens.EventScreen
import jp.aoto.zerosum.ui.screens.MapScreen
import jp.aoto.zerosum.ui.screens.ResultScreen
import jp.aoto.zerosum.ui.screens.SettingsScreen
import jp.aoto.zerosum.ui.screens.TitleScreen
import kotlinx.coroutines.launch

/** Root renderer. Every user input is translated into one core action. */
@Composable
public fun ZeroSumApp() {
    val session = remember { GameSession() }
    val scope = rememberCoroutineScope()
    val dispatch: (Action) -> Unit = { action -> scope.launch { session.dispatch(action) } }
    AppTheme {
        Box(Modifier.fillMaxSize().background(Palette.Background)) {
            when (session.state.screen) {
                Screen.TITLE -> TitleScreen(dispatch)
                Screen.MAP -> MapScreen(session.state, dispatch)
                Screen.COMBAT -> CombatScreen(session.state, dispatch)
                Screen.DRAFT -> DraftScreen(session.state, dispatch)
                Screen.DECK -> DeckScreen(session.state, dispatch)
                Screen.EVENT -> EventScreen(session.state, dispatch)
                Screen.RESULT -> ResultScreen(session.state, dispatch)
                Screen.SETTINGS, Screen.STATS -> SettingsScreen(session.state, dispatch)
            }
        }
    }
}
