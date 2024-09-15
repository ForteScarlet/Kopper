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
import love.forte.kopper.annotation.MapperGenTarget
import love.forte.kopper.annotation.MapperGenVisibility
import love.forte.kopper.processor.util.findArg
import love.forte.kopper.processor.util.findEnumArg
import love.forte.kopper.processor.util.findListArg

internal data class MapperArgs(
    val genTarget: MapperGenTarget,
    val visibility: MapperGenVisibility,

    // name
    val genTargetName: String,
    val genTargetNamePrefix: String,
    val genTargetNameSuffix: String,
    val genTargetPackages: List<String>,
) {
    val packageName: String = genTargetPackages.joinToString(".")
    inline fun targetName(declarationSimpleName: () -> String): String =
        genTargetNamePrefix +
            (genTargetName.takeIf { it.isNotEmpty() } ?: declarationSimpleName()) +
            genTargetNameSuffix

}

internal fun KSAnnotation.resolveMapperArgs(): MapperArgs {
    val genTarget = findEnumArg<MapperGenTarget>("genTarget")!!
    val visibility = findEnumArg<MapperGenVisibility>("visibility")!!

    // Name-related arguments
    val genTargetName: String = findArg("genTargetName")!!
    val genTargetNamePrefix: String = findArg("genTargetNamePrefix")!!
    val genTargetNameSuffix: String = findArg("genTargetNameSuffix")!!
    val genTargetPackages: List<String> = findListArg<String>("genTargetPackages")!!

    return MapperArgs(
        genTarget = genTarget,
        visibility = visibility,
        genTargetName = genTargetName,
        genTargetNamePrefix = genTargetNamePrefix,
        genTargetNameSuffix = genTargetNameSuffix,
        genTargetPackages = genTargetPackages
    )
}
