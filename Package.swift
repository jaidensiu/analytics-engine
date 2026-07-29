// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "Orbit",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        .library(name: "OrbitCatalog", targets: ["OrbitCatalog"]),
    ],
    targets: [
        .binaryTarget(
            name: "OrbitCatalog",
            url: "https://github.com/jaidensiu/orbit/releases/download/v0.0.5/OrbitCatalog-0.0.5.xcframework.zip",
            checksum: "8d2be91ad906d1c3009eb3b62d92fda14d7ba847107223b03031868ddaa8153e"
        ),
    ]
)
