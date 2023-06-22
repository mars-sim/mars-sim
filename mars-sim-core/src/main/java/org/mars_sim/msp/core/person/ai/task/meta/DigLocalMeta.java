/*
 * Mars Simulation Project
 * DigLocalMeta.java
 * @date 2023-06-08
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.util.FactoryMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.Shift;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Meta task for the DigLocal task.
 */
public abstract class DigLocalMeta extends FactoryMetaTask {

	// can add back SimLogger logger = SimLogger.getLogger(DigLocalMeta.class.getName())

	private static final double VALUE = 50;
	private static final int MAX = 2_000;
	private static final int CAP = 3_000;
	
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

    	if (collectionProbability == 0.0)
    		return 0;
    	
        double result = RandomUtil.getRandomDouble(collectionProbability / 10, collectionProbability) * VALUE;
       
//        logger.info(settlement, 10_000, "Early " + ResourceUtil.findAmountResourceName(resourceId) + (int)result);
        
    	// Will not perform this task if he has a mission
    	if ((person.getMission() != null) || !person.isInSettlement()) {
    		return 0;
    	}

      if (result > MAX)
    	  result = MAX;

    	// Check if an airlock is available for egress
        if (!settlement.anyAirlocksForIngressEgress(person, false)) {
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
        
//        logger.info(settlement, 10_000, "Mid " + ResourceUtil.findAmountResourceName(resourceId) + (int)result);
        
        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        
        double stress = condition.getStress();
        double fatigue = condition.getFatigue();
        double hunger = condition.getHunger();
        double exerciseMillisols = person.getCircadianClock().getTodayExerciseTime();
        
        result -= stress * 2 + fatigue/2 + hunger/2 - exerciseMillisols;

        result *= getRadiationModifier(settlement);

        if (result <= 0)
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
        	result *= Math.max(1, 1.5 * (33 - citizen));
     
        // Effect of the ratio of # indoor people vs. those outside already doing EVA 
        result *= 1.0 / (1 + settlement.getNumOutsideEVA());

        // Encourage to get this task done early in a work shift
        result *= getShiftModifier(person);

        // Effect of the amount of sunlight that influences the probability of starting this task
        double sunlight = surfaceFeatures.getSunlightRatio(settlement.getCoordinates());
        // The higher the sunlight (0 to 1, 1 being the highest) 
        result *= Math.max(.001, sunlight);
     
        if (result <= 0)
            return 0;

        result *= getPersonModifier(person);

        if (result > CAP)
        	result = CAP;

//        logger.info(settlement, 10_000, "End: " + ResourceUtil.findAmountResourceName(resourceId) + ": " + (int)result);
        return result;
    }

    /**
     * Get a modifier based on the Shift start time
     */
    private static double getShiftModifier(Person person) {
        double result = 1D;
        if (person.isOnDuty()) {
            Shift shift = person.getShiftSlot().getShift();
            int shiftPast = getMarsTime().getMillisolInt() - shift.getStart();
            if (shiftPast < 0) {
                shiftPast += 1000;
            }
            if (shiftPast < 200) {
                result += 0.1D;
            }
        }

        return result;
    }
}
