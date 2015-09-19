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
package de.matthiasmann.twleffects.lwjgl;

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.OffscreenSurface;
import de.matthiasmann.twleffects.GridImage;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

/**
 *
 * @author Matthias Mann
 */
public class LWJGLOffscreenSurface implements OffscreenSurface, GridImage {
    
    private final LWJGLEffectsRenderer renderer;
    
    private int fboID;
    private int textureID;
    private int textureWidth;
    private int textureHeight;
    private int usedWidth;
    private int usedHeight;
    private boolean bound;
    private int glMinFilter;
    private int glMagFilter;

    LWJGLOffscreenSurface(LWJGLEffectsRenderer renderer) {
        this.renderer = renderer;
        this.glMinFilter = GL_LINEAR;
        this.glMagFilter = GL_LINEAR;
    }

    boolean allocate(int width, int height) {
        if(fboID == 0) {
            fboID = glGenFramebuffersEXT();
        }
        bindFBO();
        if(width > textureWidth || height > textureHeight) {
            if(textureID == 0) {
                textureID = glGenTextures();
            }
            textureWidth = nextPowerOf2(width);
            textureHeight = nextPowerOf2(height);
            glBindTexture(GL_TEXTURE_2D, textureID);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, glMinFilter);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, glMagFilter);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);  
            glTexImage2D(GL_TEXTURE_2D, 0,
                    GL_RGBA8, textureWidth, textureHeight, 0, GL_RGBA,
                    GL_UNSIGNED_BYTE, (ByteBuffer)null);
            glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT,
                    GL_COLOR_ATTACHMENT0_EXT,
                    GL_TEXTURE_2D, textureID, 0);
            int status = glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT);
            if(status != GL_FRAMEBUFFER_COMPLETE_EXT) {
                System.out.println(status);
            }
        }
        usedWidth = width;
        usedHeight = height;
        return true;
    }
    
    private static int nextPowerOf2(int i) {
        i--;
        i |= (i >>  1);
        i |= (i >>  2);
        i |= (i >>  4);
        i |= (i >>  8);
        i |= (i >> 16);
        return i+1;
    }
    
    void bindFBO() {
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboID);
        bound = true;
    }

    void unbindFBO() {
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
        bound = false;
    }

    void checkNotBound() {
        if(bound) {
            throw new IllegalStateException("offscreen rendering active");
        }
    }
    
    public void destroy() {
        checkNotBound();
        if(fboID != 0) {
            glDeleteFramebuffersEXT(fboID);
            fboID = 0;
        }
        if(textureID != 0) {
            glDeleteTextures(textureID);
            textureID = 0;
        }
        textureWidth = 0;
        textureHeight = 0;
        usedWidth = 0;
        usedHeight = 0;
    }

    public Image createTintedVersion(Color color) {
        if(color.equals(Color.WHITE)) {
            return this;
        } else {
            return createTinted(color);
        }
    }

    public void draw(AnimationState as, int x, int y) {
        draw(as, x, y, usedWidth, usedHeight);
    }

    public void draw(AnimationState as, int x, int y, int w, int h) {
        draw(Color.WHITE, x, y, w, h);
    }
    
    public void draw(AnimationState as, float[] xy, int numVerticesX, int numVerticesY) {
        draw(Color.WHITE, xy, numVerticesX, numVerticesY);
    }
    
    void draw(Color color, int x, int y, int w, int h) {
        if(startRendering(color)) {
            float tx1 = usedWidth / (float)textureWidth;
            float ty0 = 0.0f;
            float ty1 = usedHeight / (float)textureHeight;

            glTexCoord2f(  0, ty1); glVertex2i(x    , y    );
            glTexCoord2f(  0, ty0); glVertex2i(x    , y + h);
            glTexCoord2f(tx1, ty0); glVertex2i(x + w, y + h);
            glTexCoord2f(tx1, ty1); glVertex2i(x + w, y    );
            endRendering();
        }
    }
    
    void draw(Color color, float[] xy, int numVerticesX, int numVerticesY) {
        if(xy == null) {
            throw new NullPointerException("xy");
        }
        if(numVerticesX <= 1 || numVerticesY <= 1) {
            throw new IllegalArgumentException("numVerticesX/Y");
        }
        if(numVerticesX*numVerticesY*2 > xy.length) {
            throw new IllegalArgumentException("not enough coordinates");
        }
        if(startRendering(color)) {
            float ty0 = usedHeight / (float)textureHeight;
            for(int r=1 ; r<numVerticesY ; r++) {
                float ty1 = ty0;
                ty0 = (usedHeight * (numVerticesY-1-r)) / (float)(textureHeight * (numVerticesY-1));
                float tx1 = 0.0f;
                int idx0 = (r-1) * numVerticesX*2;
                int idx1 = idx0 + numVerticesX*2;
                for(int c=1 ; c<numVerticesX ; c++,idx0+=2,idx1+=2) {
                    float tx0 = tx1;
                    tx1 = (usedWidth * c) / (float)(textureWidth * (numVerticesX-1));
                    glTexCoord2f(tx0, ty1); glVertex2f(xy[idx0  ], xy[idx0+1]);
                    glTexCoord2f(tx0, ty0); glVertex2f(xy[idx1  ], xy[idx1+1]);
                    glTexCoord2f(tx1, ty0); glVertex2f(xy[idx1+2], xy[idx1+3]);
                    glTexCoord2f(tx1, ty1); glVertex2f(xy[idx0+2], xy[idx0+3]);
                }
            }
            endRendering();
        }
    }
    
    private boolean startRendering(Color color) {
        checkNotBound();
        if(textureID == 0) {
            return false;
        }
        renderer.setColor(color);
        
        glBindTexture(GL_TEXTURE_2D, textureID);
        glBlendFunc(GL_ONE, GL_SRC_ALPHA);
        glBegin(GL_QUADS);
        return true;
    }
    
    private void endRendering() {
        glEnd();
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public int getWidth() {
        return usedWidth;
    }

    public int getHeight() {
        return usedHeight;
    }
    
    public void setGLFilter(int minFilter, int magFilter) {
        if(glMinFilter != minFilter || glMagFilter != magFilter) {
            this.glMinFilter = minFilter;
            this.glMagFilter = magFilter;
            if(textureID != 0) {
                glBindTexture(GL_TEXTURE_2D, textureID);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);
            }
        }
    }
    
    Image createTinted(final Color color) {
        return new GridImage() {
            public Image createTintedVersion(Color newColor) {
                if(newColor.equals(Color.WHITE)) {
                    return this;
                } else {
                    return createTinted(color.multiply(newColor));
                }
            }
            public void draw(AnimationState as, int x, int y) {
                LWJGLOffscreenSurface.this.draw(color, x, y, getWidth(), getHeight());
            }
            public void draw(AnimationState as, int x, int y, int width, int height) {
                LWJGLOffscreenSurface.this.draw(color, x, y, width, height);
            }
            public void draw(AnimationState as, float[] xy, int numVerticesX, int numVerticesY) {
                LWJGLOffscreenSurface.this.draw(color, xy, numVerticesX, numVerticesY);
            }
            public int getWidth() {
                return LWJGLOffscreenSurface.this.getWidth();
            }
            public int getHeight() {
                return LWJGLOffscreenSurface.this.getHeight();
            }
        };
    }
}
