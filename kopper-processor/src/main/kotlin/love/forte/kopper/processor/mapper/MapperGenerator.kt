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
import com.google.devtools.ksp.symbol.KSNode
import com.squareup.kotlinpoet.FileSpec
import love.forte.kopper.processor.def.*


/**
 *
 * @author ForteScarlet
 */
internal class MapperGenerator(
    val environment: SymbolProcessorEnvironment,
    val resolver: Resolver,
    val defs: List<MapperDef>,
) {
    data class FileWithDef(val file: FileSpec.Builder, val def: MapperDef)

    var files: MutableList<FileWithDef> = mutableListOf()

    fun addFile(def: MapperDef, file: FileSpec.Builder) {
        files.add(FileWithDef(file, def))
    }

    fun generate() {
        for (def in defs) {
            Mapper(def = def, generator = this).generate()
        }
    }
}


internal class MapperActionsGenerator(
    val environment: SymbolProcessorEnvironment,
    val resolver: Resolver,
    val mapperGenerator: MapperGenerator,
) {
    private var additionalIndex = 0

    // actions, with funSpec.Builder
    val buffer: MutableList<MapperAction> = mutableListOf()
    val actions: MutableList<MapperAction> = mutableListOf()

    fun addAction(action: MapperAction) {
        buffer.add(action)
    }

    /**
     * 请求或创建一个对应 sources 和 target 的 MapperActionDef,
     * 始终需要returns，始终不需要参数
     */
    fun requestAction(
        sources: Collection<MapperActionSourceDef>,
        target: MapperActionTargetDef,
        name: String,
        mapArgs: List<MapArgs>,
        node: KSNode?,
        sourcePrefix: String?,
    ): MapperAction {
        val expectArgs = mapArgs.sortedBy { it.target }

        val allSequence = actions.asSequence() + buffer.asSequence()

        val foundAction = allSequence.find { action ->
            if (action.defaultSourcePrefix != sourcePrefix) {
                return@find false
            }

            // mapArgs 相同
            val actionArgs = action.def.mapArgs.sortedBy { it.target }

            if (actionArgs.size != expectArgs.size) {
                return@find false
            }

            repeat(expectArgs.size) {
                if (actionArgs[it] != expectArgs[it]) {
                    return@find false
                }
            }

            // 要有返回值
            if (!action.def.target.returns) {
                return@find false
            }

            // 不要target参数
            if (action.def.target.incoming != null) {
                return@find false
            }
            // 如果目标返回值可null，但是预期不能null
            if (action.def.target.nullable && !target.nullable) {
                return@find false
            }

            // 目标的target的类型相同，或至少目标的类型结果是预期类型的子类型
            val targetType = target.declaration.asStarProjectedType()
            val actionType = action.def.target.declaration.asStarProjectedType()

            if (
                action.def.target.declaration != target.declaration
                && !targetType.isAssignableFrom(actionType)
            ) {
                return@find false
            }

            // 所有的sources相同，类型、nullable相同
            val actionSources = action.def.sources

            sl@ for (expectSource in sources) {
                val expectType = expectSource.incoming.type
                for (actionSource in actionSources) {
                    // checks
                    // 如果入参不能为null，但是预期的可以为null，跳过
                    if (!actionSource.incoming.nullable && expectSource.incoming.nullable) {
                        continue
                    }

                    // 入参类型要匹配
                    // 类型相等，或者预期类型是入参类型的子类
                    if (!actionSource.incoming.type.isAssignableFrom(expectType)) {
                        continue
                    }

                    // 有匹配的，移除匹配的目标，结束本次对 expect 的匹配
                    continue@sl
                }

                // 没有被跳过，存在不匹配的
                return@find false
            }

            true
        }

        environment.logger.info("""
            found action: $foundAction
            from sources: $sources
            for target:   $target
            or name:      $name ($additionalIndex)
            with args:    $mapArgs
        """.trimIndent())

        if (foundAction != null) return foundAction

        val def = MapperActionDef(
            environment = environment,
            resolver = resolver,
            target = target,
            name = name + "_" + (additionalIndex++),
            mapArgs = mapArgs,
            sourceFun = null,
            sources = sources.toList(),
            node = node
        )

        return MapperAction(
            def = def,
            generator = this,
            defaultSourcePrefix = sourcePrefix,
        ).also { addAction(it) }
    }
}
