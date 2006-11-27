/**
 * Mars Simulation Project
 * EventTableModel.java
 * @version 2.76 2004-07-08
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.standard.tool.monitor;

import java.util.*;
import javax.swing.table.AbstractTableModel;
import org.mars_sim.msp.simulation.events.*;

/**
 * This class provides a table model for use with the MonitorWindow that
 * provides a mean to display the Historical Event. This is actually an
 * Adapter onto the existing Event Manager.
 */
public class EventTableModel extends AbstractTableModel
            implements MonitorModel, HistoricalEventListener {

	// Column names
    private static final int TIMESTAMP = 0;
    private static final int CATEGORY = 1;
    private static final int TYPE = 2;
    private static final int ACTOR = 3;
    private static final int DESC = 4;
    private static final int COLUMNCOUNT = 5;

    static private String columnNames[];   // Names of the displayed columns
    static private Class  columnTypes[];   // Types of the individual columns

    static {
        columnNames = new String[COLUMNCOUNT];
        columnTypes = new Class[COLUMNCOUNT];
        columnNames[TIMESTAMP] = "Time";
        columnTypes[TIMESTAMP] = String.class;
        columnNames[CATEGORY] = "Category";
        columnTypes[CATEGORY] = String.class;
        columnNames[TYPE] = "Event Type";
        columnTypes[TYPE] = String.class;
        columnNames[ACTOR] = "Actor";
        columnTypes[ACTOR] = Object.class;
        columnNames[DESC] = "Description";
        columnTypes[DESC] = String.class;
    }

    private HistoricalEventManager manager;
    private List cachedEvents = new ArrayList();
    
    // Event categories to be displayed.
    private boolean displayMalfunction = true;
    private boolean displayMedical = true;
    private boolean displayMission = false;
    private boolean displayTask = false;
    private boolean displaySupply = false;

    /**
     * Create a new Event model based on the specified event manager.
     * @param manager Manager to extract events from.
     */
    public EventTableModel(HistoricalEventManager manager) {
        this.manager = manager;

		// Update the cached events.
		updateCachedEvents();

		// Add this model as an event listener.
        manager.addListener(this);
    }

	private void updateCachedEvents() {
		
		// Clean out cached events.
		cachedEvents = new ArrayList();
		
		// Filter events based on category.
		for (int x = 0; x < manager.size(); x++) {
			HistoricalEvent event = manager.getEvent(x);
			String category = event.getCategory();
			
			if (category.equals(HistoricalEventManager.MALFUNCTION) && displayMalfunction)
				cachedEvents.add(event);
				
			if (category.equals(HistoricalEventManager.MEDICAL) && displayMedical)
				cachedEvents.add(event);
				
			if (category.equals(HistoricalEventManager.MISSION) && displayMission)
				cachedEvents.add(event);
				
			if (category.equals(HistoricalEventManager.TASK) && displayTask)
				cachedEvents.add(event);
				
			if (category.equals(HistoricalEventManager.SUPPLY) && displaySupply)
				cachedEvents.add(event);
		}
		
		// Update all table listeners.
		fireTableDataChanged();
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
        return cachedEvents.size();
    }

    /**
     * Get the unit at the specified row.
     * @param row Indexes of Unit to retrieve.
     * @return Unit associated with the Event as the specified position.
     */
    public Object getObject(int row) {
    	HistoricalEvent event = (HistoricalEvent) cachedEvents.get(row);
    	Object result = null;
    	if (event != null) result = event.getSource();
    	return result;
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
        HistoricalEvent event = (HistoricalEvent) cachedEvents.get(rowIndex);

        // Invoke the appropriate method, switch is the best solution
        // althought disliked by some
        switch (columnIndex) {
            case TIMESTAMP : {
                result = event.getTimestamp().getTimeStamp();
            } break;

			case CATEGORY: {
				result = event.getCategory();
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
        return cachedEvents.size() + " events";
    }

    /**
     * A new event has been added at the specified manager.
     *
     * @param index Index of new event in the manager.
     * @param event The new event added.
     */
    public void eventAdded(int index, HistoricalEvent event) {
    	updateCachedEvents();
        // fireTableRowsInserted(index, index);
    }

    /**
     * A consequective sequence of events have been removed from the manager.
     *
     * @param startIndex First exclusive index of the event to be removed.
     * @param endIndex Last exclusive index of the event to be removed..
     */
    public void eventsRemoved(int startIndex, int endIndex) {
    	updateCachedEvents();
        // fireTableRowsDeleted(startIndex, endIndex);
    }
    
    /**
     * Checks if malfunction events are to be displayed.
     * @return true if displayed
     */
    public boolean getDisplayMalfunction() {
    	return displayMalfunction;
    }
    
    /**
     * Sets if malfunction events are to be displayed.
     * @param display true if displayed
     */
    public void setDisplayMalfunction(boolean display) {
    	displayMalfunction = display;
		updateCachedEvents();
    }
    
    /**
     * Checks if medical events are to be displayed.
     * @return true if displayed
     */
    public boolean getDisplayMedical() {
    	return displayMedical;
    }
    
    /**
     * Sets if medical events are to be displayed.
     * @param display true if displayed
     */
    public void setDisplayMedical(boolean display) {
    	displayMedical = display;
		updateCachedEvents();
    }
    
    /**
     * Checks if mission events are to be displayed.
     * @return true if displayed
     */
    public boolean getDisplayMission() {
    	return displayMission;
    }
    
    /**
     * Sets if mission events are to be displayed.
     * @param display true if displayed
     */
    public void setDisplayMission(boolean display) {
    	displayMission = display;
		updateCachedEvents();
    }
    
    /**
     * Checks if task events are to be displayed.
     * @return true if displayed
     */
    public boolean getDisplayTask() {
    	return displayTask;
    }
    
    /**
     * Sets if task events are to be displayed.
     * @param display true if displayed
     */
    public void setDisplayTask(boolean display) {
    	displayTask = display;
    	updateCachedEvents();
    }
    
    /**
     * Checks if supply events are to be displayed.
     * @return true if displayed
     */
    public boolean getDisplaySupply() {
    	return displaySupply;
    }
    
    /**
     * Sets if supply events are to be displayed.
     * @param display true if displayed
     */
    public void setDisplaySupply(boolean display) {
    	displaySupply = display;
    	updateCachedEvents();
    }
    
    /**
     * Prepares the model for deletion.
     */
    public void destroy() {
    	manager.removeListener(this);
    	manager = null;
    	cachedEvents = null;
    }
}