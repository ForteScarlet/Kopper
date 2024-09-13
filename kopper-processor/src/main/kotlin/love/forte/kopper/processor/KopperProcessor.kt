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

import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import love.forte.kopper.processor.mapper.impl.resolveToMapper

private const val MAPPER_ANNOTATION_NAME = "love.forte.kopper.annotation.Mapper"

internal class KopperProcessor(
    val environment: SymbolProcessorEnvironment
) : SymbolProcessor {


    override fun process(resolver: Resolver): List<KSAnnotated> {
        val mapperDeclarations = resolver.getSymbolsWithAnnotation(MAPPER_ANNOTATION_NAME)
            .filterIsInstance<KSClassDeclaration>()
            // interface or abstract class.
            .filter { it.isAbstract() }
            .map { mapperDeclaration ->

                // Mapper
                resolveToMapper(resolver, mapperDeclaration)

                // TODO

            }


        return emptyList()
    }

    override fun onError() {
        super.onError()
    }

    override fun finish() {
        super.finish()
    }
}
