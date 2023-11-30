/**
 * Mars Simulation Project
 * ManufactureGoodMeta.java
 * @date 2021-12-22
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.manufacture.ManufactureProcess;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.ManufactureGood;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskFactory;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.OverrideType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.Manufacture;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the ManufactureGood task.
 */
public class ManufactureGoodMeta extends MetaTask
    implements SettlementMetaTask, TaskFactory {

    /**
     * A potential jon needs to manufacture goods
     */
    private static class ManufactureGoodJob extends SettlementTask {

		private static final long serialVersionUID = 1L;
        private int minSkill;

        public ManufactureGoodJob(SettlementMetaTask owner, Building target, int minSkill, RatingScore score) {
            super(owner, "Manufacture goods", target, score);
             this.minSkill = minSkill;
        }

        @Override
        public Task createTask(Person person) {
            return new ManufactureGood(person, getBuilding());
        }

        @Override
        public Task createTask(Robot robot) {
			return new ManufactureGood(robot, getBuilding());
        }

        protected int getMinSkill() {
            return minSkill;
        }

        public Building getBuilding() {
            return (Building) getFocus();
        }
    }
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.manufactureGood"); //$NON-NLS-1$

    private static final int MAX_SKILL = 99;
    
    public ManufactureGoodMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.TINKERING);
		setTrait(TaskTrait.ARTISTIC);
		setPreferredJob(JobType.ARCHITECT, JobType.CHEMIST,
						JobType.ENGINEER, JobType.PHYSICIST);
                        
        addPreferredRobot(RobotType.MAKERBOT);
        addPreferredRobot(RobotType.REPAIRBOT);
        addPreferredRobot(RobotType.MEDICBOT);
        addPreferredRobot(RobotType.CONSTRUCTIONBOT);
	}

    /**
     * Assess a Person for a specific SettlementTask of this type.
     * 
     * @param t The Settlement task being evaluated
     * @param p Person in question
     * @return A new rating score applying the Person's modifiers
     */
    @Override
    public RatingScore assessPersonSuitability(SettlementTask t, Person p) {
        RatingScore score = RatingScore.ZERO_RATING;
        if (p.isInSettlement()
                && p.getPhysicalCondition().isFitByLevel(1000, 70, 1000)) {
            ManufactureGoodJob mgj = (ManufactureGoodJob)t;

            // Check person has minumum skill
            SkillManager skillManager = p.getSkillManager();
		    int skill = skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
            if (skill < mgj.getMinSkill()) {
                return RatingScore.ZERO_RATING;
            }
            score = super.assessPersonSuitability(t, p);
            score = assessBuildingSuitability(score, mgj.getBuilding(), p);

        }

        return score;
    }

    /**
     * Assess a Robot for a specific SettlementTask of this type.
     * 
     * @param t The Settlement task being evaluated
     * @param r Robot in question
     * @return A new rating score applying the Person's modifiers
     */
    @Override
    public RatingScore assessRobotSuitability(SettlementTask t, Robot r) {
        RatingScore score = RatingScore.ZERO_RATING;
        if (r.isInSettlement()) {
            ManufactureGoodJob mgj = (ManufactureGoodJob)t;

            // Check person has minumum skill
            SkillManager skillManager = r.getSkillManager();
		    int skill = skillManager.getEffectiveSkillLevel(SkillType.MATERIALS_SCIENCE);
            if (skill < mgj.getMinSkill()) {
                return RatingScore.ZERO_RATING;
            }
            score = new RatingScore(mgj.getScore());
            score.addModifier("performance", r.getPerformanceRating());
        }

        return score;
    }


    /**
     * Find any buulding that have manufacturing processing needing help and crete a
     * SettlementTask for each one.
     * @param settlement Place to assess
     */
    @Override
    public List<SettlementTask> getSettlementTasks(Settlement settlement) {
        List<SettlementTask> result = new ArrayList<>();
        if (!settlement.getProcessOverride(OverrideType.MANUFACTURE)) {
            // Find anything that can be worked on
            for(Building potentialBuilding :
                    ManufactureGood.getAvailableManufacturingBuilding(settlement, MAX_SKILL)) {
                Manufacture m = potentialBuilding.getManufacture();

                int minSkill = MAX_SKILL;
                int maxSkill = 0;
                for(ManufactureProcess p : m.getProcesses()) {
                    if (p.getWorkTimeRemaining() > 0D) {
                        int skillRequired = p.getInfo().getSkillLevelRequired();
                        minSkill = Math.min(minSkill, skillRequired);
                        maxSkill = Math.max(maxSkill, skillRequired);
                    }
                }

                // Create a task for this building
                if (minSkill < MAX_SKILL) {
                    RatingScore score = new RatingScore(200);
                    score.addModifier(GOODS_MODIFIER, settlement.getGoodsManager().getManufacturingFactor());

                    score.addBase(SKILL_MODIFIER, 1D + (maxSkill * 0.05D));
                    result.add(new ManufactureGoodJob(this, potentialBuilding, minSkill, score));
                }
            }
        }
        return result;
    }

    /**
     * Create a default ManufactureGood job.
     * @param person Person doing the task
     */
    @Override
    public Task constructInstance(Person person) {
        return new ManufactureGood(person);
    }

    /**
     * Create a default ManufactureGood job.
     * @param robot Robot doing the task
     */
    @Override
    public Task constructInstance(Robot robot) {
        return new ManufactureGood(robot);
    }
}
