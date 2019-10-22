/**
 * Mars Simulation Project
 * RobotTableModel.java
 * @version 3.1.0 2017-09-14
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.GameManager;
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
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionEventType;
import org.mars_sim.msp.core.person.ai.mission.MissionListener;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.robot.ai.task.BotTaskManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * The RobotTableModel maintains a list of Robot objects. By defaults the source
 * of the list is the Unit Manager. It maps key attributes of the Robot into
 * Columns.
 */
public class RobotTableModel extends UnitTableModel {

	// private static final Logger logger =
	// Logger.getLogger(RobotTableModel.class.getName());

	// Column indexes
	/** Robot name column. */
	private final static int NAME = 0;
	/** Gender column. */
	private final static int TYPE = 1;
	/** Location column. */
	private final static int LOCATION = 2;
	/** Settlement column. */
	 private final static int SETTLEMENT = 3;
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
	private final static int MISSION = 9;

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
		// columnNames[PERSONALITY] =
		// Msg.getString("RobotTableModel.column.personality"); //$NON-NLS-1$
		// columnTypes[PERSONALITY] = String.class;
		columnNames[HEALTH] = Msg.getString("RobotTableModel.column.health"); //$NON-NLS-1$
		columnTypes[HEALTH] = String.class;
		columnNames[BATTERY] = Msg.getString("RobotTableModel.column.battery"); //$NON-NLS-1$
		columnTypes[BATTERY] = String.class;
		// columnNames[FATIGUE] = Msg.getString("RobotTableModel.column.fatigue");
		// //$NON-NLS-1$
		// columnTypes[FATIGUE] = String.class;
		// columnNames[STRESS] = Msg.getString("RobotTableModel.column.stress");
		// //$NON-NLS-1$
		// columnTypes[STRESS] = String.class;
		columnNames[PERFORMANCE] = Msg.getString("RobotTableModel.column.performance"); //$NON-NLS-1$
		columnTypes[PERFORMANCE] = String.class;
		columnNames[LOCATION] = Msg.getString("RobotTableModel.column.location"); //$NON-NLS-1$
		columnTypes[LOCATION] = String.class;
		columnNames[SETTLEMENT] = Msg.getString("RobotTableModel.column.settlement"); //$NON-NLS-1$
		columnTypes[SETTLEMENT] = String.class;
		columnNames[JOB] = Msg.getString("RobotTableModel.column.job"); //$NON-NLS-1$
		columnTypes[JOB] = String.class;
		columnNames[MISSION] = Msg.getString("RobotTableModel.column.mission"); //$NON-NLS-1$
		columnTypes[MISSION] = String.class;
		columnNames[TASK] = Msg.getString("RobotTableModel.column.task"); //$NON-NLS-1$
		columnTypes[TASK] = String.class;

	}

	/** inner enum with valid source types. */
	private enum ValidSourceType {
		ALL_ROBOTS, VEHICLE_ROBOTS, SETTLEMENT_ROBOTS, SETTLEMENT_ALL_ASSOCIATED_ROBOTS, MISSION_ROBOTS;
	}

