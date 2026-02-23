#!/bin/bash
# build-xcframework.sh
# Builds and zips the DadoMatchShared XCFramework in RELEASE mode.
# Usage: ./scripts/build-xcframework.sh

set -e

FRAMEWORK_NAME="DadoMatchShared"
XCFRAMEWORK_PATH="shared/build/XCFrameworks/release/${FRAMEWORK_NAME}.xcframework"
ZIP_NAME="${FRAMEWORK_NAME}.xcframework.zip"

echo "🚀 Building ${FRAMEWORK_NAME} XCFramework (Release)..."

# ── Clean previous builds ──────────────────────────────────────────────────────
echo "🧹 Cleaning previous builds..."
./gradlew clean

# ── Build Release XCFramework ──────────────────────────────────────────────────
echo "🔨 Running Gradle task: assembleDadoMatchSharedReleaseXCFramework"
./gradlew shared:assembleDadoMatchSharedReleaseXCFramework

# ── Verify output ──────────────────────────────────────────────────────────────
if [ ! -d "$XCFRAMEWORK_PATH" ]; then
  echo "❌ XCFramework not found at: $XCFRAMEWORK_PATH"
  exit 1
fi

echo "✅ XCFramework built at: $XCFRAMEWORK_PATH"

# ── Zip for distribution ───────────────────────────────────────────────────────
echo "📦 Creating zip: $ZIP_NAME"
cd shared/build/XCFrameworks/release
zip -r "$ZIP_NAME" "${FRAMEWORK_NAME}.xcframework"
mv "$ZIP_NAME" ../../../../
cd ../../../../

echo "✅ Zip created: $ZIP_NAME"
ls -lh "$ZIP_NAME"

# ── Calculate checksum ─────────────────────────────────────────────────────────
echo ""
echo "🔐 Calculating checksum..."
CHECKSUM=$(swift package compute-checksum "$ZIP_NAME")
echo "📋 Checksum: $CHECKSUM"

echo ""
echo "🎉 XCFramework build completed!"
echo ""
echo "Next steps (if releasing manually):"
echo "  1. Run: ./scripts/create-release.sh <version>"
echo "  OR"
echo "  2. Push a tag: git tag v<version> && git push origin v<version>"
echo "     → GitHub Actions will handle the rest automatically."
