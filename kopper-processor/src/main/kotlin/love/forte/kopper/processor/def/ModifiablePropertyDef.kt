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

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType

internal sealed interface TargetPropertyDef {
    val isRequired: Boolean
    val kopperContext: KopperContext
    val name: String
    val type: KSType?
    val declaration: KSDeclaration
    val nullable: Boolean
    val node: KSNode?
}

/**
 * 非构造的其他可变参数
 */
internal data class ModifiablePropertyDef(
    override val kopperContext: KopperContext,
    // 可以修改的属性，可以在 action target 中获取
    // 必须是属性类型，必须是根属性

    override val name: String,
    override val declaration: KSDeclaration,
    override val type: KSType?,
    override val nullable: Boolean,
    override val node: KSNode?,
) : TargetPropertyDef {
    override val isRequired: Boolean
        get() = false

    override fun toString(): String {
        return "ModifiablePropertyDef(declaration=$declaration, isRequired=$isRequired, name='$name', node=$node, nullable=$nullable)"
    }
}

/**
 * 构造中所必须地初始化参数，在需要对 target 进行内部初始化时使用。
 */
internal data class RequiredParameterDef(
    override val kopperContext: KopperContext,
    override val name: String,
    override val type: KSType?,
    override val declaration: KSDeclaration,
    override val nullable: Boolean,
    override val node: KSNode?,
    val isVar: Boolean,
    val hasDefaultValue: Boolean,
) : TargetPropertyDef {
    override val isRequired: Boolean
        get() = true

    override fun toString(): String {
        return "RequiredParameterDef(declaration=$declaration, name='$name', nullable=$nullable, node=$node, isVar=$isVar, hasDefaultValue=$hasDefaultValue)"
    }


}

/**
 * 如果可以（有 `var`），则转为属性。
 */
internal fun RequiredParameterDef.asProperty(): ModifiablePropertyDef? {
    if (!isVar) {
        return null
    }

    return ModifiablePropertyDef(
        kopperContext = kopperContext,
        name = name,
        declaration = declaration,
        type = type,
        nullable = nullable,
        node = node,
    )
}
