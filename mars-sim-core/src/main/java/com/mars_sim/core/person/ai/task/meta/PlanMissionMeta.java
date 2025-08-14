/**
 * Mars Simulation Project
 * PlanMissionMeta.java
 * @date 2023-06-30
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.person.ai.mission.MissionLimitParameters;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.task.PlanMission;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;

/**
 * The Meta task for the PlanMission task.
 */
public class PlanMissionMeta extends MetaTask implements SettlementMetaTask {

    private static class PlanTaskJob extends SettlementTask {
		
		private static final long serialVersionUID = 1L;

        public PlanTaskJob(SettlementMetaTask owner, RatingScore score) {
            super(owner, "Plan Mission", null, score);
        }

        @Override
        public Task createTask(Person person) {
            return new PlanMission(person);
        }

        @Override
        public Task createTask(Robot robot) {
            throw new UnsupportedOperationException("Robots cannot plan missions");
        }
    }


    /** Task name */
    private static final String NAME = Msg.getString("Task.description.planMission"); //$NON-NLS-1$

    private static final double START_FACTOR = 100;
  

    public PlanMissionMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
		setPreferredRole(RoleType.CREW_OPERATION_OFFICER, RoleType.CHIEF_OF_MISSION_PLANNING,
				RoleType.MISSION_SPECIALIST);
		addAllLeadershipRoles();
	}

    /**
     * Creates a SettlmentTask to plan a mission. The score is based on how many missions can be
     * supported at the settlement.
     */
    @Override
    public List<SettlementTask> getSettlementTasks(Settlement settlement) {
        List<SettlementTask> results = new ArrayList<>();
        int settlementMissions = missionManager.getMissionsForSettlement(settlement).size();

        int optimalMissions = settlement.getPreferences().getIntValue(
                                                MissionLimitParameters.INSTANCE,
                                                MissionLimitParameters.TOTAL_MISSIONS, 0);
        int shortfall = optimalMissions - settlementMissions;
        if (shortfall > 0) {
            results.add(new PlanTaskJob(this, new RatingScore(shortfall * START_FACTOR)));
        }
        return results;
    }

    /**
     * Creates the person modifier based on their role in the settlement.
     */
    @Override
	public RatingScore assessPersonSuitability(SettlementTask t, Person p) {
        if (!p.isInSettlement() || !p.getMind().canStartNewMission()) {
            return RatingScore.ZERO_RATING;
        }
    		
        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = p.getPhysicalCondition();
        double fatigue = condition.getFatigue();
        double stress = condition.getStress();
        double hunger = condition.getHunger();            
        if (fatigue > 1000 || stress > 75 || hunger > 750)
            return RatingScore.ZERO_RATING;
            
        var factor = super.assessPersonSuitability(t, p);
        if (factor.getScore() == 0) {
            return factor;
        }

        double roleFactor = switch (p.getRole().getType()) {
            case CHIEF_OF_MISSION_PLANNING  -> 2.0;
            case MISSION_SPECIALIST -> 1.75;
            case COMMANDER -> 1.5;
            case SUB_COMMANDER -> 1.25;
            default -> 0.75;
        };
        factor.addModifier("role", roleFactor);

        // Get an available office space.
        Building building = BuildingManager.getAvailableFunctionTypeBuilding(p, FunctionType.ADMINISTRATION);
        assessBuildingSuitability(factor, building, p);

        return factor;
    }
}
