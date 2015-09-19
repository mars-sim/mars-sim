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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A layout manager similar to Swing's GroupLayout
 *
 * This layout manager uses two independant layout groups:
 *   one for the horizontal axis
 *   one for the vertical axis.
 * Every widget must be added to both the horizontal and the vertical group.
 *
 * When a widget is added to a group it will also be added as a child widget
 * if it was not already added. You can add widgets to DialogLayout before
 * adding them to a group to set the focus order.
 *
 * There are two kinds of groups:
 *   a sequential group which which behaves similar to BoxLayout
 *   a parallel group which alignes the start and size of each child
 *
 * Groups can be cascaded as a tree without restrictions.
 *
 * It is also possible to add widgets to DialogLayout without adding them
 * to the layout groups. These widgets are then not touched by DialogLayout's
 * layout system.
 *
 * When a widget is only added to either the horizontal or vertical groups
 * and not both, then an IllegalStateException exception is created on layout.
 *
 * To help debugging the group construction you can set the system property
 * "debugLayoutGroups" to "true" which will collect additional stack traces
 * to help locate the source of the error.
 *
 * @author Matthias Mann
 * @see #createParallelGroup() 
 * @see #createSequentialGroup()
 */
public class DialogLayout extends Widget {

    /**
     * Symbolic constant to refer to "small gap".
     * @see #getSmallGap()
     * @see Group#addGap(int)
     * @see Group#addGap(int, int, int)
     */
    public static final int SMALL_GAP   = -1;

    /**
     * Symbolic constant to refer to "medium gap".
     * @see #getMediumGap()
     * @see Group#addGap(int)
     * @see Group#addGap(int, int, int)
     */
    public static final int MEDIUM_GAP  = -2;

    /**
     * Symbolic constant to refer to "large gap".
     * @see #getLargeGap()
     * @see Group#addGap(int)
     * @see Group#addGap(int, int, int)
     */
    public static final int LARGE_GAP   = -3;

    /**
     * Symbolic constant to refer to "default gap".
     * The default gap is added (when enabled) between widgets.
     *
     * @see #getDefaultGap()
     * @see #setAddDefaultGaps(boolean)
     * @see #isAddDefaultGaps()
     * @see Group#addGap(int)
     * @see Group#addGap(int, int, int)
     */
    public static final int DEFAULT_GAP = -4;

    private static final boolean DEBUG_LAYOUT_GROUPS = Widget.getSafeBooleanProperty("debugLayoutGroups");
    
    protected Dimension smallGap;
    protected Dimension mediumGap;
    protected Dimension largeGap;
    protected Dimension defaultGap;
    protected ParameterMap namedGaps;

    protected boolean addDefaultGaps = true;
    protected boolean includeInvisibleWidgets = true;
    protected boolean redoDefaultGaps;
    protected boolean isPrepared;
    protected boolean blockInvalidateLayoutTree;
    protected boolean warnOnIncomplete;

    private Group horz;
    private Group vert;

    /**
     * Debugging aid. Captures the stack trace where one of the group was last assigned.
     */
    Throwable debugStackTrace;

    final HashMap<Widget, WidgetSpring> widgetSprings;

    /**
     * Creates a new DialogLayout widget.
     *
     * Initially both the horizontal and the vertical group are null.
     * 
     * @see #setHorizontalGroup(de.matthiasmann.twl.DialogLayout.Group)
     * @see #setVerticalGroup(de.matthiasmann.twl.DialogLayout.Group)
     */
    public DialogLayout() {
        widgetSprings = new HashMap<Widget, WidgetSpring>();
        collectDebugStack();
    }

    public Group getHorizontalGroup() {
        return horz;
    }

    /**
     * The horizontal group controls the position and size of all child
     * widgets along the X axis.
     *
     * Every widget must be part of both horizontal and vertical group.
     * Otherwise a IllegalStateException is thrown at layout time.
     *
     * If you want to change both horizontal and vertical group then
     * it's recommended to set the other group first to null:
     * <pre>
     * setVerticalGroup(null);
     * setHorizontalGroup(newHorzGroup);
     * setVerticalGroup(newVertGroup);
     * </pre>
     *
     * @param g the group used for the X axis
     * @see #setVerticalGroup(de.matthiasmann.twl.DialogLayout.Group)
     */
    public void setHorizontalGroup(Group g) {
        if(g != null) {
            g.checkGroup(this);
        }
        this.horz = g;
        collectDebugStack();
        layoutGroupsChanged();
    }

    public Group getVerticalGroup() {
        return vert;
    }

