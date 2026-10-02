package com.noise.meter.domain.model

/** What a reading means to a listener. Each band starts at [fromDecibels] and runs up to the next. */
enum class NoiseBand(val fromDecibels: Float) {
    Quiet(0f),
    Moderate(45f),
    Loud(70f),

    /** Long exposure from here on can damage hearing. */
    Harmful(85f);

    companion object {
        fun of(decibels: Float): NoiseBand =
            entries.lastOrNull { decibels >= it.fromDecibels } ?: Quiet
    }
}
