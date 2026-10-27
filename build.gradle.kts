/*
 * Copyright (c) 2026 Liyan Zhao (zly2006)
 * SPDX-License-Identifier: MIT
 */

import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenLocal
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    kotlin("multiplatform") version "2.4.0" apply false
    kotlin("plugin.compose") version "2.4.0" apply false
    id("com.android.kotlin.multiplatform.library") version "9.4.0" apply false
    id("org.jetbrains.compose") version "1.11.1" apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
}

allprojects {
    group = rootProject.property("mavenGroup") as String
    version = rootProject.property("libraryVersion") as String
}

val localRepositoryUrl =
    layout.buildDirectory
        .dir("local-maven")
        .get()
        .asFile
        .toURI()

subprojects {
    apply(plugin = "maven-publish")

    tasks.withType<Jar>().configureEach {
        from(rootProject.file("distribution-resources"))
        filesMatching(
            listOf(
                "META-INF/LICENSE-MIT.txt",
                "META-INF/LICENSE-UPSTREAM-MIT.txt",
                "META-INF/LICENSE-APACHE-2.0.txt",
                "META-INF/NOTICE.md",
            ),
        ) {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }
    }

    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.named("commonMain") {
                resources.srcDir(rootProject.file("distribution-resources"))
            }
        }
        if (name.startsWith("markdown-")) {
            apply(plugin = "org.jlleitschuh.gradle.ktlint")
            extensions.configure<KtlintExtension> {
                android.set(true)
                enableExperimentalRules.set(true)
                filter {
                    exclude("**/build/**", "**/generated/**")
                }
            }
        }
    }

    extensions.configure<PublishingExtension> {
        repositories {
            maven {
                name = "LocalVerification"
                url = localRepositoryUrl
            }
        }
        publications.withType<MavenPublication>().configureEach {
            pom {
                name.set(project.name)
                description.set("Standalone Markdown library and its source dependencies, extracted from Zhihu++.")
                url.set("https://github.com/zly2006/Markdown")
                scm {
                    url.set("https://github.com/zly2006/Markdown")
                    connection.set("scm:git:https://github.com/zly2006/Markdown.git")
                    developerConnection.set("scm:git:ssh://git@github.com/zly2006/Markdown.git")
                }
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/license/mit")
                        distribution.set("repo")
                    }
                    if (project.name == "markdown-renderer") {
                        license {
                            name.set("Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                            distribution.set("repo")
                        }
                    }
                }
                developers {
                    developer {
                        id.set("huarangmeng")
                        name.set("huarangmeng")
                    }
                    if (project.name.startsWith("markdown-")) {
                        developer {
                            id.set("zly2006")
                            name.set("Liyan Zhao")
                        }
                    }
                }
            }
        }
    }

    tasks.withType<PublishToMavenRepository>().configureEach {
        // Gradle 会在配置缓存中丢弃 repository 模型；只有带目录检查的构件写入任务禁用该缓存。
        notCompatibleWithConfigurationCache("The project-local destination guard requires the live publishing repository model")
        val allowedRepositoryUrl = localRepositoryUrl
        doFirst {
            check(repository.url == allowedRepositoryUrl) {
                "This preparation project only supports the project-local verification repository; remote publication is not configured."
            }
        }
    }
    tasks.withType<PublishToMavenLocal>().configureEach {
        enabled = false
    }
}

tasks.register("stageLocalArtifacts") {
    group = "verification"
    description = "将 KMP 根、JVM 和 Android 构件写入工程内的本地验证仓库。"
    val modules =
        listOf(
            "markdown-parser",
            "markdown-runtime",
            "markdown-renderer",
            "latex-base",
            "latex-parser",
            "latex-renderer",
            "codehighlight-parser",
            "codehighlight-render",
        )
    dependsOn(
        modules.flatMap { module ->
            listOf("KotlinMultiplatform", "Jvm", "Android").map { publication ->
                ":$module:publish${publication}PublicationToLocalVerificationRepository"
            }
        },
    )
}
