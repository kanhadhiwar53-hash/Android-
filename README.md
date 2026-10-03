# NOVA IDE V0.1

Kotlin + XML Android IDE starter project.

## Requirements
- JDK 17
- Android SDK Platform 35
- Android Gradle Plugin 8.7.3 / Gradle 8.9

## Features in V0.1
- Custom dark IDE dashboard
- Create and open starter Android project workspaces
- Basic file explorer
- Text editing and saving
- Project structure inspection console

## GitHub Actions APK build
A GitHub Actions workflow is available at `.github/workflows/android.yml`.

- Runs automatically on pushes and pull requests targeting `main`.
- Can also be started manually from the repository's **Actions** tab using **Android APK Build → Run workflow**.
- Builds the `debug` variant with JDK 17, Gradle 8.9 and Android SDK 35.
- Uploads `app/build/outputs/apk/debug/*.apk` as the `nova-ide-debug-apk` artifact for 14 days.

To download the APK, open the completed workflow run in GitHub Actions and download the artifact from its **Artifacts** section. This produces a debug APK, not a Play Store release APK.

## Build engine status
The in-app BUILD action currently inspects the project structure only. It does not compile APKs from projects created inside NOVA IDE yet.
The planned in-app build pipeline requires Android SDK build-tools (AAPT2, D8, zipalign),
Kotlin/Java compiler integration, resource processing, packaging and APK signing.

Open this repository in Android Studio or AndroidIDE as a Gradle project.
The Gradle wrapper binaries are not included in this starter commit; the GitHub Actions workflow installs Gradle 8.9 on its runner.
