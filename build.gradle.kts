plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.10.0"
}

group = "org.msaraiva"
version = "1.2.0"

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

// Data class to hold extracted documentation (must be outside tasks block for function signature)
data class PropertyDocs(
    val enumValues: MutableMap<String, String> = mutableMapOf(),
    var syntax: String? = null,
    var cssExamples: String? = null,
    var pythonExamples: String? = null,
    val seeAlso: MutableList<String> = mutableListOf()
)

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

    // Download/update Textual documentation from GitHub
    register("downloadTextualDocs") {
        description = "Download/update Textual documentation from GitHub"
        group = "generation"

        val docsDir = file("misc/textual_docs")
        outputs.dir(docsDir)

        doLast {
            val docsPath = file("misc/textual_docs")

            if (!file("misc/textual_docs/.git").exists()) {
                // Clone with sparse checkout for docs only
                println("Downloading Textual documentation...")

                // Clone
                ProcessBuilder("git", "clone", "--depth", "1",
                    "--filter=blob:none", "--no-checkout",
                    "https://github.com/Textualize/textual.git",
                    "misc/textual_docs")
                    .inheritIO()
                    .start()
                    .waitFor()

                // Init sparse checkout
                ProcessBuilder("git", "sparse-checkout", "init", "--cone")
                    .directory(docsPath)
                    .inheritIO()
                    .start()
                    .waitFor()

                // Set sparse checkout to docs only
                ProcessBuilder("git", "sparse-checkout", "set", "docs")
                    .directory(docsPath)
                    .inheritIO()
                    .start()
                    .waitFor()

                // Checkout
                ProcessBuilder("git", "checkout")
                    .directory(docsPath)
                    .inheritIO()
                    .start()
                    .waitFor()

                println("Downloaded Textual documentation to misc/textual_docs/")
            } else {
                // Update existing docs
                println("Updating Textual documentation...")
                ProcessBuilder("git", "pull")
                    .directory(docsPath)
                    .inheritIO()
                    .start()
                    .waitFor()
                println("Updated Textual documentation")
            }
        }
    }

    // Generate TCSS documentation from Textual markdown files
    register("generateTcssDocumentation") {
        description = "Generate TCSS documentation from Textual markdown files"
        group = "generation"

        dependsOn("downloadTextualDocs")

        val docsBaseDir = file("misc/textual_docs/docs")
        val stylesDir = file("$docsBaseDir/styles")
        val cssTypesDir = file("$docsBaseDir/css_types")
        val outputDir = file("src/main/java/org/msaraiva/pytcss/metadata/generated")
        val outputFile = file("$outputDir/TcssPropertyDocumentation.java")
        val typeUrlsFile = file("$outputDir/TcssCssTypeUrls.java")
        val availablePropertiesFile = file("$outputDir/TcssAvailableProperties.java")

        inputs.dir(stylesDir)
        inputs.dir(cssTypesDir)
        outputs.file(outputFile)
        outputs.file(typeUrlsFile)
        outputs.file(availablePropertiesFile)

        doLast {
            outputDir.mkdirs()

            // Scan css_types directory to find available type documentation
            val availableCssTypes = mutableSetOf<String>()
            cssTypesDir.listFiles { file -> file.extension == "md" }?.forEach { mdFile ->
                val typeName = mdFile.nameWithoutExtension.replace("_", "-")
                availableCssTypes.add(typeName)
            }
            println("Found ${availableCssTypes.size} CSS type documentation files")

            // Scan styles directory to find available property documentation (recursively)
            // Store filename → relative path mapping for accurate URL generation
            val availableProperties = mutableMapOf<String, String>()
            fileTree(stylesDir) {
                include("**/*.md")
            }.forEach { mdFile ->
                val fileName = mdFile.nameWithoutExtension
                if (fileName != "_template" && fileName != "index") {
                    // Calculate relative path from docs root
                    val relativePath = mdFile.toRelativeString(docsBaseDir)
                        .replace("\\", "/")  // Normalize path separators
                        .removeSuffix(".md")  // Remove extension
                    availableProperties[fileName] = relativePath
                }
            }
            println("Found ${availableProperties.size} property documentation files")

            val propertyDocs = mutableMapOf<String, PropertyDocs>()

            // Function to convert markdown links to HTML links
            fun convertMarkdownLinksToHtml(text: String): String {
                var result = text

                // Convert CSS type links: [<color>](../css_types/color.md) or [`<color>`](../css_types/color.md)
                val cssTypeLinkRegex = Regex("""\[`?(<[^>]+>)`?\]\(\.\./css_types/([^)]+)\.md\)""")
                result = cssTypeLinkRegex.replace(result) { match ->
                    val linkText = match.groupValues[1]
                    val typeName = match.groupValues[2].replace("_", "-")
                    if (availableCssTypes.contains(typeName)) {
                        """<a href="https://textual.textualize.io/css_types/$typeName/">$linkText</a>"""
                    } else {
                        linkText // No link if documentation doesn't exist
                    }
                }

                // Convert property links: [background](./background.md) or [`background`](./background.md)
                val propertyLinkRegex = Regex("""\[`?([a-z\-]+)`?\]\(\.\/([^)]+)\.md\)""")
                result = propertyLinkRegex.replace(result) { match ->
                    val linkText = match.groupValues[1]
                    val fileName = match.groupValues[2] // Keep filename as-is (with underscores)
                    val relativePath = availableProperties[fileName]
                    if (relativePath != null) {
                        """<a href="https://textual.textualize.io/$relativePath/">$linkText</a>"""
                    } else {
                        linkText // No link if documentation doesn't exist
                    }
                }

                return result
            }

            // Parse markdown files to extract all documentation sections (recursively)
            fileTree(stylesDir) {
                include("**/*.md")
            }.forEach { mdFile ->
                val propertyName = mdFile.nameWithoutExtension.replace("_", "-")
                val content = mdFile.readText()
                val docs = PropertyDocs()

                // Extract enum value descriptions from markdown tables
                val tableRegex = Regex("""^\|\s*`?([a-z-]+)`?\s*(?:\(default\))?\s*\|\s*(.+?)\s*\|""", RegexOption.MULTILINE)
                tableRegex.findAll(content).forEach { match ->
                    val value = match.groupValues[1].trim()
                    val description = match.groupValues[2].trim()
                    if (value != "Value" && !value.matches(Regex("-+")) && value.isNotEmpty() && description.isNotEmpty()) {
                        docs.enumValues[value] = description
                    }
                }

                // Extract Syntax section (between ## Syntax and next ##)
                val syntaxRegex = Regex("""## Syntax\s*\n(.*?)(?=\n##|\z)""", setOf(RegexOption.DOT_MATCHES_ALL))
                val syntaxMatch = syntaxRegex.find(content)
                if (syntaxMatch != null) {
                    var syntax = syntaxMatch.groupValues[1].trim()
                    // Strip markdown code block markers and HTML comments
                    syntax = syntax.replace(Regex("```[a-z]*\n?"), "")
                        .replace(Regex("--8<--[^\n]*\n?"), "")
                        .replace(Regex("<a[^>]*>"), "")
                        .replace(Regex("</a>"), "")
                        .trim()
                    // Convert markdown links to HTML
                    syntax = convertMarkdownLinksToHtml(syntax)
                    if (syntax.isNotEmpty()) {
                        docs.syntax = syntax
                    }
                }

                // Extract CSS section - find first code block after ## CSS heading
                val cssRegex = Regex("""## CSS.*?```[a-z]*\s+(.*?)```""", setOf(RegexOption.DOT_MATCHES_ALL))
                val cssMatch = cssRegex.find(content)
                if (cssMatch != null) {
                    val css = cssMatch.groupValues[1].trim()
                    if (css.isNotEmpty()) {
                        docs.cssExamples = css
                    }
                }

                // Extract Python section - find first python/py code block after ## Python heading
                val pythonRegex = Regex("""## Python.*?```(?:python|py)\s+(.*?)```""", setOf(RegexOption.DOT_MATCHES_ALL))
                val pythonMatch = pythonRegex.find(content)
                if (pythonMatch != null) {
                    val python = pythonMatch.groupValues[1].trim()
                    if (python.isNotEmpty()) {
                        docs.pythonExamples = python
                    }
                }

                // Extract See also section - parse markdown links
                val seeAlsoRegex = Regex("""## See also\s*\n(.*?)(?=\n##|\z)""", setOf(RegexOption.DOT_MATCHES_ALL))
                val seeAlsoMatch = seeAlsoRegex.find(content)
                if (seeAlsoMatch != null) {
                    val seeAlsoText = seeAlsoMatch.groupValues[1]
                    // Extract property names from markdown links like [`visibility`](./visibility.md)
                    val linkRegex = Regex("""\[`?([a-z\-]+)`?\]\([^)]+\)""")
                    linkRegex.findAll(seeAlsoText).forEach { linkMatch ->
                        val relatedProperty = linkMatch.groupValues[1].trim()
                        if (relatedProperty.isNotEmpty()) {
                            docs.seeAlso.add(relatedProperty)
                        }
                    }
                }

                // Only add if we extracted something useful
                if (docs.enumValues.isNotEmpty() || docs.syntax != null ||
                    docs.cssExamples != null || docs.pythonExamples != null || docs.seeAlso.isNotEmpty()) {
                    propertyDocs[propertyName] = docs
                }
            }

            // Generate Java source code
            outputFile.writeText(generatePropertyDocumentationClass(propertyDocs))
            println("Generated TcssPropertyDocumentation.java with ${propertyDocs.size} properties")

            // Generate CSS type URLs mapping
            typeUrlsFile.writeText(generateCssTypeUrlsClass(availableCssTypes))
            println("Generated TcssCssTypeUrls.java with ${availableCssTypes.size} CSS types")

            // Generate available properties map (filename → relative path)
            availablePropertiesFile.writeText(generateAvailablePropertiesClass(availableProperties))
            println("Generated TcssAvailableProperties.java with ${availableProperties.size} property filenames")
        }
    }

    // Make compileJava depend on generateTcssDocumentation
    compileJava {
        dependsOn("generateTcssDocumentation")
    }
}

