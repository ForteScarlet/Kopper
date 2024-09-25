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

import com.squareup.kotlinpoet.CodeBlock
import love.forte.kopper.processor.def.*


/**
 *
 * @author ForteScarlet
 */
internal interface MapperActionStatement {
    /**
     * Emit current ActionStatement to [writer].
     */
    fun emit(writer: MapperActionWriter, index: Int)
}

// Mapper Action statement,
// 从一个或0个（eval）source中，
// 读取属性、并赋予一个 target。

/**
 * Eval, 没有 source
 */
internal class EvalMapperActionStatement(
    private val mapArgs: MapArgs,
    private val eval: String,
    private val evalNullable: Boolean,
    private val action: MapperAction,
    private val targetDef: MapperActionTargetDef,
    private val property: TargetPropertyDef,
) : MapperActionStatement {
    override fun emit(writer: MapperActionWriter, index: Int) {
        writer.add(CodeBlock.of("%L = ", property.name))
        writer.add("(")
        writer.add(CodeBlock.of(eval))
        writer.add(")")

        if (!property.nullable && evalNullable) {
            writer.add("!!")
        }
    }
}

internal class FromSourceMapperActionStatement(
    private val sourceDef: MapperActionSourceDef,
    private val sourceProperty: ReadablePropertyDef,
    private val targetProperty: MapActionTargetProperty,
) : MapperActionStatement {
    override fun emit(writer: MapperActionWriter, index: Int) {
        val targetPropertyRef = targetProperty.propertyRefName

        // writer.add(CodeBlock.of("%L = ", targetProperty.name))
        val readerCode = CodeBlock.builder().apply {
            val incoming = sourceDef.incoming
            val sourceNullable = incoming.nullable
            add("%L", incoming.name ?: "this")
            if (sourceNullable) {
                add("?")
            }
            add(".")
            add(sourceProperty.readerCode(sourceNullable))
        }.build()

        // 是构造，必须提供
        when {
            targetProperty.def.nullable ||
                !sourceProperty.fullyNullable -> {
                writer.add(targetPropertyRef)
                // target 是 nullable，
                // 或者同时 source 也是 non-null
                writer.add(" = ")
                writer.add(readerCode)
            }

            // target is non-null,
            // source is nullable

            //// 是必须的，不能通过属性判断
            //// 直接添加 !!
            targetProperty.def.isRequired -> {
                writer.add(targetPropertyRef)
                writer.add(" = (")
                writer.add(readerCode)
                writer.add(")!!")
            }

            //// 不是必须的，判断 source 不为 null 再设置
            else -> {
                // (readCode).also { it -> %L = it }
                writer.add("(")
                writer.add(readerCode)
                writer.add(").also { ")
                writer.add(targetPropertyRef)
                writer.add(" = it }")

            }
        }



        TODO("Not yet implemented")
    }
}
