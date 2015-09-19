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

import de.matthiasmann.twl.model.ButtonModel;
import java.util.ArrayList;

/**
 * A radial popup menu with round buttons
 * 
 * @author Matthias Mann
 */
public class RadialPopupMenu extends PopupWindow {

    private final ArrayList<RoundButton> buttons;

    private int radius;
    private int buttonRadius;
    private int mouseButton;
    int buttonRadiusSqr;

    public RadialPopupMenu(Widget owner) {
        super(owner);
        this.buttons = new ArrayList<RoundButton>();
    }

    public int getButtonRadius() {
        return buttonRadius;
    }

    public void setButtonRadius(int buttonRadius) {
        if(buttonRadius < 0) {
            throw new IllegalArgumentException("buttonRadius");
        }
        this.buttonRadius = buttonRadius;
        this.buttonRadiusSqr = buttonRadius * buttonRadius;
        invalidateLayout();
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        if(radius < 0) {
            throw new IllegalArgumentException("radius");
        }
        this.radius = radius;
        invalidateLayout();
    }

    public int getMouseButton() {
        return mouseButton;
    }

    /**
     * Sets the mouse button to which the buttons of this radial menu should react.
     * The default is {@link Event#MOUSE_LBUTTON}
     * @param mouseButton the mouse button
     */
    public void setMouseButton(int mouseButton) {
        if(mouseButton < Event.MOUSE_LBUTTON || mouseButton > Event.MOUSE_RBUTTON) {
            throw new IllegalArgumentException("mouseButton");
        }
        this.mouseButton = mouseButton;
        for(int i=0,n=buttons.size() ; i<n ; i++) {
            buttons.get(i).setMouseButton(mouseButton);
        }
    }

    public Button addButton(String theme, Runnable cb) {
        RoundButton button = new RoundButton();
        button.setTheme(theme);
        button.addCallback(cb);
        button.setMouseButton(mouseButton);
        addButton(button);
        return button;
    }

    public void removeButton(Button btn) {
        int idx = buttons.indexOf(btn);
        if(idx >= 0) {
            buttons.remove(idx);
            removeChild(btn);
        }
    }

    protected void addButton(RoundButton button) {
        if(button == null) {
            throw new NullPointerException("button");
        }
        buttons.add(button);
        add(button);
    }

    @Override
    public boolean openPopup() {
        if(super.openPopup()) {
            if(bindMouseDrag(new Runnable() {
                public void run() {
                    boundDragEventFinished();
                }
            })) {
                setAllButtonsPressed();
            }
            return true;
        }
        return false;
    }

    /**
     * Opens the radial popup menu around the specified coordinate
     *
     * @param centerX the X coordinate of the popup center
     * @param centerY the Y coordinate of the popup center
     * @return true if the popup was opened
     */
    public boolean openPopupAt(int centerX, int centerY) {
        if(openPopup()) {
            adjustSize();
            Widget parent = getParent();
            int width = getWidth();
            int height = getHeight();
            setPosition(
                    limit(centerX - width/2, parent.getInnerX(), parent.getInnerRight() - width),
                    limit(centerY - height/2, parent.getInnerY(), parent.getInnerBottom() - height));
            return true;
        }
        return false;
    }
    
    protected static int limit(int value, int min, int max) {
        if(value < min) {
            return min;
        }
        if(value > max) {
            return max;
        }
        return value;
    }

    /**
     * Opens the radial popup menu around the current mouse position
     * and uses the event's mouse button as button activator.
     *
     * @param evt the {@link Event.Type#MOUSE_BTNDOWN} event
     * @return true if the popup was opened
     */
    public boolean openPopup(Event evt) {
        if(evt.getType() == Event.Type.MOUSE_BTNDOWN) {
            setMouseButton(evt.getMouseButton());
            return openPopupAt(evt.getMouseX(), evt.getMouseY());
        }
        return false;
    }

    @Override
    public int getPreferredInnerWidth() {
        return 2*(radius + buttonRadius);
    }

    @Override
    public int getPreferredInnerHeight() {
        return 2*(radius + buttonRadius);
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        applyThemeRadialPopupMenu(themeInfo);
    }

    protected void applyThemeRadialPopupMenu(ThemeInfo themeInfo) {
        setRadius(themeInfo.getParameter("radius", 40));
        setButtonRadius(themeInfo.getParameter("buttonRadius", 40));
    }

    @Override
    protected void layout() {
        layoutRadial();
    }

    protected void layoutRadial() {
        int numButtons = buttons.size();
        if(numButtons > 0) {
            int centerX = getInnerX() + getInnerWidth()/2;
            int centerY = getInnerY() + getInnerHeight()/2;
            float toRad = (float)(2.0*Math.PI) / numButtons;
            for(int i=0 ; i<numButtons ; i++) {
                float rad = i * toRad;
                int btnCenterX = centerX + (int)(radius * Math.sin(rad));
                int btnCenterY = centerY - (int)(radius * Math.cos(rad));
                RoundButton button = buttons.get(i);
                button.setPosition(btnCenterX - buttonRadius, btnCenterY - buttonRadius);
                button.setSize(2*buttonRadius, 2*buttonRadius);
            }
        }
    }

    protected void setAllButtonsPressed() {
        for(int i=0,n=buttons.size() ; i<n ; i++) {
            ButtonModel model = buttons.get(i).getModel();
            model.setPressed(true);
            model.setArmed(model.isHover());
        }
    }

    protected void boundDragEventFinished() {
        closePopup();
    }

    protected class RoundButton extends Button {
        @Override
        public boolean isInside(int x, int y) {
            int dx = x - (getX() + getWidth()/2);
            int dy = y - (getY() + getHeight()/2);
            return dx*dx + dy*dy <= buttonRadiusSqr;
        }
    }
}
