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
import java.util.Collection;
import java.util.Iterator;
import javax.swing.table.AbstractTableModel;

/**
 * The UnitTableModel that maintains a table model of Units objects.
 * It is only a partial implementation of the TableModel interface.
 */
abstract public class UnitTableModel extends AbstractTableModel {

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
    public UnitTableModel(String name, String names[], Class types[]) {

        // Initialize data members
        this.name = name;
        this.units = new ArrayList();
        this.columnNames = names;
        this.columnTypes = types;
    }

    /**
     * This method provideds a means to load the model with all the relevant
     * matching Units of the physical model. It should be overridden by the
     * subclasses to implement the required criteria.
     */
    public abstract void addAll();

    /**
     * Add the Units specified in the array to the current model.
     * @param units Units to add.
     */
    public void add(Iterator i) {
        while (i.hasNext()) {
            add((UnitUIProxy) i.next());
        }
    }

    /**
     * Add a unit to the model.
     * @param newUnit Unit to add to the model.
     */
    public void add(UnitUIProxy newUnit) {
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
    public UnitUIProxy getUnit(int index) {
        return (UnitUIProxy)units.get(index);
    }

    /**
     * Get the units at the specified rows
     * @param rows Indexes of Unit to retrieve.
     * @return Units at specified position.
     */
    public ArrayList getUnits(int rows[]) {
        ArrayList unitRows = new ArrayList();
        for(int i = 0; i < rows.length; i++) {
            unitRows.add(units.get(rows[i]));
        }
        return unitRows;
    }

    /**
     * Remove the units at the specified positions
     * @param rows Units to be deleted.
     */
    public void remove(Collection unitRows) {

        // Remove the rows from the model
        units.removeAll(unitRows);
        fireTableDataChanged();
    }

    /**
     * Remote a unit from the model
     * @param oldUnit Unit to remove.
     */
    public void remove(Unit oldUnit) {
        int oldIndex = units.indexOf(oldUnit);
        if (oldIndex >= 0) {
            units.remove(oldIndex);
            fireTableRowsDeleted(oldIndex, oldIndex);
        }
    }

    /**
     * Update the model contents. Ideally this should not be needed as the
     * model should be event driven and always know about the current
     * Unit state.
     */
    public void update() {
        // Just signal that all the cells have changed, this will refresh
        // displated cells.
        fireTableRowsUpdated(0, units.size());
    }
}
