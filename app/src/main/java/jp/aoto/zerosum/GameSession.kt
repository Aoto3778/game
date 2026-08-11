package jp.aoto.zerosum

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import jp.aoto.zerosum.core.engine.reduce
import jp.aoto.zerosum.core.model.Action
import jp.aoto.zerosum.core.model.GameEventKind
import jp.aoto.zerosum.core.model.GameState
import kotlinx.coroutines.delay

/** UI state holder whose only game mutation is the core reducer. */
public class GameSession(initial: GameState = GameState()) {
    public var state: GameState by mutableStateOf(initial)
        private set

    /** Dispatches an input and preserves an 80ms hit-stop on critical damage. */
    public suspend fun dispatch(action: Action) {
        val next = reduce(state, action)
        val critical = next.events.any { it.kind == GameEventKind.DAMAGE && it.amount >= 20 }
        if (critical) delay(80)
        state = next
    }
}
