/*
 * Mars Simulation Project
 * RobotTableModel.java
 * @date 2025-08-07
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.equipment.Battery;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.util.TaskManager;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.SystemCondition;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;

/**
 * The RobotTableModel maintains a list of Robot objects. By defaults the source
 * of the list is the Unit Manager. It maps key attributes of the Robot into
 * Columns.
 */
@SuppressWarnings("serial")
public class RobotTableModel extends EntityMonitorModel<Robot> {

	private static final String COUNTING_ROBOTS_KEY = "RobotTableModel.countingRobots";

	// Column indexes
	private static final int NAME = 0;
	private static final int TYPE = NAME+1;
	private static final int LOCATION = TYPE+1;
	private static final int SETTLEMENT = LOCATION+1;
	private static final int MODE = SETTLEMENT+1;
	private static final int HEALTH = MODE+1;
	private static final int BATTERY = HEALTH+1;
	private static final int BATTERY_TEMPERATURE = BATTERY+1;
	private static final int PERFORMANCE = BATTERY_TEMPERATURE+1;
	private static final int TASK = PERFORMANCE+1;
	private static final int MISSION_COL = TASK+1;

	private static final int COLUMNCOUNT = MISSION_COL+1;
	
	private static final String INOPERABLE = "Inoperable";
	private static final String OPERABLE = "Operable";

	
	private static final ColumnSpec[] COLUMNS;

	private static final Map<String, Integer> eventColumnMapping;

	/**
	 * The static initializer creates the name & type arrays.
	 */
	static {
		COLUMNS = new ColumnSpec[COLUMNCOUNT];
		COLUMNS[NAME] = new ColumnSpec(Msg.getString("entity.name"), String.class);
		COLUMNS[TYPE] = new ColumnSpec(Msg.getString("robot.type"), String.class);
		COLUMNS[SETTLEMENT] = new ColumnSpec(Msg.getString("settlement.singular"), String.class);
		COLUMNS[MODE] = new ColumnSpec(Msg.getString("robot.mode"), String.class);
		COLUMNS[HEALTH] = new ColumnSpec(Msg.getString("robot.health"), String.class);
		COLUMNS[BATTERY] = new ColumnSpec(Msg.getString("RobotTableModel.column.battery.percent"), String.class);
		COLUMNS[BATTERY_TEMPERATURE] = new ColumnSpec(Msg.getString("RobotTableModel.column.battery.temperature"), Double.class);
		COLUMNS[PERFORMANCE] = new ColumnSpec(Msg.getString("robot.performance"), String.class);
		COLUMNS[LOCATION] = new ColumnSpec(Msg.getString("RobotTableModel.column.location"), String.class);
		COLUMNS[MISSION_COL] = new ColumnSpec(Msg.getString("mission.singular"), String.class);
		COLUMNS[TASK] = new ColumnSpec(Msg.getString("task.singular"), String.class);

		eventColumnMapping = new HashMap<>();
		eventColumnMapping.put(EntityEventType.NAME_EVENT, NAME);
		eventColumnMapping.put(EntityEventType.COORDINATE_EVENT, LOCATION);
		eventColumnMapping.put(EntityEventType.STATUS_EVENT, MODE);
		eventColumnMapping.put(Battery.BATTERY_EVENT, BATTERY);
		eventColumnMapping.put(SystemCondition.PERFORMANCE_EVENT, PERFORMANCE);
		eventColumnMapping.put(TaskManager.TASK_EVENT, TASK);
		eventColumnMapping.put(EntityEventType.TASK_NAME_EVENT, TASK);
		eventColumnMapping.put(EntityEventType.TASK_ENDED_EVENT, TASK);
		eventColumnMapping.put(EntityEventType.TASK_SUBTASK_EVENT, TASK);
		eventColumnMapping.put(EntityEventType.MISSION_EVENT, MISSION_COL);
		eventColumnMapping.put(EntityEventType.DEATH_EVENT, HEALTH);
	}

	private EntityListener settlementListener;


