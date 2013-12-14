/**
 * Mars Simulation Project
 * MaintainGroundVehicleGarage.java
 * @version 3.06 2013-12-09
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnectorManager;
import org.mars_sim.msp.core.structure.building.function.GroundVehicleMaintenance;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
 * The MaintainGroundVehicleGarage class is a task for performing
 * preventive maintenance on ground vehicles in a garage.
 */
public class MaintainGroundVehicleGarage extends Task implements Serializable {
	
    private static Logger logger = Logger.getLogger(MaintainGroundVehicleGarage.class.getName());
	
	// Task phase
	private static final String MAINTAIN_VEHICLE = "Maintaining Vehicle";

	// Static members
	private static final double STRESS_MODIFIER = .1D; // The stress modified per millisol.

    // Data members
    private VehicleMaintenance garage; // The maintenance garage.
    private GroundVehicle vehicle; // Vehicle to be maintained.

    /** 
     * Constructor
     * @param person the person to perform the task
     * @throws Exception if error constructing task.
     */
    public MaintainGroundVehicleGarage(Person person) {
        super("Performing Vehicle Maintenance", person, true, false, STRESS_MODIFIER, 
        		true, 10D + RandomUtil.getRandomDouble(40D));

        // Choose an available needy ground vehicle.
        vehicle = getNeedyGroundVehicle(person);
        if (vehicle != null) vehicle.setReservedForMaintenance(true);
        
        // Determine the garage it's in.
        if (vehicle != null) {
        	Building building = BuildingManager.getBuilding(vehicle);
        	if (building != null) {
        		try {
        			garage = (VehicleMaintenance) building.getFunction(GroundVehicleMaintenance.NAME);
        			
        			// Walk to garage.
        			walkToGarageBuilding(building);
        		}
        		catch (Exception e) {
        		    logger.log(Level.SEVERE,"MaintainGroundVehicleGarage.constructor: ",e);
        		}
        	}
        	else {
        		// If not in a garage, try to add it to a garage with empty space.
        		Settlement settlement = person.getSettlement();
        		Iterator<Building> j = settlement.getBuildingManager().getBuildings(
        		        GroundVehicleMaintenance.NAME).iterator();
        		while (j.hasNext() && (garage == null)) {
        			try {
        				Building garageBuilding = j.next();
        				VehicleMaintenance garageTemp = (VehicleMaintenance) garageBuilding.getFunction(GroundVehicleMaintenance.NAME);
        				if (garageTemp.getCurrentVehicleNumber() < garageTemp.getVehicleCapacity()) {
        					garage = garageTemp;
        					garage.addVehicle(vehicle);
        					
        					// Walk to garage.
                            walkToGarageBuilding(garageBuilding);
        				} 
        			}
        			catch (Exception e) {
        			    logger.log(Level.SEVERE,"MaintainGroundVehicleGarage.constructor: ",e);
        			}
        		}
			}
        }
        
        // End task if vehicle or garage not available.
        if ((vehicle == null) || (garage == null)) endTask();    

        // Initialize phase
        addPhase(MAINTAIN_VEHICLE);
        setPhase(MAINTAIN_VEHICLE);
        
        logger.finest(person.getName() + " starting MaintainGroundVehicleGarage task.");
    }

