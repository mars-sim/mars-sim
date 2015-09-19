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

import de.matthiasmann.twl.model.IntegerModel;
import de.matthiasmann.twl.utils.CallbackSupport;
import de.matthiasmann.twl.model.ListModel;
import de.matthiasmann.twl.model.ListModel.ChangeListener;
import de.matthiasmann.twl.model.ListSelectionModel;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;

/**
 * A list box. Supports single and multiple columns.
 *
 * @param <T> the data type of the list entries
 * @author Matthias Mann
 */
public class ListBox<T> extends Widget {

    /**
     * The value returned by {@link #getSelected() } to indicate that no entry is selected.
     * @see #setSelected(int)
     * @see #setSelected(int, boolean)
     */
    public static final int NO_SELECTION = ListSelectionModel.NO_SELECTION;
    public static final int DEFAULT_CELL_HEIGHT = 20;
    public static final int SINGLE_COLUMN = -1;
    
    public enum CallbackReason {
        MODEL_CHANGED(false),
        SET_SELECTED(false),
        MOUSE_CLICK(false),
        MOUSE_DOUBLE_CLICK(true),
        KEYBOARD(false),
        KEYBOARD_RETURN(true);
        
        final boolean forceCallback;
        private CallbackReason(boolean forceCallback) {
            this.forceCallback = forceCallback;
        }
        
        public boolean actionRequested() {
            return forceCallback;
        }
    };
    
    private static final ListBoxDisplay EMPTY_LABELS[] = {};
    
    private final ChangeListener modelCallback;
    private final Scrollbar scrollbar;
    private ListBoxDisplay[] labels;
    private ListModel<T> model;
    private IntegerModel selectionModel;
    private Runnable selectionModelCallback;
    private int cellHeight = DEFAULT_CELL_HEIGHT;
    private int cellWidth = SINGLE_COLUMN;
    private boolean rowMajor = true;
    private boolean fixedCellWidth;
    private boolean fixedCellHeight;
    private int minDisplayedRows = 1;

    private int numCols = 1;
    private int firstVisible;
    private int selected = NO_SELECTION;
    private int numEntries;
    private boolean needUpdate;
    private boolean inSetSelected;
    private CallbackWithReason<?>[] callbacks;
    
