/**
 * Mars Simulation Project
 * ConsolidateContainersMeta.java
 * @date 2021-12-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.task.ConsolidateContainers;
import org.mars_sim.msp.core.person.ai.task.util.MetaTask;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.person.ai.task.util.TaskTrait;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;

/**
 * Meta task for the ConsolidateContainers task.
 */
public class ConsolidateContainersMeta extends MetaTask {
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.consolidateContainers"); //$NON-NLS-1$
    
    public ConsolidateContainersMeta() {
		super(NAME, WorkerType.BOTH, TaskScope.WORK_HOUR);
		
		setFavorite(FavoriteType.OPERATION, FavoriteType.TINKERING);
		setTrait(TaskTrait.STRENGTH);
	}

    @Override
    public Task constructInstance(Person person) {
        return new ConsolidateContainers(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

    	// a person can be within a settlement or inside a vehicle
        if (person.isInside()) {
        	
            // Probability affected by the person's stress and fatigue.
            if (!person.getPhysicalCondition().isFitByLevel(1000, 70, 1000)) {
            	return 0;
            }
            
            // Check if there are local containers that need resource consolidation.
            if (ConsolidateContainers.needResourceConsolidation(person)) {
                result = 10D;
            }

            result = applyPersonModifier(result, person);
        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
        return new ConsolidateContainers(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

        if (robot.getRobotType() == RobotType.DELIVERYBOT && robot.isInside()) {

            // Check if there are local containers that need resource consolidation.
            if (ConsolidateContainers.needResourceConsolidation(robot)) {
                result = 10D;
            }

            // Effort-driven task modifier.
            result *= robot.getPerformanceRating();
        }

        return result;
	}
}
