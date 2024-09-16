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

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import love.forte.kopper.annotation.Map
import love.forte.kopper.annotation.PropertyType
import love.forte.kopper.processor.util.asClassDeclaration
import love.forte.kopper.processor.util.hasAnno
import love.forte.kopper.processor.util.isMappableStructType

internal fun resolveToMapper(
    environment: SymbolProcessorEnvironment,
    resolver: Resolver,
    declaration: KSClassDeclaration
): Mapper {
    val originFiles = mutableListOf<KSFile>()
    declaration.containingFile?.also { originFiles.add(it) }

    val mapperAnnoType = resolver.getClassDeclarationByName<love.forte.kopper.annotation.Mapper>()
        ?: error("Cannot find Mapper annotation declaration.")

    val mapAnnoType = resolver.getClassDeclarationByName<Map>()
        ?: error("Cannot find Map annotation declaration.")

    val mapperAnnotation = declaration.annotations.first {
        mapperAnnoType.asStarProjectedType().isAssignableFrom(it.annotationType.resolve())
    }

    val mapperArgs = mapperAnnotation.resolveMapperArgs()
    val mapperName = mapperArgs.targetName { declaration.simpleName.asString() }

    val mapSet = resolveMapSets(
        originFiles = originFiles,
        environment = environment,
        resolver = resolver,
        declaration = declaration,
        mapAnnoType = mapAnnoType
    )

    return Mapper(
        environment = environment,
        resolver = resolver,
        targetName = mapperName,
        targetPackage = mapperArgs.packageName,
        mapSets = mapSet,
        superType = declaration,
        genTarget = mapperArgs.genTarget,
        genVisibility = mapperArgs.visibility,
        originatingKSFiles = originFiles,
    )
}

internal fun resolveMapSets(
    originFiles: MutableList<KSFile>,
    environment: SymbolProcessorEnvironment,
    resolver: Resolver,
    declaration: KSClassDeclaration,
    mapAnnoType: KSClassDeclaration,
): MutableList<MapperMapSet> {
    val abstractFunctions = declaration.getAllFunctions()
        .filter { it.isAbstract }
        .toList()

    return abstractFunctions
        .mapTo(mutableListOf()) { mapFun ->
            val mapSet = mapFun.resolveToMapSet(
                originFiles,
                environment,
                resolver,
                mapAnnoType,
            )

            mapSet
        }

}

internal fun KSFunctionDeclaration.resolveToMapSet(
    originFiles: MutableList<KSFile>,
    environment: SymbolProcessorEnvironment,
    resolver: Resolver,
    mapAnnoType: KSClassDeclaration,
): MapperMapSet {
    val mapArgList = annotations
        .filter {
            mapAnnoType.asStarProjectedType().isAssignableFrom(it.annotationType.resolve())
        }
        .map { it.resolveToMapArgs() }
        .toList()

    val mapSet = MapperMapSet(
        environment = environment,
        resolver = resolver,
        sourceFun = this,
        mapArgs = mapArgList,
    )

    // resolve targets
    val mapTargetType = resolver.getClassDeclarationByName<Map.Target>()
        ?: error("Cannot find Map.Target annotation declaration.")

    mapSet.resolveSources(this, mapTargetType)

    val targetArgs = mapSet.mapArgs.associateByTo(mutableMapOf()) { it.target }

    mapSet.resourceTargets(
        sourceFun = this,
        mapTargetType = mapTargetType,
        prefixPath = null,
        targetArgs = targetArgs,
    )

    // resolve maps
    mapSet.resolveMaps()

    // resolve originFiles
    resolveOriginFiles(this, originFiles)

    return mapSet
}

internal fun resolveOriginFiles(sourceFun: KSFunctionDeclaration, originFiles: MutableList<KSFile>) {
    fun KSNode.doAdd() {
        containingFile?.also(originFiles::add)
    }

    sourceFun.extensionReceiver?.doAdd()
    sourceFun.parameters.forEach { it.doAdd() }
    sourceFun.returnType?.doAdd()
}

/**
 * 根据 target 类型拥有的属性集，
 * 解析出要转化的所有目标属性，以及它所需的 source。
 *
 *
 */
