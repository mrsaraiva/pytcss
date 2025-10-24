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

#### Property Catalog Expansion (Continued)
- **17 additional properties** added to catalog (now **115 total**, up from 98)
  - **Enum properties** (4): `split`, `expand`, `text-wrap`, `text-overflow`
  - **Directional properties** (4): `constrain-x`, `constrain-y`, `grid-gutter-horizontal`, `grid-gutter-vertical`
  - **Numeric properties** (1): `line-pad`
  - **Auto-color properties** (5): `auto-color`, `auto-border-title-color`, `auto-border-subtitle-color`, `auto-link-color`, `auto-link-color-hover`
  - **Grid properties** (2): `grid-size-columns`, `grid-size-rows`
  - **Transitions property** (1): `transitions` (STRING type)
  - All new properties available in autocomplete, validation, and documentation

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
- Enhanced classes:
  - `VariableResolver`: Added 3 cross-file resolution methods
  - `TcssVariableReference`: Updated to use cross-file resolution
  - `TcssAnnotator`: Cross-file validation support, pseudo-class validation, fuzzy property suggestions, !important highlighting
  - `TcssColorProvider`: Cross-file color resolution
  - `TcssCompletionContributor`: Color icons, smart $ removal, settings integration, enum value completion, improved Ctrl+Space context detection
  - `TcssLexer`: Added EXCLAMATION, IMPORTANT_KEYWORD, INITIAL_KEYWORD tokens
  - `TcssParser`: Support for !important modifier and initial keyword
  - `TcssSyntaxHighlighter`: Distinct colors for !important (grey/METADATA) and initial (purple/CONSTANT)
  - `TcssPropertyCatalog`: 17 new properties with metadata

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
