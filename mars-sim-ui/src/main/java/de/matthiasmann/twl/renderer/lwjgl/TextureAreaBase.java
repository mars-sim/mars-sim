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
package de.matthiasmann.twl.renderer.lwjgl;

import org.lwjgl.opengl.GL11;

/**
 * Base class to render a sub region of a GL texture
 * 
 * @author Matthias Mann
 */
public class TextureAreaBase {
    
    protected final float tx0;
    protected final float ty0;
    protected final float tx1;
    protected final float ty1;
    protected final short width;
    protected final short height;

    TextureAreaBase(int x, int y, int width, int height, float texWidth, float texHeight) {
        // negative size allows for flipping
        this.width = (short)Math.abs(width);
        this.height = (short)Math.abs(height);
        float fx = x;
        float fy = y;
        if(width == 1 || width == -1) {
            fx += 0.5f;
            width = 0;
        } else if(width < 0) {
            fx -= width;
        }
        if(height == 1 || height == -1) {
            fy += 0.5f;
            height = 0;
        } else if(height < 0) {
            fy -= height;
        }
        this.tx0 = fx / texWidth;
        this.ty0 = fy / texHeight;
        this.tx1 = tx0 + (width / texWidth);
        this.ty1 = ty0 + (height / texHeight);
    }

    TextureAreaBase(TextureAreaBase src) {
        this.tx0 = src.tx0;
        this.ty0 = src.ty0;
        this.tx1 = src.tx1;
        this.ty1 = src.ty1;
        this.width = src.width;
        this.height = src.height;
    }

    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }

    void drawQuad(int x, int y, int w, int h) {
        GL11.glTexCoord2f(tx0, ty0); GL11.glVertex2i(x    , y    );
        GL11.glTexCoord2f(tx0, ty1); GL11.glVertex2i(x    , y + h);
        GL11.glTexCoord2f(tx1, ty1); GL11.glVertex2i(x + w, y + h);
        GL11.glTexCoord2f(tx1, ty0); GL11.glVertex2i(x + w, y    );
    }
}
