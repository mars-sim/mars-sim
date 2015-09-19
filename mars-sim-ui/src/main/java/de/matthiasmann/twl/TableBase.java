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

import de.matthiasmann.twl.model.DefaultTableSelectionModel;
import de.matthiasmann.twl.model.SortOrder;
import de.matthiasmann.twl.model.TableColumnHeaderModel;
import de.matthiasmann.twl.model.TreeTableNode;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.MouseCursor;
import de.matthiasmann.twl.renderer.Renderer;
import de.matthiasmann.twl.utils.CallbackSupport;
import de.matthiasmann.twl.utils.SizeSequence;
import de.matthiasmann.twl.utils.SparseGrid;
import de.matthiasmann.twl.utils.SparseGrid.Entry;
import de.matthiasmann.twl.utils.TypeMapping;

/**
 * Base class for Table and TreeTable.
 *
 * It does not have a {@link TableSelectionManager} by default. To make the
 * table entries selectable you need to install a selection manager:
 * {@link #setSelectionManager(de.matthiasmann.twl.TableSelectionManager) } or
 * {@link #setDefaultSelectionManager() }
 *
 * @see Table
 * @see TreeTable
 * @author Matthias Mann
 */
public abstract class TableBase extends Widget implements ScrollPane.Scrollable, ScrollPane.AutoScrollable, ScrollPane.CustomPageSize {

    public interface Callback {
        public void mouseDoubleClicked(int row, int column);
        public void mouseRightClick(int row, int column, Event evt);
        public void columnHeaderClicked(int column);
    }

    /**
     * IMPORTANT: Widgets implementing CellRenderer should not call
     * {@link Widget#invalidateLayout()} or {@link Widget#invalidateLayoutLocally()}
     * . This means they need to override {@link Widget#sizeChanged()}.
     */
    public interface CellRenderer {
        /**
         * Called when the CellRenderer is registered and a theme is applied.
         * @param themeInfo the theme object
         */
        public void applyTheme(ThemeInfo themeInfo);

        /**
         * The theme name for this CellRenderer. Must be relative to the Table.
         * @return the theme name.
         */
        public String getTheme();

        /**
         * This method sets the row, column and the cell data.
         * It is called before any other cell related method is called.
         * @param row the table row
         * @param column the table column
         * @param data the cell data
         */
        public void setCellData(int row, int column, Object data);

        /**
         * Returns how many columns this cell spans. Must be >= 1.
         * Is called after setCellData.
         * @return the column span.
         * @see #setCellData(int, int, java.lang.Object)
         */
        public int getColumnSpan();

        /**
         * Returns the preferred cell height in variable row height mode.
         * It is not called at all in fixed row height mode.
         * @return the preferred cell height
         * @see #setCellData(int, int, java.lang.Object)
         * @see TableBase#setVaribleRowHeight(boolean)
         */
        public int getPreferredHeight();

        /**
         * Returns the widget used to render the cell or null if no rendering
         * should happen. This widget should not be added to any widget. It
         * will be managed by the Table.
         * TableBase uses a stamping approch for cell rendering. This method
         * must not create a new widget each time.
         *
         * This method is responsible to call setPosition and setSize on the
         * returned widget.
         *
         * @param x the left edge of the cell
         * @param y the top edge of the cell
         * @param width the width of the cell
         * @param height the height of the cell
         * @param isSelected the selected state of this cell
         * @return the widget used for cell rendering or null.
         * @see #setCellData(int, int, java.lang.Object)
         */
        public Widget getCellRenderWidget(int x, int y, int width, int height, boolean isSelected);
    }

    public interface CellWidgetCreator extends CellRenderer {
        public Widget updateWidget(Widget existingWidget);
        public void positionWidget(Widget widget, int x, int y, int w, int h);
    }

    public interface KeyboardSearchHandler {
        /**
         * Update search with this key event
         *
         * @param evt the key event
         * @return true if the event was handled
         */
        public boolean handleKeyEvent(Event evt);

        /**
         * Returns true if the search is active.
         * @return true if the search is active.
         */
        public boolean isActive();

        /**
         * Called when the table position ot size has changed.
         */
        public void updateInfoWindowPosition();
    }

    public interface DragListener {
        /**
         * Signals the start of the drag operation
         *
         * @param row the row where the drag started
         * @param col the column where the drag started
         * @param evt the mouse event which started the drag
         * @return true if the drag should start, false if it should be canceled
         */
        public boolean dragStarted(int row, int col, Event evt);

        /**
         * Mouse dragging in progress
         * @param evt the MOUSE_DRAGGED event
         * @return the mouse cursor to display
         */
        public MouseCursor dragged(Event evt);

        /**
         * Mouse dragging stopped
         * @param evt the event which stopped the mouse drag
         */
        public void dragStopped(Event evt);

        /**
         * Called when the mouse drag is canceled (eg by pressing ESCAPE)
         */
        public void dragCanceled();
    }

    public static final StateKey STATE_FIRST_COLUMNHEADER = StateKey.get("firstColumnHeader");
    public static final StateKey STATE_LAST_COLUMNHEADER = StateKey.get("lastColumnHeader");
    public static final StateKey STATE_ROW_SELECTED = StateKey.get("rowSelected");
    public static final StateKey STATE_ROW_HOVER = StateKey.get("rowHover");
    public static final StateKey STATE_ROW_DROPTARGET = StateKey.get("rowDropTarget");
    public static final StateKey STATE_ROW_ODD = StateKey.get("rowOdd");
    public static final StateKey STATE_LEAD_ROW = StateKey.get("leadRow");
    public static final StateKey STATE_SELECTED = StateKey.get("selected");
    public static final StateKey STATE_SORT_ASCENDING  = StateKey.get("sortAscending");
    public static final StateKey STATE_SORT_DESCENDING = StateKey.get("sortDescending");

    private final StringCellRenderer stringCellRenderer;
    private final RemoveCellWidgets removeCellWidgetsFunction;
    private final InsertCellWidgets insertCellWidgetsFunction;
    private final CellWidgetContainer cellWidgetContainer;
    
    protected final TypeMapping<CellRenderer> cellRenderers;
    protected final SparseGrid widgetGrid;
    protected final ColumnSizeSequence columnModel;
    protected TableColumnHeaderModel columnHeaderModel;
    protected SizeSequence rowModel;
    protected boolean hasCellWidgetCreators;
    protected ColumnHeader[] columnHeaders;
    protected CellRenderer[] columnDefaultCellRenderer;
    protected TableSelectionManager selectionManager;
    protected KeyboardSearchHandler keyboardSearchHandler;
    protected DragListener dragListener;
    protected Callback[] callbacks;

    protected Image imageColumnDivider;
    protected Image imageRowBackground;
    protected Image imageRowOverlay;
    protected Image imageRowDropMarker;
    protected ThemeInfo tableBaseThemeInfo;
    protected int columnHeaderHeight;
    protected int columnDividerDragableDistance;
    protected MouseCursor columnResizeCursor;
    protected MouseCursor normalCursor;
    protected MouseCursor dragNotPossibleCursor;
    protected boolean ensureColumnHeaderMinWidth;

    protected int numRows;
    protected int numColumns;
    protected int rowHeight = 32;
    protected int defaultColumnWidth = 256;
    protected boolean autoSizeAllRows;
    protected boolean updateAllCellWidgets;
    protected boolean updateAllColumnWidth;

    protected int scrollPosX;
    protected int scrollPosY;

    protected int firstVisibleRow;
    protected int firstVisibleColumn;
    protected int lastVisibleRow;
    protected int lastVisibleColumn;
    protected boolean firstRowPartialVisible;
    protected boolean lastRowPartialVisible;

