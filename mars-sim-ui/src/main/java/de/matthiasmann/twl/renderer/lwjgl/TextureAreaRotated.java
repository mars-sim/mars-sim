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
import de.matthiasmann.twl.renderer.Texture;
import org.lwjgl.opengl.GL11;

/**
 * A rotated, tiled or flipped area inside a OpenGL texture used as UI image
 *
 * @author Matthias Mann
 */
public class TextureAreaRotated implements Image {

    protected static final int REPEAT_CACHE_SIZE = 10;
    
    private final LWJGLTexture texture;
    private final Color tintColor;
    private final float txTL;
    private final float tyTL;
    private final float txTR;
    private final float tyTR;
    private final float txBL;
    private final float tyBL;
    private final float txBR;
    private final float tyBR;
    private final char width;
    private final char height;
    private final boolean tiled;
    protected int repeatCacheID = -1;
    
    public TextureAreaRotated(LWJGLTexture texture, int x, int y, int width, int height,
            Color tintColor, boolean tiled, Texture.Rotation rotation) {
        // negative size allows for flipping
        if(rotation == Texture.Rotation.CLOCKWISE_90 || rotation == Texture.Rotation.CLOCKWISE_270) {
            this.width = (char)Math.abs(height);
            this.height = (char)Math.abs(width);
        } else {
            this.width = (char)Math.abs(width);
            this.height = (char)Math.abs(height);
        }
        
        float fx = x;
        float fy = y;
        if(width == 1) {
            fx += 0.5f;
            width = 0;
        } else if(width < -1) {
            fx -= width + 1;
        }
        if(height == 1) {
            fy += 0.5f;
            height = 0;
        } else if(height < -1) {
            fy -= height + 1;
        }
        
        float texWidth = texture.getTexWidth();
        float texHeight = texture.getTexHeight();
        
        float tx0 = fx / texWidth;
        float ty0 = fy / texHeight;
        float tx1 = tx0 + (width / texWidth);
        float ty1 = ty0 + (height / texHeight);
        
        switch(rotation) {
            default:
                txTL = txBL = tx0;
                txTR = txBR = tx1;
                tyTL = tyTR = ty0;
                tyBL = tyBR = ty1;
                break;
            case CLOCKWISE_90:
                txTL = tx0; tyTL = ty1;
                txTR = tx0; tyTR = ty0;
                txBL = tx1; tyBL = ty1;
                txBR = tx1; tyBR = ty0;
                break;
            case CLOCKWISE_180:
                txTL = tx1; tyTL = ty1;
                txTR = tx0; tyTR = ty1;
                txBL = tx1; tyBL = ty0;
                txBR = tx0; tyBR = ty0;
                break;
            case CLOCKWISE_270:
                txTL = tx1; tyTL = ty0;
                txTR = tx1; tyTR = ty1;
                txBL = tx0; tyBL = ty0;
                txBR = tx0; tyBR = ty1;
                break;
        }
        this.texture = texture;
        this.tintColor = (tintColor == null) ? Color.WHITE : tintColor;
        this.tiled = tiled;
    }

    TextureAreaRotated(TextureAreaRotated src, Color tintColor) {
        this.txTL = src.txTL;
        this.tyTL = src.tyTL;
        this.txTR = src.txTR;
        this.tyTR = src.tyTR;
        this.txBL = src.txBL;
        this.tyBL = src.tyBL;
        this.txBR = src.txBR;
        this.tyBR = src.tyBR;
        this.width = src.width;
        this.height = src.height;
        this.texture = src.texture;
        this.tiled = src.tiled;
        this.tintColor = tintColor;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void draw(AnimationState as, int x, int y) {
        draw(as, x, y, width, height);
    }

    public void draw(AnimationState as, int x, int y, int w, int h) {
        if(texture.bind(tintColor)) {
            if(tiled) {
                drawTiled(x, y, w, h);
            } else {
                GL11.glBegin(GL11.GL_QUADS);
                drawQuad(x, y, w, h);
                GL11.glEnd();
            }
        }
    }
    
    private void drawRepeat(int x, int y, int repeatCountX, int repeatCountY) {
        GL11.glBegin(GL11.GL_QUADS);
        final int w = width;
        final int h = height;
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
    
    private void drawTiled(int x, int y, int w, int h) {
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

    private void drawClipped(int x, int y, int width, int height, int repeatCountX, int repeatCountY) {
        float ctxTL = txTL;
        float ctyTL = tyTL;
        float ctxTR = txTR;
        float ctyTR = tyTR;
        float ctxBL = txBL;
        float ctyBL = tyBL;
        float ctxBR = txBR;
        float ctyBR = tyBR;
        
        if(this.width > 1) {
            float f = width / (float)this.width;
            ctxTR = ctxTL + (ctxTR - ctxTL) * f;
            ctyTR = ctyTL + (ctyTR - ctyTL) * f;
            ctxBR = ctxBL + (ctxBR - ctxBL) * f;
            ctyBR = ctyBL + (ctyBR - ctyBL) * f;
        }
        if(this.height > 1) {
            float f = height / (float)this.height;
            ctxBL = ctxTL + (ctxBL - ctxTL) * f;
            ctyBL = ctyTL + (ctyBL - ctyTL) * f;
            ctxBR = ctxTR + (ctxBR - ctxTR) * f;
            ctyBR = ctyTR + (ctyBR - ctyTR) * f;
        }

        while(repeatCountY-- > 0) {
            int y1 = y + height;
            int x0 = x;
            for(int cx=repeatCountX ; cx-- > 0 ;) {
                int x1 = x0 + width;
                GL11.glTexCoord2f(ctxTL, ctyTL); GL11.glVertex2i(x0, y );
                GL11.glTexCoord2f(ctxBL, ctyBL); GL11.glVertex2i(x0, y1);
                GL11.glTexCoord2f(ctxBR, ctyBR); GL11.glVertex2i(x1, y1);
                GL11.glTexCoord2f(ctxTR, ctyTR); GL11.glVertex2i(x1, y );
                x0 = x1;
            }
            y = y1;
        }
    }

    private void drawQuad(int x, int y, int w, int h) {
        GL11.glTexCoord2f(txTL, tyTL); GL11.glVertex2i(x    , y    );
        GL11.glTexCoord2f(txBL, tyBL); GL11.glVertex2i(x    , y + h);
        GL11.glTexCoord2f(txBR, tyBR); GL11.glVertex2i(x + w, y + h);
        GL11.glTexCoord2f(txTR, tyTR); GL11.glVertex2i(x + w, y    );
    }

    private void createRepeatCache() {
        repeatCacheID = GL11.glGenLists(1);
        texture.renderer.rotatedTextureAreas.add(this);

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
        return new TextureAreaRotated(this, newTintColor);
    }
}
