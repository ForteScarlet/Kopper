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

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import love.forte.kopper.processor.def.MapperActionDef
import java.util.LinkedList

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
    val def: MapperActionDef,
    val generator: MapperActionGenerator,
) {
    var funBuilder: FunSpec.Builder = FunSpec.builder(def.name)

    val maps: LinkedList<MapperMap> = LinkedList()

    lateinit var targetClassDeclaration: KSClassDeclaration
    lateinit var target: MapActionTarget
    var subMapperSets = mutableListOf<MapperAction>()

    fun prepare() {
        generator.actions.add(this)
        // 定义此函数
        resolveCurrentFunDeclaration()
        // 预处理所有的 map，分离出可能存在的 sub action 并同样注册、prepare
        prepareMaps()
    }

    fun generate() {

        TODO("generate from MapperAction")

    }

    private fun resolveCurrentFunDeclaration() {
        val sourceFun = def.sourceFun
        if (sourceFun != null) {
            funBuilder.addModifiers(KModifier.OVERRIDE)
            // parameters,
            // parameters
            sourceFun.extensionReceiver?.also { funBuilder.receiver(it.toTypeName()) }
            sourceFun.parameters.forEach { funBuilder.addParameter(it.name!!.asString(), it.type.toTypeName()) }
            // return
            sourceFun.returnType?.toTypeName()?.also { funBuilder.returns(it) }
        } else {
            funBuilder.addModifiers(KModifier.PRIVATE)
            // main first, source after,
            // target last
            def.sources.sortedByDescending { it.isMain }.forEach { sourceDef ->
                funBuilder.addParameter(
                    sourceDef.incoming.name ?: "_main_this",
                    type = sourceDef.incoming.type.toTypeName(),
                )
            }

            if (def.target.incoming != null) {
                funBuilder.addParameter(
                    name = def.target.incoming.name ?: "_target_this",
                    type = def.target.incoming.type.toTypeName(),
                )
            }

            if (def.target.returns) {
                funBuilder.returns(def.target.declaration.toClassName())
            }
        }
    }

    private fun prepareMaps() {
        val mapArgsWithTargetPath = def.mapArgs.toMutableList().associateBy { it.target.toPath() }

        val rootTargets = mapArgsWithTargetPath.filterTo(mutableMapOf()) { (k, _) -> !k.hasChild() }
        val deepTargets = mapArgsWithTargetPath.filterTo(mutableMapOf()) { (k, _) -> k.hasChild() }

        for ((path, mapArg) in rootTargets) {
            // 获取 target 属性
            // 检测类型，如果是 deep 类型，添加到 deepTargets 并跳过
            // 检测 target 是可变属性或require

        }

        // TODO
        // 直接单属性映射, 以 target 为准
        // 剩余的 target 从 main 中找

        // 再剩下有层级的、或类型不是可以直接转化的，将它们处理为sub action,
        // 先寻找现有的、可以完全匹配的，
        //   完全匹配，指那个
        // 找不到就请求创建一个新的


    }

    // val setWriter = writer.newMapSetWriter(funBuilder)

    // init target and emit maps.
    // target.emitInit(setWriter)

    // target.emitInitBegin(setWriter)
    // var finished = false
    //
    // maps.sortedByDescending { it is ConstructorMapperMap }
    //     .forEachIndexed { index, map ->
    //         if (!finished && map !is ConstructorMapperMap) {
    //             target.emitInitFinish(setWriter)
    //             finished = true
    //         }
    //         map.emit(setWriter, index)
    //     }
    //
    // if (!finished) {
    //     target.emitInitFinish(setWriter)
    // }
    //
    // // do return
    // if (func.returns != null && func.returns != resolver.builtIns.unitType) {
    //     setWriter.addStatement("return %L", target.name)
    // }
    //
    // val key = MapperMapSetKey(
    //     name = funName,
    //     target = target,
    //     sources = sources.toSet()
    // )
    //
    // val info = MapperMapSetInfo(
    //     funSpec = funBuilder,
    //     isAncillary = false
    // )
    //
    // writer.add(key, info)
    //
    // // subs
    // subMapperSets.forEach { it.emit(writer) }

    override fun toString(): String {
        return "MapperAction(def=$def)"
    }


}
