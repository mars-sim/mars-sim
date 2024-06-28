/*
 * Mars Simulation Project
 * Robot.java
 * @date 2023-05-09
 * @author Manny Kung
 */

package com.mars_sim.core.robot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.environment.MarsSurface;
import com.mars_sim.core.equipment.Container;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.equipment.EquipmentInventory;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.malfunction.Malfunctionable;
import com.mars_sim.core.manufacture.Salvagable;
import com.mars_sim.core.manufacture.SalvageInfo;
import com.mars_sim.core.manufacture.SalvageProcessInfo;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.util.TaskManager;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.ai.BotMind;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.function.ActivitySpot.AllocatedSpot;
import com.mars_sim.core.structure.building.function.Function;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.RoboticStation;
import com.mars_sim.core.structure.building.function.SystemType;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.mapdata.location.LocalPosition;
import com.mars_sim.tools.util.RandomUtil;

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
	private static final double WEAR_LIFETIME = 334_000D;
	/** 100 millisols. */
	private static final double MAINTENANCE_TIME = 100D;
	/** A small amount. */
	private static final double SMALL_AMOUNT = 0.00001D;

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
	/** The age of this robot. */
	private int age;
	/** The settlement the robot is currently associated with. */
	private int associatedSettlementID = -1;
	/** The height of the robot (in cm). */
	private int height;
	/** The carrying capacity of the robot. */
	private int carryingCapacity;

	private String model;
	
	/** The spot assigned to the Robot */
	private AllocatedSpot spot;
	/** The year of birth of this robot. */
	private LocalDate birthDate;
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
			skillManager.addNewSkill(e.getKey(), e.getValue());
		}

		// Construct the RoboticAttributeManager instance.
		attributes = new NaturalAttributeManager(spec.getAttributeMap());
	}

	/**
	 * Initializes the robot object.
	 */
	public void initialize() {

		// Add this robot to be owned by the settlement
		Settlement s = unitManager.getSettlementByID(associatedSettlementID);
		s.addOwnedRobot(this);

		// Put robot in proper building.
		BuildingManager.addRobotToRandomBuilding(this, s);

		// Add scope to malfunction manager.
		malfunctionManager = new MalfunctionManager(this, WEAR_LIFETIME, MAINTENANCE_TIME);
		// Add system type to malfunction manager scope
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
		// Remove a random number of days from the current earth date
		int daysPast = RandomUtil.getRandomInt(31, 5*365);
		birthDate = masterClock.getEarthTime().minusDays(daysPast).toLocalDate();

		updateAge(masterClock.getEarthTime());
	}

	/**
	 * Is the robot outside of a settlement but within its vicinity
	 *
	 * @return true if the robot is just right outside of a settlement
	 */
	public boolean isRightOutsideSettlement() {
        return LocationStateType.SETTLEMENT_VICINITY == currentStateType;
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
		return unitManager.findSettlement(getCoordinates());
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

		if (getContainerID() <= Unit.MARS_SURFACE_UNIT_ID)
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
			if (health.timePassing(pulse)) {
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
			// Check if age should be updated
			updateAge(pulse.getMasterClock().getEarthTime());

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
	private int updateAge(LocalDateTime earthTime) {
		age = (int)ChronoUnit.YEARS.between(birthDate, earthTime);
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
		return birthDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));
	}

    /**
     * Consumes a given amount of energy.
     * 
     * @param amount the amount of energy to consume (in kWh)
     * @param time the duration of time
	 */
    public void consumeEnergy(double amount, double time) {
        health.consumeEnergy(amount, time);
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
		Collection<Robot> localRobotGroup = new UnitSet<>();

		if (isInSettlement()) {
			Building building = BuildingManager.getBuilding(this);
			if (building != null && building.hasFunction(FunctionType.ROBOTIC_STATION)) {
				localRobotGroup.addAll(building.getRoboticStation().getRobotOccupants());
			}
		}
		else if (isInVehicle()) {
			localRobotGroup.addAll(((Crewable) getVehicle()).getRobotCrew());
		}

		if (localRobotGroup.contains(this)) {
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
		salvageInfo = new SalvageInfo(this, info, settlement, masterClock.getMarsTime());
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
	public double getWalkSpeedMod() {
		// Get the modified stored mass and base mass 
		double mass = getMass();
		// The modifier is a ratio of the mass the person carry and the carrying capacity 
		// Make sure it doesn't go to zero or -ve as there is always some movement
		return Math.max(carryingCapacity/mass/1.2, SMALL_AMOUNT);
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
		return eqmInventory.isEmpty();
	}


	/**
	 * Gets the stored mass.
	 */
	@Override
	public double getStoredMass() {
		return eqmInventory.getStoredMass();
	}

	/**
	 * Gets the equipment list.
	 *
	 * @return the equipment list
	 */
	@Override
	public Set<Equipment> getEquipmentSet() {
		return eqmInventory.getEquipmentSet();
	}

	/**
	 * Gets the container set.
	 *
	 * @return
	 */
	@Override
	public Set<Equipment> getContainerSet() {
		return eqmInventory.getContainerSet();
	}
	
	/**
	 * Gets the EVA suit set.
	 * 
	 * @return
	 */
	@Override
	public Set<Equipment> getSuitSet() {
		return eqmInventory.getSuitSet();
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
		return eqmInventory.getAmountResourceRemainingCapacity(resource);
	}

	/**
	 * Does it have unused space or capacity for a particular resource ?
	 * 
	 * @param resource
	 * @return
	 */
	@Override
	public boolean hasAmountResourceRemainingCapacity(int resource) {
		return eqmInventory.hasAmountResourceRemainingCapacity(resource);
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
	 * Gets all the amount resource resource stored, including inside equipment.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAllAmountResourceStored(int resource) {
		return eqmInventory.getAllAmountResourceStored(resource);
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
	 * Gets all stored amount resources in eqmInventory, including inside equipment
	 *
	 * @return all stored amount resources.
	 */
	@Override
	public Set<Integer> getAllAmountResourceIDs() {
		return eqmInventory.getAllAmountResourceIDs();
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
	 * Note: will not count EVA suits.
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
	 * Note: will not count EVA suits.
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
	 * Finds all of the containers of a particular type (excluding EVA suit).
	 *
	 * @return collection of containers or empty collection if none.
	 */
	@Override
	public Collection<Container> findContainersOfType(EquipmentType type){
		return eqmInventory.findContainersOfType(type);
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
	 * Adds an equipment to this robot.
	 *
	 * @param equipment
	 * @return true if this robot can carry it
	 */
	@Override
	public boolean addEquipment(Equipment e) {
		if (eqmInventory.addEquipment(e)) {
			fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_EQUIPMENT_EVENT, this);
			return true;
		}
		return false;
	}

	/**
	 * Removes an equipment.
	 *
	 * @param equipment
	 */
	@Override
	public boolean removeEquipment(Equipment equipment) {
		return eqmInventory.removeEquipment(equipment);
	}

	/**
	 * Stores the item resource.
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
	public boolean setContainerUnit(Unit newContainer) {
		if (newContainer != null) {
			if (newContainer.equals(getContainerUnit())) {
				return false;
			}
			// 1. Set Coordinates
			if (newContainer.getUnitType() == UnitType.MARS) {
				// Since it's on the surface of Mars,
				// First set its initial location to its old parent's location as it's leaving its parent.
				// Later it may move around and updates its coordinates by itself
				setCoordinates(getContainerUnit().getCoordinates());
			}
			else {
				// Null its coordinates since it's now slaved after its parent
				setCoordinates(newContainer.getCoordinates());
			}
			// 2. Set LocationStateType
			updateRobotState(newContainer);
			// 3. Set containerID
			// TODO: what to set for a decommissioned robot ?
			setContainerID(newContainer.getIdentifier());
			// 4. Fire the container unit event
			fireUnitUpdate(UnitEventType.CONTAINER_UNIT_EVENT, newContainer);
		}

		return true;
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
	private LocationStateType getNewLocationState(Unit newContainer) {

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

		if (newContainer.getUnitType() == UnitType.MARS)
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

		if (containerID <= MARS_SURFACE_UNIT_ID)
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
			if (roboticStation != null && roboticStation.containsRobotOccupant(this))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns the robotic station the robot is at.
	 * 
	 * @return
	 */
	public RoboticStation getOccupiedStation() {
		Building building = getBuildingLocation();
		if (building != null) {
				
			Function f = building.getFunction(FunctionType.ROBOTIC_STATION);
					
			// Check if this robot has already occupied a spot
			boolean occupied = f.checkWorkerActivitySpot(this);
			
			RoboticStation roboticStation = building.getRoboticStation();
			if (occupied && roboticStation != null && roboticStation.containsRobotOccupant(this))
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
				logger.warning(this, 60_000L, "Not possible to be retrieved from " + cu + ".");
			}
		}
		else if (ut == UnitType.MARS) {
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
					logger.warning(this, 60_000L, "Not possible to be stored into " + cu + ".");
				}
			}
			else if (destination.getUnitType() == UnitType.MARS) {
				transferred = ((MarsSurface)destination).addRobot(this);
			}
			else if (destination.getUnitType() == UnitType.SETTLEMENT) {
				transferred = ((Settlement)destination).addRobotsWithin(this);
			}
			else if (destination.getUnitType() == UnitType.BUILDING) {
				BuildingManager.setToBuilding(this, (Building)destination);
				transferred = ((Building)destination).getSettlement().addRobotsWithin(this);
				// Turn a building destination to a settlement to avoid 
				// casting issue with making containerUnit a building instance
				destination = (((Building)destination)).getSettlement();
			}

			if (!transferred) {
				logger.warning(this, 60_000L, "Cannot be stored into " + destination + ".");
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
			logger.warning(this, 60_000L, "Cannot be retrieved from " + cu + ".");
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
	 * Returns the current amount of energy in kWh. 
	 */
	public double getcurrentEnergy() {
		return health.getCurrentEnergy();
	}
	
	public EquipmentInventory getEquipmentInventory() {
		return eqmInventory;
	}
	
	/**
	 * Sets the activity spot assigned.
	 * 
	 * @param newSpot Can be null if no spot assigned
	 */
	@Override
	public void setActivitySpot(AllocatedSpot newSpot) {
		if (spot != null) {
			spot.leave(this, false);
		}
		spot = newSpot;
	}
	
	@Override
	public AllocatedSpot getActivitySpot() {
		return spot;
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
