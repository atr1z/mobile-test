<context>
## Context

This is a **Kotlin Multiplatform (KMP) mobile project** that consolidates two products — **Fraccio** (residential property management) and **Atriz Admin** (tenant/product management) — into a single shared codebase for Android and iOS. The project has just been scaffolded and is about to enter active development. Before building features, the foundation needs to be hardened with CI/CD, testing, code quality, error tracking, and logging infrastructure.

**Current state:** The project has a working KMP structure with three Android modules (`atriz`, `fraccio`, `calculator`), two iOS targets (`atriz`, `fraccio`), and a `shared` Kotlin module. Dependencies are declared in `gradle/libs.versions.toml` (Ktor 3.1.1, Koin 4.0.4, SQLDelight 2.0.2, Kotlin 2.3.0, Compose Multiplatform 1.10.0). Build configuration is complete. All app modules have basic scaffold UI (Compose for Android, SwiftUI for iOS) but no real features, tests, CI, linting, or error tracking.

**What already exists:**
- Gradle build system with version catalog (`libs.versions.toml`)
- `shared` module with `commonMain`, `androidMain`, `iosMain` source sets
- SQLDelight configured (database `AppDatabase`, package `mx.atriz.shared.db`) but no `.sq` schema files yet
- iOS Xcode project with `Debug.xcconfig`, `Dev.xcconfig`, `Release.xcconfig` build configurations
- `kotlin-test` declared in `commonTest.dependencies` but no test files written
- iOS test targets scaffolded (`atrizTests`, `fraccioTests`, `atrizUITests`, `fraccioUITests`) with empty test methods
- No `.github/` directory — no CI/CD workflows exist
- No linting or formatting tools configured

<documents>
<document index="1">
<source>gradle/libs.versions.toml</source>
<document_content>
[versions]
agp = "8.11.2"
android-compileSdk = "36"
android-minSdk = "29"
android-targetSdk = "36"
androidx-activity = "1.12.2"
androidx-appcompat = "1.7.1"
androidx-core = "1.17.0"
androidx-espresso = "3.7.0"
androidx-lifecycle = "2.9.6"
androidx-testExt = "1.3.0"
composeMultiplatform = "1.10.0"
junit = "4.13.2"
kotlin = "2.3.0"
material3 = "1.10.0-alpha05"
ktor = "3.1.1"
koin = "4.0.4"
kotlinx-serialization = "1.8.1"
kotlinx-coroutines = "1.10.2"
kotlinx-datetime = "0.6.2"
kotlinx-atomicfu = "0.27.0"
kotlinx-collections = "0.3.8"
kotlinx-io = "0.7.0"
sqldelight = "2.0.2"

[libraries]
kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }
kotlin-testJunit = { module = "org.jetbrains.kotlin:kotlin-test-junit", version.ref = "kotlin" }
junit = { module = "junit:junit", version.ref = "junit" }
androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "androidx-core" }
androidx-testExt-junit = { module = "androidx.test.ext:junit", version.ref = "androidx-testExt" }
androidx-espresso-core = { module = "androidx.test.espresso:espresso-core", version.ref = "androidx-espresso" }
# ... (Compose, Ktor, Koin, Kotlinx, SQLDelight libraries defined)

[plugins]
androidApplication = { id = "com.android.application", version.ref = "agp" }
androidLibrary = { id = "com.android.library", version.ref = "agp" }
composeMultiplatform = { id = "org.jetbrains.compose", version.ref = "composeMultiplatform" }
composeCompiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }
</document_content>
</document>
<document index="2">
<source>settings.gradle.kts</source>
<document_content>
rootProject.name = "Apps"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google { mavenContent { includeGroupAndSubgroups("androidx"); includeGroupAndSubgroups("com.android"); includeGroupAndSubgroups("com.google") } }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}
dependencyResolutionManagement {
    repositories {
        google { mavenContent { includeGroupAndSubgroups("androidx"); includeGroupAndSubgroups("com.android"); includeGroupAndSubgroups("com.google") } }
        mavenCentral()
    }
}

