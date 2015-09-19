/*
 * Copyright (c) 2008-2009, Matthias Mann
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

import de.matthiasmann.twl.Color;

/**
 * A image object can be used for rendering.
 * 
 * @author Matthias Mann
 */
public interface Image {

    /**
     * Returns the width in pixels of the image
     * @return the width in pixels of the image
     */
    public int getWidth();
    
    /**
     * Returns the height in pixels of the image
     * @return the height in pixels of the image
     */
    public int getHeight();
    
    /**
     * Draws the image in it's original size at the given location
     * @param as A time source for animation - may be null
     * @param x left coordinate
     * @param y top coordinate
     */
    public void draw(AnimationState as, int x, int y);
    
    /**
     * Draws the image scaled to the given size at the given location
     * @param as A time source for animation - may be null
     * @param x left coordinate
     * @param y top coordinate
     * @param width the width in pixels
     * @param height the height in pixels
     */
    public void draw(AnimationState as, int x, int y, int width, int height);

    /**
     * Creates a new image with is tinted with the specified color.
     *
     * Tinting works by multiplying the color of the image's pixels
     * with the specified color.
     *
     * @param color The color used for tinting.
     * @return a new Image object.
     */
    public Image createTintedVersion(Color color);
}
