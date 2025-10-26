package org.msaraiva.pytcss;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ElementColorProvider;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.msaraiva.pytcss.color.ColorFormat;
import org.msaraiva.pytcss.psi.TcssColorValue;
import org.msaraiva.pytcss.psi.TcssColorWithOpacityValue;
import org.msaraiva.pytcss.psi.TcssVariableDeclaration;
import org.msaraiva.pytcss.psi.TcssVariableReference;
import org.msaraiva.pytcss.util.VariableResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.Collection;

/**
 * Provides color preview gutter icons and color picker for TCSS color values.
 * Integrates with IntelliJ Platform's ElementColorProvider API.
 */
public class TcssColorProvider implements ElementColorProvider {
    private static ColorEditSession activeSession;

    /**
     * Extract color from a PSI element.
     * Called by IntelliJ to determine if element should show color gutter icon.
     *
     * IMPORTANT: ElementColorProvider works with leaf elements (tokens).
     * We check the parent to find TcssColorValue composite elements.
     *
     * @param element PSI element to check (will be a leaf token)
     * @return Color object or null if element is not a color value
     */
    @Nullable
    @Override
    public Color getColorFrom(@NotNull PsiElement element) {
        if (element.getFirstChild() != null) {
            return null;
        }
        // ElementColorProvider is called on leaf elements (tokens)
        // Check parent for TcssColorValue composite element
        PsiElement parent = element.getParent();
        if (!(parent instanceof TcssColorValue)) {
            return null;
        }

        TcssColorValue colorValue = (TcssColorValue) parent;

        // Special case: "auto" keyword should NOT show color icon
        // (it gets a special LineMarker instead)
        ColorFormat format = colorValue.getColorFormat();
        if (format == ColorFormat.AUTO) {
            return null;
        }

        // Resolve and return the color
        return colorValue.resolveColor();
    }

    /**
     * Resolve the target element for color picker updates.
     *
     * For variable references, this redirects to the first color value
     * in the variable declaration, so that updating the color modifies
     * the declaration rather than replacing the reference with a literal.
     *
     * @param element Original element (leaf token)
     * @return Target element to update, or null if cannot be resolved
     */
    @Nullable
    private ColorEditTarget resolveColorEditTarget(@NotNull PsiElement element) {
        if (!element.isValid()) {
            return null;
        }

        PsiElement parent = element.getParent();
        if (parent instanceof TcssVariableReference) {
            return resolveVariableTarget((TcssVariableReference) parent, 0);
        }

        if (parent instanceof TcssColorValue) {
            TcssColorValue value = (TcssColorValue) parent;
            if (!value.isValid()) {
                return null;
            }
            if (value instanceof TcssVariableReference) {
                return resolveVariableTarget((TcssVariableReference) value, 0);
            }
            PsiElement leaf = findEditLeaf(value, element);
            return leaf != null ? new ColorEditTarget(leaf, value) : null;
        }

        return null;
    }

    private static final int MAX_VARIABLE_RESOLUTION_DEPTH = 10;

    @Nullable
    private ColorEditTarget resolveVariableTarget(@NotNull TcssVariableReference reference, int depth) {
        if (depth >= MAX_VARIABLE_RESOLUTION_DEPTH) {
            return null;
        }

        String varName = reference.getVariableName();
        PsiFile file = reference.getContainingFile();
        if (file == null) {
            return null;
        }

        // Check local file first (shadowing)
        TcssVariableDeclaration declaration = VariableResolver.findDeclaration(varName, file);

        // Fall back to cross-file search
        if (declaration == null) {
            Project project = reference.getProject();
            Collection<TcssVariableDeclaration> crossFileDecls =
                    VariableResolver.findDeclarationsCrossFile(varName, project);
            if (!crossFileDecls.isEmpty()) {
                declaration = crossFileDecls.iterator().next(); // Use first found
            }
        }

        if (declaration == null || !declaration.isValid()) {
            return null;
        }

        TcssColorValue colorValue = declaration.getColorValue();
        if (colorValue == null || !colorValue.isValid()) {
            return null;
        }

        if (colorValue instanceof TcssVariableReference) {
            return resolveVariableTarget((TcssVariableReference) colorValue, depth + 1);
        }

        PsiElement leaf = findEditLeaf(colorValue, reference);
        return leaf != null ? new ColorEditTarget(leaf, colorValue) : null;
    }

