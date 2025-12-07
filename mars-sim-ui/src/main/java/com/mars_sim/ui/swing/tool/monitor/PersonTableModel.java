/*
 * Mars Simulation Project
 * PersonTableModel.java
 * @date 2024-07-21
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.Unit;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.person.ai.mission.AbstractMission;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.PhysicalConditionFormat;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.person.ai.mission.AbstractMission;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.person.ai.role.Role;
import com.mars_sim.core.person.ai.shift.ShiftSlot;
import com.mars_sim.core.person.ai.shift.ShiftSlot.WorkStatus;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskManager;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.ui.swing.utils.ColumnSpec;
import com.mars_sim.ui.swing.utils.RatingScoreRenderer;

/**
 * The PersonTableModel that maintains a list of Person objects. By defaults the
 * source of the list is the Unit Manager. It maps key attributes of the Person
 * into Columns.
 */
@SuppressWarnings("serial")
public class PersonTableModel extends UnitTableModel<Person> {

	// Column indexes
	private static final int NAME = 0;
	private static final int SETTLEMENT = NAME+1;
	private static final int TASK_DESC = SETTLEMENT+1;
	private static final int MISSION_COL = TASK_DESC+1;
	private static final int JOB = MISSION_COL+1;
	private static final int ROLE = JOB+1;
	private static final int SHIFT = ROLE+1;
	private static final int LOCATION = SHIFT+1;
	private static final int LOCALE = LOCATION+1;
	private static final int HEALTH = LOCALE+1;
	private static final int FATIGUE = HEALTH+1;
	private static final int ENERGY = FATIGUE+1;
	private static final int WATER = ENERGY+1;
	private static final int STRESS = WATER+1;
	private static final int PERFORMANCE = STRESS+1;
	private static final int EMOTION = PERFORMANCE+1;

	/** The number of Columns. */
	private static final int COLUMNCOUNT = EMOTION+1;
	/** Names of Columns. */
	private static final ColumnSpec[] COLUMNS;

	private static final Map<String, Integer> EVENT_COLUMN_MAPPING;

	private static final String DEHYDRATED = "Dehydrated";
	private static final String STARVING = "Starving";
	
