/*
 * Mars Simulation Project
 * PersonTableModel.java
 * @date 2022-08-20
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionEventType;
import org.mars_sim.msp.core.person.ai.mission.MissionListener;
import org.mars_sim.msp.core.person.ai.role.Role;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.ShiftSlot;
import org.mars_sim.msp.core.structure.ShiftSlot.WorkStatus;
import org.mars_sim.msp.core.vehicle.Crewable;

/**
 * The PersonTableModel that maintains a list of Person objects. By defaults the
 * source of the list is the Unit Manager. It maps key attributes of the Person
 * into Columns.
 */
@SuppressWarnings("serial")
public class PersonTableModel extends UnitTableModel<Person> {

	// Column indexes
	private static final int NAME = 0;
	private static final int TASK = 1;
	private static final int MISSION_COL = 2;
	private static final int JOB = 3;
	private static final int ROLE = 4;
	private static final int SHIFT = 5;
	private static final int LOCATION = 6;
	private static final int HEALTH = 7;
	private static final int ENERGY = 8;
	private static final int WATER = 9;
	private static final int FATIGUE = 10;
	private static final int STRESS = 11;
	private static final int PERFORMANCE = 12;
	private static final int EMOTION = 13;

	/** The number of Columns. */
	private static final int COLUMNCOUNT = 14;
	/** Names of Columns. */
	private static String[] columnNames;
	/** Types of Columns. */
	private static Class<?>[] columnTypes;
	private static Map<UnitEventType, Integer> eventColumnMapping;

	private static final String DEYDRATED = "Deydrated";
	private static final String STARVING = "Starving";
	
	/**
	 * The static initializer creates the name & type arrays.
	 */
	static {
		columnNames = new String[COLUMNCOUNT];
		columnTypes = new Class[COLUMNCOUNT];
		columnNames[NAME] = Msg.getString("PersonTableModel.column.name"); //$NON-NLS-1$
		columnTypes[NAME] = String.class;
		columnNames[HEALTH] = Msg.getString("PersonTableModel.column.health"); //$NON-NLS-1$
		columnTypes[HEALTH] = String.class;
		columnNames[ENERGY] = Msg.getString("PersonTableModel.column.energy"); //$NON-NLS-1$
		columnTypes[ENERGY] = String.class;
		columnNames[WATER] = Msg.getString("PersonTableModel.column.water"); //$NON-NLS-1$
		columnTypes[WATER] = String.class;
		columnNames[FATIGUE] = Msg.getString("PersonTableModel.column.fatigue"); //$NON-NLS-1$
		columnTypes[FATIGUE] = String.class;
		columnNames[STRESS] = Msg.getString("PersonTableModel.column.stress"); //$NON-NLS-1$
		columnTypes[STRESS] = String.class;
		columnNames[PERFORMANCE] = Msg.getString("PersonTableModel.column.performance"); //$NON-NLS-1$
		columnTypes[PERFORMANCE] = String.class;
		columnNames[EMOTION] = Msg.getString("PersonTableModel.column.emotion"); //$NON-NLS-1$
		columnTypes[EMOTION] = String.class;
		columnNames[LOCATION] = Msg.getString("PersonTableModel.column.location"); //$NON-NLS-1$
		columnTypes[LOCATION] = String.class;
		columnNames[ROLE] = Msg.getString("PersonTableModel.column.role"); //$NON-NLS-1$
		columnTypes[ROLE] = String.class;
		columnNames[JOB] = Msg.getString("PersonTableModel.column.job"); //$NON-NLS-1$
		columnTypes[JOB] = String.class;
		columnNames[SHIFT] = Msg.getString("PersonTableModel.column.shift"); //$NON-NLS-1$
		columnTypes[SHIFT] = String.class;
		columnNames[MISSION_COL] = Msg.getString("PersonTableModel.column.mission"); //$NON-NLS-1$
		columnTypes[MISSION_COL] = String.class;
		columnNames[TASK] = Msg.getString("PersonTableModel.column.task"); //$NON-NLS-1$
		columnTypes[TASK] = String.class;

		eventColumnMapping = new EnumMap<>(UnitEventType.class);
		eventColumnMapping.put(UnitEventType.NAME_EVENT, NAME);
		eventColumnMapping.put(UnitEventType.LOCATION_EVENT, LOCATION);
		eventColumnMapping.put(UnitEventType.HUNGER_EVENT, ENERGY);
		eventColumnMapping.put(UnitEventType.THIRST_EVENT, ENERGY);
		eventColumnMapping.put(UnitEventType.FATIGUE_EVENT, FATIGUE);
		eventColumnMapping.put(UnitEventType.STRESS_EVENT, STRESS);
		eventColumnMapping.put(UnitEventType.EMOTION_EVENT, EMOTION);
		eventColumnMapping.put(UnitEventType.PERFORMANCE_EVENT, PERFORMANCE);
		eventColumnMapping.put(UnitEventType.JOB_EVENT, JOB);
		eventColumnMapping.put(UnitEventType.ROLE_EVENT, ROLE);
		eventColumnMapping.put(UnitEventType.SHIFT_EVENT, SHIFT);
		eventColumnMapping.put(UnitEventType.TASK_EVENT, TASK);
		eventColumnMapping.put(UnitEventType.TASK_NAME_EVENT, TASK);
		eventColumnMapping.put(UnitEventType.TASK_DESCRIPTION_EVENT, TASK);
		eventColumnMapping.put(UnitEventType.TASK_ENDED_EVENT, TASK);
		eventColumnMapping.put(UnitEventType.MISSION_EVENT, MISSION_COL);
		eventColumnMapping.put(UnitEventType.ILLNESS_EVENT, HEALTH);
		eventColumnMapping.put(UnitEventType.DEATH_EVENT, HEALTH);
	}

