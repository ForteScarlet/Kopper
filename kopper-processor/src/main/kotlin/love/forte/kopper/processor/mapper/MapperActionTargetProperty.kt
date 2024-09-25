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
import love.forte.kopper.processor.def.TargetPropertyDef

/**
 * A property for mapping target.
 */
internal interface MapActionTargetProperty {
    val target: MapperActionTarget
    val def: TargetPropertyDef

    /**
     * propertyRefName,
     * e.g.
     * `targetName.propertyName`,
     * `targetName?.propertyName`,
     * and if required:
     * `propertyName`
     */
    val propertyRefName: CodeBlock

    // /**
    //  * emit a property setter with [read] into [writer]
    //  */
    // fun emit(writer: MapperActionWriter, read: PropertyRead)
}


internal data class MapActionTargetPropertyImpl(
    override val target: MapperActionTarget,
    override val def: TargetPropertyDef,
) : MapActionTargetProperty {

    override val propertyRefName: CodeBlock
        get() {
            // if it's required, just return property name
            if (def.isRequired) {
                return CodeBlock.of("%L", def.name)
            }

            return when {
                target.def.nullable ->
                    CodeBlock.of("%L?.%L", target.name, def.name)
                else ->
                    CodeBlock.of("%L.%L", target.name, def.name)
            }
        }

    // override fun emit(writer: MapperActionWriter, read: PropertyRead) {
    //     val propCon = if (target.nullable) "?." else "."
    //     val sourceCode = read.codeWithCast(writer.mapperWriter, type)
    //     val sourceNullable = read.nullable
    //
    //     val sourceCon = if (sourceNullable) "?." else "."
    //
    //     val safeSet = nullable || (!nullable && !sourceNullable)
    //
    //     val code = when (propertyType) {
    //         PropertyType.FUNCTION -> {
    //             if (safeSet) {
    //                 CodeBlock.builder().apply {
    //                     // %L?.%L(code)
    //                     add("%L", target.name)
    //                     add(propCon)
    //                     add("%L(", name)
    //                     add(sourceCode)
    //                     add(")\n")
    //                 }.build()
    //             } else {
    //                 CodeBlock.builder()
    //                     .apply {
    //                         add("(")
    //                         add(sourceCode)
    //                         add(")")
    //                         beginControlFlow("${sourceCon}also")
    //                         addStatement("%L${propCon}%L(it)", target.name, name)
    //                         endControlFlow()
    //                     }.build()
    //             }
    //         }
    //
    //         else -> {
    //             if (safeSet) {
    //                 CodeBlock.builder().apply {
    //                     // %L?.%L = code
    //                     add("«")
    //                     add("%L", target.name)
    //                     add(propCon)
    //                     add("%L = ", name)
    //                     add(sourceCode)
    //                     add("\n»")
    //                 }.build()
    //             } else {
    //                 CodeBlock.builder()
    //                     .apply {
    //                         add("(")
    //                         add(sourceCode)
    //                         add(")")
    //                         beginControlFlow("${sourceCon}also")
    //                         addStatement("%L${propCon}%L = it", target.name, name)
    //                         endControlFlow()
    //                     }.build()
    //             }
    //         }
    //     }
    //
    //     writer.add(code)
    // }
}
