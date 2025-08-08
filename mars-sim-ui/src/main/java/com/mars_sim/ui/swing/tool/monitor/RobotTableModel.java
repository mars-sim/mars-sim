/*
 * Mars Simulation Project
 * RobotTableModel.java
 * @date 2025-08-07
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitEvent;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitListener;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionEvent;
import com.mars_sim.core.person.ai.mission.MissionEventType;
import com.mars_sim.core.person.ai.mission.MissionListener;
import com.mars_sim.core.person.ai.task.util.TaskManager;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.ui.swing.utils.ColumnSpec;

/**
 * The RobotTableModel maintains a list of Robot objects. By defaults the source
 * of the list is the Unit Manager. It maps key attributes of the Robot into
 * Columns.
 */
@SuppressWarnings("serial")
public class RobotTableModel extends UnitTableModel<Robot> {

	private static final String COUNTING_ROBOTS_KEY = "RobotTableModel.countingRobots";
	private static final String NAME_ROBOTS_KEY = "RobotTableModel.nameRobots";

	// Column indexes
	private static final int NAME = 0;
	private static final int TYPE = NAME+1;
	private static final int LOCATION = TYPE+1;
	private static final int SETTLEMENT = LOCATION+1;
	private static final int MODE = SETTLEMENT+1;
	private static final int HEALTH = MODE+1;
	private static final int BATTERY = HEALTH+1;
	private static final int PERFORMANCE = BATTERY+1;
	private static final int JOB = PERFORMANCE+1;
	private static final int TASK = JOB+1;
	private static final int MISSION_COL = TASK+1;

	private static final int COLUMNCOUNT = MISSION_COL+1;
	private static final ColumnSpec[] COLUMNS;

	private static final Map<UnitEventType, Integer> eventColumnMapping;

	/**
	 * The static initializer creates the name & type arrays.
	 */
	static {
		COLUMNS = new ColumnSpec[COLUMNCOUNT];
		COLUMNS[NAME] = new ColumnSpec(Msg.getString("RobotTableModel.column.name"), String.class);
		COLUMNS[TYPE] = new ColumnSpec(Msg.getString("RobotTableModel.column.type"), String.class);
		COLUMNS[SETTLEMENT] = new ColumnSpec(Msg.getString("RobotTableModel.column.settlement"), String.class);
		COLUMNS[MODE] = new ColumnSpec(Msg.getString("RobotTableModel.column.mode"), String.class);
		COLUMNS[HEALTH] = new ColumnSpec(Msg.getString("RobotTableModel.column.health"), String.class);
		COLUMNS[BATTERY] = new ColumnSpec(Msg.getString("RobotTableModel.column.battery"), String.class);
		COLUMNS[PERFORMANCE] = new ColumnSpec(Msg.getString("RobotTableModel.column.performance"), String.class);
		COLUMNS[LOCATION] = new ColumnSpec(Msg.getString("RobotTableModel.column.location"), String.class);
		COLUMNS[JOB] = new ColumnSpec(Msg.getString("RobotTableModel.column.job"), String.class);
		COLUMNS[MISSION_COL] = new ColumnSpec(Msg.getString("RobotTableModel.column.mission"), String.class);
		COLUMNS[TASK] = new ColumnSpec(Msg.getString("RobotTableModel.column.task"), String.class);

		eventColumnMapping = new EnumMap<>(UnitEventType.class);
		eventColumnMapping.put(UnitEventType.NAME_EVENT, NAME);
		eventColumnMapping.put(UnitEventType.LOCATION_EVENT, LOCATION);
		eventColumnMapping.put(UnitEventType.STATUS_EVENT, MODE);
		eventColumnMapping.put(UnitEventType.BATTERY_EVENT, BATTERY);
		eventColumnMapping.put(UnitEventType.PERFORMANCE_EVENT, PERFORMANCE);
		eventColumnMapping.put(UnitEventType.JOB_EVENT, JOB);
		eventColumnMapping.put(UnitEventType.TASK_EVENT, TASK);
		eventColumnMapping.put(UnitEventType.TASK_NAME_EVENT, TASK);
		eventColumnMapping.put(UnitEventType.TASK_ENDED_EVENT, TASK);
		eventColumnMapping.put(UnitEventType.TASK_SUBTASK_EVENT, TASK);
		eventColumnMapping.put(UnitEventType.MISSION_EVENT, MISSION_COL);
		eventColumnMapping.put(UnitEventType.DEATH_EVENT, HEALTH);
	}

	/** inner enum with valid source types. */
	private enum ValidSourceType {
		ALL_ROBOTS, VEHICLE_ROBOTS, SETTLEMENT_ROBOTS, SETTLEMENT_ALL_ASSOCIATED_ROBOTS, MISSION_ROBOTS;
	}

