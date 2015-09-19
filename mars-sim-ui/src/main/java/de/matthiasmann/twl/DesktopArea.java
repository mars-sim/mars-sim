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

/**
 * A widget which reorders it's child when they receive focus.
 * <p>This widget's main purpose is to act as a container for
 * {@link ResizableFrame}.</p>
 * <p>This widget has the {@link #setFocusKeyEnabled(boolean) } disabled by
 * default so that the focus cycles within the current active child.</p>
 * 
 * @author Matthias Mann
 */
public class DesktopArea extends Widget {

    public DesktopArea() {
        setFocusKeyEnabled(false);
    }

    @Override
    protected void keyboardFocusChildChanged(Widget child) {
        super.keyboardFocusChildChanged(child);
        if(child != null) {
            int fromIdx = getChildIndex(child);
            assert fromIdx >= 0;
            int numChildren = getNumChildren();
            if(fromIdx < numChildren - 1) {
                moveChild(fromIdx, numChildren - 1);
            }
        }
    }

    @Override
    protected void layout() {
        // make sure that all children are still inside
        restrictChildrenToInnerArea();
    }

    protected void restrictChildrenToInnerArea() {
        final int top = getInnerY();
        final int left = getInnerX();
        final int right = getInnerRight();
        final int bottom = getInnerBottom();
        final int width = Math.max(0, right-left);
        final int height = Math.max(0, bottom-top);

        for(int i=0,n=getNumChildren() ; i<n ; i++) {
            Widget w = getChild(i);
            w.setSize(
                    Math.min(Math.max(width, w.getMinWidth()), w.getWidth()),
                    Math.min(Math.max(height, w.getMinHeight()), w.getHeight()));
            w.setPosition(
                    Math.max(left, Math.min(right - w.getWidth(), w.getX())),
                    Math.max(top, Math.min(bottom - w.getHeight(), w.getY())));
        }
    }

}
