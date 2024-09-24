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

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import love.forte.kopper.annotation.PropertyType
import love.forte.kopper.processor.mapper.Path
import love.forte.kopper.processor.util.asClassDeclaration
import love.forte.kopper.processor.util.isNullable


/**
 *
 * A source in a mapper action.
 *
 * @author ForteScarlet
 */
internal data class MapperActionSourceDef(
    val environment: SymbolProcessorEnvironment,
    val resolver: Resolver,
    val declaration: KSClassDeclaration,
    /**
     * Incoming parameter or receiver.
     */
    val incoming: MapActionIncoming,
    val isMain: Boolean,
) {

    /**
     * find Single (current root) property
     */
    fun property(name: String): ReadableProperty? {
        return findPropertyDirect(name, null)
    }

    fun property(path: Path): ReadableProperty? {
        return property(path, null)
    }

    private tailrec fun property(path: Path, parent: ReadableProperty?): ReadableProperty? {
        val current = findPropertyDirect(path.name, parent)
            ?: return null

        if (path.child == null) return current

        return property(path.child, current)
    }
}


private fun MapperActionSourceDef.findPropertyDirect(
    name: String,
    parent: ReadableProperty?,
): ReadableProperty? {
    return findPropPropertyDirect(name, parent)
        ?: findFunPropertyDirect(name, parent)
}

private fun MapperActionSourceDef.findPropPropertyDirect(
    name: String,
    parent: ReadableProperty?,
): ReadableProperty? {
    val firstProp = declaration.getAllProperties()
        .firstOrNull { it.simpleName.asString() == name }
        ?: return null

    val type = firstProp.type.resolve()

    return ReadableProperty(
        environment = environment,
        resolver = resolver,
        name = name,
        declaration = type.declaration,
        nullable = type.nullability.isNullable,
        propertyType = PropertyType.PROPERTY,
        parent = parent,
    )
}

private fun MapperActionSourceDef.findFunPropertyDirect(
    name: String,
    parent: ReadableProperty?,
): ReadableProperty? {
    val firstFun = declaration.asClassDeclaration()
        ?.getAllFunctions()
        // 没有参数，有返回值
        ?.filter { it.simpleName.asString() == name }
        ?.filter { it.parameters.isEmpty() }
        ?.filter { it.extensionReceiver == null }
        ?.filter { it.returnType != null }
        ?.firstOrNull()
        ?: return null

    val type = firstFun.returnType!!.resolve()

    return ReadableProperty(
        environment = environment,
        resolver = resolver,
        name = name,
        declaration = type.declaration,
        nullable = type.nullability.isNullable,
        propertyType = PropertyType.FUNCTION,
        parent = parent,
    )
}
