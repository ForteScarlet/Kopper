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
    kotlin("jvm")
    `kopper-dokka-partial-configure`
    `kopper-jvm-maven-publish`
    alias(libs.plugins.ksp)
}

kotlin {
    explicitApi()
    jvmToolchain(JVMConstants.KT_JVM_TARGET_VALUE)
    compilerOptions {
        javaParameters.set(true)
        jvmTarget.set(JVMConstants.KT_JVM_TARGET)
        freeCompilerArgs.addAll(
            "-Xjvm-default=all",
            "-Xjsr305=strict"
        )
    }
}

dependencies {
    api(project(":kopper-annotation"))
    // api(project(":kopper-common:kopper-common-transformer"))
    api(libs.bundles.kotlinPoet.ksp)

    // NOTE: It's important that you _don't_ use compileOnly here,
    // as it will fail to resolve at compile-time otherwise
    implementation(libs.autoService)
    ksp(libs.autoService.ksp)

    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
}

tasks.test {
    useJUnitPlatform()
}

ksp {
}
