package jp.aoto.zerosum.core.save

import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.progress.LifetimeStats
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

    /** Serializes lifetime progression for preference storage. */
    public fun encodeStats(value: LifetimeStats): String = json.encodeToString(value)

    /** Restores lifetime progression from preference storage. */
    public fun decodeStats(encoded: String): LifetimeStats = json.decodeFromString(encoded)
}
