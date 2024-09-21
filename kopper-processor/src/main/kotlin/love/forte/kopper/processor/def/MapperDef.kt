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

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import love.forte.kopper.annotation.MapperGenTarget
import love.forte.kopper.annotation.MapperGenVisibility

// 一个Mapper的预定义，描述了这个Mapper中存在的内容，
// 用于后续的代码生成使用

/**
 * A definition of mapper.
 *
 * @author ForteScarlet
 */
internal data class MapperDef(
    val environment: SymbolProcessorEnvironment,
    val resolver: Resolver,
    // nullable?
    val sourceDeclaration: KSClassDeclaration,
    /**
     * The name of this mapper.
     */
    val simpleName: String,
    val packageName: String,

    /**
     * Declaration [MapperActionDef]s.
     */
    val declarationActions: List<MapperActionDef>,

    val genTarget: MapperGenTarget,
    val genVisibility: MapperGenVisibility,
    ) {
    /**
     * The unique qualified name of this mapper.
     */
    val qualifiedName: String = "$simpleName.$packageName"
}

