/**
 * Mars Simulation Project
 * Vehicle.java
 * @version 2.74 2002-05-09
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.vehicle;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.medical.MedicalAid;
import org.mars_sim.msp.simulation.person.ai.*;
import org.mars_sim.msp.simulation.malfunction.*;
import java.io.Serializable;
import java.util.*;

/** The Vehicle class represents a generic vehicle. It keeps track of
 *  generic information about the vehicle. This class needs to be
 *  subclassed to represent a specific type of vehicle.
 */
public abstract class Vehicle extends Unit implements Serializable, Malfunctionable {

    // Vehicle Status Strings
    public final static String PARKED = "Parked";
    public final static String MOVING = "Moving";
    public final static String MALFUNCTION = "Malfunction";
    public final static String MAINTENANCE = "Periodic Maintenance";

    // Data members
    protected MalfunctionManager malfunctionManager; // The malfunction manager for the vehicle.
    private Direction direction; // Direction vehicle is traveling in
    private double speed = 0; // Current speed of vehicle in kph
    private double baseSpeed = 0; // Base speed of vehicle in kph (can be set in child class)
    private Person driver; // Driver of the vehicle
    private double distanceTraveled = 0; // Total distance traveled by vehicle (km)
    private double distanceMaint = 0; // Distance traveled by vehicle since last maintenance (km)
    protected double range; // Maximum range of vehicle. (km)
    private Coordinates destinationCoords; // Coordinates of the destination
    private Settlement destinationSettlement; // Destination settlement (it applicable)
    private String destinationType; // Type of destination ("None", "Settlement" or "Coordinates")
    private double distanceToDestination = 0; // Distance to the destination (km)
    private boolean isReserved = false; // True if vehicle is currently reserved for a driver and cannot be taken by another
    private boolean distanceMark = false; // True if vehicle is due for maintenance.
    private MarsClock estimatedTimeOfArrival; // Estimated time of arrival to destination.

    /** Constructs a Vehicle object with a given settlement
     *  @param name the vehicle's name
     *  @param settlement the settlement the vehicle is parked at
     *  @param mars the virtual Mars
     */
    Vehicle(String name, Settlement settlement, Mars mars) {
        // use Unit constructor
        super(name, settlement.getCoordinates(), mars);

        settlement.getInventory().addUnit(this);
        initVehicleData();
    }

    /** Constructs a Vehicle object
     *  @param name the vehicle's name
     *  @param mars the virtual Mars
     *  @param manager the unit manager
     *  @throws Exception when there are no available settlements
     */
    Vehicle(String name, Mars mars, UnitManager manager) throws Exception {
        // use Unit constructor
        super(name, new Coordinates(0D, 0D), mars);

        if (manager.getSettlementNum() == 0) throw new Exception("No available settlements");

        Settlement leastVehicles = null;
        int least = Integer.MAX_VALUE;
        SettlementIterator i = manager.getSettlements().iterator();
        while (i.hasNext()) {
            Settlement settlement = i.next();
            if (settlement.getParkedVehicleNum() < least) {
                least = settlement.getParkedVehicleNum();
                leastVehicles = settlement;
            }
        }
	leastVehicles.getInventory().addUnit(this);

        initVehicleData();
    }

    /** Initializes vehicle data */
    private void initVehicleData() {

	// Initialize malfunction manager.
	malfunctionManager = new MalfunctionManager(this, mars);
	malfunctionManager.addScopeString("Vehicle");
	    
        setDestinationType("None");
        direction = new Direction(0);
    }

    /** Returns vehicle's current status
     *  @return the vehicle's current status
     */
    public String getStatus() {
        String status = null;

        if (containerUnit != null) {
	    if (containerUnit instanceof Settlement) {
	        Settlement settlement = (Settlement) containerUnit;
		FacilityManager facilityManager = settlement.getFacilityManager();
		MaintenanceGarage garage = (MaintenanceGarage) facilityManager.getFacility("Maintenance Garage");
		if (garage.vehicleInGarage(this)) status = MAINTENANCE;
		else status = PARKED;
	    }
	    else status = PARKED;
	}
	else {
	    if (malfunctionManager.hasMalfunction()) {
	        status = MALFUNCTION;
	    }
	    else {
	        if (speed == 0D) status = PARKED;
	        else status = MOVING;
	    }
	}

        return status;
    }

