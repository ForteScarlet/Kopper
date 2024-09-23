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
import com.squareup.kotlinpoet.ksp.writeTo
import love.forte.kopper.processor.def.MapperDef
import love.forte.kopper.processor.def.resolveToMapperDef
import love.forte.kopper.processor.mapper.Mapper
import love.forte.kopper.processor.mapper.MapperGenerator
import love.forte.kopper.processor.mapper.resolveToMapper

private const val MAPPER_ANNOTATION_NAME = "love.forte.kopper.annotation.Mapper"

internal class KopperProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {

    private val mapperDefs = mutableListOf<MapperDef>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val mapperAnnoType = resolver.getClassDeclarationByName<love.forte.kopper.annotation.Mapper>()
            ?: error("Cannot find Mapper annotation declaration.")

        resolver.getSymbolsWithAnnotation(MAPPER_ANNOTATION_NAME)
            .filterIsInstance<KSClassDeclaration>()
            // interface or abstract class.
            .filter { it.isAbstract() }
            .forEach { mapperDeclaration ->
                val mapperAnnotation = mapperDeclaration.annotations.first {
                    mapperAnnoType.asStarProjectedType().isAssignableFrom(it.annotationType.resolve())
                }

                val mapperDef = mapperDeclaration.resolveToMapperDef(
                    environment = environment,
                    resolver = resolver,
                    mapperAnnotation = mapperAnnotation
                )

                mapperDefs.add(mapperDef)
            }

        return emptyList()
    }

    override fun finish() {
        val generator = MapperGenerator(mapperDefs)
        generator.generate()

        // TODO writeTo

        // mapperDefs.forEach { mapper ->
        //     mapper.generate().writeTo(
        //         codeGenerator = environment.codeGenerator,
        //         aggregating = true,
        //         originatingKSFiles = mapper.originatingKSFiles
        //     )
        // }
    }
}