    protected int dropMarkerRow = -1;
    protected boolean dropMarkerBeforeRow;
    
    protected static final int LAST_MOUSE_Y_OUTSIDE = Integer.MIN_VALUE;
    
    protected int lastMouseY = LAST_MOUSE_Y_OUTSIDE;
    protected int lastMouseRow = -1;
    protected int lastMouseColumn = -1;

    protected TableBase() {
        this.cellRenderers = new TypeMapping<CellRenderer>();
        this.stringCellRenderer = new StringCellRenderer();
        this.widgetGrid = new SparseGrid(32);
        this.removeCellWidgetsFunction = new RemoveCellWidgets();
        this.insertCellWidgetsFunction = new InsertCellWidgets();
        this.columnModel = new ColumnSizeSequence();
        this.columnDefaultCellRenderer = new CellRenderer[8];
        this.cellWidgetContainer = new CellWidgetContainer();

        super.insertChild(cellWidgetContainer, 0);
        setCanAcceptKeyboardFocus(true);
    }

    public TableSelectionManager getSelectionManager() {
        return selectionManager;
    }

    public void setSelectionManager(TableSelectionManager selectionManager) {
        if(this.selectionManager != selectionManager) {
            if(this.selectionManager != null) {
                this.selectionManager.setAssociatedTable(null);
            }
            this.selectionManager = selectionManager;
            if(this.selectionManager != null) {
                this.selectionManager.setAssociatedTable(this);
            }
        }
    }

    /**
     * Installs a multi row selection manager.
     *
     * @see TableRowSelectionManager
     * @see DefaultTableSelectionModel
     */
    public void setDefaultSelectionManager() {
        setSelectionManager(new TableRowSelectionManager());
    }

    public KeyboardSearchHandler getKeyboardSearchHandler() {
        return keyboardSearchHandler;
    }

    public void setKeyboardSearchHandler(KeyboardSearchHandler keyboardSearchHandler) {
        this.keyboardSearchHandler = keyboardSearchHandler;
    }

    public DragListener getDragListener() {
        return dragListener;
    }

    public void setDragListener(DragListener dragListener) {
        cancelDragging();
        this.dragListener = dragListener;
    }

    public boolean isDropMarkerBeforeRow() {
        return dropMarkerBeforeRow;
    }

    public int getDropMarkerRow() {
        return dropMarkerRow;
    }
    
    public void setDropMarker(int row, boolean beforeRow) {
        if(row < 0 || row > numRows) {
            throw new IllegalArgumentException("row");
        }
        if(row == numRows && !beforeRow) {
            throw new IllegalArgumentException("row");
        }
        dropMarkerRow = row;
        dropMarkerBeforeRow = beforeRow;
    }

    public boolean setDropMarker(Event evt) {
        int mouseY = evt.getMouseY();
        if(isMouseInside(evt) && !isMouseInColumnHeader(mouseY)) {
            mouseY -= getOffsetY();
            int row = getRowFromPosition(mouseY);
            if(row >= 0 && row < numRows) {
                int rowStart = getRowStartPosition(row);
                int rowEnd = getRowEndPosition(row);
                int margin = (rowEnd - rowStart + 2) / 4;
                if((mouseY - rowStart) < margin) {
                    setDropMarker(row, true);
                } else if((rowEnd - mouseY) < margin) {
                    setDropMarker(row+1, true);
                } else {
                    setDropMarker(row, false);
                }
                return true;
            } else if(row == numRows) {
                setDropMarker(row, true);
                return true;
            }
        }
        return false;
    }

    public void clearDropMarker() {
        dropMarkerRow = -1;
    }

    public void addCallback(Callback callback) {
        callbacks = CallbackSupport.addCallbackToList(callbacks, callback, Callback.class);
    }

    public void removeCallback(Callback callback) {
        callbacks = CallbackSupport.removeCallbackFromList(callbacks, callback);
    }
    
    public boolean isVariableRowHeight() {
        return rowModel != null;
    }

