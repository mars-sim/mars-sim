/**
 * Mars Simulation Project
 * UnitTableModel.java
 * @version 2.73 2001-11-25
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard.monitor;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.ui.standard.UnitUIProxy;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * The UnitTableModel that maintains a table model of Units objects.
 * It is only a partial implementation of the TableModel interface.
 */
abstract public class UnitTableModel extends AbstractTableModel
            implements MonitorModel {

    // Data members
    private ArrayList units;        // Collection of units
    private String name;            // Model name
    private String columnNames[];   // Names of the displayed columns
    private Class  columnTypes[];   // Types of the individual columns

    /**
     * Constructs a UnitTableModel object.
     *
     *  @param name Name of the model.
     *  @param names Names of the columns displayed.
     *  @param types The Classes of the individual columns.
     */
    protected UnitTableModel(String name, String names[], Class types[]) {

        // Initialize data members
        this.name = name;
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
     * It also check whether the contents have changed
     */
    public void update() {
        // Check the contents first
        checkContents(units);

        // Just signal that all the cells have changed, this will refresh
        // displated cells.
        if (units.size() > 0) {
            fireTableRowsUpdated(0, units.size() - 1);
        }
    }
}
