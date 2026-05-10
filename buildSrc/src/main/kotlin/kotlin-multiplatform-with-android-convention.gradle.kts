plugins {
    id("kotlin-multiplatform-convention")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    android {
        compileSdk = 35
        minSdk = 21
    }
}
