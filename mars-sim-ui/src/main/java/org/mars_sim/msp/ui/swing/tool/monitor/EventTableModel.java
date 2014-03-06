/**
 * Mars Simulation Project
 * EventTableModel.java
 * @version 3.06 2014-01-29
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventListener;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.events.HistoricalEventType;
import org.mars_sim.msp.core.structure.building.Building;

/**
 * This class provides a table model for use with the MonitorWindow that
 * provides a mean to display the Historical Event. This is actually an
 * Adapter onto the existing Event Manager.
 */
public class EventTableModel
extends AbstractTableModel
implements MonitorModel, HistoricalEventListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Column names
	private static final int TIMESTAMP = 0;
	private static final int CATEGORY = 1;
	private static final int TYPE = 2;
	private static final int ACTOR = 3;
	private static final int DESC = 4;
	private static final int COLUMNCOUNT = 5;

	/** Names of the displayed columns. */
	static private String columnNames[];
	/** Types of the individual columns. */
	static private Class<?> columnTypes[];

	static {
		columnNames = new String[COLUMNCOUNT];
		columnTypes = new Class[COLUMNCOUNT];
		columnNames[TIMESTAMP] = Msg.getString("EventTableModel.column.time");		columnTypes[TIMESTAMP] = String.class; //$NON-NLS-1$
		columnNames[CATEGORY] = Msg.getString("EventTableModel.column.category");	columnTypes[CATEGORY] = String.class; //$NON-NLS-1$
		columnNames[TYPE] = Msg.getString("EventTableModel.column.eventType");		columnTypes[TYPE] = String.class; //$NON-NLS-1$
		columnNames[ACTOR] = Msg.getString("EventTableModel.column.actor");			columnTypes[ACTOR] = Object.class; //$NON-NLS-1$
		columnNames[DESC] = Msg.getString("EventTableModel.column.description");	columnTypes[DESC] = String.class; //$NON-NLS-1$
	}

	private HistoricalEventManager manager;
	private List<HistoricalEvent> cachedEvents = new ArrayList<HistoricalEvent>();

	// Event categories to be displayed.
	private boolean displayMalfunction = true;
	private boolean displayMedical = true;
	private boolean displayMission = false;
	private boolean displayTask = false;
	private boolean displayTransport = false;

	/**
	 * constructor.
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
		cachedEvents = new ArrayList<HistoricalEvent>();

		// Filter events based on category.
		for (int x = 0; x < manager.size(); x++) {
			HistoricalEvent event = manager.getEvent(x);
			HistoricalEventType category = event.getCategory();

			if (category.equals(HistoricalEventType.MALFUNCTION) && displayMalfunction)
				cachedEvents.add(event);

			if (category.equals(HistoricalEventType.MEDICAL) && displayMedical)
				cachedEvents.add(event);

			if (category.equals(HistoricalEventType.MISSION) && displayMission)
				cachedEvents.add(event);

			if (category.equals(HistoricalEventType.TASK) && displayTask)
				cachedEvents.add(event);

			if (category.equals(HistoricalEventType.TRANSPORT) && displayTransport)
				cachedEvents.add(event);
		}

		// Update all table listeners.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fireTableDataChanged();
			}
		});
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
	public Class<?> getColumnClass(int columnIndex) {
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
		return Msg.getString("EventTableModel.unknown"); //$NON-NLS-1$
	}

	/**
	 * Get the name of the model.
	 * @return model name.
	 */
	public String getName() {
		return Msg.getString("EventTableModel.title"); //$NON-NLS-1$
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
		HistoricalEvent event = cachedEvents.get(row);
		Object result = null;
		if (event != null) {
			Object source = event.getSource();
			if (source instanceof Unit) result = source;
			else if (source instanceof Building) 
				result = ((Building) source).getBuildingManager().getSettlement();
		}
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

		if (rowIndex < cachedEvents.size()) {
			HistoricalEvent event = cachedEvents.get(rowIndex);

			if (event != null) {
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
			}
		}

		return result;
	}

	/**
	 * Gets the model count string.
	 */
	public String getCountString() {
		return Msg.getString(
			"EventTableModel.numberOfEvents", //$NON-NLS-1$
			Integer.toString(cachedEvents.size())
		);
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
	 * Checks if transport events are to be displayed.
	 * @return true if displayed
	 */
	public boolean getDisplayTransport() {
		return displayTransport;
	}

	/**
	 * Sets if transport events are to be displayed.
	 * @param display true if displayed
	 */
	public void setDisplayTransport(boolean display) {
		displayTransport = display;
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