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
            url: "https://github.com/jaidensiu/orbit/releases/download/v0.0.8/OrbitCatalog-0.0.8.xcframework.zip",
            checksum: "9a79047cb393f611e8e77b0653b8469079706745e1b32b5d125ed2ea702101cb"
        ),
    ]
)
