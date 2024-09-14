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
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.withIndent
import love.forte.kopper.annotation.PropertyType
import love.forte.kopper.processor.mapper.impl.DeepPathMapSourceProperty
import love.forte.kopper.processor.mapper.impl.DirectMapSourceProperty
import love.forte.kopper.processor.mapper.impl.MapTargetPropertyImpl
import love.forte.kopper.processor.util.findPropProperty
import love.forte.kopper.processor.util.findProperty

/**
 * A type as a mapping source.
 */
internal data class MapSource(
    var sourceMapSet: MapperMapSet,
    var isMain: Boolean = false,
    var name: String,
    var type: KSType,
) {
    private val logger: KSPLogger
        get() = sourceMapSet.environment.logger

    val nullable: Boolean
        get() = type.nullability != Nullability.NOT_NULL

    private val properties: MutableMap<String, MapSourceProperty> = mutableMapOf()
    private val subSources: MutableMap<String, MapSource> = mutableMapOf()

    fun property(
        path: String,
        propertyType: PropertyType
    ): MapSourceProperty? {
        return property(path, path, propertyType).also {
            logger.info("Find property($path, $propertyType) in source $this: $it")
        }
    }

    private fun property(
        fullPath: String,
        path: String,
        propertyType: PropertyType
    ): MapSourceProperty? {
        if ('.' !in path) {
            return if (fullPath != path) {
                property0(path, propertyType)
            } else {
                property0(path, PropertyType.AUTO)
            }
        }

        val name = path.substringBefore('.')
        val currentProperty = property0(name, PropertyType.AUTO)
            ?: return null

        val subPath = path.substringAfter('.')

        val subSource = subSources.computeIfAbsent(
            currentProperty.name
        ) {
            MapSource(
                sourceMapSet = sourceMapSet,
                isMain = false,
                name = currentProperty.name,
                type = currentProperty.type
            )
        }

        val subProperty = subSource.property(
            fullPath,
            subPath,
            PropertyType.AUTO
        ) ?: return null

        return DeepPathMapSourceProperty(
            source = subSource,
            parentProperty = currentProperty,
            name = name + "_" + subProperty.name, // TODO name?
            propertyType = subProperty.propertyType,
            type = subProperty.type,
        )
    }

    private fun property0(
        name: String,
        propertyType: PropertyType
    ): MapSourceProperty? {
        var find = properties[name]
        logger.info("property0($name, $propertyType), 1: $find")
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
            logger.info("property0($name, $propertyType), 2: $find")
        }

        logger.info("properties cache: $properties")
        return find
    }
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
    fun read(): MapSourcePropertyRead
}


internal interface MapSourcePropertyRead {
    val property: MapSourceProperty
    val name: String
    val initialCode: CodeBlock
}

internal sealed class MapTarget(
    val mapSet: MapperMapSet,
    val name: String,
    val type: KSType,
) {
    val targetSourceMap: MutableMap<String, MapSourceProperty> = mutableMapOf()

    /**
     * Kotlin's nullable or Java's platform type.
     */
    open val nullable: Boolean
        get() = type.nullability != Nullability.NOT_NULL

    /**
     * find a property from this target.
     */
    fun property(name: String): MapTargetProperty? {
        return findPropProperty(
            name = name,
            type = type,
        ) {
            MapTargetPropertyImpl(
                target = this,
                name = it.simpleName.asString(),
                propertyType = PropertyType.PROPERTY,
                type = it.type.resolve(),
            )
        }
    }

    /**
     * The initial code with [name] if needed.
     */
    abstract fun emitInit(writer: MapperMapSetWriter)

    companion object {
        /**
         * Create a [MapTarget] with return [type] only.
         *
         */
        internal fun create(
            mapSet: MapperMapSet,
            type: KSType,
            targetSourceMap: MutableMap<String, MapSourceProperty>,
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

                        val sourceProperty = targetSourceMap.remove(parameter.name!!.asString())
                            ?: error("Source property for parameter ${parameter.name} not found.")

                        add(RequiredPair(parameter, sourceProperty))
                    }
                }
            }

            return InitialRequiredMapTarget(
                mapSet = mapSet,
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
            parameter: KSValueParameter,
            type: KSType,
        ): MapTarget {
            val name = parameter.name!!.asString()

            return IncludedParameterMapTarget(
                mapSet = mapSet,
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
            receiver: KSType,
        ): MapTarget {
            return ReceiverMapTarget(
                mapSet = mapSet,
                name = "this",
                type = receiver
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
    name: String,
    type: KSType,
    val requires: List<RequiredPair>,
) : MapTarget(mapSet, name, type) {
    override fun emitInit(writer: MapperMapSetWriter) {
        val code = CodeBlock.builder()
            .apply {
                add("val %L = %T(", name, type.toClassName())
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

        writer.add(code)
    }

}


private class IncludedParameterMapTarget(
    mapSet: MapperMapSet,
    val parameter: KSValueParameter,
    name: String,
    type: KSType,
) : MapTarget(mapSet, name, type) {
    override fun emitInit(writer: MapperMapSetWriter) {}
}

private class ReceiverMapTarget(
    mapSet: MapperMapSet,
    name: String,
    type: KSType,
) : MapTarget(mapSet, name, type) {
    override fun emitInit(writer: MapperMapSetWriter) {}
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

    fun emit(writer: MapperMapSetWriter, source: MapSourceProperty)
}
