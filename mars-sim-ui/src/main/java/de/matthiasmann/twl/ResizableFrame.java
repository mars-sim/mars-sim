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

import de.matthiasmann.twl.renderer.AnimationState.StateKey;
import de.matthiasmann.twl.renderer.MouseCursor;
import de.matthiasmann.twl.utils.TintAnimator;

/**
 * A resizable frame.
 *
 * <p>All child widgets (which are not part of the frame itself) cover
 * the complete inner area {@link #layoutChildFullInnerArea(de.matthiasmann.twl.Widget) }.</p>
 * 
 * <p>The preferred way to use the ResizableFrame is to add a single
 * widget which will manage the layout of all it's children.
 * {@link DialogLayout} can be used for this to avoid creating a new class.</p>
 * 
 * @author Matthias Mann
 */
public class ResizableFrame extends Widget {

    public static final StateKey STATE_FADE = StateKey.get("fade");

    public enum ResizableAxis {
        NONE(false, false),
        HORIZONTAL(true, false),
        VERTICAL(false, true),
        BOTH(true, true);

        final boolean allowX;
        final boolean allowY;
        private ResizableAxis(boolean allowX, boolean allowY) {
            this.allowX = allowX;
            this.allowY = allowY;
        }
    };
    
    private enum DragMode {
        NONE("mouseCursor"),
        EDGE_LEFT("mouseCursor.left"),
        EDGE_TOP("mouseCursor.top"),
        EDGE_RIGHT("mouseCursor.right"),
        EDGE_BOTTOM("mouseCursor.bottom"),
        CORNER_TL("mouseCursor.top-left"),
        CORNER_TR("mouseCursor.top-right"),
        CORNER_BR("mouseCursor.bottom-right"),
        CORNER_BL("mouseCursor.bottom-left"),
        POSITION("mouseCursor.all");

        final String cursorName;
        DragMode(String cursorName) {
            this.cursorName = cursorName;
        }
    }

    private String title;
    
    private final MouseCursor[] cursors;
    private ResizableAxis resizableAxis = ResizableAxis.BOTH;
    private boolean draggable = true;
    private boolean backgroundDraggable;
    private DragMode dragMode = DragMode.NONE;
    private int dragStartX;
    private int dragStartY;
    private int dragInitialLeft;
    private int dragInitialTop;
    private int dragInitialRight;
    private int dragInitialBottom;

    private Color fadeColorInactive = Color.WHITE;
    private int fadeDurationActivate;
    private int fadeDurationDeactivate;
    private int fadeDurationShow;
    private int fadeDurationHide;

    private TextWidget titleWidget;
    private int titleAreaTop;
    private int titleAreaLeft;
    private int titleAreaRight;
    private int titleAreaBottom;

    private boolean hasCloseButton;
    private Button closeButton;
    private int closeButtonX;
    private int closeButtonY;

    private boolean hasResizeHandle;
    private Widget resizeHandle;
    private int resizeHandleX;
    private int resizeHandleY;
    private DragMode resizeHandleDragMode;
    
    public ResizableFrame() {
        title = "";
        cursors = new MouseCursor[DragMode.values().length];
        setCanAcceptKeyboardFocus(true);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        if(titleWidget != null) {
            titleWidget.setCharSequence(title);
        }
    }

    public ResizableAxis getResizableAxis() {
        return resizableAxis;
    }

    public void setResizableAxis(ResizableAxis resizableAxis) {
        if(resizableAxis == null) {
            throw new NullPointerException("resizableAxis");
        }
        this.resizableAxis = resizableAxis;
        if(resizeHandle != null) {
            layoutResizeHandle();
        }
    }

    public boolean isDraggable() {
        return draggable;
    }

    /**
     * Controls weather the ResizableFrame can be dragged via the title bar or
     * not, default is true.
     * 
     * <p>When set to false the resizing should also be disabled to present a
     * consistent behavior to the user.</p>
     * 
     * @param movable if dragging via the title bar is allowed - default is true.
     */
    public void setDraggable(boolean movable) {
        this.draggable = movable;
    }

