package com.noise.meter

import androidx.compose.ui.window.ComposeUIViewController
import com.noise.meter.di.AppContainer
import platform.UIKit.UIViewController

// Lives as long as the process, so it outlives any single view controller.
private val container by lazy { AppContainer() }

/** Entry point called from the Swift side of the iOS app. */
fun MainViewController(): UIViewController = ComposeUIViewController { App(container) }
