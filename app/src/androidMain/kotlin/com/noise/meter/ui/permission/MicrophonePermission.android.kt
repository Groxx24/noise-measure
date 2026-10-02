package com.noise.meter.ui.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect

@Composable
actual fun rememberMicrophonePermission(): MicrophonePermission {
    val context = LocalContext.current
    var isGranted by remember { mutableStateOf(context.canRecord()) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted = it }

    // The permission may have been changed in the system settings while the app was away.
    LifecycleResumeEffect(context) {
        isGranted = context.canRecord()
        onPauseOrDispose {}
    }

    return remember(isGranted, launcher) {
        MicrophonePermission(isGranted) { launcher.launch(Manifest.permission.RECORD_AUDIO) }
    }
}

private fun Context.canRecord(): Boolean =
    checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
