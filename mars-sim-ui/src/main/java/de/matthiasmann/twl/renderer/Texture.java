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
 * A texture class. Can not be used for rendering directly.
 * 
 * @author Matthias Mann
 */
public interface Texture extends Resource {

    /**
     * Returns the width in pixels of this texture.
     * @return the width in pixels of this texture.
     */
    public int getWidth();
    
    /**
     * Returns the height in pixels of this texture.
     * @return the height in pixels of this texture.
     */
    public int getHeight();
    
    /**
     * Creates an image from a sub section of this texture.
     * @param x left coordinate in the texture of the image
     * @param y top coordinate in the texture of the image
     * @param width width in pixels of the image - if negative the image is horizontaly flipped
     * @param height height in pixels of the image - if negative the image is vertically flipped
     * @param tintColor the tintColor - maybe null
     * @param tiled true if this image should do tiled rendering
     * @param rotation the rotation to apply to this sub section while rendering
     * @see Image#createTintedVersion(de.matthiasmann.twl.Color)
     * @return an image object
     */
    public Image getImage(int x, int y, int width, int height, Color tintColor, boolean tiled, Rotation rotation);

    public MouseCursor createCursor(int x, int y, int width, int height, int hotSpotX, int hotSpotY, Image imageRef);

    /**
     * After calling this function getImage() and createCursor() may fail to work
     */
    public void themeLoadingDone();
    
    public enum Rotation {
        NONE,
        CLOCKWISE_90,
        CLOCKWISE_180,
        CLOCKWISE_270
    }
}
