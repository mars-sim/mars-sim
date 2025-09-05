/*
 * Mars Simulation Project
 * EntityTableModel.java
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
 * This class provides a table model implementation that allows each row to be mapped to
 * a single simulation entity. The properties of the entity are mapped into columns
 * by the sub implementation. It provides the ability to cache specific columns in a 
 * backing store to reduce the computation effort.
 * 
 */
@SuppressWarnings("serial")
public abstract class EntityTableModel<T> extends AbstractMonitorModel {


    private List<T> entities;
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
	protected EntityTableModel(String name, String countingMsgKey, ColumnSpec[] names) {
        super(name, countingMsgKey, names);

        this.entities = new ArrayList<>();
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
     * Resets the entities that provide the Row data.
     * 
     * @param newEntities
     */
    protected void resetEntities(Collection<T> newEntities) {
		fireEnabled = false;
		
		if (!entities.isEmpty()) {
			// Take a shallow copy as going to be removing items
			List<T> oldUnits = new ArrayList<>(entities);
			for(T old : oldUnits) {
				removeEntity(old);
			}
		}

		for(T newUnit : newEntities) {
			addEntity(newUnit);
		}

		
        fireTableDataChanged();
        
		// Just fire one table event for the whole table
		fireEnabled = true;
    }

    /**
     * Adds an entity to the model. This can be overridden if special onboarding
     * logic is need, e.g. register listeners.
     * 
     * @param newEntity
     * @return 
     */
    protected boolean addEntity(T newEntity) {
        boolean add = !entities.contains(newEntity);
        if (add) {
            if (fireEnabled) {
                // Do async
                SwingUtilities.invokeLater(() -> addRow(newEntity));
            }
            else {
                addRow(newEntity);
            }
        }
        return add;
    }

    /**
     * Adds a row of entity.
     * 
     * @param newEntity
     */
    private void addRow(T newEntity) {
        entities.add(newEntity);

        if (rowCache != null) {
            // Add the data row now
            rowCache.put(newEntity, new HashMap<>());
        }

        if (fireEnabled) {
            int idx = entities.indexOf(newEntity);
            fireTableRowsInserted(idx, idx);
        }
    }

    /**
     * Removes a previously added Entity form the model.
     */
    protected void removeEntity(T oldEntity) {
        int idx = entities.indexOf(oldEntity);
        if (idx < 0) {
            return;
        }

        if (fireEnabled) {
            // Do async
            SwingUtilities.invokeLater(() -> removeRow(oldEntity, idx));
        }
        else {
            removeRow(oldEntity, idx);
        }
    }

    private void removeRow(T oldEntity, int idx) {
        entities.remove(oldEntity);
        if (rowCache != null) {
            rowCache.remove(oldEntity);
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
    protected List<T> getEntities() {
        return entities;
    }

    /**
	 * Gets the Entity<T> at the specified row.
	 *
	 * @param index Index of the row.
	 * @return Entity matching row
	 */
	protected T getEntity(int index) {
		try {
            return entities.get(index);
        }
        catch (IndexOutOfBoundsException ioe) {
            // Entity list is refreshing
            return null;
        }
	}

	/**
	 * Gets the unit at the specified row.
	 *
	 * @param row Indexes of Unit to retrieve.
	 * @return Unit at specified position.
	 */
	@Override
	public Object getObject(int row) {
		return getEntity(row);
	}


	/**
	 * Gets the number of rows in the model.
	 *
	 * @return the number of Units.
	 */
	@Override
	public int getRowCount() {
		return entities.size();
	}

    /**
     * Gets a value for a particular cell. This may come from a cached value
     * if the column is one of the cached columns.
     * 
     * @param rowIndex
     * @param columnIndex
     * @see #getEntityValue(Object, int)
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        T entity = getEntity(rowIndex);
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
        Object result = getEntityValue(entity, columnIndex);

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
        int rowIndex = entities.indexOf(entity);
        if (rowIndex < 0) {
            return;
        }

        for(int i = firstCol; i<= lastCol; i++) {
            if (cachedColumns.contains(i)) {
                // Recalculate cached value in this Thread to avoid problem
                // with calculating derived values in the UI Thread
                Object newValue = getEntityValue(entity, i);
                Object cachedValue = getCacheValue(entity, i);
                if ((cachedValue == null) || !cachedValue.equals(newValue)) {
                    setCacheValue(entity, i, getEntityValue(entity, i));
                }
            }

            // Fire the cell update in the background thread
            SwingUtilities.invokeLater(new TableCellUpdater(rowIndex, i));
        }
    }

    /**
     * Gets the real value of this entity for a specific column. This implementation
     * may involve expensive calculations.
     */
    protected abstract Object getEntityValue(T entity, int column);

    private class TableCellUpdater implements Runnable {

        private int colIndex;
        private int rowIndex;

        public TableCellUpdater(int rowIndex, int colIndex) {
            this.rowIndex = rowIndex;
            this.colIndex = colIndex;
        }

        @Override
        public void run() {
            fireTableCellUpdated(rowIndex, colIndex);
        }
    }
}
