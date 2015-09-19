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
package de.matthiasmann.twl;

import de.matthiasmann.twl.model.FloatModel;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.IllegalFormatException;
import java.util.Locale;

/**
 * A value adjuster for floats.
 *
 * @author Matthias Mann
 */
public class ValueAdjusterFloat extends ValueAdjuster {

    private float value;
    private float minValue;
    private float maxValue = 100f;
    private float dragStartValue;
    private float stepSize = 1f;
    private FloatModel model;
    private Runnable modelCallback;
    private String format = "%.2f";
    private Locale locale = Locale.ENGLISH;

    public ValueAdjusterFloat() {
        setTheme("valueadjuster");
        setDisplayText();
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public ValueAdjusterFloat(FloatModel model) {
        setTheme("valueadjuster");
        setModel(model);
    }

    public float getMaxValue() {
        return maxValue;
    }

    public float getMinValue() {
        return minValue;
    }

    public void setMinMaxValue(float minValue, float maxValue) {
        if(maxValue < minValue) {
            throw new IllegalArgumentException("maxValue < minValue");
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
        setValue(value);
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        if(value > maxValue) {
            value = maxValue;
        } else if(value < minValue) {
            value = minValue;
        }
        if(this.value != value) {
            this.value = value;
            if(model != null) {
                model.setValue(value);
            }
            setDisplayText();
        }
    }

    public float getStepSize() {
        return stepSize;
    }

    /**
     * Sets the step size for the value adjuster.
     * It must be &gt; 0.
     *
     * Default is 1.0f.
     *
     * @param stepSize the new step size
     * @throws IllegalArgumentException if stepSize is NaN or &lt;= 0.
     */
    public void setStepSize(float stepSize) {
        // NaN always compares as false
        if(!(stepSize > 0)) {
            throw new IllegalArgumentException("stepSize");
        }
        this.stepSize = stepSize;
    }

    public FloatModel getModel() {
        return model;
    }

    public void setModel(FloatModel model) {
        if(this.model != model) {
            removeModelCallback();
            this.model = model;
            if(model != null) {
                this.minValue = model.getMinValue();
                this.maxValue = model.getMaxValue();
                addModelCallback();
            }
        }
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) throws IllegalFormatException {
        // test format
        String.format(locale, format, 42f);
        this.format = format;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        if(locale == null) {
            throw new NullPointerException("locale");
        }
        this.locale = locale;
    }

    @Override
    protected String onEditStart() {
        return formatText();
    }

    @Override
    protected boolean onEditEnd(String text) {
        try {
            setValue(parseText(text));
            return true;
        } catch (ParseException ex) {
            return false;
        }
    }

    @Override
    protected String validateEdit(String text) {
        try {
            parseText(text);
            return null;
        } catch (ParseException ex) {
            return ex.toString();
        }
    }

    @Override
    protected void onEditCanceled() {
    }

    @Override
    protected boolean shouldStartEdit(char ch) {
        return (ch >= '0' && ch <= '9') || (ch == '-') || (ch == '.');
    }

    @Override
    protected void onDragStart() {
        dragStartValue = value;
    }

    @Override
    protected void onDragUpdate(int dragDelta) {
        float range = Math.max(1e-4f, Math.abs(getMaxValue() - getMinValue()));
        setValue(dragStartValue + dragDelta/Math.max(3, getWidth()/range));
    }

    @Override
    protected void onDragCancelled() {
        setValue(dragStartValue);
    }

    @Override
    protected void doDecrement() {
        setValue(value - getStepSize());
    }

    @Override
    protected void doIncrement() {
        setValue(value + getStepSize());
    }

    @Override
    protected String formatText() {
        return String.format(locale, format, value);
    }

    protected float parseText(String value) throws ParseException {
        return NumberFormat.getNumberInstance(locale).parse(value).floatValue();
    }

    protected void syncWithModel() {
        cancelEdit();
        this.minValue = model.getMinValue();
        this.maxValue = model.getMaxValue();
        this.value = model.getValue();
        setDisplayText();
    }

    @Override
    protected void afterAddToGUI(GUI gui) {
        super.afterAddToGUI(gui);
        addModelCallback();
    }

    @Override
    protected void beforeRemoveFromGUI(GUI gui) {
        removeModelCallback();
        super.beforeRemoveFromGUI(gui);
    }

    protected void removeModelCallback() {
        if(model != null && modelCallback != null) {
            model.removeCallback(modelCallback);
        }
    }

    protected void addModelCallback() {
        if(model != null && getGUI() != null) {
            if(modelCallback == null) {
                modelCallback = new ModelCallback();
            }
            model.addCallback(modelCallback);
            syncWithModel();
        }
    }
}
