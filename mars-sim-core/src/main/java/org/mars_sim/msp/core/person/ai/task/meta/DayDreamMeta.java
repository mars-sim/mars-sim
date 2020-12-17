/**
 * Mars Simulation Project
 * ReadMeta.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.FavoriteType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.task.DayDream;
import org.mars_sim.msp.core.person.ai.task.Read;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the Day Dream task. It can always be done.
 */
public class DayDreamMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    
    /** Task name */
    private static final String NAME = DayDream.NAME; //$NON-NLS-1$
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new DayDream(person);
    }

    /**
     * Always returns a potential probability.
     */
    @Override
    public double getProbability(Person person) {
    	return 0.0001D;
    }

	@Override
	public Task constructInstance(Robot robot) {
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		return 0;
	}
}
