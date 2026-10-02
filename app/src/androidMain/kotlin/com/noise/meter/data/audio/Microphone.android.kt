package com.noise.meter.data.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.math.log10
import kotlin.math.sqrt

actual fun microphone(): AudioSource = AndroidMicrophone()

private class AndroidMicrophone : AudioSource {
    /** Android asks manufacturers for 90 dB SPL to read about -22 dBFS on this audio source. */
    override val fullScaleDecibels = 112f

    override fun levels(): Flow<Float> = flow {
        val samples = ShortArray(SamplesPerReading)
        while (currentCoroutineContext().isActive) {
            val recorder = open()
            try {
                recorder.startRecording()
                // A working microphone always has some noise in it. A run of buffers with none
                // at all means the recorder has stalled, and opening it again brings it back.
                var silentReadings = 0
                while (silentReadings < MaxSilentReadings && currentCoroutineContext().isActive) {
                    // Blocks until the buffer is full, which paces the readings.
                    val count = recorder.read(samples, 0, samples.size)
                    check(count >= 0) { "Microphone stopped with error $count" }
                    if (count > 0) {
                        val level = dbfs(samples, count)
                        if (level == Float.NEGATIVE_INFINITY) silentReadings++ else silentReadings = 0
                        emit(level)
                    }
                }
            } finally {
                recorder.release()
            }
        }
    }.flowOn(Dispatchers.IO)

    @SuppressLint("MissingPermission") // Collected only after the user has allowed recording.
    private fun open(): AudioRecord {
        val recorder = AudioRecord(
            // The one source Android guarantees to be free of automatic gain and noise
            // suppression, which would flatten the readings.
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            SampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            maxOf(
                AudioRecord.getMinBufferSize(
                    SampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                ),
                SamplesPerReading * Short.SIZE_BYTES,
            ),
        )
        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            error("Microphone could not be opened")
        }
        return recorder
    }

    /** The root mean square of the first [count] samples, relative to the largest 16-bit value. */
    private fun dbfs(samples: ShortArray, count: Int): Float {
        var sumOfSquares = 0.0
        for (i in 0 until count) {
            val sample = samples[i].toDouble()
            sumOfSquares += sample * sample
        }
        val rms = sqrt(sumOfSquares / count)
        return (20 * log10(rms / Short.MAX_VALUE)).toFloat()
    }

    private companion object {
        const val SampleRate = 44_100

        /** An eighth of a second of audio per reading. */
        const val SamplesPerReading = SampleRate / 8

        /** Half a second without any signal. */
        const val MaxSilentReadings = 4
    }
}
