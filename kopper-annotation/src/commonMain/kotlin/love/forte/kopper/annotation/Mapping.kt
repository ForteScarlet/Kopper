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

import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

/**
 * A property mapper marker in a Mapper type.
 *
 * ```kotlin
 * interface MyMapper {
 *   @Mapping(source = "", target = "", ...)
 *   fun SourceModel.mapTo(): TargetModel
 *
 * }
 * ```
 *
 * @param source The source property in a source model.
 * @param target The target property in a target model.
 *
 */
@Repeatable
@Target(FUNCTION)
public annotation class Mapping(
    val target: String,
    val source: String = SAME_AS_TARGET,
    val sourceName: String = MAIN_SOURCE,
    val ignore: Boolean = false,
    val eval: String = "",
    /**
     * The `nullable` for [eval]'s result.
     * The default value is pessimistically `null` .
     */
    val evalNullable: Boolean = true,
) {

    /**
     * Mark as map target.
     */
    @kotlin.annotation.Target(VALUE_PARAMETER)
    public annotation class Target

    @kotlin.annotation.Target(VALUE_PARAMETER)
    public annotation class MainSource

    public companion object {
        /**
         * Same as [Mapping.target] if [Mapping.source]'s value is an empty string.
         *
         * @see Mapping
         */
        public const val SAME_AS_TARGET: String = ""

        public const val MAIN_SOURCE: String = ""
    }
}