include(":android:atriz")
include(":android:calculator")
include(":android:fraccio")
include(":shared")
</document_content>
</document>
<document index="3">
<source>shared/build.gradle.kts</source>
<document_content>
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_21) } }
    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework { baseName = "Shared"; isStatic = true }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.atomicfu)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.kotlinx.io.core)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.kotlinx.coroutines.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "mx.atriz.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions { sourceCompatibility = JavaVersion.VERSION_21; targetCompatibility = JavaVersion.VERSION_21 }
    defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }
}

sqldelight {
    databases { create("AppDatabase") { packageName.set("mx.atriz.shared.db") } }
}
</document_content>
</document>
<document index="4">
<source>Project module structure</source>
<document_content>
mobile/
├── shared/                          # KMP shared module
│   └── src/
│       ├── commonMain/kotlin/mx/atriz/          # Shared logic (Greeting.kt, Platform.kt, shared/config/)
│       ├── androidMain/kotlin/mx/atriz/         # Android actuals (Platform.android.kt)
│       ├── iosMain/kotlin/mx/atriz/             # iOS actuals (Platform.ios.kt)
│       └── commonTest/                          # Empty — no tests yet
├── android/
│   ├── atriz/       (namespace: mx.atriz, app ID: mx.atriz, launcher: mx.atriz.admin.MainActivity)
│   ├── fraccio/     (namespace: mx.atriz, app ID: mx.atriz, launcher: mx.atriz.fraccio.MainActivity)
│   └── calculator/  (namespace: mx.atriz, sample app)
├── ios/
│   ├── atriz/       (SwiftUI app — atrizApp.swift, ContentView.swift, AppConfig.swift)
│   ├── fraccio/     (SwiftUI app — fraccioApp.swift, ContentView.swift, AppConfig.swift)
│   ├── atrizTests/ & atrizUITests/       # Scaffolded, empty
│   ├── fraccioTests/ & fraccioUITests/   # Scaffolded, empty
│   ├── Configuration/                    # xcconfig files (Debug, Dev, Release)
│   └── ios.xcodeproj/
├── gradle/libs.versions.toml
├── build.gradle.kts                     # Root — declares plugins with apply false
├── settings.gradle.kts                  # Includes all modules
└── gradle.properties                    # JVM args, Android settings
</document_content>
</document>
<document index="5">
<source>Build variants</source>
<document_content>
Android build types: debug, dev, release
- debug: API_BASE_URL from env or localhost, ALLOW_ENV_OVERRIDE=true, app ID suffix .debug
- dev: API_BASE_URL from env or dev API, ALLOW_ENV_OVERRIDE=false, app ID suffix .dev
- release: API_BASE_URL from env or production API, ALLOW_ENV_OVERRIDE=false

iOS configurations: Debug, Dev, Release (via xcconfig files)
- Debug: localhost, ALLOW_ENV_OVERRIDE=YES, BUNDLE_ID_SUFFIX=.debug
- Dev: dev API, ALLOW_ENV_OVERRIDE=NO, BUNDLE_ID_SUFFIX=.dev
- Release: production API, ALLOW_ENV_OVERRIDE=NO, no suffix
</document_content>
</document>
</documents>
</context>

<instructions>
## Task

Set up the development infrastructure for this KMP project. This covers seven areas: unit testing, CI/CD, code quality, dependency management improvements, project conventions, Sentry error tracking, and logging. Implement each area as described below.

### 1. Unit Test Frameworks

**Shared module (`commonTest`):**
1. Add `kotlinx-coroutines-test` to `gradle/libs.versions.toml` and to `commonTest.dependencies` in `shared/build.gradle.kts`
2. Add `koin-test` to `gradle/libs.versions.toml` and to `commonTest.dependencies`
3. Add `ktor-client-mock` to `gradle/libs.versions.toml` and to `commonTest.dependencies` (for testing API clients later)
4. Create a sample test at `shared/src/commonTest/kotlin/mx/atriz/GreetingTest.kt` that tests `Greeting().greet()` returns a non-empty string containing "Hello"

**Android modules:**
5. Add `androidTest.dependencies` block to each Android app's `build.gradle.kts` with `kotlin-testJunit`, `junit`, `androidx-testExt-junit`, `androidx-espresso-core` (these are already in `libs.versions.toml`)
6. Create test directories: `android/atriz/src/androidUnitTest/`, `android/fraccio/src/androidUnitTest/`

**iOS:**
7. The iOS test targets already exist in Xcode (`atrizTests`, `fraccioTests`, `atrizUITests`, `fraccioUITests`). No changes needed — tests will be written as features are developed.

