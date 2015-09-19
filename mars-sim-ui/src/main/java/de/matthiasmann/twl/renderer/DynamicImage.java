/*
 * Copyright (c) 2008-2010, Matthias Mann
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

import java.nio.ByteBuffer;

/**
 * A dynamic image is created at runtime by the application and can be updated
 * any time.
 *
 * @author Matthias Mann
 */
public interface DynamicImage extends Image, Resource {

    public enum Format {
        /**
         * Bytes are ordered R, G, B, A
         */
        RGBA,
        /**
         * Bytes are ordered B, G, R, A
         */
        BGRA
    }

    /**
     * Updates the complete image.
     * 
     * @param data The new texels
     * @param format The format of the texel data
     * @throws IllegalArgumentException if the ByteBuffer does not contain enough data
     */
    public void update(ByteBuffer data, Format format);

    /**
     * Updates the complete image.
     * 
     * @param data The new texels
     * @param stride The number of bytes from one row to the next
     * @param format The format of the texel data
     * @throws IllegalArgumentException if the ByteBuffer does not contain enough data
     * or the stride is not a multiple of the bytes per pixel of specifed format
     */
    public void update(ByteBuffer data, int stride, Format format);

    /**
     * Updates a region of the image with new data.
     * 
     * @param xoffset Specifies a texel offset in the x direction within the image
     * @param yoffset Specifies a texel offset in the y direction within the image
     * @param width Specifies the width of the update area
     * @param height Specifies the height of the update area
     * @param data The new texels
     * @param format The format of the texel data
     * @throws IllegalArgumentException if the update area is not within the image bounds
     * or the ByteBuffer does not contain enough data
     */
    public void update(int xoffset, int yoffset, int width, int height, ByteBuffer data, Format format);
    
    /**
     * Updates a region of the image with new data.
     * 
     * @param xoffset Specifies a texel offset in the x direction within the image
     * @param yoffset Specifies a texel offset in the y direction within the image
     * @param width Specifies the width of the update area
     * @param height Specifies the height of the update area
     * @param data The new texels
     * @param stride The number of bytes from one row to the next
     * @param format The format of the texel data
     * @throws IllegalArgumentException if the update area is not within the image bounds
     * or the ByteBuffer does not contain enough data
     * or the stride is not a multiple of the bytes per pixel of specifed format
     */
    public void update(int xoffset, int yoffset, int width, int height, ByteBuffer data, int stride, Format format);

}