	/** inner enum with valid source types. */
	private enum ValidSourceType {
		ALL_PEOPLE, VEHICLE_CREW, SETTLEMENT_INHABITANTS, SETTLEMENT_ALL_ASSOCIATED_PEOPLE, MISSION_PEOPLE;
	}

	private ValidSourceType sourceType;

	private Crewable vehicle;
	private Settlement settlement;
	private Mission mission;

	private UnitListener crewListener;
	private UnitListener settlementListener;
	private MissionListener missionListener;

	/**
	 * Constructs a PersonTableModel object that displays all people from the
	 * specified vehicle.
	 *
	 * @param vehicle Monitored vehicle Person objects.
	 */
	public PersonTableModel(Crewable vehicle) {
		
		super(UnitType.PERSON, Msg.getString("PersonTableModel.nameVehicle", //$NON-NLS-1$
				((Unit)vehicle).getName()), 
				"PersonTableModel.countingPeople", //$NON-NLS-1$
				columnNames, 
				columnTypes);

		setupCache();

		sourceType = ValidSourceType.VEHICLE_CREW;
		this.vehicle = vehicle;
		resetEntities(vehicle.getCrew());
		crewListener = new PersonChangeListener(UnitEventType.INVENTORY_STORING_UNIT_EVENT,
										UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT);
		((Unit) vehicle).addUnitListener(crewListener);
	}

	/**
	 * Constructs a PersonTableModel that displays residents are all associated
	 * people with a specified settlement.
	 *
	 * @param settlement    the settlement to check.
	 * @param allAssociated Are all people associated with this settlement to be
	 *                      displayed?
	 */
	public PersonTableModel(Settlement settlement, boolean allAssociated)  {
		super (UnitType.PERSON, (allAssociated ? Msg.getString("PersonTableModel.nameAllCitizens") //$NON-NLS-1$
				 : Msg.getString("PersonTableModel.nameIndoor", //$NON-NLS-1$
					settlement.getName())
				),
				(allAssociated ? "PersonTableModel.countingCitizens" : //$NON-NLS-1$
								 "PersonTableModel.countingIndoor" //$NON-NLS-1$
				), columnNames, columnTypes);
		setupCache();
		sourceType = (allAssociated ? ValidSourceType.SETTLEMENT_ALL_ASSOCIATED_PEOPLE
							: ValidSourceType.SETTLEMENT_INHABITANTS);

		setSettlementFilter(settlement);
	}

