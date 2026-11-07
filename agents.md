# YES Flash Camera - Technical Map

This document provides a high-level overview and a deep-dive into the technical architecture and key components of the **YES Flash Camera** project.

## 1. Module Overview

The project is structured into four main modules:

*   **[:app](file:///E:/ANDROIDprj/YESFlashCamera/app)**: Main entry point of the application. It handles the UI orchestration, top-level navigation, permission management via `PermissionManager`, and high-level UI components. Key files: [MainActivity.kt](file:///E:/ANDROIDprj/YESFlashCamera/app/src/main/java/com/yes/flashcamera/presentation/MainActivity.kt).
*   **[:camera](file:///E:/ANDROIDprj/YESFlashCamera/camera)**: The core functional module for camera features. It contains the Camera2 API implementation, the repository for camera operations, domain use cases, and the MVI presentation layer. Key files: [CameraRepository.kt](file:///E:/ANDROIDprj/YESFlashCamera/camera/src/main/java/com/yes/camera/data/repository/CameraRepository.kt), [CameraScreen.kt](file:///E:/ANDROIDprj/YESFlashCamera/camera/src/main/java/com/yes/camera/presentation/ui/CameraScreen.kt).
*   **[:shared](file:///E:/ANDROIDprj/YESFlashCamera/shared)**: A library module providing common domain base classes, models, themes, and infrastructure utilities used across the project. Key files: [UseCase.kt](file:///E:/ANDROIDprj/YESFlashCamera/shared/src/main/java/com/yes/shared/domain/UseCase.kt), [CameraThreadManager.kt](file:///E:/ANDROIDprj/YESFlashCamera/shared/src/main/java/com/yes/shared/utils/CameraThreadManager.kt), [Theme.kt](file:///E:/ANDROIDprj/YESFlashCamera/shared/src/main/java/com/yes/shared/presentation/ui/theme/Theme.kt).
*   **[:settings](file:///E:/ANDROIDprj/YESFlashCamera/settings)**: Manages application settings and user preferences, utilizing `DataStore` for persistent storage. Key files: [SettingsRepository.kt](file:///E:/ANDROIDprj/YESFlashCamera/settings/src/main/java/com/yes/settings/data/repository/SettingsRepository.kt).

---

## 2. Architecture

The project follows a **Clean Architecture** approach with a presentation layer based on the **MVI (Model-View-Intent)** pattern.

### MVI Pattern
*   **Contract**: Defines `ViewState`, `Event`, and `Effect` for each screen (e.g., [CameraContract](file:///E:/ANDROIDprj/YESFlashCamera/camera/src/main/java/com/yes/camera/presentation/contract/CameraContract.kt)).
*   **ViewModel**: Manages state using `StateFlow` and handles incoming events to update the state.
*   **State**: Immutable representation of the UI state, collected in Compose screens.

### Hybrid UseCase System
The system uses a custom `UseCase` base class ([UseCase.kt](file:///E:/ANDROIDprj/YESFlashCamera/shared/src/main/java/com/yes/shared/domain/UseCase.kt)) that supports both synchronous and asynchronous execution:
*   `runSync(params)`: For immediate execution on the current thread.
*   `run(params)`: A `suspend` function for asynchronous operations.
*   `bind(params)`: Creates a `UseCaseAction` to trigger execution in either mode.

---

## 3. Core Domain

The domain layer defines the central entities and business logic:

*   **[Characteristics](file:///E:/ANDROIDprj/YESFlashCamera/camera/src/main/java/com/yes/camera/domain/model/Characteristics.kt)**: Represents the state and capabilities of the camera (ISO, Shutter Speed, Focus, Histogram data, etc.).
*   **[Dimensions](file:///E:/ANDROIDprj/YESFlashCamera/shared/src/main/java/com/yes/shared/domain/Dimensions.kt)**: A shared model for representing width and height.

---

## 4. Data Layer

Responsible for data retrieval and persistence:

*   **CameraRepository**: [CameraRepository.kt](file:///E:/ANDROIDprj/YESFlashCamera/camera/src/main/java/com/yes/camera/data/repository/CameraRepository.kt). Handles all Camera2 interop, session management, and image stream processing.
*   **SettingsRepository**: [SettingsRepository.kt](file:///E:/ANDROIDprj/YESFlashCamera/settings/src/main/java/com/yes/settings/data/repository/SettingsRepository.kt). Uses `DataStore` to persist user preferences like resolution and capture formats.

---

## 5. Domain Layer (UseCases)

Key UseCases that encapsulate business logic:

*   **Camera operations**: `OpenCameraUseCase`, `CloseCameraUseCase`, `SetInputCharacteristicsUseCase`, `SetOutputCharacteristicsUseCase`, `SubscribeCameraSettingsUseCase`.
*   **Settings management**: `GetSettingsUseCase`, `SetSettingsUseCase`.

---

## 6. Presentation Layer

Built with Jetpack Compose:

*   **Navigation**: High-level screen switching in [MainActivity.kt](file:///E:/ANDROIDprj/YESFlashCamera/app/src/main/java/com/yes/flashcamera/presentation/MainActivity.kt).
*   **Key Screens**:
    *   [CameraScreen.kt](file:///E:/ANDROIDprj/YESFlashCamera/camera/src/main/java/com/yes/camera/presentation/ui/CameraScreen.kt): Orchestrates camera preview, permissions, and success/error states.
    *   [CameraScreenSuccess.kt](file:///E:/ANDROIDprj/YESFlashCamera/camera/src/main/java/com/yes/camera/presentation/ui/views/CameraScreenSuccess.kt): The primary UI for camera control when the device is active.
*   **Design System & Theme**: Centralized in `:shared` under `com.yes.shared.presentation.ui.theme`. Consists of `AppColors`, `AppDimens`, `AppTypography`, and `AppTheme.colors/dimens` composition locals.
*   **UI Models**: [CharacteristicsUI.kt](file:///E:/ANDROIDprj/YESFlashCamera/camera/src/main/java/com/yes/camera/presentation/model/CharacteristicsUI.kt) maps domain data to presentation-friendly formats.

---

## 7. Infrastructure & Utils

Supporting components for hardware and system interop:

*   **CameraThreadManager**: [CameraThreadManager.kt](file:///E:/ANDROIDprj/YESFlashCamera/shared/src/main/java/com/yes/shared/utils/CameraThreadManager.kt). Manages background threads for camera callbacks.
*   **ImageSaver**: [ImageSaver.kt](file:///E:/ANDROIDprj/YESFlashCamera/app/src/main/java/com/yes/flashcamera/presentation/ui/ImageSaver.kt). Handles writing image data to storage.
*   **AutoExposure**: Implementation logic in [CameraRepository.kt](file:///E:/ANDROIDprj/YESFlashCamera/camera/src/main/java/com/yes/camera/data/repository/CameraRepository.kt) (AutoExposure object) for ISO and Shutter priority calculations.
*   **GL Rendering Engine**: [GLRenderer.kt](file:///E:/ANDROIDprj/YESFlashCamera/camera/src/main/java/com/yes/camera/presentation/ui/custom/gles/GLRenderer.kt). Uses OpenGL ES 2.0/3.0 for real-time preview rendering and interactive Shader-based objects (`GLScreen`, `GlMagnifierAdvanced`, `GlFocus`).

---

## 8. GLRenderer Architecture & Hardware Pipeline Rules

*   **Camera2 & SurfaceTexture Contract**: `GLRenderer` generates an OES texture (`GL_TEXTURE_EXTERNAL_OES`) via `createOESTextureObject()` and wraps it in a `SurfaceTexture`. This `SurfaceTexture` MUST be passed via `callback(surfaceTexture)` to `CameraRepository` to bind as the target Surface for Camera2 capture sessions.
*   **Dynamic Texture Binding Rule**: Never hardcode OES texture handles (e.g. `1`). Always pass `oesTextureId` created dynamically by `createOESTextureObject()` down to `GLObject.draw(mvpMatrix, oesTextureId)`.
*   **Raycasting & Touch Processing**: Touch events on `CameraPreviewContainer` (`[-1..1]` NDC space) MUST be processed via `GLRenderer.handleTouchPress()` and `handleTouchDrag()`. They use `convertNormalized2DPointToRay` (3D Ray Casting via inverted `viewProjectionMatrix`) and `Geometry.intersectionPoint(ray, plane)` to translate the magnifier overlay seamlessly in 3D world space.
*   **Shader Management & Dual Textures**: `GlMagnifierAdvanced` uses multi-texture GLSL shaders (`vertex_multiple.glsl`, `mixed_fragment.glsl`) to sample both `baseTexture` (`samplerExternalOES` for live camera stream) and `overlayTexture` (`sampler2D` for focus grid overlay) using alpha blending `mix(baseColor, overlayColor, overlayColor.a)`.
*   **Resource Cleanup**: Call `GLRenderer.release()` when destroying GL contexts to free `GLES20.glDeleteTextures` and `GLES20.glDeleteProgram` handles, avoiding VRAM leaks.

---

## 9. Performance Optimizations

*   **Zero-allocation Histogram**: [CameraRepository.kt](file:///E:/ANDROIDprj/YESFlashCamera/camera/src/main/java/com/yes/camera/data/repository/CameraRepository.kt) uses a pre-allocated `ByteArray` and `tryEmit` to update the histogram stream without GC pressure.
*   **Localized StateFlow updates**: [CameraScreen.kt](file:///E:/ANDROIDprj/YESFlashCamera/camera/src/main/java/com/yes/camera/presentation/ui/CameraScreen.kt) optimizes recomposition by selectively collecting state and using `remember` blocks for stable references.
*   **Buffering Strategies**: Utilizes standard Android `SurfaceTexture` buffering, optimized through `GLRenderer` to minimize latency in the preview pipeline.




---

# AI AGENT INSTRUCTIONS & PROJECT STANDARDS

## META-RULE: AUTONOMOUS CONTEXT UPDATE (CRITICAL)
- **Autonomous Writing:** You (the AI Agent) have permission to read and write to this `AGENTS.md` file. Every time we agree on a new architectural pattern, code style, or project decision during our chat, you MUST automatically update this file.
- **How to Update:** 
  1. Add a new bullet point under the `## ? Architecture Decisions Log (Changelog)` section with the date and the decision.
  2. If the decision changes or introduces a permanent rule, update the corresponding technical section above.
  3. Keep the file structured, clean, and concise.
- **Reporting:** After modifying the file, notify the developer in the chat: *"AGENTS.md updated: added rule [X]"*.

---

## 1. CORE ARCHITECTURE & STATE MANAGEMENT
- **Architecture Pattern:** Strict MVVM / MVI with Unidirectional Data Flow (UDF).
- **UI State Location:** All screen and business logic states MUST reside inside the `ViewModel`. 
- **Stateless UI:** Jetpack Compose screens must be **Stateless**. They must only accept `State` objects (or exposed `StateFlow` values) and lambda expressions for user events.
- **No Logic in UI:** Composable functions must never contain business logic, validation, or direct state mutation.

---

## 2. TECH STACK & DEVELOPMENT GUIDELINES
- **Language:** Kotlin (Strict type safety, explicit modifiers where needed).
- **UI Framework:** Jetpack Compose (No XML views allowed).
- **Asynchrony:** Kotlin Coroutines and `StateFlow` / `SharedFlow` for state streaming.
- **Code Consistency & Similarity Principle:** All new code modifications, UI parameter displays, and architecture decisions MUST strictly align with established patterns and conventions across the project (e.g. auto-metered parameters like ISO, Shutter, and White Balance displaying real-time hardware values rounded to the nearest integer in `MapperUI`).


---

## 3. CODE STYLE & NAMING CONVENTIONS
- **ViewModels:** Must always have the `ViewModel` suffix (e.g., `MainViewModel.kt`).
- **Screens:** Entry-point Composable screens must have the `Screen` suffix (e.g., `MainScreen.kt`).
- **State Naming:** UI State data classes must follow the format `[ScreenName]UiState` (e.g., `MainUiState`).
- **Event Naming:** User events sent to ViewModel must be represented as a sealed class/interface named `[ScreenName]UiEvent` or `Intent`.

---

## 4. CODE QUALITY & TRIAL POLICY
- **Anti-Garbage Code Rule / Rollback Policy:** If a proposed solution or trial fix does not resolve the user's issue, the agent MUST immediately revert (roll back) those experimental changes to the previous clean state before exploring alternative approaches, ensuring no redundant or unsuccessful trial changes accumulate in the codebase.
- **Strict Test Execution & Creation Policy:** NEVER run or create unit or instrumented tests unless explicitly requested by the user.

---

## ARCHITECTURE DECISIONS LOG (CHANGELOG)
*This section is automatically maintained by the AI Agent.*
- **[2026-03-31] Anti-Garbage Code Rule:** Added strict policy to immediately roll back unsuccessful trial changes to prevent accumulating garbage code.
- **[2026-03-31] GLRenderer Architecture & Dynamic Texture Contract:** Documented GLRenderer rendering pipeline, touch raycasting, dynamic OES texture binding, and lifecycle release rules.
- **[2026-03-31] Camera Hardware Resolutions Persistence:** Documented and verified that upon camera startup (`OpenCameraUseCase`), available hardware resolutions are fetched from `CameraCharacteristics` and persisted via `SettingsRepository.setResolutions(...)` into the shared DataStore repository, allowing the `:settings` module to seamlessly retrieve and display them in `SettingsScreenSuccessNew`.
- **[2026-03-31] Resolution Availability in SettingsScreenSuccessNew:** Updated `SettingsRepository` in `:settings` and `SettingsScreenSuccessNew` to ensure all possible camera resolutions fetched from the common database (and a comprehensive fallback set of standard high resolutions) are available for selection in the resolution dropdown menu upon opening the settings screen.
- **[2026-03-31] Centralized Theme & App Theme Selection:** Confirmed that the application theme and styling system reside centrally in the `:shared` module (`AppTheme`, `AppColors`, `AppDimens`). Added an extensible App Theme selection dropdown (`ExposedDropdownMenuBox`) in `SettingsScreenSuccessNew` consistent with the app's design system and persisted via `SettingsRepository` and DataStore.
- **[2026-03-31] Storage & Remaining Shots Estimation:** Verified and updated `SettingsViewModel` and `SettingsRepository.getStorageInfo` to ensure that remaining shots and storage size calculations in `SettingsScreenSuccessNew` accurately incorporate both the selected image resolution (with fallback to `resolutionItems`) and the save format (`JPEG`, `RAW`/DNG, `JPEGRAW`), with proportional file sizes per megapixel (`mbPerMp`).
- **[2026-03-31] Fixed Captured Bitmap Orientation in Preview:** Added `processCapturedBitmap(...)` in `CameraRepository.kt` to automatically rotate decoded captured bitmaps by `SENSOR_ORIENTATION` (and mirror horizontally if front camera), ensuring they display upright in `CapturedImageScreen`.
- **[2026-03-31] Settings-Driven Image Saving (`SaveCapturedImageUseCase`):** Updated `SaveCapturedImageUseCase` to retrieve the user's configured save path (`filePath`) and image format (`imgFormat`: `JPEG`, `RAW`, `JPEGRAW`) from `SettingsRepository`, passing them down to `CameraRepository.saveCapturedImage(...)` for correct persistence according to user preferences.
- **[2026-03-31] Dedicated Image Saving Function in `CameraRepository`:** Added `saveCapturedImage(...)` to `CameraRepository` in the `:camera` module supporting parameters for save path (`savePath`), image format (`ImgFormat`: JPEG, RAW/DNG, JPEGRAW), and resolution (`Dimensions`), with overloads for flexibility.
- **[2026-03-31] Refactored Single Capture Flow (`singleCapture()`):** Since `captureSurfaceJpeg` and `captureSurfaceRaw` are already configured in `SessionConfiguration` during `startSession(...)`, `applyState()` was cleaned up to manage repeating preview state (`persistentBuilder`) exclusively. `singleCapture()` now directly builds a dedicated `TEMPLATE_STILL_CAPTURE` request, applies preview settings via `applyStateToBuilder(...)`, adds `captureSurfaceJpeg` (and `captureSurfaceRaw` when format is RAW/JPEGRAW), and invokes `session.capture(...)` without mutating `persistentBuilder` or stopping preview repetition.
- **[2026-03-31] Fixed Null Bitmap Extraction in `singleCapture()`:** Resolved issue where `singleCapture()` produced null Bitmaps by updating `imageReaderJpeg` format from `ImageFormat.YUV_420_888` to `ImageFormat.JPEG`, adding YUV-to-Bitmap conversion support via `YuvImage` in `listenerJpeg`, and establishing fallbacks for zero resolution sizes during session startup.
- **[2026-03-31] Fixed Captured Bitmap Orientation in Preview:** Added `processCapturedBitmap(...)` in `CameraRepository.kt` to automatically rotate decoded captured bitmaps by `SENSOR_ORIENTATION` (and mirror horizontally if front camera), ensuring they display upright in `CapturedImageScreen`.
- **[2026-03-31] Fixed Infinite Loop on Preview Dismissal:** Added `clearCapturedBitmap()` in `CameraRepository` and called it inside `returnToSuccessState()` in `CameraViewModel` to clear the cached captured bitmap from `_characteristicsFlow` upon clicking Save or Cancel, preventing the preview screen from repeatedly popping up again.
- **[2026-03-31] Fixed Relative Path Parsing for Saving (`CameraRepository.kt`):** Added `getRelativePath(savePath)` helper to correctly extract MediaStore relative directories (e.g. `DCIM/Camera`) from absolute or custom paths specified in settings, preventing hardcoded overrides to `DCIM/FlashCamera`.
- **[2026-03-31] Added True DNG Creation via `DngCreator`:** Updated `imageReaderRaw` format from `YUV_420_888` to `ImageFormat.RAW_SENSOR`. Implemented `ImageSaver.saveDng(...)` utilizing Android's `DngCreator` along with `TotalCaptureResult` and raw `Image` data from the capture session to produce valid Adobe DNG raw files when `DNG` or `JPEG+DNG` formats are selected.
- **[2026-03-31] Synchronized RAW Capture via `rawCaptureDeferred`:** Implemented `rawCaptureDeferred` (`CompletableDeferred<Image?>`) in `CameraRepository.kt` to ensure asynchronous RAW frame arrival from `listenerRaw` is correctly synchronized and awaited before generating and saving Adobe DNG files.
- **[2026-03-31] Fixed Image Format Persistence in `:camera` SettingsRepository:** Updated `SettingsRepository.getCharacteristics()` and `subscribeSettings()` in the `:camera` module to retrieve and subscribe to `IMGFORMAT` from DataStore, ensuring that selected formats (`JPEG`, `DNG`, `JPEG+DNG`) are correctly passed to `SaveCapturedImageUseCase` and `CameraRepository`.
- **[2026-03-31] DNG Hardware Support Auto-Detection (`getCameraCharacteristics` & `OpenCameraUseCase`):** `CameraRepository.getCameraCharacteristics` queries `REQUEST_AVAILABLE_CAPABILITIES_RAW` from `CameraCharacteristics` and sets `supportsRaw` on `Characteristics`. `OpenCameraUseCase` reads `hardwareInfo.supportsRaw` and updates `SettingsRepository`.
- **[2026-03-31] Fixed Unstable DNG Selection in Settings (`MapperUI.kt`):** Fixed condition in `:settings` `MapperUI.kt` from `settings.supportsRaw == true` to `settings.supportsRaw != false`. Initial `null` values (before camera initialization) no longer override DNG selections or hide format options. Options are only hidden when `supportsRaw` is explicitly `false`.
- **[2026-03-31] Strict ViewModel Clean Architecture Rule:** Refactored `CameraViewModel` and `SettingsViewModel` so that ViewModels interact with repositories strictly through UseCases. Created `ClearCapturedBitmapUseCase` in `:camera` and restored `GetStorageInfoUseCase` in `:settings`, completely eliminating direct repository references inside ViewModels.
- **[2026-03-31] Automatic Clearing of Captured Image on Save & Smart Preview Trigger:** `CameraRepository.saveCapturedImage(...)` automatically clears `capturedBitmap` in the repository after saving. `CameraViewModel` compares `newBitmap != oldBitmap` before triggering `CameraState.CapturedPreview`, opening the preview window only when a genuinely new frame is captured.
- **[2026-03-31] Autonomous JPEG & RAW Image Readers (`listenerJpeg` & `listenerRaw`):** `listenerJpeg` decodes the captured JPEG frame and autonomously updates `_characteristicsFlow` with `capturedBitmap = bitmap`. `listenerRaw` autonomously buffers the RAW image frame into `lastRawImage` on `CameraRepository`, which is directly consumed by `saveCapturedImage` when exporting DNG files, eliminating unnecessary deferred objects.
- **[2026-03-31] Unified Shutter Trigger via `SetInputCharacteristicsUseCase`:** Pressing the shutter button in `CameraViewModel` now sets `isCaptureRequested = true` in characteristics and invokes `SetInputCharacteristicsUseCase`, routing photo capture through the standard unified characteristics pipeline and eliminating `SingleCaptureUseCase`.
- **[2026-03-31] Streamlined Single-Builder & Single-Method Pipeline:** Consolidated `applyStateToBuilder` and `applyState` into `startPreviewCaptureRequest`. Registered all capture surfaces (`previewSurface`, `histogramSurface`, `captureSurfaceJpeg`, `captureSurfaceRaw`) once in `startSession` on `persistentBuilder`. Single `persistentBuilder` handles both continuous preview (`session.setRepeatingRequest`) and photo capture (`session.capture`) based on `isCaptureRequested`. `ImageReader` listeners disconnect immediately in `finally` upon image acquisition to cancel subsequent capture requests.
- **[2026-03-31] Dynamic Target Management on `persistentBuilder` for Preview & Capture:** Refactored `processPreviewCaptureRequest` to use the exact same `persistentBuilder` and `cameraSession` for both `session.setRepeatingRequest` (preview) and `session.capture` (still photo). Capture surfaces (`captureSurfaceJpeg`, `captureSurfaceRaw`) are added temporarily to `persistentBuilder` when `isCaptureRequested == true` and immediately removed upon capture completion or failure, preventing ImageReader buffer overflow and keeping live preview updating smoothly at 30/60 fps.
- **[2026-03-31] Fixed Shutter Speed Sorting Desynchronization (`MapperUI.kt`):** Fixed sorting of `standardShutterSpeeds` in `:camera` `MapperUI.kt` from descending (`compareByDescending`) to ascending (`sortedBy { it.key }`), perfectly aligning `shutterItems` and `shutterPosition` with `CameraScreenSuccess.kt` (ensuring 1/125 and other shutter speeds trigger `startPreviewCaptureRequest` reliably without position index mismatches).
- **[2026-03-31] Lossless JPEG Buffering (`lastJpegBytes`):** `listenerJpeg` now extracts and buffers the raw compressed JPEG byte array into `lastJpegBytes` on `CameraRepository`, while simultaneously decoding a `Bitmap` for live preview in `CapturedImageScreen`. `saveCapturedImage` writes `lastJpegBytes` directly to disk using `ImageSaver.saveBytes(...)`, bypassing `lastCapturedBitmap` entirely during image export.
- **[2026-03-31] Independent Listener Detachment & Safe `lastRawImage` Acquisition:** Refactored `listenerJpeg` and `listenerRaw` to detach their respective `ImageReader` listeners independently in their `finally` blocks, ensuring `lastRawImage` is reliably captured for DNG export without race conditions or premature detachment. Updated `saveCapturedImage` to rely strictly on `lastJpegBytes` and `lastRawImage` without referencing `lastCapturedBitmap`.
- **[2026-03-31] Optimized `Image` Lifecycle Management (`listenerJpeg` & `listenerRaw`):** `listenerJpeg` now closes the acquired JPEG `Image` immediately in its `finally` block after copying its bytes into `lastJpegBytes`. `lastRawImage` (`Image`) is kept open safely during DNG export and closed immediately in `saveCapturedImage`'s `finally` block once `DngCreator` finishes writing.
- **[2026-03-31] Immediate RAW Byte Copying (`listenerRaw` & `saveDngBytes`):** `listenerRaw` now immediately copies RAW sensor bytes into `lastRawBytes` and calls `image.close()` instantly. `saveDngBytes` uses `DngCreator.writeByteBuffer(...)` to write DNG files from the buffered byte array, eliminating open `Image` references across threads and ensuring `lastRawBytes` is never null during export.
- **[2026-03-31] File Format Display on `CameraScreenSuccess`**: Added `imgFormat` (`JPEG`, `DNG`, `JPEG+DNG`) to `CharacteristicsUI` and `MapperUI`, and displayed it dynamically in English right below the resolution text in the top-right corner of `CameraScreenSuccess.kt`.
- **[2026-03-31] Preserving `supportsRaw` in `:camera` SettingsRepository & UseCase**: Updated `:camera` module's `SettingsRepository` (`getCharacteristics` and `subscribeSettings`) and `SubscribeCameraSettingsUseCase` to include `supportsRaw` from DataStore, preventing the DNG support flag from being lost or wiped out during settings or camera updates.
- **[2026-03-31] Fixed `imgFormat` Mapping in `MapperUI`**: Added `characteristicsUI.imgFormat` mapping back to domain `ImgFormat` (`JPEG`, `DNG`, `JPEGDNG`) inside `MapperUI.map(characteristicsUI: CharacteristicsUI): Characteristics`, preventing `imgFormat` from resetting to JPEG during characteristics updates.
- **[2026-03-31] Native RAW Sensor Dimension Resolution (`imageReaderRaw`):** Fixed `imageReaderRaw` initialization in `startSession` to query the maximum supported `RAW_SENSOR` size from `CameraCharacteristics` (instead of using JPEG preview dimensions), ensuring valid raw buffers are acquired and successfully exported to DNG files via `saveDngBytes`.
- **[2026-03-31] Beautiful Storage Path Formatting (`formatStoragePathForDisplay`):** Added `formatStoragePathForDisplay` in `:settings` `MapperUI.kt` to decode URL-encoded `content://` tree URIs (replacing `%3A` and `%2F` with human-readable separators like `Internal Storage / DCIM / Amigo`) and clean up file paths for polished presentation on the settings screen. Safeguarded `map(settingsUI)` so that formatted display strings do not overwrite stored file paths.
- **[2026-03-31] Isolated `rawStoragePath` & Strict DNG Visibility (`MapperUI.kt`):** Added `rawStoragePath` to `SettingsUI` so that formatting the display path never corrupts or alters the underlying storage path when modifying other settings (like resolution). Enforced strict `settings.supportsRaw == true` condition so that DNG format options are only shown when explicitly confirmed as supported by hardware.
- **[2026-03-31] Clean Relative Storage Path Parsing (`formatStoragePathForDisplay`):** Updated `formatStoragePathForDisplay` in `:settings` `MapperUI.kt` to correctly parse and extract human-readable relative paths (e.g. `DCIM/Amigo` or `DCIM/Camera`) from `content://` tree URIs and absolute paths, replacing technical URI noise with a clean folder path.
- **[2026-03-31] Precise Storage Volume & Subdirectory Extraction (`formatStoragePathForDisplay`):** Refined `formatStoragePathForDisplay` in `:settings` `MapperUI.kt` to split DocumentTree content URIs by colon `:` and extract the exact target subdirectory (such as `Alarms`, `Download`, or `DCIM/Camera`), preventing fallback to `DCIM` when selecting custom directories.
- **[2026-03-31] Fixed Folder Picker URI Assignment (`SettingsScreenSuccessNew.kt`):** Updated `dirPickerLauncher` in `SettingsScreenSuccessNew.kt` to update both `storagePath` and `rawStoragePath` with the selected `content://` URI, ensuring that newly picked directories are correctly saved to DataStore rather than being discarded.
- **[2026-03-31] Horizontal Orientation UI Element Rotation (`CameraScreenSuccess`):** Optimized `CameraScreenSuccess` for horizontal orientation. Detected landscape orientation via `LocalConfiguration` and applied smooth `animateFloatAsState` 90° clockwise rotation (`itemRotation`) to individual UI elements (buttons, icons, text labels, histogram). Updated `UniversalRadioGroup` and `ValueSelector` with `itemRotation` parameters so that individual content items rotate 90° clockwise on their own axes while keeping the slider/track layouts and indicator arrows horizontal.
- **[2026-03-31] Gesture Image Scaling & Panning (`CapturedImageScreen`):** Enhanced `CapturedImageScreen` with multi-touch pinch-to-zoom (1x to 5x range), bounded gesture panning/dragging when zoomed in, and double-tap gesture to instantly toggle zoom state between 1x and 2.5x.
- **[2026-03-31] Custom Switch Component in Settings (`SettingsScreenSuccessNew.kt`):**
  - Replaced standard Material 3 `Checkbox` items in `SettingsScreenSuccessNew` with a custom-styled Material 3 `Switch` (`SettingsSwitch`) featuring custom thumb and track colors (`Color(0xFF001d36)` navy thumb with a checkmark icon when checked, `Color(0xFFd3e2ff)` light blue track; `Color(0xFF8e9099)` grey thumb and `Color(0xFF35383f)` dark grey track when unchecked) matching the exact design specification.
- **[2026-03-31] Light Theme Addition & Dynamic Theme Switching (`Color.kt`, `Theme.kt`, `MapperUI.kt`, `MainActivity.kt`):**
  - Added a new **Light Theme** palette (`AppColors.light()`) with clean light background, surfaces, and dark text colors.
  - Added `"Light"` / `R.string.theme_light` option to theme selections in `MapperUI` and string resources (`values/strings.xml`, `values-ru/strings.xml`).
  - Wired up `MainActivity` to collect `settingsViewModel.uiState` and dynamically pass the selected theme palette (`AppColors.light()`, `AppColors.amoled()`, `AppColors.highContrast()`, or `AppColors.dark()`) to `FlashCameraTheme`, enabling instant live theme switching across the entire app.
- **[2026-03-31] Direct Theme References in Settings (`SettingsScreenSuccessNew.kt`):**
  - Removed local color variables and directly accessed `AppTheme.colors` properties (`background`, `surface`, `textPrimary`, `textSecondary`, `primaryAccent`, `stroke`, `recordActive`) throughout `SettingsScreenSuccessNew` for clean, idiomatic theme reactivity.

## App Data Flow Architecture (Optimal Routing & Zero Duplication)

### 1. Complete End-to-End Component Hierarchy (Nesting Tree) in `CameraScreenSuccess.kt`
- **`MainActivity.kt`** (Application Entry Point)
  - **`FlashCameraTheme`** (Provides `AppTheme.colors`, `dimens`, `typography`)
    - **`CameraScreen.kt`** (Permission handling, state observation, and navigation wrapper)
      - **`CameraScreenSuccess.kt`** (Main camera viewfinder and manual controls)
        - **Orientation Handler**: `OrientationEventListener` -> calculates `LayoutOrientation` -> computes smooth `topRotation` and `bottomRotation` animations (0°, 90°, 180°, 270°).
        - **Layer 1: Viewfinder & GL Preview**:
          - `Box` (Root container)
            - `CameraPreviewContainer`
              - `AndroidView` holding `TextureView` backed by `GLRenderer`
              - `GLRenderer` manages OpenGL ES 2.0/3.0 rendering, OES texture creation (`GL_TEXTURE_EXTERNAL_OES`), 3D raycasting touch processing, and Shader overlays (`GlMagnifierAdvanced`, `GlFocus`).
        - **Layer 2: Top Status Bar**:
          - `Row` (Top alignment, rotated via `graphicsLayer { rotationZ = topRotation }`)
            - `VectorShadow` (Flash toggle)
            - `VectorShadow` (Settings button -> opens `SettingsScreen`)
            - `Column` (Top-right corner labels):
              - `Text` (Active Resolution: e.g., "1920x1080")
              - `Text` (Active Save Format: "JPEG", "DNG", "JPEG+DNG")
        - **Layer 3: Bottom Control Panel**:
          - `Column` (Bottom Center alignment)
            - `ShutterBox` (Collapsible manual exposure box / EV slider row)
            - `HistogramCanvas` (Live RGB/Luma Histogram graph populated from 256-bucket array)
            - `Row` (Value Control Bar):
              - `VectorShadow` (Auto Mode toggle button)
              - `Box` (Dynamic Value Selector Container):
                - **Branch A**: If Category is WB or FOCUS and Auto Mode is ON -> `UniversalRadioGroup` (WB modes: AUTO, INCANDESCENT, DAYLIGHT, etc.; Focus modes: CONTINUOUS, MACRO, TOUCH, INFINITE).
                - **Branch B**: Otherwise (Manual mode or Shutter/ISO/Magnifier) -> `ValueSelector` (Horizontally scrollable `LazyRow` with `rememberSnapFlingBehavior`, `derivedStateOf` center item calculation, and indicator arrow `VectorShadow`).
            - `Row` (Category Selector Bar):
              - `UniversalRadioGroup` (Category items: SHUTTER, ISO, FOCUS, WB, MAGNIFIER).
            - `Row` (Shutter & Capture Control Row):
              - `VectorShadow` (Record Video button)
              - `VectorShadow` (Still Image Capture Shutter button -> triggers photo capture)
              - `VectorShadow` (Camera Switch / Mode Selector)

---

### 2. Downstream Data Flow: UI Action -> Repository & Hardware
1. **User Action**: User scrolls `ValueSelector`, clicks Auto toggle, or selects a mode in `CameraScreenSuccess`.
2. **UI State Creation**: `CameraScreenSuccess` constructs an updated `CharacteristicsUI` copy and calls `onSetCharacteristic(updatedUi)`.
3. **Event Dispatch**: `CameraScreen` forwards the action via `cameraViewModel.setEvent(CameraContract.Event.OnSetCharacteristics(characteristics))`.
4. **MVI ViewModel Handling**:
   - `CameraViewModel.handleEvent(Event.OnSetCharacteristics)` receives event.
   - `CameraViewModel.setCharacteristics`:
     a. Optimistically updates local UI state `_characteristicsInternal.value = characteristics` for instant UI feedback.
     b. Maps UI state to domain model: `domainModel = mapper.map(characteristics)`.
     c. Calls `launchHybridUseCase(block = setInputCharacteristicsUseCase.bind(SetInputCharacteristicsUseCase.Params(domainModel)))`.
5. **Hybrid UseCase Execution (`SetInputCharacteristicsUseCase`)**:
   - **Phase 1 (Sync Execution - Main Thread)**: `runSync` executes immediately without coroutine allocation: `cameraRepository.startPreviewCaptureRequest(params.characteristics)`.
   - **Phase 2 (Async Execution - Background Coroutine)**: `run` executes asynchronously: `settingsRepository.setCharacteristics(params.characteristics)` (persists to DataStore).
6. **CameraRepository & Camera2 Framework Pipeline**:
   - `startPreviewCaptureRequest(characteristics)` posts to `cameraThreadManager.handler` background thread with `UPDATE_TOKEN`.
   - `processPreviewCaptureRequest(characteristics)` executes on Handler thread:
     a. Applies color correction gains (WB Kelvin -> RGGB vector), exposure time (`CaptureRequest.SENSOR_EXPOSURE_TIME`), sensitivity (`CaptureRequest.SENSOR_SENSITIVITY`), focus distance (`CaptureRequest.LENS_FOCUS_DISTANCE`), and metering regions (`CONTROL_AE_REGIONS`, `CONTROL_AF_REGIONS`) on `persistentBuilder`.
     b. Submits updated request to hardware: `cameraSession.setRepeatingRequest(builder.build(), repeatingCaptureCallback, cameraThreadManager.handler)`.

---

### 3. Upstream Data Flow: Hardware Metadata & Streams -> UI Recomposition
1. **Camera2 Hardware Callbacks**:
   - Camera2 framework hardware session fires `repeatingCaptureCallback.onCaptureCompleted(session, request, result)`.
   - Extracts actual hardware parameters (`SENSOR_SENSITIVITY`, `SENSOR_EXPOSURE_TIME`, `LENS_FOCUS_DISTANCE`, `COLOR_CORRECTION_GAINS`, `CONTROL_AF_STATE`).
   - Throttles UI state updates (`now - lastUIUpdate > 100ms`).
   - Updates `_characteristicsFlow.update { ... }` in `CameraRepository`.
2. **Histogram Stream Pipeline**:
   - OpenGL ES / YUV buffer listener captures live preview frames.
   - `SubscribeCameraSettingsUseCase` maps raw buffer to 256-bucket histogram, applies Gaussian smoothing, and samples at 100ms intervals (`.sample(100.milliseconds)`).
3. **Reactive Stream Combination (`SubscribeCameraSettingsUseCase`)**:
   - `combine(cameraCharacteristicsFlow, histogramFlow, settingsFlow)` merges live hardware characteristics, histogram data, and DataStore settings into a unified `Characteristics` domain flow.
4. **ViewModel State Merging & Emitting**:
   - `CameraViewModel` collects from `SubscribeCameraSettingsUseCase()`.
   - Calls `mapper.merge(currentUi, domainCharacteristics)`:
     - **Auto Mode Rule**: Replaces UI string representations with actual hardware values (`actualIso`, `actualShutter`, `actualWbKelvin`, `actualFocusDistance`) for live metering feedback.
     - **Manual Mode Rule**: Preserves user's manual intent (`intent.shutterValue`, `intent.isoValue`), ignoring hardware variations.
   - Updates `_characteristicsInternal.update { ... }`.
5. **UI State Collection & Recomposition**:
   - `CameraScreenSuccess`: `val characteristics by characteristicsFlow.collectAsState()`.
   - Recomposes UI components with updated values.

---

### 4. Key Performance & Scrolling Smoothness Architecture
- **`ValueSelector` Scrolling**: Uses a `LazyRow` with `rememberSnapFlingBehavior`. `centerIndex` is computed via `derivedStateOf`.
- **Smoothness Rule**: Parameter changes during drag operations commit upon drag completion (`!isScrolling && wasDragged`) to prevent rapid mid-drag re-layouts.

---

## ARCHITECTURE DECISIONS LOG (CHANGELOG)
*This section is automatically maintained by the AI Agent.*
- **[2026-03-31] Complete End-to-End Component Hierarchy & Data Flow Map:** Documented full UI component nesting tree for `CameraScreenSuccess`, complete downstream/upstream data pipeline (UI -> ViewModel -> UseCase -> Repository -> Hardware & back), branching logic for Auto/Manual and priority exposure modes, and `ValueSelector` scrolling performance principles in `AGENTS.md`.
- **[2026-03-31] Anti-Garbage Code Rule:** Added strict policy to immediately roll back unsuccessful trial changes to prevent accumulating garbage code.
- **[2026-03-31] GLRenderer Architecture & Dynamic Texture Contract:** Documented GLRenderer rendering pipeline, touch raycasting, dynamic OES texture binding, and lifecycle release rules.
- **[2026-03-31] Camera Hardware Resolutions Persistence:** Documented and verified that upon camera startup (`OpenCameraUseCase`), available hardware resolutions are fetched from `CameraCharacteristics` and persisted via `SettingsRepository.setResolutions(...)` into the shared DataStore repository, allowing the `:settings` module to seamlessly retrieve and display them in `SettingsScreenSuccessNew`.
- **[2026-03-31] Resolution Availability in SettingsScreenSuccessNew:** Updated `SettingsRepository` in `:settings` and `SettingsScreenSuccessNew` to ensure all possible camera resolutions fetched from the common database (and a comprehensive fallback set of standard high resolutions) are available for selection in the resolution dropdown menu upon opening the settings screen.
- **[2026-03-31] Centralized Theme & App Theme Selection:** Confirmed that the application theme and styling system reside centrally in the `:shared` module (`AppTheme`, `AppColors`, `AppDimens`). Added an extensible App Theme selection dropdown (`ExposedDropdownMenuBox`) in `SettingsScreenSuccessNew` consistent with the app's design system and persisted via `SettingsRepository` and DataStore.
- **[2026-03-31] Dynamic Mode Icons with Titles & Correct Active Color (`CameraScreenSuccess.kt`, `MapperUI.kt`, `CharacteristicsUI.kt`):** Updated top category bar icon items (`IconItem`) to include parameter titles (`"WB"`, `"FOCUS"`) displayed above the mode icons (matching `TextRadioContent` layout) and corrected the active selection color to `AppTheme.colors.primaryAccent`.
- **[2026-03-31] Fixed Captured Bitmap Orientation in Preview:** Added `processCapturedBitmap(...)` in `CameraRepository.kt` to automatically rotate decoded captured bitmaps by `SENSOR_ORIENTATION` (and mirror horizontally if front camera), ensuring they display upright in `CapturedImageScreen`.
- **[2026-03-31] Settings-Driven Image Saving (`SaveCapturedImageUseCase`):** Updated `SaveCapturedImageUseCase` to retrieve the user's configured save path (`filePath`) and image format (`imgFormat`: `JPEG`, `RAW`, `JPEGRAW`) from `SettingsRepository`, passing them down to `CameraRepository.saveCapturedImage(...)` for correct persistence according to user preferences.
- **[2026-03-31] Dedicated Image Saving Function in `CameraRepository`:** Added `saveCapturedImage(...)` to `CameraRepository` in the `:camera` module supporting parameters for save path (`savePath`), image format (`ImgFormat`: JPEG, RAW/DNG, JPEGRAW), and resolution (`Dimensions`), with overloads for flexibility.
- **[2026-03-31] Refactored Single Capture Flow (`singleCapture()`):** Since `captureSurfaceJpeg` and `captureSurfaceRaw` are already configured in `SessionConfiguration` during `startSession(...)`, `applyState()` was cleaned up to manage repeating preview state (`persistentBuilder`) exclusively. `singleCapture()` now directly builds a dedicated `TEMPLATE_STILL_CAPTURE` request, applies preview settings via `applyStateToBuilder(...)`, adds `captureSurfaceJpeg` (and `captureSurfaceRaw` when format is RAW/JPEGRAW), and invokes `session.capture(...)` without mutating `persistentBuilder` or stopping preview repetition.
- **[2026-03-31] Fixed Null Bitmap Extraction in `singleCapture()`:** Resolved issue where `singleCapture()` produced null Bitmaps by updating `imageReaderJpeg` format from `ImageFormat.YUV_420_888` to `ImageFormat.JPEG`, adding YUV-to-Bitmap conversion support via `YuvImage` in `listenerJpeg`, and establishing fallbacks for zero resolution sizes during session startup.
- **[2026-03-31] Fixed Captured Bitmap Orientation in Preview:** Added `processCapturedBitmap(...)` in `CameraRepository.kt` to automatically rotate decoded captured bitmaps by `SENSOR_ORIENTATION` (and mirror horizontally if front camera), ensuring they display upright in `CapturedImageScreen`.
- **[2026-03-31] Fixed Infinite Loop on Preview Dismissal:** Added `clearCapturedBitmap()` in `CameraRepository` and called it inside `returnToSuccessState()` in `CameraViewModel` to clear the cached captured bitmap from `_characteristicsFlow` upon clicking Save or Cancel, preventing the preview screen from repeatedly popping up again.
- **[2026-03-31] Fixed Relative Path Parsing for Saving (`CameraRepository.kt`):** Added `getRelativePath(savePath)` helper to correctly extract MediaStore relative directories (e.g. `DCIM/Camera`) from absolute or custom paths specified in settings, preventing hardcoded overrides to `DCIM/FlashCamera`.
- **[2026-03-31] Added True DNG Creation via `DngCreator`:** Updated `imageReaderRaw` format from `YUV_420_888` to `ImageFormat.RAW_SENSOR`. Implemented `ImageSaver.saveDng(...)` utilizing Android's `DngCreator` along with `TotalCaptureResult` and raw `Image` data from the capture session to produce valid Adobe DNG raw files when `DNG` or `JPEG+DNG` formats are selected.
- **[2026-03-31] Synchronized RAW Capture via `rawCaptureDeferred`:** Implemented `rawCaptureDeferred` (`CompletableDeferred<Image?>`) in `CameraRepository.kt` to ensure asynchronous RAW frame arrival from `listenerRaw` is correctly synchronized and awaited before generating and saving Adobe DNG files.
- **[2026-03-31] Fixed Image Format Persistence in `:camera` SettingsRepository:** Updated `SettingsRepository.getCharacteristics()` and `subscribeSettings()` in the `:camera` module to retrieve and subscribe to `IMGFORMAT` from DataStore, ensuring that selected formats (`JPEG`, `DNG`, `JPEG+DNG`) are correctly passed to `SaveCapturedImageUseCase` and `CameraRepository`.
- **[2026-03-31] DNG Hardware Support Auto-Detection (`getCameraCharacteristics` & `OpenCameraUseCase`):** `CameraRepository.getCameraCharacteristics` queries `REQUEST_AVAILABLE_CAPABILITIES_RAW` from `CameraCharacteristics` and sets `supportsRaw` on `Characteristics`. `OpenCameraUseCase` reads `hardwareInfo.supportsRaw` and updates `SettingsRepository`.
- **[2026-03-31] Fixed Unstable DNG Selection in Settings (`MapperUI.kt`):** Fixed condition in `:settings` `MapperUI.kt` from `settings.supportsRaw == true` to `settings.supportsRaw != false`. Initial `null` values (before camera initialization) no longer override DNG selections or hide format options. Options are only hidden when `supportsRaw` is explicitly `false`.
- **[2026-03-31] Strict ViewModel Clean Architecture Rule:** Refactored `CameraViewModel` and `SettingsViewModel` so that ViewModels interact with repositories strictly through UseCases. Created `ClearCapturedBitmapUseCase` in `:camera` and restored `GetStorageInfoUseCase` in `:settings`, completely eliminating direct repository references inside ViewModels.
- **[2026-03-31] Automatic Clearing of Captured Image on Save & Smart Preview Trigger:** `CameraRepository.saveCapturedImage(...)` automatically clears `capturedBitmap` in the repository after saving. `CameraViewModel` compares `newBitmap != oldBitmap` before triggering `CameraState.CapturedPreview`, opening the preview window only when a genuinely new frame is captured.
- **[2026-03-31] Autonomous JPEG & RAW Image Readers (`listenerJpeg` & `listenerRaw`):** `listenerJpeg` decodes the captured JPEG frame and autonomously updates `_characteristicsFlow` with `capturedBitmap = bitmap`. `listenerRaw` autonomously buffers the RAW image frame into `lastRawImage` on `CameraRepository`, which is directly consumed by `saveCapturedImage` when exporting DNG files, eliminating unnecessary deferred objects.
- **[2026-03-31] Unified Shutter Trigger via `SetInputCharacteristicsUseCase`:** Pressing the shutter button in `CameraViewModel` now sets `isCaptureRequested = true` in characteristics and invokes `SetInputCharacteristicsUseCase`, routing photo capture through the standard unified characteristics pipeline and eliminating `SingleCaptureUseCase`.
- **[2026-03-31] Streamlined Single-Builder & Single-Method Pipeline:** Consolidated `applyStateToBuilder` and `applyState` into `startPreviewCaptureRequest`. Registered all capture surfaces (`previewSurface`, `histogramSurface`, `captureSurfaceJpeg`, `captureSurfaceRaw`) once in `startSession` on `persistentBuilder`. Single `persistentBuilder` handles both continuous preview (`session.setRepeatingRequest`) and photo capture (`session.capture`) based on `isCaptureRequested`. `ImageReader` listeners disconnect immediately in `finally` upon image acquisition to cancel subsequent capture requests.
- **[2026-03-31] Dynamic Target Management on `persistentBuilder` for Preview & Capture:** Refactored `processPreviewCaptureRequest` to use the exact same `persistentBuilder` and `cameraSession` for both `session.setRepeatingRequest` (preview) and `session.capture` (still photo). Capture surfaces (`captureSurfaceJpeg`, `captureSurfaceRaw`) are added temporarily to `persistentBuilder` when `isCaptureRequested == true` and immediately removed upon capture completion or failure, preventing ImageReader buffer overflow and keeping live preview updating smoothly at 30/60 fps.
- **[2026-03-31] Fixed Shutter Speed Sorting Desynchronization (`MapperUI.kt`):** Fixed sorting of `standardShutterSpeeds` in `:camera` `MapperUI.kt` from descending (`compareByDescending`) to ascending (`sortedBy { it.key }`), perfectly aligning `shutterItems` and `shutterPosition` with `CameraScreenSuccess.kt` (ensuring 1/125 and other shutter speeds trigger `startPreviewCaptureRequest` reliably without position index mismatches).
- **[2026-03-31] Lossless JPEG Buffering (`lastJpegBytes`):** `listenerJpeg` now extracts and buffers the raw compressed JPEG byte array into `lastJpegBytes` on `CameraRepository`, while simultaneously decoding a `Bitmap` for live preview in `CapturedImageScreen`. `saveCapturedImage` writes `lastJpegBytes` directly to disk using `ImageSaver.saveBytes(...)`, bypassing `lastCapturedBitmap` entirely during image export.
- **[2026-03-31] Independent Listener Detachment & Safe `lastRawImage` Acquisition:** Refactored `listenerJpeg` and `listenerRaw` to detach their respective `ImageReader` listeners independently in their `finally` blocks, ensuring `lastRawImage` is reliably captured for DNG export without race conditions or premature detachment. Updated `saveCapturedImage` to rely strictly on `lastJpegBytes` and `lastRawImage` without referencing `lastCapturedBitmap`.
- **[2026-03-31] Optimized `Image` Lifecycle Management (`listenerJpeg` & `listenerRaw`):** `listenerJpeg` now closes the acquired JPEG `Image` immediately in its `finally` block after copying its bytes into `lastJpegBytes`. `lastRawImage` (`Image`) is kept open safely during DNG export and closed immediately in `saveCapturedImage`'s `finally` block once `DngCreator` finishes writing.
- **[2026-03-31] Immediate RAW Byte Copying (`listenerRaw` & `saveDngBytes`):** `listenerRaw` now immediately copies RAW sensor bytes into `lastRawBytes` and calls `image.close()` instantly. `saveDngBytes` uses `DngCreator.writeByteBuffer(...)` to write DNG files from the buffered byte array, eliminating open `Image` references across threads and ensuring `lastRawBytes` is never null during export.
- **[2026-03-31] File Format Display on `CameraScreenSuccess`**: Added `imgFormat` (`JPEG`, `DNG`, `JPEG+DNG`) to `CharacteristicsUI` and `MapperUI`, and displayed it dynamically in English right below the resolution text in the top-right corner of `CameraScreenSuccess.kt`.
- **[2026-03-31] Preserving `supportsRaw` in `:camera` SettingsRepository & UseCase**: Updated `:camera` module's `SettingsRepository` (`getCharacteristics` and `subscribeSettings`) and `SubscribeCameraSettingsUseCase` to include `supportsRaw` from DataStore, preventing the DNG support flag from being lost or wiped out during settings or camera updates.
- **[2026-03-31] Fixed `imgFormat` Mapping in `MapperUI`**: Added `characteristicsUI.imgFormat` mapping back to domain `ImgFormat` (`JPEG`, `DNG`, `JPEGDNG`) inside `MapperUI.map(characteristicsUI: CharacteristicsUI): Characteristics`, preventing `imgFormat` from resetting to JPEG during characteristics updates.
- **[2026-03-31] Native RAW Sensor Dimension Resolution (`imageReaderRaw`):** Fixed `imageReaderRaw` initialization in `startSession` to query the maximum supported `RAW_SENSOR` size from `CameraCharacteristics` (instead of using JPEG preview dimensions), ensuring valid raw buffers are acquired and successfully exported to DNG files via `saveDngBytes`.
- **[2026-03-31] Beautiful Storage Path Formatting (`formatStoragePathForDisplay`):** Added `formatStoragePathForDisplay` in `:settings` `MapperUI.kt` to decode URL-encoded `content://` tree URIs (replacing `%3A` and `%2F` with human-readable separators like `Internal Storage / DCIM / Amigo`) and clean up file paths for polished presentation on the settings screen. Safeguarded `map(settingsUI)` so that formatted display strings do not overwrite stored file paths.
- **[2026-03-31] Isolated `rawStoragePath` & Strict DNG Visibility (`MapperUI.kt`):** Added `rawStoragePath` to `SettingsUI` so that formatting the display path never corrupts or alters the underlying storage path when modifying other settings (like resolution). Enforced strict `settings.supportsRaw == true` condition so that DNG format options are only shown when explicitly confirmed as supported by hardware.
- **[2026-03-31] Clean Relative Storage Path Parsing (`formatStoragePathForDisplay`):** Updated `formatStoragePathForDisplay` in `:settings` `MapperUI.kt` to correctly parse and extract human-readable relative paths (e.g. `DCIM/Amigo` or `DCIM/Camera`) from `content://` tree URIs and absolute paths, replacing technical URI noise with a clean folder path.
- **[2026-03-31] Precise Storage Volume & Subdirectory Extraction (`formatStoragePathForDisplay`):** Refined `formatStoragePathForDisplay` in `:settings` `MapperUI.kt` to split DocumentTree content URIs by colon `:` and extract the exact target subdirectory (such as `Alarms`, `Download`, or `DCIM/Camera`), preventing fallback to `DCIM` when selecting custom directories.
- **[2026-03-31] Fixed Folder Picker URI Assignment (`SettingsScreenSuccessNew.kt`):** Updated `dirPickerLauncher` in `SettingsScreenSuccessNew.kt` to update both `storagePath` and `rawStoragePath` with the selected `content://` URI, ensuring that newly picked directories are correctly saved to DataStore rather than being discarded.
- **[2026-03-31] Horizontal Orientation UI Element Rotation (`CameraScreenSuccess`):** Optimized `CameraScreenSuccess` for horizontal orientation. Detected landscape orientation via `LocalConfiguration` and applied smooth `animateFloatAsState` 90° clockwise rotation (`itemRotation`) to individual UI elements (buttons, icons, text labels, histogram). Updated `UniversalRadioGroup` and `ValueSelector` with `itemRotation` parameters so that individual content items rotate 90° clockwise on their own axes while keeping the slider/track layouts and indicator arrows horizontal.
- **[2026-03-31] Gesture Image Scaling & Panning (`CapturedImageScreen`):** Enhanced `CapturedImageScreen` with multi-touch pinch-to-zoom (1x to 5x range), bounded gesture panning/dragging when zoomed in, and double-tap gesture to instantly toggle zoom state between 1x and 2.5x.
- **[2026-03-31] Custom Switch Component in Settings (`SettingsScreenSuccessNew.kt`):**
  - Replaced standard Material 3 `Checkbox` items in `SettingsScreenSuccessNew` with a custom-styled Material 3 `Switch` (`SettingsSwitch`) featuring custom thumb and track colors (`Color(0xFF001d36)` navy thumb with a checkmark icon when checked, `Color(0xFFd3e2ff)` light blue track; `Color(0xFF8e9099)` grey thumb and `Color(0xFF35383f)` dark grey track when unchecked) matching the exact design specification.
- **[2026-03-31] Light Theme Addition & Dynamic Theme Switching (`Color.kt`, `Theme.kt`, `MapperUI.kt`, `MainActivity.kt`):**
  - Added a new **Light Theme** palette (`AppColors.light()`) with clean light background, surfaces, and dark text colors.
  - Added `"Light"` / `R.string.theme_light` option to theme selections in `MapperUI` and string resources (`values/strings.xml`, `values-ru/strings.xml`).
  - Wired up `MainActivity` to collect `settingsViewModel.uiState` and dynamically pass the selected theme palette (`AppColors.light()`, `AppColors.amoled()`, `AppColors.highContrast()`, or `AppColors.dark()`) to `FlashCameraTheme`, enabling instant live theme switching across the entire app.
- **[2026-03-31] Direct Theme References in Settings (`SettingsScreenSuccessNew.kt`):**
  - Removed local color variables and directly accessed `AppTheme.colors` properties (`background`, `surface`, `textPrimary`, `textSecondary`, `primaryAccent`, `stroke`, `recordActive`) throughout `SettingsScreenSuccessNew` for clean, idiomatic theme reactivity.

   - **Preview & Settings Updates**: Flow through `startPreviewCaptureRequest(characteristics)` using `persistentBuilder` and `session.setRepeatingRequest(...)`.
   - **Shutter Capture (`isCaptureRequested = true`)**:
     - Temporarily attaches `listenerJpeg` and `listenerRaw`.
     - Adds capture surfaces (`captureSurfaceJpeg`, `captureSurfaceRaw`) to `persistentBuilder`.
     - Executes `session.capture(...)`.
     - Instantly closes images and detaches listeners in `finally` blocks, buffering raw bytes into `lastJpegBytes` and `lastRawBytes`.
   - **Export (`saveCapturedImage`)**:
     - Writes `lastJpegBytes` and `lastRawBytes` directly to disk via `ImageSaver.saveBytes` and `ImageSaver.saveDngBytes`.
     - Clears state via `clearCapturedBitmap()`.

5. **Reactivity & State Flow**:
   - Hardware updates and preview bitmaps stream reactively back to ViewModels via `_characteristicsFlow` / `subscribeCameraSettingsUseCase()`, mapped by `MapperUI` for Compose UI rendering without duplication.

- **[2026-03-31] Code Consistency Rule & Real-time Auto WB Display:** Added explicit project rule requiring all new code to maintain strict consistency with existing codebase patterns. Updated `CameraRepository.rgbToKelvin` and `MapperUI` so White Balance in Auto mode displays real-time camera metadata rounded to the nearest integer Kelvin (e.g., "5000K"), matching ISO and Shutter auto-display behavior.
- **[2026-03-31] Color Temperature (Kelvin) Calculation Fix (`CameraRepository.kt`):** Fixed `rgbToKelvin` and `convertTemperatureToRggb`. `COLOR_CORRECTION_GAINS` represents white balance compensation gains ($G_{red}, G_{blue}$), which are inversely related to light color components ($I_{blue}/I_{red} \propto G_{red}/G_{blue}$). Fixed binary search ratio in `rgbToKelvin` and inverted gain calculations in `convertTemperatureToRggb` so that Kelvin temperature values (both Auto measurement and Manual setting) accurately represent blackbody illuminant temperatures.
- **[2026-03-31] Fixed Duplicate Auto WB Item Selection (`MapperUI.kt`):** Fixed issue where the first two items in the Auto White Balance selector were selected simultaneously. Filtered out `CONTROL_AWB_MODE_OFF` from `characteristics.wbModeItems` before mapping (preventing `OFF` from falling into `else -> ModeItem.WbItem.AUTO`) and applied `.distinctBy { it.id }`, ensuring all items in `wbModeItems` have unique `ModeItem.WbItem` IDs.
- **[2026-03-31] Fixed Sensor Calibration & Green Tint in Manual WB (`CameraRepository.kt`):** Resolved green tint issue during manual Kelvin adjustment. Raw camera sensors have ~2x higher green sensitivity, requiring gains to scale relative to the device's sensor baseline gains (`sensorBaseRGain`, `sensorBaseBGain`). Updated `convertTemperatureToRggb` and `rgbToKelvin` to calculate illuminant compensation relative to D65 daylight baseline scaled by live sensor gains, and preserved valid `COLOR_CORRECTION_TRANSFORM` matrices. Manual Kelvin now smoothly transitions from Cold Blue (2000K) through Daylight Neutral (6500K) to Warm Red (10000K) without any green cast.
- **[2026-03-31] Fixed Infinite Loop on Captured Image Preview (`CameraViewModel.kt` & `CameraRepository.kt`):** Added `subscriptionJob` cancellation and `lastHandledBitmap` tracking in `CameraViewModel` to prevent duplicate or looping emission of `CapturedPreview` states from multiple concurrent collection coroutines or repeated reactive emissions. Cleared `lastJpegBytes` and `lastRawBytes` in `CameraRepository.clearCapturedBitmap()`.
- **[2026-03-31] Hybrid Storage & Persistable SAF Rights (`SettingsScreenSuccessNew.kt` & `ImageSaver`):** Implemented a hybrid storage strategy. Added `takePersistableUriPermission` in `SettingsScreenSuccessNew.kt` upon picking a folder via `OpenDocumentTree()` to persist Uri permissions across app restarts. Updated `ImageSaver.getRelativePath` in `CameraRepository.kt` to extract relative storage paths (e.g. `DCIM/Camera`) from `content://` URIs, ensuring seamless fallback to `MediaStore` if SAF tree permissions are lost or revoked.
- **[2026-03-31] Robust Camera Lifecycle Management (`CameraScreen.kt`):** Implemented `LifecycleEventObserver` in [CameraScreen.kt](file:///E:/ANDROIDprj/YESFlashCamera/camera/src/main/java/com/yes/camera/presentation/ui/CameraScreen.kt) observing `ON_PAUSE` to cleanly invoke `renderer.glSurfaceView?.onPause()`, null out `surface`, and dispatch `OnCloseCamera`, and `ON_RESUME` to invoke `renderer.glSurfaceView?.onResume()`. This guarantees EGL context and surface destruction/recreation upon backgrounding and restoring, preventing preview freezing.
- **[2026-03-31] GLRenderer Pause/Resume & Rebuildable Shaders (`GLRenderer.kt`, `ShaderProgram.kt`):** Added `onPause()` and `onResume()` methods to [GLRenderer.kt](file:///E:/ANDROIDprj/YESFlashCamera/camera/src/main/java/com/yes/camera/presentation/ui/custom/gles/GLRenderer.kt) where `onPause()` safely queues `release()` on the GL thread (`queueEvent`) and pauses the GL surface view, while `onResume()` resumes the GL surface view (triggering `onSurfaceCreated` and `createSurfaceTexture`). Updated [ShaderProgram.kt](file:///E:/ANDROIDprj/YESFlashCamera/camera/src/main/java/com/yes/camera/presentation/ui/custom/gles/ShaderProgram.kt) so `useProgram()` automatically rebuilds shader programs if `programId == 0`, making full-lifecycle release and recreation 100% safe and leak-free.
- **[2026-03-31] Auto-Reloading Magnifier Texture on Resume & Real-time Focus Tint (`GLMagnifierAdvanced.kt`):** Updated `draw()` in [GLMagnifierAdvanced.kt](file:///E:/ANDROIDprj/YESFlashCamera/camera/src/main/java/com/yes/camera/presentation/ui/custom/gles/GLMagnifierAdvanced.kt) to check `if (tintColorChanged || textureHandle[0] == 0)` and assign `currentTintColor = pendingTintColor` directly, ensuring the magnifier tint color always updates in real-time according to the current focus state and correctly reloads after resume.
- **[2026-03-31] 3x3 Golden Ratio Grid & True Physical Ground Level Horizon (`GoldenRatioGridOverlay.kt`):**
  - Implemented `GoldenRatioGridOverlay` drawing fine 3x3 Golden Ratio (Phi) grid lines ($0.382 : 0.618$) with subtle contrast drop shadows and 4 golden power point crosshairs/dots at the grid intersections.
  - Implemented true physical ground level horizon line calculating `groundAngle = atan2(-ax, ay)`. The horizon line remains **100% parallel to the physical ground of the earth** in 2D space across all device rotations (Portrait, Landscape Left, Landscape Right, Reverse Portrait).
  - Fixed camera frame reference ticks dynamically orient with active cardinal rotation, turning active green (`#81C784`) when camera is aligned with ground ($\pm 1.2^\circ$).
  - Integrated `gridEnabled` state into `Settings`, `SettingsUI`, DataStore persistence, `SettingsRepository`, `Characteristics`, `CharacteristicsUI`, `CameraPreviewContainer`, and `SettingsScreenSuccessNew`.
- **[2026-03-31] Fixed Reset of `isAutoForSelectedCategory` on `CameraScreenSuccess` Open:**
  - Subscribed to manual control settings (`ISOVALUE`, `SHUTTERVALUE`, `WBVALUE`, `WBMODE`, `FOCUSVALUE`, `FOCUSMODE`) in `SettingsRepository.subscribeSettings()`.
  - Updated `SubscribeCameraSettingsUseCase` to merge persisted manual values from DataStore (`settings`) into `base` characteristics.
  - Updated `MapperUI.merge()` to sync `isShutterAuto`, `isIsoAuto`, `isWbAuto`, `isFocusAuto` from hardware mapped reality, preserving persisted manual modes across screen openings.
- **[2026-03-31] GPS EXIF Metadata Saving & Active Location Fix (`CameraLocationProvider`, `CameraRepository`, `Settings`):**
  - Added `saveLocation` setting across `:settings` and `:camera` modules, persisted via DataStore and toggled via `SettingsScreenSuccessNew` with runtime location permission requests (`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`).
  - Updated `CameraLocationProvider` to actively request fresh coordinates via `LocationManager.requestLocationUpdates()` and `startTracking()` / `stopTracking()` lifecycle hooks in `CameraRepository`.
  - Fixed `SubscribeCameraSettingsUseCase` by adding `saveLocation = settings.saveLocation` to the settings combination flow, ensuring the active location setting from DataStore successfully reaches the camera repository during image capture.
  - Configured Camera2 capture requests with `CaptureRequest.JPEG_GPS_LOCATION` and `DngCreator.setLocation(location)` for native hardware JPEG and DNG raw EXIF GPS embedding, with `ExifInterface` fallback during image export.
