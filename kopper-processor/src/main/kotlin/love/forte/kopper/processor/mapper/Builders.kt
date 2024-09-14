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

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec

// Mapper -> A Type annotated with @Mapper, e.g. an interface or an abstract class
// MapperMapSet -> A fun in Mapper.
// MapperMap -> A @Map or a pair of mapping properties

internal class MapperWriter(
    private val collect: MutableCollection<FunSpec.Builder> =
        mutableListOf()
) {
    fun add(funSpec: FunSpec.Builder) {
        collect.add(funSpec)
    }
}

internal class MapperMapSetWriter(
    private val root: FunSpec.Builder,
    private val extensions: MutableList<FunSpec.Builder> = mutableListOf(),
    private val stacks: ArrayDeque<FunSpec.Builder> = ArrayDeque()
) {
    fun add(code: CodeBlock) {
        if (stacks.isEmpty()) {
            root.addCode(code)
        } else {
            stacks.last().addCode(code)
        }
    }
    fun add(format: String, vararg args: Any?) {
        if (stacks.isEmpty()) {
            root.addCode(format, *args)
        } else {
            stacks.last().addCode(format, *args)
        }
    }

    fun push(funSpec: FunSpec.Builder, remember: Boolean = false) {
        stacks.addLast(funSpec)
        if (remember) {
            extensions.add(funSpec)
        }
    }

    fun pop(): FunSpec.Builder {
        if (stacks.isEmpty()) {
            return root
        }
        return stacks.removeLast()
    }
}
