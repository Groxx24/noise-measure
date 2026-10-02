package com.noise.meter.domain.model

/** How loud it is right now, in approximate decibels of sound pressure (dB SPL). */
data class NoiseLevel(val decibels: Float) {
    val band: NoiseBand get() = NoiseBand.of(decibels)

    /** Where the reading sits on [Range], from 0 to 1. */
    val fraction: Float
        get() = (decibels - Range.start) / (Range.endInclusive - Range.start)

    companion object {
        /** From silence to about as loud as a rock concert; readings never leave it. */
        val Range = 0f..120f
    }
}
