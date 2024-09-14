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

package love.forte.kopper.processor.mapper.impl

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FunSpec
import love.forte.kopper.annotation.Map
import love.forte.kopper.processor.mapper.*
import love.forte.kopper.processor.util.isEvalExpression

internal fun resolveToMapper(
    environment: SymbolProcessorEnvironment,
    resolver: Resolver,
    declaration: KSClassDeclaration
) { // TODO : Mapper
    val mapperAnnoType = resolver.getClassDeclarationByName<love.forte.kopper.annotation.Mapper>()
        ?: error("Cannot find Mapper annotation declaration.")

    val mapAnnoType = resolver.getClassDeclarationByName<Map>()
        ?: error("Cannot find Map annotation declaration.")

    val mapperAnnotation = declaration.annotations.first {
        mapperAnnoType.asStarProjectedType().isAssignableFrom(it.annotationType.resolve())
    }

    val mapperArgs = mapperAnnotation.resolveMapperArgs()
    val mapperName = mapperArgs.targetName { declaration.simpleName.asString() }

    val mappers = mutableListOf<FunSpec.Builder>()
    val mapperBuilder = MapperBuilderImpl(mappers)

    resolveToMapSet(
        environment = environment,
        resolver = resolver,
        declaration = declaration,
        mapAnnoType = mapAnnoType
    )
}


// internal class MapperMapSetImpl(
//     target: MapTarget? = null,
//     override val sources: MutableList<MapSource> = mutableListOf(),
//     override val maps: MutableList<MapperMap> = mutableListOf(),
//     private val mapArgs: List<MapArgs>
// ) : MapperMapSet {
//     override lateinit var target: MapTarget
//
//     init {
//         if (target != null) {
//             this.target = target
//         }
//     }
//
//     private val sourceProperties: MutableMap<String, MapSourceProperty> = linkedMapOf()
//     private val sourcesMapArgs: kotlin.collections.Map<String, kotlin.collections.Map<String, List<MapArgs>>> =
//         mapArgs
//             .filter { !it.source.isEvalExpression }
//             .groupBy { it.sourceName }
//             .mapValues { it.value.groupBy { v -> v.source } }
//
//     private val mapArgsTargetPathKey = mapArgs.associateBy { it.target }
//
//     override fun findSourceProperty(source: String?, pathName: String): MapSourceProperty? {
//         return sourceProperties.computeIfAbsent(pathName) { p ->
//             resolveProperty(p)
//         }
//     }
//
//     private fun resolveProperty(path: String): MapSourceProperty {
//         if ('.' !in path) {
//             // new source property
//             val find = sourceProperties[path]
//             if (find == null) {
//                 // TODO
//
//                 TODO()
//             }
//         }
//
//
//         val parentName = path.substringBeforeLast('.')
//         val name = path.substringAfterLast('.')
//
//
//
//
//         TODO()
//
//
//     }
//
//     override fun funSpec(): FunSpec {
//         TODO("Not yet implemented")
//     }
// }


internal fun resolveToMapSet(
    environment: SymbolProcessorEnvironment,
    resolver: Resolver,
    declaration: KSClassDeclaration,
    mapAnnoType: KSClassDeclaration,
) {

    declaration.getAllFunctions()
        .filter { it.isAbstract }
        .forEach { mapFun ->
            // initial map set
            val mapArgList = mapFun.annotations
                .filter {
                    mapAnnoType.asStarProjectedType().isAssignableFrom(it.annotationType.resolve())
                }
                .map { it.resolveToMapArgs() }
                .toList()

            val mapSet = MapperMapSet(
                target = null,
                mapArgs = mapArgList,
            )

            mapFun.resolveMapperMapFromMapAno(
                resolver = resolver,
                environment = environment,
                mapSet = mapSet,
                container = declaration,
                mapAnnoType = mapAnnoType
            )

        }

}


internal fun KSFunctionDeclaration.resolveMapperMapFromMapAno(
    environment: SymbolProcessorEnvironment,
    resolver: Resolver,
    mapSet: MapperMapSet,
    container: KSClassDeclaration,
    mapAnnoType: KSClassDeclaration,
) {
    // Sources
    var mainSource: MapSource? = null
    val sources by mapSet::sources

    val receiver = extensionReceiver?.resolve()

    this.extensionReceiver?.resolve()?.also { receiver ->
        val receiverSource = ParameterMapSource(
            sourceSet = mapSet,
            isMain = true,
            name = "this",
            type = receiver
        )

        mainSource = receiverSource
        sources.add(receiverSource)
    }

    this.parameters.forEachIndexed { index, param ->
        val mapSource = ParameterMapSource(
            sourceSet = mapSet,
            isMain = false,
            name = param.name?.asString() ?: "_s_$index",
            type = param.type.resolve()
        )

        sources.add(mapSource)
    }

    // Target
    MapTarget.create()

    // MapTarget.create(
    //     sourceSet = mapSet,
    //     name =
    // )


    TODO()
}

internal fun resolveAllPropertiesToMapperMap() {

}
