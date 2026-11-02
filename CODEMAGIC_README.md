# TaskFlow AI - Codemagic Deployment & Build Guide

This project is fully configured for automated **Android (`.apk` / `.aab`)** and **Native iOS (`.ipa`)** builds using **Codemagic CI/CD**.

---

## 📁 Repository Configuration Structure

The repository includes a ready-to-use `codemagic.yaml` file in the root directory:

1. **`codemagic.yaml`**: Pre-configured build instructions for both independent native Android and native iOS workflows.
2. **`gradle/` & `app/`**: Full Kotlin & Jetpack Compose Android build configuration.
3. **`ios/TaskFlowAI.xcodeproj` & `ios/TaskFlowAI/`**: Full native Swift & SwiftUI iOS application.

---

## 🚀 How to Build Android (.apk / .aab) & iOS (.ipa) on Codemagic

### Step 1: Upload / Connect to GitHub or Codemagic
1. Push this repository to your **GitHub** account (`ashishchavan1221/TaskFlow-AI---Application`).
2. Log into [Codemagic.io](https://codemagic.io).
3. Click **Add Application** and select your GitHub repository.

---

### Step 2: Running the Android Build Workflow (`.apk` / `.aab`)

1. Select the **`android-workflow`** in Codemagic.
2. Click **Start new build**.
3. Codemagic will run Gradle commands (`assembleDebug`, `assembleRelease`, `bundleRelease`) and produce:
   - **`app-debug.apk`** (Direct sideload testing on Android phones)
   - **`app-release.apk`** (Signed production release APK)
   - **`app-release.aab`** (Google Play Store App Bundle)

---

### Step 3: Running the Native iOS Build Workflow (`.ipa`)

1. Select the **`ios-workflow`** in Codemagic.
2. Under **App signing**:
   - Upload your **Apple Developer Certificate (`.p12`)** and **Provisioning Profile (`.mobileprovision`)**.
   - Bundle Identifier: `com.aistudio.taskflow`.
3. Click **Start new build**.
4. Codemagic will spin up a macOS M1 runner, resolve Swift Package dependencies (`firebase-ios-sdk`), archive `ios/TaskFlowAI.xcodeproj`, and export the signed **`TaskFlowAI.ipa`**.

---

## 📱 How Users Download the Built Apps

Once Codemagic completes the build:
1. Go to the **Builds** section in Codemagic.
2. Under **Artifacts**:
   - Download the **Android `.apk` / `.aab`** under `app/build/outputs/`.
   - Download the **iOS `.ipa`** under `build/ios/ipa/` (installable via TestFlight, Diawi, or Ad-Hoc install).