// Escape string for Java source code literals
fun escapeJavaString(str: String): String {
    return str
        .replace("\\", "\\\\")   // Escape backslashes first
        .replace("\"", "\\\"")    // Escape quotes
        .replace("\n", "\\n")     // Escape newlines
        .replace("\r", "\\r")     // Escape carriage returns
        .replace("\t", "\\t")     // Escape tabs
}

fun generatePropertyDocumentationClass(propertyDocs: Map<String, PropertyDocs>): String {
    val builder = StringBuilder()

    builder.appendLine("package org.msaraiva.pytcss.metadata.generated;")
    builder.appendLine()
    builder.appendLine("import org.jetbrains.annotations.NotNull;")
    builder.appendLine("import org.jetbrains.annotations.Nullable;")
    builder.appendLine()
    builder.appendLine("import java.util.Collections;")
    builder.appendLine("import java.util.LinkedHashMap;")
    builder.appendLine("import java.util.List;")
    builder.appendLine("import java.util.ArrayList;")
    builder.appendLine("import java.util.Locale;")
    builder.appendLine("import java.util.Map;")
    builder.appendLine()
    builder.appendLine("/**")
    builder.appendLine(" * Generated by Gradle task 'generateTcssDocumentation' - DO NOT EDIT MANUALLY")
    builder.appendLine(" * <p>")
    builder.appendLine(" * Property documentation extracted from Textual documentation markdown files.")
    builder.appendLine(" * Includes: enum value descriptions, syntax, CSS examples, Python examples, and related properties.")
    builder.appendLine(" */")
    builder.appendLine("public final class TcssPropertyDocumentation {")
    builder.appendLine("    private static final Map<String, Map<String, String>> ENUM_VALUE_DESCRIPTIONS;")
    builder.appendLine("    private static final Map<String, String> SYNTAX;")
    builder.appendLine("    private static final Map<String, String> CSS_EXAMPLES;")
    builder.appendLine("    private static final Map<String, String> PYTHON_EXAMPLES;")
    builder.appendLine("    private static final Map<String, List<String>> SEE_ALSO;")
    builder.appendLine()
    builder.appendLine("    static {")

    // Generate enum value descriptions
    builder.appendLine("        // Enum value descriptions")
    builder.appendLine("        Map<String, Map<String, String>> enumMap = new LinkedHashMap<>();")
    propertyDocs.filter { it.value.enumValues.isNotEmpty() }.forEach { (property, docs) ->
        builder.appendLine("        // $property enum values")
        val varName = property.replace("-", "_") + "_enum"
        builder.appendLine("        Map<String, String> $varName = new LinkedHashMap<>();")
        docs.enumValues.forEach { (value, description) ->
            val escapedDesc = escapeJavaString(description)
            builder.appendLine("        $varName.put(\"$value\", \"$escapedDesc\");")
        }
        builder.appendLine("        enumMap.put(\"$property\", $varName);")
    }
    builder.appendLine("        ENUM_VALUE_DESCRIPTIONS = Collections.unmodifiableMap(enumMap);")
    builder.appendLine()

    // Generate syntax
    builder.appendLine("        // Syntax definitions")
    builder.appendLine("        Map<String, String> syntaxMap = new LinkedHashMap<>();")
    propertyDocs.filter { it.value.syntax != null }.forEach { (property, docs) ->
        val escapedSyntax = escapeJavaString(docs.syntax!!)
        builder.appendLine("        syntaxMap.put(\"$property\", \"$escapedSyntax\");")
    }
    builder.appendLine("        SYNTAX = Collections.unmodifiableMap(syntaxMap);")
    builder.appendLine()

    // Generate CSS examples
    builder.appendLine("        // CSS examples")
    builder.appendLine("        Map<String, String> cssMap = new LinkedHashMap<>();")
    propertyDocs.filter { it.value.cssExamples != null }.forEach { (property, docs) ->
        val escapedCss = escapeJavaString(docs.cssExamples!!)
        builder.appendLine("        cssMap.put(\"$property\", \"$escapedCss\");")
    }
    builder.appendLine("        CSS_EXAMPLES = Collections.unmodifiableMap(cssMap);")
    builder.appendLine()

    // Generate Python examples
    builder.appendLine("        // Python examples")
    builder.appendLine("        Map<String, String> pythonMap = new LinkedHashMap<>();")
    propertyDocs.filter { it.value.pythonExamples != null }.forEach { (property, docs) ->
        val escapedPython = escapeJavaString(docs.pythonExamples!!)
        builder.appendLine("        pythonMap.put(\"$property\", \"$escapedPython\");")
    }
    builder.appendLine("        PYTHON_EXAMPLES = Collections.unmodifiableMap(pythonMap);")
    builder.appendLine()

    // Generate see also
    builder.appendLine("        // See also (related properties)")
    builder.appendLine("        Map<String, List<String>> seeAlsoMap = new LinkedHashMap<>();")
    propertyDocs.filter { it.value.seeAlso.isNotEmpty() }.forEach { (property, docs) ->
        val varName = property.replace("-", "_") + "_related"
        builder.appendLine("        List<String> $varName = new ArrayList<>();")
        docs.seeAlso.forEach { related ->
            builder.appendLine("        $varName.add(\"$related\");")
        }
        builder.appendLine("        seeAlsoMap.put(\"$property\", $varName);")
    }
    builder.appendLine("        SEE_ALSO = Collections.unmodifiableMap(seeAlsoMap);")
    builder.appendLine("    }")
    builder.appendLine()

    builder.appendLine("    private TcssPropertyDocumentation() {")
    builder.appendLine("        // Utility class")
    builder.appendLine("    }")
    builder.appendLine()

    // Generate accessor methods
    builder.appendLine("    /**")
    builder.appendLine("     * Gets the description for a specific enum value of a property.")
    builder.appendLine("     *")
    builder.appendLine("     * @param property the property name (e.g., \"display\", \"text-overflow\")")
    builder.appendLine("     * @param value the enum value (e.g., \"block\", \"ellipsis\")")
    builder.appendLine("     * @return the description, or null if not found")
    builder.appendLine("     */")
    builder.appendLine("    @Nullable")
    builder.appendLine("    public static String getEnumValueDescription(@NotNull String property, @NotNull String value) {")
    builder.appendLine("        Map<String, String> propertyMap = ENUM_VALUE_DESCRIPTIONS.get(property.toLowerCase(Locale.US));")
    builder.appendLine("        return propertyMap != null ? propertyMap.get(value.toLowerCase(Locale.US)) : null;")
    builder.appendLine("    }")
    builder.appendLine()

    builder.appendLine("    /**")
    builder.appendLine("     * Gets the syntax for a property.")
    builder.appendLine("     *")
    builder.appendLine("     * @param property the property name")
    builder.appendLine("     * @return the syntax string, or null if not found")
    builder.appendLine("     */")
    builder.appendLine("    @Nullable")
    builder.appendLine("    public static String getSyntax(@NotNull String property) {")
    builder.appendLine("        return SYNTAX.get(property.toLowerCase(Locale.US));")
    builder.appendLine("    }")
    builder.appendLine()

    builder.appendLine("    /**")
    builder.appendLine("     * Gets CSS examples for a property.")
    builder.appendLine("     *")
    builder.appendLine("     * @param property the property name")
    builder.appendLine("     * @return the CSS examples string, or null if not found")
    builder.appendLine("     */")
    builder.appendLine("    @Nullable")
    builder.appendLine("    public static String getCssExamples(@NotNull String property) {")
    builder.appendLine("        return CSS_EXAMPLES.get(property.toLowerCase(Locale.US));")
    builder.appendLine("    }")
    builder.appendLine()

    builder.appendLine("    /**")
    builder.appendLine("     * Gets Python examples for a property.")
    builder.appendLine("     *")
    builder.appendLine("     * @param property the property name")
    builder.appendLine("     * @return the Python examples string, or null if not found")
    builder.appendLine("     */")
    builder.appendLine("    @Nullable")
    builder.appendLine("    public static String getPythonExamples(@NotNull String property) {")
    builder.appendLine("        return PYTHON_EXAMPLES.get(property.toLowerCase(Locale.US));")
    builder.appendLine("    }")
    builder.appendLine()

    builder.appendLine("    /**")
    builder.appendLine("     * Gets related properties (see also) for a property.")
    builder.appendLine("     *")
    builder.appendLine("     * @param property the property name")
    builder.appendLine("     * @return list of related property names, or empty list if none")
    builder.appendLine("     */")
    builder.appendLine("    @NotNull")
    builder.appendLine("    public static List<String> getSeeAlso(@NotNull String property) {")
    builder.appendLine("        List<String> related = SEE_ALSO.get(property.toLowerCase(Locale.US));")
    builder.appendLine("        return related != null ? related : Collections.emptyList();")
    builder.appendLine("    }")
    builder.appendLine("}")

    return builder.toString()
}

