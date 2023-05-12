/*
 * Mars Simulation Project
 * EntityTableModel.java
 * @date 2022-10-13
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;

/**
 * This provides a table modle implementation that allows each row to be mapped to
 * a single SImualtion entity. The properties of the entity are mapped into columns
 * by the sub implementation.
 * The class provides the ability to cache specific columns in a backing store to reduce the
 * computation effort.
 * 
 */
@SuppressWarnings("serial")
public abstract class EntityTableModel<T> extends AbstractTableModel
            implements MonitorModel {

//    private static final Logger logger = Logger.getLogger(EntityTableModel.class.getName());

	private String name;
    private String[] columnNames;
    private Class<?>[] columnTypes;
    private List<T> entities;
    private Map<T,Map<Integer,Object>> rowCache;
    private Set<Integer> cachedColumns;
    private boolean fireEnabled;
    private String countingMsgKey;

    /**
	 * Constructor.
	 *
	 * @param tabMsgKey      Key in the Msg bundle used to find name & row count
	 * @param names          Names of the columns displayed.
	 * @param types          The Classes of the individual columns.
	 */
	protected EntityTableModel(String name, String countingMsgKey, String[] names, Class<?>[] types) {
		// Initialize data members
		this.name = name;
        this.countingMsgKey = countingMsgKey;

		this.columnNames = names;
		this.columnTypes = types;
        this.entities = new ArrayList<>();
        this.cachedColumns = new HashSet<>();
    }
 
    /**
     * Set which columns can be cached. This will add to any other existing cached columns
     * @param minCOl First column that can be cached.
     * @param maxCol Last column (inclusive) that can be cahced.
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
     * Reset the entities that provide the Row data.
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
        
		// Just fire one table event for teh whole table
		fireEnabled = true;
    }

    /**
     * Add an entity to the model. This can be overriden if special onboarding
     * logic is need, e.g. register listeners.
     * @param newEntity
     * @return 
     */
    protected boolean addEntity(T newEntity) {
        boolean add = !entities.contains(newEntity);
        if (add) {
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
        return add;
    }

    /**
     * Remove a previously added Entity form the model.
     */
    protected void removeEntity(T oldEntity) {
        int idx = entities.indexOf(oldEntity);

        entities.remove(oldEntity);
        if (rowCache != null) {
            rowCache.remove(oldEntity);
        }

        if (fireEnabled) {
            fireTableRowsDeleted(idx, idx);
        }
    }

    /**
     * Get the Entities held withink the model.
     * @return
     */
    protected List<T> getEntities() {
        return entities;
    }

    /**
	 * Get the Entity<T> at the specified row.
	 *
	 * @param index Index of the row.
	 * @return Entity matching row
	 */
	protected T getEntity(int index) {
		if (index > (getRowCount() - 1))
			throw new IllegalStateException("Invalid index " + index + " for " + getRowCount() + " rows");
		return entities.get(index);
	}

	/**
	 * Get the unit at the specified row.
	 *
	 * @param row Indexes of Unit to retrieve.
	 * @return Unit at specified position.
	 */
	@Override
	public Object getObject(int row) {
		return getEntity(row);
	}

    /**
	 * Return the number of columns
	 *
	 * @return column count.
	 */
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	/**
	 * Return the type of the column requested.
	 *
	 * @param columnIndex Index of column.
	 * @return Class of specified column.
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if ((columnIndex >= 0) && (columnIndex < columnTypes.length)) {
			return columnTypes[columnIndex];
		}
		return Object.class;
	}

	/**
	 * Return the name of the column requested.
	 *
	 * @param columnIndex Index of column.
	 * @return name of specified column.
	 */
	@Override
	public String getColumnName(int columnIndex) {
		if ((columnIndex >= 0) && (columnIndex < columnNames.length)) {
			return columnNames[columnIndex];
		}
		return "Unknown";
	}

	/**
	 * Get the name of the model.
	 *
	 * @return model name.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Get the number of rows in the model.
	 *
	 * @return the number of Units.
	 */
	@Override
	public int getRowCount() {
		return entities.size();
	}

    /**
	 * Gets the model count string.
	 */
	@Override
	public String getCountString() {
		return "  " + Msg.getString(countingMsgKey, getRowCount());
	}

    /**
     * Get a value for a particular cell. This may come from a cached value
     * if the column is one of the cached columns.
     * @param rowIndex
     * @param columnIndex
     * @see #getEntityValue(Object, int)
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        T entity = getEntity(rowIndex);

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
        rowCache.computeIfAbsent(entity, k -> new HashMap<Integer,Object>()).put(columnIndex, value);
    }

    /**
     * A range of column values have changed. This will recalculate any cached columns if needed
     * and always fire a model change event asynchronously.
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
                // with calculating dervied values in teh UI Thread
                Object newValue = getEntityValue(entity, i);
                Object cachedValue = getCacheValue(entity, i);
                if ((cachedValue == null) || !cachedValue.equals(newValue)) {
                    setCacheValue(entity, i, getEntityValue(entity, i));
                }
            }

            // Fire the cell update in the bacground thread
            SwingUtilities.invokeLater(new TableCellUpdater(rowIndex, i));
        }
    }

    /**
     * Tidy up any listenres or external dependencies
     */
    @Override
	public void destroy() {
        // Nothing to do for this base class
    }

    /**
     * Get the real value of this entity for a specific column. This implementation
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
