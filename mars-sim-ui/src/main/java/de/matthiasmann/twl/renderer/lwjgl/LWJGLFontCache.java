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
package de.matthiasmann.twl.renderer.lwjgl;

import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.FontCache;
import org.lwjgl.opengl.GL11;

/**
 * A font render cache - uses display lists
 * 
 * @author Matthias Mann
 */
public class LWJGLFontCache implements FontCache {

    private final LWJGLRenderer renderer;
    private final LWJGLFont font;
    private int id;
    private int width;
    private int height;
    private int[] multiLineInfo;
    private int numLines;

    LWJGLFontCache(LWJGLRenderer renderer, LWJGLFont font) {
        this.renderer = renderer;
        this.font = font;
        this.id = GL11.glGenLists(1);
    }
    
    public void draw(AnimationState as, int x, int y) {
        if(id != 0) {
            LWJGLFont.FontState fontState = font.evalFontState(as);
            renderer.tintStack.setColor(fontState.color);
            GL11.glPushMatrix();
            GL11.glTranslatef(x+fontState.offsetX, y+fontState.offsetY, 0f);
            GL11.glCallList(id);
            if(fontState.style != 0) {
                if(numLines > 0) {
                    font.drawLines(fontState, 0, 0, multiLineInfo, numLines);
                } else {
                    font.drawLine(fontState, 0, 0, width);
                }
            }
            GL11.glPopMatrix();
        }
    }

    public void destroy() {
        if(id != 0) {
            GL11.glDeleteLists(id, 1);
            id = 0;
        }
    }

    boolean startCompile() {
        if(id != 0) {
            GL11.glNewList(id, GL11.GL_COMPILE);
            this.numLines = 0;
            return true;
        }
        return false;
    }
    
    void endCompile(int width, int height) {
        GL11.glEndList();
        this.width = width;
        this.height = height;
    }

    int[] getMultiLineInfo(int numLines) {
        if(multiLineInfo == null || multiLineInfo.length < numLines) {
            multiLineInfo = new int[numLines];
        }
        this.numLines = numLines;
        return multiLineInfo;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
