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

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSNode
import love.forte.kopper.annotation.Mapping
import love.forte.kopper.processor.util.findArg

/**
 * Map args from annotation [Mapping].
 */
internal data class MappingArgs(
    val target: NodeArg<String>,
    val source: NodeArg<String>,
    val sourceName: NodeArg<String>,
    val ignore: NodeArg<Boolean>,
    val eval: NodeArg<String>,
    /**
     * The `nullable` for [eval]'s result.
     */
    val evalNullable: NodeArg<Boolean>,
    val node: KSNode?
) {
    val isEvalValid: Boolean
        get() = eval.value.isNotBlank()
}

/**
 * @see love.forte.kopper.annotation.Mapping
 */
internal fun KSAnnotation.resolveToMapArgs(): MappingArgs {
    val target: NodeArg<String> = findArg("target")!!
    val source: NodeArg<String> = findArg("source")!!
    val sourceName: NodeArg<String> = findArg("sourceName")!!
    val ignore: NodeArg<Boolean> = findArg("ignore")!!
    val eval: NodeArg<String> = findArg("eval")!!
    val evalNullable: NodeArg<Boolean> = findArg("evalNullable")!!

    return MappingArgs(
        target = target,
        source = if (Mapping.SAME_AS_TARGET == source.value) target else source,
        sourceName = sourceName,
        ignore = ignore,
        eval = eval,
        evalNullable = evalNullable,
        node = this
    )
}
