plugins {
    id("signing")
    id("maven-publish")
}

val p = project

val isSnapshot = project.version.toString().contains("SNAPSHOT", true)
val jarSources by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val jarJavadoc by tasks.registering(Jar::class) {
    if (!isSnapshot) {
        dependsOn(tasks.dokkaHtml)
        from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    }
    archiveClassifier.set("javadoc")
}

publishing {
    repositories {
        configPublishRepositories(isSnapshot)
    }

    publications {
        create<MavenPublication>("kopperDist") {
            from(components.getByName("java"))
            artifacts {
                artifact(jarSources)
                artifact(jarJavadoc)
            }

            pom {
                configPom(project.name)
            }
            showMaven()
        }
    }
}

signing {
    configSigning(Gpg.ofSystemPropOrNull()) { publishing.publications }
}

fun MavenPublication.showMaven() {
    val pom = pom
    // // show project info
    logger.lifecycle(
        """
        |=======================================================
        |= jvm.maven.name:            {}
        |= jvm.maven.groupId:         {}
        |= jvm.maven.artifactId:      {}
        |= jvm.maven.version:         {}
        |= jvm.maven.pom.description: {}
        |= jvm.maven.pom.name:        {}
        |=======================================================
        """.trimIndent(),
        name,
        groupId,
        artifactId,
        version,
        pom.description.get(),
        pom.name.get(),
    )
}

internal val TaskContainer.dokkaHtml: TaskProvider<org.jetbrains.dokka.gradle.DokkaTask>
    get() = named<org.jetbrains.dokka.gradle.DokkaTask>("dokkaHtml")
