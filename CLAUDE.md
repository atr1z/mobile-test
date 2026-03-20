# CLAUDE.md — Project Conventions

## Project Overview

Kotlin Multiplatform (KMP) mobile project consolidating two products:
- **Fraccio** — Residential property management
- **Atriz Admin** — Tenant/product management

## Module Structure

| Module | Purpose |
|--------|---------|
| `shared/` | KMP shared module — business logic, networking, data layer |
| `android/atriz/` | Atriz Admin Android app |
| `android/fraccio/` | Fraccio Android app |
| `android/calculator/` | Sample/utility Android app |
| `ios/atriz/` | Atriz Admin iOS app (SwiftUI) |
| `ios/fraccio/` | Fraccio iOS app (SwiftUI) |

## Source Set Rules

- **Shared logic** goes in `shared/src/commonMain/kotlin/mx/atriz/`
- **Android-specific** implementations go in `shared/src/androidMain/kotlin/mx/atriz/`
- **iOS-specific** implementations go in `shared/src/iosMain/kotlin/mx/atriz/`
- Use `expect`/`actual` pattern for platform-specific APIs

## Package Naming

- `mx.atriz.shared.*` — Shared module code
- `mx.atriz.admin.*` — Atriz Admin Android app
- `mx.atriz.fraccio.*` — Fraccio Android app

## Build Variants

| Variant | Android | iOS | API | App ID Suffix |
|---------|---------|-----|-----|---------------|
| Debug | `debug` | `Debug` | localhost | `.debug` |
| Dev | `dev` | `Dev` | dev API | `.dev` |
| Release | `release` | `Release` | production | (none) |

## Dependency Management

1. Add version to `gradle/libs.versions.toml` under `[versions]`
2. Add library entry under `[libraries]` using `version.ref`
3. Reference in `build.gradle.kts` via `libs.<name>` accessor
4. **Never** use hardcoded version strings in build files

## Testing

- Shared tests: `shared/src/commonTest/kotlin/mx/atriz/` — naming: `*Test.kt`
- Android unit tests: `android/<app>/src/androidUnitTest/` — naming: `*Test.kt`
- iOS tests: `ios/<app>Tests/` (Xcode test targets)
- Run shared tests: `./gradlew :shared:allTests`
- Run Android tests: `./gradlew :android:atriz:testDebugUnitTest`
- Run lint check: `./gradlew ktlintCheck`
- Auto-format: `./gradlew ktlintFormat`

## Key Libraries

- **Ktor** — HTTP client (multiplatform)
- **Koin** — Dependency injection
- **SQLDelight** — Local database (multiplatform)
- **Kermit** — Logging (multiplatform)
- **Sentry** — Error tracking (multiplatform)
- **Compose Multiplatform** — Android UI
- **SwiftUI** — iOS UI

## Build Commands

```bash
# Full Android debug build
./gradlew :android:atriz:assembleDebug :android:fraccio:assembleDebug

# Shared framework for iOS simulator
./gradlew :shared:linkReleaseFrameworkIosSimulatorArm64

# Check for outdated dependencies
./gradlew dependencyUpdates
```
