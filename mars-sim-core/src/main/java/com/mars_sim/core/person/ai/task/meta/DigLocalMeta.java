/*
 * Mars Simulation Project
 * DigLocalMeta.java
 * @date 2023-06-08
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.task.DigLocal;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.Walk;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.tools.util.RandomUtil;

/**
 * Meta task for the DigLocal task.
 */
public abstract class DigLocalMeta extends FactoryMetaTask {


	private static final double VALUE = 25;
	private static final int MAX = 2_000;
	
	private EquipmentType containerType;

    protected DigLocalMeta(String name, EquipmentType containerType) {
		super(name, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION);
		setTrait(TaskTrait.STRENGTH);

		this.containerType = containerType;
	}

    /**
     * Computes the probability of doing this task.
     * 
     * @param resourceId The id of the resource being dug
     * @param settlement
     * @param person
     * @param collectionProbability
     * @return
     */
    protected List<TaskJob> getTaskJobs(int resourceId, Settlement settlement,
                            Person person, double collectionProbability) {

        // Check preconditions
        // - an airlock is available for egress
        // - person is qualified for digging local
        // - person is physically fit for heavy EVA tasks
        // - at least one EVA suit at settlement.
        // - at least one empty bag at settlement.
    	if ((collectionProbability == 0.0)
            || !Walk.anyAirlocksForIngressEgress(person, false)
            || !DigLocal.canDigLocal(person)
            || !EVAOperation.isEVAFit(person)
            || (settlement.getNumEVASuit() == 0)
            || (settlement.findNumContainersOfType(containerType) == 0)) {                
    		return EMPTY_TASKLIST;
        }

        double base = RandomUtil.getRandomDouble(collectionProbability / 10, collectionProbability) * VALUE;
        
        if (base > MAX)
        	base = MAX;
        RatingScore result = new RatingScore(base);
 
        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = person.getPhysicalCondition();
        
        double stress = condition.getStress();
        double fatigue = condition.getFatigue();
        double hunger = condition.getHunger();
        double exerciseMillisols = person.getCircadianClock().getTodayExerciseTime();
        
        // Add a negative base to model Person fitness
        result.addBase("fitness", -(stress * 2 + fatigue/2 + hunger/2 + exerciseMillisols));
        
        if (result.getScore() <= 0)
        	return EMPTY_TASKLIST;
    
        // Effect of the ratio of # indoor people vs. those outside already doing EVA 
        result.addModifier("eva",
                1.2 - ((double)settlement.getNumOutsideEVA() / settlement.getNumCitizens()));

        // Encourage to get this task done early in a work shift
        result.addModifier("shift", getShiftModifier(person));

        // Effect of the amount of sunlight that influences the probability of starting this task
        double sunlight = surfaceFeatures.getSunlightRatio(settlement.getCoordinates());
        // The higher the sunlight (0 to 1, 1 being the highest) 
        result.addModifier("sunlight", Math.max(.001, sunlight));

        result = assessPersonSuitability(result, person);

        return createTaskJobs(result);
    }

    /**
     * Get a modifier based on the Shift start time. This is based on how far throuhg the shift a person is;
     * it is weighted towards the 1st 50% of the shift.
     */
    private static double getShiftModifier(Person person) {
        double completed = person.getShiftSlot().getShift().getShiftCompleted(getMarsTime().getMillisolInt());

        // Less than 50% compelted receives a bonus
        return 1D + (completed - 0.5D);
    }
}