    public void setVaribleRowHeight(boolean varibleRowHeight) {
        if(varibleRowHeight && rowModel == null) {
            rowModel = new RowSizeSequence(numRows);
            autoSizeAllRows = true;
            invalidateLayout();
        } else if(!varibleRowHeight) {
            rowModel = null;
        }
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumColumns() {
        return numColumns;
    }

    public int getRowFromPosition(int y) {
        if(y >= 0) {
            if(rowModel != null) {
                return rowModel.getIndex(y);
            }
            return Math.min(numRows-1, y / rowHeight);
        }
        return -1;
    }

    public int getRowStartPosition(int row) {
        checkRowIndex(row);
        if(rowModel != null) {
            return rowModel.getPosition(row);
        } else {
            return row * rowHeight;
        }
    }

    public int getRowHeight(int row) {
        checkRowIndex(row);
        if(rowModel != null) {
            return rowModel.getSize(row);
        } else {
            return rowHeight;
        }
    }

    public int getRowEndPosition(int row) {
        checkRowIndex(row);
        if(rowModel != null) {
            return rowModel.getPosition(row + 1);
        } else {
            return (row+1) * rowHeight;
        }
    }

    public int getColumnFromPosition(int x) {
        if(x >= 0) {
            int column = columnModel.getIndex(x);
            return column;
        }
        return -1;
    }

    public int getColumnStartPosition(int column) {
        checkColumnIndex(column);
        return columnModel.getPosition(column);
    }

    public int getColumnWidth(int column) {
        checkColumnIndex(column);
        return columnModel.getSize(column);
    }

    public int getColumnEndPosition(int column) {
        checkColumnIndex(column);
        return columnModel.getPosition(column + 1);
    }

    public void setColumnWidth(int column, int width) {
        checkColumnIndex(column);
        columnHeaders[column].setColumnWidth(width);    // store passed width
        if(columnModel.update(column)) {
            invalidateLayout();
        }
    }

    public AnimationState getColumnHeaderAnimationState(int column) {
        checkColumnIndex(column);
        return columnHeaders[column].getAnimationState();
    }

    /**
     * Sets the sort order animation state for all column headers.
     * @param sortColumn This column gets sort order indicators, all other columns not
     * @param sortOrder Which sort order. Can be null to disable the indicators
     */
    public void setColumnSortOrderAnimationState(int sortColumn, SortOrder sortOrder) {
        for(int column=0 ; column<numColumns ; ++column) {
            AnimationState animState = columnHeaders[column].getAnimationState();
            animState.setAnimationState(STATE_SORT_ASCENDING, (column == sortColumn) && (sortOrder == SortOrder.ASCENDING));
            animState.setAnimationState(STATE_SORT_DESCENDING, (column == sortColumn) && (sortOrder == SortOrder.DESCENDING));
        }
    }

    public void scrollToRow(int row) {
        ScrollPane scrollPane = ScrollPane.getContainingScrollPane(this);
        if(scrollPane != null && numRows > 0) {
            scrollPane.validateLayout();
            int rowStart = getRowStartPosition(row);
            int rowEnd = getRowEndPosition(row);
            int height = rowEnd - rowStart;
            scrollPane.scrollToAreaY(rowStart, height, height/2);
        }
    }

    public int getNumVisibleRows() {
        int rows = lastVisibleRow - firstVisibleRow;
        if(!lastRowPartialVisible) {
            rows++;
        }
        return rows;
    }
    
    @Override
    public int getMinHeight() {
        return Math.max(super.getMinHeight(), columnHeaderHeight);
    }

    @Override
    public int getPreferredInnerWidth() {
        if(getInnerWidth() == 0) {
            return columnModel.computePreferredWidth();
        }
        if(updateAllColumnWidth) {
            updateAllColumnWidth();
        }
        return (numColumns > 0) ? getColumnEndPosition(numColumns-1) : 0;
    }

    @Override
    public int getPreferredInnerHeight() {
        if(autoSizeAllRows) {
            autoSizeAllRows();
        }
        return columnHeaderHeight + 1 + // +1 for drop marker
                ((numRows > 0) ? getRowEndPosition(numRows-1) : 0);
    }

    public void registerCellRenderer(Class<?> dataClass, CellRenderer cellRenderer) {
        if(dataClass == null) {
            throw new NullPointerException("dataClass");
        }
        cellRenderers.put(dataClass, cellRenderer);

        if(cellRenderer instanceof CellWidgetCreator) {
            hasCellWidgetCreators = true;
        }

        // only call it when we already have a theme
        if(tableBaseThemeInfo != null) {
            applyCellRendererTheme(cellRenderer);
        }
    }

    public void setScrollPosition(int scrollPosX, int scrollPosY) {
        if(this.scrollPosX != scrollPosX || this.scrollPosY != scrollPosY) {
            this.scrollPosX = scrollPosX;
            this.scrollPosY = scrollPosY;
            invalidateLayoutLocally();
        }
    }

    public void adjustScrollPosition(int row) {
        checkRowIndex(row);
        ScrollPane scrollPane = ScrollPane.getContainingScrollPane(this);
        int numVisibleRows = getNumVisibleRows();
        if(numVisibleRows >= 1 && scrollPane != null) {
            if(row < firstVisibleRow || (row == firstVisibleRow && firstRowPartialVisible)) {
                int pos = getRowStartPosition(row);
                scrollPane.setScrollPositionY(pos);
            } else if(row > lastVisibleRow || (row == lastVisibleRow && lastRowPartialVisible)) {
                int innerHeight = Math.max(0, getInnerHeight() - columnHeaderHeight);
                int pos = getRowEndPosition(row);
                pos = Math.max(0, pos - innerHeight);
                scrollPane.setScrollPositionY(pos);
            }
        }
    }

    public int getAutoScrollDirection(Event evt, int autoScrollArea) {
        int areaY = getInnerY() + columnHeaderHeight;
        int areaHeight = getInnerHeight() - columnHeaderHeight;
        int mouseY = evt.getMouseY();
        if(mouseY >= areaY && mouseY < (areaY + areaHeight)) {
            mouseY -= areaY;
            if((mouseY <= autoScrollArea) || (areaHeight - mouseY) <= autoScrollArea) {
                // do a 2nd check in case the auto scroll areas overlap
                if(mouseY < areaHeight/2) {
                    return -1;
                } else {
                    return +1;
                }
            }
        }
        return 0;
    }

    public int getPageSizeX(int availableWidth) {
        return availableWidth;
    }

    public int getPageSizeY(int availableHeight) {
        return availableHeight - columnHeaderHeight;
    }

    public boolean isFixedWidthMode() {
        ScrollPane scrollPane = ScrollPane.getContainingScrollPane(this);
        if(scrollPane != null) {
            if(scrollPane.getFixed() != ScrollPane.Fixed.HORIZONTAL) {
                return false;
            }
        }
        return true;
    }

    protected final void checkRowIndex(int row) {
        if(row < 0 || row >= numRows) {
            throw new IndexOutOfBoundsException("row");
        }
    }

    protected final void checkColumnIndex(int column) {
        if(column < 0 || column >= numColumns) {
            throw new IndexOutOfBoundsException("column");
        }
    }

    protected final void checkRowRange(int idx, int count) {
        if(idx < 0 || count < 0 || count > numRows || idx > (numRows - count)) {
            throw new IllegalArgumentException("row");
        }
    }

    protected final void checkColumnRange(int idx, int count) {
        if(idx < 0 || count < 0 || count > numColumns || idx > (numColumns - count)) {
            throw new IllegalArgumentException("column");
        }
    }
    
    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        applyThemeTableBase(themeInfo);
        updateAll();
    }

    protected void applyThemeTableBase(ThemeInfo themeInfo) {
        this.tableBaseThemeInfo = themeInfo;
        this.imageColumnDivider = themeInfo.getImage("columnDivider");
        this.imageRowBackground = themeInfo.getImage("row.background");
        this.imageRowOverlay = themeInfo.getImage("row.overlay");
        this.imageRowDropMarker = themeInfo.getImage("row.dropmarker");
        this.rowHeight = themeInfo.getParameter("rowHeight", 32);
        this.defaultColumnWidth = themeInfo.getParameter("columnHeaderWidth", 256);
        this.columnHeaderHeight = themeInfo.getParameter("columnHeaderHeight", 10);
        this.columnDividerDragableDistance = themeInfo.getParameter("columnDividerDragableDistance", 3);
        this.ensureColumnHeaderMinWidth = themeInfo.getParameter("ensureColumnHeaderMinWidth", false);
        
        for(CellRenderer cellRenderer : cellRenderers.getUniqueValues()) {
            applyCellRendererTheme(cellRenderer);
        }
        applyCellRendererTheme(stringCellRenderer);
        updateAllColumnWidth = true;
    }

    @Override
    protected void applyThemeMouseCursor(ThemeInfo themeInfo) {
        this.columnResizeCursor = themeInfo.getMouseCursor("columnResizeCursor");
        this.normalCursor = themeInfo.getMouseCursor("mouseCursor");
        this.dragNotPossibleCursor = themeInfo.getMouseCursor("dragNotPossibleCursor");
    }
    
    protected void applyCellRendererTheme(CellRenderer cellRenderer) {
        String childThemeName = cellRenderer.getTheme();
        assert !isAbsoluteTheme(childThemeName);
        ThemeInfo childTheme = tableBaseThemeInfo.getChildTheme(childThemeName);
        if(childTheme != null) {
            cellRenderer.applyTheme(childTheme);
        }
    }

    @Override
    public void removeAllChildren() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void childAdded(Widget child) {
        // ignore
    }

    @Override
    protected void childRemoved(Widget exChild) {
        // ignore
    }

    protected int getOffsetX() {
        return getInnerX() - scrollPosX;
    }

    protected int getOffsetY() {
        return getInnerY() - scrollPosY + columnHeaderHeight;
    }

    @Override
    protected void positionChanged() {
        super.positionChanged();
        if(keyboardSearchHandler != null) {
            keyboardSearchHandler.updateInfoWindowPosition();
        }
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        if(isFixedWidthMode()) {
            updateAllColumnWidth = true;
        }
        if(keyboardSearchHandler != null) {
            keyboardSearchHandler.updateInfoWindowPosition();
        }
    }

    @Override
    protected Object getTooltipContentAt(int mouseX, int mouseY) {
        // use cached row/column
        if(lastMouseRow >= 0 && lastMouseRow < getNumRows() &&
                lastMouseColumn >= 0 && lastMouseColumn < getNumColumns()) {
            Object tooltip = getTooltipContentFromRow(lastMouseRow, lastMouseColumn);
            if(tooltip != null) {
                return tooltip;
            }
        }
        return super.getTooltipContentAt(mouseX, mouseY);
    }

