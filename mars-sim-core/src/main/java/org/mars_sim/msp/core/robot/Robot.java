/*
 * Mars Simulation Project
 * Robot.java
 * @date 2022-07-23
 * @author Manny Kung
 */

package org.mars_sim.msp.core.robot;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.environment.MarsSurface;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentInventory;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.manufacture.Salvagable;
import org.mars_sim.msp.core.manufacture.SalvageInfo;
import org.mars_sim.msp.core.manufacture.SalvageProcessInfo;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.util.TaskManager;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.robot.ai.BotMind;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.RoboticStation;
import org.mars_sim.msp.core.structure.building.function.SystemType;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * The robot class represents operating a robot on Mars.
 */
public class Robot extends Unit implements Salvagable, Temporal, Malfunctionable, Worker {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final  SimLogger logger = SimLogger.getLogger(Robot.class.getName());

	// Static members
	/** The base carrying capacity (kg) of a robot. */
	private static final double BASE_CAPACITY = 60D;
	/** Unloaded mass of EVA suit (kg.). */
	public static final double EMPTY_MASS = 80D;
	/** 334 Sols (1/2 orbit). */
	private static final double WEAR_LIFETIME = 334_000;
	/** 100 millisols. */
	private static final double MAINTENANCE_TIME = 100D;
	/** A small amount. */
	private static final double SMALL_AMOUNT = 0.00001;

	/** The string tag of operable. */
	private static final String OPERABLE = "Operable";
	/** The string tag of inoperable. */
	private static final String INOPERABLE = "Inoperable";

	// Data members
	/** Is the robot is inoperable. */
	private boolean isInoperable;
	/** Is the robot is salvaged. */
	private boolean isSalvaged;

	/** The building the robot is at. */
	private int currentBuildingInt;
	/** The year of birth of this robot. */
	private int year;
	/** The month of birth of this robot. */
	private int month;
	/** The day of birth of this robot. */
	private int day;
	/** The age of this robot. */
	private int age;
	/** The settlement the robot is currently associated with. */
	private int associatedSettlementID = -1;
	/** The height of the robot (in cm). */
	private int height;
	/** The carrying capacity of the robot. */
	private int carryingCapacity;

	private String model;

	/** Settlement position (meters) from settlement center. */
	private LocalPosition position;
	/** The Robot Type. */
	private RobotType robotType;
	/** The robot's skill manager. */
	private SkillManager skillManager;
	/** Manager for robot's natural attributes. */
	private NaturalAttributeManager attributes;
	/** robot's mind. */
	private BotMind botMind;
	/** robot's System condition. */
	private SystemCondition health;
	/** The SalvageInfo instance. */
	private SalvageInfo salvageInfo;
	/** The equipment's malfunction manager. */
	private MalfunctionManager malfunctionManager;
	/** The EquipmentInventory instance. */
	private EquipmentInventory eqmInventory;


	/**
	 * Constructor 1.
	 * 
	 * @param name
	 * @param settlement
	 * @param spec
	 */
	public Robot(String name, Settlement settlement, RobotSpec spec) {
		super(name, settlement.getCoordinates());

		// Initialize data members.
		this.associatedSettlementID = (Integer) settlement.getIdentifier();
		this.robotType = spec.getRobotType();
		this.position = LocalPosition.DEFAULT_POSITION;
		this.model = spec.getMakeModel();

		// Set base mass
		setBaseMass(spec.getMass());
		// Set height
		height = spec.getHeight();

		isSalvaged = false;
		salvageInfo = null;
		isInoperable = false;
		// set description for this robot
		super.setDescription(OPERABLE);

		// Construct the SystemCondition instance.
		health = new SystemCondition(this, spec);

		// Construct the SkillManager instance.
		skillManager = new SkillManager(this);
		for(Entry<SkillType, Integer> e : spec.getSkillMap().entrySet()) {
			skillManager.addNewSkillNExperience(e.getKey(), e.getValue());
		}

		// Construct the RoboticAttributeManager instance.
		attributes = new RoboticAttributeManager();
		for(Entry<NaturalAttributeType, Integer> a : spec.getAttributeMap().entrySet()) {
			attributes.setAttribute(a.getKey(), a.getValue());
		}
	}

