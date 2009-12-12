/**
 * Mars Simulation Project
 * TradeTab.java
 * @version 2.81 2007-04-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.monitor;

import org.mars_sim.msp.ui.swing.NumberCellRenderer;

/**
 * This class represents a table of trade good values at settlements displayed 
 * within the Monitor Window. 
 */
public class TradeTab extends TableTab {
	
	TradeTab() {
		// Use TableTab constructor
		super(new TradeTableModel(), true, false);
		
		// Override default cell renderer for format double values.
		table.setDefaultRenderer(Double.class, new NumberCellRenderer(2));
	}
}