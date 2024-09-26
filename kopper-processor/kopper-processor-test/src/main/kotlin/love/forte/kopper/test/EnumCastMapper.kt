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

/**
 *
 * @author ForteScarlet
 */
@Mapper
interface EnumCastMapper {
    enum class SourceE {
        V1, V2, V3
    }

    enum class TargetE {
        V1, V2, V3
    }

    data class Source(val value: String, val type: SourceE)
    data class Source1(val value: String, val type: SourceE?)
    data class Target(val value: String, var type: TargetE)

    // fun toTarget(source: Source): Target
    // fun Source.toTarget1(): Target

    fun toTarget0(source: Source, @Mapping.Target target: Target): Target
    fun toTarget1(source: Source?, @Mapping.Target target: Target): Target

    fun toTarget2(source: Source1, @Mapping.Target target: Target): Target
    fun toTarget3(source: Source1?, @Mapping.Target target: Target): Target
    // fun Source.toTarget01(@Mapping.Target target: Target): Target
}
