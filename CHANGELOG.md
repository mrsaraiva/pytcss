# Changelog

All notable changes to the Textual CSS (TCSS) PyCharm Plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-10-17

### Added

#### Core Language Support
- Syntax highlighting for `.tcss` files with support for all TCSS token types
- Support for all TCSS selectors: type, class, ID, pseudo-class
- Variable support (`$variable` declarations and references)
- Nested rule sets with `&` nesting selector
- Block comment support (`/* ... */`)
- Color scheme customization (Settings → Editor → Color Scheme → TCSS)

#### Color System
- Color preview gutter icons for all color formats:
  - Hex colors (`#0066cc`)
  - Named colors (`red`, `blue`, `green`, etc.)
  - RGB/HSL function calls (`rgb(255, 0, 0)`, `hsl(240, 100%, 50%)`)
- Variable color resolution with gutter icons for `$variable` references
- Chained variable resolution (`$accent: $base-color`)
- Redefined variable handling (last declaration wins)
- Opacity suffix support (`red 50%`, `#0066cc 70%`, `$primary 80%`)
- Integrated color picker with format preservation
- Circular reference detection (no crashes or infinite loops)
- Undefined variable detection with warning annotations

#### Python Integration
- Language injection for inline TCSS in Python files
- Automatic detection of `CSS` class variables in Textual apps
- Automatic detection of `DEFAULT_CSS` class variables in Textual apps
- Full TCSS features available in injected contexts (highlighting, colors, completion)

#### Editor Experience
- Code completion for TCSS properties (context-aware, 67+ properties supported)
- Code completion for variables in property values
- Code completion for color keywords
- Structure view showing variables and rule sets
- Quick documentation on hover (Ctrl+Q) for TCSS properties
- Go-to-declaration for variables (Ctrl+Click)
- Find usages for variables (Alt+F7)
- Live templates for common patterns:
  - `var` → `$variable-name: value;`
  - `rule` → `selector { property: value; }`
- Validation and diagnostics:
  - Unknown property warnings (checks against catalog of 67+ properties)
  - Undefined variable errors
  - Circular reference detection
  - Invalid color keyword warnings

#### Refactoring
- Variable rename refactoring (Shift+F6)
- Renames both declarations and all references
- Works in both `.tcss` files and Python injection contexts

### Technical Details
- Target IDE: PyCharm Community 2025.1.*
- IDE version range: 243 to 252.* (IntelliJ Platform 2024.3+)
- Java: JDK 17 (source and target)
- Plugin architecture: Custom PSI structure with proper semantic analysis

[1.0.0]: https://github.com/mrsaraiva/pytcss/releases/tag/v1.0.0
