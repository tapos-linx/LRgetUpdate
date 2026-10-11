# 📜 LR Mass Downloader (ভূমি রেকর্ড ও জরিপ গণ-ডাউনলোডার)

[![Android Version](https://img.shields.io/badge/Android-7.0%2B%20(API%2024%2B)-047857?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20MD3-064E3B?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen?style=for-the-badge&logo=githubactions&logoColor=white)](https://github.com/tapos-cpl/LR-Mass-Downloader/actions)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

> **Specialized GovTech Land Records Mass Downloader for Bangladesh Land Records & Survey (DLR&S).**  
> Covers **Cumilla (কুমিল্লা)** and **Brahmanbaria (ব্রাহ্মণবাড়িয়া)** with sequential anti-freeze queue, automated 7-mouza batching, master ledger synthesis, direct phone storage integration (`Download/LandRecords/`), and automated cache clearing.

---

## 📥 One-Click APK Download

To install on your Android device with **one click**, use the direct download link below:

| Direct Download Link | Target Version | Size |
| :--- | :--- | :--- |
| [⬇️ **Download LRMassDownloader.apk (One-Click Direct Download)**](https://github.com/tapos-cpl/LR-Mass-Downloader/releases/latest/download/LRMassDownloader.apk) | v1.0.0 (Universal Android) | ~22 MB |

> 💡 **Why is this 1-click?**  
> Tapping the link above triggers an immediate raw binary download directly in your mobile browser (Chrome, Samsung Internet, Firefox) without navigating through GitHub release sub-pages or extra clicks.

---

## ⚡ Fast Installation Guide (Fix for Slow Installs)

If APK installation from GitHub ever takes too long or pauses on your phone, follow these quick tips:

1. **Tap the Download Link**: The file `LRMassDownloader.apk` will download to your `Downloads` folder.
2. **Open the File**: Tap **Open** from the download notification or locate it in your **Files / My Files** app.
3. **Allow Installation from Source** *(First-time only)*:
   - If prompted: *"For your security, your phone is not allowed to install unknown apps from this source"*, tap **Settings** ➔ toggle **Allow from this source** on.
4. **Bypass Play Protect Wait**:
   - If Google Play Protect displays a prompt: *"Unrecognized app: Install anyway?"*, tap **"Install anyway"**.
   - ⚠️ *Note:* If you wait for Play Protect to send the app to the cloud for deep analysis, it can pause for 30–60 seconds. Tapping **"Install anyway"** installs the application in **1 to 2 seconds**.
5. **Ready!** Open **LR Mass Downloader** from your app drawer.

---

## ✨ Key Features & Workflow

### 1. 🏛️ Pure Cascading Hierarchy & Complete Dataset
- **Cumilla District (কুমিল্লা - 17 Upazilas)**: Adarsha Sadar, Sadar Dakshin, Chandina, Daudkandi, Debidwar, Burichang, Brahmanpara, Chauddagram, Laksam, Muradnagar, Barura, Homna, Titas, Meghna, Monohargonj, Nangalkot, Lalmai.
- **Brahmanbaria District (ব্রাহ্মণবাড়িয়া - 9 Upazilas)**: Sadar, Sarail, Ashuganj, Kasba, Nabinagar, Nasirnagar, Bancharampur, Akhaura, Bijoynagar.
- Selecting a District resets and dynamically loads Upazilas.
- Selecting an Upazila immediately queries and displays the total verified mouza count.

### 2. ⚡ Automated 7-Mouza Sequential Batch Queue (Anti-Repeat)
- **Automatic 7-Mouza Pipeline**: Automatically selects the first 7 mouzas when an Upazila is chosen.
- **Sequential Auto-Progression**: Once a 7-mouza batch completes, the app **automatically queues the next 7 mouzas** that have not been downloaded yet.
- **Strict Anti-Duplication**: Prevents previously downloaded mouzas from repeating or downloading in infinite loops.
- **Auto-Completion**: Automatically compiles the Master Archive once all mouzas in the Upazila are finished.

### 3. 📑 One-Click "Select All Documents"
- Support for all four standard Bangladesh Land Survey record types:
  - **CS (Cadastral Survey / সি.এস)**
  - **SA (State Acquisition / এস.এ)**
  - **RS (Revisional Survey / আর.এস)**
  - **BRS / BS (Bangladesh Survey / বি.আর.এস / বি.এস)**
- Single button to select all 4 document types simultaneously for a comprehensive survey harvest.

### 4. 🛡️ Anti-Freeze Concurrency Limiter
- **Concurrency Cap**: Maximum of **2 concurrent tasks** running simultaneously.
- Prevents UI stuttering, coroutine deadlocks, and browser memory overload.
- Live Real-time Queue HUD with progress bar, active tasks count, speed telemetry, Pause, Resume, and Clear Queue controls.

### 5. 💾 Physical Phone Storage & One-Click Master Archive
- **Direct Public Folder**: All downloaded records are saved directly to:
  ```text
  📁 Internal Storage > Download > LandRecords /
  ```
- **Tri-Format Master Output**:
  - `*_Master_Land_Records_2026.json` (Structured JSON dataset)
  - `*_Master_Land_Records_2026.txt` (Formatted human-readable ledger)
  - `*_Master_Land_Records_2026.csv` (Tabular spreadsheet format)
- **Individual Mouza Files**: Every completed mouza has its own dedicated `.json` and `.txt` document saved in the same directory.
- **Android MediaScanner Indexing**: Every saved file triggers Android's `MediaScannerConnection`, making it immediately visible in Samsung My Files, Google Files, and file managers.

### 6. 🧹 Automated Cache & Memory Cleaning
- After saving the Master Archive to phone storage, the app automatically:
  - Clears temporary cache directories (`context.cacheDir` & `context.externalCacheDir`).
  - Releases heap allocation to keep the device fast and prevent memory accumulation.

### 7. 🔒 Offline Vault & In-App Ledger Viewer
- **Saved Records Vault**: Powered by Room Database for fast offline search and record auditing.
- **In-App Ledger Viewer**: Read the full synthesized Bangla ledger directly in the app.
- **Storage Access Framework (SAF)**: Export copies to SD cards or custom folders via Android system file picker.
- **System Share**: Share files directly via WhatsApp, Gmail, or Google Drive.

---

## 🛠️ Architecture & Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Kotlin 2.0+ (100% Kotlin DSL) |
| **UI Framework** | Jetpack Compose with Material Design 3 (MD3 GovTech Emerald & Slate Palette) |
| **Architecture** | MVVM (Model-View-ViewModel) + Clean Architecture |
| **Concurrency** | Kotlin Coroutines, Flow, and `Mutex` Concurrency Limiter |
| **Database** | Android Jetpack Room with Kotlin Symbol Processing (KSP) |
| **File Storage** | Android Scoped Storage (`MediaStore.Downloads`) & POSIX File I/O with MediaScanner |
| **CI / CD** | GitHub Actions Automated APK Builder & Release Pipeline |

---

## 🚀 Building from Source

### Prerequisites
- Android Studio Ladybug (2024.2+) or newer
- JDK 17 (Eclipse Temurin or OpenJDK)
- Android SDK Platform 36 / Build-Tools 36.0.0

### Build Commands

```bash
# Clone the repository
git clone https://github.com/tapos-cpl/LR-Mass-Downloader.git
cd LR-Mass-Downloader

# Grant execution rights to the Gradle wrapper
chmod +x gradlew

# Build Debug APK
./gradlew assembleDebug

# The generated APK will be located at:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🤖 GitHub Actions Automated Release Pipeline

This repository includes a preconfigured GitHub Actions workflow in `.github/workflows/build-apk.yml`.

Whenever code is pushed to `main` or a new tag (e.g. `v1.0.0`) is created:
1. GitHub Actions automatically checks out the repo and sets up JDK 17.
2. It compiles the Android application with Gradle.
3. It packages `LRMassDownloader.apk`.
4. It attaches the APK to the **GitHub Releases** page with a direct one-click download link.

---

## 📄 License & Credits

- Developed for Bangladesh Land Records & Survey (ভূমি রেকর্ড ও জরিপ অধিদপ্তর) automation.
- Licensed under the [MIT License](LICENSE).
