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

object P {
    const val GROUP = "love.forte.kopper"
    const val DESCRIPTION = "A KSP data model properties mapper for Kotlin"
    const val HOMEPAGE = "https://github.com/ForteScarlet/Kopper"
    const val VERSION = "0.1.0"
    private const val NEXT_VERSION = "0.1.1"
    const val NEXT_SNAP_VERSION = "$NEXT_VERSION-SNAPSHOT"

    const val SCM_CONNECTION = "scm:git:$HOMEPAGE.git"
    const val SCM_DEVELOPER_CONNECTION = "scm:git:ssh://git@github.com/ForteScarlet/Kopper.git"
}

private val isSnapshotLazy: Boolean by lazy {
    systemProp("IS_SNAPSHOT").toBoolean()
}

fun isSnapshot(): Boolean = isSnapshotLazy

private val isCILazy: Boolean by lazy {
    systemProp("IS_CI").toBoolean()
}

fun isCI(): Boolean = isCILazy
