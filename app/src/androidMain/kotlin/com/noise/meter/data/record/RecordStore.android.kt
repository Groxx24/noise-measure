package com.noise.meter.data.record

import android.content.Context

class SharedPreferencesRecordStore(context: Context) : RecordStore {
    private val preferences = context.getSharedPreferences("noise_record", Context.MODE_PRIVATE)

    override fun read(): Float? =
        if (preferences.contains(Key)) preferences.getFloat(Key, 0f) else null

    override fun write(decibels: Float) {
        preferences.edit().putFloat(Key, decibels).apply()
    }

    private companion object {
        const val Key = "decibels"
    }
}