    public boolean isBackgroundDraggable() {
        return backgroundDraggable;
    }

    /**
     * Controls weather the ResizableFrame can be dragged via the background
     * (eg space not occupied by any widget or a resizable edge), default is false.
     * 
     * <p>This works independent of {@link #setDraggable(boolean) }.</p>
     * 
     * @param backgroundDraggable if dragging via the background is allowed - default is false.
     * @see #setDraggable(boolean) 
     */
    public void setBackgroundDraggable(boolean backgroundDraggable) {
        this.backgroundDraggable = backgroundDraggable;
    }

    public final boolean hasTitleBar() {
        return titleWidget != null && titleWidget.getParent() == this;
    }

    public void addCloseCallback(Runnable cb) {
        if(closeButton == null) {
            closeButton = new Button();
            closeButton.setTheme("closeButton");
            closeButton.setCanAcceptKeyboardFocus(false);
            add(closeButton);
            layoutCloseButton();
        }
        closeButton.setVisible(hasCloseButton);
        closeButton.addCallback(cb);
    }

    public void removeCloseCallback(Runnable cb) {
        if(closeButton != null) {
            closeButton.removeCallback(cb);
            closeButton.setVisible(closeButton.hasCallbacks());
        }
    }

    public int getFadeDurationActivate() {
        return fadeDurationActivate;
    }

    public int getFadeDurationDeactivate() {
        return fadeDurationDeactivate;
    }

    public int getFadeDurationHide() {
        return fadeDurationHide;
    }

    public int getFadeDurationShow() {
        return fadeDurationShow;
    }
    

    @Override
    public void setVisible(boolean visible) {
        if(visible) {
            TintAnimator tintAnimator = getTintAnimator();
            if((tintAnimator != null && tintAnimator.hasTint()) || !super.isVisible()) {
                fadeTo(hasKeyboardFocus() ? Color.WHITE : fadeColorInactive, fadeDurationShow);
            }
        } else if(super.isVisible()) {
            fadeToHide(fadeDurationHide);
        }
    }

    /**
     * Sets the visibility without triggering a fade
     * @param visible the new visibility flag
     * @see Widget#setVisible(boolean)
     */
    public void setHardVisible(boolean visible) {
        super.setVisible(visible);
    }

    protected void applyThemeResizableFrame(ThemeInfo themeInfo) {
        for(DragMode m : DragMode.values()) {
            cursors[m.ordinal()] = themeInfo.getMouseCursor(m.cursorName);
        }
        titleAreaTop = themeInfo.getParameter("titleAreaTop", 0);
        titleAreaLeft = themeInfo.getParameter("titleAreaLeft", 0);
        titleAreaRight = themeInfo.getParameter("titleAreaRight", 0);
        titleAreaBottom = themeInfo.getParameter("titleAreaBottom", 0);
        closeButtonX = themeInfo.getParameter("closeButtonX", 0);
        closeButtonY = themeInfo.getParameter("closeButtonY", 0);
        hasCloseButton = themeInfo.getParameter("hasCloseButton", false);
        hasResizeHandle = themeInfo.getParameter("hasResizeHandle", false);
        resizeHandleX = themeInfo.getParameter("resizeHandleX", 0);
        resizeHandleY = themeInfo.getParameter("resizeHandleY", 0);
        fadeColorInactive = themeInfo.getParameter("fadeColorInactive", Color.WHITE);
        fadeDurationActivate = themeInfo.getParameter("fadeDurationActivate", 0);
        fadeDurationDeactivate = themeInfo.getParameter("fadeDurationDeactivate", 0);
        fadeDurationShow = themeInfo.getParameter("fadeDurationShow", 0);
        fadeDurationHide = themeInfo.getParameter("fadeDurationHide", 0);
        invalidateLayout();

        if(super.isVisible() && !hasKeyboardFocus() &&
                (getTintAnimator() != null || !Color.WHITE.equals(fadeColorInactive))) {
            fadeTo(fadeColorInactive, 0);
        }
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        applyThemeResizableFrame(themeInfo);
    }

