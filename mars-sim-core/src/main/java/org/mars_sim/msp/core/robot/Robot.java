/**
 * Mars Simulation Project
 * Robot.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.robot;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.manufacture.Salvagable;
import org.mars_sim.msp.core.manufacture.SalvageInfo;
import org.mars_sim.msp.core.manufacture.SalvageProcessInfo;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;
import org.mars_sim.msp.core.person.health.MedicalAid;
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
import org.mars_sim.msp.core.vehicle.VehicleOperator;

/**
 * The robot class represents a robot on Mars. It keeps track of everything
 * related to that robot
 */
public class Robot extends Equipment implements VehicleOperator, Salvagable, Temporal, Malfunctionable, MissionMember, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/* default logger. */
	private static final  Logger logger = Logger.getLogger(Robot.class.getName());
//	private static String sourceName = logger.getName();

	// Static members
	/** The base carrying capacity (kg) of a robot. */
	private static final double BASE_CAPACITY = 60D;
	/** Unloaded mass of EVA suit (kg.). */
	public static final double EMPTY_MASS = 80D;
	/** 334 Sols (1/2 orbit). */
	private static final double WEAR_LIFETIME = 334_000;
	/** 100 millisols. */
	private static final double MAINTENANCE_TIME = 100D;
	
	/** The enum type of this equipment. */
	public static final String TYPE = "Robot";
	
	private static final String OPERABLE = "Operable";
	private static final String INOPERABLE = "Inoperable";
	
	
	// Data members
	/** Is the robot is inoperable. */
	private boolean isInoperable;
	/** Is the robot is salvaged. */
	private boolean isSalvaged;
	
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
	
	/** Settlement X location (meters) from settlement center. */
	private double xLoc;
	/** Settlement Y location (meters) from settlement center. */
	private double yLoc;

	/** The nick name for this robot. e.g. Chefbot 001 */
	private String nickName;
	/** The country of the robot made. */
	private String country;
	/** The sponsor of the robot. */
	private String sponsor;
	
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
	protected MalfunctionManager malfunctionManager;
	/** The birthplace of the robot. */
	private String birthplace;
	/** The birth time of the robot. */
	private String birthTimeStamp;
	/** The TaskSchedule instance. */
	//private TaskSchedule taskSchedule;
	/** The Robot Type. */
	private RobotType robotType;
	/** The building the robot is at. */
	private int currentBuildingInt;

	
	protected Robot(String name, Settlement settlement, RobotType robotType) {
		super(name, robotType.getName(), settlement); // extending equipment
		
		// Store this robot to the settlement 
		settlement.getInventory().storeUnit(this);
		// Add this robot to be owned by the settlement
		settlement.addOwnedRobot(this);

		// Initialize data members.
		this.nickName = name;
		this.associatedSettlementID = (Integer) settlement.getIdentifier();
//		System.out.println("(1) " + associatedSettlementID + " : " + settlement + " : " + name);
		this.robotType = robotType;
		xLoc = 0D;
		yLoc = 0D;
		
		isSalvaged = false;
		salvageInfo = null;
		isInoperable = false;
		// set description for this robot
		super.setDescription(OPERABLE);

		// Construct the SkillManager instance.
		skillManager = new SkillManager(this);
		// Construct the RoboticAttributeManager instance.
		attributes = new RoboticAttributeManager();
	}

	/*
	 * Uses static factory method to create an instance of RobotBuilder
	 */
	public static RobotBuilder<?> create(String name, Settlement settlement, RobotType robotType) {
		return new RobotBuilderImpl(name, settlement, robotType);
	}

	public void initialize() {
		// Put robot in proper building.
		BuildingManager.addToRandomBuilding(this, associatedSettlementID);
		
		unitManager = sim.getUnitManager();
		
		// Add scope to malfunction manager.
		malfunctionManager = new MalfunctionManager(this, WEAR_LIFETIME, MAINTENANCE_TIME);
		malfunctionManager.addScopeString(SystemType.ROBOT.getName());

		// Set up the time stamp for the robot
		birthTimeStamp = createBirthTimeStamp();
		
		// Construct the BotMind instance.
		botMind = new BotMind(this);
		// Construct the SystemCondition instance.
		health = new SystemCondition(this);

		setBaseMass(100D + (RandomUtil.getRandomInt(100) + RandomUtil.getRandomInt(100)) / 10D);
		height = 156 + RandomUtil.getRandomInt(22);

		// Set inventory total mass capacity based on the robot's strength.
		int strength = attributes.getAttribute(NaturalAttributeType.STRENGTH);
		getInventory().addGeneralCapacity(BASE_CAPACITY + strength);
	}

	/**
	 * Gets the instance of the task schedule of the robot.
	 */
