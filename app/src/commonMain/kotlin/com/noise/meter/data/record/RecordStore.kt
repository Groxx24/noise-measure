package com.noise.meter.data.record

/** Where the loudest reading is kept between launches. Each platform has its own. */
interface RecordStore {
    /** The saved record in decibels, or null if nothing has been saved yet. */
    fun read(): Float?

    fun write(decibels: Float)
}
