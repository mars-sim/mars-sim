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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 *
 * @param <T> The enum type
 * @author Matthias Mann
 */
public class PersistentEnumModel<T extends Enum<T>> extends AbstractEnumModel<T> {

    private final Preferences prefs;
    private final String prefKey;
    
    private T value;

    public PersistentEnumModel(Preferences prefs, String prefKey, T defaultValue) {
        this(prefs, prefKey, defaultValue.getDeclaringClass(), defaultValue);
    }
    
    public PersistentEnumModel(Preferences prefs, String prefKey, Class<T> enumClass, T defaultValue) {
        super(enumClass);
        if(prefs == null) {
            throw new NullPointerException("prefs");
        }
        if(prefKey == null) {
            throw new NullPointerException("prefKey");
        }
        if(defaultValue == null) {
            throw new NullPointerException("value");
        }
        this.prefs = prefs;
        this.prefKey = prefKey;
        
        T storedValue = defaultValue;
        String storedStr = prefs.get(prefKey, null);
        if(storedStr != null) {
            try {
                storedValue = Enum.valueOf(enumClass, storedStr);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(PersistentEnumModel.class.getName()).log(Level.WARNING, "Unable to parse value '" + storedStr + "' of key '" + prefKey + "' of type " + enumClass, ex);
            }
        }
        setValue(storedValue);
    }
    
    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        if(value == null) {
            throw new NullPointerException("value");
        }
        if(this.value != value) {
            this.value = value;
            storeSetting();
            doCallback();
        }
    }

    private void storeSetting() {
        if(prefs != null) {
            prefs.put(prefKey, value.name());
        }
    }

}
