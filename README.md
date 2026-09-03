# Gramps Material

Gramps Material is an unofficial native Android client for [Gramps Web](https://www.grampsweb.org/). It is built with Kotlin, Jetpack Compose, and Material 3, and is not affiliated with the Gramps project.

> **Alpha 1.0.0-alpha01** — This is an early testing release. Bugs and incomplete server compatibility are expected. Back up important Gramps data before using editing or synchronization features.

## Features

### Connect and manage a tree

- Connect to a self-hosted Gramps Web server.
- Test server readiness before signing in.
- Sign in with a server URL, username, and password.
- Select and switch between available family trees.
- Persist the selected tree and app settings locally.
- See network, server, session, and current-tree status in Settings.

### Browse family data

- Search people, places, and events in the selected tree.
- Continue browsing cached people while a network refresh is in progress or unavailable.
- View recently viewed people, with up to 20 recent entries retained.
- View person profiles with names, life years, relationship-to-home status, and linked family members.
- Browse parents, partners, children, events, timelines, DNA matches, media, sources, citations, and notes.
- Set a home person and bookmark people.
- Browse places and open coordinate-bearing places in a map application.
- Browse reports published by the server.

### Explore relationships

- View ancestor and relationship trees.
- Choose the tree mode and display between 2 and 6 generations.
- Pan, pinch-zoom, fit, refresh, and tap people in the interactive tree.
- Navigate from tree nodes directly to person profiles.

### Dashboard and appearance

- Customize the Home dashboard with Tree, Home person, Search, Stats, Birthdays, and Recent widgets.
- Use compact dashboard cards.
- Choose System, Light, or Dark theme.
- Enable Dynamic Colors on supported Android versions.
- Enable AMOLED Optimization.
- Clear cached people and trees from Settings.

### Editing and synchronization

- Edit a person's first name and surname.
- Queue name changes when needed and synchronize pending changes before the next people/family refresh.
- Refresh people and family data in the background using WorkManager.
- Keep application data in the app-private local Room database.

## Current limitations

- A compatible, authenticated Gramps Web server is required for the full experience.
- This alpha focuses on browsing. Person-name editing is the only editing workflow currently exposed; adding or editing families, events, media, sources, citations, places, notes, and other records is not implemented.
- Cached/offline browsing is read-only except for name changes queued for synchronization. Data that has not already been cached is unavailable offline.
- Places without coordinates cannot open a map. Reports are currently view-only and have no report execution or download action.
- Server API behavior and available data depend on the Gramps Web version and permissions of the signed-in account.
- The app has been tested through automated unit tests; device and server compatibility may vary.

## Requirements

- Android 8.0 (API 26) or newer
- JDK 17
- Android SDK 37
- Android Studio compatible with the checked-in Android Gradle Plugin
- A reachable Gramps Web server for sign-in and live data

## Build and test

From the project root:

```bash
./gradlew :app:assembleDebug
./gradlew test
./gradlew :app:lintDebug
```

On Windows, use `gradlew.bat` instead. Set `JAVA_HOME` to JDK 17 if Gradle cannot find Java.

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Getting started

1. Install and launch the app.
2. Choose **Connect to Gramps Web**.
3. Enter the server URL, username, and password.
4. Select **Test connection** to check the server readiness endpoint.
5. Sign in and select a family tree.
6. Use Home, Search, Places, Reports, and the tree viewer to explore your data.

HTTPS is required by default. Enable **Allow insecure local server** only when connecting to a trusted local HTTP server. Passwords are not persisted. Access and refresh tokens are stored using AndroidX `EncryptedSharedPreferences` backed by Android Keystore.

## Architecture

```text
Jetpack Compose UI → ViewModels → Repositories → Gramps Web API
                                      ↘ Room / DataStore cache
```

- `core_network` — Retrofit/OkHttp API client, authentication, repositories, and server connectivity.
- `core_database` — Room entities and DAOs for people, families, trees, recents, and pending mutations; DataStore-backed session and settings.
- `core_sync` — background people/family cache refresh and pending mutation synchronization.
- `core_ui` — navigation, session state, and Material 3 theme.
- `feature_auth` — welcome, connection, and sign-in flows.
- `feature_home` — dashboard, widgets, statistics, birthdays, and recent people.
- `feature_search` — family-tree search.
- `feature_person` — profiles, relationships, linked records, bookmarks, and name editing.
- `feature_places` / `feature_reports` — place and server-report browsing.
- `feature_tree` — ancestor and relationship graph construction, layout, and viewer.
- `feature_settings` — connection, cache, session, and appearance settings.

## Privacy and security

The app communicates directly with only the Gramps Web server you configure and includes no analytics service. Family data and caches remain app-private. The app does not store passwords, does not disable TLS certificate validation, and does not log access tokens or authorization headers in release builds. Logging out removes the stored session credentials.

Do not use an untrusted HTTP server or share exported family data through unsecured channels.

## License

Apache License 2.0. See [LICENSE](LICENSE).
