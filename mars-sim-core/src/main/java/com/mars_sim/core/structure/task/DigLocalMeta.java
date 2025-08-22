/*
 * Mars Simulation Project
 * DigLocalMeta.java
 * @date 2023-06-08
 * @author Barry Evans
 */
package com.mars_sim.core.structure.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.equipment.EquipmentType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.Walk;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.SettlementParameters;
import com.mars_sim.core.tool.RandomUtil;

/**
 * Meta task for the DigLocal task.
 */
public abstract class DigLocalMeta extends MetaTask
    implements SettlementMetaTask
 {
    /**
     * This is a Settlement task to perform a digging job.
     */
    private static class DigLocalTaskJob extends SettlementTask {
		
		private static final long serialVersionUID = 1L;

        public DigLocalTaskJob(DigLocalMeta owner, RatingScore score, int total) {
            super(owner, owner.getName().replaceFirst("ging", ""), null, score);
            setDemand(total);
            setEVA(true); // Enable the EVA based assessments
        }

        @Override
        public Task createTask(Person person) {
            return  ((DigLocalMeta)getMeta()).createTask(person);
        }

        @Override
        public Task createTask(Robot robot) {
            throw new UnsupportedOperationException("Robots cannot dig");
        }
    }

    // This defines the maximum shift completed for a person to start a dig task.
    // Anything above this value will not be considered for digging.
    private static final double MAX_SHIFT_PERC_FOR_DIG = 0.66D;
	private static final int MAX_BASE = 20_000;

	private static final SettlementParameters SETTLE_CAT = SettlementParameters.INSTANCE;
    private static final double MIN_CAPACITY = 0.25D; // Minimum capacity to trigger digging

	private EquipmentType containerType;

    protected DigLocalMeta(String name, EquipmentType containerType) {
		super(name, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION);
		setTrait(TaskTrait.STRENGTH);

		this.containerType = containerType;
	}

    /**
     * Computes the probability of doing this task for a Settlement.
     * 
     * @param resourceId The id of the resource being dug
     * @param settlement
     * @param collectionProbability
     * @return
     */
    protected List<SettlementTask> getSettlementTaskJobs(int resourceId, Settlement settlement,
                            double collectionProbability) {

        // Check preconditions
        // - an airlock is available for egress
        // - at least one EVA suit at settlement.
        // - at least one empty bag at settlement.
    	if ((collectionProbability == 0.0)
            || (settlement.getNumEVASuit() == 0)
            || (settlement.findNumContainersOfType(containerType) == 0)) {                
    		return Collections.emptyList();
        }

        double base = RandomUtil.getRandomDouble(collectionProbability / 5, collectionProbability);
        if (base <= 0) {
            return Collections.emptyList();
        }
        else if (base > MAX_BASE) {
        	base = MAX_BASE;
        }
 
        // Determine the base score
        RatingScore result = new RatingScore(base);

        // Calculate the capacity for more EVAs
        int maxEVA = settlement.getPreferences().getIntValue(SETTLE_CAT, SettlementParameters.MAX_EVA,
                                                    1);
        maxEVA -= getActiveEVAPersons(settlement);
        if (maxEVA <= 0) {
            return Collections.emptyList();
        }

        // Should use the demand & resources stored to influence the score. 50% capacity is
        // the unmodified baseline
        var capacity = (settlement.getRemainingCombinedCapacity(resourceId)
                                    / settlement.getSpecificCapacity(resourceId));
        if (capacity <= MIN_CAPACITY) {
            return Collections.emptyList();
        }
        result.addModifier("capacity", 1 + (capacity - MIN_CAPACITY));

        List<SettlementTask> resultList = new ArrayList<>();
        resultList.add(new DigLocalTaskJob(this, result, maxEVA));
        return resultList;
    }

    
	/**
	 * Gets the number of Persons doing EVAOperations in a Settlement.
	 * 
	 * @param settlement
	 * @return
	 */
    private static int getActiveEVAPersons(Settlement settlement) {
		return settlement.getAllAssociatedPeople().stream()
							.filter(p -> p.getTaskManager().getTask() instanceof EVAOperation)
							.collect(Collectors.counting()).intValue();
	}

    /**
     * Assesses a person for a specific SettlementTask of this type.
     * 
     * @param t The Settlement task being evaluated
     * @param p Person in question
     * @return A new rating score applying the Person's modifiers
     */
    @Override
    public RatingScore assessPersonSuitability(SettlementTask t, Person p) {
        // Check preconditions
        // - an airlock is available for egress
        // - person is qualified for digging local
        // - person is physically fit for heavy EVA tasks
    	if (!Walk.anyAirlocksForIngressEgress(p, false)
        || !DigLocal.canDigLocal(p)
        || !EVAOperation.isEVAFit(p)) {
            return RatingScore.ZERO_RATING;
        }

        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = p.getPhysicalCondition();

        double stress = condition.getStress();
        double fatigue = condition.getFatigue();
        double hunger = condition.getHunger();
        double thirst = condition.getThirst();
        double exerciseMillisols = p.getCircadianClock().getTodayExerciseTime();
        
        var result = new RatingScore(t.getScore());
    
        // Add a negative base to model Person fitness
        result.addBase("fitness", -(stress * 2 + fatigue + hunger + thirst + exerciseMillisols));

        result = assessPersonSuitability(result, p);

        // Encourage to get this task done early in a work shift
        result.addModifier("shift", getShiftModifier(p));

        return result;
    }

    /**
     * Creates a specific Task of the appropriate digging activity.
     * 
     * @param person
     * @return
     */
    protected abstract Task createTask(Person person);

    /**
     * Gets a modifier based on the Shift start time. This is based on how far through the shift a person is;
     * it is weighted towards the 1st 50% of the shift.
     * 
     * @param person
     * @return
     */
    private static double getShiftModifier(Person person) {
        double completed = person.getShiftSlot().getShift().getShiftCompleted(getMarsTime().getMillisolInt());

        // Do not start in the last 30% of a shift
        if (completed > MAX_SHIFT_PERC_FOR_DIG) {
            return 0D;
        }
        return 1D - completed;
    }
}
