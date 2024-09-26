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

import com.google.devtools.ksp.symbol.ClassKind
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier.*
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import love.forte.kopper.annotation.GeneratedMapper
import love.forte.kopper.annotation.MapperGenTarget
import love.forte.kopper.annotation.MapperGenVisibility
import love.forte.kopper.processor.def.MapperDef

/**
 * A Mapper with a set of [MapperAction].
 */
internal class Mapper(
    val def: MapperDef,
    val generator: MapperGenerator,
) {
    private val typeBuilder: TypeSpec.Builder = when (def.genTarget) {
        MapperGenTarget.CLASS -> {
            TypeSpec.classBuilder(def.simpleName)
        }

        MapperGenTarget.OBJECT -> {
            TypeSpec.objectBuilder(def.simpleName)
        }
    }

    init {
        initType()
    }

    private fun initType() {
        when (def.genVisibility) {
            MapperGenVisibility.PUBLIC -> {
                typeBuilder.addModifiers(PUBLIC)
            }

            MapperGenVisibility.INTERNAL -> {
                typeBuilder.addModifiers(INTERNAL)
            }
        }

        if (def.sourceDeclaration.classKind == ClassKind.INTERFACE) {
            typeBuilder.addSuperinterface(def.sourceDeclaration.toClassName())
        } else {
            typeBuilder.superclass(def.sourceDeclaration.toClassName())
        }

        if (def.genTarget == MapperGenTarget.CLASS && def.open) {
            typeBuilder.addModifiers(OPEN)
        }

        // add @Suppress("FunctionName", "LocalVariableName")
        typeBuilder.addAnnotation(
            AnnotationSpec.builder(Suppress::class)
                .addMember(
                    "%S, %S, %S",
                    "FunctionName",
                    "LocalVariableName",
                    "RedundantSuppression",
                )
                .build()
        )

        // add GeneratedMapper

        if (def.mapperAnnotation != null) {
            typeBuilder.addAnnotation(
                AnnotationSpec.builder(GeneratedMapper::class)
                    .addMember(
                        CodeBlock.builder()
                            .apply {
                                val sourceDeclaration = def.sourceDeclaration
                                add("sources = [%T::class]", sourceDeclaration.toClassName())
                            }
                            .build()
                    )
                    .build()
            )
        }
    }

    fun generate() {
        val actionGenerator = MapperActionsGenerator(
            environment = def.environment,
            resolver = def.resolver,
            generator
        )
        // 遍历两遍，先 prepare，再 generate
        def.declarationActions.forEach { def ->
            val action = MapperAction(def = def, generator = actionGenerator)
            actionGenerator.actions.add(action)
        }

        for (action in actionGenerator.actions) {
            action.prepare()
        }

        do {
            val buffers = actionGenerator.buffer.toList()
            actionGenerator.buffer.clear()

            for (buffer in buffers) {
                // prepare buffer and add to actions (releases)
                // prepare may add more buffers.
                buffer.prepare()
                actionGenerator.actions.add(buffer)
            }

        } while (actionGenerator.buffer.isNotEmpty())

        for (action in actionGenerator.actions) {
            action.generate()
            typeBuilder.addFunction(action.funBuilder.build())
        }

        generator.addFile(
            def,
            FileSpec.builder(def.packageName, def.simpleName).apply {
                addType(typeBuilder.build())
            }
        )
    }


    // fun generate0() {
    //
    //     val mapperWriter = MapperWriter(
    //         environment = def.environment,
    //         resolver = def.resolver
    //     )
    //
    //     // mapSets.forEach { mapSet ->
    //     //     mapSet.emit(mapperWriter)
    //     // }
    //
    //     mapperWriter.collect.values.sortedBy { it.isAncillary }.forEach { info ->
    //         val funSpec = info.funSpec
    //         if (info.isAncillary) {
    //             val vi = setOf(PUBLIC, INTERNAL, PRIVATE, PROTECTED)
    //             if (funSpec.modifiers.none { it in vi }) {
    //                 funSpec.addModifiers(PRIVATE)
    //             }
    //         }
    //
    //         // mapperClass.addFunction(funSpec.build())
    //     }
    //
    //     // File
    //     val file = FileSpec.builder(def.packageName, def.simpleName).apply {
    //         // addType(mapperClass.build())
    //     }
    //
    //     generator.addFile(def, file)
    // }
}




