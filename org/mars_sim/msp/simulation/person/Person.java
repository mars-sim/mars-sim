/**
 * Mars Simulation Project
 * Person.java
 * @version 2.73 2001-12-06
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person;

import java.util.*;
import java.io.Serializable;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.SettlementIterator;
import org.mars_sim.msp.simulation.structure.FacilityManager;
import org.mars_sim.msp.simulation.structure.StoreroomFacility;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.simulation.person.ai.*;

/** The Person class represents a person on the virtual Mars. It keeps
 *  track of everything related to that person and provides
 *  information about him/her.
 */
public class Person extends Unit implements Serializable {

    /**
     * Status string used when Person resides in settlement
     */
    public final static String INSETTLEMENT = "In Settlement";

    /**
     * Status string used when Person resides in settlement
     */
    public final static String INVEHICLE = "In Vehicle";

    /**
     * Status string used when Person has been buried
     */
    public final static String BURIED = "Buried";


    // Data members
    private Settlement settlement; // Person's current settlement
    private Vehicle vehicle; // Vehicle person is riding in
    private NaturalAttributeManager attributes; // Manager for Person's natural attributes
    private SkillManager skills; // Manager for Person's skills
    private Mind mind; // Person's mind
    private String locationSituation; // Where person is ("In Settlement", "In Vehicle", "Outside")
    private PhysicalCondition health; // Person's physical

    /** Constructs a Person object at a given settlement
     *  @param name the person's name
     *  @param settlement the settlement the person is at
     *  @param mars the virtual Mars
     */
    Person(String name, Settlement settlement, VirtualMars mars) {
        // Use Unit constructor
        super(name, settlement.getCoordinates(), mars);

        initPersonData(mars);
        setSettlement(settlement);
    }

    /** Constructs a Person object
     *  @param name the person's name
     *  @param mars the virtual Mars
     *  @param manager the unit manager
     *  @throws Exception if no suitable settlement is found
     */
    Person(String name, VirtualMars mars, UnitManager manager) throws Exception {
        // Use Unit constructor
        super(name, new Coordinates(0D, 0D), mars);

        initPersonData(mars);

        Settlement leastPeople = null;
        int least = Integer.MAX_VALUE;
        SettlementIterator i = manager.getSettlements().iterator();
        while (i.hasNext()) {
            Settlement settlement = i.next();
            if (settlement.getAvailablePopulationCapacity() > 0) {
                if (settlement.getPeopleNum() < least) {
                    least = settlement.getPeopleNum();
                    leastPeople = settlement;
                }
            }
        }

        if (leastPeople != null) setSettlement(leastPeople);
        else throw new Exception("No suitable settlements available");

    }

    /** Initialize person data */
    private void initPersonData(VirtualMars mars) {
        // Initialize data members
        vehicle = null;
        attributes = new NaturalAttributeManager();
        skills = new SkillManager(this);
        mind = new Mind(this, mars);
        locationSituation = INSETTLEMENT;
        health = new PhysicalCondition(mars);
    }

    /** Returns a string for the person's relative location "In
     *  Settlement", "In Vehicle" or "Outside"
     *  @return the person's location
     */
    public String getLocationSituation() {
        return locationSituation;
    }

    /** Sets the person's relative location "In Settlement", "In
     *  Vehicle" or "Outside"
     *  @param newLocation the new location
     */
    public void setLocationSituation(String newLocation) {
        locationSituation = newLocation;
    }

    /** Get settlement person is at, null if person is not at
     *  a settlement
     *  @return the person's settlement
     */
    public Settlement getSettlement() {
        return settlement;
    }

    /** Get vehicle person is in, null if person is not in vehicle
     *  @return the person's vehicle
     */
    public Vehicle getVehicle() {
        return vehicle;
    }

    /** Makes the person an inhabitant of a given settlement.
     *  This check whether any illness can be recovered.
     *  @param settlement the person's settlement
     */
    public void setSettlement(Settlement settlement) {
        this.settlement = settlement;
        location.setCoords(settlement.getCoordinates());
        settlement.addPerson(this);
        vehicle = null;

        health.canStartRecovery(settlement);
    }

    /** Makes the person a passenger in a vehicle
     *  @param vehicle the person's vehicle
     */
    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        settlement = null;
    }

    /** Sets the person's fatigue level
     *  @param fatigue new fatigue level
     */
    public void setFatigue(double fatigue) {
        health.setFatigue(fatigue);
    }

    /** Sets the person's hunger level
     *  @param hunger new hunger level
     */
    public void setHunger(double hunger) {
        health.setHunger(hunger);
    }

    /**
     * Bury the Person at the current location. The person is removed from
     * any containing Settlements or Vehicles. The body is fixed at the last
     * of the containing unit.
     */
    public void buryBody() {
        if (vehicle != null) {
            vehicle.removePassenger(this);
            setCoordinates(vehicle.getCoordinates());
            vehicle = null;
        }
        else if (settlement != null) {
            settlement.personLeave(this);
            setCoordinates(settlement.getCoordinates());
            settlement = null;
        }
        locationSituation = BURIED;
    }

    /**
     * Person has died. Update the status to reflect the change and remove
     * this Person from any Task and remove the associated Mind.
     */
    void setDead() {

        mind.setInactive();
        System.out.println(name + " is dead");
    }

    /** Person can take action with time passing
     *  @param time amount of time passing (in millisols)
     */
    public void timePassing(double time) {

        // If Person is dead, then skip
        if (health.getAlive()) {
            SimulationProperties props = mars.getSimulationProperties();
            LifeSupport support = getLifeSupport();

            // Pass the time in the physical condition first as this may kill
            // Person
            if (health.timePassing(time, support, props)) {
                // Mins action is descreased according to any illness
                mind.takeAction(time * getPerformanceRating());
            }
            else {
                // Person has died as a result of physical condition
                setDead();
            }
        }
    }

    /** Returns a reference to the Person's natural attribute manager
     *  @return the person's natural attribute manager
     */
    public NaturalAttributeManager getNaturalAttributeManager() {
        return attributes;
    }

    /**
     * Get the performance rating of this Person
     *
     * @return A value in the range of 0 to 1.
     */
    public double getPerformanceRating() {
        double rating = 1D;
        MedicalComplaint illness = health.getIllness();
        if (illness != null) {
            rating = illness.getPerformanceFactor();
        }
        return rating;
    }

    /** Returns a reference to the Person's physical condition
     *  @return the person's physical condition
     */
    public PhysicalCondition getPhysicalCondition() {
        return health;
    }

    /** Returns a reference to the Person's skill manager
     *  @return the person's skill manager
     */
    public SkillManager getSkillManager() {
        return skills;
    }

    /** Returns the person's mind
     *  @return the person's mind
     */
    public Mind getMind() {
        return mind;
    }


    /**
     * Get the LifeSupport system supporting this Person. This may be from
     * the Settlement, Vehicle or Equipment.
     *
     * @return Life support system.
     */
    private LifeSupport getLifeSupport() {
        LifeSupport support = null;
        if (locationSituation.equals(INSETTLEMENT)) {
            support = settlement.getLifeSupport();
        }
        else support = vehicle;

        return support;
    }

    /** Person consumes given amount of food
     *  @param amount amount of food to consume (in kg)
     */
    public void consumeFood(double amount) {
        health.consumeFood(amount, getLifeSupport(),
                           mars.getSimulationProperties());
    }
}
