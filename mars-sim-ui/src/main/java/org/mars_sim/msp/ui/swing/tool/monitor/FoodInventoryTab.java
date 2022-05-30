/**
 * Mars Simulation Project
 * FoodInventoryTab.java
 * @date 2022-05-27
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.NumberRenderer;

/**
 * This class represents an inventory of food at settlements displayed within
 * the Monitor Window.
 */
@SuppressWarnings("serial")
public class FoodInventoryTab extends TableTab implements UnitManagerListener {
	
	/** The minimum number of decimal places to be displayed. */
	private static final int NUM_DIGITS = 2;
	
	/**
	 * constructor.
	 *
	 * @param selectedSettlement
	 * @param window {@link MonitorWindow} the containing window.
	 */
	public FoodInventoryTab(Settlement selectedSettlement, final MonitorWindow window) {
		// Use TableTab constructor
		super(window, new FoodInventoryTableModel(selectedSettlement), true, false, MonitorWindow.FOOD_ICON);
	
		// Override default cell renderer for format double values.
//		table.setDefaultRenderer(Double.class, new NumberCellRenderer(2));

		TableColumnModel m = table.getColumnModel();
		int num = FoodInventoryTableModel.NUM_INITIAL_COLUMNS;
		int cols = FoodInventoryTableModel.NUM_DATA_COL;
		for (int i= 0; i < m.getColumnCount(); i++) {
			if (i >= num) {
				int col = i - num;
				if (col % cols == 0)
					m.getColumn(i).setCellRenderer(NumberRenderer.getIntegerRenderer());
				else if (col % cols == 1)
					m.getColumn(i).setCellRenderer(new NumberCellRenderer(NUM_DIGITS, true));
				else if (col % cols == 2)
					m.getColumn(i).setCellRenderer(NumberRenderer.getCurrencyRenderer());
			}
		}
		
		// Add as unit manager listener.
		Simulation.instance().getUnitManager().addUnitManagerListener(this);
	}

	@Override
	public void unitManagerUpdate(UnitManagerEvent event) {

		if (event.getUnit().getUnitType() == UnitType.SETTLEMENT) {

			Settlement settlement = (Settlement) event.getUnit();

			if (UnitManagerEventType.ADD_UNIT == event.getEventType()) {
				// If settlement is new, add to settlement columns.
				TableColumn column = new TableColumn(table.getColumnCount());
				column.setHeaderValue(settlement.getName());
				SwingUtilities.invokeLater(new FoodColumnModifier(column, true));
			} 
			else if (UnitManagerEventType.REMOVE_UNIT == event.getEventType()) {
				// If settlement is gone, remove from settlement columns.
				TableColumn column = table.getColumn(settlement.getName());
				if (column != null) {
					SwingUtilities.invokeLater(new FoodColumnModifier(column, false));
				}
			}
		}
	}

	/**
	 * An inner class for adding or removing food table columns.
	 */
	private class FoodColumnModifier implements Runnable {

		// Data members.
		private TableColumn column;
		private boolean addColumn;

		/**
		 * Constructor
		 *
		 * @param column    the column to add or remove.
		 * @param addColumn true for adding column or false for removing column.
		 */
		private FoodColumnModifier(TableColumn column, boolean addColumn) {
			this.column = column;
			this.addColumn = addColumn;
		}

		@Override
		public void run() {
			if (addColumn) {
				table.getColumnModel().addColumn(column);
			} else {
				table.getColumnModel().removeColumn(column);
			}
		}
	}
}