	/** The type of source for the people table. */
	private ValidSourceType sourceType;

	// List sources.
	private Crewable vehicle;
	private Set<Settlement> settlements = Collections.emptySet();
	private Mission mission;

	private UnitListener crewListener;
	private UnitListener settlementListener;
	private MissionListener missionListener;
	private boolean allAssociated;

	/**
	 * Constructs a RobotTableModel object that displays all people from the
	 * specified vehicle.
	 *
	 * @param vehicle Monitored vehicle Robot objects.
	 */
	public RobotTableModel(Crewable vehicle)  {
		super(UnitType.ROBOT, Msg.getString(NAME_ROBOTS_KEY, //$NON-NLS-1$
				((Unit) vehicle).getName()), COUNTING_ROBOTS_KEY, //$NON-NLS-1$
				COLUMNS);

		sourceType = ValidSourceType.VEHICLE_ROBOTS;
		this.vehicle = vehicle;
		resetEntities(vehicle.getRobotCrew());
		crewListener = new RobotChangeListener(UnitEventType.INVENTORY_STORING_UNIT_EVENT,
										UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT);
		((Unit) vehicle).addUnitListener(crewListener);
	}

	/**
	 * Constructs a RobotTableModel that displays all associated
	 * robots with a specified settlement.
	 *
	 * @param settlement    the settlement to check.

	 */
	public RobotTableModel() {
		super (UnitType.ROBOT, Msg.getString("RobotTableModel.nameAssociatedRobots"),
				COUNTING_ROBOTS_KEY, COLUMNS);

		this.allAssociated = true;
		setSettlementColumn(SETTLEMENT);
	}

	/**
	 * Constructs a RobotTableModel object that displays all Robot from the
	 * specified mission.
	 *
	 * @param mission Monitored mission Robot objects.
	 */
	public RobotTableModel(Mission mission)  {
		super(UnitType.ROBOT, Msg.getString(NAME_ROBOTS_KEY, //$NON-NLS-1$
				mission.getName()), "RobotTableModel.countingWorkers", //$NON-NLS-1$
				COLUMNS);

		sourceType = ValidSourceType.MISSION_ROBOTS;
		this.mission = mission;
		Collection<Robot> missionRobots = new ArrayList<>();
		Iterator<Worker> i = mission.getMembers().iterator();
		while (i.hasNext()) {
			Worker member = i.next();
			if (member.getUnitType() == UnitType.ROBOT) {
				missionRobots.add((Robot) member);
			}
		}
		resetEntities(missionRobots);
		missionListener = new LocalMissionListener();
		mission.addMissionListener(missionListener);
	}

	/**
	 * Sets the settlement filter for the Robot table.
	 * 
	 * @param filter
	 */
	@Override
	public boolean setSettlementFilter(Set<Settlement> filter) {
		if (settlementListener != null) {
			settlements.forEach(s -> s.removeUnitListener(settlementListener));
		}
		
		this.settlements = filter;

		List<Robot> entities;
		if (allAssociated) {
			sourceType = ValidSourceType.SETTLEMENT_ALL_ASSOCIATED_ROBOTS;
			entities = settlements.stream()
						.map(Settlement::getAllAssociatedRobots)
						.flatMap(Collection::stream)
						.collect(Collectors.toList());
			settlementListener = new RobotChangeListener(UnitEventType.ADD_ASSOCIATED_ROBOT_EVENT,
														UnitEventType.REMOVE_ASSOCIATED_ROBOT_EVENT);
		}
		else {
			// For now it makes no difference between robots in a settlement 
			// and the robots a settlement owns since robots cannot live a settlement
			sourceType = ValidSourceType.SETTLEMENT_ROBOTS;
			entities = settlements.stream()
						.map(Settlement::getAllAssociatedRobots)
						.flatMap(Collection::stream)
						.collect(Collectors.toList());
			settlementListener = new RobotChangeListener(UnitEventType.INVENTORY_STORING_UNIT_EVENT,
														UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT);
		}
					
		resetEntities(entities);

		// Listen to the settlements for new People
		settlements.forEach(s -> s.addUnitListener(settlementListener));

		return true;
	}

	/**
	 * Catches unit update event.
	 *
	 * @param event the unit event.
	 */
	public void unitUpdate(UnitEvent event) {
		
		Integer column = eventColumnMapping.get(event.getType());
			
		if (column != null && (column > -1) && event.getSource() instanceof Robot) {
			entityValueUpdated((Robot) event.getSource(), column, column);
		}	
	}

