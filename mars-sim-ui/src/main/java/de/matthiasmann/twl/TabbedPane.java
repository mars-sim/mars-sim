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

import de.matthiasmann.twl.model.BooleanModel;
import de.matthiasmann.twl.model.HasCallback;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;
import java.util.ArrayList;

/**
 * A tabbed pane with support for scrolling the tabs
 * 
 * @author Matthias Mann
 */
public class TabbedPane extends Widget {

    public static final StateKey STATE_FIRST_TAB = StateKey.get("firstTab");
    public static final StateKey STATE_LAST_TAB = StateKey.get("lastTab");
    
    public enum TabPosition {
        TOP(true),
        LEFT(false),
        RIGHT(true),
        BOTTOM(false);

        final boolean horz;
        private TabPosition(boolean horz) {
            this.horz = horz;
        }
    }
    
    private final ArrayList<Tab> tabs;
    private final BoxLayout tabBox;
    private final Widget tabBoxClip;
    private final Container container;
    final Container innerContainer;

    DialogLayout scrollControlls;
    Button btnScrollLeft;
    Button btnScrollRight;

    boolean scrollTabs;
    int tabScrollPosition;
    TabPosition tabPosition;
    Tab activeTab;

    public TabbedPane() {
        this.tabs = new ArrayList<Tab>();
        this.tabBox = new BoxLayout();
        this.tabBoxClip = new Widget();
        this.container = new Container();
        this.innerContainer = new Container();
        this.tabPosition = TabPosition.TOP;

        tabBox.setTheme("tabbox");
        tabBoxClip.setTheme("");
        innerContainer.setTheme("");
        innerContainer.setClip(true);

        tabBoxClip.add(tabBox);
        container.add(innerContainer);
        
        super.insertChild(container, 0);
        super.insertChild(tabBoxClip, 1);

        addActionMapping("nextTab", "cycleTabs", +1);
        addActionMapping("prevTab", "cycleTabs", -1);
        setCanAcceptKeyboardFocus(false);
    }

    public TabPosition getTabPosition() {
        return tabPosition;
    }

    public void setTabPosition(TabPosition tabPosition) {
        if(tabPosition == null) {
            throw new NullPointerException("tabPosition");
        }
        if(this.tabPosition != tabPosition) {
            this.tabPosition = tabPosition;
            tabBox.setDirection(tabPosition.horz
                    ? BoxLayout.Direction.HORIZONTAL
                    : BoxLayout.Direction.VERTICAL);
            invalidateLayout();
        }
    }

    public boolean isScrollTabs() {
        return scrollTabs;
    }

    /**
     * Allow the tabs to be scrolled if they don't fit into the available space.
     *
     * Default is false.
     *
     * If disabled the minimum size of the tabbed pane ensures that all tabs fit.
     * If enabled additional scroll controlls are displayed.
     *
     * @param scrollTabs true if tabs should scroll
     */
    public void setScrollTabs(boolean scrollTabs) {
        if(this.scrollTabs != scrollTabs) {
            this.scrollTabs = scrollTabs;

            if(scrollControlls == null && scrollTabs) {
                createScrollControlls();
            }

            tabBoxClip.setClip(scrollTabs);
            if(scrollControlls != null) {
                scrollControlls.setVisible(scrollTabs);
            }
            invalidateLayout();
        }
    }

    public Tab addTab(String title, Widget pane) {
        Tab tab = new Tab();
        tab.setTitle(title);
        tab.setPane(pane);
        tabBox.add(tab.button);
        tabs.add(tab);

        if(tabs.size() == 1) {
            setActiveTab(tab);
        }
        updateTabStates();
        return tab;
    }

    public Tab getActiveTab() {
        return activeTab;
    }
    
    public void setActiveTab(Tab tab) {
        if(tab != null) {
            validateTab(tab);
        }
        
        if(activeTab != tab) {
            Tab prevTab = activeTab;
            activeTab = tab;

            if(prevTab != null) {
                prevTab.doCallback();
            }
            if(tab != null) {
                tab.doCallback();
            }

            if(scrollTabs) {
                validateLayout();
                
                int pos, end, size;
                if(tabPosition.horz) {
                    pos  = tab.button.getX() - tabBox.getX();
                    end  = tab.button.getWidth() + pos;
                    size = tabBoxClip.getWidth();
                } else {
                    pos  = tab.button.getY() - tabBox.getY();
                    end  = tab.button.getHeight() + pos;
                    size = tabBoxClip.getHeight();
                }
                int border = (size + 19) / 20;
                pos -= border;
                end += border;
                if(pos < tabScrollPosition) {
                    setScrollPos(pos);
                } else if(end > tabScrollPosition + size) {
                    setScrollPos(end - size);
                }
            }
            
            if(tab != null && tab.pane != null) {
                tab.pane.requestKeyboardFocus();
            }
        }
    }

