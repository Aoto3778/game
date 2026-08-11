package jp.aoto.zerosum.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import jp.aoto.zerosum.core.model.GameEvent
import jp.aoto.zerosum.core.model.GameEventKind
import jp.aoto.zerosum.persistence.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.abs

/** Generates all feedback procedurally; no audio or image assets are loaded. */
public class FeedbackEngine(private val context: Context) {
    /** Plays one short waveform and haptic pattern for the newest reducer event. */
    public suspend fun emit(event: GameEvent, settings: AppSettings) {
        if (settings.haptics) vibrate(event)
        if (settings.sound) synthesize(event)
    }

    private fun vibrate(event: GameEvent) {
        val duration = when (event.kind) {
            GameEventKind.DAMAGE -> if (event.amount >= 20) 55L else 24L
            GameEventKind.VICTORY -> 70L
            GameEventKind.CARD_PLAYED, GameEventKind.EVENT_RESOLVED -> 12L
            else -> return
        }
        val vibrator = if (Build.VERSION.SDK_INT >= 31) {
            context.getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION") context.getSystemService(Vibrator::class.java)
        }
        vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private suspend fun synthesize(event: GameEvent): Unit = withContext(Dispatchers.Default) {
        val (frequency, duration, triangle) = when (event.kind) {
            GameEventKind.CARD_PLAYED -> Triple(520, 45, false)
            GameEventKind.DAMAGE -> Triple(if (event.amount >= 20) 92 else 150, 70, true)
            GameEventKind.BLOCK -> Triple(260, 42, false)
            GameEventKind.VICTORY -> Triple(760, 130, true)
            GameEventKind.DEFEAT -> Triple(72, 180, false)
            GameEventKind.EVENT_RESOLVED -> Triple(410, 55, true)
            else -> return@withContext
        }
        val sampleRate = 44_100
        val count = sampleRate * duration / 1_000
        val samples = ShortArray(count) { index ->
            val phase = (index * frequency % sampleRate).toFloat() / sampleRate
            val wave = if (triangle) 1f - 4f * abs(phase - .5f) else if (phase < .5f) 1f else -1f
            val envelope = 1f - index.toFloat() / count
            (wave * envelope * Short.MAX_VALUE * .16f).toInt().toShort()
        }
        val track = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(samples.size * 2)
            .build()
        try {
            track.write(samples, 0, samples.size)
            track.play()
            delay(duration.toLong() + 15L)
        } finally {
            track.release()
        }
    }
}
