/**
 * Mars Simulation Project
 * PlanMissionMeta.java
 * @date 2023-06-30
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.PlanMission;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementMetaTask;
import org.mars_sim.msp.core.person.ai.task.util.SettlementTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;

/**
 * The Meta task for the PlanMission task.
 */
public class PlanMissionMeta extends MetaTask implements SettlementMetaTask {

    private static class PlanTaskJob extends SettlementTask {
		
		private static final long serialVersionUID = 1L;

        public PlanTaskJob(SettlementMetaTask owner, double score) {
            super(owner, "Plan Mission", score);
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

    private static final int START_FACTOR = 50;
  

    public PlanMissionMeta() {
		super(NAME, WorkerType.PERSON, TaskScope.WORK_HOUR);
	}

    /**
     * Create a SettlmentTask to plan a mission. The score is based on how many missions can be
     * supported at the settlement
     */
    @Override
    public List<SettlementTask> getSettlementTasks(Settlement settlement) {
        List<SettlementTask> results = new ArrayList<>();
        int settlementMissions = missionManager.getMissionsForSettlement(settlement).size();

        int optimalMissions = (int) settlement.getPreferenceModifier(Settlement.MISSION_LIMIT);
        int shortfall = optimalMissions - settlementMissions;
        if (shortfall > 0) {
            results.add(new PlanTaskJob(this, shortfall * START_FACTOR));
        }
        return results;
    }

    /**
     * Create the person modifier base don their role in the settlement
     */
    @Override
    public double getPersonSettlementModifier(SettlementTask t, Person p) {
        if (!p.isInSettlement() || !p.getMind().canStartNewMission()) {
            return 0;
        }
    		
        // Probability affected by the person's stress and fatigue.
        PhysicalCondition condition = p.getPhysicalCondition();
        double fatigue = condition.getFatigue();
        double stress = condition.getStress();
        double hunger = condition.getHunger();            
        if (fatigue > 1000 || stress > 75 || hunger > 750)
            return 0;
            
        double result = (10/(fatigue + 1) + 10/(stress + 1) + 10/(hunger + 1));

        if (result > 0) {	 
            double roleFactor = switch (p.getRole().getType()) {
                case MISSION_SPECIALIST -> 3.125;
                case CHIEF_OF_MISSION_PLANNING  -> 2.25;
                case SUB_COMMANDER -> 1.375;
                case COMMANDER -> 1.25;
                default -> 1;
            };
            result *= roleFactor;

            // Get an available office space.
            Building building = BuildingManager.getAvailableAdminBuilding(p);
            result *= getBuildingModifier(building, p);

            result *= getPersonModifier(p);
        }

        return result;
    }

    @Override
    public double getRobotSettlementModifier(SettlementTask t, Robot r) {
        return 0;
    }
}
