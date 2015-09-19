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
package de.matthiasmann.twl.textarea;

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.textarea.TextAreaModel.Clear;
import de.matthiasmann.twl.textarea.TextAreaModel.Display;
import de.matthiasmann.twl.textarea.TextAreaModel.FloatPosition;
import de.matthiasmann.twl.textarea.TextAreaModel.HAlignment;
import de.matthiasmann.twl.textarea.TextAreaModel.VAlignment;
import de.matthiasmann.twl.utils.StringList;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 *
 * @param <T> the data type for this style attribute
 * @author Matthias Mann
 */
public final class StyleAttribute<T> {

    private static final ArrayList<StyleAttribute<?>> attributes = new ArrayList<StyleAttribute<?>>();

    // cascading attributes
    public static final StyleAttribute<HAlignment> HORIZONTAL_ALIGNMENT = new StyleAttribute<HAlignment>(true, HAlignment.class, HAlignment.LEFT);
    public static final StyleAttribute<VAlignment> VERTICAL_ALIGNMENT = new StyleAttribute<VAlignment>(true, VAlignment.class, VAlignment.BOTTOM);
    public static final StyleAttribute<Value> TEXT_INDENT = new StyleAttribute<Value>(true, Value.class, Value.ZERO_PX);
    public static final StyleAttribute<TextDecoration> TEXT_DECORATION = new StyleAttribute<TextDecoration>(true, TextDecoration.class, TextDecoration.NONE);
    public static final StyleAttribute<TextDecoration> TEXT_DECORATION_HOVER = new StyleAttribute<TextDecoration>(true, TextDecoration.class, null);
    public static final StyleAttribute<StringList> FONT_FAMILIES = new StyleAttribute<StringList>(true, StringList.class, new StringList("default"));
    public static final StyleAttribute<Value> FONT_SIZE = new StyleAttribute<Value>(true, Value.class, new Value(14, Value.Unit.PX));
    public static final StyleAttribute<Integer> FONT_WEIGHT = new StyleAttribute<Integer>(true, Integer.class, 400);
    public static final StyleAttribute<Boolean> FONT_ITALIC = new StyleAttribute<Boolean>(true, Boolean.class, Boolean.FALSE);
    public static final StyleAttribute<Integer> TAB_SIZE = new StyleAttribute<Integer>(true, Integer.class, 8);
    public static final StyleAttribute<String> LIST_STYLE_IMAGE = new StyleAttribute<String>(true, String.class, "ul-bullet");
    public static final StyleAttribute<OrderedListType> LIST_STYLE_TYPE = new StyleAttribute<OrderedListType>(true, OrderedListType.class, OrderedListType.DECIMAL);
    public static final StyleAttribute<Boolean> PREFORMATTED = new StyleAttribute<Boolean>(true, Boolean.class, Boolean.FALSE);
    public static final StyleAttribute<Boolean> BREAKWORD = new StyleAttribute<Boolean>(true, Boolean.class, Boolean.FALSE);
    public static final StyleAttribute<Color> COLOR = new StyleAttribute<Color>(true, Color.class, Color.WHITE);
    public static final StyleAttribute<Color> COLOR_HOVER = new StyleAttribute<Color>(true, Color.class, null);
    public static final StyleAttribute<Boolean> INHERIT_HOVER = new StyleAttribute<Boolean>(true, Boolean.class, Boolean.FALSE);

