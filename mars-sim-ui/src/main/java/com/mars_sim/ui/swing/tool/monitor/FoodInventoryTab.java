/*
 * Mars Simulation Project
 * FoodInventoryTab.java
 * @date 2022-05-27
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.Set;

import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.tool.NumberRenderer;

/**
 * This class represents an inventory of food at settlements displayed within
 * the Monitor Window.
 */
public class FoodInventoryTab extends TableTab {
	private static final String FOOD_ICON = "food";

	/**
	 * constructor.
	 *
	 * @param selectedSettlement
	 * @param window {@link MonitorWindow} the containing window.
	 */
	public FoodInventoryTab(Set<Settlement> selectedSettlement, final MonitorWindow window) {
		// Use TableTab constructor
		super(window, new FoodInventoryTableModel(selectedSettlement), true, false, FOOD_ICON);
	
		TableColumnModel m = table.getColumnModel();
		for (int i= FoodInventoryTableModel.NUM_INITIAL_COLUMNS; i < m.getColumnCount(); i++) {
			TableCellRenderer r;
			switch(i) {
				case FoodInventoryTableModel.COST_COL:
				case FoodInventoryTableModel.PRICE_COL:
					r = NumberRenderer.getCurrencyRenderer();
					break;

				case FoodInventoryTableModel.MASS_COL:
					r = DIGIT2_RENDERER;
					break;

				default:
					r = DIGIT3_RENDERER;
			}

			m.getColumn(i).setCellRenderer(r);
		}
		
		adjustColumnWidth(table);
	}
}
