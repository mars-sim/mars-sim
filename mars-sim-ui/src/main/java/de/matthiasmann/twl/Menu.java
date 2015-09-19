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

import de.matthiasmann.twl.model.BooleanModel;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;
import de.matthiasmann.twl.utils.CallbackSupport;
import de.matthiasmann.twl.utils.TypeMapping;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A menu which can be displayed as menu bar or as popup menu.
 *
 * @author Matthias Mann
 */
public class Menu extends MenuElement implements Iterable<MenuElement> {

    public static final StateKey STATE_HAS_OPEN_MENUS = StateKey.get("hasOpenMenus");
    
    public interface Listener {
        /**
         * Called before a menu popup is created.
         * <p>This is only called once until all open popups are closed.</p>
         * <p>When a menu is displayed as menu bar then no events are fired.</p>
         * 
         * @param menu the {@code Menu} which for which a popup will be opened
         */
        public void menuOpening(Menu menu);
        
        /**
         * Called after a popup has been opened.
         * <p>When a menu is displayed as menu bar then no events are fired.</p>
         * 
         * @param menu the {@code Menu} which has been opened
         * @see #menuOpening(de.matthiasmann.twl.Menu) 
         */
        public void menuOpened(Menu menu);
        
        /**
         * Called after a popup has been closed.
         * <p>When a menu is displayed as menu bar then no events are fired.</p>
         * 
         * @param menu the {@code Menu} which has been closed
         */
        public void menuClosed(Menu menu);
    }
    
    private final ArrayList<MenuElement> elements = new ArrayList<MenuElement>();
    private final TypeMapping<Alignment> classAlignments = new TypeMapping<Alignment>();
    private String popupTheme;
    private Listener[] listeners;

    /**
     * Creates a new menu without name.
     * This constructor should be used for top level menus.
     *
     * @see #createMenuBar()
     * @see #createMenuBar(de.matthiasmann.twl.Widget)
     * @see #openPopupMenu(de.matthiasmann.twl.Widget)
     * @see #openPopupMenu(de.matthiasmann.twl.Widget, int, int)
     */
    public Menu() {
    }

    /**
     * Creates a new menu with the given name.
     * This constructor should be used used for sub menus. The name is used for
     * the button which opens this sub menu.
     *
     * @param name The name of the popup menu entry
     * @see #add(de.matthiasmann.twl.MenuElement)
     */
    public Menu(String name) {
        super(name);
    }
    
    public void addListener(Listener listener) {
        listeners = CallbackSupport.addCallbackToList(listeners, listener, Listener.class);
    }
    
    public void removeListener(Listener listener) {
        listeners = CallbackSupport.removeCallbackFromList(listeners, listener);
    }

    /**
     * Returns the theme which is used when this menu is displayed as popup/sub menu.
     * @return the popup theme
     */
    public String getPopupTheme() {
        return popupTheme;
    }

    /**
     * Sets the theme which is used when this menun is displayed as popup/sub menu.
     * @param popupTheme the popup theme
     */
    public void setPopupTheme(String popupTheme) {
        String oldPopupTheme = this.popupTheme;
        this.popupTheme = popupTheme;
        firePropertyChange("popupTheme", oldPopupTheme, this.popupTheme);
    }

    /**
     * Sets the default alignment based on menu element subclasses.
     * <p>By default all alignments are {@link Alignment#FILL}</p>
     * 
     * @param clazz the class for which a default alignment should be set
     * @param value the alignment
     */
    public void setClassAlignment(Class<? extends MenuElement> clazz, Alignment value) {
        if(value == null) {
            throw new NullPointerException("value");
        }
        if(value == Alignment.FILL) {
            classAlignments.remove(clazz);
        } else {
            classAlignments.put(clazz, value);
        }
    }

    /**
     * Retrieves the default alignment for the given menu element class.
     * <p>By default all alignments are {@link Alignment#FILL}</p>
     * 
     * @param clazz the menu element class
     * @return the alignment
     */
    public Alignment getClassAlignment(Class<? extends MenuElement> clazz) {
        Alignment alignment = classAlignments.get(clazz);
        if(alignment == null) {
            return Alignment.FILL;
        }
        return alignment;
    }
    
    /**
     * Returns a mutable iterator which iterates over all menu elements
     * @return a iterator
     */
    public Iterator<MenuElement> iterator() {
        return elements.iterator();
    }

    /**
     * Returns the menu element at the given index.
     * @param index the index. Must be &lt; {code getNumElements}
     * @return the menu element
     * @throws IndexOutOfBoundsException if index is invalid
     * @see #getNumElements()
     */
    public MenuElement get(int index) {
        return elements.get(index);
    }

    /**
     * Returns the number of menu elements in this menu.
     * @return the number of menu elements
     */
    public int getNumElements() {
        return elements.size();
    }

    /**
     * Removes all menu elements
     */
    public void clear() {
        elements.clear();
    }

    /**
     * Adds the given menu element at the end. It is possible to add the same
     * menu element several times also in different menus.
     *
     * @param e the menu element
     * @return this
     */
    public Menu add(MenuElement e) {
        elements.add(e);
        return this;
    }

    /**
     * Adds a {code MenuAction} element at the end. It is equivalent to
     * {code add(new MenuAction(name, cb)) }
     *
     * @param name the name of the menu action
     * @param cb the callback when the menu action has been selected
     * @return this
     */
    public Menu add(String name, Runnable cb) {
        return add(new MenuAction(name, cb));
    }

    /**
     * Adds a {code MenuCheckbox} element at the end.  It is equivalent to
     * {code add(new MenuCheckbox(name, model)) }
     *
     * @param name the name of the menu checkbox
     * @param model the boolean model which is displayed/modified by the menu checkbox
     * @return this
     */
    public Menu add(String name, BooleanModel model) {
        return add(new MenuCheckbox(name, model));
    }

