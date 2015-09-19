/*
 * Copyright (c) 2008-2011, Matthias Mann
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

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.Image;
import org.lwjgl.opengl.GL11;

/**
 * A tiled area inside a OpenGL texture used as UI image
 *
 * @author Matthias Mann
 */
public class TextureAreaTiled extends TextureArea {
    
    public TextureAreaTiled(LWJGLTexture texture, int x, int y, int width, int height, Color tintColor) {
        super(texture, x, y, width, height, tintColor);
    }

    TextureAreaTiled(TextureAreaTiled src, Color tintColor) {
        super(src, tintColor);
    }

    @Override
    public void draw(AnimationState as, int x, int y, int w, int h) {
        if(texture.bind(tintColor)) {
            int repeatCountX = w / this.width;
            int repeatCountY = h / this.height;

            if(repeatCountX < REPEAT_CACHE_SIZE || repeatCountY < REPEAT_CACHE_SIZE) {
                drawRepeat(x, y, repeatCountX, repeatCountY);
            } else {
                drawRepeatCached(x, y, repeatCountX, repeatCountY);
            }

            int drawnX = repeatCountX * this.width;
            int drawnY = repeatCountY * this.height;
            int restWidth = w - drawnX;
            int restHeight = h - drawnY;
            if(restWidth > 0 || restHeight > 0) {
                GL11.glBegin(GL11.GL_QUADS);
                if(restWidth > 0 && repeatCountY > 0) {
                    drawClipped(x + drawnX, y, restWidth, this.height, 1, repeatCountY);
                }
                if(restHeight > 0) {
                    if(repeatCountX > 0) {
                        drawClipped(x, y + drawnY, this.width, restHeight, repeatCountX, 1);
                    }
                    if(restWidth > 0) {
                        drawClipped(x + drawnX, y + drawnY, restWidth, restHeight, 1, 1);
                    }
                }
                GL11.glEnd();
            }
        }
    }

    private void drawClipped(int x, int y, int width, int height, int repeatCountX, int repeatCountY) {
        float ctx0 = tx0;
        float cty0 = ty0;
        float ctx1 = tx1;
        float cty1 = ty1;
        if(this.width > 1) {
            ctx1 = ctx0 + width / (float)texture.getTexWidth();
        }
        if(this.height > 1) {
            cty1 = cty0 + height / (float)texture.getTexHeight();
        }

        while(repeatCountY-- > 0) {
            int y1 = y + height;
            int x0 = x;
            for(int cx=repeatCountX ; cx-- > 0 ;) {
                int x1 = x0 + width;
                GL11.glTexCoord2f(ctx0, cty0); GL11.glVertex2i(x0, y );
                GL11.glTexCoord2f(ctx0, cty1); GL11.glVertex2i(x0, y1);
                GL11.glTexCoord2f(ctx1, cty1); GL11.glVertex2i(x1, y1);
                GL11.glTexCoord2f(ctx1, cty0); GL11.glVertex2i(x1, y );
                x0 = x1;
            }
            y = y1;
        }
    }
    
    @Override
    public Image createTintedVersion(Color color) {
        if(color == null) {
            throw new NullPointerException("color");
        }
        Color newTintColor = tintColor.multiply(color);
        if(newTintColor.equals(tintColor)) {
            return this;
        }
        return new TextureAreaTiled(this, newTintColor);
    }
}
