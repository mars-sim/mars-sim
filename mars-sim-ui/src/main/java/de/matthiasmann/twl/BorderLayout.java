/*
 * Copyright (c) 2008-2012, Stefan Lange
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
 *     * Neither the name of Stefan Lange nor the names of its contributors may
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

import java.util.EnumMap;

/**
 * A Layout Manager similar to the {@link java.awt.BorderLayout} from AWT.
 * 
 * <p>The layout can only have up to 5 children mapped to a specific {@link Location}.
 * These Locations are: <code>NORTH</code>, <code>SOUTH</code>,
 * <code>EAST</code>, <code>WEST</code> and <code>CENTER</code>.</p>
 * 
 * <p><code>NORTH</code> and <code>SOUTH</code> will both fill the available horizontal space.<br>
 * <code>EAST</code> and <code>WEST</code> will fill them self between
 * <code>NORTH</code> and <code>SOUTH</code>.<br>
 * <code>CENTER</code> takes all the remaining space.</p>
 *
 * <p>All Locations will have at least their minimum size and the layout will
 * resize itself according to that.</p>
 *
 * <hr>
 *
 * Here is how the Layout looks like when all locations are filled:
 *
 * <pre>
 * |=====================================|
 * |                NORTH                |
 * |==========+===============+==========|
 * |          |               |          |
 * |          |               |          |
 * |   WEST   |    CENTER     |  EAST    |
 * |          |               |          |
 * |          |               |          |
 * |==========+===============+==========|
 * |                SOUTH                |
 * |=====================================|
 * </pre>
 *
 * @author Stefan Lange
 */
public class BorderLayout extends Widget {
    private final EnumMap<Location, Widget> widgets;
    private int hgap, vgap;

    /**
     * The location of a widget in the BorderLayout.
     */
    public enum Location {
        EAST, WEST, NORTH, SOUTH, CENTER
    }

    public BorderLayout() {
        widgets = new EnumMap<BorderLayout.Location, Widget>(Location.class);
    }

    /**
     * Adds the specific
     * <code>widget</code> to a
     * <code>location</code> in the BorderLayout.
     *
     * @param widget the widget to add
     * @param location the location to set the widget to
     */
    public void add(Widget widget, Location location) {
        if(widget == null) {
            throw new NullPointerException("widget is null");
        }
        if(location == null) {
            throw new NullPointerException("location is null");
        }
        if(widgets.containsKey(location)) {
            throw new IllegalStateException("a widget was already added to that location: " + location);
        }

        widgets.put(location, widget);
        try {
            super.insertChild(widget, getNumChildren());
        } catch(Exception e) {
            removeChild(location);
        }

    }

    /**
     * @param location the location to look retrieve
     * @return the child at the specific
     * <code>location</code> or null if there is no child.
     */
    public Widget getChild(Location location) {
        if(location == null) {
            throw new NullPointerException("location is null");
        }
        return widgets.get(location);
    }

    /**
     * Remove the child at the specific
     * <code>location</code>.
     *
     * @param location the location to remove
     * @return the removed widget or null if there is no child.
     */
    public Widget removeChild(Location location) {
        if(location == null) {
            throw new NullPointerException("location is null");
        }
        Widget w = widgets.remove(location);
        if(w != null) {
            removeChild(w);
        }

        return w;
    }

    /**
     * Adds the widget to the center location of the layout.
     */
    @Override
    public void add(Widget child) {
        add(child, Location.CENTER);
    }

