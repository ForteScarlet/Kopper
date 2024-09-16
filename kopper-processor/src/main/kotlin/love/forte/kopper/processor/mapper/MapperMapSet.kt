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

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.toTypeName

internal data class MapperMapSetFunInfo(
    val name: String,
    val receiver: KSType?,
    val parameters: List<MapperMapSetFunParameter>,
    val returns: KSType?
)

internal data class MapperMapSetFunParameter(
    val name: String?,
    val type: KSType,
)

/**
 * A set of `Map`s in a Mapper.
 *
 * @author ForteScarlet
 */
internal class MapperMapSet internal constructor(
    val environment: SymbolProcessorEnvironment,
    val resolver: Resolver,
    val func: MapperMapSetFunInfo,
    val mapArgs: List<MapArgs>,
    val sources: MutableList<MapSource> = mutableListOf(),
    val maps: MutableList<MapperMap> = mutableListOf(),
) {
    constructor(
        environment: SymbolProcessorEnvironment,
        resolver: Resolver,
        sourceFun: KSFunctionDeclaration,
        mapArgs: List<MapArgs>,
        sources: MutableList<MapSource> = mutableListOf(),
        maps: MutableList<MapperMap> = mutableListOf(),
    ) : this(
        environment = environment,
        resolver = resolver,
        func = MapperMapSetFunInfo(
            name = sourceFun.simpleName.asString(),
            receiver = sourceFun.extensionReceiver?.resolve(),
            parameters = sourceFun.parameters.map { p ->
                MapperMapSetFunParameter(
                    name = p.name?.asString(),
                    type = p.type.resolve()
                )
            },
            returns = sourceFun.returnType?.resolve()
        ),
        mapArgs = mapArgs,
        sources = sources,
        maps = maps,
    ) {
        this.sourceFun = sourceFun
    }

    lateinit var targetClassDeclaration: KSClassDeclaration
    lateinit var target: MapTarget
    var sourceFun: KSFunctionDeclaration? = null
        private set

    val ignoreTargets = mapArgs
        .filter { it.ignore }
        .mapTo(mutableSetOf()) { it.target }

    fun emit(writer: MapperWriter) {
        val funName = func.name
        val funBuilder = FunSpec.builder(func.name)
        funBuilder.addModifiers(KModifier.OVERRIDE)
        // parameters
        func.receiver?.also { funBuilder.receiver(it.toTypeName()) }
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
    }


}