    @Override
    protected void layout() {
        final int innerWidth = getInnerWidth();
        final int innerHeight = Math.max(0, getInnerHeight() - columnHeaderHeight);

        cellWidgetContainer.setPosition(getInnerX(), getInnerY() + columnHeaderHeight);
        cellWidgetContainer.setSize(innerWidth, innerHeight);

        if(updateAllColumnWidth) {
            updateAllColumnWidth();
        }
        if(autoSizeAllRows) {
            autoSizeAllRows();
        }
        if(updateAllCellWidgets) {
            updateAllCellWidgets();
        }

        final int scrollEndX = scrollPosX + innerWidth;
        final int scrollEndY = scrollPosY + innerHeight;

        int startRow = Math.min(numRows-1, Math.max(0, getRowFromPosition(scrollPosY)));
        int startColumn = Math.min(numColumns-1, Math.max(0, getColumnFromPosition(scrollPosX)));
        int endRow = Math.min(numRows-1, Math.max(startRow, getRowFromPosition(scrollEndY)));
        int endColumn = Math.min(numColumns-1, Math.max(startColumn, getColumnFromPosition(scrollEndX)));

        if(numRows > 0) {
            firstRowPartialVisible = getRowStartPosition(startRow) < scrollPosY;
            lastRowPartialVisible = getRowEndPosition(endRow) > scrollEndY;
        } else {
            firstRowPartialVisible = false;
            lastRowPartialVisible = false;
        }

        if(!widgetGrid.isEmpty()) {
            if(startRow > firstVisibleRow) {
                widgetGrid.iterate(firstVisibleRow, 0, startRow-1, numColumns, removeCellWidgetsFunction);
            }
            if(endRow < lastVisibleRow) {
                widgetGrid.iterate(endRow+1, 0, lastVisibleRow, numColumns, removeCellWidgetsFunction);
            }

            widgetGrid.iterate(startRow, 0, endRow, numColumns, insertCellWidgetsFunction);
        }

        firstVisibleRow = startRow;
        firstVisibleColumn = startColumn;
        lastVisibleRow = endRow;
        lastVisibleColumn = endColumn;

        if(numColumns > 0) {
            final int offsetX = getOffsetX();
            int colStartPos = getColumnStartPosition(0);
            for(int i=0 ; i<numColumns ; i++) {
                int colEndPos = getColumnEndPosition(i);
                Widget w = columnHeaders[i];
                if(w != null) {
                    assert w.getParent() == this;
                    w.setPosition(offsetX + colStartPos +
                            columnDividerDragableDistance, getInnerY());
                    w.setSize(Math.max(0, colEndPos - colStartPos -
                            2*columnDividerDragableDistance), columnHeaderHeight);
                    w.setVisible(columnHeaderHeight > 0);
                    AnimationState animationState = w.getAnimationState();
                    animationState.setAnimationState(STATE_FIRST_COLUMNHEADER, i == 0);
                    animationState.setAnimationState(STATE_LAST_COLUMNHEADER, i == numColumns-1);
                }
                colStartPos = colEndPos;
            }
        }
    }

    @Override
    protected void paintWidget(GUI gui) {
        if(firstVisibleRow < 0 || firstVisibleRow >= numRows) {
            return;
        }
        
        final int innerX = getInnerX();
        final int innerY = getInnerY() + columnHeaderHeight;
        final int innerWidth = getInnerWidth();
        final int innerHeight = getInnerHeight() - columnHeaderHeight;
        final int offsetX = getOffsetX();
        final int offsetY = getOffsetY();
        final Renderer renderer = gui.getRenderer();

        renderer.clipEnter(innerX, innerY, innerWidth, innerHeight);
        try {
            final AnimationState animState = getAnimationState();
            final int leadRow;
            final int leadColumn;
            final boolean isCellSelection;

            if(selectionManager != null) {
                leadRow = selectionManager.getLeadRow();
                leadColumn = selectionManager.getLeadColumn();
                isCellSelection = selectionManager.getSelectionGranularity() ==
                        TableSelectionManager.SelectionGranularity.CELLS;
            } else {
                leadRow = -1;
                leadColumn = -1;
                isCellSelection = false;
            }

            if(imageRowBackground != null) {
                paintRowImage(imageRowBackground, leadRow);
            }

            if(imageColumnDivider != null) {
                animState.setAnimationState(STATE_ROW_SELECTED, false);
                for(int col=firstVisibleColumn ; col<=lastVisibleColumn ; col++) {
                    int colEndPos = getColumnEndPosition(col);
                    int curX = offsetX + colEndPos;
                    imageColumnDivider.draw( animState, curX,innerY, 1, innerHeight);
                }
            }

            int rowStartPos = getRowStartPosition(firstVisibleRow);
            for(int row=firstVisibleRow ; row<=lastVisibleRow ; row++) {
                final int rowEndPos = getRowEndPosition(row);
                final int curRowHeight = rowEndPos - rowStartPos;
                final int curY = offsetY + rowStartPos;
                final TreeTableNode rowNode = getNodeFromRow(row);
                final boolean isRowSelected = !isCellSelection && isRowSelected(row);
                
                int colStartPos = getColumnStartPosition(firstVisibleColumn);
                for(int col=firstVisibleColumn ; col<=lastVisibleColumn ;) {
                    int colEndPos = getColumnEndPosition(col);
                    final CellRenderer cellRenderer = getCellRenderer(row, col, rowNode);
                    final boolean isCellSelected = isRowSelected || isCellSelected(row, col);

                    int curX = offsetX + colStartPos;
                    int colSpan = 1;

                    if(cellRenderer != null) {
                        colSpan = cellRenderer.getColumnSpan();
                        if(colSpan > 1) {
                            colEndPos = getColumnEndPosition(Math.max(numColumns-1, col+colSpan-1));
                        }

                        Widget cellRendererWidget = cellRenderer.getCellRenderWidget(
                                curX, curY, colEndPos - colStartPos, curRowHeight, isCellSelected);

                        if(cellRendererWidget != null) {
                            if(cellRendererWidget.getParent() != this) {
                                insertCellRenderer(cellRendererWidget);
                            }
                            paintChild(gui, cellRendererWidget);
                        }
                    }

                    col += Math.max(1, colSpan);
                    colStartPos = colEndPos;
                }

                rowStartPos = rowEndPos;
            }

            if(imageRowOverlay != null) {
                paintRowImage(imageRowOverlay, leadRow);
            }

            if(dropMarkerRow >= 0 && dropMarkerBeforeRow && imageRowDropMarker != null) {
                int y = (rowModel != null) ? rowModel.getPosition(dropMarkerRow) : (dropMarkerRow * rowHeight);
                imageRowDropMarker.draw(animState, getOffsetX(), getOffsetY() + y, columnModel.getEndPosition(), 1);
            }
        } finally {
            renderer.clipLeave();
        }
    }

    private void paintRowImage(Image img, int leadRow) {
        final AnimationState animState = getAnimationState();
        final int x = getOffsetX();
        final int width = columnModel.getEndPosition();
        final int offsetY = getOffsetY();
        
        int rowStartPos = getRowStartPosition(firstVisibleRow);
        for(int row=firstVisibleRow ; row<=lastVisibleRow ; row++) {
            final int rowEndPos = getRowEndPosition(row);
            final int curRowHeight = rowEndPos - rowStartPos;
            final int curY = offsetY + rowStartPos;

            animState.setAnimationState(STATE_ROW_SELECTED, isRowSelected(row));
            animState.setAnimationState(STATE_ROW_HOVER, dragActive == DRAG_INACTIVE &&
                    lastMouseY >= curY && lastMouseY < (curY + curRowHeight));
            animState.setAnimationState(STATE_LEAD_ROW, row == leadRow);
            animState.setAnimationState(STATE_ROW_DROPTARGET, !dropMarkerBeforeRow && row == dropMarkerRow);
            animState.setAnimationState(STATE_ROW_ODD, (row & 1) == 1);
            img.draw(animState, x, curY, width, curRowHeight);

            rowStartPos = rowEndPos;
        }
    }

    protected void insertCellRenderer(Widget widget) {
        int posX = widget.getX();
        int posY = widget.getY();
        widget.setVisible(false);
        super.insertChild(widget, super.getNumChildren());
        widget.setPosition(posX, posY);
    }

