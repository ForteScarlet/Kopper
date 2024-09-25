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
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import love.forte.kopper.processor.def.MapArgs
import love.forte.kopper.processor.def.MapperActionDef
import love.forte.kopper.processor.def.MapperActionTargetDef
import love.forte.kopper.processor.def.ModifiablePropertyDef
import love.forte.kopper.processor.util.asClassDeclaration
import love.forte.kopper.processor.util.isMappableStructType
import love.forte.kopper.processor.util.isNullable
import java.util.*

// internal data class MapperMapSetFunInfo(
//     val name: String,
//     val receiver: MapperMapSetFunReceiver?,
//     val parameters: List<MapperMapSetFunParameter>,
//     val returns: KSType?
// )
//
// internal data class MapperMapSetFunReceiver(
//     val type: KSType,
//     val parameterType: MapperMapSetFunParameterType
// )
//
// internal data class MapperMapSetFunParameter(
//     val name: String?,
//     val type: KSType,
//     val parameterType: MapperMapSetFunParameterType,
// )
//
// internal enum class MapperMapSetFunParameterType {
//     SOURCE, TARGET
// }

/**
 * A set of `Map`s in a Mapper.
 *
 * @author ForteScarlet
 */
internal class MapperAction internal constructor(
    val def: MapperActionDef,
    val generator: MapperActionsGenerator,
    // TODO 如果是 sub action，遇到默认的 source 属性时，
    //  是否应该为其配置前缀？
    val defaultSourcePrefix: String? = null,
) {
    var prepared = false
        private set

    var funBuilder: FunSpec.Builder = FunSpec.builder(def.name)

    val statements: LinkedList<MapperActionStatement> = LinkedList()

    var target: MapperActionTarget = MapperActionTarget(def.target, generator)

    fun prepare() {
        // 定义此函数
        resolveCurrentFunDeclaration()
        // 预处理所有的 map，分离出可能存在的 sub action 并同样注册、prepare
        prepareMaps()
        prepared = true
    }

    fun generate() {
        val codeBuilder = CodeBlock.builder()
        for (statement in statements) {
            codeBuilder.add("«")
            statement.emit(codeBuilder)
            codeBuilder.add("\n»")
        }
        // return target
        if (target.def.returns) {
            codeBuilder.addStatement("return %L", target.name)
        }

        funBuilder.addCode(codeBuilder.build())
    }

    private fun resolveCurrentFunDeclaration() {
        val sourceFun = def.sourceFun
        if (sourceFun != null) {
            funBuilder.addModifiers(KModifier.OVERRIDE)
            // parameters
            sourceFun.extensionReceiver?.also { funBuilder.receiver(it.toTypeName()) }
            sourceFun.parameters.forEach { funBuilder.addParameter(it.name!!.asString(), it.type.toTypeName()) }
            // return
            sourceFun.returnType?.toTypeName()?.also { funBuilder.returns(it) }
        } else {
            // If there have no source fun, should not have receiver either,
            funBuilder.addModifiers(KModifier.PRIVATE)
            // main first,
            // sources after,
            // target last
            def.sources.sortedByDescending { it.isMain }.forEach { sourceDef ->
                funBuilder.addParameter(
                    sourceDef.incoming.name!!,
                    type = sourceDef.incoming.type.toTypeName(),
                )
            }

            if (def.target.incoming != null) {
                funBuilder.addParameter(
                    name = def.target.incoming.name!!,
                    type = def.target.incoming.type.toTypeName(),
                )
            }

            if (def.target.returns) {
                funBuilder.returns(def.target.declaration.toClassName())
            }
        }
    }

    private data class ArgsAndProp(val args: MapArgs?, val prop: MapActionTargetProperty?)

    private fun prepareMaps() {
        val mapArgsWithTargetPath = def.mapArgs.toMutableList().associateBy { it.target.toPath() }

        val rootTargets: MutableMap<Path, ArgsAndProp> =
            mapArgsWithTargetPath
                .filter { (k, _) -> !k.hasChild() }
                .mapValuesTo(mutableMapOf()) { (_, arg) ->
                    ArgsAndProp(arg, null)
                }

        val deepTargets = mapArgsWithTargetPath.filterTo(mutableMapOf()) { (k, _) -> k.hasChild() }

        val requiredStatements = mutableListOf<MapperActionStatement>()
        val normalStatements = mutableListOf<MapperActionStatement>()

        // 追加普通的构造和属性

        target.def.requires?.forEach { r ->
            val p = r.name.toPath()
            if (p !in rootTargets) {
                rootTargets[p] = ArgsAndProp(
                    args = null,
                    prop = MapActionTargetPropertyImpl(
                        target = target,
                        def = r
                    )
                )
            }
        }

        // other properties.
        target.def.declaration
            .getAllProperties()
            .filter { it.simpleName.asString().toPath() !in rootTargets }
            .forEach { prop ->
                val name = prop.simpleName.asString()
                val type = prop.type.resolve()
                rootTargets[name.toPath()] = ArgsAndProp(
                    args = null,
                    prop = MapActionTargetPropertyImpl(
                        target = target,
                        def = ModifiablePropertyDef(
                            environment = def.environment,
                            resolver = def.resolver,
                            name = name,
                            declaration = type.declaration,
                            nullable = type.nullability.isNullable
                        )
                    )
                )
            }

        // 从 mapArgs 中存在的寻找
        for ((path, argAndProp) in rootTargets) {
            resolvePath(
                path,
                argAndProp.args,
                argAndProp.prop,
                deepTargets,
                requiredStatements,
                normalStatements
            )
        }

        // 查询剩余的、requires 和 modifiable properties


        // required statements 聚合到 首位，
        val mergedRequirementStatement = RequiredInitialActionStatement(
            target = target,
            requiredStatements = requiredStatements,
        )

        this.statements.add(mergedRequirementStatement)
        this.statements.addAll(normalStatements)
    }


    private fun resolvePath(
        path: Path,
        mapArgs: MapArgs?,
        prop: MapActionTargetProperty?,
        deepTargets: MutableMap<Path, MapArgs>,
        requiredStatements: MutableList<MapperActionStatement>,
        normalStatements: MutableList<MapperActionStatement>,
    ) {
        val name = path.name
        val targetProperty = prop
            ?: target.property(name)
            ?: error("Unknown target $path in $target")

        if (mapArgs?.isEvalValid == true) {
            val eval = EvalMapperActionStatement(
                eval = mapArgs.eval,
                evalNullable = mapArgs.evalNullable,
                property = targetProperty
            )

            if (targetProperty.def.isRequired) {
                requiredStatements.add(eval)
            } else {
                normalStatements.add(eval)
            }

            return
        }

        val isMappableType = targetProperty.def.declaration.isMappableStructType(def.resolver.builtIns)
        if (isMappableType) {
            // 可以产生子映射的结构体类型
            // find all sub args
            val subArgs = mutableListOf<MapArgs>()
            val iter = deepTargets.entries.iterator()
            while (iter.hasNext()) {
                val (key, value) = iter.next()
                if (key.name == name) {
                    val newValue = value.copy(target = key.child!!.paths)
                    subArgs.add(newValue)
                }
                iter.remove()
            }

            // TODO target 降级后，source 并未降级

            // 申请一个所需的 target 和 sources
            val expectSourcesForIncoming = subArgs.mapIndexedTo(LinkedList()) { i, arg ->
                val sourceName = arg.sourceName
                val source = if (sourceName.isBlank()) {
                    // main
                    def.sources.find { it.isMain }
                } else {
                    def.sources.find { (it.incoming.name ?: "this") == sourceName }
                } ?: error("Unknown source name $sourceName")

                // if (source.incoming.name == null) {
                //     source.copy(incoming = source.incoming.copy(name = "__s_$i"))
                // } else {
                source
                // }
            }



            if (expectSourcesForIncoming.isEmpty()) {
                expectSourcesForIncoming.add(def.sources.find { it.isMain }!!)
            } else if (expectSourcesForIncoming.none { it.isMain }) {
                // 没有main，取第一个作为 main
                expectSourcesForIncoming.addFirst(
                    expectSourcesForIncoming.removeFirst().copy(isMain = true)
                )
            }

            val expectSources = expectSourcesForIncoming.mapIndexed { i, s ->
                // 没有入参名字的，给个名字
                // 一般来讲就一个没有的
                if (s.incoming.name == null) {
                    s.copy(incoming = s.incoming.copy(name = "__s_$i"))
                } else {
                    s
                }
            }

            val expectTarget = MapperActionTargetDef(
                environment = def.environment,
                resolver = def.resolver,
                declaration = targetProperty.def.declaration.asClassDeclaration()!!, // TODO as class decl?
                incoming = null,
                returns = true,
                nullable = targetProperty.def.nullable,
            )

            val subAction = this.generator.requestAction(
                sources = expectSources,
                target = expectTarget,
                name = "to${expectTarget.declaration.simpleName.asString()}",
                mapArgs = subArgs,
                node = def.node,
                sourcePrefix = defaultSourcePrefix?.let { "$it.$name" } ?: name,
            )

            // 引用这个 sub action, 得到它的返回值。
            val statement = SubActionActionStatement(
                subAction = subAction,
                sources = expectSourcesForIncoming.map { MapperActionSource(action = this, def = it) },
                targetProperty = targetProperty,
            )

            if (targetProperty.def.isRequired) {
                requiredStatements.add(statement)
            } else {
                normalStatements.add(statement)
            }
        } else {
            // 普通类型，尝试直接赋值 的 statement
            val sourceDef = if (mapArgs?.sourceName?.isNotBlank() == true) {
                def.sources.find { it.incoming.name == mapArgs.sourceName }
            } else {
                def.sources.find { it.isMain }
            } ?: run {
                val msg =
                    "Source in ${mapArgs?.sourceName?.ifBlank { "<main>" } ?: "<main>"} for property ${targetProperty.def} not found. arg: $mapArgs"
                def.environment.logger.error(msg, def.node)
                error(msg)
            }

            val source = MapperActionSource(this, sourceDef)

            val sourcePropertyPath = mapArgs?.source?.takeUnless { it.isBlank() }?.toPath()
                ?: defaultSourcePrefix?.let { it.toPath() + path } ?: path

            val sourceProperty = source.property(sourcePropertyPath)
                ?: run {
                    val msg =
                        "Source property ${defaultSourcePrefix}.${sourcePropertyPath.paths} in ${source.def} not found, arg: $mapArgs"
                    def.environment.logger.error(msg, def.sourceFun ?: sourceDef.declaration)
                    error(msg)
                }

            val mapperStatement = FromSourceMapperActionStatement(
                resolver = def.resolver,
                sourceProperty = sourceProperty,
                targetProperty = targetProperty,
            )

            if (targetProperty.def.isRequired) {
                requiredStatements.add(mapperStatement)
            } else {
                normalStatements.add(mapperStatement)
            }
        }
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
