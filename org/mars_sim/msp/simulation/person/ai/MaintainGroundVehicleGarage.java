/**
 * Mars Simulation Project
 * MaintainGroundVehicleGarage.java
 * @version 2.75 2003-04-16
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import java.io.Serializable;
import java.util.Iterator;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.malfunction.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.function.GroundVehicleMaintenance;
import org.mars_sim.msp.simulation.vehicle.*;

/** 
 * The MaintainGroundVehicleGarage class is a task for performing
 * preventive maintenance on ground vehicles in a garage.
 */
public class MaintainGroundVehicleGarage extends Task implements Serializable {

    // Data members
    private GroundVehicleMaintenance garage; // The maintenance garage.
    private GroundVehicle vehicle; // Vehicle to be maintained.
    private double duration; // Duration (in millisols) the person will perform this task.

    /** 
     * Constructor
     *
     * @param person the person to perform the task
     * @param mars the virtual Mars
     */
    public MaintainGroundVehicleGarage(Person person, Mars mars) {
        super("Performing Vehicle Maintenance", person, true, mars);

        // Choose an available needy ground vehicle.
        vehicle = getNeedyGroundVehicle(person);
        
        
        // Determine the garage it's in.
        garage = getGarage(vehicle);
        
        // End task if vehicle or garage not available.
        if ((vehicle == null) || (garage == null)) done = true;    
        
        // Randomly determine duration, from 0 - 500 millisols
        duration = RandomUtil.getRandomDouble(500D);
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
            result += (manager.getTimeSinceLastMaintenance() / 200D);
        }

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
	
        return result;
    }

    /** 
     * This task simply waits until the set duration of the task is complete, then ends the task.
     * @param time the amount of time to perform this task (in millisols)
     * @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        MalfunctionManager manager = vehicle.getMalfunctionManager();
	
        // If person is incompacitated, end task.
        if (person.getPerformanceRating() == 0D) done = true;

        // Check if maintenance has already been completed.
        if (manager.getTimeSinceLastMaintenance() == 0D) done = true;

        // If vehicle has malfunction, end task.
        if (manager.hasMalfunction()) done = true;

        if (done) return timeLeft;
	
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
        if (manager.getTimeSinceLastMaintenance() == 0D) {
            // System.out.println(person.getName() + " finished " + description);
            done = true;
        }

        // Keep track of the duration of the task.
        timeCompleted += time;
        if (timeCompleted >= duration) done = true;

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
            Iterator i = settlement.getBuildingManager().getBuildings(GroundVehicleMaintenance.class).iterator();
            while (i.hasNext()) {
                GroundVehicleMaintenance garage = (GroundVehicleMaintenance) i.next();
                boolean malfunction = ((Building) garage).getMalfunctionManager().hasMalfunction();
                if (!malfunction) {
                    VehicleIterator vehicleI = garage.getVehicles().iterator();
                    while (vehicleI.hasNext()) {
                        Vehicle vehicle = vehicleI.next();
                        if ((vehicle instanceof GroundVehicle) && !vehicle.isReserved()) result.add(vehicle);
                    }
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
            totalProbWeight = manager.getTimeSinceLastMaintenance();
        }
        
        // Get random value
        double rand = RandomUtil.getRandomDouble(totalProbWeight);
        
        // Determine which vehicle was picked.
        VehicleIterator i2 = availableVehicles.iterator();
        while (i2.hasNext() && (result == null)) {
            Vehicle vehicle = i.next();
            MalfunctionManager manager = vehicle.getMalfunctionManager();
            double probWeight = manager.getTimeSinceLastMaintenance();
            if (rand < probWeight) result = (GroundVehicle) vehicle;
            else rand -= probWeight;
        }
        
        return result;
    }
    
    /**
     * Gets the maintenance garage a vehicle is in.
     * Returns null if vehicle is not in a garage.
     *
     * @param vehicle the ground vehicle
     * @return GroundVehicleMaintenance garage
     */
    private GroundVehicleMaintenance getGarage(GroundVehicle vehicle) {
        
        GroundVehicleMaintenance result = null;
        
        Settlement settlement = vehicle.getSettlement();
        if (settlement != null) {
            Iterator i = settlement.getBuildingManager().getBuildings(GroundVehicleMaintenance.class).iterator();
            while (i.hasNext()) {
                GroundVehicleMaintenance garage = (GroundVehicleMaintenance) i.next();
                if (garage.containsVehicle(vehicle)) result = garage;
            }
        }
        
        return result;
    }
}
