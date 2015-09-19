/*
 * Copyright (c) 2008-2012, Matthias Mann
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Matthias Mann nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.matthiasmann.twl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;

/**
 * A immutable color class. Colors are represented as bytes.
 *
 * @author Matthias Mann
 */
public final class Color {

    public static final Color BLACK = new Color(0xFF000000);
    public static final Color SILVER = new Color(0xFFC0C0C0);
    public static final Color GRAY = new Color(0xFF808080);
    public static final Color WHITE = new Color(0xFFFFFFFF);
    public static final Color MAROON = new Color(0xFF800000);
    public static final Color RED = new Color(0xFFFF0000);
    public static final Color PURPLE = new Color(0xFF800080);
    public static final Color FUCHSIA = new Color(0xFFFF00FF);
    public static final Color GREEN = new Color(0xFF008000);
    public static final Color LIME = new Color(0xFF00FF00);
    public static final Color OLIVE = new Color(0xFF808000);
    public static final Color ORANGE = new Color(0xFFFFA500);
    public static final Color YELLOW = new Color(0xFFFFFF00);
    public static final Color NAVY = new Color(0xFF000080);
    public static final Color BLUE = new Color(0xFF0000FF);
    public static final Color TEAL = new Color(0xFF008080);
    public static final Color AQUA = new Color(0xFF00FFFF);
    public static final Color SKYBLUE = new Color(0xFF87CEEB);
    
    public static final Color LIGHTBLUE    = new Color(0xFFADD8E6);
    public static final Color LIGHTCORAL   = new Color(0xFFF08080);
    public static final Color LIGHTCYAN    = new Color(0xFFE0FFFF);
    public static final Color LIGHTGRAY    = new Color(0xFFD3D3D3);
    public static final Color LIGHTGREEN   = new Color(0xFF90EE90);
    public static final Color LIGHTPINK    = new Color(0xFFFFB6C1);
    public static final Color LIGHTSALMON  = new Color(0xFFFFA07A);
    public static final Color LIGHTSKYBLUE = new Color(0xFF87CEFA);
    public static final Color LIGHTYELLOW  = new Color(0xFFFFFFE0);
    
    public static final Color TRANSPARENT = new Color(0);
    
    private final byte r;
    private final byte g;
    private final byte b;
    private final byte a;

    public Color(byte r, byte g, byte b, byte a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    /**
     * Creates a new color object from an integer in ARGB order.
     * 
     * bits  0- 7 are blue
     * bits  8-15 are green
     * bits 16-23 are red
     * bits 24-31 are alpha
     * 
     * @param argb the color value as integer
     */
    public Color(int argb) {
        this.a = (byte)(argb >> 24);
        this.r = (byte)(argb >> 16);
        this.g = (byte)(argb >>  8);
        this.b = (byte)(argb      );
    }

    /**
     * Converts this color into an integer in ARGB format
     *
     * @return the color value as integer
     * @see #Color(int)
     */
    public int toARGB() {
        return ((a & 255) << 24) |
                ((r & 255) << 16) |
                ((g & 255) <<  8) |
                ((b & 255)      );
    }

    public byte getR() {
        return r;
    }

    public byte getG() {
        return g;
    }

    public byte getB() {
        return b;
    }

    public byte getA() {
        return a;
    }

    public int getRed() {
        return r&255;
    }

    public int getGreen() {
        return g&255;
    }

    public int getBlue() {
        return b&255;
    }

    public int getAlpha() {
        return a&255;
    }

    public float getRedFloat() {
        return (r & 255) * (1.0f / 255f);
    }

    public float getGreenFloat() {
        return (g & 255) * (1.0f / 255f);
    }

    public float getBlueFloat() {
        return (b & 255) * (1.0f / 255f);
    }

    public float getAlphaFloat() {
        return (a & 255) * (1.0f / 255f);
    }

    public void getFloats(float[] dst, int off) {
        dst[off+0] = getRedFloat();
        dst[off+1] = getGreenFloat();
        dst[off+2] = getBlueFloat();
        dst[off+3] = getAlphaFloat();
    }

    /**
     * Retrieves a color by it's name. This uses the case insensitive lookup
     * for the color constants defined in this class.
     *
     * @param name the color name to lookup
     * @return a Color or null if the name was not found
     */
    public static Color getColorByName(String name) {
        name = name.toUpperCase(Locale.ENGLISH);
        try {
            Field f = Color.class.getField(name);
            if(Modifier.isStatic(f.getModifiers()) && f.getType() == Color.class) {
                return (Color)f.get(null);
            }
        } catch (Throwable ex) {
            // ignore
        }
        return null;
    }

    /**
     * Parses a numeric or symbolic color. Symbolic names are resolved by getColorByName
     *
     * The following hex formats are supported:
     * #RGB
     * #ARGB
     * #RRGGBB
     * #AARRGGBB
     *
     * @param value the color to parse
     * @return a Color object or null
     * @throws NumberFormatException if the hex color code can't be parsed
     * @see #getColorByName(java.lang.String)
     */
    public static Color parserColor(String value) throws NumberFormatException {
        if(value.length() > 0 && value.charAt(0) == '#') {
            String hexcode = value.substring(1);
            switch (value.length()) {
                case 4: {
                    int rgb4 = Integer.parseInt(hexcode, 16);
                    int r = ((rgb4 >> 8) & 0xF) * 0x11;
                    int g = ((rgb4 >> 4) & 0xF) * 0x11;
                    int b = ((rgb4     ) & 0xF) * 0x11;
                    return new Color(0xFF000000 | (r << 16) | (g << 8) | b);
                }
                case 5: {
                    int rgb4 = Integer.parseInt(hexcode, 16);
                    int a = ((rgb4 >> 12) & 0xF) * 0x11;
                    int r = ((rgb4 >>  8) & 0xF) * 0x11;
                    int g = ((rgb4 >>  4) & 0xF) * 0x11;
                    int b = ((rgb4      ) & 0xF) * 0x11;
                    return new Color((a << 24) | (r << 16) | (g << 8) | b);
                }
                case 7:
                    return new Color(0xFF000000 | Integer.parseInt(hexcode, 16));
                case 9:
                    return new Color((int)Long.parseLong(hexcode, 16));
                default:
                    throw new NumberFormatException("Can't parse '" + value + "' as hex color");
            }
        }
        return Color.getColorByName(value);
    }

    /**
     * Converts this color into it's hex string.
     *
     * If alpha is 255 then a string in "#RRGGBB" format is created,
     * otherwise the "#AARRGGBB" format is created
     *
     * @return hex representation of this color
     */
    @Override
    public String toString() {
        if(a != -1) {
            return String.format("#%08X", toARGB());
        } else {
            return String.format("#%06X", toARGB() & 0xFFFFFF);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Color)) {
            return false;
        }
        final Color other = (Color)obj;
        return this.toARGB() == other.toARGB();
    }

    @Override
    public int hashCode() {
        return toARGB();
    }

    public Color multiply(Color other) {
        return new Color(
                mul(r, other.r),
                mul(g, other.g),
                mul(b, other.b),
                mul(a, other.a));
    }

    private byte mul(byte a, byte b) {
        return (byte)(((a & 255) * (b & 255)) / 255);
    }
}
