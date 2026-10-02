package com.noise.meter.domain.repository

import com.noise.meter.domain.model.NoiseLevel
import kotlinx.coroutines.flow.Flow

interface NoiseRepository {
    /**
     * Emits the current noise level several times a second. Listening starts when the flow is
     * collected and stops when collection ends; the flow fails if the microphone can't be used.
     */
    fun observe(): Flow<NoiseLevel>
}
