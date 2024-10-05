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

import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    kotlin("jvm")
    alias(libs.plugins.dokka)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(8)
    compilerOptions {
        javaParameters.set(true)
        jvmTarget.set(JvmTarget.JVM_1_8)
        freeCompilerArgs.addAll(
            "-Xjvm-default=all",
            "-Xjsr305=strict"
        )
    }
}

dependencies {
    implementation(project(":kopper-annotation"))
    ksp(project(":kopper-processor"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

ksp {
}

tasks.withType<DokkaTaskPartial>().configureEach {
    enabled = false
}