//    static final Map<String, Integer> EVENT_COLUMN_MAPPING;//= new HashMap<String, Integer>(12);
//
//        static {
//            HashMap<String, Integer> m = new HashMap<String, Integer>();
//            m.put(Unit.NAME_EVENT, NAME);
//            m.put(Unit.LOCATION_EVENT, LOCATION);
//            m.put(SystemCondition.HUNGER_EVENT, HUNGER);
//            m.put(SystemCondition.FATIGUE_EVENT, FATIGUE);
//            m.put(SystemCondition.STRESS_EVENT, STRESS);
//            m.put(SystemCondition.PERFORMANCE_EVENT, PERFORMANCE);
//            m.put(Mind.JOB_EVENT, JOB);
//            m.put(TaskManager.TASK_EVENT, TASK);
//            m.put(Mind.MISSION_EVENT, MISSION);
//            m.put(SystemCondition.ILLNESS_EVENT, HEALTH);
//            m.put(SystemCondition.DEATH_EVENT, HEALTH);
//                    EVENT_COLUMN_MAPPING = Collections.unmodifiableMap(m);
//        }

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
	public RobotTableModel(MainDesktopPane desktop) {
		super(Msg.getString("RobotTableModel.tabName"), //$NON-NLS-1$
				"RobotTableModel.countingRobots", //$NON-NLS-1$
				columnNames, columnTypes);

		sourceType = ValidSourceType.ALL_ROBOTS;
		
		if (GameManager.mode == GameMode.COMMAND)
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
	public RobotTableModel(Crewable vehicle) {
		super(Msg.getString("RobotTableModel.nameRobots", //$NON-NLS-1$
				((Unit) vehicle).getName()), "RobotTableModel.countingRobots", //$NON-NLS-1$
				columnNames, columnTypes);

		sourceType = ValidSourceType.VEHICLE_ROBOTS;
		this.vehicle = vehicle;
		// setSource(vehicle.getCrew());
		// crewListener = new LocalCrewListener();
		// ((Unit) vehicle).addUnitListener(crewListener);
	}

	/**
	 * Constructs a RobotTableModel that displays residents are all associated
	 * people with a specified settlement.
	 * 
	 * @param settlement    the settlement to check.
	 * @param allAssociated Are all people associated with this settlement to be
	 *                      displayed?
	 */
	public RobotTableModel(Settlement settlement, boolean allAssociated) {
		super((allAssociated ? Msg.getString("RobotTableModel.nameAssociatedRobots", //$NON-NLS-1$
				settlement.getName())
				: Msg.getString("RobotTableModel.nameRobots", //$NON-NLS-1$
						settlement.getName())),
				(allAssociated ? "RobotTableModel.countingAssociatedRobots" : //$NON-NLS-1$
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
	 * Constructs a RobotTableModel object that displays all Robot from the
	 * specified mission.
	 * 
	 * @param mission Monitored mission Robot objects.
	 */
	public RobotTableModel(Mission mission) {
		super(Msg.getString("RobotTableModel.nameRobots", //$NON-NLS-1$
				mission.getName()), "RobotTableModel.countingMissionMembers", //$NON-NLS-1$
				columnNames, columnTypes);

		sourceType = ValidSourceType.MISSION_ROBOTS;
		this.mission = mission;
		// setSource(mission.getRobots());
		// missionListener = new LocalMissionListener();
		// mission.addMissionListener(missionListener);
	}

	/**
	 * Defines the source data from this table
	 */
	private void setSource(Collection<Robot> source) {
		Iterator<Robot> iter = source.iterator();
		while (iter.hasNext())
			addUnit(iter.next());
	}

	/**
	 * Catch unit update event.
	 * 
	 * @param event the unit event.
	 */
	public void unitUpdate(UnitEvent event) {
		SwingUtilities.invokeLater(new RobotTableUpdater(event, this));
	}

	/**
	 * Return the value of a Cell
	 * 
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = null;

		if (rowIndex < getUnitNumber()) {
			Robot robot = (Robot) getUnit(rowIndex);

			Boolean isDead = robot.getSystemCondition().isInoperable();

			switch (columnIndex) {
			case NAME: {
				result = robot.getName();
			}
				break;

			case TYPE: {
				String typeStr = robot.getRobotType().getName();
				// String letter;
				// if (typeStr.equals("male")) letter = "M";
				// else letter = "F";
				result = typeStr;
			}
				break;

			// case PERSONALITY : {
			// result = robot.getMind().getRobotalityType().getTypeString();
			// } break;

			case BATTERY: {
				double hunger = robot.getSystemCondition().getPowerDischarge();
				// result = new Float(hunger).intValue();
				if (isDead)
					result = "";
				else
					result = getHungerStatus(hunger);
			}
				break;

//			case FATIGUE : {
//				double fatigue = robot.getSystemCondition().getFatigue();
//				//result = new Float(fatigue).intValue();
//			if (isDead)	result = "";
//					else result = getFatigueStatus(fatigue);
//			} break;
//
//			case STRESS : {
//				double stress = robot.getSystemCondition().getStress();
//				//result = new Double(stress).intValue();
//				if (isDead)	result = "";
//					else result = getStressStatus(stress);
//			} break;
//
//			case PERFORMANCE : {
//				double performance = robot.getSystemCondition().getPerformanceFactor();
//				//result = new Float(performance * 100D).intValue();
//				if (isDead)	result = "";
//					else result = getPerformanceStatus(performance* 100D);
//			} break;

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

			case SETTLEMENT: {
				result = robot.getLocationTag().getLocale(); // getQuickLocation();//
			}
				break;

			case JOB: {
				result = RobotJob.getName(robot.getRobotType());
			}
				break;

			case TASK: {
				// If the Robot is dead, there is no Task Manager
				BotTaskManager mgr = robot.getBotMind().getBotTaskManager();
				result = ((mgr != null) ? mgr.getTaskDescription(false) : null);
			}
				break;

			case MISSION: {
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
	 * Give the status of a robot's hunger level
	 * 
	 * @param hunger
	 * @return status
	 */
	public String getHungerStatus(double hunger) {
		String status = "N/A";
		if (hunger < 200)
			status = Msg.getString("RobotTableModel.column.battery.level1");
		else if (hunger < 500)
			status = Msg.getString("RobotTableModel.column.battery.level2");
		else if (hunger < 1000)
			status = Msg.getString("RobotTableModel.column.battery.level3");
		else if (hunger < 2000)
			status = Msg.getString("RobotTableModel.column.battery.level4");
		else
			status = Msg.getString("RobotTableModel.column.battery.level5");
		// logger.info(" hunger pt : " + Math.round(hunger) + ", status : " + status);
		return status;
	}

//	/**
//	 * Give the status of a robot's fatigue level
//	 * @param fatigue
//	 * @return status
//
//	public String getFatigueStatus(double value) {
//		String status= "N/A";
//		if (value < 100) status = Msg.getString("RobotTableModel.column.fatigue.level1");
//		else if (value < 400) status = Msg.getString("RobotTableModel.column.fatigue.level2");
//		else if (value < 800) status = Msg.getString("RobotTableModel.column.fatigue.level3");
//		else if (value < 1200) status = Msg.getString("RobotTableModel.column.fatigue.level4");
//		else status = Msg.getString("RobotTableModel.column.fatigue.level5");
//		return status;
//	}

//	/**
//	 * Give the status of a robot's stress level
//	 * @param hunger
//	 * @return status
//
//	public String getStressStatus(double value) {
//		String status= "N/A";
//		if (value < 15) status = Msg.getString("RobotTableModel.column.stress.level1");
//		else if (value < 40) status = Msg.getString("RobotTableModel.column.stress.level2");
//		else if (value < 75) status = Msg.getString("RobotTableModel.column.stress.level3");
//		else if (value < 95) status = Msg.getString("RobotTableModel.column.stress.level4");
//		else status = Msg.getString("RobotTableModel.column.stress.level5");
//		return status;
//	}

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
		// logger.info(" Perf : " + Math.round(value) + " ; status : " + status);
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

		static final Map<UnitEventType, Integer> EVENT_COLUMN_MAPPING;// = new HashMap<String, Integer>(12);

		static {
			HashMap<UnitEventType, Integer> m = new HashMap<UnitEventType, Integer>();
			m.put(UnitEventType.NAME_EVENT, NAME);
			m.put(UnitEventType.LOCATION_EVENT, LOCATION);
			m.put(UnitEventType.ADD_ASSOCIATED_ROBOT_EVENT, SETTLEMENT);
			m.put(UnitEventType.REMOVE_ASSOCIATED_ROBOT_EVENT, SETTLEMENT);
			m.put(UnitEventType.HUNGER_EVENT, BATTERY);
			// m.put(UnitEventType.FATIGUE_EVENT, FATIGUE);
			// m.put(UnitEventType.STRESS_EVENT, STRESS);
			m.put(UnitEventType.PERFORMANCE_EVENT, PERFORMANCE);
			m.put(UnitEventType.JOB_EVENT, JOB);
			m.put(UnitEventType.TASK_EVENT, TASK);
			m.put(UnitEventType.TASK_NAME_EVENT, TASK);
			m.put(UnitEventType.TASK_ENDED_EVENT, TASK);
			m.put(UnitEventType.TASK_SUBTASK_EVENT, TASK);
			m.put(UnitEventType.MISSION_EVENT, MISSION);
			// m.put(UnitEventType.ILLNESS_EVENT, HEALTH);
			m.put(UnitEventType.DEATH_EVENT, HEALTH);
			EVENT_COLUMN_MAPPING = Collections.unmodifiableMap(m);
		}

		private final UnitEvent event;

		private final UnitTableModel tableModel;

		private RobotTableUpdater(UnitEvent event, UnitTableModel tableModel) {
			this.event = event;
			this.tableModel = tableModel;
		}

		@Override
		public void run() {
			UnitEventType eventType = event.getType();

			Integer column = EVENT_COLUMN_MAPPING.get(eventType);

//			int columnNum = -1;
//			if (eventType.equals(Unit.NAME_EVENT)) columnNum = NAME;
//			else if (eventType.equals(Unit.LOCATION_EVENT)) columnNum = LOCATION;
//			else if (eventType.equals(SystemCondition.HUNGER_EVENT)) columnNum = HUNGER;
//			else if (eventType.equals(SystemCondition.FATIGUE_EVENT)) columnNum = FATIGUE;
//			else if (eventType.equals(SystemCondition.STRESS_EVENT)) columnNum = STRESS;
//			else if (eventType.equals(SystemCondition.PERFORMANCE_EVENT)) columnNum = PERFORMANCE;
//			else if (eventType.equals(Mind.JOB_EVENT)) columnNum = JOB;
//			else if (eventType.equals(TaskManager.TASK_EVENT)) columnNum = TASK;
//			else if (eventType.equals(Mind.MISSION_EVENT)) columnNum = MISSION;
//			else if (eventType.equals(SystemCondition.ILLNESS_EVENT) ||

//			if (eventType == UnitEventType.DEATH_EVENT) {
//				if (event.getTarget() instanceof Robot) {
//					Unit unit = (Unit) event.getTarget();
//					String personName  = unit.getName();
//					String announcement = personName + " has just passed away. ";
//					//desktop.openMarqueeBanner(announcement);
//					System.out.println(announcement);
//				}
//			}

//			else if (eventType == UnitEventType.ILLNESS_EVENT) {
//				if (event.getTarget() instanceof Robot) {
//					Unit unit = (Unit) event.getTarget();
//					String personName  = unit.getName();
//					String announcement = personName + " got sick. ";
//					//desktop.disposeMarqueeBanner();
//					//desktop.openMarqueeBanner(announcement);
//					System.out.println(announcement);
//				}
//			}

			if (column != null && column > -1) {
				Unit unit = (Unit) event.getSource();
				tableModel.fireTableCellUpdated(tableModel.getUnitIndex(unit), column);
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

			if (eventType == UnitEventType.INVENTORY_STORING_UNIT_EVENT) {
				if (event.getTarget() instanceof Robot)
					addUnit((Unit) event.getTarget());
			} else if (eventType == UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT) {
				if (event.getTarget() instanceof Robot)
					removeUnit((Unit) event.getTarget());
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
			if (eventType == MissionEventType.ADD_MEMBER_EVENT)
				addUnit((Unit) event.getTarget());
			else if (eventType == MissionEventType.REMOVE_MEMBER_EVENT)
				removeUnit((Unit) event.getTarget());
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
			if (unit instanceof Robot) {
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
				if (event.getTarget() instanceof Robot)
					addUnit((Unit) event.getTarget());
			} else if (eventType == UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT) {
				if (event.getTarget() instanceof Robot)
					removeUnit((Unit) event.getTarget());
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
			if (eventType == UnitEventType.ADD_ASSOCIATED_ROBOT_EVENT)
				addUnit((Unit) event.getTarget());
			else if (eventType == UnitEventType.REMOVE_ASSOCIATED_ROBOT_EVENT)
				removeUnit((Unit) event.getTarget());
		}
	}
}