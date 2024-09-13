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

package love.forte.kopper.processor.mapper

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import love.forte.kopper.annotation.MapperGenTarget
import love.forte.kopper.annotation.MapperGenVisibility

/**
 * A Mapper with a set of [MapperMapSet].
 */
public interface Mapper {
    /**
     * The gen target name.
     */
    public val targetName: String
    public val targetPackage: String

    /**
     * The set of [MapperMapSet].
     */
    public val mapSet: List<MapperMapSet>

    public val genTarget: MapperGenTarget
    public val genVisibility: MapperGenVisibility
}

/**
 * A set of `Map`s in a Mapper.
 *
 * @author ForteScarlet
 */
public interface MapperMapSet {
    /**
     * Sources in this mapper.
     * The number of elements is at least one,
     * and the first element is the `main` source.
     */
    public val sources: List<MapSource>

    public val mainSource: MapSource
        get() = sources.first()

    public val target: MapTarget

    public val maps: List<MapperMap>

    // Other options?

    // gen

    /**
     * Generate a [FunSpec]
     */
    public fun funSpec(): FunSpec
}


/**
 * A single map in [MapperMapSet].
 */
public interface MapperMap {
    /**
     * The source. If the source is an eval expression,
     * the source will be the main source.
     */
    public val source: MapSource

    /**
     * The target.
     */
    public val target: MapTarget

    /**
     * Gen a [CodeBlock] for this map.
     */
    public fun code(): CodeBlock
}