    public ListBox() {
        LImpl li = new LImpl();

        modelCallback = li;
        scrollbar = new Scrollbar();
        scrollbar.addCallback(li);
        labels = EMPTY_LABELS;
        
        super.insertChild(scrollbar, 0);
        
        setSize(200, 300);
        setCanAcceptKeyboardFocus(true);
        setDepthFocusTraversal(false);
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public ListBox(ListModel<T> model) {
        this();
        setModel(model);
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public ListBox(ListSelectionModel<T> model) {
        this();
        setModel(model);
    }

    public ListModel<T> getModel() {
        return model;
    }

    public void setModel(ListModel<T> model) {
        if(this.model != model) {
            if(this.model != null) {
                this.model.removeChangeListener(modelCallback);
            }
            this.model = model;
            if(model != null) {
                model.addChangeListener(modelCallback);
            }
            modelCallback.allChanged();
        }
    }

    public IntegerModel getSelectionModel() {
        return selectionModel;
    }

    public void setSelectionModel(IntegerModel selectionModel) {
        if(this.selectionModel != selectionModel) {
            if(this.selectionModel != null) {
                this.selectionModel.removeCallback(selectionModelCallback);
            }
            this.selectionModel = selectionModel;
            if(selectionModel != null) {
                if(selectionModelCallback == null) {
                    selectionModelCallback = new Runnable() {
                        public void run() {
                            syncSelectionFromModel();
                        }
                    };
                }
                this.selectionModel.addCallback(selectionModelCallback);
                syncSelectionFromModel();
            }
        }
    }

    public void setModel(ListSelectionModel<T> model) {
        setSelectionModel(null);
        if(model == null) {
            setModel((ListModel<T>)null);
        } else {
            setModel(model.getListModel());
            setSelectionModel(model);
        }
    }

    public void addCallback(CallbackWithReason<CallbackReason> cb) {
        callbacks = CallbackSupport.addCallbackToList(callbacks, cb, CallbackWithReason.class);
    }

    public void removeCallback(CallbackWithReason<CallbackReason> cb) {
        callbacks = CallbackSupport.removeCallbackFromList(callbacks, cb);
    }

    private void doCallback(CallbackReason reason) {
        CallbackSupport.fireCallbacks(callbacks, reason);
    }

    public int getCellHeight() {
        return cellHeight;
    }

    public void setCellHeight(int cellHeight) {
        if(cellHeight < 1) {
            throw new IllegalArgumentException("cellHeight < 1");
        }
        this.cellHeight = cellHeight;
    }

    public int getCellWidth() {
        return cellWidth;
    }

    public void setCellWidth(int cellWidth) {
        if(cellWidth < 1 && cellWidth != SINGLE_COLUMN) {
            throw new IllegalArgumentException("cellWidth < 1");
        }
        this.cellWidth = cellWidth;
    }

    public boolean isFixedCellHeight() {
        return fixedCellHeight;
    }

    public void setFixedCellHeight(boolean fixedCellHeight) {
        this.fixedCellHeight = fixedCellHeight;
    }

    public boolean isFixedCellWidth() {
        return fixedCellWidth;
    }

    public void setFixedCellWidth(boolean fixedCellWidth) {
        this.fixedCellWidth = fixedCellWidth;
    }

    public boolean isRowMajor() {
        return rowMajor;
    }

    public void setRowMajor(boolean rowMajor) {
        this.rowMajor = rowMajor;
    }

    public int getFirstVisible() {
        return firstVisible;
    }

    public int getLastVisible() {
        return getFirstVisible() + labels.length - 1;
    }
    
    public void setFirstVisible(int firstVisible) {
        firstVisible = Math.max(0, Math.min(firstVisible, numEntries - 1));
        if(this.firstVisible != firstVisible) {
            this.firstVisible = firstVisible;
            scrollbar.setValue(firstVisible / numCols, false);
            needUpdate = true;
        }
    }

    public int getSelected() {
        return selected;
    }

    /**
     * Selects the specified entry and scrolls to make it visible
     *
     * @param selected the index or {@link #NO_SELECTION}
     * @throws IllegalArgumentException if index is invalid
     * @see #setSelected(int, boolean)
     */
    public void setSelected(int selected) {
        setSelected(selected, true, CallbackReason.SET_SELECTED);
    }

    /**
     * Selects the specified entry and optionally scrolls to that entry
     *
     * @param selected the index or {@link #NO_SELECTION}
     * @param scroll true if it should scroll to make the entry visible
     * @throws IllegalArgumentException if index is invalid
     */
    public void setSelected(int selected, boolean scroll) {
        setSelected(selected, scroll, CallbackReason.SET_SELECTED);
    }
    
    void setSelected(int selected, boolean scroll, CallbackReason reason) {
        if(selected < NO_SELECTION || selected >= numEntries) {
            throw new IllegalArgumentException();
        }
        if(scroll) {
            validateLayout();
            if(selected == NO_SELECTION) {
                setFirstVisible(0);
            } else {
                int delta = getFirstVisible() - selected;
                if(delta > 0) {
                    int deltaRows = (delta + numCols - 1) / numCols;
                    setFirstVisible(getFirstVisible() - deltaRows * numCols);
                } else {
                    delta = selected - getLastVisible();
                    if(delta > 0) {
                        int deltaRows = (delta + numCols - 1) / numCols;
                        setFirstVisible(getFirstVisible() + deltaRows * numCols);
                    }
                }
            }
        }
        if(this.selected != selected) {
            this.selected = selected;
            if(selectionModel != null) {
                try {
                    inSetSelected = true;
                    selectionModel.setValue(selected);
                } finally {
                    inSetSelected = false;
                }
            }
            needUpdate = true;
            doCallback(reason);
        } else if(reason.actionRequested() || reason == CallbackReason.MOUSE_CLICK) {
            doCallback(reason);
        }
    }

    public void scrollToSelected() {
        setSelected(selected, true, CallbackReason.SET_SELECTED);
    }

    public int getNumEntries() {
        return numEntries;
    }
    
    public int getNumRows() {
        return (numEntries + numCols - 1) / numCols;
    }

    public int getNumColumns() {
        return numCols;
    }
    
    public int findEntryByName(String prefix) {
        for(int i=selected+1 ; i<numEntries ; i++) {
            if(model.matchPrefix(i, prefix)) {
                return i;
            }
        }
        for(int i=0 ; i<selected ; i++) {
            if(model.matchPrefix(i, prefix)) {
                return i;
            }
        }
        return NO_SELECTION;
    }

    /**
     * The method always return this.
     * Use getEntryAt(x, y) to locate the listbox entry at the specific coordinates.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @return this.
     */
    @Override
    public Widget getWidgetAt(int x, int y) {
        return this;
    }

    /**
     * Returns the entry at the specific coordinates or -1 if there is no entry.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the index of the entry or -1.
     */
    public int getEntryAt(int x, int y) {
        int n = Math.max(labels.length, numEntries - firstVisible);
        for(int i=0 ; i<n ; i++) {
            if(labels[i].getWidget().isInside(x, y)) {
                return firstVisible + i;
            }
        }
        return -1;
    }

    @Override
    public void insertChild(Widget child, int index) throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAllChildren() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Widget removeChild(int index) throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        setCellHeight(themeInfo.getParameter("cellHeight", DEFAULT_CELL_HEIGHT));
        setCellWidth(themeInfo.getParameter("cellWidth", SINGLE_COLUMN));
        setRowMajor(themeInfo.getParameter("rowMajor", true));
        setFixedCellWidth(themeInfo.getParameter("fixedCellWidth", false));
        setFixedCellHeight(themeInfo.getParameter("fixedCellHeight", false));
        minDisplayedRows = themeInfo.getParameter("minDisplayedRows", 1);
    }

    protected void goKeyboard(int dir) {
        int newPos = selected + dir;
        if(newPos >= 0 && newPos < numEntries) {
            setSelected(newPos, true, CallbackReason.KEYBOARD);
        }
    }
    
    protected boolean isSearchChar(char ch) {
        return (ch != Event.CHAR_NONE) && Character.isLetterOrDigit(ch);
    }

    @Override
    protected void keyboardFocusGained() {
        setLabelFocused(true);
    }

    @Override
    protected void keyboardFocusLost() {
        setLabelFocused(false);
    }

    private void setLabelFocused(boolean focused) {
        int idx = selected - firstVisible;
        if(idx >= 0 && idx < labels.length) {
            labels[idx].setFocused(focused);
        }
    }

    @Override
    public boolean handleEvent(Event evt) {
        switch (evt.getType()) {
        case MOUSE_WHEEL:
            scrollbar.scroll(-evt.getMouseWheelDelta());
            return true;
        case KEY_PRESSED:
            switch (evt.getKeyCode()) {
            case Event.KEY_UP:
                goKeyboard(-numCols);
                break;
            case Event.KEY_DOWN:
                goKeyboard(numCols);
                break;
            case Event.KEY_LEFT:
                goKeyboard(-1);
                break;
            case Event.KEY_RIGHT:
                goKeyboard(1);
                break;
            case Event.KEY_PRIOR:
                if(numEntries > 0) {
                    setSelected(Math.max(0, selected-labels.length),
                        true, CallbackReason.KEYBOARD);
                }
                break;
            case Event.KEY_NEXT:
                setSelected(Math.min(numEntries-1, selected+labels.length),
                        true, CallbackReason.KEYBOARD);
                break;
            case Event.KEY_HOME:
                if(numEntries > 0) {
                    setSelected(0, true, CallbackReason.KEYBOARD);
                }
                break;
            case Event.KEY_END:
                setSelected(numEntries-1, true, CallbackReason.KEYBOARD);
                break;
            case Event.KEY_RETURN:
                setSelected(selected, false, CallbackReason.KEYBOARD_RETURN);
                break;
            default:
                if(evt.hasKeyChar() && isSearchChar(evt.getKeyChar())) {
                    int idx = findEntryByName(Character.toString(evt.getKeyChar()));
                    if(idx != NO_SELECTION) {
                        setSelected(idx, true, CallbackReason.KEYBOARD);
                    }
                    return true;
                }
                return false;
            }
            return true;
        case KEY_RELEASED:
            switch (evt.getKeyCode()) {
            case Event.KEY_UP:
            case Event.KEY_DOWN:
            case Event.KEY_LEFT:
            case Event.KEY_RIGHT:
            case Event.KEY_PRIOR:
            case Event.KEY_NEXT:
            case Event.KEY_HOME:
            case Event.KEY_END:
            case Event.KEY_RETURN:
                return true;
            }
            return false;
        }
        // delegate to children (listbox, displays, etc...)
        if(super.handleEvent(evt)) {
            return true;
        }
        // eat all mouse events
        return evt.isMouseEvent();
    }

    @Override
    public int getMinWidth() {
        return Math.max(super.getMinWidth(), scrollbar.getMinWidth());
    }

    @Override
    public int getMinHeight() {
        int minHeight = Math.max(super.getMinHeight(), scrollbar.getMinHeight());
        if(minDisplayedRows > 0) {
            minHeight = Math.max(minHeight, getBorderVertical() +
                    Math.min(numEntries, minDisplayedRows) * cellHeight);
        }
        return minHeight;
    }

    @Override
    public int getPreferredInnerWidth() {
        return Math.max(super.getPreferredInnerWidth(), scrollbar.getPreferredWidth());
    }

    @Override
    public int getPreferredInnerHeight() {
        return Math.max(getNumRows() * getCellHeight(), scrollbar.getPreferredHeight());
    }

    @Override
    protected void paint(GUI gui) {
        if(needUpdate) {
            updateDisplay();
        }
        // always update scrollbar
        int maxFirstVisibleRow = computeMaxFirstVisibleRow();
        scrollbar.setMinMaxValue(0, maxFirstVisibleRow);
        scrollbar.setValue(firstVisible / numCols, false);

        super.paint(gui);
    }

    private int computeMaxFirstVisibleRow() {
        int maxFirstVisibleRow = Math.max(0, numEntries - labels.length);
        maxFirstVisibleRow = (maxFirstVisibleRow + numCols - 1) / numCols;
        return maxFirstVisibleRow;
    }

    private void updateDisplay() {
        needUpdate = false;
        
        if(selected >= numEntries) {
            selected = NO_SELECTION;
        }
        
        int maxFirstVisibleRow = computeMaxFirstVisibleRow();
        int maxFirstVisible = maxFirstVisibleRow * numCols;
        if(firstVisible > maxFirstVisible) {
            firstVisible = Math.max(0, maxFirstVisible);
        }

        boolean hasFocus = hasKeyboardFocus();

        for(int i=0 ; i<labels.length ; i++) {
            ListBoxDisplay label = labels[i];
            int cell = i + firstVisible;
            if(cell < numEntries) {
                label.setData(model.getEntry(cell));
                label.setTooltipContent(model.getEntryTooltip(cell));
            } else {
                label.setData(null);
                label.setTooltipContent(null);
            }
            label.setSelected(cell == selected);
            label.setFocused(cell == selected && hasFocus);
        }
    }

    @Override
    protected void layout() {
        scrollbar.setSize(scrollbar.getPreferredWidth(), getInnerHeight());
        scrollbar.setPosition(getInnerRight() - scrollbar.getWidth(), getInnerY());
        
        int numRows = Math.max(1, getInnerHeight() / cellHeight);
        if(cellWidth != SINGLE_COLUMN) {
            numCols = Math.max(1, (scrollbar.getX() - getInnerX()) / cellWidth);
        } else {
            numCols = 1;
        }
        setVisibleCells(numRows);
        
        needUpdate = true;
    }

    private void setVisibleCells(int numRows) {
        int visibleCells = numRows * numCols;
        assert visibleCells >= 1;
        
        scrollbar.setPageSize(visibleCells);
        
        int curVisible = labels.length;
        for(int i=curVisible ; i-->visibleCells ;) {
            super.removeChild(1+i);
        }

        ListBoxDisplay[] newLabels = new ListBoxDisplay[visibleCells];
        System.arraycopy(labels, 0, newLabels, 0, Math.min(visibleCells, labels.length));
        labels = newLabels;
        
        for(int i = curVisible; i < visibleCells; i++) {
            final int cellOffset = i;
            ListBoxDisplay lbd = createDisplay();
            lbd.addListBoxCallback(new CallbackWithReason<CallbackReason>() {
                public void callback(CallbackReason reason) {
                    int cell = getFirstVisible() + cellOffset;
                    if(cell < getNumEntries()) {
                        setSelected(cell, false, reason);
                    }
                }
            });
            super.insertChild(lbd.getWidget(), 1+i);
            labels[i] = lbd;
        }
        
        int innerWidth = scrollbar.getX() - getInnerX();
        int innerHeight = getInnerHeight();
        for(int i=0 ; i<visibleCells ; i++) {
            int row, col;
            if(rowMajor) {
                row = i / numCols;
                col = i % numCols;
            } else {
                row = i % numRows;
                col = i / numRows;
            }
            int x, y, w, h;
            if(fixedCellHeight) {
                y = row * cellHeight;
                h = cellHeight;
            } else {
                y = row * innerHeight / numRows;
                h = (row+1) * innerHeight / numRows - y;
            }
            if(fixedCellWidth && cellWidth != SINGLE_COLUMN) {
                x = col * cellWidth;
                w = cellWidth;
            } else {
                x = col * innerWidth / numCols;
                w = (col+1) * innerWidth / numCols - x;
            }
            Widget cell = (Widget)labels[i];
            cell.setSize(Math.max(0, w), Math.max(0, h));
            cell.setPosition(x + getInnerX(), y + getInnerY());
        }
    }
    
    protected ListBoxDisplay createDisplay() {
        return new ListBoxLabel();
    }
    
    protected static class ListBoxLabel extends TextWidget implements ListBoxDisplay {
        public static final StateKey STATE_SELECTED = StateKey.get("selected");
        public static final StateKey STATE_EMPTY = StateKey.get("empty");

        private boolean selected;
        private CallbackWithReason<?>[] callbacks;

        public ListBoxLabel() {
            setClip(true);
            setTheme("display");
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            if(this.selected != selected) {
                this.selected = selected;
                getAnimationState().setAnimationState(STATE_SELECTED, selected);
            }
        }

        public boolean isFocused() {
            return getAnimationState().getAnimationState(STATE_KEYBOARD_FOCUS);
        }

        public void setFocused(boolean focused) {
            getAnimationState().setAnimationState(STATE_KEYBOARD_FOCUS, focused);
        }

        public void setData(Object data) {
            setCharSequence((data == null) ? "" : data.toString());
            getAnimationState().setAnimationState(STATE_EMPTY, data == null);
        }

        public Widget getWidget() {
            return this;
        }

        public void addListBoxCallback(CallbackWithReason<ListBox.CallbackReason> cb) {
            callbacks = CallbackSupport.addCallbackToList(callbacks, cb, CallbackWithReason.class);
        }

        public void removeListBoxCallback(CallbackWithReason<ListBox.CallbackReason> cb) {
            callbacks = CallbackSupport.removeCallbackFromList(callbacks, cb);
        }

        protected void doListBoxCallback(ListBox.CallbackReason reason) {
            CallbackSupport.fireCallbacks(callbacks, reason);
        }

        protected boolean handleListBoxEvent(Event evt) {
            switch(evt.getType()) {
            case MOUSE_BTNDOWN:
                if(!selected) {
                    doListBoxCallback(CallbackReason.MOUSE_CLICK);
                }
                return true;
            case MOUSE_CLICKED:
                if(selected && evt.getMouseClickCount() == 2) {
                    doListBoxCallback(CallbackReason.MOUSE_DOUBLE_CLICK);
                }
                return true;
            }
            return false;
        }

        @Override
        protected boolean handleEvent(Event evt) {
            handleMouseHover(evt);
            if(!evt.isMouseDragEvent()) {
                if(handleListBoxEvent(evt)) {
                    return true;
                }
            }
            if(super.handleEvent(evt)) {
                return true;
            }
            return evt.isMouseEventNoWheel();
        }

    }

    void entriesInserted(int first, int last) {
        int delta = last - first + 1;
        int prevNumEntries = numEntries;
        numEntries += delta;
        int fv = getFirstVisible();
        if(fv >= first && prevNumEntries >= labels.length) {
            fv += delta;
            setFirstVisible(fv);
        }
        int s = getSelected();
        if(s >= first) {
            setSelected(s + delta, false, CallbackReason.MODEL_CHANGED);
        }
        if(first <= getLastVisible() && last >= fv) {
            needUpdate = true;
        }
    }
    
    void entriesDeleted(int first, int last) {
        int delta = last - first + 1;
        numEntries -= delta;
        int fv = getFirstVisible();
        int lv = getLastVisible();
        if(fv > last) {
            setFirstVisible(fv - delta);
        } else if(fv <= last && lv >= first) {
            setFirstVisible(first);
        }
        int s = getSelected();
        if(s > last) {
            setSelected(s - delta, false, CallbackReason.MODEL_CHANGED);
        } else if(s >= first && s <= last) {
            setSelected(NO_SELECTION, false, CallbackReason.MODEL_CHANGED);
        }
    }
    
    void entriesChanged(int first, int last) {
        int fv = getFirstVisible();
        int lv = getLastVisible();
        if(fv <= last && lv >= first) {
            needUpdate = true;
        }
    }

    void allChanged() {
        numEntries = (model != null) ? model.getNumEntries() : 0;
        setSelected(NO_SELECTION, false, CallbackReason.MODEL_CHANGED);
        setFirstVisible(0);
        needUpdate = true;
    }

    void scrollbarChanged() {
        setFirstVisible(scrollbar.getValue() * numCols);
    }

    void syncSelectionFromModel() {
        if(!inSetSelected) {
            setSelected(selectionModel.getValue());
        }
    }

    private class LImpl implements ChangeListener, Runnable {
        public void entriesInserted(int first, int last) {
            ListBox.this.entriesInserted(first, last);
        }
        public void entriesDeleted(int first, int last) {
            ListBox.this.entriesDeleted(first, last);
        }
        public void entriesChanged(int first, int last) {
            ListBox.this.entriesChanged(first, last);
        }
        public void allChanged() {
            ListBox.this.allChanged();
        }
        public void run() {
            ListBox.this.scrollbarChanged();
        }
    };
}
