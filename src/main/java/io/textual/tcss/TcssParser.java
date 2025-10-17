package io.textual.tcss;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Recursive descent parser for TCSS.
 * Builds a structured PSI tree with proper element types.
 */
public class TcssParser implements PsiParser {
    @NotNull
    @Override
    public ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder builder) {
        PsiBuilder.Marker rootMarker = builder.mark();
        parseFile(builder);
        rootMarker.done(root);
        return builder.getTreeBuilt();
    }

    /**
     * Parse top-level file contents.
     * Grammar: file ::= ruleSet*
     */
    private void parseFile(@NotNull PsiBuilder builder) {
        while (!builder.eof()) {
            IElementType tokenType = builder.getTokenType();

            // Skip whitespace and comments
            if (tokenType == TcssTokenTypes.WHITE_SPACE || tokenType == TcssTokenTypes.COMMENT) {
                builder.advanceLexer();
                continue;
            }

            // Parse variable declarations at top level
            if (tokenType == TcssTokenTypes.VARIABLE) {
                parseVariableDeclaration(builder);
            }
            // Parse rule sets
            else if (isSelector(tokenType)) {
                parseRuleSet(builder);
            } else {
                // Unexpected token - skip it
                builder.error("Expected selector or variable declaration");
                builder.advanceLexer();
            }
        }
    }

    /**
     * Parse a rule set: selector { declarations and/or nested rules }
     * Grammar: ruleSet ::= selector+ LBRACE (propertyDeclaration | ruleSet)* RBRACE
     *
     * Supports CSS nesting (TCSS v0.47.0+):
     * - Nested rule sets inherit parent selector
     * - & nesting selector combines with parent
     */
    private void parseRuleSet(@NotNull PsiBuilder builder) {
        PsiBuilder.Marker ruleMarker = builder.mark();

        // Parse selectors
        while (!builder.eof() && isSelector(builder.getTokenType())) {
            PsiBuilder.Marker selectorMarker = builder.mark();
            builder.advanceLexer(); // Consume selector
            selectorMarker.done(TcssElementTypes.SELECTOR);

            // Skip whitespace and commas between selectors
            while (!builder.eof()) {
                IElementType type = builder.getTokenType();
                if (type == TcssTokenTypes.WHITE_SPACE || type == TcssTokenTypes.COMMA) {
                    builder.advanceLexer();
                } else {
                    break;
                }
            }
        }

        // Expect opening brace
        if (builder.getTokenType() == TcssTokenTypes.LBRACE) {
            builder.advanceLexer();
        } else {
            builder.error("Expected '{'");
        }

        // Parse property declarations AND nested rule sets
        while (!builder.eof()) {
            IElementType type = builder.getTokenType();

            // Skip whitespace and comments
            if (type == TcssTokenTypes.WHITE_SPACE || type == TcssTokenTypes.COMMENT) {
                builder.advanceLexer();
                continue;
            }

            // Exit on closing brace
            if (type == TcssTokenTypes.RBRACE) {
                builder.advanceLexer();
                break;
            }

            // Parse nested rule set (selector inside braces)
            if (isSelector(type)) {
                parseRuleSet(builder);  // Recursive call for nesting
            }
            // Parse property declaration
            else if (type == TcssTokenTypes.PROPERTY_NAME) {
                parsePropertyDeclaration(builder);
            } else {
                builder.error("Expected selector, property name, or '}'");
                builder.advanceLexer();
            }
        }

        ruleMarker.done(TcssElementTypes.RULE_SET);
    }

    /**
     * Parse variable declaration at top level: $variable: value;
     * Grammar: variableDeclaration ::= VARIABLE COLON propertyValue SEMICOLON
     */
    private void parseVariableDeclaration(@NotNull PsiBuilder builder) {
        PsiBuilder.Marker declMarker = builder.mark();

        // Consume variable
        if (builder.getTokenType() == TcssTokenTypes.VARIABLE) {
            builder.advanceLexer();
        }

        // Skip whitespace
        skipWhitespace(builder);

        // Expect colon
        if (builder.getTokenType() == TcssTokenTypes.COLON) {
            builder.advanceLexer();
        } else {
            builder.error("Expected ':'");
        }

        // Skip whitespace
        skipWhitespace(builder);

        // Parse value
        parsePropertyValue(builder);

        // Expect semicolon
        skipWhitespace(builder);
        if (builder.getTokenType() == TcssTokenTypes.SEMICOLON) {
            builder.advanceLexer();
        } else {
            builder.error("Expected ';'");
        }

        declMarker.done(TcssElementTypes.VARIABLE_DECLARATION);
    }

    /**
     * Parse property declaration: property-name : value ;
     * Grammar: propertyDeclaration ::= PROPERTY_NAME COLON propertyValue (SEMICOLON | RBRACE)
     */
    private void parsePropertyDeclaration(@NotNull PsiBuilder builder) {
        PsiBuilder.Marker declMarker = builder.mark();

        // Consume property name
        if (builder.getTokenType() == TcssTokenTypes.PROPERTY_NAME) {
            builder.advanceLexer();
        }

        // Skip whitespace
        skipWhitespace(builder);

        // Expect colon
        if (builder.getTokenType() == TcssTokenTypes.COLON) {
            builder.advanceLexer();
        } else {
            builder.error("Expected ':'");
        }

        // Skip whitespace
        skipWhitespace(builder);

        // Parse property value
        parsePropertyValue(builder);

        // Expect semicolon or closing brace
        skipWhitespace(builder);
        if (builder.getTokenType() == TcssTokenTypes.SEMICOLON) {
            builder.advanceLexer();
        } else if (builder.getTokenType() != TcssTokenTypes.RBRACE) {
            builder.error("Expected ';' or '}'");
        }

        declMarker.done(TcssElementTypes.PROPERTY_DECLARATION);
    }

    /**
     * Parse property value - can contain colors, numbers, strings, etc.
     * Grammar: propertyValue ::= (colorValue | number | string | identifier)+
     */
    private void parsePropertyValue(@NotNull PsiBuilder builder) {
        PsiBuilder.Marker valueMarker = builder.mark();

        // Parse value tokens until semicolon or closing brace
        while (!builder.eof()) {
            IElementType type = builder.getTokenType();

            // Exit conditions
            if (type == TcssTokenTypes.SEMICOLON || type == TcssTokenTypes.RBRACE) {
                break;
            }

            // Skip whitespace
            if (type == TcssTokenTypes.WHITE_SPACE) {
                builder.advanceLexer();
                continue;
            }

            // Parse color values
            if (type == TcssTokenTypes.HEX_COLOR) {
                parseHexColor(builder);
            } else if (type == TcssTokenTypes.COLOR_FUNCTION_NAME) {
                parseColorFunction(builder);
            } else if (type == TcssTokenTypes.COLOR_KEYWORD) {
                parseColorKeyword(builder);
            }
            // Parse variable references
            else if (type == TcssTokenTypes.VARIABLE) {
                parseVariableReference(builder);
            }
            // Parse other value types
            else if (type == TcssTokenTypes.NUMBER ||
                     type == TcssTokenTypes.STRING ||
                     type == TcssTokenTypes.IDENTIFIER ||
                     type == TcssTokenTypes.COMMA) {
                builder.advanceLexer();
            } else {
                // Unexpected token
                builder.error("Unexpected token in property value");
                builder.advanceLexer();
            }
        }

        valueMarker.done(TcssElementTypes.PROPERTY_VALUE);
    }

    /**
     * Parse hex color value: #RGB, #RRGGBB, #RRGGBBAA
     * Grammar: hexColor ::= HEX_COLOR
     */
    private void parseHexColor(@NotNull PsiBuilder builder) {
        PsiBuilder.Marker colorMarker = builder.mark();

        if (builder.getTokenType() == TcssTokenTypes.HEX_COLOR) {
            builder.advanceLexer();
        }

        colorMarker.done(TcssElementTypes.HEX_COLOR_VALUE);
        attachOpacitySuffix(builder, colorMarker);
    }

    /**
     * Parse color function: rgb(), rgba(), hsl(), hsla()
     * Grammar: colorFunction ::= COLOR_FUNCTION_NAME LPAREN arguments RPAREN
     */
    private void parseColorFunction(@NotNull PsiBuilder builder) {
        PsiBuilder.Marker functionMarker = builder.mark();

        // Consume function name
        if (builder.getTokenType() == TcssTokenTypes.COLOR_FUNCTION_NAME) {
            builder.advanceLexer();
        }

        // Skip whitespace
        skipWhitespace(builder);

        // Expect opening parenthesis
        if (builder.getTokenType() == TcssTokenTypes.LPAREN) {
            builder.advanceLexer();
        } else {
            builder.error("Expected '('");
        }

        // Parse arguments (numbers and commas)
        PsiBuilder.Marker argsMarker = builder.mark();
        while (!builder.eof()) {
            IElementType type = builder.getTokenType();

            // Exit on closing parenthesis
            if (type == TcssTokenTypes.RPAREN) {
                break;
            }

            // Skip whitespace
            if (type == TcssTokenTypes.WHITE_SPACE) {
                builder.advanceLexer();
                continue;
            }

            // Parse argument tokens
            if (type == TcssTokenTypes.NUMBER || type == TcssTokenTypes.COMMA) {
                builder.advanceLexer();
            } else {
                builder.error("Expected number or ','");
                builder.advanceLexer();
            }
        }
        argsMarker.done(TcssElementTypes.FUNCTION_ARGUMENT_LIST);

        // Expect closing parenthesis
        if (builder.getTokenType() == TcssTokenTypes.RPAREN) {
            builder.advanceLexer();
        } else {
            builder.error("Expected ')'");
        }

        functionMarker.done(TcssElementTypes.COLOR_FUNCTION_CALL);
        attachOpacitySuffix(builder, functionMarker);
    }

    /**
     * Parse named color keyword: red, blue, ansi_bright_green, etc.
     * Grammar: colorKeyword ::= COLOR_KEYWORD
     */
    private void parseColorKeyword(@NotNull PsiBuilder builder) {
        PsiBuilder.Marker keywordMarker = builder.mark();

        if (builder.getTokenType() == TcssTokenTypes.COLOR_KEYWORD) {
            builder.advanceLexer();
        }

        keywordMarker.done(TcssElementTypes.COLOR_KEYWORD_VALUE);
        attachOpacitySuffix(builder, keywordMarker);
    }

    private void attachOpacitySuffix(@NotNull PsiBuilder builder, @NotNull PsiBuilder.Marker baseMarker) {
        PsiBuilder.Marker combinedMarker = baseMarker.precede();
        PsiBuilder.Marker rollbackMarker = builder.mark();

        skipWhitespace(builder);

        if (builder.getTokenType() == TcssTokenTypes.NUMBER && tokenHasPercent(builder.getTokenText())) {
            builder.advanceLexer();
            rollbackMarker.drop();
            combinedMarker.done(TcssElementTypes.COLOR_WITH_OPACITY_VALUE);
        } else {
            rollbackMarker.rollbackTo();
            combinedMarker.drop();
        }
    }

    private boolean tokenHasPercent(@Nullable String text) {
        return text != null && text.trim().endsWith("%");
    }

    /**
     * Check if token type is a selector.
     */
    private boolean isSelector(@NotNull IElementType type) {
        return type == TcssTokenTypes.TYPE_SELECTOR ||
               type == TcssTokenTypes.CLASS_SELECTOR ||
               type == TcssTokenTypes.ID_SELECTOR ||
               type == TcssTokenTypes.PSEUDO_CLASS ||
               type == TcssTokenTypes.NESTING_SELECTOR ||
               type == TcssTokenTypes.UNIVERSAL_SELECTOR ||
               type == TcssTokenTypes.COMBINATOR;
    }

    /**
     * Parse variable reference in property value: $variable
     * Grammar: variableReference ::= VARIABLE
     */
    private void parseVariableReference(@NotNull PsiBuilder builder) {
        PsiBuilder.Marker refMarker = builder.mark();

        if (builder.getTokenType() == TcssTokenTypes.VARIABLE) {
            builder.advanceLexer();
        }

        refMarker.done(TcssElementTypes.VARIABLE_REFERENCE);
    }

    /**
     * Skip whitespace tokens.
     */
    private void skipWhitespace(@NotNull PsiBuilder builder) {
        while (!builder.eof() && builder.getTokenType() == TcssTokenTypes.WHITE_SPACE) {
            builder.advanceLexer();
        }
    }
}
