// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "AnalyticsEngine",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        .library(name: "AnalyticsEngine", targets: ["AnalyticsEngine"])
    ],
    targets: [
        .binaryTarget(
            name: "AnalyticsEngine",
            url: "https://api.github.com/repos/jaidensiu/analytics-engine/releases/assets/491734748.zip",
            checksum: "38500ea18c2eec4a66cd6b81e52bc06b186298a6edbf177f952ce8f8dca47d91"
        )
    ]
)
