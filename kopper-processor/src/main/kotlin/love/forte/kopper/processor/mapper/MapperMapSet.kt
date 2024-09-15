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
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * A set of `Map`s in a Mapper.
 *
 * @author ForteScarlet
 */
internal class MapperMapSet internal constructor(
    val environment: SymbolProcessorEnvironment,
    val resolver: Resolver,
    val sourceFun: KSFunctionDeclaration,
    val mapArgs: List<MapArgs>,
    val sources: MutableList<MapSource> = mutableListOf(),
    val maps: MutableList<MapperMap> = mutableListOf(),
) {
    lateinit var targetClassDeclaration: KSClassDeclaration
    lateinit var target: MapTarget

    val ignoreTargets = mapArgs
        .filter { it.ignore }
        .mapTo(mutableSetOf()) { it.target }

    fun emit(writer: MapperWriter) {
        val funName = sourceFun.simpleName.asString()
        val funBuilder = FunSpec.builder(sourceFun.simpleName.asString())
        funBuilder.addModifiers(KModifier.OVERRIDE)
        // parameters
        sourceFun.extensionReceiver?.also { funBuilder.receiver(it.toTypeName()) }
        sourceFun.parameters.forEach { funBuilder.addParameter(it.name!!.asString(), it.type.toTypeName()) }
        // return
        sourceFun.returnType?.toTypeName()?.also { funBuilder.returns(it) }

        val setWriter = writer.newMapSetWriter(funBuilder)

        // init target.
        target.emitInit(setWriter)

        // emit maps
        for ((index, map) in maps.withIndex()) {
            map.emit(setWriter, index)
        }

        // do return
        if (sourceFun.returnType != null && sourceFun.returnType?.resolve() != resolver.builtIns.unitType) {
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
