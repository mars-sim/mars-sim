/**
 * Mars Simulation Project
 * UnitTableModel.java
 * @version 2.73 2001-11-25
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard.monitor;

import org.mars_sim.msp.simulation.*;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * The UnitTableModel that maintains a table model of Units objects.
 * It is only a partial implementation of the TableModel interface.
 */
abstract public class UnitTableModel extends AbstractTableModel
            implements MonitorModel, MspCollectionEventListener {

    // Data members
    private ArrayList units;        // Collection of units
    private String name;            // Model name
    private String statusSuffix;    // Suffix to added to status message
    private String columnNames[];   // Names of the displayed columns
    private Class  columnTypes[];   // Types of the individual columns

    /**
     * Constructs a UnitTableModel object.
     *
     *  @param name Name of the model.
     *  @param suffix A string to add to the status message.
     *  @param names Names of the columns displayed.
     *  @param types The Classes of the individual columns.
     */
    protected UnitTableModel(String name, String suffix,
                             String names[], Class types[]) {

        // Initialize data members
        this.name = name;
        this.statusSuffix = suffix;
        this.units = new ArrayList();
        this.columnNames = names;
        this.columnTypes = types;
    }

    /**
     * Add a unit to the model.
     * @param newUnit Unit to add to the model.
     */
    protected void add(Unit newUnit) {
        if (!units.contains(newUnit)) {
            int size = units.size();
            units.add(newUnit);

            // Inform listeners of new row
            fireTableRowsInserted(size, size);
        }
    }

    /**
     * Remove a unit to the model.
     * @param oldUnit Unit to remove from the model.
     */
    protected void remove(Unit oldUnit) {
        if (units.contains(oldUnit)) {
            int index = units.indexOf(oldUnit);
            units.add(oldUnit);

            // Inform listeners of new row
            fireTableRowsDeleted(index, index);
        }
    }
    /**
     * Source collection has changed
     */
    public void collectionModified(MspCollectionEvent event) {

        if (event.getType().equals(MspCollectionEvent.CLEAR)) {
            units.clear();
            fireTableDataChanged();
        }
        else if (event.getType().equals(MspCollectionEvent.ADD)) {
            add(event.getTrigger());
        }
        else if (event.getType().equals(MspCollectionEvent.REMOVE)) {
            remove(event.getTrigger());
        }
    }

    /**
     * Return the number of columns
     * @return column count.
     */
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Return the type of the column requested.
     * @param columnIndex Index of column.
     * @return Class of specified column.
     */
    public Class getColumnClass(int columnIndex) {
        if ((columnIndex >= 0) && (columnIndex < columnTypes.length)) {
            return columnTypes[columnIndex];
        }
        return Object.class;
    }

    /**
     * Return the name of the column requested.
     * @param columnIndex Index of column.
     * @return name of specified column.
     */
    public String getColumnName(int columnIndex) {
        if ((columnIndex >= 0) && (columnIndex < columnNames.length)) {
            return columnNames[columnIndex];
        }
        return "Unknown";
    }


    /**
     * Get the name of the model.
     * @return model name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the number of rows in the model.
     * @return the number of Units.
     */
    public int getRowCount() {
        return units.size();
    }

    /**
     * Is this model already ordered according to some external criteria.
     * @return FALSE as the Units have no natural order.
     */
    public boolean getOrdered() {
        return false;
    }

    /**
     * Get the unit at the specified row.
     * @param index Index of the row.
     * @return Unit matching row
     */
    protected Unit getUnit(int index) {
        return (Unit)units.get(index);
    }

    /**
     * Get the unit at the specified row.
     * @param row Indexes of Unit to retrieve.
     * @return Unit at specified position.
     */
    public Object getObject(int row) {
        return units.get(row);
    }

    /**
     * Compare the current contents to the expected.
     *
     * @param contents The contents of this model.
     */
    protected void checkContents(List contents) {
    }

    /**
     * Update the model contents. Ideally this should not be needed as the
     * model should be event driven and always know about the current
     * Unit state.
     *
     * It also check whether the contents have changed.
     * @return a Status string.
     */
    public String update() {
        // Check the contents first
        checkContents(units);

        // Just signal that all the cells have changed, this will refresh
        // displated cells.
        if (units.size() > 0) {
            fireTableRowsUpdated(0, units.size() - 1);
        }

        return units.size() + statusSuffix;
    }
}
