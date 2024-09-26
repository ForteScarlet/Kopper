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

package love.forte.kopper.processor.def

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import love.forte.kopper.annotation.Map
import love.forte.kopper.processor.util.asClassDeclaration
import love.forte.kopper.processor.util.hasAnno
import love.forte.kopper.processor.util.isNullable
import java.util.*


internal class MapperDefResolveContext(
    private val sourceDeclaration: KSClassDeclaration,
    val environment: SymbolProcessorEnvironment,
    val resolver: Resolver,
    mapperAnnotation: KSAnnotation,
) {
    private val mapperAnnoDeclaration = resolver.getClassDeclarationByName<love.forte.kopper.annotation.Mapper>()
        ?: error("Cannot find `Mapper` annotation declaration.")

    val mapperAnnoType = mapperAnnoDeclaration.asStarProjectedType()

    val mapAnnoDeclaration = resolver.getClassDeclarationByName<Map>()
        ?: error("Cannot find `Map` annotation declaration.")

    val mapAnnoType = mapAnnoDeclaration.asStarProjectedType()

    val mapTargetAnnoDeclaration = resolver.getClassDeclarationByName<Map.Target>()
        ?: error("Cannot find `Map.Target` declaration.")

    val mapTargetAnnoType = mapTargetAnnoDeclaration.asStarProjectedType()

    val mapMainSourceAnnoDeclaration = resolver.getClassDeclarationByName<Map.MainSource>()
        ?: error("Cannot find `Map.MainSource` declaration.")

    val mapMainSourceAnnoType = mapTargetAnnoDeclaration.asStarProjectedType()

    val mapperArgs = mapperAnnotation.resolveMapperArgs()
    val mapperName = mapperArgs.targetName { sourceDeclaration.simpleName.asString() }
    val mapperPackage = mapperArgs.packageName.ifBlank { sourceDeclaration.packageName.asString() }

    val actions: MutableList<MapperActionDef> = mutableListOf()


}

/**
 * 将一个 [KSClassDeclaration] 分析为 [MapperDef]
 */
internal fun KSClassDeclaration.resolveToMapperDef(
    environment: SymbolProcessorEnvironment,
    resolver: Resolver,
    mapperAnnotation: KSAnnotation,
): MapperDef {
    val mapperContext = MapperDefResolveContext(this, environment, resolver, mapperAnnotation)

    val abstractFunctions = getAllFunctions()
        .filter { it.isAbstract }
        .toList()


    abstractFunctions.forEach { function ->
        mapperContext.resolveMapActionDefs(function)
    }

    return MapperDef(
        environment = environment,
        resolver = resolver,
        sourceDeclaration = this,
        simpleName = mapperContext.mapperName,
        packageName = mapperContext.mapperPackage,
        declarationActions = mapperContext.actions.toList(),
        mapperAnnotation = mapperAnnotation,
        genTarget = mapperContext.mapperArgs.genTarget,
        genVisibility = mapperContext.mapperArgs.visibility,
        open = mapperContext.mapperArgs.open,
    )
}

internal fun MapperDefResolveContext.resolveMapActionDefs(
    function: KSFunctionDeclaration,
) {

    /*
     *  val name: String,
     *  val mapArgs: List<MapArgs>,
     *  val sources: List<MapperActionSourceDef>,
     *  val target: MapperActionTargetDef,
     */

    val name = function.simpleName.asString()

    // map args
    val mapArgs: List<MapArgs> = function.annotations
        .filter {
            mapAnnoType.isAssignableFrom(it.annotationType.resolve())
        }
        .map { it.resolveToMapArgs() }
        .toList()

    // TODO 检测不允许存在相同的 target

    val sources = resolveActionSources(function)
    val target = resolveActionTarget(function)

    resolveMapActionDefs(
        name = name,
        mapArgs = mapArgs,
        sources = sources,
        target = target,
        source = function
    )
}