	/**
	 * Initializes the robot object.
	 */
	public void initialize() {

		unitManager = sim.getUnitManager();

		// Add this robot to be owned by the settlement
		unitManager.getSettlementByID(associatedSettlementID).addOwnedRobot(this);
		// Set the container unit
//		setContainerUnit(settlement);
		// Put robot in proper building.
		BuildingManager.addRobotToRandomBuilding(this, associatedSettlementID);

		// Add scope to malfunction manager.
		malfunctionManager = new MalfunctionManager(this, WEAR_LIFETIME, MAINTENANCE_TIME);
		malfunctionManager.addScopeString(SystemType.ROBOT.getName());

		// Set up the time stamp for the robot
		createBirthDate();

		// Construct the BotMind instance.
		botMind = new BotMind(this);

		// Set inventory total mass capacity based on the robot's strength.
		int strength = attributes.getAttribute(NaturalAttributeType.STRENGTH);
		// Set carry capacity
		carryingCapacity = (int)BASE_CAPACITY + strength;
		// Construct the EquipmentInventory instance.
		eqmInventory = new EquipmentInventory(this, carryingCapacity);
	}

	/**
	 * Create birth time of the robot.
	 *
	 */
	private void createBirthDate() {
		// Set a birth time for the person
		year = EarthClock.getCurrentYear(earthClock) - RandomUtil.getRandomInt(22, 62);
		month = RandomUtil.getRandomInt(11) + 1;
		if (month == 2) {
			if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
				day = RandomUtil.getRandomInt(28) + 1;
			} else {
				day = RandomUtil.getRandomInt(27) + 1;
			}
		}
		else if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
			day = RandomUtil.getRandomInt(30) + 1;
		}
		else {
			day = RandomUtil.getRandomInt(29) + 1;
		}

		// TODO: find out why sometimes day = 0 as seen on
		if (day == 0) {
			logger.warning(this, "date of birth is on the day 0th. Incrementing to the 1st.");
			day = 1;
		}
	}

	/**
	 * Is the robot outside of a settlement but within its vicinity
	 *
	 * @return true if the robot is just right outside of a settlement
	 */
	public boolean isRightOutsideSettlement() {
        return LocationStateType.WITHIN_SETTLEMENT_VICINITY == currentStateType;
    }

	/**
	 * Gets the robot's position at a settlement.
	 *
	 * @return distance (meters) from the settlement's center.
	 */
	@Override
	public LocalPosition getPosition() {
		return position;
	}

	/**
	 * Sets the robot's position at a settlement.
	 *
	 * @param position the distance (meters) from the settlement's center.
	 */
	@Override
	public void setPosition(LocalPosition position) {
		this.position = position;
	}

	/**
	 * Get the settlement in vicinity. This is used assume the robot's is not at a settlement
	 *
	 * @return the robot's settlement
	 */
	public Settlement getNearbySettlement() {
		return CollectionUtils.findSettlement(getCoordinates());
	}

	/**
	/**
	 * Get the settlement the robot is at.
	 * Returns null if robot is not at a settlement.
	 *
	 * @return the robot's settlement
	 */
	@Override
	public Settlement getSettlement() {

		if (getContainerID() == Unit.MARS_SURFACE_UNIT_ID)
			return null;

		Unit c = getContainerUnit();

		if (c.getUnitType() == UnitType.SETTLEMENT) {
			return (Settlement) c;
		}

		if (isInVehicleInGarage()) {
			return ((Vehicle)c).getSettlement();
		}

		if (c.getUnitType() == UnitType.PERSON || c.getUnitType() == UnitType.ROBOT) {
			return c.getSettlement();
		}

		return null;
	}

	/**
	 * Get vehicle robot is in, null if robot is not in vehicle
	 *
	 * @return the robot's vehicle
	 */
	@Override
	public Vehicle getVehicle() {
		if (isInVehicle())
			return (Vehicle) getContainerUnit();
		return null;
	}

	// TODO: allow parts to be recycled
	public void toBeSalvaged() {
		((Settlement)getContainerUnit()).removeOwnedRobot(this);
		isInoperable = true;
		// Set home town
		setAssociatedSettlement(-1);
	}

	// TODO: allow robot parts to be stowed in storage
	void setInoperable() {
		// set description for this robot
		super.setDescription(INOPERABLE);
		botMind.setInactive();
		toBeSalvaged();
	}

	public boolean isOperable() {
		return !isInoperable;
	}
	
	/**
	 * robot can take action with time passing
	 *
	 * @param pulse Current simulation time
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		if (!isValid(pulse)) {
			return false;
		}

		// If robot is dead, then skip
		if (health != null && !health.isInoperable()) {
			if (health.timePassing(pulse.getElapsed())) {
				// Mental changes with time passing.
				if (botMind != null)
					botMind.timePassing(pulse);
			} else {
				// robot has died as a result of physical condition
				setInoperable();
			}
		}

		malfunctionManager.timePassing(pulse);

		if (pulse.isNewSol()) {
			// Check if a person's age should be updated
			EarthClock earthTime = pulse.getEarthTime();
			age = earthTime.getYear() - year - 1;
			if (earthTime.getMonth() >= month)
				if (earthTime.getDayOfMonth() >= day)
					age++;
		}
		return true;
	}

	/**
	 * Returns a reference to the robot's attribute manager
	 *
	 * @return the robot's natural attribute manager
	 */
	@Override
	public NaturalAttributeManager getNaturalAttributeManager() {
		return attributes;
	}

	/**
	 * Get the performance factor that effect robot with the complaint.
	 *
	 * @return The value is between 0 -> 1.
	 */
	public double getPerformanceRating() {
		return health.getPerformanceFactor();
	}

	/**
	 * Returns a reference to the robot's system condition
	 *
	 * @return the robot's batteryCondition
	 */
	public SystemCondition getSystemCondition() {
		return health;
	}

	/**
	 * Returns the robot's mind
	 *
	 * @return the robot's mind
	 */
	public BotMind getBotMind() {
		return botMind;
	}

	@Override
	public TaskManager getTaskManager() {
		return botMind.getBotTaskManager();
	}

	/**
	 * Updates and returns the robot's age
	 *
	 * @return the robot's age
	 */
	public int updateAge(EarthClock earthTime) {
		age = earthTime.getYear() - year - 1;
		if (earthTime.getMonth() >= month)
			if (earthTime.getDayOfMonth() >= day)
				age++;

		return age;
	}

	/**
	 * Returns the robot's height in cm
	 *
	 * @return the robot's height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns the robot's birth date
	 *
	 * @return the robot's birth date
	 */
	public String getBirthDate() {
		StringBuilder s = new StringBuilder();
		s.append(year).append("-");
		if (month < 10)
			s.append("0").append(month).append("-");
		else
			s.append(month).append("-");
		if (day < 10)
			s.append("0").append(day);
		else
			s.append(day);

		return s.toString();
	}

    /**
     * Consumes a given amount of energy.
     * 
     * @param amount the amount of energy to consume (in kWh)
	 */
    public void consumeEnergy(double amount) {
        health.consumeEnergy(amount);
    }

	/**
	 * Gets the type of the robot.
	 *
	 * @return RobotType
	 */
	public RobotType getRobotType() {
		return robotType;
	}

	/**
	 * Get the mode of the robot.
	 */
	public String getModel() {
		return model;
	}

	/**
	 * Gets the robot's local group (in building or rover)
	 *
	 * @return collection of robots in robot's location.
	 */
	public Collection<Robot> getLocalRobotGroup() {
		Collection<Robot> localRobotGroup = null;

		if (isInSettlement()) {
			Building building = BuildingManager.getBuilding(this);
			if (building != null) {
				if (building.hasFunction(FunctionType.ROBOTIC_STATION)) {
					localRobotGroup = new HashSet<>(building.getRoboticStation().getRobotOccupants());
				}
			}
		} else if (isInVehicle()) {
			localRobotGroup = new HashSet<>(((Crewable) getVehicle()).getRobotCrew());
		}

		if (localRobotGroup == null) {
			localRobotGroup = Collections.emptyList();
		}
		else if (localRobotGroup.contains(this)) {
			localRobotGroup.remove(this);
		}
		return localRobotGroup;
	}

	/**
	 * Gets the name of the vehicle operator
	 *
	 * @return vehicle operator name.
	 */
	public String getOperatorName() {
		return getName();
	}

	/**
	 * Gets the settlement the robot is currently associated with.
	 *
	 * @return associated settlement or null if none.
	 */
	public Settlement getAssociatedSettlement() {
		return unitManager.getSettlementByID(associatedSettlementID);
	}

	/**
	 * Sets the associated settlement for a robot.
	 *
	 * @param newSettlement the new associated settlement or null if none.
	 */
	public void setAssociatedSettlement(int newSettlement) {
		if (associatedSettlementID != newSettlement) {

			int oldSettlement = associatedSettlementID;
			associatedSettlementID = newSettlement;

			if (oldSettlement != -1) {
				unitManager.getSettlementByID(oldSettlement).removeOwnedRobot(this);
			}
		}
	}

	public double getScientificAchievement(ScienceType science) {
		return 0;
	}

	public double getTotalScientificAchievement() {
		return 0;
	}

	public void addScientificAchievement(double achievementCredit, ScienceType science) {
	}

	/**
	 * Gets a collection of people affected by this malfunction bots
	 *
	 * @return person collection
	 */
	public Collection<Person> getAffectedPeople() {
		return getBuildingLocation().getAffectedPeople();
		// TODO: associate each bot with its owner
	}

	/**
	 * Checks if the item is salvaged.
	 *
	 * @return true if salvaged.
	 */
	public boolean isSalvaged() {
		return isSalvaged;
	}

	/**
	 * Indicate the start of a salvage process on the item.
	 *
	 * @param info       the salvage process info.
	 * @param settlement the settlement where the salvage is taking place.
	 */
	public void startSalvage(SalvageProcessInfo info, int settlement) {
		salvageInfo = new SalvageInfo(this, info, settlement);
		isSalvaged = true;
	}

	/**
	 * Gets the salvage info.
	 *
	 * @return salvage info or null if item not salvaged.
	 */
	public SalvageInfo getSalvageInfo() {
		return salvageInfo;
	}

	/**
	 * Gets the unit's malfunction manager.
	 *
	 * @return malfunction manager
	 */
	public MalfunctionManager getMalfunctionManager() {
		return malfunctionManager;
	}

	/**
	 * Gets the building the robot is located at Returns null if outside of a
	 * settlement
	 *
	 * @return building
	 */
	@Override
	public Building getBuildingLocation() {
		return computeCurrentBuilding();
	}

	/**
	 * Computes the building the robot is currently located at Returns null if
	 * outside of a settlement
	 *
	 * @return building
	 */
	public Building computeCurrentBuilding() {
//		if (isInSettlement()) {
//			currentBuilding = getSettlement().getBuildingManager().getBuildingAtPosition(getXLocation(),
//					getYLocation());
//		} else
//			currentBuilding = null;
//
//		return currentBuilding;

		if (currentBuildingInt == -1)
			return null;
		return unitManager.getBuildingByID(currentBuildingInt);
	}

	/**
	 * Computes the building the robot is currently located at Returns null if
	 * outside of a settlement
	 *
	 * @return building
	 */
	public void setCurrentBuilding(Building building) {
		if (building == null)
			currentBuildingInt = -1;
		else
			currentBuildingInt = building.getIdentifier();
	}

	@Override
	public String getTaskDescription() {
		return getBotMind().getBotTaskManager().getTaskDescription(false);
	}

	@Override
	public Mission getMission() {
		return getBotMind().getMission();
	}

	@Override
	public void setMission(Mission newMission) {
		getBotMind().setMission(newMission);
	}

	public int getProduceFoodSkill() {
		int skill = skillManager.getEffectiveSkillLevel(SkillType.COOKING) * 5;
		skill += skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE) * 2;
		skill = (int) Math.round(skill / 7D);
		return skill;
	}

	public Settlement findSettlementVicinity() {
		return getLocationTag().findSettlementVicinity();
	}

	/**
	 * Returns a reference to the robot's skill manager
	 *
	 * @return the robot's skill manager
	 */
	@Override
	public SkillManager getSkillManager() {
		return skillManager;
	}

	/**
	 * Returns the effective integer skill level from a named skill based on
	 * additional modifiers such as fatigue.
	 *
	 * @param skillType the skill's type
	 * @return the skill's effective level
	 */
	public int getEffectiveSkillLevel(SkillType skillType) {
		// Modify for fatigue, minus 1 skill level for every 1000 points of fatigue.
		return (int) Math.round(getPerformanceRating() * skillManager.getSkillLevel(skillType));
	}

	/**
	 * Calculate the modifier for walking speed based on how much this unit is carrying
	 *
	 * @return modifier
	 */
	public double calculateWalkSpeed() {
		double mass = getMass();
		// At full capacity, may still move at 10%.
		// Make sure is doesn't go -ve and there is always some movement
		return 1.1 - Math.min(mass/Math.max(carryingCapacity, SMALL_AMOUNT), 1D);
	}

	public int getAge() {
		return age;
	}

	public boolean isFit() {
		return !health.isInoperable();
	}


	/**
	 * Generate a unique name for the Robot. Generated based on
	 * the type of Robot.
	 * @param robotType
	 * @return
	 */
	public static String generateName(RobotType robotType) {
		int number = unitManager.incrementTypeCount(robotType.name());
		return String.format("%s %03d", robotType.getName(), number);
	}

	/**
	 * Gets the remaining carrying capacity available.
	 *
	 * @return capacity (kg).
	 */
	public double getRemainingCarryingCapacity() {
		double result = carryingCapacity - eqmInventory.getStoredMass();
		if (result < 0)
			return 0;
		return result;
	}

	/**
	 * Get the capacity the robot can carry
	 *
	 * @return capacity (kg)
	 */
	public double getCarryingCapacity() {
		return carryingCapacity;
	}

	/**
	 * Obtains the remaining general storage space
	 *
	 * @return quantity
	 */
	@Override
	public double getRemainingCargoCapacity() {
		return eqmInventory.getRemainingCargoCapacity();
	}

	/**
	 * Mass of Equipment is the base mass plus what every it is storing
	 */
	@Override
	public double getMass() {
		// TODO because the PErson is not fully initialised in the constructor this
		// can be null. The initialise method is the culprit.
		return (eqmInventory != null ? eqmInventory.getStoredMass() : 0) + getBaseMass();

	}

	/**
	 * Is this unit empty ?
	 *
	 * @return true if this unit doesn't carry any resources or equipment
	 */
	public boolean isEmpty() {
		return (eqmInventory.getStoredMass() == 0D);
	}


	/**
	 * Gets the stored mass
	 */
	@Override
	public double getStoredMass() {
		return eqmInventory.getStoredMass();
	}

	/**
	 * Get the equipment list
	 *
	 * @return the equipment list
	 */
	@Override
	public Set<Equipment> getEquipmentSet() {
		if (eqmInventory == null)
			return new HashSet<>();
		return eqmInventory.getEquipmentSet();
	}

	/**
	 * Does this person possess an equipment of this equipment type
	 *
	 * @param typeID
	 * @return true if this person possess this equipment type
	 */
	@Override
	public boolean containsEquipment(EquipmentType type) {
		return eqmInventory.containsEquipment(type);
	}

	/**
	 * Adds an equipment to this person
	 *
	 * @param equipment
	 * @return true if this person can carry it
	 */
	@Override
	public boolean addEquipment(Equipment e) {
		if (eqmInventory.addEquipment(e)) {
			e.setCoordinates(getCoordinates());
			e.setContainerUnit(this);
			fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_EQUIPMENT_EVENT, this);
			return true;
		}
		return false;
	}

	/**
	 * Remove an equipment
	 *
	 * @param equipment
	 */
	@Override
	public boolean removeEquipment(Equipment equipment) {
		return eqmInventory.removeEquipment(equipment);
	}

	/**
	 * Stores the item resource
	 *
	 * @param resource the item resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	@Override
	public int storeItemResource(int resource, int quantity) {
		return eqmInventory.storeItemResource(resource, quantity);
	}

	/**
	 * Retrieves the item resource
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	@Override
	public int retrieveItemResource(int resource, int quantity) {
		return eqmInventory.retrieveItemResource(resource, quantity);
	}

	/**
	 * Gets the item resource stored
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public int getItemResourceStored(int resource) {
		return eqmInventory.getItemResourceStored(resource);
	}

	/**
	 * Stores the amount resource
	 *
	 * @param resource the amount resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	@Override
	public double storeAmountResource(int resource, double quantity) {
		return eqmInventory.storeAmountResource(resource, quantity);
	}

	/**
	 * Retrieves the resource
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	@Override
	public double retrieveAmountResource(int resource, double quantity) {
		return eqmInventory.retrieveAmountResource(resource, quantity);
	}

	/**
	 * Gets the capacity of a particular amount resource
	 *
	 * @param resource
	 * @return capacity
	 */
	@Override
	public double getAmountResourceCapacity(int resource) {
		return eqmInventory.getAmountResourceCapacity(resource);
	}

	/**
	 * Obtains the remaining storage space of a particular amount resource
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceRemainingCapacity(int resource) {
		return eqmInventory.getAmountResourceCapacity(resource);
	}

	/**
	 * Does it have unused space or capacity for a particular resource ?
	 * 
	 * @param resource
	 * @return
	 */
	@Override
	public boolean hasAmountResourceRemainingCapacity(int resource) {
		return eqmInventory. hasAmountResourceRemainingCapacity(resource);
	}
	
	/**
     * Gets the total capacity that this robot can hold.
     *
     * @return total capacity (kg).
     */
	@Override
	public double getCargoCapacity() {
		return eqmInventory.getCargoCapacity();
	}

	/**
	 * Gets the amount resource stored
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceStored(int resource) {
		return eqmInventory.getAmountResourceStored(resource);
	}

	/**
	 * Gets all stored amount resources
	 *
	 * @return all stored amount resources.
	 */
	@Override
	public Set<Integer> getAmountResourceIDs() {
		return eqmInventory.getAmountResourceIDs();
	}

	/**
	 * Gets all stored item resources
	 *
	 * @return all stored item resources.
	 */
	@Override
	public Set<Integer> getItemResourceIDs() {
		return eqmInventory.getItemResourceIDs();
	}


	/**
	 * Finds the number of empty containers of a class that are contained in storage and have
	 * an empty inventory.
	 *
	 * @param containerClass  the unit class.
	 * @param brandNew  does it include brand new bag only
	 * @return number of empty containers.
	 */
	@Override
	public int findNumEmptyContainersOfType(EquipmentType containerType, boolean brandNew) {
		return eqmInventory.findNumEmptyContainersOfType(containerType, brandNew);
	}

	/**
	 * Finds the number of containers of a particular type
	 *
	 * @param containerType the equipment type.
	 * @return number of empty containers.
	 */
	@Override
	public int findNumContainersOfType(EquipmentType containerType) {
		return eqmInventory.findNumContainersOfType(containerType);
	}

	/**
	 * Finds a container in storage.
	 *
	 * @param containerType
	 * @param empty does it need to be empty ?
	 * @param resource If -1 then resource doesn't matter
	 * @return instance of container or null if none.
	 */
	@Override
	public Container findContainer(EquipmentType containerType, boolean empty, int resource) {
		return eqmInventory.findContainer(containerType, empty, resource);
	}

	/**
	 * Finds all of the containers (excluding EVA suit).
	 *
	 * @return collection of containers or empty collection if none.
	 */
	@Override
	public Collection<Container> findAllContainers() {
		return eqmInventory.findAllContainers();
	}

	/**
	 * Finds all of the containers of a particular type (excluding EVA suit).
	 *
	 * @return collection of containers or empty collection if none.
	 */
	@Override
	public Collection<Container> findContainersOfType(EquipmentType type){
		return eqmInventory.findContainersOfType(type);
	}

	/**
	 * Does it have this item resource ?
	 *
	 * @param resource
	 * @return
	 */
	@Override
	public boolean hasItemResource(int resource) {
		return eqmInventory.hasItemResource(resource);
	}

	/**
	 * Sets the unit's container unit.
	 *
	 * @param newContainer the unit to contain this unit.
	 */
	@Override
	public void setContainerUnit(Unit newContainer) {
		if (newContainer != null) {
			if (newContainer.equals(getContainerUnit())) {
				return;
			}
			// 1. Set Coordinates
			setCoordinates(newContainer.getCoordinates());
			// 2. Set LocationStateType
			updateRobotState(newContainer);
			// 3. Set containerID
			// Q: what to set for a deceased person ?
			setContainerID(newContainer.getIdentifier());
			// 4. Fire the container unit event
			fireUnitUpdate(UnitEventType.CONTAINER_UNIT_EVENT, newContainer);
		}
	}

	/**
	 * Updates the location state type of a  robot
	 *
	 * @param newContainer
	 */
	public void updateRobotState(Unit newContainer) {
		if (newContainer == null) {
			currentStateType = LocationStateType.UNKNOWN;
			return;
		}

		currentStateType = getNewLocationState(newContainer);
	}

	/**
	 * Gets the location state type based on the type of the new container unit
	 *
	 * @param newContainer
	 * @return {@link LocationStateType}
	 */
	@Override
	public LocationStateType getNewLocationState(Unit newContainer) {

		if (newContainer.getUnitType() == UnitType.SETTLEMENT)
			return LocationStateType.INSIDE_SETTLEMENT;

		if (newContainer.getUnitType() == UnitType.BUILDING)
			return LocationStateType.INSIDE_SETTLEMENT;

		if (newContainer.getUnitType() == UnitType.VEHICLE)
			return LocationStateType.INSIDE_VEHICLE;

		if (newContainer.getUnitType() == UnitType.CONSTRUCTION)
			return LocationStateType.MARS_SURFACE;

		if (newContainer.getUnitType() == UnitType.PERSON)
			return LocationStateType.ON_PERSON_OR_ROBOT;

		if (newContainer.getUnitType() == UnitType.PLANET)
			return LocationStateType.MARS_SURFACE;

		return null;
	}

	/**
	 * Is this unit inside a settlement
	 *
	 * @return true if the unit is inside a settlement
	 */
	@Override
	public boolean isInSettlement() {

		if (containerID == MARS_SURFACE_UNIT_ID)
			return false;

		if (LocationStateType.INSIDE_SETTLEMENT == currentStateType)
			return true;

		if (LocationStateType.INSIDE_VEHICLE == currentStateType) {
			return false;
//			// if the vehicle is parked in a garage
//			if (LocationStateType.INSIDE_SETTLEMENT == ((Vehicle)getContainerUnit()).getLocationStateType()) {
//				return true;
//			}
		}

		// Note: may consider the scenario of this unit
		// being carried in by another person or a robot
//		if (LocationStateType.ON_PERSON_OR_ROBOT == currentStateType)
//			return getContainerUnit().isInSettlement();

		return false;
	}

	/**
	 * Is the robot already at a robotic station ?
	 * 
	 * @return
	 */
	public boolean isAtStation() {
		Building building = getBuildingLocation();
		if (building != null) {
			RoboticStation roboticStation = building.getRoboticStation();
			if (roboticStation.containsRobotOccupant(this))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns the robotic station the robot is at ?
	 * 
	 * @return
	 */
	public RoboticStation getStation() {
		Building building = getBuildingLocation();
		if (building != null) {
			RoboticStation roboticStation = building.getRoboticStation();
			if (roboticStation.containsRobotOccupant(this))
				return roboticStation;
		}
		return null;
	}
	
	/**
	 * Transfer the unit from one owner to another owner
	 *
	 * @param origin {@link Unit} the original container unit
	 * @param destination {@link Unit} the destination container unit
	 */
	public boolean transfer(Unit destination) {
		boolean transferred = false;
		Unit cu = getContainerUnit();
		UnitType ut = cu.getUnitType();

		if (destination.equals(cu)) {
			return true;
		}

		// Check if the origin is a vehicle
		if (ut == UnitType.VEHICLE) {
			if (((Vehicle)cu).getVehicleType() != VehicleType.DELIVERY_DRONE) {
				transferred = ((Crewable)cu).removeRobot(this);
			}
			else {
				logger.warning(this, "Not possible to be retrieved from " + cu + ".");
			}
		}
		else if (ut == UnitType.PLANET) {
			transferred = ((MarsSurface)cu).removeRobot(this);
		}
		else if (ut == UnitType.BUILDING) {
			transferred = true;
		}
		else {
			// Question: should we remove this unit from settlement's robotWithin list
			// especially if it is still inside the garage of a settlement
			transferred = ((Settlement)cu).removeRobotsWithin(this);
			BuildingManager.removeRobotFromBuilding(this, getBuildingLocation());
		}

		if (transferred) {
			// Check if the destination is a vehicle
			if (destination.getUnitType() == UnitType.VEHICLE) {
				if (((Vehicle)destination).getVehicleType() != VehicleType.DELIVERY_DRONE) {
					transferred = ((Crewable)destination).addRobot(this);
				}
				else {
					logger.warning(this, "Not possible to be stored into " + cu + ".");
				}
			}
			else if (destination.getUnitType() == UnitType.PLANET) {
				transferred = ((MarsSurface)destination).addRobot(this);
			}
			else if (destination.getUnitType() == UnitType.SETTLEMENT) {
				transferred = ((Settlement)destination).addRobotsWithin(this);
			}
			else if (destination.getUnitType() == UnitType.BUILDING) {
				BuildingManager.addPersonOrRobotToBuilding(this, (Building)destination);
				transferred = ((Building)destination).getSettlement().addRobotsWithin(this);
			}

			if (!transferred) {
				logger.warning(this, "Cannot be stored into " + destination + ".");
				// NOTE: need to revert back the storage action
			}
			else {
				// Set the new container unit (which will internally set the container unit id)
				setContainerUnit(destination);
				// Fire the unit event type
				getContainerUnit().fireUnitUpdate(UnitEventType.INVENTORY_STORING_UNIT_EVENT, this);
				// Fire the unit event type
				getContainerUnit().fireUnitUpdate(UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT, this);
			}
		}
		else {
			logger.warning(this, "Cannot be retrieved from " + cu + ".");
			// NOTE: need to revert back the retrieval action
		}

		return transferred;
	}

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	public int hashCode() {
		int hashCode = getIdentifier();
		hashCode *= getRobotType().hashCode();
		return hashCode;
	}

	@Override
	public UnitType getUnitType() {
		return UnitType.ROBOT;
	}

	/**
	 * Gets the holder's unit instance
	 *
	 * @return the holder's unit instance
	 */
	@Override
	public Unit getHolder() {
		return this;
	}

	/**
	 * Sets unit's location coordinates
	 *
	 * @param newLocation the new location of the unit
	 */
	public void setCoordinates(Coordinates newLocation) {
		super.setCoordinates(newLocation);

		if (getEquipmentSet() != null && !getEquipmentSet().isEmpty()) {
			for (Equipment e: getEquipmentSet()) {
				e.setCoordinates(newLocation);
			}
		}
	}
	
	/** 
	 * Returns the current amount of energy in kWh. 
	 */
	public double getcurrentEnergy() {
		return health.getcurrentEnergy();
	}
	
	/**
	 * Compares if an object is the same as this robot
	 *
	 * @param obj
	 */
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	/**
	 * Reinitialize references after loading from a saved sim
	 */
	public void reinit() {
		botMind.reinit();
	}

	@Override
	public void destroy() {
		super.destroy();
		if (salvageInfo != null)
			salvageInfo.destroy();
		salvageInfo = null;
		attributes.destroy();
		attributes = null;
		botMind.destroy();
		botMind = null;
		health.destroy();
		health = null;
		skillManager.destroy();
		skillManager = null;
	}
}
