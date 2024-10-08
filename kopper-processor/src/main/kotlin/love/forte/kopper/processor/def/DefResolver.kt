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
import com.google.devtools.ksp.symbol.*
import love.forte.kopper.annotation.Mapping
import love.forte.kopper.processor.util.asClassDeclaration
import love.forte.kopper.processor.util.hasAnno
import love.forte.kopper.processor.util.isNullable
import java.util.*


internal class MapperDefResolveContext(
    private val sourceDeclaration: KSClassDeclaration,
    val kopperContext: KopperContext,
    mapperAnnotation: KSAnnotation,
) {
    // private val mapperAnnoDeclaration = resolver.getClassDeclarationByName<love.forte.kopper.annotation.Mapper>()
    //     ?: error("Cannot find `Mapper` annotation declaration.")
    //
    // val mapperAnnoType = mapperAnnoDeclaration.asStarProjectedType()

    private val mappingAnnoDeclaration =
        kopperContext.resolver.getClassDeclarationByName<Mapping>()
            ?: error("Cannot find `Map` annotation declaration.")

    val mapAnnoType = mappingAnnoDeclaration.asStarProjectedType()

    private val mappingTargetAnnoDeclaration =
        kopperContext.resolver.getClassDeclarationByName<Mapping.Target>()
            ?: error("Cannot find `Map.Target` declaration.")

    val mapTargetAnnoType = mappingTargetAnnoDeclaration.asStarProjectedType()

    private val mappingMainSourceAnnoDeclaration =
        kopperContext.resolver.getClassDeclarationByName<Mapping.MainSource>()
            ?: error("Cannot find `Map.MainSource` declaration.")

    val mapMainSourceAnnoType = mappingMainSourceAnnoDeclaration.asStarProjectedType()

    val mapperArgs = mapperAnnotation.resolveMapperArgs()
    val mapperName = mapperArgs.targetName { sourceDeclaration.simpleName.asString() }
    val mapperPackage = if (mapperArgs.packageSameAsSource) {
        sourceDeclaration.packageName.asString()
    } else {
        mapperArgs.packageName
    }

    val actions: MutableList<MapperActionDef> = mutableListOf()
}

/**
 * 将一个 [KSClassDeclaration] 分析为 [MapperDef]
 */
internal fun KSClassDeclaration.resolveToMapperDef(
    kopperContext: KopperContext,
    mapperAnnotation: KSAnnotation,
): MapperDef {
    val mapperContext = MapperDefResolveContext(
        this,
        kopperContext,
        mapperAnnotation
    )

    val abstractFunctions = getAllFunctions()
        .filter { it.isAbstract }
        .toList()


    abstractFunctions.forEach { function ->
        mapperContext.resolveMapActionDefs(function)
    }

    return MapperDef(
        kopperContext = kopperContext,
        sourceDeclaration = this,
        simpleName = mapperContext.mapperName,
        packageName = mapperContext.mapperPackage,
        declarationActions = mapperContext.actions.toList(),
        mapperAnnotation = mapperAnnotation,
        mapperArgs = mapperContext.mapperArgs,
        genTarget = mapperContext.mapperArgs.genTarget.value,
        genVisibility = mapperContext.mapperArgs.visibility.value,
        open = mapperContext.mapperArgs.open.value,
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
    val mappingArgs: List<MappingArgs> = function.annotations
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
        mappingArgs = mappingArgs,
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
                kopperContext = kopperContext,
                type = receiverType,
                declaration = receiverType.declaration.asClassDeclaration()!!,
                incoming = MapActionIncoming(
                    name = null,
                    type = receiverType,
                    index = -1,
                    node = receiver,
                ),
                isMain = mainMarked,
                node = receiver,
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
            kopperContext = kopperContext,
            type = type,
            declaration = type.declaration.asClassDeclaration()!!,
            incoming = MapActionIncoming(
                name = parameter.name?.asString(), // TODO null?
                type = type,
                index = index,
                node = parameter,
            ),
            isMain = isMain,
            node = parameter,
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
    val returnType = function.returnType?.resolve()?.takeIf {
        !kopperContext.resolver.builtIns.unitType.isAssignableFrom(it)
    }

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
            kopperContext = kopperContext,
            declaration = receiverType.declaration.asClassDeclaration()!!,
            incoming = MapActionIncoming(
                name = null,
                type = receiverType,
                index = -1,
                node = targetReceiver,
            ),
            returns = returnType != null,
            nullable = returnType != null && returnType.nullability.isNullable,
            node = targetReceiver
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
            kopperContext = kopperContext,
            declaration = parameterType.declaration.asClassDeclaration()!!, // TODO null?
            incoming = MapActionIncoming(
                name = targetParameter.name?.asString(), // TODO name?
                type = parameterType,
                index = targetParameterIndex,
                node = targetParameter,
            ),
            returns = returnType != null,
            nullable = returnType != null && returnType.nullability.isNullable,
            node = targetParameter
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
        kopperContext = kopperContext,
        declaration = returnDeclaration,
        incoming = null,
        returns = true,
        nullable = returnType.nullability.isNullable,
        node = function
    )
}


internal fun MapperDefResolveContext.resolveMapActionDefs(
    name: String,
    mappingArgs: List<MappingArgs>,
    sources: List<MapperActionSourceDef>,
    target: MapperActionTargetDef,
    source: KSFunctionDeclaration,
) {
    val action = MapperActionDef(
        kopperContext = kopperContext,
        name = name,
        mappingArgs = mappingArgs,
        sources = sources,
        target = target,
        sourceFun = source,
        node = source,
    )

    actions.add(action)
}

