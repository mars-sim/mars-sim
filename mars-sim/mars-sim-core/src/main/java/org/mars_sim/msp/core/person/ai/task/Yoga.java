/**
 * Mars Simulation Project
 * Yoga.java
 * @version 3.04 2013-05-11
 * @author Sebastien Venot
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.person.Person;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Yoga extends Task implements Serializable {

    private static final long serialVersionUID = 1L;

    // Task phase
    private static final String DOING_YOGA = "Doing Yoga";

    // The stress modified per millisol.
    private static final double STRESS_MODIFIER = -.7D;

    public Yoga(Person person) {
        super(DOING_YOGA, person, false, false, STRESS_MODIFIER, true, RandomUtil.getRandomInt(100));

        // Initialize phase
        addPhase(DOING_YOGA);
        setPhase(DOING_YOGA);
    }

    public static double getProbability(Person person) {

        double result = 0D;

        // Stress modifier
        result += person.getPhysicalCondition().getStress() / 2D;
        
        // No yoga outside.
        if (person.getLocationSituation().equals(Person.OUTSIDE)) {
            result = 0D;
        }

        return result;

    }

    @Override
    protected void addExperience(double time) {
        // Do nothing
    }

    @Override
    public List<String> getAssociatedSkills() {
        List<String> results = new ArrayList<String>(0);
        return results;
    }

    @Override
    public int getEffectiveSkillLevel() {
        return 0;
    }

    @Override
    protected double performMappedPhase(double time) {
        return 0;
    }
}