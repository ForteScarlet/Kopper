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

import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ksp.toClassName
import love.forte.kopper.processor.def.KopperContext
import love.forte.kopper.processor.def.MapperActionSourceDef
import love.forte.kopper.processor.def.readerCode
import love.forte.kopper.processor.util.asClassDeclaration
import love.forte.kopper.processor.util.tryTypeCast


/**
 *
 * @author ForteScarlet
 */
internal interface MapperActionStatement {
    /**
     * Emit current ActionStatement to [builder].
     */
    fun emit(builder: CodeBlock.Builder)
}

internal object TODOActionStatement : MapperActionStatement {
    override fun emit(builder: CodeBlock.Builder) {
        builder.add("TODO(%S)", "Not yet implemented")
    }
}

// Mapper Action statement,
// 从一个或0个（eval）source中，
// 读取属性、并赋予一个 target。

/**
 * Eval, 没有 source
 */
internal class EvalMapperActionStatement(
    private val eval: String,
    private val evalNullable: Boolean,
    private val property: MapActionTargetProperty,
) : MapperActionStatement {
    override fun emit(builder: CodeBlock.Builder) {
        val propertyRef = property.propertyRef
        builder.add(propertyRef)
        builder.add("·=·")
        builder.add("(")
        builder.add(CodeBlock.of(eval))
        builder.add(")")

        if (!property.def.nullable && evalNullable) {
            builder.add("!!")
        }

    }
}

internal class FromSourceMapperActionStatement(
    private val kopperContext: KopperContext,
    private val sourceProperty: MapActionSourceProperty,
    private val targetProperty: MapActionTargetProperty,
) : MapperActionStatement {
    override fun emit(builder: CodeBlock.Builder) {
        val sourceDef = sourceProperty.source.def
        val targetPropertyRef = targetProperty.propertyRef
        val incoming = sourceDef.incoming
        val sourceNullable = incoming.nullable

        val sourceOrValueNullable = sourceNullable || sourceProperty.valueNullable

        // writer.add(CodeBlock.of("%L = ", targetProperty.name))
        var readerCode = CodeBlock.builder().apply {
            add("%L", incoming.name ?: "this")
            if (sourceNullable) {
                add("?")
            }
            add(".")
            add(sourceProperty.def.readerCode(sourceNullable))
        }.build()

        val sourceClassDeclaration = sourceProperty.def.declaration.asClassDeclaration()
        val targetClassDeclaration = targetProperty.def.declaration.asClassDeclaration()

        if (sourceClassDeclaration != null && targetClassDeclaration != null) {
            readerCode = kopperContext.resolver.tryTypeCast(
                readerCode,
                nullable = sourceOrValueNullable,
                sourceDeclaration = sourceClassDeclaration,
                targetDeclaration = targetClassDeclaration,
            )
        }

        // 是构造，必须提供
        when {
            targetProperty.def.nullable ||
                !sourceProperty.valueNullable -> {
                builder.add(targetPropertyRef)
                // target 是 nullable，
                // 或者同时 source 也是 non-null
                builder.add("·=·")
                builder.add(readerCode)
            }

            // target is non-null,
            // source is nullable

            //// 是必须的，不能通过属性判断
            //// 直接添加 !!
            targetProperty.def.isRequired -> {
                builder.add(targetPropertyRef)
                builder.add("·=·(")
                builder.add(readerCode)
                builder.add(")!!")
            }

            //// 不是必须的，判断 source 不为 null 再设置
            else -> {
                // (readCode).also { it -> %L = it }
                builder.add("(")
                builder.add(readerCode)
                builder.beginControlFlow(")?.also")
                // builder.add(")?.also·{ ")
                builder.add(targetPropertyRef)
                builder.add("·=·it")
                builder.endControlFlow()
            }
        }
    }
}

/**
 * 聚合了 requirements 的 statement
 *
 * 也同样会初始化 [MapperActionTarget.name]
 *
 */
