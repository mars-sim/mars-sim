/*
 * Mars Simulation Project
 * ManufacturingMetaTask.java
 * @date 2025=01-04
 * @author Barry Evans
 */
package com.mars_sim.core.manufacture.task;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.manufacture.ManufacturingManager;
import com.mars_sim.core.manufacture.SalvageProcessInfo;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;

/**
 * This create SettlementTasks that manage any Manufacturing Tasks that can be worked on at
 * a Workshop Function.
 */
public class ManufacturingMetaTask extends MetaTask implements SettlementMetaTask {
	
    private abstract static class WorkshopJob extends SettlementTask {

		private static final long serialVersionUID = 1L;
        private int minSkill;

        public WorkshopJob(String name, SettlementMetaTask owner, Building target,
                           int minSkill, RatingScore score, int demand) {
            super(owner, name, target, score);
            this.minSkill = minSkill;
            setDemand(demand);
        }

        public int getMinSkill() {
            return minSkill;
        }

        public Building getBuilding() {
            return (Building) getFocus();
        }
    }

    private class ManufactureGoodJob extends WorkshopJob {
        ManufactureGoodJob(SettlementMetaTask owner, Building target,
                           int minSkill, RatingScore score, int demand) {
            super("Manufacture Job", owner, target, minSkill, score, demand);
        }

        @Override
        public Task createTask(Person person) {
            return new ManufactureGood(person, getBuilding());
        }

        @Override
        public Task createTask(Robot robot) {
			return new ManufactureGood(robot, getBuilding());
        }
    }

    private class SalvageGoodJob extends WorkshopJob {
        SalvageGoodJob(SettlementMetaTask owner, Building target,
                           int minSkill, RatingScore score, int demand) {
            super("Salvage Job", owner, target, minSkill, score, demand);
        }

        @Override
        public Task createTask(Person person) {
            return new SalvageGood(person, getBuilding());
        }

        @Override
        public Task createTask(Robot robot) {
			return null;
        }
    }
    
    public ManufacturingMetaTask() {
        super("Manufacturing", WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.TINKERING);
		setTrait(TaskTrait.ARTISTIC);
		setPreferredJob(JobType.ARCHITECT, JobType.CHEMIST,
						JobType.ENGINEER, JobType.PHYSICIST, JobType.TECHNICIAN);
                        
        addPreferredRobot(RobotType.MAKERBOT);
        addPreferredRobot(RobotType.REPAIRBOT);
        addPreferredRobot(RobotType.MEDICBOT);
        addPreferredRobot(RobotType.CONSTRUCTIONBOT);
    }

    @Override
    public List<SettlementTask> getSettlementTasks(Settlement settlement) {
        List<SettlementTask> results = new ArrayList<>();

        ManufacturingManager mgr = settlement.getManuManager();

        int lowestTechNeeded = mgr.getLowestOnQueue();

        for(var w : settlement.getBuildingManager().getBuildings(FunctionType.MANUFACTURE)) {
            addManuProcesses(settlement, w, lowestTechNeeded, results);

            addSalvageProcesses(settlement, w, lowestTechNeeded, results);
        }

        return results;
    }

    /**
     * Assess whether a Manufacture building needs assistent either for new or existing
     * manufacturing processes.
     * 
     * @param s
     * @param w
     * @param lowestTechNeeded
     * @param results
     */
    private void addManuProcesses(Settlement s, Building w, int lowestTechNeeded,
                    List<SettlementTask> results) {
        int demand = 0;
        int base = 100;
        var m = w.getManufacture();
        int lowestSkillNeeded = 0;
        
        // Add demands if spare capacity
        if (!m.isFull() && m.getTechLevel() >= lowestTechNeeded) {
            var capacity = m.getNumPrintersInUse() - m.getCurrentTotalProcesses();

            // How many Manufacturing jobs ar on the queue
            var queueSize = (int)s.getManuManager().getQueue().stream()
                        .filter(p -> p.getInfo() instanceof ManufactureProcessInfo)
                        .count();

            // Demand cannot be greater than queue or capacity
            demand = Math.min(queueSize, capacity);
        }

        // Count active manufacturing processes
        var active = m.getProcesses().stream()
                            .filter(p -> p.getWorkTimeRemaining() > 0D)
                            .toList();
        if (!active.isEmpty()) {
            demand += active.size();
            base += 100;
            lowestSkillNeeded = active.stream()
                                    .mapToInt(p -> p.getInfo().getSkillLevelRequired())
                                    .min().orElse(0);
        }

        if (demand > 0) {
            RatingScore score = new RatingScore(base);
            score = applyCommerceFactor(score, s, CommerceType.MANUFACTURING);
            results.add(new ManufactureGoodJob(this, w, lowestSkillNeeded, score, demand));
        }
    }

    /**
     * Assess whether a Manufacture building needs assistent either for new or existing
     * manufacturing processes.
     * 
     * @param s
     * @param w
     * @param lowestTechNeeded
     * @param results
     */
    private void addSalvageProcesses(Settlement s, Building w, int lowestTechNeeded,
                    List<SettlementTask> results) {
        int demand = 0;
        int base = 100;
        var m = w.getManufacture();
        int lowestSkillNeeded = 0;
        
        // Add demands if spare capacity
        if (!m.isFull() && m.getTechLevel() >= lowestTechNeeded) {
            var capacity = m.getNumPrintersInUse() - m.getCurrentTotalProcesses();

            // How many Manufacturing jobs ar on the queue
            var queueSize = (int)s.getManuManager().getQueue().stream()
                        .filter(p -> p.getInfo() instanceof SalvageProcessInfo)
                        .count();

            // Demand cannot be greater than queue or capacity
            demand = Math.min(queueSize, capacity);
        }

        // Count active manufacturing processes
        var active = m.getSalvageProcesses().stream()
                            .filter(p -> p.getWorkTimeRemaining() > 0D)
                            .toList();
        if (!active.isEmpty()) {
            demand += active.size();
            base += 100;
            lowestSkillNeeded = active.stream()
                                    .mapToInt(p -> p.getInfo().getSkillLevelRequired())
                                    .min().orElse(0);
        }

        if (demand > 0) {
            RatingScore score = new RatingScore(base);
            score = applyCommerceFactor(score, s, CommerceType.MANUFACTURING);
            results.add(new SalvageGoodJob(this, w, lowestSkillNeeded, score, demand));
        }
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
            WorkshopJob mgj = (WorkshopJob)t;

            // Check person has minimum skill
		    int skill = ManufactureGood.getWorkerSkill(p);
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
        if (r.isInSettlement() && (t instanceof ManufactureGoodJob mgj)) {
            // Check person has minimum skill
		    int skill = ManufactureGood.getWorkerSkill(r);
            if (skill < mgj.getMinSkill()) {
                return RatingScore.ZERO_RATING;
            }
            score = new RatingScore(mgj.getScore());
            score.addModifier("performance", r.getPerformanceRating());
        }

        return score;
    }
}
