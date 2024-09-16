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
internal data class PropertyPath(
    val root: Boolean,
    val name: String,
    val child: PropertyPath?,
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

internal fun PropertyPath.hasChild(): Boolean = child != null

internal fun PropertyPath.appendEnd(end: PropertyPath): PropertyPath {
    if (child == null) {
        return copy(child = end)
    }

    return copy(child = child.appendEnd(end))
}

internal operator fun PropertyPath.plus(childPath: PropertyPath): PropertyPath {
    return copy(
        root = true,
        child = child?.appendEnd(childPath.copy(root = false))
            ?: childPath.copy(root = false)
    )
}


internal fun String.toPropertyPath(root: Boolean = true): PropertyPath {
    val pointIndex = indexOf('.')
    if (pointIndex < 0) {
        // The end or the root
        return PropertyPath(
            root = root,
            name = this,
            child = null
        )
    }

    val currentPathName = substring(0, pointIndex)
    val subPaths = substring(pointIndex + 1, length)

    return PropertyPath(
        root = true,
        name = currentPathName,
        child = subPaths.toPropertyPath(false)
    )
}
