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

import kotlin.reflect.KClass

// TODO
@Retention(AnnotationRetention.SOURCE)
public annotation class ModelMapping {
    @Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
    public annotation class From(
        val value: KClass<*>,
        val funName: String = "", // if empty: $FROM.to$THIS(...): $THIS { ... }
    )

    @Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
    public annotation class To(
        val value: KClass<*>,
        val funName: String = "", // if empty: ${THIS}.to$THIS(...): $THIS { ... }
    )
}
