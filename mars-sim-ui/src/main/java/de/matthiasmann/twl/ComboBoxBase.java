/*
 * Copyright (c) 2008-2013, Matthias Mann
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

import de.matthiasmann.twl.renderer.AnimationState.StateKey;

/**
 * base class for drop down comboboxes.
 *
 * Manages layout of label and button and opening the popup.
 * Subclasses have to create and add the label, and add the popup content.
 *
 * @author Matthias Mann
 */
public abstract class ComboBoxBase extends Widget {

    public static final StateKey STATE_COMBOBOX_KEYBOARD_FOCUS = StateKey.get("comboboxKeyboardFocus");
    
    protected final Button button;
    protected final PopupWindow popup;
    
    protected ComboBoxBase() {
        this.button = new Button(getAnimationState());
        this.popup = new PopupWindow(this) {
            @Override
            protected void escapePressed(Event evt) {
                ComboBoxBase.this.popupEscapePressed(evt);
            }
        };

        button.addCallback(new Runnable() {
            public void run() {
                openPopup();
            }
        });

        add(button);
        setCanAcceptKeyboardFocus(true);
        setDepthFocusTraversal(false);
    }
    
    protected abstract Widget getLabel();

    protected boolean openPopup() {
        if(popup.openPopup()) {
            setPopupSize();
            return true;
        }
        return false;
    }

    @Override
    public int getPreferredInnerWidth() {
        return getLabel().getPreferredWidth() + button.getPreferredWidth();
    }

    @Override
    public int getPreferredInnerHeight() {
        return Math.max(getLabel().getPreferredHeight(), button.getPreferredHeight());
    }

    @Override
    public int getMinWidth() {
        int minWidth = super.getMinWidth();
        minWidth = Math.max(minWidth, getLabel().getMinWidth() + button.getMinWidth());
        return minWidth;
    }

    @Override
    public int getMinHeight() {
        int minInnerHeight = Math.max(getLabel().getMinHeight(), button.getMinHeight());
        return Math.max(super.getMinHeight(), minInnerHeight + getBorderVertical());
    }

    protected void setPopupSize() {
        int minHeight = popup.getMinHeight();
        int popupHeight = computeSize(minHeight,
                popup.getPreferredHeight(),
                popup.getMaxHeight());
        int popupMaxBottom = popup.getParent().getInnerBottom();
        if(getBottom() + minHeight > popupMaxBottom) {
            if(getY() - popupHeight >= popup.getParent().getInnerY()) {
                popup.setPosition(getX(), getY() - popupHeight);
            } else {
                popup.setPosition(getX(), popupMaxBottom - minHeight);
            }
        } else {
            popup.setPosition(getX(), getBottom());
        }
        popupHeight = Math.min(popupHeight, popupMaxBottom - popup.getY());
        popup.setSize(getWidth(), popupHeight);
    }

    @Override
    protected void layout() {
        int btnWidth = button.getPreferredWidth();
        int innerHeight = getInnerHeight();
        int innerX = getInnerX();
        int innerY = getInnerY();
        button.setPosition(getInnerRight() - btnWidth, innerY);
        button.setSize(btnWidth, innerHeight);
        getLabel().setPosition(innerX, innerY);
        getLabel().setSize(Math.max(0, button.getX() - innerX), innerHeight);
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        if(popup.isOpen()) {
            setPopupSize();
        }
    }

    private static void setRecursive(Widget w, StateKey what, boolean state) {
        w.getAnimationState().setAnimationState(what, state);
        for(int i=0 ; i<w.getNumChildren() ; ++i) {
            Widget child = w.getChild(i);
            setRecursive(child, what, state);
        }
    }

    @Override
    protected void keyboardFocusGained() {
        super.keyboardFocusGained();
        setRecursive(getLabel(), STATE_COMBOBOX_KEYBOARD_FOCUS, true);
    }

    @Override
    protected void keyboardFocusLost() {
        super.keyboardFocusLost();
        setRecursive(getLabel(), STATE_COMBOBOX_KEYBOARD_FOCUS, false);
    }
    
    /**
     * Called when the escape key is pressed in the open popup.
     * 
     * The default implementation closes the popup.
     * 
     * @param evt the event
     */
    protected void popupEscapePressed(Event evt) {
        popup.closePopup();
    }
}
