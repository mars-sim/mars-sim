/*
 * Mars Simulation Project
 * EventTableModel.java
 * @date 2022-09-24
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import com.mars_sim.core.Entity;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventCategory;
import com.mars_sim.core.events.HistoricalEventListener;
import com.mars_sim.core.events.HistoricalEventManager;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.utils.ColumnSpec;

/**
 * This class provides a table model for use with the MonitorWindow that
 * provides a mean to display the Historical Event. This is actually an Adapter
 * onto the existing Event Manager.
 */
@SuppressWarnings("serial")
public class EventTableModel extends AbstractMonitorModel implements HistoricalEventListener{

	// Column names
	private static final int TIMESTAMP = 0;
	private static final int CATEGORY = 1;
	private static final int TYPE = 2;
	private static final int CAUSE = 3;
	private static final int WHILE = 4;
	private static final int WHO = 5;
	private static final int ENTITY = 6;
	private static final int SETTLEMENT = 7;
	private static final int COORDINATES = 8;
	
	private static final int COLUMNCOUNT = 9;

	// Event that are too low level to display
	private static final Set<EventType> BLOCKED_EVENTS = Set.of(
//			EventType.MEDICAL_STARTS,
//			EventType.MEDICAL_TREATED,
//			EventType.MEDICAL_DEATH,
//			EventType.MEDICAL_POSTMORTEM_EXAM,
			EventType.MISSION_EMERGENCY_BEACON_ON,
			EventType.MISSION_EMERGENCY_BEACON_OFF,
			EventType.MISSION_EMERGENCY_DESTINATION,
			EventType.MISSION_NOT_ENOUGH_RESOURCES,
			EventType.MISSION_MEDICAL_EMERGENCY,
			EventType.MISSION_RENDEZVOUS,
			EventType.MISSION_RESCUE_PERSON,
			EventType.MISSION_SALVAGE_VEHICLE);

	/** Names of the displayed columns. */
	private static final ColumnSpec[] COLUMNS;

	static {
		COLUMNS = new ColumnSpec[COLUMNCOUNT];
		COLUMNS[TIMESTAMP] = new ColumnSpec(Msg.getString("EventTableModel.column.time"), MarsTime.class);
		COLUMNS[CATEGORY] = new ColumnSpec(Msg.getString("EventTableModel.column.category"),String.class);
		COLUMNS[TYPE] = new ColumnSpec(Msg.getString("EventTableModel.column.eventType"), String.class);	
		COLUMNS[CAUSE] = new ColumnSpec(Msg.getString("EventTableModel.column.cause"), String.class);
		COLUMNS[WHILE] = new ColumnSpec(Msg.getString("EventTableModel.column.while"),String.class);
		COLUMNS[WHO] = new ColumnSpec(Msg.getString("EventTableModel.column.who"), Object.class);
		COLUMNS[ENTITY] = new ColumnSpec(Msg.getString("EventTableModel.column.entity"), String.class);
		COLUMNS[SETTLEMENT] = new ColumnSpec(Msg.getString("EventTableModel.column.hometown"), String.class);
		COLUMNS[COORDINATES] = new ColumnSpec(Msg.getString("EventTableModel.column.coordinates"), String.class);
	}

	private transient List<HistoricalEvent> cachedEvents = new ArrayList<>();
	private HistoricalEventManager eventManager;
	private Set<HistoricalEventCategory> blockedTypes = new HashSet<>();

	/**
	 * Constructor. Create a new Event model based on the specified event manager.
	 *
	 * @param manager   Manager to extract events from.
	 * @param notifyBox to present notification message to user.
	 */
	public EventTableModel(MainDesktopPane desktop) {
		super(Msg.getString("EventTableModel.tabName"), "EventTableModel.numberOfEvents", COLUMNS);

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
	public boolean setSettlementFilter(Set<Settlement> filter) {
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
		SwingUtilities.invokeLater(this::fireTableDataChanged);

	}

	private boolean isDisplayable(HistoricalEvent event) {
		HistoricalEventCategory category = event.getCategory();
		EventType eventType = event.getType();
		return !blockedTypes.contains(category) && !BLOCKED_EVENTS.contains(eventType);
	}


	/**
	 * Gets the number of rows in the model.
	 *
	 * @return the number of Events.
	 */
	@Override
	public int getRowCount() {
		if (cachedEvents != null)
			return cachedEvents.size();
		else
			return 0;
	}

	/**
	 * Gets the unit at the specified row.
	 *
	 * @param row Indexes of Unit to retrieve.
	 * @return Unit associated with the Event as the specified position.
	 */
	@Override
	public Object getObject(int row) {
		HistoricalEvent event = cachedEvents.get(row);
		Object result = event.getSource();
		if (!(result instanceof Entity)) {
			result = event.getEntity();
		}
		return result;
	}

	/**
	 * Returns the value of a Cell.
	 *
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	@Override
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

				case ENTITY: {
					var con = event.getEntity();
					result = (con != null ? con.getName() : null);
				}
					break;

				case SETTLEMENT: {
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
	 * New event has been added.
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
	 * Sets the category type to display.
	 * 
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
	 * 
	 * @param type
	 * @return
	 */
	public boolean isDisplayed(HistoricalEventCategory type) {
		return !blockedTypes.contains(type);
	}
	
	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void destroy() {
		eventManager.removeListener(this);
		eventManager = null;
		cachedEvents.clear();
		cachedEvents = null;

		super.destroy();
	}

	/**
	 * No implementation is needed.
	 * 
	 * @param activate Not used
	 */
	@Override
	public void setMonitorEntites(boolean activate) {
		// Do nothing in this method
	}
}
