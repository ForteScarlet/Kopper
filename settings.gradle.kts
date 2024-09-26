plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "Kopper"

include(":kopper-annotation")
include(":kopper-common")
include(":kopper-processor")
include(":kopper-processor:kopper-processor-test")
