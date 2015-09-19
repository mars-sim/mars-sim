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
package de.matthiasmann.twl.renderer;

import de.matthiasmann.twl.Color;
import java.util.HashMap;

/**
 * An extensible typed map for font parameters.
 * 
 * @author Matthias Mann
 */
public final class FontParameter {
    
    static final HashMap<String, Parameter<?>> parameterMap = new HashMap<String, Parameter<?>>();

    public static final Parameter<Color> COLOR = newParameter("color", Color.WHITE);
    public static final Parameter<Boolean> UNDERLINE = newParameter("underline", false);
    public static final Parameter<Boolean> LINETHROUGH = newParameter("linethrough", false);
    
    private Object[] values;

    public FontParameter() {
        this.values = new Object[8];
    }

    public FontParameter(FontParameter base) {
        this.values = base.values.clone();
    }

    /**
     * Sets a parameter value
     * @param <T> the type of the parameter
     * @param param the parameter
     * @param value the value or null to revert to it's default value
     */
    public<T> void put(Parameter<T> param, T value) {
        if(param == null) {
            throw new NullPointerException("type");
        }
        if(value != null && !param.dataClass.isInstance(value)) {
            throw new ClassCastException("value");
        }
        int ordinal = param.ordinal;
        int curLength = values.length;
        if(ordinal >= curLength) {
            Object[] tmp = new Object[Math.max(ordinal + 1, curLength*2)];
            System.arraycopy(values, 0, tmp, 0, curLength);
            values = tmp;
        }
        values[ordinal] = value;
    }
    
    /**
     * Returns the value of the specified parameter
     * @param <T> the type of the parameter
     * @param param the parameter
     * @return the parameter value or it's default value when the parameter was not set
     */
    public<T> T get(Parameter<T> param) {
        if(param.ordinal < values.length) {
            Object raw = values[param.ordinal];
            if(raw != null) {
                return param.dataClass.cast(raw);
            }
        }
        return param.defaultValue;
    }
    
    /**
     * Returns an array of all registered parameter
     * @return an array of all registered parameter
     */
    public static Parameter[] getRegisteredParameter() {
        synchronized (parameterMap) {
            return parameterMap.values().toArray(new Parameter<?>[parameterMap.size()]);
        }
    }
    
    /**
     * Returns the parameter instance for the given name
     * @param name the name to look up
     * @return the parameter instance or null when the name is not registered
     */
    public static Parameter<?> getParameter(String name) {
        synchronized (parameterMap) {
            return parameterMap.get(name);
        }
    }
    
    /**
     * Registers a new parameter.
     * 
     * <p>The data class is extracted from the default value.</p>
     * <p>If the name is already registered then the existing parameter is returned.</p>
     * 
     * @param <T> the data type of the parameter
     * @param name the parameter name
     * @param defaultValue the default value
     * @return the parameter instance
     * @throws NullPointerException when one of the parameters is null
     * @throws IllegalStateException when the name is already registered but with
     *                               different dataClass or defaultValue
     */
    public static<T> Parameter<T> newParameter(String name, T defaultValue) {
        if(defaultValue == null) {
            throw new NullPointerException("defaultValue");
        }
        @SuppressWarnings("unchecked")
        Class<T> dataClass = (Class<T>)defaultValue.getClass();
        return newParameter(name, dataClass, defaultValue);
    }
    
    /**
     * Registers a new parameter.
     * 
     * <p>If the name is already registered then the existing parameter is returned.</p>
     * 
     * @param <T> the data type of the parameter
     * @param name the parameter name
     * @param dataClass the data class
     * @param defaultValue the default value - can be null.
     * @return the parameter instance
     * @throws NullPointerException when name or dataClass is null
     * @throws IllegalStateException when the name is already registered but with
     *                               different dataClass or defaultValue
     */
    public static<T> Parameter<T> newParameter(String name, Class<T> dataClass, T defaultValue) {
        if(name == null) {
            throw new NullPointerException("name");
        }
        if(dataClass == null) {
            throw new NullPointerException("dataClass");
        }
        
        synchronized (parameterMap) {
            Parameter<?> existing = parameterMap.get(name);
            if(existing != null) {
                if(existing.dataClass != dataClass || !equals(existing.defaultValue, defaultValue)) {
                    throw new IllegalStateException("type '" + name + "' already registered but different");
                }
                
                @SuppressWarnings("unchecked")
                Parameter<T> type = (Parameter<T>)existing;
                return type;
            }
            
            Parameter<T> type = new Parameter<T>(name, dataClass, defaultValue, parameterMap.size());
            parameterMap.put(name, type);
            return type;
        }
    }
    
    private static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
    
    public static final class Parameter<T> {
        final String name;
        final Class<T> dataClass;
        final T defaultValue;
        final int ordinal;

        Parameter(String name, Class<T> dataClass, T defaultValue, int ordinal) {
            this.name = name;
            this.dataClass = dataClass;
            this.defaultValue = defaultValue;
            this.ordinal = ordinal;
        }

        public final String getName() {
            return name;
        }

        public final Class<T> getDataClass() {
            return dataClass;
        }

        public final T getDefaultValue() {
            return defaultValue;
        }

        @Override
        public String toString() {
            return ordinal + ":" + name + ":" + dataClass.getSimpleName();
        }
    }
}
