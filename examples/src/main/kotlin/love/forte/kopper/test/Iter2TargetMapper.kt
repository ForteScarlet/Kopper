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
interface Iter2TargetMapper {
    data class SubSource(val name: String)
    data class SubSource2(val name: String?)

    data class ListSource1(val value: List<SubSource>)
    data class ListSource2(val value: List<SubSource2>)
    data class ListSource3(val value: List<SubSource>?)
    data class ListSource4(val value: List<SubSource2>?)

    data class SetSource1(val value: List<SubSource>)
    data class SetSource2(val value: List<SubSource2>)
    data class SetSource3(val value: List<SubSource>?)
    data class SetSource4(val value: List<SubSource2>?)

    data class CollSource1(val value: Collection<SubSource>)
    data class CollSource2(val value: Collection<SubSource2>)
    data class CollSource3(val value: Collection<SubSource>?)
    data class CollSource4(val value: Collection<SubSource2>?)

    data class IterSource1(val value: Iterable<SubSource>)
    data class IterSource2(val value: Iterable<SubSource2>)
    data class IterSource3(val value: Iterable<SubSource>?)
    data class IterSource4(val value: Iterable<SubSource2>?)


    data class SubTarget(val name: String)
    data class SubTarget2(val name: String?)

    data class Target(val value: SubTarget)
    data class Target2(val value: SubTarget2?)

    fun ls11(source: ListSource1): Target
    fun ls21(source: ListSource2): Target
    fun ls31(source: ListSource3): Target
    fun ls41(source: ListSource4): Target
    fun ls12(source: ListSource1): Target2
    fun ls22(source: ListSource2): Target2
    fun ls32(source: ListSource3): Target2
    fun ls42(source: ListSource4): Target2

    fun ss11(source: SetSource1): Target
    fun ss21(source: SetSource2): Target
    fun ss31(source: SetSource3): Target
    fun ss41(source: SetSource4): Target
    fun ss12(source: SetSource1): Target2
    fun ss22(source: SetSource2): Target2
    fun ss32(source: SetSource3): Target2
    fun ss42(source: SetSource4): Target2

    fun cs11(source: CollSource1): Target
    fun cs21(source: CollSource2): Target
    fun cs31(source: CollSource3): Target
    fun cs41(source: CollSource4): Target
    fun cs12(source: CollSource1): Target2
    fun cs22(source: CollSource2): Target2
    fun cs32(source: CollSource3): Target2
    fun cs42(source: CollSource4): Target2

    fun is11(source: IterSource1): Target
    fun is21(source: IterSource2): Target
    fun is31(source: IterSource3): Target
    fun is41(source: IterSource4): Target
    fun is12(source: IterSource1): Target2
    fun is22(source: IterSource2): Target2
    fun is32(source: IterSource3): Target2
    fun is42(source: IterSource4): Target2

}
