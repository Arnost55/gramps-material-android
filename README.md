# Gramps Material

An **unofficial, native Android client for Gramps Web**, built with Kotlin, Jetpack Compose and Material 3.

> Early scaffold — not ready for real family-tree data yet.

## Why

Gramps Web is a capable genealogy backend and web app. This project explores a phone-first Android experience with native navigation, Material You, a touch-friendly family tree, and eventually offline caching.

## MVP

- [x] Material 3 / Material You shell
- [x] Server URL setup screen
- [x] Home / Search / Tree navigation
- [x] API boundary separated from UI
- [ ] Real Gramps Web authentication
- [ ] Server capability/version check
- [ ] Person search
- [ ] Person profile
- [ ] Family relationships
- [ ] Interactive ancestor/descendant tree
- [ ] Media, places, sources and citations
- [ ] Add/edit people
- [ ] Secure credential storage
- [ ] Room offline cache
- [ ] Multiple Gramps Web servers / trees

## Architecture

```text
Compose UI
   ↓
ViewModels
   ↓
Repositories
   ├── Gramps Web REST API
   └── Room cache (planned)
```

The Gramps Web server remains the source of truth. The Android app should not invent a second genealogy data model or rely on GEDCOM syncing for normal use.

## Gramps Web API

Gramps Web exposes a REST API that can query and modify a Gramps family tree. An authenticated Gramps Web instance also exposes interactive API documentation at `/api/swagger-ui`.

Authentication is deliberately not implemented in this first scaffold. Session/API tokens are short-lived, so the production client should implement a proper login flow and secure token handling instead of storing a pasted token.

## Building

Requirements:

- Android Studio compatible with AGP 9.3
- JDK 17+
- Android SDK 37

Open the repository in Android Studio and run the `app` configuration.

## Tech

- Kotlin
- Jetpack Compose
- Material 3 / Material You
- Compile/target SDK 37
- Min SDK 26

## Project status

This is an experimental side project and **not an official Gramps project**. "Gramps" and related project names belong to their respective owners.

## License

Apache License 2.0. See [LICENSE](LICENSE).
