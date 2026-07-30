// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "Orbit",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        // Event definitions only (e.g. TabClicked, WorldIdTab).
        .library(name: "OrbitCatalog", targets: ["OrbitCatalog"]),
    ],
    targets: [
        .binaryTarget(
            name: "OrbitCatalog",
            // url/checksum are rewritten by the Publish workflow on every release.
            url: "https://github.com/jaidensiu/orbit/releases/download/v0.0.7/OrbitCatalog-0.0.7.xcframework.zip",
            checksum: "63e81c2bbe2aca1ddb1e7568c37096262cc242dccabbf8003c647dea14ac842f"
        ),
    ]
)
