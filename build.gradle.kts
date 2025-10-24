plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.10.0"
}

group = "io.textual"
version = "1.1.0"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        pycharmCommunity("2024.3.6")
        bundledPlugin("PythonCore")

        pluginVerifier()
        zipSigner()

        // Test framework
        // Using fully qualified name to avoid import issues with IntelliJ's Kotlin script engine
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
}

// Configure Gradle IntelliJ Platform Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "243"
            untilBuild = provider { null }
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    // Test configuration
    test {
        useJUnit()
    }
}

// Test source sets
sourceSets {
    test {
        java.srcDir("src/test/java")
        resources.srcDir("src/test/testData")
    }
}
