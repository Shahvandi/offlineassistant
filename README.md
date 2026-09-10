# Offline Voice Assistant

Android project for GitHub Actions.

## Build on GitHub
1. Upload all files to a GitHub repository.
2. Open **Actions**.
3. Run **Build APK** (or push to `main`).
4. When the workflow succeeds, open the run and download the artifact **OfflineVoiceAssistant-debug**.

The workflow pins Gradle to 8.10.2 instead of relying on whatever Gradle version happens to be installed on the runner.
