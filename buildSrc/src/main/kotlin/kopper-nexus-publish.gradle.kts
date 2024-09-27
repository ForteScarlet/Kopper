import java.time.Duration

plugins {
    id("io.github.gradle-nexus.publish-plugin")
}

val userInfo = sonatypeUserInfoOrNull

if (userInfo == null) {
    logger.warn("sonatype.username or sonatype.password is null, cannot config nexus publishing.")
}

nexusPublishing {
    packageGroup.set(P.GROUP)
    useStaging = project.provider { !project.version.toString().endsWith("SNAPSHOT", ignoreCase = true) }

    transitionCheckOptions {
        maxRetries = 1000
        delayBetween = Duration.ofSeconds(2)
    }

    repositories {
        sonatype {
            snapshotRepositoryUrl.set(uri("https://oss.sonatype.org/content/repositories/snapshots/"))
            username.set(userInfo?.username)
            password.set(userInfo?.password)
        }
    }
}
