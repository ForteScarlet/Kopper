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

import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.CodeBlock
import love.forte.kopper.annotation.PropertyType
import love.forte.kopper.processor.mapper.MapSourceProperty
import love.forte.kopper.processor.mapper.MapTarget
import love.forte.kopper.processor.mapper.MapTargetProperty


internal data class MapTargetPropertyImpl(
    override val target: MapTarget,
    override val name: String,
    override val propertyType: PropertyType,
    override val type: KSType,
) : MapTargetProperty {
    override fun set(source: MapSourceProperty, sourceCode: CodeBlock): CodeBlock {
        val propCon = if (target.nullable) "?." else "."

        return when (propertyType) {
            PropertyType.FUNCTION -> {
                if (nullable) {
                    CodeBlock.builder().apply {
                        // %L?.%L(code)
                        add("%L", target.name)
                        add(propCon)
                        add("%L(", name)
                        add(sourceCode)
                        add(")")
                    }.build()
                } else {
                    CodeBlock.builder()
                        .apply {
                            add(sourceCode)
                            add("${propCon}also { %L?.%L(it) }", target.name, name)
                        }.build()
                }
            }

            else -> {
                if (nullable) {
                    CodeBlock.builder().apply {
                        // %L?.%L = code
                        add("%L", target.name)
                        add(propCon)
                        add("%L = ", name)
                        add(sourceCode)
                    }.build()
                } else {
                    CodeBlock.builder()
                        .apply {
                            add(sourceCode)
                            add("${propCon}also { %L?.%L = it }", target.name, name)
                        }.build()
                }
            }
        }
    }
}
