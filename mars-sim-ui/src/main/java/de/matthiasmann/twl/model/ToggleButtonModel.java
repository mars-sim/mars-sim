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

/**
 * A toggle button model based on SimpleButtonModel.
 * Adds selected state bit. Can be backed by a BooleanModel.
 * 
 * @author Matthias Mann
 */
public class ToggleButtonModel extends SimpleButtonModel {

    protected static final int STATE_MASK_SELECTED = 256;
    
    private BooleanModel model;
    private Runnable modelCallback;
    private boolean invertModelState;
    private boolean isConnected;
    
    public ToggleButtonModel() {
    }

    public ToggleButtonModel(BooleanModel model) {
        this(model, false);
    }

    public ToggleButtonModel(BooleanModel model, boolean invertModelState) {
        setModel(model, invertModelState);
    }

    @Override
    public boolean isSelected() {
        return (state & STATE_MASK_SELECTED) != 0;
    }

    @Override
    public void setSelected(boolean selected) {
        if(model != null) {
            model.setValue(selected ^ invertModelState);
        } else {
            setSelectedState(selected);
        }
    }

    @Override
    protected void buttonAction() {
        setSelected(!isSelected());
        super.buttonAction();
    }
    
    public BooleanModel getModel() {
        return model;
    }

    public void setModel(BooleanModel model) {
        setModel(model, false);
    }
    
    public void setModel(BooleanModel model, boolean invertModelState) {
        this.invertModelState = invertModelState;
        if(this.model != model) {
            removeModelCallback();
            this.model = model;
            addModelCallback();
        }
        if(model != null) {
            syncWithModel();
        }
    }

    public boolean isInvertModelState() {
        return invertModelState;
    }

    void syncWithModel() {
        setSelectedState(model.getValue() ^ invertModelState);
    }

    @Override
    public void connect() {
        isConnected = true;
        addModelCallback();
    }

    @Override
    public void disconnect() {
        isConnected = false;
        removeModelCallback();
    }

    private void addModelCallback() {
        if(model != null && isConnected) {
            if(modelCallback == null) {
                modelCallback = new ModelCallback();
            }
            model.addCallback(modelCallback);
            syncWithModel();
        }
    }

    private void removeModelCallback() {
        if(model != null && modelCallback != null) {
            model.removeCallback(modelCallback);
        }
    }

    private void setSelectedState(boolean selected) {
        if(selected != isSelected()) {
            setStateBit(STATE_MASK_SELECTED, selected);
            fireStateCallback();
        }
    }
    
    class ModelCallback implements Runnable {
        ModelCallback() {
        }
        public void run() {
            syncWithModel();
        }
    }
}
