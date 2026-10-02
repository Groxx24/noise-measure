package com.noise.meter.data.audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.AVFAudio.setActive
import platform.CoreAudioTypes.kAudioFormatAppleLossless
import platform.Foundation.NSURL
import kotlin.time.Duration.Companion.milliseconds

actual fun microphone(): AudioSource = IosMicrophone()

private class IosMicrophone : AudioSource {
    /**
     * Apple publishes no figure, and an iPhone's input runs hotter than Android's: loud speech
     * at arm's length, about 85 dB SPL, reads near -12 dBFS. Tune against a reference meter.
     */
    override val fullScaleDecibels = 100f

    @OptIn(ExperimentalForeignApi::class)
    override fun levels(): Flow<Float> = flow {
        val session = AVAudioSession.sharedInstance()
        // Measurement mode turns off the system's automatic gain, which would flatten the readings.
        session.setCategory(
            category = AVAudioSessionCategoryRecord,
            mode = AVAudioSessionModeMeasurement,
            options = 0u,
            error = null,
        )
        session.setActive(true, error = null)

        // Only a recorder has a level meter, but nothing needs keeping, so it writes to nowhere.
        val recorder = AVAudioRecorder(
            uRL = NSURL.fileURLWithPath("/dev/null"),
            settings = mapOf(
                AVFormatIDKey to kAudioFormatAppleLossless,
                AVSampleRateKey to 44_100.0,
                AVNumberOfChannelsKey to 1,
            ),
            error = null,
        )
        recorder.meteringEnabled = true
        try {
            check(recorder.record()) { "Microphone could not be opened" }
            while (true) {
                delay(ReadingInterval)
                recorder.updateMeters()
                emit(recorder.averagePowerForChannel(0u))
            }
        } finally {
            recorder.stop()
            session.setActive(false, error = null)
        }
        // Switching the audio session on and off can take seconds, too long for the main thread.
    }.flowOn(Dispatchers.Default)

    private companion object {
        val ReadingInterval = 125.milliseconds
    }
}