	/**
	 * Constructs a RobotTableModel that displays all associated
	 * robots with a specified settlement.
	 *
	 * @param settlement    the settlement to check.

	 */
	public RobotTableModel() {
		super (Msg.getString("RobotTableModel.nameAssociatedRobots"),
				COUNTING_ROBOTS_KEY, COLUMNS);

		setSettlementColumn(SETTLEMENT);
	}

	
	/**
	 * Sets the settlement filter for the Robot table.
	 * 
	 * @param filter
	 */
	@Override
	protected boolean applySettlementFilter(Set<Settlement> filter) {
		if (settlementListener != null) {
			getSelectedSettlements().forEach(s -> s.removeEntityListener(settlementListener));
		}
		
		List<Robot> entities = filter.stream()
						.map(Settlement::getAllAssociatedRobots)
						.flatMap(Collection::stream)
						.toList();
		settlementListener = new RobotChangeListener(EntityEventType.ADD_ASSOCIATED_ROBOT_EVENT,
														EntityEventType.REMOVE_ASSOCIATED_ROBOT_EVENT);
		
		if (entities != null && !entities.isEmpty()) {	
			resetItems(entities);
		}
		// Listen to the settlements for new robots
		filter.forEach(s -> s.addEntityListener(settlementListener));

		return true;
	}

	/**
	 * Catches unit update event.
	 *
	 * @param event the unit event.
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		
		Integer column = eventColumnMapping.get(event.getType());
			
		if (column != null && (column > -1) && event.getSource() instanceof Robot r) {
			entityValueUpdated(r, column, column);
		}	
	}

	/**
	 * Returns the value of a Cell.
	 *
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	@Override
	protected Object getItemValue(Robot robot, int columnIndex) {
		Object result = null;

		switch (columnIndex) {
			case NAME: 
				result = robot.getName();
				break;

			case TYPE: 
				result = robot.getModel(); // Model gives a better description
				break;

			case SETTLEMENT: 
				result = robot.getAssociatedSettlement().getName();
				break;

			case MODE: 
				result = robot.printStatusModes();
				break;
				
			case BATTERY: 
				result = robot.getSystemCondition().getBattery().getBatteryStatus().getName();
				break;
		
			case BATTERY_TEMPERATURE:
				result = Math.round(robot.getSystemCondition().getBattery().getInternalTemperature() * 10.0) / 10.0;
				break;
				
			case HEALTH: 
				if (!robot.isOperable())
					result = INOPERABLE;
				else
					result = OPERABLE;
				break;

			case PERFORMANCE:
				result = robot.getSystemCondition().getPerformanceLevel().getName();
				break;

			case LOCATION: 
				result = robot.getLocationTag().getImmediateLocation();
				break;

			case TASK: 
				// If the Robot is dead, there is no Task Manager
				TaskManager mgr = robot.getBotMind().getBotTaskManager();
				result = ((mgr != null) ? mgr.getTaskDescription(false) : null);
				break;

			case MISSION_COL: 
				Mission m = robot.getBotMind().getMission();
				if (m != null) {
					result = m.getName();
				}
				break;
		
			default:
				break;
		}

		return result;
	}

	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void destroy() {
		super.destroy();

		getSelectedSettlements().forEach(s -> s.removeEntityListener(settlementListener));
	}	

	/**
	 * EntityListener inner class for events where a Robot joins or leaves a Unit.
	 */
	private class RobotChangeListener implements EntityListener {

		private String addEvent;
		private String removeEvent;

		public RobotChangeListener(String addEvent, String removeEvent) {
			this.addEvent = addEvent;
			this.removeEvent = removeEvent;
		}

		/**
		 * Catches unit update event.
		 *
		 * @param event the unit event.
		 */
		public void entityUpdate(EntityEvent event) {
			if (event.getTarget() instanceof Robot r) {
				String eventType = event.getType();
				if (addEvent.equals(eventType)) {
					addItem(r);
				}
				else if (removeEvent.equals(eventType)) {
					removeItem(r);
				}
			}
		}
	}
}
