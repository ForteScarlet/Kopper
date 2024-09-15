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

package love.forte.kopper.test

import love.forte.kopper.annotation.Map
import love.forte.kopper.annotation.Mapper


/**
 *
 * @author ForteScarlet
 */
@Mapper
interface CastMapper {
    data class Source(val number: Int)
    data class Target(var number: Long)

    fun Source.map(): Target
    fun Source.map(@Map.Target target: Target): Target
    fun Source.map1(@Map.Target target: Target)
}

/**
 *
 * @author ForteScarlet
 */
@Mapper
interface NullableCastMapper {
    data class Source(val number: Int?)
    data class Target(var number: Long)

    fun Source.map(): Target
    fun Source.map(@Map.Target target: Target): Target
    fun Source.map1(@Map.Target target: Target)
}