    public void removeTab(Tab tab) {
        validateTab(tab);

        int idx = (tab == activeTab) ? tabs.indexOf(tab) : -1;
        tab.setPane(null);
        tabBox.removeChild(tab.button);
        tabs.remove(tab);

        if(idx >= 0 && !tabs.isEmpty()) {
            setActiveTab(tabs.get(Math.min(tabs.size()-1, idx)));
        }
        updateTabStates();
    }

    public void removeAllTabs() {
        innerContainer.removeAllChildren();
        tabBox.removeAllChildren();
        tabs.clear();
        activeTab = null;
    }
    
    public int getNumTabs() {
        return tabs.size();
    }
    
    public Tab getTab(int index) {
        return tabs.get(index);
    }
    
    public int getActiveTabIndex() {
        if(tabs.isEmpty()) {
            return -1;
        }
        return tabs.indexOf(activeTab);
    }

    public void cycleTabs(int direction) {
        if(!tabs.isEmpty()) {
            int idx = tabs.indexOf(activeTab);
            if(idx < 0) {
                idx = 0;
            } else {
                idx += direction;
                idx %= tabs.size();
                idx += tabs.size();
                idx %= tabs.size();
            }
            setActiveTab(tabs.get(idx));
        }
    }

    @Override
    public int getMinWidth() {
        int minWidth;
        if(tabPosition.horz) {
            int tabBoxWidth;
            if(scrollTabs) {
                tabBoxWidth = tabBox.getBorderHorizontal() +
                        BoxLayout.computeMinWidthVertical(tabBox) +
                        scrollControlls.getPreferredWidth();
            } else {
                tabBoxWidth = tabBox.getMinWidth();
            }
            minWidth = Math.max(container.getMinWidth(), tabBoxWidth);
        } else {
            minWidth = container.getMinWidth() + tabBox.getMinWidth();
        }
        return Math.max(super.getMinWidth(), minWidth + getBorderHorizontal());
    }

    @Override
    public int getMinHeight() {
        int minHeight;
        if(tabPosition.horz) {
            minHeight = container.getMinHeight() + tabBox.getMinHeight();
        } else {
            minHeight = Math.max(container.getMinHeight(), tabBox.getMinHeight());
        }
        return Math.max(super.getMinHeight(), minHeight + getBorderVertical());
    }

    @Override
    public int getPreferredInnerWidth() {
        if(tabPosition.horz) {
            int tabBoxWidth;
            if(scrollTabs) {
                tabBoxWidth = tabBox.getBorderHorizontal() +
                        BoxLayout.computePreferredWidthVertical(tabBox) +
                        scrollControlls.getPreferredWidth();
            } else {
                tabBoxWidth = tabBox.getPreferredWidth();
            }
            return Math.max(container.getPreferredWidth(), tabBoxWidth);
        } else {
            return container.getPreferredWidth() + tabBox.getPreferredWidth();
        }
    }

    @Override
    public int getPreferredInnerHeight() {
        if(tabPosition.horz) {
            return container.getPreferredHeight() + tabBox.getPreferredHeight();
        } else {
            return Math.max(container.getPreferredHeight(), tabBox.getPreferredHeight());
        }
    }

