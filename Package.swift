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
            url: "https://github.com/jaidensiu/orbit/releases/download/v0.0.10/OrbitCatalog-0.0.10.xcframework.zip",
            checksum: "76790a5660306713432605fe1c6981d6d3087a11fdd1e0f0b9eec2b5b21dc96f"
        ),
    ]
)
