"""
Python TCSS Injection Examples

This file demonstrates that all TCSS plugin features work inside Python CSS and
DEFAULT_CSS class variables:
- Syntax highlighting
- Color gutter icons and picker
- Code completion
- Variable refactoring
- Quick documentation (Ctrl+Q)
- Validation and error checking
"""

from textual.app import App
from textual.widget import Widget


# Example 1: Basic CSS Injection
class BasicApp(App):
    """All TCSS features work inside this CSS string."""

    CSS = """
    /* Variables with color previews */
    $primary: #0066cc;
    $success: green;
    $error: red;

    Button {
        background: $primary;   /* Click gutter icon for color picker! */
        color: white;
        padding: 1 2;
        border: solid $primary;
    }

    Button:hover {
        background: $success;   /* Green hover state */
    }

    .error {
        background: $error;     /* Red background */
        color: white;
    }
    """


# Example 2: DEFAULT_CSS with Advanced Features
class AdvancedApp(App):
    """Demonstrates validation, completion, and grammar features."""

    DEFAULT_CSS = """
    $accent: #ffeb3b;
    $spacing: 16px;

    Screen {
        /* Type validation works here */
        background: blue;
        layout: vertical;

        /* Try Ctrl+Space after 'display: ' for enum completion */
        display: block;
    }

    Panel {
        /* !important modifier (grey highlighting) */
        padding: $spacing !important;

        /* initial keyword (purple highlighting) */
        margin: initial;

        /* Opacity suffix */
        background: $accent 80%;
    }

    /* Pseudo-class validation */
    Button:focus {
        border: thick $accent;
    }

    /* Invalid pseudo-class shows error */
    Button:hovr {
        background: red;  /* ERROR: Did you mean ':hover'? */
    }

    /* Fuzzy property suggestion */
    .test {
        backgruond: blue;  /* WARNING: Did you mean 'background'? */
    }
    """


# Example 3: Cross-File Variable Resolution
class CrossFileApp(App):
    """Variables from variables.tcss are available here via cross-file resolution."""

    CSS = """
    /* These variables are defined in variables.tcss */
    /* Hover to see color previews, Ctrl+Click to navigate */

    Button {
        background: $primary;     /* From variables.tcss */
        color: $text;             /* From variables.tcss */
        border: solid $border;    /* From variables.tcss */
        padding: $spacing-md;     /* From variables.tcss */
    }

    .success {
        background: $success;     /* From variables.tcss - green */
    }

    .error {
        background: $error;       /* From variables.tcss - red */
    }
    """


# Example 4: Widget with Both CSS Types
class MyWidget(Widget):
    """Widgets can have both CSS and DEFAULT_CSS."""

    CSS = """
    $widget-color: purple;

    .my-widget {
        background: $widget-color;  /* Widget-specific styling */
        padding: 1 2;
    }
    """

    DEFAULT_CSS = """
    $default-bg: #2d2d2d;

    .default-style {
        background: $default-bg;    /* Default widget styling */
        color: white;
    }
    """


# Example 5: All Color Formats
class ColorApp(App):
    """All color formats work with gutter preview icons."""

    CSS = """
    Screen {
        /* Hex colors */
        background: #1e1e1e;
        color: #e0e0e0;

        /* Named colors */
        border-color: seashell;
        tint: skyblue;

        /* RGB/RGBA */
        scrollbar-background: rgb(50, 50, 50);
        scrollbar-color: rgba(100, 150, 200, 0.8);

        /* HSL/HSLA */
        link-color: hsl(210, 100%, 50%);
        link-color-hover: hsla(210, 100%, 60%, 0.9);

        /* Opacity suffix */
        outline-background: red 50%;

        /* ANSI colors */
        color: ansi_bright_green;
    }
    """


# Example 6: Refactoring Support
class RefactoringApp(App):
    """Test variable refactoring in injected TCSS.

    To test:
    1. Place cursor on $theme (declaration or reference)
    2. Press Shift+F6 to rename
    3. All references update automatically
    """

    CSS = """
    $theme: #0066cc;
    $accent: #ff6600;

    Button {
        background: $theme;    /* Reference 1 */
        border: solid $theme;  /* Reference 2 */
    }

    .header {
        color: $theme;         /* Reference 3 */
    }

    .footer {
        color: $accent;        /* Different variable */
    }
    """


# Try These Features in the CSS Strings Above:
# - Ctrl+Q: Quick documentation on properties
# - Ctrl+Space: Autocomplete (properties, enums, variables, colors)
# - Ctrl+Click: Go to variable definition
# - Shift+F6: Rename variable
# - Alt+F7: Find all usages of variable
# - Click gutter icon: Open color picker
# - Hover: See color preview, documentation
