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
import de.matthiasmann.twl.renderer.MouseCursor;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.OffscreenRenderer;
import de.matthiasmann.twl.renderer.OffscreenSurface;
import de.matthiasmann.twl.renderer.Renderer;
import de.matthiasmann.twl.theme.ThemeManager;
import de.matthiasmann.twl.utils.TextUtil;
import de.matthiasmann.twl.utils.TintAnimator;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Root of the TWL class hierarchy.
 *
 * <p>When subclassing the following methods should be overridden to ensure
 * correct layout behavior:</p><ul>
 * <li>{@link #layout() }</li>
 * <li>{@link #getPreferredInnerWidth() }</li>
 * <li>{@link #getPreferredInnerHeight() }</li>
 * </ul>
 * 
 * <p>The following methods are events and can be overridden when needed:</p><ul>
 * <li>{@link #afterAddToGUI(de.matthiasmann.twl.GUI) }</li>
 * <li>{@link #allChildrenRemoved() }</li>
 * <li>{@link #beforeRemoveFromGUI(de.matthiasmann.twl.GUI) }</li>
 * <li>{@link #borderChanged() }</li>
 * <li>{@link #childAdded(de.matthiasmann.twl.Widget) }</li>
 * <li>{@link #childHidden(de.matthiasmann.twl.Widget) }</li>
 * <li>{@link #childRemoved(de.matthiasmann.twl.Widget) }</li>
 * <li>{@link #childVisibilityChanged(de.matthiasmann.twl.Widget) }</li>
 * <li>{@link #keyboardFocusChildChanged(de.matthiasmann.twl.Widget) }</li>
 * <li>{@link #keyboardFocusGained() }</li>
 * <li>{@link #keyboardFocusGained(de.matthiasmann.twl.FocusGainedCause, de.matthiasmann.twl.Widget) }</li>
 * <li>{@link #keyboardFocusLost() }</li>
 * <li>{@link #positionChanged() }</li>
 * <li>{@link #sizeChanged() }</li>
 * <li>{@link #widgetDisabled() }</li>
 * </ul>
 * 
 * <p>NOTE: The only thread safe methods of TWL are:</p><ul>
 * <li>{@link #getGUI() }</li>
 * <li>{@link GUI#invokeLater(java.lang.Runnable) }</li>
 * </ul>
 * 
 * @author Matthias Mann
 */
public class Widget {

    public static final StateKey STATE_KEYBOARD_FOCUS = StateKey.get("keyboardFocus");
    public static final StateKey STATE_HAS_OPEN_POPUPS = StateKey.get("hasOpenPopups");
    public static final StateKey STATE_HAS_FOCUSED_CHILD = StateKey.get("hasFocusedChild");
    public static final StateKey STATE_DISABLED = StateKey.get("disabled");
    
    private static final int LAYOUT_INVALID_LOCAL  = 1;
    private static final int LAYOUT_INVALID_GLOBAL = 3;
    
    private Widget parent;
    private int posX;
    private int posY;
    private int width;
    private int height;
    private int layoutInvalid;
    private boolean clip;
    private boolean visible = true;
    private boolean hasOpenPopup;
    private boolean enabled = true;
    private boolean locallyEnabled = true;
    private String theme;
    private ThemeManager themeManager;
    private Image background;
    private Image overlay;
    private Object tooltipContent;
    private Object themeTooltipContent;
    private InputMap inputMap;
    private ActionMap actionMap;
    private TintAnimator tintAnimator;
    private PropertyChangeSupport propertyChangeSupport;
    volatile GUI guiInstance;
    private OffscreenSurface offscreenSurface;
    private RenderOffscreen renderOffscreen;

    private final AnimationState animState;
    private final boolean sharedAnimState;

    private short borderLeft;
    private short borderTop;
    private short borderRight;
    private short borderBottom;

    private short minWidth;
    private short minHeight;
    private short maxWidth;
    private short maxHeight;

    private short offscreenExtraLeft;
    private short offscreenExtraTop;
    private short offscreenExtraRight;
    private short offscreenExtraBottom;
    
    private ArrayList<Widget> children;
    private Widget lastChildMouseOver;
    private Widget focusChild;
    private MouseCursor mouseCursor;
    private FocusGainedCause focusGainedCause;
    
    private boolean focusKeyEnabled = true;
    private boolean canAcceptKeyboardFocus;
    private boolean depthFocusTraversal = true;

    /**
     * Stores the state of the current focus transfer:
     * null                     no focus transfer active
     * Widget[]{ null }         transfer is active, but no previous focused widget
     * Widget[]{ prevWidget }   transfer is active, prevWidget was focused
     */
    private static final ThreadLocal<Widget[]> focusTransferInfo = new ThreadLocal<Widget[]>();
    
    /**
     * Creates a Widget with it's own animation state
     * 
     * <p>The initial theme name is the lower case version of the simple class
     * name of the concrete subclass - or in pseudo code:</p>
     * <pre>{@code getClass().getSimpleName().toLowerCase() }</pre>
     * 
     * @see #setTheme(java.lang.String) 
     */
    public Widget() {
        this(null, false);
    }

    /**
     * Creates a Widget with a shared animation state
     * 
     * <p>The initial theme name is the lower case version of the simple class
     * name of the concrete subclass - or in pseudo code:</p>
     * <pre>{@code getClass().getSimpleName().toLowerCase() }</pre>
     *
     * @param animState the animation state to share, can be null
     * @see #setTheme(java.lang.String) 
     */
    public Widget(AnimationState animState) {
        this(animState, false);
    }

    /**
     * Creates a Widget with a shared or inherited animation state
     * 
     * <p>The initial theme name is the lower case version of the simple class
     * name of the concrete subclass - or in pseudo code:</p>
     * <pre>{@code getClass().getSimpleName().toLowerCase() }</pre>
     *
     * @param animState the animation state to share or inherit, can be null
     * @param inherit true if the animation state should be inherited, false for sharing
     * @see AnimationState#AnimationState(de.matthiasmann.twl.AnimationState) 
     * @see #setTheme(java.lang.String) 
     */
    public Widget(AnimationState animState, boolean inherit) {
        // determine the default theme name from the class name of this instance
        // eg class Label => "label"
        Class<?> clazz = getClass();
        do {
            theme = clazz.getSimpleName().toLowerCase(Locale.ENGLISH);
            clazz = clazz.getSuperclass();
        } while (theme.length() == 0 && clazz != null);

        if(animState == null || inherit) {
            this.animState = new AnimationState(animState);
            this.sharedAnimState = false;
        } else {
            this.animState = animState;
            this.sharedAnimState = true;
        }
    }

    /**
     * Add a PropertyChangeListener for all properties.
     *
     * @param listener The PropertyChangeListener to be added
     * @see PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        createPropertyChangeSupport().addPropertyChangeListener(listener);
    }

    /**
     * Add a PropertyChangeListener for a specific property.
     *
     * @param propertyName The name of the property to listen on
     * @param listener The PropertyChangeListener to be added
     * @see PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener) 
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        createPropertyChangeSupport().addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Remove a PropertyChangeListener.
     *
     * @param listener The PropertyChangeListener to be removed
     * @see PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener) 
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if(propertyChangeSupport != null) {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }
    }

    /**
     * Remove a PropertyChangeListener.
     *
     * @param propertyName The name of the property that was listened on
     * @param listener The PropertyChangeListener to be removed
     * @see PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener) 
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if(propertyChangeSupport != null) {
            propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
        }
    }
    
    /**
     * Checks whether this widget or atleast one of it's children
     * owns an open popup.
     * @return true if atleast own open popup is owned (indirectly) by this widget.
     */
    public boolean hasOpenPopups() {
        return hasOpenPopup;
    }
    
    /**
     * Returns the parent of this widget or null if it is the tree root.
     * All coordinates are relative to the root of the widget tree.
     * @return the parent of this widget or null if it is the tree root
     */
    public final Widget getParent() {
        return parent;
    }
    
    /**
     * Returns the root of this widget tree.
     * All coordinates are relative to the root of the widget tree.
     * @return the root of this widget tree
     */
    public final Widget getRootWidget() {
        Widget w = this;
        Widget p;
        while((p=w.parent) != null) {
            w = p;
        }
        return w;
    }

    /**
     * Returns the GUI root of this widget tree if it has one.<p>
     *
     * Once a widget is added (indirectly) to a GUI object it will be part of
     * that GUI tree.<p>
     *
     * This method is thread safe.<p>
     *
     * Repeated calls may not return the same result. Use it like this:
     * <pre>
     * GUI gui = getGUI();
     * if(gui != null) {
     *     gui.invokeLater(....);
     * }
     * </pre>
     *
     * @return the GUI root or null if the root is not a GUI instance.
     * @see #afterAddToGUI(de.matthiasmann.twl.GUI)
     * @see #beforeRemoveFromGUI(de.matthiasmann.twl.GUI)
     */
    public final GUI getGUI() {
        return guiInstance;
    }
    
    /**
     * Returns the current visibility flag of this widget.
     * This does not check if the widget is clipped or buried behind another widget.
     * @return the current visibility flag of this widget
     */
    public final boolean isVisible() {
        return visible;
    }

    /**
     * Changes the visibility flag of this widget.
     * Widgets are by default visible.
     * Invisible widgets don't receive paint() or handleEvent() calls
     * @param visible the new visibility flag
     */
    public void setVisible(boolean visible) {
        if(this.visible != visible) {
            this.visible = visible;
            if(!visible) {
                GUI gui = getGUI();
                if(gui != null) {
                    gui.widgetHidden(this);
                }
                if(parent != null) {
                    parent.childHidden(this);
                }
            }
            if(parent != null) {
                parent.childVisibilityChanged(this);
            }
        }
    }

    /**
     * Returns the local enabled state of this widget.
     * 
     * If one of it's parents is disabled then this widget will also be
     * disabled even when it's local enabled state is true.
     *
     * @return the local enabled state.
     * @see #isEnabled()
     * @see #setEnabled(boolean)
     */
    public final boolean isLocallyEnabled() {
        return locallyEnabled;
    }

    /**
     * Checks if this widget and all it's parents are enabled.
     * If one of it's parents is disabled then it will return false.
     * 
     * This is the effective enabled state which is also represented as
     * animation state with inverse polarity {@code STATE_DISABLED}
     *
     * If a widget is disabled it will not receive keyboard or mouse events
     * except {@code MOUSE_ENTERED} and {@code MOUSE_EXITED}
     *
     * @return the effective enabled state
     * @see #isEnabled()
     * @see #setEnabled(boolean)
     */
    public final boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the local enabled state of that widget. The effective enabled state
     * of the widget is the effective enabled state of it's parent and it's
     * local enabled state.
     *
     * The effective enabled state is exposed as animation state but with
     * inverse polarity as {@code STATE_DISABLED}.
     *
     * On disabling the keyboard focus will be removed.
     *
     * If a widget is disabled it will not receive keyboard or mouse events
     * except {@code MOUSE_ENTERED} and {@code MOUSE_EXITED}
     *
     * @param enabled true if the widget should be locally enabled
     * @see #isEnabled()
     * @see #isLocallyEnabled()
     */
    public void setEnabled(boolean enabled) {
        if(this.locallyEnabled != enabled) {
            this.locallyEnabled = enabled;
            firePropertyChange("locallyEnabled", !enabled, enabled);
            recursivelyEnabledChanged(getGUI(),
                    (parent != null) ? parent.enabled : true);
        }
    }

    /**
     * Returns the absolute X coordinate of widget in it's tree
     *
     * This property can be bound and fires PropertyChangeEvent
     *
     * @return the absolute X coordinate of widget in it's tree
     * @see #addPropertyChangeListener(java.beans.PropertyChangeListener)
     * @see #addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public final int getX() {
        return posX;
    }


    /**
     * Returns the absolute Y coordinate of widget in it's tree
     *
     * This property can be bound and fires PropertyChangeEvent
     *
     * @return the absolute Y coordinate of widget in it's tree
     * @see #addPropertyChangeListener(java.beans.PropertyChangeListener)
     * @see #addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public final int getY() {
        return posY;
    }
    
    /**
     * Returns the width of this widget
     *
     * This property can be bound and fires PropertyChangeEvent
     *
     * @return the width of this widget
     * @see #addPropertyChangeListener(java.beans.PropertyChangeListener)
     * @see #addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public final int getWidth() {
        return width;
    }

    /**
     * Returns the height of this widget
     *
     * This property can be bound and fires PropertyChangeEvent
     *
     * @return the height of this widget
     * @see #addPropertyChangeListener(java.beans.PropertyChangeListener)
     * @see #addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener) 
     */
    public final int getHeight() {
        return height;
    }
    
    /**
     * Returns the right X coordinate of this widget
     * @return getX() + getWidth()
     */
    public final int getRight() {
        return posX + width;
    }
    
    /**
     * Returns the bottom Y coordinate of this widget
     * @return getY() + getHeight()
     */
    public final int getBottom() {
        return posY + height;
    }

    /**
     * The inner X position takes the left border into account
     * @return getX() + getBorderLeft()
     */
    public final int getInnerX() {
        return posX + borderLeft;
    }

    /**
     * The inner Y position takes the top border into account
     * @return getY() + getBorderTop()
     */
    public final int getInnerY() {
        return posY + borderTop;
    }
    
    /**
     * The inner width takes the left and right border into account.
     * @return the inner width - never negative
     */
    public final int getInnerWidth() {
        return Math.max(0, width - borderLeft - borderRight);
    }

    /**
     * The inner height takes the top and bottom border into account.
     * @return the inner height - never negative
     */
    public final int getInnerHeight() {
        return Math.max(0, height - borderTop - borderBottom);
    }
    
    /**
     * Returns the right X coordinate while taking the right border into account.
     * @return getInnerX() + getInnerWidth()
     */
    public final int getInnerRight() {
        return posX + Math.max(borderLeft, width - borderRight);
    }
    
    /**
     * Returns the bottom Y coordinate while taking the bottom border into account.
     * @return getInnerY() + getInnerHeight()
     */
    public final int getInnerBottom() {
        return posY + Math.max(borderTop, height - borderBottom);
    }
    
    /**
     * Checks if the given absolute (to this widget's tree) coordinates are inside this widget.
     * 
     * @param x the X coordinate to test
     * @param y the Y coordinate to test
     * @return true if it was inside
     */
    public boolean isInside(int x, int y) {
        return (x >= posX) && (y >= posY) && (x < posX + width) && (y < posY + height);
    }

    /**
     * Changes the position of this widget.
     * 
     * <p>When the position has changed then<ul>
     * <li>The positions of all children are updated</li>
     * <li>{@link #positionChanged()} is called</li>
     * <li>{@link PropertyChangeEvent} are fired for "x" and "y"</li>
     * </ul></p>
     *
     * <p>This method should only be called from within the layout() method of the
     * parent. Otherwise it could lead to bad interaction with theming and result
     * in a wrong position after the theme has been applied.</p>
     *
     * <p>NOTE: Position is absolute in the widget's tree.</p>
     *
     * @param x The new x position, can be negative
     * @param y The new y position, can be negative
     * @return true if the position was changed, false if new position == old position
     * @see #layout()
     */
    public boolean setPosition(int x, int y) {
        return setPositionImpl(x, y);
    }
    
    /** 
     * Changes the size of this widget.
     * Zero size is allowed but not negative.
     * Size is not checked against parent widgets.
     * 
     * When the size has changed then
     * - the parent widget's childChangedSize is called
     * - sizeChanged is called
     * - PropertyChangeEvent are fired for "width" and "height"
     *
     * This method should only be called from within the layout() method of the
     * parent. Otherwise it could lead to bad interaction with theming and result
     * in a wrong size after the theme has been applied.
     *
     * @param width The new width (including border)
     * @param height The new height (including border)
     * @return true if the size was changed, false if new size == old size
     * @throws java.lang.IllegalArgumentException if the size is negative
     * @see #sizeChanged()
     * @see #layout()
     */
    public boolean setSize(int width, int height) {
        if(width < 0 || height < 0) {
            throw new IllegalArgumentException("negative size");
        }
        int oldWidth = this.width;
        int oldHeight = this.height;
        if(oldWidth != width || oldHeight != height) {
            this.width = width;
            this.height = height;

            sizeChanged();
            
            if(propertyChangeSupport != null) {
                firePropertyChange("width", oldWidth, width);
                firePropertyChange("height", oldHeight, height);
            }
            return true;
        }
        return false;
    }
    
    /** 
     * Changes the inner size of this widget.
     * Calls setSize after adding the border width/height.
     * 
     * @param width The new width (exclusive border)
     * @param height The new height (exclusive border)
     * @return true if the size was changed, false if new size == old size
     * @see #setSize(int,int)
     */
    public boolean setInnerSize(int width, int height) {
        return setSize(width + borderLeft + borderRight, height + borderTop + borderBottom);
    }

    public short getBorderTop() {
        return borderTop;
    }

    public short getBorderLeft() {
        return borderLeft;
    }

    public short getBorderBottom() {
        return borderBottom;
    }

    public short getBorderRight() {
        return borderRight;
    }

    public int getBorderHorizontal() {
        return borderLeft + borderRight;
    }

    public int getBorderVertical() {
        return borderTop + borderBottom;
    }

    /**
     * Sets a border for this widget.
     * @param top the top border
     * @param left the left border
     * @param bottom the bottom  border
     * @param right the right border
     * @return true if the border values have changed
     * @throws IllegalArgumentException if any of the parameters is negative.
     */
    public boolean setBorderSize(int top, int left, int bottom, int right) {
        if(top < 0 || left < 0 || bottom < 0 || right < 0) {
            throw new IllegalArgumentException("negative border size");
        }
        if(this.borderTop != top ||  this.borderBottom != bottom ||
                this.borderLeft != left || this.borderRight != right) {
            int innerWidth = getInnerWidth();
            int innerHeight = getInnerHeight();
            int deltaLeft = left - this.borderLeft;
            int deltaTop = top - this.borderTop;
            this.borderLeft = (short)left;
            this.borderTop = (short)top;
            this.borderRight = (short)right;
            this.borderBottom = (short)bottom;
            
            // first adjust child position
            if(children != null && (deltaLeft != 0 || deltaTop != 0)) {
                for(int i=0,n=children.size() ; i<n ; i++) {
                    adjustChildPosition(children.get(i), deltaLeft, deltaTop);
                }
            }
            
            // now change size
            setInnerSize(innerWidth, innerHeight);
            borderChanged();
            return true;
        }
        return false;
    }
    
    /**
     * Sets a border for this widget.
     * @param horizontal the border width for left and right
     * @param vertical the border height for top and bottom
     * @return true if the border values have changed
     * @throws IllegalArgumentException if horizontal or vertical is negative.
     */
    public boolean setBorderSize(int horizontal, int vertical) {
        return setBorderSize(vertical, horizontal, vertical, horizontal);
    }
    
    /**
     * Sets a uniform border for this widget.
     * @param border the border width/height on all edges
     * @return true if the border values have changed
     * @throws IllegalArgumentException if border is negative.
     */
    public boolean setBorderSize(int border) {
        return setBorderSize(border, border, border, border);
    }
    
    /**
     * Sets the border width for this widget.
     * @param border the border object or null for no border
     * @return true if the border values have changed
     */
    public boolean setBorderSize(Border border) {
        if(border == null) {
            return setBorderSize(0, 0, 0, 0);
        } else {
            return setBorderSize(border.getBorderTop(), border.getBorderLeft(),
                                    border.getBorderBottom(), border.getBorderRight());
        }
    }

    public short getOffscreenExtraTop() {
        return offscreenExtraTop;
    }

    public short getOffscreenExtraLeft() {
        return offscreenExtraLeft;
    }

    public short getOffscreenExtraBottom() {
        return offscreenExtraBottom;
    }

    public short getOffscreenExtraRight() {
        return offscreenExtraRight;
    }
    
    /**
     * Sets the offscreen rendering extra area for this widget.
     * @param top the extra area on top
     * @param left the extra area on left
     * @param bottom the extra area on bottom
     * @param right the extra area on right
     * @throws IllegalArgumentException if any of the parameters is negative.
     * @see #setRenderOffscreen(de.matthiasmann.twl.Widget.RenderOffscreen) 
     */
    public void setOffscreenExtra(int top, int left, int bottom, int right) {
        if(top < 0 || left < 0 || bottom < 0 || right < 0) {
            throw new IllegalArgumentException("negative offscreen extra size");
        }
        this.offscreenExtraTop = (short)top;
        this.offscreenExtraLeft = (short)left;
        this.offscreenExtraBottom = (short)bottom;
        this.offscreenExtraRight = (short)right;
    }
    
    /**
     * Sets the offscreen rendering extra area for this widget.
     * @param offscreenExtra the border object or null for no extra area
     * @throws IllegalArgumentException if any of the values is negative.
     * @see #setRenderOffscreen(de.matthiasmann.twl.Widget.RenderOffscreen) 
     */
    public void setOffscreenExtra(Border offscreenExtra) {
        if(offscreenExtra == null) {
            setOffscreenExtra(0, 0, 0, 0);
        } else {
            setOffscreenExtra(offscreenExtra.getBorderTop(), offscreenExtra.getBorderLeft(),
                    offscreenExtra.getBorderBottom(), offscreenExtra.getBorderRight());
        }
    }
    
    /**
     * Returns the minimum width of the widget.
     * Layout manager will allocate atleast the minimum width to a widget even
     * when the container is not big enough.
     *
     * The default implementation will not return values smaller then the
     * current border width.
     *
     * @return the minimum width of the widget
     */
    public int getMinWidth() {
        return Math.max(minWidth, borderLeft + borderRight);
    }

    /**
     * Returns the minimum height of the widget.
     * Layout manager will allocate atleast the minimum height to a widget even
     * when the container is not big enough.
     *
     * The default implementation will not return values smaller then the
     * current border width.
     *
     * @return the minimum height of the widget
     */
    public int getMinHeight() {
        return Math.max(minHeight, borderTop + borderBottom);
    }

    /**
     * Sets the minimum size of the widget. This size includes the border.
     *
     * <p>The minimum size is set via the theme in {@link #applyThemeMinSize(de.matthiasmann.twl.ThemeInfo)}</p>
     *
     * @param width the minimum width
     * @param height the minimum height
     * @see #getMinWidth()
     * @see #getMinHeight()
     * @throws IllegalArgumentException when width or height is negative
     */
    public void setMinSize(int width, int height) {
        if(width < 0 || height < 0) {
            throw new IllegalArgumentException("negative size");
        }
        minWidth = (short)Math.min(width, Short.MAX_VALUE);
        minHeight = (short)Math.min(height, Short.MAX_VALUE);
    }

    /**
     * Computes the preferred inner width (the size of the widget without the border)
     *
     * The default implementation uses the current position of the children.
     *
     * It is highly recommended to override this method as the default implementation
     * lead to unstable layouts.
     *
     * The default behavior might change in the future to provide a better default
     * behavior.
     *
     * @return the preferred inner width
     */
    public int getPreferredInnerWidth() {
        int right = getInnerX();
        if(children != null) {
            for(int i=0,n=children.size() ; i<n ; i++) {
                Widget child = children.get(i);
                right = Math.max(right, child.getRight());
            }
        }
        return right - getInnerX();
    }

    /**
     * Returns the preferred width based on it's children and preferred inner width.
     *
     * Subclasses can overwrite this method to compute the preferred size differently.
     *
     * @return the preferred width.
     * @see #getPreferredInnerWidth()
     */
    public int getPreferredWidth() {
        int prefWidth = borderLeft + borderRight + getPreferredInnerWidth();
        Image bg = getBackground();
        if(bg != null) {
            prefWidth = Math.max(prefWidth, bg.getWidth());
        }
        return Math.max(minWidth, prefWidth);
    }

    /**
     * Computes the preferred inner height (the size of the widget without the border)
     *
     * The default implementation uses the current position of the children.
     *
     * It is highly recommended to override this method as the default implementation
     * lead to unstable layouts.
     *
     * The default behavior might change in the future to provide a better default
     * behavior.
     *
     * @return the preferred inner height
     */
    public int getPreferredInnerHeight() {
        int bottom = getInnerY();
        if(children != null) {
            for(int i=0,n=children.size() ; i<n ; i++) {
                Widget child = children.get(i);
                bottom = Math.max(bottom, child.getBottom());
            }
        }
        return bottom - getInnerY();
    }

    /**
     * Returns the preferred height.
     *
     * This method determines the preferred height based on it's children.
     * Subclasses can overwrite this method to compute the preferred size differently.
     *
     * @return the preferred height.
     * @see #getPreferredInnerHeight() 
     */
    public int getPreferredHeight() {
        int prefHeight = borderTop + borderBottom + getPreferredInnerHeight();
        Image bg = getBackground();
        if(bg != null) {
            prefHeight = Math.max(prefHeight, bg.getHeight());
        }
        return Math.max(minHeight, prefHeight);
    }

    /**
     * Returns the maximum width of the widget.
     *
     * A maximum of 0 means that the widgets wants it's preferred size and no
     * extra space from layout.
     * A value &gt; 0 is used for widgets which can expand to cover available
     * area to that maximum.
     *
     * @return the maximum width
     */
    public int getMaxWidth() {
        return maxWidth;
    }

    /**
     * Returns the maximum height of the widget.
     *
     * A maximum of 0 means that the widgets wants it's preferred size and no
     * extra space from layout.
     * A value &gt; 0 is used for widgets which can expand to cover available
     * area to that maximum.
     *
     * @return the maximum height
     */
    public int getMaxHeight() {
        return maxHeight;
    }

    /**
     * Sets the maximum size of the widget.
     * A value of 0 means no expansion, use {@link Short#MAX_VALUE} for unbounded expansion.
     *
     * <p>The maximum size is set via the theme in {@link #applyThemeMaxSize(de.matthiasmann.twl.ThemeInfo)}</p>
     * 
     * @param width the maximum width
     * @param height the maximum height
     * @see #getMaxWidth()
     * @see #getMaxHeight()
     * @throws IllegalArgumentException when width or height is negative
     */
    public void setMaxSize(int width, int height) {
        if(width < 0 || height < 0) {
            throw new IllegalArgumentException("negative size");
        }
        maxWidth = (short)Math.min(width, Short.MAX_VALUE);
        maxHeight = (short)Math.min(height, Short.MAX_VALUE);
    }

    /**
     * A helper method to compute the size of a widget based on min, max and
     * preferred size.
     *
     * If max size is &gt; 0 then the preferred size is limited to max.
     *
     * @param min the minimum size of the widget
     * @param preferred the preferred size of the widget
     *                  or the available space where the widget is fitted into
     * @param max the maximum size of the widget
     * @return Math.max(min, (max > 0) ? Math.min(preferred, max) : preferred)
     */
    public static int computeSize(int min, int preferred, int max) {
        if(max > 0) {
            preferred = Math.min(preferred, max);
        }
        return Math.max(min, preferred);
    }

    /**
     * Auto adjust the size of this widget based on it's preferred size.
     * 
     * Subclasses can provide more functionality
     */
    public void adjustSize() {
        /*
        System.out.println(this+" minSize="+getMinWidth()+","+getMinHeight()+
                " prefSize="+getPreferredWidth()+","+getPreferredHeight()+
                " maxSize="+getMaxWidth()+","+getMaxHeight());
         * */
        setSize(computeSize(getMinWidth(), getPreferredWidth(), getMaxWidth()),
                computeSize(getMinHeight(), getPreferredHeight(), getMaxHeight()));
        validateLayout();
    }

    /**
     * Called when something has changed which affected the layout of this widget.
     *
     * The default implementation calls invalidateLayoutLocally() followed by childInvalidateLayout()
     *
     * Called by the default implementation of borderChanged.
     *
     * @see #invalidateLayoutLocally()
     * @see #borderChanged()
     */
    public void invalidateLayout() {
        if(layoutInvalid < LAYOUT_INVALID_GLOBAL) {
            invalidateLayoutLocally();
            if(parent != null) {
                layoutInvalid = LAYOUT_INVALID_GLOBAL;
                parent.childInvalidateLayout(this);
            }
        }
    }
    
    /**
     * Calls layout() if the layout is marked invalid.
     * @see #invalidateLayout()
     * @see #layout()
     */
    public void validateLayout() {
        if(layoutInvalid != 0) {
            /* Reset the flag first so that widgets like TextArea can invalidate
             * their layout from inside layout()
             */
            layoutInvalid = 0;
            layout();
        }
        if(children != null) {
            for(int i=0,n=children.size() ; i<n ; i++) {
                children.get(i).validateLayout();
            }
        }
    }
    
    /**
     * Returns the current theme name of this widget.
     * The default theme name is the lower case simple class name of this widget.
     * @return the current theme name of this widget
     */
    public String getTheme() {
        return theme;
    }
    
    /**
     * Changes the theme name of this widget - DOES NOT call reapplyTheme()
     *
     * <p>If the theme name is empty then this widget won't receive theme data
     * and is not included in the theme path, but it's children are still
     * themed.</p>
     *
     * <p>A theme name must not contain spaces or '*'.
     * A '/' is only allowed as first character to indicate an absolute theme path.
     * A '.' is only allowed for absolute theme paths.</p>
     * 
     * @param theme The new theme path element
     * @throws java.lang.NullPointerException if theme is null
     * @throws java.lang.IllegalArgumentException if the theme name is invalid
     * @see GUI#applyTheme(ThemeManager)
     * @see #reapplyTheme()
     * @see #getThemePath()
     * @see #isAbsoluteTheme(java.lang.String)
     */
    public void setTheme(String theme) {
        if(theme == null) {
            throw new IllegalArgumentException("theme is null");
        }
        if(theme.length() > 0) {
            int slashIdx = theme.lastIndexOf('/');
            if(slashIdx > 0) {
                throw new IllegalArgumentException("'/' is only allowed as first character in theme name");
            }
            if(slashIdx < 0) {
                if(theme.indexOf('.') >= 0) {
                    throw new IllegalArgumentException("'.' is only allowed for absolute theme paths");
                }
            } else if(theme.length() == 1) {
                throw new IllegalArgumentException("'/' requires a theme path");
            }
            for(int i=0,n=theme.length() ; i<n ; i++) {
                char ch = theme.charAt(i);
                if(Character.isISOControl(ch) || ch == '*') {
                    throw new IllegalArgumentException("invalid character '" + TextUtil.toPrintableString(ch) + "' in theme name");
                }
            }
        }
        this.theme = theme;
    }
    
    /**
     * Returns this widget's theme path by concatenating the theme names
     * from all parents separated by '.'.
     *
     * If a parent theme is empty then it will be omitted from the theme path.
     *
     * The theme path will start with the first absolute theme starting from
     * this widget up to the GUI.
     *
     * @return the effective theme path - can be empty
     */
    public final String getThemePath() {
        return getThemePath(0).toString();
    }

    /**
     * Returns true if paint() is clipped to this widget.
     * @return true if paint() is clipped to this widget
     */
    public boolean isClip() {
        return clip;
    }

    /**
     * Sets whether paint() must be clipped to this Widget or not.
     *
     * Clipping is performed for the whole widget and all it's children.
     * The clip area is the outer area of the widget (it does include the border).
     *
     * If the widget theme has effects which extend outside of the widget (like
     * shadow or glow) then clipping will also clip the this effect. A work
     * around is to not apply clipping to the widget itself but to a child
     * which will act as a clip container - this child may not need a theme.
     *
     * @param clip true if clipping must be used - default is false
     **/
    public void setClip(boolean clip) {
        this.clip = clip;
    }

    /**
     * Returns if this widget will handle the FOCUS_KEY.
     * @return if this widget will handle the FOCUS_KEY.
     */
    public boolean isFocusKeyEnabled() {
        return focusKeyEnabled;
    }

    /**
     * Controls the handling of the FOCUS_KEY.
     * <p>The default is true.</p>
     * <p>When enabled the focus key (TAB) will cycle through all (indirect)
     * children which can receive keyboard focus. The order is defined
     * by {@link #getKeyboardFocusOrder() }.</p>
     * @param focusKeyEnabled if true this widget will handle the focus key.
     */
    public void setFocusKeyEnabled(boolean focusKeyEnabled) {
        this.focusKeyEnabled = focusKeyEnabled;
    }

    /**
     * Returns the current background image or null.
     * @return the current background image or null
     * @see #paintBackground(de.matthiasmann.twl.GUI)
     */
    public Image getBackground() {
        return background;
    }

    /**
     * Sets the background image that should be drawn before drawing this widget
     * @param background the new background image - can be null
     * @see #paintBackground(de.matthiasmann.twl.GUI)
     */
    public void setBackground(Image background) {
        this.background = background;
    }

    /**
     * Returns the current overlay image or null.
     * @return the current overlay image or null.
     * @see #paintOverlay(de.matthiasmann.twl.GUI)
     */
    public Image getOverlay() {
        return overlay;
    }

    /**
     * Sets the overlay image that should be drawn after drawing the children
     * @param overlay the new overlay image - can be null
     * @see #paintOverlay(de.matthiasmann.twl.GUI)
     */
    public void setOverlay(Image overlay) {
        this.overlay = overlay;
    }
    
    /**
     * Returns the mouse cursor which should be used for the given
     * mouse coordinates and modifiers.
     * 
     * The default implementation calls {@link #getMouseCursor() }
     * 
     * @param evt only {@link Event#getMouseX() }, {@link Event#getMouseY() } and {@link Event#getModifiers() } are valid.
     * @return the mouse cursor or null when no mouse cursor is defined for this widget
     */
    public MouseCursor getMouseCursor(Event evt) {
        return getMouseCursor();
    }

    public MouseCursor getMouseCursor() {
        return mouseCursor;
    }

    public void setMouseCursor(MouseCursor mouseCursor) {
        this.mouseCursor = mouseCursor;
    }

    /**
     * Returns the number of children in this widget.
     * @return the number of children in this widget
     */
    public final int getNumChildren() {
        if(children != null) {
            return children.size();
        }
        return 0;
    }

    /**
     * Returns the child at the given index
     * @param index
     * @return the child widget
     * @throws java.lang.IndexOutOfBoundsException if the index is invalid
     */
    public final Widget getChild(int index) throws IndexOutOfBoundsException {
        if(children != null) {
            return children.get(index);
        }
        throw new IndexOutOfBoundsException();
    }

    /**
     * Adds a new child at the end of this widget.
     * This call is equal to <code>insertChild(child, getNumChildren())</code>
     *
     * @param child the child that should be added
     * @throws java.lang.NullPointerException if child is null
     * @throws java.lang.IllegalArgumentException if the child is already in a tree
     * @see #insertChild(de.matthiasmann.twl.Widget, int)
     * @see #getNumChildren()
     */
    public void add(Widget child) {
        insertChild(child, getNumChildren());
    }
    
    /**
     * Inserts a new child into this widget.
     * The position of the child is treated as relative to this widget and adjusted.
     * If a theme was applied to this widget then this theme is also applied to the new child.
     * 
     * @param child the child that should be inserted
     * @param index the index where it should be inserted
     * @throws java.lang.IndexOutOfBoundsException if the index is invalid
     * @throws java.lang.NullPointerException if child is null
     * @throws java.lang.IllegalArgumentException if the child is already in a tree
     */
    public void insertChild(Widget child, int index) throws IndexOutOfBoundsException {
        if(child == null) {
            throw new IllegalArgumentException("child is null");
        }
        if(child == this) {
            throw new IllegalArgumentException("can't add to self");
        }
        if(child.parent != null) {
            throw new IllegalArgumentException("child widget already in tree");
        }
        if(children == null) {
            children = new ArrayList<Widget>();
        }
        if(index < 0 || index > children.size()) {
            throw new IndexOutOfBoundsException();
        }
        child.setParent(this);  // can throw exception - see PopupWindow
        children.add(index, child);
        GUI gui = getGUI();
        if(gui != null) {
            child.recursivelySetGUI(gui);
        }
        adjustChildPosition(child, posX + borderLeft, posY + borderTop);
        child.recursivelyEnabledChanged(null, enabled);
        if(gui != null) {
            child.recursivelyAddToGUI(gui);
        }
        if(themeManager != null) {
            child.applyTheme(themeManager);
        }
        try {
            childAdded(child);
        } catch(Exception ex) {
            getLogger().log(Level.SEVERE, "Exception in childAdded()", ex);
        }
        // A newly added child can't have open popups
        // because it needs a GUI for this - and it had no parent up to now
    }
    
    /**
     * Returns the index of the specified child in this widget.
     * Uses object identity for comparing.
     * @param child the child which index should be returned
     * @return the index of the child or -1 if it was not found
     */
    public final int getChildIndex(Widget child) {
        if(children != null) {
            // can't use children.indexOf(child) as this uses equals()
            for(int i=0,n=children.size() ; i<n ; i++) {
                if(children.get(i) == child) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Removes the specified child from this widget.
     * Uses object identity for comparing.
     * @param child the child that should be removed.
     * @return true if the child was found and removed.
     */
    public boolean removeChild(Widget child) {
        int idx = getChildIndex(child);
        if(idx >= 0) {
            removeChild(idx);
            return true;
        }
        return false;
    }

    /**
     * Removes the specified child from this widget.
     * The position of the removed child is changed to the relative
     * position to this widget.
     * Calls invalidateLayout after removing the child.
     * 
     * @param index the index of the child
     * @return the removed widget
     * @throws java.lang.IndexOutOfBoundsException if the index is invalid
     * @see #invalidateLayout()
     */
    public Widget removeChild(int index) throws IndexOutOfBoundsException {
        if(children != null) {
            Widget child = children.remove(index);
            unparentChild(child);
            if(lastChildMouseOver == child) {
                lastChildMouseOver = null;
            }
            if(focusChild == child) {
                focusChild = null;
            }
            childRemoved(child);
            return child;
        }
        throw new IndexOutOfBoundsException();
    }

    /**
     * Removes all children of this widget.
     * The position of the all removed children is changed to the relative
     * position to this widget.
     * Calls allChildrenRemoved after removing all children.
     * 
     * @see #allChildrenRemoved()
     */
    public void removeAllChildren() {
        if(children != null) {
            focusChild = null;
            lastChildMouseOver = null;
            for(int i=0,n=children.size() ; i<n ; i++) {
                Widget child = children.get(i);
                unparentChild(child);
            }
            children.clear(); // we expect that new children will be added - so keep list
            if(hasOpenPopup) {
                GUI gui = getGUI();
                assert(gui != null);
                recalcOpenPopups(gui);
            }
            allChildrenRemoved();
        }
    }
    
    /**
     * Clean up GL resources. When overwritten then super method must be called.
     */
    public void destroy() {
        if(children != null) {
            for(int i=0,n=children.size() ; i<n ; i++) {
                children.get(i).destroy();
            }
        }
        if(offscreenSurface != null) {
            offscreenSurface.destroy();
            offscreenSurface = null;
        }
    }

    public boolean canAcceptKeyboardFocus() {
        return canAcceptKeyboardFocus;
    }

    public void setCanAcceptKeyboardFocus(boolean canAcceptKeyboardFocus) {
        this.canAcceptKeyboardFocus = canAcceptKeyboardFocus;
    }

    public boolean isDepthFocusTraversal() {
        return depthFocusTraversal;
    }

    public void setDepthFocusTraversal(boolean depthFocusTraversal) {
        this.depthFocusTraversal = depthFocusTraversal;
    }

    /**
     * Requests that the keyboard focus is transfered to this widget.
     *
     * <p>Use with care - users don't expect focus changes while working with the UI</p>
     *
     * <p>Focus transfer only works when the widget is added to the GUI tree.
     * See {@link #getGUI()}.</p>
     * 
     * @return true if keyboard focus was transfered to this widget.
     */
    public boolean requestKeyboardFocus() {
        if(parent != null && visible) {
            if(parent.focusChild == this) {
                return true;
            }

            boolean clear = focusTransferStart();
            try {
                return parent.requestKeyboardFocus(this);
            } finally {
                focusTransferClear(clear);
            }
        }
        return false;
    }

    /**
     * If this widget currently has the keyboard focus, then the keyboard focus is removed.
     * The focus will be transferred to the parent widget.
     */
    public void giveupKeyboardFocus() {
        if(parent != null && parent.focusChild == this) {
            parent.requestKeyboardFocus(null);
        }
    }
    
    /**
     * Checks if this widget has the keyboard focus
     * @return true if this widget has the keyboard focus
     */
    public boolean hasKeyboardFocus() {
        if(parent != null) {
            return parent.focusChild == this;
        }
        return false;
    }

    public boolean focusNextChild() {
        return moveFocus(true, +1);
    }

    public boolean focusPrevChild() {
        return moveFocus(true, -1);
    }

    public boolean focusFirstChild() {
        return moveFocus(false, +1);
    }

    public boolean focusLastChild() {
        return moveFocus(false, -1);
    }

    /**
     * Returns the animation state object.
     * @return the animation state object.
     */
    public AnimationState getAnimationState() {
        return animState;
    }

    /**
     * Returns true if the animation state of this widget is shared with
     * another widget.
     * 
     * A widget with a shared animation state should normally not modify
     * the animation state itself. How a shared animation state is used
     * depends on the widgets.
     * 
     * @return true if it is shared
     * @see #Widget(de.matthiasmann.twl.AnimationState) 
     */
    public boolean hasSharedAnimationState() {
        return sharedAnimState;
    }

    /**
     * Returns the current tine animation object or null if none was set
     * @return the current tine animation object or null if none was set
     */
    public TintAnimator getTintAnimator() {
        return tintAnimator;
    }

    /**
     * Sets the tint animation object. Can be null to disable tinting.
     * @param tintAnimator the new tint animation object
     */
    public void setTintAnimator(TintAnimator tintAnimator) {
        this.tintAnimator = tintAnimator;
    }

    /**
     * Returns the currently active offscreen rendering delegate or null if none was set
     * @return the currently active offscreen rendering delegate or null if none was set
     */
    public RenderOffscreen getRenderOffscreen() {
        return renderOffscreen;
    }

    /**
     * Sets set offscreen rendering delegate. Can be null to disable offscreen rendering.
     * @param renderOffscreen the offscreen rendering delegate.
     */
    public void setRenderOffscreen(RenderOffscreen renderOffscreen) {
        this.renderOffscreen = renderOffscreen;
    }

    
    /**
     * Returns the currently set tooltip content.
     * @return the currently set tooltip content. Can be null.
     */
    public Object getTooltipContent() {
        return tooltipContent;
    }

    /**
     * Changes the tooltip context. If the tooltip is currently active then
     * it's refreshed with the new content.
     *
     * @param tooltipContent the new tooltip content.
     * @see #updateTooltip()
     * @see #getTooltipContent()
     */
    public void setTooltipContent(Object tooltipContent) {
        this.tooltipContent = tooltipContent;
        updateTooltip();
    }
    
    /**
     * Returns the current input map.
     * @return the current input map or null.
     */
    public InputMap getInputMap() {
        return inputMap;
    }

    /**
     * Sets the input map for key strokes.
     * 
     * @param inputMap the input map or null.
     * @see #handleKeyStrokeAction(java.lang.String, de.matthiasmann.twl.Event)
     */
    public void setInputMap(InputMap inputMap) {
        this.inputMap = inputMap;
    }

    /**
     * Returns the current action map. If no action map has been set then
     * {@code null} is returned.
     * @return the current action map or null.
     */
    public ActionMap getActionMap() {
        return actionMap;
    }

    /**
     * Returns the current action map. If no action map has been set then
     * a new one is created and set (setActionMap is not called).
     * @return the current action map (or the new action map).
     */
    public ActionMap getOrCreateActionMap() {
        if(actionMap == null) {
            actionMap = new ActionMap();
        }
        return actionMap;
    }
    
    /**
     * Installs an action map for this widget.
     * @param actionMap the new action map or null.
     */
    public void setActionMap(ActionMap actionMap) {
        this.actionMap = actionMap;
    }

    /**
     * Returns the visible widget at the specified location.
     * Use this method to locate drag&drop tragets.
     *
     * Subclasses can overwrite this method hide implementation details.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the widget at that location.
     */
    public Widget getWidgetAt(int x, int y) {
        Widget child = getChildAt(x, y);
        if(child != null) {
            return child.getWidgetAt(x, y);
        }
        return this;
    }

    //
    // start of API for derived widgets
    //
    
    /**
     * Apply the given theme.
     * 
     * This method also calls invalidateLayout()
     * 
     * @param themeInfo The theme info for this widget
     */
    protected void applyTheme(ThemeInfo themeInfo) {
        applyThemeBackground(themeInfo);
        applyThemeOverlay(themeInfo);
        applyThemeBorder(themeInfo);
        applyThemeOffscreenExtra(themeInfo);
        applyThemeMinSize(themeInfo);
        applyThemeMaxSize(themeInfo);
        applyThemeMouseCursor(themeInfo);
        applyThemeInputMap(themeInfo);
        applyThemeTooltip(themeInfo);
        invalidateLayout();
    }

    protected void applyThemeBackground(ThemeInfo themeInfo) {
        setBackground(themeInfo.getImage("background"));
    }

    protected void applyThemeOverlay(ThemeInfo themeInfo) {
        setOverlay(themeInfo.getImage("overlay"));
    }

    protected void applyThemeBorder(ThemeInfo themeInfo) {
        setBorderSize(themeInfo.getParameterValue("border", false, Border.class));
    }

    protected void applyThemeOffscreenExtra(ThemeInfo themeInfo) {
        setOffscreenExtra(themeInfo.getParameterValue("offscreenExtra", false, Border.class));
    }

    protected void applyThemeMinSize(ThemeInfo themeInfo) {
        setMinSize(
                themeInfo.getParameter("minWidth", 0),
                themeInfo.getParameter("minHeight", 0));
    }

    protected void applyThemeMaxSize(ThemeInfo themeInfo) {
        setMaxSize(
                themeInfo.getParameter("maxWidth", Short.MAX_VALUE),
                themeInfo.getParameter("maxHeight", Short.MAX_VALUE));
    }

    protected void applyThemeMouseCursor(ThemeInfo themeInfo) {
        setMouseCursor(themeInfo.getMouseCursor("mouseCursor"));
    }

    protected void applyThemeInputMap(ThemeInfo themeInfo) {
        setInputMap(themeInfo.getParameterValue("inputMap", false, InputMap.class));
    }

    protected void applyThemeTooltip(ThemeInfo themeInfo) {
        themeTooltipContent = themeInfo.getParameterValue("tooltip", false);
        if(tooltipContent == null) {
            updateTooltip();
        }
    }

    protected Object getThemeTooltipContent() {
        return themeTooltipContent;
    }

    /**
     * Automatic tooltip support.
     *
     * This function is called when the mouse is idle over the widget for a certain time.
     *
     * The default implementation returns the result from {@code getTooltipContent}
     * if it is non null, otherwise the result from {@code getThemeTooltipContent}
     * is returned.
     *
     * This method is not called if the tooltip is already open and the mouse is
     * moved but does not leave this widget. If the tooltip depends on the mouse
     * position then {@code updateTooltip} must be called from {@code handleEvent}.
     *
     * @param mouseX the mouse X coordinate
     * @param mouseY the mouse Y coordinate
     * @return the tooltip message or null if no tooltip is specified.
     * @see #updateTooltip()
     */
    protected Object getTooltipContentAt(int mouseX, int mouseY) {
        Object content = getTooltipContent();
        if(content == null) {
            content = getThemeTooltipContent();
        }
        return content;
    }

    /**
     * Called by setTooltipContent and applyThemeTooltip.
     * If this widget currently has an open tooltip then this tooltip is updated
     * to show the new content.
     *
     * @see #getTooltipContent()
     */
    protected void updateTooltip() {
        GUI gui = getGUI();
        if(gui != null) {
            gui.requestTooltipUpdate(this, false);
        }
    }

    /**
     * If this widget currently has an open tooltip then this tooltip is reset
     * and the tooltip timer is restarted.
     *
     * @see #getTooltipContent()
     */
    protected void resetTooltip() {
        GUI gui = getGUI();
        if(gui != null) {
            gui.requestTooltipUpdate(this, true);
        }
    }

    /**
     * Installs an action mapping for the given action in the current action map.
     * If no action map is set then a new one will be created.
     *
     * The mapping will invoke a public method on {@code this} widget.
     *
     * This is equal to calling {@code addActionMapping} on {@code ActionMap} with
     * {@code this} as target and {@code ActionMap.FLAG_ON_PRESSED} as flags.
     *
     * @param action the action name
     * @param methodName the method name to invoke on this widget
     * @param params optional parameters which can be passed to the method
     * @see #getActionMap()
     * @see ActionMap#addMapping(java.lang.String, java.lang.Object, java.lang.reflect.Method, java.lang.Object[], int)
     * @see #getInputMap()
     */
    protected void addActionMapping(String action, String methodName, Object ... params) {
        getOrCreateActionMap().addMapping(action, this, methodName, params, ActionMap.FLAG_ON_PRESSED);
    }

    /**
     * If the widget changed some internal state which may
     * require different theme information then this function
     * can be used to reapply the current theme.
     */
    public void reapplyTheme() {
        if(themeManager != null) {
            applyTheme(themeManager);
        }
    }
    
    /**
     * Checks whether the mouse is inside the widget or not.
     * <p>Calls {@link #isInside(int, int)} with the mouse coordinates.</p>
     *
     * @param evt the mouse event
     * @return true if the widgets wants to claim this mouse event
     */
    protected boolean isMouseInside(Event evt) {
        return isInside(evt.getMouseX(), evt.getMouseY());
    }
    
    /**
     * Called when an event occurred that this widget could be interested in.
     *
     * <p>The default implementation handles only keyboard events and delegates
     * them to the child widget which has keyboard focus.
     * If focusKey handling is enabled then this widget cycles the keyboard
     * focus through it's children.
     * If the key was not consumed by a child or focusKey and an inputMap is
     * specified then the event is translated by the InputMap and
     * <code>handleKeyStrokeAction</code> is called when a mapping was found.</p>
     *
     * <p>If the widget wants to receive mouse events then it must return true
     * for all mouse events except for MOUSE_WHEEL (which is optional) event.
     * Otherwise the following mouse event are not send. Before mouse movement
     * or button events are send a MOUSE_ENTERED event is send first.</p>
     * 
     * @param evt The event - do not store this object - it may be reused
     * @return true if the widget handled this event
     * @see #setFocusKeyEnabled(boolean)
     * @see #handleKeyStrokeAction(java.lang.String, de.matthiasmann.twl.Event)
     * @see #setInputMap(de.matthiasmann.twl.InputMap)
     */
    protected boolean handleEvent(Event evt) {
        if(evt.isKeyEvent()) {
            return handleKeyEvent(evt);
        }
        return false;
    }

    /**
     * Called when a key stroke was found in the inputMap.
     *
     * @param action the action associated with the key stroke
     * @param event the event which caused the action
     * @return true if the action was handled
     * @see #setInputMap(de.matthiasmann.twl.InputMap) 
     */
    protected boolean handleKeyStrokeAction(String action, Event event) {
        if(actionMap != null) {
            return actionMap.invoke(action, event);
        }
        return false;
    }
    
    /**
     * Moves the child at index from to index to. This will shift the position
     * of all children in between.
     * 
     * @param from the index of the child that should be moved
     * @param to the new index for the child at from
     * @throws java.lang.IndexOutOfBoundsException if from or to are invalid
     */
    protected void moveChild(int from, int to) {
        if(children == null) {
            throw new IndexOutOfBoundsException();
        }
        if(to < 0 || to >= children.size()) {
            throw new IndexOutOfBoundsException("to");
        }
        if(from < 0 || from >= children.size()) {
            throw new IndexOutOfBoundsException("from");
        }
        Widget child = children.remove(from);
        children.add(to, child);
    }
    
    /**
     * A child requests keyboard focus.
     * Default implementation will grant keyboard focus and
     * request itself keyboard focus.
     *
     * @param child The child that wants keyboard focus
     * @return true if the child received the focus.
     */
    protected boolean requestKeyboardFocus(Widget child) {
        if(child != null && child.parent != this) {
            throw new IllegalArgumentException("not a direct child");
        }
        if(focusChild != child) {
            if(child == null) {
                recursivelyChildFocusLost(focusChild);
                focusChild = null;
                keyboardFocusChildChanged(null);
            } else {
                boolean clear = focusTransferStart();
                try {
                    // first request focus for ourself
                    {
                        FocusGainedCause savedCause = focusGainedCause;
                        if(savedCause == null) {
                            focusGainedCause = FocusGainedCause.CHILD_FOCUSED;
                        }
                        try {
                            if(!requestKeyboardFocus()) {
                                return false;
                            }
                        } finally {
                            focusGainedCause = savedCause;
                        }
                    }

                    // second change focused child
                    recursivelyChildFocusLost(focusChild);
                    focusChild = child;
                    keyboardFocusChildChanged(child);
                    if(!child.sharedAnimState) {
                        child.animState.setAnimationState(STATE_KEYBOARD_FOCUS, true);
                    }

                    // last inform the child widget why it gained keyboard focus
                    FocusGainedCause cause = child.focusGainedCause;
                    Widget[] fti = focusTransferInfo.get();
                    child.keyboardFocusGained(
                            (cause != null) ? cause : FocusGainedCause.MANUAL,
                            (fti != null) ? fti[0] : null);
                } finally {
                    focusTransferClear(clear);
                }
            }
        }
        if(!sharedAnimState) {
            animState.setAnimationState(STATE_HAS_FOCUSED_CHILD, focusChild != null);
        }
        return focusChild != null;
    }
    
    /**
     * Called when this widget is removed from the GUI tree.
     * After this call getGUI() will return null.
     * 
     * @param gui the GUI object - same as getGUI()
     * @see #getGUI()
     */
    protected void beforeRemoveFromGUI(GUI gui) {
    }
    
    /**
     * Called after this widget has been added to a GUI tree.
     * 
     * @param gui the GUI object - same as getGUI()
     * @see #getGUI()
     */
    protected void afterAddToGUI(GUI gui) {
    }
    
    /**
     * Called when the layoutInvalid flag is set.
     *
     * The default implementation does nothing.
     */
    protected void layout() {
    }

    /**
     * Called when the position of this widget was changed.
     * The default implementation does nothing.
     * 
     * Child positions are already updated to retain the absolute
     * coordinate system. This has the side effect of firing child's
     * positionChanged before the parent's.
     */
    protected void positionChanged() {
    }

    /**
     * Called when the size of this widget has changed.
     * The default implementation calls invalidateLayoutLocally. As size changes
     * are normally the result of the parent's layout() function.
     * 
     * @see #invalidateLayoutLocally()
     */
    protected void sizeChanged() {
        invalidateLayoutLocally();
    }

    /**
     * Called when the border size has changed.
     * The default implementation calls invalidateLayout.
     * 
     * @see #invalidateLayout()
     */
    protected void borderChanged() {
        invalidateLayout();
    }

    /**
     * Called when the layout of a child has been invalidated.
     * The default implementation calls invalidateLayout.
     *
     * @param child the child which was invalidated
     * @see #invalidateLayout()
     */
    protected void childInvalidateLayout(Widget child) {
        invalidateLayout();
    }

    /**
     * A new child has been added.
     * The default implementation calls invalidateLayout.
     *
     * @param child the new child
     * @see #invalidateLayout()
     */
    protected void childAdded(Widget child) {
        invalidateLayout();
    }

    /**
     * A child has been removed.
     * The default implementation calls invalidateLayout.
     * 
     * @param exChild the removed widget - no longer a child
     * @see #invalidateLayout()
     */
    protected void childRemoved(Widget exChild) {
        invalidateLayout();
    }

    /**
     * All children have been removed.
     * This is called by {@code removeAllChildren} instead of {@code childRemoved}.
     * 
     * The default implementation calls invalidateLayout.
     * 
     * @see #invalidateLayout()
     */
    protected void allChildrenRemoved() {
        invalidateLayout();
    }

    /**
     * Called when the visibility state of a child was changed.
     * The default implementation does nothing.
     * 
     * @param child the child which changed it's visibility state
     * @see #setVisible(boolean) 
     */
    protected void childVisibilityChanged(Widget child) {
    }

    /**
     * The current keyboard focus child has changed.
     * The default implementation does nothing.
     * 
     * @param child The child which has now the keyboard focus in this hierachy level or null
     */
    protected void keyboardFocusChildChanged(Widget child) {
    }

    /**
     * Called when this widget has lost the keyboard focus.
     * The default implementation does nothing.
     */
    protected void keyboardFocusLost() {
    }

    /**
     * Called when this widget has gained the keyboard focus.
     * The default implementation does nothing.
     *
     * @see #keyboardFocusGained(de.matthiasmann.twl.FocusGainedCause, de.matthiasmann.twl.Widget) 
     */
    protected void keyboardFocusGained() {
    }

    /**
     * Called when this widget has gained the keyboard focus.
     * The default implementation calls {@link #keyboardFocusGained() }
     *
     * @param cause the cause for the this focus transfer
     * @param previousWidget the widget which previously had the keyboard focus - can be null.
     */
    protected void keyboardFocusGained(FocusGainedCause cause, Widget previousWidget) {
        // System.out.println(this + " " + cause + " " + previousWidget);
        keyboardFocusGained();
    }

    /**
     * This method is called when this widget has been disabled,
     * either directly or one of it's parents.
     *
     * <p>The default implementation does nothing.</p>
     */
    protected void widgetDisabled() {
    }

    /**
     * Paints this widget and it's children.
     * <p>A subclass should overwrite paintWidget() instead of this function.</p>
     * 
     * <p>The default implementation calls the following method in order:</p><ol>
     * <li>{@link #paintBackground(de.matthiasmann.twl.GUI)}</li>
     * <li>{@link #paintWidget(de.matthiasmann.twl.GUI)}</li>
     * <li>{@link #paintChildren(de.matthiasmann.twl.GUI)}</li>
     * <li>{@link #paintOverlay(de.matthiasmann.twl.GUI)}</li></ol>
     *
     * @param gui the GUI object
     */
    protected void paint(GUI gui) {
        paintBackground(gui);
        paintWidget(gui);
        paintChildren(gui);
        paintOverlay(gui);
    }
    
    /**
     * Called by {@link #paint(de.matthiasmann.twl.GUI)} after painting the
     * background and before painting all children.
     * 
     * <p>This should be overwritten instead of {@code paint} if normal themeable
     * painting is desired by the subclass.</p>
     * 
     * <p>The default implementation does nothing.</p>
     * 
     * @param gui the GUI object - it's the same as getGUI()
     */
    protected void paintWidget(GUI gui) {
    }
    
    /**
     * Paint the background image of this widget.
     * @param gui the GUI object
     * @see #paint(de.matthiasmann.twl.GUI) 
     */
    protected void paintBackground(GUI gui) {
        Image bgImage = getBackground();
        if(bgImage != null) {
            bgImage.draw(getAnimationState(), posX, posY, width, height);
        }
    }

    /**
     * Paints the overlay image of this widget.
     * @param gui the GUI object
     * @see #paint(de.matthiasmann.twl.GUI) 
     */
    protected void paintOverlay(GUI gui) {
        Image ovImage = getOverlay();
        if(ovImage != null) {
            ovImage.draw(getAnimationState(), posX, posY, width, height);
        }
    }

    /**
     * Paints all children in index order. Invisible children are skipped.
     * @param gui the GUI object
     * @see #paint(de.matthiasmann.twl.GUI) 
     */
    protected void paintChildren(GUI gui) {
        if(children != null) {
            for(int i=0,n=children.size() ; i<n ; i++) {
                Widget child = children.get(i);
                if(child.visible) {
                    child.drawWidget(gui);
                }
            }
        }
    }

    /**
     * Paints a specified child. Does not check for visibility.
     *
     * @param gui the GUI object
     * @param child the child Widget
     */
    protected void paintChild(GUI gui, Widget child) {
        if(child.parent != this) {
            throw new IllegalArgumentException("can only render direct children");
        }
        child.drawWidget(gui);
    }

    /**
     * Called after all other widgets have been rendered when a drag operation is in progress.
     * The mouse position can be outsife of this widget
     * 
     * @param gui the GUI object
     * @param mouseX the current mouse X position
     * @param mouseY the current mouse Y position
     * @param modifier the current active modifiers - see {@link Event#getModifiers() }
     */
    protected void paintDragOverlay(GUI gui, int mouseX, int mouseY, int modifier) {
    }
    
    /**
     * Invalidates only the layout of this widget. Does not invalidate the layout of the parent.
     * Should only be used for things like scrolling.
     *
     * This method is called by sizeChanged()
     * 
     * @see #sizeChanged()
     */
    protected final void invalidateLayoutLocally() {
        if(layoutInvalid < LAYOUT_INVALID_LOCAL) {
            layoutInvalid = LAYOUT_INVALID_LOCAL;
            GUI gui = getGUI();
            if(gui != null) {
                gui.hasInvalidLayouts = true;
            }
        }
    }

    /**
     * Sets size and position of a child widget so that it consumes the complete
     * inner area.
     *
     * @param child A child widget
     */
    protected void layoutChildFullInnerArea(Widget child) {
        if(child.parent != this) {
            throw new IllegalArgumentException("can only layout direct children");
        }
        child.setPosition(getInnerX(), getInnerY());
        child.setSize(getInnerWidth(), getInnerHeight());
    }

    /**
     * Sets size and position of all child widgets so that they all consumes the
     * complete inner area. If there is more then one child then they will overlap.
     */
    protected void layoutChildrenFullInnerArea() {
        if(children != null) {
            for(int i=0,n=children.size() ; i<n ; i++) {
                layoutChildFullInnerArea(children.get(i));
            }
        }
    }

    /**
     * Returns all children of this widget in their focus travel order.
     * <p>The returned list is only iterated and not stored.</p>
     * <p>The default implementation just returns an unmodifable view of
     * the internal children list.</p>
     * @return a read only collection with all children in focus order.
     */
    protected List<Widget> getKeyboardFocusOrder() {
        if(children == null) {
            return Collections.<Widget>emptyList();
        }
        return Collections.unmodifiableList(children);
    }

    private int collectFocusOrderList(ArrayList<Widget> list) {
        int idx = -1;
        for(Widget child : getKeyboardFocusOrder()) {
            if(child.visible && child.isEnabled()) {
                if(child.canAcceptKeyboardFocus) {
                    if(child == focusChild) {
                        idx = list.size();
                    }
                    list.add(child);
                }
                if(child.depthFocusTraversal) {
                    int subIdx = child.collectFocusOrderList(list);
                    if(subIdx != -1) {
                        idx = subIdx;
                    }
                }
            }
        }
        return idx;
    }

    private boolean moveFocus(boolean relative, int dir) {
        ArrayList<Widget> focusList = new ArrayList<Widget>();
        int curIndex = collectFocusOrderList(focusList);
        if(focusList.isEmpty()) {
            return false;
        }
        if(dir < 0) {
            if(!relative || --curIndex < 0) {
                curIndex = focusList.size() - 1;
            }
        } else if(!relative || ++curIndex >= focusList.size()) {
            curIndex = 0;
        }
        Widget widget = focusList.get(curIndex);
        try {
            widget.focusGainedCause = FocusGainedCause.FOCUS_KEY;
            widget.requestKeyboardFocus(null);
            widget.requestKeyboardFocus();
        } finally {
            widget.focusGainedCause = null;
        }
        return true;
    }

    private boolean focusTransferStart() {
        Widget[] fti = focusTransferInfo.get();
        if(fti == null) {
            Widget root = getRootWidget();
            Widget w = root;
            while(w.focusChild != null) {
                w = w.focusChild;
            }
            if(w == root) {
                w = null;
            }
            focusTransferInfo.set(new Widget[]{ w });
            return true;
        }
        return false;
    }

    private void focusTransferClear(boolean clear) {
        if(clear) {
            focusTransferInfo.set(null);
        }
    }

    /**
     * Returns the visible child widget which is at the specified coordinate.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the child widget at that location or null if there is no visible child.
     * @see #getX()
     * @see #getY()
     */
    protected final Widget getChildAt(int x, int y) {
        if(children != null) {
            for(int i=children.size(); i-->0 ;) {
                Widget child = children.get(i);
                if(child.visible && child.isInside(x, y)) {
                    return child;
                }
            }
        }
        return null;
    }

    /**
     * Updates the tint animation when a fade is active.
     * 
     * Can be overridden to do additional things like hide the widget
     * after the end of the animation.
     */
    protected void updateTintAnimation() {
        tintAnimator.update();
    }

    /**
     * Fire an existing PropertyChangeEvent to any registered listeners.
     *
     * @param evt The PropertyChangeEvent object
     * @see PropertyChangeSupport#firePropertyChange(java.beans.PropertyChangeEvent)
     */
    protected final void firePropertyChange(PropertyChangeEvent evt) {
        if(propertyChangeSupport != null) {
            propertyChangeSupport.firePropertyChange(evt);
        }
    }

    /**
     * Report a bound property update to any registered listeners.
     *
     * @param propertyName The programmatic name of the property that was changed
     * @param oldValue The old value of the property
     * @param newValue The new value of the property
     * @see PropertyChangeSupport#firePropertyChange(java.lang.String, boolean, boolean)
     */
    protected final void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        if(propertyChangeSupport != null) {
            propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Report a bound property update to any registered listeners.
     *
     * @param propertyName The programmatic name of the property that was changed
     * @param oldValue The old value of the property
     * @param newValue The new value of the property
     * @see PropertyChangeSupport#firePropertyChange(java.lang.String, int, int) 
     */
    protected final void firePropertyChange(String propertyName, int oldValue, int newValue) {
        if(propertyChangeSupport != null) {
            propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Report a bound property update to any registered listeners.
     *
     * @param propertyName The programmatic name of the property that was changed
     * @param oldValue The old value of the property
     * @param newValue The new value of the property
     * @see PropertyChangeSupport#firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
     */
    protected final void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if(propertyChangeSupport != null) {
            propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    //
    // start of internal stuff
    //
    
    void setParent(Widget parent) {
        this.parent = parent;
    }
    
    private void unparentChild(Widget child) {
        GUI gui = getGUI();
        if(child.hasOpenPopup) { 
            assert(gui != null);
            gui.closePopupFromWidgets(child);
        }
        recursivelyChildFocusLost(child);
        if(gui != null) {
            child.recursivelyRemoveFromGUI(gui);
        }
        child.recursivelyClearGUI(gui);
        child.parent = null;
        try {
            child.destroy();
        } catch(Exception ex) {
            getLogger().log(Level.SEVERE, "Exception in destroy()", ex);
        }
        adjustChildPosition(child, -posX, -posY);
        child.recursivelyEnabledChanged(null, child.locallyEnabled);
    }

    private void recursivelySetGUI(GUI gui) {
        assert guiInstance == null : "guiInstance must be null";
        guiInstance = gui;
        if(children != null) {
            for(int i=children.size() ; i-->0 ;) {
                children.get(i).recursivelySetGUI(gui);
            }
        }
    }

    private void recursivelyAddToGUI(GUI gui) {
        assert guiInstance == gui : "guiInstance must be equal to gui";
        if(layoutInvalid != 0) {
            gui.hasInvalidLayouts = true;
        }
        if(!sharedAnimState) {
            animState.setGUI(gui);
        }
        try {
            afterAddToGUI(gui);
        } catch(Exception ex) {
            getLogger().log(Level.SEVERE, "Exception in afterAddToGUI()", ex);
        }
        if(children != null) {
            for(int i=children.size() ; i-->0 ;) {
                children.get(i).recursivelyAddToGUI(gui);
            }
        }
    }

    private void recursivelyClearGUI(GUI gui) {
        assert guiInstance == gui : "guiInstance must be null";
        guiInstance = null;
        themeManager = null;
        if(children != null) {
            for(int i=children.size() ; i-->0 ;) {
                children.get(i).recursivelyClearGUI(gui);
            }
        }
    }

    private void recursivelyRemoveFromGUI(GUI gui) {
        assert guiInstance == gui : "guiInstance must be equal to gui";
        if(children != null) {
            for(int i=children.size() ; i-->0 ;) {
                children.get(i).recursivelyRemoveFromGUI(gui);
            }
        }
        focusChild = null;
        if(!sharedAnimState) {
            animState.setGUI(null);
        }
        try {
            beforeRemoveFromGUI(gui);
        } catch(Exception ex) {
            getLogger().log(Level.SEVERE, "Exception in beforeRemoveFromGUI()", ex);
        }
    }

    private void recursivelyChildFocusLost(Widget w) {
        while(w != null) {
            Widget next = w.focusChild;
            if(!w.sharedAnimState) {
                w.animState.setAnimationState(STATE_KEYBOARD_FOCUS, false);
            }
            try {
                w.keyboardFocusLost();
            } catch(Exception ex) {
                getLogger().log(Level.SEVERE, "Exception in keyboardFocusLost()", ex);
            }
            w.focusChild = null;
            w = next;
        }
    }

    private void recursivelyEnabledChanged(GUI gui, boolean enabled) {
        enabled &= locallyEnabled;
        if(this.enabled != enabled) {
            this.enabled = enabled;
            if(!sharedAnimState) {
                getAnimationState().setAnimationState(STATE_DISABLED, !enabled);
            }
            if(!enabled) {
                if(gui != null) {
                    gui.widgetDisabled(this);
                }
                try {
                    widgetDisabled();
                } catch(Exception ex) {
                    getLogger().log(Level.SEVERE, "Exception in widgetDisabled()", ex);
                }
                try {
                    giveupKeyboardFocus();
                } catch(Exception ex) {
                    getLogger().log(Level.SEVERE, "Exception in giveupKeyboardFocus()", ex);
                }
            }
            try {
                firePropertyChange("enabled", !enabled, enabled);
            } catch(Exception ex) {
                getLogger().log(Level.SEVERE, "Exception in firePropertyChange(\"enabled\")", ex);
            }
            if(children != null) {
                for(int i=children.size() ; i-->0 ;) {
                    Widget child = children.get(i);
                    child.recursivelyEnabledChanged(gui, enabled);
                }
            }
        }
    }
    
    private void childHidden(Widget child) {
        if(focusChild == child) {
            recursivelyChildFocusLost(focusChild);
            focusChild = null;
        }
        if(lastChildMouseOver == child) {
            lastChildMouseOver = null;
        }
    }
    
    final void setOpenPopup(GUI gui, boolean hasOpenPopup) {
        if(this.hasOpenPopup != hasOpenPopup) {
            this.hasOpenPopup = hasOpenPopup;
            if(!sharedAnimState) {
                getAnimationState().setAnimationState(STATE_HAS_OPEN_POPUPS, hasOpenPopup);
            }
            if(parent != null) {
                if(hasOpenPopup) {
                    parent.setOpenPopup(gui, true);
                } else {
                    parent.recalcOpenPopups(gui);
                }
            }
        }
    }
    
    final void recalcOpenPopups(GUI gui) {
        // 1) check self
        if(gui.hasOpenPopups(this)) {
            setOpenPopup(gui, true);
            return;
        }
        // 2) check children (don't compute, just check the flag)
        if(children != null) {
            for(int i=children.size() ; i-->0 ;) {
                if(children.get(i).hasOpenPopup) {
                    setOpenPopup(gui, true);
                    return;
                }
            }
        }
        setOpenPopup(gui, false);
    }
    
    final boolean isLayoutInvalid() {
        return layoutInvalid != 0;
    }
    
    final void drawWidget(GUI gui) {
        if(renderOffscreen != null) {
            drawWidgetOffscreen(gui);
            return;
        }
        if(tintAnimator != null && tintAnimator.hasTint()) {
            drawWidgetTint(gui);
            return;
        }
        if(clip) {
            drawWidgetClip(gui);
            return;
        }
        paint(gui);
    }

    private void drawWidgetTint(GUI gui) {
        if(tintAnimator.isFadeActive()) {
            updateTintAnimation();
        }
        final Renderer renderer = gui.getRenderer();
        tintAnimator.paintWithTint(renderer);
        try {
            if(clip) {
                drawWidgetClip(gui);
            } else {
                paint(gui);
            }
        } finally {
            renderer.popGlobalTintColor();
        }
    }

    private void drawWidgetClip(GUI gui) {
        Renderer renderer = gui.getRenderer();
        renderer.clipEnter(posX, posY, width, height);
        try {
            paint(gui);
        } finally {
            renderer.clipLeave();
        }
    }
    
    private void drawWidgetOffscreen(GUI gui) {
        final RenderOffscreen ro = this.renderOffscreen;
        final Renderer renderer = gui.getRenderer();
        final OffscreenRenderer offscreenRenderer = renderer.getOffscreenRenderer();
        if(offscreenRenderer != null) {
            int extraTop = offscreenExtraTop;
            int extraLeft = offscreenExtraLeft;
            int extraRight = offscreenExtraRight;
            int extraBottom = offscreenExtraBottom;
            int[] effectExtra = ro.getEffectExtraArea(this);
            if(effectExtra != null) {
                extraTop += effectExtra[0];
                extraLeft += effectExtra[1];
                extraRight += effectExtra[2];
                extraBottom += effectExtra[3];
            }
            if(offscreenSurface != null && !ro.needPainting(gui, parent, offscreenSurface)) {
                ro.paintOffscreenSurface(gui, this, offscreenSurface);
                return;
            }
            offscreenSurface = offscreenRenderer.startOffscreenRendering(
                    this, offscreenSurface, posX-extraLeft, posY-extraTop,
                    width+extraLeft+extraRight, height+extraTop+extraBottom);
            if(offscreenSurface != null) {
                try {
                    if(tintAnimator != null && tintAnimator.hasTint()) {
                        drawWidgetTint(gui);
                    } else {
                        paint(gui);
                    }
                } finally {
                    offscreenRenderer.endOffscreenRendering();
                }
                ro.paintOffscreenSurface(gui, this, offscreenSurface);
                return;
            }
        }
        renderOffscreen = null;
        ro.offscreenRenderingFailed(this);
        drawWidget(gui);
    }
    
    Widget getWidgetUnderMouse() {
        if(!visible) {
            return null;
        }
        Widget w = this;
        while(w.lastChildMouseOver != null && w.visible) {
            w = w.lastChildMouseOver;
        }
        return w;
    }
    
    private static void adjustChildPosition(Widget child, int deltaX, int deltaY) {
        child.setPositionImpl(child.posX + deltaX, child.posY + deltaY);
    }

    final boolean setPositionImpl(int x, int y) {
        int deltaX = x - posX;
        int deltaY = y - posY;
        if(deltaX != 0 || deltaY != 0) {
            this.posX = x;
            this.posY = y;

            if(children != null) {
                for(int i=0,n=children.size() ; i<n ; i++) {
                    adjustChildPosition(children.get(i), deltaX, deltaY);
                }
            }

            positionChanged();

            if(propertyChangeSupport != null) {
                firePropertyChange("x", x - deltaX, x);
                firePropertyChange("y", y - deltaY, y);
            }
            return true;
        }
        return false;
    }
    
    void applyTheme(ThemeManager themeManager) {
        this.themeManager = themeManager;
        
        final String themePath = getThemePath();
        if(themePath.length() == 0) {
            if(children != null) {
                for(int i=0,n=children.size() ; i<n ; i++) {
                    children.get(i).applyTheme(themeManager);
                }
            }
            return;
        }

        final DebugHook hook = DebugHook.getDebugHook();
        hook.beforeApplyTheme(this);
        
        ThemeInfo themeInfo = null;
        try {
            themeInfo = themeManager.findThemeInfo(themePath);
            if(themeInfo != null && theme.length() > 0) {
                try {
                    applyTheme(themeInfo);
                } catch(Exception ex) {
                    getLogger().log(Level.SEVERE, "Exception in applyTheme()", ex);
                }
            }
        } finally {
            hook.afterApplyTheme(this);
        }

        applyThemeToChildren(themeManager, themeInfo, hook);
    }

    /**
     * Checks if the given theme name is absolute or relative to it's parent.
     * An absolute theme name starts with a '/'.
     * 
     * @param theme the theme name or path.
     * @return true if the theme is absolute.
     */
    public static boolean isAbsoluteTheme(String theme) {
        return theme.length() > 1 && theme.charAt(0) == '/';
    }

    private void applyThemeImpl(ThemeManager themeManager, ThemeInfo themeInfo, DebugHook hook) {
        this.themeManager = themeManager;
        if(theme.length() > 0) {
            hook.beforeApplyTheme(this);
            try {
                if(isAbsoluteTheme(theme)) {
                    themeInfo = themeManager.findThemeInfo(theme.substring(1));
                } else {
                    themeInfo = themeInfo.getChildTheme(theme);
                }
                if(themeInfo != null) {
                    try {
                        applyTheme(themeInfo);
                    } catch(Exception ex) {
                        getLogger().log(Level.SEVERE, "Exception in applyTheme()", ex);
                    }
                }
            } finally {
                hook.afterApplyTheme(this);
            }
        }
        applyThemeToChildren(themeManager, themeInfo, hook);
    }

    private void applyThemeToChildren(ThemeManager themeManager, ThemeInfo themeInfo, DebugHook hook) {
        if(children != null && themeInfo != null) {
            for(int i=0,n=children.size() ; i<n ; i++) {
                Widget child = children.get(i);
                child.applyThemeImpl(themeManager, themeInfo, hook);
            }
        }
    }

    private StringBuilder getThemePath(int length) {
        StringBuilder sb;
        length += theme.length();
        boolean abs = isAbsoluteTheme(theme);
        if(parent != null && !abs) {
            sb = parent.getThemePath(length+1);
            if(theme.length() > 0 && sb.length() > 0) {
                sb.append('.');
            }
        } else {
            sb = new StringBuilder(length);
        }
        if(abs) {
            return sb.append(theme.substring(1));
        }
        return sb.append(theme);
    }

    Event translateMouseEvent(Event evt) {
        if(renderOffscreen instanceof OffscreenMouseAdjustments) {
            int[] newXY = ((OffscreenMouseAdjustments)renderOffscreen).adjustMouseCoordinates(this, evt);
            evt = evt.createSubEvent(newXY[0], newXY[1]);
        }
        return evt;
    }
    
    Widget routeMouseEvent(Event evt) {
        assert !evt.isMouseDragEvent();
        evt = translateMouseEvent(evt);
        if(children != null) {
            for(int i=children.size(); i-->0 ;) {
                Widget child = children.get(i);
                if(child.visible && child.isMouseInside(evt)) {
                    // we send the real event only only if we can transfer the mouse "focus" to this child
                    if(setMouseOverChild(child, evt)) {
                        if(evt.getType() == Event.Type.MOUSE_ENTERED ||
                                evt.getType() == Event.Type.MOUSE_EXITED) {
                            return child;
                        }
                        Widget result = child.routeMouseEvent(evt);
                        if(result != null) {
                            // need to check if the focus was transfered to this child or its descendants
                            // if not we need to transfer focus on mouse click here
                            // this can happen if we click on a widget which doesn't want the keyboard focus itself
                            if(evt.getType() == Event.Type.MOUSE_BTNDOWN && focusChild != child) {
                                try {
                                    child.focusGainedCause = FocusGainedCause.MOUSE_BTNDOWN;
                                    if(child.isEnabled() && child.canAcceptKeyboardFocus()) {
                                        requestKeyboardFocus(child);
                                    }
                                } finally {
                                    child.focusGainedCause = null;
                                }
                            }
                            return result;
                        }
                        // widget no longer wants mouse events
                    }
                    // found a widget - but it doesn't want mouse events
                    // so assumes it's "invisible" for the mouse
                }
            }
        }

        // the following code is only executed for the widget which received
        // the click event. That's why we can call {@code requestKeyboardFocus(null)}
        if(evt.getType() == Event.Type.MOUSE_BTNDOWN && isEnabled() && canAcceptKeyboardFocus()) {
            try {
                focusGainedCause = FocusGainedCause.MOUSE_BTNDOWN;
                if(focusChild == null) {
                    requestKeyboardFocus();
                } else {
                    requestKeyboardFocus(null);
                }
            } finally {
                focusGainedCause = null;
            }
        }
        if(evt.getType() != Event.Type.MOUSE_WHEEL) {
            // no child has mouse over
            setMouseOverChild(null, evt);
        }
        if(!isEnabled() && isMouseAction(evt)) {
            return this;
        }
        if(handleEvent(evt)) {
            return this;
        }
        return null;
    }

    static boolean isMouseAction(Event evt) {
        Event.Type type = evt.getType();
        return type == Event.Type.MOUSE_BTNDOWN ||
                type == Event.Type.MOUSE_BTNUP ||
                type == Event.Type.MOUSE_CLICKED ||
                type == Event.Type.MOUSE_DRAGGED;
    }

    void routePopupEvent(Event evt) {
        handleEvent(evt);
        if(children != null) {
            for(int i=0,n=children.size() ; i<n ; i++) {
                children.get(i).routePopupEvent(evt);
            }
        }
    }

    static boolean getSafeBooleanProperty(String name) {
        try {
            return Boolean.getBoolean(name);
        } catch (AccessControlException ex) {
            return false;
        }
    }
    
    private static final boolean WARN_ON_UNHANDLED_ACTION = getSafeBooleanProperty("warnOnUnhandledAction");

    private boolean handleKeyEvent(Event evt) {
        if(children != null) {
            if(focusKeyEnabled && guiInstance != null) {
                guiInstance.setFocusKeyWidget(this);
            }
            if(focusChild != null && focusChild.isVisible()) {
                if(focusChild.handleEvent(evt)) {
                    return true;
                }
            }
        }
        if(inputMap != null) {
            String action = inputMap.mapEvent(evt);
            if(action != null) {
                if(handleKeyStrokeAction(action, evt)) {
                    return true;
                }
                if(WARN_ON_UNHANDLED_ACTION) {
                    Logger.getLogger(getClass().getName()).log(Level.WARNING,
                            "Unhandled action ''{0}'' for class ''{1}''",
                            new Object[]{ action, getClass().getName() });
                }
            }
        }
        return false;
    }

    void handleFocusKeyEvent(Event evt) {
        if(evt.isKeyPressedEvent()) {
            if((evt.getModifiers() & Event.MODIFIER_SHIFT) != 0) {
                focusPrevChild();
            } else {
                focusNextChild();
            }
        }
    }

    boolean setMouseOverChild(Widget child, Event evt) {
        if (lastChildMouseOver != child) {
            if(child != null) {
                Widget result = child.routeMouseEvent(evt.createSubEvent(Event.Type.MOUSE_ENTERED));
                if(result == null) {
                    // this child widget doesn't want mouse events
                    return false;
                }
            }
            if (lastChildMouseOver != null) {
                lastChildMouseOver.routeMouseEvent(evt.createSubEvent(Event.Type.MOUSE_EXITED));
            }
            lastChildMouseOver = child;
        }
        return true;
    }
    
    void collectLayoutLoop(ArrayList<Widget> result) {
        if(layoutInvalid != 0) {
            result.add(this);
        }
        if(children != null) {
            for(int i=0,n=children.size() ; i<n ; i++) {
                children.get(i).collectLayoutLoop(result);
            }
        }
    }

    private PropertyChangeSupport createPropertyChangeSupport() {
        if(propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        return propertyChangeSupport;
    }

    private Logger getLogger() {
        return Logger.getLogger(Widget.class.getName());
    }
    
    /**
     * When this interface is installed in a Widget then the widget tries to
     * render into an offscreen surface.
     */
    public static interface RenderOffscreen {
        /**
         * This method is called after the widget has been sucessfully rendered
         * into an offscreen surface.
         * 
         * @param gui the GUI instance
         * @param widget the widget
         * @param surface the resulting offscreen surface
         */
        public void paintOffscreenSurface(GUI gui, Widget widget, OffscreenSurface surface);
        
        /**
         * Called when {@link OffscreenRenderer#startOffscreenRendering(de.matthiasmann.twl.renderer.OffscreenSurface, int, int, int, int) }
         * failed.
         * At the moment this method is called the RenderOffscreen instance has
         * already been removed from the widget.
         * @param widget the widget
         */
        public void offscreenRenderingFailed(Widget widget);
        
        /**
         * Returns the extra area around the widget needed for the effect.
         * <p>All returned values must be &gt;= 0.</p>
         * 
         * <p>The returned object can be reused on the next call and should not
         * be stored by the caller.</p>
         * 
         * @param widget the widget
         * @return the extra area in {@code top, left, right, bottom} order or null
         */
        public int[] getEffectExtraArea(Widget widget);
        
        /**
         * Called before offscreen rendering is started.
         * 
         * <p>NOTE: when this function returns false none of the paint methods
         * of that widget are called which might effect some widgets.</p>
         * 
         * <p>If you are unsure it is always safer to return true.</p>
         * 
         * @param gui the GUI instance
         * @param widget the widget
         * @param surface the previous offscreen surface - never null
         * @return true if the surface needs to be updated, false if no new rendering should be done
         */
        public boolean needPainting(GUI gui, Widget widget, OffscreenSurface surface);
    }
    
    public interface OffscreenMouseAdjustments extends RenderOffscreen {
        
        /**
         * Called when mouse events are routed for the widget.
         * 
         * <p>All mouse coordinates in TWL are absolute.</p>
         * 
         * <p>The returned object can be reused on the next call and should not
         * be stored by the caller.</p>
         * 
         * @param widget the widget
         * @param evt the mouse event
         * @return the new mouse coordinates in {@code x, y} order
         */
        public int[] adjustMouseCoordinates(Widget widget, Event evt);
    }
}