internal class RequiredInitialActionStatement(
    private val target: MapperActionTarget,
    private val requiredStatements: List<MapperActionStatement>,
) : MapperActionStatement {
    override fun emit(builder: CodeBlock.Builder) {
        val targetDef = target.def

        val name = if (target.isNameInitialized()) {
            target.name
        } else {
            (targetDef.incoming?.let { it.name ?: "this" } ?: "_t_0")
                // initial target name
                .also { target.name = it }
        }

        // 初始化 target 的内容
        val initialCode = CodeBlock.builder().apply {
            // Target(a = a, b = b, ...)
            add("%T(", targetDef.declaration.toClassName())
            requiredStatements.forEachIndexed { index, statement ->
                statement.emit(this)
                if (index != requiredStatements.lastIndex) {
                    add(", ")
                }
            }
            add(")")
        }.build()


        when {
            // 没有入参，直接初始化
            targetDef.incoming == null -> {
                builder.add("val %L·=·", name)
                builder.add(initialCode)
            }
            // 有入参、入参 nullable，则参考返回值
            targetDef.incoming.nullable -> {
                when {
                    // 有返回值，返回值 nullable，那么如果入参为null则直接return null
                    targetDef.returns && targetDef.nullable -> {
                        builder.add("val %L·=·", name)
                        builder.add(initialCode)
                        builder.add(" ?:·return null")
                    }
                    // 没有返回值，则如果入参为null直接 return
                    !targetDef.returns && targetDef.nullable -> {
                        builder.add("val %L·=·", name)
                        builder.add(initialCode)
                        builder.add(" ?:·return")
                    }
                }
            }

            else -> {
                // 有入参、不是 nullable，说白了就是不需要target初始化
                builder.add(
                    "/* " +
                        "The input parameter of target already exists and cannot be null, " +
                        "so it doesn't need to be initialised. " +
                        "*/"
                )

            }
        }
    }
}

private data class IncomingNamePair(val name: String, val source: MapperActionSource)

private inline fun resolveIncomingNamesAndReceiver(
    sources: List<MapperActionSource>,
    subAction: MapperAction,
    onReceiver: (MapperActionSource) -> Unit
): Array<IncomingNamePair?> {
    val subActionSources = subAction.def.sources
    val incomingNamePairs = Array<IncomingNamePair?>(subActionSources.count { it.incoming.index >= 0 }) { null }
    // 根据类型、可空性寻找source的匹配入参名称
    val sources0 = sources.toMutableList()

    loop@ for (subActionSource in subActionSources) {
        val subSourceIncoming = subActionSource.incoming
        val iter = sources0.iterator()
        while (iter.hasNext()) {
            val incomingSource = iter.next()

            // 类型匹配, incoming 是 sub source 的子类
            val incoming = incomingSource.def.incoming
            if (!subSourceIncoming.type.isAssignableFrom(incoming.type)) {
                continue
            }

            // null匹配
            // 如果入参不能为null，但是预期的可以为null
            if (!subActionSource.incoming.nullable && incoming.nullable) {
                continue
            }

            // 匹配，记录名称
            // 如果index是 -1 则 receiver
            if (subSourceIncoming.index < 0) {
                onReceiver(incomingSource)
                // receiver = incomingSource
            } else {
                val name: String = subSourceIncoming.name!!
                incomingNamePairs[subSourceIncoming.index] = IncomingNamePair(name, incomingSource)
            }

            iter.remove()
            continue@loop
        }
    }

    return incomingNamePairs
}

