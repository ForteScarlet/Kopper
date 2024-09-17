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

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.CodeBlock
import love.forte.kopper.annotation.PropertyType

/**
 * A property for mapping source.
 */
internal interface MapSourceProperty {
    val source: MapSource

    /**
     * Name of source property.
     */
    val name: String

    /**
     * The [MapSourceProperty]'s [PropertyType].
     * - If it is a function, it must have no parameters and have a return value, e.g. `fun prop(): AType`.
     *   The `get` prefix is disregarded and its name can be specified manually and directly.
     */
    val propertyType: PropertyType

    /**
     * Kotlin's nullable or Java's platform type.
     */
    val nullable: Boolean

    /**
     * Read this property's value.
     * @return An expression that can be stored by a local variable,
     * the type of the expression is type
     */
    fun read(): PropertyRead
}

internal interface MapSourceTypedProperty : MapSourceProperty {
    val type: KSType
    override val nullable: Boolean
        get() = type.nullability != Nullability.NOT_NULL
}


internal data class PropertyRead(
    val name: String,
    val code: CodeBlock,
    val nullable: Boolean,
    val type: KSType? = null,
)

internal fun PropertyRead.codeWithCast(writer: MapperWriter, target: KSType): CodeBlock {
    return if (type != null) {
        writer.tryTypeCast(code, nullable, type, target)
    } else {
        code
    }
}


internal data class InternalMapSetSourceProperty(
    override val source: MapSource,
    override val type: KSType,
    override val name: String,
    override val propertyType: PropertyType,
    // val mapSet: MapperMapSet,
    val subFunName: String,
    val receiverProperty: MapSourceProperty?,
    val parameters: List<String>,
) : MapSourceTypedProperty {
    override fun read(): PropertyRead {
        val code = CodeBlock.builder()
            .apply {
                if (receiverProperty != null) {
                    val read = receiverProperty.read()
                    val con = if (read.nullable) "?." else "."
                    add(read.code)
                    add(con)
                }
                // else {
                //     val main = mapSet.sources.find { it.isMain }
                //     if (main != null) {
                //         val con = if (main.nullable) "?." else "."
                //         add("%L", main.name)
                //         add(con)
                //     }
                // }
                add("%L(", name)
                parameters.forEachIndexed { index, pname ->
                    add("%L", pname)
                    if (index != parameters.lastIndex) {
                        add(",")
                    }
                }
                add(")")
            }
            .build()

        return PropertyRead(
            name = source.name,
            code = code,
            nullable = nullable,
            type = type,
        )
    }
}


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
