# Textual CSS (TCSS) PyCharm Plugin

A PyCharm/IntelliJ IDEA plugin that provides syntax highlighting support for Textual CSS files.

## What is Textual CSS?

Textual CSS (TCSS) is a stylesheet language used by [Textual](https://textual.textualize.io/), a TUI (Text User Interface) framework for Python. It's similar to web CSS but designed specifically for terminal applications.

Learn more at: https://textual.textualize.io/guide/CSS/

## Plugin Features

### Core Language Support
- Syntax highlighting for `.tcss` files
- Support for all TCSS selectors (type, class, ID, pseudo-class)
- Variable support (`$variable` declarations and references)
- Nested rule sets with `&` nesting selector
- Full Textual-specific property coverage
- Color scheme customization

### Color System
- Color previews in the gutter for all formats (hex, RGB, HSL, named colors)
- Variable color resolution (shows colors for `$primary` references)
- Cross-file variable resolution (use variables from any `.tcss` file in your project)
- Opacity suffix support (`red 50%`, `#fff 80%`)
- Color picker with format preservation
- Python `CSS`/`DEFAULT_CSS` injection for inline TCSS

### Editor Experience
- Code completion with color preview icons (properties, variables, color keywords)
- Smart `$` completion (auto-removes `$` for named colors, keeps it for variables)
- Configurable completion behavior (Settings → Languages & Frameworks → Textual CSS)
- Enum value completion for 15+ properties (display, layout, border, overflow, etc.)
- Fuzzy suggestions for property name typos ("Did you mean...?")
- Pseudo-class validation (all 19 valid pseudo-classes with error messages)
- Grammar support for `!important` modifier and `initial` keyword
- Live templates for common TCSS patterns
- Structure view (outline of rules and variables)
- Quick documentation on hover
- Validation and diagnostics (including duplicate variable warnings)
- Rename refactoring for variables

## Installation

### From GitHub Releases (Recommended)

1. Go to the [Releases page](https://github.com/mrsaraiva/pytcss/releases)
2. Download the latest `.zip` file from the release assets
3. In PyCharm/IntelliJ IDEA, open:
   - **File → Settings → Plugins** (Windows/Linux)
   - **PyCharm → Preferences → Plugins** (macOS)
4. Click the gear icon ⚙️ at the top and select **Install Plugin from Disk...**
5. Navigate to and select the downloaded `.zip` file
6. Click **OK** and restart the IDE when prompted

### From JetBrains Marketplace (Coming Soon)

Once published, you'll be able to install directly from the IDE:
1. Go to **Settings → Plugins**
2. Click the **Marketplace** tab
3. Search for "Textual CSS"
4. Click **Install**
5. Restart the IDE when prompted

### From Source

1. Clone this repository
2. Build the plugin:
   ```bash
   ./gradlew buildPlugin
   ```
3. The plugin ZIP will be created in `build/distributions/`
4. Follow the steps in "From GitHub Releases" above, using your built ZIP file

## Building

### Prerequisites

- JDK 17 or higher
- Gradle (wrapper included)

### Build Commands

```bash
# Build the plugin
./gradlew buildPlugin

# Run the plugin in a sandbox IDE for testing
./gradlew runIde

# Build and verify the plugin
./gradlew verifyPlugin
```

## Development

### Project Structure

```
tcss-pycharm-plugin/
├── src/main/
│   ├── java/io/textual/tcss/
│   │   ├── color/                         # Color parsing utilities
│   │   ├── psi/                           # Structured PSI elements (rules, properties, variables)
│   │   ├── util/                          # VariableResolver and helpers
│   │   ├── python/                        # Python CSS/DEFAULT_CSS injection
│   │   ├── completion/                    # Code completion contributor, color icons
│   │   ├── index/                         # File-based indexing for cross-file resolution
│   │   ├── inspection/                    # Inspections (duplicate variables, etc.)
│   │   ├── settings/                      # Plugin settings UI and persistence
│   │   ├── structure/                     # Structure view
│   │   ├── documentation/                 # Documentation provider
│   │   ├── validation/                    # Annotator for diagnostics
│   │   ├── templates/                     # Live template context
│   │   ├── metadata/                      # Property catalog
│   │   ├── TcssLanguage.java              # Language definition
│   │   ├── TcssFileType.java              # File type definition
│   │   ├── TcssLexer.java                 # Lexical analyzer
│   │   ├── TcssParser.java                # Recursive-descent parser
│   │   ├── TcssTokenTypes.java            # Token type definitions
│   │   ├── TcssColorProvider.java         # ElementColorProvider for gutter previews
│   │   ├── TcssColorSettingsPage.java     # Color customization UI
│   │   └── TcssElementFactory.java        # PSI element factory for refactoring
│   └── resources/
│       ├── META-INF/
│       │   ├── plugin.xml                 # Plugin configuration
│       │   └── liveTemplates/tcss.xml     # Live templates
│       └── icons/                         # Plugin icons
├── src/test/                              # Automated tests
│   ├── java/io/textual/tcss/             # Test classes
│   └── testData/                          # Test fixtures
├── examples/                              # Example TCSS files
├── docs/devel/                            # Development documentation
├── build.gradle.kts                       # Build configuration
├── settings.gradle.kts                    # Gradle settings
└── README.md                              # This file
```

## Supported TCSS Features

### Selectors
- **Type selectors**: `Button`, `Static`, `Container`
- **Class selectors**: `.success`, `.error`, `.disabled`
- **ID selectors**: `#dialog`, `#sidebar`
- **Pseudo-classes**: All 19 Textual pseudo-classes validated (`:hover`, `:focus`, `:active`, `:disabled`, `:light`, `:dark`, `:blur`, `:can-focus`, `:has-children`, `:first-child`, `:last-child`, `:odd-child`, `:even-child`, `:only-child`, `:focus-within`, `:inline`, `:inline-block`, `:vertical-scroll`, `:horizontal-scroll`)
- **Combinators**: `>`, `~`, `+`
- **Nesting selector**: `&`

### Variables
```tcss
$primary: blue;
$border: wide $primary;
```

### Properties
115 Textual CSS properties supported, including:
- Layout: `dock`, `width`, `height`, `align`, `display`
- Styling: `background`, `color`, `border`, `text-style`
- Grid: `grid-columns`, `grid-rows`, `grid-gutter`
- Spacing: `margin`, `padding`, `offset`
- And many more...

### Comments
```tcss
/* Block comments are supported */
```

### Nested Rules
```tcss
#questions {
    border: heavy $primary;
    
    .button {
        width: 1fr;
        
        &.affirmative {
            border: heavy green;
        }
    }
}
```

## Python Integration

The plugin automatically injects TCSS language support into Python `CSS` and `DEFAULT_CSS` class variables:

```python
from textual.app import App

class MyApp(App):
    CSS = """
    Button {
        background: $primary;  # ← Color preview works here!
        color: white;
    }
    """

    DEFAULT_CSS = """
    .error { color: red; }  # ← Syntax highlighting + completion!
    """
```

This works automatically - no configuration needed!

## Roadmap

### v1.1 (Current)
- Cross-file variable resolution (use variables from any `.tcss` file)
- Color preview icons in autocomplete
- Smart `$` completion (auto-removes for colors, keeps for variables)
- Configurable completion behavior via settings page
- Duplicate variable inspection with navigation
- Performance optimizations for variable resolution
- Automated testing framework
- Grammar completeness: `!important` modifier and `initial` keyword support
- Pseudo-class validation (19 pseudo-classes with fuzzy error suggestions)
- Fuzzy property name suggestions for typos
- Enum value completion for 15+ enumerated properties
- Property catalog expansion (17 new properties, 115 total)

### v1.0 (Released)
- Color preview for static and computed/dynamic values
- Python TCSS injection for `CSS` / `DEFAULT_CSS`
- Opacity suffix support (`color: red 50%;`)
- Code completion (properties, variables, color keywords)
- Live templates for common patterns
- Documentation on hover for all properties
- Variable rename refactoring (Shift+F6)
- Go-to-declaration for variables (Ctrl+Click)
- Find usages for variables (Alt+F7)
- Validation and diagnostics:
  - Unknown property warnings
  - Undefined variable errors
  - Circular reference detection
  - Invalid color keyword warnings

### Future Enhancements
- Find usages for selectors (classes, IDs, type selectors)
- Unused variable detection
- Invalid property value inspections
- Enhanced error recovery in parser
- Refactoring support for selectors

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Related Projects

- [Textual](https://github.com/Textualize/textual) - The TUI framework
- [TCSS TextMate Grammar](https://github.com/Textualize/tcss-textmate-grammar) - Original TextMate grammar
- [TCSS VSCode Extension](https://github.com/Textualize/tcss-vscode-extension) - VSCode extension

## Support

- Documentation: https://textual.textualize.io/guide/CSS/
- Issues: https://github.com/mrsaraiva/pytcss/issues
- Textual Discord: https://discord.gg/Enf6Z3qhVr

## Acknowledgments

- Based on the [TCSS TextMate grammar](https://github.com/Textualize/tcss-textmate-grammar) by Textualize
- Inspired by the [TCSS VSCode extension](https://github.com/Textualize/tcss-vscode-extension)
