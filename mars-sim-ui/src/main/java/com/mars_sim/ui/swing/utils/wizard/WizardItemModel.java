/**
 * Mars Simulation Project
 * WizardItemModel.java
 * @date 2026-02-03
 * @author Barry Evans
 */

package com.mars_sim.ui.swing.utils.wizard;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.MonitorableEntity;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.components.EnhancedTableModel;

/**
 * An abstract table model for a wizard step that involves selecting an item.
 * The items can have error states that prevent selection.
 * @param <T> The type of item to select.
 */
@SuppressWarnings("serial")
public abstract class WizardItemModel<T> extends AbstractTableModel 
	implements EnhancedTableModel, EntityListener {

	// Data members.
	private List<T> items;
	private List<ColumnSpec> columns;

	/**
	 * Constructor
	 */
	protected WizardItemModel(List<ColumnSpec> columns) {
		
		items = new ArrayList<>();
		this.columns = columns;
	}
	
	/**
	 * Set the items to select from
	 * @param items
	 */
	protected void setItems(List<T> items) {
		this.items.addAll(items);

		// Add listener to each item so we can update the table when they change
		for (T item : items) {
			if (item instanceof MonitorableEntity me) {
				me.addEntityListener(this);
			}
		}
	}

	/**
	 * Release resources by removing listeners from items.
	 */
	void release() {
		for (T item : items) {
			if (item instanceof MonitorableEntity me) {
				me.removeEntityListener(this);
			}
		}
	}
	
	/**
	 * Catches entity update event and updates the table row for the changed entity.
	 * @param entity the entity event.
	 */
	@Override
	public void entityUpdate(EntityEvent entity) {
		// Find the row for the changed entity and update it
		var idx = items.indexOf(entity.getSource());
		if (idx != -1) {
			fireTableRowsUpdated(idx, idx);
		}
	}

	/**
	 * Returns the number of rows in the model.
	 * 
	 * @return number of rows.
	 */
	@Override
	public int getRowCount() {
		return items.size();
	}

	/**
	 * Returns the number of columns in the model.
	 * 
	 * @return number of columns.
	 */
	@Override
	public int getColumnCount() {
		return columns.size();
	}

	/**
	 * Returns the name of the column at columnIndex.
	 * 
	 * @param columnIndex the column index.
	 * @return column name.
	 */
	@Override
	public String getColumnName(int columnIndex) {
		return columns.get(columnIndex).name();
    }
	
	/**
	 * Returns the class of the column at columnIndex.
	 * @param columnIndex the column index.
	 * @return column class.
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columns.get(columnIndex).type();
	}

	/**
	 * Returns the spec of the column at columnIndex.
	 * 
	 * @param modelIndex the column index.
	 * @return column spec.
	 */
	@Override
	public ColumnSpec getColumnSpec(int modelIndex) {
		return columns.get(modelIndex);
	}

	/**
	 * Returns the tooltip for the cell at row and column.
	 * This invokes getToolTipAt for the item in the row and column.
	 * 
	 * @param row the row index.
	 * @param col the column index.
	 * @return Default is no tooltip
	 */
	@Override
	public final String getToolTipAt(int row, int col) {
		T target = getItem(row);
		return getToolTipAt(target, col);
	}
	
	/**
	 * Is there a tooltip to display for this Item and column
	 * @param item Item under the mouse
	 * @param col Column number
	 * @return Tooltip text to display
	 */
	protected String getToolTipAt(T item, int col) {
		return isFailureCell(item, col);
	}

	/**
	 * Gets the unit at a row index.
	 * 
	 * @param row the row index.
	 * @return the unit in the row, or null if none.
	 */
	T getItem(int row) {
		if ((row > -1) && (row < getRowCount())) {
			return items.get(row);
		}
		return null;
	}

	/**
	 * Delegates to getItemValue to get the value for the cell at columnIndex and rowIndex.
	 * @param rowIndex the row index.
	 * @param columnIndex the column index.
	 * @return the cell value.
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = "unknown";

		if (rowIndex < items.size()) {
			T item = getItem(rowIndex);
			result = getItemValue(item, columnIndex);
		}

		return result;
	}

	/**
	 * Checks if a table cell is a failure cell.
	 * 
	 * @param item the table row.
	 * @param column the table column.
	 * @return the failure reason if cell is a failure cell, null or empty string otherwise.
	 */
	protected abstract String isFailureCell(T item, int column);

	/**
	 * Gets the value for the cell at columnIndex and rowIndex.
	 * @param item the item.
	 * @param column the column index.
	 * @return Rendered values
	 */
	protected abstract Object getItemValue(T item, int column);
	
	/**
	 * Checks if row contains a failure cell.
	 * 
	 * @param row the row index.
	 * @return true if row has failure cell.
	 */
	boolean isFailureItem(int row) {
		T item = items.get(row);
		for (int x = 0; x < getColumnCount(); x++) {
			String failure = isFailureCell(item, x);

			// Stop on first failure
			if (failure != null) {
				return true;
			}
		}
		return false;
	}
}
