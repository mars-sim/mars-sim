/**
 * Mars Simulation Project
 * AbstractWizardItemModel.java
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

/**
 * An abstract table model for a wizard step that involves selecting an item.
 * The items can have error states that prevent selection.
 * @param <T> The type of item to select.
 */
@SuppressWarnings("serial")
public abstract class AbstractWizardItemModel<T> extends AbstractTableModel 
	implements EntityListener, WizardItemModel<T> {

	// Data members.
	private List<T> items;
	private List<ColumnSpec> columns;

	/**
	 * Constructor
	 */
	protected AbstractWizardItemModel(List<ColumnSpec> columns) {
		
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
	@Override
	public void release() {
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
		return isFailureCell(row, col);
	}

	/**
	 * Gets the unit at a row index.
	 * 
	 * @param row the row index.
	 * @return the unit in the row, or null if none.
	 */
	@Override
	public T getItem(int row) {
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
	@Override
	public boolean isFailureItem(int row) {
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

	/**
	 * Checks if the cell at the specified row and column is in a failure state.
	 * @param row the row index.
	 * @param column the column index.
	 * @return Message if the cell is in a failure state, or null if valid.
	 */
	@Override
	public String isFailureCell(int row, int column) {
		T item = items.get(row);
		return isFailureCell(item, column);
	}

	/**
	 * Default implementation of failure cell check returns no failure.
	 * Subclasses should override this to return a failure message if the cell is in a failure state, or null if not.
	 * @param item Item being checked
	 * @param column Column being checked
	 * @return Message if the cell is in a failure state, or null if valid.
	 */
	protected String isFailureCell(T item, int column) {
		return null;
	}
}
