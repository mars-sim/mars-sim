/*
 * Mars Simulation Project
 * RobotTableModel.java
 * @date 2021-12-07
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionEventType;
import org.mars_sim.msp.core.person.ai.mission.MissionListener;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * The RobotTableModel maintains a list of Robot objects. By defaults the source
 * of the list is the Unit Manager. It maps key attributes of the Robot into
 * Columns.
 */
@SuppressWarnings("serial")
public class RobotTableModel extends UnitTableModel {

	// Column indexes
	/** Robot name column. */
	private final static int NAME = 0;
	/** Gender column. */
	private final static int TYPE = 1;
	/** Location column. */
	private final static int LOCATION = 2;
	/** Settlement column. */
	private final static int SETTLEMENT_COL = 3;
	/** Health column. */
	private final static int HEALTH = 4;
	/** Hunger column. */
	private final static int BATTERY = 5;
	/** Fatigue column. */
	// private final static int FATIGUE = 6;
	/** Stress column. */
	// private final static int STRESS = 7;
	/** Performance column. */
	private final static int PERFORMANCE = 6;
	/** Job column. */
	private final static int JOB = 7;
	/** Task column. */
	private final static int TASK = 8;
	/** Mission column. */
	private final static int MISSION_COL = 9;

	/** The number of Columns. */
	private final static int COLUMNCOUNT = 10;
	/** Names of Columns. */
	private static String columnNames[];
	/** Types of Columns. */
	private static Class<?> columnTypes[];

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
		columnNames[SETTLEMENT_COL] = Msg.getString("RobotTableModel.column.settlement"); //$NON-NLS-1$
		columnTypes[SETTLEMENT_COL] = String.class;
		columnNames[JOB] = Msg.getString("RobotTableModel.column.job"); //$NON-NLS-1$
		columnTypes[JOB] = String.class;
		columnNames[MISSION_COL] = Msg.getString("RobotTableModel.column.mission"); //$NON-NLS-1$
		columnTypes[MISSION_COL] = String.class;
		columnNames[TASK] = Msg.getString("RobotTableModel.column.task"); //$NON-NLS-1$
		columnTypes[TASK] = String.class;
	}

	/** inner enum with valid source types. */
	private enum ValidSourceType {
		ALL_ROBOTS, VEHICLE_ROBOTS, SETTLEMENT_ROBOTS, SETTLEMENT_ALL_ASSOCIATED_ROBOTS, MISSION_ROBOTS;
	}

	private static UnitManager unitManager = Simulation.instance().getUnitManager();

	/** The type of source for the people table. */
	private ValidSourceType sourceType;

	// List sources.
	private Crewable vehicle;
	private Settlement settlement;
	private Mission mission;

	private UnitListener crewListener;
	private UnitListener settlementListener;
	private MissionListener missionListener;
	private UnitManagerListener unitManagerListener;

	/**
	 * constructor. Constructs a RobotTableModel object that displays all people in
	 * the simulation.
	 *
	 * @param unitManager Manager containing Robot objects.
	 */
	public RobotTableModel(MainDesktopPane desktop) throws Exception {
		super(Msg.getString("RobotTableModel.tabName"), //$NON-NLS-1$
				"RobotTableModel.countingRobots", //$NON-NLS-1$
				columnNames, columnTypes);

		sourceType = ValidSourceType.ALL_ROBOTS;

		if (mode == GameMode.COMMAND)
			setSource(unitManager.getCommanderSettlement().getRobots());
		else
			setSource(unitManager.getRobots());

		unitManagerListener = new LocalUnitManagerListener();
		unitManager.addUnitManagerListener(unitManagerListener);

	}

	/**
	 * Constructs a RobotTableModel object that displays all people from the
	 * specified vehicle.
	 *
	 * @param vehicle Monitored vehicle Robot objects.
	 */
	public RobotTableModel(Crewable vehicle) throws Exception {
		super(Msg.getString("RobotTableModel.nameRobots", //$NON-NLS-1$
				((Unit) vehicle).getName()), "RobotTableModel.countingRobots", //$NON-NLS-1$
				columnNames, 
				columnTypes);

		sourceType = ValidSourceType.VEHICLE_ROBOTS;
		this.vehicle = vehicle;
		setSource(vehicle.getRobotCrew());
		crewListener = new LocalCrewListener();
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
	public RobotTableModel(Settlement settlement, boolean allAssociated) throws Exception {
		super ((allAssociated ? Msg.getString("RobotTableModel.nameAssociatedRobots") //$NON-NLS-1$
				: Msg.getString("RobotTableModel.nameRobots", //$NON-NLS-1$
					settlement.getName())
				),
				(allAssociated ? "RobotTableModel.countingRobots" : //$NON-NLS-1$
						"RobotTableModel.countingResidents" //$NON-NLS-1$
				), columnNames, columnTypes);

		this.settlement = settlement;
		if (allAssociated) {
			sourceType = ValidSourceType.SETTLEMENT_ALL_ASSOCIATED_ROBOTS;
			setSource(settlement.getAllAssociatedRobots());
			settlementListener = new AssociatedSettlementListener();
			settlement.addUnitListener(settlementListener);
		} else {
			sourceType = ValidSourceType.SETTLEMENT_ROBOTS;
			setSource(settlement.getRobots());
			settlementListener = new InhabitantSettlementListener();
			settlement.addUnitListener(settlementListener);
		}
	}

	/**
	 * Constructs a RobotTableModel object for a settlement.
	 * 
	 * @param settlement
	 * @throws Exception
	 */
	public RobotTableModel(Settlement settlement) throws Exception {
		super(Msg.getString("RobotTableModel.nameAssociatedRobots", //$NON-NLS-1$
				settlement.getName()),
				"RobotTableModel.countingRobots", //$NON-NLS-1$
				columnNames, columnTypes);

		this.settlement = settlement;
		sourceType = ValidSourceType.SETTLEMENT_ROBOTS;
		setSource(settlement.getRobots());
		settlementListener = new AssociatedSettlementListener(); //InhabitantSettlementListener();
		settlement.addUnitListener(settlementListener);
	}

	/**
	 * Constructs a RobotTableModel object that displays all Robot from the
	 * specified mission.
	 *
	 * @param mission Monitored mission Robot objects.
	 */
	public RobotTableModel(Mission mission) throws Exception {
		super(Msg.getString("RobotTableModel.nameRobots", //$NON-NLS-1$
				mission.getName()), "RobotTableModel.countingMissionMembers", //$NON-NLS-1$
				columnNames, columnTypes);

		sourceType = ValidSourceType.MISSION_ROBOTS;
		this.mission = mission;
		Collection<Robot> missionRobots = new ArrayList<>();
		Iterator<MissionMember> i = mission.getMembers().iterator();
		while (i.hasNext()) {
			MissionMember member = i.next();
			if (member.getUnitType() == UnitType.ROBOT) {
				missionRobots.add((Robot) member);
			}
		}
		setSource(missionRobots);
		missionListener = new LocalMissionListener();
		mission.addMissionListener(missionListener);
	}

	/**
	 * Defines the source data from this table.
	 */
	private void setSource(Collection<Robot> source) {
		Iterator<Robot> iter = source.iterator();
		while (iter.hasNext())
			addUnit(iter.next());
	}

	/**
	 * Catches unit update event.
	 *
	 * @param event the unit event.
	 */
	public void unitUpdate(UnitEvent event) {
		SwingUtilities.invokeLater(new RobotTableUpdater(event, this));
	}

	/**
	 * Returns the value of a Cell.
	 *
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = null;

		if (rowIndex < getUnitNumber()) {
			Robot robot = (Robot) getUnit(rowIndex);

			switch (columnIndex) {

			case NAME: {
				result = robot.getName();
			}
				break;

			case TYPE: {
				String typeStr = robot.getRobotType().getName();

				result = typeStr;
			}
				break;

			case BATTERY: {
				double kWh = robot.getSystemCondition().getcurrentEnergy();
				// result = new Float(hunger).intValue();
				if (robot.getSystemCondition().isInoperable())
					result = "";
				else
					result = Math.round(kWh * 100.0)/100.0;
			}
				break;

			case HEALTH: {
				{
					if (robot.getSystemCondition().isInoperable())
						result = "Inoperable";
					else
						result = "Operable";
				}
			}
				break;

			case LOCATION: {
				result = robot.getLocationTag().getImmediateLocation();
			}
				break;

			case SETTLEMENT_COL: {
				result = robot.getLocationTag().getLocale();
			}
				break;

			case JOB: {
				result = RobotJob.getName(robot.getRobotType());
			}
				break;

			case TASK: {
				// If the Robot is dead, there is no Task Manager
				TaskManager mgr = robot.getBotMind().getBotTaskManager();
				result = ((mgr != null) ? mgr.getTaskDescription(false) : null);
			}
				break;

			case MISSION_COL: {
				Mission mission = robot.getBotMind().getMission();
				if (mission != null) {
					result = mission.getDescription();
				}
			}
				break;
			}
		}

		return result;
	}

	/**
	 * Gives the status of a robot's battery level.
	 *
	 * @param level
	 * @return status
	 */
	public String getBatteryStatus(double level) {
		String status = "N/A";
		if (level < 10)
			status = Msg.getString("RobotTableModel.column.battery.level1");
		else if (level < 30)
			status = Msg.getString("RobotTableModel.column.battery.level2");
		else if (level < 60)
			status = Msg.getString("RobotTableModel.column.battery.level3");
		else if (level < 80)
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
	public String getPerformanceStatus(double value) {
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

		if (sourceType == ValidSourceType.ALL_ROBOTS) {
			UnitManager unitManager = Simulation.instance().getUnitManager();
			unitManager.removeUnitManagerListener(unitManagerListener);
			unitManagerListener = null;
		} else if (sourceType == ValidSourceType.VEHICLE_ROBOTS) {
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

	@Override
	public boolean equals(Object o) {
		boolean result = super.equals(o);

		if (o instanceof RobotTableModel) {
			RobotTableModel oModel = (RobotTableModel) o;
			if (!sourceType.equals(oModel.sourceType))
				result = false;
		}

		return result;
	}

	/**
	 * Inner class for updating the robot table.
	 */
	private static class RobotTableUpdater implements Runnable {

		static final Map<UnitEventType, Integer> EVENT_COLUMN_MAPPING;

		static {
			HashMap<UnitEventType, Integer> m = new HashMap<>();
			m.put(UnitEventType.NAME_EVENT, NAME);
			m.put(UnitEventType.LOCATION_EVENT, LOCATION);
			m.put(UnitEventType.ROBOT_POWER_EVENT, BATTERY);
			m.put(UnitEventType.PERFORMANCE_EVENT, PERFORMANCE);
			m.put(UnitEventType.JOB_EVENT, JOB);
			m.put(UnitEventType.TASK_EVENT, TASK);
			m.put(UnitEventType.TASK_NAME_EVENT, TASK);
			m.put(UnitEventType.TASK_ENDED_EVENT, TASK);
			m.put(UnitEventType.TASK_SUBTASK_EVENT, TASK);
			m.put(UnitEventType.MISSION_EVENT, MISSION_COL);
			m.put(UnitEventType.DEATH_EVENT, HEALTH);
			EVENT_COLUMN_MAPPING = Collections.unmodifiableMap(m);
		}

		private final UnitEvent event;

		private final RobotTableModel tableModel;

		private RobotTableUpdater(UnitEvent event, RobotTableModel tableModel) {
			this.event = event;
			this.tableModel = tableModel;
		}

		@Override
		public void run() {
			UnitEventType eventType = event.getType();

			Integer column = EVENT_COLUMN_MAPPING.get(eventType);

//			if (eventType == UnitEventType.NAME_EVENT) {
//				
//			}
//			
//			else if (eventType == UnitEventType.LOCATION_EVENT) {
//				
//			}
//			else if (eventType == UnitEventType.ROBOT_POWER_EVENT) {
//				
//			}
//			else if (eventType == UnitEventType.PERFORMANCE_EVENT) {
//				
//			}
//			else if (eventType == UnitEventType.JOB_EVENT) {
//				
//			}
//			else if (eventType == UnitEventType.TASK_EVENT) {
//				
//			}
//			else if (eventType == UnitEventType.TASK_NAME_EVENT) {
//				
//			}
//			else if (eventType == UnitEventType.TASK_ENDED_EVENT) {
//				
//			}
//			else if (eventType == UnitEventType.TASK_SUBTASK_EVENT) {
//				
//			}
//			else if (eventType == UnitEventType.MISSION_EVENT) {
//				
//			}
//			else if (eventType == UnitEventType.DEATH_EVENT) {
//				
//			}
			
			if (column != null && column > -1) {
				if (event.getSource() instanceof Unit) {
					Unit source = (Unit) event.getSource();
					if (source instanceof Robot) {
						tableModel.fireTableCellUpdated(tableModel.getUnitIndex(source), column);
					}
				}
				
				if (event.getTarget() instanceof Unit) {
					Unit target = (Unit) event.getTarget();
					if (target instanceof Robot) {
						tableModel.fireTableCellUpdated(tableModel.getUnitIndex(target), column);
					}
				}
			}
		}
	}

	/**
	 * UnitListener inner class for crewable vehicle.
	 */
	private class LocalCrewListener implements UnitListener {

		/**
		 * Catch unit update event.
		 *
		 * @param event the unit event.
		 */
		public void unitUpdate(UnitEvent event) {
			UnitEventType eventType = event.getType();
			Unit unit = (Unit) event.getTarget();
			if (unit != null && unit.getUnitType() == UnitType.ROBOT) {
				if (eventType == UnitEventType.INVENTORY_STORING_UNIT_EVENT) {
					if (!containsUnit(unit))
						addUnit(unit);
				} else if (eventType == UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT) {
					if (containsUnit(unit))
						removeUnit(unit);
				}
			}
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
			if (unit != null && unit.getUnitType() == UnitType.ROBOT) {
				if (eventType == MissionEventType.ADD_MEMBER_EVENT) {
					if (!containsUnit(unit))
						addUnit(unit);
				}
				else if (eventType == MissionEventType.REMOVE_MEMBER_EVENT) {
					if (containsUnit(unit))
						removeUnit(unit);
				}
			}
		}
	}

	/**
	 * UnitManagerListener inner class.
	 */
	private class LocalUnitManagerListener implements UnitManagerListener {

		/**
		 * Catch unit manager update event.
		 *
		 * @param event the unit event.
		 */
		public void unitManagerUpdate(UnitManagerEvent event) {
			Unit unit = event.getUnit();
			UnitManagerEventType eventType = event.getEventType();
			if (unit != null && unit.getUnitType() == UnitType.ROBOT) {
				if (eventType == UnitManagerEventType.ADD_UNIT) {
					if (!containsUnit(unit))
						addUnit(unit);
				} else if (eventType == UnitManagerEventType.REMOVE_UNIT) {
					if (containsUnit(unit))
						removeUnit(unit);
				}
			}
		}
	}

	/**
	 * UnitListener inner class for settlements for all inhabitants list.
	 */
	private class InhabitantSettlementListener implements UnitListener {
		/**
		 * Catch unit update event.
		 *
		 * @param event the unit event.
		 */
		public void unitUpdate(UnitEvent event) {
			UnitEventType eventType = event.getType();
			
			if (eventType == UnitEventType.INVENTORY_STORING_UNIT_EVENT) {
				Unit unit = (Unit) event.getTarget();
				if (unit != null && unit.getUnitType() == UnitType.ROBOT) {
					if (!containsUnit(unit))
						addUnit(unit);
				}
			} else if (eventType == UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT) {
				Unit unit = (Unit) event.getTarget();
				if (unit != null && unit.getUnitType() == UnitType.ROBOT) {
					if (containsUnit(unit))
						removeUnit(unit);
				}
			}
		}
	}

	/**
	 * UnitListener inner class for settlements for associated people list.
	 */
	private class AssociatedSettlementListener implements UnitListener {

		/**
		 * Catch unit update event.
		 *
		 * @param event the unit event.
		 */
		public void unitUpdate(UnitEvent event) {
			UnitEventType eventType = event.getType();
			if (eventType == UnitEventType.ADD_ASSOCIATED_ROBOT_EVENT) {	
				Unit unit = (Unit) event.getTarget();
				if (unit != null && unit.getUnitType() == UnitType.ROBOT) {
					if (!containsUnit(unit))
						addUnit(unit);
				}
			} else if (eventType == UnitEventType.REMOVE_ASSOCIATED_ROBOT_EVENT) {
				Unit unit = (Unit) event.getTarget();
				if (unit != null && unit.getUnitType() == UnitType.ROBOT) {
					if (containsUnit(unit))
						removeUnit(unit);
				}
			}
		}
	}
}
