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

import de.matthiasmann.twl.model.IntegerModel;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.utils.CallbackSupport;

/**
 * A scroll bar.<br/>
 * <br/>
 * The Scrollbar supports two optional image parameters which can be used
 * to color the track on up/left and right/bottom side of the thumb differently:
 * <ul>
 * <li>trackImageUp (vertical only)</li>
 * <li>trackImageLeft (horizontal only)</li>
 * <li>trackImageRight (horizontal only)</li>
 * <li>trackImageBottom (vertical only)</li>
 * </ul>
 * These image are drawn in the inner widget area up the the edge of the thumb.
 * use inset to exclude/include more area.
 *
 * @author Matthias Mann
 */
public class Scrollbar extends Widget {

    public enum Orientation {
        HORIZONTAL,
        VERTICAL
    };
    
    private static final int INITIAL_DELAY = 300;
    private static final int REPEAT_DELAY = 75;

    private final Orientation orientation;
    private final Button btnUpLeft;
    private final Button btnDownRight;
    private final DraggableButton thumb;
    private final L dragTimerCB;
    private Timer timer;
    private int trackClicked;
    private int trackClickLimit;
    private Runnable[] callbacks;
    private Image trackImageUpLeft;
    private Image trackImageDownRight;
    private IntegerModel model;
    private Runnable modelCB;

    private int pageSize;
    private int stepSize;
    private boolean scaleThumb;
    
    private int minValue;
    private int maxValue;
    private int value;
    
    public Scrollbar() {
        this(Orientation.VERTICAL);
    }

    public Scrollbar(Orientation orientation) {
        this.orientation  = orientation;
        this.btnUpLeft    = new Button();
        this.btnDownRight = new Button();
        this.thumb        = new DraggableButton();

        Runnable cbUpdateTimer = new Runnable() {
            public void run() {
                updateTimer();
            }
        };

        if(orientation == Orientation.HORIZONTAL) {
            setTheme("hscrollbar");
            btnUpLeft.setTheme("leftbutton");
            btnDownRight.setTheme("rightbutton");
        } else {
            setTheme("vscrollbar");
            btnUpLeft.setTheme("upbutton");
            btnDownRight.setTheme("downbutton");
        }

        dragTimerCB = new L();

        btnUpLeft.setCanAcceptKeyboardFocus(false);
        btnUpLeft.getModel().addStateCallback(cbUpdateTimer);
        btnDownRight.setCanAcceptKeyboardFocus(false);
        btnDownRight.getModel().addStateCallback(cbUpdateTimer);
        thumb.setCanAcceptKeyboardFocus(false);
        thumb.setTheme("thumb");
        thumb.setListener(dragTimerCB);
        
        add(btnUpLeft);
        add(btnDownRight);
        add(thumb);
        
        this.pageSize = 10;
        this.stepSize = 1;
        this.maxValue = 100;
        
        setSize(30, 200);
        setDepthFocusTraversal(false);
    }

    public void addCallback(Runnable cb) {
        callbacks = CallbackSupport.addCallbackToList(callbacks, cb, Runnable.class);
    }

    public void removeCallback(Runnable cb) {
        callbacks = CallbackSupport.removeCallbackFromList(callbacks, cb);
    }

