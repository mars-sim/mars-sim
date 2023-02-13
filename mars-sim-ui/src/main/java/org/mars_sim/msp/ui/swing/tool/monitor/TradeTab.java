/*
 * Mars Simulation Project
 * TradeTab.java
 * @date 2022-06-16
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.tool.NumberRenderer;

/**
 * This class represents a table of trade good values at settlements displayed
 * within the Monitor Window.
 */
@SuppressWarnings("serial")
public class TradeTab extends TableTab {
	private static final String TRADE_ICON = "trade";

	/**
	 * constructor.
	 * 
	 * @param selectedSettlement
	 * @param window {@link MonitorWindow} the containing window.
	 */
	public TradeTab(Settlement selectedSettlement, final MonitorWindow window) {
		// Use TableTab constructor
		super(window, new TradeTableModel(selectedSettlement), true, false, TRADE_ICON);

		TableColumnModel m = table.getColumnModel();
		for(int i = TradeTableModel.NUM_INITIAL_COLUMNS; i < m.getColumnCount(); i++) {
			TableCellRenderer renderer;
			switch(i) {
				case TradeTableModel.PRICE_COL:
					renderer = NumberRenderer.getCurrencyRenderer();
					break;

				case TradeTableModel.COST_COL:
					renderer = NumberRenderer.getCurrencyRenderer();
					break;

				case TradeTableModel.QUANTITY_COL:
					renderer = NumberRenderer.getIntegerRenderer();
					break;

				default:
					renderer = DIGIT2_RENDERER;
			}
			m.getColumn(i).setCellRenderer(renderer);				
		}
		
		super.adjustColumnWidth(table);
	}
}
