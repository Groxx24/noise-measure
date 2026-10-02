package com.noise.meter.data.audio

/** The device's microphone. Collect its levels only once the user has allowed recording. */
expect fun microphone(): AudioSource
