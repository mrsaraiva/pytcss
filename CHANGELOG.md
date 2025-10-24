# Changelog

All notable changes to the Textual CSS (TCSS) PyCharm Plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

#### Cross-File Variable Resolution
- **Project-wide variable resolution**: Variables declared in any `.tcss` file can now be used across the entire project
- **File-based indexing**: Implemented `TcssVariableIndex` using IntelliJ's `ScalarIndexExtension` for O(1) variable lookups
- **Local shadowing semantics**: File-local variables always take precedence over cross-file declarations
- **Cross-file validation**: Undefined variable errors now check the entire project, not just the current file
- **Cross-file color resolution**: Color gutter icons and picker now work with variables from other files
- **Duplicate variable inspection**: Warns when the same variable is declared in multiple files
  - Quick-fix navigation showing all declaration locations in a popup chooser
  - Inspect via Settings → Editor → Inspections → TCSS → Duplicate variable declaration

#### Enhanced Autocomplete
- **Color preview icons**: Autocomplete now shows colored square icons for:
  - Named colors (`seashell`, `seagreen`, etc.)
  - Variables (showing their resolved color)
  - Adaptive borders based on color brightness for better visibility
- **Smart $ removal**: When typing `$` and selecting a named color, the `$` is automatically removed
  - Variables keep the `$` prefix (e.g., typing `$sec` → selecting `$secondary` → inserts `$secondary`)
  - Named colors remove the `$` (e.g., typing `$sea` → selecting `seashell` → inserts `seashell`)
- **Cross-file variable completion**: Variables from other files now appear in autocomplete with source file information
  - Local variables shown as `variable (local)`
  - Cross-file variables shown as `variable (filename.tcss)` with `(project-wide)` tail text

#### Plugin Settings
- **Configurable completion behavior**: New settings page at Settings → Languages & Frameworks → Textual CSS
- **Dollar prefix filtering**: Control whether typing `$` shows both variables and colors, or only variables
  - When enabled (default): Typing `$` shows both variables and named colors (with smart removal)
  - When disabled: Typing `$` only shows variables
  - Setting takes effect immediately without IDE restart

#### Property Catalog Improvements
- **Expanded property coverage**: Added 28 missing TCSS properties to `TcssPropertyCatalog`
    - Total properties: 98 (up from 70)
    - Alignment variants: `align-horizontal`, `align-vertical`
    - Border sides: `border-bottom`, `border-left`, `border-right`, `border-top`
    - Content alignment: `content-align-horizontal`, `content-align-vertical`
    - Margin sides: `margin-bottom`, `margin-left`, `margin-right`, `margin-top`
    - Offset directions: `offset-x`, `offset-y`
    - Outline sides: `outline-bottom`, `outline-left`, `outline-right`, `outline-top`
    - Overflow directions: `overflow-x`, `overflow-y`
    - Padding sides: `padding-bottom`, `padding-left`, `padding-right`, `padding-top`
    - Scrollbar sizing: `scrollbar-size-horizontal`, `scrollbar-size-vertical`
    - Additional properties: `constrain`, `overlay`
    - All new properties now available in autocomplete, validation, and documentation

#### Testing Infrastructure
- Automated test framework setup with 5 test cases:
  - `testCrossFileColorResolution()`: Verifies cross-file variable resolution
  - `testLocalShadowing()`: Ensures local variables override cross-file ones
  - `testDuplicateDetection()`: Validates duplicate variable detection
  - `testUndefinedVariableError()`: Confirms undefined variables return null
  - `testCrossFileCompletion()`: Checks cross-file variables appear in completion

#### Examples
- Added `examples/` directory with 5 demonstration files:
  - `colors.tcss`: Common color variable definitions
  - `theme.tcss`: Theme variables with intentional duplicate
  - `main.tcss`: Uses cross-file variables
  - `layout.tcss`: Demonstrates local shadowing
  - `README.md`: Testing instructions

#### Grammar Completeness
- **!important modifier support**: Full parsing, highlighting, and semantic support
  - Works with all value types (colors, variables, numbers, enum values, initial keyword)
  - Distinct grey highlighting for entire `!important` modifier
  - Proper PSI structure with `IMPORTANT_MODIFIER` composite element
  - Tested with and without semicolons, various whitespace patterns
