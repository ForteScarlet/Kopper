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

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.withIndent
import love.forte.kopper.annotation.PropertyType
import love.forte.kopper.processor.mapper.impl.MapTargetPropertyImpl
import love.forte.kopper.processor.util.findProperty
import love.forte.kopper.processor.util.isEvalExpression

/**
 * A type as a mapping source.
 */
internal interface MapSource {
    /**
     * Current [MapperMapSet]
     */
    val sourceSet: MapperMapSet

    /**
     * name of this source.
     */
    val name: String

    /**
     * Type of this source.
     */
    val type: KSType

    /**
     * Is a main source in [sourceSet]
     */
    val isMain: Boolean

    /**
     * Kotlin's nullable or Java's platform type.
     */
    val nullable: Boolean
        get() = type.nullability != Nullability.NOT_NULL

    /**
     * find a property from this source.
     */
    fun property(namePath: String, propertyType: PropertyType): MapSourceProperty?
}

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
     * Type of this property.
     */
    val type: KSType

    /**
     * Kotlin's nullable or Java's platform type.
     */
    val nullable: Boolean
        get() = type.nullability != Nullability.NOT_NULL

    /**
     * Read this property's value.
     * @return An expression that can be stored by a local variable,
     * the type of the expression is type
     */
    fun read(): MapSourceReadProperty
}


internal interface MapSourceReadProperty {
    val property: MapSourceProperty
    val name: String
    val initialCode: CodeBlock

    val readCode: CodeBlock
        get() = CodeBlock.builder()
            .apply {
                add("val %L = ", name)
                add(initialCode)
            }
            .build()
}

internal sealed class MapTarget protected constructor(
    val mapSet: MapperMapSet,
    val path: String,
    val name: String,
    val type: KSType,
) {
    /**
     * Kotlin's nullable or Java's platform type.
     */
    open val nullable: Boolean
        get() = type.nullability != Nullability.NOT_NULL

    /**
     * find a property from this target.
     */
    fun property(name: String, type: KSType, propertyType: PropertyType): MapTargetProperty? {
        return findProperty(
            name = name,
            type = type,
            propertyType = propertyType,
            onProperty = {
                MapTargetPropertyImpl(
                    target = this,
                    name = it.simpleName.asString(),
                    propertyType = PropertyType.PROPERTY,
                    type = it.type.resolve(),
                )
            },
            onFunction = {
                MapTargetPropertyImpl(
                    target = this,
                    name = it.simpleName.asString(),
                    propertyType = PropertyType.PROPERTY,
                    type = it.returnType!!.resolve(),
                )
            }
        )
    }

    /**
     * The initial code with [name] if needed.
     */
    abstract fun init(): CodeBlock?

    companion object {
        /**
         * Create a [MapTarget] with return [type] only.
         *
         */
        internal fun create(
            mapSet: MapperMapSet,
            path: String,
            type: KSType,
        ): MapTarget {
            val name = "__target"
            // TODO check type?
            val classDl = (type.declaration as KSClassDeclaration)
            val constructor = classDl.primaryConstructor ?: classDl.getConstructors().firstOrNull()

            val requires: List<RequiredPair> = if (constructor == null) {
                emptyList()
            } else {
                buildList {
                    for (parameter in constructor.parameters) {
                        require(parameter.isVal || parameter.isVar || parameter.hasDefault) {
                            "Constructor's parameter must be a property or has default value, but $parameter"
                        }

                        val mapArg = mapSet.findTargetMapArg(path)?.takeUnless { it.source.isEvalExpression }
                        val sourcePath =

                        mapSet.findSourceProperty(path)

                        val sourceProperty = mapSet.findSourceProperty(path)
                            ?: error("Can't found source property for target [$path]")

                        add(RequiredPair(parameter, sourceProperty))
                    }
                }
            }

            return InitialRequiredMapTarget(
                mapSet = mapSet,
                path = path,
                name = name,
                type = type,
                requires = requires
            )
        }

        /**
         * Create a [MapTarget] with included [KSValueParameter]
         *
         */
        internal fun create(
            mapSet: MapperMapSet,
            path: String,
            parameter: KSValueParameter,
            type: KSType,
        ): MapTarget {
            val name = parameter.name!!.asString()

            return IncludedParameterMapTarget(
                mapSet = mapSet,
                path = path,
                parameter = parameter,
                name = name,
                type = type,
            )
        }

        /**
         * Create a [MapTarget] with included [receiver]
         *
         */
        internal fun create(
            mapSet: MapperMapSet,
            path: String,
            receiver: KSType,
            type: KSType,
        ): MapTarget {
            return ReceiverMapTarget(
                mapSet = mapSet,
                path = path,
                name = "this",
                type = type
            )
        }


    }
}

internal data class RequiredPair(
    val require: KSValueParameter,
    val source: MapSourceProperty,
)

private class InitialRequiredMapTarget(
    mapSet: MapperMapSet,
    path: String,
    name: String,
    type: KSType,
    val requires: List<RequiredPair>,
) : MapTarget(mapSet, path, name, type) {
    override fun init(): CodeBlock {
        return CodeBlock.builder()
            .apply {
                add("val %L = %T(", name, type)
                withIndent {
                    requires.forEach { (require, source) ->
                        add("%L = ", require.name!!.asString())
                        if (require.type.resolve().nullability == Nullability.NOT_NULL && source.nullable) {
                            add(source.read().initialCode)
                            add("!!")
                        } else {
                            add(source.read().initialCode)
                        }
                        add(", ")
                    }
                }
                add(")")
            }
            .build()
    }

}


private class IncludedParameterMapTarget(
    mapSet: MapperMapSet,
    path: String,
    val parameter: KSValueParameter,
    name: String,
    type: KSType,
) : MapTarget(mapSet, path, name, type) {
    override fun init(): CodeBlock? = null
}

private class ReceiverMapTarget(
    mapSet: MapperMapSet,
    path: String,
    name: String,
    type: KSType,
) : MapTarget(mapSet, path, name, type) {
    override fun init(): CodeBlock? = null
}

/**
 * A property for mapping target.
 */
internal interface MapTargetProperty {
    val target: MapTarget

    /**
     * Name of target property.
     */
    val name: String

    /**
     * The [MapTargetProperty]'s [PropertyType].
     * - If it is a property, it must have a `var` property or a constructor property.
     * - If it is a function, it must have only one parameter, e.g. `fun prop(value: AType)`.
     *   The `set` prefix is disregarded and its name can be specified manually and directly.
     */
    val propertyType: PropertyType

    /**
     * Type of this property.
     */
    val type: KSType

    /**
     * Kotlin's nullable or Java's platform type.
     */
    val nullable: Boolean
        get() = type.nullability != Nullability.NOT_NULL

    /**
     * Set the value from Source
     */
    fun set(source: MapSourceProperty, sourceCode: CodeBlock): CodeBlock
}
