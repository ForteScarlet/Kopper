plugins {
    `kopper-dokka-multi-module`
    `kopper-nexus-publish`
}

group = P.GROUP
description = P.DESCRIPTION
version = if (isSnapshot()) {
    P.NEXT_SNAP_VERSION
} else {
    P.VERSION
}

repositories {
    mavenCentral()
}

allprojects {
    group = P.GROUP
    description = P.DESCRIPTION

    version = if (isSnapshot()) {
        P.NEXT_SNAP_VERSION
    } else {
        P.VERSION
    }

    repositories {
        mavenCentral()
    }
}
