# Kshetrajna (क्षेत्रज्ञ)
### Capture What Matters.

Kshetrajna is a precision field camera, geodetic documentation system, and private photo journal engineered for Android and the modern web. Inspired by the philosophical concept of the observer or knower of the field, Kshetrajna is built for professionals, researchers, surveyors, and field workers who require authentic, tamper-evident visual records accompanied by verified temporal and spatial provenance.

Whether documenting geological surveys, ecological fieldwork, property inspections, agricultural milestones, architectural progress, or personal observations, Kshetrajna ensures every image preserves genuine context without fabrication or intrusive cloud telemetry.

---

## Design Philosophy

The product embodies four core tenets:

1. **Authenticity Over Fabrication**: If a satellite lock is weak or unavailable, Kshetrajna records genuine timestamps and clearly flags location status. It never invents, estimates, or synthesizes coordinates.
2. **Non-Destructive Provenance**: The pristine camera original is permanently stored and preserved. Stamped versions are generated as independent derivatives, allowing users to inspect original frames at any time.
3. **Absolute Privacy by Default**: All records, coordinates, notes, and photos reside exclusively in secure on-device storage. There is zero external telemetry, zero mandatory cloud accounts, and zero background analytics.
4. **Instrument-Grade Precision**: The interface blends deep forest green, warm gold, warm ivory, and technical white with precision cyan-blue geodetic accents, providing high readability in both bright outdoor sunlight and low-light field conditions.

---

## Distribution Modes

Kshetrajna is architected to launch across three primary deployment channels:
1. **Google Play Store**: Native Android App Bundle (`.aab`) with R8 optimization, scoped storage, and strict permission compliance.
2. **Standalone APK**: Sideloadable Android package for direct distribution and enterprise field deployments.
3. **Progressive Web App (PWA)**: Zero-install, cross-platform web application located in `/pwa` with offline service workers, web camera capture, HTML5 canvas timestamping, and IndexedDB local journal.

---

## Key Capabilities

### 1. Viewfinder and Framing Experience
* **Centred Shutter Architecture**: A prominent 72dp capture control is mathematically locked to the horizontal centre of the bottom control deck, ensuring natural ergonomics during one-handed or gloved field operation.
* **CameraX Pipeline**: Hardware-accelerated preview rendering with automatic lens switching, tap-to-focus support, and zero-shutter-lag capture.
* **Composition Reticle**: Rule-of-thirds compositional grid for alignment, horizon leveling, and structural documentation.
* **Lighting Controls**: Multi-state flash control (Off, Auto, Always On) and sensor readiness monitoring.

### 2. Geodetic Telemetry and Topological Radar
* **Integrated Location Indicator**: A compact, non-intrusive status strip built into the top bar showing live GPS fix status, estimated accuracy radius, and satellite readiness without obscuring the live preview.
* **Topological Compass and Radar**: A custom canvas-rendered reticle providing cardinal orientation (N, S, E, W), range rings, and dynamic accuracy circles without dependency on external mapping services.
* **Dual Geodetic Notation**: Coordinates are calculated and displayed in both Degrees-Minutes-Seconds (DMS) notation and Decimal Degrees (DD) with high mathematical precision.
* **Fix Provenance**: Detailed telemetry inspection displaying fix age, altitude above sea level, and reverse-geocoded terrestrial locality.

### 3. Non-Destructive Visual Stamping
* **Branded Stamp Banner**: Generates an export-ready derivative featuring the Kshetrajna insignia, Sanskrit wordmark, tagline, verified capture date and time, GPS coordinates, accuracy threshold, and custom field notes.
* **Dual Persistence Pipeline**: Both the raw original and the stamped asset are saved as separate files on the filesystem, linked by an internal database record.

### 4. Photographic Import and EXIF Harmonization
* **Existing Photo Analysis**: Import previously captured photographs via the system Photo Picker.
* **Automated EXIF Extraction**: Automatically extracts genuine embedded capture dates, camera hardware details, and embedded GPS tags from existing files.
* **Fallback Resolution**: If an imported photo lacks embedded location metadata, users can choose to stamp it with their current device location or save it cleanly without location data.

### 5. Field Journal and Archive
* **Local Database Storage**: Powered by an on-device SQLite database via Android Room with indices optimized for fast searching and filtering.
* **Inspection Studio**: High-resolution zoomable photo viewer with toggleable views between the raw original and stamped derivative, accompanied by an interactive geodetic radar map.
* **Native Sharing**: Export or share verified field reports and images directly to other applications through Android's FileProvider.

---

## Technical Architecture

* **Application ID**: `com.kshetrajna.app`
* **Target SDK**: Android 16 (API 36) / Min SDK: Android 7.0 (API 24)
* **Framework**: Native Android (100% Kotlin), Jetpack Compose, Material Design 3.
* **Camera Engine**: Android Jetpack CameraX.
* **Database**: Android Room 2.7.0 (SQLite) with schema migrations.
* **Image Loading**: Coil Compose.
* **PWA Engine**: HTML5, ES6 JavaScript, Canvas 2D API, Service Worker Cache API, IndexedDB.

---

## Building and Releasing

### Prerequisites
* JDK 17 or higher
* Android SDK Platform 36

### Build Debug APK
```bash
./gradlew assembleDebug
```
The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

### Build Release APK
```bash
./gradlew assembleRelease
```

### Build Google Play Store App Bundle (AAB)
```bash
./gradlew bundleRelease
```
The App Bundle will be generated at `app/build/outputs/bundle/release/app-release.aab`.

### Generating a Release Signing Keystore
For Google Play Store submission:
```bash
keytool -genkeypair -v -keystore my-upload-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```
Set the environment variables before building:
* `KEYSTORE_PATH`: Path to your `.jks` file
* `STORE_PASSWORD`: Keystore password
* `KEY_PASSWORD`: Key password

---

## Deploying the Progressive Web App (PWA)

The `/pwa` directory is self-contained and zero-build. It can be served from any static web host or GitHub Pages:

1. In GitHub Repository Settings, navigate to **Pages**.
2. Select branch `main` and folder `/pwa` (or use GitHub Actions).
3. Open the published URL on any mobile device (Chrome, Safari, Edge).
4. Tap **"Add to Home Screen"** or **"Install App"** to use Kshetrajna as a native-like offline application!

---

## Permissions & Compliance

* **Camera (`android.permission.CAMERA`)**: Required to operate the hardware image sensor.
* **Precise Location (`android.permission.ACCESS_FINE_LOCATION`)**: Used exclusively to read GPS satellite coordinates at shutter release. Fully optional.
* **Approximate Location (`android.permission.ACCESS_COARSE_LOCATION`)**: Network-based location fallback.
* **Zero Storage Permissions**: Leverages scoped storage and the Android Photo Picker (`PickVisualMedia`).

See [PRIVACY_POLICY.md](./PRIVACY_POLICY.md) for the complete Play Store-compliant privacy disclosure.
