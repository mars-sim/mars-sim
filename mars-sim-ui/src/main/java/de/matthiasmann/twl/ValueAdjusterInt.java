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
package de.matthiasmann.twl;

import de.matthiasmann.twl.model.IntegerModel;

/**
 * A value adjuster for integers.
 *
 * @author Matthias Mann
 */
public class ValueAdjusterInt extends ValueAdjuster {

    private int value;
    private int minValue;
    private int maxValue = 100;
    private int dragStartValue;
    private IntegerModel model;
    private Runnable modelCallback;

    public ValueAdjusterInt() {
        setTheme("valueadjuster");
        setDisplayText();
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public ValueAdjusterInt(IntegerModel model) {
        setTheme("valueadjuster");
        setModel(model);
    }

    public int getMaxValue() {
        if(model != null) {
            maxValue = model.getMaxValue();
        }
        return maxValue;
    }

    public int getMinValue() {
        if(model != null) {
            minValue = model.getMinValue();
        }
        return minValue;
    }

    public void setMinMaxValue(int minValue, int maxValue) {
        if(maxValue < minValue) {
            throw new IllegalArgumentException("maxValue < minValue");
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
        setValue(value);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        value = Math.max(getMinValue(), Math.min(getMaxValue(), value));
        if(this.value != value) {
            this.value = value;
            if(model != null) {
                model.setValue(value);
            }
            setDisplayText();
        }
    }

    public IntegerModel getModel() {
        return model;
    }

    public void setModel(IntegerModel model) {
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
    
    
    @Override
    protected String onEditStart() {
        return formatText();
    }

    @Override
    protected boolean onEditEnd(String text) {
        try {
            setValue(Integer.parseInt(text));
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    @Override
    protected String validateEdit(String text) {
        try {
            Integer.parseInt(text);
            return null;
        } catch (NumberFormatException ex) {
            return ex.toString();
        }
    }

    @Override
    protected void onEditCanceled() {
    }

    @Override
    protected boolean shouldStartEdit(char ch) {
        return (ch >= '0' && ch <= '9') || (ch == '-');
    }

    @Override
    protected void onDragStart() {
        dragStartValue = value;
    }

    @Override
    protected void onDragUpdate(int dragDelta) {
        int range = Math.max(1, Math.abs(getMaxValue() - getMinValue()));
        setValue(dragStartValue + dragDelta/Math.max(3, getWidth()/range));
    }

    @Override
    protected void onDragCancelled() {
        setValue(dragStartValue);
    }

    @Override
    protected void doDecrement() {
        setValue(value - 1);
    }

    @Override
    protected void doIncrement() {
        setValue(value + 1);
    }

    @Override
    protected String formatText() {
        return Integer.toString(value);
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
