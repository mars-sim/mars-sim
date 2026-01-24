/*
 * Mars Simulation Project
 * EntityListLauncher.java
 * @date 2023-05-26
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.event.MouseInputAdapter;

import com.mars_sim.core.Entity;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.UIContext;

/**
 * This class listens for double click event on a JLast. When an event triggers; a details window
 * is launched.
 */
public class EntityListLauncher extends MouseInputAdapter {
    public static final String TOOLTIP = Msg.getString("entity.doubleClick");
    private UIContext desktop;

    /**
     * Create a launcher that will create a UnitDetails window.
     * 
     * @param desktop
     */
    public EntityListLauncher(UIContext desktop) {
        this.desktop = desktop;
    }

    /**
     * Catch the double click mouse event. The component under the click event is retrieved
     * which should be a JList; from this the selected row is found.
     * This desktop is that used to open the appropriate tools window.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        // If double-click, open a details window
        if (e.getClickCount() >= 2 && e.getComponent() instanceof JList list) {
            desktop.showDetails((Entity) list.getSelectedValue());
        }
    }
}
