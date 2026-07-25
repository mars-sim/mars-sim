/**
 * Mars Simulation Project
 * LandmarkEVA.java
 * @date 2026-07-18
 * @author Barry Evans
 */
package com.mars_sim.core.mission.predefined;

import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.mission.MissionProject;
import com.mars_sim.core.mission.objectives.LandmarkObjective;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.PhysicalEffort;
import com.mars_sim.core.person.ai.task.util.TaskPhase;

/**
 * This is an EVAOperation Task that will visit a Landmark.
 */
class LandmarkEVA extends EVAOperation {

    private static final double EVA_DURATION = 0;
    private static final TaskPhase EVA_PHASE = new TaskPhase("Visit Landmark", 
                            	createPhaseImpact(PhysicalEffort.HIGH, SkillType.REPORTING));

    private LandmarkObjective objective;
    private MissionProject parent;

    public LandmarkEVA(Person p, MissionProject parent, LandmarkObjective objective) {
        super("Landmark EVA", p, EVA_DURATION, EVA_PHASE);
        this.objective = objective;
        setRandomOutsideLocation(p.getVehicle());

        this.parent = parent;
    }

	/**
	 * Performs the method mapped to the task's current phase.
	 *
	 * @param time the amount of time the phase is to be performed.
	 * @return the remaining time after the phase has been performed.
	 */
	@Override
	protected double performMappedPhase(double time) {

		time = super.performMappedPhase(time);
		if (!isDone() && (EVA_PHASE.equals(getPhase()))) {
            // Just check Person can continue
			time = checkReadiness(time);
		}
		return time;
	}

    /**
     * Clears down the EVA operation, recording the EVA completion for the landmark objective.
     */
    @Override
    protected void clearDown() {
        var w = getWorker();

        objective.recordEVA(w, getTimeCompleted());
        parent.fireMissionUpdate(MissionObjective.CHANGE_EVENT, w);
        super.clearDown();
    }
}
