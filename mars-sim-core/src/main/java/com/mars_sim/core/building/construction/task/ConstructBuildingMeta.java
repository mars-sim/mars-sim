/*
 * Mars Simulation Project
 * ConstructBuildingMeta.java
 * @date 2025-09-06
 * @author Scott Davis
 */
package com.mars_sim.core.building.construction.task;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.shift.ShiftManager;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.Walk;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for the ConstructBuilding task.
 */
public class ConstructBuildingMeta extends MetaTask implements SettlementMetaTask {
	
    private static class ConstructionTaskJob extends SettlementTask {
		
		private static final long serialVersionUID = 1L;
        private ConstructionSite site;


        public ConstructionTaskJob(SettlementMetaTask owner, ConstructionSite site, int demand, RatingScore score) {
            super(owner, "Construct Building", site, score);
            this.site = site;

            setDemand(demand);
        }

        @Override
        public Task createTask(Person person) {
            return new ConstructBuilding(person, site);
        }

        @Override
        public Task createTask(Robot robot) {
            throw new UnsupportedOperationException("Robots cannot construct buildings");
        }
    }

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.constructBuilding"); //$NON-NLS-1$

    /* The maximum shift fraction completed for a person to start this task.
    If above this value, the person will not consider picking this task. */
    private static final double MAX_SHIFT_FRACTION = 0.66D;
 
	private static final double PHASE_WEIGHT = 0.2D;
    private static final int SITE_BASE_SCORE = 100;

    public ConstructBuildingMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.STRENGTH, TaskTrait.ARTISTIC);
		setPreferredJob(JobType.ARCHITECT, JobType.ENGINEER, JobType.TECHNICIAN);
	}

    /**
     * Gets the settlement tasks for any construction sites in the settlement with an active Mission.
     * The score is based on the base weight and can be modified by the settlement's needs or other factors.
     *
     * @param settlement the settlement.
     * @return list of settlement tasks.
     */
    @Override
    public List<SettlementTask> getSettlementTasks(Settlement settlement) {
		var activeSites = settlement
				.getConstructionManager().getConstructionSites()
                .stream()
                .filter(cs -> cs.getWorkOnSite() != null)
                .toList();

		List<SettlementTask> tasks = new ArrayList<>();

		for (ConstructionSite cs: activeSites) {
            var stage = cs.getCurrentConstructionStage();

            // Look to help with any active construction sites that have a mission
            // and sufficient work remaining to justify the task, and no missing materials.
            double remainingWork = stage.getRequiredWorkTime() - stage.getCompletedWorkTime();
            if ((remainingWork >= 100D) && !stage.hasMissingConstructionMaterials()) {
                var score = new RatingScore(SITE_BASE_SCORE);
                int phasesLeft = 2 - cs.getRemainingPhases().size();
                score.addModifier("progress", 1d + (phasesLeft * PHASE_WEIGHT)); // More completed, higher score

                tasks.add(new ConstructionTaskJob(this, cs, 1, score));
            }
		}
        return tasks;
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
