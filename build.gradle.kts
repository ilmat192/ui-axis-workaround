import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform") version "1.3.72"
    `maven-publish`
}

group = "org.jetbrains.kotlin.native.xcode"
version = "1.3.72.0"

repositories {
    jcenter()
}

kotlin {
    // The problematic headers are not available on watchOS and macOS
    // but we build this library for them too to simplify depending on it.
    ios()
    iosArm32()
    tvos()
    watchos()
    macosX64()

    targets.withType(KotlinNativeTarget::class.java) {
        compilations["main"].apply {
            // Workaround for https://youtrack.jetbrains.com/issue/KT-36721.
            val moduleName = "${project.group}.${project.name}"

            val manifest = file("src/manifest/manifest.properties")
            kotlinOptions.freeCompilerArgs += listOf(
                    "-manifest", manifest.absolutePath,
                    "-module-name", moduleName
            )
            compileKotlinTask.inputs.file(manifest)
        }

        // An improvised test.
        val presetName = preset?.name
        if (presetName != null && !presetName.startsWith("watch") && !presetName.startsWith("macos")) {
            compilations["test"].apply {
                tasks["check"].dependsOn(compileKotlinTask)
                cinterops.create("testInterop")
            }
        }
    }

    sourceSets["commonMain"].dependencies {
        api(kotlin("stdlib-common"))
    }

    sourceSets["commonTest"].dependencies {
        implementation(kotlin("test-annotations-common"))
    }
}

