/**
 * Mars Simulation Project
 * TendGreenhouse.java
 * @version 3.07 2015-01-06
 * @author Scott Davis
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
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RoboticAttribute;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Crop;
import org.mars_sim.msp.core.structure.building.function.Farming;

/** 
 * The TendGreenhouse class is a task for tending the greenhouse in a settlement.
 * This is an effort driven task.
 */
public class TendGreenhouse
extends Task
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(TendGreenhouse.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.tendGreenhouse"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase TENDING = new TaskPhase(Msg.getString(
            "Task.phase.tending")); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase INSPECTING_CROP = new TaskPhase(Msg.getString(
            "Task.phase.inspecting")); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase CLEANING = new TaskPhase(Msg.getString(
            "Task.phase.cleaning")); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase CHECKING_ON_EQUIPMENT = new TaskPhase(Msg.getString(
            "Task.phase.checkingOnEquipment")); //$NON-NLS-1$

    // Static members
    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = -.1D;

    // Data members
    /** The greenhouse the person is tending. */
    private Farming greenhouse;

    /**
     * Constructor.
     * @param person the person performing the task.
     */
    public TendGreenhouse(Person person) {
        // Use Task constructor
        super(NAME, person, false, false, STRESS_MODIFIER, true, 
                10D + RandomUtil.getRandomDouble(50D));

        // Initialize data members
        if (person.getSettlement() != null) {
            setDescription(Msg.getString("Task.description.tendGreenhouse.detail", 
                    person.getSettlement().getName())); //$NON-NLS-1$
        }
        else {
            endTask();
        }

        // Get available greenhouse if any.
        Building farmBuilding = getAvailableGreenhouse(person);
        if (farmBuilding != null) {
            greenhouse = (Farming) farmBuilding.getFunction(BuildingFunction.FARMING);

            // Walk to greenhouse.
            walkToActivitySpotInBuilding(farmBuilding, false);
        }
        else {
            endTask();
        }

        // Initialize phase
        addPhase(TENDING);
        addPhase(INSPECTING_CROP);
        addPhase(CLEANING);
        addPhase(CHECKING_ON_EQUIPMENT);
        setPhase(TENDING);
    }

    /**
     * Constructor 2.
     * @param robot the robot performing the task.
     */
    public TendGreenhouse(Robot robot) {
        // Use Task constructor
        super(NAME, robot, false, false, 0, true, 
                10D + RandomUtil.getRandomDouble(50D));

        // Initialize data members
        if (robot.getSettlement() != null) {
            setDescription(Msg.getString("Task.description.tendGreenhouse.detail", 
            		robot.getSettlement().getName())); //$NON-NLS-1$
        }
        else {
            endTask();
        }

        // Get available greenhouse if any.
        Building farmBuilding = getAvailableGreenhouse(robot);
        if (farmBuilding != null) {
            greenhouse = (Farming) farmBuilding.getFunction(BuildingFunction.FARMING);

            // Walk to greenhouse.
            walkToActivitySpotInBuilding(farmBuilding, false);
        }
        else {
            endTask();
        }

        // Initialize phase
        addPhase(TENDING);
        addPhase(INSPECTING_CROP);
        addPhase(CLEANING);
        addPhase(CHECKING_ON_EQUIPMENT);
        setPhase(TENDING);
    }
    
    @Override
    protected BuildingFunction getRelatedBuildingFunction() {
        return BuildingFunction.FARMING;
    }
    
    protected BuildingFunction getRelatedBuildingRoboticFunction() {
        return BuildingFunction.FARMING;
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }     
        else if (TENDING.equals(getPhase())) {
            return tendingPhase(time);
        }
        else if (INSPECTING_CROP.equals(getPhase())) {
            return inspectingPhase(time);
        }
        else if (CLEANING.equals(getPhase())) {
            return cleaningPhase(time);
        }
        else if (CHECKING_ON_EQUIPMENT.equals(getPhase())) {
            return checkingPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs the tending phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double tendingPhase(double time) {

        // Check if greenhouse has malfunction.
        if (greenhouse.getBuilding().getMalfunctionManager().hasMalfunction()) {
            endTask();
            return time;
        }

    	int rand = RandomUtil.getRandomInt(3);
    	
    	if (rand == 0) { 		
       		//System.out.println("0: setPhase(INSPECTING_CROP)");
    		setPhase(INSPECTING_CROP);
    		return time;
    	}
    	else if (rand == 1) {
       		//System.out.println("1: setPhase(CLEANING)");
    		setPhase(CLEANING);
    		return time;
    	}
    	else if (rand == 2) {
       		//System.out.println("2: setPhase(CHECKING_ON_EQUIPMENT)");
    		setPhase(CHECKING_ON_EQUIPMENT);
    		return time;
    	}
    	else  { //if (rand == 3) {
    		//System.out.println("3: continue to tend");
  
	        double workTime = 0;
	        double factor = 2D;
	        
			if (person != null) {			
		        workTime = time * factor;
			}
			else if (robot != null) {
			     // TODO: how to lengthen the work time for a robot even though it moves slower than a person 
				// should it incurs penalty on workTime?
				workTime = time * factor;
			}
	
	        // Determine amount of effective work time based on "Botany" skill
	        int greenhouseSkill = getEffectiveSkillLevel();
	        if (greenhouseSkill == 0) {
	            workTime /= 2;
	        }
	        else {
	            workTime += workTime * (double) greenhouseSkill;
	        }
	
	        // Add this work to the greenhouse.
	        greenhouse.addWork(workTime);
	        //System.out.println("TendGreenhouse : just greenhouse.addWork(workTime) ");
	        
	        // Add experience
	        addExperience(time);
	
	        // Check for accident in greenhouse.
	        checkForAccident(time);
	    	
	        return 0D;
    	}
    
    }

    /**
     * Performs the inspecting phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double inspectingPhase(double time) {
    	double remaingTime = 0;
    	
   		//System.out.println("inspectingPhase");
   		
        double workTime = 0;
        double factor = 1.4;//2D;
        
		if (person != null) {			
	        workTime = time * factor;
		}
		else if (robot != null) {
		     // TODO: how to lengthen the work time for a robot even though it moves slower than a person 
			// should it incurs penalty on workTime?
			workTime = time * factor;
		}

        // Determine amount of effective work time based on "Botany" skill
        int greenhouseSkill = getEffectiveSkillLevel();
        if (greenhouseSkill == 0) {
            workTime /= 2;
        }
        else {
            workTime += workTime * (double) greenhouseSkill;
        }

        // Add this work to the greenhouse.
        greenhouse.addWork(workTime);
        //System.out.println("TendGreenhouse : just greenhouse.addWork(workTime) ");
        
        // Add experience
        addExperience(time);

        // Check for accident in greenhouse.
        checkForAccident(time);
    	
    	
    	return remaingTime;
    }
    
    /**
     * Performs the cleaning phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double cleaningPhase(double time) {
    	double remaingTime = 0;
  
  		//System.out.println("cleaningPhase");
  		 
        double workTime = 0;
        double factor = 1.5;//2D;
        
		if (person != null) {			
	        workTime = time * factor;
		}
		else if (robot != null) {
		     // TODO: how to lengthen the work time for a robot even though it moves slower than a person 
			// should it incurs penalty on workTime?
			workTime = time * factor;
		}

        // Determine amount of effective work time based on "Botany" skill
        int greenhouseSkill = getEffectiveSkillLevel();
        if (greenhouseSkill == 0) {
            workTime /= 2;
        }
        else {
            workTime += workTime * (double) greenhouseSkill;
        }

        // Add this work to the greenhouse.
        greenhouse.addWork(workTime);
        //System.out.println("TendGreenhouse : just greenhouse.addWork(workTime) ");
        
        // Add experience
        addExperience(time);

        // Check for accident in greenhouse.
        checkForAccident(time);
    	
    	return remaingTime;
    }
    
    /**
     * Performs the checking phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double checkingPhase(double time) {
      	double remaingTime = 0;
  
  		//System.out.println("checkingPhase");
  		 
        double workTime = 0;
        double factor = 1.3;//2D;
        
		if (person != null) {			
	        workTime = time * factor;
		}
		else if (robot != null) {
		     // TODO: how to lengthen the work time for a robot even though it moves slower than a person 
			// should it incurs penalty on workTime?
			workTime = time * factor;
		}

        // Determine amount of effective work time based on "Botany" skill
        int greenhouseSkill = getEffectiveSkillLevel();
        if (greenhouseSkill == 0) {
            workTime /= 2;
        }
        else {
            workTime += workTime * (double) greenhouseSkill;
        }

        // Add this work to the greenhouse.
        greenhouse.addWork(workTime);
        //System.out.println("TendGreenhouse : just greenhouse.addWork(workTime) ");
        
        // Add experience
        addExperience(time);

        // Check for accident in greenhouse.
        checkForAccident(time);
    	
    	return remaingTime;
    }
    
    @Override
    protected void addExperience(double time) {
        // Add experience to "Botany" skill
        // (1 base experience point per 100 millisols of work)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double newPoints = time / 100D;
        int experienceAptitude = 0;
		if (person != null) 
	        experienceAptitude = person.getNaturalAttributeManager().getAttribute(NaturalAttribute.EXPERIENCE_APTITUDE);
		else if (robot != null) 
	        experienceAptitude = robot.getRoboticAttributeManager().getAttribute(RoboticAttribute.EXPERIENCE_APTITUDE);

        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
        newPoints *= getTeachingExperienceModifier();
		if (person != null) 
	        person.getMind().getSkillManager().addExperience(SkillType.BOTANY, newPoints);
		else if (robot != null) 
	        robot.getBotMind().getSkillManager().addExperience(SkillType.BOTANY, newPoints);

    }

    
    /**
     * Check for accident in greenhouse.
     * @param time the amount of time working (in millisols)
     */
    private void checkForAccident(double time) {

        double chance = .001D;

        // Greenhouse farming skill modification.
        int skill = getEffectiveSkillLevel();
        if (skill <= 3) {
            chance *= (4 - skill);
        }
        else {
            chance /= (skill - 2);
        }

        // Modify based on the wear condition.
        chance *= greenhouse.getBuilding().getMalfunctionManager().getWearConditionAccidentModifier();

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            // logger.info(person.getName() + " has accident while tending the greenhouse.");
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
     * @param person the person
     * @return available greenhouse
     */
    public static Building getAvailableGreenhouse(Unit unit) {
        Building result = null;
        Person person = null;
        Robot robot = null;
        
        if (unit instanceof Person) {
         	person = (Person) unit;
            LocationSituation location = person.getLocationSituation();
            if (location == LocationSituation.IN_SETTLEMENT) {
                BuildingManager manager = person.getSettlement().getBuildingManager();
                List<Building> farmBuildings = manager.getBuildings(BuildingFunction.FARMING);
                farmBuildings = BuildingManager.getNonMalfunctioningBuildings(farmBuildings);
                farmBuildings = getFarmsNeedingWork(farmBuildings);
                farmBuildings = BuildingManager.getLeastCrowdedBuildings(farmBuildings);

                if (farmBuildings.size() > 0) {
                    Map<Building, Double> farmBuildingProbs = BuildingManager.getBestRelationshipBuildings(
                            person, farmBuildings);
                    result = RandomUtil.getWeightedRandomObject(farmBuildingProbs);
                }
            }
        }
        else if (unit instanceof Robot) {
        	robot = (Robot) unit;
            LocationSituation location = robot.getLocationSituation();
            if (location == LocationSituation.IN_SETTLEMENT) {
                BuildingManager manager = robot.getSettlement().getBuildingManager();
                List<Building> buildings = manager.getBuildings(BuildingFunction.FARMING);
                buildings = BuildingManager.getNonMalfunctioningBuildings(buildings);
                buildings = getFarmsNeedingWork(buildings);
                //buildings = BuildingManager.getLeastCrowdedBuildings(buildings);

                // TODO: add person's good/bad feeling toward robots
                int size = buildings.size();
                //System.out.println("size is "+size);
                int selected = 0; 
                if (size == 0) 
                	result = null;
                if (size >= 1) {
                	selected = RandomUtil.getRandomInt(size-1);         
                	result = buildings.get(selected);
                }
                //System.out.println("getAvailableGreenhouse() : selected is "+selected); 
            }
        }
        return result;
    }

    /**
     * Gets a list of farm buildings needing work from a list of buildings with the farming function.
     * @param buildingList list of buildings with the farming function.
     * @return list of farming buildings needing work.
     */
    private static List<Building> getFarmsNeedingWork(List<Building> buildingList) {
        List<Building> result = new ArrayList<Building>();

        Iterator<Building> i = buildingList.iterator();
        while (i.hasNext()) {
            Building building = i.next();
            Farming farm = (Farming) building.getFunction(BuildingFunction.FARMING);
            if (farm.requiresWork()) {
                result.add(building);
            }
        }

        return result;
    }

    /**
     * Gets the number of crops that currently need work this Sol.
     * @param settlement the settlement.
     * @return number of crops.
     */
    public static int getCropsNeedingTending(Settlement settlement) {

        int result = 0;

        BuildingManager manager = settlement.getBuildingManager();
        Iterator<Building> i = manager.getBuildings(BuildingFunction.FARMING).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            Farming farm = (Farming) building.getFunction(BuildingFunction.FARMING);
            Iterator<Crop> j = farm.getCrops().iterator();
            while (j.hasNext()) {
                Crop crop = j.next();
                if (crop.requiresWork()) {
                    result++;
                }
            }
        }
        //System.out.println("getCropsNeedingTending() : result is " + result); 
        return result;
    }

    @Override
    //TODO: get agility score of a person/robot
    public int getEffectiveSkillLevel() {
    	SkillManager manager = null;
    
		if (person != null) 
			manager = person.getMind().getSkillManager();
		else if (robot != null) 
			manager = robot.getBotMind().getSkillManager();
        
        return manager.getEffectiveSkillLevel(SkillType.BOTANY);
    }  

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(1);
        results.add(SkillType.BOTANY);
        return results;
    }

    @Override
    public void destroy() {
        super.destroy();

        greenhouse = null;
    }
}