    /** Returns true if vehicle is reserved by someone
     *  @return true if vehicle is currently reserved by someone
     */
    public boolean isReserved() {
        return isReserved;
    }

    /** Reserves a vehicle or cancels a reservation
     *  @param status the vehicle's reserved status
     */
    public void setReserved(boolean status) {
        // System.out.println(getName() + " reserved: " + status);
        isReserved = status;
    }

    /** Returns speed of vehicle
     *  @return the vehicle's speed (in km/hr)
     */
    public double getSpeed() {
        return speed;
    }

    /** Sets the vehicle's current speed
     *  @param speed the vehicle's speed (in km/hr)
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /** Returns base speed of vehicle
     *  @return the vehicle's base speed (in km/hr)
     */
    public double getBaseSpeed() {
        return baseSpeed;
    }

    /** Sets the base speed of vehicle
     * @param speed the vehicle's base speed (in km/hr)
     */
    public void setBaseSpeed(double speed) {
        baseSpeed = speed;
    }

    /** Gets the range of the vehicle
     *  @return the range of the vehicle (in km)
     */
    public double getRange() {
        return range;
    }

    /** Returns total distance traveled by vehicle (in km.)
     *  @return the total distanced traveled by the vehicle (in km)
     */
    public double getTotalDistanceTraveled() {
        return distanceTraveled;
    }

    /** Adds a distance (in km.) to the vehicle's total distance traveled
     *  @param distance distance to add to total distance traveled (in km)
     */
    public void addTotalDistanceTraveled(double distance) {
        distanceTraveled += distance;
    }

    /** Returns distance traveled by vehicle since last maintenance (in km.)
     *  @return distance traveled by vehicle since last maintenance (in km)
     */
    public double getDistanceLastMaintenance() {
        return distanceMaint;
    }

    /** Adds a distance (in km.) to the vehicle's distance since last maintenance.
     *  Set distanceMark to true if vehicle is due for maintenance.
     *  @param distance distance to add (in km)
     */
    public void addDistanceLastMaintenance(double distance) {
        distanceMaint += distance;
        if ((distanceMaint > 5000D) && !distanceMark) {
            distanceMark = true;
        }
    }

    /** Sets vehicle's distance since last maintenance to zero */
    public void clearDistanceLastMaintenance() {
        distanceMaint = 0;
    }

    /** Returns direction of vehicle (0 = north, clockwise in radians)
     *  @return the direction the vehicle is traveling (in radians)
     */
    public Direction getDirection() {
        return (Direction) direction.clone();
    }

    /** Sets the vehicle's facing direction (0 = north, clockwise in radians)
     *  @param direction the direction the vehicle is travleling (in radians)
     */
    public void setDirection(Direction direction) {
        this.direction.setDirection(direction.getDirection());
    }

    /** Returns driver of the vehicle
     *  @return the driver
     */
    public Person getDriver() {
	if (!inventory.containsUnit(driver)) driver = null;
        else if (!driver.getPhysicalCondition().getAlive()) driver = null;
	return driver;
    }

    /** Sets the driver of the vehicle
     *  @param driver the driver
     */
    public void setDriver(Person driver) {
	if (!inventory.containsUnit(driver)) this.driver = null;
	else this.driver = driver;
    }

    /** Returns the current settlement vehicle is parked at.
     *  Returns null if vehicle is not currently parked at a settlement.
     *  @return the settlement the vehicle is parked at
     */
    public Settlement getSettlement() {

	Unit topUnit = getTopContainerUnit();

	if ((topUnit != null) && (topUnit instanceof Settlement)) {
	    return (Settlement) topUnit;
	}
	else return null;
    }

