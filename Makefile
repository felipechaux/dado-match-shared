.PHONY: dev remote build-framework help

XCFRAMEWORK_PATH := shared/build/XCFrameworks/release/DadoMatchShared.xcframework

## Activate local development mode:
##   1. Builds the debug XCFramework from source
##   2. Swaps Package.swift to use the local path
##   3. Marks Package.swift as assume-unchanged so git won't stage it
##
## After running `make dev`, open Xcode and do:
##   File → Packages → Reset Package Caches
dev: build-framework
	cp Package.local.swift Package.swift
	git update-index --assume-unchanged Package.swift
	@echo ""
	@echo "✅ Local dev mode active."
	@echo "   XCFramework: $(XCFRAMEWORK_PATH)"
	@echo "   In Xcode: File → Packages → Reset Package Caches"
	@echo "   Run 'make remote' before committing or tagging a release."

## Restore the remote release Package.swift and re-enable git tracking.
remote:
	git update-index --no-assume-unchanged Package.swift
	git checkout Package.swift
	@echo ""
	@echo "✅ Remote release Package.swift restored."
	@echo "   Package.swift is tracked by git again."

## Build the release XCFramework without switching Package.swift.
build-framework:
	@echo "🔨 Building release XCFramework..."
	./gradlew shared:assembleDadoMatchSharedReleaseXCFramework
	@echo "✅ Built: $(XCFRAMEWORK_PATH)"

help:
	@echo ""
	@echo "  make dev             Build XCFramework + activate local Package.swift"
	@echo "  make remote          Restore release Package.swift (before committing)"
	@echo "  make build-framework Rebuild XCFramework only (keep current Package.swift)"
	@echo ""
