#!/usr/bin/env bash
set -e
APK="${1:-$(dirname "$0")/../project/app/build/outputs/apk/debug/app-debug.apk}"
[ -f "$APK" ] || { echo "APK not found: $APK  (build first: cd project && ./gradlew assembleDebug)"; exit 1; }
adb devices
adb get-state >/dev/null 2>&1 || { echo "No authorized device (USB debugging + data cable + Allow prompt)."; exit 1; }
adb install -r "$APK"
adb shell monkey -p com.aistudio.taskflow.qrvkty -c android.intent.category.LAUNCHER 1
