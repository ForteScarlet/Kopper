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

package love.forte.kopper.processor

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ksp.writeTo
import love.forte.kopper.processor.def.KopperContext
import love.forte.kopper.processor.def.MapperDef
import love.forte.kopper.processor.def.resolveToMapperDef
import love.forte.kopper.processor.mapper.MapperGenerator

private const val MAPPER_ANNOTATION_NAME = "love.forte.kopper.annotation.Mapper"

internal class KopperProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {

    private lateinit var resolver: Resolver
    private val mapperDefs = mutableListOf<MapperDef>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        this.resolver = resolver
        val mapperAnnoType = resolver.getClassDeclarationByName<love.forte.kopper.annotation.Mapper>()
            ?: error("Cannot find Mapper annotation declaration.")

        val kopperContext = KopperContext(environment, resolver)

        resolver.getSymbolsWithAnnotation(MAPPER_ANNOTATION_NAME)
            .filterIsInstance<KSClassDeclaration>()
            // interface or abstract class.
            .filter { it.isAbstract() }
            .forEach { mapperDeclaration ->
                val mapperAnnotation = mapperDeclaration.annotations.first {
                    mapperAnnoType.asStarProjectedType().isAssignableFrom(it.annotationType.resolve())
                }

                val mapperDef = mapperDeclaration.resolveToMapperDef(
                    kopperContext = kopperContext,
                    mapperAnnotation = mapperAnnotation
                )

                mapperDefs.add(mapperDef)
            }

        return emptyList()
    }

    override fun finish() {
        val generator = MapperGenerator(
            environment = environment,
            resolver = resolver,
            mapperDefs
        )

        generator.generate()
        // find all originating files

        val files = generator.files.map { (file, def) -> file.build() to def }

        files.forEach { (file, def) ->
            environment.logger.info(
                "Writing implementation file ${file.name} for mapper ${def.packageName}.${def.simpleName}",
                def.sourceDeclaration
            )
            
            file.writeTo(
                codeGenerator = environment.codeGenerator,
                aggregating = true,
                originatingKSFiles = def.originatingKSFiles()
            )
        }
    }
}

private fun MapperDef.originatingKSFiles(): List<KSFile> {
    return buildList {
        sourceDeclaration.containingFile?.also(this::add)
        for (action in declarationActions) {
            action.sourceFun?.containingFile?.also(this::add)
            action.target.declaration.containingFile?.also(this::add)

            for (source in action.sources) {
                source.declaration.containingFile?.also(this::add)
            }
        }
    }
}
