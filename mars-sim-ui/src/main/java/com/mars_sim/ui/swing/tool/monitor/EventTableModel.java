/*
 * Mars Simulation Project
 * EventTableModel.java
 * @date 2025-10-16
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventCategory;
import com.mars_sim.core.events.HistoricalEventListener;
import com.mars_sim.core.events.HistoricalEventManager;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.utils.ColumnSpec;

/**
 * This class provides a table model for use with the MonitorWindow that
 * provides a mean to display the Historical Event. This is actually an Adapter
 * onto the existing Event Manager.
 */
@SuppressWarnings("serial")
public class EventTableModel extends EntityTableModel<HistoricalEvent> implements HistoricalEventListener{

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

	private HistoricalEventManager eventManager;
	private Set<HistoricalEventCategory> blockedTypes = new HashSet<>();
	private Set<String> settlementNames = Collections.emptySet();

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
		
		// Add listener only when fully constructed
		eventManager.addListener(this);

		setSettlementColumn(SETTLEMENT);
	}

	/**
	 * Sets the settlement filter.
	 */
	@Override
	public boolean setSettlementFilter(Set<Settlement> settlements) {

		settlementNames = settlements.stream()
				.map(Settlement::getName)
				.collect(Collectors.toSet());
		
		reloadEvents();
		return true;
	}

	/**
	 * Reloads the events from the event manager based on the current filters.
	 */
	private void reloadEvents() {
		Collection<HistoricalEvent> events = eventManager.getEvents().stream()
				.filter(this::isDisplayable)
				.toList();
	
		resetEntities(events);		
	}

	/**
	 * No implementation is needed.
	 * 
	 * @param activate Not used
	 */
	@Override
	public void setMonitorEntites(boolean activate) {
		// No need of monitoring any events. As each event is generated, it won't change.
	}

	/**
	 * Is the event displayable based on the current filters:
	 * 1) Home Town is in Settlement filter
	 * 2) Category is not blocked by user
	 * 3) Event Type is not blocked by system
	 * @param event
	 * @return
	 */
	private boolean isDisplayable(HistoricalEvent event) {
		if (!settlementNames.contains(event.getHomeTown())) {
			return false;
		}
		HistoricalEventCategory category = event.getCategory();
		EventType eventType = event.getType();
		return !blockedTypes.contains(category) && !BLOCKED_EVENTS.contains(eventType);
	}

	/**
	 * Returns the value of a Cell.
	 *
	 * @param event 
	 * @param columnIndex Column index of the cell.
	 */
	@Override
	protected Object getEntityValue(HistoricalEvent event, int column) {
		Object result = null;

		switch (column) {
		
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
				
			default:
		}

		return result;
	}

	/**
	 * New event has been added.
	 */
	public synchronized void eventAdded(HistoricalEvent event) {
		if (isDisplayable(event)) {
			addEntity(event);
		}
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
		reloadEvents();
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

		super.destroy();
	}

	/**
	 * Events have been removed from the system.
	 */
	@Override
	public void eventsRemoved(int startIndex, int endIndex) {
		reloadEvents();
	}
}
