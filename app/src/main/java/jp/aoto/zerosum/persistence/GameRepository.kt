package jp.aoto.zerosum.persistence

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import jp.aoto.zerosum.core.model.GameState
import jp.aoto.zerosum.core.model.RunStatus
import jp.aoto.zerosum.core.progress.LifetimeStats
import jp.aoto.zerosum.core.save.GameStateJson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "zero_sum")

/** Persisted presentation switches. */
public data class AppSettings(
    val sound: Boolean = true,
    val haptics: Boolean = true,
    val reducedMotion: Boolean = false,
)

/** DataStore adapter for resumable state, settings, and lifetime progression. */
public class GameRepository(private val context: Context) {
    private object Keys {
        val save = stringPreferencesKey("active_game_json")
        val stats = stringPreferencesKey("lifetime_stats_json")
        val recordedRun = stringPreferencesKey("recorded_run_key")
        val sound = booleanPreferencesKey("sound")
        val haptics = booleanPreferencesKey("haptics")
        val reducedMotion = booleanPreferencesKey("reduced_motion")
    }

    /** Live settings with safe defaults. */
    public val settings: Flow<AppSettings> = context.dataStore.data.safe().map { prefs ->
        AppSettings(
            sound = prefs[Keys.sound] ?: true,
            haptics = prefs[Keys.haptics] ?: true,
            reducedMotion = prefs[Keys.reducedMotion] ?: false,
        )
    }

    /** Live lifetime totals. */
    public val stats: Flow<LifetimeStats> = context.dataStore.data.safe().map { prefs ->
        prefs[Keys.stats]?.let(::decodeStatsSafe) ?: LifetimeStats()
    }

    /** Returns an active serialized run, including mid-combat state. */
    public suspend fun loadActiveRun(): GameState? = context.dataStore.data.first()[Keys.save]
        ?.let(::decodeStateSafe)
        ?.takeIf { it.runStatus == RunStatus.ACTIVE }

    /** Stores every active reducer state so process death loses no turn. */
    public suspend fun save(state: GameState) {
        context.dataStore.edit { prefs ->
            if (state.runStatus == RunStatus.ACTIVE) prefs[Keys.save] = GameStateJson.encode(state)
            else prefs.remove(Keys.save)
        }
    }

    /** Records a terminal run once even across recomposition or process restart. */
    public suspend fun recordTerminal(state: GameState) {
        if (state.runStatus == RunStatus.ACTIVE || state.runStatus == RunStatus.NOT_STARTED) return
        val key = "${state.seed}:${state.runStatus}:${state.stats.turns}:${state.stats.combatsWon}"
        context.dataStore.edit { prefs ->
            if (prefs[Keys.recordedRun] != key) {
                val old = prefs[Keys.stats]?.let(::decodeStatsSafe) ?: LifetimeStats()
                prefs[Keys.stats] = GameStateJson.encodeStats(old.record(state, state.dailyChallenge))
                prefs[Keys.recordedRun] = key
            }
            prefs.remove(Keys.save)
        }
    }

    /** Persists all settings atomically. */
    public suspend fun setSettings(value: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.sound] = value.sound
            prefs[Keys.haptics] = value.haptics
            prefs[Keys.reducedMotion] = value.reducedMotion
        }
    }

    private fun decodeStateSafe(value: String): GameState? = runCatching { GameStateJson.decode(value) }.getOrNull()
    private fun decodeStatsSafe(value: String): LifetimeStats = runCatching { GameStateJson.decodeStats(value) }.getOrDefault(LifetimeStats())
    private fun Flow<androidx.datastore.preferences.core.Preferences>.safe(): Flow<androidx.datastore.preferences.core.Preferences> =
        catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
}
