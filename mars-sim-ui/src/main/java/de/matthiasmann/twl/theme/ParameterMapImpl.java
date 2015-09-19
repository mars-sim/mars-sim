/*
 * Copyright (c) 2008-2011, Matthias Mann
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
package de.matthiasmann.twl.theme;

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.DebugHook;
import de.matthiasmann.twl.ParameterList;
import de.matthiasmann.twl.ParameterMap;
import de.matthiasmann.twl.renderer.Font;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.MouseCursor;
import de.matthiasmann.twl.utils.CascadedHashMap;
import java.util.Map;

/**
 *
 * @author Matthias Mann
 */
class ParameterMapImpl extends ThemeChildImpl implements ParameterMap {
    
    private final CascadedHashMap<String, Object> params;

    ParameterMapImpl(ThemeManager manager, ThemeInfoImpl parent) {
        super(manager, parent);
        this.params = new CascadedHashMap<String, Object>();
    }
    
    void copy(ParameterMapImpl src) {
        params.collapseAndSetFallback(src.params);
    }

    public Font getFont(String name) {
        Font value = getParameterValue(name, true, Font.class);
        if(value != null) {
            return value;
        }
        return manager.getDefaultFont();
    }

    public Image getImage(String name) {
        Image img = getParameterValue(name, true, Image.class);
        if(img == ImageManager.NONE) {
            return null;
        }
        return img;
    }

    public MouseCursor getMouseCursor(String name) {
        MouseCursor value = getParameterValue(name, false, MouseCursor.class);
        return value;
    }

    public ParameterMap getParameterMap(String name) {
        ParameterMap value = getParameterValue(name, true, ParameterMap.class);
        if(value == null) {
            return manager.emptyMap;
        }
        return value;
    }

    public ParameterList getParameterList(String name) {
        ParameterList value = getParameterValue(name, true, ParameterList.class);
        if(value == null) {
            return manager.emptyList;
        }
        return value;
    }

    public boolean getParameter(String name, boolean defaultValue) {
        Boolean value = getParameterValue(name, true, Boolean.class);
        if(value != null) {
            return value.booleanValue();
        }
        return defaultValue;
    }

    public int getParameter(String name, int defaultValue) {
        Integer value = getParameterValue(name, true, Integer.class);
        if(value != null) {
            return value.intValue();
        }
        return defaultValue;
    }

    public float getParameter(String name, float defaultValue) {
        Float value = getParameterValue(name, true, Float.class);
        if(value != null) {
            return value.floatValue();
        }
        return defaultValue;
    }

    public String getParameter(String name, String defaultValue) {
        String value = getParameterValue(name, true, String.class);
        if(value != null) {
            return value;
        }
        return defaultValue;
    }

    public Color getParameter(String name, Color defaultValue) {
        Color value = getParameterValue(name, true, Color.class);
        if(value != null) {
            return value;
        }
        return defaultValue;
    }

    public <E extends Enum<E>> E getParameter(String name, E defaultValue) {
        Class<E> enumType = defaultValue.getDeclaringClass();
        E value = getParameterValue(name, true, enumType);
        if(value != null) {
            return value;
        }
        return defaultValue;
    }

    public Object getParameterValue(String name, boolean warnIfNotPresent) {
        Object value = params.get(name);
        if(value == null && warnIfNotPresent) {
            missingParameter(name, null);
        }
        return value;
    }

    public <T> T getParameterValue(String name, boolean warnIfNotPresent, Class<T> clazz) {
        return getParameterValue(name, warnIfNotPresent, clazz, null);
    }

    public <T> T getParameterValue(String name, boolean warnIfNotPresent, Class<T> clazz, T defaultValue) {
        Object value = params.get(name);
        if(value == null && warnIfNotPresent) {
            missingParameter(name, clazz);
        }
        if(!clazz.isInstance(value)) {
            if(value != null) {
                wrongParameterType(name, clazz, value.getClass());
            }
            return defaultValue;
        }
        return clazz.cast(value);
    }


    protected void wrongParameterType(String paramName, Class<?> expectedType, Class<?> foundType) {
        DebugHook.getDebugHook().wrongParameterType(this, paramName, expectedType, foundType, getParentDescription());
    }

    protected void missingParameter(String paramName, Class<?> dataType) {
        DebugHook.getDebugHook().missingParameter(this, paramName, getParentDescription(), dataType);
    }
    
    protected void replacingWithDifferentType(String paramName, Class<?> oldType, Class<?> newType) {
        DebugHook.getDebugHook().replacingWithDifferentType(this, paramName, oldType, newType, getParentDescription());
    }

    Object getParam(String name) {
        return params.get(name);
    }
    
    void put(Map<String, ?> params) {
        for(Map.Entry<String, ?> e : params.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }
    
    void put(String paramName, Object value) {
        Object old = params.put(paramName, value);
        if(old != null && value != null) {
            Class<?> oldClass = old.getClass();
            Class<?> newClass = value.getClass();

            if(oldClass != newClass && !areTypesCompatible(oldClass, newClass)) {
                replacingWithDifferentType(paramName, oldClass, newClass);
            }
        }
    }
    
    private static boolean areTypesCompatible(Class<?> classA, Class<?> classB) {
        for(Class<?> clazz : BASE_CLASSES) {
            if(clazz.isAssignableFrom(classA) && clazz.isAssignableFrom(classB)) {
                return true;
            }
        }
        return false;
    }
    
    private static final Class<?> BASE_CLASSES[] = {
        Image.class,
        Font.class,
        MouseCursor.class
    };
}