    protected abstract TreeTableNode getNodeFromRow(int row);
    protected abstract Object getCellData(int row, int column, TreeTableNode node);
    protected abstract Object getTooltipContentFromRow(int row, int column);

    protected boolean isRowSelected(int row) {
        if(selectionManager != null) {
            return selectionManager.isRowSelected(row);
        }
        return false;
    }

    protected boolean isCellSelected(int row, int column) {
        if(selectionManager != null) {
            return selectionManager.isCellSelected(row, column);
        }
        return false;
    }

    /**
     * Sets the default cell renderer for the specified column
     * The column numbers are not affected by model changes.
     * 
     * @param column the column, must eb &gt;= 0
     * @param cellRenderer the CellRenderer to use or null to restore the global default
     */
    public void setColumnDefaultCellRenderer(int column, CellRenderer cellRenderer) {
        if(column >= columnDefaultCellRenderer.length) {
            CellRenderer[] tmp = new CellRenderer[Math.max(column+1, numColumns)];
            System.arraycopy(columnDefaultCellRenderer, 0, tmp, 0, columnDefaultCellRenderer.length);
            columnDefaultCellRenderer = tmp;
        }

        columnDefaultCellRenderer[column] = cellRenderer;
    }
    
    /**
     * Returns the default cell renderer for the specified column
     * @param column the column, must eb &gt;= 0
     * @return the previously set CellRenderer or null if non was set
     */
    public CellRenderer getColumnDefaultCellRenderer(int column) {
        if(column < columnDefaultCellRenderer.length) {
            return columnDefaultCellRenderer[column];
        }
        return null;
    }
    
    protected CellRenderer getCellRendererNoDefault(Object data) {
        Class<? extends Object> dataClass = data.getClass();
        return cellRenderers.get(dataClass);
    }
    
    protected CellRenderer getDefaultCellRenderer(int col) {
        CellRenderer cellRenderer = getColumnDefaultCellRenderer(col);
        if(cellRenderer == null) {
            cellRenderer = stringCellRenderer;
        }
        return cellRenderer;
    }
    
    protected CellRenderer getCellRenderer(Object data, int col) {
        CellRenderer cellRenderer = getCellRendererNoDefault(data);
        if(cellRenderer == null) {
            cellRenderer = getDefaultCellRenderer(col);
        }
        return cellRenderer;
    }

    protected CellRenderer getCellRenderer(int row, int col, TreeTableNode node) {
        final Object data = getCellData(row, col, node);
        if(data != null) {
            CellRenderer cellRenderer = getCellRenderer(data, col);
            cellRenderer.setCellData(row, col, data);
            return cellRenderer;
        }
        return null;
    }

    protected int computeRowHeight(int row) {
        final TreeTableNode rowNode = getNodeFromRow(row);
        int height = 0;
        for(int column = 0; column < numColumns; column++) {
            CellRenderer cellRenderer = getCellRenderer(row, column, rowNode);
            if(cellRenderer != null) {
                height = Math.max(height, cellRenderer.getPreferredHeight());
                column += Math.max(cellRenderer.getColumnSpan() - 1, 0);
            }
        }
        return height;
    }

    protected int clampColumnWidth(int width) {
        return Math.max(2*columnDividerDragableDistance+1, width);
    }

    protected int computePreferredColumnWidth(int index) {
        return clampColumnWidth(columnHeaders[index].getPreferredWidth());
    }

    protected boolean autoSizeRow(int row) {
        int height = computeRowHeight(row);
        return rowModel.setSize(row, height);
    }

    protected void autoSizeAllRows() {
        if(rowModel != null) {
            rowModel.initializeAll(numRows);
        }
        autoSizeAllRows = false;
    }

    protected void removeCellWidget(Widget widget) {
        int idx = cellWidgetContainer.getChildIndex(widget);
        if(idx >= 0) {
            cellWidgetContainer.removeChild(idx);
        }
    }

    void insertCellWidget(int row, int column, WidgetEntry widgetEntry) {
        CellWidgetCreator cwc = (CellWidgetCreator)getCellRenderer(row, column, null);
        Widget widget = widgetEntry.widget;

        if(widget != null) {
            if(widget.getParent() != cellWidgetContainer) {
                cellWidgetContainer.insertChild(widget, cellWidgetContainer.getNumChildren());
            }

            int x = getColumnStartPosition(column);
            int w = getColumnEndPosition(column) - x;
            int y = getRowStartPosition(row);
            int h = getRowEndPosition(row) - y;

            cwc.positionWidget(widget, x + getOffsetX(), y + getOffsetY(), w, h);
        }
    }

    protected void updateCellWidget(int row, int column) {
        WidgetEntry we = (WidgetEntry)widgetGrid.get(row, column);
        Widget oldWidget = (we != null) ? we.widget : null;
        Widget newWidget = null;

        TreeTableNode rowNode = getNodeFromRow(row);
        CellRenderer cellRenderer = getCellRenderer(row, column, rowNode);
        if(cellRenderer instanceof CellWidgetCreator) {
            CellWidgetCreator cellWidgetCreator = (CellWidgetCreator)cellRenderer;
            if(we != null && we.creator != cellWidgetCreator) {
                // the cellWidgetCreator has changed for this cell
                // discard the old widget
                removeCellWidget(oldWidget);
                oldWidget = null;
            }
            newWidget = cellWidgetCreator.updateWidget(oldWidget);
            if(newWidget != null) {
                if(we == null) {
                    we = new WidgetEntry();
                    widgetGrid.set(row, column, we);
                }
                we.widget = newWidget;
                we.creator = cellWidgetCreator;
            }
        }

        if(newWidget == null && we != null) {
            widgetGrid.remove(row, column);
        }
        
        if(oldWidget != null && newWidget != oldWidget) {
            removeCellWidget(oldWidget);
        }
    }

    protected void updateAllCellWidgets() {
        if(!widgetGrid.isEmpty() || hasCellWidgetCreators) {
            for(int row=0 ; row<numRows ; row++) {
                for(int col=0 ; col<numColumns ; col++) {
                    updateCellWidget(row, col);
                }
            }
        }

        updateAllCellWidgets = false;
    }

    protected void removeAllCellWidgets() {
        cellWidgetContainer.removeAllChildren();
    }

    protected DialogLayout.Gap getColumnMPM(int column) {
        if(tableBaseThemeInfo != null) {
            ParameterMap columnWidthMap = tableBaseThemeInfo.getParameterMap("columnWidths");
            Object obj = columnWidthMap.getParameterValue(Integer.toString(column), false);
            if(obj instanceof DialogLayout.Gap) {
                return (DialogLayout.Gap)obj;
            }
            if(obj instanceof Integer) {
                return new DialogLayout.Gap(((Integer)obj).intValue());
            }
        }
        return null;
    }

    protected ColumnHeader createColumnHeader(int column) {
        ColumnHeader btn = new ColumnHeader();
        btn.setTheme("columnHeader");
        btn.setCanAcceptKeyboardFocus(false);
        super.insertChild(btn, super.getNumChildren());
        return btn;
    }

    protected void updateColumnHeader(int column) {
        Button columnHeader = columnHeaders[column];
        columnHeader.setText(columnHeaderModel.getColumnHeaderText(column));
        StateKey[] states = columnHeaderModel.getColumnHeaderStates();
        if(states.length > 0) {
            AnimationState animationState = columnHeader.getAnimationState();
            for(int i=0 ; i<states.length ; i++) {
                animationState.setAnimationState(states[i],
                        columnHeaderModel.getColumnHeaderState(column, i));
            }
        }
    }

