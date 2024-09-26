/*
 * Copyright (c) 2024. Kopper.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.net.URI

plugins {
    id("org.jetbrains.dokka")
}


tasks.named("dokkaHtml").configure {
    tasks.findByName("kaptKotlin")?.also { kaptKotlinTask ->
        dependsOn(kaptKotlinTask)
    }
    tasks.findByName("kspKotlin")?.also { kspKotlinTask ->
        dependsOn(kspKotlinTask)
    }
    tasks.findByName("kspKotlinJvm")?.also { kspKotlinTask ->
        dependsOn(kspKotlinTask)
    }
}
tasks.named("dokkaHtmlPartial").configure {
    tasks.findByName("kaptKotlin")?.also { kaptKotlinTask ->
        dependsOn(kaptKotlinTask)
    }
    tasks.findByName("kspKotlin")?.also { kspKotlinTask ->
        dependsOn(kspKotlinTask)
    }
    tasks.findByName("kspKotlinJvm")?.also { kspKotlinTask ->
        dependsOn(kspKotlinTask)
    }
}

// dokka config
tasks.withType<DokkaTaskPartial>().configureEach {
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        separateInheritedMembers = true
        mergeImplicitExpectActualDeclarations = true
        homepageLink = P.HOMEPAGE
    }

    dokkaSourceSets.configureEach {
        version = P.VERSION
        documentedVisibilities.set(
            listOf(
                DokkaConfiguration.Visibility.PUBLIC,
                DokkaConfiguration.Visibility.PROTECTED
            )
        )
        fun checkModule(projectFileName: String): Boolean {
            val moduleMdFile = project.file(projectFileName)
            if (moduleMdFile.exists()) {
                moduleMdFile.useLines { lines ->
                    val head = lines.first { it.isNotBlank() }.trim()
                    if (head == "# Module ${project.name}") {
                        includes.from(projectFileName)
                        return true
                    }
                }
            }

            return false
        }

        checkModule("Module.md")
        // if (!checkModule("Module.md")) {
        //     checkModule("README.md")
        // }

        sourceLink {
            localDirectory.set(projectDir.resolve("src"))
            val relativeTo = projectDir.relativeTo(rootProject.projectDir)
                .path
                .replace('\\', '/')

            remoteUrl.set(URI.create("${P.HOMEPAGE}/tree/main/$relativeTo/src/").toURL())
            remoteLineSuffix.set("#L")
        }

        perPackageOption {
            matchingRegex.set(".*internal.*") // will match all .internal packages and sub-packages
            suppress.set(true)
        }


        fun externalDocumentation(docUri: URI) {
            externalDocumentationLink {
                url.set(docUri.toURL())
                packageListUrl.set(docUri.resolve("package-list").toURL())
            }
        }

        externalDocumentation(uri("https://square.github.io/kotlinpoet/1.x/kotlinpoet/"))
        externalDocumentation(uri("https://square.github.io/kotlinpoet/1.x/interop-ksp/"))
    }
}
