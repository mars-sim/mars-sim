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
package de.matthiasmann.twl.textarea;

/**
 * A class to hold a value and the unit in which it is specified.
 *
 * @author Matthias Mann
 */
public final class Value {
    public final float value;
    public final Unit unit;

    public Value(float value, Unit unit) {
        this.value = value;
        this.unit = unit;

        if(unit == null) {
            throw new NullPointerException("unit");
        }
        if(unit == Unit.AUTO && value != 0f) {
            throw new IllegalArgumentException("value must be 0 for Unit.AUTO");
        }
    }

    @Override
    public String toString() {
        if(unit == Unit.AUTO) {
            return unit.getPostfix();
        }
        return value + unit.getPostfix();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Value) {
            final Value other = (Value)obj;
            return (this.value == other.value) && (this.unit == other.unit);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Float.floatToIntBits(this.value);
        hash = 17 * hash + this.unit.hashCode();
        return hash;
    }

    public static final Value ZERO_PX = new Value(0, Unit.PX);
    public static final Value AUTO = new Value(0, Unit.AUTO);
    
    public enum Unit {
        PX(false, "px"),
        PT(false, "pt"),
        EM(true, "em"),
        EX(true, "ex"),
        PERCENT(false, "%"),
        AUTO(false, "auto");

        final boolean fontBased;
        final String postfix;
        private Unit(boolean fontBased, String postfix) {
            this.fontBased = fontBased;
            this.postfix = postfix;
        }

        public boolean isFontBased() {
            return fontBased;
        }

        public String getPostfix() {
            return postfix;
        }
    }
}