    protected void updateColumnHeaderNumbers() {
        for(int i=0 ; i<columnHeaders.length ; i++) {
            columnHeaders[i].column = i;
        }
    }
    
    private void removeColumnHeaders(int column, int count) throws IndexOutOfBoundsException {
        for(int i = 0 ; i < count ; i++) {
            int idx = super.getChildIndex(columnHeaders[column + i]);
            if(idx >= 0) {
                super.removeChild(idx);
            }
        }
    }

    protected boolean isMouseInColumnHeader(int y) {
        y -= getInnerY();
        return y >= 0 && y < columnHeaderHeight;
    }

    protected int getColumnSeparatorUnderMouse(int x) {
        x -= getOffsetX();
        x += columnDividerDragableDistance;
        int col = columnModel.getIndex(x);
        int dist = x - columnModel.getPosition(col);
        if(dist < 2*columnDividerDragableDistance) {
            return col - 1;
        }
        return -1;
    }

    protected int getRowUnderMouse(int y) {
        y -= getOffsetY();
        int row = getRowFromPosition(y);
        return row;
    }

    protected int getColumnUnderMouse(int x) {
        x -= getOffsetX();
        int col = columnModel.getIndex(x);
        return col;
    }

    @Override
    protected boolean handleEvent(Event evt) {
        if(dragActive != DRAG_INACTIVE) {
            return handleDragEvent(evt);
        }

        if(evt.isKeyEvent() &&
                keyboardSearchHandler != null &&
                keyboardSearchHandler.isActive() &&
                keyboardSearchHandler.handleKeyEvent(evt)) {
            return true;
        }
        
        if(super.handleEvent(evt)) {
            return true;
        }

        if(evt.isMouseEvent()) {
            return handleMouseEvent(evt);
        }

        if(evt.isKeyEvent() &&
                keyboardSearchHandler != null &&
                keyboardSearchHandler.handleKeyEvent(evt)) {
            return true;
        }

        return false;
    }

    @Override
    protected boolean handleKeyStrokeAction(String action, Event event) {
        if(!super.handleKeyStrokeAction(action, event)) {
            if(selectionManager == null) {
                return false;
            }
            if(!selectionManager.handleKeyStrokeAction(action, event)) {
                return false;
            }
        }
        // remove focus from childs
        requestKeyboardFocus(null);
        return true;
    }

    protected static final int DRAG_INACTIVE      = 0;
    protected static final int DRAG_COLUMN_HEADER = 1;
    protected static final int DRAG_USER          = 2;
    protected static final int DRAG_IGNORE        = 3;

    protected int dragActive;
    protected int dragColumn;
    protected int dragStartX;
    protected int dragStartColWidth;
    protected int dragStartSumWidth;
    protected MouseCursor dragCursor;

    protected void cancelDragging() {
        if(dragActive == DRAG_USER) {
            if(dragListener != null) {
                dragListener.dragCanceled();
            }
            dragActive = DRAG_IGNORE;
        }
    }

    protected boolean handleDragEvent(Event evt) {
        if(evt.isMouseEvent()) {
            return handleMouseEvent(evt);
        }

        if(evt.isKeyPressedEvent() && evt.getKeyCode() == Event.KEY_ESCAPE) {
            switch(dragActive) {
                case DRAG_USER:
                    cancelDragging();
                    break;
                case DRAG_COLUMN_HEADER:
                    columnHeaderDragged(dragStartColWidth);
                    dragActive = DRAG_IGNORE;
                    break;
            }
            dragCursor = null;
        }

        return true;
    }

    void mouseLeftTableArea() {
        lastMouseY = LAST_MOUSE_Y_OUTSIDE;
        lastMouseRow = -1;
        lastMouseColumn = -1;
    }

    @Override
    Widget routeMouseEvent(Event evt) {
        if(evt.getType() == Event.Type.MOUSE_EXITED) {
            mouseLeftTableArea();
        } else {
            lastMouseY = evt.getMouseY();
        }

        if(dragActive == DRAG_INACTIVE) {
            boolean inHeader = isMouseInColumnHeader(evt.getMouseY());
            if(inHeader) {
                if(lastMouseRow != -1 || lastMouseColumn != -1) {
                    lastMouseRow = -1;
                    lastMouseColumn = -1;
                    resetTooltip();
                }
            } else {
                final int row = getRowUnderMouse(evt.getMouseY());
                final int column = getColumnUnderMouse(evt.getMouseX());

                if(lastMouseRow != row || lastMouseColumn != column) {
                    lastMouseRow = row;
                    lastMouseColumn = column;
                    resetTooltip();
                }
            }
        }

        return super.routeMouseEvent(evt);
    }

    protected boolean handleMouseEvent(Event evt) {
        final Event.Type evtType = evt.getType();

        if(dragActive != DRAG_INACTIVE) {
            switch(dragActive) {
                case DRAG_COLUMN_HEADER: {
                    final int innerWidth = getInnerWidth();
                    if(dragColumn >= 0 && innerWidth > 0) {
                        int newWidth = clampColumnWidth(evt.getMouseX() - dragStartX);
                        columnHeaderDragged(newWidth);
                    }
                    break;
                }
                case DRAG_USER: {
                    dragCursor = dragListener.dragged(evt);
                    if(evt.isMouseDragEnd()) {
                        dragListener.dragStopped(evt);
                    }
                    break;
                }
                case DRAG_IGNORE:
                    break;
                default:
                    throw new AssertionError();
            }
            if(evt.isMouseDragEnd()) {
                dragActive = DRAG_INACTIVE;
                dragCursor = null;
            }
            return true;
        }

        boolean inHeader = isMouseInColumnHeader(evt.getMouseY());
        if(inHeader) {
            final int column = getColumnSeparatorUnderMouse(evt.getMouseX());
            final boolean fixedWidthMode = isFixedWidthMode();

            // lastMouseRow and lastMouseColumn have been updated in routeMouseEvent()
            
            if(column >= 0 && (column < getNumColumns()-1 || !fixedWidthMode)) {
                if(evtType == Event.Type.MOUSE_BTNDOWN) {
                    dragStartColWidth = getColumnWidth(column);
                    dragColumn = column;
                    dragStartX = evt.getMouseX() - dragStartColWidth;
                    if(fixedWidthMode) {
                        for(int i=0 ; i<numColumns ; ++i) {
                            columnHeaders[i].setColumnWidth(getColumnWidth(i));
                        }
                        dragStartSumWidth = dragStartColWidth + getColumnWidth(column+1);
                    }
                }

                if(evt.isMouseDragEvent()) {
                    dragActive = DRAG_COLUMN_HEADER;
                }
                return true;
            }
        } else {
            // lastMouseRow and lastMouseColumn have been updated in routeMouseEvent()
            final int row = lastMouseRow;
            final int column = lastMouseColumn;

            if(evt.isMouseDragEvent()) {
                if(dragListener != null && dragListener.dragStarted(row, row, evt)) {
                    dragCursor = dragListener.dragged(evt);
                    dragActive = DRAG_USER;
                } else {
                    dragActive = DRAG_IGNORE;
                }
                return true;
            }

            if(selectionManager != null) {
                selectionManager.handleMouseEvent(row, column, evt);
            }
            
            if(evtType == Event.Type.MOUSE_CLICKED && evt.getMouseClickCount() == 2) {
                if(callbacks != null) {
                    for(Callback cb : callbacks) {
                        cb.mouseDoubleClicked(row, column);
                    }
                }
            }

            if(evtType == Event.Type.MOUSE_BTNUP && evt.getMouseButton() == Event.MOUSE_RBUTTON) {
                if(callbacks != null) {
                    for(Callback cb : callbacks) {
                        cb.mouseRightClick(row, column, evt);
                    }
                }
            }
        }
        
        // let ScrollPane handle mouse wheel
        return evtType != Event.Type.MOUSE_WHEEL;
    }

