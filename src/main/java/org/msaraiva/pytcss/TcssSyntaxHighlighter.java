package org.msaraiva.pytcss;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class TcssSyntaxHighlighter extends SyntaxHighlighterBase {
    public static final TextAttributesKey COMMENT =
            createTextAttributesKey("TCSS_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    
    public static final TextAttributesKey VARIABLE =
            createTextAttributesKey("TCSS_VARIABLE", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    
    public static final TextAttributesKey ID_SELECTOR =
            createTextAttributesKey("TCSS_ID_SELECTOR", DefaultLanguageHighlighterColors.IDENTIFIER);
    
    public static final TextAttributesKey CLASS_SELECTOR =
            createTextAttributesKey("TCSS_CLASS_SELECTOR", DefaultLanguageHighlighterColors.CLASS_NAME);
    
    public static final TextAttributesKey PSEUDO_CLASS =
            createTextAttributesKey("TCSS_PSEUDO_CLASS", DefaultLanguageHighlighterColors.METADATA);
    
    public static final TextAttributesKey TYPE_SELECTOR =
            createTextAttributesKey("TCSS_TYPE_SELECTOR", DefaultLanguageHighlighterColors.CLASS_REFERENCE);
    
    public static final TextAttributesKey PROPERTY_NAME =
            createTextAttributesKey("TCSS_PROPERTY_NAME", DefaultLanguageHighlighterColors.KEYWORD);
    
    public static final TextAttributesKey NUMBER =
            createTextAttributesKey("TCSS_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    
    public static final TextAttributesKey STRING =
            createTextAttributesKey("TCSS_STRING", DefaultLanguageHighlighterColors.STRING);
    
    public static final TextAttributesKey BRACES =
            createTextAttributesKey("TCSS_BRACES", DefaultLanguageHighlighterColors.BRACES);
    
    public static final TextAttributesKey SEMICOLON =
            createTextAttributesKey("TCSS_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON);
    
    public static final TextAttributesKey COMMA =
            createTextAttributesKey("TCSS_COMMA", DefaultLanguageHighlighterColors.COMMA);
    
    public static final TextAttributesKey OPERATOR =
            createTextAttributesKey("TCSS_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);

    // Color-specific highlighting
    public static final TextAttributesKey HEX_COLOR =
            createTextAttributesKey("TCSS_HEX_COLOR", DefaultLanguageHighlighterColors.NUMBER);

    public static final TextAttributesKey COLOR_FUNCTION_NAME =
            createTextAttributesKey("TCSS_COLOR_FUNCTION_NAME", DefaultLanguageHighlighterColors.FUNCTION_CALL);

    public static final TextAttributesKey COLOR_KEYWORD =
            createTextAttributesKey("TCSS_COLOR_KEYWORD", DefaultLanguageHighlighterColors.CONSTANT);

    public static final TextAttributesKey PARENTHESES =
            createTextAttributesKey("TCSS_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);

    public static final TextAttributesKey COLON =
            createTextAttributesKey("TCSS_COLON", DefaultLanguageHighlighterColors.DOT);

    // Grammar completeness highlighting
    public static final TextAttributesKey INITIAL_KEYWORD =
            createTextAttributesKey("TCSS_INITIAL_KEYWORD", DefaultLanguageHighlighterColors.CONSTANT);

    public static final TextAttributesKey IMPORTANT_MODIFIER =
            createTextAttributesKey("TCSS_IMPORTANT_MODIFIER", DefaultLanguageHighlighterColors.METADATA);

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new TcssLexer();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        if (tokenType.equals(TcssTokenTypes.COMMENT)) {
            return pack(COMMENT);
        } else if (tokenType.equals(TcssTokenTypes.VARIABLE)) {
            return pack(VARIABLE);
        } else if (tokenType.equals(TcssTokenTypes.ID_SELECTOR)) {
            return pack(ID_SELECTOR);
        } else if (tokenType.equals(TcssTokenTypes.CLASS_SELECTOR)) {
            return pack(CLASS_SELECTOR);
        } else if (tokenType.equals(TcssTokenTypes.PSEUDO_CLASS)) {
            return pack(PSEUDO_CLASS);
        } else if (tokenType.equals(TcssTokenTypes.TYPE_SELECTOR)) {
            return pack(TYPE_SELECTOR);
        } else if (tokenType.equals(TcssTokenTypes.PROPERTY_NAME)) {
            return pack(PROPERTY_NAME);
        } else if (tokenType.equals(TcssTokenTypes.NUMBER)) {
            return pack(NUMBER);
        } else if (tokenType.equals(TcssTokenTypes.STRING)) {
            return pack(STRING);
        } else if (tokenType.equals(TcssTokenTypes.LBRACE) || tokenType.equals(TcssTokenTypes.RBRACE)) {
            return pack(BRACES);
        } else if (tokenType.equals(TcssTokenTypes.SEMICOLON)) {
            return pack(SEMICOLON);
        } else if (tokenType.equals(TcssTokenTypes.COMMA)) {
            return pack(COMMA);
        } else if (tokenType.equals(TcssTokenTypes.NESTING_SELECTOR) ||
                   tokenType.equals(TcssTokenTypes.COMBINATOR) ||
                   tokenType.equals(TcssTokenTypes.UNIVERSAL_SELECTOR)) {
            return pack(OPERATOR);
        } else if (tokenType.equals(TcssTokenTypes.HEX_COLOR)) {
            return pack(HEX_COLOR);
        } else if (tokenType.equals(TcssTokenTypes.COLOR_FUNCTION_NAME)) {
            return pack(COLOR_FUNCTION_NAME);
        } else if (tokenType.equals(TcssTokenTypes.COLOR_KEYWORD)) {
            return pack(COLOR_KEYWORD);
        } else if (tokenType.equals(TcssTokenTypes.LPAREN) || tokenType.equals(TcssTokenTypes.RPAREN)) {
            return pack(PARENTHESES);
        } else if (tokenType.equals(TcssTokenTypes.COLON)) {
            return pack(COLON);
        } else if (tokenType.equals(TcssTokenTypes.INITIAL_KEYWORD)) {
            return pack(INITIAL_KEYWORD);
        } else if (tokenType.equals(TcssTokenTypes.EXCLAMATION)) {
            return pack(IMPORTANT_MODIFIER);
        }
        return TextAttributesKey.EMPTY_ARRAY;
    }
}
