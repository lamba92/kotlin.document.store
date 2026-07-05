plugins {
    id("kotlin-multiplatform-convention")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    android {
        compileSdk = 36
        minSdk = 21
        // `sourceSetTreeName = "test"` wires `androidDeviceTest -> commonTest`, so the shared
        // abstract test suites run on-device via a platform `actual`. Without it the AGP
        // KMP-library plugin leaves the device-test compilation an island.
        withDeviceTestBuilder { sourceSetTreeName = "test" }
            .configure { instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
    }
}
