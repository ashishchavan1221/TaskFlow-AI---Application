# TaskFlow AI — Enterprise Task & Team Management Platform

<div align="center">

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase%20Suite-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com/)
[![Gemini AI](https://img.shields.io/badge/AI-Google%20Gemini-8E75C2?style=for-the-badge&logo=google&logoColor=white)](https://ai.google.dev/)
[![Codemagic CI/CD](https://img.shields.io/badge/CI%2FCD-Codemagic-F65314?style=for-the-badge&logo=codemagic&logoColor=white)](https://codemagic.io/)

<p align="center">
  <b>A state-of-the-art enterprise-grade mobile solution combining real-time multi-device cloud synchronization, strict multi-tier role isolation, AI-powered workflow automation, dynamic time tracking, and automated CI/CD builds.</b>
</p>

</div>

---

## 📖 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Architecture & Tech Stack](#-architecture--tech-stack)
- [Repository Structure](#-repository-structure)
- [Prerequisites](#-prerequisites)
- [Getting Started & Local Setup](#-getting-started--local-setup)
- [Firebase Backend Configuration](#-firebase-backend-configuration)
- [Codemagic CI/CD Pipelines (Android & iOS)](#-codemagic-cicd-pipelines-android--ios)
- [Security & Compliance](#-security--compliance)
- [Testing & Quality Assurance](#-testing--quality-assurance)
- [Author & Maintenance](#-author--maintenance)

---

## 🌟 Overview

**TaskFlow AI** is built for modern distributed teams and enterprise operations. It eliminates communication silos and administrative overhead by unifying project tracking, task execution, role-based governance, and real-time team communication into a single, cohesive experience.

Designed natively for Android using modern **Jetpack Compose (Material 3)** and powered by **Firebase** cloud architecture with **Google Gemini AI**, TaskFlow delivers immediate real-time sync across devices with zero lag or duplicate entries.

---

## ⚡ Key Features

### 1. 🛡️ Role-Isolated Multi-Tier Workspaces
- **Organization Owner / Admin Dashboard:** Complete bird's-eye view of organizational pipeline, company-wide deliverable statuses, member rosters, manager approvals, and immutable audit logs.
- **Department Manager Dashboard:** Department-scoped deliverables, approval workflows, workload metrics, and interactive priority bottleneck charts.
- **Staff Associate Dashboard:** Focused, distraction-free view showing only tasks assigned to the current member.

### 2. 🤖 Gemini AI Workflow Assistant
- Direct bottom-navigation AI assistant powered by Google Gemini.
- Automatically breaks complex deliverables into actionable subtasks, drafts operational summaries, and suggests priority tags.

### 3. ⏱️ Dynamic Time Log Tracker & Stopwatch
- Built directly into task details with live `Start Timer` and `Stop & Log` controls (`00:00:00`).
- Seamlessly converts elapsed sessions into fractional billable hours.
- Quick-log incremental presets (`+15m`, `+30m`, `+1h`, `+2h`).

### 4. 👥 Team Directory, Workgroups & Group Chat
- Organization admins can configure cross-functional workgroups with up to two lead managers and staff associates.
- Dedicated real-time group chat modal with role badges, sender avatars, audible notifications, and immediate sync.

### 5. 📊 Interactive Visual Analytics
- Integrated D3.js Priority Donut Chart in the Manager view displaying real-time priority distribution (Urgent, High, Medium, Low) and bottleneck risk assessments.

### 6. 📝 Completion Notes & Verification
- Mandatory completion modal requiring execution notes (e.g., invoice numbers, filing IDs) and completion timestamps before marking tasks complete.
- Visual completion badges and audit timestamps visible to managers and admins.

### 7. 🔔 Audible Reminders & Alerts
- In-app notification banners combined with native device chime (`AudioManager.STREAM_NOTIFICATION`) for high-priority task alerts.

### 8. 📜 Statutory Compliance & GSTIN Verification
- Immutable administrative compliance ledger verifying mandatory business details (GSTIN verification and checksum checks).

---

## 🏗️ Architecture & Tech Stack

```mermaid
graph TD
    subgraph Client ["Android Client (Jetpack Compose)"]
        UI[Material 3 UI Components]
        VM[AAC ViewModels & StateFlow]
        REPO[Repository Layer]
        UI --> VM --> REPO
    end

    subgraph Cloud ["Firebase Backend & Services"]
        AUTH[Firebase Auth]
        FS[(Cloud Firestore Real-Time DB)]
        CF[Cloud Functions (Node.js 20)]
        ST[Cloud Storage]
        REPO --> AUTH
        REPO --> FS
        REPO --> CF
        REPO --> ST
    end

    subgraph AI ["AI Engine"]
        GEMINI[Google Gemini API]
        VM --> GEMINI
        CF --> GEMINI
    end

    subgraph CI ["Automated DevOps"]
        CM[Codemagic CI/CD]
        CM --> APK[Android .apk / .aab]
        CM --> IPA[iOS .ipa]
    end
```

| Layer | Technologies |
|---|---|
| **Language** | Kotlin 2.0.21, Java 17 |
| **Android Architecture** | Jetpack Compose, Material 3, Navigation Compose, Kotlin Coroutines, StateFlow, ViewModel |
| **Backend** | Firebase Cloud Functions (Node.js 20, v2 triggers), Firebase Admin SDK |
| **Database & Storage** | Cloud Firestore (with fine-grained security rules), Firebase Storage |
| **Artificial Intelligence** | Google Gemini API (Generative AI SDK) |
| **Build System** | Gradle 8.9+ (Kotlin DSL), KSP, Android Gradle Plugin 8.7+ |
| **CI/CD** | Codemagic CI/CD (`codemagic.yaml`) |

---

## 📁 Repository Structure

```
taskflow-ai/
├── app/                               # Native Android application module
│   ├── src/main/java/                 # Kotlin source code
│   │   └── com/example/
│   │       ├── data/                  # Firebase data repositories & models
│   │       ├── ui/                    # Jetpack Compose UI screens & components
│   │       │   ├── auth/              # Login, registration, role selection
│   │       │   ├── dashboard/         # Owner, Manager, and Staff dashboards
│   │       │   ├── tasks/             # Task pipelines, modals, timers, filters
│   │       │   ├── team/              # Workgroups, team directory, group chat
│   │       │   ├── settings/          # Security, MPIN, GSTIN verification
│   │       │   └── theme/             # Material 3 colors, typography, theme
│   │       └── viewmodel/             # StateFlow ViewModels
│   ├── src/main/res/                  # Android resources, drawables, launcher icons
│   ├── google-services.json           # Firebase client configuration
│   └── build.gradle.kts               # App-level dependencies & plugins
├── codemagic.yaml                     # Codemagic CI/CD build configuration
├── CODEMAGIC_README.md                # Dedicated CI/CD build & release guide
├── docs/                              # Comprehensive architectural specifications
│   ├── ARCHITECTURE.md                # Data models, security boundaries, entity flow
│   ├── MASTER_PROMPT.md               # Master technical specification & guidelines
│   ├── SETUP_FIREBASE.md              # Firebase project deployment manual
│   ├── STATUS.md                      # Feature status & traceability ledger
│   ├── TASKS.md                       # Sprint work packages (WP0–WP16)
│   └── TEST_PLAN.md                   # Automated testing strategies
├── firebase.json                      # Firebase project configuration
├── firestore.indexes.json             # Compound indexes for high-speed queries
├── firestore.rules                    # Granular Firestore security rules
├── storage.rules                      # Cloud Storage access rules
├── functions/                         # Firebase Cloud Functions (Node.js)
│   ├── index.js                       # Cloud endpoints (atomic registration, AI assist)
│   ├── package.json                   # Functions dependencies
│   └── test/                          # Functions unit test suite
├── gradle/                            # Gradle wrapper & version catalogs
│   ├── libs.versions.toml             # Centralized dependencies
│   └── wrapper/                       # Gradle wrapper binaries
├── rules-tests/                       # Automated security rules test suite
├── scripts/                           # Utility scripts (provision-demo, ADB install)
└── build.gradle.kts                   # Root build configuration
```

---

## 💻 Prerequisites

Ensure you have the following installed locally:

- **[Android Studio](https://developer.android.com/studio)** (Iguana / Jellyfish / Koala or later)
- **JDK 17** or **JDK 21**
- **[Node.js](https://nodejs.org/)** (v20 LTS recommended for Firebase Functions)
- **[Firebase CLI](https://firebase.google.com/docs/cli)** (`npm install -g firebase-tools`)
- **Android Device or Emulator** running Android 9.0+ (API level 28+)

---

## 🚀 Getting Started & Local Setup

### 1. Clone the Repository
```bash
git clone https://github.com/ashishchavan1221/TaskFlow-AI---Application.git
cd TaskFlow-AI---Application
```

### 2. Environment Variables Configuration
Copy the example environment file and configure your API keys:
```bash
cp .env.example .env
```
Open `.env` and provide your Gemini API key:
```ini
GEMINI_API_KEY=your_gemini_api_key_here
```

### 3. Open in Android Studio
1. Launch Android Studio.
2. Select **Open** and choose the cloned repository folder.
3. Allow Gradle to download dependencies and sync the project catalog (`gradle/libs.versions.toml`).
4. Select your target Android device or emulator and press **Run (`Shift + F10`)**.

---

## ☁️ Firebase Backend Configuration

### 1. Login & Connect to Firebase
```bash
firebase login
firebase use --add <your-firebase-project-id>
```

### 2. Deploy Security Rules & Firestore Indexes
```bash
firebase deploy --only firestore:rules,storage,firestore:indexes
```

### 3. Deploy Cloud Functions
```bash
cd functions
npm install
cd ..
firebase deploy --only functions
```

---

## 📦 Codemagic CI/CD Pipelines (Android & iOS)

This repository includes a production-ready `codemagic.yaml` for zero-configuration builds.

### Android Workflow (`android-workflow`)
Automatically builds installable APKs and Play Store App Bundles:
- **`app-debug.apk`** (direct sideload testing)
- **`app-release.aab`** (Google Play Store release)

### iOS Workflow (`ios-kmp-workflow`)
Automatically spins up a macOS M1 runner to compile and package:
- **`app.ipa`** (TestFlight / Ad-Hoc distribution)

Refer to [CODEMAGIC_README.md](file:///c:/Users/asus/Downloads/taskflow-ai/CODEMAGIC_README.md) for full signing setup and artifact download steps.

---

## 🔒 Security & Compliance

- **Zero Client-Side Privilege Escalation:** Critical actions (such as company registration and member role assignment) run through transactional Firebase Cloud Functions.
- **Granular Firestore Rules:** Subcollections (`organizations/{orgId}/tasks`, `members`) are strictly locked so members only read authorized organizational resources.
- **Keystore & Secret Protection:** `.env` and private keys are ignored by `.gitignore` to prevent sensitive leakage.

---

## 🧪 Testing & Quality Assurance

- **Cloud Functions Unit Tests:**
  ```bash
  cd functions
  npm test
  ```
- **Android JVM & Robolectric Tests:**
  ```bash
  ./gradlew testDebugUnitTest
  ```
- **Security Rules Emulation Tests:**
  ```bash
  cd rules-tests
  npm test
  ```

---

## 👤 Author & Maintenance

**TaskFlow AI**  
Developed and maintained by **[Ashish Chavan](https://github.com/ashishchavan1221)**  
GitHub: [@ashishchavan1221](https://github.com/ashishchavan1221)
