/**
 * Mars Simulation Project
 * UnitTab.java
 * @version 3.1.1 2020-07-22
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

/**
 * This class represents a unit table displayed within the Monitor Window.
 */
@SuppressWarnings("serial")
public class UnitTab
extends TableTab {

	/**
	 * Constructor.
	 * @param model the table model.
	 * @param mandatory Is this table view mandatory.
	 */
	public UnitTab(final MonitorWindow window, UnitTableModel model, boolean mandatory, String icon) {
		// Use TableTab constructor
		super(window, model, mandatory, false, icon);
	}
}
