/**
 * Mars Simulation Project
 * EventTab.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * This class represents a historical event table displayed within the Monitor Window. 
 */
public class EventTab
extends TableTab {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * constructor.
	 * @param window {@link MonitorWindow} the containing window
	 * @param model {@link EventTableModel}
	 */
	public EventTab(final MonitorWindow window, EventTableModel model) {
		// Use TableTab constructor
		super(window, model, true, false);
	}

	void filterCategories(MainDesktopPane desktop) {
		EventFilter filter = new EventFilter((EventTableModel) getModel(), desktop);
		filter.show();
	}
}