/**
 * Mars Simulation Project
 * ConstructBuildingMeta.java
 * @date 2021-10-20
 * @author Scott Davis
 */
package com.mars_sim.core.person.ai.task.meta;

import java.util.List;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.fav.FavoriteType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.ConstructionMission;
import com.mars_sim.core.person.ai.task.ConstructBuilding;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.tools.Msg;

/**
 * Meta task for the ConstructBuilding task.
 */
public class ConstructBuildingMeta extends FactoryMetaTask {


    /** default logger. */
    private static SimLogger logger = SimLogger.getLogger(ConstructBuildingMeta.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.constructBuilding"); //$NON-NLS-1$

	private static final double WEIGHT = 100D;

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

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        // Probability affected by the person's stress and fatigue.
        if (!person.getPhysicalCondition().isFitByLevel(500, 50, 500)) {
        	return 0;
        }

        // Check if an airlock is available
        if (EVAOperation.getWalkableAvailableAirlock(person, false) == null) {
            return 0;
        }

        // Check if it is night time.
        if (EVAOperation.isGettingDark(person)) {
        	return 0;
        }

		ConstructionMission mission = ConstructBuilding.getMissionNeedingAssistance(person);
		if (mission == null)
			return 0;

        if (person.isInSettlement()) {
            Settlement settlement = person.getSettlement();

            result = getProbability(settlement);
        }

        return result * getPersonModifier(person);
    }


    public double getProbability(Settlement settlement) {

        double result = 0D;

        try {
            // Crowded settlement modifier
            int associated = settlement.getNumCitizens();
            int cap = settlement.getPopulationCapacity();
            if (associated >= cap) {
                result = WEIGHT * associated/cap * associated;
            }

            // Check all building construction missions occurring at the settlement.
            List<ConstructionMission> missions = ConstructBuilding.
                    getAllMissionsNeedingAssistance(settlement);

            int size = missions.size();

//            double factor = 0;
//            if (size == 0)
//            	factor = 1;
//            else if (size == 1)
//            	factor = Math.pow(1.5, 2);
//            else
//            	factor = Math.pow(size, 2);
//
//            result /= factor;

            result *= size;

        }
        catch (Exception e) {
            logger.severe(settlement, "Error finding building construction missions.", e);
        }

        return result;
    }
}
