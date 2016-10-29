/**
 * Mars Simulation Project
 * Robot.java
 * @version 3.08 2016-05-04
 * @author Manny Kung
 */

package org.mars_sim.msp.core.robot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.manufacture.Salvagable;
import org.mars_sim.msp.core.manufacture.SalvageInfo;
import org.mars_sim.msp.core.manufacture.SalvageProcessInfo;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.robot.RoboticAttribute;
import org.mars_sim.msp.core.robot.RoboticAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ShiftType;
import org.mars_sim.msp.core.person.TaskSchedule;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.medical.MedicalAid;
import org.mars_sim.msp.core.robot.ai.BotMind;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.RoboticStation;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleOperator;

/**
 * The robot class represents a robot on Mars. It keeps track of everything
 * related to that robot
 */
public class Robot
//extends Unit
extends Equipment
implements Salvagable,  Malfunctionable, VehicleOperator, MissionMember, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /* default logger. */
	private static transient Logger logger = Logger.getLogger(Robot.class.getName());
    /** The base carrying capacity (kg) of a robot. */
    private final static double BASE_CAPACITY = 60D;

	// Static members
	public static final String TYPE = "Robot";
	/** Unloaded mass of EVA suit (kg.). */
	public static final double EMPTY_MASS = 80D;

	/** 334 Sols (1/2 orbit). */
	private static final double WEAR_LIFETIME = 334000D;
	/** 100 millisols. */
	private static final double MAINTENANCE_TIME = 100D;


    // Data members
    private String name;
    private Person owner;
    /** The height of the robot (in cm). */
    private int height;
    /** Settlement X location (meters) from settlement center. */
    private double xLoc;
    /** Settlement Y location (meters) from settlement center. */
    private double yLoc;
    /** True if robot is dead and buried. */
    private boolean isBuried;
	private boolean isSalvaged;

    /** The robot's achievement in scientific fields. */
    //private Map<ScienceType, Double> scientificAchievement;

    /** Manager for robot's natural attributes. */
    private RoboticAttributeManager attributes;
    /** robot's mind. */
    private BotMind botMind;
    /** robot's System condition. */
    private SystemCondition health;

	private SalvageInfo salvageInfo;
	/** The equipment's malfunction manager. */
	protected MalfunctionManager malfunctionManager;
    /** The birthplace of the robot. */
    private String birthplace;
    /** The birth time of the robot. */
    private EarthClock birthTimeStamp;
    /** The settlement the robot is currently associated with. */
    private Settlement associatedSettlement;

    private TaskSchedule taskSchedule;

    private RobotType robotType;

    private RobotConfig config ;

    private LifeSupportType support ;

    private MarsClock marsClock;
    
    private EarthClock earthClock;
    
    private MasterClock masterClock;
    
  
    /**
     * Constructs a robot object at a given settlement.
     * @param name the robot's name
     * @param gender {@link robotGender} the robot's gender
     * @param birthplace the location of the robot's birth
     * @param settlement {@link Settlement} the settlement the robot is at
     * @throws Exception if no inhabitable building available at settlement.
     */
    public Robot(String name, RobotType robotType, String birthplace, Settlement settlement, Coordinates location) {
        super(name, location); // if extending equipment
    	//super(name, settlement.getCoordinates()); // if extending Unit
        //super(name, null, birthplace, settlement); // if extending Person

		// Initialize data members.
        this.name = name;
		isSalvaged = false;
		salvageInfo = null;
        this.name = name;
        this.robotType = robotType;
        this.birthplace = birthplace;
        this.associatedSettlement = settlement;
        // Initialize data members
        xLoc = 0D;
        yLoc = 0D;
        isBuried = false;

        masterClock = Simulation.instance().getMasterClock();
        marsClock = masterClock.getMarsClock();
        earthClock = masterClock.getEarthClock();
        
        config = SimulationConfig.instance().getRobotConfiguration();
        support = getLifeSupportType();

		// Add scope to malfunction manager.
		malfunctionManager = new MalfunctionManager(this, WEAR_LIFETIME, MAINTENANCE_TIME);
		malfunctionManager.addScopeString(TYPE);

        String timeString = createTimeString();

        birthTimeStamp = new EarthClock(timeString);
        attributes = new RoboticAttributeManager(this);
        botMind = new BotMind(this);
        health = new SystemCondition(this);
        //scientificAchievement = new HashMap<ScienceType, Double>(0);
        // 2015-03-19 Added TaskSchedule class
        taskSchedule = new TaskSchedule(this);

        setBaseMass(100D + (RandomUtil.getRandomInt(100) + RandomUtil.getRandomInt(100))/10D);
        height = 156 + RandomUtil.getRandomInt(22);

        // Set inventory total mass capacity based on the robot's strength.
        int strength = attributes.getAttribute(RoboticAttribute.STRENGTH);
        getInventory().addGeneralCapacity(BASE_CAPACITY + strength);

        // Put robot into the settlement.
        settlement.getInventory().storeUnit(this);
        // Put robot in proper building.
        BuildingManager.addToRandomBuilding(this, settlement);
    }


    /**
     * Gets the instance of the task schedule for a person.
     */
    public TaskSchedule getTaskSchedule() {
    	return taskSchedule;
    }

    /**
     * Create a string representing the birth time of the robot.
     * @return birth time string.
     */
    private String createTimeString() {
        // Set a birth time for the robot
        int year = 2043 + RandomUtil.getRandomInt(10)
                + RandomUtil.getRandomInt(10);
        int month = RandomUtil.getRandomInt(11) + 1;
        int day;
        if (month == 2) {
            if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
                day = RandomUtil.getRandomInt(28) + 1;
            } else {
                day = RandomUtil.getRandomInt(27) + 1;
            }
        } else {
            if (month % 2 == 1) {
                day = RandomUtil.getRandomInt(30) + 1;
            } else {
                day = RandomUtil.getRandomInt(29) + 1;
            }
        }

        int hour = RandomUtil.getRandomInt(23);
        int minute = RandomUtil.getRandomInt(59);
        int second = RandomUtil.getRandomInt(59);

        return month + "/" + day + "/" + year + " " + hour + ":"
        + minute + ":" + second;
    }

    /**
     * @return {@link LocationSituation} the robot's location
     */
    public LocationSituation getLocationSituation() {
        if (isBuried)
            return LocationSituation.BURIED;
        else {
            Unit container = getContainerUnit();
            if (container == null)
                return LocationSituation.OUTSIDE;
            else if (container instanceof Settlement)
                return LocationSituation.IN_SETTLEMENT;
            else if (container instanceof Vehicle)
                return LocationSituation.IN_VEHICLE;
        }
        return null;
    }

    /**
     * Gets the robot's X location at a settlement.
     * @return X distance (meters) from the settlement's center.
     */
    public double getXLocation() {
        return xLoc;
    }

    /**
     * Sets the robot's X location at a settlement.
     * @param xLocation the X distance (meters) from the settlement's center.
     */
    public void setXLocation(double xLocation) {
        this.xLoc = xLocation;
    }

    /**
     * Gets the robot's Y location at a settlement.
     * @return Y distance (meters) from the settlement's center.
     */
    public double getYLocation() {
        return yLoc;
    }

    /**
     * Sets the robot's Y location at a settlement.
     * @param yLocation
     */
    public void setYLocation(double yLocation) {
        this.yLoc = yLocation;
    }

    /**
     * Get settlement robot is at, null if robot is not at a settlement
     * @return the robot's settlement
     */
   // 2015-12-04 Changed getSettlement() to fit the original specs of the Location Matrix
   public Settlement getSettlement() {
       if (getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
    	   Settlement settlement = (Settlement) getContainerUnit();
    	   return settlement;
       }

       else if (getLocationSituation() == LocationSituation.OUTSIDE)
    	   return null;

       else if (getLocationSituation() == LocationSituation.IN_VEHICLE) {
    	   Vehicle vehicle = (Vehicle) getContainerUnit();
    	   Settlement settlement = (Settlement) vehicle.getContainerUnit();
    	   return settlement;
       }

       else if (getLocationSituation() == LocationSituation.BURIED) {
    	   return null;
       }

       else {
    	   System.err.println("Error in determining " + getName() + "'s getSettlement() ");
    	   return null;
       }
   }


    /**
     * Get vehicle robot is in, null if robot is not in vehicle
     *
     * @return the robot's vehicle
     */
    public Vehicle getVehicle() {
        if (LocationSituation.IN_VEHICLE == getLocationSituation())
            return (Vehicle) getContainerUnit();
        else
            return null;
    }

    /**
     * Sets the unit's container unit. Overridden from Unit class.
     * @param containerUnit the unit to contain this unit.
     */
    public void setContainerUnit(Unit containerUnit) {
        super.setContainerUnit(containerUnit);
    }


    // TODO: allow parts to be recycled
    public void toBeSalvaged() {
        Unit containerUnit = getContainerUnit();
        if (containerUnit != null) {
            containerUnit.getInventory().retrieveUnit(this);
        }
        isBuried = true;
        setAssociatedSettlement(null);
    }


    // TODO: allow robot parts to be stowed in storage
    void setInoperable() {
        botMind.setInactive();
        toBeSalvaged();
    }

    /**
     * robot can take action with time passing
     * @param time amount of time passing (in millisols).
     */
    public void timePassing(double time) {
/*
    	// convert to using owner

		Unit container = getContainerUnit();
		if (container instanceof Person) {
			Person person = (Person) container;
			if (!person.getPhysicalCondition().isDead()) {
				malfunctionManager.activeTimePassing(time);
			}
		}
		malfunctionManager.timePassing(time);
*/
        // If robot is dead, then skip
        if (!health.isInoperable()) {

        	support = getLifeSupportType();
            // Pass the time in the physical condition first as this may
            // result in death.
            if (health.timePassing(time, support, config)) {

                // Mental changes with time passing.
                botMind.timePassing(time);
            }
            else {
                // robot has died as a result of physical condition
                setInoperable();
            }
        }
    }


    /**
     * Returns a reference to the robot's attribute manager
     * @return the robot's natural attribute manager
     */
    public RoboticAttributeManager getRoboticAttributeManager() {
        return attributes;
    }

    /**
     * Get the performance factor that effect robot with the complaint.
     * @return The value is between 0 -> 1.
     */
    public double getPerformanceRating() {
        return health.getPerformanceFactor();
    }

    /**
     * Returns a reference to the robot's system condition
     * @return the robot's batteryCondition
     */
    public SystemCondition getSystemCondition() {
        return health;
    }

    MedicalAid getAccessibleAid() {
		return null; }

    /**
     * Returns the robot's mind
     * @return the robot's mind
     */
    public BotMind getBotMind() {
        return botMind;
    }

    /**
     * Returns the robot's age
     * @return the robot's age
     */
    public int getAge() {
        //EarthClock simClock = Simulation.instance().getMasterClock().getEarthClock();
        int age = earthClock.getYear() - birthTimeStamp.getYear() - 1;
        if (earthClock.getMonth() >= birthTimeStamp.getMonth()
                && earthClock.getMonth() >= birthTimeStamp.getMonth()) {
            age++;
        }

        return age;
    }

    /**
     * Returns the robot's height in cm
     * @return the robot's height
     */
    public int getHeight() {
        return height;
    }


    /**
     * Returns the robot's birth date
     * @return the robot's birth date
     */
    public String getBirthDate() {
        return birthTimeStamp.getDateString();
    }

    /**
     * Get the LifeSupport system supporting this robot. This may be from the
     * Settlement, Vehicle or Equipment.
     * @return Life support system.
     */
    private LifeSupportType getLifeSupportType() {

        LifeSupportType result = null;
        List<LifeSupportType> lifeSupportUnits = new ArrayList<LifeSupportType>();

        Settlement settlement = getSettlement();
        if (settlement != null) {
            lifeSupportUnits.add(settlement);
        }
        else {
            Vehicle vehicle = getVehicle();
            if ((vehicle != null) && (vehicle instanceof LifeSupportType)) {

                if (BuildingManager.getBuilding(vehicle) != null) {
                    lifeSupportUnits.add(vehicle.getSettlement());
                }
                else {
                    lifeSupportUnits.add((LifeSupportType) vehicle);
                }
            }
        }

        // Get all contained units.
        Iterator<Unit> i = getInventory().getContainedUnits().iterator();
        while (i.hasNext()) {
            Unit contained = i.next();
            if (contained instanceof LifeSupportType) {
                lifeSupportUnits.add((LifeSupportType) contained);
            }
        }

        // TODO: turn off the checking of oxygen and water for robot
        // Get first life support unit that checks out.
        Iterator<LifeSupportType> j = lifeSupportUnits.iterator();
        while (j.hasNext() && (result == null)) {
            LifeSupportType goodUnit = j.next();
            if (goodUnit.lifeSupportCheck()) {
                result = goodUnit;
            }
        }

        // If no good units, just get first life support unit.
        if ((result == null) && (lifeSupportUnits.size() > 0)) {
            result = lifeSupportUnits.get(0);
        }

        return result;
    }

    /**
     * robot consumes given amount of power.
     * @param amount the amount of power to consume (in kg)
     * @param takeFromInv is power taken from local inventory?

    public void consumePower(double amount, boolean takeFromInv) {
        if (takeFromInv) {
            //System.out.println(this.getName() + " is calling consumeFood() in Robot.java");
        	health.consumePower(amount, getContainerUnit());
        }
    }
*/

    /**
     * Gets the type of the robot.
     * @return RobotType
     */
    public RobotType getRobotType() {
       return robotType;
    }

    /**
     * Gets the birthplace of the robot
     * @return the birthplace
     * @deprecated
     * TODO internationalize the place of birth for display in user interface.
     */
    public String getBirthplace() {
        return birthplace;
    }

    /**
     * Gets the robot's local group (in building or rover)
     * @return collection of robots in robot's location.
     */
    public Collection<Robot> getLocalRobotGroup() {
        Collection<Robot> localRobotGroup = new ConcurrentLinkedQueue<Robot>();

        if (getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            Building building = BuildingManager.getBuilding(this);
            if (building != null) {
                if (building.hasFunction(BuildingFunction.ROBOTIC_STATION)) {
                    RoboticStation roboticStation = (RoboticStation) building.getFunction(BuildingFunction.ROBOTIC_STATION);
                    localRobotGroup = new ConcurrentLinkedQueue<Robot>(roboticStation.getRobotOccupants());
                }
            }
        } else if (getLocationSituation() == LocationSituation.IN_VEHICLE) {
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
     * @return true if vehicle operator is fit.
     */
    public boolean isFitForOperatingVehicle() {
        return false; //!health.hasSeriousMedicalProblems();
    }

    /**
     * Gets the name of the vehicle operator
     * @return vehicle operator name.
     */
    public String getOperatorName() {
        return getName();
    }

    /**
     * Gets the settlement the robot is currently associated with.
     * @return associated settlement or null if none.
     */
    public Settlement getAssociatedSettlement() {
        return associatedSettlement;
    }

    /**
     * Sets the associated settlement for a robot.
     * @param newSettlement the new associated settlement or null if none.
     */
    public void setAssociatedSettlement(Settlement newSettlement) {
        if (associatedSettlement != newSettlement) {
            Settlement oldSettlement = associatedSettlement;
            associatedSettlement = newSettlement;
            fireUnitUpdate(UnitEventType.ASSOCIATED_SETTLEMENT_EVENT, associatedSettlement);
            if (oldSettlement != null) {
                oldSettlement.fireUnitUpdate(UnitEventType.REMOVE_ASSOCIATED_ROBOT_EVENT, this);
            }
            if (newSettlement != null) {
                newSettlement.fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_ROBOT_EVENT, this);
            }
        }
    }

    public double getScientificAchievement(ScienceType science) { return 0;}

    public double getTotalScientificAchievement() {return 0;}

    public void addScientificAchievement(double achievementCredit, ScienceType science) {}


	/**
	 * Gets a collection of people affected by this malfunction bots
	 * @return person collection
	 */
    //TODO: associate each bot with its owner
	public Collection<Person> getAffectedPeople() {
		Collection<Person> people = new ConcurrentLinkedQueue<Person>();

		// Check all people.
		Iterator<Person> i = Simulation.instance().getUnitManager().getPeople().iterator();
		while (i.hasNext()) {
			Person person = i.next();
			Task task = person.getMind().getTaskManager().getTask();

			// Add all people maintaining this equipment.
			if (task instanceof Maintenance) {
				if (((Maintenance) task).getEntity() == this) {
					if (!people.contains(person)) people.add(person);
				}
			}

			// Add all people repairing this equipment.
			if (task instanceof Repair) {
				if (((Repair) task).getEntity() == this) {
					if (!people.contains(person)) people.add(person);
				}
			}
		}

		return people;
	}

	/**
	 * Checks if the item is salvaged.
	 * @return true if salvaged.
	 */
	public boolean isSalvaged() {
		return isSalvaged;
	}

	public String getName() {
		return name;
	}

	/**
	 * Indicate the start of a salvage process on the item.
	 * @param info the salvage process info.
	 * @param settlement the settlement where the salvage is taking place.
	 */
	public void startSalvage(SalvageProcessInfo info, Settlement settlement) {
		salvageInfo = new SalvageInfo(this, info, settlement);
		isSalvaged = true;
	}

	/**
	 * Gets the salvage info.
	 * @return salvage info or null if item not salvaged.
	 */
	public SalvageInfo getSalvageInfo() {
		return salvageInfo;
	}


	/**
	 * Gets the unit's malfunction manager.
	 * @return malfunction manager
	 */
	public MalfunctionManager getMalfunctionManager() {
		return malfunctionManager;
	}


	/**
	  * Gets the building the robot is located at
	  * Returns null if outside of a settlement
	  * @return building
	  */
	// 2015-10-78 Added getBuildingLocation()
    public Building getBuildingLocation() {
        Building result = null;
        if (getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            BuildingManager manager = getSettlement().getBuildingManager();
            result = manager.getBuildingAtPosition(getXLocation(), getYLocation());
            //List<Building> buildings = manager.getBuildings();
            //Iterator<Building> i = buildings.iterator();
            // while (i.hasNext()) {
            //	Building b = i.next();
            //	String buildingType = b.getBuildingType();
            //}
        }

        return result;
    }

	@Override
    public String getTaskDescription() {
        return getBotMind().getBotTaskManager().getTaskDescription(true);
    }

	@Override
	public void setMission(Mission newMission) {
	    getBotMind().setMission(newMission);
	}

	@Override
	public void setShiftType(ShiftType shiftType) {
		//taskSchedule.setShiftType(shiftType);
	}

	public int getProduceFoodSkill() {
		//SkillManager skillManager = robot.getBotMind().getSkillManager();
		int skill = getSkillManager().getEffectiveSkillLevel(SkillType.COOKING) * 5;
	    skill += getSkillManager().getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE) * 2;
	    skill = (int) Math.round(skill / 7D);
	    return skill;
	}
    
	public SkillManager getSkillManager() {
		return botMind.getSkillManager();
	}
	
	
    @Override
    public void destroy() {
        super.destroy();
    	if (salvageInfo != null) salvageInfo.destroy();
		salvageInfo = null;
        attributes.destroy();
        attributes = null;
        botMind.destroy();
        botMind = null;
        health.destroy();
        health = null;
        birthTimeStamp = null;
        associatedSettlement = null;
        //scientificAchievement.clear();
        //scientificAchievement = null;
    }
}