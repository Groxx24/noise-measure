package com.noise.meter.domain.usecase

import com.noise.meter.data.record.RecordStore
import com.noise.meter.data.repository.StoredNoiseRecordRepository
import com.noise.meter.domain.model.NoiseLevel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateNoiseRecordUseCaseTest {
    @Test
    fun invoke_keepsOnlyTheLoudestReading() = runBlocking {
        val store = FakeRecordStore()
        val repository = StoredNoiseRecordRepository(store)
        val updateNoiseRecord = UpdateNoiseRecordUseCase(repository)

        listOf(60f, 85f, 70f).forEach { updateNoiseRecord(NoiseLevel(it)) }

        assertEquals(NoiseLevel(85f), repository.observe().first())
        assertEquals(85f, store.read())
    }

    @Test
    fun invoke_startsFromTheSavedRecord() = runBlocking {
        val store = FakeRecordStore(saved = 90f)
        val repository = StoredNoiseRecordRepository(store)

        UpdateNoiseRecordUseCase(repository)(NoiseLevel(80f))

        assertEquals(NoiseLevel(90f), repository.observe().first())
        assertEquals(90f, store.read())
    }
}

private class FakeRecordStore(private var saved: Float? = null) : RecordStore {
    override fun read(): Float? = saved

    override fun write(decibels: Float) {
        saved = decibels
    }
}
