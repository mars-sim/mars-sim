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
package de.matthiasmann.twl.theme;

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.DebugHook;
import de.matthiasmann.twl.ParameterList;
import de.matthiasmann.twl.ParameterMap;
import de.matthiasmann.twl.renderer.Font;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.MouseCursor;
import java.util.ArrayList;

/**
 *
 * @author Matthias Mann
 */
public class ParameterListImpl extends ThemeChildImpl implements ParameterList {

    final ArrayList<Object> params;

    ParameterListImpl(ThemeManager manager, ThemeInfoImpl parent) {
        super(manager, parent);
        this.params = new ArrayList<Object>();
    }

    public int getSize() {
        return params.size();
    }

    public Font getFont(int idx) {
        Font value = getParameterValue(idx, Font.class);
        if(value != null) {
            return value;
        }
        return manager.getDefaultFont();
    }

    public Image getImage(int idx) {
        Image img = getParameterValue(idx, Image.class);
        if(img == ImageManager.NONE) {
            return null;
        }
        return img;
    }

    public MouseCursor getMouseCursor(int idx) {
        MouseCursor value = getParameterValue(idx, MouseCursor.class);
        return value;
    }

    public ParameterMap getParameterMap(int idx) {
        ParameterMap value = getParameterValue(idx, ParameterMap.class);
        if(value == null) {
            return manager.emptyMap;
        }
        return value;
    }

    public ParameterList getParameterList(int idx) {
        ParameterList value = getParameterValue(idx, ParameterList.class);
        if(value == null) {
            return manager.emptyList;
        }
        return value;
    }

    public boolean getParameter(int idx, boolean defaultValue) {
        Boolean value = getParameterValue(idx, Boolean.class);
        if(value != null) {
            return value.booleanValue();
        }
        return defaultValue;
    }

    public int getParameter(int idx, int defaultValue) {
        Integer value = getParameterValue(idx, Integer.class);
        if(value != null) {
            return value.intValue();
        }
        return defaultValue;
    }

    public float getParameter(int idx, float defaultValue) {
        Float value = getParameterValue(idx, Float.class);
        if(value != null) {
            return value.floatValue();
        }
        return defaultValue;
    }

    public String getParameter(int idx, String defaultValue) {
        String value = getParameterValue(idx, String.class);
        if(value != null) {
            return value;
        }
        return defaultValue;
    }

    public Color getParameter(int idx, Color defaultValue) {
        Color value = getParameterValue(idx, Color.class);
        if(value != null) {
            return value;
        }
        return defaultValue;
    }

    public <E extends Enum<E>> E getParameter(int idx, E defaultValue) {
        Class<E> enumType = defaultValue.getDeclaringClass();
        E value = getParameterValue(idx, enumType);
        if(value != null) {
            return value;
        }
        return defaultValue;
    }

    public Object getParameterValue(int idx) {
        return params.get(idx);
    }

    public <T> T getParameterValue(int idx, Class<T> clazz) {
        Object value = getParameterValue(idx);
        if(value != null && !clazz.isInstance(value)) {
            wrongParameterType(idx, clazz, value.getClass());
            return null;
        }
        return clazz.cast(value);
    }

    protected void wrongParameterType(int idx, Class<?> expectedType, Class<?> foundType) {
        DebugHook.getDebugHook().wrongParameterType(this, idx, expectedType, foundType, getParentDescription());
    }
}
