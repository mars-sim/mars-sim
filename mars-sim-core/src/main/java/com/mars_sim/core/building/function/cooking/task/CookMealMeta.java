/*
 * Mars Simulation Project
 * CookMealMeta.java
 * @date 2022-08-30
 * @author Scott Davis
 */
package com.mars_sim.core.building.function.cooking.task;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.cooking.Cooking;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.util.MetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementMetaTask;
import com.mars_sim.core.person.ai.task.util.SettlementTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.person.ai.task.util.TaskUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;

/**
 * Meta task for the CookMeal task.
 */
public class CookMealMeta extends MetaTask 
            implements SettlementMetaTask {

  private static class CookMealJob extends SettlementTask {
		
		private static final long serialVersionUID = 1L;

        private Cooking kitchen;

        public CookMealJob(SettlementMetaTask owner, Cooking kitchen, int demand, RatingScore score) {
            super(owner, "Cook Meal", kitchen.getBuilding(), score);
            this.kitchen = kitchen;

            setDemand(demand);
        }

        @Override
        public Task createTask(Person person) {
            return new CookMeal(person, kitchen);
        }

        @Override
        public Task createTask(Robot robot) {
            return new CookMeal(robot, kitchen);
        }
    }

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.cookMeal"); //$NON-NLS-1$
	
    public CookMealMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.ANY_HOUR);
		setFavorite(FavoriteType.COOKING);
		setTrait(TaskTrait.ARTISTIC);
		setPreferredJob(JobType.CHEF);
        addPreferredRobot(RobotType.CHEFBOT);
	}

    /**
     * Gets the score for this person to cook a meal
     * 
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
    @Override
	public RatingScore assessPersonSuitability(SettlementTask t, Person p) {

        RatingScore factor = super.assessPersonSuitability(t, p);
        if (factor.getScore() <= 0) {
            return factor;
        }

        double att = p.getNaturalAttributeManager()
        				.getAttribute(NaturalAttributeType.CREATIVITY) / 20.0;	
        factor.addModifier("attribute", att);

        // Crowding modifier.
        var b = ((CookMealJob) t).kitchen.getBuilding();
        return assessBuildingSuitability(factor, b, p);
	}

    /**
     * Gets the score for a settlement task for a robot. The over crowding probability is considered.
     * 
	 * @return The factor to adjust task score; 0 means task is not applicable
     */
	@Override
	public RatingScore assessRobotSuitability(SettlementTask t, Robot r)  {
        return TaskUtil.assessRobot(t, r);
    }

    @Override
    public List<SettlementTask> getSettlementTasks(Settlement settlement) {
        List<SettlementTask> results = new ArrayList<>();

        // IKs it meal time and can at least one meal me cooked
        if (CookMeal.isMealTime(settlement, CookMeal.PREP_TIME) && Cooking.hasMealIngredients(settlement)) {
            int mealShortfall = Cooking.getSettlementMealShortfall(settlement);
            if (mealShortfall > 0) {
                // See if there is an available kitchen.
                for(var kitchenBuilding : settlement.getBuildingManager().getBuildings(FunctionType.COOKING)) {
                    Cooking kitchen = kitchenBuilding.getCooking();

                    // Check if enough meals have been cooked at kitchen for this meal time.
                    boolean enoughMeals = kitchen.getCookNoMore();
                    int demand = kitchen.getCookCapacity() - kitchen.getNumCooks();
                    if (!enoughMeals && (demand > 0)) {
                        
                        RatingScore rating = new RatingScore(250);
                        rating.addBase("cleanliness", (kitchen.getCleanliness() + 1) * 10);

                        rating.addModifier("meals", 1 + (mealShortfall/10D));

                        results.add(new CookMealJob(this, kitchen, demand, rating));
                    }
                }
            }
        }

        return results;
    }
}