internal fun MapperDefResolveContext.resolveActionSources(
    function: KSFunctionDeclaration
): List<MapperActionSourceDef> {
    val list = LinkedList<MapperActionSourceDef>()

    var mainMarked = false
    var resolveIndex = 0

    val receiverSource = function.extensionReceiver
        // Not a target
        ?.takeUnless { it.hasAnno(mapTargetAnnoType) }
        ?.let { receiver ->
            val receiverType = receiver.resolve()

            if (receiver.hasAnno(mapMainSourceAnnoType)) {
                mainMarked = true
            }

            MapperActionSourceDef(
                environment = environment,
                resolver = resolver,
                declaration = receiverType.declaration.asClassDeclaration()!!,
                incoming = MapActionIncoming(
                    name = null,
                    type = receiverType,
                    index = -1
                ),
                isMain = mainMarked
            )
        }?.also { resolveIndex++ }

    function.parameters.forEachIndexed { index, parameter ->
        if (parameter.hasAnno(mapTargetAnnoType)) {
            return@forEachIndexed
        }

        var isMain = false

        if (parameter.hasAnno(mapMainSourceAnnoType)) {
            if (mainMarked) {
                error("@Map.MainSource can only have one in $function")
            }
            mainMarked = true
            isMain = true
        }

        val type = parameter.type.resolve()

        val def = MapperActionSourceDef(
            environment = environment,
            resolver = resolver,
            declaration = type.declaration.asClassDeclaration()!!,
            incoming = MapActionIncoming(
                name = parameter.name?.asString(), // TODO null?
                type = type,
                index = index
            ),
            isMain = isMain
        )

        list.add(def)

        resolveIndex++
    }

    if (receiverSource != null) {
        if (!mainMarked) {
            mainMarked = true
            list.addFirst(receiverSource.copy(isMain = true))
        } else {
            list.addFirst(receiverSource)
        }
    }

    // 到最后也没有标记main的，取出参数第一个，标记为 main
    if (!mainMarked) {
        val first = list.removeFirst()
        list.addFirst(first.copy(isMain = true))
    }

    return list.toList()
}

internal fun MapperDefResolveContext.resolveActionTarget(
    function: KSFunctionDeclaration
): MapperActionTargetDef {
    val returnType = function.returnType?.resolve()?.takeIf { !resolver.builtIns.unitType.isAssignableFrom(it) }

    fun checkIncomingType(type: KSType) {
        check(returnType == null || returnType.isAssignableFrom(type)) {
            "Incoming target must be assignable from the return type $returnType"
        }
    }

    // find @Map.Target receiver
    val targetReceiver = function.extensionReceiver
        ?.takeIf { it.hasAnno(mapTargetAnnoType) }

    if (targetReceiver != null) {
        val receiverType = targetReceiver.resolve()
        // 入参应当是返回值的子类型，如果有返回值的话
        checkIncomingType(receiverType)

        return MapperActionTargetDef(
            environment = environment,
            resolver = resolver,
            declaration = receiverType.declaration.asClassDeclaration()!!,
            incoming = MapActionIncoming(
                name = null,
                type = receiverType,
                index = -1
            ),
            returns = returnType != null,
            nullable = returnType != null && returnType.nullability.isNullable
        )
    }

    // find @Map.Target parameter
    val targets = function.parameters.filter { it.hasAnno(mapTargetAnnoType) }
    val targetParameter: KSValueParameter?
    val targetParameterIndex: Int

    when (targets.size) {
        1 -> {
            targetParameter = targets[0]
            targetParameterIndex = 0
        }

        0 -> {
            targetParameter = null
            targetParameterIndex = -2
        }

        else -> error("@Map.Target can only have one or zero in $function")
    }

    // 有参数
    if (targetParameter != null) {
        val parameterType = targetParameter.type.resolve()
        // 入参应当是返回值的子类型，如果有返回值的话
        checkIncomingType(parameterType)

        return MapperActionTargetDef(
            environment = environment,
            resolver = resolver,
            declaration = parameterType.declaration.asClassDeclaration()!!, // TODO null?
            incoming = MapActionIncoming(
                name = targetParameter.name?.asString(), // TODO name?
                type = parameterType,
                index = targetParameterIndex
            ),
            returns = returnType != null,
            nullable = returnType != null && returnType.nullability.isNullable
        )
    }

    // 没有参数，那 returns 必须存在，且不能是抽象的。
    checkNotNull(returnType) {
        "Return type must be exists if there no target parameter or receiver incoming to $function"
    }

    val returnDeclaration = returnType.declaration.asClassDeclaration()

    check(
        returnDeclaration != null
            && returnDeclaration.classKind == ClassKind.CLASS
            && !returnDeclaration.isAbstract()
    ) {
        "Return type can not be abstract and must be a constructable class."
    }

    return MapperActionTargetDef(
        environment = environment,
        resolver = resolver,
        declaration = returnDeclaration,
        incoming = null,
        returns = true,
        nullable = returnType.nullability.isNullable,
    )
}


internal fun MapperDefResolveContext.resolveMapActionDefs(
    name: String,
    mapArgs: List<MapArgs>,
    sources: List<MapperActionSourceDef>,
    target: MapperActionTargetDef,
    source: KSFunctionDeclaration,
) {
    val action = MapperActionDef(
        environment = environment,
        resolver = resolver,
        name = name,
        mapArgs = mapArgs,
        sources = sources,
        target = target,
        sourceFun = source,
        node = source,
    )

    actions.add(action)
}

