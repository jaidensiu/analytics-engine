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
            url: "https://github.com/jaidensiu/orbit/releases/download/v0.0.4/Orbit-0.0.4.xcframework.zip",
            checksum: "908adf68af5a01470e971b138191888206d26a9077dfc7024eb7d7d2358d14e7"
        )
    ]
)
