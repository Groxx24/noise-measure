package com.noise.meter

import android.app.Application
import com.noise.meter.data.record.SharedPreferencesRecordStore
import com.noise.meter.di.AppContainer

class NoiseMeterApplication : Application() {
    /** Lives as long as the process, so it outlives any single activity. */
    val container: AppContainer by lazy { AppContainer(SharedPreferencesRecordStore(this)) }
}
