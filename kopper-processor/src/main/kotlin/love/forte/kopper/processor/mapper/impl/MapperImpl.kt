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
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import love.forte.kopper.annotation.Map
import love.forte.kopper.annotation.PropertyType
import love.forte.kopper.processor.mapper.*
import love.forte.kopper.processor.util.asClassDeclaration
import love.forte.kopper.processor.util.findPropOrConstructorProperty
import love.forte.kopper.processor.util.hasAnno

internal fun resolveToMapper(
    environment: SymbolProcessorEnvironment,
    resolver: Resolver,
    declaration: KSClassDeclaration
): Mapper {
    val mapperAnnoType = resolver.getClassDeclarationByName<love.forte.kopper.annotation.Mapper>()
        ?: error("Cannot find Mapper annotation declaration.")

    val mapAnnoType = resolver.getClassDeclarationByName<Map>()
        ?: error("Cannot find Map annotation declaration.")

    val mapperAnnotation = declaration.annotations.first {
        mapperAnnoType.asStarProjectedType().isAssignableFrom(it.annotationType.resolve())
    }

    val mapperArgs = mapperAnnotation.resolveMapperArgs()
    val mapperName = mapperArgs.targetName { declaration.simpleName.asString() }
    //
    // val mappers = mutableListOf<FunSpec.Builder>()
    // val mapperBuilder = MapperBuilderImpl(mappers)

    val mapSet = resolveMapSets(
        environment = environment,
        resolver = resolver,
        declaration = declaration,
        mapAnnoType = mapAnnoType
    )

    return Mapper(
        targetName = mapperName,
        targetPackage = mapperArgs.packageName,
        mapSets = mapSet,
        superType = declaration,
        genTarget = mapperArgs.genTarget,
        genVisibility = mapperArgs.visibility
    )
}

internal fun resolveMapSets(
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
                environment,
                resolver,
                mapAnnoType,
            )

            // val mapSet = MapperMapSet(
            //
            //     target = null,
            //     mapArgs = mapArgList,
            // )
            //
            // mapFun.resolveMapperMapFromMapAno(
            //     resolver = resolver,
            //     environment = environment,
            //     mapSet = mapSet,
            //     container = declaration,
            //     mapAnnoType = mapAnnoType
            // )

            mapSet
        }

}

internal fun KSFunctionDeclaration.resolveToMapSet(
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
        prefixPath = "",
        targetArgs = targetArgs
    )

    // resolve maps
    mapSet.resolveMaps()

    return mapSet
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
    prefixPath: String = "",
    targetArgs: MutableMap<String, MapArgs>,
) {
    var receiver: KSType? = null
    var parameter: KSValueParameter? = null

    val targetType: KSType =
        sourceFun.extensionReceiver
            ?.takeIf { it.hasAnno(mapTargetType.asStarProjectedType()) }
            ?.resolve()?.also { receiver = it }
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
            val path = if (prefixPath.isEmpty()) name else "$prefixPath.$name"

            val targetArg = targetArgs.remove(path)

            val targetSourceProperty: MapSourceProperty = if (targetArg != null) {
                // 有明確指定的 @Map 目标，用 arg.source
                val argSourceName = targetArg.sourceName
                val targetSource =
                    if (argSourceName.isEmpty()) sources.first { it.isMain }
                    else sources.first { it.name == argSourceName }

                targetSource.property(targetArg.source, targetArg.sourceType)
                    ?: error("Source property ${targetArg.source} for target property [$path] is not found.")
            } else {
                // 没有，去 source 找同名同路径的
                sources.sortedByDescending { it.isMain }.firstNotNullOfOrNull { mapSource ->
                    mapSource.property(path, PropertyType.AUTO)
                } ?: error("Source property for target property [$path] is not found.")
            }

            name to targetSourceProperty
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
    for ((target, property) in target.targetSourceMap) {
        val targetProperty = this.target.property(target)
            ?: error("unknown target $target from ${this.target}")

        val map = PropertyMapperMap(
            source = property.source,
            sourceProperty = property,
            target = this.target,
            targetProperty = targetProperty
        )

        this.maps.add(map)
    }
}
