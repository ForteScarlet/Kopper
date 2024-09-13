import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

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

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.dokka)
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
    explicitApi()
    applyDefaultHierarchyTemplate()

    jvmToolchain(8)
    jvm {
        withJava()
        compilerOptions {
            javaParameters.set(true)
            jvmTarget.set(JvmTarget.JVM_1_8)
            freeCompilerArgs.addAll(
                "-Xjvm-default=all",
                "-Xjsr305=strict"
            )
            testRuns["test"].executionTask.configure {
                useJUnitPlatform()
            }
        }
    }

    js(IR) {
        nodejs()
        binaries.library()
    }
    // tier1
    macosX64()
    macosArm64()
    iosSimulatorArm64()
    iosX64()

    // tier2
    linuxX64()
    linuxArm64()
    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()
    iosArm64()

    // tier3
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    mingwX64()
    watchosDeviceArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
        binaries.library()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
        binaries.library()
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
