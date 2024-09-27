/*
 * Copyright (c) 2023-2024. Kopper.
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

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.publish.maven.MavenPom
import org.gradle.kotlin.dsl.assign
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("Sonatype Userinfo")

// private val sonatypeUserInfo by lazy {
//     val userInfo = love.forte.gradle.common.publication.sonatypeUserInfoOrNull
//
//     if (userInfo == null) {
//         logger.warn("sonatype.username or sonatype.password is null, cannot config nexus publishing.")
//     }
//
//     userInfo
// }
//
// val sonatypeUsername: String? get() = sonatypeUserInfo?.username
// val sonatypePassword: String? get() = sonatypeUserInfo?.password
//
// val ReleaseRepository by lazy {
//     Repositories.Central.Default.copy(SimpleCredentials(sonatypeUsername, sonatypePassword))
// }
// val SnapshotRepository by lazy {
//     Repositories.Snapshot.Default.copy(SimpleCredentials(sonatypeUsername, sonatypePassword))
// }

fun MavenPom.configPom(projectName: String) {
    this.name = projectName
    description = P.DESCRIPTION
    url = P.HOMEPAGE
    licenses {
        license {
            name = "Apache License, Version 2.0"
            url = "https://www.apache.org/licenses/LICENSE-2.0"
        }
    }
    scm {
        url = P.HOMEPAGE
        connection = P.SCM_CONNECTION
        developerConnection = P.SCM_DEVELOPER_CONNECTION
    }
    developers {
        developer {
            id = "forte"
            name = "ForteScarlet"
            email = "ForteScarlet@163.com"
            url = "https://github.com/ForteScarlet"
            properties.put("homepage", "https://forte.love")
        }
    }

    issueManagement {
        system.set("GitHub Issues")
        url.set("https://github.com/ForteScarlet/Kopper/issues")
    }
}

fun RepositoryHandler.configPublishRepositories(
    isSnapshot: Boolean,
) {
    mavenLocal()
    if (isSnapshot) {
        maven {
            name = "Snapshot"
            setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
            val (username, password) = sonatypeUserInfoOrNull ?: return@maven
            credentials {
                this.username = username
                this.password = password
            }
        }
    } else {
        maven {
            name = "Release"
            // https://central.sonatype.org/publish/publish-maven/#distribution-management-and-authentication
            setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            val (username, password) = sonatypeUserInfoOrNull ?: run {
                logger.warn("sonatypeUserInfo is null.")
                return@maven
            }
            credentials {
                this.username = username
                this.password = password
            }
        }
        // maven {
        //     name = "Release"
        //
        // setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
        // val (username, password) = sonatypeUserInfoOrNull ?: return@maven
        // credentials {
        //     this.username = username
        //     this.password = password
        // }
        // }
    }
}
