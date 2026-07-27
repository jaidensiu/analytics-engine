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
            url: "https://github.com/jaidensiu/analytics-engine/releases/download/v0.0.2/AnalyticsEngine-0.0.2.xcframework.zip",
            checksum: "bb211607123bc2784fbf9b86822deec824a730e071c11e425df19672e2b8cd40"
        )
    ]
)