    /**
     * This is not supproted in the BorderLayout.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void insertChild(Widget child, int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("insert child is not supported by the BorderLayout");
    }

    @Override
    protected void childRemoved(Widget exChild) {
        for(Location loc : widgets.keySet()) {
            if(widgets.get(loc) == exChild) {
                widgets.remove(loc);
                break;
            }
        }
        super.childRemoved(exChild);
    }

    @Override
    protected void allChildrenRemoved() {
        widgets.clear();
        super.allChildrenRemoved();
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        hgap = themeInfo.getParameter("hgap", 0);
        vgap = themeInfo.getParameter("vgap", 0);

        super.applyTheme(themeInfo);
    }

    @Override
    protected void layout() {
        int top = getInnerY();
        int bottom = getInnerBottom();
        int left = getInnerX();
        int right = getInnerRight();

        Widget w;

        if((w = widgets.get(Location.NORTH)) != null) {
            w.setPosition(left, top);
            w.setSize(Math.max(right - left, 0), Math.max(w.getPreferredHeight(), 0));
            top += w.getPreferredHeight() + vgap;
        }
        if((w = widgets.get(Location.SOUTH)) != null) {
            w.setPosition(left, bottom - w.getPreferredHeight());
            w.setSize(Math.max(right - left, 0), Math.max(w.getPreferredHeight(), 0));
            bottom -= w.getPreferredHeight() + vgap;
        }
        if((w = widgets.get(Location.EAST)) != null) {
            w.setPosition(right - w.getPreferredWidth(), top);
            w.setSize(Math.max(w.getPreferredWidth(), 0), Math.max(bottom - top, 0));
            right -= w.getPreferredWidth() + hgap;
        }
        if((w = widgets.get(Location.WEST)) != null) {
            w.setPosition(left, top);
            w.setSize(Math.max(w.getPreferredWidth(), 0), Math.max(bottom - top, 0));
            left += w.getPreferredWidth() + hgap;
        }
        if((w = widgets.get(Location.CENTER)) != null) {
            w.setPosition(left, top);
            w.setSize(Math.max(right - left, 0), Math.max(bottom - top, 0));
        }
    }

    @Override
    public int getMinWidth() {
        return computeMinWidth();
    }

    @Override
    public int getMinHeight() {
        return computeMinHeight();
    }

    @Override
    public int getPreferredInnerWidth() {
        return computePrefWidth();
    }

    @Override
    public int getPreferredInnerHeight() {
        return computePrefHeight();
    }

    private int computeMinWidth() {
        int size = 0;

        size += getChildMinWidth(widgets.get(Location.EAST), hgap);
        size += getChildMinWidth(widgets.get(Location.WEST), hgap);
        size += getChildMinWidth(widgets.get(Location.CENTER), 0);

        size = Math.max(size, getChildMinWidth(widgets.get(Location.NORTH), 0));
        size = Math.max(size, getChildMinWidth(widgets.get(Location.SOUTH), 0));

        return size;
    }

    private int computeMinHeight() {
        int size = 0;

        size = Math.max(size, getChildMinHeight(widgets.get(Location.EAST), 0));
        size = Math.max(size, getChildMinHeight(widgets.get(Location.WEST), 0));
        size = Math.max(size, getChildMinHeight(widgets.get(Location.CENTER), 0));

        size += getChildMinHeight(widgets.get(Location.NORTH), vgap);
        size += getChildMinHeight(widgets.get(Location.SOUTH), vgap);

        return size;
    }

    private int computePrefWidth() {
        int size = 0;

        size += getChildPrefWidth(widgets.get(Location.EAST), hgap);
        size += getChildPrefWidth(widgets.get(Location.WEST), hgap);
        size += getChildPrefWidth(widgets.get(Location.CENTER), 0);

        size = Math.max(size, getChildPrefWidth(widgets.get(Location.NORTH), 0));
        size = Math.max(size, getChildPrefWidth(widgets.get(Location.SOUTH), 0));

        return size;
    }

    private int computePrefHeight() {
        int size = 0;

        size = Math.max(size, getChildPrefHeight(widgets.get(Location.EAST), 0));
        size = Math.max(size, getChildPrefHeight(widgets.get(Location.WEST), 0));
        size = Math.max(size, getChildPrefHeight(widgets.get(Location.CENTER), 0));

        size += getChildPrefHeight(widgets.get(Location.NORTH), vgap);
        size += getChildPrefHeight(widgets.get(Location.SOUTH), vgap);

        return size;
    }

    // return 0 since a child of the BorderLayout can be null
    private int getChildMinWidth(Widget w, int gap) {
        if(w != null) {
            return w.getMinWidth() + gap;
        }
        return 0;
    }

    private int getChildMinHeight(Widget w, int gap) {
        if(w != null) {
            return w.getMinHeight() + gap;
        }
        return 0;
    }

    private int getChildPrefWidth(Widget w, int gap) {
        if(w != null) {
            return computeSize(w.getMinWidth(), w.getPreferredWidth(), w.getMaxWidth()) + gap;
        }
        return 0;
    }

    private int getChildPrefHeight(Widget w, int gap) {
        if(w != null) {
            return computeSize(w.getMinHeight(), w.getPreferredHeight(), w.getMaxHeight()) + gap;
        }
        return 0;
    }
}