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
            url: "https://api.github.com/repos/jaidensiu/analytics-engine/releases/assets/491703699.zip",
            checksum: "a1832e7c19ba25cdbcc66e74cf24fbcde07a3e29b8049a340b15edfc7f55c9c1"
        )
    ]
)
