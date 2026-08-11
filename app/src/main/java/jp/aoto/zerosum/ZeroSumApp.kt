package jp.aoto.zerosum

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
import jp.aoto.zerosum.ui.screens.StatsScreen
import jp.aoto.zerosum.ui.screens.TitleScreen
import kotlinx.coroutines.launch
import jp.aoto.zerosum.feedback.FeedbackEngine
import jp.aoto.zerosum.persistence.AppSettings
import jp.aoto.zerosum.persistence.GameRepository
import jp.aoto.zerosum.core.progress.LifetimeStats

/** Root renderer. Every user input is translated into one core action. */
@Composable
public fun ZeroSumApp() {
    val session = remember { GameSession() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repository = remember(context) { GameRepository(context.applicationContext) }
    val feedback = remember(context) { FeedbackEngine(context.applicationContext) }
    val settings by repository.settings.collectAsState(initial = AppSettings())
    val lifetime by repository.stats.collectAsState(initial = LifetimeStats())
    var persistenceReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        repository.loadActiveRun()?.let(session::restore)
        persistenceReady = true
    }
    LaunchedEffect(session.state.events) {
        session.state.events.lastOrNull()?.let { event ->
            try {
                feedback.emit(event, settings)
            } catch (_: IllegalStateException) {
                // Devices without an available audio sink still remain fully playable.
            }
        }
    }
    val dispatch: (Action) -> Unit = { action ->
        scope.launch {
            session.dispatch(action)
            val snapshot = session.state
            if (persistenceReady) {
                repository.save(snapshot)
                repository.recordTerminal(snapshot)
            }
        }
    }
    AppTheme {
        Box(Modifier.fillMaxSize().background(Palette.Background)) {
            when (session.state.screen) {
                Screen.TITLE -> TitleScreen(dispatch)
                Screen.MAP -> MapScreen(session.state, dispatch)
                Screen.COMBAT -> CombatScreen(session.state, dispatch, settings.reducedMotion)
                Screen.DRAFT -> DraftScreen(session.state, dispatch)
                Screen.DECK -> DeckScreen(session.state, dispatch)
                Screen.EVENT -> EventScreen(session.state, dispatch)
                Screen.RESULT -> ResultScreen(session.state, dispatch)
                Screen.SETTINGS -> SettingsScreen(session.state, settings, dispatch) { value ->
                    scope.launch { repository.setSettings(value) }
                }
                Screen.STATS -> StatsScreen(session.state, lifetime, dispatch)
            }
        }
    }
}
