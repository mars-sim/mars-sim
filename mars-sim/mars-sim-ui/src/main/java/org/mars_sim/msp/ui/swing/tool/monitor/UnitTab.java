/**
 * Mars Simulation Project
 * UnitTab.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

/**
 * This class represents a unit table displayed within the Monitor Window. 
 */
public class UnitTab
extends TableTab {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * @param model the table model.
	 * @param mandatory Is this table view mandatory.
	 */
	public UnitTab(final MonitorWindow window, UnitTableModel model, boolean mandatory) {
		// Use TableTab constructor
		super(window, model, mandatory, false);
	}
}