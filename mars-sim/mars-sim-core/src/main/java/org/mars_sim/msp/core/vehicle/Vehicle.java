/**
 * Mars Simulation Project
 * Vehicle.java
 * @version 3.04 2012-12-04
 * @author Scott Davis
 */

package org.mars_sim.msp.core.vehicle;

import org.mars_sim.msp.core.*;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.manufacture.Salvagable;
import org.mars_sim.msp.core.manufacture.SalvageInfo;
import org.mars_sim.msp.core.manufacture.SalvageProcessInfo;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/** The Vehicle class represents a generic vehicle. It keeps track of
 *  generic information about the vehicle. This class needs to be
 *  subclassed to represent a specific type of vehicle.
 */
public abstract class Vehicle extends Unit implements Serializable, 
        Malfunctionable, Salvagable {

    private static Logger logger = Logger.getLogger(Vehicle.class.getName());
    
	// Unit Event Types
	public final static String STATUS_EVENT = "vehicle status";
	public final static String SPEED_EVENT = "vehicle speed";
	public final static String OPERATOR_EVENT = "vehicle operator";
	public final static String EMERGENCY_BEACON_EVENT = "vehicle emergency beacon event";
	public final static String RESERVED_EVENT = "vehicle reserved event";
	
    // Vehicle Status Strings
    public final static String PARKED = "Parked";
    public final static String MOVING = "Moving";
    public final static String MALFUNCTION = "Malfunction";
    public final static String MAINTENANCE = "Periodic Maintenance";
    public final static String TOWED = "Towed";
    
    // The error margin for determining vehicle range. (actual distance / safe distance)
    public final static double RANGE_ERROR_MARGIN = 1.7D;

    // Maintenance info
    private static final double WEAR_LIFETIME = 668000D; // 668 Sols (1 orbit)
    private static final double MAINTENANCE_TIME = 500D; // 1 Sol
    
    // Data members
    protected MalfunctionManager malfunctionManager; // The malfunction manager for the vehicle.
    private Direction direction; // Direction vehicle is traveling in
    private double speed = 0; // Current speed of vehicle in kph
    private double baseSpeed = 0; // Base speed of vehicle in kph (can be set in child class)
    private VehicleOperator vehicleOperator; // The operator of the vehicle
    private double distanceTraveled = 0; // Total distance traveled by vehicle (km)
    private double distanceMaint = 0; // Distance traveled by vehicle since last maintenance (km)
    private double fuelEfficiency; // The fuel efficiency of the vehicle. (km/kg)
    private boolean isReservedMission = false; // True if vehicle is currently reserved for a mission and cannot be taken by another
    private boolean distanceMark = false; // True if vehicle is due for maintenance.
    private ArrayList<Coordinates> trail; // A collection of locations that make up the vehicle's trail.
    private boolean reservedForMaintenance = false; // True if vehicle is currently reserved for periodic maintenance.
    private boolean emergencyBeacon = false; // The emergency beacon for the vehicle.  True if beacon is turned on.
    private Vehicle towingVehicle; // The vehicle that is currently towing this vehicle.
    private String status; // The vehicle's status.
    private boolean isSalvaged; // True if vehicle is salvaged.
    private SalvageInfo salvageInfo; // The vehicle's salvage info.
    private double width; // Width of vehicle (meters).
    private double length; // Length of vehicle (meters).
    private double xLocParked; // Parked X location (meters) from center of settlement.
    private double yLocParked; // Parked Y location (meters) from center of settlement.
    private double facingParked; // Parked facing (degrees clockwise from North).

    /**
     * Constructor to be used for testing.
     * @param name the vehicle's name
     * @param description the configuration description of the vehicle.
     * @param settlement the settlement the vehicle is parked at.
     * @param baseSpeed the base speed of the vehicle (kph)
     * @param baseMass the base mass of the vehicle (kg)
     * @param fuelEfficiency the fuel efficiency of the vehicle (km/kg)
     * @throws Exception if error constructing vehicle
     */
    protected Vehicle(String name, String description, Settlement settlement, 
    		double baseSpeed, double baseMass, double fuelEfficiency) {
    	
    	// Use Unit constructor
        super(name, settlement.getCoordinates());
        
        settlement.getInventory().storeUnit(this);

        // Initialize vehicle data
        setDescription(description);
        direction = new Direction(0);
	    trail = new ArrayList<Coordinates>();
	    setBaseSpeed(baseSpeed);
	    setBaseMass(baseMass);
	    this.fuelEfficiency = fuelEfficiency;
	    status = PARKED;
	    isSalvaged = false;
	    salvageInfo = null;
	    width = 0D;
	    length = 0D;
	    xLocParked = 0D;
	    yLocParked = 0D;
	    facingParked = 0D;
	    
	    // Initialize malfunction manager.
	    malfunctionManager = new MalfunctionManager(this, WEAR_LIFETIME, MAINTENANCE_TIME);
	    malfunctionManager.addScopeString("Vehicle");
    }
    
    /** 
     * Constructs a Vehicle object with a given settlement
     * @param name the vehicle's name
     * @param description the configuration description of the vehicle.
     * @param settlement the settlement the vehicle is parked at.
     * @throws an exception if vehicle could not be constructed.
     */
    Vehicle(String name, String description, Settlement settlement) {
	    
    	// Use Unit constructor
        super(name, settlement.getCoordinates());
        
        settlement.getInventory().storeUnit(this);

        // Initialize vehicle data
        setDescription(description);
        direction = new Direction(0);
	    trail = new ArrayList<Coordinates>();
	    status = PARKED;
	    
	    // Initialize malfunction manager.
	    malfunctionManager = new MalfunctionManager(this, WEAR_LIFETIME, MAINTENANCE_TIME);
	    malfunctionManager.addScopeString("Vehicle");
    	
	    // Get vehicle configuration.
	    VehicleConfig config = SimulationConfig.instance().getVehicleConfiguration();
		
	    // Set width and length of vehicle.
	    width = config.getWidth(description);
	    length = config.getLength(description);
	    
	    // Set base speed.
	    setBaseSpeed(config.getBaseSpeed(description));

	    // Set the empty mass of the rover.
	    setBaseMass(config.getEmptyMass(description));
	    
	    // Set the fuel efficiency of the rover.
	    fuelEfficiency = config.getFuelEfficiency(getDescription());
	    
	    // Set initial parked location and facing at settlement.
	    determinedSettlementParkedLocationAndFacing();
    }
    
    /**
     * Gets the vehicle's width.
     * @return width (meters).
     */
    public double getWidth() {
        return width;
    }
    
    /**
     * Gets the vehicle's length.
     * @return length (meters).
     */
    public double getLength() {
        return length;
    }
    
    /**
     * Gets the x location of the vehicle when parked at a settlement.
     * @return x location (meters from settlement center - West: positive, East: negative).
     */
    public double getXLocation() {
        return xLocParked;
    }
    
    /**
     * Sets the x location of the vehicle when parked at a settlement.
     * @param xLocation the x location (meters from settlement center - West: positive, East: negative).
     */
    public void setXLocation(double xLocation) {
    	xLocParked = xLocation;
    }
    
    /**
     * Gets the y location of the vehicle when parked at a settlement.
     * @return y location (meters from settlement center - North: positive, South: negative).
     */
    public double getYLocation() {
        return yLocParked;
    }
    
    /**
     * Sets the y location of the vehicle when parked at a settlement.
     * @param yLocation the y location (meters from settlement center - North: positive, South: negative).
     */
    public void setYLocation(double yLocation) {
    	yLocParked = yLocation;
    }
    
    /**
     * Gets the facing of the vehicle when parked at a settlement.
     * @return facing (degrees from North clockwise).
     */
    public double getFacing() {
        return facingParked;
    }
    
    /**
     * Sets the facing of the vehicle when parked at a settlement.
     * @param facing (degrees from North clockwise).
     */
    public void setFacing(double facing) {
    	facingParked = facing;
    }
    
    /** Returns vehicle's current status
     *  @return the vehicle's current status
     */
    public String getStatus() {

    	// Update status string if necessary.
        updateStatus();

        return status;
    }
    
    /**
     * Updates the vehicle's status.
     */
    private void updateStatus() {
    	
    	// Update status based on current situation.
    	String newStatus = PARKED;
    	if (reservedForMaintenance) newStatus = MAINTENANCE;
    	else if (towingVehicle != null) newStatus = TOWED;
		else if (malfunctionManager.hasMalfunction()) newStatus = MALFUNCTION;
        else if (speed > 0D) newStatus = MOVING;
    	
    	if (!status.equals(newStatus)) {
    		status = newStatus;
    		fireUnitUpdate(STATUS_EVENT, newStatus);
    	}
    }

    /** 
     * Checks if the vehicle is reserved for any reason.
     * @return true if vehicle is currently reserved
     */
    public boolean isReserved() {
        return isReservedMission || reservedForMaintenance;
    }

    /**
     * Checks if the vehicle is reserved for a mission.
     * @return true if vehicle is reserved for a mission.
     */
    public boolean isReservedForMission() {
    	return isReservedMission;
    }
    
    /** 
     * Sets if the vehicle is reserved for a mission or not.
     * @param reserved the vehicle's reserved for mission status
     */
    public void setReservedForMission(boolean reserved) {
    	if (isReservedMission != reserved) {
    		isReservedMission = reserved;
    		fireUnitUpdate(RESERVED_EVENT);
    	}
    }
    
    /**
     * Checks if the vehicle is reserved for maintenance.
     * @return true if reserved for maintenance.
     */
    public boolean isReservedForMaintenance() {
        return reservedForMaintenance;
    }
    
    /**
     * Sets if the vehicle is reserved for maintenance or not.
     * @param reserved true if reserved for maintenance
     */
    public void setReservedForMaintenance(boolean reserved) {
    	if (reservedForMaintenance != reserved) {
    		reservedForMaintenance = reserved;
    		fireUnitUpdate(RESERVED_EVENT);
    	}
    }
    
    /**
     * Sets the vehicle that is currently towing this vehicle.
     * @param towingVehicle the vehicle
     */
    public void setTowingVehicle(Vehicle towingVehicle) {
    	if (this == towingVehicle) throw new IllegalArgumentException("Vehicle cannot tow itself.");
    	this.towingVehicle = towingVehicle;
    }
    
    /**
     * Gets the vehicle that is currently towing this vehicle.
     * @return towing vehicle
     */
    public Vehicle getTowingVehicle() {
    	return towingVehicle;
    }

    /** 
     * Gets the speed of vehicle
     * @return the vehicle's speed (in km/hr)
     */
    public double getSpeed() {
        return speed;
    }

    /** 
     * Sets the vehicle's current speed
     * @param speed the vehicle's speed (in km/hr)
     */
    public void setSpeed(double speed) {
    	if (speed < 0D) throw new IllegalArgumentException("Vehicle speed cannot be less than 0 km/hr: " + speed);
        this.speed = speed;
        fireUnitUpdate(SPEED_EVENT);
    }

    /** 
     * Gets the base speed of vehicle
     * @return the vehicle's base speed (in km/hr)
     */
    public double getBaseSpeed() {
        return baseSpeed;
    }

    /** 
     * Sets the base speed of vehicle
     * @param speed the vehicle's base speed (in km/hr)
     */
    public void setBaseSpeed(double speed) {
    	if (speed < 0D) throw new IllegalArgumentException("Vehicle base speed cannot be less than 0 km/hr");
        baseSpeed = speed;
    }

    /** 
     * Gets the range of the vehicle
     * @return the range of the vehicle (in km)
     * @throws Exception if error getting range.
     */
    public double getRange() {
    	double fuelCapacity = getInventory().getAmountResourceCapacity(getFuelType(), false);
        return fuelCapacity * fuelEfficiency / RANGE_ERROR_MARGIN;
    }

    /**
     * Gets the fuel efficiency of the vehicle.
     * @return fuel efficiency (km/kg)
     */
    public double getFuelEfficiency() {
    	return fuelEfficiency;
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
        if ((distanceMaint > 5000D) && !distanceMark) distanceMark = true;
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

    /** 
     * Gets the operator of the vehicle (person or AI)
     * @return the vehicle operator
     */
    public VehicleOperator getOperator() {
	    return vehicleOperator;
    }

    /** 
     * Sets the operator of the vehicle
     * @param vehicleOperator the vehicle operator
     */
    public void setOperator(VehicleOperator vehicleOperator) {
	    this.vehicleOperator = vehicleOperator;
	    fireUnitUpdate(OPERATOR_EVENT, vehicleOperator);
    }
    
    /**
     * Checks if a particular operator is appropriate for a vehicle.
     * @param operator the operator to check
     * @return true if appropriate operator for this vehicle.
     */
    public abstract boolean isAppropriateOperator(VehicleOperator operator);

    /** Returns the current settlement vehicle is parked at.
     *  Returns null if vehicle is not currently parked at a settlement.
     *  @return the settlement the vehicle is parked at
     */
    public Settlement getSettlement() {

	    Unit topUnit = getTopContainerUnit();

	    if ((topUnit != null) && (topUnit instanceof Settlement)) return (Settlement) topUnit;
	    else return null;
    }
    
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
     * @throws Exception if error during time.
     */
    public void timePassing(double time) {
//    	try  {
    		// Update status if necessary.
    		updateStatus();
    		
        	if (getStatus().equals(MOVING)) malfunctionManager.activeTimePassing(time);
	    	malfunctionManager.timePassing(time);
	    	addToTrail(getCoordinates());
        
        	// Make sure reservedForMaintenance is false if vehicle needs no maintenance.
        	if (getStatus().equals(MAINTENANCE)) {
            	if (malfunctionManager.getEffectiveTimeSinceLastMaintenance() <= 0D) setReservedForMaintenance(false);
        	}
        	
        	if (isReservedMission) {
        	    // Set reserved for mission to false if the vehicle is not associated with a mission.
        	    if (Simulation.instance().getMissionManager().getMissionForVehicle(this) == null) {
        	        logger.log(Level.SEVERE, getName() + " is mission reserved but has no mission.");
        	        setReservedForMission(false);
        	    }
        	}
        	else {
        	    if (Simulation.instance().getMissionManager().getMissionForVehicle(this) != null) {
        	        logger.log(Level.SEVERE, getName() + " is not mission reserved but is on a mission.");
                }
        	}
        	
        	// If operator is dead, remove operator and stop vehicle.
        	VehicleOperator operator = vehicleOperator;
        	if ((operator != null) && (operator instanceof Person)) {
        		Person personOperator = (Person) operator;
        		if (personOperator.getPhysicalCondition().isDead()) {
        			setOperator(null);
        			setSpeed(0);
        		}
        	}
//    	}
//    	catch (Exception e) {
//    		throw new Exception("Vehicle " + getName() + " timePassing(): " + e.getMessage());
//    	}
    }

    /**
     * Gets a collection of people affected by this entity.
     * @return person collection
     */
    public Collection<Person> getAffectedPeople() {
        Collection<Person> people = new ConcurrentLinkedQueue<Person>();

        // Check all people.
        Iterator<Person> i = Simulation.instance().getUnitManager().getPeople().iterator();
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

    /**
     * Gets the vehicle's trail as a collection of coordinate locations.
     * @return trail collection
     */
    public Collection<Coordinates> getTrail() {
        return trail;
    }

    /**
     * Adds a location to the vehicle's trail if appropriate.
     * @param location location to be added to trail
     */
    public void addToTrail(Coordinates location) {
	    
        if (getSettlement() != null) {
            if (trail.size() > 0) trail.clear();
	    }
	    else if (trail.size() > 0) {
	        Coordinates lastLocation = trail.get(trail.size() - 1);
	        if (!lastLocation.equals(location) && (lastLocation.getDistance(location) >= 2D)) 
	            trail.add(new Coordinates(location));
	    }
	    else trail.add(new Coordinates(location));
    }
    
    /**
     * Gets the resource type that this vehicle uses for fuel.
     * @return resource type
     */
    public abstract AmountResource getFuelType();
    
    /**
     * Gets the estimated distance traveled in one sol.
     * @return distance traveled (km)
     */
    public double getEstimatedTravelDistancePerSol() {
    	// Get estimated average speed (km / hr).
    	double estSpeed = baseSpeed / 2D;
    	
    	// Return estimated average speed in km / sol.
    	return estSpeed / 60D / 60D / MarsClock.convertSecondsToMillisols(1D) * 1000D;
    }
    
    /**
     * Checks if the vehicle's emergency beacon is turned on.
     * @return true if beacon is on.
     */
    public boolean isEmergencyBeacon() {
    	return emergencyBeacon;
    }
    
    /**
     * Sets the vehicle's emergency beacon on or off. 
     * @param isOn true if beacon is on.
     */
    public void setEmergencyBeacon(boolean isOn) {
    	if (emergencyBeacon != isOn) {
    		emergencyBeacon = isOn;
    		fireUnitUpdate(EMERGENCY_BEACON_EVENT);
    	}
    }
    
    /**
     * Checks if the item is salvaged.
     * @return true if salvaged.
     */
    public boolean isSalvaged() {
        return isSalvaged;
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
     * Set initial parked location and facing at settlement.
     */
    public abstract void determinedSettlementParkedLocationAndFacing();
    
    @Override
    public void destroy() {
        super.destroy();
        
        malfunctionManager.destroy();
        malfunctionManager = null;
        direction = null;
        vehicleOperator = null;
        trail.clear();
        trail = null;
        towingVehicle = null;
        status = null;
        if (salvageInfo != null) salvageInfo.destroy();
        salvageInfo = null;
    }
}