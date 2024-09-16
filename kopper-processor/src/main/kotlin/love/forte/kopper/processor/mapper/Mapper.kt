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
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier.*
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import love.forte.kopper.annotation.MapperGenTarget
import love.forte.kopper.annotation.MapperGenVisibility

/**
 * A Mapper with a set of [MapperMapSet].
 */
internal class Mapper(
    val environment: SymbolProcessorEnvironment,
    val resolver: Resolver,
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
    val genVisibility: MapperGenVisibility,

    // for writing
    val originatingKSFiles: MutableList<KSFile> = mutableListOf(),
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
                mapperClass.addModifiers(PUBLIC)
            }

            MapperGenVisibility.INTERNAL -> {
                mapperClass.addModifiers(INTERNAL)
            }
        }

        if (superType.classKind == ClassKind.INTERFACE) {
            mapperClass.addSuperinterface(superType.toClassName())
        } else {
            mapperClass.superclass(superType.toClassName())
        }

        val mapperWriter = MapperWriter(
            environment = environment,
            resolver = resolver
        )

        mapSets.forEach { mapSet ->
            mapSet.emit(mapperWriter)
        }

        mapperWriter.collect.values.sortedBy { it.isAncillary }.forEach { info ->
            val funSpec = info.funSpec
            if (info.isAncillary) {
                val vi = setOf(PUBLIC, INTERNAL, PRIVATE, PROTECTED)
                if (funSpec.modifiers.none { it in vi }) {
                    funSpec.addModifiers(PRIVATE)
                }
            }

            mapperClass.addFunction(funSpec.build())
        }

        // File
        val file = FileSpec.builder(targetPackage, targetName).apply {
            addType(mapperClass.build())
        }.build()

        return file
    }
}