    @Nullable
    private PsiElement findEditLeaf(@NotNull TcssColorValue value, @NotNull PsiElement fallback) {
        PsiElement child = value.getFirstChild();
        while (child != null) {
            if (child.isValid() && !child.getText().trim().isEmpty()) {
                return child;
            }
            child = child.getNextSibling();
        }

        com.intellij.lang.ASTNode node = value.getNode().getFirstChildNode();
        while (node != null) {
            PsiElement psi = node.getPsi();
            if (psi != null && psi.isValid() && !psi.getText().trim().isEmpty()) {
                return psi;
            }
            node = node.getTreeNext();
        }

        return fallback.isValid() ? fallback : null;
    }

    /**
     * Update element when user picks a new color from the color picker.
     * Preserves the original color format (hex stays hex, rgb stays rgb, etc.)
     *
     * IMPORTANT: ElementColorProvider passes leaf elements (tokens).
     * We need to work with the parent TcssColorValue composite element.
     *
     * For variable references: Updates the variable declaration, not the reference.
     * This ensures changing a variable's color updates $variable definition.
     *
     * @param element PSI element to update (will be a leaf token)
     * @param color   New color chosen by user
     */
    @Override
    public void setColorTo(@NotNull PsiElement element, @NotNull Color color) {
        ColorEditSession session = activeSession;

        boolean elementValid = element.isValid();
        ColorEditTarget target = null;
        if (elementValid) {
            target = resolveColorEditTarget(element);
            if (target == null || !target.leaf.isValid()) {
                return;
            }
        }

        Project project = elementValid
            ? element.getProject()
            : session != null ? session.project : null;
        if (project == null) {
            return;
        }

        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        Document document = null;

        if (elementValid) {
            PsiFile psiFile = element.getContainingFile();
            document = psiFile != null ? documentManager.getDocument(psiFile) : null;
        } else if (session != null) {
            document = session.document;
        }
        if (document == null) {
            return;
        }

        if (session == null || session.document != document) {
            session = new ColorEditSession(project, document);
            activeSession = session;
        }

        if (target != null) {
            TcssColorValue colorValue = target.colorValue;
            if (colorValue == null || !colorValue.isValid()) {
                session.clear();
                return;
            }

            ColorFormat detectedFormat = colorValue.getColorFormat();
            if (detectedFormat == null || detectedFormat == ColorFormat.AUTO) {
                session.clear();
                return;
            }

            TextRange range = colorValue.getTextRange();
            if (range == null) {
                session.clear();
                return;
            }

            RangeMarker marker = ensureRangeMarker(document, session.rangeMarker, range);
            if (marker == null) {
                session.clear();
                return;
            }

            session.rangeMarker = marker;

            if (colorValue instanceof TcssColorWithOpacityValue) {
                TcssColorWithOpacityValue withOpacity = (TcssColorWithOpacityValue) colorValue;
                TcssColorValue baseColor = withOpacity.getBaseColor();
                ColorFormat baseFormat = baseColor != null ? baseColor.getColorFormat() : detectedFormat;
                session.format = baseFormat != null ? baseFormat : detectedFormat;
                session.opacityContext = withOpacity.createOpacityContext();
            } else {
                session.format = detectedFormat;
                session.opacityContext = null;
            }
        }

        RangeMarker marker = session.rangeMarker;
        ColorFormat format = session.format;
        TcssColorWithOpacityValue.OpacityContext opacityContext = session.opacityContext;

        if (marker == null || !marker.isValid() || format == null || format == ColorFormat.AUTO) {
            return;
        }

        ColorFormat finalFormat = format;
        ColorEditSession finalSession = session;

        WriteCommandAction.runWriteCommandAction(project, () -> {
            if (!marker.isValid()) {
                finalSession.clear();
                return;
            }

            int startOffset = marker.getStartOffset();
            int endOffset = marker.getEndOffset();
            if (startOffset < 0 || endOffset > finalSession.document.getTextLength() || startOffset >= endOffset) {
                finalSession.clear();
                return;
            }

            documentManager.doPostponedOperationsAndUnblockDocument(finalSession.document);

            CharSequence currentText = finalSession.document.getCharsSequence().subSequence(startOffset, endOffset);
            String newColorText = formatColor(color, finalFormat, opacityContext);
            if (CharSequence.compare(currentText, newColorText) == 0) {
                return;
            }

            finalSession.document.replaceString(startOffset, endOffset, newColorText);
            marker.setGreedyToLeft(true);
            marker.setGreedyToRight(true);
            documentManager.commitDocument(finalSession.document);
        });
    }

