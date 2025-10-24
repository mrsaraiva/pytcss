# Cross-File Variable Resolution Examples

These TCSS files demonstrate the cross-file variable resolution feature.

## How to Test

1. **Run the sandbox IDE**:
   ```bash
   ./gradlew runIde
   ```

2. **Open this `examples/` folder in the sandbox IDE**

3. **Open all TCSS files** in the editor

## Features to Test

### 1. Cross-File Color Previews
- **Open**: `main.tcss`
- **Look for**: Color gutter icons on lines with `$primary`, `$secondary`, etc.
- **Expected**: You should see colored squares in the gutter even though the variables are defined in `colors.tcss`
- **Try**: Click the color gutter icon to open the color picker

### 2. Go-to-Definition (Ctrl+Click)
- **Open**: `main.tcss`
- **Try**: Ctrl+Click on `$primary` on line 12
- **Expected**: Should navigate to the declaration in `colors.tcss` (or show popup if duplicate)
- **Try**: Ctrl+Click on `$accent` on line 32
- **Expected**: Should navigate to `theme.tcss`

### 3. Duplicate Variable Detection
- **Open**: `colors.tcss` or `theme.tcss`
- **Look at**: The `$primary` variable declaration in both files
- **Expected**: Should see a **WARNING** highlight on both declarations
- **Try**: Click on the warning, then click "Show all declarations of '$primary'"
- **Expected**: A popup showing both files with `$primary` declarations

### 4. Code Completion
- **Open**: Any TCSS file (or create a new one)
- **Type**: `Button { background: $`
- **Expected**: Completion popup shows ALL variables from ALL files, with source file names:
  - `$primary (colors.tcss)`
  - `$primary (theme.tcss)`
  - `$secondary (colors.tcss)`
  - `$accent (theme.tcss)`
  - `$spacing-small (layout.tcss)`
  - etc.

### 5. Local Shadowing
- **Open**: `layout.tcss`
- **Look at**: Line 6 where `$primary: #ff0000;` is declared
- **Look at**: Line 12 where `$primary` is used in `Container { background: $primary; }`
- **Expected**: Gutter icon shows **RED** color (not blue from colors.tcss)
- **Why**: Local variables shadow cross-file ones within the same file

### 6. Undefined Variable Error
- **Open**: Any TCSS file
- **Type**: `Button { color: $nonexistent; }`
- **Expected**: Red error highlighting on `$nonexistent` with message "Undefined variable 'nonexistent'"

## File Descriptions

### `colors.tcss`
Defines common color variables used throughout the project.
- `$primary`: Blue (#0066cc)
- `$secondary`: Orange (#ff6600)
- `$success`, `$warning`, `$danger`: Status colors
- `$background`, `$text`, `$border`: Layout colors

### `theme.tcss`
Defines theme-specific variables.
- **`$primary`**: Blue (#007bff) - **DUPLICATE!** Also defined in colors.tcss
- `$accent`: Gray
- `$highlight`: Yellow
- `$shadow`: Semi-transparent black

### `main.tcss`
Main stylesheet that **uses cross-file variables** from both `colors.tcss` and `theme.tcss`.
All the `$variables` here are resolved across files.

### `layout.tcss`
Demonstrates **local shadowing**.
- Redefines `$primary` locally as RED
- Within this file, `$primary` is red, not blue
- Other cross-file variables like `$border` and `$secondary` still work normally
- Defines local-only spacing variables

## Expected Behavior Summary

| Feature | What to Look For |
|---------|------------------|
| **Cross-file colors** | Gutter icons show colors from other files |
| **Go-to-definition** | Ctrl+Click navigates to declaration in other files |
| **Duplicates** | WARNING on variables declared in multiple files |
| **Completion** | All variables from all files shown with source file name |
| **Local shadowing** | Local redefinitions override cross-file (see layout.tcss) |
| **Undefined errors** | Red highlighting for variables that don't exist anywhere |
