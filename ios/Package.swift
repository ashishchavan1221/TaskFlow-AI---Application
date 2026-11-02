// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "TaskFlowAI",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(
            name: "TaskFlowAI",
            targets: ["TaskFlowAI"]
        ),
    ],
    dependencies: [
        .package(
            url: "https://github.com/firebase/firebase-ios-sdk.git",
            .upToNextMajor(from: "10.28.0")
        )
    ],
    targets: [
        .target(
            name: "TaskFlowAI",
            dependencies: [
                .product(name: "FirebaseAuth", package: "firebase-ios-sdk"),
                .product(name: "FirebaseFirestore", package: "firebase-ios-sdk"),
                .product(name: "FirebaseFunctions", package: "firebase-ios-sdk"),
                .product(name: "FirebaseStorage", package: "firebase-ios-sdk"),
                .product(name: "FirebaseMessaging", package: "firebase-ios-sdk")
            ],
            path: "TaskFlowAI"
        )
    ]
)
