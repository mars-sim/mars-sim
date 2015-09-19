/*
 * Copyright (c) 2008-2009, Matthias Mann
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
 * A boolean model mapped to a single bit on an IntegerModel
 *
 * @author Matthias Mann
 */
public class BitfieldBooleanModel extends HasCallback implements BooleanModel {

    private final IntegerModel bitfield;
    private final int bitmask;

    public BitfieldBooleanModel(IntegerModel bitfield, int bit) {
        if(bitfield == null) {
            throw new NullPointerException("bitfield");
        }
        if(bit < 0 || bit > 30) {
            throw new IllegalArgumentException("invalid bit index");
        }
        if(bitfield.getMinValue() != 0) {
            throw new IllegalArgumentException("bitfield.getMinValue() != 0");
        }
        int bitfieldMax = bitfield.getMaxValue();
        if((bitfieldMax & (bitfieldMax+1)) != 0) {
            throw new IllegalArgumentException("bitfield.getmaxValue() must eb 2^x");
        }
        if(bitfieldMax < (1 << bit)) {
            throw new IllegalArgumentException("bit index outside of bitfield range");
        }
        this.bitfield = bitfield;
        this.bitmask = 1 << bit;
        bitfield.addCallback(new CB());
    }

    public boolean getValue() {
        return ((bitfield.getValue() & bitmask) != 0);
    }
    
    public void setValue(boolean value) {
        int oldBFValue = bitfield.getValue();
        int newBFValue = value ? (oldBFValue | bitmask) : (oldBFValue & ~bitmask);
        if(oldBFValue != newBFValue) {
            bitfield.setValue(newBFValue);
            // bitfield's callback will call our callback
        }
    }

    class CB implements Runnable {
        public void run() {
            doCallback();
        }
    }
}