    /**
     * The vertical group controls the position and size of all child
     * widgets along the Y axis.
     *
     * Every widget must be part of both horizontal and vertical group.
     * Otherwise a IllegalStateException is thrown at layout time.
     *
     * @param g the group used for the Y axis
     * @see #setHorizontalGroup(de.matthiasmann.twl.DialogLayout.Group) 
     */
    public void setVerticalGroup(Group g) {
        if(g != null) {
            g.checkGroup(this);
        }
        this.vert = g;
        collectDebugStack();
        layoutGroupsChanged();
    }

    public Dimension getSmallGap() {
        return smallGap;
    }

    public void setSmallGap(Dimension smallGap) {
        this.smallGap = smallGap;
        maybeInvalidateLayoutTree();
    }

    public Dimension getMediumGap() {
        return mediumGap;
    }

    public void setMediumGap(Dimension mediumGap) {
        this.mediumGap = mediumGap;
        maybeInvalidateLayoutTree();
    }

    public Dimension getLargeGap() {
        return largeGap;
    }

    public void setLargeGap(Dimension largeGap) {
        this.largeGap = largeGap;
        maybeInvalidateLayoutTree();
    }

    public Dimension getDefaultGap() {
        return defaultGap;
    }

    public void setDefaultGap(Dimension defaultGap) {
        this.defaultGap = defaultGap;
        maybeInvalidateLayoutTree();
    }

    public boolean isAddDefaultGaps() {
        return addDefaultGaps;
    }

    /**
     * Determine whether default gaps should be added from the theme or not.
     * 
     * @param addDefaultGaps if true then default gaps are added.
     */
    public void setAddDefaultGaps(boolean addDefaultGaps) {
        this.addDefaultGaps = addDefaultGaps;
    }

    /**
     * removes all default gaps from all groups.
     */
    public void removeDefaultGaps() {
        if(horz != null && vert != null) {
            horz.removeDefaultGaps();
            vert.removeDefaultGaps();
            maybeInvalidateLayoutTree();
        }
    }

    /**
     * Adds theme dependant default gaps to all groups.
     */
    public void addDefaultGaps() {
        if(horz != null && vert != null) {
            horz.addDefaultGap();
            vert.addDefaultGap();
            maybeInvalidateLayoutTree();
        }
    }

    public boolean isIncludeInvisibleWidgets() {
        return includeInvisibleWidgets;
    }

    /**
     * Controls whether invisible widgets should be included in the layout or
     * not. If they are not included then the layout is recomputed when the
     * visibility of a child widget changes.
     *
     * The default is true
     *
     * @param includeInvisibleWidgets If true then invisible widgets are included,
     *      if false they don't contribute to the layout.
     */
    public void setIncludeInvisibleWidgets(boolean includeInvisibleWidgets) {
        if(this.includeInvisibleWidgets != includeInvisibleWidgets) {
            this.includeInvisibleWidgets = includeInvisibleWidgets;
            layoutGroupsChanged();
        }
    }

    private void collectDebugStack() {
        warnOnIncomplete = true;
        if(DEBUG_LAYOUT_GROUPS) {
            debugStackTrace = new Throwable("DialogLayout created/used here").fillInStackTrace();
        }
    }

    private void warnOnIncomplete() {
        warnOnIncomplete = false;
        getLogger().log(Level.WARNING, "Dialog layout has incomplete state", debugStackTrace);
    }

    static Logger getLogger() {
        return Logger.getLogger(DialogLayout.class.getName());
    }

    protected void applyThemeDialogLayout(ThemeInfo themeInfo) {
        try {
            blockInvalidateLayoutTree = true;
            setSmallGap(themeInfo.getParameterValue("smallGap", true, Dimension.class, Dimension.ZERO));
            setMediumGap(themeInfo.getParameterValue("mediumGap", true, Dimension.class, Dimension.ZERO));
            setLargeGap(themeInfo.getParameterValue("largeGap", true, Dimension.class, Dimension.ZERO));
            setDefaultGap(themeInfo.getParameterValue("defaultGap", true, Dimension.class, Dimension.ZERO));
            namedGaps = themeInfo.getParameterMap("namedGaps");
        } finally {
            blockInvalidateLayoutTree = false;
        }
        invalidateLayout();
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        applyThemeDialogLayout(themeInfo);
    }

    @Override
    public int getMinWidth() {
        if(horz != null) {
            prepare();
            return horz.getMinSize(AXIS_X) + getBorderHorizontal();
        }
        return super.getMinWidth();
    }

    @Override
    public int getMinHeight() {
        if(vert != null) {
            prepare();
            return vert.getMinSize(AXIS_Y) + getBorderVertical();
        }
        return super.getMinHeight();
    }

    @Override
    public int getPreferredInnerWidth() {
        if(horz != null) {
            prepare();
            return horz.getPrefSize(AXIS_X);
        }
        return super.getPreferredInnerWidth();
    }

