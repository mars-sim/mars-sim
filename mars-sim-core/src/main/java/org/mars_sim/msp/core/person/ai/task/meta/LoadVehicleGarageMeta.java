/**
 * Mars Simulation Project
 * LoadVehicleGarageMeta.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.util.List;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.robot.ai.job.Deliverybot;

/**
 * Meta task for the LoadVehicleGarage task.
 */
public class LoadVehicleGarageMeta extends MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.loadVehicleGarage"); //$NON-NLS-1$

    /** default logger. */
    private static SimLogger logger = SimLogger.getLogger(LoadVehicleGarageMeta.class.getName());

    public LoadVehicleGarageMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		setFavorite(FavoriteType.OPERATION);
		setTrait(TaskTrait.STRENGTH);
		setPreferredJob(JobType.LOADERS);
	}

    @Override
    public Task constructInstance(Person person) {
        return new LoadVehicleGarage(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.isInSettlement()) {
            if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000))
            	return 0;
            
            // Check all vehicle missions occurring at the settlement.
            try {
                List<Mission> missions = LoadVehicleGarage.getAllMissionsNeedingLoading(person.getSettlement(), true);
                int num = missions.size();
                if (num == 0)
                	return 0;
                else
                	result = 100D * num;
                
            }
            catch (Exception e) {
                logger.severe(person, "Error finding loading missions.", e);
            }
            result *= person.getSettlement().getGoodsManager().getTransportationFactor();

            result = applyPersonModifier(result, person);
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
		return new LoadVehicleGarage(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

        if (robot.getRobotType() == RobotType.DELIVERYBOT
    		&& robot.getLocationStateType() == LocationStateType.INSIDE_SETTLEMENT) {

            // Check all vehicle missions occurring at the settlement.
            try {
                List<Mission> missions = LoadVehicleGarage.getAllMissionsNeedingLoading(robot.getSettlement(), true);
                result = 100D * missions.size();
            }
            catch (Exception e) {
                logger.severe(robot, "Error finding loading missions.", e);
            }

            // Effort-driven task modifier.
            result *= robot.getPerformanceRating();

        }

        return result;
    }
}
