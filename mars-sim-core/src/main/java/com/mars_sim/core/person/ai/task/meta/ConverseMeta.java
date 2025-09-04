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
import com.mars_sim.core.person.ai.task.util.TaskTrait;
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
     * <p><b>Note:</b> We intentionally avoid calling the deprecated
     * {@code FactoryMetaTask#getProbability(Person)}. If a non-deprecated overload exists
     * upstream in {@link FactoryMetaTask}, {@link #safeBaseProbability(Person)} can be
     * used to incorporate its baseline without referencing deprecated APIs.</p>
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

        // If you'd like to incorporate a base from a future non-deprecated overload:
        // double base = safeBaseProbability(person);
        // result = Math.max(result, base);

        return result;
    }

    /**
     * Compatibility helper that resolves a non-deprecated probability method at runtime.
     * <p>
     * Searches {@link FactoryMetaTask} for a public/protected method named {@code getProbability}
     * whose first parameter is {@link Person} and which is <b>not</b> annotated with {@link Deprecated}.
     * If found, it invokes that method with the {@code person} and default values for trailing params.
     * </p>
     * <p>
     * If no such non-deprecated overload exists, returns {@code 0d}. This keeps us from referencing
     * deprecated APIs at compile time and avoids warnings or build breaks due to deprecations.
     * </p>
     */
    @SuppressWarnings("unused")
    private double safeBaseProbability(Person person) {
        try {
            for (var m : FactoryMetaTask.class.getMethods()) {
                if (!"getProbability".equals(m.getName())) continue;
                var params = m.getParameterTypes();
                if (params.length == 0 || params[0] != Person.class) continue;
                if (m.getAnnotation(Deprecated.class) != null) continue; // skip deprecated overloads

                // Build default args for any trailing parameters.
                Object[] args = new Object[params.length];
                args[0] = person;
                for (int i = 1; i < params.length; i++) {
                    Class<?> t = params[i];
                    if (!t.isPrimitive()) {
                        args[i] = null;
                    }
                    else if (t == boolean.class) {
                        args[i] = Boolean.FALSE;
                    }
                    else if (t == byte.class) {
                        args[i] = (byte) 0;
                    }
                    else if (t == short.class) {
                        args[i] = (short) 0;
                    }
                    else if (t == int.class) {
                        args[i] = 0;
                    }
                    else if (t == long.class) {
                        args[i] = 0L;
                    }
                    else if (t == float.class) {
                        args[i] = 0F;
                    }
                    else if (t == double.class) {
                        args[i] = 0D;
                    }
                    else { // char, etc.
                        args[i] = '\0';
                    }
                }
                Object result = m.invoke(this, args);
                if (result instanceof Number n) return n.doubleValue();
            }
        }
        catch (Throwable ignore) {
            // fall through to neutral base
        }
        return 0D;
        }
}
