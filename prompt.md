## Context

This is a Kotlin Multiplatform (KMP) mobile project for Fraccio (residential management) and Atriz Admin (tenant/product management) apps. The project structure includes:

- `shared/` - KMP shared module with minimal current dependencies
- `android/fraccio/` - Fraccio Android app using Jetpack Compose
- `android/atriz/` - Atriz Admin Android app using Jetpack Compose
- `ios/` - iOS targets (SwiftUI)
- `docs/spec.md` - Complete API specification with endpoints and data models

Current state:
- Shared module has basic KMP setup with JVM 11 target
- Android apps use JVM 11 and have Compose dependencies
- Shared module `commonMain` dependencies section is empty
- No networking, DI, serialization, or database libraries are currently configured

The spec defines authentication flows, Fraccio features (houses, streets, charges, payments), and Atriz Admin features (tenants, products) with detailed API endpoints and data models.

## Task

1. **Update shared module dependencies** in `/Users/atriz/Atriz/mobile/shared/build.gradle.kts`:
   - Add ktor for HTTP requests
   - Add koin for dependency injection  
   - Add kotlinx.serialization for JSON serialization
   - Add kotlinx.coroutines for async operations
   - Add kotlinx.datetime for date/time operations
   - Add kotlinx.atomicfu for atomic operations
   - Add kotlinx.collections for collections operations
   - Add kotlinx.io for IO operations
   - Add SQLDelight for local database (not Room as mentioned - Room is Android-only)

2. **Update Android configuration** in both `/Users/atriz/Atriz/mobile/android/fraccio/build.gradle.kts` and `/Users/atriz/Atriz/mobile/android/atriz/build.gradle.kts`:
   - Change JVM target from 11 to 21
   - Ensure shared module dependency is properly configured

3. **Update version catalog** in `/Users/atriz/Atriz/mobile/gradle/libs.versions.toml`:
   - Add version entries for all new libraries
   - Add library definitions for proper dependency management

## Design / Pattern to Follow

Follow existing KMP structure:
- Use `commonMain` for shared business logic and data access
- Use `androidMain` and `iosMain` for platform-specific implementations
- Maintain existing namespace patterns (`mx.atriz.shared` for shared module)
- Use version catalog for dependency management as currently established
- Follow the authentication flow and data models defined in `docs/spec.md`

## Constraints

- Do not change the existing project structure or file organization
- Do not modify iOS-specific code - only ensure shared module compiles for iOS targets
- Use SQLDelight instead of Room for cross-platform database compatibility
- Follow the exact data models and API contracts specified in `docs/spec.md`
- Maintain existing Compose dependencies and configuration in Android apps
- Do not add unnecessary dependencies - only those specified in the requirements

## Expected Output

- Shared module builds successfully for all targets (Android, iOS)
- All specified libraries are properly integrated with correct versions
- Android apps compile with JVM 21 target
- Project follows the authentication patterns and data models from `docs/spec.md`
- Dependencies are managed through the version catalog
- No compilation errors across the entire project