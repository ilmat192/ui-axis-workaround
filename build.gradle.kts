import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform") version "1.3.71"
    `maven-publish`
}

// TODO: Determine a group/name/version.
group = "org.jetbrains.kotlin"
version = "1.3.71"

repositories {
    jcenter()
}

kotlin {
    ios()
    iosArm32()
    tvos()
    // There is no requested headers for watchOS.

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
        compilations["test"].apply {
            tasks["check"].dependsOn(compileKotlinTask)
            cinterops.create("testInterop")
        }
    }

    sourceSets["commonMain"].dependencies {
        api(kotlin("stdlib-common"))
    }

    sourceSets["commonTest"].dependencies {
        implementation(kotlin("test-annotations-common"))
    }
}

