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

import com.squareup.kotlinpoet.FileSpec
import love.forte.kopper.processor.def.MapperActionDef
import love.forte.kopper.processor.def.MapperActionSourceDef
import love.forte.kopper.processor.def.MapperActionTargetDef
import love.forte.kopper.processor.def.MapperDef


/**
 *
 * @author ForteScarlet
 */
internal class MapperGenerator(
    val defs: List<MapperDef>,
) {
    var files: MutableList<FileSpec.Builder> = mutableListOf()

    fun addFile(file: FileSpec.Builder) {
        files.add(file)
    }

    fun generate() {
        for (def in defs) {
            Mapper(def = def, generator = this).generate()
        }
    }
}


internal class MapperActionGenerator(
    val mapperGenerator: MapperGenerator,
) {
    // actions, with funSpec.Builder
    var actions: MutableList<MapperAction> = mutableListOf()

    fun addAction(action: MapperAction) {
        actions.add(action)
    }

    /**
     * 请求或创建一个对应 sources 和 target 的 MapperActionDef
     */
    fun requestAction(
        sources: Collection<MapperActionSourceDef>,
        target: MapperActionTargetDef,
    ): MapperActionDef {

        TODO()
    }
}
