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

import com.google.devtools.ksp.symbol.KSAnnotation
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import love.forte.kopper.annotation.MapperGenTarget
import love.forte.kopper.annotation.MapperGenVisibility
import love.forte.kopper.processor.util.findArg
import love.forte.kopper.processor.util.isEvalExpression

/**
 * A Mapper with a set of [MapperMapSet].
 */
internal class Mapper(
    /**
     * The gen target name.
     */
    val targetName: String,
    val targetPackage: String,

    /**
     * The set of [MapperMapSet].
     */
    val mapSet: MutableList<MapperMapSet> = mutableListOf(),

    val genTarget: MapperGenTarget,
    val genVisibility: MapperGenVisibility
) {


}

/**
 * A set of `Map`s in a Mapper.
 *
 * @author ForteScarlet
 */
internal class MapperMapSet internal constructor(
    target: MapTarget? = null,
    internal val sources: MutableList<MapSource> = mutableListOf(),
    internal val maps: MutableList<MapperMap> = mutableListOf(),
    private val mapArgs: List<MapArgs>
) {
    lateinit var target: MapTarget

    init {
        if (target != null) {
            this.target = target
        }
    }

    val mainSource: MapSource
        get() = sources.first { it.isMain }

    fun findTargetMapArg(targetPath: String): MapArgs? {
        return mapArgs.find { it.target == targetPath }
    }

    private val sourcesMapArgs: Map<String, Map<String, List<MapArgs>>> =
        mapArgs
            .filter { !it.source.isEvalExpression }
            .groupBy { it.sourceName }
            .mapValues { it.value.groupBy { v -> v.source } }

    private val mapArgsTargetPathKey = mapArgs.associateBy { it.target }

    fun findSourceProperty(source: String?, pathName: String): MapSourceProperty? {
        val mapSource = if (source == null) mainSource else sources.find { it.name == source }
            ?: error("Can't find required source '$source'")

        mapSource.property(pathName, )

        TODO()

        // return sourceProperties.computeIfAbsent(pathName) { p ->
        //     resolveProperty(source, p)
        // }
    }

    private fun resolveProperty(source: String?, path: String): MapSourceProperty {
        if ('.' !in path) {
            // new source property
            val find = sourceProperties[path]
            if (find == null) {
                // TODO

                TODO()
            }
        }


        val parentName = path.substringBeforeLast('.')
        val name = path.substringAfterLast('.')




        TODO()


    }

    fun funSpec(): FunSpec {
        TODO("Not yet implemented")
    }
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
     * The target property.
     */
    public val targetProperty: MapTargetProperty

    /**
     * Gen a [CodeBlock] for this map.
     *
     * @param index The index of this map.
     */
    public fun code(index: Int): CodeBlock
}


internal data class MapperArgs(
    val genTarget: MapperGenTarget,
    val visibility: MapperGenVisibility,

    // name
    val genTargetName: String,
    val genTargetNamePrefix: String,
    val genTargetNameSuffix: String,
    val genTargetPackages: List<String>,
) {
    val packageName: String = genTargetPackages.joinToString(".")
    inline fun targetName(declarationSimpleName: () -> String): String =
        genTargetNamePrefix +
            (genTargetName.takeIf { it.isNotEmpty() } ?: declarationSimpleName()) +
            genTargetNameSuffix


}

internal fun KSAnnotation.resolveMapperArgs(): MapperArgs {
    val genTarget: MapperGenTarget = findArg("genTarget")!!
    val visibility: MapperGenVisibility = findArg("visibility")!!

    // Name-related arguments
    val genTargetName: String = findArg("genTargetName")!!
    val genTargetNamePrefix: String = findArg("genTargetNamePrefix")!!
    val genTargetNameSuffix: String = findArg("genTargetNameSuffix")!!
    val genTargetPackages: List<String> = findArg<Array<String>>("genTargetPackages")!!.toList()

    return MapperArgs(
        genTarget = genTarget,
        visibility = visibility,
        genTargetName = genTargetName,
        genTargetNamePrefix = genTargetNamePrefix,
        genTargetNameSuffix = genTargetNameSuffix,
        genTargetPackages = genTargetPackages
    )
}

