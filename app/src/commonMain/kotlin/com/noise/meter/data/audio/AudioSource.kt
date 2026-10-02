package com.noise.meter.data.audio

import kotlinx.coroutines.flow.Flow

/** Something that hears, reduced to how strong its signal is. */
interface AudioSource {
    /**
     * The sound pressure, in dB SPL, taken to drive this source to full scale (0 dBFS). It differs
     * between platforms because each applies its own gain to the microphone.
     */
    val fullScaleDecibels: Float

    /**
     * Emits the signal level several times a second for as long as it is collected, in dBFS:
     * 0 is the loudest the source can capture and quieter is more negative. No audio is kept.
     * The flow fails if the source can't be opened.
     */
    fun levels(): Flow<Float>
}
