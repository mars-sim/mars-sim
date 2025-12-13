/*
 * Mars Simulation Project
 * CachingTableModel.java
 * @date 2025-08-27
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import com.mars_sim.ui.swing.utils.ColumnSpec;

/**
 * This class provides a table model implementation that caches values for specific columns to
 * reduce the computational effort to calculate the value from the source item.
 * The source value of item properties are calculated in the getEntityValue method that
 * is implemented by subclasses.
 */
@SuppressWarnings("serial")
public abstract class CachingTableModel<T> extends AbstractMonitorModel {


    private List<T> items;
    private Map<T ,Map<Integer, Object>> rowCache;
    private Set<Integer> cachedColumns;
    private boolean fireEnabled;

    /**
	 * Constructor.
	 *
     * @param name Name of the table
	 * @param countingMsgKey      Key in the Msg bundle used to display row count
	 * @param columns        Details of the columns displayed.
	 */
	protected CachingTableModel(String name, String countingMsgKey, ColumnSpec[] names) {
        super(name, countingMsgKey, names);

        this.items = new ArrayList<>();
        this.cachedColumns = new HashSet<>();
    }
 
    /**
     * Sets which columns can be cached. This will add to any other existing cached columns.
     * 
     * @param minCOl First column that can be cached.
     * @param maxCol Last column (inclusive) that can be cached.
     */
    protected void setCachedColumns(int minCol, int maxCol) {
        for(int i = minCol; i <= maxCol; i++) {
            cachedColumns.add(i);
        }

        // Caching activated
        if (rowCache == null) {
            resetCache();
        }
    }

    protected void resetCache() {
        rowCache = new HashMap<>();   
    }

    /**
     * Resets the items that provide the Row data. This will flush the cache.
     * 
     * @param newItems New items to load into the table.
     */
    protected void resetItems(Collection<T> newItems) {
		fireEnabled = false;
		
		if (!items.isEmpty()) {
			// Take a shallow copy as going to be removing items
			List<T> oldUnits = new ArrayList<>(items);
			for(T old : oldUnits) {
				removeItem(old);
			}
		}

        resetCache();
		for(T newUnit : newItems) {
			addItem(newUnit);
		}

		
        fireTableDataChanged();
        
		// Just fire one table event for the whole table
		fireEnabled = true;
    }

    /**
     * Adds an item to the model. This can be overridden if special onboarding
     * logic is need, e.g. register listeners.
     * 
     * @param newItem Item to add
     * @return 
     */
    protected boolean addItem(T newItem) {
        boolean add = !items.contains(newItem);
        if (add) {
            if (fireEnabled) {
                // Do async
                SwingUtilities.invokeLater(() -> addRow(newItem));
            }
            else {
                addRow(newItem);
            }
        }
        return add;
    }

    /**
     * Adds a row to the actual model.
     * 
     * @param newItem Item to add
     */
    private void addRow(T newItem) {
        items.add(newItem);

        if (rowCache != null) {
            // Add the data row now
            rowCache.put(newItem, new HashMap<>());
        }

        if (fireEnabled) {
            int idx = items.indexOf(newItem);
            fireTableRowsInserted(idx, idx);
        }
    }

    /**
     * Removes a previously added item from the model.
     */
    protected void removeItem(T oldItem) {
        int idx = items.indexOf(oldItem);
        if (idx < 0) {
            return;
        }

        if (fireEnabled) {
            // Do async
            SwingUtilities.invokeLater(() -> removeRow(oldItem, idx));
        }
        else {
            removeRow(oldItem, idx);
        }
    }

    /**
     * Removes a row from the model.
     * @param oldItem
     * @param idx
     */
    private void removeRow(T oldItem, int idx) {
        items.remove(oldItem);
        if (rowCache != null) {
            rowCache.remove(oldItem);
        }

        if (fireEnabled) {
            fireTableRowsDeleted(idx, idx);
        }
    }

    /**
     * Gets the Entities held within the model.
     * 
     * @return
     */
    protected List<T> getItems() {
        return items;
    }

    /**
	 * Gets the item at the specified row.
	 *
	 * @param index Index of the row.
	 * @return Item matching row
	 */
	protected T getItem(int index) {
		try {
            return items.get(index);
        }
        catch (IndexOutOfBoundsException ioe) {
            // Entity list is refreshing
            return null;
        }
	}

	/**
	 * Gets the number of rows in the model.
	 *
	 * @return the number of Units.
	 */
	@Override
	public int getRowCount() {
		return items.size();
	}

    /**
     * Gets a value for a particular cell. This may come from a cached value
     * if the column is one of the cached columns.
     * 
     * @param rowIndex
     * @param columnIndex
     * @see #getItemValue(Object, int)
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        T entity = getItem(rowIndex);
        if (entity == null) {
            return null;
        }
        
        // Pick a value out of the cache if suitable
        boolean useCache = cachedColumns.contains(columnIndex);
        if (useCache) {
            Object cachedValue = getCacheValue(entity, columnIndex);
            if (cachedValue != null) {
                return cachedValue;
            }
        }

        // Get the value direct from the entity
        Object result = getItemValue(entity, columnIndex);

        // Store the result ?
        if (useCache) {
            // Add the value to the cache
            setCacheValue(entity, columnIndex, result);
        }

        return result;
    }

    private Object getCacheValue(T entity, int columnIndex) {
        Map<Integer, Object> rowValues = rowCache.get(entity);
        if (rowValues != null) {
            return rowValues.get(columnIndex);
        }
        return null;
    }

    private void setCacheValue(T entity, int columnIndex, Object value) {
        rowCache.computeIfAbsent(entity, k -> new HashMap<Integer, Object>()).put(columnIndex, value);
    }

    /**
     * Updates a range of column values that have been changed. 
     * This will recalculate any cached columns if needed
     * and always fire a model change event asynchronously.
     * 
     * @param entity
     * @param firstCol
     * @param lastCol
     */
	protected void entityValueUpdated(T entity, int firstCol, int lastCol) {
        int rowIndex = items.indexOf(entity);
        if (rowIndex < 0) {
            return;
        }

        for(int i = firstCol; i<= lastCol; i++) {
            if (cachedColumns.contains(i)) {
                // Recalculate cached value in this Thread to avoid problem
                // with calculating derived values in the UI Thread
                Object newValue = getItemValue(entity, i);
                Object cachedValue = getCacheValue(entity, i);
                if ((cachedValue == null) || !cachedValue.equals(newValue)) {
                    setCacheValue(entity, i, getItemValue(entity, i));
                }
            }

            // Fire the cell update in the background thread
            SwingUtilities.invokeLater(new TableCellUpdater(rowIndex, i));
        }
    }

    /**
     * Gets the real value of this item for a specific column. This implementation
     * may involve expensive calculations.
     */
    protected abstract Object getItemValue(T entity, int column);

    private class TableCellUpdater implements Runnable {

        private int colIndex;
        private int rowIndex;

        public TableCellUpdater(int rowIndex, int colIndex) {
            this.rowIndex = rowIndex;
            this.colIndex = colIndex;
        }

        @Override
        public void run() {
        	if (rowIndex >= 0 && rowIndex < getRowCount()) {
                fireTableCellUpdated(rowIndex, colIndex);
            }
        }
    }
}
