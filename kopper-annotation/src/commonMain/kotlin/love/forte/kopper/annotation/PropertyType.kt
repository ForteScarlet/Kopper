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

package love.forte.kopper.annotation

/**
 * Enum representing different types of properties
 */
public enum class PropertyType {
    /**
     * Attempt some degree of automatic judgment depending on the situation.
     * If it cannot be judged and cannot be found, an exception is thrown.
     */
    AUTO,

    /**
     * A property implemented as ... em, a property.
     */
    PROPERTY,

    /**
     * A property implemented as a function.
     * This function must be parameterless.
     */
    FUNCTION,
}