    @Override
    protected void updateTintAnimation() {
        TintAnimator tintAnimator = getTintAnimator();
        tintAnimator.update();
        if(!tintAnimator.isFadeActive() && tintAnimator.isZeroAlpha()) {
            setHardVisible(false);
        }
    }

    protected void fadeTo(Color color, int duration) {
        //System.out.println("Start fade to " + color + " over " + duration + " ms");
        allocateTint().fadeTo(color, duration);
        if(!super.isVisible() && color.getAlpha() != 0) {
            setHardVisible(true);
        }
    }

    protected void fadeToHide(int duration) {
        if(duration <= 0) {
            setHardVisible(false);
        } else {
            allocateTint().fadeToHide(duration);
        }
    }

    private TintAnimator allocateTint() {
        TintAnimator tintAnimator = getTintAnimator();
        if(tintAnimator == null) {
            tintAnimator = new TintAnimator(new TintAnimator.AnimationStateTimeSource(getAnimationState(), STATE_FADE));
            setTintAnimator(tintAnimator);
            if(!super.isVisible()) {
                // we start with TRANSPARENT when hidden
                tintAnimator.fadeToHide(0);
            }
        }
        return tintAnimator;
    }

    protected boolean isFrameElement(Widget widget) {
        return widget == titleWidget || widget == closeButton || widget == resizeHandle;
    }

    @Override
    protected void layout() {
        int minWidth = getMinWidth();
        int minHeight = getMinHeight();
        if(getWidth() < minWidth || getHeight() < minHeight) {
            int width = Math.max(getWidth(), minWidth);
            int height = Math.max(getHeight(), minHeight);
            if(getParent() != null) {
                int x = Math.min(getX(), getParent().getInnerRight() - width);
                int y = Math.min(getY(), getParent().getInnerBottom() - height);
                setPosition(x, y);
            }
            setSize(width, height);
        }

        for(int i=0,n=getNumChildren() ; i<n ; i++) {
            Widget child = getChild(i);
            if(!isFrameElement(child)) {
                layoutChildFullInnerArea(child);
            }
        }

        layoutTitle();
        layoutCloseButton();
        layoutResizeHandle();
    }

    protected void layoutTitle() {
        int titleX = getTitleX(titleAreaLeft);
        int titleY = getTitleY(titleAreaTop);
        int titleWidth = Math.max(0, getTitleX(titleAreaRight) - titleX);
        int titleHeight = Math.max(0, getTitleY(titleAreaBottom) - titleY);

        if(titleAreaLeft != titleAreaRight && titleAreaTop != titleAreaBottom) {
            if(titleWidget == null) {
                titleWidget = new TextWidget(getAnimationState());
                titleWidget.setTheme("title");
                titleWidget.setMouseCursor(cursors[DragMode.POSITION.ordinal()]);
                titleWidget.setCharSequence(title);
                titleWidget.setClip(true);
            }
            if(titleWidget.getParent() == null) {
                insertChild(titleWidget, 0);
            }

            titleWidget.setPosition(titleX, titleY);
            titleWidget.setSize(titleWidth, titleHeight);
        } else if(titleWidget != null && titleWidget.getParent() == this) {
            titleWidget.destroy();
            removeChild(titleWidget);
        }
    }

    protected void layoutCloseButton() {
        if(closeButton != null) {
            closeButton.adjustSize();
            closeButton.setPosition(
                    getTitleX(closeButtonX),
                    getTitleY(closeButtonY));
            closeButton.setVisible(closeButton.hasCallbacks() && hasCloseButton);
        }
    }

