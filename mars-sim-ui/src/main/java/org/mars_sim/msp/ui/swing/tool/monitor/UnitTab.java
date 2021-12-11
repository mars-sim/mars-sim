/*
 * Mars Simulation Project
 * UnitTab.java
 * @date 2021-12-07
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
	public UnitTab(final MonitorWindow window, UnitTableModel model, boolean mandatory, String icon) throws Exception {
		// Use TableTab constructor
		super(window, model, mandatory, false, icon);
	}
}
