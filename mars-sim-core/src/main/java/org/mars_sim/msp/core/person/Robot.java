/**
 * Mars Simulation Project
 * Robot.java
 * @version 3.08 2015-02-10
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LifeSupport;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.medical.MedicalAid;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleOperator;

/**
 * The robot class represents a robot on Mars. It keeps track of everything
 * related to that robot and provides information about him/her.
 */
public class Robot
extends Unit
//extends Person
implements VehicleOperator, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /* default logger. */
	private static transient Logger logger = Logger.getLogger(Robot.class.getName());
     

    /** The base carrying capacity (kg) of a robot. */
    private final static double BASE_CAPACITY = 60D;

    // Data members
    /** Manager for robot's natural attributes. */
    private NaturalAttributeManager attributes;
    /** robot's mind. */
    private Mind mind;
    /** robot's physical condition. */
    private PhysicalCondition health;
    /** True if robot is dead and buried. */
    private boolean isBuried;
    /** The height of the robot (in cm). */
    private int height;
    /** The birthplace of the robot. */
    private String birthplace;
    /** The birth time of the robot. */
    private EarthClock birthTimeStamp;
    /** The settlement the robot is currently associated with. */
    private Settlement associatedSettlement;
    /** The robot's achievement in scientific fields. */
    private Map<ScienceType, Double> scientificAchievement;
    /** Settlement X location (meters) from settlement center. */
    private double xLoc;
    /** Settlement Y location (meters) from settlement center. */
    private double yLoc;
    
    private RobotType robotType;
    private String name;
    
    /**
     * Constructs a robot object at a given settlement.
     * @param name the robot's name
     * @param gender {@link robotGender} the robot's gender
     * @param birthplace the location of the robot's birth
     * @param settlement {@link Settlement} the settlement the robot is at
     * @throws Exception if no inhabitable building available at settlement.
     */
    public Robot(String name, RobotType robotType, String birthplace, Settlement settlement) {
        super(name, settlement.getCoordinates()); // if extending Unit
        //super(name, null, birthplace, settlement); // if extending Person
         
        this.name = name;
        this.robotType = robotType;
        this.birthplace = birthplace;
        this.associatedSettlement = settlement;
        // Initialize data members
        xLoc = 0D;
        yLoc = 0D;
        isBuried = false;
        
        String timeString = createTimeString();
        
        birthTimeStamp = new EarthClock(timeString);
        attributes = new NaturalAttributeManager(this);
        mind = new Mind(this);
        health = new PhysicalCondition(this);
        scientificAchievement = new HashMap<ScienceType, Double>(0);

        setBaseMass(100D + (RandomUtil.getRandomInt(100) + RandomUtil.getRandomInt(100))/10D);
        height = 156 + RandomUtil.getRandomInt(22);
        
        // Set inventory total mass capacity based on the robot's strength.
        int strength = attributes.getAttribute(NaturalAttribute.STRENGTH);
        getInventory().addGeneralCapacity(BASE_CAPACITY + strength);

        // Put robot in proper building.
        settlement.getInventory().storeUnit(this);
        BuildingManager.addToRandomBuilding(this, settlement);
        
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
    public Settlement getSettlement() {
        if (LocationSituation.IN_SETTLEMENT == getLocationSituation())
            return (Settlement) getContainerUnit();
        else
            return null;
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
     * @param containerUnit
     *            the unit to contain this unit.
     */
    public void setContainerUnit(Unit containerUnit) {
        
        super.setContainerUnit(containerUnit);
    }


    // TODO: allow parts to be recycled
    public void buryBody() {
        Unit containerUnit = getContainerUnit();
        if (containerUnit != null) {
            containerUnit.getInventory().retrieveUnit(this);
        }
        isBuried = true;
        setAssociatedSettlement(null);
    }


    // TODO: allow robot parts to be stowed in storage 
    void setDead() {
        mind.setInactive();
        buryBody();
    }

    /**
     * robot can take action with time passing
     * @param time amount of time passing (in millisols).
     */
    public void timePassing(double time) {

        // If robot is dead, then skip
        if (health.getDeathDetails() == null) {

            RobotConfig config = SimulationConfig.instance().getRobotConfiguration();
            LifeSupport support = getLifeSupport();

            // Pass the time in the physical condition first as this may
            // result in death.
            if (health.timePassing(time, support, config)) {

                // Mental changes with time passing.
                mind.timePassing(time);
            } 
            else {
                // robot has died as a result of physical condition
                setDead();
            }
        }

    }

    /**
     * Returns a reference to the robot's natural attribute manager
     * @return the robot's natural attribute manager
     */
    public NaturalAttributeManager getNaturalAttributeManager() {
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
     * Returns a reference to the robot's physical condition
     * @return the robot's physical condition
     */
    public PhysicalCondition getPhysicalCondition() {
        return health;
    }

    MedicalAid getAccessibleAid() {
		return null; }

    /**
     * Returns the robot's mind
     * @return the robot's mind
     */
    public Mind getMind() {
        return mind;
    }

    /**
     * Returns the robot's age
     * @return the robot's age
     */
    public int getAge() {
        EarthClock simClock = Simulation.instance().getMasterClock().getEarthClock();
        int age = simClock.getYear() - birthTimeStamp.getYear() - 1;
        if (simClock.getMonth() >= birthTimeStamp.getMonth()
                && simClock.getMonth() >= birthTimeStamp.getMonth()) {
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
    private LifeSupport getLifeSupport() {

        LifeSupport result = null;
        List<LifeSupport> lifeSupportUnits = new ArrayList<LifeSupport>();

        Settlement settlement = getSettlement();
        if (settlement != null) {
            lifeSupportUnits.add(settlement);
        }
        else {
            Vehicle vehicle = getVehicle();
            if ((vehicle != null) && (vehicle instanceof LifeSupport)) {
                
                if (BuildingManager.getBuilding(vehicle) != null) {
                    lifeSupportUnits.add(vehicle.getSettlement());
                }
                else {
                    lifeSupportUnits.add((LifeSupport) vehicle);
                }
            }
        }

        // Get all contained units.
        Iterator<Unit> i = getInventory().getContainedUnits().iterator();
        while (i.hasNext()) {
            Unit contained = i.next();
            if (contained instanceof LifeSupport) {
                lifeSupportUnits.add((LifeSupport) contained);
            }
        }

        // TODO: turn off the checking of oxygen and water for robot
        // Get first life support unit that checks out.
        Iterator<LifeSupport> j = lifeSupportUnits.iterator();
        while (j.hasNext() && (result == null)) {
            LifeSupport goodUnit = j.next();
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

    public void consumeFood(double amount, boolean takeFromInv) {}
    
    public void consumeDessert(double amount, boolean takeFromInv) {}
    	 
    /**
     * robot consumes given amount of power.
     * @param amount the amount of power to consume (in kg)
     * @param takeFromInv is power taken from local inventory?
     */
    public void consumePower(double amount, boolean takeFromInv) {
        if (takeFromInv) {
            //System.out.println(this.getName() + " is calling consumeFood() in Robot.java");
        	health.consumePower(amount, getContainerUnit());
        }
    }

    public PersonGender getGender() {return null;}
    
    /**
     * Gets the gender of the robot.
     * @return the gender
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

    //public Collection<Person> getLocalGroup() {return null;}
    
    /**
     * Gets the robot's local group (in building or rover)
     * @return collection of robots in robot's location.
     */
    public Collection<Robot> getLocalGroup() {
        Collection<Robot> localGroup = new ConcurrentLinkedQueue<Robot>();

        if (getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            Building building = BuildingManager.getBuilding(this);
            if (building != null) {
                if (building.hasFunction(BuildingFunction.LIFE_SUPPORT)) {
                    org.mars_sim.msp.core.structure.building.function.LifeSupport lifeSupport = 
                            (org.mars_sim.msp.core.structure.building.function.LifeSupport) 
                            building.getFunction(BuildingFunction.LIFE_SUPPORT);
                    localGroup = new ConcurrentLinkedQueue<Robot>(lifeSupport.getRobotOccupants());
                }
            }
        } else if (getLocationSituation() == LocationSituation.IN_VEHICLE) {
            Crewable crewableVehicle = (Crewable) getVehicle();
            localGroup = new ConcurrentLinkedQueue<Robot>(crewableVehicle.getRobots());
        }

        if (localGroup.contains(this)) {
            localGroup.remove(this);
        }
        return localGroup;
    }

    /**
     * Checks if the vehicle operator is fit for operating the vehicle.
     * @return true if vehicle operator is fit.
     */
    public boolean isFitForOperatingVehicle() {
        return !health.hasSeriousMedicalProblems();
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
    
    
    @Override
    public void destroy() {
        super.destroy();
        attributes.destroy();
        attributes = null;
        mind.destroy();
        mind = null;
        health.destroy();
        health = null;
        birthTimeStamp = null;
        associatedSettlement = null;
        scientificAchievement.clear();
        scientificAchievement = null;
    }
}