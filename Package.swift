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
            url: "https://github.com/jaidensiu/orbit/releases/download/v0.0.6/OrbitCatalog-0.0.6.xcframework.zip",
            checksum: "3ecf6ee9c2623aa9afb906abe3fbf311ea1040a1b189922f5f83717bb7083a5f"
        ),
    ]
)
