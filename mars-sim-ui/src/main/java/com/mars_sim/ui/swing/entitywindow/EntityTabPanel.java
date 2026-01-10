/*
 * Mars Simulation Project
 * EntityTabPanel.java
 * @date 2025-12-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow;

import javax.swing.Icon;

import com.mars_sim.core.Entity;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.tool.monitor.EntityMonitorModel;
import com.mars_sim.ui.swing.tool.monitor.MonitorWindow;
import com.mars_sim.ui.swing.unit_window.TabPanel;

/**
 * This is a typesafe Panel to be used for Entity Content Panel. 
 * It represents a single Entity of type T.
 * 
 * Eventually this will consume the TabPanel functionality.
 */
@SuppressWarnings("serial")
public abstract class EntityTabPanel<T extends Entity> extends TabPanel {

    // Name of the icon to use for the general tab
    protected static final String GENERAL_ICON = "info";
    protected static final String GENERAL_TITLE = Msg.getString("EntityGeneral.title");
    protected static final String GENERAL_TOOLTIP = Msg.getString("EntityGeneral.tooltip");

    private T entity;

    protected EntityTabPanel(String tabTitle, Icon tabIcon, String tabToolTip, UIContext context, T entity) {
        super(tabTitle, tabIcon, tabToolTip, context);
        this.entity = entity;
    }

    /**
     * Get the entity this panel is displaying.
     * @return
     */
    protected T getEntity() {
        return entity;
    }

    /**
	 * Displays a new Unit model in the monitor window.
	 *
	 * @param model the new model to display
	 */
	protected void showModel(EntityMonitorModel<?> model) {
		var cw = getContext().openToolWindow(MonitorWindow.NAME);
		((MonitorWindow)cw).displayModel(model);
	}
}