    @Override
    public int getPreferredInnerHeight() {
        if(vert != null) {
            prepare();
            return vert.getPrefSize(AXIS_Y);
        }
        return super.getPreferredInnerHeight();
    }

    @Override
    public void adjustSize() {
        if(horz != null && vert != null) {
            prepare();
            int minWidth = horz.getMinSize(AXIS_X);
            int minHeight = vert.getMinSize(AXIS_Y);
            int prefWidth = horz.getPrefSize(AXIS_X);
            int prefHeight = vert.getPrefSize(AXIS_Y);
            int maxWidth = getMaxWidth();
            int maxHeight = getMaxHeight();
            setInnerSize(
                    computeSize(minWidth, prefWidth, maxWidth),
                    computeSize(minHeight, prefHeight, maxHeight));
            doLayout();
        }
    }

    @Override
    protected void layout() {
        if(horz != null && vert != null) {
            prepare();
            doLayout();
        } else if(warnOnIncomplete) {
            warnOnIncomplete();
        }
    }
    
    protected void prepare() {
        if(redoDefaultGaps) {
            if(addDefaultGaps) {
                try {
                    blockInvalidateLayoutTree = true;
                    removeDefaultGaps();
                    addDefaultGaps();
                } finally {
                    blockInvalidateLayoutTree = false;
                }
            }
            redoDefaultGaps = false;
            isPrepared = false;
        }
        if(!isPrepared) {
            for(WidgetSpring s : widgetSprings.values()) {
                if(includeInvisibleWidgets || s.w.isVisible()) {
                    s.prepare();
                }
            }
            isPrepared = true;
        }
    }

    protected void doLayout() {
        horz.setSize(AXIS_X, getInnerX(), getInnerWidth());
        vert.setSize(AXIS_Y, getInnerY(), getInnerHeight());
        try{
            for(WidgetSpring s : widgetSprings.values()) {
                if(includeInvisibleWidgets || s.w.isVisible()) {
                    s.apply();
                }
            }
        }catch(IllegalStateException ex) {
            if(debugStackTrace != null && ex.getCause() == null) {
                ex.initCause(debugStackTrace);
            }
            throw ex;
        }
    }

    @Override
    public void invalidateLayout() {
        isPrepared = false;
        super.invalidateLayout();
    }

    @Override
    protected void paintWidget(GUI gui) {
        isPrepared = false;
        // super.paintWidget() is empty
    }

    @Override
    protected void sizeChanged() {
        isPrepared = false;
        super.sizeChanged();
    }

    @Override
    protected void afterAddToGUI(GUI gui) {
        isPrepared = false;
        super.afterAddToGUI(gui);
    }

    /**
     * Creates a new parallel group.
     * All children in a parallel group share the same position and size of it's axis.
     *
     * @return the new parallel Group.
     */
    public Group createParallelGroup() {
        return new ParallelGroup();
    }

    /**
     * Creates a parallel group and adds the specified widgets.
     *
     * @see #createParallelGroup()
     * @param widgets the widgets to add
     * @return a new parallel Group.
     */
    public Group createParallelGroup(Widget ... widgets) {
        return createParallelGroup().addWidgets(widgets);
    }

    /**
     * Creates a parallel group and adds the specified groups.
     *
     * @see #createParallelGroup()
     * @param groups the groups to add
     * @return a new parallel Group.
     */
    public Group createParallelGroup(Group ... groups) {
        return createParallelGroup().addGroups(groups);
    }

    /**
     * Creates a new sequential group.
     * All children in a sequential group are ordered with increasing coordinates
     * along it's axis in the order they are added to the group. The available
     * size is distributed among the children depending on their min/preferred/max
     * sizes.
     * 
     * @return a new sequential Group.
     */
    public Group createSequentialGroup() {
        return new SequentialGroup();
    }

    /**
     * Creates a sequential group and adds the specified widgets.
     *
     * @see #createSequentialGroup()
     * @param widgets the widgets to add
     * @return a new sequential Group.
     */
    public Group createSequentialGroup(Widget ... widgets) {
        return createSequentialGroup().addWidgets(widgets);
    }

    /**
     * Creates a sequential group and adds the specified groups.
     *
     * @see #createSequentialGroup()
     * @param groups the groups to add
     * @return a new sequential Group.
     */
    public Group createSequentialGroup(Group ... groups) {
        return createSequentialGroup().addGroups(groups);
    }

    @Override
    public void insertChild(Widget child, int index) throws IndexOutOfBoundsException {
        super.insertChild(child, index);
        widgetSprings.put(child, new WidgetSpring(child));
    }

    @Override
    public void removeAllChildren() {
        super.removeAllChildren();
        widgetSprings.clear();
        recheckWidgets();
        layoutGroupsChanged();
    }