	/**
	 * The static initializer creates the name & type arrays.
	 */
	static {
		COLUMNS = new ColumnSpec[COLUMNCOUNT];
		COLUMNS[NAME] = new ColumnSpec(Msg.getString("PersonTableModel.column.name"), String.class);
		COLUMNS[SETTLEMENT] = new ColumnSpec("Settlement", String.class);
		COLUMNS[HEALTH] = new ColumnSpec(Msg.getString("PersonTableModel.column.health"), String.class);
		COLUMNS[ENERGY] = new ColumnSpec(Msg.getString("PersonTableModel.column.energy"), String.class);
		COLUMNS[WATER] = new ColumnSpec(Msg.getString("PersonTableModel.column.water"), String.class);
		COLUMNS[FATIGUE] = new ColumnSpec(Msg.getString("PersonTableModel.column.fatigue"), String.class);
		COLUMNS[STRESS] = new ColumnSpec(Msg.getString("PersonTableModel.column.stress"), String.class);
		COLUMNS[PERFORMANCE] = new ColumnSpec(Msg.getString("PersonTableModel.column.performance"), String.class);
		COLUMNS[EMOTION] = new ColumnSpec(Msg.getString("PersonTableModel.column.emotion"), String.class);
		COLUMNS[LOCATION] = new ColumnSpec(Msg.getString("PersonTableModel.column.location"), String.class);
		COLUMNS[LOCALE] = new ColumnSpec(Msg.getString("PersonTableModel.column.locale"), String.class);
		COLUMNS[ROLE] = new ColumnSpec(Msg.getString("PersonTableModel.column.role"), String.class);
		COLUMNS[JOB] = new ColumnSpec(Msg.getString("PersonTableModel.column.job"), String.class);
		COLUMNS[SHIFT] = new ColumnSpec(Msg.getString("PersonTableModel.column.shift"), String.class);
		COLUMNS[MISSION_COL] = new ColumnSpec(Msg.getString("PersonTableModel.column.mission"), String.class);
		COLUMNS[TASK_DESC] = new ColumnSpec(Msg.getString("PersonTableModel.column.task"), String.class);

		EVENT_COLUMN_MAPPING = new HashMap<>();
		EVENT_COLUMN_MAPPING.put(EntityEventType.NAME_EVENT, NAME);
		EVENT_COLUMN_MAPPING.put(EntityEventType.COORDINATE_EVENT, LOCATION);
		EVENT_COLUMN_MAPPING.put(EntityEventType.HUNGER_EVENT, ENERGY);
		EVENT_COLUMN_MAPPING.put(EntityEventType.THIRST_EVENT, ENERGY);
		EVENT_COLUMN_MAPPING.put(EntityEventType.FATIGUE_EVENT, FATIGUE);
		EVENT_COLUMN_MAPPING.put(EntityEventType.STRESS_EVENT, STRESS);
		EVENT_COLUMN_MAPPING.put(EntityEventType.EMOTION_EVENT, EMOTION);
		EVENT_COLUMN_MAPPING.put(EntityEventType.PERFORMANCE_EVENT, PERFORMANCE);
		EVENT_COLUMN_MAPPING.put(EntityEventType.JOB_EVENT, JOB);
		EVENT_COLUMN_MAPPING.put(Role.ROLE_EVENT, ROLE);
		EVENT_COLUMN_MAPPING.put(EntityEventType.SHIFT_EVENT, SHIFT);
		EVENT_COLUMN_MAPPING.put(TaskManager.TASK_EVENT, TASK_DESC);
		EVENT_COLUMN_MAPPING.put(EntityEventType.TASK_NAME_EVENT, TASK_DESC);
		EVENT_COLUMN_MAPPING.put(EntityEventType.TASK_DESCRIPTION_EVENT, TASK_DESC);
		EVENT_COLUMN_MAPPING.put(EntityEventType.TASK_ENDED_EVENT, TASK_DESC);
		EVENT_COLUMN_MAPPING.put(EntityEventType.MISSION_EVENT, MISSION_COL);
		EVENT_COLUMN_MAPPING.put(EntityEventType.ILLNESS_EVENT, HEALTH);
		EVENT_COLUMN_MAPPING.put(EntityEventType.DEATH_EVENT, HEALTH);
		EVENT_COLUMN_MAPPING.put(EntityEventType.BURIAL_EVENT, HEALTH);
		EVENT_COLUMN_MAPPING.put(EntityEventType.REVIVED_EVENT, HEALTH);
	}

	/** 
	 * Inner enum with valid source types. 
	 */
	private enum ValidSourceType {
		ALL_PEOPLE, VEHICLE_CREW, SETTLEMENT_ALL_ASSOCIATED_PEOPLE, MISSION_PEOPLE;
	}

	private ValidSourceType sourceType;

	private boolean isLiveCB = true;
	private boolean isDeceasedCB = false;
	private boolean isBuriedCB = false;
	
	private transient Crewable vehicle;
	private Set<Settlement> settlements = Collections.emptySet();
	private Mission mission;

	private transient EntityListener crewListener;
	private transient EntityListener settlementListener;
	private transient EntityListener missionListener;

