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
package de.matthiasmann.twleffects.lwjgl;

import de.matthiasmann.twl.Rect;
import de.matthiasmann.twl.renderer.OffscreenRenderer;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;

/**
 *
 * @author Matthias Mann
 */
public class LWJGLEffectsRenderer extends LWJGLRenderer {

    private final LWJGLOffscreenRenderer offscreenRenderer;
    
    public LWJGLEffectsRenderer() throws LWJGLException {
        super();
        
        ContextCapabilities caps = GLContext.getCapabilities();
        if(caps.GL_EXT_framebuffer_object && caps.OpenGL14) {
            offscreenRenderer = new LWJGLOffscreenRenderer(this);
        } else {
            offscreenRenderer = null;
        }
    }

    @Override
    public OffscreenRenderer getOffscreenRenderer() {
        return offscreenRenderer;
    }

    @Override
    protected void setClipRect() {
        if(offscreenRenderer != null && offscreenRenderer.activeSurface != null) {
            setClipRectOffscreen();
        } else {
            super.setClipRect();
        }
    }
    
    protected void setClipRectOffscreen() {
        final Rect rect = clipRectTemp;
        if(clipStack.getClipRect(rect)) {
            offscreenRenderer.setClipRect(rect);
        } else {
            offscreenRenderer.disableClipRect();
        }
    }
    
    protected void startOffscreenRendering() {
        pushGlobalTintColorReset();
        clipStack.pushDisable();
    }
    
    protected void endOffscreenRendering() {
        popGlobalTintColor();
        clipStack.pop();
    }
}
