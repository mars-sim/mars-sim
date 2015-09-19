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

/**
 * A info window. It can interact with the mouse but not keyboard.
 * Only one can be visible at any time. It is designed to provide additional
 * information for the user while editing a text.
 *
 * @author Matthias Mann
 */
public class InfoWindow extends Container {

    private final Widget owner;

    public InfoWindow(Widget owner) {
        if(owner == null) {
            throw new NullPointerException("owner");
        }
        
        this.owner = owner;
    }

    public Widget getOwner() {
        return owner;
    }

    public boolean isOpen() {
        return getParent() != null;
    }
    
    public boolean openInfo() {
        if(getParent() != null) {
            return true;
        }
        if(isParentInfoWindow(owner)) {
            return false;
        }
        GUI gui = owner.getGUI();
        if(gui != null) {
            gui.openInfo(this);
            focusFirstChild();
            return true;
        }
        return false;
    }

    public void closeInfo() {
        GUI gui = getGUI();
        if(gui != null) {
            gui.closeInfo(this);
        }
    }

    /**
     * Called after the info window has been closed
     */
    protected void infoWindowClosed() {
    }

    private static boolean isParentInfoWindow(Widget w) {
        while(w != null) {
            if(w instanceof InfoWindow) {
                return true;
            }
            w = w.getParent();
        }
        return false;
    }
}
