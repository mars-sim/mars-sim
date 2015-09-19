/*
 * Copyright (c) 2008-2014, Matthias Mann
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
import de.matthiasmann.twl.renderer.QueriablePixels;
import de.matthiasmann.twl.renderer.SupportsDrawRepeat;
import org.lwjgl.opengl.GL11;

/**
 * A area inside a OpenGL texture used as UI image
 * 
 * @author Matthias Mann
 */
public class TextureArea extends TextureAreaBase implements Image, SupportsDrawRepeat, QueriablePixels {

    protected static final int REPEAT_CACHE_SIZE = 10;
    
    protected final LWJGLTexture texture;
    protected final Color tintColor;
    protected int repeatCacheID = -1;
    
    public TextureArea(LWJGLTexture texture, int x, int y, int width, int height, Color tintColor) {
        super(x, y, width, height, texture.getTexWidth(), texture.getTexHeight());
        this.texture = texture;
        this.tintColor = (tintColor == null) ? Color.WHITE : tintColor;
    }

    TextureArea(TextureArea src, Color tintColor) {
        super(src);
        this.texture = src.texture;
        this.tintColor = tintColor;
    }

    public int getPixelValue(int x, int y) {
        if(x < 0 || y < 0 || x >= width || y >= height) {
            throw new IllegalArgumentException();
        }
        
        int texWidth = texture.getTexWidth();
        int texHeight = texture.getTexHeight();
        
        int baseX = (int)(tx0*texWidth);
        int baseY = (int)(ty0*texHeight);
        
        if(tx0 > tx1) {
            x = baseX - x;
        } else {
            x = baseX + x;
        }
        
        if(ty0 > ty1) {
            y = baseY - y;
        } else {
            y = baseY + y;
        }
        
        if(x < 0) {
            x = 0;
        } else if(x >= texWidth) {
            x = texWidth-1;
        }
        
        if(y < 0) {
            y = 0;
        } else if(y >= texHeight) {
            y = texHeight-1;
        }
        
        return texture.getPixelValue(x, y);
    }

    public void draw(AnimationState as, int x, int y) {
        draw(as, x, y, width, height);
    }

    public void draw(AnimationState as, int x, int y, int w, int h) {
        if(texture.bind(tintColor)) {
            GL11.glBegin(GL11.GL_QUADS);
            drawQuad(x, y, w, h);
            GL11.glEnd();
        }
    }

    public void draw(AnimationState as, int x, int y, int width, int height, int repeatCountX, int repeatCountY) {
        if(texture.bind(tintColor)) {
            if((repeatCountX * this.width != width) || (repeatCountY * this.height != height)) {
                drawRepeatSlow(x, y, width, height, repeatCountX, repeatCountY);
                return;
            }
            if(repeatCountX < REPEAT_CACHE_SIZE || repeatCountY < REPEAT_CACHE_SIZE) {
                drawRepeat(x, y, repeatCountX, repeatCountY);
                return;
            }
            drawRepeatCached(x, y, repeatCountX, repeatCountY);
        }
    }

    private void drawRepeatSlow(int x, int y, int width, int height, int repeatCountX, int repeatCountY) {
        GL11.glBegin(GL11.GL_QUADS);
        while(repeatCountY > 0) {
            int rowHeight = height / repeatCountY;

            int cx = 0;
            for(int xi=0 ; xi<repeatCountX ;) {
                int nx = ++xi * width / repeatCountX;
                drawQuad(x+cx, y, nx-cx, rowHeight);
                cx = nx;
            }

            y += rowHeight;
            height -= rowHeight;
            repeatCountY--;
        }
        GL11.glEnd();
    }
    
    protected void drawRepeat(int x, int y, int repeatCountX, int repeatCountY) {
        final int w = width;
        final int h = height;
        GL11.glBegin(GL11.GL_QUADS);
        while(repeatCountY-- > 0) {
            int curX = x;
            int cntX = repeatCountX;
            while(cntX-- > 0) {
                drawQuad(curX, y, w, h);
                curX += w;
            }
            y += h;
        }
        GL11.glEnd();
    }

    protected void drawRepeatCached(int x, int y, int repeatCountX, int repeatCountY) {
        if(repeatCacheID < 0) {
            createRepeatCache();
        }
        
        int cacheBlocksX = repeatCountX / REPEAT_CACHE_SIZE;
        int repeatsByCacheX = cacheBlocksX * REPEAT_CACHE_SIZE;

        if(repeatCountX > repeatsByCacheX) {
            drawRepeat(x + width * repeatsByCacheX, y,
                    repeatCountX - repeatsByCacheX, repeatCountY);
        }

        do {
            GL11.glPushMatrix();
            GL11.glTranslatef(x, y, 0f);
            GL11.glCallList(repeatCacheID);

            for(int i=1 ; i<cacheBlocksX ; i++) {
                GL11.glTranslatef(width * REPEAT_CACHE_SIZE, 0f, 0f);
                GL11.glCallList(repeatCacheID);
            }

            GL11.glPopMatrix();
            repeatCountY -= REPEAT_CACHE_SIZE;
            y += height * REPEAT_CACHE_SIZE;
        } while(repeatCountY >= REPEAT_CACHE_SIZE);
        
        if(repeatCountY > 0) {
            drawRepeat(x, y, repeatsByCacheX, repeatCountY);
        }
    }

    protected void createRepeatCache() {
        repeatCacheID = GL11.glGenLists(1);
        texture.renderer.textureAreas.add(this);

        GL11.glNewList(repeatCacheID, GL11.GL_COMPILE);
        drawRepeat(0, 0, REPEAT_CACHE_SIZE, REPEAT_CACHE_SIZE);
        GL11.glEndList();
    }

    void destroyRepeatCache() {
        GL11.glDeleteLists(repeatCacheID, 1);
        repeatCacheID = -1;
    }

    public Image createTintedVersion(Color color) {
        if(color == null) {
            throw new NullPointerException("color");
        }
        Color newTintColor = tintColor.multiply(color);
        if(newTintColor.equals(tintColor)) {
            return this;
        }
        return new TextureArea(this, newTintColor);
    }

}