    /** 
     * Returns the weighted probability that a person might perform this task.
     * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        try {
        	// Get all vehicles requiring maintenance.
        	if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
        		Iterator<Vehicle> i = getAllVehicleCandidates(person).iterator();
        		while (i.hasNext()) {
        			Vehicle vehicle = i.next();
        			MalfunctionManager manager = vehicle.getMalfunctionManager();
        			boolean hasMalfunction = manager.hasMalfunction();
        			boolean hasParts = Maintenance.hasMaintenanceParts(person, vehicle);
        			double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
        			boolean minTime = (effectiveTime >= 1000D);
        			if (!hasMalfunction && hasParts && minTime) {
        				double entityProb = effectiveTime / 20D;
        				if (entityProb > 100D) entityProb = 100D;
        				result += entityProb;
        			}
            	}
        	}
		}
        catch (Exception e) {
            logger.log(Level.SEVERE,"getProbability()",e);
        }
        
		// Determine if settlement has available space in garage.
		boolean garageSpace = false;
		boolean needyVehicleInGarage = false;
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {	
			Settlement settlement = person.getSettlement();
			Iterator<Building> j = settlement.getBuildingManager().getBuildings(GroundVehicleMaintenance.NAME).iterator();
			while (j.hasNext() && !garageSpace) {
				try {
					Building building = j.next();
					VehicleMaintenance garage = (VehicleMaintenance) building.getFunction(GroundVehicleMaintenance.NAME);
					if (garage.getCurrentVehicleNumber() < garage.getVehicleCapacity()) garageSpace = true;
					
					Iterator<Vehicle> i = garage.getVehicles().iterator();
					while (i.hasNext()) {
						if (i.next().isReservedForMaintenance()) needyVehicleInGarage = true;
					}
				}
				catch (Exception e) {}
			}
		}
		if (!garageSpace && !needyVehicleInGarage) result = 0D;

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
		// Job modifier.
        Job job = person.getMind().getJob();
		if (job != null) result *= job.getStartTaskProbabilityModifier(MaintainGroundVehicleGarage.class);        
	
        return result;
    }
    
    /**
     * Walk to garage building.
     * @param garageBuilding the garage building.
     */
    private void walkToGarageBuilding(Building garageBuilding) {
        
        // Determine location within garage building.
        // TODO: Use action point rather than random internal location.
        Point2D.Double buildingLoc = LocalAreaUtil.getRandomInteriorLocation(garageBuilding);
        Point2D.Double settlementLoc = LocalAreaUtil.getLocalRelativeLocation(buildingLoc.getX(), 
                buildingLoc.getY(), garageBuilding);
        
        // Check if there is a valid interior walking path between buildings.
        BuildingConnectorManager connectorManager = person.getSettlement().getBuildingConnectorManager();
        Building currentBuilding = BuildingManager.getBuilding(person);
        
        if (connectorManager.hasValidPath(currentBuilding, garageBuilding)) {
            Task walkingTask = new WalkInterior(person, garageBuilding, settlementLoc.getX(), 
                    settlementLoc.getY());
            addSubTask(walkingTask);
        }
        else {
            // TODO: Add task for EVA walking to get to garage building.
            BuildingManager.addPersonToBuilding(person, garageBuilding, settlementLoc.getX(), 
                    settlementLoc.getY());
        }
    }
    
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisol) the phase is to be performed.
     * @return the remaining time (millisol) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (MAINTAIN_VEHICLE.equals(getPhase())) return maintainVehiclePhase(time);
    	else return time;
    }
    
    /**
     * Performs the maintain vehicle phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left after performing the phase.
     * @throws Exception if error performing the phase.
     */
    private double maintainVehiclePhase(double time) {
        MalfunctionManager manager = vehicle.getMalfunctionManager();
    	
        // If person is incompacitated, end task.
        if (person.getPerformanceRating() == 0D) endTask();

        // Check if maintenance has already been completed.
        if (manager.getEffectiveTimeSinceLastMaintenance() == 0D) endTask();

        // If vehicle has malfunction, end task.
        if (manager.hasMalfunction()) endTask();

        if (isDone()) return time;
    	
        // Add repair parts if necessary.
        Inventory inv = person.getTopContainerUnit().getInventory();
        if (Maintenance.hasMaintenanceParts(inv, vehicle)) {
        	Map<Part, Integer> parts = new HashMap<Part, Integer>(manager.getMaintenanceParts());
        	Iterator<Part> j = parts.keySet().iterator();
        	while (j.hasNext()) {
        		Part part = j.next();
        		int number = parts.get(part);
        		inv.retrieveItemResources(part, number);
        		manager.maintainWithParts(part, number);
        	}
        }
        else {
        	vehicle.setReservedForMaintenance(false);
            garage.removeVehicle(vehicle);
            endTask();
			return time;
		}
        
        // Determine effective work time based on "Mechanic" skill.
        double workTime = time;
        int mechanicSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.MECHANICS);
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

        // Add work to the maintenance
        manager.addMaintenanceWorkTime(workTime);
            
        // Add experience points
        addExperience(time);
            
        // If maintenance is complete, task is done.
        if (manager.getEffectiveTimeSinceLastMaintenance() == 0D) {
            // logger.info(person.getName() + " finished " + description);
            vehicle.setReservedForMaintenance(false);
            garage.removeVehicle(vehicle);
            endTask();
        }

        // Check if an accident happens during maintenance.
        checkForAccident(time);
    	
        return 0D;
    }
    
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// Add experience to "Mechanics" skill
		// (1 base experience point per 100 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude" attribute.
        double newPoints = time / 100D;
        int experienceAptitude = person.getNaturalAttributeManager().getAttribute(
        	NaturalAttributeManager.EXPERIENCE_APTITUDE);
        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
        person.getMind().getSkillManager().addExperience(Skill.MECHANICS, newPoints);
	}

    /**
     * Check for accident with entity during maintenance phase.
     * @param time the amount of time (in millisols)
     */
    private void checkForAccident(double time) {

        double chance = .001D;

        // Mechanic skill modification.
        int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.MECHANICS);
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        // Modify based on the vehicle's wear condition.
        chance *= vehicle.getMalfunctionManager().getWearConditionAccidentModifier();
        
        if (RandomUtil.lessThanRandPercent(chance * time)) {
            logger.info(person.getName() + " has accident while performing maintenance on " 
        	    		         + vehicle.getName() 
        	    		         + ".");
            vehicle.getMalfunctionManager().accident();
        }
    }

    /** 
     * Gets the vehicle  the person is maintaining.
     * Returns null if none.
     * @return entity
     */
    public Malfunctionable getVehicle() {
        return vehicle;
    }
    
    /**
     * Gets all ground vehicles requiring maintenance in a local garage.
     * @param person person checking.
     * @return collection of ground vehicles available for maintenance.
     */
    private static Collection<Vehicle> getAllVehicleCandidates(Person person) {
		Collection<Vehicle> result = new ConcurrentLinkedQueue<Vehicle>();
        
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			Iterator<Vehicle> vI = person.getSettlement().getParkedVehicles().iterator();
			while (vI.hasNext()) {
				Vehicle vehicle = vI.next();
				if ((vehicle instanceof GroundVehicle) && !vehicle.isReservedForMission()) result.add(vehicle);
			}
		}
        
		return result;
    }
    
    /**
     * Gets a ground vehicle that requires maintenance in a local garage.
     * Returns null if none available.
     * @param person person checking.
     * @return ground vehicle
     * @throws Exception if error finding needy vehicle.
     */
    private GroundVehicle getNeedyGroundVehicle(Person person) {
            
        GroundVehicle result = null;

        // Find all vehicles that can be maintained.
        Collection<Vehicle> availableVehicles = getAllVehicleCandidates(person);
        
        // Populate vehicles and probabilities.
        Map<Vehicle, Double> vehicleProb = new HashMap<Vehicle, Double>(availableVehicles.size());
        Iterator<Vehicle> i = availableVehicles.iterator();
        while (i.hasNext()) {
            Vehicle vehicle = i.next();
            double prob = getProbabilityWeight(vehicle);
            if (prob > 0D) {
                vehicleProb.put(vehicle, prob);
            }
        }
        
        // Randomly determine needy vehicle.
        if (!vehicleProb.isEmpty()) {
            result = (GroundVehicle) RandomUtil.getWeightedRandomObject(vehicleProb);
        }
        
        if (result != null) {
            setDescription("Performing maintenance on " + result.getName());
        }
        
        return result;
    }
    
    /**
     * Gets the probability weight for a vehicle.
     * @param vehicle the vehicle.
     * @return the probability weight.
     * @throws Exception if error determining probability weight.
     */
    private double getProbabilityWeight(Vehicle vehicle) {
    	double result = 0D;
		MalfunctionManager manager = vehicle.getMalfunctionManager();
		boolean hasMalfunction = manager.hasMalfunction();
		double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
		boolean minTime = (effectiveTime >= 1000D); 
		boolean enoughParts = Maintenance.hasMaintenanceParts(person, vehicle);
		if (!hasMalfunction && minTime && enoughParts) result = effectiveTime;
		return result;
    }
    
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getMind().getSkillManager();
		return manager.getEffectiveSkillLevel(Skill.MECHANICS);
	}
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List<String> getAssociatedSkills() {
		List<String> results = new ArrayList<String>(1);
		results.add(Skill.MECHANICS);
		return results;
	}
	
	@Override
	public void destroy() {
	    super.destroy();
	    
	    garage = null;
	    vehicle = null;
	}
}