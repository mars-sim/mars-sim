/*
 * Mars Simulation Project
 * DigLocalMeta.java
 * @date 2022-07-18
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Meta task for the DigLocal task.
 */
public abstract class DigLocalMeta extends MetaTask {

//	private static SimLogger logger = SimLogger.getLogger(DigLocalMeta.class.getName());

	private static final double VALUE = 0.05;
	private static final int MAX = 2_000;
	private static final int CAP = 6_000;
	
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

        double result = 0;
       
    	// Will not perform this task if he has a mission
    	if ((person.getMission() != null) || !person.isInSettlement()) {
    		return 0;
    	}
 
      if (result == 0)
    	  result = RandomUtil.getRandomDouble(collectionProbability / 2.0, collectionProbability) * VALUE;
      else
    	  result *= RandomUtil.getRandomDouble(collectionProbability / 2.0, collectionProbability) * VALUE;
            
      if (result > MAX)
    	  result = MAX;

    	// Check if an airlock is available
//        if (!settlement.isAirlockAvailable(person, false))
//    		return 0;
     
        //Checked for radiation events
    	boolean[] exposed = settlement.getExposed();

		if (exposed[2]) {
			// SEP can give lethal dose of radiation
            return 0;
		}
      
        // Checks if the person is physically fit for heavy EVA tasks
		if (!EVAOperation.isEVAFit(person))
			return 0;

        // Check at least one EVA suit at settlement.
        int numSuits = settlement.findNumContainersOfType(EquipmentType.EVA_SUIT);
        if (numSuits == 0) {
            return 0;
        }
    
        // Check if at least one empty bag at settlement.
        int numEmptyBags = settlement.findNumContainersOfType(containerType);
        if (numEmptyBags == 0) {
            return 0;
        }

        // Checks if the person's settlement is at meal time and is hungry
        if (EVAOperation.isHungryAtMealTime(person))
        	result *= .2;
        
        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        
        double stress = condition.getStress();
        double fatigue = condition.getFatigue();
        double hunger = condition.getHunger();

        result -= stress * 2 + fatigue/2 + hunger/2;

        if (result < 0)
        	return 0;

	    int indoor = settlement.getIndoorPeopleCount(); 
	    int citizen = settlement.getNumCitizens();
	    int cap = settlement.getPopulationCapacity();

	    // Effect of a crowded settlement. Only if indoor has more than its capacity
        if (indoor > cap)
            result *= indoor - cap;

        // Effect of population. The smaller the population, the more they are motivated to dig.
        if (citizen <= 32)
            // Adds effect of the # of citizen 
        	result *= Math.max(1, 33 - citizen);
     
        // Effect of the ratio of # indoor people vs. those outside already doing EVA 
        result *= (1.0 + indoor) / (1 + settlement.getNumOutsideEVA());
        
        // Effect of the beginning of a work shift
        // shiftBonus will have a minimum of 10
        double shiftBonus = person.getTaskSchedule().obtainScoreAtStartOfShift();
        // Encourage to get this task done early in a work shift
        result *= shiftBonus;

        // Effect of the amount of sunlight that influences the probability of starting this task
        double sunlight = surfaceFeatures.getSunlightRatio(settlement.getCoordinates());
        // The higher the sunlight (0 to 1, 1 being the highest) 
        result *= Math.max(.01, sunlight * 2);
     
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

        if (result > CAP)
        	result = CAP;

        return result;
    }
}
