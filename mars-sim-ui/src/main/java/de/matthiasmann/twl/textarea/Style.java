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

import java.util.HashMap;
import java.util.Map;

/**
 * Stores the styles which should be applied to a certain element.
 * 
 * @author Matthias Mann
 */
public class Style {

    private final Style parent;
    private final StyleSheetKey styleSheetKey;
    private Object[] values;

    /**
     * Creates an empty Style without a parent, class reference and no attributes
     */
    public Style() {
        this(null, null);
    }

    /**
     * Creates an Style with the given parent and class reference.
     *
     * @param parent the parent style. Can be null.
     * @param styleSheetKey key for style sheet lookup. Can be null.
     */
    public Style(Style parent, StyleSheetKey styleSheetKey) {
        this.parent = parent;
        this.styleSheetKey = styleSheetKey;
    }

    /**
     * Creates an Style with the given parent and class reference and copies the
     * given attributes.
     *
     * @param parent the parent style. Can be null.
     * @param styleSheetKey key for style sheet lookup. Can be null.
     * @param values a map with attributes for this Style. Can be null.
     */
    public Style(Style parent, StyleSheetKey styleSheetKey, Map<StyleAttribute<?>, Object> values) {
        this(parent, styleSheetKey);
        
        if(values != null) {
            putAll(values);
        }
    }

    protected Style(Style src) {
        this.parent = src.parent;
        this.styleSheetKey = src.styleSheetKey;
        this.values = (src.values != null) ? src.values.clone() : null;
    }

    /**
     * Resolves the Style in which the specified attribute is defined.
     *
     * If a attribute does not cascade then this method does nothing.
     *
     * If a StyleSheetResolver is specified then this method will treat
     * style sheet styles referenced by this Style as if they are part
     * of a Style in this chain.
     * 
     * @param attribute The attribute to lookup.
     * @param resolver A StyleSheetResolver to resolve the style sheet key. Can be null.
     * @return The Style which defined the specified attribute, will never return null.
     * @see StyleAttribute#isInherited()
     * @see #getParent()
     */
    public Style resolve(StyleAttribute<?> attribute, StyleSheetResolver resolver) {
        if(!attribute.isInherited()) {
            return this;
        }

        return doResolve(this, attribute.ordinal(), resolver);
    }

    private static Style doResolve(Style style, int ord, StyleSheetResolver resolver) {
        for(;;) {
            if(style.parent == null) {
                return style;
            }
            if(style.rawGet(ord) != null) {
                return style;
            }
            if(resolver != null && style.styleSheetKey != null) {
                Style styleSheetStyle = resolver.resolve(style);
                if(styleSheetStyle != null && styleSheetStyle.rawGet(ord) != null) {
                    // return main style here because class style has no parent chain
                    return style;
                }
            }
            style = style.parent;
        }

    }

    /**
     * Retrives the value of the specified attribute without resolving the style.
     *
     * If the attribute is not set in this Style and a StyleSheetResolver was
     * specified then the lookup is continued in the style sheet.
     *
     * @param <V> The data type of the attribute
     * @param attribute The attribute to lookup.
     * @param resolver A StyleSheetResolver to resolve the style sheet key. Can be null.
     * @return The attribute value if it was set, or the default value of the attribute.
     */
    public<V> V getNoResolve(StyleAttribute<V> attribute, StyleSheetResolver resolver) {
        Object value = rawGet(attribute.ordinal());
        if(value == null) {
            if(resolver != null && styleSheetKey != null) {
                Style styleSheetStyle = resolver.resolve(this);
                if(styleSheetStyle != null) {
                    value = styleSheetStyle.rawGet(attribute.ordinal());
                }
            }
            if(value == null) {
                return attribute.getDefaultValue();
            }
        }
        return attribute.getDataType().cast(value);
    }

    /**
     * Retrives the value of the specified attribute from the resolved style.
     *
     * @param <V> The data type of the attribute
     * @param attribute The attribute to lookup.
     * @param resolver A StyleSheetResolver to resolve the style sheet key. Can be null.
     * @return The attribute value if it was set, or the default value of the attribute.
     * @see #resolve(de.matthiasmann.twl.textarea.StyleAttribute, de.matthiasmann.twl.textarea.StyleSheetResolver)
     * @see #getNoResolve(de.matthiasmann.twl.textarea.StyleAttribute, de.matthiasmann.twl.textarea.StyleSheetResolver)
     */
    public<V> V get(StyleAttribute<V> attribute, StyleSheetResolver resolver) {
        return resolve(attribute, resolver).getNoResolve(attribute, resolver);
    }