    protected void layoutResizeHandle() {
        if(hasResizeHandle && resizeHandle == null) {
            resizeHandle = new Widget(getAnimationState(), true);
            resizeHandle.setTheme("resizeHandle");
            super.insertChild(resizeHandle, 0);
        }
        if(resizeHandle != null) {
            if(resizeHandleX > 0) {
                if(resizeHandleY > 0) {
                    resizeHandleDragMode = DragMode.CORNER_TL;
                } else {
                    resizeHandleDragMode = DragMode.CORNER_TR;
                }
            } else if(resizeHandleY > 0) {
                resizeHandleDragMode = DragMode.CORNER_BL;
            } else {
                resizeHandleDragMode = DragMode.CORNER_BR;
            }

            resizeHandle.adjustSize();
            resizeHandle.setPosition(
                    getTitleX(resizeHandleX),
                    getTitleY(resizeHandleY));
            resizeHandle.setVisible(hasResizeHandle &&
                    resizableAxis == ResizableAxis.BOTH);
        } else {
            resizeHandleDragMode = DragMode.NONE;
        }
    }

    @Override
    protected void keyboardFocusGained() {
        fadeTo(Color.WHITE, fadeDurationActivate);
    }

    @Override
    protected void keyboardFocusLost() {
        if(!hasOpenPopups() && super.isVisible()) {
            fadeTo(fadeColorInactive, fadeDurationDeactivate);
        }
    }

    @Override
    public int getMinWidth() {
        int minWidth = super.getMinWidth();
        for(int i=0,n=getNumChildren() ; i<n ; i++) {
            Widget child = getChild(i);
            if(!isFrameElement(child)) {
                minWidth = Math.max(minWidth, child.getMinWidth() + getBorderHorizontal());
            }
        }
        if(hasTitleBar() && titleAreaRight < 0) {
            minWidth = Math.max(minWidth, titleWidget.getPreferredWidth() + titleAreaLeft - titleAreaRight);
        }
        return minWidth;
    }

    @Override
    public int getMinHeight() {
        int minHeight = super.getMinHeight();
        for(int i=0,n=getNumChildren() ; i<n ; i++) {
            Widget child = getChild(i);
            if(!isFrameElement(child)) {
                minHeight = Math.max(minHeight, child.getMinHeight() + getBorderVertical());
            }
        }
        return minHeight;
    }

    @Override
    public int getMaxWidth() {
        int maxWidth = super.getMaxWidth();
        for(int i=0,n=getNumChildren() ; i<n ; i++) {
            Widget child = getChild(i);
            if(!isFrameElement(child)) {
                int aMaxWidth = child.getMaxWidth();
                if(aMaxWidth > 0) {
                    aMaxWidth += getBorderHorizontal();
                    if(maxWidth == 0 || aMaxWidth < maxWidth) {
                        maxWidth = aMaxWidth;
                    }
                }
            }
        }
        return maxWidth;
    }

    @Override
    public int getMaxHeight() {
        int maxHeight = super.getMaxHeight();
        for(int i=0,n=getNumChildren() ; i<n ; i++) {
            Widget child = getChild(i);
            if(!isFrameElement(child)) {
                int aMaxHeight = child.getMaxHeight();
                if(aMaxHeight > 0) {
                    aMaxHeight += getBorderVertical();
                    if(maxHeight == 0 || aMaxHeight < maxHeight) {
                        maxHeight = aMaxHeight;
                    }
                }
            }
        }
        return maxHeight;
    }

    @Override
    public int getPreferredInnerWidth() {
        int prefWidth = 0;
        for(int i=0,n=getNumChildren() ; i<n ; i++) {
            Widget child = getChild(i);
            if(!isFrameElement(child)) {
                prefWidth = Math.max(prefWidth, child.getPreferredWidth());
            }
        }
        return prefWidth;
    }

    @Override
    public int getPreferredWidth() {
        int prefWidth = super.getPreferredWidth();
        if(hasTitleBar() && titleAreaRight < 0) {
            prefWidth = Math.max(prefWidth, titleWidget.getPreferredWidth() + titleAreaLeft - titleAreaRight);
        }
        return prefWidth;
    }

