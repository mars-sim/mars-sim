/*
 * Copyright (c) 2012, Gerrit Grunwald
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * The names of its contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package eu.hansolo.steelseries.tools;

import static java.lang.Math.round;


/**
 * @author Gerrit Grunwald <han.solo at muenster.de>
 */
public class Histogram {

    private final float MIN_VALUE; // the minimum value of the gauge dial
    private final float MINOR_TICKMARK_SPACING;  // the distance between two minor tickmarks
    private final double[] FREQUENCE;   // frequence[i] = # occurences of value i
    private double max;            // max frequency of any value
    private int n;

    // Create a new histogram.
    public Histogram(final double MIN_VALUE, final double MAX_VALUE, final double MINOR_TICKMARK_SPACING) {
        this.MIN_VALUE = (float) MIN_VALUE;
        this.MINOR_TICKMARK_SPACING = (float) MINOR_TICKMARK_SPACING;
        n = (int) ((MAX_VALUE - MIN_VALUE) / MINOR_TICKMARK_SPACING);

        FREQUENCE = new double[n];
    }

    // Add one occurrence of the value i.
    public void addDataPoint(final int INDEX) {
        FREQUENCE[INDEX > n - 1 ? n - 1 : INDEX]++;
        max = FREQUENCE[INDEX] > max ? FREQUENCE[INDEX] : max;
    }

    public void addDataPoint(final double VALUE) {
        int index = round((float) VALUE / MINOR_TICKMARK_SPACING) - round(MIN_VALUE / MINOR_TICKMARK_SPACING);
        addDataPoint(index);
    }

    public double[] getData() {
        return FREQUENCE.clone();
    }
}
