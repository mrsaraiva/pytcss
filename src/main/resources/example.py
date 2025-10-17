"""
Example Python file for testing TCSS language injection in CSS and DEFAULT_CSS class variables.

This file demonstrates that TCSS syntax highlighting, color previews, and other plugin features
work inside Python string literals assigned to CSS or DEFAULT_CSS class variables.
"""

from textual.app import App
from textual.widget import Widget


# Test 1: Basic CSS class variable injection
class BasicApp(App):
    """Test basic TCSS injection in CSS class variable."""

    CSS = """
    /* This TCSS should have full syntax highlighting and features */
    $primary: #0066cc;
    $success: green;

    Button {
        background: $primary;   /* Should show blue color gutter icon */
        color: white;
        padding: 1 2;
    }

    .success {
        background: $success;   /* Should show green color gutter icon */
    }
    """


# Test 2: DEFAULT_CSS class variable injection
class DefaultCssApp(App):
    """Test TCSS injection in DEFAULT_CSS class variable."""

    DEFAULT_CSS = """
    $error: red;
    $warning: yellow;

    .error {
        background: $error;     /* Should show red color gutter icon */
        color: white;
    }

    .warning {
        background: $warning;   /* Should show yellow color gutter icon */
        color: black;
    }
    """


# Test 3: Opacity suffix support in Python injection
class OpacityApp(App):
    """Test opacity suffix in injected TCSS."""

    CSS = """
    $primary: #0066cc;

    .semi-transparent {
        background: red 50%;        /* Should show semi-transparent red */
        color: $primary 70%;        /* Should resolve variable + apply opacity */
        border: #ff0000 25%;        /* Should show very transparent red */
    }
    """


# Test 4: Code completion in Python injection
class CompletionTestApp(App):
    """Test that code completion works in injected TCSS.

    When editing the CSS string below:
    - Type a property name and press Ctrl+Space for completion
    - Type $ and press Ctrl+Space for variable completion
    - Type a color keyword and press Ctrl+Space for color completion
    """

    CSS = """
    $my-var: blue;

    Button {
        /* Try typing: back<Ctrl+Space> to complete 'background' */
        /* Try typing: $<Ctrl+Space> to see variable suggestions */
        /* Try typing: color: r<Ctrl+Space> to see color keywords */
    }
    """


# Test 5: Variable rename refactoring in Python injection
class RenameTestApp(App):
    """Test variable rename refactoring in injected TCSS.

    To test:
    1. Place cursor on $theme-color (either declaration or reference)
    2. Press Shift+F6 to rename
    3. Both declaration and all references should be renamed
    """

    CSS = """
    $theme-color: #0066cc;
    $other-color: red;

    Button {
        background: $theme-color;   /* Reference 1 */
        border: solid $theme-color; /* Reference 2 */
    }

    .header {
        color: $theme-color;        /* Reference 3 */
    }
    """


# Test 6: Nested rules in Python injection
class NestedRulesApp(App):
    """Test that nested rules work correctly in injected TCSS."""

    CSS = """
    $primary: #0066cc;

    #dialog {
        background: white;

        Button {
            background: $primary;

            &:hover {
                background: lighten($primary, 20%);
            }

            &.danger {
                background: red;    /* Should show red gutter icon */
            }
        }
    }
    """


# Test 7: Invalid - should NOT have injection
class NoInjectionApp(App):
    """These should NOT have TCSS injection."""

    # Regular string variable (not CSS or DEFAULT_CSS)
    STYLES = """
    This is just a regular string.
    $primary: blue;  /* Should NOT have color preview */
    """

    # Instance variable (not class variable)
    def __init__(self):
        self.CSS = """
        Button { background: red; }  /* Should NOT have injection */
        """


# Test 8: Widget with both CSS and DEFAULT_CSS
class BothCssTypesWidget(Widget):
    """Test widget with both CSS types."""

    CSS = """
    $widget-primary: purple;

    .widget-class {
        background: $widget-primary;  /* Should show purple */
    }
    """

    DEFAULT_CSS = """
    $widget-default: orange;

    .default-class {
        background: $widget-default;  /* Should show orange */
    }
    """
