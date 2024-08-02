/*
 * Mars Simulation Project
 * TradeTab.java
 * @date 2022-06-16
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.monitor;

import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.mars_sim.ui.swing.NumberCellRenderer;
import com.mars_sim.ui.swing.tool.NumberRenderer;

/**
 * This class represents a table of trade good values at settlements displayed
 * within the Monitor Window.
 */
@SuppressWarnings("serial")
public class TradeTab extends TableTab {
	private static final String TRADE_ICON = "trade";

	private NumberCellRenderer currency = new NumberCellRenderer(2, "$ ");
	
	/**
	 * constructor.
	 * 
	 * @param selectedSettlement
	 * @param window {@link MonitorWindow} the containing window.
	 */
	public TradeTab(final MonitorWindow window) {
		// Use TableTab constructor
		super(window, new TradeTableModel(), true, false, TRADE_ICON);

	
		TableColumnModel m = table.getColumnModel();
		for(int i = TradeTableModel.NUM_INITIAL_COLUMNS; i < m.getColumnCount(); i++) {
			TableCellRenderer renderer;
			switch(i) {
				case TradeTableModel.PRICE_COL:
					renderer = currency;
					break;

				case TradeTableModel.COST_COL:
					renderer = currency;
					break;

				case TradeTableModel.QUANTITY_COL:
					renderer = NumberRenderer.getIntegerRenderer();
					break;

				case TradeTableModel.FLATTEN_COL:
					renderer = DIGIT3_RENDERER;
					break;
					
				default:
					renderer = DIGIT2_RENDERER;
			}
			m.getColumn(i).setCellRenderer(renderer);				
		}
		
		adjustColumnWidth(table);
	}
}
