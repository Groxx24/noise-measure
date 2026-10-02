package com.noise.meter.ui.permission

import androidx.compose.runtime.Composable

/** Whether the user has allowed recording, and a way to ask them. */
class MicrophonePermission(val isGranted: Boolean, val request: () -> Unit)

@Composable
expect fun rememberMicrophonePermission(): MicrophonePermission
