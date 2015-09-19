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

/**
 * A callback to invoke an action on either a Widget's actionMap or
 * on an directly passed action map.
 * 
 * @author Matthias Mann
 */
public final class ActionCallback implements Runnable {
    
    private final Widget widget;
    private final ActionMap actionMap;
    private final String action;

    /**
     * Creates a callback invoking an action on the widget's actionMap.
     * If the widget has no actionMap then no action is performed.
     * 
     * @param widget the widget
     * @param action the action
     * @throws NullPointerException if either widget or action is null
     * @see Widget#getActionMap() 
     */
    public ActionCallback(Widget widget, String action) {
        if(widget == null) {
            throw new NullPointerException("widget");
        }
        if(action == null) {
            throw new NullPointerException("action");
        }
        this.widget = widget;
        this.actionMap = null;
        this.action = action;
    }

    /**
     * Creates a callback invoking an action on actionMap.
     * 
     * @param actionMap the actionMap to use
     * @param action the action
     * @throws NullPointerException if either actionMap or action is null
     */
    public ActionCallback(ActionMap actionMap, String action) {
        if(actionMap == null) {
            throw new NullPointerException("actionMap");
        }
        if(action == null) {
            throw new NullPointerException("action");
        }
        this.widget = null;
        this.actionMap = actionMap;
        this.action = action;
    }

    public void run() {
        ActionMap am = actionMap;
        if(am == null) {
            am = widget.getActionMap();
            if(am == null) {
                return;
            }
        }
        am.invokeDirect(action);
    }
}