- **initial keyword support**: Works universally for all TCSS properties
  - Purple highlighting to distinguish from property names
  - `TcssInitialKeyword` PSI element for semantic analysis
  - Compatible with `!important` modifier (`padding: initial !important;`)
  - Full lexer and parser integration

#### Enhanced Validation
- **Pseudo-class validation**: All 19 valid TCSS pseudo-classes enforced
  - Valid classes: `:hover`, `:focus`, `:active`, `:disabled`, `:light`, `:dark`, `:blur`, `:can-focus`, `:has-children`, `:first-child`, `:last-child`, `:odd-child`, `:even-child`, `:only-child`, `:focus-within`, `:inline`, `:inline-block`, `:vertical-scroll`, `:horizontal-scroll`
  - Fuzzy suggestions for typos using Levenshtein distance (e.g., `:hovr` → "Did you mean ':hover'?")
  - Error tooltip showing all valid pseudo-classes for easy reference
  - Maximum edit distance of 2 for typo suggestions
- **Fuzzy property name suggestions**: Intelligent typo detection for property names
  - Examples: `backgruond` → "background", `colr` → "color", `bordre` → "border"
  - Integrated into existing `TcssAnnotator` validation
  - Same Levenshtein distance algorithm as pseudo-class validation

#### Editor Experience Enhancements
- **Enum value completion**: Context-aware completion for 15+ enumerated properties
  - `display`: block, grid, hidden, none
  - `layout`: horizontal, vertical, grid
  - `border` / `border-style`: 19 styles (solid, dashed, double, round, thick, ascii, wide, tall, heavy, etc.)
  - `overflow` / `overflow-x` / `overflow-y`: scroll, hidden, auto
  - `visibility`: visible, hidden
  - `box-sizing`: border-box, content-box
  - `position`: relative, absolute
  - `text-align`: start, end, left, right, center, justify
  - `align-horizontal` / `content-align-horizontal`: left, center, right
  - `align-vertical` / `content-align-vertical`: top, middle, bottom
  - `dock` / `split`: top, right, bottom, left, none (split also: horizontal, vertical)
  - `scrollbar-gutter`: auto, stable
  - `text-wrap`: wrap, nowrap
  - `text-overflow`: clip, fold, ellipsis
  - `overlay`: none, screen
  - `constrain` / `constrain-x` / `constrain-y`: inflect, inside, none
  - `expand`: greedy, optimal
  - Properties show ONLY valid enum values without variable contamination
  - Full Ctrl+Space support with proper context detection (property name vs value)
  - Automatic completion as you type when PropertyValue PSI element exists

#### Property Catalog Expansion
- **Property catalog now complete** with **111 total properties** - full parity with Textual
  - **Verification**: All properties from Textual's `styles.py` are accounted for
  - **15 properties added in Phase 2**:
    - **Enum properties** (4): `split`, `expand`, `text-wrap`, `text-overflow`
    - **Directional properties** (4): `constrain-x`, `constrain-y`, `grid-gutter-horizontal`, `grid-gutter-vertical`
    - **Numeric properties** (1): `line-pad`
    - **Auto-color properties** (5): `auto-color`, `auto-border-title-color`, `auto-border-subtitle-color`, `auto-link-color`, `auto-link-color-hover`
    - **Additional properties** (1): `grid-size-columns`
  - All properties available in autocomplete, validation, and documentation

#### Documentation Enhancements Phase 1 - Property & Type URLs
- **Property-specific documentation URLs**: All 111 properties now link to their specific documentation pages
  - Example: `background` → `https://textual.textualize.io/styles/background/`
  - URLs automatically generated from property names
- **CSS type documentation URLs**: Properties with complex types show additional type documentation link
  - Properties with COLOR type → link to `css_types/color/` (explains hex, RGB, HSL formats)
  - Properties with LENGTH type → link to `css_types/scalar/` (explains units: cells, fr, %, vw, vh, auto)
  - Properties with border/hatch/keyline types → link to respective type documentation
  - Example: Hovering over `background` shows links to BOTH property page AND color type documentation
  - Example: Hovering over `width` shows links to BOTH property page AND scalar type documentation
- **Implementation**: Zero-overhead static URL generation at build time
- **Coverage**: ~15 properties show type documentation links (all COLOR, LENGTH, NUMBER properties plus special STRING types)

#### Documentation Enhancements Phase 2 - Rich Property Documentation
- **Comprehensive property documentation**: Property hover (Ctrl+Q) now shows Textual-quality documentation
  - Formal syntax with type annotations
  - CSS examples with comments
  - Python API examples
  - Related properties (see also)
  - All extracted from official Textual documentation
