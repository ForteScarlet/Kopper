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
import java.util.*

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
    parentProperty: MapSourceProperty? = null,
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
        parentProperty = parentProperty,
    )

    // resolve targets
    val mapTargetAnnoType = resolver.getClassDeclarationByName<Map.Target>()
        ?: error("Cannot find Map.Target annotation declaration.")

    mapSet.resolveSources(mapTargetAnnoType)

    mapSet.initial(mapTargetAnnoType)

    // resolve originFiles
    resolveOriginFiles(this, originFiles)

    return mapSet
}


internal fun resolveMapSet(
    environment: SymbolProcessorEnvironment,
    resolver: Resolver,
    mapArgList: List<MapArgs>,
    func: MapperMapSetFunInfo,
    sources: List<MapSource>,
    parentProperty: MapSourceProperty? = null,
    prefixPath: Path? = null,
): MapperMapSet {
    val mapSet = MapperMapSet(
        environment = environment,
        resolver = resolver,
        func = func,
        mapArgs = mapArgList,
        parentProperty = parentProperty,
    )

    mapSet.sources.addAll(sources)

    // resolve targets
    val mapTargetAnnoType = resolver.getClassDeclarationByName<Map.Target>()
        ?: error("Cannot find Map.Target annotation declaration.")

    mapSet.initial(mapTargetAnnoType, prefixPath = prefixPath)

    return mapSet
}

internal fun MapperMapSet.initial(
    mapTargetAnnoType: KSClassDeclaration,
    prefixPath: Path? = null,
) {
    val targetArgs = mapArgs.associateByTo(mutableMapOf()) { it.target }

    resourceTargets(
        prefixPath = prefixPath,
        targetArgs = targetArgs,
    )

    // resolve maps
    resolveMaps()
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
    prefixPath: Path? = null,
    targetArgs: MutableMap<String, MapArgs>,
) {
    val receiver: KSType? = func.receiver?.takeIf { it.isTarget }?.type
    val parameter: MapperMapSetFunParameter? = func.parameters.firstOrNull { it.isTarget }
    val returns = func.returns

    val targetType: KSType =
        receiver ?: parameter?.type ?: returns
        ?: error("No target type found in set $this")

    val targetClassDeclaration =
        targetType.declaration.asClassDeclaration()
            ?: error("Target type must be a class type.")

    this.targetClassDeclaration = targetClassDeclaration

    val targetMap = mutableMapOf<Path, MapSourceProperty>()

    val mainSource = sources.find { it.isMain }

    for (property in targetClassDeclaration.getAllProperties()) {
        val name = property.simpleName.asString()
        val path =
            if (prefixPath == null) name.toPath() else prefixPath + name.toPath() // "$prefixPath.$name"

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
                if (argSourceName.isBlank()) mainSource!!
                else sources.firstOrNull { it.name == argSourceName }
                    ?: error("arg source name $argSourceName not found in ${sources.map { it.name }}")

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
                    // 是结构化的
                    // 伪装结构化 property，
                    // 或者说构建一个基于内部 MapperMapSet 的 property

                    resolveSubMapSetProperty(
                        mainSource = mainSource,
                        property = property,
                        name = name,
                        path = path,
                        targetArg = null,
                        targetArgs = targetArgs,
                    ) {
                        targetSource.property(targetArg.source.toPath(), targetArg.sourceType)
                            ?: error("Source property ${targetArg.source} for target property [$path] is not found in set $this")

                    }

                } else {
                    targetSource.property(targetArg.source.toPath(), targetArg.sourceType)
                        ?: error(
                            "Source property [${targetArg.source} in ${sources.map { it.name }}] " +
                                "in $targetSource for target property [$path] is not found in set $this"
                        )
                }
            }
        } else {
            if (isMappableStructType) {
                // 是结构化的
                // 伪装结构化 property，
                // 或者说构建一个基于内部 MapperMapSet 的 property

                resolveSubMapSetProperty(
                    mainSource = mainSource,
                    property = property,
                    name = name,
                    path = path,
                    targetArg = null,
                    targetArgs = targetArgs,
                ) {
                    sources.sortedByDescending { it.isMain }.firstNotNullOfOrNull { mapSource ->
                        mapSource.property(path, PropertyType.AUTO)
                    } ?: error("Source property for target property [$path] is not found in set $this")
                }
            } else {
                // 没有，去 source 找同名同路径的
                sources.sortedByDescending { it.isMain }.firstNotNullOfOrNull { mapSource ->
                    mapSource.property(path, PropertyType.AUTO)
                } ?: error("Source property for target property [$path] is not found in set $this")
            }
        }

        targetMap[name.toPath()] = targetSourceProperty
    }

    fun initialRequiredTarget() {

    }

    val mapTarget = when {
        // is receiver
        receiver != null && receiver.nullability == Nullability.NOT_NULL -> {
            MapTarget.create(
                mapSet = this,
                receiver = receiver
            )
        }

        parameter != null && parameter.type.nullability == Nullability.NOT_NULL -> {
            MapTarget.create(
                mapSet = this,
                parameterName = parameter.name!!,
                type = parameter.type
            )
        }

        else -> {
            // return type
            val returnType = func.returns!!
            MapTarget.create(
                mapSet = this,
                type = returnType,
                targetSourceMap = targetMap,
                nullableParameter = when {
                    receiver != null -> "this"
                    parameter != null -> parameter.name
                    else -> null
                }
            )
        }
    }

    this.target = mapTarget
    this.target.targetSourceMap.putAll(targetMap)
}

