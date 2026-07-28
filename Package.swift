// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "Orbit",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        .library(name: "Orbit", targets: ["Orbit"])
    ],
    targets: [
        .binaryTarget(
            name: "Orbit",
            // url/checksum are rewritten by the Publish workflow on every release -- these values
            // are only accurate again once a release has run under the new Orbit naming.
            url: "https://github.com/jaidensiu/orbit/releases/download/v0.0.2/Orbit-0.0.2.xcframework.zip",
            checksum: "bb211607123bc2784fbf9b86822deec824a730e071c11e425df19672e2b8cd40"
        )
    ]
)
