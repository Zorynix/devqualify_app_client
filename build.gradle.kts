plugins {
    kotlin("jvm") version "2.1.10"
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.android.application) apply false
    id("com.google.dagger.hilt.android") version "2.56.2" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0"
    id("com.google.gms.google-services") version "4.4.3" apply false
    id("com.google.protobuf") version "0.9.5" apply false
}

detekt {
    toolVersion = "1.23.8"
    config.setFrom(file("$rootDir/detekt.yml"))
    parallel = true
    source.setFrom(
        "app/src/main/java",
    )
    autoCorrect = true
}

dependencies {
    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.detekt)
    detektPlugins(libs.detekt.rules.compose)
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    ktlint {
        verbose.set(true)
        outputToConsole.set(true)
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        }
        filter {
            exclude("**/generated/**")
            include("**/*.kt")
        }
    }

    tasks.withType<Test> {
        testLogging {
            events("passed", "skipped", "failed", "standardOut", "standardError")
            outputs.upToDateWhen { false }
            showStandardStreams = true
            showExceptions = true
            showCauses = true
            showStackTraces = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL

            showStandardStreams = true
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
        
        afterSuite(KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
            if (desc.parent == null) {
                println("\nTest summary:")
                println("  Total: ${result.testCount}")
                println("  Passed: ${result.successfulTestCount}")
                println("  Failed: ${result.failedTestCount}")
                println("  Skipped: ${result.skippedTestCount}")
                println("  Success rate: ${if (result.testCount > 0) "%.2f".format((result.successfulTestCount.toDouble() / result.testCount) * 100) else "0"}%")
                println("  Time: ${result.endTime - result.startTime}ms\n")
            }
        }))
    }
}

tasks {
    register("format") {
        description = "Formats Kotlin code using ktlint"
        group = "formatting"
        dependsOn(":ktlintFormat")
    }

    register("checkAll") {
        description = "Checks code with ktlint and Detekt"
        group = "verification"
        dependsOn(":ktlintCheck", ":detekt")
    }
    
    register("uiTests") {
        description = "Runs UI tests (androidTest) with detailed output"
        group = "verification"
        dependsOn(":app:connectedDebugAndroidTest")
        doLast {
            println("\n=== UI Tests Summary ===")
            println("Check the test reports in app/build/reports/androidTests/connected/")
            println("========================\n")
        }
    }
}
