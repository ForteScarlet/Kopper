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

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.CodeBlock
import love.forte.kopper.annotation.PropertyType

/**
 * A type as a mapping source.
 */
public interface MapSource {
    /**
     * name of this source.
     */
    public val name: String

    /**
     * Type of this source.
     */
    public val type: KSType

    /**
     * Kotlin's nullable or Java's platform type.
     */
    public val nullable: Boolean
        get() = type.nullability != Nullability.NOT_NULL

    /**
     * find a property from this source.
     */
    public fun property(name: String, type: KSType, propertyType: PropertyType): MapSourceProperty?
}

/**
 * A property for mapping source.
 */
public interface MapSourceProperty {
    public val source: MapSource

    /**
     * Name of source property.
     */
    public val name: String

    /**
     * The [MapSourceProperty]'s [PropertyType].
     * - If it is a function, it must have no parameters and have a return value, e.g. `fun prop(): AType`.
     *   The `get` prefix is disregarded and its name can be specified manually and directly.
     */
    public val propertyType: PropertyType

    /**
     * Type of this property.
     */
    public val type: KSType

    /**
     * Kotlin's nullable or Java's platform type.
     */
    public val nullable: Boolean
        get() = type.nullability != Nullability.NOT_NULL

    /**
     * Read this property's value.
     * @return An expression that can be stored by a local variable,
     * the type of the expression is type
     */
    public fun read(): CodeBlock
}

/**
 * A type as a mapping target.
 */
public interface MapTarget {
    public val name: String
    public val type: KSType
    public val properties: List<MapTargetProperty>
}

/**
 * A property for mapping target.
 */
public interface MapTargetProperty {
    /**
     * Name of target property.
     */
    public val name: String

    /**
     * The [MapTargetProperty]'s [PropertyType].
     * - If it is a property, it must have a `var` property or a constructor property.
     * - If it is a function, it must have only one parameter, e.g. `fun prop(value: AType)`.
     *   The `set` prefix is disregarded and its name can be specified manually and directly.
     */
    public val propertyType: PropertyType

    /**
     * Type of this property.
     */
    public val type: KSType

    /**
     * Kotlin's nullable or Java's platform type.
     */
    public val nullable: Boolean
        get() = type.nullability != Nullability.NOT_NULL
}
