/*
 * Mars Simulation Project
 * UnitTab.java
 * @date 2021-12-07
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.List;

import com.mars_sim.core.Unit;
import com.mars_sim.mapdata.location.Coordinates;

/**
 * This class represents a unit table displayed within the Monitor Window.
 */
@SuppressWarnings("serial")
public class UnitTab
extends TableTab {

	/**
	 * Constructor.
	 * 
	 * @param model the table model.
	 * @param mandatory Is this table view mandatory.
	 */
	public UnitTab(final MonitorWindow window, UnitTableModel<?> model, boolean mandatory, String icon) {
		// Use TableTab constructor
		super(window, model, mandatory, false, icon);

		adjustColumnWidth(table);

		setEntityDriven(true);
		setNavigatable(true);
	}

	/**
	 * Gets the coordinates of the selected unit.
	 * 
	 * @return Coordinates, maybe null
	 */
	@Override
    public Coordinates getSelectedCoordinates() {
		List<?> rows = getSelection();
		if (!rows.isEmpty() && (rows.get(0) instanceof Unit u)) {
			return u.getCoordinates();
		}
		return null;
    }
}
