/**
 * Mars Simulation Project
 * LoadVehicle.java
 * @version 2.81 2007-08-12
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;

import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.InventoryException;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.UnitIterator;
import org.mars_sim.msp.simulation.equipment.Equipment;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.MissionManager;
import org.mars_sim.msp.simulation.person.ai.mission.VehicleMission;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.ItemResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.simulation.vehicle.VehicleIterator;

/** 
 * The UnloadVehicle class is a task for unloading a fuel and supplies from a vehicle.
 */
public class UnloadVehicle extends Task implements Serializable {
	
	// Task phase
	private static final String UNLOADING = "Unloading";

    // The amount of resources (kg) one person of average strength can unload per millisol.
    private static double UNLOAD_RATE = 20D;
	private static final double STRESS_MODIFIER = .1D; // The stress modified per millisol.
	private static final double DURATION = 100D; // The duration of the task (millisols).

    // Data members
    private Vehicle vehicle;  // The vehicle that needs to be unloaded.
    private Settlement settlement; // The settlement the person is unloading to.

    /**
     * Constructor
     * @param person the person to perform the task.
     * @throws Exception if error constructing task.
     */
    public UnloadVehicle(Person person) throws Exception {
    	// Use Task constructor.
    	super("Unloading vehicle", person, true, false, STRESS_MODIFIER, true, DURATION);
    	
    	settlement = person.getSettlement();
    	
    	VehicleMission mission = getMissionNeedingUnloading();
    	if (mission != null) vehicle = mission.getVehicle();
    	else vehicle = getNonMissionVehicleNeedingUnloading(settlement);
    	
    	if (vehicle != null) {
    		setDescription("Unloading " + vehicle.getName());
    		
    		// Initialize task phase
            addPhase(UNLOADING);
            setPhase(UNLOADING);
    	}
    	else endTask();
    }
    
    /** 
     * Constructor
     * @param person the person to perform the task
     * @param vehicle the vehicle to be unloaded
     * @throws Exception if error constructing task.
     */
    public UnloadVehicle(Person person, Vehicle vehicle) throws Exception {
    	// Use Task constructor.
        super("Unloading vehicle", person, true, false, STRESS_MODIFIER, true, DURATION);

	    setDescription("Unloading " + vehicle.getName());
        this.vehicle = vehicle;

        settlement = person.getSettlement();
        
        // Initialize phase
        addPhase(UNLOADING);
        setPhase(UNLOADING);

        // System.out.println(person.getName() + " is unloading " + vehicle.getName());
    }
    
