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

import java.util.IdentityHashMap;

/**
 *
 * @author Matthias Mann
 */
public class MenuManager extends PopupWindow {

    private final boolean isMenuBar;
    private final IdentityHashMap<MenuElement, Widget> popups;
    private final Runnable closeCB;
    private final Runnable timerCB;

    private boolean mouseOverOwner;
    private Widget lastMouseOverWidget;
    private Timer timer;

    public MenuManager(Widget owner, boolean isMenuBar) {
        super(owner);
        this.isMenuBar = isMenuBar;
        this.popups = new IdentityHashMap<MenuElement, Widget>();
        this.closeCB = new Runnable() {
            public void run() {
                closePopup();
            }
        };
        this.timerCB = new Runnable() {
            public void run() {
                popupTimer();
            }
        };
    }

    public Runnable getCloseCallback() {
        return closeCB;
    }

    boolean isSubMenuOpen(Menu menu) {
        Widget popup = popups.get(menu);
        if(popup != null) {
            return popup.getParent() == this;
        }
        return false;
    }

    void closeSubMenu(int level) {
        while(getNumChildren() > level) {
            closeSubMenu();
        }
    }
    
    Widget openSubMenu(int level, Menu menu, Widget btn, boolean setPosition) {
        Widget popup = popups.get(menu);
        if(popup == null) {
            popup = menu.createPopup(this, level+1, btn);
            popups.put(menu, popup);
        }

        if(popup.getParent() == this) {
            closeSubMenu(level+1);
            return popup;
        }

        if(!isOpen()) {
            if(!openPopup()) {
                closePopup();
                return null;
            }
            getParent().layoutChildFullInnerArea(this);
        }

        while(getNumChildren() > level) {
            closeSubMenu();
        }
        add(popup);

        popup.adjustSize();

        if(setPosition) {
            int popupWidth = popup.getWidth();
            int popupX = btn.getRight();
            int popupY = btn.getY();

            if(level == 0) {
                popupX = btn.getX();
                popupY = btn.getBottom();
            }

            if(popupWidth + btn.getRight() > getInnerRight()) {
                popupX = btn.getX() - popupWidth;
                if(popupX < getInnerX()) {
                    popupX = getInnerRight() - popupWidth;
                }
            }

            int popupHeight = popup.getHeight();
            if(popupY + popupHeight > getInnerBottom()) {
                popupY = Math.max(getInnerY(), getInnerBottom() - popupHeight);
            }

            popup.setPosition(popupX, popupY);
        }

        return popup;
    }
    
    void closeSubMenu() {
        removeChild(getNumChildren() - 1);
    }

    @Override
    public void closePopup() {
        stopTimer();
        GUI gui = getGUI();
        super.closePopup();
        removeAllChildren();
        popups.clear();
        if(gui != null) {
            gui.resendLastMouseMove();
        }
    }
    
    /**
     * Returns the popup widget for the specified menu
     * @param menu the menu for which to return the popup
     * @return the popup widget or null if not open
     */
    public Widget getPopupForMenu(Menu menu) {
        return popups.get(menu);
    }

    @Override
    protected void afterAddToGUI(GUI gui) {
        super.afterAddToGUI(gui);
        timer = gui.createTimer();
        timer.setDelay(300);
        timer.setCallback(timerCB);
    }

    @Override
    protected void layout() {
    }

    @Override
    Widget routeMouseEvent(Event evt) {
        mouseOverOwner = false;
        Widget widget = super.routeMouseEvent(evt);
        if(widget == this && isMenuBar && getOwner().isMouseInside(evt)) {
            Widget menuBarWidget = getOwner().routeMouseEvent(evt);
            if(menuBarWidget != null) {
                mouseOverOwner = true;
                widget = menuBarWidget;
            }
        }

        Widget mouseOverWidget = getWidgetUnderMouse();
        if(lastMouseOverWidget != mouseOverWidget) {
            lastMouseOverWidget = mouseOverWidget;
            if(isMenuBar && widget.getParent() == getOwner() && (widget instanceof Menu.SubMenuBtn)) {
                popupTimer();   // no delay on menu bar itself
            } else {
                startTimer();
            }
        }
        
        return widget;
    }

    @Override
    protected boolean handleEventPopup(Event evt) {
        if(isMenuBar && getOwner().handleEvent(evt)) {
            return true;
        }
        if(super.handleEventPopup(evt)) {
            return true;
        }
        if(evt.getType() == Event.Type.MOUSE_CLICKED) {
            mouseClickedOutside(evt);
            return true;
        }
        return false;
    }

    @Override
    Widget getWidgetUnderMouse() {
        if(mouseOverOwner) {
            return getOwner().getWidgetUnderMouse();
        }
        return super.getWidgetUnderMouse();
    }

    void popupTimer() {
        if((lastMouseOverWidget instanceof Menu.SubMenuBtn) && lastMouseOverWidget.isEnabled()) {
            ((Menu.SubMenuBtn)lastMouseOverWidget).run();
        } else if(lastMouseOverWidget != this) {
            int level = 0;
            // search for the MenuPopup containing this widget
            // it knows which menu level we need to close
            for(Widget w=lastMouseOverWidget ; w!=null ; w=w.getParent()) {
                if(w instanceof Menu.MenuPopup) {
                    level = ((Menu.MenuPopup)w).level;
                    break;
                }
            }
            closeSubMenu(level);
        }
    }

    void startTimer() {
        if(timer != null) {
            timer.stop();
            timer.start();
        }
    }
    
    void stopTimer() {
        if(timer != null) {
            timer.stop();
        }
    }
}
