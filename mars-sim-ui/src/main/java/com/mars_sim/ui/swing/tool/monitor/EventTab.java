/**
 * Mars Simulation Project
 * EventTab.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.monitor;

import com.mars_sim.ui.swing.UIContext;

/**
 * This class represents a historical event table displayed within the Monitor
 * Window.
 */
@SuppressWarnings("serial")
public class EventTab extends TableTab {
	private static final String EVENT_ICON = "event";

	/**
	 * constructor.
	 * 
	 * @param window {@link MonitorWindow} the containing window
	 * @param context the UI context
	 */
	public EventTab(final MonitorWindow window, UIContext context) {
		// Use TableTab constructor
		super(window, new EventTableModel(context.getSimulation().getEventManager()), true, false,
				EVENT_ICON);
		
		setFilterable(true);
	}

	void filterCategories(UIContext context) {
		EventFilter filter = new EventFilter((EventTableModel) getModel(), context.getTopFrame());
		filter.setVisible(true);
	}
}
