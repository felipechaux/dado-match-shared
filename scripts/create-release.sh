#!/bin/bash
# create-release.sh
# Creates a complete GitHub release with the DadoMatchShared XCFramework.
#
# IMPORTANT: prefer using the GitHub Actions workflow by pushing a tag:
#   git tag v1.0.2 && git push origin v1.0.2
#
# Use this script for LOCAL / manual releases only.
# Usage:  ./scripts/create-release.sh <version>
# Example: ./scripts/create-release.sh 1.0.2

set -e

# ── Configuration ──────────────────────────────────────────────────────────────
FRAMEWORK_NAME="DadoMatchShared"
REPO="felipechaux/dado-match-shared"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
ZIP_NAME="${FRAMEWORK_NAME}.xcframework.zip"

# ── Colors ─────────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m'

log_info()    { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $1"; }
log_step()    { echo -e "${PURPLE}[STEP]${NC} $1"; }

# ── Usage ──────────────────────────────────────────────────────────────────────
show_usage() {
  echo "Usage: $0 <version>"
  echo ""
  echo "Examples:"
  echo "  $0 1.0.2"
  echo "  $0 2.1.0"
  echo ""
  echo "NOTE: For automated releases, push a tag instead:"
  echo "  git tag v1.0.2 && git push origin v1.0.2"
  echo ""
  echo "This script will:"
  echo "  1. Build the XCFramework (release build)"
  echo "  2. Create a git tag (v<version>)"
  echo "  3. Create a GitHub release with the zip asset"
  echo "  4. Update Package.swift with the download URL and checksum"
  echo "  5. Update VERSION and gradle.properties"
  echo "  6. Commit and push everything to main"
}

# ── Validate version format ────────────────────────────────────────────────────
validate_version() {
  local version="$1"
  if [[ ! $version =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    log_error "Invalid version format: '$version'. Must be X.Y.Z (e.g. 1.0.2)"
    exit 1
  fi
}

# ── Prerequisites ──────────────────────────────────────────────────────────────
check_prerequisites() {
  log_step "Checking prerequisites..."

  if ! git rev-parse --git-dir > /dev/null 2>&1; then
    log_error "Not in a git repository."
    exit 1
  fi

  local missing=()
  for tool in gh git zip swift; do
    if ! command -v "$tool" > /dev/null 2>&1; then
      missing+=("$tool")
    fi
  done

  if [ ${#missing[@]} -gt 0 ]; then
    log_error "Missing required tools: ${missing[*]}"
    exit 1
  fi

  if ! gh auth status > /dev/null 2>&1; then
    log_error "GitHub CLI not authenticated. Run: gh auth login"
    exit 1
  fi

  if ! git diff-index --quiet HEAD --; then
    log_error "Working directory is not clean. Commit or stash your changes first."
    git status --porcelain
    exit 1
  fi

  log_success "All prerequisites satisfied."
}

# ── Ensure main branch is up to date ──────────────────────────────────────────
ensure_main_branch() {
  log_step "Ensuring we're on main branch and up to date..."

  local current_branch
  current_branch=$(git rev-parse --abbrev-ref HEAD)

  if [ "$current_branch" != "main" ]; then
    log_info "Switching from '$current_branch' to 'main'..."
    git checkout main
  fi

  git fetch origin

  local behind
  behind=$(git rev-list --count HEAD..origin/main 2>/dev/null || echo "0")
  if [ "$behind" -gt 0 ]; then
    log_info "$behind commits behind origin/main — pulling..."
    git pull origin main
  fi

  log_success "main branch is ready."
}

# ── Check version not already released ────────────────────────────────────────
check_version_exists() {
  local version="$1"
  local tag="v$version"

  log_info "Checking if version $version already exists..."

  if git tag -l | grep -q "^$tag$"; then
    log_error "Tag $tag already exists locally."
    log_info "Delete it with: git tag -d $tag && git push origin --delete $tag"
    exit 1
  fi

  if gh release view "$tag" --repo "$REPO" > /dev/null 2>&1; then
    log_error "Release $tag already exists on GitHub."
    log_info "Delete it with: gh release delete $tag --repo $REPO --yes"
    exit 1
  fi

  log_success "Version $version is available."
}

# ── Build XCFramework ──────────────────────────────────────────────────────────
build_xcframework() {
  log_step "Building XCFramework (Release)..."
  cd "$PROJECT_DIR"
  chmod +x "$SCRIPT_DIR/build-xcframework.sh"
  "$SCRIPT_DIR/build-xcframework.sh"

  if [ ! -f "$ZIP_NAME" ]; then
    log_error "Zip file not found after build: $ZIP_NAME"
    exit 1
  fi
  log_success "XCFramework built and zipped."
}

# ── Compute checksum ───────────────────────────────────────────────────────────
compute_checksum() {
  log_info "Computing checksum..."
  local checksum
  checksum=$(swift package compute-checksum "$ZIP_NAME")
  echo "$checksum" > /tmp/dado_xcframework_checksum
  log_info "SHA256: $checksum"
}

# ── Create git tag ─────────────────────────────────────────────────────────────
create_git_tag() {
  local version="$1"
  local tag="v$version"
  log_step "Creating git tag: $tag"
  git tag -a "$tag" -m "Release $tag — DadoMatchShared XCFramework"
  git push origin "$tag"
  log_success "Tag $tag created and pushed."
}

# ── Create GitHub release ──────────────────────────────────────────────────────
create_github_release() {
  local version="$1"
  local tag="v$version"
  local checksum
  checksum="$(cat /tmp/dado_xcframework_checksum)"
  local build_date
  build_date=$(date '+%Y-%m-%d %H:%M:%S %Z')

  log_step "Creating GitHub release: $tag"

  local notes="# DadoMatchShared $tag

🚀 **New Release**

## 📦 What's Included
- Optimized XCFramework for iOS (release build)
- Compatible with iOS 16.0+
- Ready for Swift Package Manager integration

## 📋 Technical Details
| Field | Value |
|---|---|
| **File** | \`${FRAMEWORK_NAME}.xcframework.zip\` |
| **SHA256** | \`$checksum\` |
| **Built** | $build_date |

## 🔧 Swift Package Manager
\`\`\`swift
dependencies: [
    .package(url: \"https://github.com/$REPO\", from: \"$version\")
]
\`\`\`

---
*Manual release created on $build_date*"

  gh release create "$tag" "$ZIP_NAME" \
    --repo "$REPO" \
    --title "${FRAMEWORK_NAME} $tag" \
    --notes "$notes"

  log_success "GitHub release $tag created."
  log_info "URL: https://github.com/$REPO/releases/tag/$tag"
}

# ── Update Package.swift ───────────────────────────────────────────────────────
update_package_swift() {
  local version="$1"
  local tag="v$version"
  local checksum
  checksum="$(cat /tmp/dado_xcframework_checksum)"
  local download_url="https://github.com/$REPO/releases/download/$tag/${ZIP_NAME}"

  log_step "Updating Package.swift..."

  cp "$PROJECT_DIR/Package.swift" "$PROJECT_DIR/Package.swift.bak"

  cat > "$PROJECT_DIR/Package.swift" << SWIFT
// swift-tools-version: 5.9
// Auto-generated by create-release.sh — do not edit manually.
import PackageDescription

let package = Package(
    name: "DadoMatchShared",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(
            name: "DadoMatchShared",
            targets: ["DadoMatchSharedWrapper"]
        ),
    ],
    dependencies: [
        .package(url: "https://github.com/RevenueCat/purchases-hybrid-common.git", exact: "17.32.0"),
    ],
    targets: [
        .binaryTarget(
            name: "DadoMatchSharedBinary",
            url: "$download_url",
            checksum: "$checksum"
        ),
        .target(
            name: "DadoMatchSharedWrapper",
            dependencies: [
                "DadoMatchSharedBinary",
                .product(name: "PurchasesHybridCommon", package: "purchases-hybrid-common"),
                .product(name: "PurchasesHybridCommonUI", package: "purchases-hybrid-common"),
            ]
        )
    ]
)
SWIFT

  rm -f "$PROJECT_DIR/Package.swift.bak"
  log_success "Package.swift updated."
  log_info "Download URL: $download_url"
}

# ── Update VERSION and gradle.properties ───────────────────────────────────────
update_version_files() {
  local version="$1"

  log_info "Updating VERSION file..."
  echo "$version" > "$PROJECT_DIR/VERSION"

  log_success "VERSION → $version"
}

# ── Commit and push ────────────────────────────────────────────────────────────
commit_and_push() {
  local version="$1"
  local checksum
  checksum="$(cat /tmp/dado_xcframework_checksum)"

  log_step "Committing and pushing release changes to main..."

  git add Package.swift VERSION

  if git diff --cached --quiet; then
    log_info "No changes to commit (files already up to date)."
    return 0
  fi

  git commit -m "chore: release $version [skip ci]

- Update Package.swift with release download URL
- Update Package.swift checksum: $checksum
- Update VERSION to $version"

  git push origin main
  log_success "Changes pushed to origin/main."
}

# ── Cleanup ────────────────────────────────────────────────────────────────────
cleanup() {
  log_info "Cleaning up temporary files..."
  rm -f "$ZIP_NAME"
  rm -f /tmp/dado_xcframework_checksum
  log_success "Cleanup done."
}

# ── Main ───────────────────────────────────────────────────────────────────────
main() {
  local version="$1"

  echo "🚀 Starting manual release for DadoMatchShared..."
  echo ""

  if [ -z "$version" ] || [ "$version" = "-h" ] || [ "$version" = "--help" ]; then
    show_usage
    exit 0
  fi

  validate_version "$version"
  log_info "Creating release for version: $version"
  echo ""

  check_prerequisites
  ensure_main_branch
  check_version_exists "$version"
  build_xcframework
  compute_checksum
  create_git_tag "$version"
  create_github_release "$version"
  update_package_swift "$version"
  update_version_files "$version"
  commit_and_push "$version"
  cleanup

  echo ""
  log_success "🎉 Release v$version created successfully!"
  echo ""
  echo "╔══════════════════════════════════════════════════╗"
  echo "║               Release Summary                    ║"
  echo "╚══════════════════════════════════════════════════╝"
  echo "  🏷️  Tag:     v$version"
  echo "  📦  Asset:   ${FRAMEWORK_NAME}.xcframework.zip"
  echo "  🔗  URL:     https://github.com/$REPO/releases/tag/v$version"
  echo ""
  echo "Next steps:"
  echo "  1. Update your iOS project to use version $version"
  echo "  2. Test: File → Add Package Dependencies → $REPO"
  echo ""
}

main "$@"