/**
 * Mars Simulation Project
 * GoodsTableModel.java
 * @date 21-07-22
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.mission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.goods.CommerceMission;
import org.mars_sim.msp.core.goods.Good;

/**
 * Abstract model for a goods table.
 */
abstract class GoodsTableModel
	extends AbstractTableModel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members.
	private Map<Good, Integer> goodsMap;
	private List<Good> goodsList;

	/**
	 * Construc */
	public GoodsTableModel() {
		// Use AbstractTableModel constructor.
		super();

		// Initialize goods map and list.
		goodsList = new ArrayList<>();
		goodsMap = new HashMap<>();
	}

	/**
	 * Returns the number of rows in the model.
	 * @return number of rows.
	 */
	public int getRowCount() {
		return goodsList.size();
	}

	/**
	 * Returns the number of columns in the model.
	 * @return number of columns.
	 */
	public int getColumnCount() {
		return 2;
	}

	/**
	 * Returns the name of the column at columnIndex.
	 * @param columnIndex the column index.
	 * @return column name.
	 */
	public String getColumnName(int columnIndex) {
		if (columnIndex == 0) return "Good";
		else return "Amount";
	}

	/**
	 * Returns the value for the cell at columnIndex and rowIndex.
	 * @param row the row whose value is to be queried.
	 * @param column the column whose value is to be queried.
	 * @return the value Object at the specified cell.
	 */
	public Object getValueAt(int row, int column) {
		Object result = "unknown";

		if (row < goodsList.size()) {
			Good good = goodsList.get(row); 
			if (column == 0) result = good.getName();
			else result = goodsMap.get(good);
		}

		return result;
	}

	/**
	 * Get the load data
	 */
	protected abstract Map<Good,Integer> getLoad(CommerceMission commerce);

	/**
	 * Updates the table data.
	 */
	protected void updateTable(CommerceMission commerce) {
		Map<Good,Integer> load = getLoad(commerce);
		if (load != null) {
			goodsMap = load;
			goodsList = new ArrayList<>(goodsMap.keySet());
			Collections.sort(goodsList);
		}
		else {
			goodsMap.clear();
			goodsList.clear();
		}
		fireTableDataChanged();
	}
}