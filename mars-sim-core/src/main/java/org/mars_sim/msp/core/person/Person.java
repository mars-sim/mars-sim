/**
 * Mars Simulation Project
 * Person.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;
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
import org.mars_sim.msp.core.science.Science;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Medical;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleOperator;

/**
 * The Person class represents a person on Mars. It keeps track of everything
 * related to that person and provides information about him/her.
 */
public class Person extends Unit implements VehicleOperator, Serializable {

    private static transient Logger logger = Logger.getLogger(Person.class.getName());

    /**
     * Status string used when Person resides in settlement
     */
    public final static String INSETTLEMENT = "In Settlement";

    /**
     * Status string used when Person resides in a vehicle
     */
    public final static String INVEHICLE = "In Vehicle";

    /**
     * Status string used when Person is outside
     */
    public final static String OUTSIDE = "Outside";

    /**
     * Status string used when Person has been buried
     */
    public final static String BURIED = "Buried";

    public final static String MALE = "male";
    public final static String FEMALE = "female";

    // The base carrying capacity (kg) of a person.
    private final static double BASE_CAPACITY = 60D;

    // Data members
    private NaturalAttributeManager attributes; // Manager for Person's natural
                                                // attributes
    private Mind mind; // Person's mind
    private PhysicalCondition health; // Person's physical
    private boolean isBuried; // True if person is dead and buried.
    private String gender; // The gender of the person (male or female).
    private int height; // The height of the person (in cm).
    private String birthplace; // The birthplace of the person.
    private EarthClock birthTimeStamp; // The birth time of the person.
    private Settlement associatedSettlement; // The settlement the person is
                                             // currently associated with.
    private Map<Science, Double> scientificAchievement; // The person's achievement in
                                                        // scientific fields.
    private double xLoc; // Settlement X location (meters) from settlement center.
    private double yLoc; // Settlement Y location (meters) from settlement center.

    /**
     * Constructs a Person object at a given settlement
     * 
     * @param name the person's name
     * @param gender the person's gender ("male" or "female")
     * @param birthplace the location of the person's birth
     * @param settlement the settlement the person is at
     * @throws Exception if no inhabitable building available at settlement.
     */
    public Person(String name, String gender, String birthplace, Settlement settlement) {
        // Use Unit constructor
        super(name, settlement.getCoordinates());

        // Initialize data members
        xLoc = 0D;
        yLoc = 0D;
        this.gender = gender;
        this.birthplace = birthplace;
        String timeString = createTimeString();
        birthTimeStamp = new EarthClock(timeString);
        attributes = new NaturalAttributeManager(this);
        mind = new Mind(this);
        isBuried = false;
        health = new PhysicalCondition(this);
        scientificAchievement = new HashMap<Science, Double>(0);

        // Set base mass of person from 58 to 76, peaking at 67.
        setBaseMass(56D + (RandomUtil.getRandomInt(100)
                + RandomUtil.getRandomInt(100))/10D);

        // Set height of person as gender-correlated curve.
        height = ( this.gender == "MALE" ? 156 + (RandomUtil.getRandomInt(22)
                + RandomUtil.getRandomInt(22)) : 146 + (RandomUtil.getRandomInt(15)
                        + RandomUtil.getRandomInt(15)) );
        
        // Set inventory total mass capacity based on the person's strength.
        int strength = attributes
                .getAttribute(NaturalAttributeManager.STRENGTH);
        getInventory().addGeneralCapacity(BASE_CAPACITY + strength);

        // Put person in proper building.
        settlement.getInventory().storeUnit(this);
        BuildingManager.addToRandomBuilding(this, settlement);
        associatedSettlement = settlement;
    }

