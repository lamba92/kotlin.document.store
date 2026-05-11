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
        // Mirror the host monorepo's IJ-sync AGP downgrade (see ../settings.gradle.kts):
        // the IJ Android plugin lags behind stable AGP, so during IDEA sync (but not
        // Android Studio sync) the host pins agp = 9.0.0-alpha06. AgpVersionCompatibilityRule
        // refuses to mix AGP versions across composite builds, so this submodule has to
        // downgrade in lockstep — otherwise included-build :core / :stores:* fail
        // androidCompileClasspath resolution at sync time.
        create("libs") {
            from(files("gradle/versions.toml"))
            val isIdeaSync = System.getProperty("idea.sync.active") == "true"
            val isAndroidStudio = System.getProperty("idea.platform.prefix") == "AndroidStudio"
            if (isIdeaSync && !isAndroidStudio) {
                version("android-gradle-plugin", "9.0.0-alpha06")
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