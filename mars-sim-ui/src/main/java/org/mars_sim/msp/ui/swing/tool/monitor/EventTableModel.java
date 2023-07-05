/*
 * Mars Simulation Project
 * EventTableModel.java
 * @date 2022-09-24
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.Entity;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.events.HistoricalEventListener;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * This class provides a table model for use with the MonitorWindow that
 * provides a mean to display the Historical Event. This is actually an Adapter
 * onto the existing Event Manager.
 */
@SuppressWarnings("serial")
public class EventTableModel extends AbstractTableModel implements MonitorModel, HistoricalEventListener{

	// Column names
	private static final int TIMESTAMP = 0;
	private static final int CATEGORY = 1;
	private static final int TYPE = 2;
	private static final int CAUSE = 3;
	private static final int WHILE = 4;
	private static final int WHO = 5;
	private static final int CONTAINER = 6;
	private static final int HOMETOWN = 7;
	private static final int COORDINATES = 8;
	
	private static final int COLUMNCOUNT = 9;

	// Event that are too low level to display
	private static final Set<EventType> BLOCKED_EVENTS = Set.of(EventType.MEDICAL_STARTS,
																EventType.MEDICAL_TREATED,
																EventType.MEDICAL_DEATH,
																EventType.MISSION_EMERGENCY_BEACON_ON,
																EventType.MISSION_EMERGENCY_BEACON_OFF,
																EventType.MISSION_EMERGENCY_DESTINATION,
																EventType.MISSION_NOT_ENOUGH_RESOURCES,
																EventType.MISSION_MEDICAL_EMERGENCY,
																EventType.MISSION_RENDEZVOUS,
																EventType.MISSION_RESCUE_PERSON,
																EventType.MISSION_SALVAGE_VEHICLE);

	/** Names of the displayed columns. */
	static private String columnNames[];
	/** Types of the individual columns. */
	static private Class<?> columnTypes[];

	static {
		columnNames = new String[COLUMNCOUNT];
		columnTypes = new Class[COLUMNCOUNT];
		columnNames[TIMESTAMP] = Msg.getString("EventTableModel.column.time"); //$NON-NLS-1$
		columnTypes[TIMESTAMP] = String.class;
		columnNames[CATEGORY] = Msg.getString("EventTableModel.column.category"); //$NON-NLS-1$
		columnTypes[CATEGORY] = String.class;
		columnNames[TYPE] = Msg.getString("EventTableModel.column.eventType"); //$NON-NLS-1$
		columnTypes[TYPE] = String.class;	
		columnNames[CAUSE] = Msg.getString("EventTableModel.column.cause"); //$NON-NLS-1$
		columnTypes[CAUSE] = String.class;
		columnNames[WHILE] = Msg.getString("EventTableModel.column.while"); //$NON-NLS-1$
		columnTypes[WHILE] = String.class;
		columnNames[WHO] = Msg.getString("EventTableModel.column.who"); //$NON-NLS-1$
		columnTypes[WHO] = Object.class;
		columnNames[CONTAINER] = Msg.getString("EventTableModel.column.container"); //$NON-NLS-1$
		columnTypes[CONTAINER] = String.class;
		columnNames[HOMETOWN] = Msg.getString("EventTableModel.column.hometown"); //$NON-NLS-1$
		columnTypes[HOMETOWN] = String.class;
		columnNames[COORDINATES] = Msg.getString("EventTableModel.column.coordinates"); //$NON-NLS-1$
		columnTypes[COORDINATES] = String.class;
	}

	private transient List<HistoricalEvent> cachedEvents = new ArrayList<>();
	private HistoricalEventManager eventManager;
	private Set<HistoricalEventCategory> blockedTypes = new HashSet<>();

	/**
	 * constructor. Create a new Event model based on the specified event manager.
	 *
	 * @param manager   Manager to extract events from.
	 * @param notifyBox to present notification message to user.
	 */
	public EventTableModel(MainDesktopPane desktop) {

		// Add this model as an event listener.
		this.eventManager = desktop.getSimulation().getEventManager();
		
		 blockedTypes.add(HistoricalEventCategory.TASK);
		 blockedTypes.add(HistoricalEventCategory.TRANSPORT);

		// Update the cached events.
		updateCachedEvents();
		
		// Add listener only when fully constructed
		eventManager.addListener(this);
	}

	@Override
	public boolean setSettlementFilter(Settlement filter) {
		// Events do not support filtering
		return false;
	}

