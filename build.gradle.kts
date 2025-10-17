plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.10.0"
}

group = "io.textual"
version = "1.0.0"

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
    }
}

// Configure Gradle IntelliJ Platform Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
intellijPlatform {
    buildSearchableOptions = false
    
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "243"
            untilBuild = "252.*"
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
}
