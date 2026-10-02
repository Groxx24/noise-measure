package com.noise.meter.ui.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType

@Composable
actual fun rememberMicrophonePermission(): MicrophonePermission {
    var isGranted by remember {
        mutableStateOf(
            AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeAudio) ==
                AVAuthorizationStatusAuthorized,
        )
    }
    return remember(isGranted) {
        MicrophonePermission(isGranted) {
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeAudio) { isGranted = it }
        }
    }
}
