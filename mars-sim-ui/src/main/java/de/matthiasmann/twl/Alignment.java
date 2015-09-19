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
package de.matthiasmann.twl;

/**
 *
 * @author Matthias Mann
 */
public enum Alignment {

    LEFT(HAlignment.LEFT,0,1),
    CENTER(HAlignment.CENTER,1,1),
    RIGHT(HAlignment.RIGHT,2,1),
    TOP(HAlignment.CENTER,1,0),
    BOTTOM(HAlignment.CENTER,1,2),
    TOPLEFT(HAlignment.LEFT,0,0),
    TOPRIGHT(HAlignment.RIGHT,2,0),
    BOTTOMLEFT(HAlignment.LEFT,0,2),
    BOTTOMRIGHT(HAlignment.RIGHT,2,2),
    FILL(HAlignment.CENTER,1,1);
    
    final HAlignment fontHAlignment;
    final byte hpos;
    final byte vpos;
    
    private Alignment(HAlignment fontHAlignment, int hpos, int vpos) {
        this.fontHAlignment = fontHAlignment;
        this.hpos = (byte)hpos;
        this.vpos = (byte)vpos;
    }

    public HAlignment getFontHAlignment() {
        return fontHAlignment;
    }

    /**
     * Returns the horizontal position for this alignment.
     * @return 0 for left, 1 for center and 2 for right
     */
    public int getHPosition() {
        return hpos;
    }

    /**
     * Returns the vertical position for this alignment.
     * @return 0 for top, 1 for center and 2 for bottom
     */
    public int getVPosition() {
        return vpos;
    }
    

    public int computePositionX(int containerWidth, int objectWidth) {
        return Math.max(0, containerWidth - objectWidth) * hpos / 2;
    }

    public int computePositionY(int containerHeight, int objectHeight) {
        return Math.max(0, containerHeight - objectHeight) * vpos / 2;
    }
}
