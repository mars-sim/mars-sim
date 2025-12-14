/*
 * Mars Simulation Project
 * EntityTabPanel.java
 * @date 2025-12-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow;

import javax.swing.Icon;

import com.mars_sim.core.Entity;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.unit_window.TabPanel;

/**
 * This is a typesafe Panel to be used for Entity Content Panel. 
 * It represents a single Entity of type T.
 * 
 * Eventually this will consume the TabPanel functionality.
 */
public class EntityTabPanel<T extends Entity> extends TabPanel {
    private T entity;
    private UIContext context;

    protected EntityTabPanel(String tabTitle, Icon tabIcon, String tabToolTip, UIContext context, T entity) {
        super(tabTitle, tabIcon, tabToolTip, null);
        this.entity = entity;
        this.context = context;
    }

    /**
     * Get the entity this panel is displaying.
     * @return
     */
    protected T getEntity() {
        return entity;
    }
    
    /**
     * Get the UI context.
     * @return
     */
    protected UIContext getContext() {
        return context;
    }
}
