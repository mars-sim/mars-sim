/**
 * Mars Simulation Project
 * TendGreenhouse.java
 * @version 2.75 2004-01-15
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.malfunction.Malfunctionable;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.InhabitableBuilding;
import org.mars_sim.msp.simulation.structure.building.function.Farming;

/** The TendGreenhouse class is a task for tending the greenhouse in a settlement.
 *  It has the phases, "Planting", "Tending" and "Harvesting".
 *  This is an effort driven task.
 */
public class TendGreenhouse extends Task implements Serializable {

    // Data members
    private Farming greenhouse; // The greenhouse the person is tending.
    private Settlement settlement; // The settlement the greenhouse is in.
    private double duration; // The duration (in millisols) the person will perform the task.

    public TendGreenhouse(Person person, Mars mars) {
        // Use Task constructor
        super("Tending Greenhouse", person, true, false, mars);
        
        // Initialize data members
        description = "Tending Greenhouse at " + person.getSettlement().getName();
        
        // Get available greenhouse if any.
        greenhouse = getAvailableGreenhouse(person);
        
        if (greenhouse != null) {
            InhabitableBuilding building = (InhabitableBuilding) greenhouse;
            if (!building.containsPerson(person)) {
                try {
                    building.addPerson(person);
                }
                catch (BuildingException e) {
                    System.out.println("TendGreenhouse: " + e.getMessage());
                    endTask();
                }
            }
        }
        else endTask();
        
        // Randomly determine duration, from 0 - 500 millisols
        duration = RandomUtil.getRandomDouble(500D);
    }

    /** Returns the weighted probability that a person might perform this task.
     *  Returns a 25 probability if person is at a settlement.
     *  Returns a 0 if not.
     */
    public static double getProbability(Person person, Mars mars) {
        double result = 0D;
	    
        // See if there is an available greenhouse.
        if (getAvailableGreenhouse(person) != null) result = 25D;
        
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        return result;
    }

    /** Performs the tending greenhouse task for a given amount of time.
     *  @param time amount of time to perform the task (in millisols)
     *  @return amount of time remaining after finishing with task (in millisols)
     */
    double performTask(double time) {
        double timeLeft = super.performTask(time);

        if (subTask != null) return timeLeft;
        
        // If person is incompacitated, end task.
        if (person.getPerformanceRating() == 0D) {
            endTask();
            return timeLeft;
        }
        
        // Check if greenhouse has malfunction.
        if (((Building) greenhouse).getMalfunctionManager().hasMalfunction()) {
            endTask();
            return timeLeft;
        }
        
        // Determine amount of effective work time based on "Greenhouse Farming" skill.
        double workTime = timeLeft;
        int greenhouseSkill = person.getSkillManager().getEffectiveSkillLevel("Greenhouse Farming");
        if (greenhouseSkill == 0) workTime /= 2;
        else workTime += workTime * (.2D * (double) greenhouseSkill);
        
        // Add this work to the greenhouse.
        greenhouse.addWork(workTime);
        
        // Keep track of the duration of the task.
        timeCompleted += time;
        if (timeCompleted >= duration) endTask();

        // Add experience to "Greenhouse Farming" skill
        // (1 base experience point per 100 millisols of work)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double experience = timeLeft / 100D;
        double experienceAptitude = (double) person.getNaturalAttributeManager().getAttribute("Experience Aptitude");
        experience += experience * ((experienceAptitude - 50D) / 100D);
        person.getSkillManager().addExperience("Greenhouse Farming", experience);
        
        // Check for accident in greenhouse.
        checkForAccident(time);
	    
        return 0D;
    }

    /**
     * Check for accident in greenhouse.
     * @param time the amount of time working (in millisols)
     */
    private void checkForAccident(double time) {

        double chance = .001D;

        // Greenhouse farming skill modification.
        int skill = person.getSkillManager().getEffectiveSkillLevel("Greenhouse Farming");
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            // System.out.println(person.getName() + " has accident while tending the greenhouse.");
            
            ((Malfunctionable) greenhouse).getMalfunctionManager().accident();
        }
    }
    
    /** 
     * Gets the greenhouse the person is tending.
     * @return greenhouse
     */
    public Farming getGreenhouse() {
        return greenhouse;
    }
    
    /**
     * Gets an available greenhouse that the person can use.
     * Returns null if no greenhouse is currently available.
     *
     * @param person the person
     * @return available greenhouse
     */
    private static Farming getAvailableGreenhouse(Person person) {
     
        Farming result = null;
     
        String location = person.getLocationSituation();
        if (location.equals(Person.INSETTLEMENT)) {
            Settlement settlement = person.getSettlement();
            List farmlist = new ArrayList();
            Iterator i = settlement.getBuildingManager().getBuildings(Farming.class).iterator();
            while (i.hasNext()) {
                Farming farm = (Farming) i.next();
                boolean requiresWork = farm.requiresWork();
                boolean malfunction = ((Building) farm).getMalfunctionManager().hasMalfunction();   
                if (requiresWork && !malfunction) farmlist.add(farm);
            }
            
            if (farmlist.size() > 0) {
                // Pick random farm from list.
                int rand = RandomUtil.getRandomInt(farmlist.size() - 1);
                result = (Farming) farmlist.get(rand);
            }
        }
        
        return result;
    }
}
