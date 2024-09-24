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
import love.forte.kopper.processor.def.MapperActionTargetDef
import love.forte.kopper.processor.util.asClassDeclaration
import love.forte.kopper.processor.util.findPropProperty

internal sealed class MapperActionTarget(
    val def: MapperActionTargetDef,
    val generator: MapperActionsGenerator,
) {
    // TODO Local readable variable name
    val name: String = ""
    val targetSourceMap: MutableMap<Path, MapActionSourceProperty> = mutableMapOf()

    /**
     * Kotlin's nullable or Java's platform type.
     */
    open val nullable: Boolean
        get() = type.nullability != Nullability.NOT_NULL

    /**
     * find a property from this target.
     */
    fun property(name: String): MapActionTargetProperty? {
        return findPropProperty(
            name = name,
            declaration = def.declaration,
        ) {
            MapActionTargetPropertyImpl(
                target = this,
                name = it.simpleName.asString(),
                propertyType = PropertyType.PROPERTY,
                type = it.type.resolve(),
            )
        }
    }

    abstract fun emitInitBegin(writer: MapperActionWriter)

    abstract fun emitInitFinish(writer: MapperActionWriter)

    companion object {
        /**
         * Create a [MapperActionTarget] with return [type] only.
         *
         */
        internal fun create(
            action: MapperAction,
            type: KSType,
            targetSourceMap: MutableMap<Path, MapActionSourceProperty>,
            nullableParameter: String?,
        ): MapperActionTarget {
            val name = "__target"

            // TODO check type?
            val declaration = type.declaration.asClassDeclaration()

            require(declaration != null && !declaration.isAbstract()) {
                "Type $declaration is not a constructable type."
            }

            val constructor = declaration.primaryConstructor
                ?: error("Type $declaration must have a primary constructor.")
            // declaration.getConstructors().firstOrNull()

            val target = InitialRequiredMapperTarget(
                mapSet = action,
                name = name,
                type = type,
                nullableParameter = nullableParameter,
            )

            for (parameter in constructor.parameters) {
                require(parameter.isVal || parameter.isVar || parameter.hasDefault) {
                    "Constructor's parameter must be a property or has default value, but $parameter"
                }

                val pname = parameter.name!!.asString()

                val args = action.def.mapArgs.firstOrNull {
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
                        targetParameter = parameter,
                        nullableParameter = nullableParameter,
                    )

                    action.maps.add(requireMap)
                } else {
                    // check null
                    sourceProperty ?: error("Source property for parameter ${parameter.name} not found.")

                    val requireMap = SourceConstructorMapperMap(
                        source = sourceProperty.source,
                        sourceProperty = sourceProperty,
                        target = target,
                        targetParameter = parameter,
                        nullableParameter = nullableParameter,
                    )

                    action.maps.add(requireMap)
                }
            }

            return target
        }

        /**
         * Create a [MapperActionTarget] with included [KSValueParameter]
         *
         */
        internal fun create(
            mapSet: MapperAction,
            parameterName: String,
            type: KSType,
        ): MapperActionTarget {
            return IncludedParameterMapperTarget(
                mapSet = mapSet,
                name = parameterName,
                type = type,
            )
        }

        /**
         * Create a [MapperActionTarget] with included [receiver]
         *
         */
        internal fun create(
            mapSet: MapperAction,
            receiver: KSType,
        ): MapperActionTarget {
            return ReceiverMapTarget(
                mapSet = mapSet,
                name = "this",
                type = receiver
            )
        }
    }
}

private class IncludedParameterMapperTarget(
    mapSet: MapperAction,
    name: String,
    type: KSType,
) : MapperActionTarget(name, type) {
    override fun emitInitBegin(writer: MapperActionWriter) {
    }

    override fun emitInitFinish(writer: MapperActionWriter) {
    }
}

private class InitialRequiredMapperTarget(
    mapSet: MapperAction,
    name: String,
    type: KSType,
    val nullableParameter: String?
) : MapperActionTarget(name, type) {
    override fun emitInitBegin(writer: MapperActionWriter) {
        val code = CodeBlock.builder().apply {
            addStatement("val %L = %T(", name, type.toClassName())
            indent()
        }.build()
        writer.add(code)
    }

    override fun emitInitFinish(writer: MapperActionWriter) {
        writer.add(CodeBlock.builder().unindent().addStatement(")").build())
    }
}

private class ReceiverMapTarget(
    mapSet: MapperAction,
    name: String,
    type: KSType,
) : MapperActionTarget(name, type) {
    override fun emitInitBegin(writer: MapperActionWriter) {
    }

    override fun emitInitFinish(writer: MapperActionWriter) {
    }
}