    @Nullable
    private RangeMarker ensureRangeMarker(@NotNull Document document,
                                          @Nullable RangeMarker existing,
                                          @NotNull TextRange range) {
        int start = range.getStartOffset();
        int end = range.getEndOffset();

        if (start < 0 || end > document.getTextLength() || start >= end) {
            return null;
        }

        if (existing != null && existing.isValid() &&
            existing.getStartOffset() == start &&
            existing.getEndOffset() == end) {
            return existing;
        }

        RangeMarker marker = document.createRangeMarker(start, end);
        marker.setGreedyToLeft(true);
        marker.setGreedyToRight(true);
        return marker;
    }

    private static final class ColorEditSession {
        private final Project project;
        private final Document document;
        private RangeMarker rangeMarker;
        private ColorFormat format;
        private TcssColorWithOpacityValue.OpacityContext opacityContext;

        private ColorEditSession(@NotNull Project project, @NotNull Document document) {
            this.project = project;
            this.document = document;
        }

        private void clear() {
            rangeMarker = null;
            format = null;
            opacityContext = null;
        }
    }

    private static final class ColorEditTarget {
        private final PsiElement leaf;
        private final TcssColorValue colorValue;

        private ColorEditTarget(@NotNull PsiElement leaf, @NotNull TcssColorValue colorValue) {
            this.leaf = leaf;
            this.colorValue = colorValue;
        }
    }

    /**
     * Format color according to specified format.
     * Preserves user's original format choice.
     */
    @NotNull
    private String formatColor(@NotNull Color color,
                               @NotNull ColorFormat format,
                               @Nullable TcssColorWithOpacityValue.OpacityContext opacityContext) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = color.getAlpha();
        boolean useAlphaChannel = opacityContext == null && a < 255;

        String baseText;

        switch (format) {
            case HEX_3:
            case HEX_4:
            case HEX_6:
            case HEX_8:
                // Convert to hex format
                if (useAlphaChannel) {
                    baseText = String.format("#%02x%02x%02x%02x", r, g, b, a);
                } else {
                    baseText = String.format("#%02x%02x%02x", r, g, b);
                }
                break;

            case RGB:
            case RGBA:
                // Convert to rgb/rgba format
                if (useAlphaChannel) {
                    float alpha = a / 255.0f;
                    baseText = String.format("rgba(%d, %d, %d, %.2f)", r, g, b, alpha);
                } else {
                    baseText = String.format("rgb(%d, %d, %d)", r, g, b);
                }
                break;

            case HSL:
            case HSLA:
                // Convert RGB to HSL
                float[] hsl = rgbToHsl(r, g, b);
                int h = Math.round(hsl[0] * 360);
                int s = Math.round(hsl[1] * 100);
                int l = Math.round(hsl[2] * 100);

                if (useAlphaChannel) {
                    float alpha = a / 255.0f;
                    baseText = String.format("hsla(%d, %d%%, %d%%, %.2f)", h, s, l, alpha);
                } else {
                    baseText = String.format("hsl(%d, %d%%, %d%%)", h, s, l);
                }
                break;

            case KEYWORD:
                // Try to find nearest named color
                // For now, just convert to hex
                baseText = String.format("#%02x%02x%02x", r, g, b);
                break;

            default:
                baseText = String.format("#%02x%02x%02x", r, g, b);
                break;
        }

        if (opacityContext != null) {
            return opacityContext.render(baseText, a);
        }

        return baseText;
    }

    /**
     * Convert RGB to HSL.
     * @param r Red (0-255)
     * @param g Green (0-255)
     * @param b Blue (0-255)
     * @return float array [h, s, l] where h,s,l are 0-1
     */
    @NotNull
    private float[] rgbToHsl(int r, int g, int b) {
        float rf = r / 255.0f;
        float gf = g / 255.0f;
        float bf = b / 255.0f;

        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;

        float h = 0, s = 0, l = (max + min) / 2.0f;

        if (delta != 0) {
            s = l < 0.5f ? delta / (max + min) : delta / (2.0f - max - min);

            if (max == rf) {
                h = ((gf - bf) / delta) + (gf < bf ? 6 : 0);
            } else if (max == gf) {
                h = ((bf - rf) / delta) + 2;
            } else {
                h = ((rf - gf) / delta) + 4;
            }
            h /= 6.0f;
        }

        return new float[]{h, s, l};
    }
}
