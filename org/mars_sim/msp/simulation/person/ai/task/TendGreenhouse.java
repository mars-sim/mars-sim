/**
 * Mars Simulation Project
 * TendGreenhouse.java
 * @version 2.77 2004-08-16
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;

/** 
 * The TendGreenhouse class is a task for tending the greenhouse in a settlement.
 * It has the phases, "Planting", "Tending" and "Harvesting".
 * This is an effort driven task.
 */
public class TendGreenhouse extends Task implements Serializable {

	// Static members
	private static final double STRESS_MODIFIER = -.1D; // The stress modified per millisol.

    // Data members
    private Farming greenhouse; // The greenhouse the person is tending.
    private Settlement settlement; // The settlement the greenhouse is in.
    private double duration; // The duration (in millisols) the person will perform the task.

    public TendGreenhouse(Person person) {
        // Use Task constructor
        super("Tending Greenhouse", person, true, false, STRESS_MODIFIER);
        
        // Initialize data members
        description = "Tending Greenhouse at " + person.getSettlement().getName();
        
        // Get available greenhouse if any.
        try {
        	Building farmBuilding = getAvailableGreenhouse(person);
        	if (farmBuilding != null) {
        		greenhouse = (Farming) farmBuilding.getFunction(Farming.NAME);
        		BuildingManager.addPersonToBuilding(person, farmBuilding);
        	}
        	else endTask();
        }
        catch (BuildingException e) {
			System.err.println("TendGreenhouse: " + e.getMessage());
			endTask();
        }
        
        // Randomly determine duration, from 0 - 500 millisols
        duration = RandomUtil.getRandomDouble(500D);
    }

	/** 
	 * Returns the weighted probability that a person might perform this task.
	 * @param person the person to perform the task
	 * @return the weighted probability that a person might perform this task
	 */
    public static double getProbability(Person person) {
        double result = 0D;
        
        try {
			// See if there is an available greenhouse.
        	Building farmingBuilding = getAvailableGreenhouse(person);
        	if (farmingBuilding != null) {
        		result = 75D;
        		
        		// Crowding modifier.
        		result *= Task.getCrowdingProbabilityModifier(person, farmingBuilding);
        	}
        }
        catch (BuildingException e) {
        	System.err.println("TendGreenhouse.getProbability(): " + e.getMessage());
        }
        
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

		// Job modifier.
		result *= person.getMind().getJob().getStartTaskProbabilityModifier(TendGreenhouse.class);

        return result;
    }

    /** 
     * Performs the tending greenhouse task for a given amount of time.
     * @param time amount of time to perform the task (in millisols)
     * @return amount of time remaining after finishing with task (in millisols)
     * @throws Exception if error in performing task.
     */
    double performTask(double time) throws Exception {
        double timeLeft = super.performTask(time);

        if (subTask != null) return timeLeft;
        
        // If person is incompacitated, end task.
        if (person.getPerformanceRating() == 0D) {
            endTask();
            return timeLeft;
        }
        
        // Check if greenhouse has malfunction.
        if (greenhouse.getBuilding().getMalfunctionManager().hasMalfunction()) {
            endTask();
            return timeLeft;
        }
        
        // Determine amount of effective work time based on "Botany" skill.
        double workTime = timeLeft;
        int greenhouseSkill = person.getSkillManager().getEffectiveSkillLevel(Skill.BOTANY);
        if (greenhouseSkill == 0) workTime /= 2;
        else workTime += workTime * (.2D * (double) greenhouseSkill);
        
        // Add this work to the greenhouse.
        try {
        	greenhouse.addWork(workTime);
        }
        catch (Exception e) {
        	throw new Exception("TendGreenhouse.performTask(): Adding work to greenhouse: " + e.getMessage());
        }
        
        // Keep track of the duration of the task.
        timeCompleted += time;
        if (timeCompleted >= duration) endTask();

        // Add experience to "Botany" skill
        // (1 base experience point per 100 millisols of work)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double experience = timeLeft / 100D;
        double experienceAptitude = (double) person.getNaturalAttributeManager().getAttribute("Experience Aptitude");
        experience += experience * ((experienceAptitude - 50D) / 100D);
        experience *= getTeachingExperienceModifier();
        person.getSkillManager().addExperience(Skill.BOTANY, experience);
        
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
        int skill = person.getSkillManager().getEffectiveSkillLevel(Skill.BOTANY);
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            // System.out.println(person.getName() + " has accident while tending the greenhouse.");
            greenhouse.getBuilding().getMalfunctionManager().accident();
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
     * @throws BuildingException if error finding farm building.
     */
    private static Building getAvailableGreenhouse(Person person) throws BuildingException {
     
        Building result = null;
     
        String location = person.getLocationSituation();
        if (location.equals(Person.INSETTLEMENT)) {
        	BuildingManager manager = person.getSettlement().getBuildingManager();
            List farmBuildings = manager.getBuildings(Farming.NAME);
			farmBuildings = BuildingManager.getNonMalfunctioningBuildings(farmBuildings);
			farmBuildings = getFarmsNeedingWork(farmBuildings);
			farmBuildings = BuildingManager.getLeastCrowdedBuildings(farmBuildings); 
			
			if (farmBuildings.size() > 0) {
				// Pick random farm from list.
				int rand = RandomUtil.getRandomInt(farmBuildings.size() - 1);
				result = (Building) farmBuildings.get(rand);
			}
        }
        
        return result;
    }
    
    /**
     * Gets a list of farm buildings needing work from a list of buildings with the farming function.
     * @param buildingList list of buildings with the farming function.
     * @return list of farming buildings needing work.
     * @throws BuildingException if any buildings in building list don't have the farming function.
     */
    private static List getFarmsNeedingWork(List buildingList) throws BuildingException {
    	List result = new ArrayList();
    	
    	Iterator i = buildingList.iterator();
    	while (i.hasNext()) {
    		Building building = (Building) i.next();
    		Farming farm = (Farming) building.getFunction(Farming.NAME);
    		if (farm.requiresWork()) result.add(building);
    	}
    	
    	return result;
    }
    
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getSkillManager();
		return manager.getEffectiveSkillLevel(Skill.BOTANY);
	}  
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List getAssociatedSkills() {
		List results = new ArrayList();
		results.add(Skill.BOTANY);
		return results;
	}
}