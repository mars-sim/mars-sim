/**
 * Mars Simulation Project
 * EventTab.java
 * @version 2.75 2004-01-17
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.standard.tool.monitor;

import org.mars_sim.msp.ui.standard.MainDesktopPane;

/**
 * This class represents a historical event table displayed within the Monitor Window. 
 */
public class EventTab extends TableTab {

	public EventTab(EventTableModel model) {
		
		// Use TableTab constructor
		super(model, true, false);
	}

	void filterCategories(MainDesktopPane desktop) {
		EventFilter filter = new EventFilter((EventTableModel) getModel(), desktop);
		filter.show();
	}
}