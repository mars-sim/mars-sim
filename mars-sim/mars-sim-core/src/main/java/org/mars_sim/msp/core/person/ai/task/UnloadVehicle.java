/**
 * Mars Simulation Project
 * UnloadVehicle.java
 * @version 3.03 2012-07-26
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Towing;
import org.mars_sim.msp.core.vehicle.Vehicle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
 * The UnloadVehicle class is a task for unloading a fuel and supplies from a vehicle.
 */
public class UnloadVehicle extends Task implements Serializable {
	
    private static Logger logger = Logger.getLogger(UnloadVehicle.class.getName());
    
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
    public UnloadVehicle(Person person) {
    	// Use Task constructor.
    	super("Unloading vehicle", person, true, false, STRESS_MODIFIER, true, DURATION);
    	
    	settlement = person.getSettlement();
    	
    	VehicleMission mission = getMissionNeedingUnloading();
    	if (mission != null) vehicle = mission.getVehicle();
    	else vehicle = getNonMissionVehicleNeedingUnloading(settlement);
    	
    	if (vehicle != null) {
    		setDescription("Unloading " + vehicle.getName());
    		
    		// If vehicle is in a garage, add person to garage.
            Building garage = BuildingManager.getBuilding(vehicle);
            if (garage != null) {
                BuildingManager.addPersonToBuilding(person, garage);
            }
    		
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
    public UnloadVehicle(Person person, Vehicle vehicle) {
    	// Use Task constructor.
        super("Unloading vehicle", person, true, false, STRESS_MODIFIER, true, DURATION);

	    setDescription("Unloading " + vehicle.getName());
        this.vehicle = vehicle;

        settlement = person.getSettlement();
        
        // If vehicle is in a garage, add person to garage.
        Building garage = BuildingManager.getBuilding(vehicle);
        if (garage != null) {
            BuildingManager.addPersonToBuilding(person, garage);
        }
        
        // Initialize phase
        addPhase(UNLOADING);
        setPhase(UNLOADING);

        // logger.info(person.getName() + " is unloading " + vehicle.getName());
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
        	
        	// Check all vehicle missions occurring at the settlement.
        	try {
        		int numVehicles = 0;
        		numVehicles += getAllMissionsNeedingUnloading(person.getSettlement()).size();
        		if (getNonMissionVehicleNeedingUnloading(person.getSettlement()) != null) numVehicles++;
        		result = 50D * numVehicles;
        	}
        	catch (Exception e) {
        		logger.log(Level.SEVERE,"Error finding unloading missions. " + e.getMessage());
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
    		Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
    		while (i.hasNext()) {
    			Vehicle vehicle = i.next();
                boolean needsUnloading = false;
    			if (!vehicle.isReserved()) {
                    if (vehicle.getInventory().getTotalInventoryMass(false) > 0D) needsUnloading = true;
                    if (vehicle instanceof Towing) {
                        if (((Towing) vehicle).getTowedVehicle() != null) needsUnloading = true;
                    }
                }
                if (needsUnloading) result = vehicle;
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
    private static List<Mission> getAllMissionsNeedingUnloading(Settlement settlement) {
    	
    	List<Mission> result = new ArrayList<Mission>();
    	
    	MissionManager manager = Simulation.instance().getMissionManager();
    	Iterator<Mission> i = manager.getMissions().iterator();
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
    private VehicleMission getMissionNeedingUnloading() {
    	
    	VehicleMission result = null;
    	
    	List<Mission> unloadingMissions = getAllMissionsNeedingUnloading(person.getSettlement());
    	
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
    protected double performMappedPhase(double time) {
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
    protected double unloadingPhase(double time) {
    	
        // Determine unload rate.
		int strength = person.getNaturalAttributeManager().getAttribute(NaturalAttributeManager.STRENGTH);
		double strengthModifier = .1D + (strength * .018D);
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
        	Iterator<Unit> k = vehicleInv.findAllUnitsOfClass(Equipment.class).iterator();
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
        Iterator<AmountResource> i = vehicleInv.getAllAmountResourcesStored(false).iterator();
        while (i.hasNext() && (amountUnloading > 0D)) {
            AmountResource resource = i.next();
            double amount = vehicleInv.getAmountResourceStored(resource, false);
            if (amount > amountUnloading) amount = amountUnloading;
            double capacity = settlementInv.getAmountResourceRemainingCapacity(resource, true, false);
            if (capacity < amount) {
                amount = capacity;
                amountUnloading = 0D;
            }
            try {
                vehicleInv.retrieveAmountResource(resource, amount);
                settlementInv.storeAmountResource(resource, amount, true);
            }
            catch (Exception e) {}
            amountUnloading -= amount;
        }
        
        // Unload item resources.
        if (amountUnloading > 0D) {
        	Iterator<ItemResource> j = vehicleInv.getAllItemResourcesStored().iterator();
        	while (j.hasNext() && (amountUnloading > 0D)) {
        		ItemResource resource = j.next();
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
        
        // Unload towed vehicles.
        if (vehicle instanceof Towing) {
            Towing towingVehicle = (Towing) vehicle;
            Vehicle towedVehicle = towingVehicle.getTowedVehicle();
            if (towedVehicle != null) {
                towingVehicle.setTowedVehicle(null);
                towedVehicle.setTowingVehicle(null);
                if (!settlementInv.containsUnit(towedVehicle)) {
                    settlementInv.storeUnit(towedVehicle);
                    towedVehicle.determinedSettlementParkedLocationAndFacing();
                }
            }
        }
		
        if (isFullyUnloaded(vehicle)) endTask();
        
        return 0D;
    }
    
    private void unloadEquipmentInventory(Equipment equipment) {
    	Inventory eInv = equipment.getInventory();
    	Inventory sInv = settlement.getInventory();
    	
        // Unload amount resources.
    	// Note: only unloading amount resources at the moment.
    	Iterator<AmountResource> i = eInv.getAllAmountResourcesStored(false).iterator();
    	while (i.hasNext()) {
    	    AmountResource resource = i.next();
    	    double amount = eInv.getAmountResourceStored(resource, false);
    	    double capacity = sInv.getAmountResourceRemainingCapacity(resource, true, false);
    	    if (amount < capacity) amount = capacity;
    	    try {
    	        eInv.retrieveAmountResource(resource, amount);
    	        sInv.storeAmountResource(resource, amount, true);
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
    static public boolean isFullyUnloaded(Vehicle vehicle) {
        return (vehicle.getInventory().getTotalInventoryMass(false) == 0D);
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
	
	@Override
	public void destroy() {
	    super.destroy();
	    
	    vehicle = null;
	    settlement = null;
	}
}