internal class SubActionActionStatement(
    private val subAction: MapperAction,
    private val sources: List<MapperActionSource>,
    private val targetProperty: MapActionTargetProperty,
) : MapperActionStatement {
    // 引用当前内部的一个创建的子sub action.

    override fun emit(builder: CodeBlock.Builder) {
        // sub action 应当与当前所求的属性值相匹配，
        // 因此只传递参数、而不需要判断结果的可空性，大概。

        // sub action 没有 target

        // first: sub action incoming name,
        // second: sources incoming name.
        var receiver: MapperActionSource? = null

        val incomingNamePair = resolveIncomingNamesAndReceiver(sources, subAction) {
            receiver = it
        }

        val invokeCode = CodeBlock.builder().apply {
            add("%L(", subAction.def.name)

            // receiver is first
            if (receiver != null) {
                add("%L, ", receiver!!.def.incoming.name ?: "this")
            }

            incomingNamePair
                .forEachIndexed { index, pair ->
                    val (name, value) = pair ?: return@forEachIndexed
                    add("%L·=·%L", name, value.def.incoming.name ?: "this")
                    if (index != incomingNamePair.lastIndex) {
                        add(", ")
                    }
                }
            add(")")
        }.build()

        val targetPropertyRef = targetProperty.propertyRef
        builder.add(targetPropertyRef)
        builder.add("·=·")
        builder.add(invokeCode)
    }
}

/**
 * iterable map to iterable.
 */
internal class SubIter2IterActionStatement(
    private val subAction: MapperAction,
    private val sources: List<MapperActionSource>,
    private val sourceProperty: MapActionSourceProperty,
    private val targetProperty: MapActionTargetProperty,
    private val subTargetType: KSType,
    private val sourceMapItSource: MapperActionSourceDef
) : MapperActionStatement {
    override fun emit(builder: CodeBlock.Builder) {
        // 目标如果是 Collection、List，直接 map
        // 否则 mapTo(mutableSetOf())
        // source.map { itSource.name -> subAction(...) }

        // 先把map内的函数构造出来，map it 的名字为 sourceMapItSource.incoming.name
        // first: sub action incoming name,
        // second: sources incoming name.
        var receiver: MapperActionSource? = null

        val incomingNamePair = resolveIncomingNamesAndReceiver(sources, subAction) {
            receiver = it
        }

        val mapInvokeCode = CodeBlock.builder().apply {
            add("%L(", subAction.def.name)

            // receiver is first
            if (receiver != null) {
                add("%L, ", receiver!!.def.incoming.name ?: "this")
            }

            incomingNamePair
                .forEachIndexed { index, pair ->
                    val (name, value) = pair ?: return@forEachIndexed
                    add("%L·=·%L", name, value.def.incoming.name ?: "this")
                    if (index != incomingNamePair.lastIndex) {
                        add(", ")
                    }
                }
            add(")")
        }.build()

        // 然后选择 map
        // target 是 collection 或 list -> source.map
        // target 是 set, mutableSet -> mapTo(mutableSetOf())
        // 是 MutableColl, MutableList -> mapTo(mutableListOf())
        // 其他的，不管，用 map
        // .mapFunction { name -> }
        val targetDeclaration = targetProperty.def.declaration
        val targetPackageName = targetDeclaration.packageName.asString()
        val targetSimpleName = targetDeclaration.simpleName.asString()
        val mapFunctionCodeBuilder = CodeBlock.builder()

        var ifNullThenMember: MemberName? = null

        when {
            targetPackageName != KT_COLL_PKG && targetSimpleName != JAVA_UTIL -> {
                mapFunctionCodeBuilder.add("%M", COLL_MAP)
                ifNullThenMember = EMPTY_LIST
            }

            else -> when (targetSimpleName) {
                "List", "Collection", "Iterable" -> {
                    mapFunctionCodeBuilder.add("%M", COLL_MAP)
                    ifNullThenMember = EMPTY_LIST
                }
                "Set" -> {
                    mapFunctionCodeBuilder.add("%M(%M())", COLL_MAP_TO, MUT_SET_OF)
                    ifNullThenMember = EMPTY_SET
                }
                "MutableSet" -> {
                    mapFunctionCodeBuilder.add("%M(%M())", COLL_MAP_TO, MUT_SET_OF)
                    ifNullThenMember = MUT_SET_OF
                }
                else -> {
                    mapFunctionCodeBuilder.add("%M(%M())", COLL_MAP_TO, MUT_LIST_OF)
                    ifNullThenMember = MUT_LIST_OF
                }
            }
        }

        val invokeCode = CodeBlock.builder().apply {
            // source.map
            add(sourceProperty.propertyAccessor())
            if (sourceProperty.valueNullable) {
                add("?")
            }
            add(".")
            add(mapFunctionCodeBuilder.build())
            beginControlFlow(" { %L -> ", sourceMapItSource.incoming.name!!)
            add(mapInvokeCode)
            endControlFlow()
        }.build()

        val targetPropertyRef = targetProperty.propertyRef
        builder.add(targetPropertyRef)
        builder.add("·=·")
        builder.add(invokeCode)
        if (!targetProperty.def.nullable && sourceProperty.valueNullable) {
            builder.add(" ?: %M()", ifNullThenMember)
        }
    }

    companion object {
        const val KT_COLL_PKG = "kotlin.collections"
        const val JAVA_UTIL = "java.util"


        // Iterable.map
        val COLL_MAP = MemberName("kotlin.collections", "map")

        // Iterable.mapTo
        val COLL_MAP_TO = MemberName("kotlin.collections", "mapTo")

        val MUT_SET_OF = MemberName("kotlin.collections", "mutableSetOf")
        val MUT_LIST_OF = MemberName("kotlin.collections", "mutableListOf")

        val EMPTY_LIST = MemberName("kotlin.collections", "emptyList")
        val EMPTY_SET = MemberName("kotlin.collections", "emptySet")
    }
}

