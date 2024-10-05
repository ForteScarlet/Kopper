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

package love.forte.kopper.processor.def

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import love.forte.kopper.processor.util.isNullable
import java.util.concurrent.ConcurrentHashMap


/**
 *
 * @author ForteScarlet
 */
internal data class MapperActionTargetDef(
    val kopperContext: KopperContext,
    /**
     * The class declaration of this target.
     * From parameter, receiver or return type.
     */
    val declaration: KSClassDeclaration,
    /**
     * Incoming or `null` if no incoming parameter or receiver.
     */
    val incoming: MapActionIncoming?,
    /**
     * True if return is required
     */
    val returns: Boolean,
    /**
     * If [returns] can be null.
     */
    val nullable: Boolean,

    val node: KSNode?,
) {
    private val propertyCache = ConcurrentHashMap<String, TargetPropertyDef>()

    /**
     * Find root property from [declaration] by [name].
     */
    fun property(name: String): TargetPropertyDef? {
        fun find(): TargetPropertyDef? {
            // 先找找 requires
            requires?.forEach { require ->
                if (require.name == name) {
                    return require
                    // TODO required? asProperty?
                    // return require.asProperty()
                }
            }

            val foundProp = declaration.getAllProperties()
                // 寻找名字匹配的，类型似乎无关紧要——毕竟名字是唯一的
                .find { it.simpleName.asString() == name }
                ?: return null

            val type = foundProp.type.resolve()
            return ModifiablePropertyDef(
                kopperContext = kopperContext,
                name = name,
                type = type,
                declaration = type.declaration,
                nullable = type.nullability.isNullable,
                node = foundProp,
            )
        }

        return propertyCache.compute(name) { _, curr ->
            curr ?: find()
        }
    }

    /**
     * Required constructor parameters.
     * Null if it has no primary constructor.
     */
    val requires: List<RequiredParameterDef>? by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val constructor = declaration.primaryConstructor ?: return@lazy null

        constructor.parameters.map { parameter ->
            val type = parameter.type.resolve()
            RequiredParameterDef(
                kopperContext = kopperContext,
                name = parameter.name!!.asString(),
                type = type,
                declaration = type.declaration,
                nullable = type.nullability.isNullable,
                node = parameter,
                isVar = parameter.isVar,
                hasDefaultValue = parameter.hasDefault,
            )
        }
    }
}