    @Override
    public int getPreferredInnerHeight() {
        int prefHeight = 0;
        for(int i=0,n=getNumChildren() ; i<n ; i++) {
            Widget child = getChild(i);
            if(!isFrameElement(child)) {
                prefHeight = Math.max(prefHeight, child.getPreferredHeight());
            }
        }
        return prefHeight;
    }

    @Override
    public void adjustSize() {
        layoutTitle();
        super.adjustSize();
    }

    private int getTitleX(int offset) {
        return (offset < 0) ? getRight() + offset : getX() + offset;
    }

    private int getTitleY(int offset) {
        return (offset < 0) ? getBottom() + offset : getY() + offset;
    }

    @Override
    protected boolean handleEvent(Event evt) {
        boolean isMouseExit = evt.getType() == Event.Type.MOUSE_EXITED;

        if(isMouseExit && resizeHandle != null && resizeHandle.isVisible()) {
            resizeHandle.getAnimationState().setAnimationState(
                    TextWidget.STATE_HOVER, false);
        }

        if(dragMode != DragMode.NONE) {
            if(evt.isMouseDragEnd()) {
                dragMode = DragMode.NONE;
            } else if(evt.getType() == Event.Type.MOUSE_DRAGGED) {
                handleMouseDrag(evt);
            }
            return true;
        }

        if(!isMouseExit && resizeHandle != null && resizeHandle.isVisible()) {
            resizeHandle.getAnimationState().setAnimationState(
                    TextWidget.STATE_HOVER, resizeHandle.isMouseInside(evt));
        }

        if(!evt.isMouseDragEvent()) {
            if(evt.getType() == Event.Type.MOUSE_BTNDOWN &&
                    evt.getMouseButton() == Event.MOUSE_LBUTTON &&
                    handleMouseDown(evt)) {
                return true;
            }
        }

        if(super.handleEvent(evt)) {
            return true;
        }

        return evt.isMouseEvent();
    }

    @Override
    public MouseCursor getMouseCursor(Event evt) {
        DragMode cursorMode = dragMode;
        if(cursorMode == DragMode.NONE) {
            cursorMode = getDragMode(evt.getMouseX(), evt.getMouseY());
            if(cursorMode == DragMode.NONE) {
                return getMouseCursor();
            }
        }
        
        return cursors[cursorMode.ordinal()];
    }

    private DragMode getDragMode(int mx, int my) {
        boolean left = mx < getInnerX();
        boolean right = mx >= getInnerRight();

        boolean top = my < getInnerY();
        boolean bot = my >= getInnerBottom();

        if(hasTitleBar()) {
            if(titleWidget.isInside(mx, my)) {
                if(draggable) {
                    return DragMode.POSITION;
                } else {
                    return DragMode.NONE;
                }
            }
            top = my < titleWidget.getY();
        }

        if(closeButton != null && closeButton.isVisible() && closeButton.isInside(mx, my)) {
            return DragMode.NONE;
        }

        if(resizableAxis == ResizableAxis.NONE) {
            if(backgroundDraggable) {
                return DragMode.POSITION;
            }
            return DragMode.NONE;
        }
        
        if(resizeHandle != null && resizeHandle.isVisible() && resizeHandle.isInside(mx, my)) {
            return resizeHandleDragMode;
        }

        if(!resizableAxis.allowX) {
            left = false;
            right = false;
        }
        if(!resizableAxis.allowY) {
            top = false;
            bot = false;
        }
        
        if(left) {
            if(top) {
                return DragMode.CORNER_TL;
            }
            if(bot) {
                return DragMode.CORNER_BL;
            }
            return DragMode.EDGE_LEFT;
        }
        if(right) {
            if(top) {
                return DragMode.CORNER_TR;
            }
            if(bot) {
                return DragMode.CORNER_BR;
            }
            return DragMode.EDGE_RIGHT;
        }
        if(top) {
            return DragMode.EDGE_TOP;
        }
        if(bot) {
            return DragMode.EDGE_BOTTOM;
        }
        if(backgroundDraggable) {
            return DragMode.POSITION;
        }
        return DragMode.NONE;
    }

