package org.msaraiva.pytcss.color;

/**
 * Enum representing different color format types supported by TCSS.
 * Used for categorizing color values and determining parsing/formatting strategies.
 */
public enum ColorFormat {
    /**
     * 3-digit hex color: #RGB (e.g., #f00)
     */
    HEX_3,

    /**
     * 4-digit hex color with alpha: #RGBA (e.g., #f00f)
     */
    HEX_4,

    /**
     * 6-digit hex color: #RRGGBB (e.g., #ff0000)
     */
    HEX_6,

    /**
     * 8-digit hex color with alpha: #RRGGBBAA (e.g., #ff0000ff)
     */
    HEX_8,

    /**
     * RGB function: rgb(r, g, b) (e.g., rgb(255, 0, 0))
     */
    RGB,

    /**
     * RGBA function with alpha: rgba(r, g, b, a) (e.g., rgba(255, 0, 0, 0.5))
     */
    RGBA,

    /**
     * HSL function: hsl(h, s%, l%) (e.g., hsl(120, 100%, 50%))
     */
    HSL,

    /**
     * HSLA function with alpha: hsla(h, s%, l%, a) (e.g., hsla(120, 100%, 50%, 0.5))
     */
    HSLA,

    /**
     * Named color keyword (e.g., red, blue, ansi_bright_green)
     */
    KEYWORD,

    /**
     * Auto keyword for automatic color selection
     */
    AUTO
}
