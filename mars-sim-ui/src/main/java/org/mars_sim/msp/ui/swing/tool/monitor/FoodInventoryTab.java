/*
 * Mars Simulation Project
 * FoodInventoryTab.java
 * @date 2022-05-27
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import javax.swing.table.TableColumnModel;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.NumberRenderer;

/**
 * This class represents an inventory of food at settlements displayed within
 * the Monitor Window.
 */
@SuppressWarnings("serial")
public class FoodInventoryTab extends TableTab implements UnitManagerListener {
	
	/** The minimum 2 of decimal places to be displayed. */
	private static final int TWO_DIGITS = 2;
	/** The minimum 3 decimal places to be displayed. */
	private static final int THREE_DIGITS = 3;
	
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
		int init = FoodInventoryTableModel.NUM_INITIAL_COLUMNS;
		int numCols = FoodInventoryTableModel.NUM_DATA_COL;
		for (int i= 0; i < m.getColumnCount(); i++) {
			if (i >= init) {
				int col = i - init;
				int c = col % numCols;
				if (c == 2)
					m.getColumn(i).setCellRenderer(new NumberCellRenderer(TWO_DIGITS, true));				
				else if (c == 5 || c == 6)
					m.getColumn(i).setCellRenderer(NumberRenderer.getCurrencyRenderer());
				else 
					m.getColumn(i).setCellRenderer(new NumberCellRenderer(THREE_DIGITS, true));

			}
		}
		
		// Add as unit manager listener.
		Simulation.instance().getUnitManager().addUnitManagerListener(this);
	}

	@Override
	public void unitManagerUpdate(UnitManagerEvent event) {
	}
}
