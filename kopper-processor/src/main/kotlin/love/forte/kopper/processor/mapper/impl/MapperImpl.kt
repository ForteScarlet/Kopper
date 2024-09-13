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

package love.forte.kopper.processor.mapper.impl

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import love.forte.kopper.annotation.Map

internal fun resolveToMapper(resolver: Resolver, declaration: KSClassDeclaration) { // TODO : Mapper
    val mapAnnoType = resolver.getClassDeclarationByName<Map>()
        ?: error("Cannot find Map annotation.")

    val abstractFunctions = declaration.getAllFunctions()
        .filter { it.isAbstract }
        .forEach { mapFun ->
            val mapAnnoList = mapFun.annotations.filter {
                mapAnnoType.asStarProjectedType().isAssignableFrom(it.annotationType.resolve())
            }.toList()

            println("mapFun:      $mapFun")
            println("MapAnnoList: $mapAnnoList")
            println()

        }

}
