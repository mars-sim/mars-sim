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
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.CollectionUtils;
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
	private static final int TASK = PERFORMANCE+1;
	private static final int MISSION_COL = TASK+1;

	private static final int COLUMNCOUNT = MISSION_COL+1;
	
	private static final String PERCENT = " % - ";
	private static final String INOPERABLE = "Inoperable";
	private static final String OPERABLE = "Operable";
	private static final String NA = "N/A";
	private static final String B_LEVEL0 = Msg.getString("RobotTableModel.column.battery.level0");
	private static final String B_LEVEL1 = Msg.getString("RobotTableModel.column.battery.level1");
	private static final String B_LEVEL2 = Msg.getString("RobotTableModel.column.battery.level2");
	private static final String B_LEVEL3 = Msg.getString("RobotTableModel.column.battery.level3");
	private static final String B_LEVEL4 = Msg.getString("RobotTableModel.column.battery.level4");
	private static final String B_LEVEL5 = Msg.getString("RobotTableModel.column.battery.level5");
	private static final String B_LEVEL6 = Msg.getString("RobotTableModel.column.battery.level6");
	private static final String B_LEVEL7 = Msg.getString("RobotTableModel.column.battery.level7");

	private static final String P_LEVEL0 = Msg.getString("RobotTableModel.column.performance.level0");
	private static final String P_LEVEL1 = Msg.getString("RobotTableModel.column.performance.level1");
	private static final String P_LEVEL2 = Msg.getString("RobotTableModel.column.performance.level2");
	private static final String P_LEVEL3 = Msg.getString("RobotTableModel.column.performance.level3");
	private static final String P_LEVEL4 = Msg.getString("RobotTableModel.column.performance.level4");
	
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
		COLUMNS[MISSION_COL] = new ColumnSpec(Msg.getString("RobotTableModel.column.mission"), String.class);
		COLUMNS[TASK] = new ColumnSpec(Msg.getString("RobotTableModel.column.task"), String.class);

		eventColumnMapping = new EnumMap<>(UnitEventType.class);
		eventColumnMapping.put(UnitEventType.NAME_EVENT, NAME);
		eventColumnMapping.put(UnitEventType.COORDINATE_EVENT, LOCATION);
		eventColumnMapping.put(UnitEventType.STATUS_EVENT, MODE);
		eventColumnMapping.put(UnitEventType.BATTERY_EVENT, BATTERY);
		eventColumnMapping.put(UnitEventType.PERFORMANCE_EVENT, PERFORMANCE);
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
		
		Collection<Robot> crew = CollectionUtils.sortByName(vehicle.getRobotCrew());
		resetEntities(crew);
		
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
		
		CollectionUtils.sortByName(missionRobots);
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
						.sorted(Comparator.comparing(Robot::getName))
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
						.sorted(Comparator.comparing(Robot::getName))
						.collect(Collectors.toList());
			settlementListener = new RobotChangeListener(UnitEventType.INVENTORY_STORING_UNIT_EVENT,
														UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT);
		}
		
		if (entities != null && !entities.isEmpty()) {	
			resetEntities(entities);
		}
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
				result = getBatteryStatus(robot.getSystemCondition().getBattery().getBatteryPercent());
				break;
		
			case HEALTH: 
				if (!robot.isOperable())
					result = INOPERABLE;
				else
					result = OPERABLE;
				break;

			case PERFORMANCE:
				result = getPerformanceStatus(robot.getSystemCondition().getPerformanceFactor());
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
	 * Gives the status of a robot's battery level.
	 *
	 * @param percent
	 * @return status
	 */
	private static String getBatteryStatus(double percent) {
		String status = Math.round(percent * 10.0)/10.0 + PERCENT;
		if (percent < 1)
			status += B_LEVEL0;
		else if (percent < 10)
			status += B_LEVEL1;
		else if (percent < 20)
			status += B_LEVEL2;
		else if (percent < 40)
			status += B_LEVEL3;
		else if (percent < 60)
			status += B_LEVEL4;
		else if (percent < 80)
			status += B_LEVEL5;
		else if (percent < 95)
			status += B_LEVEL6;
		else
			status += B_LEVEL7;
		return status;
	}

	/**
	 * Give the status of a robot's hunger level
	 *
	 * @param hunger
	 * @return status
	 */
	private static String getPerformanceStatus(double value) {
		String status = Math.round(value * 100.0)/1.0 + PERCENT;
		if (value > 98)
			status += P_LEVEL4;
		else if (value < 99)
			status += P_LEVEL3;
		else if (value < 75)
			status += P_LEVEL2;
		else if (value < 50)
			status += P_LEVEL1;
		else if (value < 25)
			status += P_LEVEL0;
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
		 * Catches mission update event.
		 *
		 * @param event the mission event.
		 */
		public void missionUpdate(MissionEvent event) {
			MissionEventType eventType = event.getType();
			Unit unit = (Unit) event.getTarget();
			if (unit instanceof Robot r) {
				if (eventType == MissionEventType.ADD_MEMBER_EVENT) {
					addEntity(r);
				}
				else if (eventType == MissionEventType.REMOVE_MEMBER_EVENT) {
					removeEntity(r);
				}
			}
		}
	}

	/**
	 * UnitListener inner class for events where a Robot joins or leaves a Unit.
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
			if (event.getTarget() instanceof Robot r) {
				UnitEventType eventType = event.getType();
				if (eventType == addEvent) {
					addEntity(r);
				}
				else if (eventType == removeEvent) {
					removeEntity(r);
				}
			}
		}
	}
}
