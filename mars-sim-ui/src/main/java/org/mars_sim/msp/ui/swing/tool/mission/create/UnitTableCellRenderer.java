/**
 * Mars Simulation Project
 * UnitTableCellRenderer.java
 * @version 3.1.0 2017-09-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission.create;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


/**
 * Cell renderer for unit tables that marks failure cells as red.
 */
@SuppressWarnings("serial")
class UnitTableCellRenderer extends DefaultTableCellRenderer {

	// Private data members.
	private UnitTableModel model;
	
	/**
	 * Constructor
	 * @param model the unit table model.
	 */
	UnitTableCellRenderer(UnitTableModel model) {
		this.model = model;
	}
	
	/**
	 * Returns the default table cell renderer.
	 * @param table the table the cell is in.
	 * @param value the value in the cell.
	 * @return the rendering component.
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		// If failure cell, mark background red.
		if (model.isFailureCell(row, column)) setBackground(Color.RED);
		else if (!isSelected) setBackground(Color.WHITE);
		
		return result;
	}
}