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
 * A simple implementation of a property
 *
 * @param <T> the type of the property value
 *
 * @author Matthias Mann
 */
public class SimpleProperty<T> extends AbstractProperty<T> {

    private final Class<T> type;
    private final String name;
    private boolean readOnly;
    private T value;

    public SimpleProperty(Class<T> type, String name, T value) {
        this(type, name, value, false);
    }

    public SimpleProperty(Class<T> type, String name, T value, boolean readOnly) {
        this.type = type;
        this.name = name;
        this.readOnly = readOnly;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean canBeNull() {
        return false;
    }

    public T getPropertyValue() {
        return value;
    }

    /**
     * Changes the property value. It calls {@code valueChanged} to determine if
     * the value has really changed and if so updates the value and calls the callbacks.
     * 
     * @param value the new value for the property
     * @throws IllegalArgumentException is not thrown but part of the Property interface
     * @throws NullPointerException if value is null and canBeNull returned false
     * @see #canBeNull()
     * @see #valueChanged(java.lang.Object) 
     */
    public void setPropertyValue(T value) throws IllegalArgumentException {
        if(value == null && !canBeNull()) {
            throw new NullPointerException("value");
        }
        if(valueChanged(value)) {
            this.value = value;
            fireValueChangedCallback();
        }
    }

    public Class<T> getType() {
        return type;
    }

    /**
     * This method is used by setPropertyValue to check if the callback should be fired or not
     *
     * The default implementation calls equals on the current value.
     * 
     * @param newValue the new value passed to setPropertyValue
     * @return true if the value has changed and the callback should be fired
     */
    protected boolean valueChanged(T newValue) {
        return value != newValue && (value == null || !value.equals(newValue));
    }
}