    /** Returns distance to destination in kilometers
     *  Returns 0 if vehicle is not currently moving toward a destination
     *  @return the distance to the vehicle's destination
     */
    public double getDistanceToDestination() {
        return distanceToDestination;
    }

    /** Sets the vehicle's distance to its destination
     *  @param distanceToDestination the distance to the vehicle's destination
     */
    public void setDistanceToDestination(double distanceToDestination) {
        this.distanceToDestination = distanceToDestination;
    }

    /** Gets the type of destination for the vehicle
     *  @return the vehicle's destination type
     */
    public String getDestinationType() {
        return destinationType;
    }

    /** Sets the type of destination for the vehicle ("Coordinates", "Settlement" or "None")
     *  @param the vehicle's destination type
     */
    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    /** Sets the destination coordinates
     *  @param destinationCoords the vehicle's destination location
     */
    public void setDestination(Coordinates destinationCoords) {
        this.destinationCoords = destinationCoords;
        if (destinationType == null) destinationType = "Coordinates";
    }

    /** Returns the destination coordinates.
     *  (null if no destination).
     *  @return the vehicle's destination location
     */
    public Coordinates getDestination() {
        return destinationCoords;
    }

    /** Sets the destination settlement
     *  @param destinationSettlement the vehicle's destination settlement
     */
    public void setDestinationSettlement(Settlement destinationSettlement) {
        this.destinationSettlement = destinationSettlement;
        if (destinationSettlement != null) {
            setDestination(destinationSettlement.getCoordinates());
            destinationType = "Settlement";
        }
    }

    /** Returns the destination settlement.
     *  (null if no destination settlement).
     *  @return the vehicle's destination settlement
     */
    public Settlement getDestinationSettlement() {
        return destinationSettlement;
    }

    /** Returns the ETA (Estimated Time of Arrival)
     *  @return ETA as string ("13-Adir-05  056.349")
     */
    public String getETA() {
        if (estimatedTimeOfArrival != null)
            return estimatedTimeOfArrival.getTimeStamp();
        else return "";
    }

    /** Sets the ETA (Estimated Time of Arrival) of the vehicle.
     *  @param newETA new ETA of the vehicle
     */
    public void setETA(MarsClock newETA) {
        this.estimatedTimeOfArrival = newETA;
    }

    /**
     * Returns a MedicalAid that is available in this Vehicle. This implementation
     * return null sinc eby default Vehicles do not carry medical facilities.
     * This should be overridden by subclasses that support medical facilities.
     * @return Location medical facility.
     */
    public MedicalAid getMedicalFacility() {
        return null;
    }

    /**
     * Returns a string describing the vehicle.
     * @return string describing vehicle
     */
    public abstract String getDescription();

    /**
     * Gets the unit's malfunction manager.
     * @return malfunction manager
     */
    public MalfunctionManager getMalfunctionManager() {
        return malfunctionManager;
    }

    /**
     * Time passing for vehicle.
     * @param time the amount of time passing (millisols)
     */
    public void timePassing(double time) {
        if (getStatus().equals(MOVING)) malfunctionManager.activeTimePassing(time);
	malfunctionManager.timePassing(time);
    }

    /**
     * Gets a collection of people affected by this entity.
     * @return person collection
     */
    public PersonCollection getAffectedPeople() {
        PersonCollection people = new PersonCollection();

        // Check all people.
        PersonIterator i = mars.getUnitManager().getPeople().iterator();
        while (i.hasNext()) {
            Person person = i.next();
            Task task = person.getMind().getTaskManager().getTask();

            // Add all people maintaining this vehicle.
            if (task instanceof Maintenance) {
                if (((Maintenance) task).getEntity() == this) {
                    if (!people.contains(person)) people.add(person);
		}
            }

            // Add all people repairing this vehicle.
            if (task instanceof Repair) {
                if (((Repair) task).getEntity() == this) {
                    if (!people.contains(person)) people.add(person);
		}
            }
        }

        return people;
    }
}
