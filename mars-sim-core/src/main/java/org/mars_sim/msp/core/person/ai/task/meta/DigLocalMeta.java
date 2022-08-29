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

/**
 * Meta task for the DigLocal task.
 */
public abstract class DigLocalMeta extends MetaTask {

//	private static SimLogger logger = SimLogger.getLogger(DigLocalMeta.class.getName());

	private static final double VALUE = 1.0;
	private static final int MAX = 5000;
	private static final int LIMIT = 10000;
	
	private EquipmentType containerType;

    public DigLocalMeta(String name, EquipmentType containerType) {
		super(name, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION);
		setTrait(TaskTrait.STRENGTH);

		this.containerType = containerType;
	}

    protected double getProbability(Settlement settlement, Person person, double collectionProbability) {

    	// Will not perform this task if he has a mission
    	if ((person.getMission() != null) || !person.isInSettlement()) {
    		return 0;
    	}

        double result = 0D;

    	// Check if an airlock is available
        if (EVAOperation.getWalkableAvailableAirlock(person, false) == null)
    		return 0;

        //Checked for radiation events
    	boolean[]exposed = settlement.getExposed();

		if (exposed[2]) {
			// SEP can give lethal dose of radiation
            return 0;
		}

        // Check if it is night time.
        if (EVAOperation.isGettingDark(person))
        	return 0;

        // Checks if the person's settlement is at meal time and is hungry
        if (EVAOperation.isHungryAtMealTime(person))
        	return 0;

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

        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        
        double stress = condition.getStress();
        double fatigue = condition.getFatigue();
        double hunger = condition.getHunger();

        result = collectionProbability * VALUE;
   
        if (result > MAX)
        	result = MAX;

        result = result - stress * 3 - fatigue/2 - hunger/2;

        if (result < 0)
        	return 0;

	    int indoor = settlement.getIndoorPeopleCount(); 
	    int citizen = settlement.getNumCitizens();

	    // Crowded settlement modifier
        if (indoor > settlement.getPopulationCapacity())
            result = result * (indoor - settlement.getPopulationCapacity());

        if (citizen <= 24 && citizen >= 4)
            // Adds effect of the # of citizen 
        	result *= (25 - citizen) * 5;

        // Adds effect of the ratio of # indoor people vs. those outside already doing EVA 
        result *= (1.0 + indoor) / (1 + settlement.getNumOutsideEVA()) ;

        // shiftBonus will have a minimum of 10
        double shiftBonus = person.getTaskSchedule().obtainScoreAtStartOfShift();
        
        // Encourage to get this task done early in a work shift
        result *= shiftBonus / 10;
        
        // The amount of sunlight influences the probability of starting this task
        double sunlight = surfaceFeatures.getSunlightRatio(settlement.getCoordinates());
        
        if (sunlight > 0.1) {
        	result *= sunlight * 10;
        }
        
        if (result <= 0)
            return 0;
        
        result = applyPersonModifier(result, person);
        
    	if (exposed[0]) {
    		// Baseline can give a fair amount dose of radiation
			result = result/50D;
		}

    	if (exposed[1]) {
    		// GCR can give nearly lethal dose of radiation
			result = result/100D;
		}
    	
        if (result > LIMIT)
        	result = LIMIT;
        
        return result;
    }
}
