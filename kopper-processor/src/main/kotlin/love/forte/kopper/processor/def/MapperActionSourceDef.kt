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
import com.google.devtools.ksp.symbol.KSType
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
    val kopperContext: KopperContext,
    val type: KSType?,
    val declaration: KSClassDeclaration,
    /**
     * Incoming parameter or receiver.
     */
    val incoming: MapActionIncoming,
    val isMain: Boolean,
    val node: KSNode?,
) {
    /**
     * find Single (current root) property
     */
    fun property(name: String): ReadablePropertyDef? {
        return findPropertyDirect(declaration, name, null)
    }

    fun property(path: Path): ReadablePropertyDef? {
        return property(path, 1, declaration, path, null)
    }

    private tailrec fun property(
        full: Path,
        number: Int,
        declaration: KSClassDeclaration,
        path: Path,
        parent: ReadablePropertyDef?
    ): ReadablePropertyDef? {
        val current = findPropertyDirect(declaration, path.name, parent) ?: return null

        if (path.child == null) return current

        val currDeclaration = current.declaration.asClassDeclaration()
            ?: error(
                "Source $this's property ${path.name} " +
                    "with is the number $number in $full is not a class declaration."
            )

        return property(full, number + 1, currDeclaration, path.child, current)
    }

    override fun toString(): String {
        return "MapperActionSourceDef(declaration=$declaration, incoming=$incoming, isMain=$isMain)"
    }
}


private fun MapperActionSourceDef.findPropertyDirect(
    declaration: KSClassDeclaration,
    name: String,
    parent: ReadablePropertyDef?,
): ReadablePropertyDef? {

    return findPropPropertyDirect(declaration, name, parent)
        ?: findFunPropertyDirect(declaration, name, parent)
}

private fun MapperActionSourceDef.findPropPropertyDirect(
    declaration: KSClassDeclaration,
    name: String,
    parent: ReadablePropertyDef?,
): ReadablePropertyDef? {
    val firstProp = declaration.getAllProperties()
        .firstOrNull { it.simpleName.asString() == name }
        ?: return null

    val type = firstProp.type.resolve()

    return ReadablePropertyDef(
        kopperContext = kopperContext,
        name = name,
        type = type,
        declaration = type.declaration,
        nullable = type.nullability.isNullable,
        propertyType = PropertyType.PROPERTY,
        parent = parent,
        node = firstProp,
    )
}

private fun MapperActionSourceDef.findFunPropertyDirect(
    declaration: KSClassDeclaration,
    name: String,
    parent: ReadablePropertyDef?,
): ReadablePropertyDef? {
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

    return ReadablePropertyDef(
        kopperContext = kopperContext,
        name = name,
        type = type,
        declaration = type.declaration,
        nullable = type.nullability.isNullable,
        propertyType = PropertyType.FUNCTION,
        parent = parent,
        node = firstFun,
    )
}
