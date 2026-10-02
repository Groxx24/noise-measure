package com.noise.meter.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class NoiseLevelTest {
    @Test
    fun band_startsAtItsThreshold() {
        assertEquals(NoiseBand.Quiet, NoiseLevel(44.9f).band)
        assertEquals(NoiseBand.Moderate, NoiseLevel(45f).band)
        assertEquals(NoiseBand.Loud, NoiseLevel(70f).band)
        assertEquals(NoiseBand.Harmful, NoiseLevel(85f).band)
        assertEquals(NoiseBand.Harmful, NoiseLevel(120f).band)
    }

    @Test
    fun fraction_spansTheRange() {
        assertEquals(0f, NoiseLevel(0f).fraction)
        assertEquals(0.5f, NoiseLevel(60f).fraction)
        assertEquals(1f, NoiseLevel(120f).fraction)
    }
}
