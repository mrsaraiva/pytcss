package io.textual.tcss;

import com.intellij.psi.tree.IElementType;

/**
 * PSI element type constants for structured TCSS AST.
 * These types are used by the parser to mark syntactic constructs in the PSI tree.
 */
public interface TcssElementTypes {
    // Structural elements
    IElementType RULE_SET = new TcssElementType("RULE_SET");
    IElementType SELECTOR = new TcssElementType("SELECTOR");
    IElementType PROPERTY_DECLARATION = new TcssElementType("PROPERTY_DECLARATION");
    IElementType PROPERTY_VALUE = new TcssElementType("PROPERTY_VALUE");

    // Color value elements
    IElementType COLOR_VALUE = new TcssElementType("COLOR_VALUE");
    IElementType HEX_COLOR_VALUE = new TcssElementType("HEX_COLOR_VALUE");
    IElementType COLOR_FUNCTION_CALL = new TcssElementType("COLOR_FUNCTION_CALL");
    IElementType COLOR_KEYWORD_VALUE = new TcssElementType("COLOR_KEYWORD_VALUE");
    IElementType COLOR_WITH_OPACITY_VALUE = new TcssElementType("COLOR_WITH_OPACITY_VALUE");
    IElementType FUNCTION_ARGUMENT_LIST = new TcssElementType("FUNCTION_ARGUMENT_LIST");

    // Variable elements
    IElementType VARIABLE_DECLARATION = new TcssElementType("VARIABLE_DECLARATION");
    IElementType VARIABLE_REFERENCE = new TcssElementType("VARIABLE_REFERENCE");
}
