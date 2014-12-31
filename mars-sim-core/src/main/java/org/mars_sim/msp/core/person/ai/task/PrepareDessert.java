/**
 * Mars Simulation Project
 * PrepareDessert.java
 * @version 3.07 2014-12-30
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
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;

/** 
 * The PrepareDessert class is a task for making dessert 
 */
// 2014-11-06 Note that SkillType stays the same as COOKING
// 2014-11-28 Changed Class name from MakeSoy to PrepareDessert
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
	private static final double STRESS_MODIFIER = -.1D;

	// Starting time (millisol) for making soy product in 0 degrees longitude.
	private static final double AFTERNOON_START = 650D;

    private static final double MORNING_START = 400D;
    
 
	// Time (millisols) duration.
	private static final double DURATION = 80D;

	// Data members
	/** The kitchen the person is making soymmlk. */
	private PreparingDessert kitchen;

	/**
	 * Constructor.
	 * @param person the person performing the task.
	 * @throws Exception if error constructing task.
	 */
	public PrepareDessert(Person person) {
        // Use Task constructor
        super(NAME, person, true, false, STRESS_MODIFIER, false, 0D);

        // logger.info("just called MakeSoy's constructor");

        // Initialize data members
        setDescription(Msg.getString("Task.description.prepareDessert.detail", 
                getSoyProductName())); //$NON-NLS-1$
        
        // Get available kitchen if any.
        Building kitchenBuilding = getAvailableKitchen(person);
        
        if (kitchenBuilding != null) {
            kitchen = (PreparingDessert) kitchenBuilding.getFunction(BuildingFunction.PREPARING_DESSERT);
            // Walk to kitchen building.
            walkToActivitySpotInBuilding(kitchenBuilding);
        }
        else endTask();

        // 2014-12-30 Added sugarcaneJuiceAvailable
        double soymilkAvailable = kitchen.checkAmountAV("Soymilk");
        double sugarcaneJuiceAvailable = kitchen.checkAmountAV("Sugarcane Juice");
        if (soymilkAvailable < 0.5 && sugarcaneJuiceAvailable < 0.5) {
            logger.severe("less than 0.5 kg soymilk and sugarcane juice left!");
        	endTask();
        } else  {
                
	        // Add task phase
	        addPhase(PREPARING_DESSERT);
	        setPhase(PREPARING_DESSERT);
	
	        String jobName = person.getMind().getJob().getName(person.getGender());
	        logger.finest(jobName + " " + person.getName() + " making dessert in " + kitchen.getBuilding().getName() + 
	                " at " + person.getSettlement());
        }
    }
    
  	@Override
    protected BuildingFunction getRelatedBuildingFunction() {
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
     * Performs the soy product making phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) left after performing the phase.
     */
    private double preparingDessertPhase(double time) {

        // If kitchen has malfunction, end task.
        if (kitchen.getBuilding().getMalfunctionManager().hasMalfunction()) {
            endTask();
            return time;
        }

        if (!isDessertTime(person)) {
            endTask();
            kitchen.cleanup();
            return time;
        }

        // Determine amount of effective work time based on "MakingSoy" skill.
        double workTime = time;
        int soyMakingSkill = getEffectiveSkillLevel();
        if (soyMakingSkill == 0) workTime /= 2;
        else workTime += workTime * (.2D * (double) soyMakingSkill);

        // round off to 2 decimal places
        double roundOffWorkTime = Math.round(workTime * 100.0) / 100.0;
         
        // Add this work to the kitchen.
        kitchen.addWork(roundOffWorkTime);

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
        // Add experience to "MakingSoy" skill
        // (1 base experience point per 25 millisols of work)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double newPoints = time / 25D;
        int experienceAptitude = person.getNaturalAttributeManager().getAttribute(
                NaturalAttribute.EXPERIENCE_APTITUDE);
        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
        newPoints *= getTeachingExperienceModifier();
        person.getMind().getSkillManager().addExperience(SkillType.COOKING, newPoints);
    }

    /**
     * Gets the kitchen the person is making soy products.
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

        // MakingSoy skill modification.
        int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.COOKING);
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        // Modify based on the kitchen building's wear condition.
        chance *= kitchen.getBuilding().getMalfunctionManager().getWearConditionAccidentModifier();

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            // logger.info(person.getName() + " has accident while making soy products.");
            kitchen.getBuilding().getMalfunctionManager().accident();
        }
    }	

    /**
     * Checks if it is currently the time at the chef's location.
     * @param person the person to check for.
     * @return true if it is soy product making time
     */
    public static boolean isDessertTime(Person person) {
        boolean result = false;

        double timeOfDay = Simulation.instance().getMasterClock().getMarsClock().getMillisol();
        double timeDiff = 1000D * (person.getCoordinates().getTheta() / (2D * Math.PI));
        double modifiedTime = timeOfDay + timeDiff;
        if (modifiedTime >= 1000D) {
            modifiedTime -= 1000D;
        }

        if ((modifiedTime >= MORNING_START) && (modifiedTime <= (MORNING_START + DURATION))) {
            result = true;
        }        
        if ((modifiedTime >= AFTERNOON_START) && (modifiedTime <= (AFTERNOON_START + DURATION))) {
        	//logger.info("isSoyTime() : Yes it's time for making soy products!");
        	result = true;
        }

        
        return result;
    }

    /**
     * Gets the name of soy product the chef is making based on the time.
     * @return "Soymilk"
     */
    //TODO: May change to specific products such as Soymilk, Soybean oil and Tofu in near future
    private String getSoyProductName() {        
    	String result = "Soymilk";
        return result;
    }

    /**
     * Gets an available kitchen at the person's settlement.
     * @param person the person to check for.
     * @return kitchen or null if none available.
     */
    public static Building getAvailableKitchen(Person person) {
        Building result = null;

        LocationSituation location = person.getLocationSituation();
        if (location == LocationSituation.IN_SETTLEMENT) {
            BuildingManager manager = person.getSettlement().getBuildingManager();
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
                if (kitchen.getNumCooks() < kitchen.getCookCapacity()) result.add(building);
            }
        }

        return result;
    }

    @Override
    public int getEffectiveSkillLevel() {
        SkillManager manager = person.getMind().getSkillManager();
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