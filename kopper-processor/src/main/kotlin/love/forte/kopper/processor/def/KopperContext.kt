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

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.KSBuiltIns
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSType
import love.forte.kopper.annotation.Mapping

/**
 *
 * @author ForteScarlet
 */
internal data class KopperContext(
    val environment: SymbolProcessorEnvironment,
    val resolver: Resolver,
) {
    val logger get() = environment.logger

    // builtIns
    val builtIns: KSBuiltIns = resolver.builtIns

    fun isIterable(type: KSType?): Boolean {
        return type != null && builtIns.iterableType.isAssignableFrom(type.makeNotNullable())
    }

    private val mappingAnnoDeclaration = resolver.getClassDeclarationByName<Mapping>()
        ?: error("Cannot find `Map` annotation declaration.")

    val mapAnnoType = mappingAnnoDeclaration.asStarProjectedType()

    private val mappingTargetAnnoDeclaration = resolver.getClassDeclarationByName<Mapping.Target>()
        ?: error("Cannot find `Map.Target` declaration.")

    val mapTargetAnnoType = mappingTargetAnnoDeclaration.asStarProjectedType()

    private val mappingMainSourceAnnoDeclaration = resolver.getClassDeclarationByName<Mapping.MainSource>()
        ?: error("Cannot find `Map.MainSource` declaration.")

    val mapMainSourceAnnoType = mappingMainSourceAnnoDeclaration.asStarProjectedType()


}
