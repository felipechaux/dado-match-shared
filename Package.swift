// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "DadoMatchShared",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(
            name: "DadoMatchShared",
            targets: ["DadoMatchShared"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "DadoMatchShared",
            path: "./shared/build/XCFrameworks/debug/DadoMatchShared.xcframework"
        )
    ]
)
