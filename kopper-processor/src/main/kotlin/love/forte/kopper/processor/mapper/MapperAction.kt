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

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import love.forte.kopper.annotation.Map
import love.forte.kopper.processor.def.MapArgs
import love.forte.kopper.processor.util.hasAnno

internal data class MapperMapSetFunInfo(
    val name: String,
    val receiver: MapperMapSetFunReceiver?,
    val parameters: List<MapperMapSetFunParameter>,
    val returns: KSType?
)

internal data class MapperMapSetFunReceiver(
    val type: KSType,
    val parameterType: MapperMapSetFunParameterType
)

internal inline val MapperMapSetFunReceiver.isSource: Boolean
    get() = parameterType == MapperMapSetFunParameterType.SOURCE

internal inline val MapperMapSetFunReceiver.isTarget: Boolean
    get() = parameterType == MapperMapSetFunParameterType.TARGET

internal data class MapperMapSetFunParameter(
    val name: String?,
    val type: KSType,
    val parameterType: MapperMapSetFunParameterType,
)

internal inline val MapperMapSetFunParameter.isSource: Boolean
    get() = parameterType == MapperMapSetFunParameterType.SOURCE

internal inline val MapperMapSetFunParameter.isTarget: Boolean
    get() = parameterType == MapperMapSetFunParameterType.TARGET

internal enum class MapperMapSetFunParameterType {
    SOURCE, TARGET
}

/**
 * A set of `Map`s in a Mapper.
 *
 * @author ForteScarlet
 */
internal class MapperAction internal constructor(
    val environment: SymbolProcessorEnvironment,
    val resolver: Resolver,
    val func: MapperMapSetFunInfo,
    val mapArgs: List<MapArgs>,
    val sources: MutableList<MapActionSource> = mutableListOf(),
    val maps: MutableList<MapperMap> = mutableListOf(),
    /**
     * If a sub mapSet
     */
    val parentProperty: MapSourceProperty? = null,
) {
    constructor(
        environment: SymbolProcessorEnvironment,
        resolver: Resolver,
        sourceFun: KSFunctionDeclaration,
        mapArgs: List<MapArgs>,
        sources: MutableList<MapActionSource> = mutableListOf(),
        maps: MutableList<MapperMap> = mutableListOf(),
        parentProperty: MapSourceProperty? = null,
    ) : this(
        environment = environment,
        resolver = resolver,
        func = MapperMapSetFunInfo(
            name = sourceFun.simpleName.asString(),
            receiver = sourceFun.extensionReceiver?.resolve()?.let {
                MapperMapSetFunReceiver(
                    type = it,
                    parameterType = if (
                        it.hasAnno(resolver.getClassDeclarationByName<Map.Target>()!!.asStarProjectedType())
                    ) {
                        MapperMapSetFunParameterType.TARGET
                    } else {
                        MapperMapSetFunParameterType.SOURCE
                    }
                )
            },
            parameters = sourceFun.parameters.map { p ->
                MapperMapSetFunParameter(
                    name = p.name?.asString(),
                    type = p.type.resolve(),
                    parameterType = if (
                        p.hasAnno(resolver.getClassDeclarationByName<Map.Target>()!!.asStarProjectedType())
                    ) {
                        MapperMapSetFunParameterType.TARGET
                    } else {
                        MapperMapSetFunParameterType.SOURCE
                    }
                )
            },
            returns = sourceFun.returnType?.resolve()?.takeIf { !resolver.builtIns.unitType.isAssignableFrom(it) }
        ),
        mapArgs = mapArgs,
        sources = sources,
        maps = maps,
        parentProperty = parentProperty,
    ) {
        this.sourceFun = sourceFun
    }

    lateinit var targetClassDeclaration: KSClassDeclaration
    lateinit var target: MapTarget
    var sourceFun: KSFunctionDeclaration? = null
        private set

    var subMapperSets = mutableListOf<MapperAction>()

    val ignoreTargets = mapArgs
        .filter { it.ignore }
        .mapTo(mutableSetOf()) { it.target }

    fun emit(writer: MapperWriter) {
        val funName = func.name
        val funBuilder = FunSpec.builder(func.name)
        if (sourceFun != null) {
            funBuilder.addModifiers(KModifier.OVERRIDE)
        } else {
            funBuilder.addModifiers(KModifier.PRIVATE)
        }

        // parameters
        func.receiver?.also { funBuilder.receiver(it.type.toTypeName()) }
        func.parameters.forEach { funBuilder.addParameter(it.name!!, it.type.toTypeName()) }
        // return
        func.returns?.toTypeName()?.also { funBuilder.returns(it) }

        val setWriter = writer.newMapSetWriter(funBuilder)

        // init target and emit maps.
        // target.emitInit(setWriter)
        target.emitInitBegin(setWriter)
        var finished = false

        maps.sortedByDescending { it is ConstructorMapperMap }
            .forEachIndexed { index, map ->
                if (!finished && map !is ConstructorMapperMap) {
                    target.emitInitFinish(setWriter)
                    finished = true
                }
                map.emit(setWriter, index)
            }

        if (!finished) {
            target.emitInitFinish(setWriter)
        }

        // do return
        if (func.returns != null && func.returns != resolver.builtIns.unitType) {
            setWriter.addStatement("return %L", target.name)
        }

        val key = MapperMapSetKey(
            name = funName,
            target = target,
            sources = sources.toSet()
        )

        val info = MapperMapSetInfo(
            funSpec = funBuilder,
            isAncillary = false
        )

        writer.add(key, info)

        // subs
        subMapperSets.forEach { it.emit(writer) }
    }

    override fun toString(): String {
        return "MapperMapSet(func=$func, mapArgs=$mapArgs, sourceFun=$sourceFun, parentProperty=$parentProperty)"
    }


}