    /** 
     * Returns the weighted probability that a person might perform this task.
     * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
        	
        	// Check all vehicle missions occuring at the settlement.
        	try {
        		int numVehicles = 0;
        		numVehicles += getAllMissionsNeedingUnloading(person.getSettlement()).size();
        		if (getNonMissionVehicleNeedingUnloading(person.getSettlement()) != null) numVehicles++;
        		result = 500D * numVehicles;
        	}
        	catch (Exception e) {
        		System.err.println("Error finding unloading missions. " + e.getMessage());
        		e.printStackTrace(System.err);
        	}
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
		// Job modifier.
        Job job = person.getMind().getJob();
		if (job != null) result *= job.getStartTaskProbabilityModifier(LoadVehicle.class);        
	
        return result;
    }
    
    /**
     * Gets a vehicle that needs unloading that isn't reserved for a mission.
     * @param settlement the settlement the vehicle is at.
     * @return vehicle or null if none.
     */
    private static Vehicle getNonMissionVehicleNeedingUnloading(Settlement settlement) {
    	Vehicle result = null;
    	
    	if (settlement != null) {
    		VehicleIterator i = settlement.getParkedVehicles().iterator();
    		while (i.hasNext()) {
    			Vehicle vehicle = i.next();
    			try {
    				if (!vehicle.isReserved() && (vehicle.getInventory().getTotalInventoryMass() > 0D)) result = vehicle;
    			}
    			catch(InventoryException e) {
    				e.printStackTrace(System.err);
    			}
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Gets a list of all disembarking vehicle missions at a settlement.
     * @param settlement the settlement.
     * @return list of vehicle missions.
     * @throws Exception if error finding missions.
     */
    private static List<Mission> getAllMissionsNeedingUnloading(Settlement settlement) throws Exception {
    	
    	List<Mission> result = new ArrayList<Mission>();
    	
    	MissionManager manager = Simulation.instance().getMissionManager();
    	Iterator i = manager.getMissions().iterator();
    	while (i.hasNext()) {
    		Mission mission = (Mission) i.next();
    		if (mission instanceof VehicleMission) {
    			if (VehicleMission.DISEMBARKING.equals(mission.getPhase())) {
    				VehicleMission vehicleMission = (VehicleMission) mission;
    				if (vehicleMission.hasVehicle()) {
    					Vehicle vehicle = vehicleMission.getVehicle();
    					if (settlement == vehicle.getSettlement()) {
    						if (!isFullyUnloaded(vehicle)) result.add(vehicleMission);
    					}
    				}
    			}
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Gets a random vehicle mission unloading at the settlement.
     * @return vehicle mission.
     * @throws Exception if error finding vehicle mission.
     */
    private VehicleMission getMissionNeedingUnloading() throws Exception {
    	
    	VehicleMission result = null;
    	
    	List unloadingMissions = getAllMissionsNeedingUnloading(person.getSettlement());
    	
    	if (unloadingMissions.size() > 0) {
    		int index = RandomUtil.getRandomInt(unloadingMissions.size() - 1);
    		result = (VehicleMission) unloadingMissions.get(index);
    	}
    	
    	return result;
    }
    
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisol) the phase is to be performed.
     * @return the remaining time (millisol) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) throws Exception {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (UNLOADING.equals(getPhase())) return unloadingPhase(time);
    	else return time;
    }
    
    /**
     * Perform the unloading phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) after performing the phase.
     * @throws Exception if error in loading phase.
     */
    protected double unloadingPhase(double time) throws Exception {
    	
        // Determine unload rate.
		int strength = person.getNaturalAttributeManager().getAttribute(NaturalAttributeManager.STRENGTH);
		double strengthModifier = (double) strength / 50D;
        double amountUnloading = UNLOAD_RATE * strengthModifier * time;

        // If vehicle is not in a garage, unload rate is reduced.
        Building garage = BuildingManager.getBuilding(vehicle);
        if (garage == null) amountUnloading /= 4D;
        
        Inventory vehicleInv = vehicle.getInventory();
        if (settlement == null) {
        	endTask();
        	return 0D;
        }
        Inventory settlementInv = settlement.getInventory();
        
        // Unload equipment.
        if (amountUnloading > 0D) {
        	UnitIterator k = vehicleInv.findAllUnitsOfClass(Equipment.class).iterator();
        	while (k.hasNext() && (amountUnloading > 0D)) {
        		Equipment equipment = (Equipment) k.next();
        		
        		// Unload inventories of equipment (if possible)
        		unloadEquipmentInventory(equipment);
        		
        		vehicleInv.retrieveUnit(equipment);
        		settlementInv.storeUnit(equipment);
        		amountUnloading -= equipment.getMass();
        	}
        }
        
        // Unload amount resources.
        Iterator i = vehicleInv.getAllAmountResourcesStored().iterator();
        while (i.hasNext() && (amountUnloading > 0D)) {
        	AmountResource resource = (AmountResource) i.next();
        	double amount = vehicleInv.getAmountResourceStored(resource);
        	if (amount > amountUnloading) amount = amountUnloading;
        	double capacity = settlementInv.getAmountResourceRemainingCapacity(resource);
        	if (capacity < amount) {
        		amount = capacity;
        		amountUnloading = 0D;
        	}
        	try {
        		vehicleInv.retrieveAmountResource(resource, amount);
        		settlementInv.storeAmountResource(resource, amount);
        	}
        	catch (Exception e) {}
			amountUnloading -= amount;
        }
        
        // Unload item resources.
        if (amountUnloading > 0D) {
        	Iterator j = vehicleInv.getAllItemResourcesStored().iterator();
        	while (j.hasNext() && (amountUnloading > 0D)) {
        		ItemResource resource = (ItemResource) j.next();
        		int num = vehicleInv.getItemResourceNum(resource);
        		if ((num * resource.getMassPerItem()) > amountUnloading) {
        			num = (int) Math.round(amountUnloading / resource.getMassPerItem());
        			if (num == 0) num = 1;
        		}
        		vehicleInv.retrieveItemResources(resource, num);
        		settlementInv.storeItemResources(resource, num);
        		amountUnloading -= (num * resource.getMassPerItem());
        	}
        }
		
        if (isFullyUnloaded(vehicle)) endTask();
        
        return 0D;
    }
    
    private void unloadEquipmentInventory(Equipment equipment) throws InventoryException {
    	Inventory eInv = equipment.getInventory();
    	Inventory sInv = settlement.getInventory();
    	
        // Unload amount resources.
    	// Note: only unloading amount resources at the moment.
        Iterator i = eInv.getAllAmountResourcesStored().iterator();
        while (i.hasNext()) {
        	AmountResource resource = (AmountResource) i.next();
        	double amount = eInv.getAmountResourceStored(resource);
        	double capacity = sInv.getAmountResourceRemainingCapacity(resource);
        	if (amount < capacity) amount = capacity;
        	try {
        		eInv.retrieveAmountResource(resource, amount);
        		sInv.storeAmountResource(resource, amount);
        	}
        	catch (Exception e) {}
        }
    }
    
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// This task adds no experience.
	}

    /** 
     * Returns true if the vehicle is fully unloaded.
     * @param vehicle Vehicle to check.
     * @return is vehicle fully unloaded?
     * @throws InventoryException if error checking vehicle.
     */
    static public boolean isFullyUnloaded(Vehicle vehicle) throws InventoryException {
        return (vehicle.getInventory().getTotalInventoryMass() == 0D);
    }
    
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		return 0;	
	}
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List<String> getAssociatedSkills() {
		List<String> results = new ArrayList<String>(0);
		return results;
	}
}