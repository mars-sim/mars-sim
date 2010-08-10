/**
 * Mars Simulation Project
 * EventTab.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import org.mars_sim.msp.ui.swing.MainDesktopPane;

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