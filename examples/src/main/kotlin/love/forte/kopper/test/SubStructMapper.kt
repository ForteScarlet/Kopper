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
interface SubStructEvalMapper {
    data class Source(val value: SourceSub)
    data class SourceSub(val number: Int)

    data class Target(val value: TargetSub)
    data class TargetSub(val number: Long)

    @Mapping(target = "value", eval = "subMap(source.value)")
    fun map(source: Source): Target

    @Mapping(target = "value", eval = "value.subMap1()")
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

    data class TargetSub(val number: Long, val name: String)

    data class Target(val value: TargetSub, val target: String)

    @Mapping(target = "value")
    fun map(source: Source): Target

    @Mapping(target = "value")
    fun Source.map1(): Target

    @Mapping(target = "value.number", source = "value.age")
    fun map2(source: Source1): Target

    @Mapping(target = "value.number", source = "value.age")
    fun Source1.map2_1(): Target
}

@Mapper
abstract class NestedWithoutMapMapper {
    data class SourceSub(val number: Int, val name: String)
    data class Source(val value: SourceSub, val target: String)
    data class Source2(val value: SourceSub?, val target: String)

    data class TargetSub(val number: Long, val name: String)
    data class Target(val value: TargetSub, val target: String)
    data class Target2(val value: TargetSub?, val target: String)

    abstract fun map1(source: Source): Target
    abstract fun map2(source: Source2): Target
    abstract fun map3(source: Source): Target2
    abstract fun map4(source: Source2): Target2
}