//	public TaskSchedule getTaskSchedule() {
//		return taskSchedule;
//	}

	/**
	 * Create a string representing the birth time of the person.
	 *
	 * @return birth time string.
	 */
	private String createBirthTimeStamp() {
		StringBuilder s = new StringBuilder();
		// Set a birth time for the person
		year = EarthClock.getCurrentYear(earthClock) - RandomUtil.getRandomInt(22, 62);
		// 2003 + RandomUtil.getRandomInt(10) + RandomUtil.getRandomInt(10);
		s.append(year);

		month = RandomUtil.getRandomInt(11) + 1;
		s.append("-");
		if (month < 10)
			s.append(0);
		s.append(month).append("-");

		if (month == 2) {
			if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
				day = RandomUtil.getRandomInt(28) + 1;
			} else {
				day = RandomUtil.getRandomInt(27) + 1;
			}
		}

		else if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
			day = RandomUtil.getRandomInt(30) + 1;
		} else {
			day = RandomUtil.getRandomInt(29) + 1;
		}

		// TODO: find out why sometimes day = 0 as seen on
		if (day == 0) {
			logger.warning(nickName + "'s date of birth is on the day 0th. Incrementing to the 1st.");
			day = 1;
		}

		if (day < 10)
			s.append(0);
		s.append(day).append(" ");

		int hour = RandomUtil.getRandomInt(23);
		if (hour < 10)
			s.append(0);
		s.append(hour).append(":");

		int minute = RandomUtil.getRandomInt(59);
		if (minute < 10)
			s.append(0);
		s.append(minute).append(":");

		int second = RandomUtil.getRandomInt(59);
		if (second < 10)
			s.append(0);
		s.append(second).append(".000");

		return s.toString();
	}
	
//	/**
//	 * Is the robot in a vehicle inside a garage
//	 * 
//	 * @return true if the robot is in a vehicle inside a garage
//	 */
//	public boolean isInVehicleInGarage() {
//	if (getContainerUnit() instanceof Vehicle) {
//			Building b = BuildingManager.getBuilding((Vehicle) getContainerUnit());
//			if (b != null)
//				// still inside the garage
//				return true;
//		}
//		return false;
//	}

	/**
	 * Is the robot outside of a settlement but within its vicinity
	 * 
	 * @return true if the robot is just right outside of a settlement
	 */
	public boolean isRightOutsideSettlement() {
        return LocationStateType.WITHIN_SETTLEMENT_VICINITY == currentStateType;
    }

