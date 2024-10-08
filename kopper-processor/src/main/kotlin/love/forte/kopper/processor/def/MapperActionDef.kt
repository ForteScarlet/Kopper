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

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode


/**
 * A definition of mapper action that mapping some Sources to a Target in a mapper.
 *
 * @author ForteScarlet
 */
internal data class MapperActionDef(
    val kopperContext: KopperContext,
    /**
     * The name of this action in a mapper.
     */
    val name: String,
    val mappingArgs: List<MappingArgs>,
    val sources: List<MapperActionSourceDef>,
    val target: MapperActionTargetDef,
    /**
     * The super source function.
     */
    val sourceFun: KSFunctionDeclaration?,
    val node: KSNode?,
    )