    /**
     * Adds a {code MenuSpacer} element at the end.  It is equivalent to
     * {code add(new MenuSpacer()) }
     *
     * @return this
     */
    public Menu addSpacer() {
        return add(new MenuSpacer());
    }

    /**
     * Creates a menu bar by adding all menu widgets to the specified container.
     *
     * @param container the container for the menu widgets.
     * @see #createMenuBar()
     */
    public void createMenuBar(Widget container) {
        MenuManager mm = createMenuManager(container, true);
        for(Widget w : createWidgets(mm, 0)) {
            container.add(w);
        }
    }

    /**
     * Creates a menu bar with a DialogLayout as conatiner. This is the preferred
     * method to create a menu bar.
     *
     * @return the menu bar conatiner
     */
    public Widget createMenuBar() {
        DialogLayout l = new DialogLayout();
        setWidgetTheme(l, "menubar");

        MenuManager mm = createMenuManager(l, true);
        Widget[] widgets = createWidgets(mm, 0);

        l.setHorizontalGroup(l.createSequentialGroup().addWidgetsWithGap("menuitem", widgets));
        l.setVerticalGroup(l.createParallelGroup(widgets));

        for(int i=0,n=elements.size() ; i<n ; i++) {
            MenuElement e = elements.get(i);
            
            Alignment alignment = e.getAlignment();
            if(alignment == null) {
                alignment = getClassAlignment(e.getClass());
            }
            
            l.setWidgetAlignment(widgets[i], alignment);
        }
        
        l.getHorizontalGroup().addGap();
        return l;
    }

    /**
     * Creates a popup menu from this menu. The popup is positioned to the right of
     * the parent widget.
     *
     * @param parent the parent widget for the popup.
     * @return the MenuManager which manages this popup
     * @see MenuManager#closePopup() 
     */
    public MenuManager openPopupMenu(Widget parent) {
        MenuManager mm = createMenuManager(parent, false);
        mm.openSubMenu(0, this, parent, true);
        return mm;
    }

    /**
     * Creates a popup menu from this menu at the specified position.
     *
     * @param parent the parent widget for the popup.
     * @param x the absolute X coordinate for the popup
     * @param y the absolute Y coordinate for the popup
     * @return the MenuManager which manages this popup
     * @see MenuManager#closePopup()
     */
    public MenuManager openPopupMenu(Widget parent, int x, int y) {
        MenuManager mm = createMenuManager(parent, false);
        Widget popup = mm.openSubMenu(0, this, parent, false);
        if(popup != null) {
            popup.setPosition(x, y);
        }
        return mm;
    }

    @Override
    protected Widget createMenuWidget(MenuManager mm, int level) {
        SubMenuBtn smb = new SubMenuBtn(mm, level);
        setWidgetTheme(smb, "submenu");
        return smb;
    }

    protected MenuManager createMenuManager(Widget parent, boolean isMenuBar) {
        return new MenuManager(parent, isMenuBar);
    }

    protected Widget[] createWidgets(MenuManager mm, int level) {
        Widget[] widgets = new Widget[elements.size()];
        for(int i=0,n=elements.size() ; i<n ; i++) {
            MenuElement e = elements.get(i);
            widgets[i] = e.createMenuWidget(mm, level);
        }
        return widgets;
    }

    DialogLayout createPopup(MenuManager mm, int level, Widget btn) {
        if(listeners != null) {
            for(Listener l : listeners) {
                l.menuOpening(this);
            }
        }
        
        Widget[] widgets = createWidgets(mm, level);
        MenuPopup popup = new MenuPopup(btn, level, this);
        if(popupTheme != null) {
            popup.setTheme(popupTheme);
        }
        popup.setHorizontalGroup(popup.createParallelGroup(widgets));
        popup.setVerticalGroup(popup.createSequentialGroup().addWidgetsWithGap("menuitem", widgets));
        return popup;
    }
    
    void fireMenuOpened() {
        if(listeners != null) {
            for(Listener l : listeners) {
                l.menuOpened(this);
            }
        }
    }
    
    void fireMenuClosed() {
        if(listeners != null) {
            for(Listener l : listeners) {
                l.menuClosed(this);
            }
        }
    }

    static class MenuPopup extends DialogLayout {
        private final Widget btn;
        private final Menu menu;
        final int level;

        MenuPopup(Widget btn, int level, Menu menu) {
            this.btn = btn;
            this.menu = menu;
            this.level = level;
        }

        @Override
        protected void afterAddToGUI(GUI gui) {
            super.afterAddToGUI(gui);
            menu.fireMenuOpened();
            btn.getAnimationState().setAnimationState(STATE_HAS_OPEN_MENUS, true);
        }

        @Override
        protected void beforeRemoveFromGUI(GUI gui) {
            btn.getAnimationState().setAnimationState(STATE_HAS_OPEN_MENUS, false);
            menu.fireMenuClosed();
            super.beforeRemoveFromGUI(gui);
        }

        @Override
        protected boolean handleEvent(Event evt) {
            return super.handleEvent(evt) || evt.isMouseEventNoWheel();
        }
    }
    
    class SubMenuBtn extends MenuBtn implements Runnable {
        private final MenuManager mm;
        private final int level;

        @SuppressWarnings("LeakingThisInConstructor")
        public SubMenuBtn(MenuManager mm, int level) {
            this.mm = mm;
            this.level = level;
            
            addCallback(this);
        }

        public void run() {
            mm.openSubMenu(level, Menu.this, this, true);
        }
    }
}