    private boolean handleMouseDown(Event evt) {
        final int mx = evt.getMouseX();
        final int my = evt.getMouseY();

        dragStartX = mx;
        dragStartY = my;
        dragInitialLeft = getX();
        dragInitialTop = getY();
        dragInitialRight = getRight();
        dragInitialBottom = getBottom();

        dragMode = getDragMode(mx, my);
        return dragMode != DragMode.NONE;
    }

    private void handleMouseDrag(Event evt) {
        final int dx = evt.getMouseX() - dragStartX;
        final int dy = evt.getMouseY() - dragStartY;

        int minWidth = getMinWidth();
        int minHeight = getMinHeight();
        int maxWidth = getMaxWidth();
        int maxHeight = getMaxHeight();

        // make sure max size is not smaller then min size
        if(maxWidth > 0 && maxWidth < minWidth) {
            maxWidth = minWidth;
        }
        if(maxHeight > 0 && maxHeight < minHeight) {
            maxHeight = minHeight;
        }

        int left = dragInitialLeft;
        int top = dragInitialTop;
        int right = dragInitialRight;
        int bottom = dragInitialBottom;

        switch(dragMode) {
        case CORNER_BL:
        case CORNER_TL:
        case EDGE_LEFT:
            left = Math.min(left + dx, right - minWidth);
            if(maxWidth > 0) {
                left = Math.max(left, Math.min(dragInitialLeft, right - maxWidth));
            }
            break;
        case CORNER_BR:
        case CORNER_TR:
        case EDGE_RIGHT:
            right = Math.max(right + dx, left + minWidth);
            if(maxWidth > 0) {
                right = Math.min(right, Math.max(dragInitialRight, left + maxWidth));
            }
            break;
        case POSITION:
            if(getParent() != null) {
                int minX = getParent().getInnerX();
                int maxX = getParent().getInnerRight();
                int width = dragInitialRight - dragInitialLeft;
                left = Math.max(minX, Math.min(maxX - width, left + dx));
                right = Math.min(maxX, Math.max(minX + width, right + dx));
            } else {
                left += dx;
                right += dx;
            }
            break;
        }

        switch(dragMode) {
        case CORNER_TL:
        case CORNER_TR:
        case EDGE_TOP:
            top = Math.min(top + dy, bottom - minHeight);
            if(maxHeight > 0) {
                top = Math.max(top, Math.min(dragInitialTop, bottom - maxHeight));
            }
            break;
        case CORNER_BL:
        case CORNER_BR:
        case EDGE_BOTTOM:
            bottom = Math.max(bottom + dy, top + minHeight);
            if(maxHeight > 0) {
                bottom = Math.min(bottom, Math.max(dragInitialBottom, top + maxHeight));
            }
            break;
        case POSITION:
            if(getParent() != null) {
                int minY = getParent().getInnerY();
                int maxY = getParent().getInnerBottom();
                int height = dragInitialBottom - dragInitialTop;
                top = Math.max(minY, Math.min(maxY - height, top + dy));
                bottom = Math.min(maxY, Math.max(minY + height, bottom + dy));
            } else {
                top += dy;
                bottom += dy;
            }
            break;
        }

        setArea(top, left, right, bottom);
    }

    private void setArea(int top, int left, int right, int bottom) {
        Widget p = getParent();
        if(p != null) {
            top = Math.max(top, p.getInnerY());
            left = Math.max(left, p.getInnerX());
            right = Math.min(right, p.getInnerRight());
            bottom = Math.min(bottom, p.getInnerBottom());
        }

        setPosition(left, top);
        setSize(Math.max(getMinWidth(), right-left),
                Math.max(getMinHeight(), bottom-top));
    }
}
