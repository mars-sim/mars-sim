/**
 * Mars Simulation Project
 * UnitTab.java
 * @version 2.80 2007-01-07
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.standard.tool.monitor;

/**
 * This class represents a unit table displayed within the Monitor Window. 
 */
public class UnitTab extends TableTab {

	/**
	 * Constructor
	 * @param model the table model.
	 * @param mandatory Is this table view mandatory.
	 */
	public UnitTab(UnitTableModel model, boolean mandatory) {
		
		// Use TableTab constructor
		super(model, mandatory, false);
	}
}