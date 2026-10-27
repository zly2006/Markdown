/*
 * Copyright (c) 2026 Liyan Zhao (zly2006)
 * SPDX-License-Identifier: MIT
 */

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "Markdown"
include(":markdown-parser", ":markdown-runtime", ":markdown-renderer")
include(":latex-base", ":latex-parser", ":latex-renderer")
include(":codehighlight-parser", ":codehighlight-render")

listOf("latex-base", "latex-parser", "latex-renderer").forEach {
    project(":$it").projectDir = file("third_party/latex/$it")
}
listOf("codehighlight-parser", "codehighlight-render").forEach {
    project(":$it").projectDir = file("third_party/codehigh/$it")
}