fun generateCssTypeUrlsClass(availableTypes: Set<String>): String {
    val builder = StringBuilder()

    builder.appendLine("package org.msaraiva.pytcss.metadata.generated;")
    builder.appendLine()
    builder.appendLine("import org.jetbrains.annotations.Nullable;")
    builder.appendLine()
    builder.appendLine("import java.util.HashMap;")
    builder.appendLine("import java.util.Map;")
    builder.appendLine()
    builder.appendLine("/**")
    builder.appendLine(" * Generated by Gradle task 'generateTcssDocumentation' - DO NOT EDIT MANUALLY")
    builder.appendLine(" * <p>")
    builder.appendLine(" * Maps CSS type names to documentation URLs based on available documentation files.")
    builder.appendLine(" */")
    builder.appendLine("public final class TcssCssTypeUrls {")
    builder.appendLine("    private static final Map<String, String> TYPE_TO_URL;")
    builder.appendLine()
    builder.appendLine("    static {")
    builder.appendLine("        TYPE_TO_URL = new HashMap<>();")

    // Generate entries for each available type, sorted alphabetically
    availableTypes.sorted().forEach { typeName ->
        val url = "https://textual.textualize.io/css_types/$typeName/"
        builder.appendLine("        TYPE_TO_URL.put(\"$typeName\", \"$url\");")
    }

    builder.appendLine("    }")
    builder.appendLine()
    builder.appendLine("    private TcssCssTypeUrls() {")
    builder.appendLine("        // Utility class")
    builder.appendLine("    }")
    builder.appendLine()
    builder.appendLine("    /**")
    builder.appendLine("     * Gets the documentation URL for a CSS type.")
    builder.appendLine("     *")
    builder.appendLine("     * @param typeName the CSS type name (e.g., \"color\", \"scalar\", \"border\")")
    builder.appendLine("     * @return the documentation URL, or null if type has no documentation")
    builder.appendLine("     */")
    builder.appendLine("    @Nullable")
    builder.appendLine("    public static String getTypeUrl(String typeName) {")
    builder.appendLine("        return TYPE_TO_URL.get(typeName);")
    builder.appendLine("    }")
    builder.appendLine("}")

    return builder.toString()
}

