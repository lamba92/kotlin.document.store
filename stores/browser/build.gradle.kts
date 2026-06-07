@file:Suppress("OPT_IN_USAGE")

plugins {
    `publishing-convention`
    `kotlin-multiplatform-convention`
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    js {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }
    wasmJs {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                api(projects.core)
            }
        }
        commonTest {
            dependencies {
                implementation(projects.tests)
            }
        }
        jsMain {
            dependencies {
                api(npm("idb-keyval", "6.2.1"))
            }
        }
        wasmJsMain {
            dependencies {
                api(npm("idb-keyval", "6.2.1"))
            }
        }
    }
}
