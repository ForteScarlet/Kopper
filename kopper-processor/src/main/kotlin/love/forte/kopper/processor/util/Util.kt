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
import love.forte.kopper.annotation.PropertyType
import java.util.logging.Logger

internal const val EVAL: String = "eval"

/**
 * Is `eval(...)` expression.
 */
internal val String.isEvalExpression: Boolean
    get() = startsWith("$EVAL(") && endsWith(")")

internal fun String.evalExpressionValue(): String {
    check(isEvalExpression) {
        "$this is not an eval expression."
    }

    return substring(EVAL.length + 1, length - 1)
}


internal inline fun <T> findProperty(
    name: String,
    type: KSType,
    propertyType: PropertyType,
    onProperty: (KSPropertyDeclaration) -> T?,
    onFunction: (KSFunctionDeclaration) -> T?,
): T? {
    return when (propertyType) {
        PropertyType.PROPERTY -> findPropProperty(name, type, onProperty)
        PropertyType.FUNCTION -> findFunProperty(name, type, onFunction)
        PropertyType.AUTO -> findPropProperty(name, type, onProperty)
            ?: findFunProperty(name, type, onFunction)
    }
}


internal inline fun <T> findPropProperty(name: String, type: KSType, block: (KSPropertyDeclaration) -> T?): T? {
    return type.declaration.asClassDeclaration()
        ?.getAllProperties()
        // 返回值是 type
        ?.firstOrNull { it.simpleName.asString() == name }
        ?.let(block)
}

internal inline fun <T> findFunProperty(name: String, type: KSType, block: (KSFunctionDeclaration) -> T?): T? {
    return type.declaration.asClassDeclaration()
        ?.getAllFunctions()
        // 没有参数，有返回值
        ?.filter { it.simpleName.asString() == name }
        ?.filter { it.parameters.isEmpty() }
        ?.filter { it.returnType != null }
        ?.firstOrNull()
        ?.let(block)
}

internal inline fun <reified T> KSAnnotation.findArg(name: String): T? {
    return (
        arguments.find { it.name?.asString() == name }?.value
            ?: defaultArguments.find { it.name?.asString() == name }?.value
        )
        as? T
}

internal inline fun <reified T> KSAnnotation.findListArg(name: String): List<T>? {
    return findArg<List<T>>(name)
}

internal inline fun <reified T : Enum<T>> KSAnnotation.findEnumArg(name: String): T? {
    val value = (
        arguments.find { it.name?.asString() == name }?.value
            ?: defaultArguments.find { it.name?.asString() == name }?.value
        ) ?: return null

    if (value is T) return value
    // https://github.com/google/ksp/issues/429
    val enumValue = ((value as? KSType)?.declaration as? KSClassDeclaration)?.simpleName?.asString()
        ?: return null

    return enumValueOf<T>(enumValue)
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
    return when {
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
        // is number, for UXxx
        isUByte() || isUShort() || isUInt() || isULong() -> false

        // 更多检测?
        this is KSClassDeclaration -> true
        this is KSTypeAlias -> asClassDeclaration()?.isMappableStructType(builtIns) ?: false
        else -> false
    }
}

internal fun KSDeclaration.isDirectMappableType(builtIns: KSBuiltIns): Boolean =
    !isMappableStructType(builtIns)
