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
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import love.forte.kopper.annotation.MapperGenTarget
import love.forte.kopper.annotation.MapperGenVisibility
import love.forte.kopper.processor.util.findArg
import love.forte.kopper.processor.util.findEnumArg
import love.forte.kopper.processor.util.findListArg

/**
 * A Mapper with a set of [MapperMapSet].
 */
internal class Mapper(
    /**
     * The gen target name.
     */
    val targetName: String,
    val targetPackage: String,

    /**
     * The set of [MapperMapSet].
     */
    val mapSets: MutableList<MapperMapSet> = mutableListOf(),

    val superType: KSClassDeclaration,

    val genTarget: MapperGenTarget,
    val genVisibility: MapperGenVisibility
) {


    fun generate(): FileSpec {
        val mapperClass = when (genTarget) {
            MapperGenTarget.CLASS -> {
                TypeSpec.classBuilder(targetName)
            }

            MapperGenTarget.OBJECT -> {
                TypeSpec.objectBuilder(targetName)
            }
        }

        when (genVisibility) {
            MapperGenVisibility.PUBLIC -> {
                mapperClass.addModifiers(KModifier.PUBLIC)
            }

            MapperGenVisibility.INTERNAL -> {
                mapperClass.addModifiers(KModifier.INTERNAL)
            }
        }

        if (superType.classKind == ClassKind.INTERFACE) {
            mapperClass.addSuperinterface(superType.toClassName())
        } else {
            mapperClass.superclass(superType.toClassName())
        }

        val functions = mutableListOf<FunSpec.Builder>()
        val mapperWriter = MapperWriter(collect = functions)

        mapSets.forEach { mapSet ->
            mapSet.emit(mapperWriter)
        }

        functions.forEach {
            mapperClass.addFunction(it.build())
        }

        // File
        val file = FileSpec.builder(targetPackage, targetName).apply {
            indent("    ")
            addType(mapperClass.build())
        }.build()

        return file
    }


}

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

    fun emit(writer: MapperWriter) {
        val funBuilder = FunSpec.builder(sourceFun.simpleName.asString())
        funBuilder.addModifiers(KModifier.OVERRIDE)
        // parameters
        sourceFun.extensionReceiver?.also { funBuilder.receiver(it.toTypeName()) }
        sourceFun.parameters.forEach { funBuilder.addParameter(it.name!!.asString(), it.type.toTypeName()) }
        // return
        sourceFun.returnType?.toTypeName()?.also { funBuilder.returns(it) }


        val extensions = mutableListOf<FunSpec.Builder>()
        val setWriter = MapperMapSetWriter(funBuilder, extensions)

        // init target.
        target.emitInit(setWriter)
        setWriter.add("\n")

        // emit maps
        for ((index, map) in maps.withIndex()) {
            map.emit(setWriter, index)
            setWriter.add("\n")
        }

        // do return
        if (sourceFun.returnType != null && sourceFun.returnType?.resolve() != resolver.builtIns.unitType) {
            setWriter.add("return %L", target.name)
        }

        // write fun
        writer.add(funBuilder)
        extensions.forEach { ef ->
            if (KModifier.PUBLIC !in ef.modifiers
                && KModifier.INTERNAL !in ef.modifiers
                && KModifier.PRIVATE !in ef.modifiers
            ) {
                ef.addModifiers(KModifier.PRIVATE)
            }

            writer.add(ef)
        }
    }


}


/**
 * A single map in [MapperMapSet].
 */
internal interface MapperMap {
    /**
     * The source. If the source is an eval expression,
     * the source will be the main source.
     */
    val source: MapSource

    /**
     * The target.
     */
    val target: MapTarget

    /**
     * The target property.
     */
    val targetProperty: MapTargetProperty

    /**
     * Gen a [CodeBlock] for this map.
     *
     * @param index The index of this map.
     */
    fun emit(writer: MapperMapSetWriter, index: Int)
}


internal data class MapperArgs(
    val genTarget: MapperGenTarget,
    val visibility: MapperGenVisibility,

    // name
    val genTargetName: String,
    val genTargetNamePrefix: String,
    val genTargetNameSuffix: String,
    val genTargetPackages: List<String>,
) {
    val packageName: String = genTargetPackages.joinToString(".")
    inline fun targetName(declarationSimpleName: () -> String): String =
        genTargetNamePrefix +
            (genTargetName.takeIf { it.isNotEmpty() } ?: declarationSimpleName()) +
            genTargetNameSuffix


}

internal fun KSAnnotation.resolveMapperArgs(): MapperArgs {
    val genTarget = findEnumArg<MapperGenTarget>("genTarget")!!
    val visibility = findEnumArg<MapperGenVisibility>("visibility")!!

    // Name-related arguments
    val genTargetName: String = findArg("genTargetName")!!
    val genTargetNamePrefix: String = findArg("genTargetNamePrefix")!!
    val genTargetNameSuffix: String = findArg("genTargetNameSuffix")!!
    val genTargetPackages: List<String> = findListArg<String>("genTargetPackages")!!

    return MapperArgs(
        genTarget = genTarget,
        visibility = visibility,
        genTargetName = genTargetName,
        genTargetNamePrefix = genTargetNamePrefix,
        genTargetNameSuffix = genTargetNameSuffix,
        genTargetPackages = genTargetPackages
    )
}