    protected void doCallback() {
        CallbackSupport.fireCallbacks(callbacks);
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public IntegerModel getModel() {
        return model;
    }

    public void setModel(IntegerModel model) {
        if(this.model != model) {
            if(this.model != null) {
                this.model.removeCallback(modelCB);
            }
            this.model = model;
            if(model != null) {
                if(modelCB == null) {
                    modelCB = new Runnable() {
                        public void run() {
                            syncModel();
                        }
                    };
                }
                model.addCallback(modelCB);
                syncModel();
            }
        }
    }

    public int getValue() {
        return value;
    }

    public void setValue(int current) {
        setValue(current, true);
    }

    public void setValue(int value, boolean fireCallbacks) {
        value = range(value);
        int oldValue = this.value;
        if(oldValue != value) {
            this.value = value;
            setThumbPos();
            firePropertyChange("value", oldValue, value);
            if(model != null) {
                model.setValue(value);
            }
            if(fireCallbacks) {
                doCallback();
            }
        }
    }
    
    public void scroll(int amount) {
        if(minValue < maxValue) {
            setValue(value + amount);
        } else {
            setValue(value - amount);
        }
    }

    /**
     * Tries to make the specified area completely visible. If it is larger
     * then the page size then it scrolls to the start of the area.
     * 
     * @param start the position of the area
     * @param size size of the area
     * @param extra the extra space which should be visible around the area
     */
    public void scrollToArea(int start, int size, int extra) {
        if(size <= 0) {
            return;
        }
        if(extra < 0) {
            extra = 0;
        }
        
        int end = start + size;
        start = range(start);
        int pos = value;

        int startWithExtra = range(start - extra);
        if(startWithExtra < pos) {
            pos = startWithExtra;
        }
        int pageEnd = pos + pageSize;
        int endWithExtra = end + extra;
        if(endWithExtra > pageEnd) {
            pos = range(endWithExtra - pageSize);
            if(pos > startWithExtra) {
                size = end - start;
                pos = start - Math.max(0, pageSize - size) / 2;
            }
        }
        
        setValue(pos);
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMinMaxValue(int minValue, int maxValue) {
        if(maxValue < minValue) {
            throw new IllegalArgumentException("maxValue < minValue");
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.value = range(value);
        setThumbPos();
        thumb.setVisible(minValue != maxValue);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        if(pageSize < 1) {
            throw new IllegalArgumentException("pageSize < 1");
        }
        this.pageSize = pageSize;
        if(scaleThumb) {
            setThumbPos();
        }
    }

    public int getStepSize() {
        return stepSize;
    }

    public void setStepSize(int stepSize) {
        if(stepSize < 1) {
            throw new IllegalArgumentException("stepSize < 1");
        }
        this.stepSize = stepSize;
    }

    public boolean isScaleThumb() {
        return scaleThumb;
    }

    public void setScaleThumb(boolean scaleThumb) {
        this.scaleThumb = scaleThumb;
        setThumbPos();
    }

    public void externalDragStart() {
        thumb.getAnimationState().setAnimationState(Button.STATE_PRESSED, true);
        dragTimerCB.dragStarted();
    }

    public void externalDragged(int deltaX, int deltaY) {
        dragTimerCB.dragged(deltaX, deltaY);
    }

    public void externalDragStopped() {
        // dragTimerCB.dragStopped(); (it's empty anyway)
        thumb.getAnimationState().setAnimationState(Button.STATE_PRESSED, false);
    }

    public boolean isUpLeftButtonArmed() {
        return btnUpLeft.getModel().isArmed();
    }

    public boolean isDownRightButtonArmed() {
        return btnDownRight.getModel().isArmed();
    }

    public boolean isThumbDragged() {
        return thumb.getModel().isPressed();
    }

    public void setThumbTooltipContent(Object tooltipContent) {
        thumb.setTooltipContent(tooltipContent);
    }

    public Object getThumbTooltipContent() {
        return thumb.getTooltipContent();
    }
    
    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        applyThemeScrollbar(themeInfo);
    }

    protected void applyThemeScrollbar(ThemeInfo themeInfo) {
        setScaleThumb(themeInfo.getParameter("scaleThumb", false));
        if(orientation == Orientation.HORIZONTAL) {
            trackImageUpLeft    = themeInfo.getParameterValue("trackImageLeft",  false, Image.class);
            trackImageDownRight = themeInfo.getParameterValue("trackImageRight", false, Image.class);
        } else {
            trackImageUpLeft    = themeInfo.getParameterValue("trackImageUp",   false, Image.class);
            trackImageDownRight = themeInfo.getParameterValue("trackImageDown", false, Image.class);
        }
    }

    @Override
    protected void paintWidget(GUI gui) {
        int x = getInnerX();
        int y = getInnerY();
        if(orientation == Orientation.HORIZONTAL) {
            int h = getInnerHeight();
            if(trackImageUpLeft != null) {
                trackImageUpLeft.draw(getAnimationState(), x, y, thumb.getX() - x, h);
            }
            if(trackImageDownRight != null) {
                int thumbRight = thumb.getRight();
                trackImageDownRight.draw(getAnimationState(), thumbRight, y, getInnerRight() - thumbRight, h);
            }
        } else {
            int w = getInnerWidth();
            if(trackImageUpLeft != null) {
                trackImageUpLeft.draw(getAnimationState(), x, y, w, thumb.getY() - y);
            }
            if(trackImageDownRight != null) {
                int thumbBottom = thumb.getBottom();
                trackImageDownRight.draw(getAnimationState(), x, thumbBottom, w, getInnerBottom() - thumbBottom);
            }
        }
    }

    @Override
    protected void afterAddToGUI(GUI gui) {
        super.afterAddToGUI(gui);
        timer = gui.createTimer();
        timer.setCallback(dragTimerCB);
        timer.setContinuous(true);
        if(model != null) {
            // modelCB is created when the model was set
            model.addCallback(modelCB);
        }
    }

    @Override
    protected void beforeRemoveFromGUI(GUI gui) {
        super.beforeRemoveFromGUI(gui);
        if(model != null) {
            model.removeCallback(modelCB);
        }
        if(timer != null) {
            timer.stop();
        }
        timer = null;
    }

    @Override
    public boolean handleEvent(Event evt) {
        if(evt.getType() == Event.Type.MOUSE_BTNUP &&
                evt.getMouseButton() == Event.MOUSE_LBUTTON) {
            trackClicked = 0;
            updateTimer();
        }

        if(!super.handleEvent(evt)) {
            if(evt.getType() == Event.Type.MOUSE_BTNDOWN &&
                    evt.getMouseButton() == Event.MOUSE_LBUTTON) {
                if(isMouseInside(evt)) {
                    if(orientation == Orientation.HORIZONTAL) {
                        trackClickLimit = evt.getMouseX();
                        if(evt.getMouseX() < thumb.getX()) {
                            trackClicked = -1;
                        } else {
                            trackClicked = 1;
                        }
                    } else {
                        trackClickLimit = evt.getMouseY();
                        if(evt.getMouseY() < thumb.getY()) {
                            trackClicked = -1;
                        } else {
                            trackClicked = 1;
                        }
                    }
                    updateTimer();
                }
            }
        }

        boolean page = (evt.getModifiers() & Event.MODIFIER_CTRL) != 0;
        int step = page ? pageSize : stepSize;

        if(evt.getType() == Event.Type.KEY_PRESSED) {
            switch(evt.getKeyCode()) {
            case Event.KEY_LEFT:
                if(orientation == Orientation.HORIZONTAL) {
                    setValue(value - step);
                    return true;
                }
                break;
            case Event.KEY_RIGHT:
                if(orientation == Orientation.HORIZONTAL) {
                    setValue(value + step);
                    return true;
                }
                break;
            case Event.KEY_UP:
                if(orientation == Orientation.VERTICAL) {
                    setValue(value - step);
                    return true;
                }
                break;
            case Event.KEY_DOWN:
                if(orientation == Orientation.VERTICAL) {
                    setValue(value + step);
                    return true;
                }
                break;
            case Event.KEY_PRIOR:
                if(orientation == Orientation.VERTICAL) {
                    setValue(value - pageSize);
                    return true;
                }
                break;
            case Event.KEY_NEXT:
                if(orientation == Orientation.VERTICAL) {
                    setValue(value + pageSize);
                    return true;
                }
                break;
            }
        }
        
        if(evt.getType() == Event.Type.MOUSE_WHEEL) {
            setValue(value - step * evt.getMouseWheelDelta());
        }

        // eat all mouse events
        return evt.isMouseEvent();
    }

    int range(int current) {
        if(minValue < maxValue) {
            if(current < minValue) {
                current = minValue;
            } else if(current > maxValue) {
                current = maxValue;
            }
        } else {
            if(current > minValue) {
                current = minValue;
            } else if(current < maxValue) {
                current = maxValue;
            }
        }
        return current;
    }
    
    void onTimer(int nextDelay) {
        timer.setDelay(nextDelay);
        if(trackClicked != 0) {
            int thumbPos;
            if(orientation == Orientation.HORIZONTAL) {
                thumbPos = thumb.getX();
            } else {
                thumbPos = thumb.getY();
            }
            if((trackClickLimit - thumbPos) * trackClicked > 0) {
                scroll(trackClicked * pageSize);
            }
        } else if(btnUpLeft.getModel().isArmed()) {
            scroll(-stepSize);
        } else if(btnDownRight.getModel().isArmed()) {
            scroll(stepSize);
        }
    }
    
    void updateTimer() {
        if(timer != null) {
            if(trackClicked != 0 ||
                    btnUpLeft.getModel().isArmed() ||
                    btnDownRight.getModel().isArmed()) {
                if(!timer.isRunning()) {
                    onTimer(INITIAL_DELAY);
                    // onTimer() can call setValue() which calls user code
                    // that user code could potentially remove the Scrollbar from GUI
                    if(timer != null) {
                        timer.start();
                    }
                }
            } else {
                timer.stop();
            }
        }
    }
    
    void syncModel() {
        setMinMaxValue(model.getMinValue(), model.getMaxValue());
        setValue(model.getValue());
    }

    @Override
    public int getMinWidth() {
        if(orientation == Orientation.HORIZONTAL) {
            return Math.max(super.getMinWidth(), btnUpLeft.getMinWidth() + thumb.getMinWidth() + btnDownRight.getMinWidth());
        } else {
            return Math.max(super.getMinWidth(), thumb.getMinWidth());
        }
    }

    @Override
    public int getMinHeight() {
        if(orientation == Orientation.HORIZONTAL) {
            return Math.max(super.getMinHeight(), thumb.getMinHeight());
        } else {
            return Math.max(super.getMinHeight(), btnUpLeft.getMinHeight() + thumb.getMinHeight() + btnDownRight.getMinHeight());
        }
    }

    @Override
    public int getPreferredWidth() {
        return getMinWidth();
    }

    @Override
    public int getPreferredHeight() {
        return getMinHeight();
    }
    
    @Override
    protected void layout() {
        if(orientation == Orientation.HORIZONTAL) {
            btnUpLeft.setSize(btnUpLeft.getPreferredWidth(), getHeight());
            btnUpLeft.setPosition(getX(), getY());
            btnDownRight.setSize(btnUpLeft.getPreferredWidth(), getHeight());
            btnDownRight.setPosition(getX() + getWidth() - btnDownRight.getWidth(), getY());
        } else {
            btnUpLeft.setSize(getWidth(), btnUpLeft.getPreferredHeight());
            btnUpLeft.setPosition(getX(), getY());
            btnDownRight.setSize(getWidth(), btnDownRight.getPreferredHeight());
            btnDownRight.setPosition(getX(), getY() + getHeight() - btnDownRight.getHeight());
        }
        setThumbPos();
    }

    int calcThumbArea() {
        if(orientation == Orientation.HORIZONTAL) {
            return Math.max(1, getWidth() - btnUpLeft.getWidth() - thumb.getWidth() - btnDownRight.getWidth());
        } else {
            return Math.max(1, getHeight() - btnUpLeft.getHeight() - thumb.getHeight() - btnDownRight.getHeight());
        }
    }

    private void setThumbPos() {
        int delta = maxValue - minValue;
        if(orientation == Orientation.HORIZONTAL) {
            int thumbWidth = thumb.getPreferredWidth();
            if(scaleThumb) {
                long availArea = Math.max(1, getWidth() - btnUpLeft.getWidth() - btnDownRight.getWidth());
                thumbWidth = (int)Math.max(thumbWidth, availArea * pageSize / (pageSize + delta + 1));
            }
            thumb.setSize(thumbWidth, getHeight());

            int xpos = btnUpLeft.getX() + btnUpLeft.getWidth();
            if(delta != 0) {
                xpos += (value - minValue) * calcThumbArea() / delta;
            }
            thumb.setPosition(xpos, getY());
        } else {
            int thumbHeight = thumb.getPreferredHeight();
            if(scaleThumb) {
                long availArea = Math.max(1, getHeight() - btnUpLeft.getHeight() - btnDownRight.getHeight());
                thumbHeight = (int)Math.max(thumbHeight, availArea * pageSize / (pageSize + delta + 1));
            }
            thumb.setSize(getWidth(), thumbHeight);

            int ypos = btnUpLeft.getY() + btnUpLeft.getHeight();
            if(delta != 0) {
                ypos += (value - minValue) * calcThumbArea() / delta;
            }
            thumb.setPosition(getX(), ypos);
        }
    }
    
    final class L implements DraggableButton.DragListener, Runnable {
        private int startValue;
        public void dragStarted() {
            startValue = getValue();
        }
        public void dragged(int deltaX, int deltaY) {
            int mouseDelta;
            if(getOrientation() == Orientation.HORIZONTAL) {
                mouseDelta = deltaX;
            } else {
                mouseDelta = deltaY;
            }
            int delta = (getMaxValue() - getMinValue()) * mouseDelta / calcThumbArea();
            int newValue = range(startValue + delta);
            setValue(newValue);
        }
        public void dragStopped() {
        }
        public void run() {
            onTimer(REPEAT_DELAY);
        }
    };
}
