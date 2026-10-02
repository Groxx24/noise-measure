package com.noise.meter.data.repository

import com.noise.meter.data.audio.AudioSource
import com.noise.meter.domain.model.NoiseLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class MicrophoneNoiseRepositoryTest {
    @Test
    fun observe_turnsSignalLevelIntoDecibels() = runBlocking {
        val levels = MicrophoneNoiseRepository(FakeAudioSource(-22f, -62f)).observe().toList()

        assertEquals(listOf(NoiseLevel(90f), NoiseLevel(50f)), levels)
    }

    @Test
    fun observe_staysWithinRange() = runBlocking {
        // A silent buffer has no signal at all, which is minus infinity in dBFS.
        val levels = MicrophoneNoiseRepository(FakeAudioSource(Float.NEGATIVE_INFINITY, 20f))
            .observe()
            .toList()

        assertEquals(listOf(NoiseLevel(0f), NoiseLevel(120f)), levels)
    }
}

private class FakeAudioSource(private vararg val levels: Float) : AudioSource {
    override fun levels(): Flow<Float> = flowOf(*levels.toTypedArray())
}
