/**
 * Mars Simulation Project
 * PersonTableModel.java
 * @version 3.08 2015-07-02

 * @author Barry Evans
 */

package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.SwingUtilities;

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
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.Role;
import org.mars_sim.msp.core.person.RoleType;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionEventType;
import org.mars_sim.msp.core.person.ai.mission.MissionListener;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.task.TaskManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * The PersonTableModel that maintains a list of Person objects. By defaults
 * the source of the list is the Unit Manager.
 * It maps key attributes of the Person into Columns.
 */
public class PersonTableModel
extends UnitTableModel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private static final Logger logger = Logger.getLogger(PersonTableModel.class.getName());

//	private static MainDesktopPane desktop;

	// Column indexes
	/** Person name column. */
	private final static int NAME = 0;
	/** Location column. */
	private final static int LOCATION = 1;
	/** Role column. */
	private final static int ROLE = 2;
	/** Job column. */
	private final static int JOB = 3;
	/** Shift column. */
	private final static int SHIFT = 4;
	/** Task column. */
	private final static int TASK = 5;
	/** Mission column. */
	private final static int MISSION = 6;
	/** Gender column. */
	private final static int GENDER = 7;
	/** Personality column. */
	private final static int PERSONALITY = 8;
	/** Health column. */
	private final static int HEALTH = 9;
	/** Hunger column. */
	private final static int HUNGER = 10;
	/** Fatigue column. */
	private final static int FATIGUE = 11;
	/** Stress column. */
	private final static int STRESS = 12;
	/** Performance column. */
	private final static int PERFORMANCE = 13;

	/** The number of Columns. */
	private final static int COLUMNCOUNT = 14;
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
		columnNames[NAME] = Msg.getString("PersonTableModel.column.name"); //$NON-NLS-1$
		columnTypes[NAME] = String.class;
		columnNames[GENDER] = Msg.getString("PersonTableModel.column.gender"); //$NON-NLS-1$
		columnTypes[GENDER] = String.class;
		columnNames[PERSONALITY] = Msg.getString("PersonTableModel.column.personality"); //$NON-NLS-1$
		columnTypes[PERSONALITY] = String.class;
		columnNames[HEALTH] = Msg.getString("PersonTableModel.column.health"); //$NON-NLS-1$
		columnTypes[HEALTH] = String.class;
		columnNames[HUNGER] = Msg.getString("PersonTableModel.column.hunger"); //$NON-NLS-1$
		columnTypes[HUNGER] = String.class;
		columnNames[FATIGUE] = Msg.getString("PersonTableModel.column.fatigue"); //$NON-NLS-1$
		columnTypes[FATIGUE] = String.class;
		columnNames[STRESS] = Msg.getString("PersonTableModel.column.stress"); //$NON-NLS-1$
		columnTypes[STRESS] = String.class;
		columnNames[PERFORMANCE] = Msg.getString("PersonTableModel.column.performance"); //$NON-NLS-1$
		columnTypes[PERFORMANCE] = String.class;
		columnNames[LOCATION] = Msg.getString("PersonTableModel.column.location"); //$NON-NLS-1$
		columnTypes[LOCATION] = String.class;
		columnNames[ROLE] = Msg.getString("PersonTableModel.column.role"); //$NON-NLS-1$
		columnTypes[ROLE] = String.class;
		columnNames[JOB] = Msg.getString("PersonTableModel.column.job"); //$NON-NLS-1$
		columnTypes[JOB] = String.class;
		columnNames[SHIFT] = Msg.getString("PersonTableModel.column.shift"); //$NON-NLS-1$
		columnTypes[SHIFT] = String.class;
		columnNames[MISSION] = Msg.getString("PersonTableModel.column.mission"); //$NON-NLS-1$
		columnTypes[MISSION] = String.class;
		columnNames[TASK] = Msg.getString("PersonTableModel.column.task"); //$NON-NLS-1$
		columnTypes[TASK] = String.class;

	}

	/** inner enum with valid source types. */
	private enum ValidSourceType {
		ALL_PEOPLE,
		VEHICLE_CREW,
		SETTLEMENT_INHABITANTS,
		SETTLEMENT_ALL_ASSOCIATED_PEOPLE,
		MISSION_PEOPLE;
	}

	/*
    static final Map<String, Integer> EVENT_COLUMN_MAPPING;//= new HashMap<String, Integer>(12);

        static {
            HashMap<String, Integer> m = new HashMap<String, Integer>();
            m.put(Unit.NAME_EVENT, NAME);
            m.put(Unit.LOCATION_EVENT, LOCATION);
            m.put(PhysicalCondition.HUNGER_EVENT, HUNGER);
            m.put(PhysicalCondition.FATIGUE_EVENT, FATIGUE);
            m.put(PhysicalCondition.STRESS_EVENT, STRESS);
            m.put(PhysicalCondition.PERFORMANCE_EVENT, PERFORMANCE);
            m.put(Mind.JOB_EVENT, JOB);
            m.put(TaskManager.TASK_EVENT, TASK);
            m.put(Mind.MISSION_EVENT, MISSION);
            m.put(PhysicalCondition.ILLNESS_EVENT, HEALTH);
            m.put(PhysicalCondition.DEATH_EVENT, HEALTH);
                    EVENT_COLUMN_MAPPING = Collections.unmodifiableMap(m);
        }
	 */

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

	/** Map for caching a person's hunger, fatigue, stress and performance status strings. */
	private Map<Unit, Map<Integer, String>> performanceValueCache;

	/**
	 * constructor.
	 * Constructs a PersonTableModel object that displays all people in the simulation.
	 * @param unitManager Manager containing Person objects.
	 */
	public PersonTableModel(UnitManager unitManager, MainDesktopPane desktop ) {
		super(
			Msg.getString("PersonTableModel.tabName"), //$NON-NLS-1$
			"PersonTableModel.countingPeople", //$NON-NLS-1$
			columnNames,
			columnTypes
		);

		sourceType = ValidSourceType.ALL_PEOPLE;
		setSource(unitManager.getPeople());
		unitManagerListener = new LocalUnitManagerListener();
		unitManager.addUnitManagerListener(unitManagerListener);

		//2014-12-30 Added desktop
//		this.desktop = desktop;

	}

	/**
	 * Constructs a PersonTableModel object that displays all people from the
	 * specified vehicle.
	 * @param vehicle Monitored vehicle Person objects.
	 */
	public PersonTableModel(Crewable vehicle) {
		super(
			Msg.getString(
				"PersonTableModel.namePeople", //$NON-NLS-1$
				((Unit) vehicle).getName()
			),
			"PersonTableModel.countingPeople", //$NON-NLS-1$
			columnNames,
			columnTypes
		);

		sourceType = ValidSourceType.VEHICLE_CREW;
		this.vehicle = vehicle;
		setSource(vehicle.getCrew());
		crewListener = new LocalCrewListener();
		((Unit) vehicle).addUnitListener(crewListener);
	}

	/**
	 * Constructs a PersonTableModel that displays residents are all associated people with
	 * a specified settlement.
	 * @param settlement the settlement to check.
	 * @param allAssociated Are all people associated with this settlement to be displayed?
	 */
	public PersonTableModel(Settlement settlement, boolean allAssociated) {
		super(
			(
				allAssociated ?
				Msg.getString(
					"PersonTableModel.nameAssociatedPeople", //$NON-NLS-1$
					settlement.getName()
				) :
				Msg.getString(
					"PersonTableModel.namePeople", //$NON-NLS-1$
					settlement.getName()
				)
			),(
				allAssociated ?
				"PersonTableModel.countingAssociatedPeople" : //$NON-NLS-1$
				"PersonTableModel.countingResidents" //$NON-NLS-1$
			),
			columnNames,
			columnTypes
		);

		this.settlement = settlement;
		if (allAssociated) {
			sourceType = ValidSourceType.SETTLEMENT_ALL_ASSOCIATED_PEOPLE;
			setSource(settlement.getAllAssociatedPeople());
			settlementListener = new AssociatedSettlementListener();
			settlement.addUnitListener(settlementListener);
		}
		else {
			sourceType = ValidSourceType.SETTLEMENT_INHABITANTS;
			setSource(settlement.getInhabitants());
			settlementListener = new InhabitantSettlementListener();
			settlement.addUnitListener(settlementListener);
		}
	}

	/**
	 * Constructs a PersonTableModel object that displays all Person from the
	 * specified mission.
	 * @param mission Monitored mission Person objects.
	 */
	public PersonTableModel(Mission mission) {
		super(
			Msg.getString(
				"PersonTableModel.namePeople", //$NON-NLS-1$
				mission.getName()
			),
			"PersonTableModel.countingMissionMembers", //$NON-NLS-1$
			columnNames,
			columnTypes
		);

		sourceType = ValidSourceType.MISSION_PEOPLE;
		this.mission = mission;
		Collection<Person> missionPeople = new ArrayList<Person>();
		Iterator<MissionMember> i = mission.getMembers().iterator();
		while (i.hasNext()) {
		    MissionMember member = i.next();
		    if (member instanceof Person) {
		        missionPeople.add((Person) member);
		    }
		}
		setSource(missionPeople);
		missionListener = new LocalMissionListener();
		mission.addMissionListener(missionListener);
	}

	/**
	 * Defines the source data from this table
	 */
	private void setSource(Collection<Person> source) {
		Iterator<Person> iter = source.iterator();
		while(iter.hasNext()) addUnit(iter.next());
	}

	@Override
	protected void addUnit(Unit newUnit) {

	    if (performanceValueCache == null) {
	        performanceValueCache = new HashMap<Unit, Map<Integer, String>>();
	    }

	    if (!performanceValueCache.containsKey(newUnit)) {
	        try {
	            Map<Integer, String> performanceItemMap = new HashMap<Integer, String>(4);

	            Person person = (Person) newUnit;
	            PhysicalCondition condition = person.getPhysicalCondition();

	            double hunger = condition.getHunger();
	            double energy = condition.getEnergy();
	            String hungerString = getHungerStatus(hunger, energy);
	            performanceItemMap.put(HUNGER, hungerString);

	            double fatigue = condition.getFatigue();
	            String fatigueString = getFatigueStatus(fatigue);
	            performanceItemMap.put(FATIGUE, fatigueString);

	            double stress = condition.getStress();
	            String stressString = getStressStatus(stress);
	            performanceItemMap.put(STRESS, stressString);

	            double performance = condition.getPerformanceFactor() * 100D;
	            String performanceString = getPerformanceStatus(performance);
	            performanceItemMap.put(PERFORMANCE, performanceString);

	            performanceValueCache.put(newUnit, performanceItemMap);
	        }
	        catch (Exception e) {}
	    }
	    super.addUnit(newUnit);
	}

	@Override
	protected void removeUnit(Unit oldUnit) {

	    if (performanceValueCache == null) {
	        performanceValueCache = new HashMap<Unit, Map<Integer, String>>();
	    }
	    if (performanceValueCache.containsKey(oldUnit)) {
	        Map<Integer, String> performanceItemMap = performanceValueCache.get(oldUnit);
	        performanceItemMap.clear();
	        performanceValueCache.remove(oldUnit);
	    }
	    super.removeUnit(oldUnit);
	}

	/**
	 * Catch unit update event.
	 * @param event the unit event.
	 */
	public void unitUpdate(UnitEvent event) {
		SwingUtilities.invokeLater(new PersonTableUpdater(event, this));
		/*
		String eventType = event.getType();

            Integer column = EVENT_COLUMN_MAPPING.get(eventType);

//            int columnNum = -1;
//    		if (eventType.equals(Unit.NAME_EVENT)) columnNum = NAME;
//    		else if (eventType.equals(Unit.LOCATION_EVENT)) columnNum = LOCATION;
//    		else if (eventType.equals(PhysicalCondition.HUNGER_EVENT)) columnNum = HUNGER;
//    		else if (eventType.equals(PhysicalCondition.FATIGUE_EVENT)) columnNum = FATIGUE;
//    		else if (eventType.equals(PhysicalCondition.STRESS_EVENT)) columnNum = STRESS;
//    		else if (eventType.equals(PhysicalCondition.PERFORMANCE_EVENT)) columnNum = PERFORMANCE;
//    		else if (eventType.equals(Mind.JOB_EVENT)) columnNum = JOB;
//    		else if (eventType.equals(TaskManager.TASK_EVENT)) columnNum = TASK;
//    		else if (eventType.equals(Mind.MISSION_EVENT)) columnNum = MISSION;
//    		else if (eventType.equals(PhysicalCondition.ILLNESS_EVENT) ||
//    				eventType.equals(PhysicalCondition.DEATH_EVENT)) columnNum = HEALTH;

            if (column != null && column> -1) {
                Unit unit = (Unit) event.getSource();
                fireTableCellUpdated(getUnitIndex(unit), column);
            }
		 */
	}

	/**
	 * Return the value of a Cell
	 * @param rowIndex Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = null;

		if (rowIndex < getUnitNumber()) {
			Person person = (Person)getUnit(rowIndex);

			Boolean isDead = person.getPhysicalCondition().isDead();
			Boolean isStarving = person.getPhysicalCondition().isStarving();

			switch (columnIndex) {
			case NAME : {
				result = person.getName();
			} break;

			case GENDER : {
				String genderStr = person.getGender().getName();
				String letter;
				if (genderStr.equals("male")) letter = "M";
				else letter = "F";
				result = letter;
			} break;

			case PERSONALITY : {
				result = person.getMind().getMBTI().getTypeString();
			} break;

			case HUNGER : {
				double hunger = person.getPhysicalCondition().getHunger();
		        double energy = person.getPhysicalCondition().getEnergy();
				//result = new Float(hunger).intValue();
				if (isDead)	result = "";
				else if (isStarving) result = "Starving";
				else result = getHungerStatus(hunger, energy);
			} break;

			case FATIGUE : {
				double fatigue = person.getPhysicalCondition().getFatigue();
				//result = new Float(fatigue).intValue();
			if (isDead)	result = "";
					else result = getFatigueStatus(fatigue);
			} break;

			case STRESS : {
				double stress = person.getPhysicalCondition().getStress();
				//result = new Double(stress).intValue();
				if (isDead)	result = "";
					else result = getStressStatus(stress);
			} break;

			case PERFORMANCE : {
				double performance = person.getPhysicalCondition().getPerformanceFactor();
				//result = new Float(performance * 100D).intValue();
				if (isDead)	result = "";
					else result = getPerformanceStatus(performance* 100D);
			} break;

			case HEALTH : {

				result = person.getPhysicalCondition().getHealthSituation();
			} break;

			case LOCATION : {
				LocationSituation locationSituation = person.getLocationSituation();
				if (locationSituation == LocationSituation.IN_SETTLEMENT) {
					if (person.getParkedSettlement() != null) result = person.getParkedSettlement().getName();
				}
				else if (locationSituation == LocationSituation.IN_VEHICLE) {
					if (person.getVehicle() != null) result = person.getVehicle().getName();
				}
				else result = locationSituation.getName();
			} break;

			case ROLE : {
				if (person.getPhysicalCondition().isDead())
					result = "N/A";
				else {
					Role role = person.getRole();
					if (role != null) {
					    result = role.getType();
					}
					else {
					    result = null;
					}
				}
			} break;

			case JOB : {
				// If person is dead, get job from death info.
				if (person.getPhysicalCondition().isDead())
					result = person.getPhysicalCondition().getDeathDetails().getJob();
				else {
					if (person.getMind().getJob() != null) result = person.getMind().getJob().getName(person.getGender());
					else result = null;
				}
			} break;

			case SHIFT : {
				// If person is dead, disable it.
				if (person.getPhysicalCondition().isDead())
					result = ShiftType.OFF; //person.getPhysicalCondition().getDeathDetails().getJob();
				else {
					ShiftType shift = person.getTaskSchedule().getShiftType();
					if (shift != null) result = shift;
					else result = null;
				}
			} break;

			case TASK : {
				// If the Person is dead, there is no Task Manager
				TaskManager mgr = person.getMind().getTaskManager();
				result = ((mgr != null)? mgr.getTaskName() : null); // .getTaskDescription(true) // .getTaskClassName()		 	
			} break;

			case MISSION : {
				Mission mission = person.getMind().getMission();
				if (mission != null) {
					result = mission.getDescription();
				}
			} break;
			}
		}

		return result;
	}

	/**
	 * Give the status of a person's hunger level
	 * @param hunger
	 * @return status
	 */
	public String getHungerStatus(double hunger, double energy) {
		String status= "N/A";
		if (hunger < 100 || energy > 5000) status = Msg.getString("PersonTableModel.column.hunger.level1");
		else if (hunger < 500 || energy > 3000) status = Msg.getString("PersonTableModel.column.hunger.level2");
		else if (hunger < 1000 || energy > 1000) status = Msg.getString("PersonTableModel.column.hunger.level3");
		else if (hunger < 2000 || energy > 500) status = Msg.getString("PersonTableModel.column.hunger.level4");
		else status = Msg.getString("PersonTableModel.column.hunger.level5");
		//logger.info(" hunger pt : " + Math.round(hunger) + ", status : " + status);
		return status;
	}


	/**
	 * Give the status of a person's fatigue level
	 * @param fatigue
	 * @return status
	 */
	public String getFatigueStatus(double value) {
		String status= "N/A";
		if (value < 500) status = Msg.getString("PersonTableModel.column.fatigue.level1");
		else if (value < 800) status = Msg.getString("PersonTableModel.column.fatigue.level2");
		else if (value < 1200) status = Msg.getString("PersonTableModel.column.fatigue.level3");
		else if (value < 1500) status = Msg.getString("PersonTableModel.column.fatigue.level4");
		else status = Msg.getString("PersonTableModel.column.fatigue.level5");
		return status;
	}


	/**
	 * Give the status of a person's stress level
	 * @param hunger
	 * @return status
	 */
	public String getStressStatus(double value) {
		String status= "N/A";
		if (value < 15) status = Msg.getString("PersonTableModel.column.stress.level1");
		else if (value < 40) status = Msg.getString("PersonTableModel.column.stress.level2");
		else if (value < 75) status = Msg.getString("PersonTableModel.column.stress.level3");
		else if (value < 95) status = Msg.getString("PersonTableModel.column.stress.level4");
		else status = Msg.getString("PersonTableModel.column.stress.level5");
		return status;
	}


	/**
	 * Give the status of a person's hunger level
	 * @param hunger
	 * @return status
	 */
	public String getPerformanceStatus(double value) {
		String status= "N/A";
		if (value > 95 ) status = Msg.getString("PersonTableModel.column.performance.level1");
		else if (value > 75) status = Msg.getString("PersonTableModel.column.performance.level2");
		else if (value > 50) status = Msg.getString("PersonTableModel.column.performance.level3");
		else if (value > 25) status = Msg.getString("PersonTableModel.column.performance.level4");
		else if (value <= 25) status = Msg.getString("PersonTableModel.column.performance.level5");
		//logger.info(" Perf : " + Math.round(value) + " ; status : " + status);
		return status;
	}

	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void destroy() {
		super.destroy();

		if (sourceType == ValidSourceType.ALL_PEOPLE) {
			UnitManager unitManager = Simulation.instance().getUnitManager();
			unitManager.removeUnitManagerListener(unitManagerListener);
			unitManagerListener = null;
		}
		else if (sourceType == ValidSourceType.VEHICLE_CREW) {
			((Unit) vehicle).removeUnitListener(crewListener);
			crewListener = null;
			vehicle = null;
		}
		else if (sourceType == ValidSourceType.MISSION_PEOPLE) {
			mission.removeMissionListener(missionListener);
			missionListener = null;
			mission = null;
		}
		else {
			settlement.removeUnitListener(settlementListener);
			settlementListener = null;
			settlement = null;
		}

		if (performanceValueCache != null) {
		    performanceValueCache.clear();
        }
		performanceValueCache = null;
	}

	@Override
	public boolean equals(Object o) {
		boolean result = super.equals(o);

		if (o instanceof PersonTableModel) {
			PersonTableModel oModel = (PersonTableModel) o;
			if (!sourceType.equals(oModel.sourceType)) result = false;
		}

		return result;
	}

	/**
	 * Inner class for updating the person table.
	 */
	private static class PersonTableUpdater
	implements Runnable {

		static final Map<UnitEventType, Integer> EVENT_COLUMN_MAPPING;//= new HashMap<String, Integer>(12);

		static {
			HashMap<UnitEventType, Integer> m = new HashMap<UnitEventType, Integer>();
			m.put(UnitEventType.NAME_EVENT, NAME);
			m.put(UnitEventType.LOCATION_EVENT, LOCATION);
			m.put(UnitEventType.HUNGER_EVENT, HUNGER);
			m.put(UnitEventType.FATIGUE_EVENT, FATIGUE);
			m.put(UnitEventType.STRESS_EVENT, STRESS);
			m.put(UnitEventType.PERFORMANCE_EVENT, PERFORMANCE);
			m.put(UnitEventType.JOB_EVENT, JOB);
			m.put(UnitEventType.ROLE_EVENT, ROLE);
			m.put(UnitEventType.SHIFT_EVENT, SHIFT);
			m.put(UnitEventType.TASK_EVENT, TASK);
			m.put(UnitEventType.TASK_NAME_EVENT, TASK);
			m.put(UnitEventType.TASK_ENDED_EVENT, TASK);
			m.put(UnitEventType.TASK_SUBTASK_EVENT, TASK);
			m.put(UnitEventType.MISSION_EVENT, MISSION);
			m.put(UnitEventType.ILLNESS_EVENT, HEALTH);
			m.put(UnitEventType.DEATH_EVENT, HEALTH);
			EVENT_COLUMN_MAPPING = Collections.unmodifiableMap(m);
		}

		private final UnitEvent event;

		private final PersonTableModel tableModel;

		private PersonTableUpdater(UnitEvent event, PersonTableModel tableModel) {
			this.event = event;
			this.tableModel = tableModel;
		}

		@Override
		public void run() {
			UnitEventType eventType = event.getType();

			Integer column = EVENT_COLUMN_MAPPING.get(eventType);
			/*
			int columnNum = -1;
			if (eventType.equals(Unit.NAME_EVENT)) columnNum = NAME;
			else if (eventType.equals(Unit.LOCATION_EVENT)) columnNum = LOCATION;
			else if (eventType.equals(PhysicalCondition.HUNGER_EVENT)) columnNum = HUNGER;
			else if (eventType.equals(PhysicalCondition.FATIGUE_EVENT)) columnNum = FATIGUE;
			else if (eventType.equals(PhysicalCondition.STRESS_EVENT)) columnNum = STRESS;
			else if (eventType.equals(PhysicalCondition.PERFORMANCE_EVENT)) columnNum = PERFORMANCE;
			else if (eventType.equals(Mind.JOB_EVENT)) columnNum = JOB;
			else if (eventType.equals(TaskManager.TASK_EVENT)) columnNum = TASK;
			else if (eventType.equals(Mind.MISSION_EVENT)) columnNum = MISSION;
			else if (eventType.equals(PhysicalCondition.ILLNESS_EVENT) ||
			*/

			if (eventType == UnitEventType.DEATH_EVENT) {
				if (event.getTarget() instanceof Person) {
					Unit unit = (Unit) event.getTarget();
					String personName  = unit.getName();
					String announcement = personName + " has just passed away. ";
					//desktop.openMarqueeBanner(announcement);
					System.out.println(announcement);
				}
			}
			else if (eventType == UnitEventType.ILLNESS_EVENT) {
				if (event.getTarget() instanceof Person) {
					Unit unit = (Unit) event.getTarget();
					String personName  = unit.getName();
					String announcement = personName + " got sick.";
					//desktop.disposeMarqueeBanner();
					//desktop.openMarqueeBanner(announcement);
					System.out.println(announcement);
				}
			}
			else if (eventType == UnitEventType.JOB_EVENT) {
				if (event.getTarget() instanceof Person) {
					Unit unit = (Unit) event.getTarget();
					String personName  = unit.getName();
					String announcement = personName + " just got a new job.";
					//desktop.disposeMarqueeBanner();
					//desktop.openMarqueeBanner(announcement);
					System.out.println(announcement);
				}
			}
			else if (eventType == UnitEventType.ROLE_EVENT) {
				if (event.getTarget() instanceof Person) {
					Unit unit = (Unit) event.getTarget();
					String personName  = unit.getName();
					String announcement = personName + " just got a new role type.";
					System.out.println(announcement);
				}
			}
			else if (eventType == UnitEventType.SHIFT_EVENT) {
				if (event.getTarget() instanceof Person) {
					Unit unit = (Unit) event.getTarget();
					String personName  = unit.getName();
					String announcement = personName + " was just assigned a new work shift.";
					System.out.println(announcement);
				}
			}
			else if (eventType == UnitEventType.HUNGER_EVENT) {
			    Person person = (Person) event.getSource();
			    double hunger = person.getPhysicalCondition().getHunger();
			    double energy = person.getPhysicalCondition().getEnergy();
			    String hungerString = tableModel.getHungerStatus(hunger, energy);
			    if ((tableModel.performanceValueCache != null) &&
			            tableModel.performanceValueCache.containsKey(person)) {
			        Map<Integer, String> performanceItemMap = tableModel.performanceValueCache.get(person);
			        String oldHungerString = performanceItemMap.get(HUNGER);
			        if (hungerString.equals(oldHungerString)) {
			            return;
			        }
			        else {
			            performanceItemMap.put(HUNGER, hungerString);
			        }
			    }
			}
			else if (eventType == UnitEventType.FATIGUE_EVENT) {
                Person person = (Person) event.getSource();
                double fatigue = person.getPhysicalCondition().getFatigue();
                String fatigueString = tableModel.getFatigueStatus(fatigue);
                if ((tableModel.performanceValueCache != null) &&
                        tableModel.performanceValueCache.containsKey(person)) {
                    Map<Integer, String> performanceItemMap = tableModel.performanceValueCache.get(person);
                    String oldFatigueString = performanceItemMap.get(FATIGUE);
                    if (fatigueString.equals(oldFatigueString)) {
                        return;
                    }
                    else {
                        performanceItemMap.put(FATIGUE, fatigueString);
                    }
                }
            }
			else if (eventType == UnitEventType.STRESS_EVENT) {
                Person person = (Person) event.getSource();
                double stress = person.getPhysicalCondition().getStress();
                String stressString = tableModel.getStressStatus(stress);
                if ((tableModel.performanceValueCache != null) &&
                        tableModel.performanceValueCache.containsKey(person)) {
                    Map<Integer, String> performanceItemMap = tableModel.performanceValueCache.get(person);
                    String oldStressString = performanceItemMap.get(STRESS);
                    if (stressString.equals(oldStressString)) {
                        return;
                    }
                    else {
                        performanceItemMap.put(STRESS, stressString);
                    }
                }
            }
			else if (eventType == UnitEventType.PERFORMANCE_EVENT) {
                Person person = (Person) event.getSource();
                double performance = person.getPhysicalCondition().getPerformanceFactor() * 100D;
                String performanceString = tableModel.getPerformanceStatus(performance);
                if ((tableModel.performanceValueCache != null) &&
                        tableModel.performanceValueCache.containsKey(person)) {
                    Map<Integer, String> performanceItemMap = tableModel.performanceValueCache.get(person);
                    String oldStressString = performanceItemMap.get(PERFORMANCE);
                    if (performanceString.equals(oldStressString)) {
                        return;
                    }
                    else {
                        performanceItemMap.put(PERFORMANCE, performanceString);
                    }
                }
            }

			if (column != null && column> -1) {
				Unit unit = (Unit) event.getSource();
				tableModel.fireTableCellUpdated(tableModel.getUnitIndex(unit), column);
			}
		}
	}

	/**
	 * UnitListener inner class for crewable vehicle.
	 */
	private class LocalCrewListener
	implements UnitListener {

		/**
		 * Catch unit update event.
		 * @param event the unit event.
		 */
		public void unitUpdate(UnitEvent event) {
			UnitEventType eventType = event.getType();

			if (eventType == UnitEventType.INVENTORY_STORING_UNIT_EVENT) {
				if (event.getTarget() instanceof Person) addUnit((Unit) event.getTarget());
			}
			else if (eventType == UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT) {
				if (event.getTarget() instanceof Person) removeUnit((Unit) event.getTarget());
			}
		}
	}

	/**
	 * MissionListener inner class.
	 */
	private class LocalMissionListener
	implements MissionListener {

		/**
		 * Catch mission update event.
		 * @param event the mission event.
		 */
		public void missionUpdate(MissionEvent event) {
			MissionEventType eventType = event.getType();
			if (eventType == MissionEventType.ADD_MEMBER_EVENT) addUnit((Unit) event.getTarget());
			else if (eventType == MissionEventType.REMOVE_MEMBER_EVENT) removeUnit((Unit) event.getTarget());
		}
	}

	/**
	 * UnitManagerListener inner class.
	 */
	private class LocalUnitManagerListener
	implements UnitManagerListener {

		/**
		 * Catch unit manager update event.
		 * @param event the unit event.
		 */
		public void unitManagerUpdate(UnitManagerEvent event) {
			Unit unit = event.getUnit();
			UnitManagerEventType eventType = event.getEventType();
			if (unit instanceof Person) {
				if (eventType == UnitManagerEventType.ADD_UNIT) {
					if (!containsUnit(unit)) addUnit(unit);
				}
				else if (eventType == UnitManagerEventType.REMOVE_UNIT) {
					if (containsUnit(unit)) removeUnit(unit);
				}
			}
		}
	}

	/**
	 * UnitListener inner class for settlements for all inhabitants list.
	 */
	private class InhabitantSettlementListener
	implements UnitListener {

		/**
		 * Catch unit update event.
		 * @param event the unit event.
		 */
		public void unitUpdate(UnitEvent event) {
			UnitEventType eventType = event.getType();
			if (eventType == UnitEventType.INVENTORY_STORING_UNIT_EVENT) {
				if (event.getTarget() instanceof Person) addUnit((Unit) event.getTarget());
			}
			else if (eventType == UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT) {
				if (event.getTarget() instanceof Person) removeUnit((Unit) event.getTarget());
			}
		}
	}

	/**
	 * UnitListener inner class for settlements for associated people list.
	 */
	private class AssociatedSettlementListener
	implements UnitListener {

		/**
		 * Catch unit update event.
		 * @param event the unit event.
		 */
		public void unitUpdate(UnitEvent event) {
			UnitEventType eventType = event.getType();
			if (eventType == UnitEventType.ADD_ASSOCIATED_PERSON_EVENT)
				addUnit((Unit) event.getTarget());
			else if (eventType == UnitEventType.REMOVE_ASSOCIATED_PERSON_EVENT)
				removeUnit((Unit) event.getTarget());
		}
	}
}