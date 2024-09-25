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
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.CodeBlock

private fun Resolver.ul(): KSClassDeclaration = getClassDeclarationByName<ULong>()!!
private fun Resolver.ui(): KSClassDeclaration = getClassDeclarationByName<UInt>()!!
private fun Resolver.ub(): KSClassDeclaration = getClassDeclarationByName<UByte>()!!
private fun Resolver.us(): KSClassDeclaration = getClassDeclarationByName<UShort>()!!

private inline val KSType.d: KSDeclaration get() = declaration


    /**
 * 对一些非结构的、简单的类型进行类型转化。
 */
internal fun Resolver.tryTypeCast(
        code: CodeBlock,
        nullable: Boolean,
        sourceDeclaration: KSClassDeclaration,
        targetDeclaration: KSClassDeclaration,
): CodeBlock {
    val ulDeclaration by lazy(LazyThreadSafetyMode.NONE) { ul() }
    val uiDeclaration by lazy(LazyThreadSafetyMode.NONE) { ui() }
    val ubDeclaration by lazy(LazyThreadSafetyMode.NONE) { ub() }
    val usDeclaration by lazy(LazyThreadSafetyMode.NONE) { us() }


    val con = if (nullable) "?." else "."
    fun codeBuilder(): CodeBlock.Builder = code.toBuilder().add(con)
    return when (sourceDeclaration) {
        // number
        builtIns.numberType.d -> when (targetDeclaration) {
            ubDeclaration -> codeBuilder().add("toUByte()").build()
            usDeclaration -> codeBuilder().add("toUShort()").build()
            uiDeclaration -> codeBuilder().add("toUInt()").build()
            ulDeclaration -> codeBuilder().add("toULong()").build()
            builtIns.byteType.d -> codeBuilder().add("toByte()").build()
            builtIns.shortType.d -> codeBuilder().add("toShort()").build()
            builtIns.intType.d -> codeBuilder().add("toInt()").build()
            builtIns.charType.d -> codeBuilder().add("toInt()").add(con).add("toChar()").build()
            builtIns.longType.d -> codeBuilder().add("toLong()").build()
            builtIns.doubleType.d -> codeBuilder().add("toDouble()").build()
            builtIns.floatType.d -> codeBuilder().add("toFloat()").build()
            builtIns.stringType.d -> codeBuilder().add("toString()").build()
            else -> code
        }

        builtIns.byteType.d -> when (targetDeclaration) {
            ubDeclaration -> codeBuilder().add("toUByte()").build()
            usDeclaration -> codeBuilder().add("toUShort()").build()
            uiDeclaration -> codeBuilder().add("toUInt()").build()
            ulDeclaration -> codeBuilder().add("toULong()").build()
            builtIns.shortType.d -> codeBuilder().add("toShort()").build()
            builtIns.intType.d -> codeBuilder().add("toInt()").build()
            builtIns.charType.d -> codeBuilder().add("toInt()").add(con).add("toChar()").build()
            builtIns.longType.d -> codeBuilder().add("toLong()").build()
            builtIns.doubleType.d -> codeBuilder().add("toDouble()").build()
            builtIns.floatType.d -> codeBuilder().add("toFloat()").build()
            builtIns.stringType.d -> codeBuilder().add("toString()").build()
            else -> code
        }

        builtIns.shortType.d -> when (targetDeclaration) {
            ubDeclaration -> codeBuilder().add("toUByte()").build()
            usDeclaration -> codeBuilder().add("toUShort()").build()
            uiDeclaration -> codeBuilder().add("toUInt()").build()
            ulDeclaration -> codeBuilder().add("toULong()").build()
            builtIns.byteType.d -> codeBuilder().add("toByte()").build()
            builtIns.intType.d -> codeBuilder().add("toInt()").build()
            builtIns.charType.d -> codeBuilder().add("toInt()").add(con).add("toChar()").build()
            builtIns.longType.d -> codeBuilder().add("toLong()").build()
            builtIns.doubleType.d -> codeBuilder().add("toDouble()").build()
            builtIns.floatType.d -> codeBuilder().add("toFloat()").build()
            builtIns.stringType.d -> codeBuilder().add("toString()").build()
            else -> code
        }

        builtIns.intType.d -> when (targetDeclaration) {
            ubDeclaration -> codeBuilder().add("toUByte()").build()
            usDeclaration -> codeBuilder().add("toUShort()").build()
            uiDeclaration -> codeBuilder().add("toUInt()").build()
            ulDeclaration -> codeBuilder().add("toULong()").build()
            builtIns.byteType.d -> codeBuilder().add("toByte()").build()
            builtIns.shortType.d -> codeBuilder().add("toShort()").build()
            builtIns.charType.d -> codeBuilder().add("toChar()").build()
            builtIns.longType.d -> codeBuilder().add("toLong()").build()
            builtIns.doubleType.d -> codeBuilder().add("toDouble()").build()
            builtIns.floatType.d -> codeBuilder().add("toFloat()").build()
            builtIns.stringType.d -> codeBuilder().add("toString()").build()
            else -> code
        }

        builtIns.longType.d -> when (targetDeclaration) {
            ubDeclaration -> codeBuilder().add("toUByte()").build()
            usDeclaration -> codeBuilder().add("toUShort()").build()
            uiDeclaration -> codeBuilder().add("toUInt()").build()
            ulDeclaration -> codeBuilder().add("toULong()").build()
            builtIns.byteType.d -> codeBuilder().add("toByte()").build()
            builtIns.shortType.d -> codeBuilder().add("toShort()").build()
            builtIns.charType.d -> codeBuilder().add("toInt()").add(con).add("toChar()").build()
            builtIns.intType.d -> codeBuilder().add("toInt()").build()
            builtIns.doubleType.d -> codeBuilder().add("toDouble()").build()
            builtIns.floatType.d -> codeBuilder().add("toFloat()").build()
            builtIns.stringType.d -> codeBuilder().add("toString()").build()

            else -> code
        }

        // u number
        ubDeclaration -> when (targetDeclaration) {
            usDeclaration -> codeBuilder().add("toUShort()").build()
            uiDeclaration -> codeBuilder().add("toUInt()").build()
            ulDeclaration -> codeBuilder().add("toULong()").build()
            builtIns.byteType.d -> codeBuilder().add("toShort()").build()
            builtIns.shortType.d -> codeBuilder().add("toShort()").build()
            builtIns.intType.d -> codeBuilder().add("toInt()").build()
            builtIns.charType.d -> codeBuilder().add("toInt()").add(con).add("toChar()").build()
            builtIns.longType.d -> codeBuilder().add("toLong()").build()
            builtIns.doubleType.d -> codeBuilder().add("toDouble()").build()
            builtIns.floatType.d -> codeBuilder().add("toFloat()").build()
            builtIns.stringType.d -> codeBuilder().add("toString()").build()
            else -> code
        }

        usDeclaration -> when (targetDeclaration) {
            ubDeclaration -> codeBuilder().add("toUByte()").build()
            uiDeclaration -> codeBuilder().add("toUInt()").build()
            ulDeclaration -> codeBuilder().add("toULong()").build()
            builtIns.byteType.d -> codeBuilder().add("toShort()").build()
            builtIns.shortType.d -> codeBuilder().add("toShort()").build()
            builtIns.intType.d -> codeBuilder().add("toInt()").build()
            builtIns.charType.d -> codeBuilder().add("toInt()").add(con).add("toChar()").build()
            builtIns.longType.d -> codeBuilder().add("toLong()").build()
            builtIns.doubleType.d -> codeBuilder().add("toDouble()").build()
            builtIns.floatType.d -> codeBuilder().add("toFloat()").build()
            builtIns.stringType.d -> codeBuilder().add("toString()").build()
            else -> code
        }

        uiDeclaration -> when (targetDeclaration) {
            usDeclaration -> codeBuilder().add("toUShort()").build()
            ubDeclaration -> codeBuilder().add("toUByte()").build()
            ulDeclaration -> codeBuilder().add("toULong()").build()
            builtIns.byteType.d -> codeBuilder().add("toShort()").build()
            builtIns.shortType.d -> codeBuilder().add("toShort()").build()
            builtIns.intType.d -> codeBuilder().add("toInt()").build()
            builtIns.charType.d -> codeBuilder().add("toInt()").add(con).add("toChar()").build()
            builtIns.longType.d -> codeBuilder().add("toLong()").build()
            builtIns.doubleType.d -> codeBuilder().add("toDouble()").build()
            builtIns.floatType.d -> codeBuilder().add("toFloat()").build()
            builtIns.stringType.d -> codeBuilder().add("toString()").build()
            else -> code
        }

        ulDeclaration -> when (targetDeclaration) {
            usDeclaration -> codeBuilder().add("toUShort()").build()
            ubDeclaration -> codeBuilder().add("toUByte()").build()
            uiDeclaration -> codeBuilder().add("toUInt()").build()
            builtIns.byteType.d -> codeBuilder().add("toShort()").build()
            builtIns.shortType.d -> codeBuilder().add("toShort()").build()
            builtIns.intType.d -> codeBuilder().add("toInt()").build()
            builtIns.charType.d -> codeBuilder().add("toInt()").add(con).add("toChar()").build()
            builtIns.longType.d -> codeBuilder().add("toLong()").build()
            builtIns.doubleType.d -> codeBuilder().add("toDouble()").build()
            builtIns.floatType.d -> codeBuilder().add("toFloat()").build()
            builtIns.stringType.d -> codeBuilder().add("toString()").build()
            else -> code
        }

        // string
        builtIns.stringType.d -> when (targetDeclaration) {
            usDeclaration -> codeBuilder().add("toUShort()").build()
            ubDeclaration -> codeBuilder().add("toUByte()").build()
            uiDeclaration -> codeBuilder().add("toUInt()").build()
            ulDeclaration -> codeBuilder().add("toULong()").build()
            builtIns.byteType.d -> codeBuilder().add("toShort()").build()
            builtIns.shortType.d -> codeBuilder().add("toShort()").build()
            builtIns.intType.d -> codeBuilder().add("toInt()").build()
            builtIns.charType.d -> codeBuilder().add("toInt()").add(con).add("toChar()").build()
            builtIns.longType.d, builtIns.numberType -> codeBuilder().add("toLong()").build()
            builtIns.doubleType.d -> codeBuilder().add("toDouble()").build()
            builtIns.floatType.d -> codeBuilder().add("toFloat()").build()
            else -> code
        }

        else -> code
    }
}