internal class SubIter2TargetActionStatement(
    private val subAction: MapperAction,
    private val sources: List<MapperActionSource>,
    private val sourceProperty: MapActionSourceProperty,
    private val targetProperty: MapActionTargetProperty,
    private val mapFirstSource: MapperActionSourceDef
) : MapperActionStatement {
    override fun emit(builder: CodeBlock.Builder) {
        // 先把转化用的函数构造出来，map it 的名字为 sourceMapItSource.incoming.name
        // first: sub action incoming name,
        // second: sources incoming name.
        var receiver: MapperActionSource? = null

        val incomingNamePair = resolveIncomingNamesAndReceiver(sources, subAction) {
            receiver = it
        }

        val mapInvokeCode = CodeBlock.builder().apply {
            add("%L(", subAction.def.name)

            // receiver is first
            if (receiver != null) {
                add("%L, ", receiver!!.def.incoming.name ?: "this")
            }

            incomingNamePair
                .forEachIndexed { index, pair ->
                    val (name, value) = pair ?: return@forEachIndexed
                    add("%L·=·%L", name, value.def.incoming.name ?: "this")
                    if (index != incomingNamePair.lastIndex) {
                        add(", ")
                    }
                }
            add(")")
        }.build()

        // target not null, source iter nullable:
        //   target = source?.firstOrNull()?.let { s -> subAction() }!!
        // target nullable, source not null
        //   target = source.firstOrNull()?.let { s -> subAction() }
        // target not null, source not null
        //   target = source.firstOrNull()?.let { s -> subAction() }!!

        val sourceValueNullable = sourceProperty.valueNullable

        val sourceIterReadCode = CodeBlock.builder().apply {
            add(sourceProperty.propertyAccessor())
            if (sourceValueNullable) {
                add("?")
            }
            beginControlFlow(".%M()?.let { %L -> ", ITER_FIRST_OR_NULL, mapFirstSource.incoming.name!!)
            add(mapInvokeCode)
            endControlFlow()
        }.build()


        val invokeCode = CodeBlock.builder().apply {
            add(sourceIterReadCode)
            if (!targetProperty.def.nullable) {
                // target not nullable
                add("!!")
            }
        }.build()

        val targetPropertyRef = targetProperty.propertyRef
        builder.add(targetPropertyRef)
        builder.add("·=·")
        builder.add(invokeCode)
    }


    companion object {
        val ITER_FIRST_OR_NULL = MemberName("kotlin.collections", "firstOrNull")
    }
}
