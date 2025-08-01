/*
 * Mars Simulation Project
 * Robot.java
 * @date 2025-07-31
 * @author Manny Kung
 */

package com.mars_sim.core.robot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.Set;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.Function;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.RoboticStation;
import com.mars_sim.core.building.function.SystemType;
import com.mars_sim.core.building.function.ActivitySpot.AllocatedSpot;
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
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
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
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.robot.ai.BotMind;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.unit.AbstractMobileUnit;
import com.mars_sim.core.unit.MobileUnit;
import com.mars_sim.core.unit.UnitHolder;
import com.mars_sim.core.vehicle.Crewable;

/**
 * The robot class represents operating a robot on Mars.
 */
public class Robot extends AbstractMobileUnit implements Salvagable, Temporal, Malfunctionable, Worker {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final  SimLogger logger = SimLogger.getLogger(Robot.class.getName());

	// Static members
	
	private static final String REPAIRBOT = "RepairBot";
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

	/** String name of a robot */	
	public static final String TYPE = SystemType.ROBOT.getName();
	
	private static final String CURRENTLY = "Currently ";
	
	/** The string tag of operable. */
	private static final String OPERABLE = "Operable";
	/** The string tag of inoperable. */
	private static final String INOPERABLE = "Inoperable";

	// Data members
	/** Is the robot is inoperable. */
	private boolean isInoperable;
	/** Is the robot is salvaged. */
	private boolean isSalvaged;

	/** The age of this robot. */
	private int age;
	/** The height of the robot (in cm). */
	private int height;
	/** The carrying capacity of the robot. */
	private int carryingCapacity;

	private String model;
	
	/** The spot assigned to the Robot */
	private AllocatedSpot spot;
	/** The year of birth of this robot. */
	private LocalDate birthDate;
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

	static {
		// Initialize the parts
		ItemResourceUtil.initBotParts();
	}

	/**
	 * Constructor 1.
	 * 
	 * @param name
	 * @param settlement
	 * @param spec
	 */
	public Robot(String name, Settlement settlement, RobotSpec spec) {
		super(name, settlement);

		// Initialize data members.
		this.robotType = spec.getRobotType();
		this.model = spec.getMakeModel();
		
		// Set base mass
		setBaseMass(spec.getMass());
		// Set height
		height = spec.getHeight();

		isSalvaged = false;
		salvageInfo = null;
		isInoperable = false;
		
		// Set description for this robot
		setDescription("[ " + CURRENTLY + OPERABLE + " ] " + spec.getDescription());
		
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
		Settlement s = getAssociatedSettlement();
		s.addOwnedRobot(this);

		// Put robot in proper building.
		BuildingManager.addRobotToRandomBuilding(this, s);

		// Add scope to malfunction manager.
		malfunctionManager = new MalfunctionManager(this, WEAR_LIFETIME, MAINTENANCE_TIME);
		// Add system type to malfunction manager scope
		malfunctionManager.addScopeString(SystemType.ROBOT.getName());

		// Add TYPE to the standard scope
		SimulationConfig.instance().getPartConfiguration().addScopes(TYPE);

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
		
		// Calculate and set the base mass based on parts and inventory content
		setBaseMass(calculateMass()); 
	}

