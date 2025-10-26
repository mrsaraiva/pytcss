# Textual CSS Plugin Examples

Quick showcase of all plugin features. Open these files in PyCharm to see the plugin in action!

## Getting Started

1. **Install the plugin** - See installation instructions in the [main README](../README.md#installation)
2. **Open this folder** in PyCharm: `File → Open → select examples/`
3. **Open the TCSS files** to explore features

## Files Overview

### variables.tcss
**What**: Shared color and spacing variables used across files
**Try**:
- Hover over any variable to see color preview in tooltip
- Notice gutter icons showing colors

### styles.tcss
**What**: Demonstrates cross-file variable resolution
**Try**:
- **Cross-file variables**: Uses variables from `variables.tcss`
- **Go-to-definition**: Ctrl+Click on `$secondary` → jumps to `variables.tcss`
- **Completion**: Type `$` to see variables from both files with source indicators
- **Duplicate detection**: Notice WARNING on `$primary` (declared in both files)
- **Local shadowing**: `$accent` is local to this file only
- **Color gutter**: Hover over variables to see resolved colors

### features.tcss
**What**: Comprehensive v1.2 feature showcase
**Demonstrates**:
- Quick documentation (Ctrl+Q)
- Property value type validation
- Pseudo-class validation (19 valid classes)
- Fuzzy property suggestions for typos
- Enum value completion
- !important modifier and initial keyword
- All 111 supported properties
- All color formats

### app.py
**What**: Python CSS/DEFAULT_CSS injection
**Try**: All TCSS features work inside Python `CSS` and `DEFAULT_CSS` strings

## Key Features to Try

### 📚 Quick Documentation (NEW in v1.2!)
Place cursor on any property name and press **Ctrl+Q** (or Cmd+J on Mac):
- **Rich documentation** extracted from official Textual docs
- **Formal syntax** with type annotations
- **CSS examples** with inline comments
- **Python examples** showing API usage
- **See also** section with related properties
- **Clickable links** to official Textual documentation

**Try it**: In `features.tcss`, press Ctrl+Q on `display`, `layout`, or `background`

### 🎨 Color System
- **Gutter icons**: Colored squares show resolved colors (even for variables!)
- **Color picker**: Click gutter icon to edit colors interactively
- **Formats**: Hex, RGB/RGBA, HSL/HSLA, named colors (150+), ANSI colors
- **Opacity suffix**: `red 50%`, `#0066cc 70%`, `$primary 80%`
- **Cross-file resolution**: Variables from other files show their colors

**Try it**: In `variables.tcss`, click any color gutter icon

### ✅ Validation & Error Detection

#### Property Value Type Checking
Validates that values match expected types (e.g., COLOR vs NUMBER).
- **Example**: `background: 123` → ERROR: expects COLOR, got NUMBER
- **Try it**: See `.type-validation` section in `features.tcss`

#### Pseudo-Class Validation
All 19 valid TCSS pseudo-classes recognized; invalid ones show errors.
- **Example**: `Button:hovr` → ERROR: "Did you mean ':hover'?"
- **Try it**: See pseudo-class section in `features.tcss`

#### Fuzzy Property Suggestions
Typos in property names get helpful suggestions.
- **Example**: `backgruond: blue` → "Did you mean 'background'?"
- **Try it**: See `.fuzzy-test` section in `features.tcss`

### 💡 Code Completion
Trigger with **Ctrl+Space**:

#### Property Name Completion
- Type `bac` → suggests `background` with description and type
- Shows all 111 supported properties

#### Enum Value Completion
- After `display:` → suggests `block`, `grid`, `hidden`, `none`
- Works for 15+ enumerated properties
- **Try it**: In `features.tcss`, type `layout: ` then Ctrl+Space

#### Variable Completion
- Type `$` → shows all variables (local + cross-file) with color preview icons
- Source file indicated: `$primary (variables.tcss)`
- **Smart $ removal**: Type `$blu`, select `blue` → inserts `blue` (not `$blue`)

#### Color Keyword Completion
- Type `bl` → suggests `blue`, `black`, `blueviolet` with color preview icons
- 150+ named colors supported

### 🔗 Cross-File Variables
Variables defined in one file work everywhere:
- **Resolution**: `$primary` from `variables.tcss` works in `styles.tcss`
- **Navigation**: Ctrl+Click on variable → jumps to declaration (even in other files)
- **Completion**: Type `$` → see variables from all `.tcss` files in project
- **Color preview**: Gutter icons show colors even for cross-file variables
- **Local shadowing**: File-local variables override cross-file ones
- **Duplicate detection**: WARNING when same variable declared in multiple files

**Try it**: Open both `variables.tcss` and `styles.tcss` side by side

### 🔧 Refactoring
- **Rename**: Place cursor on variable, press **Shift+F6** → renames all references
- **Find usages**: **Alt+F7** → shows all places variable is used
- **Go to definition**: **Ctrl+Click** → jumps to variable declaration
- **Works across files**: Renaming updates all files in project

**Try it**: In `app.py`, press Shift+F6 on `$theme` variable

### 🐍 Python Integration
Full TCSS support in Python `CSS` and `DEFAULT_CSS` class variables:
- All features work: syntax highlighting, colors, completion, validation
- **Try it**: Open `app.py` and explore the CSS strings

### 📖 Grammar Features
- **!important modifier**: `background: blue !important;` (grey highlighting)
- **initial keyword**: `padding: initial;` (purple highlighting)
- Both work with all property types

**Try it**: See `.grammar` section in `features.tcss`

## Plugin Settings

Configure completion behavior:

**Settings → Languages & Frameworks → Textual CSS**
- **Dollar prefix filtering**: Control whether `$` shows both variables and colors, or only variables

## Feature Summary

| Feature | File | How to Test |
|---------|------|-------------|
| Quick docs | features.tcss | Ctrl+Q on any property |
| Color preview | variables.tcss | Hover over colors, see gutter icons |
| Color picker | variables.tcss | Click gutter icon |
| Cross-file variables | styles.tcss | Ctrl+Click on $secondary |
| Variable completion | styles.tcss | Type $ and Ctrl+Space |
| Duplicate detection | styles.tcss | See WARNING on $primary |
| Type validation | features.tcss | See .type-validation section |
| Pseudo-class validation | features.tcss | See :hovr error |
| Fuzzy suggestions | features.tcss | See .fuzzy-test section |
| Enum completion | features.tcss | Type display: and Ctrl+Space |
| Python injection | app.py | All features in CSS strings |
| Refactoring | app.py | Shift+F6 on $theme |

## Documentation Links

- **Textual CSS Guide**: https://textual.textualize.io/guide/CSS/
- **Plugin Repository**: https://github.com/mrsaraiva/pytcss
- **Report Issues**: https://github.com/mrsaraiva/pytcss/issues

## Tips

1. **Explore progressively**: Start with `variables.tcss`, then `styles.tcss`, then `features.tcss`
2. **Use Ctrl+Q liberally**: Every property has rich documentation
3. **Try the keyboard shortcuts**: They make editing much faster
4. **Check the gutter**: Color icons and error markers appear there
5. **Experiment**: The examples are meant to be edited and explored!
