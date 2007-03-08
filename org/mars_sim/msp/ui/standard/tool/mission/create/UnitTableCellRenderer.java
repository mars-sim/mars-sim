package org.mars_sim.msp.ui.standard.tool.mission.create;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

class UnitTableCellRenderer extends DefaultTableCellRenderer {

	private UnitTableModel model;
	
	UnitTableCellRenderer(UnitTableModel model) {
		this.model = model;
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (model.isFailureCell(row, column)) setBackground(Color.RED);
		else if (!isSelected) setBackground(Color.WHITE);
		
		return result;
	}
}