	/**
	 * Calculates the mass of the output of a process.
	 * 
	 * @param processName
	 * @return
	 */
    private static double calculateMass() {
		
		// Note: it's haphazard to match the string of the manu process since it can change.
		// Will need to implement a better way in matching and looking for the manu process 
    	// that assemblies the item of interest.
		String processName = ItemResourceUtil.ASSEMBLE_A_REPARTBOT;
	
	    Optional<ManufactureProcessInfo> found = SimulationConfig.instance().
	    		getManufactureConfiguration().getManufactureProcessList()
		 	.stream()
			.filter(f -> f.getName().equalsIgnoreCase(processName))
			.findFirst();
	
		if (found.isPresent()) {
			var manufactureProcessInfo = found.get();
			// Calculate total mass as the summation of the multiplication of the quantity and mass of each part 
			var mass = manufactureProcessInfo.calculateTotalInputMass();
			// Calculate output quantity
			var quantity = manufactureProcessInfo.calculateOutputQuantity(REPAIRBOT);					
			// Save the key value pair onto the weights Map
			return mass/quantity;
		}

	    logger.severe("The process '" + processName + "' cannot be found.");
	    		
		return EMPTY_MASS;
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
	@Override
	public boolean isRightOutsideSettlement() {
        return LocationStateType.SETTLEMENT_VICINITY == getLocationStateType();
    }

	private void toBeSalvaged() {
		((Settlement)getContainerUnit()).removeOwnedRobot(this);
		isInoperable = true;
	}

	private  void setInoperable() {
		// set description for this robot
		super.setDescription(getDescription().replace(OPERABLE, INOPERABLE));
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
	 * Gets a collection of people affected by this malfunction bots
	 *
	 * @return person collection
	 */
	@Override
	public Collection<Person> getAffectedPeople() {
		return getBuildingLocation().getAffectedPeople();
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
	public double getSpecificCapacity(int resource) {
		return eqmInventory.getSpecificCapacity(resource);
	}

	/**
	 * Obtains the remaining combined capacity of storage space of a particular amount resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getRemainingCombinedCapacity(int resource) {
		return eqmInventory.getRemainingCombinedCapacity(resource);
	}

	/**
	 * Obtains the remaining specific capacity of storage space of a particular amount resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getRemainingSpecificCapacity(int resource) {
		return eqmInventory.getRemainingSpecificCapacity(resource);
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
	public double getSpecificAmountResourceStored(int resource) {
		return eqmInventory.getSpecificAmountResourceStored(resource);
	}

	/**
	 * Gets all the amount resource resource stored, including inside equipment.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAllSpecificAmountResourceStored(int resource) {
		return eqmInventory.getAllSpecificAmountResourceStored(resource);
	}
	
	/**
	 * Gets the quantity of all stock and specific amount resource stored.
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
	public Set<Integer> getSpecificResourceStoredIDs() {
		return eqmInventory.getSpecificResourceStoredIDs();
	}

	/**
	 * Gets all stored amount resources in eqmInventory, including inside equipment
	 *
	 * @return all stored amount resources.
	 */
	@Override
	public Set<Integer> getAllAmountResourceStoredIDs() {
		return eqmInventory.getAllAmountResourceStoredIDs();
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
		return eqmInventory.addEquipment(e);
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
	private boolean setContainerUnit(UnitHolder newContainer) {
		if (newContainer != null) {
			var cu = getContainerUnit();
			
			if (newContainer.equals(cu)) {
				return true;
			}
	
			// 1. Set Coordinates
			if (newContainer instanceof MobileUnit mu) {
				setCoordinates(mu.getCoordinates());
			}
			else if (cu instanceof MobileUnit mu) {
				// Since it's on the surface of Mars,
				// First set its initial location to its old parent's location as it's leaving its parent.
				// Later it may move around and updates its coordinates by itself
				setCoordinates(mu.getCoordinates());
			}

			// 2. Set LocationStateType
			// 3. Set container
			setContainer(newContainer, defaultLocationState(newContainer));
		}

		return true;
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
	@Override
	public boolean transfer(UnitHolder destination) {
		boolean transferred = false;
		var cu = getContainerUnit();

		if (destination.equals(cu)) {
			return true;
		}

		// Check if the origin is a vehicle
		if (cu instanceof Crewable c) {
			transferred = c.removeRobot(this);
		}
		else if (cu instanceof MarsSurface ms) {
			transferred = ms.removeRobot(this);
		}
		else if (cu instanceof Settlement s) {
			// Question: should we remove this unit from settlement's robotWithin list
			// especially if it is still inside the garage of a settlement
			transferred = s.removeRobotsWithin(this);
			BuildingManager.removeRobotFromBuilding(this, getBuildingLocation());
		}
		else {
			logger.warning(this, 20_000, "Not possible to be retrieved from " + cu + ".");
		}

		if (!transferred) {
			logger.severe(this, 20_000, "Cannot be retrieved from " + cu + ".");
			// NOTE: need to revert back to the previous container unit cu
		}
		
		else {
			// Check if the destination is a vehicle
			if (destination instanceof Crewable c) {
				transferred = c.addRobot(this);
			}
			else if (destination instanceof MarsSurface ms) {
				transferred = ms.addRobot(this);
			}
			else if (destination instanceof Settlement s) {
				transferred = s.addRobotsWithin(this);
			}
			else if (destination instanceof Building b) {
				BuildingManager.setToBuilding(this, b);
				transferred = b.getSettlement().addRobotsWithin(this);
				// Turn a building destination to a settlement to avoid 
				// casting issue with making containerUnit a building instance
				destination = b.getSettlement();
			}

			if (!transferred) {
				logger.warning(this, 20_000, "Cannot be stored into " + destination + ".");
				// NOTE: need to revert back the storage action
			}
			else {
				// Set the new container unit (which will internally set the container unit id)
				setContainerUnit(destination);
			}
		}

		return transferred;
	}

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	@Override
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
	 * Returns the current amount of energy in kWh. 
	 */
	public double getcurrentEnergy() {
		return health.getkWattHourStored();
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
