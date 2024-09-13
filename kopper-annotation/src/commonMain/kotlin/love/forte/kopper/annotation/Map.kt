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

package love.forte.kopper.annotation

/**
 * A property mapper marker
 *
 * ```kotlin
 * interface MyMapper {
 *   @Map(source = "", target = "", ...)
 *   fun SourceModel.mapTo(): TargetModel
 *
 * }
 * ```
 *
 * @param source The source property in source model (with type [sourceType])
 * @param target The target property in target model (with type [sourceType])
 *
 */
@Repeatable
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
public annotation class Map(
    val target: String,
    val source: String = SAME_AS_TARGET,
    val targetType: PropertyType = PropertyType.PROPERTY,
    val sourceType: PropertyType = PropertyType.PROPERTY,
    val ignore: Boolean = false,

    val eval: String = ""
)

/**
 * Same as [Map.target] if [Map.source]'s value is an empty string.
 *
 * @see Map
 */
internal const val SAME_AS_TARGET: String = ""
