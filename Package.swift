// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "Orbit",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        // Event definitions only (e.g. TabClicked, WorldIdTab) -- for consumers that just need
        // the shared vocabulary (testing, UI code) without pulling in the delivery runtime.
        // `Orbit` already re-exposes these same types under its own module, so don't import
        // both in the same target: they're two independently-compiled copies, and Swift would
        // treat `OrbitCatalog.TabClicked` and `Orbit.TabClicked` as unrelated types.
        .library(name: "OrbitCatalog", targets: ["OrbitCatalog"]),
        // Everything needed to send events: the catalog's event types plus the
        // queue/retry/transport runtime.
        .library(name: "Orbit", targets: ["Orbit"]),
    ],
    targets: [
        .binaryTarget(
            name: "OrbitCatalog",
            // url/checksum are rewritten by the Publish workflow on every release.
            url: "https://github.com/jaidensiu/orbit/releases/download/v0.0.5/OrbitCatalog-0.0.5.xcframework.zip",
            checksum: "8d2be91ad906d1c3009eb3b62d92fda14d7ba847107223b03031868ddaa8153e"
        ),
        .binaryTarget(
            name: "Orbit",
            // url/checksum are rewritten by the Publish workflow on every release.
            url: "https://github.com/jaidensiu/orbit/releases/download/v0.0.5/Orbit-0.0.5.xcframework.zip",
            checksum: "8860e81236965b7b0587a3f1b56a936f2bd27c4e2a6846d8d12c519e79f79ed6"
        ),
    ]
)
