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
package de.matthiasmann.twl.renderer;

import de.matthiasmann.twl.utils.StateSelect;
import de.matthiasmann.twl.utils.StringList;
import java.io.IOException;
import java.net.URL;

/**
 * A font mapper which tries to retrieve the closest font for the specified parameters
 * 
 * @author Matthias Mann
 */
public interface FontMapper extends Resource {
    
    public static final int STYLE_NORMAL = 0;
    public static final int STYLE_BOLD   = 1;
    public static final int STYLE_ITALIC = 2;
    
    /**
     * Register this font is as a weak font.
     * 
     * <p>If a font is already registered for the specified
     * fontFamily and style then this registration is ignored.</p>
     * <p>If a non weak font is registered for the specified
     * fontFamily and style then an existing weak font is replaced.</p>
     */
    public static final int REGISTER_WEAK = 256;
    
    /**
     * Retrive the cloest font for the given parameters
     * 
     * @param fontFamilies a list of family names with decreasing piority
     * @param fontSize the desired font size in pixels
     * @param style a combination of the STYLE_* flags
     * @param select the StateSelect object
     * @param fontParams the font parameters - must be exactly 1 more then
     *                   the number of expressions in the select object
     * @return the Font object or {@code null} if the font could not be found
     * @throws NullPointerException when one of the parameters is null
     * @throws IllegalArgumentException when the number of font parameters doesn't match the number of state expressions
     */
    public Font getFont(StringList fontFamilies, int fontSize, int style,
            StateSelect select, FontParameter ... fontParams);
    
    /**
     * Registers a font file
     * 
     * @param fontFamily the font family for which to register the font
     * @param style a combination of the STYLE_* and REGISTER_* flags
     * @param url the URL for the font file
     * @return true if the specified font could be registered
     */
    public boolean registerFont(String fontFamily, int style, URL url);
    
    /**
     * Registers a font file and determines the style from the font itself.
     * 
     * @param fontFamily the font family for which to register the font
     * @param url the URL for the font file
     * @return true if the specified font could be registered
     * @throws IOException when the font could not be parsed 
     */
    public boolean registerFont(String fontFamily, URL url) throws IOException;
}
