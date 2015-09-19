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
package de.matthiasmann.twl;

/**
 * A simple FPS counter.
 * 
 * <p>Measures the time required to render a specified number of
 * frames (default 100) using System.nanoTime and displays the
 * frame rate:</p><pre>{@code 1e9 * framesToCount / elapsedNanoseconds }</pre>
 * 
 * <p>This widget does not generate garbage while measuring and
 * displaying the frame rate.</p>
 * 
 * @see System#nanoTime()
 * @author Matthias Mann
 */
public class FPSCounter extends Label {

    private long startTime;
    private int frames;
    private int framesToCount = 100;
    
    private final StringBuilder fmtBuffer;
    private final int decimalPoint;
    private final long scale;

    /**
     * Creates the FPS counter with the given number of integer and decimal digits
     *
     * @param numIntegerDigits number of integer digits - must be >= 2
     * @param numDecimalDigits number of decimal digits - must be >= 0
     */
    public FPSCounter(int numIntegerDigits, int numDecimalDigits) {
        if(numIntegerDigits < 2) {
            throw new IllegalArgumentException("numIntegerDigits must be >= 2");
        }
        if(numDecimalDigits < 0) {
            throw new IllegalArgumentException("numDecimalDigits must be >= 0");
        }
        decimalPoint = numIntegerDigits + 1;
        startTime = System.nanoTime();
        fmtBuffer = new StringBuilder();
        fmtBuffer.setLength(numIntegerDigits + numDecimalDigits + Integer.signum(numDecimalDigits));

        // compute the scale based on the number of decimal places
        long tmp = (long)1e9;
        for(int i=0 ; i<numDecimalDigits ; i++) {
            tmp *= 10;
        }
        this.scale = tmp;
        
        // set default text so that initial size is computed correctly
        updateText(0);
    }

    /**
     * Creates the FPS counter with 3 integer digits and 2 decimal digits
     * @see #FPSCounter(int, int)
     */
    public FPSCounter() {
        this(3, 2);
    }
    
    public int getFramesToCount() {
        return framesToCount;
    }

    /**
     * Specified how many frames to count to compute the FPS. Larger values
     * result in a more accurate result and slower update.
     *
     * @param framesToCount the number of frames to count
     */
    public void setFramesToCount(int framesToCount) {
        if(framesToCount <= 0) {
            throw new IllegalArgumentException("framesToCount < 1");
        }
        this.framesToCount = framesToCount;
    }

    @Override
    protected void paintWidget(GUI gui) {
        if(++frames >= framesToCount) {
            updateFPS();
        }
        super.paintWidget(gui);
    }
    
    private void updateFPS() {
        long curTime = System.nanoTime();
        long elapsed = curTime - startTime;
        startTime = curTime;
        
        updateText((int)((frames * scale + (elapsed >> 1)) / elapsed));
        frames = 0;
    }

    private void updateText(int value) {
        StringBuilder buf = fmtBuffer;
        int pos = buf.length();
        do {
            buf.setCharAt(--pos, (char)('0' + (value % 10)));
            value /= 10;
            if(decimalPoint == pos) {
                buf.setCharAt(--pos, '.');
            }
        } while(pos > 0);
        if(value > 0) {
            // when the frame rate is too high, then we display "999.99"
            pos = buf.length();
            do {
                buf.setCharAt(--pos, '9');
                if(decimalPoint == pos) {
                    --pos;
                }
            } while(pos > 0);
        }
        setCharSequence(buf);
    }

}
