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

package love.forte.kopper.annotation

import kotlin.reflect.KClass

/**
 * A mapper marker.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
public annotation class Mapper(
    // target
    val genTarget: MapperGenTarget = MapperGenTarget.OBJECT,
    val visibility: MapperGenVisibility = MapperGenVisibility.INTERNAL,
    val open: Boolean = false,
    // name
    val genTargetName: String = "", // same as Mapper and plus with prefix and suffix
    val genTargetNamePrefix: String = "",
    val genTargetNameSuffix: String = "Impl",
    val genTargetPackages: Array<String> = [],

    // TODO annotations
    /**
     *
     * ```kotlin
     * @Mapper(
     *     annotations = [
     *         Mapper.Anno(Component::class)
     *     ]
     * )
     * interface MyMapper
     * ```
     *
     * ```kotlin
     * @Mapper(
     *     annotations = [
     *         Mapper.Anno(
     *             value = Component::class,
     *             members = [
     *                 Mapper.AnnoMember(
     *                     format = "name = %S",
     *                     args = ["MyMapperName"]
     *                 )
     *             ]
     *         )
     *     ]
     * )
     * interface MyMapper
     * ```
     *
     * @suppress TODO
     */
    val annotations: Array<Anno> = [],

    // TODO strategy

) {
    public annotation class Anno(
        val value: KClass<out Annotation>,
        val members: Array<AnnoMember> = []
    )

    public annotation class AnnoMember(
        val format: String,
        val args: Array<String> = []
    )


}

public enum class MapperGenTarget {
    /**
     * Generate the mapper implement as a simple final class.
     *
     * ```Kotlin
     * @Mapper(genTarget = CLASS)
     * interface MyMapper {
     *   // ...
     * }
     *
     * // Generated:
     *
     * class MyMapperImpl : MyMapper {
     *   // ...
     * }
     * ```
     *
     *
     */
    CLASS,

    /**
     * Generate the mapper implement as an object.
     *
     * ```Kotlin
     * @Mapper(genTarget = CLASS)
     * interface MyMapper {
     *   // ...
     * }
     *
     * // Generated:
     *
     * object MyMapperImpl : MyMapper {
     *   // ...
     * }
     * ```
     *
     *
     */
    OBJECT,
}

public enum class MapperGenVisibility {
    PUBLIC,
    INTERNAL,
}
