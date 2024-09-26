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

import love.forte.kopper.annotation.Mapper.Companion.PACKAGE_SAME_AS_SOURCE
import kotlin.reflect.KClass

/**
 * A Mapper marker.
 *
 * Annotated on an interface or abstract class,
 * the annotation processor scans all the **abstract functions** in it,
 * analyses them, and generates an implementation type for it.
 *
 * ```kotlin
 * // source
 * @Mapper
 * interface MyMapper { ... }
 *
 * // generated
 * @GeneratedMapper(sources = [MyMapper::class])
 * internal object MyMapperImpl : MyMapper { ... }
 * ```
 *
 * @property genTarget The type of the generated class.
 * [OBJECT][MapperGenTarget.OBJECT] is default.
 * @property visibility The visibility of the generated class.
 * [INTERNAL][MapperGenVisibility.INTERNAL] is default.
 * @property open Make the generated class `open` if possible
 * ([genTarget] is [CLASS][MapperGenTarget.CLASS]).
 * @property genTargetName The base name of the generated class.
 * Full class name is [genTargetNamePrefix] + [genTargetName] + [genTargetNameSuffix].
 * If [genTargetName] is empty, source function's name will be used.
 * @property genTargetNamePrefix The name's prefix of the generated class. See also: [genTargetName]
 * @property genTargetNameSuffix The name's suffix of the generated class. See also: [genTargetName]
 * @property genTargetPackage The package name of the generated class.
 * if value is [PACKAGE_SAME_AS_SOURCE] then the source function's package will be used.
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
    val genTargetPackage: String = PACKAGE_SAME_AS_SOURCE,

    // TODO annotations
    // /**
    //  *
    //  * ```kotlin
    //  * @Mapper(
    //  *     annotations = [
    //  *         Mapper.Anno(Component::class)
    //  *     ]
    //  * )
    //  * interface MyMapper
    //  * ```
    //  *
    //  * ```kotlin
    //  * @Mapper(
    //  *     annotations = [
    //  *         Mapper.Anno(
    //  *             value = Component::class,
    //  *             members = [
    //  *                 Mapper.AnnoMember(
    //  *                     format = "name = %S",
    //  *                     args = ["MyMapperName"]
    //  *                 )
    //  *             ]
    //  *         )
    //  *     ]
    //  * )
    //  * interface MyMapper
    //  * ```
    //  *
    //  * @suppress TODO
    //  */
    // val annotations: Array<Anno> = [],

    // TODO requirements, etc.

    // TODO null strategy, etc.

) {
    public annotation class Anno(
        val value: KClass<out Annotation>,
        val members: Array<AnnoMember> = []
    )

    public annotation class AnnoMember(
        val format: String,
        val args: Array<String> = []
    )

    public companion object {
        public const val PACKAGE_SAME_AS_SOURCE: String = "<SOURCE>"
    }
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
