// swift-tools-version: 5.9
// LOCAL DEVELOPMENT ONLY – points to the locally built XCFramework.
// Use `make dev` to activate and `make remote` to restore the release version.
// Do NOT commit Package.swift when this is active.
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
            path: "shared/build/XCFrameworks/release/DadoMatchShared.xcframework"
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
