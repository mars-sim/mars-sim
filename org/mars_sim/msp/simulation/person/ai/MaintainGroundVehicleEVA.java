/**
 * Mars Simulation Project
 * MaintainGroundVehicleEVA.java
 * @version 2.75 2003-04-18
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
 * preventive maintenance on ground vehicles outside a settlement.
 */
public class MaintainGroundVehicleEVA extends EVAOperation implements Serializable {
 
    // Phase names
    private static final String EXIT_AIRLOCK = "Exit Airlock";
    private static final String MAINTAIN_VEHICLE = "Maintain Vehicle";
    private static final String ENTER_AIRLOCK = "Enter Airlock";
 
    private GroundVehicle vehicle; // Vehicle to be maintained.
    private Airlock airlock; // Airlock to be used for EVA.
    private double duration; // Duration (in millisols) the person will perform this task.
    
    public MaintainGroundVehicleEVA(Person person, Mars mars) {
        super("Performing Vehicle Maintenance", person, mars);
   
        // Choose an available needy ground vehicle.
        vehicle = getNeedyGroundVehicle(person);
        if (vehicle != null) vehicle.setReservedForMaintenance(true);
        else endTask();
        
        // Get an available airlock.
        airlock = getAvailableAirlock(person);
        if (airlock == null) endTask();
        
        // Randomly determine duration, from 0 - 500 millisols
        duration = RandomUtil.getRandomDouble(500D);
        
        System.out.println(person.getName() + " starting MaintainGroundVehicleEVA task.");
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

        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            VehicleIterator i = getAllVehicleCandidates(person).iterator();
            while (i.hasNext()) {
                MalfunctionManager manager = i.next().getMalfunctionManager();
                result += (manager.getTimeSinceLastMaintenance() / 200D);
            }
        }

        // Check if an airlock is available
        if (getAvailableAirlock(person) == null) result = 0D;

        // Check if it is night time.
        if (mars.getSurfaceFeatures().getSurfaceSunlight(person.getCoordinates()) == 0) result = 0D; 

        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
	
        return result;
    }
    
    /**
     * Perform the task.
     * @param time the amount of time (millisols) to perform the task
     * @return amount of time remaining after performing the task
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        while ((timeLeft > 0D) && !done) {
            if (phase.equals(EXIT_AIRLOCK)) timeLeft = exitEVA(timeLeft);
            else if (phase.equals(MAINTAIN_VEHICLE)) timeLeft = maintainVehicle(timeLeft);
            else if (phase.equals(ENTER_AIRLOCK)) timeLeft = enterEVA(timeLeft);
        }					            
	
        // Add experience to "EVA Operations" skill.
        // (1 base experience point per 20 millisols of time spent)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double experience = time / 50D;
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        experience += experience * (((double) nManager.getAttribute("Experience Aptitude") - 50D) / 100D);
        person.getSkillManager().addExperience("EVA Operations", experience);

        return timeLeft;
    }
    
    /**
     * Perform the exit airlock phase of the task.
     *
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     */
    private double exitEVA(double time) {
        try {
            time = exitAirlock(time, airlock);
        }
        catch (Exception e) { 
            System.out.println(e.getMessage()); 
        }
        
        if (exitedAirlock) phase = MAINTAIN_VEHICLE;
        return time;
    }
    
    /**
     * Perform the maintain vehicle phase of the task.
     *
     * @param time the time to perform this phase (in millisols)
     * @return the time remaining after performing this phase (in millisols)
     */
    private double maintainVehicle(double time) {
        
        MalfunctionManager manager = vehicle.getMalfunctionManager();
        boolean malfunction = manager.hasMalfunction();
        boolean finishedMaintenance = (manager.getTimeSinceLastMaintenance() == 0D);
        if (finishedMaintenance) vehicle.setReservedForMaintenance(false);
        
        if (finishedMaintenance || malfunction || shouldEndEVAOperation()) {
            phase = ENTER_AIRLOCK;
            return time;
        }
        
        // Determine effective work time based on "Mechanic" skill.
        double workTime = time;
        int mechanicSkill = person.getSkillManager().getEffectiveSkillLevel("Mechanic");
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

        // Add work to the maintenance
        manager.addMaintenanceWorkTime(workTime);

        // Add experience to "Mechanic" skill.
        // (1 base experience point per 100 millisols of time spent)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double experience = time / 100D;
        NaturalAttributeManager nManager = person.getNaturalAttributeManager();
        experience += experience * (((double) nManager.getAttribute("Experience Aptitude") - 50D) / 100D);
        person.getSkillManager().addExperience("Mechanic", experience);

        // Keep track of the duration of the task.
        timeCompleted += time;
        if (timeCompleted >= duration) phase = ENTER_AIRLOCK;
	
        // Check if an accident happens during maintenance.
        checkForAccident(time);

        return 0D;
    }   
    
    /**
     * Perform the enter airlock phase of the task.
     *
     * @param time amount of time to perform the phase
     * @return time remaining after performing the phase
     */
    private double enterEVA(double time) {
        try {
            time = enterAirlock(time, airlock);
        }
        catch (Exception e) { 
            System.out.println(e.getMessage()); 
        }
        
        if (enteredAirlock) endTask();
        return time;
    }	
    
    
    
    /**
     * Ends the task and performs any final actions.
     */
    public void endTask() {
        if (vehicle != null) vehicle.setReserved(false);
        done = true;
    }
    
    /**
     * Check for accident with entity during maintenance phase.
     * @param time the amount of time (in millisols)
     */
    protected void checkForAccident(double time) {

        // Use EVAOperation checkForAccident() method.
        super.checkForAccident(time);
        
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
     * Gets all ground vehicles requiring maintenance that are parked outside the settlement.
     *
     * @param person person checking.
     * @return collection of ground vehicles available for maintenance.
     */
    private static VehicleCollection getAllVehicleCandidates(Person person) {
        VehicleCollection result = new VehicleCollection();
        
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            Settlement settlement = person.getSettlement();
            VehicleCollection parkedVehicles = settlement.getParkedVehicles();
            
            // Remove all ground vehicles in garages.
            Iterator i = settlement.getBuildingManager().getBuildings(GroundVehicleMaintenance.class).iterator();
            while (i.hasNext()) {
                GroundVehicleMaintenance garage = (GroundVehicleMaintenance) i.next();
                VehicleIterator vI = parkedVehicles.iterator();
                while (vI.hasNext()) {
                    Vehicle vehicle = vI.next();
                    if (garage.containsVehicle(vehicle)) vI.remove();
                }
            }
            
            VehicleIterator vI = parkedVehicles.iterator();
            while (vI.hasNext()) {
                Vehicle vehicle = vI.next();
                if ((vehicle instanceof GroundVehicle) && !vehicle.isReserved()) result.add(vehicle);
            }
        }
        
        return result;
    }
    
    /**
     * Gets a ground vehicle that requires maintenance outside the settlement.
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
}
