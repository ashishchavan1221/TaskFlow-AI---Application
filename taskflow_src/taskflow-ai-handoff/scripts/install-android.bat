@echo off
REM Usage: scripts\install-android.bat [path-to-apk]
REM Default APK = project\app\build\outputs\apk\debug\app-debug.apk (exists only AFTER you build it)
set ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe
if not exist "%ADB%" set ADB=adb
set APK=%~1
if "%APK%"=="" set APK=%~dp0..\project\app\build\outputs\apk\debug\app-debug.apk
if not exist "%APK%" (echo APK not found: %APK% & echo Build first: cd project ^& gradlew.bat assembleDebug & exit /b 1)
"%ADB%" devices
"%ADB%" get-state >nul 2>&1 || (echo No authorized device. Enable USB debugging, use a data cable, tap Allow on the phone. & exit /b 1)
"%ADB%" install -r "%APK%" && "%ADB%" shell monkey -p com.aistudio.taskflow.qrvkty -c android.intent.category.LAUNCHER 1
