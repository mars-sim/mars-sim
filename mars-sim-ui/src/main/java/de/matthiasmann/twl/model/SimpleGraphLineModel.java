/*
 * Copyright (c) 2008, Matthias Mann
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
package de.matthiasmann.twl.model;

/**
 * A simple graph line model which allows to shift points from right to left.
 * 
 * @author Matthias Mann
 */
public class SimpleGraphLineModel implements GraphLineModel {

    private String visualStyleName;
    private float minValue = 0;
    private float maxValue = 100;
    private float[] data;

    public SimpleGraphLineModel(String style, int size, float minValue, float maxValue) {
        setVisualStyleName(style);
        this.data = new float[size];
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public String getVisualStyleName() {
        return visualStyleName;
    }

    public void setVisualStyleName(String visualStyleName) {
        if(visualStyleName.length() < 1) {
            throw new IllegalArgumentException("Invalid style name");
        }
        this.visualStyleName = visualStyleName;
    }

    public int getNumPoints() {
        return data.length;
    }

    public float getPoint(int idx) {
        return data[idx];
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void addPoint(float value) {
        System.arraycopy(data, 1, data, 0, data.length - 1);
        data[data.length-1] = value;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }

    public void setMinValue(float minValue) {
        this.minValue = minValue;
    }

    public void setNumPoints(int numPoints) {
        float[] newData = new float[numPoints];
        int overlap = Math.min(data.length, numPoints);
        System.arraycopy(
                data, data.length - overlap,
                newData, numPoints - overlap, overlap);
        this.data = newData;
    }
}
