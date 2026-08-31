# Gramps Material

**Gramps Material** is an unofficial, native Android client for Gramps Web, built with Kotlin, Jetpack Compose, and Material 3. It is not affiliated with the Gramps project.

## Current features

- Connect to a Gramps Web server and verify `GET /ready`
- Token-based sign-in with encrypted token storage
- Tree selection persisted in DataStore
- Real people search, person profiles, and relationship navigation
- Recently viewed people and cached people available offline
- Interactive, cycle-safe ancestor tree with pan, pinch zoom, tap-to-open, and 2–6 generations
- Light, dark, dynamic-color, and AMOLED appearance options
- Local cache clearing and logout

## Current limitations

- Requires a compatible, authenticated Gramps Web server.
- Media, sources, citations, places, events beyond the profile response, favorites, and editing are not complete.
- Offline mode is read-only and only supports already-cached people/search results.
- No device validation is claimed unless recorded separately for a connected device.

## Build

Requirements:

- JDK 17
- Android SDK 37
- Android Studio compatible with the checked-in Android Gradle Plugin

```bash
./gradlew :app:assembleDebug
./gradlew test
./gradlew :app:lintDebug
```

On Windows, use `gradlew.bat` instead. Set `JAVA_HOME` to JDK 17 if Gradle cannot find Java.

## Connection

1. Launch the app and choose **Connect to Gramps Web**.
2. Enter the server URL, username, and password.
3. Use **Test connection** to validate the server readiness endpoint.
4. HTTPS is required by default. Enable **Allow insecure local server** only for a trusted local HTTP instance.
5. Sign in and select a tree.

Passwords are not persisted. Access and refresh tokens are stored using AndroidX `EncryptedSharedPreferences` backed by Android Keystore.

## Architecture

```text
Compose UI → ViewModels → Repositories → Gramps Web / Room
```

- `core_network`: Retrofit/OkHttp Gramps Web client and repositories
- `core_database`: Room cache, recent people, and DataStore/session state
- `core_ui`: theme and Navigation Compose
- `feature_*`: authentication, home, search, person, tree, and settings

## Privacy and security

This client communicates directly with the Gramps Web server you configure. Do not use an untrusted HTTP server. The app does not store passwords, does not disable TLS validation, and should not log tokens or authorization headers.

## License

Apache License 2.0. See [LICENSE](LICENSE).
