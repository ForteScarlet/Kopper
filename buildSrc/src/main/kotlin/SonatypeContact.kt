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

object SonatypeContact {
    const val SYSTEM_PROPERTY_SONATYPE_USERNAME_NAME = "forte.gradle.common.sonatype.username.env.name"
    const val SYSTEM_PROPERTY_SONATYPE_PASSWORD_NAME = "forte.gradle.common.sonatype.password.env.name"

    const val DEFAULT_SONATYPE_USERNAME = "SONATYPE_USERNAME"
    const val DEFAULT_SONATYPE_PASSWORD = "SONATYPE_PASSWORD"

    @JvmField
    val SONATYPE_USERNAME: String = systemProp(SYSTEM_PROPERTY_SONATYPE_USERNAME_NAME)
        ?: DEFAULT_SONATYPE_USERNAME

    @JvmField
    val SONATYPE_PASSWORD: String = systemProp(SYSTEM_PROPERTY_SONATYPE_PASSWORD_NAME)
        ?: DEFAULT_SONATYPE_PASSWORD

}

data class SonatypeUserInfo(val username: String, val password: String)

val sonatypeUserInfoOrNull: SonatypeUserInfo? by lazy {
    val username = systemProp(SonatypeContact.SONATYPE_USERNAME) ?: return@lazy null
    val password = systemProp(SonatypeContact.SONATYPE_PASSWORD) ?: return@lazy null
    SonatypeUserInfo(username, password)
}


val sonatypeUserInfo: SonatypeUserInfo
    get() = sonatypeUserInfoOrNull ?: throw NullPointerException("current system sonatype user info is null.")

