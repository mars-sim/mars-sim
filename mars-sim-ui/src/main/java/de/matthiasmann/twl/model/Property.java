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
 * A genric property interface for use with the PropertySheet widget
 *
 * @param <T> The type of the property values
 * 
 * @author Matthias Mann
 */
public interface Property<T> {

    /**
     * The property name as displayed in the UI
     * @return the property name
     */
    public String getName();

    /**
     * Returns true if this property is read only.
     * Calling the setValue() method on a read only property is undefined.
     *
     * @return true if this property is read only
     */
    public boolean isReadOnly();

    /**
     * Returns true if this property can be set to null. This is similar to
     * a SQL null.
     * 
     * @return true if this property can be null
     */
    public boolean canBeNull();

    /**
     * Retrieves the current property value
     * @return the current property value
     */
    public T getPropertyValue();

    /**
     * Changes the property value. If the value is invalid then an
     * IllegalArgumentException argument except should be thrown and
     * the UI will indicate the invalid state of the input.
     *
     * @param value The new value
     * @throws IllegalArgumentException if the new value can't be accepted
     * @throws NullPointerException if value is null and canBeNull returned false
     * @see #canBeNull()
     */
    public void setPropertyValue(T value) throws IllegalArgumentException;

    /**
     * Returns the type of the property. This is used to select an
     * editor widget in the PropertySheet.
     *
     * @return The class of the property values
     */
    public Class<T> getType();

    /**
     * Registers a value changed callback. It should be called when the value
     * was changed from something other then setValue
     *
     * @param cb the value changed callback
     */
    public void addValueChangedCallback(Runnable cb);

    /**
     * Removes a value changed callback
     *
     * @param cb the value changed callback
     */
    public void removeValueChangedCallback(Runnable cb);
}