internal fun MapperMapSet.resourceTargets(
    sourceFun: KSFunctionDeclaration,
    mapTargetType: KSClassDeclaration,
    prefixPath: PropertyPath? = null,
    targetArgs: MutableMap<String, MapArgs>,
) {
    var receiver: KSType? = null
    var parameter: KSValueParameter? = null

    val targetType: KSType =
        sourceFun.extensionReceiver
            ?.takeIf { it.hasAnno(mapTargetType.asStarProjectedType()) }
            ?.resolve()
            ?.also { receiver = it }
            ?: sourceFun.parameters
                .firstOrNull {
                    it.hasAnno(mapTargetType.asStarProjectedType())
                }
                ?.also { parameter = it }
                ?.type?.resolve()
            ?: sourceFun.returnType?.resolve()
            ?: error("No target type found")

    val targetClassDeclaration =
        targetType.declaration.asClassDeclaration()
            ?: error("Target type must be a class type.")

    this.targetClassDeclaration = targetClassDeclaration

    val targetMap = targetClassDeclaration.getAllProperties()
        .associateTo(mutableMapOf()) { property ->
            val name = property.simpleName.asString()
            val path =
                if (prefixPath == null) name.toPropertyPath() else prefixPath + name.toPropertyPath() // "$prefixPath.$name"

            val targetArg = targetArgs[path.paths]

            // TODO target source property,
            //  如果目标是某个结构化对象，
            //  使用一个伪装的 fun 包装此 property

            val isMappableStructType = property.type
                .resolve()
                .declaration
                .isMappableStructType(resolver.builtIns)

            // 是结构化目标，且没有eval，

            val targetSourceProperty: MapSourceProperty = if (targetArg != null) {
                // 有明確指定的 @Map 目标，用 arg.source
                val argSourceName = targetArg.sourceName
                val targetSource =
                    if (argSourceName.isEmpty()) sources.first { it.isMain }
                    else sources.first { it.name == argSourceName }

                // 如果eval有效, 构建 eval property
                if (targetArg.isEvalValid) {
                    EvalSourceProperty(
                        name = "${name}_eval",
                        source = this.sources.first { it.isMain },
                        nullable = targetArg.evalNullable,
                        eval = targetArg.eval,
                    )
                } else {

                    if (isMappableStructType) {
                        // TODO 是结构化的
                        //  伪装结构化 property，或者说构建一个基于内部 MapperMapSet 的 property
                    }

                    targetSource.property(targetArg.source.toPropertyPath(), targetArg.sourceType)
                        ?: error("Source property ${targetArg.source} for target property [$path] is not found.")
                }
            } else {
                if (isMappableStructType) {
                    // TODO 是结构化的
                    //  伪装结构化 property，或者说构建一个基于内部 MapperMapSet 的 property
                }

                // 没有，去 source 找同名同路径的
                sources.sortedByDescending { it.isMain }.firstNotNullOfOrNull { mapSource ->
                    mapSource.property(path, PropertyType.AUTO)
                } ?: error("Source property for target property [$path] is not found.")
            }

            name.toPropertyPath() to targetSourceProperty
        }

    val receiver0 = receiver
    val parameter0 = parameter

    val mapTarget = when {
        // is receiver
        receiver0 != null -> {
            MapTarget.create(
                mapSet = this,
                receiver = receiver0
            )
        }

        parameter0 != null -> {
            MapTarget.create(
                mapSet = this,
                parameter = parameter0,
                type = parameter0.type.resolve()
            )
        }

        else -> {
            // return type
            val returnType = sourceFun.returnType!!.resolve()
            MapTarget.create(
                mapSet = this,
                type = returnType,
                targetSourceMap = targetMap
            )
        }
    }

    this.target = mapTarget
    this.target.targetSourceMap.putAll(targetMap)
}

internal fun MapperMapSet.resolveSources(
    sourceFun: KSFunctionDeclaration,
    mapTargetType: KSClassDeclaration,
) {
    // 整理所有的 MapSource, 通过 receiver 或 parameter
    sourceFun.extensionReceiver
        // not target
        ?.takeUnless { it.hasAnno(mapTargetType.asStarProjectedType()) }
        ?.resolve()
        ?.also {
            sources.add(
                MapSource(
                    sourceMapSet = this,
                    name = "this",
                    type = it
                )
            )
        }

    for (parameter in sourceFun.parameters) {
        // not target
        if (!parameter.hasAnno(mapTargetType.asStarProjectedType())) {
            sources.add(
                MapSource(
                    sourceMapSet = this,
                    name = parameter.name?.asString() ?: "?ERROR",
                    type = parameter.type.resolve()
                )
            )
        }
    }

    sources.firstOrNull()?.isMain = true
}

/**
 * 先进行 [resourceTargets] 和 [resolveSources]
 *
 */
internal fun MapperMapSet.resolveMaps() {
    data class PathPropertyEntry(
        val path: PropertyPath,
        val property: MapSourceProperty,
    )

    val topLevelTargets = mutableMapOf<String, PathPropertyEntry>()
    // Key is the root path name.
    val childExistsTargets = mutableMapOf<String, MutableList<PathPropertyEntry>>()

    // 区分：根属性、只有一个但是包含子元素的和带有多个子元素的
    target.targetSourceMap.forEach { (path, property) ->
        val rootName = path.name
        if (!path.hasChild()) {
            if (rootName !in childExistsTargets) {
                topLevelTargets[rootName] = PathPropertyEntry(path, property)
            }
        } else {
            childExistsTargets.computeIfAbsent(rootName) { mutableListOf() }
                .add(PathPropertyEntry(path, property))

            // 不再在top-level中
            topLevelTargets.remove(rootName)
        }
    }

    // 处理顶层、只有一个元素的。
    for ((target, entry) in topLevelTargets) {
        val (path, property) = entry
        resolveSingleTopTarget(target, path, property)
    }

    // TODO 处理多层级目标?

}

private fun MapperMapSet.resolveSingleTopTarget(
    target: String,
    path: PropertyPath,
    property: MapSourceProperty
) {
    // ignore, eval, etc.
    val mapArgs = mapArgs.find { it.target == target } // single target.
    if (mapArgs?.ignore == true) {
        environment.logger.info("Target $target in $this is ignore.")
    }

    val targetProperty = this.target.property(path.name)
        ?: error("unknown target $target from ${this.target}")

    val map = if (mapArgs?.isEvalValid == true) {
        val eval = mapArgs.eval
        val evalNullable = mapArgs.evalNullable
        EvalPropertyMapperMap(
            eval = eval,
            evalNullable = evalNullable,
            target = this.target,
            targetProperty = targetProperty
        )
    } else {
        SourcePropertyMapperMap(
            source = property.source,
            sourceProperty = property,
            target = this.target,
            targetProperty = targetProperty
        )
    }

    this.maps.add(map)
}
