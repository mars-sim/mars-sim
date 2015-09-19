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
package de.matthiasmann.twl.model;

/**
 * A simple float data model.
 *
 * Out of range values are limited to minValue ... maxValue.
 * If the value is set to NaN then it is converted to minValue.
 *
 * @author Matthias Mann
 */
public class SimpleFloatModel extends AbstractFloatModel {

    private final float minValue;
    private final float maxValue;
    private float value;

    public SimpleFloatModel(float minValue, float maxValue, float value) {
        if(Float.isNaN(minValue)) {
            throw new IllegalArgumentException("minValue is NaN");
        }
        if(Float.isNaN(maxValue)) {
            throw new IllegalArgumentException("maxValue is NaN");
        }
        if(minValue > maxValue) {
            throw new IllegalArgumentException("minValue > maxValue");
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.value = limit(value);
    }

    public float getMaxValue() {
        return maxValue;
    }

    public float getMinValue() {
        return minValue;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        value = limit(value);
        if(this.value != value) {
            this.value = value;
            doCallback();
        }
    }

    protected float limit(float value) {
        if(Float.isNaN(value)) {
            return minValue;
        }
        return Math.max(minValue, Math.min(maxValue, value));
    }

}
