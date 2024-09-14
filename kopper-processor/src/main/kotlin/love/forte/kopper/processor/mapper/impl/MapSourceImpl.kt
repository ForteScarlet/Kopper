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
import love.forte.kopper.processor.mapper.MapSourceReadProperty
import love.forte.kopper.processor.mapper.MapperMapSet
import love.forte.kopper.processor.util.findProperty

internal data class ParameterMapSource(
    override val sourceSet: MapperMapSet,
    override var isMain: Boolean,
    override val name: String,
    override val type: KSType,
) : MapSource {
    private val properties: MutableMap<String, MapSourceProperty> = mutableMapOf()

    override fun property(
        name: String,
        propertyType: PropertyType
    ): MapSourceProperty? {
        // TODO split name path?

        var find = properties[name]
        if (find == null) {
            find = findProperty(
                name = name,
                type = type,
                propertyType = propertyType,
                onProperty = {
                    DirectMapSourceProperty(
                        source = this,
                        name = it.simpleName.asString(),
                        propertyType = PropertyType.PROPERTY,
                        type = it.type.resolve(),
                    )
                },
                onFunction = {
                    DirectMapSourceProperty(
                        source = this,
                        name = it.simpleName.asString(),
                        propertyType = PropertyType.PROPERTY,
                        type = it.returnType!!.resolve(),
                    )
                }
            )?.also { properties[name] = it }
        }

        return find
    }
}

internal data class DirectMapSourceProperty(
    override val source: MapSource,
    override val name: String,
    override val propertyType: PropertyType,
    override val type: KSType,
) : MapSourceProperty {
    private var counter = 0

    override fun read(): MapSourceReadProperty {
        val sourceNullable = source.nullable
        val conOp = if (sourceNullable) "?." else "."
        val initialCode = when (propertyType) {
            PropertyType.FUNCTION -> CodeBlock.of("%L${conOp}%L()", source.name, name)
            else -> CodeBlock.of("%L${conOp}%L", source.name, name)
        }

        return MapSourceReadPropertyImpl(
            name = "${source.name}_${name}_${counter++}",
            initialCode = initialCode,
            property = this
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
) : MapSourceProperty {
    private var counter = 0

    override fun read(): MapSourceReadProperty {
        val sourceNullable = parentProperty.nullable
        val parentPropertyReadCode = parentProperty.read()
        val conOp = if (sourceNullable) "?." else "."
        val initialCode = when (propertyType) {
            PropertyType.FUNCTION -> {
                CodeBlock.builder()
                    .apply {
                        add(parentPropertyReadCode.name)
                        add(conOp)
                        add("%L()", name)
                    }.build()
            }

            else -> {
                CodeBlock.builder()
                    .apply {
                        add(parentPropertyReadCode.name)
                        add(conOp)
                        add("%L", name)
                    }
                    .build()
            }
        }

        return MapSourceReadPropertyImpl(
            name = "${source.name}_${name}_${counter++}",
            initialCode = initialCode,
            property = this
        )
    }
}


internal class EvalSourceProperty(
    override val name: String,
    override val source: MapSource,
    override val type: KSType,
    private val eval: String,
) : MapSourceProperty {
    override val propertyType: PropertyType
        get() = PropertyType.AUTO

    override fun read(): MapSourceReadProperty {
        return MapSourceReadPropertyImpl(
            CodeBlock.of(eval),
            property = this,
            name = name
        )
    }
}


private data class MapSourceReadPropertyImpl(
    override val initialCode: CodeBlock,
    override val property: MapSourceProperty,
    override val name: String
) : MapSourceReadProperty

