/*
 * Copyright (c) 2008, Matthias Mann
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

import de.matthiasmann.twl.HAlignment;

/**
 * A font rendering interface
 * @author Matthias Mann
 */
public interface Font extends Resource {

    /**
     * Returns true if the font is proportional or false if it's fixed width.
     * @return true if the font is proportional
     */
    boolean isProportional();
    
    /**
     * Returns the base line of the font measured in pixels from the top of the text bounding box
     * @return the base line of the font measured in pixels from the top of the text bounding box
     */
    int getBaseLine();

    /**
     * Returns the line height in pixels for this font
     * @return the line height in pixels for this font
     */
    int getLineHeight();

    /**
     * Returns the width of a ' '
     * @return the width of a ' '
     */
    int getSpaceWidth();
    
    /**
     * Returns the width of the letter 'M'
     * @return the width of the letter 'M'
     */
    int getEM();

    /**
     * Returns the height of the letter 'x'
     * @return the height of the letter 'x'
     */
    int getEX();

    /**
     * Computes the width in pixels of the longest text line. Lines are splitted at '\n'
     * @param str the text to evaluate
     * @return the width in pixels of the longest line
     */
    int computeMultiLineTextWidth(CharSequence str);

    /**
     * Computes the width in pixels of a text
     * @param str the text to evaluate
     * @return the width in pixels
     */
    int computeTextWidth(CharSequence str);

    /**
     * Computes the width in pixels of a text
     * @param str the text to evaluate
     * @param start index of first character in str
     * @param end index after last character in str
     * @return the width in pixels
     */
    int computeTextWidth(CharSequence str, int start, int end);

    /**
     * Computes how many glyphs of the supplied CharSequence can be display
     * completly in the given amount of pixels.
     * 
     * @param str the CharSequence
     * @param start the start index in the CharSequence
     * @param end the end index (exclusive) in the CharSequence
     * @param width the number of available pixels.
     * @return the number (relative to start) of fitting glyphs
     */
    int computeVisibleGlpyhs(CharSequence str, int start, int end, int width);

    /**
     * Draws multi line text - lines are splitted at '\n'
     * @param as A time source for animation - may be null
     * @param x left coordinate of the text block 
     * @param y top coordinate of the text block
     * @param str the text to draw
     * @param width the width of the text block
     * @param align horizontal alignment for shorter lines
     * @return the height in pixels of the multi line text
     */
    int drawMultiLineText(AnimationState as, int x, int y, CharSequence str, int width, HAlignment align);

    /**
     * Draws a single line text
     * @param as A time source for animation - may be null
     * @param x left coordinate of the text block 
     * @param y top coordinate of the text block
     * @param str the text to draw
     * @return the width in pixels of the text
     */
    int drawText(AnimationState as, int x, int y, CharSequence str);

    /**
     * Draws a single line text
     * @param as A time source for animation - may be null
     * @param x left coordinate of the text block 
     * @param y top coordinate of the text block
     * @param str the text to draw
     * @param start index of first character to draw in str
     * @param end index after last character to draw in str
     * @return the width in pixels of the text
     */
    int drawText(AnimationState as, int x, int y, CharSequence str, int start, int end);

    /**
     * Caches a text for faster drawing
     * @param prevCache the previous cached text or null
     * @param str the text to cache
     * @param width the width of the text block
     * @param align horizontal alignment for shorter lines
     * @return A cache object or null if caching was not possible
     */
    FontCache cacheMultiLineText(FontCache prevCache, CharSequence str, int width, HAlignment align);

    /**
     * Caches a text for faster drawing
     * @param prevCache the previous cached text or null
     * @param str the text to cache
     * @return A cache object or null if caching was not possible
     */
    FontCache cacheText(FontCache prevCache, CharSequence str);
    
    /**
     * Caches a text for faster drawing
     * @param prevCache the previous cached text or null
     * @param str the text to cache
     * @param start index of first character to draw in str
     * @param end index after last character to draw in str
     * @return A cache object or null if caching was not possible
     */
    FontCache cacheText(FontCache prevCache, CharSequence str, int start, int end);
}
