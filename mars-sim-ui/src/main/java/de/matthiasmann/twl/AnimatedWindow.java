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
package de.matthiasmann.twl;

import de.matthiasmann.twl.utils.CallbackSupport;
import de.matthiasmann.twl.model.BooleanModel;

/**
 * A simple animated window - it changes size
 *
 * @author Matthias Mann
 */
public class AnimatedWindow extends Widget {

    private int numAnimSteps = 10;
    private int currentStep;
    private int animSpeed;

    private BooleanModel model;
    private Runnable modelCallback;
    private Runnable[] callbacks;
    
    public AnimatedWindow() {
        setVisible(false); // we start closed
    }

    public void addCallback(Runnable cb) {
        callbacks = CallbackSupport.addCallbackToList(callbacks, cb, Runnable.class);
    }

    public void removeCallback(Runnable cb) {
        callbacks = CallbackSupport.removeCallbackFromList(callbacks, cb);
    }

    private void doCallback() {
        CallbackSupport.fireCallbacks(callbacks);
    }

    public int getNumAnimSteps() {
        return numAnimSteps;
    }

    public void setNumAnimSteps(int numAnimSteps) {
        if(numAnimSteps < 1) {
            throw new IllegalArgumentException("numAnimSteps");
        }
        this.numAnimSteps = numAnimSteps;
    }

    public void setState(boolean open) {
        if(open && !isOpen()) {
            animSpeed = 1;
            setVisible(true);
            doCallback();
        } else if(!open && !isClosed()) {
            animSpeed = -1;
            doCallback();
        }
        if(model != null) {
            model.setValue(open);
        }
    }
    
    public BooleanModel getModel() {
        return model;
    }

    public void setModel(BooleanModel model) {
        if(this.model != model) {
            if(this.model != null) {
                this.model.removeCallback(modelCallback);
            }
            this.model = model;
            if(model != null) {
                if(modelCallback == null) {
                    modelCallback = new ModelCallback();
                }
                model.addCallback(modelCallback);
                syncWithModel();
            }
        }
    }
    
    public boolean isOpen() {
        return currentStep == numAnimSteps && animSpeed >= 0;
    }
    
    public boolean isOpening() {
        return animSpeed > 0;
    }
    
    public boolean isClosed() {
        return currentStep == 0 && animSpeed <= 0;
    }
    
    public boolean isClosing() {
        return animSpeed < 0;
    }
    
    public boolean isAnimating() {
        return animSpeed != 0;
    }

    @Override
    public boolean handleEvent(Event evt) {
        if(isOpen()) {
            if(super.handleEvent(evt)) {
                return true;
            }
            if(evt.isKeyPressedEvent()) {
                switch (evt.getKeyCode()) {
                case Event.KEY_ESCAPE:
                    setState(false);
                    return true;
                default:
                    break;
                }
            }
            return false;
        }
        if(isClosed()) {
            return false;
        }
        // eat every event when we animate
        int mouseX = evt.getMouseX() - getX();
        int mouseY = evt.getMouseY() - getY();
        return mouseX >= 0 && mouseX < getAnimatedWidth() &&
                mouseY >= 0 && mouseY < getAnimatedHeight();
    }

    @Override
    public int getMinWidth() {
        int minWidth = 0;
        for(int i=0,n=getNumChildren() ; i<n ; i++) {
            Widget child = getChild(i);
            minWidth = Math.max(minWidth, child.getMinWidth());
        }
        return Math.max(super.getMinWidth(), minWidth + getBorderHorizontal());
    }

    @Override
    public int getMinHeight() {
        int minHeight = 0;
        for(int i=0,n=getNumChildren() ; i<n ; i++) {
            Widget child = getChild(i);
            minHeight = Math.max(minHeight, child.getMinHeight());
        }
        return Math.max(super.getMinHeight(), minHeight + getBorderVertical());
    }

    @Override
    public int getPreferredInnerWidth() {
        return BoxLayout.computePreferredWidthVertical(this);
    }

    @Override
    public int getPreferredInnerHeight() {
        return BoxLayout.computePreferredHeightHorizontal(this);
    }

    @Override
    protected void layout() {
        layoutChildrenFullInnerArea();
    }
    
    @Override
    protected void paint(GUI gui) {
        if(animSpeed != 0) {
            animate();
        }
        
        if(isOpen()) {
            super.paint(gui);
        } else if(!isClosed() && getBackground() != null) {
            getBackground().draw(getAnimationState(),
                    getX(), getY(), getAnimatedWidth(), getAnimatedHeight());
        }
    }

    private void animate() {
        currentStep += animSpeed;
        if(currentStep == 0 || currentStep == numAnimSteps) {
            setVisible(currentStep > 0);
            animSpeed = 0;
            doCallback();
        }
    }

    private int getAnimatedWidth() {
        return getWidth() * currentStep / numAnimSteps;
    }

    private int getAnimatedHeight() {
        return getHeight() * currentStep / numAnimSteps;
    }
    
    void syncWithModel() {
        setState(model.getValue());
    }
    
    class ModelCallback implements Runnable {
        ModelCallback() {
        }

        public void run() {
            syncWithModel();
        }
    }
}
