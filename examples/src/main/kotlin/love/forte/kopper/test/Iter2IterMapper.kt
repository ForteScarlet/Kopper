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
interface Iter2IterMapper {
    data class SubSource1(val name: String)
    data class SubSource2(val name: String?)

    data class ListSource1(val value: List<SubSource1>)
    data class ListSource2(val value: List<SubSource1>?)
    data class ListSource3(val value: List<SubSource2>)
    data class ListSource4(val value: List<SubSource2>?)

    data class SetSource1(val value: Set<SubSource1>)
    data class SetSource2(val value: Set<SubSource1>?)
    data class SetSource3(val value: Set<SubSource2>)
    data class SetSource4(val value: Set<SubSource2>?)

    data class CollSource1(val value: Collection<SubSource1>)
    data class CollSource2(val value: Collection<SubSource1>?)
    data class CollSource3(val value: Collection<SubSource2>)
    data class CollSource4(val value: Collection<SubSource2>?)

    data class IterSource1(val value: Iterable<SubSource1>)
    data class IterSource2(val value: Iterable<SubSource1>?)
    data class IterSource3(val value: Iterable<SubSource2>)
    data class IterSource4(val value: Iterable<SubSource2>?)


    data class SubTarget1(val name: String)
    data class SubTarget2(val name: String?)

    data class ListTarget1(val value: List<SubTarget1>)
    data class ListTarget2(val value: List<SubTarget1>?)
    data class ListTarget3(val value: List<SubTarget2>)
    data class ListTarget4(val value: List<SubTarget2>?)

    data class SetTarget1(val value: Set<SubTarget1>)
    data class SetTarget2(val value: Set<SubTarget1>?)
    data class SetTarget3(val value: Set<SubTarget2>)
    data class SetTarget4(val value: Set<SubTarget2>?)

    data class CollTarget1(val value: Collection<SubTarget1>)
    data class CollTarget2(val value: Collection<SubTarget1>?)
    data class CollTarget3(val value: Collection<SubTarget2>)
    data class CollTarget4(val value: Collection<SubTarget2>?)

    data class IterTarget1(val value: Iterable<SubTarget1>)
    data class IterTarget2(val value: Iterable<SubTarget1>?)
    data class IterTarget3(val value: Iterable<SubTarget2>)
    data class IterTarget4(val value: Iterable<SubTarget2>?)