    @Override
    public Widget removeChild(int index) throws IndexOutOfBoundsException {
        final Widget widget = super.removeChild(index);
        widgetSprings.remove(widget);
        recheckWidgets();
        layoutGroupsChanged();
        return widget;
    }

    /**
     * Sets the alignment of the specified widget.
     * The widget must have already been added to this container for this method to work.
     *
     * <p>The default alignment of a widget is {@link Alignment#FILL}</p>
     * 
     * @param widget the widget for which the alignment should be set
     * @param alignment the new alignment
     * @return true if the widget's alignment was changed, false otherwise
     */
    public boolean setWidgetAlignment(Widget widget, Alignment alignment) {
        if(widget == null) {
            throw new NullPointerException("widget");
        }
        if(alignment == null) {
            throw new NullPointerException("alignment");
        }
        WidgetSpring ws = widgetSprings.get(widget);
        if(ws != null) {
            assert widget.getParent() == this;
            ws.alignment = alignment;
            return true;
        }
        return false;
    }

    protected void recheckWidgets() {
        if(horz != null) {
            horz.recheckWidgets();
        }
        if(vert != null) {
            vert.recheckWidgets();
        }
    }
    
    protected void layoutGroupsChanged() {
        redoDefaultGaps = true;
        maybeInvalidateLayoutTree();
    }
    
    protected void maybeInvalidateLayoutTree() {
        if(horz != null && vert != null && !blockInvalidateLayoutTree) {
            invalidateLayout();
        }
    }

    @Override
    protected void childVisibilityChanged(Widget child) {
        if(!includeInvisibleWidgets) {
            layoutGroupsChanged(); // this will also clear isPrepared
        }
    }

    void removeChild(WidgetSpring widgetSpring) {
        Widget widget = widgetSpring.w;
        int idx = getChildIndex(widget);
        assert idx >= 0;
        super.removeChild(idx);
        widgetSprings.remove(widget);
    }

    public static class Gap {
        public final int min;
        public final int preferred;
        public final int max;

        public Gap() {
            this(0,0,32767);
        }
        public Gap(int size) {
            this(size, size, size);
        }
        public Gap(int min, int preferred) {
            this(min, preferred, 32767);
        }
        public Gap(int min, int preferred, int max) {
            if(min < 0) {
                throw new IllegalArgumentException("min");
            }
            if(preferred < min) {
                throw new IllegalArgumentException("preferred");
            }
            if(max < 0 || (max > 0 && max < preferred)) {
                throw new IllegalArgumentException("max");
            }
            this.min = min;
            this.preferred = preferred;
            this.max = max;
        }
    }
    
    static final int AXIS_X = 0;
    static final int AXIS_Y = 1;

    static abstract class Spring {
        abstract int getMinSize(int axis);
        abstract int getPrefSize(int axis);
        abstract int getMaxSize(int axis);
        abstract void setSize(int axis, int pos, int size);

        Spring() {
        }
        
        void collectAllSprings(HashSet<Spring> result) {
            result.add(this);
        }

        boolean isVisible() {
            return true;
        }
    }

    private static class WidgetSpring extends Spring {
        final Widget w;
        Alignment alignment;
        int x;
        int y;
        int width;
        int height;
        int minWidth;
        int minHeight;
        int maxWidth;
        int maxHeight;
        int prefWidth;
        int prefHeight;
        int flags;

        WidgetSpring(Widget w) {
            this.w = w;
            this.alignment = Alignment.FILL;
        }

        void prepare() {
            this.x = w.getX();
            this.y = w.getY();
            this.width = w.getWidth();
            this.height = w.getHeight();
            this.minWidth = w.getMinWidth();
            this.minHeight = w.getMinHeight();
            this.maxWidth = w.getMaxWidth();
            this.maxHeight = w.getMaxHeight();
            this.prefWidth = computeSize(minWidth, w.getPreferredWidth(), maxWidth);
            this.prefHeight = computeSize(minHeight, w.getPreferredHeight(), maxHeight);
            this.flags = 0;
        }

        @Override
        int getMinSize(int axis) {
            switch(axis) {
            case AXIS_X: return minWidth;
            case AXIS_Y: return minHeight;
            default: throw new IllegalArgumentException("axis");
            }
        }

        @Override
        int getPrefSize(int axis) {
            switch(axis) {
            case AXIS_X: return prefWidth;
            case AXIS_Y: return prefHeight;
            default: throw new IllegalArgumentException("axis");
            }
        }

        @Override
        int getMaxSize(int axis) {
            switch(axis) {
            case AXIS_X: return maxWidth;
            case AXIS_Y: return maxHeight;
            default: throw new IllegalArgumentException("axis");
            }
        }

