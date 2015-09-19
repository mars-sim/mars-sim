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
import de.matthiasmann.twl.model.ListModel;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.utils.TypeMapping;

/**
 * A wheel widget.
 *
 * @param <T> The data type for the wheel items
 * 
 * @author Matthias Mann
 */
public class WheelWidget<T> extends Widget {
    
    public interface ItemRenderer {
        public Widget getRenderWidget(Object data);
    }
    
    private final TypeMapping<ItemRenderer> itemRenderer;
    private final L listener;
    private final R renderer;
    private final Runnable timerCB;
    
    protected int itemHeight;
    protected int numVisibleItems;
    protected Image selectedOverlay;
    
    private static final int TIMER_INTERVAL = 30;
    private static final int MIN_SPEED = 3;
    private static final int MAX_SPEED = 100;

    protected Timer timer;
    protected int dragStartY;
    protected long lastDragTime;
    protected long lastDragDelta;
    protected int lastDragDist;
    protected boolean hasDragStart;
    protected boolean dragActive;
    protected int scrollOffset;
    protected int scrollAmount;
    
    protected ListModel<T> model;
    protected IntegerModel selectedModel;
    protected int selected;
    protected boolean cyclic;

    public WheelWidget() {
        this.itemRenderer = new TypeMapping<ItemRenderer>();
        this.listener = new L();
        this.renderer = new R();
        this.timerCB = new Runnable() {
            public void run() {
                onTimer();
            }
        };
        
        itemRenderer.put(String.class, new StringItemRenderer());
        
        super.insertChild(renderer, 0);
        setCanAcceptKeyboardFocus(true);
    }

    public WheelWidget(ListModel<T> model) {
        this();
        this.model = model;
    }

    public ListModel<T> getModel() {
        return model;
    }
    
    public void setModel(ListModel<T> model) {
        removeListener();
        this.model = model;
        addListener();
        invalidateLayout();
    }

    public IntegerModel getSelectedModel() {
        return selectedModel;
    }

    public void setSelectedModel(IntegerModel selectedModel) {
        removeSelectedListener();
        this.selectedModel = selectedModel;
        addSelectedListener();
    }

    public int getSelected() {
        return selected;
    }
    
    public void setSelected(int selected) {
        int oldSelected = this.selected;
        if(oldSelected != selected) {
            this.selected = selected;
            if(selectedModel != null) {
                selectedModel.setValue(selected);
            }
            firePropertyChange("selected", oldSelected, selected);
        }
    }
    
    public boolean isCyclic() {
        return cyclic;
    }

    public void setCyclic(boolean cyclic) {
        this.cyclic = cyclic;
    }

    public int getItemHeight() {
        return itemHeight;
    }

    public int getNumVisibleItems() {
        return numVisibleItems;
    }

    public boolean removeItemRenderer(Class<? extends T> clazz) {
        if(itemRenderer.remove(clazz)) {
            super.removeAllChildren();
            invalidateLayout();
            return true;
        }
        return false;
    }
    
    public void registerItemRenderer(Class<? extends T> clazz, ItemRenderer value) {
        itemRenderer.put(clazz, value);
        invalidateLayout();
    }

    public void scroll(int amount) {
        scrollInt(amount);
        scrollAmount = 0;
    }
    
    protected void scrollInt(int amount) {
        int pos = selected;
        int half = itemHeight / 2;
        
        scrollOffset += amount;
        while(scrollOffset >= half) {
            scrollOffset -= itemHeight;
            pos++;
        }
        while(scrollOffset <= -half) {
            scrollOffset += itemHeight;
            pos--;
        }
        
        if(!cyclic) {
            int n = getNumEntries();
            if(n > 0) {
                while(pos >= n) {
                    pos--;
                    scrollOffset += itemHeight;
                }
            }
            while(pos < 0) {
                pos++;
                scrollOffset -= itemHeight;
            }
            scrollOffset = Math.max(-itemHeight, Math.min(itemHeight, scrollOffset));
        }
        
        setSelected(pos);
        
        if(scrollOffset == 0 && scrollAmount == 0) {
            stopTimer();
        } else {
            startTimer();
        }
    }
    
