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

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import java.util.LinkedList

// Mapper -> A Type annotated with @Mapper, e.g., an interface or an abstract class
// MapperMapSet -> A function in Mapper.
// MapperMap -> A @Map or a pair of mapping properties

internal data class MapperMapSetInfo(
    val funSpec: FunSpec.Builder,
    val isAncillary: Boolean,
)

/**
 * A key marks a MapSet function.
 */
internal data class MapperMapSetKey(
    val name: String,
    val target: MapperActionTarget,
    val sources: Set<MapperActionSource>,
)

internal class MapperWriter(
    val environment: SymbolProcessorEnvironment,
    val resolver: Resolver,
    val collect: MutableMap<MapperMapSetKey, MapperMapSetInfo> = linkedMapOf(),
    val mapSetWriters: ArrayDeque<MapperActionWriter> = ArrayDeque(),
) {
    private var indexEmitter: Int = 0

    /**
     * Emit a next index value.
     * Not thread-safe.
     */
    fun nextIndex(): Int = indexEmitter++

    fun add(key: MapperMapSetKey, info: MapperMapSetInfo) {
        collect.compute(key) { k, current ->
            require(current == null) { "Conflicted keys: $k" }
            info
        }
    }

    fun newMapperActionWriter(
        root: FunSpec.Builder,
        stacks: LinkedList<FunSpec.Builder> = LinkedList()
    ): MapperActionWriter {
        val writer = MapperActionWriter(
            environment = environment,
            resolver = resolver,
            mapperWriter = this,
            root = root,
            funList = stacks
        )
        mapSetWriters.add(writer)
        return writer
    }

    /**
     * 对一些非结构的、简单的类型进行类型转化。
     */
    fun tryTypeCast(
        code: CodeBlock,
        nullable: Boolean,
        sourceType: KSType,
        targetType: KSType,
    ): CodeBlock {
        val sourceType0 = sourceType.makeNotNullable()
        val targetType0 = targetType.makeNotNullable()

        val builtIns = resolver.builtIns
        val con = if (nullable) "?." else "."
        fun codeBuilder(): CodeBlock.Builder = code.toBuilder().add(con)
        return when (sourceType0) {
            // number
            builtIns.numberType -> when (targetType0) {
                ubType -> codeBuilder().add("toUByte()").build()
                usType -> codeBuilder().add("toUShort()").build()
                uiType -> codeBuilder().add("toUInt()").build()
                ulType -> codeBuilder().add("toULong()").build()
                builtIns.byteType -> codeBuilder().add("toByte()").build()
                builtIns.shortType -> codeBuilder().add("toShort()").build()
                builtIns.intType -> codeBuilder().add("toInt()").build()
                builtIns.charType -> codeBuilder().add("toInt()").add(con).add("toChar()").build()
                builtIns.longType -> codeBuilder().add("toLong()").build()
                builtIns.doubleType -> codeBuilder().add("toDouble()").build()
                builtIns.floatType -> codeBuilder().add("toFloat()").build()
                builtIns.stringType -> codeBuilder().add("toString()").build()
                else -> code
            }
            builtIns.byteType -> when (targetType0) {
                ubType -> codeBuilder().add("toUByte()").build()
                usType -> codeBuilder().add("toUShort()").build()
                uiType -> codeBuilder().add("toUInt()").build()
                ulType -> codeBuilder().add("toULong()").build()
                builtIns.shortType -> codeBuilder().add("toShort()").build()
                builtIns.intType -> codeBuilder().add("toInt()").build()
                builtIns.charType -> codeBuilder().add("toInt()").add(con).add("toChar()").build()
                builtIns.longType -> codeBuilder().add("toLong()").build()
                builtIns.doubleType -> codeBuilder().add("toDouble()").build()
                builtIns.floatType -> codeBuilder().add("toFloat()").build()
                builtIns.stringType -> codeBuilder().add("toString()").build()
                else -> code
            }
            builtIns.shortType -> when (targetType0) {
                ubType -> codeBuilder().add("toUByte()").build()
                usType -> codeBuilder().add("toUShort()").build()
                uiType -> codeBuilder().add("toUInt()").build()
                ulType -> codeBuilder().add("toULong()").build()
                builtIns.byteType -> codeBuilder().add("toByte()").build()
                builtIns.intType -> codeBuilder().add("toInt()").build()
                builtIns.charType -> codeBuilder().add("toInt()").add(con).add("toChar()").build()
                builtIns.longType -> codeBuilder().add("toLong()").build()
                builtIns.doubleType -> codeBuilder().add("toDouble()").build()
                builtIns.floatType -> codeBuilder().add("toFloat()").build()
                builtIns.stringType -> codeBuilder().add("toString()").build()
                else -> code
            }
            builtIns.intType -> when (targetType0) {
                ubType -> codeBuilder().add("toUByte()").build()
                usType -> codeBuilder().add("toUShort()").build()
                uiType -> codeBuilder().add("toUInt()").build()
                ulType -> codeBuilder().add("toULong()").build()
                builtIns.byteType -> codeBuilder().add("toByte()").build()
                builtIns.shortType -> codeBuilder().add("toShort()").build()
                builtIns.charType -> codeBuilder().add("toChar()").build()
                builtIns.longType -> codeBuilder().add("toLong()").build()
                builtIns.doubleType -> codeBuilder().add("toDouble()").build()
                builtIns.floatType -> codeBuilder().add("toFloat()").build()
                builtIns.stringType -> codeBuilder().add("toString()").build()
                else -> code
            }
            builtIns.longType -> when (targetType0) {
                ubType -> codeBuilder().add("toUByte()").build()
                usType -> codeBuilder().add("toUShort()").build()
                uiType -> codeBuilder().add("toUInt()").build()
                ulType -> codeBuilder().add("toULong()").build()
                builtIns.byteType -> codeBuilder().add("toByte()").build()
                builtIns.shortType -> codeBuilder().add("toShort()").build()
                builtIns.charType -> codeBuilder().add("toInt()").add(con).add("toChar()").build()
                builtIns.intType -> codeBuilder().add("toInt()").build()
                builtIns.doubleType -> codeBuilder().add("toDouble()").build()
                builtIns.floatType -> codeBuilder().add("toFloat()").build()
                builtIns.stringType -> codeBuilder().add("toString()").build()

                else -> code
            }

            // u number
            ubType -> when (targetType0) {
                usType -> codeBuilder().add("toUShort()").build()
                uiType -> codeBuilder().add("toUInt()").build()
                ulType -> codeBuilder().add("toULong()").build()
                builtIns.byteType -> codeBuilder().add("toShort()").build()
                builtIns.shortType -> codeBuilder().add("toShort()").build()
                builtIns.intType -> codeBuilder().add("toInt()").build()
                builtIns.charType -> codeBuilder().add("toInt()").add(con).add("toChar()").build()
                builtIns.longType -> codeBuilder().add("toLong()").build()
                builtIns.doubleType -> codeBuilder().add("toDouble()").build()
                builtIns.floatType -> codeBuilder().add("toFloat()").build()
                builtIns.stringType -> codeBuilder().add("toString()").build()
                else -> code
            }
            usType -> when (targetType0) {
                ubType -> codeBuilder().add("toUByte()").build()
                uiType -> codeBuilder().add("toUInt()").build()
                ulType -> codeBuilder().add("toULong()").build()
                builtIns.byteType -> codeBuilder().add("toShort()").build()
                builtIns.shortType -> codeBuilder().add("toShort()").build()
                builtIns.intType -> codeBuilder().add("toInt()").build()
                builtIns.charType -> codeBuilder().add("toInt()").add(con).add("toChar()").build()
                builtIns.longType -> codeBuilder().add("toLong()").build()
                builtIns.doubleType -> codeBuilder().add("toDouble()").build()
                builtIns.floatType -> codeBuilder().add("toFloat()").build()
                builtIns.stringType -> codeBuilder().add("toString()").build()
                else -> code
            }
            uiType -> when (targetType0) {
                usType -> codeBuilder().add("toUShort()").build()
                ubType -> codeBuilder().add("toUByte()").build()
                ulType -> codeBuilder().add("toULong()").build()
                builtIns.byteType -> codeBuilder().add("toShort()").build()
                builtIns.shortType -> codeBuilder().add("toShort()").build()
                builtIns.intType -> codeBuilder().add("toInt()").build()
                builtIns.charType -> codeBuilder().add("toInt()").add(con).add("toChar()").build()
                builtIns.longType -> codeBuilder().add("toLong()").build()
                builtIns.doubleType -> codeBuilder().add("toDouble()").build()
                builtIns.floatType -> codeBuilder().add("toFloat()").build()
                builtIns.stringType -> codeBuilder().add("toString()").build()
                else -> code
            }
            ulType -> when (targetType0) {
                usType -> codeBuilder().add("toUShort()").build()
                ubType -> codeBuilder().add("toUByte()").build()
                uiType -> codeBuilder().add("toUInt()").build()
                builtIns.byteType -> codeBuilder().add("toShort()").build()
                builtIns.shortType -> codeBuilder().add("toShort()").build()
                builtIns.intType -> codeBuilder().add("toInt()").build()
                builtIns.charType -> codeBuilder().add("toInt()").add(con).add("toChar()").build()
                builtIns.longType -> codeBuilder().add("toLong()").build()
                builtIns.doubleType -> codeBuilder().add("toDouble()").build()
                builtIns.floatType -> codeBuilder().add("toFloat()").build()
                builtIns.stringType -> codeBuilder().add("toString()").build()
                else -> code
            }

            // string
            builtIns.stringType -> when (targetType0) {
                usType -> codeBuilder().add("toUShort()").build()
                ubType -> codeBuilder().add("toUByte()").build()
                uiType -> codeBuilder().add("toUInt()").build()
                ulType -> codeBuilder().add("toULong()").build()
                builtIns.byteType -> codeBuilder().add("toShort()").build()
                builtIns.shortType -> codeBuilder().add("toShort()").build()
                builtIns.intType -> codeBuilder().add("toInt()").build()
                builtIns.charType -> codeBuilder().add("toInt()").add(con).add("toChar()").build()
                builtIns.longType, builtIns.numberType -> codeBuilder().add("toLong()").build()
                builtIns.doubleType -> codeBuilder().add("toDouble()").build()
                builtIns.floatType -> codeBuilder().add("toFloat()").build()
                else -> code
            }

            else -> code
        }
    }


    private val ulType by lazy(LazyThreadSafetyMode.NONE) { resolver.ul() }
    private val uiType by lazy(LazyThreadSafetyMode.NONE) { resolver.ui() }
    private val ubType by lazy(LazyThreadSafetyMode.NONE) { resolver.ub() }
    private val usType by lazy(LazyThreadSafetyMode.NONE) { resolver.us() }

    companion object {
        private fun Resolver.ul(): KSType = getClassDeclarationByName<ULong>()!!.asStarProjectedType()
        private fun Resolver.ui(): KSType = getClassDeclarationByName<UInt>()!!.asStarProjectedType()
        private fun Resolver.ub(): KSType = getClassDeclarationByName<UByte>()!!.asStarProjectedType()
        private fun Resolver.us(): KSType = getClassDeclarationByName<UShort>()!!.asStarProjectedType()
    }
}

internal class MapperActionWriter(
    val environment: SymbolProcessorEnvironment,
    val resolver: Resolver,
    val mapperWriter: MapperWriter,
    val root: FunSpec.Builder,
    val funList: LinkedList<FunSpec.Builder> = LinkedList()
) {
    private var indexEmitter: Int = 0

    /**
     * Emit a next index value.
     * Not thread-safe.
     */
    fun nextIndex(): Int = indexEmitter++

    fun add(code: CodeBlock) {
        if (funList.isEmpty()) {
            root.addCode(code)
        } else {
            funList.last().addCode(code)
        }
    }

    fun add(format: String, vararg args: Any?) {
        if (funList.isEmpty()) {
            root.addCode(format, *args)
        } else {
            funList.last().addCode(format, *args)
        }
    }

    fun addStatement(format: String, vararg args: Any) {
        if (funList.isEmpty()) {
            root.addStatement(format, *args)
        } else {
            funList.last().addCode(format, *args)
        }
    }

    fun push(funSpec: FunSpec.Builder) {
        funList.addLast(funSpec)
    }

    fun pop(): FunSpec.Builder {
        if (funList.isEmpty()) {
            return root
        }
        return funList.removeLast()
    }
}