	/**
	 * Returns the value of a Cell.
	 *
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	@Override
	protected Object getEntityValue(Robot robot, int columnIndex) {
		Object result = null;

		switch (columnIndex) {
			case NAME: 
				result = robot.getName();
				break;

			case TYPE: 
				result = robot.getRobotType().getName();
				break;

			case SETTLEMENT: 
				result = robot.getAssociatedSettlement().getName();
				break;

			case MODE: 
				result = robot.printStatusModes();
				break;
				
			case BATTERY: 
				result = getBatteryStatus(robot.getSystemCondition().getBatteryLevel());
				break;
		
			case HEALTH: 
				if (!robot.isOperable())
					result = "Inoperable";
				else
					result = "Operable";
				break;

			case PERFORMANCE:
				result = getPerformanceStatus(robot.getSystemCondition().getPerformanceFactor());
				break;

			case LOCATION: 
				result = robot.getLocationTag().getImmediateLocation();
				break;

			case JOB: 
				result = robot.getRobotType().getName();
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
	 * Gives the status of a robot's battery level.
	 *
	 * @param level
	 * @return status
	 */
	private static String getBatteryStatus(double level) {
		String status;
		if (level < 1)
			status = Msg.getString("RobotTableModel.column.battery.level0");
		else if (level < 10)
			status = Msg.getString("RobotTableModel.column.battery.level1");
		else if (level < 20)
			status = Msg.getString("RobotTableModel.column.battery.level2");
		else if (level < 40)
			status = Msg.getString("RobotTableModel.column.battery.level3");
		else if (level < 60)
			status = Msg.getString("RobotTableModel.column.battery.level4");
		else if (level < 80)
			status = Msg.getString("RobotTableModel.column.battery.level5");
		else if (level < 95)
			status = Msg.getString("RobotTableModel.column.battery.level6");
		else
			status = Msg.getString("RobotTableModel.column.battery.level7");
		return status;
	}

	/**
	 * Give the status of a robot's hunger level
	 *
	 * @param hunger
	 * @return status
	 */
	private static String getPerformanceStatus(double value) {
		String status = "N/A";
		if (value > 98)
			status = Msg.getString("RobotTableModel.column.performance.level1");
		else if (value < 99)
			status = Msg.getString("RobotTableModel.column.performance.level2");
		else if (value < 75)
			status = Msg.getString("RobotTableModel.column.performance.level3");
		else if (value < 50)
			status = Msg.getString("RobotTableModel.column.performance.level4");
		else if (value < 25)
			status = Msg.getString("RobotTableModel.column.performance.level5");
		return status;
	}

	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void destroy() {
		super.destroy();

		if (sourceType == ValidSourceType.VEHICLE_ROBOTS) {
			((Unit) vehicle).removeUnitListener(crewListener);
			crewListener = null;
			vehicle = null;
		} else if (sourceType == ValidSourceType.MISSION_ROBOTS) {
			mission.removeMissionListener(missionListener);
			missionListener = null;
			mission = null;
		} else {
			settlements.forEach(s -> s.removeUnitListener(settlementListener));
			settlements = null;
		}
	}	

	/**
	 * MissionListener inner class.
	 */
	private class LocalMissionListener implements MissionListener {

		/**
		 * Catch mission update event.
		 *
		 * @param event the mission event.
		 */
		public void missionUpdate(MissionEvent event) {
			MissionEventType eventType = event.getType();
			Unit unit = (Unit) event.getTarget();
			if (unit instanceof Robot) {
				if (eventType == MissionEventType.ADD_MEMBER_EVENT) {
					addEntity((Robot) unit);
				}
				else if (eventType == MissionEventType.REMOVE_MEMBER_EVENT) {
					removeEntity((Robot) unit);
				}
			}
		}
	}

	/**
	 * UnitListener inner class for events where a Robot joins or leaves a Unit
	 */
	private class RobotChangeListener implements UnitListener {

		private UnitEventType addEvent;
		private UnitEventType removeEvent;

		public RobotChangeListener(UnitEventType addEvent, UnitEventType removeEvent) {
			this.addEvent = addEvent;
			this.removeEvent = removeEvent;
		}

		/**
		 * Catches unit update event.
		 *
		 * @param event the unit event.
		 */
		public void unitUpdate(UnitEvent event) {
			UnitEventType eventType = event.getType();
			if (eventType == addEvent) {
				Unit unit = (Unit)event.getTarget();
				if (unit.getUnitType() == UnitType.ROBOT) {
					addEntity((Robot) unit);
				}
			}
			else if (eventType == removeEvent) {
				Unit unit = (Unit)event.getTarget();
				if (unit.getUnitType() == UnitType.ROBOT) {
					removeEntity((Robot) unit);
				}
			}
		}
	}
}