	private synchronized void updateCachedEvents() {

		// Clean out existing cached events for the Event Table.
		cachedEvents = new ArrayList<>();

		List<HistoricalEvent> events = new ArrayList<>(eventManager.getEvents());

		for (HistoricalEvent event : events) {
			if (isDisplayable(event)) {
				cachedEvents.add(event);
			}

		}

		// Update all table listeners.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fireTableDataChanged();
			}
		});

	}

	private boolean isDisplayable(HistoricalEvent event) {
		HistoricalEventCategory category = event.getCategory();
		EventType eventType = event.getType();
		return !blockedTypes.contains(category) && !BLOCKED_EVENTS.contains(eventType);
	}

	/**
	 * Return the number of columns
	 *
	 * @return column count.
	 */
	public int getColumnCount() {
		return columnNames.length;
	}

	/**
	 * Return the type of the column requested.
	 *
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
	 *
	 * @param columnIndex Index of column.
	 * @return name of specified column.
	 */
	public String getColumnName(int columnIndex) {
		if ((columnIndex >= 0) && (columnIndex < columnNames.length)) {
			return columnNames[columnIndex];
		}
		return Msg.getString("unknown"); //$NON-NLS-1$
	}

	/**
	 * Get the name of the model.
	 *
	 * @return model name.
	 */
	public String getName() {
		return Msg.getString("EventTableModel.tabName"); //$NON-NLS-1$
	}

	/**
	 * Get the number of rows in the model.
	 *
	 * @return the number of Events.
	 */
	public int getRowCount() {
		if (cachedEvents != null)
			return cachedEvents.size();
		else
			return 0;
	}

	/**
	 * Get the unit at the specified row.
	 *
	 * @param row Indexes of Unit to retrieve.
	 * @return Unit associated with the Event as the specified position.
	 */
	public Object getObject(int row) {
		HistoricalEvent event = cachedEvents.get(row);
		Object result = event.getSource();
		if (!(result instanceof Entity)) {
			result = event.getContainer();
		}
		return result;
	}

	/**
	 * Is this model already ordered according to some external criteria.
	 *
	 * @return TRUE as the events are time ordered.
	 */
	public boolean getOrdered() {
		return true;
	}

	/**
	 * Return the value of a Cell
	 *
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = null;

		if (rowIndex < cachedEvents.size()) {
			HistoricalEvent event = cachedEvents.get(rowIndex);
			if (event != null) {
				switch (columnIndex) {
				
				case TIMESTAMP: {
					result = event.getTimestamp();
				}
					break;

				case CATEGORY: {
					result = event.getCategory().getName();
				}
					break;

				case TYPE: {
					result = event.getType().getName();
				}
					break;
					
				case CAUSE: {
					result = event.getWhatCause();
				}
					break;	

				case WHILE: {
					result = event.getWhileDoing();
				}
					break;

				case WHO: {
					result = event.getWho();
				}
					break;

				case CONTAINER: {
					result = event.getContainer();
				}
					break;

				case HOMETOWN: {
					result = event.getHomeTown();
				}
					break;
					

				case COORDINATES: {
					result = event.getCoordinates();
				}
					break;
					
				default: {
					result = null;
				}
					break;
	
				}
			} // end of if event
		}

		return result;
	}

	/**
	 * Gets the model count string.
	 */
	public String getCountString() {
		return "  " + Msg.getString("EventTableModel.numberOfEvents", //$NON-NLS-2$
				cachedEvents.size());
	}

	/**
	 * New event has been added
	 */
	public synchronized void eventAdded(HistoricalEvent event) {
		if (isDisplayable(event)) {
			cachedEvents.add(event);
			fireTableRowsInserted(cachedEvents.size()-1, cachedEvents.size()-1);
		}
	}

	/**
	 * A consecutive sequence of events have been removed from the manager.
	 *
	 * @param startIndex First exclusive index of the event to be removed.
	 * @param endIndex   Last exclusive index of the event to be removed..
	 */
	public void eventsRemoved(int startIndex, int endIndex) {
		updateCachedEvents();
	}

	/**
	 * Set the category type to display
	 * @param type
	 * @param isDisplayed
	 */
	public void setDisplayed(HistoricalEventCategory type, boolean isDisplayed) {
		if (isDisplayed) {
			blockedTypes.remove(type);
		}
		else {
			blockedTypes.add(type);
		}
		updateCachedEvents();
	}

	/**
	 * Is a category event being displayed?
	 * @param type
	 * @return
	 */
	public boolean isDisplayed(HistoricalEventCategory type) {
		return !blockedTypes.contains(type);
	}
	
	/**
	 * Prepares the model for deletion.
	 */
	public void destroy() {
		eventManager.removeListener(this);
		eventManager = null;
		cachedEvents.clear();
		cachedEvents = null;
	}

	@Override
	public void setMonitorEntites(boolean activate) {
		// TODO Auto-generated method stub
		
	}

}
