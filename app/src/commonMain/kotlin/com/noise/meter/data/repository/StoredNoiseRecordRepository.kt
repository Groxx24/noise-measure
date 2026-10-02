package com.noise.meter.data.repository

import com.noise.meter.data.record.RecordStore
import com.noise.meter.domain.model.NoiseLevel
import com.noise.meter.domain.repository.NoiseRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class StoredNoiseRecordRepository(private val store: RecordStore) : NoiseRecordRepository {
    // Read once, on first use; after that the store is only written to.
    private val record by lazy { MutableStateFlow(store.read()?.let(::NoiseLevel)) }

    override fun observe(): Flow<NoiseLevel?> = record

    override suspend fun save(record: NoiseLevel) {
        this.record.value = record
        store.write(record.decibels)
    }
}
