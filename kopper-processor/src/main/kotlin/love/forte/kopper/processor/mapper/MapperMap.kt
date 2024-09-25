// /*
//  * Copyright (c) 2024. Kopper.
//  *
//  * Licensed under the Apache License, Version 2.0 (the "License");
//  * you may not use this file except in compliance with the License.
//  * You may obtain a copy of the License at
//  *
//  *     https://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing, software
//  * distributed under the License is distributed on an "AS IS" BASIS,
//  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  * See the License for the specific language governing permissions and
//  * limitations under the License.
//  */
//
// package love.forte.kopper.processor.mapper
//
// import com.google.devtools.ksp.symbol.KSValueParameter
// import com.google.devtools.ksp.symbol.Nullability
// import com.squareup.kotlinpoet.CodeBlock
//
// /**
//  * A single map in [MapperAction].
//  */
// internal sealed interface MapperMap : MapperActionStatement {
//     /**
//      * Emit current Map to [writer].
//      */
//     override fun emit(writer: MapperActionWriter, index: Int)
// }
//
// internal interface ConstructorMapperMap : MapperMap {
//     val target: MapperActionTarget
//     val targetParameter: KSValueParameter
// }
//
// /**
//  * Required properties' mapper map.
//  */
// internal data class SourceConstructorMapperMap(
//     val source: MapperActionSource,
//     val sourceProperty: MapActionSourceProperty,
//     override val target: MapperActionTarget,
//     override val targetParameter: KSValueParameter,
//     val nullableParameter: String?,
// ) : ConstructorMapperMap {
//     override fun emit(writer: MapperActionWriter, index: Int) {
//         // parameter = source,
//         val sourceRead = sourceProperty.read()
//         val code = CodeBlock.builder()
//             .apply {
//                 add("«")
//                 add("%L = ", targetParameter.name!!.asString())
//                 val parameterType = targetParameter.type.resolve()
//
//                 add(sourceRead.codeWithCast(writer.mapperWriter, parameterType))
//                 if (
//                     parameterType.nullability == Nullability.NOT_NULL
//                     && sourceRead.nullable
//                 ) {
//                     add("!!")
//                 }
//
//                 add(",\n»")
//             }
//             .build()
//
//         writer.add(code)
//     }
// }
//
// /**
//  * Required properties' mapper map.
//  */
// internal data class EvalConstructorMapperMap(
//     val eval: String,
//     val evalNullable: Boolean,
//     override val target: MapperActionTarget,
//     override val targetParameter: KSValueParameter,
//     val nullableParameter: String?,
// ) : ConstructorMapperMap {
//     override fun emit(writer: MapperActionWriter, index: Int) {
//         val eval = CodeBlock.builder()
//             .apply {
//                 add("«")
//                 add("%L = ", targetParameter.name!!.asString())
//                 add("(")
//                 add(eval)
//                 add(")")
//                 if (
//                     targetParameter.type.resolve().nullability == Nullability.NOT_NULL
//                     && evalNullable
//                 ) {
//                     add("!!")
//                 }
//                 add(",\n»")
//             }
//             .build()
//
//         writer.add(eval)
//     }
// }
//
//
// internal interface PropertyMapperMap : MapperMap {
//     val target: MapperActionTarget
//     val targetProperty: MapActionTargetProperty
// }
//
// /**
//  * Normal properties' mapper map.
//  */
// internal data class SourcePropertyMapperMap(
//     /**
//      * The source. If the source is an eval expression,
//      * the source will be the main source.
//      */
//     val source: MapperActionSource,
//     val sourceProperty: MapActionSourceProperty,
//     override val target: MapperActionTarget,
//     override val targetProperty: MapActionTargetProperty,
// ) : PropertyMapperMap {
//     override fun emit(writer: MapperActionWriter, index: Int) {
//         targetProperty.emit(writer, sourceProperty.read())
//     }
// }
//
// internal data class EvalPropertyMapperMap(
//     val eval: String,
//     val evalNullable: Boolean,
//     override val target: MapperActionTarget,
//     override val targetProperty: MapActionTargetProperty,
// ) : PropertyMapperMap {
//     override fun emit(writer: MapperActionWriter, index: Int) {
//         val read = PropertyRead(name = "eval", CodeBlock.of(eval), nullable = evalNullable)
//         // TODO targetProperty.emit(writer, read)
//     }
// }
//
// // TODO submodel (sub mapSet) mapper map?
