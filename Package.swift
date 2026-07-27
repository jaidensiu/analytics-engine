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
            url: "https://api.github.com/repos/jaidensiu/analytics-engine/releases/assets/ASSET_ID.zip",
            checksum: "CHECKSUM"
        )
    ]
)