        @Override
        void setSize(int axis, int pos, int size) {
            this.flags |= 1 << axis;
            switch(axis) {
            case AXIS_X:
                this.x = pos;
                this.width = size;
                break;
            case AXIS_Y:
                this.y = pos;
                this.height = size;
                break;
            default:
                throw new IllegalArgumentException("axis");
            }
        }

        void apply() {
            if(flags != 3) {
                invalidState();
            }
            if(alignment != Alignment.FILL) {
                int newWidth = Math.min(width, prefWidth);
                int newHeight = Math.min(height, prefHeight);
                w.setPosition(
                        x + alignment.computePositionX(width, newWidth),
                        y + alignment.computePositionY(height, newHeight));
                w.setSize(newWidth, newHeight);
            } else {
                w.setPosition(x, y);
                w.setSize(width, height);
            }
        }

        @Override
        boolean isVisible() {
            return w.isVisible();
        }

        @SuppressWarnings("PointlessBitwiseExpression")
        void invalidState() {
            StringBuilder sb = new StringBuilder();
            sb.append("Widget ").append(w)
                    .append(" with theme ").append(w.getTheme())
                    .append(" is not part of the following groups:");
            if((flags & (1 << AXIS_X)) == 0) {
                sb.append(" horizontal");
            }
            if((flags & (1 << AXIS_Y)) == 0) {
                sb.append(" vertical");
            }
            throw new IllegalStateException(sb.toString());
        }
    }

    private class GapSpring extends Spring {
        final int min;
        final int pref;
        final int max;
        final boolean isDefault;

        GapSpring(int min, int pref, int max, boolean isDefault) {
            convertConstant(AXIS_X, min);
            convertConstant(AXIS_X, pref);
            convertConstant(AXIS_X, max);
            this.min = min;
            this.pref = pref;
            this.max = max;
            this.isDefault = isDefault;
        }

        @Override
        int getMinSize(int axis) {
            return convertConstant(axis, min);
        }

        @Override
        int getPrefSize(int axis) {
            return convertConstant(axis, pref);
        }

        @Override
        int getMaxSize(int axis) {
            return convertConstant(axis, max);
        }

        @Override
        void setSize(int axis, int pos, int size) {
        }

        private int convertConstant(int axis, int value) {
            if(value >= 0) {
                return value;
            }
            Dimension dim;
            switch(value) {
            case SMALL_GAP:
                dim = smallGap;
                break;
            case MEDIUM_GAP:
                dim = mediumGap;
                break;
            case LARGE_GAP:
                dim = largeGap;
                break;
            case DEFAULT_GAP:
                dim = defaultGap;
                break;
            default:
                throw new IllegalArgumentException("Invalid gap size: " + value);
            }
            if(dim == null) {
                return 0;
            } else if(axis == AXIS_X) {
                return dim.getX();
            } else {
                return dim.getY();
            }
        }
    }

    static final Gap NO_GAP = new Gap(0,0,32767);

    private class NamedGapSpring extends Spring {
        final String name;

        public NamedGapSpring(String name) {
            this.name = name;
        }

        @Override
        int getMaxSize(int axis) {
            return getGap().max;
        }

        @Override
        int getMinSize(int axis) {
            return getGap().min;
        }

        @Override
        int getPrefSize(int axis) {
            return getGap().preferred;
        }

        @Override
        void setSize(int axis, int pos, int size) {
        }

        private Gap getGap() {
            if(namedGaps != null) {
                return namedGaps.getParameterValue(name, true, Gap.class, NO_GAP);
            }
            return NO_GAP;
        }
    }

    public abstract class Group extends Spring {
        final ArrayList<Spring> springs = new ArrayList<Spring>();
        boolean alreadyAdded;

        void checkGroup(DialogLayout owner) {
            if(DialogLayout.this != owner) {
                throw new IllegalArgumentException("Can't add group from different layout");
            }
            if(alreadyAdded) {
                throw new IllegalArgumentException("Group already added to another group");
            }
        }

        /**
         * Adds another group. A group can only be added once.
         *
         * WARNING: No check is made to prevent cycles.
         * 
         * @param g the child Group
         * @return this Group
         */
        public Group addGroup(Group g) {
            g.checkGroup(DialogLayout.this);
            g.alreadyAdded = true;
            addSpring(g);
            return this;
        }

        /**
         * Adds several groups. A group can only be added once.
         *
         * WARNING: No check is made to prevent cycles.
         *
         * @param groups the groups to add
         * @return this Group
         */
        public Group addGroups(Group ... groups) {
            for(Group g : groups) {
                addGroup(g);
            }
            return this;
        }

