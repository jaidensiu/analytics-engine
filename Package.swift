// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "AnalyticsEngineCore",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        .library(name: "AnalyticsEngineCore", targets: ["AnalyticsEngineCore"])
    ],
    targets: [
        .binaryTarget(
            name: "AnalyticsEngineCore",
            url: "https://api.github.com/repos/jaidensiu/analytics-engine/releases/assets/ASSET_ID.zip",
            checksum: "CHECKSUM"
        )
    ]
)
