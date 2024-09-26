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
import love.forte.kopper.annotation.MapperGenTarget

/**
 *
 * @author ForteScarlet
 */
@Mapper(genTarget = MapperGenTarget.CLASS)
interface NestedSubActionSourceMapper {
    data class Source1(val value: SubSource1)
    data class SubSource1(val name: String)

    data class Source2(val value: SubSource2)
    data class SubSource2(val age: String)

    data class Source3(val value: SubSource3)
    data class SubSource3(val size: ULong)

    data class Target(val sub: SubTarget) {
        var size: UInt = 0u
    }

    data class SubTarget(val name: String, val age: String)

    @Map(target = "sub.name", source = "value.name")
    // ⬆️ Default source name is the MainSource,
    // and it will be the receiver or first source in parameter if without @Map.MainSource.
    @Map(target = "sub.age", source = "value.age", sourceName = "source2")
    @Map(target = "size", source = "value.size", sourceName = "source3")
    fun toTarget(source1: Source1, source2: Source2, source3: Source3): Target
}
