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

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.Gradient;
import de.matthiasmann.twl.renderer.Gradient.Stop;
import de.matthiasmann.twl.renderer.Gradient.Type;
import de.matthiasmann.twl.renderer.Gradient.Wrap;
import de.matthiasmann.twl.renderer.Image;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Matthias Mann
 */
public class GradientImage implements Image {
    
    private final LWJGLRenderer renderer;
    private final Type type;
    private final Wrap wrap;
    private final Stop[] stops;
    private final Color tint;
    private final float endPos;

    GradientImage(GradientImage src, Color tint) {
        this.renderer = src.renderer;
        this.type = src.type;
        this.wrap = src.wrap;
        this.stops = src.stops;
        this.endPos = src.endPos;
        this.tint = tint;
    }

    public GradientImage(LWJGLRenderer renderer, Gradient gradient) {
        if(gradient == null) {
            throw new NullPointerException("gradient");
        }
        if(gradient.getNumStops() < 1) {
            throw new IllegalArgumentException("Need at least 1 stop for a gradient");
        }
        
        this.renderer = renderer;
        this.type = gradient.getType();
        this.tint = Color.WHITE;
        if(gradient.getNumStops() == 1) {
            Color color = gradient.getStop(0).getColor();
            wrap = Wrap.SCALE;
            stops = new Stop[] {
                new Stop(0.0f, color),
                new Stop(1.0f, color)
            };
            endPos = 1.0f;
        } else if(gradient.getWrap() == Wrap.MIRROR) {
            int numStops = gradient.getNumStops();
            wrap = Wrap.REPEAT;
            stops = new Stop[numStops*2-1];
            for(int i=0 ; i<numStops ; i++) {
                stops[i] = gradient.getStop(i);
            }
            endPos = stops[numStops-1].getPos() * 2;
            for(int i=numStops,j=numStops-2 ; j>=0 ; i++,j--) {
                stops[i] = new Stop(endPos - stops[j].getPos(), stops[j].getColor());
            }
        } else {
            wrap = gradient.getWrap();
            stops = gradient.getStops();
            endPos = stops[stops.length-1].getPos();
        }
    }
    

    public Image createTintedVersion(Color color) {
        return new GradientImage(this, tint.multiply(color));
    }

    private boolean isHorz() {
        return type == Type.HORIZONTAL;
    }
    
    private int getLastPos() {
        return Math.round(stops[stops.length-1].getPos());
    }
    
    public int getHeight() {
        return isHorz() ? 1 : getLastPos();
    }

    public int getWidth() {
        return isHorz() ? getLastPos() : 1;
    }

    public void draw(AnimationState as, int x, int y) {
        if(isHorz()) {
            drawHorz(x, y, getLastPos(), 1);
        } else {
            drawVert(x, y, 1, getLastPos());
        }
    }

    public void draw(AnimationState as, int x, int y, int width, int height) {
        if(isHorz()) {
            drawHorz(x, y, width, height);
        } else {
            drawVert(x, y, width, height);
        }
    }
    
    private void drawHorz(int x, int y, int width, int height) {
        if(width <= 0 || height <= 0) {
            return;
        }
        TintStack tintStack = renderer.tintStack.push(tint);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBegin(GL11.GL_QUAD_STRIP);
        if(wrap == Wrap.SCALE) {
            for(Stop stop : stops) {
                tintStack.setColor(stop.getColor());
                float pos = stop.getPos() * width / endPos;
                GL11.glVertex2f(x + pos, y);
                GL11.glVertex2f(x + pos, y + height);
            }
        } else {
            float lastPos = 0;
            float offset = 0;
            Color lastColor = stops[0].getColor();
            outer: do{
                for(Stop stop : stops) {
                    float pos = stop.getPos() + offset;
                    Color color = stop.getColor();
                    if(pos >= width) {
                        float t = (width - lastPos) / (pos - lastPos);
                        setColor(tintStack, lastColor, color, t);
                        break outer;
                    }
                    tintStack.setColor(color);
                    GL11.glVertex2f(x + pos, y);
                    GL11.glVertex2f(x + pos, y + height);
                    lastPos = pos;
                    lastColor = color;
                }
                offset += endPos;
            }while(wrap == Wrap.REPEAT);
            GL11.glVertex2f(x + width, y);
            GL11.glVertex2f(x + width, y + height);
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
    
    private void drawVert(int x, int y, int width, int height) {
        if(width <= 0 || height <= 0) {
            return;
        }
        TintStack tintStack = renderer.tintStack.push(tint);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBegin(GL11.GL_QUAD_STRIP);
        if(wrap == Wrap.SCALE) {
            for(Stop stop : stops) {
                tintStack.setColor(stop.getColor());
                float pos = stop.getPos() * height / endPos;
                GL11.glVertex2f(x        , y + pos);
                GL11.glVertex2f(x + width, y + pos);
            }
        } else {
            float lastPos = 0;
            float offset = 0;
            Color lastColor = stops[0].getColor();
            outer: do{
                for(Stop stop : stops) {
                    float pos = stop.getPos() + offset;
                    Color color = stop.getColor();
                    if(pos >= height) {
                        float t = (height - lastPos) / (pos - lastPos);
                        setColor(tintStack, lastColor, color, t);
                        break outer;
                    }
                    tintStack.setColor(color);
                    GL11.glVertex2f(x        , y + pos);
                    GL11.glVertex2f(x + width, y + pos);
                    lastPos = pos;
                    lastColor = color;
                }
                offset += endPos;
            }while(wrap == Wrap.REPEAT);
            GL11.glVertex2f(x        , y + height);
            GL11.glVertex2f(x + width, y + height);
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
    
    private static void setColor(TintStack tintStack, Color a, Color b, float t) {
        tintStack.setColor(
                mix(a.getRed(),   b.getRed(),   t),
                mix(a.getGreen(), b.getGreen(), t),
                mix(a.getBlue(),  b.getBlue(),  t),
                mix(a.getAlpha(), b.getAlpha(), t));
    }
    
    private static float mix(int a, int b, float t) {
        return a + (b-a) * t;
    }
    
}