	/**
	 * Constructs a PersonTableModel object that displays all Person from the
	 * specified mission.
	 *
	 * @param mission Monitored mission Person objects.
	 */
	public PersonTableModel(Mission mission)  {
		super(UnitType.PERSON, Msg.getString("PersonTableModel.nameMission", //$NON-NLS-1$
				mission.getName()), "PersonTableModel.countingMissionMembers", //$NON-NLS-1$
				columnNames, columnTypes);
		
		setupCache();

		sourceType = ValidSourceType.MISSION_PEOPLE;
		this.mission = mission;
		Collection<Person> missionPeople = new ArrayList<>();
		for(Worker member : mission.getMembers()) {
			if (member.getUnitType() == UnitType.PERSON) {
				missionPeople.add((Person) member);
			}
		}
		resetEntities(missionPeople);
		missionListener = new LocalMissionListener();
		mission.addMissionListener(missionListener);
	}

	private void setupCache() {
		setCachedColumns(FATIGUE, PERFORMANCE);
	}

	@Override
	public boolean setSettlementFilter(Settlement filter) {	
		if ((sourceType != ValidSourceType.SETTLEMENT_ALL_ASSOCIATED_PEOPLE) &&
			(sourceType != ValidSourceType.SETTLEMENT_INHABITANTS)) {
				return false;
		}

		if (settlementListener != null) {
			if (settlement != null) {
				settlement.removeUnitListener(settlementListener);
			}
			settlementListener = null;
		}

		this.settlement = filter;
		if (settlement == null)
			return false;
		
		if (sourceType == ValidSourceType.SETTLEMENT_ALL_ASSOCIATED_PEOPLE) {
			resetEntities(settlement.getAllAssociatedPeople());
			settlementListener = new PersonChangeListener(UnitEventType.ADD_ASSOCIATED_PERSON_EVENT,
														UnitEventType.REMOVE_ASSOCIATED_PERSON_EVENT);
			settlement.addUnitListener(settlementListener);
		} else {
			resetEntities(settlement.getIndoorPeople());
			settlementListener = new PersonChangeListener(UnitEventType.INVENTORY_STORING_UNIT_EVENT,
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
	@Override
	public void unitUpdate(UnitEvent event) {
		UnitEventType eventType = event.getType();

		Integer column = eventColumnMapping.get(eventType);

		if (column != null && column > -1) {
			Person unit = (Person) event.getSource();
			entityValueUpdated(unit, column, column);
		}
	}

	/**
	 * Returns the value of a Cell.
	 *
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	@Override
	protected Object getEntityValue(Person person, int columnIndex) {
		Object result = null;

		switch (columnIndex) {

			case TASK: {
				// If the Person is dead, there is no Task Manager
				Task task = person.getMind().getTaskManager().getTask();
				result = ((task != null) ? task.getDescription() : "");
			}
			break;

			case MISSION_COL: {
				mission = person.getMind().getMission();
				if (mission != null) {
					result = mission.getFullMissionDesignation();
				}
			}
			break;

			case NAME: {
				result = person.getName();
			}
			break;

			case ENERGY: {
				PhysicalCondition pc = person.getPhysicalCondition();
				if (pc.isDead())
					result = "";
				else if (pc.isStarving())
					result = STARVING;
				else {
					result = PhysicalCondition.getHungerStatus(pc.getHunger(), pc.getEnergy());
				}
			}
			break;

			case WATER: {
				PhysicalCondition pc = person.getPhysicalCondition();
				if (pc.isDead())
					result = "";
				else if (pc.isDehydrated())
					result = DEYDRATED;
				else {
					result = PhysicalCondition.getThirstyStatus(pc.getThirst());
				}
			}
			break;

			case FATIGUE: {
				// double fatigue = person.getPhysicalCondition().getFatigue();
				// result = new Float(fatigue).intValue();
				if (person.getPhysicalCondition().isDead())
					result = "";
				else
					result = PhysicalCondition.getFatigueStatus(person.getPhysicalCondition().getFatigue());
			}
			break;

			case STRESS: {
				// double stress = person.getPhysicalCondition().getStress();
				// result = new Double(stress).intValue();
				if (person.getPhysicalCondition().isDead())
					result = "";
				else
					result = PhysicalCondition.getStressStatus(person.getPhysicalCondition().getStress());
			}
			break;

			case PERFORMANCE: {
				// double performance = person.getPhysicalCondition().getPerformanceFactor();
				// result = new Float(performance * 100D).intValue();
				if (person.getPhysicalCondition().isDead())
					result = "";
				else
					result = PhysicalCondition.getPerformanceStatus(person.getPhysicalCondition().getPerformanceFactor() * 100D);
			}
			break;

			case EMOTION: {
				if (person.getPhysicalCondition().isDead())
					result = "";
				else
					result = person.getMind().getEmotion().getDescription();
			}
			break;

			case HEALTH: {
				result = person.getPhysicalCondition().getHealthSituation();
			}
				break;

			case LOCATION: {
				result = person.getLocationTag().getQuickLocation();
			}
				break;

			case ROLE: {
				if (person.getPhysicalCondition().isDead())
					result = "";
				else {
					Role role = person.getRole();
					if (role != null) {
						result = role.getType().getName();
					} else {
						result = null;
					}
				}
			}
				break;

			case JOB: {
				// If person is dead, get job from death info.
				if (person.getPhysicalCondition().isDead())
					result = person.getPhysicalCondition().getDeathDetails().getJob().getName();
				else {
					if (person.getMind().getJob() != null)
						result = person.getMind().getJob().getName();
					else
						result = null;
				}
			}
				break;

			case SHIFT: {
				// If person is dead, disable it.
				if (person.getPhysicalCondition().isDead())
					result = "";
				else {
					ShiftSlot shift = person.getShiftSlot();
		
					if (shift.getStatus() == WorkStatus.ON_CALL) {
						result = "On Call";
					}
					else {
						result = shift.getShift().getName();
					}
				}
			}
		}
		return result;
	}


	
	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void destroy() {
		super.destroy();

		 if (sourceType == ValidSourceType.VEHICLE_CREW) {
			((Unit) vehicle).removeUnitListener(crewListener);
			crewListener = null;
			vehicle = null;
		} else if (sourceType == ValidSourceType.MISSION_PEOPLE) {
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
			Object target = event.getTarget();
			if (target instanceof Person p) {
				MissionEventType eventType = event.getType();

				if (eventType == MissionEventType.ADD_MEMBER_EVENT) {
					addEntity(p);
				}
				else if (eventType == MissionEventType.REMOVE_MEMBER_EVENT) {
					removeEntity(p);
				}
			}
		}
	}

	/**
	 * UnitListener inner class for watching Person move in/out of a Unit
	 */
	private class PersonChangeListener implements UnitListener {

		private UnitEventType addEvent;
		private UnitEventType removeEvent;

		public PersonChangeListener(UnitEventType addEvent, UnitEventType removeEvent) {
			this.addEvent = addEvent;
			this.removeEvent = removeEvent;
		}

		/**
		 * Catch unit update event.
		 *
		 * @param event the unit event.
		 */
		public void unitUpdate(UnitEvent event) {
			Object target = event.getTarget();
			if (target instanceof Person p) {
				UnitEventType eventType = event.getType();
				if (eventType == addEvent) {
					addEntity(p);
				}
				else if (eventType == removeEvent) {
					removeEntity(p);
				}
			}
		}
	}
}