        /**
         * Adds a widget to this group.
         *
         * <p>If the widget is already a child widget of the DialogLayout then it
         * keeps it current settings, otherwise it is added the alignment is set
         * to {@link Alignment#FILL}.</p>
         *
         * @param w the child widget.
         * @return this Group
         * @see Widget#add(de.matthiasmann.twl.Widget)
         */
        public Group addWidget(Widget w) {
            if(w.getParent() != DialogLayout.this) {
                DialogLayout.this.add(w);
            }
            WidgetSpring s = widgetSprings.get(w);
            if(s == null) {
                throw new IllegalStateException("WidgetSpring for Widget not found: " + w);
            }
            addSpring(s);
            return this;
        }

        /**
         * Adds a widget to this group.
         *
         * <p>If the widget is already a child widget of the DialogLayout then it
         * it's alignment is set to the specified value overwriting any current
         * alignment setting, otherwise it is added to the DialogLayout.</p>
         *
         * @param w the child widget.
         * @param alignment the alignment of the child widget.
         * @return this Group
         * @see Widget#add(de.matthiasmann.twl.Widget) 
         * @see #setWidgetAlignment(de.matthiasmann.twl.Widget, de.matthiasmann.twl.Alignment)
         */
        public Group addWidget(Widget w, Alignment alignment) {
            addWidget(w);
            setWidgetAlignment(w, alignment);
            return this;
        }

        /**
         * Adds several widgets to this group. The widget is automatically added as child widget.
         * 
         * @param widgets The widgets which should be added.
         * @return this Group
         */
        public Group addWidgets(Widget ... widgets) {
            for(Widget w : widgets) {
                addWidget(w);
            }
            return this;
        }

        /**
         * Adds several widgets to this group, inserting the specified gap in between.
         * Each widget also gets an animation state set depending on it's position.
         *
         * The state gapName+"NotFirst" is set to false for widgets[0] and true for all others
         * The state gapName+"NotLast" is set to false for widgets[n-1] and true for all others
         *
         * @param gapName the name of the gap to insert between widgets
         * @param widgets The widgets which should be added.
         * @return this Group
         */
        public Group addWidgetsWithGap(String gapName, Widget ... widgets) {
            StateKey stateNotFirst = StateKey.get(gapName.concat("NotFirst"));
            StateKey stateNotLast = StateKey.get(gapName.concat("NotLast"));
            for(int i=0,n=widgets.length ; i<n ;i++) {
                if(i > 0) {
                    addGap(gapName);
                }
                Widget w = widgets[i];
                addWidget(w);
                AnimationState as = w.getAnimationState();
                as.setAnimationState(stateNotFirst, i > 0);
                as.setAnimationState(stateNotLast, i < n-1);
            }
            return this;
        }
        
        /**
         * Adds a generic gap. Can use symbolic gap names.
         *
         * @param min the minimum size in pixels or a symbolic constant
         * @param pref the preferred size in pixels or a symbolic constant
         * @param max the maximum size in pixels or a symbolic constant
         * @return this Group
         * @see DialogLayout#SMALL_GAP
         * @see DialogLayout#MEDIUM_GAP
         * @see DialogLayout#LARGE_GAP
         * @see DialogLayout#DEFAULT_GAP
         */
        public Group addGap(int min, int pref, int max) {
            addSpring(new GapSpring(min, pref, max, false));
            return this;
        }

        /**
         * Adds a fixed sized gap. Can use symbolic gap names.
         *
         * @param size the size in pixels or a symbolic constant
         * @return this Group
         * @see DialogLayout#SMALL_GAP
         * @see DialogLayout#MEDIUM_GAP
         * @see DialogLayout#LARGE_GAP
         * @see DialogLayout#DEFAULT_GAP
         */
        public Group addGap(int size) {
            addSpring(new GapSpring(size, size, size, false));
            return this;
        }

        /**
         * Adds a gap with minimum size. Can use symbolic gap names.
         *
         * @param minSize the minimum size in pixels or a symbolic constant
         * @return this Group
         * @see DialogLayout#SMALL_GAP
         * @see DialogLayout#MEDIUM_GAP
         * @see DialogLayout#LARGE_GAP
         * @see DialogLayout#DEFAULT_GAP
         */
        public Group addMinGap(int minSize) {
            addSpring(new GapSpring(minSize, minSize, Short.MAX_VALUE, false));
            return this;
        }

        /**
         * Adds a flexible gap with no minimum size.
         *
         * <p>This is equivalent to {@code addGap(0, 0, Short.MAX_VALUE) }</p>
         * @return this Group
         */
        public Group addGap() {
            addSpring(new GapSpring(0, 0, Short.MAX_VALUE, false));
            return this;
        }

        /**
         * Adds a named gap.
         * 
         * <p>Named gaps are configured via the theme parameter "namedGaps" which
         * maps from names to &lt;gap&gt; objects.</p>
         * 
         * <p>They behave equal to {@link #addGap(int, int, int) }.</p>
         * 
         * @param name the name of the gap (vcase sensitive)
         * @return this Group
         */
        public Group addGap(String name) {
            if(name.length() == 0) {
                throw new IllegalArgumentException("name");
            }
            addSpring(new NamedGapSpring(name));
            return this;
        }

