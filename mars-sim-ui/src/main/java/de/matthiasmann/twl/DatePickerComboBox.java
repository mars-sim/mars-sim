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
package de.matthiasmann.twl;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import de.matthiasmann.twl.model.DateModel;

/**
 * A date picker combobox
 * 
 * @author Matthias Mann
 */
public class DatePickerComboBox extends ComboBoxBase {

    private final ComboboxLabel label;
    private final DatePicker datePicker;

    public DatePickerComboBox() {
        this(Locale.getDefault(), DateFormat.getDateInstance());
    }
    
    /**
     * Constructs a date picker combo box using the specified locale and date format style
     * @param locale the locale
     * @param style the date style
     * @see DateFormat#getDateInstance(int, java.util.Locale) 
     */
    public DatePickerComboBox(Locale locale, int style) {
        this(locale, DateFormat.getDateInstance(style, locale));
    }
    
    public DatePickerComboBox(Locale locale, DateFormat dateFormat) {
        L l = new L();
        
        label = new ComboboxLabel(getAnimationState());
        label.setTheme("display");
        label.addCallback(l);
        
        datePicker = new DatePicker(locale, dateFormat);
        datePicker.addCallback(l);
        
        popup.add(datePicker);
        popup.setTheme("datepickercomboboxPopup");
        
        button.getModel().addStateCallback(l);
        
        add(label);
    }

    public void setModel(DateModel model) {
        datePicker.setModel(model);
    }

    public DateModel getModel() {
        return datePicker.getModel();
    }

    public void setDateFormat(Locale locale, DateFormat dateFormat) {
        datePicker.setDateFormat(locale, dateFormat);
    }

    public DateFormat getDateFormat() {
        return datePicker.getDateFormat();
    }

    public Locale getLocale() {
        return datePicker.getLocale();
    }
    
    @Override
    protected ComboboxLabel getLabel() {
        return label;
    }

    protected DatePicker getDatePicker() {
        return datePicker;
    }
    
    @Override
    protected void setPopupSize() {
        int minWidth = popup.getMinWidth();
        int minHeight = popup.getMinHeight();
        int popupWidth = computeSize(minWidth,
                popup.getPreferredWidth(),
                popup.getMaxWidth());
        int popupHeight = computeSize(minHeight,
                popup.getPreferredHeight(),
                popup.getMaxHeight());
        Widget container = popup.getParent();
        int popupMaxRight = container.getInnerRight();
        int popupMaxBottom = container.getInnerBottom();
        int x = getX();
        int y = getBottom();
        if(x + popupWidth > popupMaxRight) {
            if(getRight() - popupWidth >= container.getInnerX()) {
                x = getRight() - popupWidth;
            } else {
                x = popupMaxRight - minWidth;
            }
        }
        if(y + popupHeight > popupMaxBottom) {
            if(getY() - popupHeight >= container.getInnerY()) {
                y = getY() - popupHeight;
            } else {
                y = popupMaxBottom - minHeight;
            }
        }
        popupWidth = Math.min(popupWidth, popupMaxRight - x);
        popupHeight = Math.min(popupHeight, popupMaxBottom - y);
        popup.setPosition(x, y);
        popup.setSize(popupWidth, popupHeight);
    }
    
    protected void updateLabel() {
        label.setText(datePicker.formatDate());
    }
    
    void updateHover() {
        getAnimationState().setAnimationState(Label.STATE_HOVER,
                label.hover || button.getModel().isHover());
    }

    protected class ComboboxLabel extends Label {
        boolean hover;

        public ComboboxLabel(AnimationState animState) {
            super(animState);
            setAutoSize(false);
            setClip(true);
            setTheme("display");
        }

        @Override
        public int getPreferredInnerHeight() {
            int prefHeight = super.getPreferredInnerHeight();
            if(getFont() != null) {
                prefHeight = Math.max(prefHeight, getFont().getLineHeight());
            }
            return prefHeight;
        }

        @Override
        protected void handleMouseHover(Event evt) {
            if(evt.isMouseEvent()) {
                boolean newHover = evt.getType() != Event.Type.MOUSE_EXITED;
                if(newHover != hover) {
                    hover = newHover;
                    updateHover();
                }
            }
        }
    }
    
    class L implements Runnable, CallbackWithReason<Label.CallbackReason>, DatePicker.Callback {
        public void run() {
            updateHover();
        }

        public void callback(Label.CallbackReason reason) {
            openPopup();
        }

        public void calendarChanged(Calendar calendar) {
            updateLabel();
        }
    }
}
