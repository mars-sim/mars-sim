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
 *
 * @author Matthias Mann
 */
public class MenuAction extends MenuElement {

    private Runnable cb;

    public MenuAction() {
    }

    public MenuAction(Runnable cb) {
        this.cb = cb;
    }

    /**
     * Creates a menu action which displays the given name and invokes the
     * specified callback when activated.
     * 
     * @param name the name/text of the menu action
     * @param cb the callback to invoke
     * @see #setCallback(java.lang.Runnable) 
     */
    public MenuAction(String name, Runnable cb) {
        super(name);
        this.cb = cb;
    }

    public Runnable getCallback() {
        return cb;
    }

    /**
     * Sets the callback to invoke when the menu action is triggered.
     * 
     * <p>this callback is invoked after the menu is closed.</p>
     * 
     * @param cb the callback (can be null)
     */
    public void setCallback(Runnable cb) {
        this.cb = cb;
    }

    @Override
    protected Widget createMenuWidget(MenuManager mm, int level) {
        Button b = new MenuBtn();
        setWidgetTheme(b, "button");

        b.addCallback(mm.getCloseCallback());
        
        if(cb != null) {
            b.addCallback(cb);
        }
        
        return b;
    }

}
