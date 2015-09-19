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

import de.matthiasmann.twl.utils.CallbackSupport;

/**
 * A simple button model.
 * Supported state bit: hover, armed, pressed.
 * 
 * @author Matthias Mann
 */
public class SimpleButtonModel implements ButtonModel {

    protected static final int STATE_MASK_HOVER    = 1;
    protected static final int STATE_MASK_PRESSED  = 2;
    protected static final int STATE_MASK_ARMED    = 4;
    protected static final int STATE_MASK_DISABLED = 8;
    
    protected Runnable[] actionCallbacks;
    protected Runnable[] stateCallbacks;
    protected int state;
    
    public boolean isSelected() {
        return false;
    }

    public boolean isPressed() {
        return (state & STATE_MASK_PRESSED) != 0;
    }

    public boolean isArmed() {
        return (state & STATE_MASK_ARMED) != 0;
    }

    public boolean isHover() {
        return (state & STATE_MASK_HOVER) != 0;
    }

    public boolean isEnabled() {
        // !Caution! negated logic
        return (state & STATE_MASK_DISABLED) == 0;
    }

    public void setSelected(boolean selected) {
    }

    public void setPressed(boolean pressed) {
        if(pressed != isPressed()) {
            boolean fireAction = !pressed && isArmed() && isEnabled();
            setStateBit(STATE_MASK_PRESSED, pressed);
            fireStateCallback();
            if(fireAction) {
                buttonAction();
            }
        }
    }

    public void setArmed(boolean armed) {
        if(armed != isArmed()) {
            setStateBit(STATE_MASK_ARMED, armed);
            fireStateCallback();
        }
    }

    public void setHover(boolean hover) {
        if(hover != isHover()) {
            setStateBit(STATE_MASK_HOVER, hover);
            fireStateCallback();
        }
    }

    public void setEnabled(boolean enabled) {
        if(enabled != isEnabled()) {
            setStateBit(STATE_MASK_DISABLED, !enabled);
            fireStateCallback();
        }
    }

    protected void buttonAction() {
        fireActionCallback();
    }

    protected void setStateBit(int mask, boolean set) {
        if(set) {
            state |= mask;
        } else {
            state &= ~mask;
        }
    }

    protected void fireStateCallback() {
        CallbackSupport.fireCallbacks(stateCallbacks);
    }

    public void fireActionCallback() {
        CallbackSupport.fireCallbacks(actionCallbacks);
    }

    public void addActionCallback(Runnable callback) {
        actionCallbacks = CallbackSupport.addCallbackToList(actionCallbacks, callback, Runnable.class);
    }

    public void removeActionCallback(Runnable callback) {
        actionCallbacks = CallbackSupport.removeCallbackFromList(actionCallbacks, callback);
    }

    public boolean hasActionCallbacks() {
        return actionCallbacks != null;
    }

    public void addStateCallback(Runnable callback) {
        stateCallbacks = CallbackSupport.addCallbackToList(stateCallbacks, callback, Runnable.class);
    }

    public void removeStateCallback(Runnable callback) {
        stateCallbacks = CallbackSupport.removeCallbackFromList(stateCallbacks, callback);
    }

    public void connect() {
    }

    public void disconnect() {
    }
    
}
