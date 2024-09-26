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

import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration

plugins {
    id("org.jetbrains.dokka")
}

repositories {
    mavenCentral()
}

tasks.named<org.jetbrains.dokka.gradle.DokkaMultiModuleTask>("dokkaHtmlMultiModule") {
    moduleName.set("Kopper")
    outputDirectory.set(rootProject.file("build/dokka/html"))

    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        // footerMessage = "© 2022-${Year.now().value} <a href='https://github.com/simple-robot'>Simple Robot</a>. All rights reserved."
        separateInheritedMembers = true
        mergeImplicitExpectActualDeclarations = true
        homepageLink = P.HOMEPAGE
    }
}

