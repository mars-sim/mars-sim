/*
 * Mars Simulation Project
 * UnitTab.java
 * @date 2021-12-07
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.List;

import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

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
	public UnitTab(final MonitorWindow window, UnitTableModel<?> model, boolean mandatory, String icon) {
		// Use TableTab constructor
		super(window, model, mandatory, false, icon);

		// Generic renderer
		TableColumnModel m = table.getColumnModel();
		for(int i = 0; i < m.getColumnCount(); i++) {
			TableColumn tc = m.getColumn(i);
			Class<?> columnClass = model.getColumnClass(tc.getModelIndex());
			if (columnClass.equals(Double.class)) {
				tc.setCellRenderer(DIGIT2_RENDERER);
			}
			else if (columnClass.equals(Number.class)) {
				tc.setCellRenderer(DIGIT2_RENDERER);
			}			
		}
		
		super.adjustColumnWidth(table);

		setEntityDriven(true);
		setNavigatable(true);
	}

	/**
	 * Display details for selected rows
	 */
	@Override
	public void displayDetails(MainDesktopPane desktop) {
		List<?> rows = getSelection();
		for(Object o : rows) {
			if (o instanceof Unit u) {
				desktop.openUnitWindow(u, false);
			}
		}
	}

	/**
	 * Get the Coordinates of the selectd Unit
	 * @return Cooridnates, maybe null
	 */
    public Coordinates getSelectedCoordinates() {
		List<?> rows = getSelection();
		if (!rows.isEmpty() && (rows.get(0) instanceof Unit u)) {
			return u.getCoordinates();
		}
		return null;
    }
}
