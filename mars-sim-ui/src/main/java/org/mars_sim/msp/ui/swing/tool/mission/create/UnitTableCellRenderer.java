/**
 * Mars Simulation Project
 * UnitTableCellRenderer.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission.create;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
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
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column) {
		super.setBackground(null); // CLear teh background from previous error cell

		JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		// If failure cell, mark background red.
		int rowModel = table.convertRowIndexToModel(row);
		if (model.isFailureCell(rowModel, column))
			l.setBackground(Color.RED);

		return this;
	}
}
