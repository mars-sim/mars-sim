/**
 * Mars Simulation Project
 * TableSorter.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.structure.Settlement;

import java.util.Arrays;


/**
 * This TableModel acts as a proxy to provide sorting on a remote Table Model.
 * It is based on the TableSorter provided as part of the Swing UI Tutorial but
 * this version has been simplified as it assumes that only column types that
 * are Comparable will be sorted.
 * Also only one column can be used as a sorting key
 */
@SuppressWarnings("serial")
public class TableSorter extends AbstractTableModel
                    implements TableModelListener, MonitorModel {

    // Minimum time (milliseconds) between table sorts.
    private static final long SORTING_TIME_BUFFER = 500L;

    private int [] indexes;
    private boolean sortAscending = false;
    private int sortedColumn;
    private MonitorModel sourceModel;
    private long lastSortedTime;

    /**
     * Create a sorter model that provides sorting in front of the specified
     * model.
     * @param model Real source of data.
     */
    public TableSorter(MonitorModel model) {
        sourceModel = model;
        sourceModel.addTableModelListener(this);

        reallocateIndexes();
    }

    /**
     * This method signifies whether this model has a natural ordering.
     * @return TRUE as this model has embedded sorting.
     */
    public boolean getOrdered() {
        return true;
    }

    /**
     * Compare two rows according to their cell values
     */
    @SuppressWarnings("unchecked")
	private int compare(int row1, int row2) {
    	Comparable<Object> obj1 = (Comparable<Object>) sourceModel.getValueAt(row1, sortedColumn);
    	Object obj2 = sourceModel.getValueAt(row2, sortedColumn);
        int result = 0;
        if (obj1 == null) {
            result = (obj2 == null ? 0 : 1);
        }
        else if (obj2 == null) {
            result = -1;
        }
        else {
        	try {
        		result = obj1.compareTo(obj2);
        	}
        	catch (ClassCastException e) {}
        }
        if (result != 0) {
            return sortAscending ? result : -result;
        }
        return 0;
    }

    /**
     * Reset index row mappings
     */
    private void reallocateIndexes() {
        int rowCount = sourceModel.getRowCount();

        // Set up a new array of indexes with the right number of elements
        // for the new data model.
        indexes = new int[rowCount];

        // Initialise with the identity mapping.
        for (int row = 0; row < rowCount; row++) {
            indexes[row] = row;
        }
    }

    /**
     * Fired when there are changed to the source table. If any rows have been
     * deleted/inserted or a change to the sorted column, then re-sort
     */
    public void tableChanged(TableModelEvent e) {

        // boolean resort = false;
        TableModelEvent newEvent = null;
        int type = e.getType();

        // Determine the first and low rows.
        int firstRow = e.getFirstRow();
        int lastRow = e.getLastRow();
        if(indexes == null) return;
        for (int x = 0; x < indexes.length; x++) {
        	if (indexes[x] == e.getFirstRow()) firstRow = x;
        	if (indexes[x] == e.getLastRow()) lastRow = x;
        }

        // Decide whether to resort
        if ((type == TableModelEvent.DELETE) ||
            (type == TableModelEvent.INSERT)) {
            sortModel();
            newEvent = new TableModelEvent(this, firstRow, lastRow, e.getColumn(), e.getType());
        }
        else if ((e.getColumn() == sortedColumn) ||
                (e.getColumn() == TableModelEvent.ALL_COLUMNS)) {
            // Check time buffer for sorting.
            long currentTime = System.currentTimeMillis();
            long timeDiff = currentTime - lastSortedTime;
            if (timeDiff > SORTING_TIME_BUFFER) {

                // If the model has been resorted, flag all changes
                if (sortModel()) {
                    newEvent = new TableModelEvent(this, 0, sourceModel.getRowCount());
                }

                lastSortedTime = currentTime;
            }
        }

        // Fallback position, reconstruct new event by applying changes.
        if (newEvent == null) {
            if ((firstRow == -1) || (firstRow != lastRow)) {
                firstRow = 0;
                if (sourceModel != null) lastRow = sourceModel.getRowCount();
            }
            newEvent = new TableModelEvent(this, firstRow, lastRow, e.getColumn());
        }

        fireTableChanged(newEvent);
    }

    /**
     * Simple N2 sort
     */
    private void n2sort() {
        for (int i = 0; i < getRowCount(); i++) {
            for (int j = i+1; j < getRowCount(); j++) {
                if (compare(indexes[i], indexes[j]) < 0) {
                    swap(i, j);
                }
            }
        }
    }

    @Override
    public int getColumnCount() {
        return sourceModel.getColumnCount();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return sourceModel.getColumnClass(columnIndex);
    }

    @Override
    public String getColumnName(int columnIndex) {
        return sourceModel.getColumnName(columnIndex);
    }

    @Override
    public String getName() {
        if (sourceModel != null) return sourceModel.getName();
        else return "";
    }

    @Override
    public int getRowCount() {
        if (sourceModel != null) return sourceModel.getRowCount();
        else return 0;
    }

    /**
     * Return the cell value according to the ordered rows. This method will
     * remap the specified row according to the sorted mapping.
     * @param aRow Row offset.
     * @param aColumn Column offset.
     * @return Value of cell.
     */
    @Override
    public Object getValueAt(int aRow, int aColumn) {
    	if (indexes.length > aRow) {
    		return sourceModel.getValueAt(indexes[aRow], aColumn);
    	}
    	return null;
    }

    /**
     * Get a list of objects from the source model. The row indexes are remapped
     * according to the sorted mappings.
     *
     * @param row Indexes of rows in the sorted model.
     * @return List of objects.
     */
    public Object getObject(int row) {
    	if (row < indexes.length)
    		return sourceModel.getObject(indexes[row]);
    	else return null;
    }

    /**
     * The mapping only affects the contents of the data rows.
     * Pass all requests to these rows through the mapping array: "indexes".
     * @param aValue New value for cell.
     * @param aRow Row offset.
     * @param aColumn Column offset.
     */
    @Override
    public void setValueAt(Object aValue, int aRow, int aColumn) {
        sourceModel.setValueAt(aValue, indexes[aRow], aColumn);
    }

    /**
     * Gets the model count string.
     */
    @Override
    public String getCountString() {
    	return sourceModel.getCountString();
    }

    /**
     * Sort the table model by the column specified in an particular order.
     * If the specified column does not use a Comparable object then it
     * can not be sorted.
     *
     * @param column Column index of sorted column.
     * @param ascending Sort in the ascending order.
     */
    public void sortByColumn(int column, boolean ascending) {
        sortAscending = ascending;
        sortedColumn = column;

        boolean sorted = sortModel();
        if (sorted) {
            // Resorted without adding rows, i.e. updated
            fireTableRowsUpdated(0, sourceModel.getRowCount());
        }
    }

    /**
     * This method sorts the table according to the defined settings.
     * It recreates the row mappings.
     *
     * @return Return whether a reordering has been performed
     */
    private boolean sortModel() {
        int original[] = indexes.clone();
        reallocateIndexes();

        // Only do sorting if cells contains Comparable
        // Note: Number.class cannot be sorted
        // See https://stackoverflow.com/questions/480632/why-doesnt-java-lang-number-implement-comparable
        Class<?> cellType = sourceModel.getColumnClass(sortedColumn);
        if (Comparable.class.isAssignableFrom(cellType)) {

            // Different sorting methods
            n2sort();
            //shuttlesort(original, indexes, 0, indexes.length);
        }

        // Compare the results to see if anything has changed, aims to
        // minimise full refresh events
        return !Arrays.equals(original, indexes);
    }

    private void swap(int i, int j) {
        int tmp = indexes[i];
        indexes[i] = indexes[j];
        indexes[j] = tmp;
    }

    /**
     * Prepares the model for deletion.
     */
    @Override
    public void destroy() {
        // Do nothing
    }

    @Override
    public void setSettlementFilter(Settlement filter) {
        sourceModel.setSettlementFilter(filter); 
    }
}