    @Override
    public MouseCursor getMouseCursor(Event evt) {
        switch(dragActive) {
            case DRAG_COLUMN_HEADER:
                return columnResizeCursor;
            case DRAG_USER:
                return dragCursor;
            case DRAG_IGNORE:
                return dragNotPossibleCursor;
        }
        
        boolean inHeader = isMouseInColumnHeader(evt.getMouseY());
        if(inHeader) {
            final int column = getColumnSeparatorUnderMouse(evt.getMouseX());
            final boolean fixedWidthMode = isFixedWidthMode();

            // lastMouseRow and lastMouseColumn have been updated in routeMouseEvent()
            
            if(column >= 0 && (column < getNumColumns()-1 || !fixedWidthMode)) {
                return columnResizeCursor;
            }
        }
        
        return normalCursor;
    }

    private void columnHeaderDragged(int newWidth) {
        if(isFixedWidthMode()) {
            assert dragColumn+1 < numColumns;
            newWidth = Math.min(newWidth, dragStartSumWidth - 2*columnDividerDragableDistance);
            columnHeaders[dragColumn  ].setColumnWidth(newWidth);
            columnHeaders[dragColumn+1].setColumnWidth(dragStartSumWidth - newWidth);
            updateAllColumnWidth = true;
            invalidateLayout();
        } else {
            setColumnWidth(dragColumn, newWidth);
        }
    }

    protected void columnHeaderClicked(int column) {
        if(callbacks != null) {
            for(Callback cb : callbacks) {
                cb.columnHeaderClicked(column);
            }
        }
    }

    protected void updateAllColumnWidth() {
        if(getInnerWidth() > 0) {
            columnModel.initializeAll(numColumns);
            updateAllColumnWidth = false;
        }
    }

    protected void updateAll() {
        if(!widgetGrid.isEmpty()) {
            removeAllCellWidgets();
            widgetGrid.clear();
        }

        if(rowModel != null) {
            autoSizeAllRows = true;
        }

        updateAllCellWidgets = true;
        updateAllColumnWidth = true;
        invalidateLayout();
    }

    protected void modelAllChanged() {
        if(columnHeaders != null) {
            removeColumnHeaders(0, columnHeaders.length);
        }

        dropMarkerRow = -1;
        columnHeaders = new ColumnHeader[numColumns];
        for(int i=0 ; i<numColumns ; i++) {
            columnHeaders[i] = createColumnHeader(i);
            updateColumnHeader(i);
        }
        updateColumnHeaderNumbers();
        
        if(selectionManager != null) {
            selectionManager.modelChanged();
        }
        
        updateAll();
    }

    protected void modelRowChanged(int row) {
        if(rowModel != null) {
            if(autoSizeRow(row)) {
                invalidateLayout();
            }
        }
        for(int col=0 ; col<numColumns ; col++) {
            updateCellWidget(row, col);
        }
        invalidateLayoutLocally();
    }

    protected void modelRowsChanged(int idx, int count) {
        checkRowRange(idx, count);
        boolean rowHeightChanged = false;
        for(int i=0 ; i<count ; i++) {
            if(rowModel != null) {
                rowHeightChanged |= autoSizeRow(idx+i);
            }
            for(int col=0 ; col<numColumns ; col++) {
                updateCellWidget(idx+i, col);
            }
        }
        invalidateLayoutLocally();
        if(rowHeightChanged) {
            invalidateLayout();
        }
    }

    protected void modelCellChanged(int row, int column) {
        checkRowIndex(row);
        checkColumnIndex(column);
        if(rowModel != null) {
            autoSizeRow(row);
        }
        updateCellWidget(row, column);
        invalidateLayout();
    }

    protected void modelRowsInserted(int row, int count) {
        checkRowRange(row, count);
        if(rowModel != null) {
            rowModel.insert(row, count);
        }
        if(dropMarkerRow > row || (dropMarkerRow == row && dropMarkerBeforeRow)) {
            dropMarkerRow += count;
        }
        if(!widgetGrid.isEmpty() || hasCellWidgetCreators) {
            removeAllCellWidgets();
            widgetGrid.insertRows(row, count);

            for(int i=0 ; i<count ; i++) {
                for(int col=0 ; col<numColumns ; col++) {
                    updateCellWidget(row+i, col);
                }
            }
        }
        // invalidateLayout() before sp.setScrollPositionY() as this may cause a
        // call to invalidateLayoutLocally() which is redundant.
        invalidateLayout();
        if(row < getRowFromPosition(scrollPosY)) {
            ScrollPane sp = ScrollPane.getContainingScrollPane(this);
            if(sp != null) {
                int rowsStart = getRowStartPosition(row);
                int rowsEnd = getRowEndPosition(row + count - 1);
                sp.setScrollPositionY(scrollPosY + rowsEnd - rowsStart);
            }
        }
        if(selectionManager != null) {
            selectionManager.rowsInserted(row, count);
        }
    }

    protected void modelRowsDeleted(int row, int count) {
        if(row+count <= getRowFromPosition(scrollPosY)) {
            ScrollPane sp = ScrollPane.getContainingScrollPane(this);
            if(sp != null) {
                int rowsStart = getRowStartPosition(row);
                int rowsEnd = getRowEndPosition(row + count - 1);
                sp.setScrollPositionY(scrollPosY - rowsEnd + rowsStart);
            }
        }
        if(rowModel != null) {
            rowModel.remove(row, count);
        }
        if(dropMarkerRow >= row) {
            if(dropMarkerRow < (row + count)) {
                dropMarkerRow = -1;
            } else {
                dropMarkerRow -= count;
            }
        }
        if(!widgetGrid.isEmpty()) {
            widgetGrid.iterate(row, 0, row+count-1, numColumns, removeCellWidgetsFunction);
            widgetGrid.removeRows(row, count);
        }
        if(selectionManager != null) {
            selectionManager.rowsDeleted(row, count);
        }
        invalidateLayout();
    }

    protected void modelColumnsInserted(int column, int count) {
        checkColumnRange(column, count);
        ColumnHeader[] newColumnHeaders = new ColumnHeader[numColumns];
        System.arraycopy(columnHeaders, 0, newColumnHeaders, 0, column);
        System.arraycopy(columnHeaders, column, newColumnHeaders, column+count,
                numColumns - (column+count));
        for(int i=0 ; i<count ; i++) {
            newColumnHeaders[column+i] = createColumnHeader(column+i);
        }
        columnHeaders = newColumnHeaders;
        updateColumnHeaderNumbers();

        columnModel.insert(column, count);

        if(!widgetGrid.isEmpty() || hasCellWidgetCreators) {
            removeAllCellWidgets();
            widgetGrid.insertColumns(column, count);

            for(int row=0 ; row<numRows ; row++) {
                for(int i=0 ; i<count ; i++) {
                    updateCellWidget(row, column + i);
                }
            }
        }
        if(column < getColumnStartPosition(scrollPosX)) {
            ScrollPane sp = ScrollPane.getContainingScrollPane(this);
            if(sp != null) {
                int columnsStart = getColumnStartPosition(column);
                int columnsEnd = getColumnEndPosition(column + count - 1);
                sp.setScrollPositionX(scrollPosX + columnsEnd - columnsStart);
            }
        }
        invalidateLayout();
    }

