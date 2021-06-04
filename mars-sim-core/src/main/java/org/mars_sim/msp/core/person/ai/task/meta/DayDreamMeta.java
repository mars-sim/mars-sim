/**
 * Mars Simulation Project
 * DayDreamMeta.java
 * @version 3.1.2 2020-09-02
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.DayDream;
import org.mars_sim.msp.core.person.ai.task.utils.MetaTask;
import org.mars_sim.msp.core.person.ai.task.utils.Task;

/**
 * Meta task for the Day Dream task. It can always be done.
 */
public class DayDreamMeta extends MetaTask {

    public DayDreamMeta() {
		super(DayDream.NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
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
}
