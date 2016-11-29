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
import org.mars_sim.msp.core.structure.building.function.farming.Crop;
import org.mars_sim.msp.core.structure.building.function.farming.CropType;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;
import org.mars_sim.msp.core.tool.Conversion;

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
    private static final TaskPhase INSPECTING = new TaskPhase(Msg.getString(
            "Task.phase.inspecting")); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase CLEANING = new TaskPhase(Msg.getString(
            "Task.phase.cleaning")); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase SAMPLING = new TaskPhase(Msg.getString(
            "Task.phase.sampling")); //$NON-NLS-1$

    // Static members
    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = -.1D;

    // Data members
    /** The greenhouse the person is tending. */
    private Farming greenhouse;

    //private SkillManager skillManager;
    
    /**
     * Constructor.
     * @param person the person performing the task.
     */
    public TendGreenhouse(Person person) {
        // Use Task constructor
        super(NAME, person, false, false, STRESS_MODIFIER, true, 
                10D + RandomUtil.getRandomDouble(50D));
        // Initialize data members
        if (person.getParkedSettlement() != null) {
       //     setDescription(Msg.getString("Task.description.tendGreenhouse"));//.detail", 
                    //person.getParkedSettlement().getName())); //$NON-NLS-1$
        }
        else {
            endTask();
        }

		//skillManager = person.getMind().getSkillManager();

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
        addPhase(INSPECTING);
        addPhase(CLEANING);
        addPhase(SAMPLING);
        addPhase(TENDING);
        
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
        if (robot.getParkedSettlement() != null) {
        //    setDescription(Msg.getString("Task.description.tendGreenhouse"));//
            		//robot.getParkedSettlement().getName())); //$NON-NLS-1$
        }
        else {
            endTask();
        }

		//skillManager = robot.getBotMind().getSkillManager();
        
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
        addPhase(INSPECTING);
        addPhase(CLEANING);
        addPhase(SAMPLING);
        addPhase(TENDING);
        
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
        else if (INSPECTING.equals(getPhase())) {
            return inspectingPhase(time);
        }
        else if (CLEANING.equals(getPhase())) {
            return cleaningPhase(time);
        }
        else if (SAMPLING.equals(getPhase())) {
            return samplingPhase(time);
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

        double workTime = 0, remainingTime = 0;
        
        // Check if greenhouse has malfunction.
        if (greenhouse.getBuilding().getMalfunctionManager().hasMalfunction()) {
            //endTask();
            return 0;
        }

    	int rand = RandomUtil.getRandomInt(19);
    	
    	if (rand == 0) { 		
       		//System.out.println("0: setPhase(INSPECTING)");
    		setPhase(INSPECTING);
            //endTask();
            return 0;
    	}
    	else if (rand == 1) {
       		//System.out.println("1: setPhase(CLEANING)");
    		setPhase(CLEANING);
            //endTask();    		
    		return 0;
    	}
    	else if (rand == 2) {
       		//System.out.println("2: setPhase(CHECKING_ON_EQUIPMENT)");
    		setPhase(SAMPLING);
            //endTask();    		
    		return 0;
    	}
    	else  {
 
    		// 85% of the change for rand == 3 to 9 	
    		//setPhase(TENDING);
/*    		  	
    		// Obtain a needy crop to work on
    		Crop needyCrop = greenhouse.getNeedyCrop();
        	//System.out.println("1: needyCrop is " + needyCrop.getCropType().getName());
    		
    		if (needyCrop == null) {
	        	List<Crop> list = greenhouse.getCrops();
	        	int size = list.size();
	        	int rand1 = RandomUtil.getRandomInt(0, size-1);	        	
	        	needyCrop = list.get(rand1);    
	
	        	//System.out.println("2: needyCrop is " + needyCrop.getCropType().getName());
    		}
        	
       		if (needyCrop != null) {
*/	
	        	//logger.info("3: needyCrop is " + needyCrop.getCropType().getName());

	            //setDescription(Msg.getString("Task.description.tendGreenhouse.tend", Conversion.capitalize(needyCrop.getCropType().getName())));
	            
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
		
		        //System.out.println("TendGreenhouse : before greenhouse.addWork(workTime) ");
		        // Add this work to the greenhouse.
		        workTime = greenhouse.addWork(workTime, this);
		        //System.out.println("TendGreenhouse : after greenhouse.addWork(workTime) ");
		        
		        // Add experience
		        addExperience(time);
		
		        // Check for accident in greenhouse.
		        checkForAccident(time);    	
		       
		        remainingTime = time - workTime;
		        
		        if (remainingTime < 0)
		        	remainingTime = 0;
    	}
  
        return remainingTime;
    }
    
    public void setCrop(Crop needyCrop) {
        setDescription(Msg.getString("Task.description.tendGreenhouse.tend", Conversion.capitalize(needyCrop.getCropType().getName())));

    }

    /**
     * Performs the inspecting phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double inspectingPhase(double time) {
    	double remainingTime = time;
    	
    	String goal = null;    	
    	int rand = RandomUtil.getRandomInt(7);
    	
    	if (rand == 0) 
    		goal = "the Environmental Control System";
    	else if (rand == 1)
    		goal = "the HVAC System";
    	else if (rand == 2)
    		goal = "the Waste Disposal System";
    	else if (rand == 3)
    		goal = "the Containment System";
    	else if (rand == 4)
    		goal = "Any Traces of Contamination";
    	else if (rand == 5)
    		goal = "the Foundation/Structural Elements";
    	else if (rand == 6)
    		goal = "the Thermal Budget";
    	else if (rand == 7)
    		goal = "the Water and Irrigation System";
    	
        setDescription(Msg.getString("Task.description.tendGreenhouse.inspect", goal));
        
   		//System.out.println("inspectingPhase");
   		
        double workTime = 0;
        double factor = .5;//2D;
        
		if (person != null) {			
	        workTime = time * factor;
		}
		else if (robot != null) {
		     // TODO: how to lengthen the work time for a robot even though it moves slower than a person 
			// should it incurs penalty on workTime?
			workTime = time * factor*.5d;
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
        //greenhouse.addWork(workTime, null);
        
        // Add experience
        addExperience(time);

        // Check for accident in greenhouse.
        checkForAccident(time);
    	
        remainingTime = time - workTime;
        
        if (remainingTime < 0)
        	remainingTime = 0;
        //else
    	//	setPhase(CLEANING);
        	
        endTask();
    	return remainingTime;
    }
    
    /**
     * Performs the cleaning phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double cleaningPhase(double time) {
    	double remainingTime = 0;
  
    	String goal = null;    	
    	int rand = RandomUtil.getRandomInt(2);
    	
    	if (rand == 0) 
    		goal = "the Floor and Walls";
    	else if (rand == 1)
    		goal = "the Canopy";
    	else if (rand == 2)
    		goal = "the Equipment";
    	else if (rand == 2)
    		goal = "Pipings, Trays and Valves";
    	
        setDescription(Msg.getString("Task.description.tendGreenhouse.clean", goal));
        
  		//System.out.println("cleaningPhase");
  		 
        double workTime = 0;
        double factor = .5;//2D;
        
		if (person != null) {			
	        workTime = time * factor;
		}
		else if (robot != null) {
		     // TODO: how to lengthen the work time for a robot even though it moves slower than a person 
			// should it incurs penalty on workTime?
			workTime = time * factor*.5d;
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
        //greenhouse.addWork(workTime, null);

        // Add experience
        addExperience(time);

        // Check for accident in greenhouse.
        checkForAccident(time);
    	
        remainingTime = time - workTime;
        
        if (remainingTime < 0)
        	remainingTime = 0;
        //else
    	//	setPhase(CLEANING);
        	
        endTask();
    	return remainingTime;
    }
    
    /**
     * Performs the sampling phase in the botany lab
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double samplingPhase(double time) {
      	double remainingTime = 0, workTime = 0;

		// Obtain the crop with the highest VP to work on in the lab
		CropType type = greenhouse.selectNewCrop();

		if (type == null)
			// Obtain a needy crop to work on
			type = greenhouse.getNeedyCrop().getCropType();	
		
		if (type != null) {
	      	//System.out.println("type is " + type);
			boolean isDone = greenhouse.checkBotanyLab(type);
			//System.out.println("isDone is " + isDone);
			if (isDone) {
		        setDescription(Msg.getString("Task.description.tendGreenhouse.sample", Conversion.capitalize(type.getName()) + " for Lab Work"));
	
		        double factor = .5;//2D;
		        
				if (person != null) {			
			        workTime = time * factor;
				}
				else if (robot != null) {
				     // TODO: how to lengthen the work time for a robot even though it moves slower than a person 
					// should it incurs penalty on workTime?
					workTime = time * factor*.5d;
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
		        //greenhouse.addWork(workTime, null);
	      
		        // Add experience
		        addExperience(time);
		
		        // Check for accident in greenhouse.
		        checkForAccident(time);
		    
			}
	       
		}
		
        remainingTime = time - workTime;
        
        if (remainingTime < 0)
        	remainingTime = 0;
        //else
    	//	setPhase(CLEANING);
        endTask();
    	return remainingTime;
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
        BuildingManager buildingManager;
        
        if (unit instanceof Person) {
         	person = (Person) unit;
            LocationSituation location = person.getLocationSituation();
            if (location == LocationSituation.IN_SETTLEMENT) {
                buildingManager = person.getParkedSettlement().getBuildingManager();
                //List<Building> farmBuildings = buildingManager.getBuildings(BuildingFunction.FARMING);
                //farmBuildings = BuildingManager.getNonMalfunctioningBuildings(farmBuildings);
                //farmBuildings = BuildingManager.getFarmsNeedingWork(farmBuildings);
                //farmBuildings = BuildingManager.getLeastCrowdedBuildings(farmBuildings);
                List<Building> farmBuildings = buildingManager.getFarmsNeedingWork();
                
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
            	buildingManager = robot.getParkedSettlement().getBuildingManager();
                //List<Building> buildings = buildingManager.getBuildings(BuildingFunction.FARMING);
                //buildings = BuildingManager.getNonMalfunctioningBuildings(buildings);
                //buildings = Farming.getFarmsNeedingWork(buildings);
    			//if (RandomUtil.getRandomInt(4) == 0) // robot is not as inclined to move around
    			//	buildings = BuildingManager.getLeastCrowded4BotBuildings(buildings);
                List<Building> farmBuildings = buildingManager.getFarmsNeedingWork();
                
                // TODO: add person's good/bad feeling toward robots
                int size = farmBuildings.size();
                //System.out.println("size is "+size);
                int selected = 0; 
                if (size == 0) 
                	result = null;
                if (size >= 1) {
                	selected = RandomUtil.getRandomInt(size-1);         
                	result = farmBuildings.get(selected);
                }
                //System.out.println("getAvailableGreenhouse() : selected is "+selected); 
            }
        }
        return result;
    }

    @Override
    //TODO: get agility score of a person/robot
    public int getEffectiveSkillLevel() { 
    	SkillManager skillManager = null;
//    	if (skillManager == null) 
//    		System.out.println("skillManager is null"); 
		if (person != null) 
			skillManager = person.getMind().getSkillManager();
		else if (robot != null) 
			skillManager = robot.getBotMind().getSkillManager();

        return skillManager.getEffectiveSkillLevel(SkillType.BOTANY);
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