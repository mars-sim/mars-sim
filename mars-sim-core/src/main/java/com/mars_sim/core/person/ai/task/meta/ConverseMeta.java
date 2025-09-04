/*
 * Mars Simulation Project
 * ConverseMeta.java
 * @date 2023-10-28
 * @author Manny Kung
 */
package com.mars_sim.core.person.ai.task.meta;

import java.lang.reflect.Method;

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

    // ---------------------------------------------------------------------
    //           Low-complexity helpers to avoid deprecated API
    // ---------------------------------------------------------------------

    /** Finds a non-deprecated getProbability(...) overload starting with Person. */
    private static Method findProbabilityMethod() {
        for (Method m : FactoryMetaTask.class.getMethods()) {
            if (isCandidateProbability(m)) {
                return m;
            }
        }
        return null;
    }

    /** Checks whether a Method is a non-deprecated getProbability(Person, ...) candidate. */
    private static boolean isCandidateProbability(Method m) {
        if (!"getProbability".equals(m.getName())) return false;
        Class<?>[] params = m.getParameterTypes();
        if (params.length == 0 || params[0] != Person.class) return false;
        return m.getAnnotation(Deprecated.class) == null;
    }

    /** Default argument for a primitive/boxed parameter (used for trailing args). */
    private static Object defaultValue(Class<?> t) {
        if (!t.isPrimitive()) return null;
        if (t == boolean.class) return Boolean.FALSE;
        if (t == byte.class)    return (byte) 0;
        if (t == short.class)   return (short) 0;
        if (t == int.class)     return 0;
        if (t == long.class)    return 0L;
        if (t == float.class)   return 0F;
        if (t == double.class)  return 0D;
        if (t == char.class)    return '\0';
        return null;
    }

    /** Builds an argument array for invoking a probability method. */
    private static Object[] buildArgs(Method m, Person person) {
        Class<?>[] params = m.getParameterTypes();
        Object[] args = new Object[params.length];
        args[0] = person;
        for (int i = 1; i < params.length; i++) {
            args[i] = defaultValue(params[i]);
        }
        return args;
    }

    /**
     * Compatibility helper that resolves a non-deprecated probability method at runtime and invokes it.
     * <p>If none exists or invocation fails, returns {@code 0d}.</p>
     */
    @SuppressWarnings("unused")
    private double safeBaseProbability(Person person) {
        try {
            Method m = findProbabilityMethod();
            if (m == null) return 0D;
            Object result = m.invoke(this, buildArgs(m, person));
            return (result instanceof Number n) ? n.doubleValue() : 0D;
        }
        catch (Throwable ignore) {
            return 0D;
        }
    }
}
