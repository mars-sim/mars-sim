/*
 * Mars Simulation Project
 * ConverseMeta.java
 * @date 2023-10-28
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task.meta;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.task.Converse;
import com.mars_sim.core.person.ai.task.util.FactoryMetaTask;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskScope;
import com.mars_sim.core.person.ai.task.util.TaskTrait;
import com.mars_sim.core.person.ai.task.util.WorkerType;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * Meta task for Converse task.
 */
public class ConverseMeta extends FactoryMetaTask {

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.converse"); //$NON-NLS-1$

    private static final double VALUE = 1.2;
    private static final int CAP = 10;

    public ConverseMeta() {
        super(NAME, WorkerType.PERSON, TaskScope.ANY_HOUR);
        setTrait(TaskTrait.PEOPLE, TaskTrait.RELAXATION);
    }

    @Override
    public Task constructInstance(Person person) {
        return new Converse(person);
    }

    /**
     * Probability that this task is the best selection for the person.
     *
     * <p><b>Note:</b> Intentionally avoids calling the deprecated
     * {@code FactoryMetaTask#getProbability(Person)}. No reflection is used.</p>
     */
    @Override
    public double getProbability(Person person) {
        // Avoid chatting when outside
        if (person.isOutside()) {
            return 0D;
        }

        // Compute Converse-specific probability.
        double result = RandomUtil.getRandomDouble(
                person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.CONVERSATION)) / 20D;

        boolean isOnShiftNow = person.isOnDuty();
        if (!isOnShiftNow) {
            result = result / 2D;
        }

        result = result + result * person.getPreference().getPreferenceScore(this) / 10D;

        result *= VALUE;

        if (result > CAP) {
            result = CAP;
        }

        return result;
    }
}
