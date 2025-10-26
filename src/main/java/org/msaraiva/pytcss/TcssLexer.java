package org.msaraiva.pytcss;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.msaraiva.pytcss.color.NamedColors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TcssLexer extends LexerBase {
    private CharSequence buffer;
    private int startOffset;
    private int endOffset;
    private int currentOffset;
    private IElementType tokenType;
    private int tokenStart;
    private int tokenEnd;

    // Context tracking for color detection
    private boolean inPropertyValue = false;
    private int braceDepth = 0;

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.currentOffset = startOffset;
        this.inPropertyValue = false;
        this.braceDepth = 0;
        advance();
    }

    @Override
    public int getState() {
        // Encode state for incremental parsing
        return (braceDepth << 1) | (inPropertyValue ? 1 : 0);
    }

    @Nullable
    @Override
    public IElementType getTokenType() {
        return tokenType;
    }

    @Override
    public int getTokenStart() {
        return tokenStart;
    }

    @Override
    public int getTokenEnd() {
        return tokenEnd;
    }

    @Override
    public void advance() {
        if (currentOffset >= endOffset) {
            tokenType = null;
            return;
        }

        tokenStart = currentOffset;
        char c = buffer.charAt(currentOffset);

        // Skip whitespace
        if (Character.isWhitespace(c)) {
            while (currentOffset < endOffset && Character.isWhitespace(buffer.charAt(currentOffset))) {
                currentOffset++;
            }
            tokenEnd = currentOffset;
            tokenType = TcssTokenTypes.WHITE_SPACE;
            return;
        }

        // Comments
        if (c == '/' && currentOffset + 1 < endOffset && buffer.charAt(currentOffset + 1) == '*') {
            currentOffset += 2;
            while (currentOffset + 1 < endOffset) {
                if (buffer.charAt(currentOffset) == '*' && buffer.charAt(currentOffset + 1) == '/') {
                    currentOffset += 2;
                    break;
                }
                currentOffset++;
            }
            tokenEnd = currentOffset;
            tokenType = TcssTokenTypes.COMMENT;
            return;
        }

        // Variables
        if (c == '$') {
            currentOffset++;
            while (currentOffset < endOffset) {
                char ch = buffer.charAt(currentOffset);
                if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '-') {
                    currentOffset++;
                } else {
                    break;
                }
            }
            tokenEnd = currentOffset;
            tokenType = TcssTokenTypes.VARIABLE;
            return;
        }

        // Hex color vs ID selector disambiguation
        if (c == '#') {
            currentOffset++;
            int hexCount = 0;
            int tempOffset = currentOffset;

            // Count hex digits after #
            while (tempOffset < endOffset && hexCount < 8) {
                char ch = buffer.charAt(tempOffset);
                if ((ch >= '0' && ch <= '9') ||
                    (ch >= 'a' && ch <= 'f') ||
                    (ch >= 'A' && ch <= 'F')) {
                    hexCount++;
                    tempOffset++;
                } else {
                    break;
                }
            }

            // Valid hex color: 3, 4, 6, or 8 digits (anywhere, not just in property values)
            // This allows hex colors in variable declarations like: $primary: #0066cc;
            if (hexCount == 3 || hexCount == 4 || hexCount == 6 || hexCount == 8) {
                currentOffset = tempOffset;
                tokenEnd = currentOffset;
                tokenType = TcssTokenTypes.HEX_COLOR;
                return;
            }

            // Otherwise: ID selector
            while (currentOffset < endOffset) {
                char ch = buffer.charAt(currentOffset);
                if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '-') {
                    currentOffset++;
                } else {
                    break;
                }
            }
            tokenEnd = currentOffset;
            tokenType = TcssTokenTypes.ID_SELECTOR;
            return;
        }

        // Class selector
        if (c == '.') {
            currentOffset++;
            while (currentOffset < endOffset) {
                char ch = buffer.charAt(currentOffset);
                if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '-') {
                    currentOffset++;
                } else {
                    break;
                }
            }
            tokenEnd = currentOffset;
            tokenType = TcssTokenTypes.CLASS_SELECTOR;
            return;
        }

        // Pseudo-class selector vs colon separator
        if (c == ':') {
            // Check if followed by letter (pseudo-class like :hover) or not (property separator)
            if (currentOffset + 1 < endOffset && Character.isLetter(buffer.charAt(currentOffset + 1))) {
                // Pseudo-class selector
                currentOffset++;
                while (currentOffset < endOffset) {
                    char ch = buffer.charAt(currentOffset);
                    if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '-') {
                        currentOffset++;
                    } else {
                        break;
                    }
                }
                tokenEnd = currentOffset;
                tokenType = TcssTokenTypes.PSEUDO_CLASS;
            } else {
                // Property separator - enter property value context
                currentOffset++;
                tokenEnd = currentOffset;
                tokenType = TcssTokenTypes.COLON;
                inPropertyValue = true;
            }
            return;
        }

        // Numbers
        if (Character.isDigit(c)) {
            while (currentOffset < endOffset && (Character.isDigit(buffer.charAt(currentOffset)) || buffer.charAt(currentOffset) == '.')) {
                currentOffset++;
            }
            // Check for units
            if (currentOffset < endOffset) {
                char ch = buffer.charAt(currentOffset);
                if (ch == 'f' || ch == '%' || ch == 'w' || ch == 'h' || ch == 'v') {
                    currentOffset++;
                    if (currentOffset < endOffset && buffer.charAt(currentOffset) == 'r') {
                        currentOffset++;
                    } else if (currentOffset < endOffset && (buffer.charAt(currentOffset) == 'w' || buffer.charAt(currentOffset) == 'h')) {
                        currentOffset++;
                    }
                }
            }
            tokenEnd = currentOffset;
            tokenType = TcssTokenTypes.NUMBER;
            return;
        }

        // Identifiers and keywords (property names, type selectors, colors)
        if (Character.isLetter(c) || c == '_') {
            while (currentOffset < endOffset) {
                char ch = buffer.charAt(currentOffset);
                if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '-') {
                    currentOffset++;
                } else {
                    break;
                }
            }
            tokenEnd = currentOffset;
            String text = buffer.subSequence(tokenStart, tokenEnd).toString();

            // Check if it's a property name (followed by :)
            int nextNonWhitespace = tokenEnd;
            while (nextNonWhitespace < endOffset && Character.isWhitespace(buffer.charAt(nextNonWhitespace))) {
                nextNonWhitespace++;
            }
            if (nextNonWhitespace < endOffset && buffer.charAt(nextNonWhitespace) == ':'
                && (nextNonWhitespace + 1 >= endOffset || !Character.isLetter(buffer.charAt(nextNonWhitespace + 1)))) {
                tokenType = TcssTokenTypes.PROPERTY_NAME;
                // Don't enter property value context yet - that happens when we see the : token
            } else if (inPropertyValue) {
                // In property value context - check for color functions and keywords
                String lowerText = text.toLowerCase();

                // Check for color functions (followed by parenthesis)
                if ((lowerText.equals("rgb") || lowerText.equals("rgba") ||
                     lowerText.equals("hsl") || lowerText.equals("hsla")) &&
                    nextNonWhitespace < endOffset && buffer.charAt(nextNonWhitespace) == '(') {
                    tokenType = TcssTokenTypes.COLOR_FUNCTION_NAME;
                }
                // Check for "auto" keyword (special TCSS color keyword)
                else if (lowerText.equals("auto")) {
                    tokenType = TcssTokenTypes.COLOR_KEYWORD;
                }
                // Check for "initial" keyword (special TCSS keyword for property reset)
                else if (lowerText.equals("initial")) {
                    tokenType = TcssTokenTypes.INITIAL_KEYWORD;
                }
                // Check for named colors
                else if (NamedColors.isNamedColor(text)) {
                    tokenType = TcssTokenTypes.COLOR_KEYWORD;
                }
                // Otherwise regular identifier
                else {
                    tokenType = TcssTokenTypes.IDENTIFIER;
                }
            } else if (Character.isUpperCase(text.charAt(0))) {
                tokenType = TcssTokenTypes.TYPE_SELECTOR;
            } else {
                tokenType = TcssTokenTypes.IDENTIFIER;
            }
            return;
        }

        // Strings
        if (c == '"' || c == '\'') {
            char quote = c;
            currentOffset++;
            while (currentOffset < endOffset) {
                char ch = buffer.charAt(currentOffset);
                if (ch == '\\') {
                    currentOffset += 2;
                } else if (ch == quote) {
                    currentOffset++;
                    break;
                } else {
                    currentOffset++;
                }
            }
            tokenEnd = currentOffset;
            tokenType = TcssTokenTypes.STRING;
            return;
        }

        // Special characters
        switch (c) {
            case '{':
                tokenType = TcssTokenTypes.LBRACE;
                braceDepth++;
                inPropertyValue = false; // Reset when entering block
                break;
            case '}':
                tokenType = TcssTokenTypes.RBRACE;
                braceDepth--;
                inPropertyValue = false; // Exit property value context
                break;
            case ';':
                tokenType = TcssTokenTypes.SEMICOLON;
                inPropertyValue = false; // Exit property value context
                break;
            case ',':
                tokenType = TcssTokenTypes.COMMA;
                break;
            case '(':
                tokenType = TcssTokenTypes.LPAREN;
                break;
            case ')':
                tokenType = TcssTokenTypes.RPAREN;
                break;
            case '&':
                tokenType = TcssTokenTypes.NESTING_SELECTOR;
                break;
            case '>':
                tokenType = TcssTokenTypes.COMBINATOR;
                break;
            case '*':
                tokenType = TcssTokenTypes.UNIVERSAL_SELECTOR;
                break;
            case '!':
                tokenType = TcssTokenTypes.EXCLAMATION;
                break;
            default:
                tokenType = TcssTokenTypes.BAD_CHARACTER;
                break;
        }
        currentOffset++;
        tokenEnd = currentOffset;
    }

    @NotNull
    @Override
    public CharSequence getBufferSequence() {
        return buffer;
    }

    @Override
    public int getBufferEnd() {
        return endOffset;
    }
}
