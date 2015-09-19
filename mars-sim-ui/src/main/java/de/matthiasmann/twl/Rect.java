/*
 * Copyright (c) 2008-2010, Matthias Mann
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
package de.matthiasmann.twl;

/**
 * An mutable rectangle class.
 *
 * @author Matthias Mann
 */
public class Rect {

    private int x0;
    private int y0;
    private int x1;
    private int y1;

    public Rect() {
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public Rect(int x, int y, int w, int h) {
        setXYWH(x, y, w, h);
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public Rect(Rect src) {
        set(src.getX(), src.getY(), src.getRight(), src.getBottom());
    }
    
    public void setXYWH(int x, int y, int w, int h) {
        this.x0 = x;
        this.y0 = y;
        this.x1 = x + Math.max(0, w);
        this.y1 = y + Math.max(0, h);
    }
    
    public void set(int x0, int y0, int x1, int y1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    public void set(Rect src) {
        this.x0 = src.x0;
        this.y0 = src.y0;
        this.x1 = src.x1;
        this.y1 = src.y1;
    }
    
    /**
     * Computes the intersection of this rectangle with the other rectangle.
     * If they don't overlapp then this rect will be set to zero width and height.
     *
     * @param other The other rectangle to compute the intersection with
     */
    public void intersect(Rect other) {
        x0 = Math.max(x0, other.x0);
        y0 = Math.max(y0, other.y0);
        x1 = Math.min(x1, other.x1);
        y1 = Math.min(y1, other.y1);
        if(x1 < x0 || y1 < y0) {
            x1 = x0;
            y1 = y0;
        }
    }

    public boolean isInside(int x, int y) {
        return (x >= x0) && (y >= y0) && (x < x1) && (y < y1);
    }
    
    public int getX() {
        return x0;
    }

    public int getY() {
        return y0;
    }

    public int getRight() {
        return x1;
    }

    public int getBottom() {
        return y1;
    }
    
    public int getWidth() {
        return x1 - x0;
    }
    
    public int getHeight() {
        return y1 - y0;
    }
    
    public int getCenterX() {
        return (x0 + x1) / 2;
    }
    
    public int getCenterY() {
        return (y0 + y1) / 2;
    }

    public Dimension getSize() {
        return new Dimension(getWidth(), getHeight());
    }

    public boolean isEmpty() {
        return x1 <= x0 || y1 <= y0;
    }

    @Override
    public String toString() {
        return "Rect[x0=" + x0 + ", y0=" + y0 + ", x1=" + x1 + ", y1=" + y1 + ']';
    }

}
