/**
 * Mars Simulation Project
 * UnitTableModel.java
 * @version 2.72 2001-07-22
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.table.*;

/**
 * The UnitTableModel that maintains a table model of Units objects.
 * It is only a partial implementation of the TableModel interface.
 */
abstract public class UnitTableModel extends AbstractTableModel {

    // Data members
    private ArrayList units;        // Collection of units
    private String name;             // Model name


    /** Constructs a UnitTableModel object
     *  @param name Name of the model.
     */
    public UnitTableModel(String name) {

        // Initialize data members
        this.name = name;
        this.units = new ArrayList();
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
    public void add(UnitUIProxy units[]) {
        for(int i = 0; i < units.length; i++) {
           add(units[i]);
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
