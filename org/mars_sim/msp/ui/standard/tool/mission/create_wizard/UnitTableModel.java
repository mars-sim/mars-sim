package org.mars_sim.msp.ui.standard.tool.mission.create_wizard;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.UnitCollection;

abstract class UnitTableModel extends AbstractTableModel {

	// Domain members.
	protected UnitCollection units;
	protected List columns;
	
	UnitTableModel() {
		// Use AbstractTableModel constructor.
		super();
		
		units = new UnitCollection();
		columns = new ArrayList();
	}
	
	public int getRowCount() {
		return units.size();
	}

	public int getColumnCount() {
		return columns.size();
	}

	public String getColumnName(int columnIndex) {
		return (String) columns.get(columnIndex);
    }
	
	Unit getUnit(int row) {
		Unit result = null;
		if ((row > -1) && (row < getRowCount())) result = units.get(row);
		return result;
	}
	
	abstract void updateTable();
	
	abstract boolean isFailureCell(int row, int column);
	
	boolean isFailureRow(int row) {
		boolean result = false;
		for (int x = 0; x < getColumnCount(); x++) {
			if (isFailureCell(row, x)) result = true;
		}
		return result;
	}
}