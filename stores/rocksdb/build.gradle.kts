plugins {
    `publishing-convention`
    `kotlin-multiplatform-with-android-convention`
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    android {
        namespace = "com.github.lamba92.kotlin.document.store.stores.rocksdb"
    }

    jvm()

    mingwX64()

    linuxX64()
    linuxArm64()

    macosArm64()

    iosArm64()
    iosSimulatorArm64()

    watchosArm64()
    watchosSimulatorArm64()

    tvosArm64()
    tvosSimulatorArm64()

    androidNativeX64()
    androidNativeX86()
    androidNativeArm64()
    androidNativeArm32()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                api(projects.core)
                api(libs.rocksdb.multiplatform)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.tests)
            }
        }

        jvmTest {
            dependencies {
                runtimeOnly(libs.junit.jupiter.engine)
                implementation(libs.junit.jupiter.api)
                implementation(kotlin("test-junit5"))
            }
        }

        val nativeDesktopTest by creating {
            dependsOn(commonTest.get())
        }

        mingwTest {
            dependsOn(nativeDesktopTest)
        }

        linuxTest {
            dependsOn(nativeDesktopTest)
        }

        macosTest {
            dependsOn(nativeDesktopTest)
        }

        val appleMobileTest by creating {
            dependsOn(commonTest.get())
        }
        iosTest {
            dependsOn(appleMobileTest)
        }
        watchosTest {
            dependsOn(appleMobileTest)
        }
        tvosTest {
            dependsOn(appleMobileTest)
        }

        val commonJvmMain by creating {
            dependsOn(commonMain.get())
        }
        androidMain {
            dependsOn(commonJvmMain)
        }
        jvmMain {
            dependsOn(commonJvmMain)
        }

        val commonJvmTest by creating {
            dependsOn(commonTest.get())
        }

        jvmTest {
            dependsOn(commonJvmTest)
        }
    }
}