internal inline fun MapperMapSet.resolveSubMapSetProperty(
    mainSource: MapSource?,
    property: KSPropertyDeclaration,
    name: String,
    path: Path,
    targetArg: MapArgs?,
    targetArgs: MutableMap<String, MapArgs>,
    getTargetReceiverProperty: () -> MapSourceTypedProperty
): InternalMapSetSourceProperty {

    val subTargetNameArgs = mutableMapOf<String, MapArgs>()
    val subSources = mutableSetOf<MapSource>()
    val subFunParameters = mutableListOf<String>()

    for ((key, value) in targetArgs) {
        val newTargetKey = key.substringAfter("$name.")
        val source = if (value.sourceName.isNotBlank()) {
            sources.find { it.name == value.sourceName }
        } else {
            mainSource
        }


        source?.also { s ->
            subFunParameters.add(s.name)
            val newSource = s.copy(
                isMain = false,
                name = if (s.name == "this") "__this" else s.name
            )
            subSources.add(newSource)
        }

        val newArgsValue = value.copy(
            target = newTargetKey,
            sourceName = value.sourceName.ifBlank {
                val mainName = mainSource?.name ?: return@ifBlank ""
                if (mainName == "this") "__this" else mainName
            }
        )

        subTargetNameArgs[newTargetKey] = newArgsValue
    }

    // TODO targetArgs.removeAll ?

    val internalMapSetSources = LinkedList(subSources)

    // 去 source 找同名同路径的
    val targetReceiverProperty = getTargetReceiverProperty()

    val mainPropertySource = MapSource(
        this,
        isMain = true,
        name = "this",
        type = targetReceiverProperty.type,
    )

    internalMapSetSources.addFirst(mainPropertySource)

    val internalMapSet = resolveMapSet(
        environment = environment,
        resolver = resolver,
        mapArgList = subTargetNameArgs.values.toList(),
        func = MapperMapSetFunInfo(
            name = "resolve" +
                targetClassDeclaration.simpleName.asString().replaceFirstChar { it.uppercase() } +
                name.replaceFirstChar { it.uppercase() } +
                "For_" + func.name,
            receiver = MapperMapSetFunReceiver(
                type = mainPropertySource.type,
                parameterType = MapperMapSetFunParameterType.SOURCE
            ),
            // TODO 寻找所有所需的 sources. 是不是需要判断 target 已经存在与否?
            parameters = internalMapSetSources
                .filter { !it.isMain }
                .map { s ->
                    MapperMapSetFunParameter(s.name, s.type, MapperMapSetFunParameterType.SOURCE)
                },
            returns = property.type.resolve(),
        ),
        sources = internalMapSetSources,
        parentProperty = targetReceiverProperty,
    )

    internalMapSetSources.forEach { it.sourceMapSet = internalMapSet }

    this.subMapperSets.add(internalMapSet)

    return InternalMapSetSourceProperty(
        source = mainPropertySource,
        type = property.type.resolve(), // propertySource.type,
        name = internalMapSet.func.name,
        propertyType = PropertyType.FUNCTION,
        subFunName = internalMapSet.func.name,
        receiverProperty = targetReceiverProperty,
        parameters = subFunParameters,
    )

}


internal fun MapperMapSet.resolveSources(
    mapTargetType: KSClassDeclaration,
) {
    // 整理所有的 MapSource, 通过 receiver 或 parameter
    func.receiver
        // not target
        ?.takeUnless { it.isTarget }
        ?.also {
            sources.add(
                MapSource(
                    sourceMapSet = this,
                    name = "this",
                    type = it.type
                )
            )
        }

    for (parameter in func.parameters) {
        // not target
        if (!parameter.type.hasAnno(mapTargetType.asStarProjectedType())) {
            sources.add(
                MapSource(
                    sourceMapSet = this,
                    name = parameter.name ?: "?ERROR",
                    type = parameter.type
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
        val path: Path,
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
    path: Path,
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
