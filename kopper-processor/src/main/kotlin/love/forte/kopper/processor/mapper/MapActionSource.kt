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

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Nullability
import love.forte.kopper.annotation.PropertyType
import love.forte.kopper.processor.util.findProperty

/**
 * A type as a mapping source.
 */
internal data class MapActionSource(
    var sourceMapSet: MapperAction,
    var isMain: Boolean = false,
    var name: String,
    var type: KSType,
) {
    private val logger: KSPLogger
        get() = sourceMapSet.environment.logger

    val nullable: Boolean
        get() = type.nullability != Nullability.NOT_NULL

    private val properties: MutableMap<Path, MapSourceTypedProperty> = mutableMapOf()
    private val subSources: MutableMap<String, MapActionSource> = mutableMapOf()

    fun property(
        path: Path,
        propertyType: PropertyType
    ): MapSourceTypedProperty? = property(null, path, path, propertyType)

    private fun property(
        parentProperty: MapSourceProperty? = null,
        fullPath: Path,
        path: Path,
        propertyType: PropertyType
    ): MapSourceTypedProperty? {
        if (path.child == null) {
            return if (parentProperty != null) {
                logger.info("has parent prop(${parentProperty}) for path $path")
                if (fullPath != path) {
                    propertyDeep0(this, parentProperty, type, path.name, propertyType, properties)
                } else {
                    propertyDeep0(this, parentProperty, type, path.name, PropertyType.AUTO, properties)
                }
            } else {
                if (fullPath != path) {
                    propertyDirect0(this, type, path.name, propertyType, properties)
                } else {
                    propertyDirect0(this, type, path.name, PropertyType.AUTO, properties)
                }
            }
        }

        val name = path.name

        val currentProperty = if (parentProperty == null) {
            propertyDirect0(this, type, name, PropertyType.AUTO, properties)
        } else {
            propertyDeep0(this, parentProperty, type, name, PropertyType.AUTO, properties)
        } ?: return null

        val subPath = path.child

        val subSource = subSources.computeIfAbsent(
            currentProperty.name
        ) {
            MapActionSource(
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

    override fun toString(): String {
        return "MapSource(isMain=$isMain, name='$name', type=$type)"
    }


}


private fun propertyDirect0(
    source: MapActionSource,
    from: KSType,
    name: String,
    propertyType: PropertyType,
    properties: MutableMap<Path, MapSourceTypedProperty>,
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
    source: MapActionSource,
    parentProperty: MapSourceProperty,
    from: KSType,
    name: String,
    propertyType: PropertyType,
    properties: MutableMap<Path, MapSourceTypedProperty>,
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
    properties: MutableMap<Path, MapSourceTypedProperty>,
    onProperty: (KSPropertyDeclaration) -> MapSourceTypedProperty?,
    onFunction: (KSFunctionDeclaration) -> MapSourceTypedProperty?
): MapSourceTypedProperty? {
    val key = name.toPath()
    var find = properties[key]
    if (find == null) {
        find = findProperty(
            name = name,
            type = from,
            propertyType = propertyType,
            onProperty = onProperty,
            onFunction = onFunction
        )?.also { properties[key] = it }
    }

    return find
}
