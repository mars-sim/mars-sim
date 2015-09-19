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

import de.matthiasmann.twl.renderer.AnimationState.StateKey;

/**
 * <p>A scroll pane to scroll another widget if it requires more space then
 * available.</p>
 *
 * <p>It requires the following child themes:</p><table>
 * <tr><th>Theme</th><th>Description</th></tr>
 * <tr><td>hscrollbar</td><td>The horizontal scrollbar</td></tr>
 * <tr><td>vscrollbar</td><td>The vertical scrollbar</td></tr>
 * <tr><td>dragButton</td><td>The drag button in the bottom right corner.
 *     Only needed when hasDragButton is true.</td></tr>
 * </table><br/>
 * For the remaining theme parameters look at {@link #applyThemeScrollPane(de.matthiasmann.twl.ThemeInfo) }
 *
 * @author Matthias Mann
 */
public class ScrollPane extends Widget {

    public static final StateKey STATE_DOWNARROW_ARMED = StateKey.get("downArrowArmed");
    public static final StateKey STATE_RIGHTARROW_ARMED = StateKey.get("rightArrowArmed");
    public static final StateKey STATE_HORIZONTAL_SCROLLBAR_VISIBLE = StateKey.get("horizontalScrollbarVisible");
    public static final StateKey STATE_VERTICAL_SCROLLBAR_VISIBLE = StateKey.get("verticalScrollbarVisible");
    public static final StateKey STATE_AUTO_SCROLL_UP = StateKey.get("autoScrollUp");
    public static final StateKey STATE_AUTO_SCROLL_DOWN = StateKey.get("autoScrollDown");

    /**
     * Controls which axis of the scroll pane should be fixed
     */
    public enum Fixed {
        /**
         * No axis is fixed - the scroll pane may show 2 scroll bars
         */
        NONE,
        /**
         * The horizontal axis is fixed - only a vertical scroll bar may be shown
         */
        HORIZONTAL,
        /**
         * The vertical axis is fixed - only a horizontal scroll bar may be shown
         */
        VERTICAL
    }

    /**
     * Indicates that the content handles scrolling itself.
     *
     * This interfaces also allows for a larger scrollable size then
     * the Widget size limitations.
     *
     * The {@code ScrollPane} will set the size of content to the available
     * content area.
     */
    public interface Scrollable {
        /**
         * Called when the content is scrolled either by a call to
         * {@link ScrollPane#setScrollPositionX(int) },
         * {@link ScrollPane#setScrollPositionY(int) } or
         * through one of the scrollbars.
         *
         * @param scrollPosX the new horizontal scroll position. Always &gt;= 0.
         * @param scrollPosY the new vertical scroll position. Always &gt;= 0.
         */
        public void setScrollPosition(int scrollPosX, int scrollPosY);
    }

    /**
     * Custom auto scroll area checking. This is needed when the content
     * has column headers.
     */
    public interface AutoScrollable {
        /**
         * Returns the auto scroll direction for the specified mouse event.
         *
         * @param evt the mouse event which could trigger an auto scroll
         * @param autoScrollArea the size of the auto scroll area.
         *     This is a theme parameter of the {@link ScrollPane}
         * @return the auto scroll direction.
         *     -1 for upwards
         *      0 for no auto scrolling
         *     +1 for downwards
         * @see ScrollPane#checkAutoScroll(de.matthiasmann.twl.Event)
         */
        public int getAutoScrollDirection(Event evt, int autoScrollArea);
    }

    /**
     * Custom page sizes for page scrolling and scroll bar thumb sizing.
     * This is needed when the content has column or row headers.
     */
    public interface CustomPageSize {
        /**
         * Computes the horizontal page size based on the available width.
         * @param availableWidth the available width (the visible area)
         * @return the page size. Must be &gt; 0 and &lt;= availableWidth
         */
        public int getPageSizeX(int availableWidth);
        /**
         * Computes the vertical page size based on the available height.
         * @param availableHeight the available height (the visible area)
         * @return the page size. Must be &gt; 0 and &lt;= availableHeight
         */
        public int getPageSizeY(int availableHeight);
    }

