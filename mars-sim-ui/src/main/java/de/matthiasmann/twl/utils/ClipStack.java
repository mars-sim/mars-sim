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
package de.matthiasmann.twl.utils;

import de.matthiasmann.twl.Rect;

/**
 * A stack for clipping regions
 * 
 * @author Matthias Mann
 */
public class ClipStack {
    
    private Entry[] clipRects;
    private int numClipRects;

    public ClipStack() {
        this.clipRects = new Entry[8];
    }
    
    /**
     * Pushes the intersection of the new clip region and the current clip region
     * onto the stack.
     * 
     * @param x the left start
     * @param y the top start
     * @param w the width
     * @param h the height
     * @see #pop() 
     */
    public void push(int x, int y, int w, int h) {
        Entry tos = push();
        tos.setXYWH(x, y, w, h);
        intersect(tos);
    }
    
    /**
     * Pushes the intersection of the new clip region and the current clip region
     * onto the stack.
     * 
     * @param rect the new clip region.
     * @throws NullPointerException if rect is null
     * @see #pop() 
     */
    public void push(Rect rect) {
        if(rect == null) {
            throw new NullPointerException("rect");
        }
        Entry tos = push();
        tos.set(rect);
        intersect(tos);
    }
    
    /**
     * Pushes an "disable clipping" onto the stack.
     * @see #pop() 
     */
    public void pushDisable() {
        Entry rect = push();
        rect.disabled = true;
    }
    
    /**
     * Removes the active clip regions from the stack.
     * @throws IllegalStateException when no clip regions are on the stack
     */
    public void pop() {
        if(numClipRects == 0) {
            underflow();
        }
        numClipRects--;
    }

    /**
     * Checks if the top of stack is an empty region (nothing will be rendered).
     * This can be used to speedup rendering by skipping all rendering when the
     * clip region is empty.
     * @return true if the TOS is an empty region
     */
    public boolean isClipEmpty() {
        Entry tos = clipRects[numClipRects-1];
        return tos.isEmpty() && !tos.disabled;
    }
    
    /**
     * Retrieves the active clip region from the top of the stack
     * @param rect the rect coordinates - may not be updated when clipping is disabled
     * @return true if clipping is active, false if clipping is disabled
     */
    public boolean getClipRect(Rect rect) {
        if(numClipRects == 0) {
            return false;
        }
        Entry tos = clipRects[numClipRects-1];
        rect.set(tos);
        return !tos.disabled;
    }
    
    /**
     * Returns the current number of entries in the clip stack
     * @return the number of entries
     */
    public int getStackSize() {
        return numClipRects;
    }
    
    /**
     * Clears the clip stack
     */
    public void clearStack() {
        numClipRects = 0;
    }

    protected Entry push() {
        if(numClipRects == clipRects.length) {
            grow();
        }
        Entry rect;
        if((rect = clipRects[numClipRects]) == null) {
            rect = new Entry();
            clipRects[numClipRects] = rect;
        }
        rect.disabled = false;
        numClipRects++;
        return rect;
    }

    protected void intersect(Rect tos) {
        if(numClipRects > 1) {
            Entry prev = clipRects[numClipRects-2];
            if(!prev.disabled) {
                tos.intersect(prev);
            }
        }
    }

    private void grow() {
        Entry[] newRects = new Entry[numClipRects*2];
        System.arraycopy(clipRects, 0, newRects, 0, numClipRects);
        clipRects = newRects;
    }

    private void underflow() {
        throw new IllegalStateException("empty");
    }
    
    protected static class Entry extends Rect {
        boolean disabled;
    }
}
