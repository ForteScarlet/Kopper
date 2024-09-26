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

package love.forte.kopper.processor.def

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSNode
import love.forte.kopper.annotation.MapperGenTarget
import love.forte.kopper.annotation.MapperGenVisibility
import love.forte.kopper.processor.util.findArg
import love.forte.kopper.processor.util.findEnumArg
import love.forte.kopper.processor.util.findListArg

/**
 * @see love.forte.kopper.annotation.Mapper
 */
internal data class MapperArgs(
    val genTarget: NodeArg<MapperGenTarget>,
    val visibility: NodeArg<MapperGenVisibility>,
    val open: NodeArg<Boolean>,

    // name
    val genTargetName: NodeArg<String>,
    val genTargetNamePrefix: NodeArg<String>,
    val genTargetNameSuffix: NodeArg<String>,
    val genTargetPackages: NodeArg<List<String>>,
    val node: KSNode?,
) {
    val packageName: String = genTargetPackages.value.joinToString(".")
    inline fun targetName(declarationSimpleName: () -> String): String =
        genTargetNamePrefix.value +
            (genTargetName.value.takeIf { it.isNotEmpty() } ?: declarationSimpleName()) +
            genTargetNameSuffix.value

}

internal fun KSAnnotation.resolveMapperArgs(): MapperArgs {
    val genTarget = findEnumArg<MapperGenTarget>("genTarget")!!
    val visibility = findEnumArg<MapperGenVisibility>("visibility")!!

    // Name-related arguments
    val genTargetName: NodeArg<String> = findArg("genTargetName")!!
    val genTargetNamePrefix: NodeArg<String> = findArg("genTargetNamePrefix")!!
    val genTargetNameSuffix: NodeArg<String> = findArg("genTargetNameSuffix")!!
    val genTargetPackages: NodeArg<List<String>> = findListArg<String>("genTargetPackages")!!
    val open: NodeArg<Boolean> = findArg("open")!!

    return MapperArgs(
        genTarget = genTarget,
        visibility = visibility,
        genTargetName = genTargetName,
        genTargetNamePrefix = genTargetNamePrefix,
        genTargetNameSuffix = genTargetNameSuffix,
        genTargetPackages = genTargetPackages,
        open = open,
        node = this,
    )
}
