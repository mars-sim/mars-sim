/**
 * Mars Simulation Project
 * EventTableModel.java
 * @version 2.75 2002-05-27
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard.monitor;

import org.mars_sim.msp.simulation.events.*;
import org.mars_sim.msp.simulation.MarsClock;
import org.mars_sim.msp.simulation.Unit;
import javax.swing.table.AbstractTableModel;

/**
 * This class provides a table model for use with the MonitorWindow that
 * provides a mean to display the Historical Event. This is actually an
 * Adapter onto the existing Event Manager.
 */

public class EventTableModel extends AbstractTableModel
            implements MonitorModel, HistoricalEventListener {

    private static final int TIMESTAMP = 0;
    private static final int TYPE = 1;
    private static final int ACTOR = 2;
    private static final int DESC = 3;
    private static final int COLUMNCOUNT = 4;

    static private String columnNames[];   // Names of the displayed columns
    static private Class  columnTypes[];   // Types of the individual columns

    static {
        columnNames = new String[COLUMNCOUNT];
        columnTypes = new Class[COLUMNCOUNT];
        columnNames[TIMESTAMP] = "Time";
        columnTypes[TIMESTAMP] = String.class;
        columnNames[TYPE] = "Event Type";
        columnTypes[TYPE] = String.class;
        columnNames[ACTOR] = "Actor";
        columnTypes[ACTOR] = Object.class;
        columnNames[DESC] = "Description";
        columnTypes[DESC] = String.class;
    }

    private HistoricalEventManager manager;

    /**
     * Create a new Event model based on the specified event manager.
     * @param manager Manager to extract events from.
     */
    public EventTableModel(HistoricalEventManager manager) {
        this.manager = manager;

        manager.addListener(this);
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
        return "Historical Events";
    }

    /**
     * Get the number of rows in the model.
     * @return the number of Events.
     */
    public int getRowCount() {
        return manager.size();
    }

    /**
     * Get the unit at the specified row.
     * @param row Indexes of Unit to retrieve.
     * @return Unit associated with the Event as the specified position.
     */
    public Object getObject(int row) {
        return manager.getEvent(row).getSource();
    }

    /**
     * Is this model already ordered according to some external criteria.
     * @return TRUE as the events are time ordered.
     */
    public boolean getOrdered() {
        return true;
    }

    /**
     * Return the value of a Cell
     * @param rowIndex Row index of the cell.
     * @param columnIndex Column index of the cell.
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = null;
        HistoricalEvent event = (HistoricalEvent)manager.getEvent(rowIndex);

        // Invoke the appropriate method, switch is the best solution
        // althought disliked by some
        switch (columnIndex) {
            case TIMESTAMP : {
                result = event.getTimestamp().getTimeStamp();
            } break;

            case ACTOR: {
                result = event.getSource();
            } break;

            case DESC : {
                result = event.getDescription();
            } break;

            case TYPE : {
                result = event.getType();
            } break;
        }

        return result;
    }

    /**
     * This table needs to perform no actions during the update.
     * @return A status string.
     */
    public String update() {
        return manager.size() + " events";
    }

    /**
     * A new event has been added at the specified manager.
     *
     * @param index Index of new event in the manager.
     * @param event The new event added.
     */
    public void eventAdded(int index, HistoricalEvent event) {
        fireTableRowsInserted(index, index);
    }

    /**
     * A consequective sequence of events have been removed from the manager.
     *
     * @param startIndex First exclusive index of the event to be removed.
     * @param endIndex Last exclusive index of the event to be removed..
     */
    public void eventsRemoved(int startIndex, int endIndex) {
        fireTableRowsDeleted(startIndex, endIndex);
    }
}