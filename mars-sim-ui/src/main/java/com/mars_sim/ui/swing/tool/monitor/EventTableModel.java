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

import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventCategory;
import com.mars_sim.core.events.HistoricalEventListener;
import com.mars_sim.core.events.HistoricalEventManager;
import com.mars_sim.core.events.HistoricalEventType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.utils.ColumnSpec;

/**
 * This class provides a table model for use with the MonitorWindow that
 * provides a mean to display the Historical Event. This is actually an Adapter
 * onto the existing Event Manager.
 */
@SuppressWarnings("serial")
class EventTableModel extends CachingTableModel<HistoricalEvent> implements HistoricalEventListener{

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
	private static final Set<HistoricalEventType> BLOCKED_EVENTS = Set.of(
			HistoricalEventType.MISSION_EMERGENCY_BEACON_OFF,
			HistoricalEventType.MISSION_EMERGENCY_DESTINATION,
			HistoricalEventType.MISSION_NOT_ENOUGH_RESOURCES,
			HistoricalEventType.MISSION_MEDICAL_EMERGENCY,
			HistoricalEventType.MISSION_RENDEZVOUS,
			HistoricalEventType.MISSION_RESCUE_PERSON,
			HistoricalEventType.MISSION_SALVAGE_VEHICLE);

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
	private Set<Settlement> settlements = Collections.emptySet();

	/**
	 * Constructor. Create a new Event model based on the specified event manager.
	 *
	 * @param manager   Manager to extract events from.
	 * @param notifyBox to present notification message to user.
	 */
	public EventTableModel(HistoricalEventManager manager) {
		super(Msg.getString("EventTableModel.tabName"), "EventTableModel.numberOfEvents", COLUMNS);

		// Add this model as an event listener.
		this.eventManager = manager;
		
		// Add listener only when fully constructed
		eventManager.addListener(this);

		setSettlementColumn(SETTLEMENT);
	}

	/**
	 * Sets the settlement filter.
	 */
	@Override
	public boolean setSettlementFilter(Set<Settlement> settlements) {

		this.settlements = settlements;
		
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
	
		resetItems(events);		
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
		if (!settlements.contains(event.getHomeTown())) {
			return false;
		}
		HistoricalEventCategory category = event.getCategory();
		HistoricalEventType eventType = event.getType();
		return !blockedTypes.contains(category) && !BLOCKED_EVENTS.contains(eventType);
	}

	/**
	 * Returns the value of a Cell.
	 *
	 * @param event 
	 * @param columnIndex Column index of the cell.
	 */
	@Override
	protected Object getItemValue(HistoricalEvent event, int column) {
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
				var home = event.getHomeTown();
				result = home != null ? home.getName() : null;
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
	 * Gets the Entity associated to the event
	 *
	 * @param row Indexes of Unit to retrieve.
	 * @return Unit at specified position.
	 */
	@Override
	public Object getObject(int row) {
		return getItem(row).getEntity();
	}
	/**
	 * New event has been added.
	 */
	public synchronized void eventAdded(HistoricalEvent event) {
		if (isDisplayable(event)) {
			addItem(event);
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