    /**
     * Retrives the value of the specified attribute without resolving the style.
     * 
     * @param <V> The data type of the attribute
     * @param attribute The attribute to lookup.
     * @return the attribute value or null (no default value)
     */
    public<V> V getRaw(StyleAttribute<V> attribute) {
        Object value = rawGet(attribute.ordinal());
        return attribute.getDataType().cast(value);
    }
    
    /**
     * Returns the parent of this Style or null. The parent is used to lookup
     * attributes which can be inherited and are not specified in this Style.
     * 
     * @return the parent of this Style or null.
     * @see StyleAttribute#isInherited()
     */
    public Style getParent() {
        return parent;
    }

    /**
     * Returns the style sheet key for this Style or null.
     * It is used to lookup attributes which are not set in this Style.
     * 
     * @return the style sheet key this Style or null.
     */
    public StyleSheetKey getStyleSheetKey() {
        return styleSheetKey;
    }

    /**
     * Creates a copy of this Style and sets the specified attributes.
     *
     * It is possible to set a attribute to null to 'unset' it.
     *
     * @param values The attributes to set in the new Style.
     * @return a new Style with the same parent, styleSheetKey and modified attributes.
     */
    public Style with(Map<StyleAttribute<?>, Object> values) {
        Style newStyle = new Style(this);
        newStyle.putAll(values);
        return newStyle;
    }

    /**
     * Creates a copy of this Style and sets the specified attributes.
     *
     * It is possible to set a attribute to null to 'unset' it.
     * 
     * @param <V> The data type of the attribute
     * @param attribute The attribute to set.
     * @param value The new value of that attribute. Can be null.
     * @return a new Style with the same parent, styleSheetKey and modified attribute.
     */
    public<V> Style with(StyleAttribute<V> attribute, V value) {
        Style newStyle = new Style(this);
        newStyle.put(attribute, value);
        return newStyle;
    }
    
    /**
     * Returns a Style which doesn't contain any value for an attribute where
     * {@link StyleAttribute#isInherited() } returns false.
     * 
     * @return a Style with the same parent, styleSheetKey and modified attribute.
     */
    public Style withoutNonInheritable() {
        if(values != null) {
            for(int i=0,n=values.length ; i<n ; i++) {
                if(values[i] != null && !StyleAttribute.getAttribute(i).isInherited()) {
                    return withoutNonInheritableCopy();
                }
            }
        }
        return this;
    }
    
    private Style withoutNonInheritableCopy() {
        Style result = new Style(parent, styleSheetKey);
        for(int i=0,n=values.length ; i<n ; i++) {
            Object value = values[i];
            if(value != null) {
                StyleAttribute<?> attribute = StyleAttribute.getAttribute(i);
                if(attribute.isInherited()) {
                    result.put(attribute, value);
                }
            }
        }
        return result;
    }
    
    protected void put(StyleAttribute<?> attribute, Object value) {
        if(attribute == null) {
            throw new IllegalArgumentException("attribute is null");
        }
        if(value == null) {
            if(values == null) {
                return;
            }
        } else {
            if(!attribute.getDataType().isInstance(value)) {
                throw new IllegalArgumentException("value is a " + value.getClass() +
                        " but must be a " + attribute.getDataType());
            }
            ensureValues();
        }

        values[attribute.ordinal()] = value;
    }

    protected final void putAll(Map<StyleAttribute<?>, Object> values) {
        for(Map.Entry<StyleAttribute<?>, Object> e : values.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    protected final void putAll(Style src) {
        if(src.values != null) {
            ensureValues();
            for(int i=0,n=values.length ; i<n ; i++) {
                Object value = src.values[i];
                if(value != null) {
                    this.values[i] = value;
                }
            }
        }
    }

    protected final void ensureValues() {
        if(this.values == null) {
            this.values = new Object[StyleAttribute.getNumAttributes()];
        }
    }
    
    protected final Object rawGet(int idx) {
        final Object[] vals = values;
        if(vals != null) {
            return vals[idx];
        }
        return null;
    }
    
    /**
     * Creates a map which will contain all set attributes of this Style.
     * Changes to that map have no impact on this Style.
     * @return a map which will contain all set attributes of this Style.
     */
    public Map<StyleAttribute<?>, Object> toMap() {
        HashMap<StyleAttribute<?>, Object> result = new HashMap<StyleAttribute<?>, Object>();
        for(int ord=0 ; ord<values.length ; ord++) {
            Object value = values[ord];
            if(value != null) {
                result.put(StyleAttribute.getAttribute(ord), value);
            }
        }
        return result;
    }
}
