# 📜 LR Mass Downloader (ভূমি রেকর্ড ও জরিপ গণ-ডাউনলোডার)

[![Android Version](https://img.shields.io/badge/Android-7.0%2B%20(API%2024%2B)-047857?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20MD3-064E3B?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Build Status](https://img.shields.io/badge/Build-Auto--Release-brightgreen?style=for-the-badge&logo=githubactions&logoColor=white)](https://github.com/tapos-cpl/LR-Mass-Downloader/actions)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

> **Specialized GovTech Land Records Mass Downloader for Bangladesh Land Records & Survey (DLR&S).**  
> Covers **Cumilla (কুমিল্লা - 17 Upazilas)** and **Brahmanbaria (ব্রাহ্মণবাড়িয়া - 9 Upazilas)** with sequential anti-freeze queue, automated 7-mouza batching, master ledger synthesis, direct phone storage integration (`Download/LandRecords/`), and automated cache clearing.

---

## 📥 One-Click APK Direct Download

To install on your Android device with **one click**, use the direct download links below:

| Download Type | Direct Link | Description |
| :--- | :--- | :--- |
| 🚀 **Direct 1-Click APK (Latest)** | [⬇️ **Download LRMassDownloader.apk**](https://github.com/tapos-cpl/LR-Mass-Downloader/releases/latest/download/LRMassDownloader.apk) | Always downloads the newest auto-built APK binary directly to your phone. |
| 🔗 **Relative Repo Link** | [⬇️ **Download Latest Release APK**](../../releases/latest/download/LRMassDownloader.apk) | Works within any GitHub repository or fork without 404 errors. |
| 📦 **All Releases & Versions** | [📂 **View All Releases & Version History**](../../releases) | Browse all previous versions and changelogs. |
| ⚙️ **Live Build Status** | [📊 **GitHub Actions Live Runs**](../../actions) | Real-time CI/CD build logs and artifact downloads. |

---

## 💡 How the 404 Error is Solved

If you previously encountered a **404 Not Found** error when clicking the APK download link, here is why and how it is resolved:

1. **GitHub Actions Build Duration**:
   - Whenever code is committed or pushed to GitHub, GitHub Actions starts a new build in the cloud.
   - It takes approximately **1 to 2 minutes** to compile the Android app and upload the APK to GitHub Releases.
   - While the build is running, GitHub does not have the file yet and will return `404`.
   - **Solution**: Visit the [GitHub Actions Tab](../../actions) to watch the build finish. Once the green checkmark appears, the download link goes live immediately!
2. **Repository Name Compatibility**:
   - If your repository on GitHub is named differently (for example `lr-mass-downloader` in lowercase or under another GitHub account), a hardcoded link will return 404.
   - **Solution**: The relative link `[Download Latest Release APK](../../releases/latest/download/LRMassDownloader.apk)` automatically adapts to your exact repository name!

---

## ⚡ Fast Installation Guide (Fix for Slow Installs)

If APK installation from GitHub ever takes too long or pauses on your phone:

1. **Tap the Download Link**: The file `LRMassDownloader.apk` will download to your `Downloads` folder.
2. **Open the File**: Tap **Open** from the download notification or locate it in your **Files / My Files** app.
3. **Allow Installation from Source** *(First-time only)*:
   - If prompted: *"For your security, your phone is not allowed to install unknown apps from this source"*, tap **Settings** ➔ toggle **Allow from this source** on.
4. **Bypass Play Protect Wait**:
   - If Google Play Protect displays a prompt: *"Unrecognized app: Install anyway?"*, tap **"Install anyway"**.
   - ⚠️ *Note:* If you wait for Play Protect to send the app to the cloud for deep analysis, it can pause for 30–60 seconds. Tapping **"Install anyway"** installs the application in **1 to 2 seconds**.
5. **Ready!** Open **LR Mass Downloader** from your app drawer.

---

## 🔄 Automated Versioning & GitHub Sync System

This project features an automated versioning and release system:

### How it works:
1. **Dynamic Versioning**:
   - The app's version is configured in `version.properties` and dynamically calculated in `app/build.gradle.kts`.
   - In GitHub Actions CI/CD (`.github/workflows/build-apk.yml`), every push automatically increments the version (e.g. `1.0.1`, `1.0.2`, `1.0.3`...) based on `github.run_number`.
   - A unique Git tag (e.g. `v1.0.4`) and a GitHub Release are automatically generated and marked as `latest`.
2. **In-App Version Indicator**:
   - The app UI displays its exact active version: e.g. `Installed: v1.0.3 (Build 103)` in the **APK Dialog** and **GovTech Header**.
3. **One-Command Auto Version Bump & Push**:
   You can run the included helper script in terminal:
   ```bash
   ./scripts/auto_commit_and_push.sh "Your update message"
   ```
   This will:
   - Automatically increment the build number and patch version in `version.properties`.
   - Commit all pending files.
   - Create a local version tag.
   - Push to your remote `origin` on GitHub.
   - Trigger the GitHub Actions workflow to publish the new APK release automatically!

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
- "Select All" toggle quickly selects or deselects all four record types simultaneously.

### 4. 🛡️ Anti-Freeze Concurrency Management
- Limits concurrent downloads to maximum **2 active tasks at a time**.
- Remaining tasks remain in `PENDING` state until an active slot frees up.
- Prevents Android UI thread freezing, memory overflow, and network packet starvation.

### 5. 📁 Master Archive Synthesis & Phone Storage
- Exports **Master JSON**, **Human-Readable TXT Ledger**, and **CSV Table**.
- Directly stored in public device memory: `Download/LandRecords/<District>/<Upazila>/`.
- Automatically scanned via Android `MediaScannerConnection` so files appear instantly in Samsung My Files, Google Files, and desktop MTP connections.
- **Auto-Clears Cache**: Automatically purges temporary download chunks after master file generation to conserve phone storage.
- Storage Access Framework (SAF) fallback for external SD cards.

---

## 🛠️ Tech Stack & Architecture

| Layer | Technology |
| :--- | :--- |
| **Language** | Kotlin 2.0+ |
| **UI Framework** | Jetpack Compose with Material Design 3 (Emerald/Amber GovTech palette) |
| **Architecture** | MVVM with `StateFlow` and unidirectional data flow |
| **Concurrency** | Kotlin Coroutines, Flow, and Mutex Concurrency Limiter |
| **Database** | Android Jetpack Room with Kotlin Symbol Processing (KSP) |
| **File Storage** | Android Scoped Storage (`MediaStore.Downloads`) & POSIX File I/O with MediaScanner |
| **CI / CD** | GitHub Actions Automated APK Builder & Release Pipeline (`.github/workflows/build-apk.yml`) |

---

## 🚀 Building from Source

### Prerequisites
- Android Studio Ladybug (2024.2+) or newer
- JDK 17 (Eclipse Temurin or OpenJDK)
- Android SDK Platform 36

### Build Commands

```bash
# Clone the repository
git clone https://github.com/tapos-cpl/LR-Mass-Downloader.git
cd LR-Mass-Downloader

# Grant execution rights to the Gradle wrapper
chmod +x gradlew

# Build Debug APK
./gradlew assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License & Credits

- Developed for Bangladesh Land Records & Survey (ভূমি রেকর্ড ও জরিপ অধিদপ্তর) automation.
- Licensed under the [MIT License](LICENSE).
