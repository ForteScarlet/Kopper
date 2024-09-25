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

import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.CodeBlock
import love.forte.kopper.processor.def.ReadablePropertyDef
import love.forte.kopper.processor.def.fullyNullable
import love.forte.kopper.processor.def.readerCode

/**
 * A property for mapping source.
 */
internal interface MapActionSourceProperty {
    val source: MapperActionSource
    val def: ReadablePropertyDef

    val valueNullable: Boolean
        get() = source.def.incoming.nullable || def.fullyNullable

    /**
     * Read this property's value.
     */
    fun propertyAccessor(): CodeBlock
}

internal interface MapActionSourceTypedProperty : MapActionSourceProperty {
    val type: KSType
    // val nullable: Boolean
    //     get() = type.nullability != Nullability.NOT_NULL
}


internal data class PropertyRead(
    val name: String,
    val code: CodeBlock,
    val nullable: Boolean,
    val type: KSType? = null,
)

internal fun PropertyRead.codeWithCast(writer: MapperWriter, target: KSType): CodeBlock {
    return if (type != null) {
        writer.tryTypeCast(code, nullable, type, target)
    } else {
        code
    }
}

// internal data class InternalMapSetActionSourceProperty(
//     override val source: MapperActionSource,
//     override val type: KSType,
//     override val def: ReadablePropertyDef,
//     // val mapSet: MapperMapSet,
//     val subFunName: String,
//     val receiverProperty: MapActionSourceProperty?,
//     val parameters: List<String>,
// ) : MapActionSourceTypedProperty {
//     override fun propertyAccessor(): CodeBlock {
//         return CodeBlock.builder().apply {
//             val readerCode = def.readerCode(source.nullable)
//         }.build()
//     }
//
//     fun read(): PropertyRead {
//         val code = CodeBlock.builder()
//             .apply {
//                 if (receiverProperty != null) {
//                     val read = receiverProperty.propertyAccessor()
//                     val con = if (read.nullable) "?." else "."
//                     add(read.code)
//                     add(con)
//                 }
//                 // else {
//                 //     val main = mapSet.sources.find { it.isMain }
//                 //     if (main != null) {
//                 //         val con = if (main.nullable) "?." else "."
//                 //         add("%L", main.name)
//                 //         add(con)
//                 //     }
//                 // }
//                 add("%L(", name)
//                 parameters.forEachIndexed { index, pname ->
//                     add("%L", pname)
//                     if (index != parameters.lastIndex) {
//                         add(",")
//                     }
//                 }
//                 add(")")
//             }
//             .build()
//
//         return PropertyRead(
//             name = source.name,
//             code = code,
//             nullable = nullable,
//             type = type,
//         )
//     }
// }


internal data class DirectMapActionSourceProperty(
    override val source: MapperActionSource,
    override val def: ReadablePropertyDef,
    override val type: KSType,
) : MapActionSourceTypedProperty {
    override fun propertyAccessor(): CodeBlock {
        val sourceDef = source.def
        val sourceNullable = sourceDef.incoming.nullable
        return CodeBlock.builder().apply {
            add("%L", sourceDef.incoming.name ?: "this")
            if (sourceNullable) {
                add("?")
            }
            add(".")

            val readerCode = def.readerCode(sourceNullable)
            add(readerCode)
        }.build()
    }

    // fun read(): PropertyRead {
    //     val sourceNullable = source.nullable
    //     val conOp = if (sourceNullable) "?." else "."
    //     val initialCode = when (propertyType) {
    //         PropertyType.FUNCTION -> CodeBlock.of("%L${conOp}%L()", source.name, name)
    //         else -> CodeBlock.of("%L${conOp}%L", source.name, name)
    //     }
    //
    //     return PropertyRead(
    //         name = source.name,
    //         code = initialCode,
    //         nullable = nullable,
    //         type = type,
    //     )
    // }
}

//
// /**
//  * `a.b.c`
//  */
// internal class DeepPathMapActionSourceProperty(
//     override val source: MapperActionSource,
//     private val parentProperty: MapActionSourceProperty,
//     /**
//      * Last final simple name.
//      */
//     override val name: String,
//     override val propertyType: PropertyType,
//     override val type: KSType,
// ) : MapActionSourceTypedProperty {
//     override fun read(): PropertyRead {
//         val parentPropertyReadCode = parentProperty.read()
//         val conOp = if (parentPropertyReadCode.nullable) "?." else "."
//         val initialCode = when (propertyType) {
//             PropertyType.FUNCTION -> {
//                 CodeBlock.builder()
//                     .apply {
//                         add(parentPropertyReadCode.code)
//                         add(conOp)
//                         add("%L()", name)
//                     }.build()
//             }
//
//             else -> {
//                 CodeBlock.builder()
//                     .apply {
//                         add(parentPropertyReadCode.code)
//                         add(conOp)
//                         add("%L", name)
//                     }
//                     .build()
//             }
//         }
//
//         return PropertyRead(
//             name = source.name,
//             code = initialCode,
//             nullable = nullable,
//             type = type,
//         )
//     }
// }

// internal class EvalActionSourceProperty(
//     override val name: String,
//     override val source: MapperActionSource,
//     override val nullable: Boolean,
//     private val eval: String,
// ) : MapActionSourceProperty {
//     override val propertyType: PropertyType
//         get() = PropertyType.AUTO
//
//     override fun read(): PropertyRead {
//         return PropertyRead(
//             name = name,
//             code = CodeBlock.of(eval),
//             nullable = nullable,
//             type = null,
//         )
//     }
// }