fun generateAvailablePropertiesClass(availableProperties: Map<String, String>): String {
    val builder = StringBuilder()

    builder.appendLine("package org.msaraiva.pytcss.metadata.generated;")
    builder.appendLine()
    builder.appendLine("import java.util.HashMap;")
    builder.appendLine("import java.util.Map;")
    builder.appendLine("import org.jetbrains.annotations.Nullable;")
    builder.appendLine()
    builder.appendLine("/**")
    builder.appendLine(" * Generated by Gradle task 'generateTcssDocumentation' - DO NOT EDIT MANUALLY")
    builder.appendLine(" * <p>")
    builder.appendLine(" * Maps property filenames (with underscores) to their relative paths for documentation URLs.")
    builder.appendLine(" * Used to generate correct documentation links including subdirectory structure.")
    builder.appendLine(" */")
    builder.appendLine("public final class TcssAvailableProperties {")
    builder.appendLine("    private static final Map<String, String> PROPERTY_PATHS;")
    builder.appendLine()
    builder.appendLine("    static {")
    builder.appendLine("        PROPERTY_PATHS = new HashMap<>();")

    // Generate entries for each available property, sorted alphabetically by filename
    availableProperties.toSortedMap().forEach { (fileName, relativePath) ->
        builder.appendLine("        PROPERTY_PATHS.put(\"$fileName\", \"$relativePath\");")
    }

    builder.appendLine("    }")
    builder.appendLine()
    builder.appendLine("    private TcssAvailableProperties() {")
    builder.appendLine("        // Utility class")
    builder.appendLine("    }")
    builder.appendLine()
    builder.appendLine("    /**")
    builder.appendLine("     * Checks if a property filename has documentation.")
    builder.appendLine("     *")
    builder.appendLine("     * @param fileName the property filename (with underscores, e.g., \"box_sizing\")")
    builder.appendLine("     * @return true if documentation exists for this property")
    builder.appendLine("     */")
    builder.appendLine("    public static boolean hasDocumentation(String fileName) {")
    builder.appendLine("        return PROPERTY_PATHS.containsKey(fileName);")
    builder.appendLine("    }")
    builder.appendLine()
    builder.appendLine("    /**")
    builder.appendLine("     * Gets the relative path for a property's documentation.")
    builder.appendLine("     *")
    builder.appendLine("     * @param fileName the property filename (with underscores, e.g., \"grid_columns\")")
    builder.appendLine("     * @return the relative path (e.g., \"styles/grid/grid_columns\"), or null if no documentation")
    builder.appendLine("     */")
    builder.appendLine("    @Nullable")
    builder.appendLine("    public static String getRelativePath(String fileName) {")
    builder.appendLine("        return PROPERTY_PATHS.get(fileName);")
    builder.appendLine("    }")
    builder.appendLine("}")

    return builder.toString()
}

// Test source sets
sourceSets {
    test {
        java.srcDir("src/test/java")
        resources.srcDir("src/test/testData")
    }
}
