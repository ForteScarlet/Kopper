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

import com.squareup.kotlinpoet.CodeBlock

/**
 * A single map in [MapperMapSet].
 */
internal interface MapperMap {
    /**
     * Emit current Map to [writer].
     */
    fun emit(writer: MapperMapSetWriter, index: Int)
}


internal data class PropertyMapperMap(
    /**
     * The source. If the source is an eval expression,
     * the source will be the main source.
     */
    val source: MapSource,
    val sourceProperty: MapSourceProperty,
    val target: MapTarget,
    val targetProperty: MapTargetProperty
) : MapperMap {
    override fun emit(writer: MapperMapSetWriter, index: Int) {
        targetProperty.emit(writer, sourceProperty.read())
    }
}

internal data class EvalMapperMap(
    val eval: String,
    val evalNullable: Boolean,
    val target: MapTarget,
    val targetProperty: MapTargetProperty
) : MapperMap {
    override fun emit(writer: MapperMapSetWriter, index: Int) {
        targetProperty.emit(writer, PropertyRead(name = "eval", CodeBlock.of(eval), nullable = evalNullable))
    }
}

// TODO submodel (sub mapSet) mapper map?