    @Override
    protected void layout() {
        int scrollCtrlsWidth = 0;
        int scrollCtrlsHeight = 0;
        int tabBoxWidth = tabBox.getPreferredWidth();
        int tabBoxHeight = tabBox.getPreferredHeight();

        if(scrollTabs) {
            scrollCtrlsWidth = scrollControlls.getPreferredWidth();
            scrollCtrlsHeight = scrollControlls.getPreferredHeight();
        }

        if(tabPosition.horz) {
            tabBoxHeight = Math.max(scrollCtrlsHeight, tabBoxHeight);
        } else {
            tabBoxWidth = Math.max(scrollCtrlsWidth, tabBoxWidth);
        }

        tabBox.setSize(tabBoxWidth, tabBoxHeight);
        
        switch(tabPosition) {
            case TOP:
                tabBoxClip.setPosition(getInnerX(), getInnerY());
                tabBoxClip.setSize(Math.max(0, getInnerWidth() - scrollCtrlsWidth), tabBoxHeight);
                container.setSize(getInnerWidth(), Math.max(0, getInnerHeight() - tabBoxHeight));
                container.setPosition(getInnerX(), tabBoxClip.getBottom());
                break;

            case LEFT:
                tabBoxClip.setPosition(getInnerX(), getInnerY());
                tabBoxClip.setSize(tabBoxWidth, Math.max(0, getInnerHeight() - scrollCtrlsHeight));
                container.setSize(Math.max(0, getInnerWidth() - tabBoxWidth), getInnerHeight());
                container.setPosition(tabBoxClip.getRight(), getInnerY());
                break;

            case RIGHT:
                tabBoxClip.setPosition(getInnerX() - tabBoxWidth, getInnerY());
                tabBoxClip.setSize(tabBoxWidth, Math.max(0, getInnerHeight() - scrollCtrlsHeight));
                container.setSize(Math.max(0, getInnerWidth() - tabBoxWidth), getInnerHeight());
                container.setPosition(getInnerX(), getInnerY());
                break;

            case BOTTOM:
                tabBoxClip.setPosition(getInnerX(), getInnerY() - tabBoxHeight);
                tabBoxClip.setSize(Math.max(0, getInnerWidth() - scrollCtrlsWidth), tabBoxHeight);
                container.setSize(getInnerWidth(), Math.max(0, getInnerHeight() - tabBoxHeight));
                container.setPosition(getInnerX(), getInnerY());
                break;
        }

        if(scrollControlls != null) {
            if(tabPosition.horz) {
                scrollControlls.setPosition(tabBoxClip.getRight(), tabBoxClip.getY());
                scrollControlls.setSize(scrollCtrlsWidth, tabBoxHeight);
            } else {
                scrollControlls.setPosition(tabBoxClip.getX(), tabBoxClip.getBottom());
                scrollControlls.setSize(tabBoxWidth, scrollCtrlsHeight);
            }
            setScrollPos(tabScrollPosition);
        }
    }

    private void createScrollControlls() {
        scrollControlls = new DialogLayout();
        scrollControlls.setTheme("scrollControls");

        btnScrollLeft = new Button();
        btnScrollLeft.setTheme("scrollLeft");
        btnScrollLeft.addCallback(new CB(-1));

        btnScrollRight = new Button();
        btnScrollRight.setTheme("scrollRight");
        btnScrollRight.addCallback(new CB(+1));

        DialogLayout.Group horz = scrollControlls.createSequentialGroup()
                .addWidget(btnScrollLeft)
                .addGap("scrollButtons")
                .addWidget(btnScrollRight);

        DialogLayout.Group vert = scrollControlls.createParallelGroup()
                .addWidget(btnScrollLeft)
                .addWidget(btnScrollRight);

        scrollControlls.setHorizontalGroup(horz);
        scrollControlls.setVerticalGroup(vert);

        super.insertChild(scrollControlls, 2);
    }

    void scrollTabs(int dir) {
        dir *= Math.max(1, tabBoxClip.getWidth() / 10);
        setScrollPos(tabScrollPosition + dir);
    }
    
    private void setScrollPos(int pos) {
        int maxPos;
        if(tabPosition.horz) {
            maxPos = tabBox.getWidth() - tabBoxClip.getWidth();
        } else {
            maxPos = tabBox.getHeight() - tabBoxClip.getHeight();
        }
        pos = Math.max(0, Math.min(pos, maxPos));
        tabScrollPosition = pos;
        if(tabPosition.horz) {
            tabBox.setPosition(tabBoxClip.getX()-pos, tabBoxClip.getY());
        } else {
            tabBox.setPosition(tabBoxClip.getX(), tabBoxClip.getY()-pos);
        }
        if(scrollControlls != null) {
            btnScrollLeft.setEnabled(pos > 0);
            btnScrollRight.setEnabled(pos < maxPos);
        }
    }

    @Override
    public void insertChild(Widget child, int index) {
        throw new UnsupportedOperationException("use addTab/removeTab");
    }

    @Override
    public void removeAllChildren() {
        throw new UnsupportedOperationException("use addTab/removeTab");
    }

    @Override
    public Widget removeChild(int index) {
        throw new UnsupportedOperationException("use addTab/removeTab");
    }

    protected void updateTabStates() {
        for(int i=0,n=tabs.size() ; i<n ; i++) {
            Tab tab = tabs.get(i);
            AnimationState animationState = tab.button.getAnimationState();
            animationState.setAnimationState(STATE_FIRST_TAB, i == 0);
            animationState.setAnimationState(STATE_LAST_TAB, i == n-1);
        }
    }
    
