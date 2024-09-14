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

import com.squareup.kotlinpoet.CodeBlock
import love.forte.kopper.processor.mapper.*

internal data class PropertyMapperMap(
    /**
     * The source. If the source is an eval expression,
     * the source will be the main source.
     */
    override val source: MapSource,
    val sourceProperty: MapSourceProperty,

    /**
     * The target.
     */
    override val target: MapTarget,
    override val targetProperty: MapTargetProperty
) : MapperMap {
    override fun code(index: Int): CodeBlock {
        val code = CodeBlock.builder()
        // init source code
        val sourceProp = "_s_${source.name}_$index"
        val sourceParamCode = CodeBlock
            .builder()
            .apply {
                add("val %L = ", sourceProp)
                add(sourceProperty.read().name)
            }
            .build()

        code.add(sourceParamCode)
        code.addStatement("")

        // set to property
        val setCode = targetProperty.set(sourceProperty, CodeBlock.of("%L", sourceProp))
        code.add(setCode)
        // new line.
        code.addStatement("")

        return code.build()
    }
}
