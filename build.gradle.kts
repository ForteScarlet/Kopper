plugins {
    `kopper-dokka-multi-module`
    `kopper-nexus-publish`
}

group = P.GROUP

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

    version = if (isSnapshot()) {
        P.NEXT_SNAP_VERSION
    } else {
        P.VERSION
    }

    repositories {
        mavenCentral()
    }
}