    private void validateTab(Tab tab) {
        if(tab.button.getParent() != tabBox) {
            throw new IllegalArgumentException("Invalid tab");
        }
    }

    public class Tab extends HasCallback implements BooleanModel {
        final TabButton button;
        Widget pane;
        Runnable closeCallback;
        Object userValue;

        Tab() {
            button = new TabButton(this);
        }

        public boolean getValue() {
            return activeTab == this;
        }

        public void setValue(boolean value) {
            if(value) {
                setActiveTab(this);
            }
        }

        public Widget getPane() {
            return pane;
        }

        public void setPane(Widget pane) {
            if(this.pane != pane) {
                if(this.pane != null) {
                    innerContainer.removeChild(this.pane);
                }
                this.pane = pane;
                if(pane != null) {
                    pane.setVisible(getValue());
                    innerContainer.add(pane);
                }
            }
        }

        public Tab setTitle(String title) {
            button.setText(title);
            return this;
        }
        
        public String getTitle() {
            return button.getText();
        }

        public Tab setTooltipContent(Object tooltipContent) {
            button.setTooltipContent(tooltipContent);
            return this;
        }

        public Object getUserValue() {
            return userValue;
        }

        public void setUserValue(Object userValue) {
            this.userValue = userValue;
        }

        /**
         * Sets the user theme for the tab button. If no user theme is set
         * ({@code null}) then it will use "tabbutton" or
         * "tabbuttonWithCloseButton" if a close callback is registered.
         * 
         * @param theme the user theme name - can be null.
         * @return {@code this}
         */
        public Tab setTheme(String theme) {
            button.setUserTheme(theme);
            return this;
        }

        public Runnable getCloseCallback() {
            return closeCallback;
        }

        public void setCloseCallback(Runnable closeCallback) {
            if(this.closeCallback != null) {
                button.removeCloseButton();
            }
            this.closeCallback = closeCallback;
            if(closeCallback != null) {
                button.setCloseButton(closeCallback);
            }
        }

        @Override
        protected void doCallback() {
            if(pane != null) {
                pane.setVisible(getValue());
            }
            super.doCallback();
        }
    }

    private static class TabButton extends ToggleButton {
        Button closeButton;
        Alignment closeButtonAlignment;
        int closeButtonOffsetX;
        int closeButtonOffsetY;
        String userTheme;
        
        TabButton(BooleanModel model) {
            super(model);
            setCanAcceptKeyboardFocus(false);
            closeButtonAlignment = Alignment.RIGHT;
        }

        public void setUserTheme(String userTheme) {
            this.userTheme = userTheme;
            doSetTheme();
        }
        
        private void doSetTheme() {
            if(userTheme != null) {
                setTheme(userTheme);
            } else if(closeButton != null) {
                setTheme("tabbuttonWithCloseButton");
            } else {
                setTheme("tabbutton");
            }
            reapplyTheme();
        }

        @Override
        protected void applyTheme(ThemeInfo themeInfo) {
            super.applyTheme(themeInfo);
            if(closeButton != null) {
                closeButtonAlignment = themeInfo.getParameter("closeButtonAlignment", Alignment.RIGHT);
                closeButtonOffsetX = themeInfo.getParameter("closeButtonOffsetX", 0);
                closeButtonOffsetY = themeInfo.getParameter("closeButtonOffsetY", 0);
            } else {
                closeButtonAlignment = Alignment.RIGHT;
                closeButtonOffsetX = 0;
                closeButtonOffsetY = 0;
            }
        }

        void setCloseButton(Runnable callback) {
            closeButton = new Button();
            closeButton.setTheme("closeButton");
            doSetTheme();
            add(closeButton);
            closeButton.addCallback(callback);
        }
        
        void removeCloseButton() {
            removeChild(closeButton);
            closeButton = null;
            doSetTheme();
        }

        @Override
        public int getPreferredInnerHeight() {
            return computeTextHeight();
        }

        @Override
        public int getPreferredInnerWidth() {
            return computeTextWidth();
        }
        
        @Override
        protected void layout() {
            if(closeButton != null) {
                closeButton.adjustSize();
                closeButton.setPosition(
                        getX() + closeButtonOffsetX + closeButtonAlignment.computePositionX(getWidth(), closeButton.getWidth()),
                        getY() + closeButtonOffsetY + closeButtonAlignment.computePositionY(getHeight(), closeButton.getHeight()));
            }
        }
    }

    private class CB implements Runnable {
        final int dir;

        CB(int dir) {
            this.dir = dir;
        }

        public void run() {
            scrollTabs(dir);
        }
    }
}
