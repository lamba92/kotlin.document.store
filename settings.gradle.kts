@file:Suppress("UnstableApiUsage")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("com.gradle.develocity") version "4.0.2"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    rulesMode = RulesMode.PREFER_SETTINGS
    versionCatalogs {
        create("libs") {
            from(files("gradle/versions.toml"))
            // Lockstep with the host IDEA-sync AGP pin (../settings.gradle.kts):
            // AgpVersionCompatibilityRule forbids mixing AGP versions across the composite build,
            // so this submodule pins the same IDE-supported 9.0.0 during sync.
            val isIdeaSync = System.getProperty("idea.sync.active") == "true"
            val isAndroidStudio = System.getProperty("idea.platform.prefix") == "AndroidStudio"
            if (isIdeaSync && !isAndroidStudio) {
                version("android-gradle-plugin", "9.0.0")
            }
        }
    }
}

rootProject.name = "kotlin-document-store"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":core",
    ":samples:js-http-client",
    ":samples:kmp-app",
    ":samples:ktor-server",
    ":stores:browser",
    ":stores:rocksdb",
    ":stores:mvstore",
    ":tests",
    ":version-catalog",
)

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
        publishing {
            onlyIf { isCi }
        }
    }
}

val isCi
    get() = System.getenv("CI") == "true"