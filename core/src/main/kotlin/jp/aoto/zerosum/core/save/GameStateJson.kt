package jp.aoto.zerosum.core.save

import jp.aoto.zerosum.core.model.GameState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Versioned, deterministic JSON codec used by Android DataStore. */
public object GameStateJson {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = false
        prettyPrint = false
    }

    /** Encodes a complete resumable state. */
    public fun encode(state: GameState): String = json.encodeToString(state)

    /** Decodes a previously encoded complete state. */
    public fun decode(encoded: String): GameState = json.decodeFromString(encoded)
}
