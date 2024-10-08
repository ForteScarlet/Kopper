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
import org.gradle.api.DomainObjectCollection
import org.gradle.api.publish.Publication
import org.gradle.plugins.signing.SigningExtension


data class Gpg(val keyId: String, val secretKey: String, val password: String) {
    companion object {
        fun ofSystemProp(
            keyId: String = CiConstant.GPG_KEY_ID,
            secretKey: String = CiConstant.GPG_SECRET_KEY,
            password: String = CiConstant.GPG_PASSWORD,
        ) = Gpg(
            systemProp(keyId)!!,
            systemProp(secretKey)!!,
            systemProp(password)!!,
        )

        fun ofSystemPropOrNull(
            keyId: String = CiConstant.GPG_KEY_ID,
            secretKey: String = CiConstant.GPG_SECRET_KEY,
            password: String = CiConstant.GPG_PASSWORD,
        ): Gpg? {
            val keyIdValue = systemProp(keyId)
            val secretKeyValue = systemProp(secretKey)
            val passwordValue = systemProp(password)

            if (keyIdValue == null) {
                logger.warn("GPG keyId {} is null", keyId)
                return null
            }

            if (secretKeyValue == null) {
                logger.warn("GPG secretKey {} is null", secretKey)
                return null
            }

            if (passwordValue == null) {
                logger.warn("GPG password {} is null", password)
                return null
            }

            return Gpg(keyIdValue, secretKeyValue, passwordValue)
        }
    }
}

inline fun SigningExtension.configSigning(
    gpg: Gpg?,
    publications: () -> DomainObjectCollection<Publication>
) {
    logger.info("Config signing, gpg is {} null, key id: {}", if (gpg == null) "" else "not", gpg?.keyId)
    val (keyId, secretKey, password) = gpg ?: return
    useInMemoryPgpKeys(keyId, secretKey, password)
    sign(publications()) //publishingExtension.publications)
}
