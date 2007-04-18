/**
 * Mars Simulation Project
 * TradeTab.java
 * @version 2.81 2007-04-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.monitor;

/**
 * This class represents a table of trade good values at settlements displayed 
 * within the Monitor Window. 
 */
public class TradeTab extends TableTab {
	
	TradeTab() {
		// Use TableTab constructor
		super(new TradeTableModel(), true, false);
	}
}