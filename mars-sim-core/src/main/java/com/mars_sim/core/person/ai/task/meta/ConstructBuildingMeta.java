/*
 * Mars Simulation Project
 * ConstructBuildingMeta.java
 * @date 2025-09-06
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.shift.ShiftManager;
import com.mars_sim.core.person.ai.task.ConstructBuilding;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.Walk;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskJob;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.structure.task.DigLocal;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for the ConstructBuilding task.
 */
public class ConstructBuildingMeta extends FactoryMetaTask {
	
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(ConstructBuildingMeta.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.constructBuilding"); //$NON-NLS-1$

    /* The maximum shift fraction completed for a person to start this task.
    If above this value, the person will not consider picking this task. */
 private static final double MAX_SHIFT_FRACTION = 0.66D;
 
	private static final double WEIGHT = 500D;

    public ConstructBuildingMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.STRENGTH, TaskTrait.ARTISTIC);
		setPreferredJob(JobType.ARCHITECT, JobType.ENGINEER, JobType.TECHNICIAN);
	}

    @Override
    public Task constructInstance(Person person) {
        return new ConstructBuilding(person);
    }

    /**
     * Assesses a person constructing a building. 
     * Assessment is based on role/job & number of construction missions.
     */
    @Override
    public List<TaskJob> getTaskJobs(Person person) {

        // Probability affected by the person's stress and fatigue.
        if (!person.isInSettlement()) {
        	return EMPTY_TASKLIST;
        }
        
        if (!person.getPhysicalCondition().isEVAFit()) {
            return EMPTY_TASKLIST;
        }
        
        if (EVAOperation.isGettingDark(person)) {
            return EMPTY_TASKLIST;
        }
        
		List<ConstructionSite> sites = person.getAssociatedSettlement()
				.getConstructionManager().getConstructionSites(); 
		// Note: using .getConstructionSitesNeedingMission() returns zero sites
		if (sites.isEmpty())
			return EMPTY_TASKLIST;
		
		List<Mission> missions = new ArrayList<>();

		for (ConstructionSite cs: sites) {
			Mission m = cs.getWorkOnSite();
			if (m != null)
				missions.add(m);
		}
        if (!Walk.anyAirlocksForIngressEgress(person, false)) {
            return EMPTY_TASKLIST;
        }

        var score = new RatingScore(WEIGHT * missions.size());
        score = assessPersonSuitability(score, person);
        return createTaskJobs(score);
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
        result.addModifier("shift", ShiftManager.getShiftModifier(p, 
        		MAX_SHIFT_FRACTION, getMarsTime().getMillisolInt()));

        return result;
    }
}
