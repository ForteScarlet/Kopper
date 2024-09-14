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

public class MapperBuilderImpl(
    private val collect: MutableCollection<FunSpec.Builder> =
        mutableListOf()
) {
    public fun add(funSpec: FunSpec.Builder) {
        collect.add(funSpec)
    }
}

public class MapperMapSetBuilder(
    private val root: FunSpec.Builder,
    private val stacks: ArrayDeque<FunSpec.Builder> = ArrayDeque()
) {
    public fun add(code: CodeBlock) {
        if (stacks.isEmpty()) {
            root.addCode(code)
        } else {
            stacks.last().addCode(code)
        }
    }

    public fun push(funSpec: FunSpec.Builder) {
        stacks.addLast(funSpec)
    }

    public fun pop(): FunSpec.Builder {
        if (stacks.isEmpty()) {
            return root
        }
        return stacks.removeLast()
    }
}
