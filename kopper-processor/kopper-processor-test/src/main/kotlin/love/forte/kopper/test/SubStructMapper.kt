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
interface SubStructEvalMapper {
    data class Source(val value: SourceSub)
    data class SourceSub(val number: Int)

    data class Target(val value: TargetSub)
    data class TargetSub(val number: Long)

    @Map(target = "value", eval = "subMap(source.value)")
    fun map(source: Source): Target

    @Map(target = "value", eval = "value.subMap1()")
    fun Source.map1(): Target

    fun subMap(source: SourceSub): TargetSub
    fun SourceSub.subMap1(): TargetSub
}

@Mapper
interface SubStructWithMapMapper {
    data class Source(val value: SourceSub, val target: String)
    data class SourceSub(val number: Int, val name: String)
    data class Source1(val value: SourceSub1, val target: String)
    data class SourceSub1(val age: Int, val name: String)

    data class Target(val value: TargetSub, val target: String)
    data class TargetSub(val number: Long, val name: String)

    @Map(target = "value")
    fun map(source: Source): Target

    @Map(target = "value")
    fun Source.map1(): Target

    @Map(target = "value.number", source = "value.age")
    fun map2(source: Source1): Target

    @Map(target = "value.number", source = "value.age")
    fun Source1.map2_1(): Target
}

@Mapper
abstract class NestedWithoutMapMapper {
    data class Source(val value: SourceSub, val target: String)
    data class SourceSub(val number: Int, val name: String)

    data class Target(val value: TargetSub, val target: String)
    data class TargetSub(val number: Long, val name: String)

    abstract fun map(source: Source): Target

    abstract fun Source.map1(): Target
}
