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

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.withType
import org.gradle.process.CommandLineArgumentProvider
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget


@OptIn(ExperimentalKotlinGradlePluginApi::class)
inline fun KotlinJvmTarget.configJava(crossinline block: KotlinJvmTarget.() -> Unit = {}) {
    withJava()
    compilerOptions {
        javaParameters.set(true)
        freeCompilerArgs.addAll(
            "-Xjvm-default=all"
        )

    }

    testRuns["test"].executionTask.configure {
        useJUnitPlatform {
            val dir = project.rootProject.layout.buildDirectory.dir("test-reports/jvm/html/${project.name}")
            reports.html.outputLocation.set(dir)
        }
    }
    block()
}


fun KotlinTopLevelExtension.configJavaToolchain(jdkVersion: Int) {
    jvmToolchain(jdkVersion)
}

inline fun KotlinMultiplatformExtension.configKotlinJvm(
    jdkVersion: Int = JVMConstants.KT_JVM_TARGET_VALUE,
    crossinline block: KotlinJvmTarget.() -> Unit = {}
) {
    configJavaToolchain(jdkVersion)
    jvm {
        configJava(block)
    }
}

inline fun KotlinJvmProjectExtension.configKotlinJvm(
    jdkVersion: Int = JVMConstants.KT_JVM_TARGET_VALUE,
    crossinline block: KotlinJvmProjectExtension.() -> Unit = {}
) {
    configJavaToolchain(jdkVersion)
    compilerOptions {
        javaParameters = true
        jvmTarget.set(JvmTarget.fromTarget(jdkVersion.toString()))
        // freeCompilerArgs.addAll("-Xjvm-default=all", "-Xjsr305=strict")
        freeCompilerArgs.set(freeCompilerArgs.getOrElse(emptyList()) + listOf("-Xjvm-default=all", "-Xjsr305=strict"))
    }
    block()
}

inline fun Project.configJavaCompileWithModule(
    moduleName: String? = null,
    jvmVersion: String = JVMConstants.KT_JVM_TARGET_STR,
    crossinline block: JavaCompile.() -> Unit = {}
) {
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = jvmVersion
        targetCompatibility = jvmVersion

        if (moduleName != null) {
            options.compilerArgumentProviders.add(CommandLineArgumentProvider {
                // Provide compiled Kotlin classes to javac – needed for Java/Kotlin mixed sources to work
                listOf("--patch-module", "$moduleName=${sourceSets["main"].output.asPath}")
            })
        }

        block()
    }
}

@PublishedApi
internal val Project.sourceSets: SourceSetContainer
    get() = extensions.getByName<SourceSetContainer>("sourceSets")
