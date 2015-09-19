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

import de.matthiasmann.twl.renderer.AttributedStringFontCache;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLFont.FontState;
import java.nio.FloatBuffer;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Matthias Mann
 */
class LWJGLAttributedStringFontCache extends VertexArray implements AttributedStringFontCache {

    final LWJGLRenderer renderer;
    final BitmapFont font;
    int width;
    int height;
    private Run[] runs;
    private int numRuns;

    LWJGLAttributedStringFontCache(LWJGLRenderer renderer, BitmapFont font) {
        this.renderer = renderer;
        this.font = font;
        this.runs = new Run[8];
    }
    
    @Override
    public FloatBuffer allocate(int maxGlyphs) {
        numRuns = 0;
        return super.allocate(maxGlyphs);
    }
    
    Run addRun() {
        if(runs.length == numRuns) {
            grow();
        }
        Run run = runs[numRuns];
        if(run == null) {
            run = new Run();
            runs[numRuns] = run;
        }
        numRuns++;
        return run;
    }
    
    private void grow() {
        Run[] newRuns = new Run[numRuns * 2];
        System.arraycopy(runs, 0, newRuns, 0, numRuns);
        runs = newRuns;
    }
    
    public void destroy() {
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public void draw(int x, int y) {
        if(font.bind()) {
            bind();
            GL11.glPushMatrix();
            GL11.glTranslatef(x, y, 0f);
            final TintStack tintStack = renderer.tintStack;
            
            try {
                int idx = 0;
                for(int i=0 ; i<numRuns ; i++) {
                    final Run run = runs[i];
                    final FontState state = run.state;
                    final int numVertices = run.numVertices;
                    
                    tintStack.setColor(state.color);
                    
                    if(numVertices > 0) {
                        drawVertices(idx, numVertices);
                        idx += numVertices;
                    }
                    
                    if(state.style != 0) {
                        drawLines(run);
                    }
                }
            } finally {
                GL11.glPopMatrix();
                unbind();
            }
        }
    }
    
    private void drawLines(Run run) {
        final FontState state = run.state;
        
        if((state.style & LWJGLFont.STYLE_UNDERLINE) != 0) {
            font.drawLine(
                    run.x,
                    run.y + font.getBaseLine() + state.underlineOffset,
                    run.xend);
        }
        if((state.style & LWJGLFont.STYLE_LINETHROUGH) != 0) {
            font.drawLine(
                    run.x,
                    run.y + font.getLineHeight()/2,
                    run.xend);
        }
    }
    
    static class Run {
        LWJGLFont.FontState state;
        int numVertices;
        int x;
        int xend;
        int y;
    }
}
