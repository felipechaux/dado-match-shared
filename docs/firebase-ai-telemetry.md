# AI icebreaker telemetry — Firebase setup

The `:shared` module now emits observability for icebreaker generation from
`RoutingIcebreakerService` via `AiTelemetry` (impl: `CrashlyticsAiTelemetry`):

| Signal | Tool | What you get |
| --- | --- | --- |
| Per-attempt latency + which provider won | **Analytics** event `ai_icebreaker` | params: `provider`, `success`, `latency_ms`, `premium`, `fell_back`, `error_code` |
| A provider failed / fell back | **Crashlytics** non-fatal `AiProviderException` | custom keys: `ai_provider`, `ai_error_code`, `ai_fell_back` |
| Both providers failed (user got nothing) | **Crashlytics** non-fatal `AiTotalFailureException` | custom keys: `ai_primary_error`, `ai_fallback_error` |

> Until the steps below are done in the **consuming apps**, all telemetry calls are
> harmless no-ops (every Firebase touch is wrapped in `runCatching`). `:shared` is a
> library/XCFramework — it cannot initialise Firebase itself.

## Android app module (consumer project)

1. Download `google-services.json` from the Firebase console → drop in the **app**
   module root (`app/google-services.json`).
2. Root `build.gradle.kts` plugins (add, `apply false`):
   ```kotlin
   alias(libs.plugins.google.services) apply false
   id("com.google.firebase.crashlytics") version "3.0.2" apply false
   ```
3. App module `build.gradle.kts`:
   ```kotlin
   plugins {
       id("com.google.gms.google-services")
       id("com.google.firebase.crashlytics")
   }
   ```
   GitLive pulls the native Firebase SDKs transitively; the two plugins enable
   config parsing and crash/mapping upload. `FirebaseApp` auto-initialises.

## iOS app (Xcode project that consumes the XCFramework)

1. Add `GoogleService-Info.plist` from the Firebase console to the app target.
2. Link the Firebase SDK products **in the app** (SwiftPM `firebase-ios-sdk`):
   `FirebaseAnalytics` and `FirebaseCrashlytics`. GitLive's bindings reference these
   symbols; the app must provide them.
3. Call `FirebaseApp.configure()` at launch (`@main App.init` / `AppDelegate`).
4. Add a Crashlytics **run-script build phase** that uploads dSYMs (input files:
   `${DWARF_DSYM_FOLDER_PATH}/${DWARF_DSYM_FILE_NAME}`, `$(SRCROOT)/$(BUILT_PRODUCTS_DIR)/$(INFOPLIST_PATH)`).

## Where the data shows up

- **Crashlytics → Issues → Non-fatals**: `AiProviderException` / `AiTotalFailureException`,
  filterable by the `ai_*` custom keys. This is your reliability / fallback-rate view.
- **Analytics → Events → `ai_icebreaker`**: per-provider `latency_ms` and `success`.
  Use **DebugView** for real-time during testing (console aggregation lags hours).
  For real latency comparison (NVIDIA vs Gemini), link Analytics → BigQuery and
  `AVG(latency_ms) GROUP BY provider, fell_back`.
