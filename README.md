# Nem Icons — ColorOS Icon Studio

A professional Android app for styling app icons in the OPPO ColorOS aesthetic.  
Built with Kotlin · Jetpack Compose · Material 3 · local image processing.

---

## ✨ Features

| Feature | Details |
|---|---|
| 🎨 **Gradient Recoloring** | Two-tone gradient with Sobel edge preservation |
| 🖼 **Style Modes** | Gradient, Tint, Hue Shift |
| 🔍 **Compare View** | Side-by-side original vs. styled |
| 📦 **Batch Processing** | Style hundreds of icons at once |
| 🌈 **Color Picker** | Full HSV wheel + HEX + RGB inputs |
| 🎞 **Wallpaper Palette** | Extract colors from any image |
| 💾 **Palette Library** | Save & reuse custom palettes |
| 📤 **Export PNG** | Individual icons with transparency |
| 📦 **Export ZIP** | All library icons in one file |
| 📱 **Export MTZ** | Xiaomi/HyperOS theme package |
| 🔒 **100% Local** | No internet, no APIs, no cloud |

---

## 🏗 Building the APK

### Option A — GitHub Actions (recommended, automatic)

1. Push this project to a GitHub repository  
2. Go to **Actions** tab → **Build APK** workflow  
3. Click **Run workflow** or just push to `main`  
4. Download `NemIcons-debug.apk` from the **Artifacts** section of the completed run

### Option B — Android Studio (local build)

```bash
# Prerequisites: Android Studio with SDK 34, JDK 17
git clone <your-repo-url>
cd NemIcons

# Option 1: Android Studio → Open project → Run 'app'
# Option 2: Command line
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### Option C — Command line with SDK installed

```bash
# Set ANDROID_HOME
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools

./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

> **Note**: The `gradle-wrapper.jar` file is not included in this repo (it's binary).  
> GitHub Actions uses `android-actions/setup-android` which handles this automatically.  
> For local builds, run `gradle wrapper --gradle-version=8.6` once to generate it,  
> or download from: https://services.gradle.org/distributions/gradle-wrapper.jar

---

## 📁 Project Structure

```
NemIcons/
├── app/src/main/
│   ├── java/com/nem/icons/
│   │   ├── MainActivity.kt                     # Entry point
│   │   ├── domain/model/Models.kt              # Data classes
│   │   ├── utils/
│   │   │   ├── IconProcessor.kt                # Core styling engine
│   │   │   ├── ExportUtils.kt                  # PNG / ZIP / MTZ export
│   │   │   └── PaletteExtractor.kt             # Wallpaper color extraction
│   │   ├── data/repository/
│   │   │   ├── AppRepository.kt                # Installed apps
│   │   │   ├── IconLibraryRepository.kt        # Saved icons
│   │   │   └── PaletteRepository.kt            # Palettes
│   │   └── ui/
│   │       ├── MainViewModel.kt                # State management
│   │       ├── theme/Theme.kt                  # Material 3 dark theme
│   │       ├── components/
│   │       │   ├── CommonComponents.kt         # Shared UI
│   │       │   └── ColorPicker.kt              # HSV/HEX/RGB picker
│   │       └── screens/
│   │           ├── MainNavigation.kt           # Bottom nav
│   │           ├── AppsScreen.kt               # App list + batch
│   │           ├── EditorScreen.kt             # Icon editor
│   │           ├── LibraryScreen.kt            # Saved icons
│   │           ├── PalettesScreen.kt           # Palette manager
│   │           └── SettingsScreen.kt           # Settings
│   └── res/...
├── .github/workflows/build.yml                 # Auto-build workflow
└── README.md
```

---

## 🎨 Icon Processing Engine

`IconProcessor.kt` applies a **non-destructive ColorOS-style recolor**:

1. **Edge extraction** — Sobel operator detects logo outlines
2. **Highlight/shadow separation** — luminance thresholding
3. **Gradient mapping** — gradient replaces midtones only
4. **Detail preservation** — edges, highlights, shadows are retained at full weight
5. **Color adjustments** — brightness / contrast / saturation via `ColorMatrix`

This preserves the original icon's shape, logo, and 3D feel while applying the two-tone gradient.

---

## 📋 Requirements

- Android 8.0+ (API 26)
- ~15 MB storage for the app
- Storage permission for wallpaper import and export

---

## 📱 MTZ Format

The MTZ export creates a ZIP with:
```
theme.mtz (renamed .zip)
├── description.xml    — Theme metadata
├── preview.png        — First icon as preview
└── icons/
    ├── com.example.app1.png
    ├── com.example.app2.png
    └── ...
```
Compatible with Xiaomi/HyperOS theme manager.