        /**
         * Remove all default gaps from this and child groups
         */
        public void removeDefaultGaps() {
            for(int i=springs.size() ; i-->0 ;) {
                Spring s = springs.get(i);
                if(s instanceof GapSpring) {
                    if(((GapSpring)s).isDefault) {
                        springs.remove(i);
                    }
                } else if(s instanceof Group) {
                    ((Group)s).removeDefaultGaps();
                }
            }
        }

        /**
         * Add a default gap between all children except if the neighbour is already a Gap.
         */
        public void addDefaultGap() {
            for(int i=0 ; i<springs.size() ; i++) {
                Spring s = springs.get(i);
                if(s instanceof Group) {
                    ((Group)s).addDefaultGap();
                }
            }
        }

        /**
         * Removes the specified group from this group.
         * 
         * @param g the group to remove
         * @param removeWidgets if true all widgets in the specified group
         *      should be removed from the {@code DialogLayout}
         * @return true if it was found and removed, false otherwise
         */
        public boolean removeGroup(Group g, boolean removeWidgets) {
            for(int i=0 ; i<springs.size() ; i++) {
                if(springs.get(i) == g) {
                    springs.remove(i);
                    if(removeWidgets) {
                        g.removeWidgets();
                        DialogLayout.this.recheckWidgets();
                    }
                    DialogLayout.this.layoutGroupsChanged();
                    return true;
                }
            }
            return false;
        }

        /**
         * Removes all elements from this group
         *
         * @param removeWidgets if true all widgets in this group are removed
         *      from the {@code DialogLayout}
         */
        public void clear(boolean removeWidgets) {
            if(removeWidgets) {
                removeWidgets();
            }
            springs.clear();
            if(removeWidgets) {
                DialogLayout.this.recheckWidgets();
            }
            DialogLayout.this.layoutGroupsChanged();
        }

        void addSpring(Spring s) {
            springs.add(s);
            DialogLayout.this.layoutGroupsChanged();
        }

        void recheckWidgets() {
            for(int i=springs.size() ; i-->0 ;) {
                Spring s = springs.get(i);
                if(s instanceof WidgetSpring) {
                    if(!widgetSprings.containsKey(((WidgetSpring)s).w)) {
                        springs.remove(i);
                    }
                } else if(s instanceof Group) {
                    ((Group)s).recheckWidgets();
                }
            }
        }
        
        void removeWidgets() {
            for(int i=springs.size() ; i-->0 ;) {
                Spring s = springs.get(i);
                if(s instanceof WidgetSpring) {
                    removeChild((WidgetSpring)s);
                } else if(s instanceof Group) {
                    ((Group)s).removeWidgets();
                }
            }
        }
    }

    static class SpringDelta implements Comparable<SpringDelta> {
        final int idx;
        final int delta;

        SpringDelta(int idx, int delta) {
            this.idx = idx;
            this.delta = delta;
        }

        public int compareTo(SpringDelta o) {
            return delta - o.delta;
        }
    }

    class SequentialGroup extends Group {
        SequentialGroup() {
        }

        @Override
        int getMinSize(int axis) {
            int size = 0;
            for(int i=0,n=springs.size() ; i<n ; i++) {
                Spring s = springs.get(i);
                if(includeInvisibleWidgets || s.isVisible()) {
                    size += s.getMinSize(axis);
                }
            }
            return size;
        }

        @Override
        int getPrefSize(int axis) {
            int size = 0;
            for(int i=0,n=springs.size() ; i<n ; i++) {
                Spring s = springs.get(i);
                if(includeInvisibleWidgets || s.isVisible()) {
                    size += s.getPrefSize(axis);
                }
            }
            return size;
        }

        @Override
        int getMaxSize(int axis) {
            int size = 0;
            boolean hasMax = false;
            for(int i=0,n=springs.size() ; i<n ; i++) {
                Spring s = springs.get(i);
                if(includeInvisibleWidgets || s.isVisible()) {
                    int max = s.getMaxSize(axis);
                    if(max > 0) {
                        size += max;
                        hasMax = true;
                    } else {
                        size += s.getPrefSize(axis);
                    }
                }
            }
            return hasMax ? size : 0;
        }
        
