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

package love.forte.kopper.processor.mapper


/**
 *
 * @author ForteScarlet
 */
internal data class Path(
    val name: String,
    val child: Path?,
) {
    val paths: String
        get() = buildString {
            appendCurrentPaths(this)
        }

    private fun appendCurrentPaths(builder: Appendable) {
        builder.append(name)
        if (child != null) {
            builder.append('.')
        }
        child?.appendCurrentPaths(builder)
    }
}

internal fun Path.hasChild(): Boolean = child != null

internal fun Path.appendEnd(end: Path): Path {
    if (child == null) {
        return copy(child = end)
    }

    return copy(child = child.appendEnd(end))
}

internal operator fun Path.plus(childPath: Path): Path {
    return copy(
        child = child?.appendEnd(childPath)
            ?: childPath
    )
}

internal fun String.toPath(): Path {
    val pointIndex = indexOf('.')
    if (pointIndex < 0) {
        // The end or the root
        return Path(name = this, child = null)
    }

    val currentPathName = substring(0, pointIndex)
    val subPaths = substring(pointIndex + 1, length)

    return Path(
        name = currentPathName,
        child = subPaths.toPath()
    )
}
