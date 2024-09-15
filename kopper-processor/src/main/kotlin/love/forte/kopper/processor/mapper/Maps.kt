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
import com.google.devtools.ksp.symbol.*
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

    private val properties: MutableMap<String, MapSourceTypedProperty> = mutableMapOf()
    private val subSources: MutableMap<String, MapSource> = mutableMapOf()

    fun property(
        path: String,
        propertyType: PropertyType
    ): MapSourceTypedProperty? = property(null, path, path, propertyType)

    private fun property(
        parentProperty: MapSourceProperty? = null,
        fullPath: String,
        path: String,
        propertyType: PropertyType
    ): MapSourceTypedProperty? {
        if ('.' !in path) {
            return if (parentProperty != null) {
                logger.info("has parent prop(${parentProperty}) for path $path")
                if (fullPath != path) {
                    propertyDeep0(this, parentProperty, type, path, propertyType, properties)
                } else {
                    propertyDeep0(this, parentProperty, type, path, PropertyType.AUTO, properties)
                }
            } else {
                if (fullPath != path) {
                    propertyDirect0(this, type, path, propertyType, properties)
                } else {
                    propertyDirect0(this, type, path, PropertyType.AUTO, properties)
                }
            }
        }

        val name = path.substringBefore('.')

        val currentProperty = if (parentProperty == null) {
            propertyDirect0(this, type, name, PropertyType.AUTO, properties)
        } else {
            propertyDeep0(this, parentProperty, type, name, PropertyType.AUTO, properties)
        } ?: return null

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

        return subSource.property(
            parentProperty = currentProperty,
            fullPath = fullPath,
            path = subPath,
            propertyType = PropertyType.AUTO
        )
    }


}


private fun propertyDirect0(
    source: MapSource,
    from: KSType,
    name: String,
    propertyType: PropertyType,
    properties: MutableMap<String, MapSourceTypedProperty>,
): MapSourceTypedProperty? = property0(
    from = from,
    name = name,
    propertyType = propertyType,
    properties = properties,
    onProperty = {
        DirectMapSourceProperty(
            source = source,
            name = it.simpleName.asString(),
            propertyType = PropertyType.PROPERTY,
            type = it.type.resolve(),
        )
    },
    onFunction = {
        DirectMapSourceProperty(
            source = source,
            name = it.simpleName.asString(),
            propertyType = PropertyType.FUNCTION,
            type = it.returnType!!.resolve(),
        )
    },
)

private fun propertyDeep0(
    source: MapSource,
    parentProperty: MapSourceProperty,
    from: KSType,
    name: String,
    propertyType: PropertyType,
    properties: MutableMap<String, MapSourceTypedProperty>,
): MapSourceTypedProperty? = property0(
    from = from,
    name = name,
    propertyType = propertyType,
    properties = properties,
    onProperty = {
        DeepPathMapSourceProperty(
            source = source,
            parentProperty = parentProperty,
            name = name,
            propertyType = PropertyType.PROPERTY,
            type = it.type.resolve(),
        )
    },
    onFunction = {
        DeepPathMapSourceProperty(
            source = source,
            parentProperty = parentProperty,
            name = name,
            propertyType = PropertyType.FUNCTION,
            type = it.returnType!!.resolve(),
        )
    },
)

private inline fun property0(
    from: KSType,
    name: String,
    propertyType: PropertyType,
    properties: MutableMap<String, MapSourceTypedProperty>,
    onProperty: (KSPropertyDeclaration) -> MapSourceTypedProperty?,
    onFunction: (KSFunctionDeclaration) -> MapSourceTypedProperty?
): MapSourceTypedProperty? {
    var find = properties[name]
    if (find == null) {
        find = findProperty(
            name = name,
            type = from,
            propertyType = propertyType,
            onProperty = onProperty,
            onFunction = onFunction
        )?.also { properties[name] = it }
    }

    return find
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
                    requires.forEachIndexed { index, (require, source) ->
                        val requireType = require.type.resolve()
                        val readCode = source.read().codeWithCast(writer.mapperWriter, requireType)
                        add("\n%L = ", require.name!!.asString())
                        if (requireType.nullability == Nullability.NOT_NULL && source.nullable) {
                            add(readCode)
                            add("!!")
                        } else {
                            add(readCode)
                        }

                        if (requires.lastIndex != index) {
                            add(",")
                        } else {
                            add("\n")
                        }
                    }
                }
                add(")\n")
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

    /**
     * emit a property setter with [read] into [writer]
     */
    fun emit(writer: MapperMapSetWriter, read: PropertyRead)
}
