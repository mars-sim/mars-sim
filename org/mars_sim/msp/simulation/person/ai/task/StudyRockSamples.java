/**
 * Mars Simulation Project
 * StudyRockSamples.java
 * @version 2.75 2003-11-25
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.Lab;
import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Resource;
import org.mars_sim.msp.simulation.malfunction.MalfunctionManager;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.InhabitableBuilding;
import org.mars_sim.msp.simulation.structure.building.function.Research;
import org.mars_sim.msp.simulation.vehicle.ExplorerRover;
import org.mars_sim.msp.simulation.vehicle.Vehicle;

/** 
 * The StudyRockSamples class is a task for scientific research on 
 * collected rock samples. 
 */
public class StudyRockSamples extends Task implements Serializable {

    // Rate of rock sample research (kg / millisol)
    private static final double RESEARCH_RATE = .01D;
	
    // Data members
    private Inventory inv;   // The inventory containing the rock samples.
    private MalfunctionManager malfunctions; // The labs associated malfunction manager.
    private Lab lab;         // The laboratory the person is working in.
    private double duration; // The duration (in millisols) the person will perform this task.

    /** 
     * Constructor for StudyRockSamples task.  
     * This is an effort driven task.
     * @param person the person to perform the task
     * @param mars the virtual Mars
     */
    public StudyRockSamples(Person person, Mars mars) {
        super("Studying Rock Samples", person, true, mars);
        
        // Find available lab for person.
        lab = getAvailableLab(person);
        
        if (lab != null) {
            String location = person.getLocationSituation();
            if (location.equals(Person.INSETTLEMENT)) {
                InhabitableBuilding building = (InhabitableBuilding) lab;
                malfunctions = building.getMalfunctionManager();
                try {     
                    if (!building.containsPerson(person)) building.addPerson(person);
                    lab.addResearcher();
                    inv = person.getSettlement().getInventory();
                }
                catch (BuildingException e) {
                    System.out.println("StudyRockSamples: person is already in building.");
                    endTask();
                }
                catch (Exception e) { 
                    System.out.println("StudyRockSamples: lab building is already full.");
                    endTask();
                }
            }
            else if (location.equals(Person.INVEHICLE)) {
                Vehicle vehicle = person.getVehicle();
                malfunctions = vehicle.getMalfunctionManager();
                try {
                    lab.addResearcher();
                    inv = vehicle.getInventory();
                }
                catch (Exception e) {
                    System.out.println("StudyRockSamples: rover lab is already full.");
                    endTask();
                }
            }
        }
        else endTask();

        // Randomly determine duration from 0 - 500 millisols.
        duration = RandomUtil.getRandomDouble(500D);
    }

    /** Returns the weighted probability that a person might perform this task.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, Mars mars) {
        double result = 0D;

        // Find available lab for person.
        Lab lab = getAvailableLab(person);
        if (lab != null) result = 25D;
	    
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        return result;
    }

    /** Performs the mechanic task for a given amount of time.
     *  @param time amount of time to perform the task (in millisols)
     *  @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        // If person is incompacitated, end task.
        if (person.getPerformanceRating() == 0D) endTask();

        // Check for laboratory malfunction.
        if (malfunctions.hasMalfunction()) endTask();

        if (isDone()) {
            try {
                lab.removeResearcher();
            }
            catch (Exception e) {
                System.out.println("StudyRockSamples.performTask(): " + e.getMessage());
            }
            return timeLeft;
        }
	
        // Determine effective research time based on "Areology" skill.
        double researchTime = timeLeft;
        int areologySkill = person.getSkillManager().getEffectiveSkillLevel("Areology");
        if (areologySkill == 0) researchTime /= 2D;
        if (areologySkill > 1) researchTime += researchTime * (.2D * areologySkill);

        // Remove studied rock samples.
        double studied = RESEARCH_RATE * researchTime;
        inv.removeResource(Resource.ROCK_SAMPLES, studied);
	
        // Add experience to "Areology" skill.
        // (1 base experience point per 100 millisols of research time spent)
        // Experience points adjusted by person's "Academic Aptitude" attribute.
        double experience = timeLeft / 100D;
        experience += experience *
                (((double) person.getNaturalAttributeManager().getAttribute("Academic Aptitude") - 50D) / 100D);
        person.getSkillManager().addExperience("Areology", experience);

        // Keep track of the duration of this task.
        timeCompleted += time;
        if (timeCompleted >= duration) {
            endTask();
            try {
                lab.removeResearcher();
            }
            catch (Exception e) {
                System.out.println("StudyRockSamples.performTask(): " + e.getMessage());
            }
        }

        // Check for lab accident.
        checkForAccident(time);
	
        return 0D;
    }

    /**
     * Check for accident in laboratory.
     * @param time the amount of time researching (in millisols)
     */
    private void checkForAccident(double time) {

        double chance = .001D;

        // Areology skill modification.
        int skill = person.getSkillManager().getEffectiveSkillLevel("Areology");
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            // System.out.println(person.getName() + " has a lab accident while researching rock samples.");
	    
            if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
                Building building = (Building) lab;
                building.getMalfunctionManager().accident();
            }
            else if (person.getLocationSituation().equals(Person.INVEHICLE)) {
                person.getVehicle().getMalfunctionManager().accident(); 
            }
        }
    }
    
    /**
     * Gets an available lab that the person can use.
     * Returns null if no lab is currently available.
     *
     * @param person the person
     * @return available lab
     */
    private static Lab getAvailableLab(Person person) {
     
        Lab result = null;
     
        String location = person.getLocationSituation();
        if (location.equals(Person.INSETTLEMENT)) result = getSettlementLab(person, person.getSettlement());
        if (location.equals(Person.INVEHICLE)) result = getVehicleLab(person.getVehicle());
        
        return result;
    }
    
    /**
     * Gets an available lab in a settlement.
     * Returns null if no lab is currently available.
     *
     * @param person the person
     * @param settlement the settlement
     * @return available lab
     */
    private static Lab getSettlementLab(Person person, Settlement settlement) {
        
        Lab result = null;
        
        Inventory inv = settlement.getInventory();
        if (inv.getResourceMass(Resource.ROCK_SAMPLES) <= 0D) return null;
        
        List lablist = new ArrayList();
        Iterator i = settlement.getBuildingManager().getBuildings(Research.class).iterator();
        while (i.hasNext()) {
            Research lab = (Research) i.next();
            boolean malfunction = ((Building) lab).getMalfunctionManager().hasMalfunction();
            boolean labSpace = (lab.getResearcherNum() < lab.getLaboratorySize());
            if (!malfunction && labSpace) lablist.add(lab);
        }
        
        if (lablist.size() > 0) {
            // Pick random lab from list.
            int rand = RandomUtil.getRandomInt(lablist.size() - 1);
            result = (Lab) lablist.get(rand);
        }
        
        return result;
    }
    
    /**
     * Gets an available lab in a vehicle.
     * Returns null if no lab is currently available.
     *
     * @param vehicle the vehicle
     * @return available lab
     */
    private static Lab getVehicleLab(Vehicle vehicle) {
        
        Lab result = null;
        
        if (vehicle instanceof ExplorerRover) {
            ExplorerRover rover = (ExplorerRover) vehicle;
            Inventory inv = rover.getInventory();
            if (inv.getResourceMass(Resource.ROCK_SAMPLES) <= 0D) return null;
            
            Lab lab = rover.getLab();
            if (lab.getResearcherNum() == lab.getLaboratorySize()) {
                if (rover.getMalfunctionManager().hasMalfunction()) {
                    result = lab;
                }
            }
        }
        
        return result;
    }
}
