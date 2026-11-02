# TaskFlow AI - Codemagic Deployment & Build Guide

This project is fully configured for automated **Android (`.apk` / `.aab`)** and **iOS (`.ipa`)** builds using **Codemagic CI/CD**.

---

## 📁 Repository Configuration Structure

The repository includes a ready-to-use `codemagic.yaml` file in the root directory:

1. **`codemagic.yaml`**: Pre-configured build instructions for both Android and iOS workflows.
2. **`gradle/` & `build.gradle.kts`**: Full Gradle configuration for Android compilation.
3. **`app/`**: Source code containing Kotlin & Jetpack Compose UI, ViewModels, and Firebase data layer.

---

## 🚀 How to Build Android (.apk) & iOS (.ipa) on Codemagic

### Step 1: Upload / Connect to GitHub or Codemagic
1. Export the project ZIP from AI Studio or push this workspace to your **GitHub** account.
2. Log into [Codemagic.io](https://codemagic.io).
3. Click **Add Application** and connect your GitHub repository (or upload the project ZIP).

---

### Step 2: Running the Android Build Workflow (`.apk` / `.aab`)

1. Select the **`android-workflow`** in Codemagic.
2. Click **Start new build**.
3. Codemagic will run the Gradle build step and produce:
   - **`app-debug.apk`** (Installable directly on Android phones)
   - **`app-release.aab`** (Google Play Store Bundle)

---

### Step 3: Running the iOS Build Workflow (`.ipa`)

1. Select the **`ios-kmp-workflow`** in Codemagic.
2. Under **App signing**:
   - Upload your **Apple Developer Certificate (`.p12`)** and **Provisioning Profile (`.mobileprovision`)**.
   - Set the Bundle Identifier to `com.aistudio.taskflow`.
3. Click **Start new build**.
4. Codemagic will spin up a macOS M1 runner, build the Apple Framework, and generate the signed **`.ipa` file**.

---

## 📱 How Users Download the Built Apps

Once Codemagic completes the build:
1. Go to the **Builds** section in Codemagic.
2. Under **Artifacts**:
   - Download the **Android `.apk`** for Android devices.
   - Download the **iOS `.ipa`** for iOS devices (via TestFlight, Diawi, or Ad-Hoc install).
