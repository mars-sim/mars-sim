/**
 * Mars Simulation Project
 * ManufactureGoodMeta.java
 * @date 2024-09-09
 * @author Scott Davis
 */
package com.mars_sim.core.structure.building.function.task;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
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
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for the ManufactureGood task.
 */
public class ManufactureGoodMeta extends MetaTask
    implements SettlementMetaTask, TaskFactory {

	/** Default logger. */
	// May add back private static final SimLogger logger = SimLogger.getLogger(ManufactureGoodMeta.class.getName())
	
	
    /**
     * A potential job to manufacture goods
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
						JobType.ENGINEER, JobType.PHYSICIST, JobType.TECHNICIAN);
                        
        addPreferredRobot(RobotType.MAKERBOT);
        addPreferredRobot(RobotType.REPAIRBOT);
        addPreferredRobot(RobotType.MEDICBOT);
        addPreferredRobot(RobotType.CONSTRUCTIONBOT);
	}

    /**
     * Assesses a Person for a specific SettlementTask of this type.
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

            // Check person has minimum skill
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
     * Assesses a Robot for a specific SettlementTask of this type.
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

            // Check person has minimum skill
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
     * Finds any building that have manufacturing processing needing help and creates a
     * SettlementTask for each one.
     * 
     * @param settlement Place to assess
     */
    @Override
    public List<SettlementTask> getSettlementTasks(Settlement settlement) {
        List<SettlementTask> result = new ArrayList<>();
        if (settlement.getProcessOverride(OverrideType.MANUFACTURE)) {
        	return result;
        }
        
        // Find anything that can be worked on
        for (Building potentialBuilding :
                ManufactureGood.getAvailableManufacturingBuilding(settlement, MAX_SKILL)) {
            
            Manufacture m = potentialBuilding.getManufacture();

            int minSkill = MAX_SKILL;
            int maxSkill = 0; 
            for (ManufactureProcessInfo info : m.getPossibleManufactureProcesses()) {
                int skillRequired = info.getSkillLevelRequired();
                minSkill = Math.min(minSkill, skillRequired);
                maxSkill = Math.max(maxSkill, skillRequired);
            }

            // Create a task for this building
            if (minSkill < MAX_SKILL) {
            	int work = 0;
            	
                if (ManufactureGood.hasProcessRequiringWork(potentialBuilding, minSkill)) {
                	work = 100;
                }
                
                RatingScore score = new RatingScore(100.0 + work);
                
	            score = applyCommerceFactor(score, settlement, CommerceType.MANUFACTURING);

                score.addBase(SKILL_MODIFIER, 1D + (maxSkill * 0.075D));
                
                result.add(new ManufactureGoodJob(this, potentialBuilding, minSkill, score));
            }
        }
 
        return result;
    }

    /**
     * Creates a default ManufactureGood job.
     * 
     * @param person Person doing the task
     */
    @Override
    public Task constructInstance(Person person) {
        return new ManufactureGood(person);
    }

    /**
     * Creates a default ManufactureGood job.
     * 
     * @param robot Robot doing the task
     */
    @Override
    public Task constructInstance(Robot robot) {
        return new ManufactureGood(robot);
    }
}
