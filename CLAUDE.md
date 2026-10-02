# Noise Meter

A one-screen sound level meter for Android and iOS: the current noise level in decibels in the
middle of the screen, on a bar that fills the screen from the bottom and changes colour as it gets
louder. Built with Compose Multiplatform. Single Gradle module `:app`, package `com.noise.meter`,
plus the Xcode host project in `iosApp/`.

## Source sets

| Source set | Holds |
|---|---|
| `app/src/commonMain` | Everything shared: UI, ViewModels, domain, data. New code goes here by default. |
| `app/src/androidMain` | `MainActivity`, the manifest, launcher resources and `actual` declarations. |
| `app/src/iosMain` | `MainViewController` (called from Swift) and `actual` declarations. |
| `iosApp/` | A thin SwiftUI shell that hosts the shared UI. No app logic in Swift. |

Common code must not use `java.*` or `android.*`. When a platform API is needed, declare an
`expect` in `commonMain` next to its caller and put the `actual`s in `Xxx.android.kt` and
`Xxx.ios.kt` (see `data/audio/Microphone.kt`).

## Architecture

Follow SOLID, MVVM and Clean Architecture. Every feature goes through the same four layers:

```
UI (Compose)  →  ViewModel  →  Use case  →  Repository  →  data source (microphone)
```

Dependencies point inward only: `ui` → `domain` ← `data`. The `domain` package must not import
Android, Compose or any platform audio API.

| Layer | Package | Responsibility |
|---|---|---|
| UI | `ui/<feature>/` | Composables. Render state, emit events. No business logic, no data access. |
| ViewModel | `ui/<feature>/` | Holds one immutable `UiState` as `StateFlow`, turns UI events into use-case calls. Talks only to use cases. |
| Use case | `domain/usecase/` | One business action per class, exposed as `operator fun invoke`. Talks only to repository interfaces. |
| Repository | interface in `domain/repository/`, implementation in `data/repository/` | The single source of truth for a kind of data. Maps raw data-source values to domain models. |
| Data source | `data/audio/`, `data/record/` | `AudioSource` and the per-platform microphone behind it; `RecordStore` and the per-platform storage behind it. |

Rules that follow from this:

- Domain models live in `domain/model/` and are plain Kotlin. Raw signal levels (dBFS) never leave
  `data`; the repository turns them into `NoiseLevel`.
- A ViewModel never touches a repository or data source directly, even for a trivial read. Add a
  use case.
- Each screen has a stateful `XxxRoute` (gets the ViewModel, collects state) and a stateless
  `XxxScreen(state, onEvent…)` that can be previewed and tested without a ViewModel.
- One class, one reason to change. Depend on interfaces across layer boundaries and pass
  dependencies in through constructors; never construct a repository or data source inside its
  consumer.
- Prefer adding a new use case over widening an existing one.

## Technologies

- **Compose Multiplatform** for all UI, shared by both platforms. No XML layouts, no Fragments,
  no SwiftUI screens.
- **Kotlin Coroutines and Flow** for all async work. `suspend` functions for one-shot calls, `Flow`
  for observed data, `viewModelScope` in ViewModels. No callbacks, no RxJava, no LiveData.

Not decided yet: dependency injection, navigation, persistence, a permissions library, and
anything else. Do not add a library for these without asking first. Until then, dependencies are
wired by hand in `di/AppContainer.kt` and nowhere else. Each platform creates one container
(`NoiseMeterApplication`, `MainViewController`), passing in the few dependencies that need a
platform object to build (the `RecordStore`), and routes build their ViewModel from it with
`viewModel { … }`.

## Microphone

- The microphone is on only after the user taps Start (`NoiseViewModel.setMeasuring`) and while
  `NoiseViewModel.uiState` is collected. The app opens stopped. `NoiseRoute` collects the state
  only once the permission is granted, and `collectAsStateWithLifecycle` stops it when the app
  goes to the background. Keep it that way: never record without a visible meter.
- The record (loudest reading) is the only thing saved, as one number, and it is kept between
  launches. No persistence library is chosen: `RecordStore` (`data/record/`) is implemented with
  `SharedPreferences` on Android and `NSUserDefaults` on iOS.
- No audio is recorded or stored; each buffer is reduced to one level and dropped.
- The microphone permission is the one platform concern the UI owns
  (`ui/permission/MicrophonePermission.kt`), because asking needs an Activity on Android.
- Readings are approximate. Phones are not calibrated, so `MicrophoneNoiseRepository` adds one
  fixed offset to the signal level to estimate dB SPL. The offset is per platform
  (`AudioSource.fullScaleDecibels`), because each applies its own gain to the microphone.

## Build

Use Android Studio's bundled JDK; the system default is too new for this Gradle version:

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:compileKotlinIosSimulatorArm64   # checks that shared code compiles for iOS
```

To run on iOS, open `iosApp/iosApp.xcodeproj` in Xcode (16 or newer); its build phase calls Gradle
to build the shared framework. Set `TEAM_ID` in `iosApp/Configuration/Config.xcconfig` to run on a
device.

Library versions in `gradle/libs.versions.toml` are pinned on purpose: this setup is limited to
Android Gradle Plugin 8.13 and compile SDK 36, and newer Compose Multiplatform releases need
plugin 9.1+ and SDK 37. Do not bump them without upgrading the toolchain as a whole.

## UI conventions

- The app is always dark, in both system themes. Colours live in `ui/theme/Theme.kt`, including
  `noiseColor`, which maps a level to the bar's colour; the system bars are forced to match in
  `MainActivity` and in `iosApp/iosApp/Info.plist`.
- User-visible text goes in `commonMain/composeResources/values/strings.xml`; read it through the
  generated `Res` class (`com.noise.meter.resources`). Format arguments must be positional
  (`%1$d`, `%2$s`). Only what the Android manifest needs stays in `androidMain/res`.