	/**
	 * Constructs a PersonTableModel that displays residents are all associated
	 * people with a specified settlement.
	 *
	 */
	public PersonTableModel()  {
		super (UnitType.PERSON, Msg.getString("PersonTableModel.nameAllCitizens"),
				"PersonTableModel.countingCitizens", COLUMNS);
		setupCache();
		
		setSettlementColumn(SETTLEMENT);

		sourceType = ValidSourceType.SETTLEMENT_ALL_ASSOCIATED_PEOPLE;
	}
	
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
				COLUMNS);

		setupCache();

		sourceType = ValidSourceType.VEHICLE_CREW;
		this.vehicle = vehicle;
		
		Collection<Person> crew = CollectionUtils.sortByName(vehicle.getCrew());
		
		resetEntities(crew);
		
		crewListener = new PersonChangeListener(EntityEventType.INVENTORY_STORING_UNIT_EVENT,
										EntityEventType.INVENTORY_RETRIEVING_UNIT_EVENT);
		((Unit) vehicle).addEntityListener(crewListener);
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
				COLUMNS);
		
		setupCache();

		sourceType = ValidSourceType.MISSION_PEOPLE;
		this.mission = mission;
		Collection<Person> missionPeople = new ArrayList<>();
		for(Worker member : mission.getMembers()) {
			if (member.getUnitType() == UnitType.PERSON) {
				missionPeople.add((Person) member);
			}
		}
		
		CollectionUtils.sortByName(missionPeople);
		resetEntities(missionPeople);
		
		missionListener = new LocalMissionListener();
		mission.addEntityListener(missionListener);
	}

	private void setupCache() {
		setCachedColumns(FATIGUE, PERFORMANCE);
	}

	@Override
	public boolean setSettlementFilter(Set<Settlement> filter) {	

		if (settlementListener != null) {
			settlements.forEach(s -> s.removeEntityListener(settlementListener));
			settlementListener = null;
		}

		this.settlements = filter;
		
		Collection<Person> entities = null;
		
		if (isLiveCB) {
			if (sourceType == ValidSourceType.SETTLEMENT_ALL_ASSOCIATED_PEOPLE) {

				entities = settlements.stream()
								.map(Settlement::getAllAssociatedPeople)
								.flatMap(Collection::stream)
								.sorted(Comparator.comparing(Person::getName))
								.collect(Collectors.toList());
				settlementListener = new PersonChangeListener(EntityEventType.ADD_ASSOCIATED_PERSON_EVENT,
										EntityEventType.REMOVE_ASSOCIATED_PERSON_EVENT);
			}
			else {

				entities = settlements.stream()
								.map(Settlement::getIndoorPeople)
								.flatMap(Collection::stream)
								.sorted(Comparator.comparing(Person::getName))
								.collect(Collectors.toList());
				settlementListener = new PersonChangeListener(EntityEventType.INVENTORY_STORING_UNIT_EVENT,
												EntityEventType.INVENTORY_RETRIEVING_UNIT_EVENT);
			}
		}
		else if (isDeceasedCB) {
			if (sourceType == ValidSourceType.SETTLEMENT_ALL_ASSOCIATED_PEOPLE) {

				entities = settlements.stream()
								.map(Settlement::getDeceasedPeople)
								.flatMap(Collection::stream)
								.sorted(Comparator.comparing(Person::getName))
								.collect(Collectors.toList());
				settlementListener = new PersonChangeListener(EntityEventType.ADD_ASSOCIATED_PERSON_EVENT,
										EntityEventType.REMOVE_ASSOCIATED_PERSON_EVENT);
			}
			else {

				entities = settlements.stream()
								.map(Settlement::getIndoorPeople)
								.flatMap(Collection::stream)
								.sorted(Comparator.comparing(Person::getName))
								.collect(Collectors.toList());
				settlementListener = new PersonChangeListener(EntityEventType.INVENTORY_STORING_UNIT_EVENT,
												EntityEventType.INVENTORY_RETRIEVING_UNIT_EVENT);
			}
		}
		else if (isBuriedCB) {
			if (sourceType == ValidSourceType.SETTLEMENT_ALL_ASSOCIATED_PEOPLE) {
	
				entities = settlements.stream()
								.map(Settlement::getBuriedPeople)
								.flatMap(Collection::stream)
								.sorted(Comparator.comparing(Person::getName))
								.collect(Collectors.toList());
				settlementListener = new PersonChangeListener(EntityEventType.ADD_ASSOCIATED_PERSON_EVENT,
											EntityEventType.REMOVE_ASSOCIATED_PERSON_EVENT);
			}
			else {

				entities = settlements.stream()
								.map(Settlement::getIndoorPeople)
								.flatMap(Collection::stream)
								.sorted(Comparator.comparing(Person::getName))
								.collect(Collectors.toList());
				settlementListener = new PersonChangeListener(EntityEventType.INVENTORY_STORING_UNIT_EVENT,
												EntityEventType.INVENTORY_RETRIEVING_UNIT_EVENT);
			}		
		}
		
		if (entities != null && !entities.isEmpty()) {		
			resetEntities(entities);
		}

		// Listen to the settlements for new People
		settlements.forEach(s -> s.addEntityListener(settlementListener));

		return true;
	}

	/**
	 * Shows live citizens if selected.
	 * 
	 * @param isLive
	 */
	public void showAlive(boolean isLive) {
		this.isLiveCB = isLive;
		if (isLive) {
			this.isDeceasedCB = false;
			this.isBuriedCB = false;
		}
	}
	
	/**
	 * Shows deceased citizens if selected.
	 * 
	 * @param isDeceased
	 */
	public void showDeceased(boolean isDeceased) {
		this.isDeceasedCB = isDeceased;
		if (isDeceased) {
			this.isLiveCB = false;
			this.isBuriedCB = false;
		}
	}
	
	/**
	 * Shows buried citizens if selected.
	 * 
	 * @param isBuried
	 */
	public void showBuried(boolean isBuried) {
		this.isBuriedCB = isBuried;
		if (isBuried) {
			this.isLiveCB = false;
			this.isDeceasedCB = false;
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

			case TASK_DESC: {
				// If the Person is dead, there is no Task Manager
				Task task = person.getMind().getTaskManager().getTask();
				result = ((task != null) ? task.getDescription() : "");
			}
			break;

			case MISSION_COL: {
				var m = person.getMind().getMission();
				if (m != null) {
					result = m.getFullMissionDesignation();
				}
			}
			break;

			case NAME:
				result = person.getName();
			break;

			case SETTLEMENT:
				result = person.getAssociatedSettlement().getName();
			break;

			case ENERGY: {
				PhysicalCondition pc = person.getPhysicalCondition();
				if (!pc.isDead()) {
					if (pc.isStarving())
						result = STARVING;
					else
						result = PhysicalConditionFormat.getHungerStatus(pc, false);
				}
			}
			break;

			case WATER: {
				PhysicalCondition pc = person.getPhysicalCondition();
				if (!pc.isDead()) {
					if (pc.isDehydrated())
						result = DEHYDRATED;
					else
						result = PhysicalConditionFormat.getThirstyStatus(pc, false);
				}
			}
			break;

			case FATIGUE:
				if (!person.getPhysicalCondition().isDead())
					result = PhysicalConditionFormat.getFatigueStatus(person.getPhysicalCondition(), false);
				break;

			case STRESS:
				if (!person.getPhysicalCondition().isDead())
					result = PhysicalConditionFormat.getStressStatus(person.getPhysicalCondition(), false);
				break;

			case PERFORMANCE:
				if (!person.getPhysicalCondition().isDead())
					result = PhysicalConditionFormat.getPerformanceStatus(person.getPhysicalCondition(), false);
				break;

			case EMOTION: 
				if (!person.getPhysicalCondition().isDead())
					result = person.getMind().getEmotion().getDescription();
				break;

			case HEALTH: 
				result = PhysicalConditionFormat.getHealthSituation(person.getPhysicalCondition());
				break;

			case LOCATION:
				result = person.getLocationTag().getImmediateLocation();
				break;

			case LOCALE:
				result = person.getLocationTag().getLocale();
				break;
				
			case ROLE:
				if (!person.getPhysicalCondition().isDead()) {
					Role role = person.getRole();
					if (role != null) {
						result = role.getType().getName();
					}
				}
				break;

			case JOB:
				// If person is dead, get job from death info.
				if (person.getPhysicalCondition().isDead())
					result = person.getPhysicalCondition().getDeathDetails().getJob().getName();
				else if (person.getMind().getJobType() != null)
					result = person.getMind().getJobType().getName();
				break;

			case SHIFT:
				// If person is dead, disable it.
				if (!person.getPhysicalCondition().isDead()) {
					ShiftSlot shift = person.getShiftSlot();		
					if (shift.getStatus() == WorkStatus.ON_CALL) {
						result = WorkStatus.ON_CALL.getName();
					}
					else {
						result = shift.getStatusDescription();
					}
				}
				break;
			
			default:
				throw new IllegalArgumentException("Unknown column " + columnIndex);
		}
		return result;
	}
	
	/**
     * Returns the score breakdown if TASK_DESC column is selected.
     * 
     * @param rowIndex Row index of cell
     * @param columnIndex Column index of cell
     * @return Return null by default
     */
    @Override
    public String getToolTipAt(int rowIndex, int columnIndex) {
		String result = null;
		if (columnIndex == TASK_DESC) {
			Person p = getEntity(rowIndex);
			if (p != null) {
				// If the Person is dead, there is no Task Manager
				var score = p.getMind().getTaskManager().getScore();
				result = (score != null ? "<html>" + RatingScoreRenderer.getHTMLFragment(score) + "</html>"
								: null);
			}
		}
        return result;
    }

	
	/**
	 * Catches unit update event.
	 *
	 * @param event the unit event.
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		String eventType = event.getType();

		Integer column = EVENT_COLUMN_MAPPING.get(eventType);

		if (column != null && column > -1) {
			Person unit = (Person) event.getSource();
			entityValueUpdated(unit, column, column);
		}
	}

	/**
	 * Prepares the model for deletion.
	 */
	@Override
	public void destroy() {
		super.destroy();

		 if (sourceType == ValidSourceType.VEHICLE_CREW) {
			((Unit) vehicle).removeEntityListener(crewListener);
			crewListener = null;
			vehicle = null;
		} else if (sourceType == ValidSourceType.MISSION_PEOPLE) {
			mission.removeEntityListener(missionListener);
			missionListener = null;
			mission = null;
		} else {
			settlements.forEach(s -> s.removeEntityListener(settlementListener));
			settlementListener = null;
		}
	}

	/**
	 * MissionListener inner class.
	 */
	private class LocalMissionListener implements EntityListener {
		/**
		 * Catches mission update event.
		 *
		 * @param event the entity event.
		 */
		@Override
		public void entityUpdate(EntityEvent event) {
			Object target = event.getTarget();
			if (target instanceof Person p) {
				String eventType = event.getType();

				if (eventType.equals(AbstractMission.ADD_MEMBER_EVENT)) {
					addEntity(p);
				}
				else if (eventType.equals(AbstractMission.REMOVE_MEMBER_EVENT)) {
					removeEntity(p);
				}
			}
		}
	}

	/**
	 * EntityListener inner class for watching Person move in/out of a Unit.
	 */
	private class PersonChangeListener implements EntityListener {

		private String addEvent;
		private String removeEvent;

		public PersonChangeListener(String addEvent, String removeEvent) {
			this.addEvent = addEvent;
			this.removeEvent = removeEvent;
		}

		/**
		 * Catches unit update event.
		 *
		 * @param event the unit event.
		 */
		public void entityUpdate(EntityEvent event) {
			if (event.getTarget() instanceof Person p) {
				String eventType = event.getType();
				if (addEvent.equals(eventType)) {
					addEntity(p);
				}
				else if (removeEvent.equals(eventType)) {
					removeEntity(p);
				}
			}
		}
	}
}
