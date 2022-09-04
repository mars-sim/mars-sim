/*
 * Mars Simulation Project
 * DigLocalMeta.java
 * @date 2022-07-18
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * Meta task for the DigLocal task.
 */
public abstract class DigLocalMeta extends MetaTask {

	private static SimLogger logger = SimLogger.getLogger(DigLocalMeta.class.getName());

	private static final double VALUE = 2.5;
	private static final int MAX = 10000;
	private static final int LIMIT = 20000;
	
	private EquipmentType containerType;

    public DigLocalMeta(String name, EquipmentType containerType) {
		super(name, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION);
		setTrait(TaskTrait.STRENGTH);

		this.containerType = containerType;
	}

    /**
     * Computes the probability of doing this task.
     * 
     * @param settlement
     * @param person
     * @param collectionProbability
     * @return
     */
    protected double getProbability(int resourceId, Settlement settlement, Person person, double collectionProbability) {
    	String resource = "";
    	if (resourceId == ResourceUtil.regolithID)
    		resource = "Regolith";
    	else
    		resource = "Ice";
    	
    	// Will not perform this task if he has a mission
    	if ((person.getMission() != null) || !person.isInSettlement()) {
    		return 0;
    	}

        double result = 0D;

    	// Check if an airlock is available
//        if (!settlement.isAirlockAvailable(person, false))
//    		return 0;

        logger.info(person, "A. DigLocalMeta: " + resource + " " + result);
        
        //Checked for radiation events
    	boolean[] exposed = settlement.getExposed();

		if (exposed[2]) {
			// SEP can give lethal dose of radiation
            return 0;
		}

        logger.info(person, "B. DigLocalMeta: " + resource + " " + result);
        
        // Checks if the person is physically fit for heavy EVA tasks
		if (!EVAOperation.isEVAFit(person))
			return 0;
		
        logger.info(person, "C. DigLocalMeta: " + resource + " " + result);

        // Check at least one EVA suit at settlement.
        int numSuits = settlement.findNumContainersOfType(EquipmentType.EVA_SUIT);
        if (numSuits == 0) {
            return 0;
        }
	
        logger.info(person, "D. DigLocalMeta: " + resource + " " + result);
      
        // Check if at least one empty bag at settlement.
        int numEmptyBags = settlement.findNumContainersOfType(containerType);
        if (numEmptyBags == 0) {
            return 0;
        }

        logger.info(person, "E. DigLocalMeta: " + resource + " " + result);

        // Checks if the person's settlement is at meal time and is hungry
        if (EVAOperation.isHungryAtMealTime(person))
        	result = .2;
        
        logger.info(person, "F. DigLocalMeta: " + resource + " " + result);

        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        
        double stress = condition.getStress();
        double fatigue = condition.getFatigue();
        double hunger = condition.getHunger();

        if (result == 0)
        	result = collectionProbability * VALUE;
        else
        	result *= collectionProbability * VALUE;
        
        logger.info(person, "1 DigLocalMeta: " + resource + " " + result);
        
        if (result > MAX)
        	result = MAX;

        result -= stress * 2 + fatigue/4 + hunger/4;

        if (result < 0)
        	return 0;

	    int indoor = settlement.getIndoorPeopleCount(); 
	    int citizen = settlement.getNumCitizens();
	    int cap = settlement.getPopulationCapacity();

	    // Effect of a crowded settlement. Only if indoor has more than its capacity
        if (indoor > cap)
            result *= indoor - cap;

        // Effect of population. The smaller the population, the more they are motivated to dig.
        if (citizen <= 24)
            // Adds effect of the # of citizen 
        	result *= (25 - citizen) * 1.2;
        logger.info(person, "2 DigLocalMeta: " + resource + " " + result);
        
        // Effect of the ratio of # indoor people vs. those outside already doing EVA 
        result *= (1.0 + indoor) / (1 + settlement.getNumOutsideEVA());
        
        // Effect of the beginning of a work shift
        // shiftBonus will have a minimum of 10
        double shiftBonus = person.getTaskSchedule().obtainScoreAtStartOfShift();
        // Encourage to get this task done early in a work shift
        result *= shiftBonus / 10;
        logger.info(person, "3 DigLocalMeta: " + resource + " " + result);
        // Effect of the amount of sunlight that influences the probability of starting this task
        double sunlight = surfaceFeatures.getSunlightRatio(settlement.getCoordinates());
        // The higher the sunlight (0 to 1, 1 being the highest) 
//        if (sunlight > 0.01) {
        	result *= Math.max(.01, sunlight * 10);
//        }
            logger.info(person, "4 DigLocalMeta: " + resource + " " + result);
            
        if (result <= 0)
            return 0;

        result = applyPersonModifier(result, person);

    	if (exposed[0]) {
    		// Baseline can give a fair amount dose of radiation
			result /= 50D;
		}

    	if (exposed[1]) {
    		// GCR can give nearly lethal dose of radiation
			result /= 100D;
		}

        if (result > LIMIT)
        	result = LIMIT;

        logger.info(person, "5 DigLocalMeta: " + resource + " " + result);
        return result;
    }
}
