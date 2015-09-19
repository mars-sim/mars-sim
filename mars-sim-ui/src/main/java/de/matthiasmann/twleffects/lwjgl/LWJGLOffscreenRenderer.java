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

import de.matthiasmann.twl.Rect;
import de.matthiasmann.twl.Widget;
import org.lwjgl.opengl.GL11;
import de.matthiasmann.twl.renderer.OffscreenRenderer;
import de.matthiasmann.twl.renderer.OffscreenSurface;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;

/**
 *
 * @author Matthias Mann
 */
public class LWJGLOffscreenRenderer implements OffscreenRenderer {
    
    private final LWJGLEffectsRenderer renderer;
    
    LWJGLOffscreenSurface activeSurface;
    int viewportStartX;
    int viewportStartY;
    int viewportHeight;
    boolean hasScissor;
    
    LWJGLOffscreenRenderer(LWJGLEffectsRenderer renderer) {
        this.renderer = renderer;
    }

    public OffscreenSurface startOffscreenRendering(Widget widget, OffscreenSurface oldSurface, int x, int y, int width, int height) {
        if(width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width or height <= 0");
        }
        if(activeSurface != null) {
            throw new IllegalStateException("offscreen rendering already active");
        }
        
        LWJGLOffscreenSurface surface = (LWJGLOffscreenSurface)oldSurface;
        if(surface == null) {
            surface = new LWJGLOffscreenSurface(renderer);
        }
        surface.checkNotBound();
        if(!surface.allocate(width, height)) {
            surface.destroy();
            return null;
        }
        
        activeSurface = surface;
        viewportStartX = x;
        viewportStartY = y;
        viewportHeight = height;
        
        renderer.startOffscreenRendering();
        
        glPushAttrib(GL_VIEWPORT_BIT | GL_TRANSFORM_BIT | GL_COLOR_BUFFER_BIT | GL_SCISSOR_BIT);
        disableClipRect();
        glViewport(0, 0, width, height);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(x, x + width, y + height, y, -1.0, 1.0);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glBlendFuncSeparate(
                GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA,
                GL_ZERO, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        
        return surface;
    }

    public void endOffscreenRendering() {
        if(activeSurface == null) {
            throw new IllegalStateException("no offscreen rendering active");
        }
        
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glPopAttrib();
        
        activeSurface.unbindFBO();
        activeSurface = null;
        
        renderer.endOffscreenRendering();
    }
    
    void disableClipRect() {
        glDisable(GL_SCISSOR_TEST);
        hasScissor = false;
    }
    
    void setClipRect(Rect rect) {
        int x0 = Math.max(0, rect.getX() - viewportStartX);
        int y0 = Math.max(0, rect.getY() - viewportStartY);
        int x1 = Math.max(0, rect.getRight() - viewportStartX);
        int y1 = Math.max(0, rect.getBottom() - viewportStartY);
        GL11.glScissor(x0, viewportHeight - y1, x1-x0, y1-y0);
        if(!hasScissor) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            hasScissor = true;
        }
    }
}
