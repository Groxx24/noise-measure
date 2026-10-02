package com.noise.meter.data.record

import platform.Foundation.NSNumber
import platform.Foundation.NSUserDefaults

class UserDefaultsRecordStore : RecordStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun read(): Float? = (defaults.objectForKey(Key) as? NSNumber)?.floatValue

    override fun write(decibels: Float) {
        defaults.setFloat(decibels, forKey = Key)
    }

    private companion object {
        const val Key = "noise_record_decibels"
    }
}