    fun listSource12ListTarget1(source: ListSource1): ListTarget1
    fun listSource12ListTarget2(source: ListSource1): ListTarget2
    fun listSource12ListTarget3(source: ListSource1): ListTarget3
    fun listSource12ListTarget4(source: ListSource1): ListTarget4
    fun listSource12SetTarget1(source: ListSource1): SetTarget1
    fun listSource12SetTarget2(source: ListSource1): SetTarget2
    fun listSource12SetTarget3(source: ListSource1): SetTarget3
    fun listSource12SetTarget4(source: ListSource1): SetTarget4
    fun listSource12CollTarget1(source: ListSource1): CollTarget1
    fun listSource12CollTarget2(source: ListSource1): CollTarget2
    fun listSource12CollTarget3(source: ListSource1): CollTarget3
    fun listSource12CollTarget4(source: ListSource1): CollTarget4
    fun listSource12IterTarget1(source: ListSource1): IterTarget1
    fun listSource12IterTarget2(source: ListSource1): IterTarget2
    fun listSource12IterTarget3(source: ListSource1): IterTarget3
    fun listSource12IterTarget4(source: ListSource1): IterTarget4
    fun listSource22ListTarget1(source: ListSource2): ListTarget1
    fun listSource22ListTarget2(source: ListSource2): ListTarget2
    fun listSource22ListTarget3(source: ListSource2): ListTarget3
    fun listSource22ListTarget4(source: ListSource2): ListTarget4
    fun listSource22SetTarget1(source: ListSource2): SetTarget1
    fun listSource22SetTarget2(source: ListSource2): SetTarget2
    fun listSource22SetTarget3(source: ListSource2): SetTarget3
    fun listSource22SetTarget4(source: ListSource2): SetTarget4
    fun listSource22CollTarget1(source: ListSource2): CollTarget1
    fun listSource22CollTarget2(source: ListSource2): CollTarget2
    fun listSource22CollTarget3(source: ListSource2): CollTarget3
    fun listSource22CollTarget4(source: ListSource2): CollTarget4
    fun listSource22IterTarget1(source: ListSource2): IterTarget1
    fun listSource22IterTarget2(source: ListSource2): IterTarget2
    fun listSource22IterTarget3(source: ListSource2): IterTarget3
    fun listSource22IterTarget4(source: ListSource2): IterTarget4
    fun listSource32ListTarget1(source: ListSource3): ListTarget1
    fun listSource32ListTarget2(source: ListSource3): ListTarget2
    fun listSource32ListTarget3(source: ListSource3): ListTarget3
    fun listSource32ListTarget4(source: ListSource3): ListTarget4
    fun listSource32SetTarget1(source: ListSource3): SetTarget1
    fun listSource32SetTarget2(source: ListSource3): SetTarget2
    fun listSource32SetTarget3(source: ListSource3): SetTarget3
    fun listSource32SetTarget4(source: ListSource3): SetTarget4
    fun listSource32CollTarget1(source: ListSource3): CollTarget1
    fun listSource32CollTarget2(source: ListSource3): CollTarget2
    fun listSource32CollTarget3(source: ListSource3): CollTarget3
    fun listSource32CollTarget4(source: ListSource3): CollTarget4
    fun listSource32IterTarget1(source: ListSource3): IterTarget1
    fun listSource32IterTarget2(source: ListSource3): IterTarget2
    fun listSource32IterTarget3(source: ListSource3): IterTarget3
    fun listSource32IterTarget4(source: ListSource3): IterTarget4
    fun listSource42ListTarget1(source: ListSource4): ListTarget1
    fun listSource42ListTarget2(source: ListSource4): ListTarget2
    fun listSource42ListTarget3(source: ListSource4): ListTarget3
    fun listSource42ListTarget4(source: ListSource4): ListTarget4
    fun listSource42SetTarget1(source: ListSource4): SetTarget1
    fun listSource42SetTarget2(source: ListSource4): SetTarget2
    fun listSource42SetTarget3(source: ListSource4): SetTarget3
    fun listSource42SetTarget4(source: ListSource4): SetTarget4
    fun listSource42CollTarget1(source: ListSource4): CollTarget1
    fun listSource42CollTarget2(source: ListSource4): CollTarget2
    fun listSource42CollTarget3(source: ListSource4): CollTarget3
    fun listSource42CollTarget4(source: ListSource4): CollTarget4
    fun listSource42IterTarget1(source: ListSource4): IterTarget1
    fun listSource42IterTarget2(source: ListSource4): IterTarget2
    fun listSource42IterTarget3(source: ListSource4): IterTarget3
    fun listSource42IterTarget4(source: ListSource4): IterTarget4
    fun setSource12ListTarget1(source: SetSource1): ListTarget1
    fun setSource12ListTarget2(source: SetSource1): ListTarget2
    fun setSource12ListTarget3(source: SetSource1): ListTarget3
    fun setSource12ListTarget4(source: SetSource1): ListTarget4
    fun setSource12SetTarget1(source: SetSource1): SetTarget1
    fun setSource12SetTarget2(source: SetSource1): SetTarget2
    fun setSource12SetTarget3(source: SetSource1): SetTarget3
    fun setSource12SetTarget4(source: SetSource1): SetTarget4
    fun setSource12CollTarget1(source: SetSource1): CollTarget1
    fun setSource12CollTarget2(source: SetSource1): CollTarget2
    fun setSource12CollTarget3(source: SetSource1): CollTarget3
    fun setSource12CollTarget4(source: SetSource1): CollTarget4
    fun setSource12IterTarget1(source: SetSource1): IterTarget1
    fun setSource12IterTarget2(source: SetSource1): IterTarget2
    fun setSource12IterTarget3(source: SetSource1): IterTarget3
    fun setSource12IterTarget4(source: SetSource1): IterTarget4
    fun setSource22ListTarget1(source: SetSource2): ListTarget1
    fun setSource22ListTarget2(source: SetSource2): ListTarget2
    fun setSource22ListTarget3(source: SetSource2): ListTarget3
    fun setSource22ListTarget4(source: SetSource2): ListTarget4
    fun setSource22SetTarget1(source: SetSource2): SetTarget1
    fun setSource22SetTarget2(source: SetSource2): SetTarget2
    fun setSource22SetTarget3(source: SetSource2): SetTarget3
    fun setSource22SetTarget4(source: SetSource2): SetTarget4
    fun setSource22CollTarget1(source: SetSource2): CollTarget1
    fun setSource22CollTarget2(source: SetSource2): CollTarget2
    fun setSource22CollTarget3(source: SetSource2): CollTarget3
    fun setSource22CollTarget4(source: SetSource2): CollTarget4
    fun setSource22IterTarget1(source: SetSource2): IterTarget1
    fun setSource22IterTarget2(source: SetSource2): IterTarget2
    fun setSource22IterTarget3(source: SetSource2): IterTarget3
    fun setSource22IterTarget4(source: SetSource2): IterTarget4
    fun setSource32ListTarget1(source: SetSource3): ListTarget1
    fun setSource32ListTarget2(source: SetSource3): ListTarget2
    fun setSource32ListTarget3(source: SetSource3): ListTarget3
    fun setSource32ListTarget4(source: SetSource3): ListTarget4
    fun setSource32SetTarget1(source: SetSource3): SetTarget1
    fun setSource32SetTarget2(source: SetSource3): SetTarget2
    fun setSource32SetTarget3(source: SetSource3): SetTarget3
    fun setSource32SetTarget4(source: SetSource3): SetTarget4
    fun setSource32CollTarget1(source: SetSource3): CollTarget1
    fun setSource32CollTarget2(source: SetSource3): CollTarget2
    fun setSource32CollTarget3(source: SetSource3): CollTarget3
    fun setSource32CollTarget4(source: SetSource3): CollTarget4
    fun setSource32IterTarget1(source: SetSource3): IterTarget1
    fun setSource32IterTarget2(source: SetSource3): IterTarget2
    fun setSource32IterTarget3(source: SetSource3): IterTarget3
    fun setSource32IterTarget4(source: SetSource3): IterTarget4
    fun setSource42ListTarget1(source: SetSource4): ListTarget1
    fun setSource42ListTarget2(source: SetSource4): ListTarget2
    fun setSource42ListTarget3(source: SetSource4): ListTarget3
    fun setSource42ListTarget4(source: SetSource4): ListTarget4
    fun setSource42SetTarget1(source: SetSource4): SetTarget1
    fun setSource42SetTarget2(source: SetSource4): SetTarget2
    fun setSource42SetTarget3(source: SetSource4): SetTarget3
    fun setSource42SetTarget4(source: SetSource4): SetTarget4
    fun setSource42CollTarget1(source: SetSource4): CollTarget1
    fun setSource42CollTarget2(source: SetSource4): CollTarget2
    fun setSource42CollTarget3(source: SetSource4): CollTarget3
    fun setSource42CollTarget4(source: SetSource4): CollTarget4
    fun setSource42IterTarget1(source: SetSource4): IterTarget1
    fun setSource42IterTarget2(source: SetSource4): IterTarget2
    fun setSource42IterTarget3(source: SetSource4): IterTarget3
    fun setSource42IterTarget4(source: SetSource4): IterTarget4
    fun collSource12ListTarget1(source: CollSource1): ListTarget1
    fun collSource12ListTarget2(source: CollSource1): ListTarget2
    fun collSource12ListTarget3(source: CollSource1): ListTarget3
    fun collSource12ListTarget4(source: CollSource1): ListTarget4
    fun collSource12SetTarget1(source: CollSource1): SetTarget1
    fun collSource12SetTarget2(source: CollSource1): SetTarget2
    fun collSource12SetTarget3(source: CollSource1): SetTarget3
    fun collSource12SetTarget4(source: CollSource1): SetTarget4
    fun collSource12CollTarget1(source: CollSource1): CollTarget1
    fun collSource12CollTarget2(source: CollSource1): CollTarget2
    fun collSource12CollTarget3(source: CollSource1): CollTarget3
    fun collSource12CollTarget4(source: CollSource1): CollTarget4
    fun collSource12IterTarget1(source: CollSource1): IterTarget1
    fun collSource12IterTarget2(source: CollSource1): IterTarget2
    fun collSource12IterTarget3(source: CollSource1): IterTarget3
    fun collSource12IterTarget4(source: CollSource1): IterTarget4
    fun collSource22ListTarget1(source: CollSource2): ListTarget1
    fun collSource22ListTarget2(source: CollSource2): ListTarget2
    fun collSource22ListTarget3(source: CollSource2): ListTarget3
    fun collSource22ListTarget4(source: CollSource2): ListTarget4
    fun collSource22SetTarget1(source: CollSource2): SetTarget1
    fun collSource22SetTarget2(source: CollSource2): SetTarget2
    fun collSource22SetTarget3(source: CollSource2): SetTarget3
    fun collSource22SetTarget4(source: CollSource2): SetTarget4
    fun collSource22CollTarget1(source: CollSource2): CollTarget1
    fun collSource22CollTarget2(source: CollSource2): CollTarget2
    fun collSource22CollTarget3(source: CollSource2): CollTarget3
    fun collSource22CollTarget4(source: CollSource2): CollTarget4
    fun collSource22IterTarget1(source: CollSource2): IterTarget1
    fun collSource22IterTarget2(source: CollSource2): IterTarget2
    fun collSource22IterTarget3(source: CollSource2): IterTarget3
    fun collSource22IterTarget4(source: CollSource2): IterTarget4
    fun collSource32ListTarget1(source: CollSource3): ListTarget1
    fun collSource32ListTarget2(source: CollSource3): ListTarget2
    fun collSource32ListTarget3(source: CollSource3): ListTarget3
    fun collSource32ListTarget4(source: CollSource3): ListTarget4
    fun collSource32SetTarget1(source: CollSource3): SetTarget1
    fun collSource32SetTarget2(source: CollSource3): SetTarget2
    fun collSource32SetTarget3(source: CollSource3): SetTarget3
    fun collSource32SetTarget4(source: CollSource3): SetTarget4
    fun collSource32CollTarget1(source: CollSource3): CollTarget1
    fun collSource32CollTarget2(source: CollSource3): CollTarget2
    fun collSource32CollTarget3(source: CollSource3): CollTarget3
    fun collSource32CollTarget4(source: CollSource3): CollTarget4
    fun collSource32IterTarget1(source: CollSource3): IterTarget1
    fun collSource32IterTarget2(source: CollSource3): IterTarget2
    fun collSource32IterTarget3(source: CollSource3): IterTarget3
    fun collSource32IterTarget4(source: CollSource3): IterTarget4
    fun collSource42ListTarget1(source: CollSource4): ListTarget1
    fun collSource42ListTarget2(source: CollSource4): ListTarget2
    fun collSource42ListTarget3(source: CollSource4): ListTarget3
    fun collSource42ListTarget4(source: CollSource4): ListTarget4
    fun collSource42SetTarget1(source: CollSource4): SetTarget1
    fun collSource42SetTarget2(source: CollSource4): SetTarget2
    fun collSource42SetTarget3(source: CollSource4): SetTarget3
    fun collSource42SetTarget4(source: CollSource4): SetTarget4
    fun collSource42CollTarget1(source: CollSource4): CollTarget1
    fun collSource42CollTarget2(source: CollSource4): CollTarget2
    fun collSource42CollTarget3(source: CollSource4): CollTarget3
    fun collSource42CollTarget4(source: CollSource4): CollTarget4
    fun collSource42IterTarget1(source: CollSource4): IterTarget1
    fun collSource42IterTarget2(source: CollSource4): IterTarget2
    fun collSource42IterTarget3(source: CollSource4): IterTarget3
    fun collSource42IterTarget4(source: CollSource4): IterTarget4
    fun iterSource12ListTarget1(source: IterSource1): ListTarget1
    fun iterSource12ListTarget2(source: IterSource1): ListTarget2
    fun iterSource12ListTarget3(source: IterSource1): ListTarget3
    fun iterSource12ListTarget4(source: IterSource1): ListTarget4
    fun iterSource12SetTarget1(source: IterSource1): SetTarget1
    fun iterSource12SetTarget2(source: IterSource1): SetTarget2
    fun iterSource12SetTarget3(source: IterSource1): SetTarget3
    fun iterSource12SetTarget4(source: IterSource1): SetTarget4
    fun iterSource12CollTarget1(source: IterSource1): CollTarget1
    fun iterSource12CollTarget2(source: IterSource1): CollTarget2
    fun iterSource12CollTarget3(source: IterSource1): CollTarget3
    fun iterSource12CollTarget4(source: IterSource1): CollTarget4
    fun iterSource12IterTarget1(source: IterSource1): IterTarget1
    fun iterSource12IterTarget2(source: IterSource1): IterTarget2
    fun iterSource12IterTarget3(source: IterSource1): IterTarget3
    fun iterSource12IterTarget4(source: IterSource1): IterTarget4
    fun iterSource22ListTarget1(source: IterSource2): ListTarget1
    fun iterSource22ListTarget2(source: IterSource2): ListTarget2
    fun iterSource22ListTarget3(source: IterSource2): ListTarget3
    fun iterSource22ListTarget4(source: IterSource2): ListTarget4
    fun iterSource22SetTarget1(source: IterSource2): SetTarget1
    fun iterSource22SetTarget2(source: IterSource2): SetTarget2
    fun iterSource22SetTarget3(source: IterSource2): SetTarget3
    fun iterSource22SetTarget4(source: IterSource2): SetTarget4
    fun iterSource22CollTarget1(source: IterSource2): CollTarget1
    fun iterSource22CollTarget2(source: IterSource2): CollTarget2
    fun iterSource22CollTarget3(source: IterSource2): CollTarget3
    fun iterSource22CollTarget4(source: IterSource2): CollTarget4
    fun iterSource22IterTarget1(source: IterSource2): IterTarget1
    fun iterSource22IterTarget2(source: IterSource2): IterTarget2
    fun iterSource22IterTarget3(source: IterSource2): IterTarget3
    fun iterSource22IterTarget4(source: IterSource2): IterTarget4
    fun iterSource32ListTarget1(source: IterSource3): ListTarget1
    fun iterSource32ListTarget2(source: IterSource3): ListTarget2
    fun iterSource32ListTarget3(source: IterSource3): ListTarget3
    fun iterSource32ListTarget4(source: IterSource3): ListTarget4
    fun iterSource32SetTarget1(source: IterSource3): SetTarget1
    fun iterSource32SetTarget2(source: IterSource3): SetTarget2
    fun iterSource32SetTarget3(source: IterSource3): SetTarget3
    fun iterSource32SetTarget4(source: IterSource3): SetTarget4
    fun iterSource32CollTarget1(source: IterSource3): CollTarget1
    fun iterSource32CollTarget2(source: IterSource3): CollTarget2
    fun iterSource32CollTarget3(source: IterSource3): CollTarget3
    fun iterSource32CollTarget4(source: IterSource3): CollTarget4
    fun iterSource32IterTarget1(source: IterSource3): IterTarget1
    fun iterSource32IterTarget2(source: IterSource3): IterTarget2
    fun iterSource32IterTarget3(source: IterSource3): IterTarget3
    fun iterSource32IterTarget4(source: IterSource3): IterTarget4
    fun iterSource42ListTarget1(source: IterSource4): ListTarget1
    fun iterSource42ListTarget2(source: IterSource4): ListTarget2
    fun iterSource42ListTarget3(source: IterSource4): ListTarget3
    fun iterSource42ListTarget4(source: IterSource4): ListTarget4
    fun iterSource42SetTarget1(source: IterSource4): SetTarget1
    fun iterSource42SetTarget2(source: IterSource4): SetTarget2
    fun iterSource42SetTarget3(source: IterSource4): SetTarget3
    fun iterSource42SetTarget4(source: IterSource4): SetTarget4
    fun iterSource42CollTarget1(source: IterSource4): CollTarget1
    fun iterSource42CollTarget2(source: IterSource4): CollTarget2
    fun iterSource42CollTarget3(source: IterSource4): CollTarget3
    fun iterSource42CollTarget4(source: IterSource4): CollTarget4
    fun iterSource42IterTarget1(source: IterSource4): IterTarget1
    fun iterSource42IterTarget2(source: IterSource4): IterTarget2
    fun iterSource42IterTarget3(source: IterSource4): IterTarget3
    fun iterSource42IterTarget4(source: IterSource4): IterTarget4

}
