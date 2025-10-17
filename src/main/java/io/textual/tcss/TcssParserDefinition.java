package io.textual.tcss;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import io.textual.tcss.psi.*;
import org.jetbrains.annotations.NotNull;

public class TcssParserDefinition implements ParserDefinition {
    public static final IFileElementType FILE = new IFileElementType(TcssLanguage.INSTANCE);

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new TcssLexer();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return new TcssParser();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @NotNull
    @Override
    public TokenSet getWhitespaceTokens() {
        return TokenSet.create(TcssTokenTypes.WHITE_SPACE);
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return TokenSet.create(TcssTokenTypes.COMMENT);
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return TokenSet.create(TcssTokenTypes.STRING);
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode node) {
        IElementType type = node.getElementType();

        // Map element types to PSI classes
        if (type == TcssElementTypes.RULE_SET) {
            return new TcssRuleSet(node);
        } else if (type == TcssElementTypes.PROPERTY_DECLARATION) {
            return new TcssPropertyDeclaration(node);
        } else if (type == TcssElementTypes.PROPERTY_VALUE) {
            return new TcssPropertyValue(node);
        } else if (type == TcssElementTypes.HEX_COLOR_VALUE) {
            return new TcssHexColorValue(node);
        } else if (type == TcssElementTypes.COLOR_FUNCTION_CALL) {
            return new TcssColorFunctionCall(node);
        } else if (type == TcssElementTypes.COLOR_KEYWORD_VALUE) {
            return new TcssColorKeyword(node);
        } else if (type == TcssElementTypes.COLOR_WITH_OPACITY_VALUE) {
            return new TcssColorWithOpacityValue(node);
        } else if (type == TcssElementTypes.VARIABLE_DECLARATION) {
            return new TcssVariableDeclaration(node);
        } else if (type == TcssElementTypes.VARIABLE_REFERENCE) {
            return new TcssVariableReference(node);
        }

        // Default: generic PSI element
        return new TcssPsiElement(node);
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new TcssFile(viewProvider);
    }
}
