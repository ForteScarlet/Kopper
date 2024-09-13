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

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.CodeBlock
import love.forte.kopper.annotation.PropertyType
import love.forte.kopper.processor.mapper.MapSource
import love.forte.kopper.processor.mapper.MapSourceProperty

internal class ParameterMapSource(
    override val name: String,
    override val type: KSType,
) : MapSource {
    override fun property(name: String, type: KSType, propertyType: PropertyType): MapSourceProperty? {
        return when (propertyType) {
            PropertyType.PROPERTY -> findPropProperty(name, type)
            PropertyType.FUNCTION -> findFunProperty(name, type)
            PropertyType.AUTO -> findPropProperty(name, type) ?: findFunProperty(name, type)
        }
    }

    private fun findPropProperty(name: String, type: KSType): MapSourceProperty? {
        return (type.declaration as? KSClassDeclaration)
            ?.getAllProperties()
            // 返回值是 type
            ?.firstOrNull { it.simpleName.asString() == name && it.type.resolve() == type }
            ?.let { prop ->
                MapSourcePropertyImpl(
                    source = this,
                    name = prop.simpleName.asString(),
                    propertyType = PropertyType.PROPERTY,
                    type = prop.type.resolve(),
                )
            }
    }

    private fun findFunProperty(name: String, type: KSType): MapSourceProperty? {
        return (type.declaration as? KSClassDeclaration)
            ?.getAllFunctions()
            // 没有参数，有返回值，返回值是 type
            ?.firstOrNull {
                if (it.simpleName.asString() != name) return@firstOrNull false
                if (it.parameters.isEmpty()) return@firstOrNull false
                val returnType = it.returnType ?: return@firstOrNull false
                returnType.resolve() == type
            }
            ?.let { func ->
                MapSourcePropertyImpl(
                    source = this,
                    name = func.simpleName.asString(),
                    propertyType = PropertyType.PROPERTY,
                    type = func.returnType!!.resolve(),
                )
            }
    }
}


internal class MapSourcePropertyImpl(
    override val source: MapSource,
    override val name: String,
    override val propertyType: PropertyType,
    override val type: KSType,
) : MapSourceProperty {
    override fun read(): CodeBlock {
        val sourceNullable = source.nullable
        return if (sourceNullable) {
            when (propertyType) {
                PropertyType.FUNCTION -> CodeBlock.of("%L?.%L()", source.name, name)
                else -> CodeBlock.of("%L?.%L", source.name, name)
            }
        } else {
            when (propertyType) {
                PropertyType.FUNCTION -> CodeBlock.of("%L.%L()", source.name, name)
                else -> CodeBlock.of("%L.%L", source.name, name)
            }
        }
    }

}
