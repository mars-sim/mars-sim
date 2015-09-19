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
package de.matthiasmann.twleffects;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.OffscreenSurface;
import de.matthiasmann.twl.utils.TintAnimator;
import de.matthiasmann.twl.utils.TintAnimator.TimeSource;

/**
 *
 * @author Matthias Mann
 */
public class MinimizeEffect implements Widget.RenderOffscreen {

    private final TimeSource timeSource;
    private float animationDuration;
    private int numVerticesX;
    private int numVerticesY;
    private float[] xy;

    public MinimizeEffect(TimeSource timeSource) {
        this.timeSource = timeSource;
        this.animationDuration = 1000;
        
        setNumVertices(4, 4);
        timeSource.resetTime();
    }

    public MinimizeEffect(Widget widget) {
        this(new TintAnimator.GUITimeSource(widget));
    }

    public float getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(float animationDuration) {
        if(animationDuration <= 0.0f) {
            throw new IllegalArgumentException("animationDuration");
        }
        this.animationDuration = animationDuration;
    }
    
    public final void setNumVertices(int x, int y) {
        if(x < 2 || y < 2) {
            throw new IllegalArgumentException("not enough vertices");
        }
        this.numVerticesX = x;
        this.numVerticesY = y;
        this.xy = new float[x*y*2];
    }
    
    public void offscreenRenderingFailed(Widget widget) {
    }

    public void paintOffscreenSurface(GUI gui, Widget widget, OffscreenSurface surface) {
        float time = timeSource.getTime() / animationDuration;
        if(time >= 1.0f) {
            widget.setRenderOffscreen(null);
            surface.destroy();
            return;
        }
        
        int widgetY = widget.getY() - widget.getOffscreenExtraTop();
        int widgetX = widget.getX() - widget.getOffscreenExtraTop();
        int width = surface.getWidth();
        int height = surface.getHeight();
        int centerX = gui.getWidth() / 2;
        
        for(int r=0,idx=0 ; r<numVerticesY ; r++) {
            final float yfrac = r / (float)(numVerticesY-1);
            float t = time * (1 + yfrac);
            float tt1 = (1+t) * (1+t);
            float y = widgetY + height*yfrac + gui.getHeight() * t;
            
            for(int c=0 ; c<numVerticesX ; c++,idx+=2) {
                float x = centerX + (widgetX - centerX + width * c / (float)(numVerticesX-1)) / tt1;
                xy[idx+0] = x;
                xy[idx+1] = y;
            }
        }
        
        ((GridImage)surface).draw(null, xy, numVerticesX, numVerticesY);
    }

    public int[] getEffectExtraArea(Widget widget) {
        return null;
    }

    public boolean needPainting(GUI gui, Widget widget, OffscreenSurface surface) {
        return true;
    }
    
}