    private static final int AUTO_SCROLL_DELAY = 50;

    final Scrollbar scrollbarH;
    final Scrollbar scrollbarV;
    private final Widget contentArea;
    private DraggableButton dragButton;
    private Widget content;
    private Fixed fixed = Fixed.NONE;
    private Dimension hscrollbarOffset = Dimension.ZERO;
    private Dimension vscrollbarOffset = Dimension.ZERO;
    private Dimension contentScrollbarSpacing = Dimension.ZERO;
    private boolean inLayout;
    private boolean expandContentSize;
    private boolean scrollbarsAlwaysVisible;
    private int scrollbarsToggleFlags;
    private int autoScrollArea;
    private int autoScrollSpeed;
    private Timer autoScrollTimer;
    private int autoScrollDirection;

    public ScrollPane() {
        this(null);
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public ScrollPane(Widget content) {
        this.scrollbarH = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
        this.scrollbarV = new Scrollbar(Scrollbar.Orientation.VERTICAL);
        this.contentArea = new Widget();

        Runnable cb = new Runnable() {
            public void run() {
                scrollContent();
            }
        };

        scrollbarH.addCallback(cb);
        scrollbarH.setVisible(false);
        scrollbarV.addCallback(cb);
        scrollbarV.setVisible(false);
        contentArea.setClip(true);
        contentArea.setTheme("");
        
        super.insertChild(contentArea, 0);
        super.insertChild(scrollbarH, 1);
        super.insertChild(scrollbarV, 2);
        setContent(content);
        setCanAcceptKeyboardFocus(true);
    }

    public Fixed getFixed() {
        return fixed;
    }

    /**
     * Controls if this scroll pane has a fixed axis which will not show a scrollbar.
     * 
     * Default is {@link Fixed#NONE}
     *
     * @param fixed the fixed axis.
     */
    public void setFixed(Fixed fixed) {
        if(fixed == null) {
            throw new NullPointerException("fixed");
        }
        if(this.fixed != fixed) {
            this.fixed = fixed;
            invalidateLayout();
        }
    }

    public Widget getContent() {
        return content;
    }

    /**
     * Sets the widget which should be scrolled.
     *
     * <p>The following interfaces change the behavior of the scroll pane when
     * they are implemented by the content:</p><ul>
     * <li>{@link Scrollable}</li>
     * <li>{@link AutoScrollable}</li>
     * <li>{@link CustomPageSize}</li>
     * </ul>
     * @param content the new scroll pane content
     */
    public void setContent(Widget content) {
        if(this.content != null) {
            contentArea.removeAllChildren();
            this.content = null;
        }
        if(content != null) {
            this.content = content;
            contentArea.add(content);
        }
    }

    public boolean isExpandContentSize() {
        return expandContentSize;
    }

    /**
     * Control if the content size.
     *
     * If set to true then the content size will be the larger of it's preferred
     * size and the size of the content area.
     * If set to false then the content size will be it's preferred area.
     *
     * Default is false
     * 
     * @param expandContentSize true if the content should always cover the content area
     */
    public void setExpandContentSize(boolean expandContentSize) {
        if(this.expandContentSize != expandContentSize) {
            this.expandContentSize = expandContentSize;
            invalidateLayoutLocally();
        }
    }

    /**
     * Forces a layout of the scroll pane content to update the ranges of the
     * scroll bars.
     * 
     * This method should be called after changes to the content which might
     * affect it's size and before computing a new scroll position.
     *
     * @see #scrollToAreaX(int, int, int)
     * @see #scrollToAreaY(int, int, int)
     */
    public void updateScrollbarSizes() {
        invalidateLayoutLocally();
        validateLayout();
    }

    public int getScrollPositionX() {
        return scrollbarH.getValue();
    }

    public int getMaxScrollPosX() {
        return scrollbarH.getMaxValue();
    }

    public void setScrollPositionX(int pos) {
        scrollbarH.setValue(pos);
    }

    /**
     * Tries to make the specified horizontal area completely visible. If it is
     * larger then the horizontal page size then it scrolls to the start of the area.
     *
     * @param start the position of the area
     * @param size size of the area
     * @param extra the extra space which should be visible around the area
     * @see Scrollbar#scrollToArea(int, int, int)
     */
    public void scrollToAreaX(int start, int size, int extra) {
        scrollbarH.scrollToArea(start, size, extra);
    }

    public int getScrollPositionY() {
        return scrollbarV.getValue();
    }

    public int getMaxScrollPosY() {
        return scrollbarV.getMaxValue();
    }

    public void setScrollPositionY(int pos) {
        scrollbarV.setValue(pos);
    }

    /**
     * Tries to make the specified vertical area completely visible. If it is
     * larger then the vertical page size then it scrolls to the start of the area.
     *
     * @param start the position of the area
     * @param size size of the area
     * @param extra the extra space which should be visible around the area
     * @see Scrollbar#scrollToArea(int, int, int)
     */
    public void scrollToAreaY(int start, int size, int extra) {
        scrollbarV.scrollToArea(start, size, extra);
    }

    public int getContentAreaWidth() {
        return contentArea.getWidth();
    }

    public int getContentAreaHeight() {
        return contentArea.getHeight();
    }

    /**
     * Returns the horizontal scrollbar widget, be very careful with changes to it.
     * @return the horizontal scrollbar
     */
    public Scrollbar getHorizontalScrollbar() {
        return scrollbarH;
    }

    /**
     * Returns the vertical scrollbar widget, be very careful with changes to it.
     * @return the vertical scrollbar
     */
    public Scrollbar getVerticalScrollbar() {
        return scrollbarV;
    }

    /**
     * Creates a DragListener which can be used to drag the content of this ScrollPane around.
     * @return a DragListener to scroll this this ScrollPane.
     */
    public DraggableButton.DragListener createDragListener() {
        return new DraggableButton.DragListener() {
            int startScrollX;
            int startScrollY;
            public void dragStarted() {
                startScrollX = getScrollPositionX();
                startScrollY = getScrollPositionY();
            }
            public void dragged(int deltaX, int deltaY) {
                setScrollPositionX(startScrollX - deltaX);
                setScrollPositionY(startScrollY - deltaY);
            }
            public void dragStopped() {
            }
        };
    }

    /**
     * Checks for an auto scroll event. This should be called when a drag & drop
     * operation is in progress and the drop target is inside a scroll pane.
     *
     * @param evt the mouse event which should be checked.
     * @return true if auto scrolling is started/active.
     * @see #stopAutoScroll()
     */
    public boolean checkAutoScroll(Event evt) {
        GUI gui = getGUI();
        if(gui == null) {
            stopAutoScroll();
            return false;
        }

        autoScrollDirection = getAutoScrollDirection(evt);
        if(autoScrollDirection == 0) {
            stopAutoScroll();
            return false;
        }

        setAutoScrollMarker();

        if(autoScrollTimer == null) {
            autoScrollTimer = gui.createTimer();
            autoScrollTimer.setContinuous(true);
            autoScrollTimer.setDelay(AUTO_SCROLL_DELAY);
            autoScrollTimer.setCallback(new Runnable() {
                public void run() {
                    doAutoScroll();
                }
            });
            doAutoScroll();
        }
        autoScrollTimer.start();
        return true;
    }

    /**
     * Stops an activate auto scroll. This must be called when the drag & drop
     * operation is finished.
     *
     * @see #checkAutoScroll(de.matthiasmann.twl.Event)
     */
    public void stopAutoScroll() {
        if(autoScrollTimer != null) {
            autoScrollTimer.stop();
        }
        autoScrollDirection = 0;
        setAutoScrollMarker();
    }

    /**
     * Returns the ScrollPane instance which has the specified widget as content.
     *
     * @param widget the widget to retrieve the containing ScrollPane for.
     * @return the ScrollPane or null if that widget is not directly in a ScrollPane.
     * @see #setContent(de.matthiasmann.twl.Widget)
     */
    public static ScrollPane getContainingScrollPane(Widget widget) {
        Widget ca = widget.getParent();
        if(ca != null) {
            Widget sp = ca.getParent();
            if(sp instanceof ScrollPane) {
                ScrollPane scrollPane = (ScrollPane)sp;
                assert scrollPane.getContent() == widget;
                return scrollPane;
            }
        }
        return null;
    }

    @Override
    public int getMinWidth() {
        int minWidth = super.getMinWidth();
        int border = getBorderHorizontal();
        //minWidth = Math.max(minWidth, scrollbarH.getMinWidth() + border);
        if(fixed == Fixed.HORIZONTAL && content != null) {
            int sbWidth = scrollbarV.isVisible() ? scrollbarV.getMinWidth() : 0;
            minWidth = Math.max(minWidth, content.getMinWidth() + border + sbWidth);
        }
        return minWidth;
    }

    @Override
    public int getMinHeight() {
        int minHeight = super.getMinHeight();
        int border = getBorderVertical();
        //minHeight = Math.max(minHeight, scrollbarV.getMinHeight() + border);
        if(fixed == Fixed.VERTICAL && content != null) {
            int sbHeight = scrollbarH.isVisible() ? scrollbarH.getMinHeight() : 0;
            minHeight = Math.max(minHeight, content.getMinHeight() + border + sbHeight);
        }
        return minHeight;
    }

    @Override
    public int getPreferredInnerWidth() {
        if(content != null) {
            switch(fixed) {
            case HORIZONTAL:
                int prefWidth = computeSize(
                        content.getMinWidth(),
                        content.getPreferredWidth(),
                        content.getMaxWidth());
                if(scrollbarV.isVisible()) {
                    prefWidth += scrollbarV.getPreferredWidth();
                }
                return prefWidth;
            case VERTICAL:
                return content.getPreferredWidth();
            }
        }
        return 0;
    }

    @Override
    public int getPreferredInnerHeight() {
        if(content != null) {
            switch(fixed) {
            case HORIZONTAL:
                return content.getPreferredHeight();
            case VERTICAL:
                int prefHeight = computeSize(
                        content.getMinHeight(),
                        content.getPreferredHeight(),
                        content.getMaxHeight());
                if(scrollbarH.isVisible()) {
                    prefHeight += scrollbarH.getPreferredHeight();
                }
                return prefHeight;
            }
        }
        return 0;
    }

    @Override
    public void insertChild(Widget child, int index) {
        throw new UnsupportedOperationException("use setContent");
    }

    @Override
    public void removeAllChildren() {
        throw new UnsupportedOperationException("use setContent");
    }

    @Override
    public Widget removeChild(int index) {
        throw new UnsupportedOperationException("use setContent");
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        applyThemeScrollPane(themeInfo);
    }

    /**
     * The following theme parameters are required by the scroll pane:<table>
     * <tr><th>Parameter name</th><th>Type</th><th>Description</th></tr>
     * <tr><td>autoScrollArea</td><td>integer</td><td>The size of the auto scroll area</td></tr>
     * <tr><td>autoScrollSpeed</td><td>integer</td><td>The speed in pixels to scroll every 50 ms</td></tr>
     * <tr><td>hasDragButton</td><td>boolean</td><td>If the dragButton should be shown or not</td></tr>
     * <tr><td>scrollbarsAlwaysVisible</td><td>boolean</td><td>Show scrollbars always (true) or only when needed (false)</td></tr>
     * </table>
     * <br/>
     * The following optional parameters can be used to change the appearance of
     * the scroll pane:<table>
     * <tr><th>Parameter name</th><th>Type</th><th>Description</th></tr>
     * <tr><td>hscrollbarOffset</td><td>Dimension</td><td>Moves the horizontal scrollbar but does not
     *      change the available area for the scroll content.</td></tr>
     * <tr><td>vscrollbarOffset</td><td>Dimension</td><td>Moves the vertical scrollbar but does not
     *      change the available area for the scroll content.</td></tr>
     * <tr><td>contentScrollbarSpacing</td><td>Dimension</td><td>An optional spacing between
     *      the scrollbar and the content area. This is only applied when the corresponding
     *      scrollbar is visible. It should be &gt;= 0.</td></tr>
     * </table>
     *
     * @param themeInfo the theme info
     */
    protected void applyThemeScrollPane(ThemeInfo themeInfo) {
        autoScrollArea = themeInfo.getParameter("autoScrollArea", 5);
        autoScrollSpeed = themeInfo.getParameter("autoScrollSpeed", autoScrollArea * 2);
        hscrollbarOffset = themeInfo.getParameterValue("hscrollbarOffset", false, Dimension.class, Dimension.ZERO);
        vscrollbarOffset = themeInfo.getParameterValue("vscrollbarOffset", false, Dimension.class, Dimension.ZERO);
        contentScrollbarSpacing = themeInfo.getParameterValue("contentScrollbarSpacing", false, Dimension.class, Dimension.ZERO);
        scrollbarsAlwaysVisible = themeInfo.getParameter("scrollbarsAlwaysVisible", false);

        boolean hasDragButton = themeInfo.getParameter("hasDragButton", false);
        if(hasDragButton && dragButton == null) {
            dragButton = new DraggableButton();
            dragButton.setTheme("dragButton");
            dragButton.setListener(new DraggableButton.DragListener() {
                public void dragStarted() {
                    scrollbarH.externalDragStart();
                    scrollbarV.externalDragStart();
                }
                public void dragged(int deltaX, int deltaY) {
                    scrollbarH.externalDragged(deltaX, deltaY);
                    scrollbarV.externalDragged(deltaX, deltaY);
                }
                public void dragStopped() {
                    scrollbarH.externalDragStopped();
                    scrollbarV.externalDragStopped();
                }
            });
            super.insertChild(dragButton, 3);
        } else if(!hasDragButton && dragButton != null) {
            assert super.getChild(3) == dragButton;
            super.removeChild(3);
            dragButton = null;
        }
    }

    protected int getAutoScrollDirection(Event evt) {
        if(content instanceof AutoScrollable) {
            return ((AutoScrollable)content).getAutoScrollDirection(evt, autoScrollArea);
        }
        if(contentArea.isMouseInside(evt)) {
            int mouseY = evt.getMouseY();
            int areaY = contentArea.getY();
            if((mouseY - areaY) <= autoScrollArea ||
                    (contentArea.getBottom() - mouseY) <= autoScrollArea) {
                // use a 2nd check to decide direction in case the autoScrollAreas overlap
                if(mouseY < (areaY + contentArea.getHeight()/2)) {
                    return -1;
                } else {
                    return +1;
                }
            }
        }
        return 0;
    }

    @Override
    public void validateLayout() {
        if(!inLayout) {
            try {
                inLayout = true;
                if(content != null) {
                    content.validateLayout();
                }
                super.validateLayout();
            } finally {
                inLayout = false;
            }
        }
    }

    @Override
    protected void childInvalidateLayout(Widget child) {
        if(child == contentArea) {
            // stop invalidate layout chain here when it comes from contentArea
            invalidateLayoutLocally();
        } else {
            super.childInvalidateLayout(child);
        }
    }

    @Override
    protected void paintWidget(GUI gui) {
        // clear flags - used to detect layout loops
        scrollbarsToggleFlags = 0;
    }

    @Override
    protected void layout() {
        if(content != null) {
            int innerWidth = getInnerWidth();
            int innerHeight = getInnerHeight();
            int availWidth = innerWidth;
            int availHeight = innerHeight;
            innerWidth += vscrollbarOffset.getX();
            innerHeight += hscrollbarOffset.getY();
            int scrollbarHX = hscrollbarOffset.getX();
            int scrollbarHY = innerHeight;
            int scrollbarVX = innerWidth;
            int scrollbarVY = vscrollbarOffset.getY();
            int requiredWidth;
            int requiredHeight;
            boolean repeat;
            boolean visibleH = false;
            boolean visibleV = false;

            switch(fixed) {
            case HORIZONTAL:
                requiredWidth = availWidth;
                requiredHeight = content.getPreferredHeight();
                break;
            case VERTICAL:
                requiredWidth = content.getPreferredWidth();
                requiredHeight = availHeight;
                break;
            default:
                requiredWidth = content.getPreferredWidth();
                requiredHeight = content.getPreferredHeight();
                break;
            }

            //System.out.println("required="+requiredWidth+","+requiredHeight+" avail="+availWidth+","+availHeight);

            int hScrollbarMax = 0;
            int vScrollbarMax = 0;

            // don't add scrollbars if we have zero size
            if(availWidth > 0 && availHeight > 0) {
                do{
                    repeat = false;

                    if(fixed != Fixed.HORIZONTAL) {
                        hScrollbarMax = Math.max(0, requiredWidth - availWidth);
                        if(hScrollbarMax > 0 || scrollbarsAlwaysVisible || ((scrollbarsToggleFlags & 3) == 3)) {
                            repeat |= !visibleH;
                            visibleH = true;
                            int prefHeight = scrollbarH.getPreferredHeight();
                            scrollbarHY = innerHeight - prefHeight;
                            availHeight = Math.max(0, scrollbarHY - contentScrollbarSpacing.getY());
                        }
                    } else {
                        hScrollbarMax = 0;
                        requiredWidth = availWidth;
                    }

                    if(fixed != Fixed.VERTICAL) {
                        vScrollbarMax = Math.max(0, requiredHeight - availHeight);
                        if(vScrollbarMax > 0 || scrollbarsAlwaysVisible || ((scrollbarsToggleFlags & 12) == 12)) {
                            repeat |= !visibleV;
                            visibleV = true;
                            int prefWidth = scrollbarV.getPreferredWidth();
                            scrollbarVX = innerWidth - prefWidth;
                            availWidth = Math.max(0, scrollbarVX - contentScrollbarSpacing.getX());
                        }
                    } else {
                        vScrollbarMax = 0;
                        requiredHeight = availHeight;
                    }
                }while(repeat);
            }

            // if a scrollbar visibility state has changed set it's flag to detect layout loops
            if(visibleH && !scrollbarH.isVisible()) {
                scrollbarsToggleFlags |= 1;
            }
            if(!visibleH && scrollbarH.isVisible()) {
                scrollbarsToggleFlags |= 2;
            }
            if(visibleV && !scrollbarV.isVisible()) {
                scrollbarsToggleFlags |= 4;
            }
            if(!visibleV && scrollbarV.isVisible()) {
                scrollbarsToggleFlags |= 8;
            }
            
            boolean changedH = visibleH ^ scrollbarH.isVisible();
            boolean changedV = visibleV ^ scrollbarV.isVisible();
            if(changedH || changedV) {
                if((changedH && fixed == Fixed.VERTICAL) ||
                   (changedV && fixed == Fixed.HORIZONTAL)) {
                    invalidateLayout();
                } else {
                    invalidateLayoutLocally();
                }
            }

            int pageSizeX, pageSizeY;
            if(content instanceof CustomPageSize) {
                CustomPageSize customPageSize = (CustomPageSize)content;
                pageSizeX = customPageSize.getPageSizeX(availWidth);
                pageSizeY = customPageSize.getPageSizeY(availHeight);
            } else {
                pageSizeX = availWidth;
                pageSizeY = availHeight;
            }

            scrollbarH.setVisible(visibleH);
            scrollbarH.setMinMaxValue(0, hScrollbarMax);
            scrollbarH.setSize(Math.max(0, scrollbarVX - scrollbarHX), Math.max(0, innerHeight - scrollbarHY));
            scrollbarH.setPosition(getInnerX() + scrollbarHX, getInnerY() + scrollbarHY);
            scrollbarH.setPageSize(Math.max(1, pageSizeX));
            scrollbarH.setStepSize(Math.max(1, pageSizeX / 10));

            scrollbarV.setVisible(visibleV);
            scrollbarV.setMinMaxValue(0, vScrollbarMax);
            scrollbarV.setSize(Math.max(0, innerWidth - scrollbarVX), Math.max(0, scrollbarHY - scrollbarVY));
            scrollbarV.setPosition(getInnerX() + scrollbarVX, getInnerY() + scrollbarVY);
            scrollbarV.setPageSize(Math.max(1, pageSizeY));
            scrollbarV.setStepSize(Math.max(1, pageSizeY / 10));

            if(dragButton != null) {
                dragButton.setVisible(visibleH && visibleV);
                dragButton.setSize(Math.max(0, innerWidth - scrollbarVX), Math.max(0, innerHeight - scrollbarHY));
                dragButton.setPosition(getInnerX() + scrollbarVX, getInnerY() + scrollbarHY);
            }

            contentArea.setPosition(getInnerX(), getInnerY());
            contentArea.setSize(availWidth, availHeight);
            if(content instanceof Scrollable) {
                content.setPosition(contentArea.getX(), contentArea.getY());
                content.setSize(availWidth, availHeight);
            } else if(expandContentSize) {
                content.setSize(Math.max(availWidth, requiredWidth),
                        Math.max(availHeight, requiredHeight));
            } else {
                content.setSize(Math.max(0, requiredWidth), Math.max(0, requiredHeight));
            }

            AnimationState animationState = getAnimationState();
            animationState.setAnimationState(STATE_HORIZONTAL_SCROLLBAR_VISIBLE, visibleH);
            animationState.setAnimationState(STATE_VERTICAL_SCROLLBAR_VISIBLE, visibleV);

            scrollContent();
        } else {
            scrollbarH.setVisible(false);
            scrollbarV.setVisible(false);
        }
    }

    @Override
    protected boolean handleEvent(Event evt) {
        if(evt.isKeyEvent() && content != null && content.canAcceptKeyboardFocus()) {
            if(content.handleEvent(evt)) {
                content.requestKeyboardFocus();
                return true;
            }
        }
        if(super.handleEvent(evt)) {
            return true;
        }
        switch(evt.getType()) {
        case KEY_PRESSED:
        case KEY_RELEASED: {
            int keyCode = evt.getKeyCode();
            if(keyCode == Event.KEY_LEFT ||
                    keyCode == Event.KEY_RIGHT) {
                return scrollbarH.handleEvent(evt);
            }
            if(keyCode == Event.KEY_UP ||
                    keyCode == Event.KEY_DOWN ||
                    keyCode == Event.KEY_PRIOR ||
                    keyCode == Event.KEY_NEXT) {
                return scrollbarV.handleEvent(evt);
            }
            break;
        }
        case MOUSE_WHEEL:
            if(scrollbarV.isVisible()) {
                return scrollbarV.handleEvent(evt);
            }
            return false;
        }
        return evt.isMouseEvent() && contentArea.isMouseInside(evt);
    }

    @Override
    protected void paint(GUI gui) {
        if(dragButton != null) {
            AnimationState as = dragButton.getAnimationState();
            as.setAnimationState(STATE_DOWNARROW_ARMED, scrollbarV.isDownRightButtonArmed());
            as.setAnimationState(STATE_RIGHTARROW_ARMED, scrollbarH.isDownRightButtonArmed());
        }
        super.paint(gui);
    }

    void scrollContent() {
       if(content instanceof Scrollable) {
            Scrollable scrollable = (Scrollable)content;
            scrollable.setScrollPosition(scrollbarH.getValue(), scrollbarV.getValue());
       } else {
            content.setPosition(
                    contentArea.getX()-scrollbarH.getValue(),
                    contentArea.getY()-scrollbarV.getValue());
       }
    }

    void setAutoScrollMarker() {
        int scrollPos = scrollbarV.getValue();
        AnimationState animationState = getAnimationState();
        animationState.setAnimationState(STATE_AUTO_SCROLL_UP,   autoScrollDirection < 0 && scrollPos > 0);
        animationState.setAnimationState(STATE_AUTO_SCROLL_DOWN, autoScrollDirection > 0 && scrollPos < scrollbarV.getMaxValue());
    }

    void doAutoScroll() {
        scrollbarV.setValue(scrollbarV.getValue() + autoScrollDirection * autoScrollSpeed);
        setAutoScrollMarker();
    }
}
