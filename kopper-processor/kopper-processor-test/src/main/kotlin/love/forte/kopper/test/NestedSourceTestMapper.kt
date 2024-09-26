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

@Mapper
interface NestedSourceTestMapper {
    data class TargetClass(var name: String)
    data class Source(val value0: SourceSub1)
    data class SourceSub1(val value1: SourceSub2)
    class SourceSub2(private val sub: SourceSub3) {
        fun value2(): SourceSub3 = sub
    }
    data class SourceSub3(val name: String)

    @Map(target = "name", source = "value0.value1.value2.name")
    fun Source.map1(): TargetClass

    @Map(target = "name", source = "value0.value1.value2.name")
    fun Source.map2(@Map.Target target: TargetClass): TargetClass

    @Map(target = "name", source = "value0.value1.value2.name")
    fun Source.map3(@Map.Target target: TargetClass)
}