### 2. GitHub Actions CI/CD

Create `.github/workflows/` with the following workflow files:

**`.github/workflows/ci.yml`** — Runs on every push and PR to `main` and `develop`:
1. **Lint & Format check** — Run the Kotlin linting/formatting tool (from step 3 below)
2. **Shared module tests** — `./gradlew :shared:allTests`
3. **Android unit tests** — `./gradlew :android:atriz:testDebugUnitTest :android:fraccio:testDebugUnitTest`
4. **Android debug build** — `./gradlew :android:atriz:assembleDebug :android:fraccio:assembleDebug`
5. Use `actions/setup-java@v4` with JDK 21 (temurin) and `gradle/actions/setup-gradle@v4` for caching
6. Cache Gradle dependencies and build outputs

**`.github/workflows/ios-ci.yml`** — Runs on every push and PR to `main` and `develop`:
1. Run on `macos-latest`
2. Build shared framework: `./gradlew :shared:linkReleaseFrameworkIosSimulatorArm64`
3. Build iOS apps with `xcodebuild` for both atriz and fraccio schemes (destination: iPhone 16 Simulator)
4. Run iOS unit tests with `xcodebuild test`

**`.github/workflows/deploy.yml`** — Manual trigger (`workflow_dispatch`) with environment input (dev/production):
1. **Android**: Build signed release APK/AAB (`assembleRelease` / `bundleRelease`), upload as artifact
2. **iOS**: Build archive with `xcodebuild archive`, export IPA, upload as artifact
3. Use GitHub secrets for signing: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` for Android; Apple certificates/provisioning profiles for iOS
4. Add placeholder steps for store upload (Google Play via `r0adkll/upload-google-play` action, App Store via `fastlane` or `xcrun altool`) — leave these commented out with TODO markers since signing credentials aren't configured yet

### 3. Code Quality Tools (Linting & Formatting)

Use **ktlint** via the `org.jlleitschuh.gradle.ktlint` Gradle plugin for Kotlin linting and formatting:

1. Add ktlint plugin version to `gradle/libs.versions.toml` under `[versions]` and `[plugins]`
2. Apply the ktlint plugin in the root `build.gradle.kts`
3. Create `.editorconfig` at the project root with ktlint rules:
   - `indent_size = 4`
   - `max_line_length = 120`
   - `insert_final_newline = true`
   - Disable `ktlint_standard_no-wildcard-imports` (KMP projects commonly use wildcard imports)
4. Verify `./gradlew ktlintCheck` and `./gradlew ktlintFormat` tasks are available

For **Swift** (iOS):
5. Create `.swiftlint.yml` at the project root with rules appropriate for SwiftUI projects
6. Add SwiftLint to the iOS CI workflow (`brew install swiftlint && swiftlint lint --strict ios/`)

### 4. Dependency Management

The version catalog in `gradle/libs.versions.toml` already handles Kotlin/Gradle dependencies well. Improve it:

1. Add a Gradle `dependencyUpdates` plugin (e.g., `com.github.ben-manes.versions`) to `gradle/libs.versions.toml` and root `build.gradle.kts` so the team can run `./gradlew dependencyUpdates` to check for outdated dependencies
2. Add a `renovate.json` or `dependabot.yml` (`.github/dependabot.yml`) configuration for automated dependency update PRs — cover Gradle dependencies and GitHub Actions versions

### 5. Project Structure & Conventions

Create a `CLAUDE.md` file at the project root that documents the project conventions for agent-assisted development:
- Module structure and what belongs where (`shared` vs `android/*` vs `ios/*`)
- Source set rules: shared logic in `commonMain`, platform-specific in `androidMain`/`iosMain`
- Package naming: `mx.atriz.shared.*` for shared, `mx.atriz.admin.*` for Atriz Android, `mx.atriz.fraccio.*` for Fraccio Android
- Build variant conventions (debug/dev/release)
- Dependency addition process (add to `libs.versions.toml` first, then reference via `libs.*`)
- Testing conventions (where test files go, naming pattern `*Test.kt`)

### 6. Sentry Error Tracking

1. Add the Sentry Kotlin Multiplatform SDK to `gradle/libs.versions.toml`:
   - `sentry-kotlin-multiplatform` library
2. Add Sentry dependency to `shared/build.gradle.kts` in `commonMain.dependencies`
3. Create a Sentry initialization wrapper at `shared/src/commonMain/kotlin/mx/atriz/shared/monitoring/SentryInit.kt`:
   - `fun initSentry(dsn: String, environment: String)` that configures Sentry with the DSN and environment name
   - Use `expect`/`actual` if platform-specific Sentry initialization is needed
4. Add placeholder DSN configuration that reads from build config (Android) and Info.plist (iOS) — use empty DSN by default so the app doesn't crash without Sentry configured
5. For iOS: Add the Sentry Cocoa SDK dependency (via SPM in the Xcode project or document the manual step as a TODO in a comment)

### 7. Logging

1. Add **Kermit** (by Touchlab) as the KMP logging library to `gradle/libs.versions.toml` and `shared/build.gradle.kts` `commonMain.dependencies`
   - Kermit provides a unified `Logger` API across all platforms with platform-native log output (Logcat on Android, os_log on iOS)
2. Create a logging setup file at `shared/src/commonMain/kotlin/mx/atriz/shared/logging/AppLogger.kt`:
   - Configure a shared `Logger` instance with appropriate severity filtering per build type (verbose in debug, warn+ in release)
   - Integrate with Sentry: add a custom `LogWriter` that forwards error/fatal logs to Sentry as breadcrumbs
3. Wire Kermit into the existing Ktor client logging: configure `ktor-client-logging` to use the Kermit logger

</instructions>

<examples>
## Design / Pattern to Follow

<example>
<description>Adding a new dependency: version catalog entry + build.gradle.kts usage</description>
<code>
# In gradle/libs.versions.toml:
[versions]
kermit = "2.0.4"

[libraries]
kermit = { module = "co.touchlab:kermit", version.ref = "kermit" }

# In shared/build.gradle.kts:
commonMain.dependencies {
    implementation(libs.kermit)
}
</code>
</example>

<example>
<description>Expect/actual pattern for platform-specific initialization</description>
<code>
// commonMain — shared/src/commonMain/kotlin/mx/atriz/shared/monitoring/SentryInit.kt
expect fun initSentry(dsn: String, environment: String)

// androidMain — shared/src/androidMain/kotlin/mx/atriz/shared/monitoring/SentryInit.android.kt
actual fun initSentry(dsn: String, environment: String) {
    SentryAndroid.init(context) { options ->
        options.dsn = dsn
        options.environment = environment
    }
}

// iosMain — shared/src/iosMain/kotlin/mx/atriz/shared/monitoring/SentryInit.ios.kt
actual fun initSentry(dsn: String, environment: String) {
    SentrySDK.start { options in
        options.dsn = dsn
        options.environment = environment
    }
}
</code>
</example>

<example>
<description>GitHub Actions workflow for Gradle-based KMP project</description>
<code>
name: CI
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew ktlintCheck

  test:
    runs-on: ubuntu-latest
    needs: lint
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew :shared:allTests
      - run: ./gradlew :android:atriz:testDebugUnitTest :android:fraccio:testDebugUnitTest
</code>
</example>

<example>
<description>commonTest sample test using kotlin-test</description>
<code>
// shared/src/commonTest/kotlin/mx/atriz/GreetingTest.kt
package mx.atriz

import kotlin.test.Test
import kotlin.test.assertTrue

class GreetingTest {
    @Test
    fun greetReturnsNonEmptyString() {
        val greeting = Greeting().greet()
        assertTrue(greeting.isNotEmpty())
        assertTrue(greeting.contains("Hello"))
    }
}
</code>
</example>

<example>
<description>.editorconfig for ktlint in a KMP project</description>
<code>
root = true

[*]
indent_style = space
indent_size = 4
end_of_line = lf
charset = utf-8
trim_trailing_whitespace = true
insert_final_newline = true

[*.{kt,kts}]
max_line_length = 120
ktlint_standard_no-wildcard-imports = disabled
</code>
</example>
</examples>

<constraints>
## Constraints

<over_engineering_prevention>
Keep solutions minimal and focused on the current task:

**Scope:** Only add what's listed in the task sections above. Don't implement features, screens, API clients, or database schemas — this is infrastructure only.

**Documentation:** Only create `CLAUDE.md` as specified. Don't add READMEs, ADRs, or other docs.

**Abstractions:** Don't create base classes, generic utilities, or framework wrappers beyond what's specified (SentryInit, AppLogger).

**File creation:** Don't create placeholder files for future features. Only create the files explicitly listed.
</over_engineering_prevention>

**Scope boundaries:**

- Only modify build files (`build.gradle.kts`, `libs.versions.toml`, `settings.gradle.kts`, `gradle.properties`) for dependency and plugin additions
- Only create new files in: `.github/workflows/`, `.editorconfig`, `.swiftlint.yml`, `.github/dependabot.yml`, `CLAUDE.md`, `shared/src/commonTest/`, `shared/src/commonMain/kotlin/mx/atriz/shared/monitoring/`, `shared/src/commonMain/kotlin/mx/atriz/shared/logging/`, and platform `actual` counterparts
- Do NOT modify any existing Kotlin source files (`Greeting.kt`, `Platform.kt`, `App.kt`, `MainActivity.kt`, `AppConfig.kt`) or Swift files
- Do NOT modify iOS Xcode project files (`.pbxproj`) — document manual Xcode steps as TODOs in comments instead
- All new Gradle dependencies must go through `libs.versions.toml` — never use hardcoded version strings in `build.gradle.kts`

**Quality standards:**

- GitHub Actions workflows must be valid YAML with correct syntax
- ktlint configuration must be compatible with Kotlin 2.3.0 and Compose
- Sentry and Kermit versions should be the latest stable releases compatible with this project's Kotlin version (2.3.0)
</constraints>

<expected_output>

## Expected Output

**Files created:**

- `.github/workflows/ci.yml` — Android + shared CI (lint, test, build)
- `.github/workflows/ios-ci.yml` — iOS CI (build, test)
- `.github/workflows/deploy.yml` — Manual deploy workflow with signing placeholders
- `.github/dependabot.yml` — Automated dependency update configuration
- `.editorconfig` — ktlint and editor formatting rules
- `.swiftlint.yml` — SwiftLint configuration for iOS code
- `CLAUDE.md` — Project conventions for agent-assisted development
- `shared/src/commonTest/kotlin/mx/atriz/GreetingTest.kt` — Sample shared test
- `shared/src/commonMain/kotlin/mx/atriz/shared/monitoring/SentryInit.kt` — Sentry init expect declaration
- `shared/src/androidMain/kotlin/mx/atriz/shared/monitoring/SentryInit.android.kt` — Android actual
- `shared/src/iosMain/kotlin/mx/atriz/shared/monitoring/SentryInit.ios.kt` — iOS actual
- `shared/src/commonMain/kotlin/mx/atriz/shared/logging/AppLogger.kt` — Kermit logger setup

**Files modified:**

- `gradle/libs.versions.toml` — Added versions and libraries for: ktlint plugin, kermit, sentry-kotlin-multiplatform, kotlinx-coroutines-test, koin-test, ktor-client-mock, ben-manes versions plugin
- `build.gradle.kts` (root) — Applied ktlint plugin and ben-manes versions plugin
- `shared/build.gradle.kts` — Added kermit and sentry to `commonMain.dependencies`; added coroutines-test, koin-test, ktor-client-mock to `commonTest.dependencies`
- `android/atriz/build.gradle.kts` — Added `androidTest.dependencies` block
- `android/fraccio/build.gradle.kts` — Added `androidTest.dependencies` block

**Verification checklist:**

- [ ] All paths are real, verified paths from the workspace
- [ ] All new dependencies use `libs.versions.toml` entries (no hardcoded versions)
- [ ] GitHub Actions workflows use correct Gradle task names for this project's module structure
- [ ] ktlint plugin version is compatible with Kotlin 2.3.0
- [ ] Kermit and Sentry versions are latest stable and compatible with Kotlin 2.3.0 / KMP
- [ ] Every `expect` declaration has matching `actual` implementations for both Android and iOS
- [ ] No existing source files (`.kt`, `.swift`) were modified
- [ ] No Xcode project files were modified
- [ ] `./gradlew ktlintCheck`, `./gradlew :shared:allTests`, `./gradlew assembleDebug` would succeed with these changes
- [ ] iOS CI workflow references correct Xcode scheme names and simulator destinations
</expected_output>
