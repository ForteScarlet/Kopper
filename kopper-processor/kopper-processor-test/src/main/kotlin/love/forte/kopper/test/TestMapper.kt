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
interface TestMapperWithoutTarget {
    data class Source1(val name: String, val age: Int)
    data class Source2(val size: Long)

    data class Target(val name: String, val age: Int, val size: Long) {
        var size2: Long = 1L
    }

    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun Source1.mapTo1(source2: Source2): Target

    @Map("name", "name")
    @Map("age", "age")
    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun Source1.mapTo2(source2: Source2): Target

    @Map("name", "name")
    @Map("age", "age")
    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun mapTo3(source1: Source1, source2: Source2): Target

    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun mapTo4(source1: Source1, source2: Source2): Target

}

@Mapper
interface TestMapperIncludedTarget {
    data class Source1(val name: String, val age: Int)
    data class NullableSource1(val name: String?, val age: Int)
    data class Source2(val size: Long)

    class Target {
        lateinit var name: String
        var age: Int = 0
        var size: Long = 0L
        var size2: Long = 1L
    }

    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun Source1.mapTo1(source2: Source2, @Map.Target target: Target): Target

    @Map("name", "name")
    @Map("age", "age")
    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun Source1.mapTo2(source2: Source2, @Map.Target target: Target): Target

    @Map("name", "name")
    @Map("age", "age")
    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun mapTo3(source1: Source1, source2: Source2, @Map.Target target: Target): Target

    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun mapTo4(source1: Source1, source2: Source2, @Map.Target target: Target): Target

    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun Source1.mapTo2_1(source2: Source2, @Map.Target target: Target)

    @Map("name", "name")
    @Map("age", "age")
    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun Source1.mapTo2_2(source2: Source2, @Map.Target target: Target)

    @Map("name", "name")
    @Map("age", "age")
    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun mapTo2_3(source1: Source1, source2: Source2, @Map.Target target: Target)

    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun mapTo2_4(source1: Source1, source2: Source2, @Map.Target target: Target)

}

@Mapper
interface NullableTestMapperWithoutTarget {
    data class Source1(val name: String?, val age: Int?)
    data class Source2(val size: Long)

    data class Target(val name: String, val age: Int, val size: Long) {
        var size2: Long = 1L
    }

    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun Source1.mapTo1(source2: Source2): Target

    @Map("name", "name")
    @Map("age", "age")
    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun Source1.mapTo2(source2: Source2): Target

    @Map("name", "name")
    @Map("age", "age")
    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun mapTo3(source1: Source1, source2: Source2): Target

    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun mapTo4(source1: Source1, source2: Source2): Target

}

@Mapper
interface NullableTestMapperIncludedTarget {
    data class Source1(val name: String?, val age: Int?)
    data class Source2(val size: Long)

    class Target {
        lateinit var name: String
        var age: Int = 0
        var size: Long = 0L
        var size2: Long = 1L
    }

    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun Source1.mapTo1(source2: Source2, @Map.Target target: Target): Target

    @Map("name", "name")
    @Map("age", "age")
    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun Source1.mapTo2(source2: Source2, @Map.Target target: Target): Target

    @Map("name", "name")
    @Map("age", "age")
    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun mapTo3(source1: Source1, source2: Source2, @Map.Target target: Target): Target

    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun mapTo4(source1: Source1, source2: Source2, @Map.Target target: Target): Target

    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun Source1.mapTo2_1(source2: Source2, @Map.Target target: Target)

    @Map("name", "name")
    @Map("age", "age")
    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun Source1.mapTo2_2(source2: Source2, @Map.Target target: Target)

    @Map("name", "name")
    @Map("age", "age")
    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun mapTo2_3(source1: Source1, source2: Source2, @Map.Target target: Target)

    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun mapTo2_4(source1: Source1, source2: Source2, @Map.Target target: Target)

}
