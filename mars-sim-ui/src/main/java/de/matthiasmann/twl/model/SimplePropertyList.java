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

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple property list property. Used to create sub properties in the PropertySheet.
 *
 * @author Matthias Mann
 */
public class SimplePropertyList extends AbstractProperty<PropertyList> implements PropertyList {

    private final String name;
    private final ArrayList<Property<?>> properties;

    public SimplePropertyList(String name) {
        this.name = name;
        this.properties = new ArrayList<Property<?>>();
    }

    public SimplePropertyList(String name, Property<?>... properties) {
        this(name);
        this.properties.addAll(Arrays.asList(properties));
    }

    public String getName() {
        return name;
    }

    public boolean isReadOnly() {
        return true;
    }

    public boolean canBeNull() {
        return false;
    }

    public PropertyList getPropertyValue() {
        return this;
    }

    public void setPropertyValue(PropertyList value) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported");
    }

    public Class<PropertyList> getType() {
        return PropertyList.class;
    }

    public int getNumProperties() {
        return properties.size();
    }

    public Property<?> getProperty(int idx) {
        return properties.get(idx);
    }

    public void addProperty(Property<?> property) {
        properties.add(property);
        fireValueChangedCallback();
    }

    public void addProperty(int idx, Property<?> property) {
        properties.add(idx, property);
        fireValueChangedCallback();
    }

    public void removeProperty(int idx) {
        properties.remove(idx);
        fireValueChangedCallback();
    }

    public void removeAllProperties() {
        properties.clear();
        fireValueChangedCallback();
    }
}