    // non cascading attribute
    public static final StyleAttribute<Clear> CLEAR = new StyleAttribute<Clear>(false, Clear.class, Clear.NONE);
    public static final StyleAttribute<Display> DISPLAY = new StyleAttribute<Display>(false, Display.class, Display.INLINE);
    public static final StyleAttribute<FloatPosition> FLOAT_POSITION = new StyleAttribute<FloatPosition>(false, FloatPosition.class, FloatPosition.NONE);
    public static final StyleAttribute<Value> WIDTH = new StyleAttribute<Value>(false, Value.class, Value.AUTO);
    public static final StyleAttribute<Value> HEIGHT = new StyleAttribute<Value>(false, Value.class, Value.AUTO);
    public static final StyleAttribute<String> BACKGROUND_IMAGE = new StyleAttribute<String>(false, String.class, null);
    public static final StyleAttribute<Color> BACKGROUND_COLOR = new StyleAttribute<Color>(false, Color.class, Color.TRANSPARENT);
    public static final StyleAttribute<Color> BACKGROUND_COLOR_HOVER = new StyleAttribute<Color>(false, Color.class, Color.TRANSPARENT);
    public static final StyleAttribute<Value> MARGIN_TOP = new StyleAttribute<Value>(false, Value.class, Value.ZERO_PX);
    public static final StyleAttribute<Value> MARGIN_LEFT = new StyleAttribute<Value>(false, Value.class, Value.ZERO_PX);
    public static final StyleAttribute<Value> MARGIN_RIGHT = new StyleAttribute<Value>(false, Value.class, Value.ZERO_PX);
    public static final StyleAttribute<Value> MARGIN_BOTTOM = new StyleAttribute<Value>(false, Value.class, Value.ZERO_PX);
    public static final StyleAttribute<Value> PADDING_TOP = new StyleAttribute<Value>(false, Value.class, Value.ZERO_PX);
    public static final StyleAttribute<Value> PADDING_LEFT = new StyleAttribute<Value>(false, Value.class, Value.ZERO_PX);
    public static final StyleAttribute<Value> PADDING_RIGHT = new StyleAttribute<Value>(false, Value.class, Value.ZERO_PX);
    public static final StyleAttribute<Value> PADDING_BOTTOM = new StyleAttribute<Value>(false, Value.class, Value.ZERO_PX);

    // boxes
    public static final BoxAttribute MARGIN = new BoxAttribute(MARGIN_TOP, MARGIN_LEFT, MARGIN_RIGHT, MARGIN_BOTTOM);
    public static final BoxAttribute PADDING = new BoxAttribute(PADDING_TOP, PADDING_LEFT, PADDING_RIGHT, PADDING_BOTTOM);
    
    /**
     * A inherited attribute will be looked up in the parent style if it is not set.
     *
     * @return true if this attribute is inherited from the parent.
     */
    public boolean isInherited() {
        return inherited;
    }
    
    public Class<T> getDataType() {
        return dataType;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns a unique id for this StyleAttribute. This value is may change
     * when this class is modified and should not be used for persistent storage.
     * @return a unique id &lt; {@code getNumAttributes}
     * @see #getNumAttributes()
     */
    public int ordinal() {
        return ordinal;
    }

    /**
     * Returns the name of this StyleAttribute.
     * This method uses reflection to search for the field name.
     * @return the name of this StyleAttribute.
     */
    public String name() {
        try {
            for(Field f : StyleAttribute.class.getFields()) {
                if(Modifier.isStatic(f.getModifiers()) && f.get(null) == this) {
                    return f.getName();
                }
            }
        } catch(Throwable ex) {
            // ignore
        }
        return "?";
    }

    @Override
    public String toString() {
        return name();
    }

    private final boolean inherited;
    private final Class<T> dataType;
    private final T defaultValue;
    private final int ordinal;

    @SuppressWarnings("LeakingThisInConstructor")
    private StyleAttribute(boolean inherited, Class<T> dataType, T defaultValue) {
        this.inherited = inherited;
        this.dataType = dataType;
        this.defaultValue = defaultValue;
        this.ordinal = attributes.size();
        attributes.add(this);
    }
    
    /**
     * Returns the number of implemented StyleAttributes.
     * @return the number of implemented StyleAttributes.
     */
    public static int getNumAttributes() {
        return attributes.size();
    }

    /**
     * Returns the StyleAttribute given it's unique id.
     * @param ordinal the unique id of the desired StyleAttribute.
     * @return the StyleAttribute given it's unique id.
     * @throws IndexOutOfBoundsException if the given id is invalid.
     * @see #ordinal()
     */
    public static StyleAttribute<?> getAttribute(int ordinal) throws IndexOutOfBoundsException {
        return attributes.get(ordinal);
    }

    /**
     * Returns the StyleAttribute given it's name.
     * @param name the name of the StyleAttribute.
     * @return the StyleAttribute
     * @throws IllegalArgumentException if no StyleAttribute with the given name exists.
     * @see #name() 
     */
    public static StyleAttribute<?> getAttribute(String name) throws IllegalArgumentException {
        try {
            Field f = StyleAttribute.class.getField(name);
            if(Modifier.isStatic(f.getModifiers()) &&
                    f.getType() == StyleAttribute.class) {
                return (StyleAttribute<?>)f.get(null);
            }
        } catch(Throwable ex) {
            // ignore
        }
        throw new IllegalArgumentException("No style attribute " + name);
    }
}
