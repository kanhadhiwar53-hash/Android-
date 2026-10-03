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

## Build engine status
The BUILD action currently inspects the project structure only. It does not compile an APK yet.
The planned build pipeline requires Android SDK build-tools (AAPT2, D8, zipalign),
Kotlin/Java compiler integration, resource processing, packaging and APK signing.

Open this repository in Android Studio or AndroidIDE as a Gradle project.
The Gradle wrapper binaries are not included in this starter commit.
