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

import com.google.devtools.ksp.symbol.*
import love.forte.kopper.annotation.PropertyType

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


private inline fun <T> findPropProperty(name: String, type: KSType, block: (KSPropertyDeclaration) -> T?): T? {
    return (type.declaration as? KSClassDeclaration)
        ?.getAllProperties()
        // 返回值是 type
        ?.firstOrNull { it.simpleName.asString() == name && it.type.resolve() == type }
        ?.let(block)
}

private inline fun <T> findFunProperty(name: String, type: KSType, block: (KSFunctionDeclaration) -> T?): T? {
    return (type.declaration as? KSClassDeclaration)
        ?.getAllFunctions()
        // 没有参数，有返回值，返回值是 type
        ?.firstOrNull {
            if (it.simpleName.asString() != name) return@firstOrNull false
            if (it.parameters.isEmpty()) return@firstOrNull false
            val returnType = it.returnType ?: return@firstOrNull false
            returnType.resolve() == type
        }
        ?.let(block)
}

internal inline fun <reified T> KSAnnotation.findArg(name: String): T? {
    return arguments.find { it.name?.asString() == name }
        as? T
}
