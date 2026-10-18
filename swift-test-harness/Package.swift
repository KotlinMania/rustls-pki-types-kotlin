// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "SwiftTestHarness",
    platforms: [
        .macOS(.v15)
    ],
    dependencies: [
        .package(name: "RustlsPkiTypes", path: "../build/SPMPackage/macosArm64/Debug")
    ],
    targets: [
        .testTarget(
            name: "SwiftTestHarnessTests",
            dependencies: [
                .product(name: "RustlsPkiTypesLibrary", package: "RustlsPkiTypes")
            ],
            path: "Tests/SwiftTestHarnessTests",
            linkerSettings: [
                .unsafeFlags([
                    "-L", "../build/swift-test",
                    "-lRustlsPkiTypes",
                    "-Xlinker", "-rpath",
                    "-Xlinker", "-rpath",
                ]),
            ]
        ),
    ]
)
