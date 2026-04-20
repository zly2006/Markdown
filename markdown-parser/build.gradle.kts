plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    jvmToolchain(21)

    androidLibrary {
        namespace = "com.hrm.markdown.parser"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "MarkdownParser"
            isStatic = true
        }
    }

    jvm()

    js {
        browser()
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral(true)

    signAllPublications()

    coordinates(
        "io.github.zly2006",
        "markdown-parser",
        rootProject.property("VERSION").toString()
    )

    pom {
        name.set("Kotlin Multiplatform Markdown Parser")
        description.set(
            """
            Cross-platform Markdown parsing solution with:
            - Full Markdown syntax support
            - AST (Abstract Syntax Tree) generation
            - Incremental parsing support
            - Multi-platform support (Android/iOS/JVM/JS/WasmJS)
        """.trimIndent()
        )
        inceptionYear.set("2026")
        url.set("https://github.com/zly2006/Markdown")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("zly2006")
                name.set("zly2006")
                url.set("https://github.com/zly2006/")
            }
        }
        scm {
            url.set("https://github.com/zly2006/Markdown")
            connection.set("scm:git:git://github.com/zly2006/Markdown.git")
            developerConnection.set("scm:git:ssh://git@github.com/zly2006/Markdown.git")
        }
    }
}
