/**
 * Mars Simulation Project
 * MaintainGroundVehicleGarage.java
 * @version 2.76 2004-05-02
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.Iterator;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.malfunction.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;
import org.mars_sim.msp.simulation.vehicle.*;

/** 
 * The MaintainGroundVehicleGarage class is a task for performing
 * preventive maintenance on ground vehicles in a garage.
 */
public class MaintainGroundVehicleGarage extends Task implements Serializable {

	// Static members
	private static final double STRESS_MODIFIER = .1D; // The stress modified per millisol.

    // Data members
    private VehicleMaintenance garage; // The maintenance garage.
    private GroundVehicle vehicle; // Vehicle to be maintained.
    private double duration; // Duration (in millisols) the person will perform this task.

    /** 
     * Constructor
     *
     * @param person the person to perform the task
     * @param mars the virtual Mars
     */
    public MaintainGroundVehicleGarage(Person person, Mars mars) {
        super("Performing Vehicle Maintenance", person, true, false, STRESS_MODIFIER, mars);

        // Choose an available needy ground vehicle.
        vehicle = getNeedyGroundVehicle(person);
        if (vehicle != null) vehicle.setReservedForMaintenance(true);
        
        // Determine the garage it's in.
        Building building = BuildingManager.getBuilding(vehicle);
        if (building != null) {
        	try {
        		garage = (VehicleMaintenance) building.getFunction(GroundVehicleMaintenance.NAME);
        	}
        	catch (Exception e) {
        		System.err.println("MaintainGroundVehicleGarage.constructor: " + e.getMessage());
        	}
        }
        
        // End task if vehicle or garage not available.
        if ((vehicle == null) || (garage == null)) endTask();    
        
        // Randomly determine duration, from 0 - 500 millisols
        duration = RandomUtil.getRandomDouble(500D);
        
        // System.out.println(person.getName() + " starting MaintainGroundVehicleGarage task.");
    }

    /** 
     * Returns the weighted probability that a person might perform this task.
     * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     * @param person the person to perform the task
     * @param mars the virtual Mars
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, Mars mars) {
        double result = 0D;

        VehicleIterator i = getAllVehicleCandidates(person).iterator();
        while (i.hasNext()) {
            MalfunctionManager manager = i.next().getMalfunctionManager();
            double entityProb = (manager.getEffectiveTimeSinceLastMaintenance() / 200D);
            if (entityProb > 50D) entityProb = 50D;
            result += entityProb;
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
	
        return result;
    }

    /** 
     * This task simply waits until the set duration of the task is complete, then ends the task.
     * @param time the amount of time to perform this task (in millisols)
     * @return amount of time remaining after finishing with task (in millisols)
     * @throws Exception if error performing task.
     */
    double performTask(double time) throws Exception {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        MalfunctionManager manager = vehicle.getMalfunctionManager();
	
        // If person is incompacitated, end task.
        if (person.getPerformanceRating() == 0D) endTask();

        // Check if maintenance has already been completed.
        if (manager.getEffectiveTimeSinceLastMaintenance() == 0D) endTask();

        // If vehicle has malfunction, end task.
        if (manager.hasMalfunction()) endTask();

        if (isDone()) return timeLeft;
	
        // Determine effective work time based on "Mechanic" skill.
        double workTime = timeLeft;
        int mechanicSkill = person.getSkillManager().getEffectiveSkillLevel("Mechanic");
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

        // Add work to the maintenance
        manager.addMaintenanceWorkTime(workTime);

        // Add experience to "Mechanic" skill.
        // (1 base experience point per 100 millisols of time spent)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double experience = timeLeft / 100D;
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        experience += experience * (((double) nManager.getAttribute("Experience Aptitude") - 50D) / 100D);
        person.getSkillManager().addExperience("Mechanic", experience);

        // If maintenance is complete, task is done.
        if (manager.getEffectiveTimeSinceLastMaintenance() == 0D) {
            // System.out.println(person.getName() + " finished " + description);
            vehicle.setReservedForMaintenance(false);
            endTask();
        }

        // Keep track of the duration of the task.
        timeCompleted += time;
        if (timeCompleted >= duration) endTask();

        // Check if an accident happens during maintenance.
        checkForAccident(timeLeft);
	
        return 0D;
    }

    /**
     * Check for accident with entity during maintenance phase.
     * @param time the amount of time (in millisols)
     */
    private void checkForAccident(double time) {

        double chance = .001D;

        // Mechanic skill modification.
        int skill = person.getSkillManager().getEffectiveSkillLevel("Mechanic");
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
     *
     * @param person person checking.
     * @return collection of ground vehicles available for maintenance.
     */
    private static VehicleCollection getAllVehicleCandidates(Person person) {
        VehicleCollection result = new VehicleCollection();
        
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            Settlement settlement = person.getSettlement();
            Iterator i = settlement.getBuildingManager().getBuildings(GroundVehicleMaintenance.NAME).iterator();
            while (i.hasNext()) {
            	try {
            		Building building = (Building) i.next();
                	VehicleMaintenance garage = (VehicleMaintenance) building.getFunction(GroundVehicleMaintenance.NAME);
                	boolean malfunction = building.getMalfunctionManager().hasMalfunction();
                	if (!malfunction) {
                    	VehicleIterator vehicleI = garage.getVehicles().iterator();
                    	while (vehicleI.hasNext()) {
                        	Vehicle vehicle = vehicleI.next();
                        	if ((vehicle instanceof GroundVehicle) && !vehicle.isReserved()) result.add(vehicle);
                    	}
                    }
                }
                catch (Exception e) {
                	System.err.println("MaintainGroundVehicleGarage.getAllVehicleCandidates(): " + e.getMessage());
                }
            }
        }
        
        return result;
    }
    
    /**
     * Gets a ground vehicle that requires maintenance in a local garage.
     * Returns null if none available.
     *
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
            Vehicle vehicle = i.next();
            MalfunctionManager manager = vehicle.getMalfunctionManager();
            double probWeight = manager.getEffectiveTimeSinceLastMaintenance();
            if (rand < probWeight) result = (GroundVehicle) vehicle;
            else rand -= probWeight;
        }
        
        return result;
    }
}