//	/**
//	 * @return {@link LocationSituation} the robot's location
//	 */
//	@Override
//	public LocationSituation getLocationSituation() {
//		if (isInoperable)
//			return LocationSituation.DECOMMISSIONED;
//		else {
//			Unit container = getContainerUnit();
//			if (container instanceof Settlement)
//				return LocationSituation.IN_SETTLEMENT;
//			else if (container instanceof Vehicle)
//				return LocationSituation.IN_VEHICLE;
//			else if (container instanceof MarsSurface)
//				return LocationSituation.OUTSIDE;
//		}
//		return LocationSituation.UNKNOWN;
//	}

	/**
	 * Gets the robot's X location at a settlement.
	 * 
	 * @return X distance (meters) from the settlement's center.
	 */
	public double getXLocation() {
		return xLoc;
	}

	/**
	 * Sets the robot's X location at a settlement.
	 * 
	 * @param xLocation the X distance (meters) from the settlement's center.
	 */
	public void setXLocation(double xLocation) {
		this.xLoc = xLocation;
	}

	/**
	 * Gets the robot's Y location at a settlement.
	 * 
	 * @return Y distance (meters) from the settlement's center.
	 */
	public double getYLocation() {
		return yLoc;
	}

	/**
	 * Sets the robot's Y location at a settlement.
	 * 
	 * @param yLocation
	 */
	public void setYLocation(double yLocation) {
		this.yLoc = yLocation;
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

		if (c instanceof Settlement) {
			return (Settlement) c;
		}

		else if (c instanceof Vehicle) {
			Building b = BuildingManager.getBuilding((Vehicle) getContainerUnit());
			if (b != null)
				// still inside the garage
				return b.getSettlement();
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
		else
			return null;
	}

	/**
	 * Sets the unit's container unit. Overridden from Unit class.
	 * 
	 * @param containerUnit the unit to contain this unit.
	 */
	public void setContainerUnit(Unit containerUnit) {
		super.setContainerUnit(containerUnit);
//		if (containerUnit instanceof Vehicle) {
//			vehicle = containerUnit.getIdentifier();
//		} else
//			vehicle = -1;
	}

	// TODO: allow parts to be recycled
	public void toBeSalvaged() {
		Unit containerUnit = getContainerUnit();
		if (!(containerUnit instanceof MarsSurface)) {
			containerUnit.getInventory().retrieveUnit(this);
		}
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

	MedicalAid getAccessibleAid() {
		return null;
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


//    /**
//     * robot consumes given amount of power.
//     * @param amount the amount of power to consume (in kg)
//     * @param takeFromInv is power taken from local inventory?
//
//    public void consumePower(double amount, boolean takeFromInv) {
//        if (takeFromInv) {
//            //System.out.println(this.getName() + " is calling consumeFood() in Robot.java");
//        	health.consumePower(amount, getContainerUnit());
//        }
//    }

	/**
	 * Gets the type of the robot.
	 * 
	 * @return RobotType
	 */
	public RobotType getRobotType() {
		return robotType;
	}

	public void setRobotType(RobotType t) {
		this.robotType = t;
	}

	/**
	 * Gets the birthplace of the robot
	 * 
	 * @return the birthplace
	 * @deprecated TODO internationalize the place of birth for display in user
	 *             interface.
	 */
	public String getBirthplace() {
		return birthplace;
	}

	/**
	 * Gets the robot's local group (in building or rover)
	 * 
	 * @return collection of robots in robot's location.
	 */
	public Collection<Robot> getLocalRobotGroup() {
		Collection<Robot> localRobotGroup = new ConcurrentLinkedQueue<Robot>();

		if (isInSettlement()) {
			Building building = BuildingManager.getBuilding(this);
			if (building != null) {
				if (building.hasFunction(FunctionType.ROBOTIC_STATION)) {
					RoboticStation roboticStation = building.getRoboticStation();
					localRobotGroup = new ConcurrentLinkedQueue<Robot>(roboticStation.getRobotOccupants());
				}
			}
		} else if (isInVehicle()) {
			Crewable robotCrewableVehicle = (Crewable) getVehicle();
			localRobotGroup = new ConcurrentLinkedQueue<Robot>(robotCrewableVehicle.getRobotCrew());
		}

		if (localRobotGroup.contains(this)) {
			localRobotGroup.remove(this);
		}
		return localRobotGroup;
	}

	/**
	 * Checks if the vehicle operator is fit for operating the vehicle.
	 * 
	 * @return true if vehicle operator is fit.
	 */
	public boolean isFitForOperatingVehicle() {
		return isFit(); 
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
	// TODO: associate each bot with its owner
	public Collection<Person> getAffectedPeople() {
		Collection<Person> people = new ConcurrentLinkedQueue<Person>();

		// Check all people.
		Iterator<Person> i = unitManager.getPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			Task task = person.getMind().getTaskManager().getTask();

			// Add all people maintaining this equipment.
			if (task instanceof Maintenance) {
				if (((Maintenance) task).getEntity() == this) {
					if (!people.contains(person))
						people.add(person);
				}
			}

			// Add all people repairing this equipment.
			if (task instanceof Repair) {
				if (((Repair) task).getEntity() == this) {
					if (!people.contains(person))
						people.add(person);
				}
			}
		}

		return people;
	}

	/**
	 * Checks if the item is salvaged.
	 * 
	 * @return true if salvaged.
	 */
	public boolean isSalvaged() {
		return isSalvaged;
	}

	public String getName() {
		return nickName;
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
//		currentBuilding = building;
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

	//@Override
	public void setShiftType(ShiftType shiftType) {
		// taskSchedule.setShiftType(shiftType);
	}

	public int getProduceFoodSkill() {
		int skill = skillManager.getEffectiveSkillLevel(SkillType.COOKING) * 5;
		skill += skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE) * 2;
		skill = (int) Math.round(skill / 7D);
		return skill;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String c) {
		this.country = c;
		if (c != null)
			birthplace = "Earth";
		else
			birthplace = "Mars";
	}

	/*
	 * Sets sponsoring agency for the person
	 */
	public void setSponsor(String sponsor) {
		this.sponsor = sponsor;
	}

	//@Override
	public void setVehicle(Vehicle vehicle) {
		// this.vehicle = vehicle;
	}

	@Override
	public String getNickName() {
		return nickName;
	}

	@Override
	public String getImmediateLocation() {
		return getLocationTag().getImmediateLocation();
	}

	/**
	 * Obtains the modified immediate location 
	 * 
	 * @return the name string of the location the unit is at
	 */
	public String getModifiedLoc() {
		return getLocationTag().getModifiedLoc();
	}
	
	@Override
	public String getLocale() {
		return getLocationTag().getLocale();
	}
	
	public String getExtendedLocations() {
		return getLocationTag().getExtendedLocation();
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
	public double getWalkSpeedMod() {
		double mass = getInventory().getTotalInventoryMass(false);
		double cap = getInventory().getGeneralCapacity();
		// At full capacity, may still move at 10% 
		return 1.1 - mass/cap;
	}
	
	public int getAge() {
		return age;
	}

	@Override
	public boolean isFit() {
		return !health.isInoperable();
	}
	
	@Override
	protected UnitType getUnitType() {
		return UnitType.ROBOT;
	}


	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Robot r = (Robot) obj;
		return this.getIdentifier() == r.getIdentifier()
				&& this.robotType == r.getRobotType(); 
//				&& this.nickName.equals(r.getNickName());
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
		birthTimeStamp = null;
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
	 * Obtains a robot type for the specfied Settlement
	 * 
	 * @param s The Settlement in question
	 * @return
	 */
	public static RobotType selectNewRobotType(Settlement s) {
	
		// Ordinal numder
		// 0 CHEFBOT
		// 1 CONSTRUCTIONBOT
		// 2 DELIVERYBOT
		// 3 GARDENBOT
		// 4 MAKERBOT
		// 5 MEDICBOT
		// 6 REPAIRBOT
		
		int[] numBots = new int[] { 0, 0, 0, 0, 0, 0, 0 };

		RobotType robotType = null;
		int max = s.getProjectedNumOfRobots();

		// find out how many in each robot type 
		// if robots already exists in this settlement
		Iterator<Robot> i = s.getRobots().iterator();
		while (i.hasNext()) {
			Robot robot = i.next();
				RobotType type = robot.getRobotType();
				int ordinalNum = type.ordinal();
				numBots[ordinalNum]++;
//				
//			if (robot.getRobotType() == RobotType.MAKERBOT)
//				numBots[0]++;
//			else if (robot.getRobotType() == RobotType.GARDENBOT)
//				numBots[1]++;
//			else if (robot.getRobotType() == RobotType.REPAIRBOT)
//				numBots[2]++;
//			else if (robot.getRobotType() == RobotType.CHEFBOT)
//				numBots[3]++;
//			else if (robot.getRobotType() == RobotType.MEDICBOT)
//				numBots[4]++;
//			else if (robot.getRobotType() == RobotType.DELIVERYBOT)
//				numBots[5]++;
//			else if (robot.getRobotType() == RobotType.CONSTRUCTIONBOT)
//				numBots[6]++;
		}

		// determine the robotType
		if (max <= 4) {
			if (numBots[2] < 1)
				robotType = RobotType.DELIVERYBOT;
			else if (numBots[4] < 1)
				robotType = RobotType.MAKERBOT;
			else if (numBots[3] < 1)
				robotType = RobotType.GARDENBOT;
			else if (numBots[6] < 1)
				robotType = RobotType.REPAIRBOT;
//			else if (numBots[0] < 1)
//				robotType = RobotType.CHEFBOT;
		}

		else if (max <= 6) {
			if (numBots[2] < 1)
				robotType = RobotType.DELIVERYBOT;
			else if (numBots[4] < 1)
				robotType = RobotType.MAKERBOT;
			else if (numBots[3] < 1)
				robotType = RobotType.GARDENBOT;
			else if (numBots[6] < 1)
				robotType = RobotType.REPAIRBOT;
			else if (numBots[0] < 1)
				robotType = RobotType.CHEFBOT;
//			else if (numBots[5] < 1)
//				robotType = RobotType.MEDICBOT;
//			 else if (numBots[6] < 1)
//			 robotType = RobotType.CONSTRUCTIONBOT;
		}

		else if (max <= 9) {
			if (numBots[2] < 1)
				robotType = RobotType.DELIVERYBOT;
			else if (numBots[4] < 2)
				robotType = RobotType.MAKERBOT;
			else if (numBots[3] < 2)
				robotType = RobotType.GARDENBOT;
			else if (numBots[6] < 1)
				robotType = RobotType.REPAIRBOT;
			else if (numBots[0] < 1)
				robotType = RobotType.CHEFBOT;
			else if (numBots[5] < 1)
				robotType = RobotType.MEDICBOT;
//			else if (numBots[2] < 1)
//				robotType = RobotType.DELIVERYBOT;
			else if (numBots[1] < 1)
				robotType = RobotType.CONSTRUCTIONBOT;
		}

		else if (max <= 12) {
			if (numBots[2] < 1)
				robotType = RobotType.DELIVERYBOT;
			else if (numBots[4] < 3)
				robotType = RobotType.MAKERBOT;
			else if (numBots[3] < 3)
				robotType = RobotType.GARDENBOT;
			else if (numBots[6] < 2)
				robotType = RobotType.REPAIRBOT;
			else if (numBots[0] < 1)
				robotType = RobotType.CHEFBOT;
			else if (numBots[5] < 1)
				robotType = RobotType.MEDICBOT;
//			else if (numBots[2] < 1)
//				robotType = RobotType.DELIVERYBOT;
			else if (numBots[1] < 1)
				robotType = RobotType.CONSTRUCTIONBOT;
		}

		else if (max <= 18) {
			if (numBots[2] < 2)
				robotType = RobotType.DELIVERYBOT;
			else if (numBots[4] < 5)
				robotType = RobotType.MAKERBOT;
			else if (numBots[3] < 3)
				robotType = RobotType.GARDENBOT;
			else if (numBots[6] < 4)
				robotType = RobotType.REPAIRBOT;
			else if (numBots[0] < 2)
				robotType = RobotType.CHEFBOT;
			else if (numBots[5] < 1)
				robotType = RobotType.MEDICBOT;
//			else if (numBots[2] < 1)
//				robotType = RobotType.DELIVERYBOT;
			else if (numBots[1] < 1)
				robotType = RobotType.CONSTRUCTIONBOT;
		}

		else if (max <= 24) {
			if (numBots[2] < 2)
				robotType = RobotType.DELIVERYBOT;
			else if (numBots[4] < 7)
				robotType = RobotType.MAKERBOT;
			else if (numBots[3] < 5)
				robotType = RobotType.GARDENBOT;
			else if (numBots[6] < 5)
				robotType = RobotType.REPAIRBOT;
			else if (numBots[0] < 2)
				robotType = RobotType.CHEFBOT;
			else if (numBots[5] < 1)
				robotType = RobotType.MEDICBOT;
//			else if (numBots[2] < 1)
//				robotType = RobotType.DELIVERYBOT;
			else if (numBots[1] < 2)
				robotType = RobotType.CONSTRUCTIONBOT;
		}

		else if (max <= 36) {
			if (numBots[2] < 2)
				robotType = RobotType.DELIVERYBOT;
			else if (numBots[4] < 9)
				robotType = RobotType.MAKERBOT;
			else if (numBots[3] < 9)
				robotType = RobotType.GARDENBOT;
			else if (numBots[6] < 7)
				robotType = RobotType.REPAIRBOT;
			else if (numBots[0] < 5)
				robotType = RobotType.CHEFBOT;
			else if (numBots[5] < 1)
				robotType = RobotType.MEDICBOT;
//			else if (numBots[2] < 2)
//				robotType = RobotType.DELIVERYBOT;
			else if (numBots[1] < 3)
				robotType = RobotType.CONSTRUCTIONBOT;
		}

		else if (max <= 48) {
			if (numBots[2] < 3)
				robotType = RobotType.DELIVERYBOT;
			else if (numBots[4] < 11)
				robotType = RobotType.MAKERBOT;
			else if (numBots[3] < 11)
				robotType = RobotType.GARDENBOT;
			else if (numBots[6] < 10)
				robotType = RobotType.REPAIRBOT;
			else if (numBots[0] < 7)
				robotType = RobotType.CHEFBOT;
			else if (numBots[5] < 2)
				robotType = RobotType.MEDICBOT;
//			else if (numBots[2] < 3)
//				robotType = RobotType.DELIVERYBOT;
			else if (numBots[1] < 4)
				robotType = RobotType.CONSTRUCTIONBOT;
		}

		else {
			if (numBots[2] < 3)
				robotType = RobotType.DELIVERYBOT;
			else if (numBots[4] < 11)
				robotType = RobotType.MAKERBOT;
			else if (numBots[3] < 11)
				robotType = RobotType.GARDENBOT;
			else if (numBots[6] < 10)
				robotType = RobotType.REPAIRBOT;
			else if (numBots[0] < 7)
				robotType = RobotType.CHEFBOT;
			else if (numBots[5] < 2)
				robotType = RobotType.MEDICBOT;
//			else if (numBots[2] < 3)
//				robotType = RobotType.DELIVERYBOT;
			else if (numBots[1] < 4)
				robotType = RobotType.CONSTRUCTIONBOT;
			else {
				int rand = RandomUtil.getRandomInt(20);
				if (rand <= 3)
					robotType = RobotType.MAKERBOT;
				else if (rand <= 7)
					robotType = RobotType.GARDENBOT;
				else if (rand <= 11)
					robotType = RobotType.REPAIRBOT;
				else if (rand <= 14)
					robotType = RobotType.CHEFBOT;
				else if (rand <= 16)
					robotType = RobotType.DELIVERYBOT;
				else if (rand <= 18)
					robotType = RobotType.CONSTRUCTIONBOT;
				else if (rand <= 20)
					robotType = RobotType.MEDICBOT;
				else
					robotType = RobotType.MAKERBOT;
			}
		}

		if (robotType == null) {
			logger.config("UnitManager : robotType is null");
			robotType = RobotType.MAKERBOT;
		}
		return robotType;
	}
}
