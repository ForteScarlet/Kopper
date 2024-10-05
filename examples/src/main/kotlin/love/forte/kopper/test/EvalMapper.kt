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

import love.forte.kopper.annotation.Mapper
import love.forte.kopper.annotation.Mapping


@Mapper
interface EvalMapper {
    data class Target(var name: String, var size: Long)
    data class Source(val name: String)

    @Mapping(target = "size", eval = "1 + 1", evalNullable = false)
    fun Source.map1(): Target

    @Mapping(target = "size", eval = "1 + 1", evalNullable = false)
    fun Source.map2(@Mapping.Target target: Target): Target

    @Mapping(target = "size", eval = "1 + 1", evalNullable = false)
    fun Source.map3(@Mapping.Target target: Target)


    @Mapping(target = "size", eval = "1 + 1")
    fun Source.map2_1(): Target

    @Mapping(target = "size", eval = "1 + 1")
    fun Source.map2_2(@Mapping.Target target: Target): Target

    @Mapping(target = "size", eval = "1 + 1")
    fun Source.map2_3(@Mapping.Target target: Target)
}

