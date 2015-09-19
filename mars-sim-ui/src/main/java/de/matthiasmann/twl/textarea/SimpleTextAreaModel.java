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

import de.matthiasmann.twl.model.HasCallback;
import java.util.Collections;
import java.util.Iterator;

/**
 * A simple text area model which represents the complete text as a single
 * paragraph.
 * 
 * <p>The initial style is an empty style - see {@link Style#Style() }.
 * It can be changed before setting the text.</p>
 *
 * @author Matthias Mann
 * @see #setStyle(de.matthiasmann.twl.textarea.Style) 
 */
public class SimpleTextAreaModel extends HasCallback implements TextAreaModel {

    private Style style;
    private Element element;

    public SimpleTextAreaModel() {
        style = new Style();
    }

    /**
     * Constructs a SimpleTextAreaModel with pre-formatted text.
     * Use {@code '\n'} to create line breaks.
     * 
     * @param text the text (interpreted as pre-formatted)
     * @see #setText(java.lang.String)
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public SimpleTextAreaModel(String text) {
        this();
        setText(text);
    }

    /**
     * Returns the style used for the next call to {@link #setText(java.lang.String, boolean) } 
     * @return the style 
     */
    public Style getStyle() {
        return style;
    }

    /**
     * Sets the style used for the next call to {@link #setText(java.lang.String, boolean) }.
     * It does not affect the currently set text.
     * 
     * @param style the style
     * @throws NullPointerException when style is {@code null}
     */
    public void setStyle(Style style) {
        if(style == null) {
            throw new NullPointerException("style");
        }
        this.style = style;
    }

    /**
     * Sets the text for this SimpleTextAreaModel as pre-formatted text.
     * Use {@code '\n'} to create line breaks.
     *
     * This is equivalent to calling {@code setText(text, true);}
     * @param text the text (interpreted as pre-formatted)
     * @see #setText(java.lang.String, boolean) 
     */
    public void setText(String text) {
        setText(text, true);
    }

    /**
     * Sets the text for this SimpleTextAreaModel.
     * Use {@code '\n'} to create line breaks.
     *
     * <p>The {@code preformatted} will set the white space attribute as follows:</p>
     * <pre>false = {@code white-space: normal}<br>true  = {@code white-space: pre}</pre>
     * 
     * @param text the text
     * @param preformatted if the text should be treated as pre-formated or not.
     */
    public void setText(String text, boolean preformatted) {
        Style textstyle = style.with(StyleAttribute.PREFORMATTED, preformatted);
        element = new TextElement(textstyle, text);
        doCallback();
    }
    
    public Iterator<Element> iterator() {
        return ((element != null)
                ? Collections.<Element>singletonList(element)
                : Collections.<Element>emptyList()).iterator();
    }
}
