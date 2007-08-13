/**
 * Mars Simulation Project
 * MaintainGroundVehicleGarage.java
 * @version 2.81 2007-08-12
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.malfunction.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.SkillManager;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;
import org.mars_sim.msp.simulation.vehicle.*;

/** 
 * The MaintainGroundVehicleGarage class is a task for performing
 * preventive maintenance on ground vehicles in a garage.
 */
public class MaintainGroundVehicleGarage extends Task implements Serializable {
	
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
    public MaintainGroundVehicleGarage(Person person) throws Exception {
        super("Performing Vehicle Maintenance", person, true, false, STRESS_MODIFIER, 
        		true, RandomUtil.getRandomDouble(100D));

        // Choose an available needy ground vehicle.
        vehicle = getNeedyGroundVehicle(person);
        if (vehicle != null) vehicle.setReservedForMaintenance(true);
        
        // Determine the garage it's in.
        if (vehicle != null) {
        	Building building = BuildingManager.getBuilding(vehicle);
        	if (building != null) {
        		try {
        			garage = (VehicleMaintenance) building.getFunction(GroundVehicleMaintenance.NAME);
        			BuildingManager.addPersonToBuilding(person, building);
        		}
        		catch (Exception e) {
        			System.err.println("MaintainGroundVehicleGarage.constructor: " + e.getMessage());
        		}
        	}
        	else {
        		// If not in a garage, try to add it to a garage with empty space.
        		Settlement settlement = person.getSettlement();
        		Iterator j = settlement.getBuildingManager().getBuildings(GroundVehicleMaintenance.NAME).iterator();
        		while (j.hasNext()) {
        			try {
        				Building garageBuilding = (Building) j.next();
        				VehicleMaintenance garageTemp = (VehicleMaintenance) garageBuilding.getFunction(GroundVehicleMaintenance.NAME);
        				if (garageTemp.getCurrentVehicleNumber() < garageTemp.getVehicleCapacity()) {
        					garage = garageTemp;
        					garage.addVehicle(vehicle);
        					BuildingManager.addPersonToBuilding(person, garageBuilding);
        				} 
        			}
        			catch (Exception e) {
        				System.err.println("MaintainGroundVehicleGarage.constructor: " + e.getMessage());
        			}
        		}
			}
        }
        
        // End task if vehicle or garage not available.
        if ((vehicle == null) || (garage == null)) endTask();    

        // Initialize phase
        addPhase(MAINTAIN_VEHICLE);
        setPhase(MAINTAIN_VEHICLE);
        
        // System.out.println(person.getName() + " starting MaintainGroundVehicleGarage task.");
    }

    /** 
     * Returns the weighted probability that a person might perform this task.
     * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

		// Get all vehicles requiring maintenance.
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
        	VehicleIterator i = getAllVehicleCandidates(person).iterator();
        	while (i.hasNext()) {
            	MalfunctionManager manager = i.next().getMalfunctionManager();
            	double entityProb = (manager.getEffectiveTimeSinceLastMaintenance() / 200D);
            	if (entityProb > 50D) entityProb = 50D;
            	result += entityProb;
        	}
		}
        
		// Determine if settlement has available space in garages or 
		// garage has vehicle currently being worked on.
		boolean garageSpace = false;
		boolean vehicleMaint = false;
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {	
			Settlement settlement = person.getSettlement();
			Iterator j = settlement.getBuildingManager().getBuildings(GroundVehicleMaintenance.NAME).iterator();
			while (j.hasNext()) {
				try {
					Building building = (Building) j.next();
					VehicleMaintenance garage = (VehicleMaintenance) building.getFunction(GroundVehicleMaintenance.NAME);
					if (garage.getCurrentVehicleNumber() < garage.getVehicleCapacity()) garageSpace = true;
					VehicleIterator h = garage.getVehicles().iterator();
					while (h.hasNext()) {
						if (h.next().isReservedForMaintenance()) vehicleMaint = true;
					}
				}
				catch (Exception e) {}
			}
		}
		if (!garageSpace && !vehicleMaint) result = 0D;

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
		// Job modifier.
        Job job = person.getMind().getJob();
		if (job != null) result *= job.getStartTaskProbabilityModifier(MaintainGroundVehicleGarage.class);        
	
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
    	if (MAINTAIN_VEHICLE.equals(getPhase())) return maintainVehiclePhase(time);
    	else return time;
    }
    
    /**
     * Performs the maintain vehicle phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left after performing the phase.
     * @throws Exception if error performing the phase.
     */
    private double maintainVehiclePhase(double time) throws Exception {
        MalfunctionManager manager = vehicle.getMalfunctionManager();
    	
            // If person is incompacitated, end task.
            if (person.getPerformanceRating() == 0D) endTask();

            // Check if maintenance has already been completed.
            if (manager.getEffectiveTimeSinceLastMaintenance() == 0D) endTask();

            // If vehicle has malfunction, end task.
            if (manager.hasMalfunction()) endTask();

            if (isDone()) return time;
    	
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
                // System.out.println(person.getName() + " finished " + description);
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

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            // System.out.println(person.getName() + " has accident while performing maintenance on " + vehicle.getName() + ".");
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
    private static VehicleCollection getAllVehicleCandidates(Person person) {
		VehicleCollection result = new VehicleCollection();
        
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			VehicleIterator vI = person.getSettlement().getParkedVehicles().iterator();
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
     */
    private GroundVehicle getNeedyGroundVehicle(Person person) {
            
        GroundVehicle result = null;

        // Find all vehicles that can be maintained.
        VehicleCollection availableVehicles = getAllVehicleCandidates(person);
        
        // Determine total probability weight.
        double totalProbWeight = 0D;
        VehicleIterator i = availableVehicles.iterator();
        while (i.hasNext()) {
            MalfunctionManager manager = i.next().getMalfunctionManager();
            totalProbWeight = manager.getEffectiveTimeSinceLastMaintenance();
        }
        
        // Get random value
        double rand = RandomUtil.getRandomDouble(totalProbWeight);
        
        // Determine which vehicle was picked.
        VehicleIterator i2 = availableVehicles.iterator();
        while (i2.hasNext() && (result == null)) {
            Vehicle vehicle = i2.next();
            MalfunctionManager manager = vehicle.getMalfunctionManager();
            double probWeight = manager.getEffectiveTimeSinceLastMaintenance();
            if (rand < probWeight) result = (GroundVehicle) vehicle;
            else rand -= probWeight;
        }
        
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
}