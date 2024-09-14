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

import com.google.devtools.ksp.symbol.KSAnnotation
import love.forte.kopper.annotation.Map
import love.forte.kopper.annotation.PropertyType
import love.forte.kopper.processor.util.findArg

internal data class MapArgs(
    val target: String,
    val source: String,
    val sourceName: String,
    val targetType: PropertyType,
    val sourceType: PropertyType,
    val ignore: Boolean
)

/**
 * @see love.forte.kopper.annotation.Map
 */
internal fun KSAnnotation.resolveToMapArgs(): MapArgs {
    val target: String = findArg("target")!!
    val source: String = findArg("source")!!
    val sourceName: String = findArg("sourceName")!!
    val targetType: PropertyType = findArg("targetType")!!
    val sourceType: PropertyType = findArg("sourceType")!!
    val ignore: Boolean = findArg("ignore")!!

    return MapArgs(
        target = target,
        source = if (Map.SAME_AS_TARGET == source) target else source,
        sourceName = sourceName,
        targetType = targetType,
        sourceType = sourceType,
        ignore = ignore
    )
}
