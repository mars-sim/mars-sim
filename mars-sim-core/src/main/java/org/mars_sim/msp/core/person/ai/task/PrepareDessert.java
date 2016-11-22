/**
 * Mars Simulation Project
 * PrepareDessert.java
 * @version 3.08 2015-04-28
 * @author Manny Kung
 * 
 *   
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RoboticAttribute;
import org.mars_sim.msp.core.robot.ai.job.RobotJob;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;

/** 
 * The PrepareDessert class is a task for making dessert 
 */

public class PrepareDessert
extends Task
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(PrepareDessert.class.getName());

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.prepareDessert"); //$NON-NLS-1$
	
    /** Task phases. */
    private static final TaskPhase PREPARING_DESSERT = new TaskPhase(Msg.getString(
            "Task.phase.prepareDessert")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = -.2D;

	// Data members
	/** The kitchen the person is making dessert. */
	private PreparingDessert kitchen;

	/**
	 * Constructor.
	 * @param person the person performing the task.
	 * @throws Exception if error constructing task.
	 */
	public PrepareDessert(Person person) {
        // Use Task constructor
        super(NAME, person, true, false, STRESS_MODIFIER, false, 0D);

        // Initialize data members
        setDescription(Msg.getString("Task.description.prepareDessert.detail", 
                getDessertName())); //$NON-NLS-1$
        
        // Get an available dessert preparing kitchen.
        Building kitchenBuilding = getAvailableKitchen(person);
        
        if (kitchenBuilding != null) {
            kitchen = (PreparingDessert) kitchenBuilding.getFunction(BuildingFunction.PREPARING_DESSERT);
            
            // Walk to kitchen building.
            walkToActivitySpotInBuilding(kitchenBuilding, false);
            
            boolean isAvailable = kitchen.getAListOfDesserts().size() > 0;
            
            // Check if enough desserts have been prepared at the kitchen for this meal time.
            boolean enoughDessert = kitchen.getMakeNoMoreDessert();
	        
	        if (isAvailable && !enoughDessert) {
	                
		        // Set the chef name at the kitchen.
		        kitchen.setChef(person.getName());
		        
		        // Add task phase
		        addPhase(PREPARING_DESSERT);
		        setPhase(PREPARING_DESSERT);

		        String jobName = person.getMind().getJob().getName(person.getGender());
		        logger.finest(jobName + " " + person.getName() + " making dessert in " + kitchen.getBuilding().getNickName() + 
		                " at " + person.getParkedSettlement());
	        }
	        else {
	            // No dessert available or enough desserts have been prepared for now.
	            endTask();
	        }
        }
        else {
            // No dessert preparing kitchen available.
            endTask();
        }
    }
    
	public PrepareDessert(Robot robot) {
        // Use Task constructor
        super(NAME, robot, true, false, STRESS_MODIFIER, false, 0D);

        // Initialize data members
        setDescription(Msg.getString("Task.description.prepareDessert.detail", 
                getDessertName())); //$NON-NLS-1$
        
        // Get available kitchen if any.
        Building kitchenBuilding = getAvailableKitchen(robot);
        
        if (kitchenBuilding != null) {
            kitchen = (PreparingDessert) kitchenBuilding.getFunction(BuildingFunction.PREPARING_DESSERT);
            
            // Walk to kitchen building.
            walkToActivitySpotInBuilding(kitchenBuilding, false);

            boolean isAvailable = kitchen.getAListOfDesserts().size() > 0;
            
            // Check if enough desserts have been prepared at the kitchen for this meal time.
            boolean enoughDessert = kitchen.getMakeNoMoreDessert();
	        
	        if (isAvailable && !enoughDessert) {
	                
		        // Set the chef name at the kitchen.
		        kitchen.setChef(robot.getName());
		        
		        // Add task phase
		        addPhase(PREPARING_DESSERT);
		        setPhase(PREPARING_DESSERT);

		        String jobName = RobotJob.getName(robot.getRobotType());
		        logger.finest(jobName + " " + robot.getName() + " making dessert in " + kitchen.getBuilding().getNickName() + 
		                " at " + robot.getParkedSettlement());
	        }
	        else {
	            // No dessert available or enough has been prepared for now.
	            endTask();
	        }
        }
        else endTask();
    }
	
  	@Override
    protected BuildingFunction getRelatedBuildingFunction() {
        return BuildingFunction.PREPARING_DESSERT;
    }
  	
  	@Override
    protected BuildingFunction getRelatedBuildingRoboticFunction() {
        return BuildingFunction.PREPARING_DESSERT;
    }
    
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time the phase is to be performed.
     * @return the remaining time after the phase has been performed.
     */
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("The Preparing Desert task phase is null");
        }
        else if (PREPARING_DESSERT.equals(getPhase())) {
            return preparingDessertPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs the dessert making phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) left after performing the phase.
     */
    private double preparingDessertPhase(double time) {

        // If kitchen has malfunction, end task.
        if (kitchen.getBuilding().getMalfunctionManager().hasMalfunction()) {
            endTask();
            return time;
        }

		if (person != null) {
	        // If meal time is over, end task.
            if (!CookMeal.isMealTime(person.getCoordinates())) {
                logger.finest(person + " ending preparing desserts due to meal time over.");
                endTask();
                return time;
            }
            
            // If enough desserts have been prepared for this meal time, end task.
            if (kitchen.getMakeNoMoreDessert()) {
                logger.finest(person + " ending preparing desserts due to enough desserts prepared.");
                endTask();
                return time;
            }
		}
		else if (robot != null) {
		    // If meal time is over, end task.
            if (!CookMeal.isMealTime(robot)) {
                logger.finest(robot + " ending preparing desserts due to meal time over.");
                endTask();
                return time;
            }
            
            // If enough desserts have been prepared for this meal time, end task.
            if (kitchen.getMakeNoMoreDessert()) {
                logger.finest(robot + " ending preparing desserts due to enough desserts prepared.");
                endTask();
                return time;
            }
		}
        
		double workTime = time;      

		if (robot != null) {
		    // A robot moves slower than a person and incurs penalty on workTime
		    workTime = time/2;
		}
        
        // Determine amount of effective work time based on Cooking skill.
        int dessertMakingSkill = getEffectiveSkillLevel();
        if (dessertMakingSkill == 0) {
            workTime /= 2;
        }
        else {
            workTime += workTime * (.2D * (double) dessertMakingSkill);
        }
         
        // Add this work to the kitchen.
        kitchen.addWork(workTime);

        // Add experience
        addExperience(time);

        // Check for accident in kitchen.
        checkForAccident(time);

        return 0D;
    }

    /**
     * Adds experience to the person's skills used in this task.
     * @param time the amount of time (ms) the person performed this task.
     */
    protected void addExperience(double time) {
        // Add experience to cooking skill
        // (1 base experience point per 25 millisols of work)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double newPoints = time / 25D;
        int experienceAptitude = 0;
        
		if (person != null) {
	        experienceAptitude = person.getNaturalAttributeManager().getAttribute(
	                NaturalAttribute.EXPERIENCE_APTITUDE);	    
		}
		else if (robot != null) {
			experienceAptitude = robot.getRoboticAttributeManager().getAttribute(
                RoboticAttribute.EXPERIENCE_APTITUDE);
		}
        
        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        
		if (person != null) {
		    person.getMind().getSkillManager().addExperience(SkillType.COOKING, newPoints);	
		}
		else if (robot != null) {
        	robot.getBotMind().getSkillManager().addExperience(SkillType.COOKING, newPoints);
		}
    }

    /**
     * Gets the kitchen the person is making desserts.
     * @return kitchen
     */
    public PreparingDessert getKitchen() {
        return kitchen;
    }

    /**
     * Check for accident in kitchen.
     * @param time the amount of time working (in millisols)
     */
    private void checkForAccident(double time) {

        double chance = .001D;
        int skill = 0;
        // cooking skill modification.
		if (person != null) {
		    skill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.COOKING);	
		}
		else if (robot != null) {
        	skill = robot.getBotMind().getSkillManager().getEffectiveSkillLevel(SkillType.COOKING);
		}
        
        if (skill <= 3) {
            chance *= (4 - skill);
        }
        else {
            chance /= (skill - 2);
        }

        // Modify based on the kitchen building's wear condition.
        chance *= kitchen.getBuilding().getMalfunctionManager().getWearConditionAccidentModifier();

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            if (person != null) {
                logger.fine(person.getName() + " has accident while making dessert");
            }
            else if (robot != null) {
                logger.fine(robot.getName() + " has accident while making dessert");
            }
            kitchen.getBuilding().getMalfunctionManager().accident();
        }
    }	

    /**
     * Gets the name of dessert the chef is making based on the time.
     * @return result
     */
    //TODO: May change to specific products such as Soymilk, Soybean oil and Tofu in near future
    private String getDessertName() {        
    	String result = "Dessert";
        return result;
    }

    /**
     * Gets an available kitchen building at the person's settlement.
     * @param person the person to check for.
     * @return kitchen building or null if none available.
     */
    public static Building getAvailableKitchen(Person person) {
        Building result = null;
        
        LocationSituation location = person.getLocationSituation();	  			
        if (location == LocationSituation.IN_SETTLEMENT) {
            BuildingManager manager = person.getParkedSettlement().getBuildingManager();
            List<Building> kitchenBuildings = manager.getBuildings(BuildingFunction.PREPARING_DESSERT);
            kitchenBuildings = BuildingManager.getNonMalfunctioningBuildings(kitchenBuildings);
            kitchenBuildings = getKitchensNeedingCooks(kitchenBuildings);
            kitchenBuildings = BuildingManager.getLeastCrowdedBuildings(kitchenBuildings); 

            if (kitchenBuildings.size() > 0) {
            	
                Map<Building, Double> kitchenBuildingProbs = BuildingManager.getBestRelationshipBuildings(
                        person, kitchenBuildings);
                
                result = RandomUtil.getWeightedRandomObject(kitchenBuildingProbs);
            }
        }		

        return result;
    }
    
    public static Building getAvailableKitchen(Robot robot) {
        Building result = null;

        LocationSituation location = robot.getLocationSituation();
        if (location == LocationSituation.IN_SETTLEMENT) {
            BuildingManager manager = robot.getParkedSettlement().getBuildingManager();
            List<Building> kitchenBuildings = manager.getBuildings(BuildingFunction.PREPARING_DESSERT);
            kitchenBuildings = BuildingManager.getNonMalfunctioningBuildings(kitchenBuildings);
            kitchenBuildings = getKitchensNeedingCooks(kitchenBuildings);
			if (RandomUtil.getRandomInt(2) == 0) // robot is not as inclined to move around
				kitchenBuildings = BuildingManager.getLeastCrowded4BotBuildings(kitchenBuildings); 

            if (kitchenBuildings.size() > 0) {
               // Map<Building, Double> kitchenBuildingProbs = BuildingManager.getBestRelationshipBuildings(
                //        robot, kitchenBuildings);
               // result = RandomUtil.getWeightedRandomObject(kitchenBuildingProbs);
                int selected = RandomUtil.getRandomInt(kitchenBuildings.size()-1);
            	result = kitchenBuildings.get(selected);	
            }
        }		

        return result;
    }

    /**
     * Gets a list of kitchen buildings that have room for more cooks.
     * @param kitchenBuildings list of kitchen buildings
     * @return list of kitchen buildings
     * @throws BuildingException if error
     */
    private static List<Building> getKitchensNeedingCooks(List<Building> kitchenBuildings) {
        List<Building> result = new ArrayList<Building>();

        if (kitchenBuildings != null) {
            Iterator<Building> i = kitchenBuildings.iterator();
            while (i.hasNext()) {
                Building building = i.next();
                PreparingDessert kitchen = (PreparingDessert) building.getFunction(BuildingFunction.PREPARING_DESSERT);
                if (kitchen.getNumCooks() < kitchen.getCookCapacity()) {
                    result.add(building);
                }
            }
        }

        return result;
    }

    @Override
    public int getEffectiveSkillLevel() {
    	
        SkillManager manager = null;
		if (person != null) {
	        manager = person.getMind().getSkillManager();
		}
		else if (robot != null) {
			manager = robot.getBotMind().getSkillManager();
		}
        
        return manager.getEffectiveSkillLevel(SkillType.COOKING);
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(1);
        results.add(SkillType.COOKING);
        return results;
    }

    @Override
    public void destroy() {
        super.destroy();

        kitchen = null;
    }
}