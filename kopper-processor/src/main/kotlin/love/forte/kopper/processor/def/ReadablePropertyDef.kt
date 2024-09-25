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
import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.CodeBlock
import love.forte.kopper.annotation.PropertyType


/**
 * A real readable property in any [MapperActionSourceDef].
 * @author ForteScarlet
 */
internal data class ReadablePropertyDef(
    val environment: SymbolProcessorEnvironment,
    val resolver: Resolver,
    // 可以读取的属性，可以在 action source 中获取
    // 属性也许是函数类型的?
    // 属性也许是某个类型的子属性，而不是 source 来的，
    // 那么就是从其他 property 调用而来。

    val name: String,
    val declaration: KSDeclaration,
    val propertyType: PropertyType,

    /**
     * 当前属性类型是否可为 `null`
     */
    val nullable: Boolean,

    // 如果是嵌套的，它应该有个父属性，否则是个根属性
    val parent: ReadablePropertyDef?
) {
    override fun toString(): String {
        return buildString {
            parent?.also {
                append(it.toString())
                if (it.fullyNullable) {
                    append('?')
                }
                append('.')
            }
            append(name)
            if (propertyType == PropertyType.FUNCTION) {
                append("()")
            }
            append(" [declaration:")
            append(declaration.toString())
            append(']')
        }
    }
}

/**
 * 全链路nullable, 即如果 [ReadablePropertyDef.parent] 那么也会被影响为 `nullable`。
 */
internal val ReadablePropertyDef.fullyNullable: Boolean
    get() = fullyNullable()

private tailrec fun ReadablePropertyDef.fullyNullable(): Boolean {
    val p = parent ?: return nullable
    if (nullable) return true
    return p.fullyNullable()
}


internal fun ReadablePropertyDef.readerCode(sourceNullable: Boolean = false): CodeBlock {
    return CodeBlock.builder().apply {
        val parent = parent
        if (parent != null) {
            add(parent.readerCode(sourceNullable))
            if (sourceNullable || parent.fullyNullable) {
                add("?")
            }
            add(".")
        }

        add("%L", name)
        if (propertyType == PropertyType.FUNCTION) {
            add("()")
        }
    }.build()
}