    public void autoScroll(int dir) {
        if(dir != 0) {
            if(scrollAmount != 0 && Integer.signum(scrollAmount) != Integer.signum(dir)) {
                scrollAmount = dir;
            } else {
                scrollAmount += dir;
            }
            startTimer();
        }
    }
    
    @Override
    public int getPreferredInnerHeight() {
        return numVisibleItems * itemHeight;
    }

    @Override
    public int getPreferredInnerWidth() {
        int width = 0;
        for(int i=0,n=getNumEntries() ; i<n ; i++) {
            Widget w = getItemRenderer(i);
            if(w != null) {
                width = Math.max(width, w.getPreferredWidth());
            }
        }
        return width;
    }

    @Override
    protected void paintOverlay(GUI gui) {
        super.paintOverlay(gui);
        if(selectedOverlay != null) {
            int y = getInnerY() + itemHeight * (numVisibleItems/2);
            if((numVisibleItems & 1) == 0) {
                y -= itemHeight/2;
            }
            selectedOverlay.draw(getAnimationState(), getX(), y, getWidth(), itemHeight);
        }
    }

    @Override
    protected boolean handleEvent(Event evt) {
        if(evt.isMouseDragEnd() && dragActive) {
            int absDist = Math.abs(lastDragDist);
            if(absDist > 3 && lastDragDelta > 0) {
                int amount = (int)Math.min(1000, absDist * 100 / lastDragDelta);
                autoScroll(amount * Integer.signum(lastDragDist));
            }
            
            hasDragStart = false;
            dragActive = false;
            return true;
        }
        
        if(evt.isMouseDragEvent()) {
            if(hasDragStart) {
                long time = getTime();
                dragActive = true;
                lastDragDist = dragStartY - evt.getMouseY();
                lastDragDelta = Math.max(1, time - lastDragTime);
                scroll(lastDragDist);
                dragStartY = evt.getMouseY();
                lastDragTime = time;
            }
            return true;
        }
        
        if(super.handleEvent(evt)) {
            return true;
        }
        
        switch(evt.getType()) {
            case MOUSE_WHEEL:
                autoScroll(itemHeight * evt.getMouseWheelDelta());
                return true;
                
            case MOUSE_BTNDOWN:
                if(evt.getMouseButton() == Event.MOUSE_LBUTTON) {
                    dragStartY = evt.getMouseY();
                    lastDragTime = getTime();
                    hasDragStart = true;
                }
                return true;
                
            case KEY_PRESSED:
                switch(evt.getKeyCode()) {
                    case Event.KEY_UP:
                        autoScroll(-itemHeight);
                        return true;
                    case Event.KEY_DOWN:
                        autoScroll(+itemHeight);
                        return true;
                }
                return false;
        }
        
        return evt.isMouseEvent();
    }
    
    protected long getTime() {
        GUI gui = getGUI();
        return (gui != null) ? gui.getCurrentTime() : 0;
    }
    
    protected int getNumEntries() {
        return (model == null) ? 0 : model.getNumEntries();
    }
    
    protected Widget getItemRenderer(int i) {
        T item = model.getEntry(i);
        if(item != null) {
            ItemRenderer ir = itemRenderer.get(item.getClass());
            if(ir != null) {
                Widget w = ir.getRenderWidget(item);
                if(w != null) {
                    if(w.getParent() != renderer) {
                        w.setVisible(false);
                        renderer.add(w);
                    }
                    return w;
                }
            }
        }
        return null;
    }
    
    protected void startTimer() {
        if(timer != null && !timer.isRunning()) {
            timer.start();
        }
    }
    
    protected void stopTimer() {
        if(timer != null) {
            timer.stop();
        }
    }
    
    protected void onTimer() {
        int amount = scrollAmount;
        int newAmount = amount;
        
        if(amount == 0 && !dragActive) {
            amount = -scrollOffset;
        }
        
        if(amount != 0) {
            int absAmount = Math.abs(amount);
            int speed = absAmount * TIMER_INTERVAL / 200;
            int dir = Integer.signum(amount) * Math.min(absAmount,
                    Math.max(MIN_SPEED, Math.min(MAX_SPEED, speed)));
            
            if(newAmount != 0) {
                newAmount -= dir;
            }
            
            scrollAmount = newAmount;
            scrollInt(dir);
        }
    }

