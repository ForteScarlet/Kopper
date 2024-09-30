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

package love.forte.kopper.processor.util

import com.google.devtools.ksp.processing.KSBuiltIns
import com.google.devtools.ksp.symbol.*
import love.forte.kopper.processor.def.NodeArg


internal inline fun <reified T> KSAnnotation.findArg(name: String): NodeArg<T>? {
    val argument = (
        arguments.find { it.name?.asString() == name }
            ?: defaultArguments.find { it.name?.asString() == name }
        ) ?: return null

    val value = argument.value as? T
        ?: return null

    return NodeArg(value, argument)
}

// internal inline fun <reified T> KSAnnotation.findListArg(name: String): NodeArg<List<T>>? {
//     return findArg<List<T>>(name)
// }

internal inline fun <reified T : Enum<T>> KSAnnotation.findEnumArg(name: String): NodeArg<T>? {
    val argument = (
        arguments.find { it.name?.asString() == name }
            ?: defaultArguments.find { it.name?.asString() == name }
        ) ?: return null
    val value = argument.value

    if (value is T) return NodeArg(value, argument)

    // https://github.com/google/ksp/issues/429
    val enumValue = ((value as? KSType)?.declaration as? KSClassDeclaration)?.simpleName?.asString()
        ?: return null

    return NodeArg(enumValueOf<T>(enumValue), argument)
}

internal fun KSAnnotated.hasAnno(targetAnoType: KSType): Boolean =
    annotations.any { ano ->
        ano.annotationType.resolve().let { at ->
            targetAnoType.isAssignableFrom(at)
        }
    }

internal fun KSType.hasAnno(targetAnoType: KSType): Boolean =
    declaration.hasAnno(targetAnoType)

/**
 * If is [KSClassDeclaration], return it,
 * if is [KSTypeAlias], try again.
 */
internal tailrec fun KSDeclaration.asClassDeclaration(): KSClassDeclaration? {
    if (this is KSClassDeclaration) return this
    // TODO KSTypeParameter?
    if (this !is KSTypeAlias) return null

    return type.resolve().declaration.asClassDeclaration()
}


private const val KT_NUMBER_PACKAGE = "kotlin"
private const val U_BYTE_NAME = "UByte"
private const val U_SHORT_NAME = "UShort"
private const val U_INT_NAME = "UInt"
private const val U_LONG_NAME = "ULong"

private fun KSDeclaration.isUByte(): Boolean =
    packageName.asString() == KT_NUMBER_PACKAGE && simpleName.asString() == U_BYTE_NAME

private fun KSDeclaration.isUShort(): Boolean =
    packageName.asString() == KT_NUMBER_PACKAGE && simpleName.asString() == U_SHORT_NAME

private fun KSDeclaration.isUInt(): Boolean =
    packageName.asString() == KT_NUMBER_PACKAGE && simpleName.asString() == U_INT_NAME

private fun KSDeclaration.isULong(): Boolean =
    packageName.asString() == KT_NUMBER_PACKAGE && simpleName.asString() == U_LONG_NAME

internal fun KSDeclaration.isMappableStructType(builtIns: KSBuiltIns): Boolean {
    val thisClass = this.asClassDeclaration()
    return when {
        this == builtIns.annotationType.declaration -> false
        this == builtIns.unitType.declaration -> false
        this == builtIns.anyType.declaration -> false
        this == builtIns.arrayType.declaration -> false
        this == builtIns.numberType.declaration -> false
        this == builtIns.byteType.declaration -> false
        this == builtIns.shortType.declaration -> false
        this == builtIns.intType.declaration -> false
        this == builtIns.longType.declaration -> false
        this == builtIns.floatType.declaration -> false
        this == builtIns.doubleType.declaration -> false
        this == builtIns.charType.declaration -> false
        this == builtIns.booleanType.declaration -> false
        this == builtIns.stringType.declaration -> false
        this == builtIns.iterableType.declaration -> false
        // is number, for UXxx
        isUByte() || isUShort() || isUInt() || isULong() -> false
        // 更多检测?
        this is KSClassDeclaration -> when {
            // is data class -> true
            this.modifiers.contains(Modifier.DATA) -> true
            // no class, no interface -> false
            this.classKind != ClassKind.CLASS
                && this.classKind != ClassKind.INTERFACE // cannot be initialized, but can be mapped.
                -> false

            // is iterable, not struct.
            builtIns.iterableType.isAssignableFrom(this.asStarProjectedType()) -> false

            else -> false
        }

        this is KSTypeAlias -> asClassDeclaration()?.isMappableStructType(builtIns) ?: false
        else -> false
    }
}

internal val Nullability.isNullable: Boolean
    get() = this != Nullability.NOT_NULL
