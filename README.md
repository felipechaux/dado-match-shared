# DadoMatch Shared KMP

Kotlin Multiplatform shared module for DadoMatch iOS and Android applications.

## 📱 Platforms

- **Android** (API 24+)
- **iOS** (iOS 16.0+)

## 🛠 Architecture

This module provides shared business logic, UI components, and utilities using:

- **Kotlin Multiplatform** for cross-platform code sharing
- **Compose Multiplatform** for shared UI components
- **Ktor** for networking
- **Koin** for dependency injection
- **Moko Resources** for shared resources

## 📦 Integration

### Android Integration

#### Local Development (Current Configuration)

The Android project is currently configured to use `includeBuild` for direct integration:

Add to your `settings.gradle.kts`:

```kotlin
includeBuild("../dado-match-shared")
```

Then in your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.dadomatch.shared:shared")
}
```

> **Note**: Published package integration is not necessary at the moment. We're using local `includeBuild` for development.

### iOS Integration

#### Manual Package Import in Xcode

You can add the package to your Xcode project through Swift Package Manager using either local or remote sources:

##### Option 1: Local Development (Recommended for Development)

1. **Prepare the local package**: Ensure `Package.swift` is configured correctly in the `dado-match-shared` repository folder.
2. In Xcode, go to **File → Add Package Dependencies**
3. Click **Add Local...**
4. Navigate to and select the `dado-match-shared` repository folder on your local machine
5. Click **Add Package**
6. After package verification, select target **DadoMatchShared**
7. Click **Add Package**
8. Build the project: **Product → Build** (⌘+B)

##### Option 2: Remote Package (Recommended for Production)

**Prerequisites**: For remote integration, you need to configure GitHub API authentication on your machine.

Create or update your `~/.netrc` file with your GitHub credentials:

```bash
machine api.github.com
  login [replace-with-your-username]
  password [replace-with-your-kmp-token]
machine github.com
  login [replace-with-your-username]  
  password [replace-with-your-kmp-token]
```

**Steps**:
1. In Xcode, go to **File → Add Package Dependencies**
2. Enter the package URL: `https://github.com/felipechaux/dado-match-shared`
3. Select **Branch** and choose **main** (currently only the main branch is supported, not specific versions)
4. Click **Add Package**
5. After package verification, select target **DadoMatchShared**
6. Click **Add Package**
7. Build the project: **Product → Build** (⌘+B)

#### Programmatic Integration

Or add to your `Package.swift`:

```swift
dependencies: [
    .package(url: "https://github.com/felipechaux/dado-match-shared", from: "1.0.0")
]
```

## 🔄 Versioning

This project follows [Semantic Versioning](https://semver.org/):

- **MAJOR**: Incompatible API changes
- **MINOR**: Backwards-compatible functionality additions
- **PATCH**: Backwards-compatible bug fixes

### Version History

- `1.0.0` - Initial release with core functionality

## 🚀 Development

### Getting Started

First, clone the repository:

```bash
git clone https://github.com/felipechaux/dado-match-shared
cd dado-match-shared
```

### Prerequisites

- JDK 17+
- Xcode 15+ (for iOS builds)
- Android SDK

### Building

```bash
# Run tests
./gradlew shared:test

# Build Android
./gradlew shared:assembleRelease

# Build iOS XCFramework

## For Development (Debug)
./gradlew shared:assembleDadoMatchSharedDebugXCFramework

## For Release
./gradlew shared:assembleDadoMatchSharedReleaseXCFramework
```

### Creating a Release

Create a release using the release script:
```bash
./scripts/create-release.sh 1.1.0
```

This will automatically:
- Update version in `gradle.properties`
- Create and push a git tag
- Trigger CI/CD to build and publish the release

## 🏗 CI/CD

The project uses GitHub Actions for:

- **Continuous Integration**: Build and test on every PR
- **Release Automation**: Automatically build and publish releases
- **XCFramework Distribution**: Generate and upload iOS frameworks
- **Package Publishing**: Publish to GitHub Packages

## 📄 License

Proprietary - ChauxDevApps

## 🤝 Contributing

1. Create a feature branch
2. Make your changes
3. Add tests if applicable
4. Create a pull request

## 📞 Support

For questions or issues, contact ChauxDevApps


