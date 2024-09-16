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

import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ksp.toClassName
import love.forte.kopper.annotation.PropertyType
import love.forte.kopper.processor.util.asClassDeclaration
import love.forte.kopper.processor.util.findPropProperty

internal sealed class MapTarget(
    val mapSet: MapperMapSet,
    val name: String,
    val type: KSType,
) {
    val targetSourceMap: MutableMap<Path, MapSourceProperty> = mutableMapOf()

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

    abstract fun emitInitBegin(writer: MapperMapSetWriter)

    abstract fun emitInitFinish(writer: MapperMapSetWriter)

    companion object {
        /**
         * Create a [MapTarget] with return [type] only.
         *
         */
        internal fun create(
            mapSet: MapperMapSet,
            type: KSType,
            targetSourceMap: MutableMap<Path, MapSourceProperty>,
        ): MapTarget {
            val name = "__target"

            // TODO check type?
            val declaration = type.declaration.asClassDeclaration()

            require(declaration != null && !declaration.isAbstract()) {
                "Type $declaration is not a constructable type."
            }

            val constructor = declaration.primaryConstructor
                ?: error("Type $declaration must have a primary constructor.")
            // declaration.getConstructors().firstOrNull()

            val target = InitialRequiredMapTarget(mapSet = mapSet, name = name, type = type)

            for (parameter in constructor.parameters) {
                require(parameter.isVal || parameter.isVar || parameter.hasDefault) {
                    "Constructor's parameter must be a property or has default value, but $parameter"
                }

                val pname = parameter.name!!.asString()

                val args = mapSet.mapArgs.firstOrNull {
                    it.target == pname
                }

                val sourceProperty = targetSourceMap.remove(pname.toPath())

                if (args?.isEvalValid == true) {
                    val eval = args.eval
                    val evalNullable = args.evalNullable
                    // is eval
                    val requireMap = EvalConstructorMapperMap(
                        eval = eval,
                        evalNullable = evalNullable,
                        target = target,
                        targetParameter = parameter
                    )

                    mapSet.maps.add(requireMap)
                } else {
                    // check null
                    sourceProperty ?: error("Source property for parameter ${parameter.name} not found.")

                    val requireMap = SourceConstructorMapperMap(
                        source = sourceProperty.source,
                        sourceProperty = sourceProperty,
                        target = target,
                        targetParameter = parameter
                    )

                    mapSet.maps.add(requireMap)
                }
            }

            return target
        }

        /**
         * Create a [MapTarget] with included [KSValueParameter]
         *
         */
        internal fun create(
            mapSet: MapperMapSet,
            parameterName: String,
            type: KSType,
        ): MapTarget {
            return IncludedParameterMapTarget(
                mapSet = mapSet,
                name = parameterName,
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

private class IncludedParameterMapTarget(
    mapSet: MapperMapSet,
    name: String,
    type: KSType,
) : MapTarget(mapSet, name, type) {
    override fun emitInitBegin(writer: MapperMapSetWriter) {
    }

    override fun emitInitFinish(writer: MapperMapSetWriter) {
    }
}

private class InitialRequiredMapTarget(
    mapSet: MapperMapSet,
    name: String,
    type: KSType,
) : MapTarget(mapSet, name, type) {
    override fun emitInitBegin(writer: MapperMapSetWriter) {
        val code = CodeBlock.builder().apply {
            addStatement("val %L = %T(", name, type.toClassName())
            indent()
        }.build()
        writer.add(code)
    }

    override fun emitInitFinish(writer: MapperMapSetWriter) {
        writer.add(CodeBlock.builder().unindent().addStatement(")").build())
    }
}

private class ReceiverMapTarget(
    mapSet: MapperMapSet,
    name: String,
    type: KSType,
) : MapTarget(mapSet, name, type) {
    override fun emitInitBegin(writer: MapperMapSetWriter) {
    }

    override fun emitInitFinish(writer: MapperMapSetWriter) {
    }
}