- **Enhanced enum completions**: Enum value completions show rich descriptions
  - Before: "block — valid for display"
  - After: "block — Display the widget as normal."
- **Automated extraction**: Gradle task parses Textual markdown documentation at build time
  - Extracts 5 documentation sections from `docs/styles/*.md`:
    - Enum value descriptions from markdown tables
    - Syntax definitions with type annotations
    - CSS examples from CSS section code blocks
    - Python examples from Python section code blocks
    - Related properties from "See also" section links
  - Generates `TcssPropertyDocumentation.java` with static documentation maps
  - Task runs automatically during compilation (`compileJava` depends on it)
- **Coverage**: 46 properties with rich documentation extracted
  - Enum descriptions: 7 properties
  - Syntax: 46 properties
  - CSS examples: 46 properties
  - Python examples: 46 properties
  - See also: 15+ properties with related links
- **Professional UX**: Documentation display includes:
  - Property name + one-line description from catalog
  - Value type indicator
  - Syntax section with proper formatting
  - CSS Examples section with highlighted comments
  - Python section with API usage patterns
  - See also section with related property names
  - Type documentation link (for properties using CSS types)
  - Property documentation link
- **Zero runtime overhead**: All documentation is static constants compiled into the plugin

### Changed

#### Performance Improvements
- **Optimized cross-file lookups**: `getAllDeclarationsCrossFile()` now loads each file only once
  - Previous: O(n²) complexity with repeated file loads
  - Current: O(n) complexity with single-pass file loading
- **Icon caching**: Color preview icons are cached to avoid redundant image generation

### Fixed
- Fixed offset calculation bug in completion filtering (was checking wrong character position)
- Fixed double `$` insertion when selecting variables in autocomplete
- Corrected `TcssVariableReference.resolveColor()` to use cross-file resolution methods
- Fixed navigation popup in duplicate variable inspection to use correct API (`showInFocusCenter()`)

### Technical Details
- New classes:
  - `TcssVariableIndex`: File-based index for cross-file variable resolution
  - `ColorIconProvider`: Generates colored square icons with adaptive borders
  - `TcssPluginSettings`: Persistent settings storage
  - `TcssSettingsComponent`: Settings UI component
  - `TcssSettingsConfigurable`: Settings page integration
  - `TcssDuplicateVariableInspection`: Warns on duplicate variable declarations
  - `NavigateToDeclarationsQuickFix`: Navigation to all variable declarations
  - `CrossFileVariableTest`: Automated test suite
  - `TcssConstants`: Central registry for validation constants (10+ enum sets, fuzzy matching utility)
  - `TcssInitialKeyword`: PSI element for initial keyword
  - `TcssCssTypeUrls`: Maps CSS types to documentation URLs
  - `TcssPropertyDocumentation` (GENERATED): Comprehensive property documentation including syntax, CSS/Python examples, enum descriptions, and related properties
- Enhanced classes:
  - `VariableResolver`: Added 3 cross-file resolution methods
  - `TcssVariableReference`: Updated to use cross-file resolution
  - `TcssAnnotator`: Cross-file validation support, pseudo-class validation, fuzzy property suggestions, !important highlighting
  - `TcssColorProvider`: Cross-file color resolution
  - `TcssCompletionContributor`: Color icons, smart $ removal, settings integration, enum value completion with rich descriptions, improved Ctrl+Space context detection
  - `TcssLexer`: Added EXCLAMATION, IMPORTANT_KEYWORD, INITIAL_KEYWORD tokens
  - `TcssParser`: Support for !important modifier and initial keyword
  - `TcssSyntaxHighlighter`: Distinct colors for !important (grey/METADATA) and initial (purple/CONSTANT)
  - `TcssPropertyCatalog`: 17 new properties with metadata, auto-generated URLs for property and type documentation
  - `TcssPropertyInfo`: Added dual URL support (property + type), getCssTypeName() static method
  - `TcssDocumentationProvider`: Shows both property and type documentation links
- Build enhancements:
  - `generateTcssDocumentation` Gradle task: Comprehensive markdown parser that extracts syntax, CSS/Python examples, enum descriptions, and see-also links from Textual documentation, then generates Java code with static documentation maps
  - `TcssDocumentationProvider`: Enhanced to show rich multi-section documentation with HTML escaping

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
