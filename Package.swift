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
            url: "https://github.com/jaidensiu/orbit/releases/download/v0.0.3/Orbit-0.0.3.xcframework.zip",
            checksum: "5c6e492192492eb24947731cffd02b82db44dc83551e62c45c3fea445552e3a9"
        )
    ]
)
