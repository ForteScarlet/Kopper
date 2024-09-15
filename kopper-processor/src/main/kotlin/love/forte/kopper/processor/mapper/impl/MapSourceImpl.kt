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
import love.forte.kopper.processor.mapper.MapSource
import love.forte.kopper.processor.mapper.MapSourceProperty
import love.forte.kopper.processor.mapper.MapSourceTypedProperty
import love.forte.kopper.processor.mapper.PropertyRead


internal data class DirectMapSourceProperty(
    override val source: MapSource,
    override val name: String,
    override val propertyType: PropertyType,
    override val type: KSType,
) : MapSourceTypedProperty {
    override fun read(): PropertyRead {
        val sourceNullable = source.nullable
        val conOp = if (sourceNullable) "?." else "."
        val initialCode = when (propertyType) {
            PropertyType.FUNCTION -> CodeBlock.of("%L${conOp}%L()", source.name, name)
            else -> CodeBlock.of("%L${conOp}%L", source.name, name)
        }

        return PropertyRead(
            name = source.name,
            code = initialCode,
            nullable = nullable,
            type = type,
        )
    }
}


/**
 * `a.b.c`
 */
internal class DeepPathMapSourceProperty(
    override val source: MapSource,
    private val parentProperty: MapSourceProperty,
    /**
     * Last final simple name.
     */
    override val name: String,
    override val propertyType: PropertyType,
    override val type: KSType,
) : MapSourceTypedProperty {
    override fun read(): PropertyRead {
        val parentPropertyReadCode = parentProperty.read()
        val conOp = if (parentPropertyReadCode.nullable) "?." else "."
        val initialCode = when (propertyType) {
            PropertyType.FUNCTION -> {
                CodeBlock.builder()
                    .apply {
                        add(parentPropertyReadCode.code)
                        add(conOp)
                        add("%L()", name)
                    }.build()
            }

            else -> {
                CodeBlock.builder()
                    .apply {
                        add(parentPropertyReadCode.code)
                        add(conOp)
                        add("%L", name)
                    }
                    .build()
            }
        }

        return PropertyRead(
            name = source.name,
            code = initialCode,
            nullable = nullable,
            type = type,
        )
    }
}


internal class EvalSourceProperty(
    override val name: String,
    override val source: MapSource,
    override val nullable: Boolean,
    private val eval: String,
) : MapSourceProperty {
    override val propertyType: PropertyType
        get() = PropertyType.AUTO

    override fun read(): PropertyRead {
        return PropertyRead(
            name = name,
            code = CodeBlock.of(eval),
            nullable = nullable,
            type = null,
        )
    }
}


