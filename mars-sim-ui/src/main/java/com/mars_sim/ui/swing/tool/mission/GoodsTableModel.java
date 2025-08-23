/**
 * Mars Simulation Project
 * GoodsTableModel.java
 * @date 21-07-22
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.mission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.goods.Good;

/**
 * Abstract model for a goods table.
 */
public class GoodsTableModel extends AbstractTableModel {

	/**
	 * This is the value of a good and its amount.
	 */
	public record GoodAmount(Good good, int amount) {}

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
	@Override
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
	@Override
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
	 * Get teh good and amount at a specific row.
	 * @param row
	 * @return
	 */
	public GoodAmount getValueAt(int row) {
		if (row < goodsList.size()) {
			Good good = goodsList.get(row); 
			return new GoodAmount(good, goodsMap.get(good));
		}
		return null;
	}

	/**
	 * Updates the table data.
	 */
	public void updateTable(Map<Good,Integer> load) {
		if (load != null) {
			goodsMap = new HashMap<>(load);
			goodsList = new ArrayList<>(goodsMap.keySet());
		}
		else {
			goodsMap.clear();
			goodsList.clear();
		}
		fireTableDataChanged();
	}

	/**
	 * Adjust the ammount for the selected Good by the delta.
	 * If the Good is not in the model it will be added with delta as the initial value.
	 * @param good
	 * @param delta
	 */
	public void changeGoodAmount(Good good, int delta) {
		int idx = goodsList.indexOf(good);
		int newValue = goodsMap.merge(good, delta, Integer::sum);

		// What is the notification
		if (newValue == 0) {
			// Good removed so full table has changed
			goodsMap.remove(good);
			goodsList.remove(good);
			fireTableDataChanged();
		}
		else if (idx == -1) {
			// New good added so full table has changed
			goodsList.add(good);
			fireTableDataChanged();
		}
		else {
			// Existing good changed.
			fireTableRowsUpdated(idx, idx);
		}
	}

	/**
	 * Get the current goods and ammounts in this model
	 * @return
	 */
	public Map<Good, Integer> getGoods() {
		return goodsMap;
	}
}