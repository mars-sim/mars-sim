/**
 * Mars Simulation Project
 * PersonTableModel.java
 * @version 3.06 2014-01-29
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionEventType;
import org.mars_sim.msp.core.person.ai.mission.MissionListener;
import org.mars_sim.msp.core.person.ai.task.TaskManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.Crewable;

/**
 * The PersonTableModel that maintains a list of Person objects. By defaults
 * the source of the list is the Unit Manager.
 * It maps key attributes of the Person into Columns.
 */
public class PersonTableModel extends UnitTableModel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Column indexes
	/** Person name column. */
	private final static int NAME = 0;
	/** Gender column. */
	private final static int GENDER = 1;
	/** Location column. */
	private final static int LOCATION = 2;
	/** Personality column. */
	private final static int PERSONALITY = 3;
	/** Hunger column. */
	private final static int HUNGER = 4;
	/** Fatigue column. */
	private final static int FATIGUE = 5;
	/** Stress column. */
	private final static int STRESS = 6;
	/** Performance column. */
	private final static int PERFORMANCE = 7;
	/** Job column. */
	private final static int JOB = 8;
	/** Task column. */
	private final static int TASK = 9;
	/** Mission column. */
	private final static int MISSION = 10;
	/** Health column. */
	private final static int HEALTH = 11;
	/** The number of Columns. */
	private final static int COLUMNCOUNT = 12;
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
		columnNames[NAME] = "Name";
		columnTypes[NAME] = String.class;
		columnNames[GENDER] = "Gender";
		columnTypes[GENDER] = String.class;
		columnNames[PERSONALITY] = "Personality";
		columnTypes[PERSONALITY] = String.class; 
		columnNames[HUNGER] = "Hunger";
		columnTypes[HUNGER] = Integer.class;
		columnNames[FATIGUE] = "Fatigue";
		columnTypes[FATIGUE] = Integer.class;
		columnNames[STRESS] = "Stress %";
		columnTypes[STRESS] = Integer.class;
		columnNames[PERFORMANCE] = "Performance %";
		columnTypes[PERFORMANCE] = Integer.class;
		columnNames[LOCATION] = "Location";
		columnTypes[LOCATION] = String.class;
		columnNames[JOB] = "Job";
		columnTypes[JOB] = String.class;
		columnNames[MISSION] = "Mission";
		columnTypes[MISSION] = String.class;
		columnNames[TASK] = "Task";
		columnTypes[TASK] = String.class;
		columnNames[HEALTH] = "Health";
		columnTypes[HEALTH] = String.class;
	}

	// Valid source types.
	private static final String ALL_PEOPLE = "All People";
	private static final String VEHICLE_CREW = "Vehicle Crew";
	private static final String SETTLEMENT_INHABITANTS = "Settlement Inhabitants";
	private static final String SETTLEMENT_ALL_ASSOCIATED_PEOPLE = "All People Associated with Settlement";
	private static final String MISSION_PEOPLE = "Mission People";

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

	private String sourceType; // The type of source for the people table.

	// List sources.
	private Crewable vehicle;
	private Settlement settlement;
	private Mission mission;
	private UnitListener crewListener;
	private UnitListener settlementListener;
	private MissionListener missionListener;
	private UnitManagerListener unitManagerListener;

	/**
	 * constructor.
	 * Constructs a PersonTableModel object that displays all people in the simulation.
	 * @param unitManager Manager containing Person objects.
	 */
	public PersonTableModel(UnitManager unitManager) {
		super("All People", " people", columnNames, columnTypes);

		sourceType = ALL_PEOPLE;
		setSource(unitManager.getPeople());
		unitManagerListener = new LocalUnitManagerListener();
		unitManager.addUnitManagerListener(unitManagerListener);
	}

	/**
	 * Constructs a PersonTableModel object that displays all people from the
	 * specified vehicle.
	 * @param vehicle Monitored vehicle Person objects.
	 */
	public PersonTableModel(Crewable vehicle) {
		super(((Unit) vehicle).getName() + " - People", " people",
				columnNames, columnTypes);

		sourceType = VEHICLE_CREW;
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
		super(settlement.getName() + (allAssociated ? " - All Associated People" : " - People"), 
				(allAssociated ? " associated people" : " residents"), columnNames, columnTypes);

		this.settlement = settlement;
		if (allAssociated) {
			sourceType = SETTLEMENT_ALL_ASSOCIATED_PEOPLE;
			setSource(settlement.getAllAssociatedPeople());
			settlementListener = new AssociatedSettlementListener();
			settlement.addUnitListener(settlementListener);
		}
		else {
			sourceType = SETTLEMENT_INHABITANTS;
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
		super(mission.getName() + " - People", " mission members",
				columnNames, columnTypes);

		sourceType = MISSION_PEOPLE;
		this.mission = mission;
		setSource(mission.getPeople());
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

			// Invoke the appropriate method, switch is the best solution
			// although disliked by some
			switch (columnIndex) {
			case NAME : {
				result = person.getName();
			} break;

			case GENDER : {
				String genderStr = person.getGender().substring(0, 1).toUpperCase() +
						person.getGender().substring(1);
				result = genderStr;
			} break;

			case PERSONALITY : {
				result = person.getMind().getPersonalityType().getTypeString();
			} break;

			case HUNGER : {
				double hunger = person.getPhysicalCondition().getHunger();
				result = new Float(hunger).intValue();
			} break;

			case FATIGUE : {
				double fatigue = person.getPhysicalCondition().getFatigue();
				result = new Float(fatigue).intValue();
			} break;

			case STRESS : {
				double stress = person.getPhysicalCondition().getStress();
				result = new Double(stress).intValue();
			} break;

			case PERFORMANCE : {
				double performance = person.getPhysicalCondition().getPerformanceFactor();
				result = new Float(performance * 100D).intValue();
			} break;

			case HEALTH : {
				result = person.getPhysicalCondition().getHealthSituation();
			} break;

			case LOCATION : {
				String locationSituation = person.getLocationSituation();
				if (locationSituation.equals(Person.INSETTLEMENT)) {
					if (person.getSettlement() != null) result = person.getSettlement().getName();
				}
				else if (locationSituation.equals(Person.INVEHICLE)) {
					if (person.getVehicle() != null) result = person.getVehicle().getName();
				}
				else result = locationSituation;
			} break;

			case JOB : {
				// If person is dead, get job from death info.
				if (person.getPhysicalCondition().isDead()) 
					result = person.getPhysicalCondition().getDeathDetails().getJob();
				else {
					if (person.getMind().getJob() != null) result = person.getMind().getJob().getName();
					else result = null;
				} 
			} break;

			case TASK : {
				// If the Person is dead, there is no Task Manager
				TaskManager mgr = person.getMind().getTaskManager();
				result = ((mgr != null)? mgr.getTaskName() : null);
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
	 * Prepares the model for deletion.
	 */
	public void destroy() {
		super.destroy();

		if (sourceType.equals(ALL_PEOPLE)) {
			UnitManager unitManager = Simulation.instance().getUnitManager();
			unitManager.removeUnitManagerListener(unitManagerListener);
			unitManagerListener = null;
		}
		else if (sourceType.equals(VEHICLE_CREW)) {
			((Unit) vehicle).removeUnitListener(crewListener);
			crewListener = null;
			vehicle = null;
		}
		else if (sourceType.equals(MISSION_PEOPLE)) {
			mission.removeMissionListener(missionListener);
			missionListener = null;
			mission = null;
		}
		else {
			settlement.removeUnitListener(settlementListener);
			settlementListener = null;
			settlement = null;
		}
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
			m.put(UnitEventType.TASK_EVENT, TASK);
			m.put(UnitEventType.TASK_ENDED_EVENT, TASK);
			m.put(UnitEventType.TASK_SUBTASK_EVENT, TASK);
			m.put(UnitEventType.MISSION_EVENT, MISSION);
			m.put(UnitEventType.ILLNESS_EVENT, HEALTH);
			m.put(UnitEventType.DEATH_EVENT, HEALTH);
			EVENT_COLUMN_MAPPING = Collections.unmodifiableMap(m);
		}

		private final UnitEvent event;

		private final UnitTableModel tableModel;

		private PersonTableUpdater(UnitEvent event, UnitTableModel tableModel) {
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
			eventType.equals(PhysicalCondition.DEATH_EVENT)) columnNum = HEALTH;
			*/
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