        /**
         * Add a default gap between all children except if the neighbour is already a Gap.
         */
        @Override
        public void addDefaultGap() {
            if(springs.size() > 1) {
                boolean wasGap = true;
                for(int i=0 ; i<springs.size() ; i++) {
                    Spring s = springs.get(i);
                    if(includeInvisibleWidgets || s.isVisible()) {
                        boolean isGap = (s instanceof GapSpring) || (s instanceof NamedGapSpring);
                        if(!isGap && !wasGap) {
                            springs.add(i++, new GapSpring(DEFAULT_GAP, DEFAULT_GAP, DEFAULT_GAP, true));
                        }
                        wasGap = isGap;
                    }
                }
            }
            super.addDefaultGap();
        }

        @Override
        void setSize(int axis, int pos, int size) {
            int prefSize = getPrefSize(axis);
            if(size == prefSize) {
                for(Spring s : springs) {
                    if(includeInvisibleWidgets || s.isVisible()) {
                        int spref = s.getPrefSize(axis);
                        s.setSize(axis, pos, spref);
                        pos += spref;
                    }
                }
            } else if(springs.size() == 1) {
                // no need to check visibility flag
                Spring s = springs.get(0);
                s.setSize(axis, pos, size);
            } else if(springs.size() > 1) {
                setSizeNonPref(axis, pos, size, prefSize);
            }
        }

        private void setSizeNonPref(int axis, int pos, int size, int prefSize) {
            int delta = size - prefSize;
            boolean useMin = delta < 0;
            if(useMin) {
                delta = -delta;
            }

            SpringDelta[] deltas = new SpringDelta[springs.size()];
            int resizeable = 0;
            for(int i=0 ; i<springs.size() ; i++) {
                Spring s = springs.get(i);
                if(includeInvisibleWidgets || s.isVisible()) {
                    int sdelta = useMin
                            ? s.getPrefSize(axis) - s.getMinSize(axis)
                            : s.getMaxSize(axis) - s.getPrefSize(axis);
                    if(sdelta > 0)  {
                        deltas[resizeable++] = new SpringDelta(i, sdelta);
                    }
                }
            }
            if(resizeable > 0) {
                if(resizeable > 1) {
                    Arrays.sort(deltas, 0, resizeable);
                }
                
                int sizes[] = new int[springs.size()];

                int remaining = resizeable;
                for(int i=0 ; i<resizeable ; i++) {
                    SpringDelta d = deltas[i];
                    
                    int sdelta = delta / remaining;
                    int ddelta = Math.min(d.delta, sdelta);
                    delta -= ddelta;
                    remaining--;
                    
                    if(useMin) {
                        ddelta = -ddelta;
                    }
                    sizes[d.idx] = ddelta;
                }

                for(int i=0 ; i<springs.size() ; i++) {
                    Spring s = springs.get(i);
                    if(includeInvisibleWidgets || s.isVisible()) {
                        int ssize = s.getPrefSize(axis) + sizes[i];
                        s.setSize(axis, pos, ssize);
                        pos += ssize;
                    }
                }
            } else {
                for(Spring s : springs) {
                    if(includeInvisibleWidgets || s.isVisible()) {
                        int ssize;
                        if(useMin) {
                            ssize = s.getMinSize(axis);
                        } else {
                            ssize = s.getMaxSize(axis);
                            if(ssize == 0) {
                                ssize = s.getPrefSize(axis);
                            }
                        }
                        s.setSize(axis, pos, ssize);
                        pos += ssize;
                    }
                }
            }
        }
    }

    class ParallelGroup extends Group {
        ParallelGroup() {
        }

        @Override
        int getMinSize(int axis) {
            int size = 0;
            for(int i=0,n=springs.size() ; i<n ; i++) {
                Spring s = springs.get(i);
                if(includeInvisibleWidgets || s.isVisible()) {
                    size = Math.max(size, s.getMinSize(axis));
                }
            }
            return size;
        }

        @Override
        int getPrefSize(int axis) {
            int size = 0;
            for(int i=0,n=springs.size() ; i<n ; i++) {
                Spring s = springs.get(i);
                if(includeInvisibleWidgets || s.isVisible()) {
                    size = Math.max(size, s.getPrefSize(axis));
                }
            }
            return size;
        }

        @Override
        int getMaxSize(int axis) {
            int size = 0;
            for(int i=0,n=springs.size() ; i<n ; i++) {
                Spring s = springs.get(i);
                if(includeInvisibleWidgets || s.isVisible()) {
                    size = Math.max(size, s.getMaxSize(axis));
                }
            }
            return size;
        }

        @Override
        void setSize(int axis, int pos, int size) {
            for(int i=0,n=springs.size() ; i<n ; i++) {
                Spring s = springs.get(i);
                if(includeInvisibleWidgets || s.isVisible()) {
                    s.setSize(axis, pos, size);
                }
            }
        }

        @Override
        public Group addGap() {
            getLogger().log(Level.WARNING, "Useless call to addGap() on ParallelGroup", new Throwable());
            return this;
        }
    }
}
