// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "Orbit",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        // Event definitions only (e.g. TabClicked, WorldIdTab) -- for consumers that just need the
        // shared vocabulary (testing, UI code) without pulling in the delivery runtime. `Orbit`
        // already re-exposes these same types under its own module, so don't import both in the
        // same target: they're two independently-compiled copies, and Swift would treat
        // `OrbitCatalog.TabClicked` and `Orbit.TabClicked` as unrelated types.
        .library(name: "OrbitCatalog", targets: ["OrbitCatalog"]),
        // Everything needed to send events: the catalog's event types plus the queue/retry/transport
        // runtime.
        .library(name: "Orbit", targets: ["Orbit"]),
    ],
    targets: [
        .binaryTarget(
            name: "OrbitCatalog",
            url: "https://github.com/jaidensiu/orbit/releases/download/v0.0.3/OrbitCatalog-0.0.3.xcframework.zip",
            checksum: "0000000000000000000000000000000000000000000000000000000000000000"
        ),
        .binaryTarget(
            name: "Orbit",
            url: "https://github.com/jaidensiu/orbit/releases/download/v0.0.4/Orbit-0.0.4.xcframework.zip",
            checksum: "908adf68af5a01470e971b138191888206d26a9077dfc7024eb7d7d2358d14e7"
        )
    ]
)
