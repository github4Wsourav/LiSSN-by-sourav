# 🎵 LiSSN by sourav

> **A next-generation, high-fidelity music streaming client for Android and Desktop created by Sourav.**  
> Powered by Compose Multiplatform and featuring a natively integrated **FxSound 64-bit DSP Audio Enhancer**, **Google Account Login**, **Listen Together (Room Host/Join)**, and **1-Click Offline Downloads**.

[![Latest Release](https://img.shields.io/github/v/release/github4Wsourav/LiSSN-by-sourav?style=for-the-badge&logo=github)](https://github.com/github4Wsourav/LiSSN-by-sourav/releases/latest)
[![Download APK](https://img.shields.io/badge/Download_Android_APK-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://github.com/github4Wsourav/LiSSN-by-sourav/releases/latest/download/LiSSN-Android.apk)
[![Download Desktop](https://img.shields.io/badge/Download_Windows-0078D4?style=for-the-badge&logo=windows&logoColor=white)](https://github.com/github4Wsourav/LiSSN-by-sourav/releases/latest/download/LiSSN-Windows.zip)
[![FxSound Powered](https://img.shields.io/badge/FxSound-DSP_Engine-FF2A6D?style=for-the-badge&logo=soundcharts&logoColor=white)](https://github.com/fxsound2/fxsound-app)

---

### 📥 Direct Downloads

| Platform | Download Link | Description |
|---|---|---|
| 📱 **Android Phone / Tablet** | **[Download the latest Android APK](https://github.com/github4Wsourav/LiSSN-by-sourav/releases/latest/download/LiSSN-Android.apk)** | ARM64 APK for Android 12+ |
| 💻 **Windows Laptop** | **[Download the latest Windows app](https://github.com/github4Wsourav/LiSSN-by-sourav/releases/latest/download/LiSSN-Windows.zip)** | Extract the ZIP, then launch the included LiSSN executable |

Android may ask you to allow your browser or file manager to install apps. Approve that prompt to install the downloaded APK.

Releases are built automatically when a `vX.Y.Z` tag is pushed. Each release contains both assets above. To publish a version, push its tag (for example, `v2.5.1`) and wait for the **Build and Release LiSSN** workflow to finish successfully. A release is published only after both platform builds succeed.

---

## 🌟 Highlights & Key Features

### 1. 🔐 Google Account Login
- Direct sign-in using your standard **Google Account**.
- Secure session handling that keeps your library, playlists, and history seamlessly synchronized.
- Zero reliance on Discord or Spotify accounts.

### 2. 🎛️ Integrated FxSound 64-bit DSP Audio Enhancer
Reverse-engineered from [`fxsound2/fxsound-app`](https://github.com/fxsound2/fxsound-app) and ported directly into the audio rendering pipeline:
- **Master Power Switch**: One-tap toggle directly in the playback bottom sheet and Settings. Turn it ON/OFF at any time while a song is playing to immediately hear the drastic improvement!
- **Voice Clarity & Fidelity (Harmonic Exciter)**: High-frequency harmonic restoration (3.5 kHz – 12 kHz) bringing vocals and lead instruments crystal-clear into the foreground.
- **Dynamic Boost (Loudness Maximizer)**: Soft-knee compressor and peak limiter that maximizes energy and volume punch without digital distortion or clipping.
- **HyperBass Punch**: Resonant sub-bass filtering (60 Hz – 120 Hz) providing deep, tight low-end impact.
- **3D Spatial Surround & Ambience**: Mid/Side (M/S) stereo field matrix with micro-delays for an immersive, expansive acoustic stage.
- **10-Band Graphic Equalizer**: Precision control across 31 Hz, 62 Hz, 125 Hz, 250 Hz, 500 Hz, 1 kHz, 2 kHz, 4 kHz, 8 kHz, and 16 kHz.
- **Built-in Presets**: *Music (Studio)*, *Voice Boost (Clarity)*, *Bass Punch (HyperBass)*, *Dynamic Maximizer*, and *3D Spatial Surround*.

### 3. 🖼️ Pure Music with High-Resolution Thumbnails
- Completely removed lyrics panels, lyrics offsets, and video canvas overhead.
- Enjoy an uncluttered, distraction-free player interface with high-resolution album artwork, smooth seekbars, and responsive playback controls.

### 4. ⬇️ Built-In 1-Click Song Downloader
- Download any song directly to your device with full metadata and embedded album art.
- Offline library access whenever you are on the go.

### 5. 👥 Play & Listen Together (Host & Join)
- **Host a Room**: Generate a simple room code and stream playback in perfect sync.
- **Join a Room**: Enter a friend's room code to listen together in real time.

### 6. 🚫 Clean, Tracking-Free & Uninterrupted
- No SponsorBlock delays or query timeouts.
- No third-party Discord RPC or Spotify background daemons.
- Completely free and ad-free experience.

### 7. 🎨 Identical SimpMusic Architecture & Dashboard
- Preserves 100% of the rich recommendations, Top Charts, explore categories, artist pages, album screens, and sleek Compose Multiplatform UI styling (including the user-favorite dark Spotify theme).

---

## 🛠️ Architecture & Tech Stack

```
LiSSN by sourav
├── androidApp/          # Android application target & manifests
├── desktopApp/          # Windows / macOS / Linux desktop target
├── composeApp/          # Shared Compose Multiplatform UI, screens & themes
│   ├── ui/screen/
│   │   ├── home/        # Dashboard, Top Charts, FxSound Settings
│   │   ├── player/      # Now Playing Spotify/Apple/SoundCloud styles
│   │   └── other/       # Artist, Album, Library, Credit
│   └── component/       # FxSoundSettingsGroup, ModalBottomSheet, Player
├── core/
│   ├── domain/          # FxSoundConfig, DataStoreManager, Media interfaces
│   ├── media/
│   │   ├── media3/      # ExoPlayer with FxSoundAudioProcessor
│   │   └── media-jvm/   # libmpv / ffmpeg DSP filter graph
│   ├── service/         # YouTube Music scraper, ListenTogether client
│   └── data/            # Room DB, DataStore persistence & reactive binders
└── .github/workflows/   # Automated CI/CD release pipeline
```

---

## 🚀 Building from Source

### Prerequisites
- JDK 21+
- Android SDK (for Android build)

### Build Android APK
```bash
./gradlew :androidApp:assembleDebug
```
Output: `androidApp/build/outputs/apk/debug/androidApp-debug.apk` (debug-signed APK)

### Build Desktop Application
```bash
./gradlew :desktopApp:createDistributable
```
Output: `desktopApp/build/compose/binaries/main/` (Windows application image on Windows)

---

## 👤 Author
**Sourav** — Creator & Developer  
GitHub: [@github4Wsourav](https://github.com/github4Wsourav)  
Repository: [LiSSN-by-sourav](https://github.com/github4Wsourav/LiSSN-by-sourav)

---

## 📄 License
Licensed under the GNU General Public License v3.0 (GPL-3.0).