    /**
     * Create a string representing the birth time of the person.
     * @return birth time string.
     */
    private String createTimeString() {
        // Set a birth time for the person
        int year = 2003 + RandomUtil.getRandomInt(10)
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
     * Returns a string for the person's relative location "In Settlement", "In
     * Vehicle", "Outside" or "Buried"
     * 
     * @return the person's location
     */
    public String getLocationSituation() {
        String location = null;

        if (isBuried)
            location = BURIED;
        else {
            Unit container = getContainerUnit();
            if (container == null)
                location = OUTSIDE;
            else if (container instanceof Settlement)
                location = INSETTLEMENT;
            else if (container instanceof Vehicle)
                location = INVEHICLE;
        }

        return location;
    }
    
    /**
     * Gets the person's X location at a settlement.
     * @return X distance (meters) from the settlement's center.
     */
    public double getXLocation() {
        return xLoc;
    }
    
    /**
     * Sets the person's X location at a settlement.
     * @param xLocation the X distance (meters) from the settlement's center.
     */
    public void setXLocation(double xLocation) {
        this.xLoc = xLocation;
    }
    
    /**
     * Gets the person's Y location at a settlement.
     * @return Y distance (meters) from the settlement's center.
     */
    public double getYLocation() {
        return yLoc;
    }
    
    /**
     * Sets the person's Y location at a settlement.
     * @param yLocation
     */
    public void setYLocation(double yLocation) {
        this.yLoc = yLocation;
    }

    /**
     * Get settlement person is at, null if person is not at a settlement
     * 
     * @return the person's settlement
     */
    public Settlement getSettlement() {
        if (INSETTLEMENT.equals(getLocationSituation()))
            return (Settlement) getContainerUnit();
        else
            return null;
    }

    /**
     * Get vehicle person is in, null if person is not in vehicle
     * 
     * @return the person's vehicle
     */
    public Vehicle getVehicle() {
        if (INVEHICLE.equals(getLocationSituation()))
            return (Vehicle) getContainerUnit();
        else
            return null;
    }

    /**
     * Sets the unit's container unit. Overridden from Unit class.
     * 
     * @param containerUnit
     *            the unit to contain this unit.
     */
    public void setContainerUnit(Unit containerUnit) {
        super.setContainerUnit(containerUnit);

        MedicalAid aid = getAccessibleAid();
        if ((aid != null) && (health.canTreatProblems(aid)))
            health.requestAllTreatments(aid);
    }

    /**
     * Bury the Person at the current location. The person is removed from any
     * containing Settlements or Vehicles. The body is fixed at the last
     * location of the containing unit.
     */
    public void buryBody() {
        Unit containerUnit = getContainerUnit();
        if (containerUnit != null) {
            containerUnit.getInventory().retrieveUnit(this);
        }
        isBuried = true;
        setAssociatedSettlement(null);
    }

    /**
     * Person has died. Update the status to reflect the change and remove this
     * Person from any Task and remove the associated Mind.
     */
    void setDead() {
        mind.setInactive();
        buryBody();
    }

    /**
     * Person can take action with time passing
     * 
     * @param time amount of time passing (in millisols).
     */
    public void timePassing(double time) {

        // If Person is dead, then skip
        if (health.getDeathDetails() == null) {

            PersonConfig config = SimulationConfig.instance()
                    .getPersonConfiguration();
            LifeSupport support = getLifeSupport();

            // Pass the time in the physical condition first as this may
            // result in death.
            if (health.timePassing(time, support, config)) {
                // Mental changes with time passing.
                mind.timePassing(time);
            } else {
                // Person has died as a result of physical condition
                setDead();
            }
        }
    }

    /**
     * Returns a reference to the Person's natural attribute manager
     * 
     * @return the person's natural attribute manager
     */
    public NaturalAttributeManager getNaturalAttributeManager() {
        return attributes;
    }

    /**
     * Get the performance factor that effect Person with the complaint.
     * 
     * @return The value is between 0 -> 1.
     */
    public double getPerformanceRating() {
        return health.getPerformanceFactor();
    }

    /**
     * Returns a reference to the Person's physical condition
     * 
     * @return the person's physical condition
     */
    public PhysicalCondition getPhysicalCondition() {
        return health;
    }

    /**
     * Find a medical aid according to the current location.
     * 
     * @return Accessible aid.
     */
    MedicalAid getAccessibleAid() {
        MedicalAid found = null;

        String location = getLocationSituation();
        if (location.equals(INSETTLEMENT)) {
            Settlement settlement = getSettlement();
            List<Building> infirmaries = settlement.getBuildingManager().getBuildings(
                    MedicalCare.NAME);
            if (infirmaries.size() > 0) {
                int rand = RandomUtil.getRandomInt(infirmaries.size() - 1);
                Building foundBuilding = infirmaries.get(rand);
                found = (MedicalAid) foundBuilding.getFunction(MedicalCare.NAME);
            }
        }
        if (location.equals(Person.INVEHICLE)) {
            Vehicle vehicle = getVehicle();
            if (vehicle instanceof Medical) {
                found = ((Medical) vehicle).getSickBay();
            }
        }

        return found;
    }

    /**
     * Returns the person's mind
     * 
     * @return the person's mind
     */
    public Mind getMind() {
        return mind;
    }

    /**
     * Returns the person's age
     * 
     * @return the person's age
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
     * Returns the person's height in cm
     * 
     * @return the person's height
     */
    public int getHeight() {

        return height;
    }
    
    
    /**
     * Returns the person's birth date
     * 
     * @return the person's birth date
     */
    public String getBirthDate() {
        return birthTimeStamp.getDateString();
    }

    /**
     * Get the LifeSupport system supporting this Person. This may be from the
     * Settlement, Vehicle or Equipment.
     * 
     * @return Life support system.
     */
    private LifeSupport getLifeSupport() {

        Collection<Unit> lifeSupportUnits = new ConcurrentLinkedQueue<Unit>();

        // Get all container units.
        Unit container = getContainerUnit();
        while (container != null) {
            if (container instanceof LifeSupport)
                lifeSupportUnits.add(container);
            container = container.getContainerUnit();
        }

        // Get all contained units.
        Iterator<Unit> i = getInventory().getContainedUnits().iterator();
        while (i.hasNext()) {
            Unit contained = i.next();
            if (contained instanceof LifeSupport)
                lifeSupportUnits.add(contained);
        }

        // Get first life support unit that checks out.
        i = lifeSupportUnits.iterator();
        while (i.hasNext()) {
            LifeSupport goodUnit = (LifeSupport) i.next();
            try {
                if (goodUnit.lifeSupportCheck())
                    return goodUnit;
            } catch (Exception e) {
            }
        }

        // If no good units, just get first life support unit.
        i = lifeSupportUnits.iterator();
        if (i.hasNext())
            return (LifeSupport) i.next();

        // If no life support units at all, return null.
        return null;
    }

    /**
     * Person consumes given amount of food
     * 
     * @param amount the amount of food to consume (in kg)
     * @param takeFromInv is food taken from local inventory?
     */
    public void consumeFood(double amount, boolean takeFromInv) {
        if (takeFromInv) {
            health.consumeFood(amount, getContainerUnit());
        }
        else {
            health.consumeFood(amount);
        }
    }

    /**
     * Gets the gender of the person ("male" or "female")
     * 
     * @return the gender
     */
    public String getGender() {
        return gender;
    }
    
    /**
     * Gets the birthplace of the person
     * 
     * @return the gender
     */
    public String getBirthplace() {
        return birthplace;
    }
    /**
     * Gets the person's local group of people (in building or rover)
     * 
     * @return collection of people in person's location.
     */
    public Collection<Person> getLocalGroup() {
        Collection<Person> localGroup = new ConcurrentLinkedQueue<Person>();

        if (getLocationSituation().equals(Person.INSETTLEMENT)) {
            Building building = BuildingManager.getBuilding(this);
            if (building != null) {
                String lifeSupportName = org.mars_sim.msp.core.structure.building.function.LifeSupport.NAME;
                if (building.hasFunction(lifeSupportName)) {
                    org.mars_sim.msp.core.structure.building.function.LifeSupport lifeSupport = 
                            (org.mars_sim.msp.core.structure.building.function.LifeSupport) 
                            building.getFunction(lifeSupportName);
                    localGroup = new ConcurrentLinkedQueue<Person>(lifeSupport.getOccupants());
                }
            }
        } else if (getLocationSituation().equals(Person.INVEHICLE)) {
            Crewable crewableVehicle = (Crewable) getVehicle();
            localGroup = new ConcurrentLinkedQueue<Person>(crewableVehicle.getCrew());
        }

        if (localGroup.contains(this))
            localGroup.remove(this);

        return localGroup;
    }

    /**
     * Checks if the vehicle operator is fit for operating the vehicle.
     * 
     * @return true if vehicle operator is fit.
     */
    public boolean isFitForOperatingVehicle() {
        return !health.hasSeriousMedicalProblems();
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
     * Gets the settlement the person is currently associated with.
     * 
     * @return associated settlement or null if none.
     */
    public Settlement getAssociatedSettlement() {
        return associatedSettlement;
    }

    /**
     * Sets the associated settlement for a person.
     * 
     * @param newSettlement the new associated settlement or null if none.
     */
    public void setAssociatedSettlement(Settlement newSettlement) {
        if (associatedSettlement != newSettlement) {
            Settlement oldSettlement = associatedSettlement;
            associatedSettlement = newSettlement;
            fireUnitUpdate(UnitEventType.ASSOCIATED_SETTLEMENT_EVENT, associatedSettlement);
            if (oldSettlement != null) {
                oldSettlement.fireUnitUpdate(UnitEventType.REMOVE_ASSOCIATED_PERSON_EVENT, this);
            }
            if (newSettlement != null) {
                newSettlement.fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_PERSON_EVENT, this);
            }
        }
    }

    /**
     * Gets the person's achievement credit for a given scientific field.
     * 
     * @param science the scientific field.
     * @return achievement credit.
     */
    public double getScientificAchievement(Science science) {
        double result = 0D;

        if (scientificAchievement.containsKey(science)) {
            result = scientificAchievement.get(science);
        }

        return result;
    }

    /**
     * Gets the person's total scientific achievement credit.
     * 
     * @return achievement credit.
     */
    public double getTotalScientificAchievement() {
        double result = 0D;

        Iterator<Double> i = scientificAchievement.values().iterator();
        while (i.hasNext()) {
            result += i.next();
        }

        return result;
    }

    /**
     * Add achievement credit to the person in a scientific field.
     * 
     * @param achievementCredit the achievement credit.
     * @param science the scientific field.
     */
    public void addScientificAchievement(double achievementCredit, Science science) {
        if (scientificAchievement.containsKey(science)) {
            achievementCredit += scientificAchievement.get(science);
        }

        scientificAchievement.put(science, achievementCredit);
    }

    @Override
    public void destroy() {
        super.destroy();

        attributes.destroy();
        attributes = null;
        mind.destroy();
        mind = null;
        health.destroy();
        health = null;
        gender = null;
        birthTimeStamp = null;
        associatedSettlement = null;
        scientificAchievement.clear();
        scientificAchievement = null;
    }
}