    @Override
    protected void layout() {
        layoutChildFullInnerArea(renderer);
    }
    
    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        applyThemeWheel(themeInfo);
    }
    
    protected void applyThemeWheel(ThemeInfo themeInfo) {
        itemHeight = themeInfo.getParameter("itemHeight", 10);
        numVisibleItems = themeInfo.getParameter("visibleItems", 5);
        selectedOverlay = themeInfo.getImage("selectedOverlay");
        invalidateLayout();
    }
    
    @Override
    protected void afterAddToGUI(GUI gui) {
        super.afterAddToGUI(gui);
        addListener();
        addSelectedListener();
        timer = gui.createTimer();
        timer.setCallback(timerCB);
        timer.setDelay(TIMER_INTERVAL);
        timer.setContinuous(true);
    }

    @Override
    protected void beforeRemoveFromGUI(GUI gui) {
        timer.stop();
        timer = null;
        removeListener();
        removeSelectedListener();
        super.beforeRemoveFromGUI(gui);
    }
    
    @Override
    public void insertChild(Widget child, int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAllChildren() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Widget removeChild(int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    
    private void addListener() {
        if(model != null) {
            model.addChangeListener(listener);
        }
    }
    
    private void removeListener() {
        if(model != null) {
            model.removeChangeListener(listener);
        }
    }
    
    private void addSelectedListener() {
        if(selectedModel != null) {
            selectedModel.addCallback(listener);
            syncSelected();
        }
    }
    
    private void removeSelectedListener() {
        if(selectedModel != null) {
            selectedModel.removeCallback(listener);
        }
    }
    
    void syncSelected() {
        setSelected(selectedModel.getValue());
    }
    
    void entriesDeleted(int first, int last) {
        if(selected > first) {
            if(selected > last) {
                setSelected(selected - (last-first+1));
            } else {
                setSelected(first);
            }
        }
        invalidateLayout();
    }
    
    void entriesInserted(int first, int last) {
        if(selected >= first) {
            setSelected(selected + (last-first+1));
        }
        invalidateLayout();
    }
    
    class L implements ListModel.ChangeListener, Runnable {
        public void allChanged() {
            invalidateLayout();
        }
        public void entriesChanged(int first, int last) {
            invalidateLayout();
        }
        public void entriesDeleted(int first, int last) {
            WheelWidget.this.entriesDeleted(first, last);
        }
        public void entriesInserted(int first, int last) {
            WheelWidget.this.entriesInserted(first, last);
        }
        public void run() {
            syncSelected();
        }
    }
    
    class R extends Widget {
        public R() {
            setTheme("");
            setClip(true);
        }
        
        @Override
        protected void paintWidget(GUI gui) {
            if(model == null) {
                return;
            }

            int width = getInnerWidth();
            int x = getInnerX();
            int y = getInnerY();

            int numItems = model.getNumEntries();
            int numDraw  = numVisibleItems;
            int startIdx = selected - numVisibleItems/2;

            if((numDraw & 1) == 0) {
                y -= itemHeight / 2;
                numDraw++;
            }

            if(scrollOffset > 0) {
                y -= scrollOffset;
                numDraw++;
            }
            if(scrollOffset < 0) {
                y -= itemHeight + scrollOffset;
                numDraw++;
                startIdx--;
            }

            main: for(int i=0 ; i<numDraw ; i++) {
                int idx = startIdx + i;

                while(idx < 0) {
                    if(!cyclic) {
                        continue main;
                    }
                    idx += numItems;
                }

                while(idx >= numItems) {
                    if(!cyclic) {
                        continue main;
                    }
                    idx -= numItems;
                }

                Widget w = getItemRenderer(idx);
                if(w != null) {
                    w.setSize(width, itemHeight);
                    w.setPosition(x, y + i*itemHeight);
                    w.validateLayout();
                    paintChild(gui, w);
                }
            }
        }

        @Override
        public void invalidateLayout() {
        }

        @Override
        protected void sizeChanged() {
        }
    }
    
    public static class StringItemRenderer extends Label implements WheelWidget.ItemRenderer {
        public StringItemRenderer() {
            setCache(false);
        }
        
        public Widget getRenderWidget(Object data) {
            setText(String.valueOf(data));
            return this;
        }

        @Override
        protected void sizeChanged() {
        }
    }
}