    protected void modelColumnsDeleted(int column, int count) {
        if(column+count <= getColumnStartPosition(scrollPosX)) {
            ScrollPane sp = ScrollPane.getContainingScrollPane(this);
            if(sp != null) {
                int columnsStart = getColumnStartPosition(column);
                int columnsEnd = getColumnEndPosition(column + count - 1);
                sp.setScrollPositionY(scrollPosX - columnsEnd + columnsStart);
            }
        }
        columnModel.remove(column, count);
        if(!widgetGrid.isEmpty()) {
            widgetGrid.iterate(0, column, numRows, column+count-1, removeCellWidgetsFunction);
            widgetGrid.removeColumns(column, count);
        }

        removeColumnHeaders(column, count);

        ColumnHeader[] newColumnHeaders = new ColumnHeader[numColumns];
        System.arraycopy(columnHeaders, 0, newColumnHeaders, 0, column);
        System.arraycopy(columnHeaders, column+count, newColumnHeaders, column, numColumns - count);
        columnHeaders = newColumnHeaders;
        updateColumnHeaderNumbers();
        
        invalidateLayout();
    }

    protected void modelColumnHeaderChanged(int column) {
        checkColumnIndex(column);
        updateColumnHeader(column);
    }

    class RowSizeSequence extends SizeSequence {
        public RowSizeSequence(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        protected void initializeSizes(int index, int count) {
            for(int i=0 ; i<count ; i++,index++) {
                table[index] = computeRowHeight(index);
            }
        }
    }

    protected class ColumnSizeSequence extends SizeSequence {
        @Override
        protected void initializeSizes(int index, int count) {
            boolean useSprings = isFixedWidthMode();
            if(!useSprings) {
                int sum = 0;
                for(int i=0 ; i<count ; i++) {
                    int width = computePreferredColumnWidth(index+i);
                    table[index+i] = width;
                    sum += width;
                }
                useSprings = sum < getInnerWidth();
            }
            if(useSprings) {
                computeColumnHeaderLayout();
                for(int i=0 ; i<count ; i++) {
                    table[index+i] = clampColumnWidth(columnHeaders[i].springWidth);
                }
            }
        }
        protected boolean update(int index) {
            int width;
            if(isFixedWidthMode()) {
                computeColumnHeaderLayout();
                width = clampColumnWidth(columnHeaders[index].springWidth);
            } else {
                width = computePreferredColumnWidth(index);
                if(ensureColumnHeaderMinWidth) {
                    width = Math.max(width, columnHeaders[index].getMinWidth());
                }
            }
            return setSize(index, width);
        }
        void computeColumnHeaderLayout() {
            if(columnHeaders != null) {
                DialogLayout.SequentialGroup g = (DialogLayout.SequentialGroup)(new DialogLayout()).createSequentialGroup();
                for(ColumnHeader h : columnHeaders) {
                    g.addSpring(h.spring);
                }
                g.setSize(DialogLayout.AXIS_X, 0, getInnerWidth());
            }
        }
        int computePreferredWidth() {
            int count = getNumColumns();
            if(!isFixedWidthMode()) {
                int sum = 0;
                for(int i=0 ; i<count ; i++) {
                    int width = computePreferredColumnWidth(i);
                    sum += width;
                }
                return sum;
            }
            if(columnHeaders != null) {
                DialogLayout.SequentialGroup g = (DialogLayout.SequentialGroup)(new DialogLayout()).createSequentialGroup();
                for(ColumnHeader h : columnHeaders) {
                    g.addSpring(h.spring);
                }
                return g.getPrefSize(DialogLayout.AXIS_X);
            }
            return 0;
        }
    }

    class RemoveCellWidgets implements SparseGrid.GridFunction {
        public void apply(int row, int column, Entry e) {
            WidgetEntry widgetEntry = (WidgetEntry)e;
            Widget widget = widgetEntry.widget;
            if(widget != null) {
                removeCellWidget(widget);
            }
        }
    }

    class InsertCellWidgets implements SparseGrid.GridFunction {
        public void apply(int row, int column, Entry e) {
            insertCellWidget(row, column, (WidgetEntry)e);
        }
    }

    protected class ColumnHeader extends Button implements Runnable {
        int column;
        private int columnWidth;
        int springWidth;

        @SuppressWarnings("LeakingThisInConstructor")
        public ColumnHeader() {
            addCallback(this);
        }

        final DialogLayout.Spring spring = new DialogLayout.Spring() {
            @Override
            int getMinSize(int axis) {
                return clampColumnWidth(getMinWidth());
            }
            @Override
            int getPrefSize(int axis) {
                return getPreferredWidth();
            }
            @Override
            int getMaxSize(int axis) {
                return getMaxWidth();
            }
            @Override
            void setSize(int axis, int pos, int size) {
                springWidth = size;
            }
        };

        public int getColumnWidth() {
            return columnWidth;
        }

        public void setColumnWidth(int columnWidth) {
            this.columnWidth = columnWidth;
        }

        @Override
        public int getPreferredWidth() {
            if(columnWidth > 0) {
                return columnWidth;
            }
            DialogLayout.Gap mpm = getColumnMPM(column);
            int prefWidth = (mpm != null) ? mpm.preferred : defaultColumnWidth;
            return Math.max(prefWidth, super.getPreferredWidth());
        }

        @Override
        public int getMinWidth() {
            DialogLayout.Gap mpm = getColumnMPM(column);
            int minWidth = (mpm != null) ? mpm.min : 0;
            return Math.max(minWidth, super.getPreferredWidth());
        }
        
        @Override
        public int getMaxWidth() {
            DialogLayout.Gap mpm = getColumnMPM(column);
            int maxWidth = (mpm != null) ? mpm.max : 32767;
            return maxWidth;
        }

        @Override
        public void adjustSize() {
            // don't do anything
        }

        @Override
        protected boolean handleEvent(Event evt) {
            if(evt.isMouseEventNoWheel()) {
                mouseLeftTableArea();
            }
            return super.handleEvent(evt);
        }

        @Override
        protected void paintWidget(GUI gui) {
            Renderer renderer = gui.getRenderer();
            renderer.clipEnter(getX(), getY(), getWidth(), getHeight());
            try {
                paintLabelText(getAnimationState());
            } finally {
                renderer.clipLeave();
            }
        }

        public void run() {
            columnHeaderClicked(column);
        }
    }

    static class WidgetEntry extends SparseGrid.Entry {
        Widget widget;
        CellWidgetCreator creator;
    }

    static class CellWidgetContainer extends Widget {
        CellWidgetContainer() {
            setTheme("");
            setClip(true);
        }

        @Override
        protected void childInvalidateLayout(Widget child) {
            // always ignore
        }

        @Override
        protected void sizeChanged() {
            // always ignore
        }

        @Override
        protected void childAdded(Widget child) {
            // always ignore
        }

        @Override
        protected void childRemoved(Widget exChild) {
            // always ignore
        }

        @Override
        protected void allChildrenRemoved() {
            // always ignore
        }
    }

    public static class StringCellRenderer extends TextWidget implements CellRenderer {
        public StringCellRenderer() {
            setCache(false);
            setClip(true);
        }

        @Override
        public void applyTheme(ThemeInfo themeInfo) {
            super.applyTheme(themeInfo);
        }

        public void setCellData(int row, int column, Object data) {
            setCharSequence(String.valueOf(data));
        }

        public int getColumnSpan() {
            return 1;
        }

        @Override
        protected void sizeChanged() {
            // this method is overriden to prevent Widget.sizeChanged() from
            // calling invalidateLayout().
            // StringCellRenderer is used as a stamp and does not participate
            // in layouts - so invalidating the layout would lead to many
            // or even constant relayouts and bad performance
        }

        public Widget getCellRenderWidget(int x, int y, int width, int height, boolean isSelected) {
            setPosition(x, y);
            setSize(width, height);
            getAnimationState().setAnimationState(STATE_SELECTED, isSelected);
            return this;
        }
    }
}
