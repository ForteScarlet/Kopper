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

data class SourceType1(val name: String, val age: Int)
data class SourceType2(val size: Long)

data class TargetType(val name: String, val age: Int, val size: Long) {
    var size2: Long = 1L
}

/**
 *
 * @author ForteScarlet
 */
@Mapper
interface TestMapper {

    // @Map("name", "name")
    // @Map("age", "age")
    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun SourceType1.mapTo1(source2: SourceType2): TargetType

    @Map("name", "name")
    @Map("age", "age")
    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun SourceType1.mapTo2(source2: SourceType2): TargetType

    @Map("name", "name")
    @Map("age", "age")
    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun mapTo3(source1: SourceType1, source2: SourceType2): TargetType

    @Map("size", sourceName = "source2")
    @Map("size2", source = "size", sourceName = "source2")
    fun mapTo4(source1: SourceType1, source2: SourceType2): TargetType

}
