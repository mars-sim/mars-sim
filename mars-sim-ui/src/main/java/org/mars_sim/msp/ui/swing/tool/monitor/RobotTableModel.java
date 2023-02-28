/*
 * Mars Simulation Project
 * RobotTableModel.java
 * @date 2021-12-07
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionEventType;
import org.mars_sim.msp.core.person.ai.mission.MissionListener;
import org.mars_sim.msp.core.person.ai.task.util.TaskManager;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Crewable;

/**
 * The RobotTableModel maintains a list of Robot objects. By defaults the source
 * of the list is the Unit Manager. It maps key attributes of the Robot into
 * Columns.
 */
@SuppressWarnings("serial")
public class RobotTableModel extends UnitTableModel<Robot> {

	// Column indexes
	/** Robot name column. */
	private static final int NAME = 0;
	/** Gender column. */
	private static final int TYPE = 1;
	/** Location column. */
	private static final int LOCATION = 2;
	/** Health column. */
	private static final int HEALTH = 3;
	/** Hunger column. */
	private static final int BATTERY = 4;

	/** Performance column. */
	private static final int PERFORMANCE = 5;
	/** Job column. */
	private static final int JOB = 6;
	/** Task column. */
	private static final int TASK = 7;
	/** Mission column. */
	private static final int MISSION_COL = 8;

	/** The number of Columns. */
	private static final int COLUMNCOUNT = 9;
	/** Names of Columns. */
	private static String[] columnNames;
	/** Types of Columns. */
	private static Class<?>[] columnTypes;
	private static final Map<UnitEventType, Integer> eventColumnMapping;

	/**
	 * The static initializer creates the name & type arrays.
	 */
	static {
		columnNames = new String[COLUMNCOUNT];
		columnTypes = new Class[COLUMNCOUNT];
		columnNames[NAME] = Msg.getString("RobotTableModel.column.name"); //$NON-NLS-1$
		columnTypes[NAME] = String.class;
		columnNames[TYPE] = Msg.getString("RobotTableModel.column.type"); //$NON-NLS-1$
		columnTypes[TYPE] = String.class;
		columnNames[HEALTH] = Msg.getString("RobotTableModel.column.health"); //$NON-NLS-1$
		columnTypes[HEALTH] = String.class;
		columnNames[BATTERY] = Msg.getString("RobotTableModel.column.battery"); //$NON-NLS-1$
		columnTypes[BATTERY] = String.class;
		columnNames[PERFORMANCE] = Msg.getString("RobotTableModel.column.performance"); //$NON-NLS-1$
		columnTypes[PERFORMANCE] = String.class;
		columnNames[LOCATION] = Msg.getString("RobotTableModel.column.location"); //$NON-NLS-1$
		columnTypes[LOCATION] = String.class;
		columnNames[JOB] = Msg.getString("RobotTableModel.column.job"); //$NON-NLS-1$
		columnTypes[JOB] = String.class;
		columnNames[MISSION_COL] = Msg.getString("RobotTableModel.column.mission"); //$NON-NLS-1$
		columnTypes[MISSION_COL] = String.class;
		columnNames[TASK] = Msg.getString("RobotTableModel.column.task"); //$NON-NLS-1$
		columnTypes[TASK] = String.class;

		eventColumnMapping = new EnumMap<>(UnitEventType.class);
		eventColumnMapping.put(UnitEventType.NAME_EVENT, NAME);
		eventColumnMapping.put(UnitEventType.LOCATION_EVENT, LOCATION);
		eventColumnMapping.put(UnitEventType.ROBOT_POWER_EVENT, BATTERY);
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
	private Settlement settlement;
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
		super(UnitType.ROBOT, Msg.getString("RobotTableModel.nameRobots", //$NON-NLS-1$
				((Unit) vehicle).getName()), "RobotTableModel.countingRobots", //$NON-NLS-1$
				columnNames, 
				columnTypes);

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
	 * @param allAssociated Are all robots associated with this settlement to be
	 *                      displayed?
	 */
	public RobotTableModel(Settlement settlement, boolean allAssociated) {
		super (UnitType.ROBOT, (allAssociated ? Msg.getString("RobotTableModel.nameAssociatedRobots") //$NON-NLS-1$
			 	: Msg.getString("RobotTableModel.nameRobots", //$NON-NLS-1$
					settlement.getName())
				),
				(allAssociated ? "RobotTableModel.countingRobots" : //$NON-NLS-1$
						"RobotTableModel.countingResidents" //$NON-NLS-1$
				), columnNames, columnTypes);

		this.allAssociated = allAssociated;
		setSettlementFilter(settlement);
	}

	/**
	 * Constructs a RobotTableModel object that displays all Robot from the
	 * specified mission.
	 *
	 * @param mission Monitored mission Robot objects.
	 */
	public RobotTableModel(Mission mission)  {
		super(UnitType.ROBOT, Msg.getString("RobotTableModel.nameRobots", //$NON-NLS-1$
				mission.getName()), "RobotTableModel.countingWorkers", //$NON-NLS-1$
				columnNames, columnTypes);

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
	 * Set teh settlement filter for the Robot table
	 * @param filter
	 */
	@Override
	public boolean setSettlementFilter(Settlement filter) {
		if (settlementListener != null) {
			settlement.removeUnitListener(settlementListener);
		}
		
		this.settlement = filter;
		if (allAssociated) {
			sourceType = ValidSourceType.SETTLEMENT_ALL_ASSOCIATED_ROBOTS;
			resetEntities(settlement.getAllAssociatedRobots());
			settlementListener = new RobotChangeListener(UnitEventType.ADD_ASSOCIATED_ROBOT_EVENT,
														UnitEventType.REMOVE_ASSOCIATED_ROBOT_EVENT);
			settlement.addUnitListener(settlementListener);
		}
		else {
			sourceType = ValidSourceType.SETTLEMENT_ROBOTS;
			resetEntities(settlement.getRobots());
			settlementListener = new RobotChangeListener(UnitEventType.INVENTORY_STORING_UNIT_EVENT,
														UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT);
			settlement.addUnitListener(settlementListener);
		}

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

			case BATTERY: 
				if (robot.getSystemCondition().isInoperable())
					result = null;
				else
					result = getBatteryStatus(robot.getSystemCondition().getBatteryState());
				break;

			case HEALTH: 
				if (robot.getSystemCondition().isInoperable())
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
				Mission mission = robot.getBotMind().getMission();
				if (mission != null) {
					result = mission.getName();
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
		String status = "N/A";
		if (level < 10)
			status = Msg.getString("RobotTableModel.column.battery.level1");
		else if (level < 30)
			status = Msg.getString("RobotTableModel.column.battery.level2");
		else if (level < 60)
			status = Msg.getString("RobotTableModel.column.battery.level3");
		else if (level < 99)
			status = Msg.getString("RobotTableModel.column.battery.level4");
		else
			status = Msg.getString("RobotTableModel.column.battery.level5");
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
			settlement.removeUnitListener(settlementListener);
			settlementListener = null;
			settlement = null;
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
		 * Catch unit update event.
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
