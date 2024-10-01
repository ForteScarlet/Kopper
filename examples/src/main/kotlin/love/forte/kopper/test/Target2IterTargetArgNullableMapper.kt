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

@Mapper
interface Target2IterTargetArgNullableMapper {
    data class SubSource1(val name: String)
    data class SubSource2(val name: String?)

    data class Source1(val value: SubSource1)
    data class Source2(val value: SubSource2?)

    data class SubTarget1(val name: String)
    data class SubTarget2(val name: String?)

    data class ListTarget1(val value: List<SubTarget1?>)
    data class ListTarget2(val value: List<SubTarget2?>)
    data class ListTarget3(val value: List<SubTarget1?>?)
    data class ListTarget4(val value: List<SubTarget2?>?)

    data class SetTarget1(val value: Set<SubTarget1?>)
    data class SetTarget2(val value: Set<SubTarget2?>)
    data class SetTarget3(val value: Set<SubTarget1?>?)
    data class SetTarget4(val value: Set<SubTarget2?>?)

    data class CollTarget1(val value: Collection<SubTarget1?>)
    data class CollTarget2(val value: Collection<SubTarget2?>)
    data class CollTarget3(val value: Collection<SubTarget1?>?)
    data class CollTarget4(val value: Collection<SubTarget2?>?)

    data class IterTarget1(val value: Iterable<SubTarget1?>)
    data class IterTarget2(val value: Iterable<SubTarget2?>)
    data class IterTarget3(val value: Iterable<SubTarget1?>?)
    data class IterTarget4(val value: Iterable<SubTarget2?>?)

    fun mapSource1ToListTarget1(source: Source1): ListTarget1
    fun mapSource1ToListTarget2(source: Source1): ListTarget2
    fun mapSource1ToListTarget3(source: Source1): ListTarget3
    fun mapSource1ToListTarget4(source: Source1): ListTarget4
    fun mapSource2ToListTarget1(source: Source2): ListTarget1
    fun mapSource2ToListTarget2(source: Source2): ListTarget2
    fun mapSource2ToListTarget3(source: Source2): ListTarget3
    fun mapSource2ToListTarget4(source: Source2): ListTarget4

    fun mapSource1ToSetTarget1(source: Source1): SetTarget1
    fun mapSource1ToSetTarget2(source: Source1): SetTarget2
    fun mapSource1ToSetTarget3(source: Source1): SetTarget3
    fun mapSource1ToSetTarget4(source: Source1): SetTarget4
    fun mapSource2ToSetTarget1(source: Source2): SetTarget1
    fun mapSource2ToSetTarget2(source: Source2): SetTarget2
    fun mapSource2ToSetTarget3(source: Source2): SetTarget3
    fun mapSource2ToSetTarget4(source: Source2): SetTarget4

    fun mapSource1ToCollTarget1(source: Source1): CollTarget1
    fun mapSource1ToCollTarget2(source: Source1): CollTarget2
    fun mapSource1ToCollTarget3(source: Source1): CollTarget3
    fun mapSource1ToCollTarget4(source: Source1): CollTarget4
    fun mapSource2ToCollTarget1(source: Source2): CollTarget1
    fun mapSource2ToCollTarget2(source: Source2): CollTarget2
    fun mapSource2ToCollTarget3(source: Source2): CollTarget3
    fun mapSource2ToCollTarget4(source: Source2): CollTarget4

    fun mapSource1ToIterTarget1(source: Source1): IterTarget1
    fun mapSource1ToIterTarget2(source: Source1): IterTarget2
    fun mapSource1ToIterTarget3(source: Source1): IterTarget3
    fun mapSource1ToIterTarget4(source: Source1): IterTarget4
    fun mapSource2ToIterTarget1(source: Source2): IterTarget1
    fun mapSource2ToIterTarget2(source: Source2): IterTarget2
    fun mapSource2ToIterTarget3(source: Source2): IterTarget3
    fun mapSource2ToIterTarget4(source: Source2): IterTarget4

}
