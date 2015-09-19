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
package de.matthiasmann.twl.renderer;

import de.matthiasmann.twl.Color;
import java.util.ArrayList;

/**
 * Info class used to construct a gradient image
 * 
 * @author Matthias Mann
 * @see Renderer#createGradient(de.matthiasmann.twl.renderer.Gradient) 
 */
public class Gradient {
    
    public enum Type {
        HORIZONTAL,
        VERTICAL
    }

    public enum Wrap {
        SCALE,
        CLAMP,
        REPEAT,
        MIRROR
    }
    
    private final Type type;
    private Wrap wrap;
    private final ArrayList<Stop> stops;

    public Gradient(Type type) {
        if(type == null) {
            throw new NullPointerException("type");
        }
        this.type = type;
        this.wrap = Wrap.SCALE;
        this.stops = new ArrayList<Stop>();
    }

    public Type getType() {
        return type;
    }

    public void setWrap(Wrap wrap) {
        if(wrap == null) {
            throw new NullPointerException("wrap");
        }
        this.wrap = wrap;
    }

    public Wrap getWrap() {
        return wrap;
    }

    public int getNumStops() {
        return stops.size();
    }

    public Stop getStop(int index) {
        return stops.get(index);
    }
    
    public Stop[] getStops() {
        return stops.toArray(new Stop[stops.size()]);
    }
    
    public void addStop(float pos, Color color) {
        if(color == null) {
            throw new NullPointerException("color");
        }
        int numStops = stops.size();
        if(numStops == 0) {
            if(!(pos >= 0)) {
                throw new IllegalArgumentException("first stop must be >= 0.0f");
            }
            if(pos > 0) {
                stops.add(new Stop(0.0f, color));
            }
        }
        if(numStops > 0 && !(pos > stops.get(numStops-1).pos)) {
            throw new IllegalArgumentException("pos must be monotone increasing");
        }
        stops.add(new Stop(pos, color));
    }
    
    public static class Stop {
        final float pos;
        final Color color;

        public Stop(float pos, Color color) {
            this.pos = pos;
            this.color = color;
        }

        public float getPos() {
            return pos;
        }

        public Color getColor() {
            return color;
        }
    }   
}
