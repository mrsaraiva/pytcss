package io.textual.tcss;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

public interface TcssTokenTypes {
    IElementType WHITE_SPACE = TokenType.WHITE_SPACE;
    IElementType BAD_CHARACTER = TokenType.BAD_CHARACTER;
    
    IElementType COMMENT = new TcssTokenType("COMMENT");
    IElementType VARIABLE = new TcssTokenType("VARIABLE");
    IElementType ID_SELECTOR = new TcssTokenType("ID_SELECTOR");
    IElementType CLASS_SELECTOR = new TcssTokenType("CLASS_SELECTOR");
    IElementType PSEUDO_CLASS = new TcssTokenType("PSEUDO_CLASS");
    IElementType TYPE_SELECTOR = new TcssTokenType("TYPE_SELECTOR");
    IElementType UNIVERSAL_SELECTOR = new TcssTokenType("UNIVERSAL_SELECTOR");
    IElementType PROPERTY_NAME = new TcssTokenType("PROPERTY_NAME");
    IElementType IDENTIFIER = new TcssTokenType("IDENTIFIER");
    IElementType NUMBER = new TcssTokenType("NUMBER");
    IElementType STRING = new TcssTokenType("STRING");
    IElementType LBRACE = new TcssTokenType("LBRACE");
    IElementType RBRACE = new TcssTokenType("RBRACE");
    IElementType SEMICOLON = new TcssTokenType("SEMICOLON");
    IElementType COMMA = new TcssTokenType("COMMA");
    IElementType NESTING_SELECTOR = new TcssTokenType("NESTING_SELECTOR");
    IElementType COMBINATOR = new TcssTokenType("COMBINATOR");

    // Color-specific tokens
    IElementType HEX_COLOR = new TcssTokenType("HEX_COLOR");
    IElementType COLOR_FUNCTION_NAME = new TcssTokenType("COLOR_FUNCTION_NAME");
    IElementType COLOR_KEYWORD = new TcssTokenType("COLOR_KEYWORD");
    IElementType LPAREN = new TcssTokenType("LPAREN");
    IElementType RPAREN = new TcssTokenType("RPAREN");
    IElementType COLON = new TcssTokenType("COLON");
}
