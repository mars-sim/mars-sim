/*
 * Mars Simulation Project
 * PhysicalConditionFormat.java
 * @date 2024-09-23
 * @author Barry Evans
 */
package com.mars_sim.core.person;


/**
 * Helper class to create text representations of the PhysicalCondition instances.
 */
public final class PhysicalConditionFormat {

	private static final String WELL = "Well";
	private static final String DEAD_COLON = "Dead: ";
	private static final String SICK_COLON = "Sick: ";

    /**
     * Helper cannot be created
     */
    private PhysicalConditionFormat() {}


    /**
	 * Gets a string description of the most mostSeriousProblem health situation.
	 *
	 * @return A string containing the current illness if any.
	 */
	public static String getHealthSituation(PhysicalCondition pc) {
		String situation = WELL;
	
        var mostSeriousProblem = pc.getMostSerious();
    	if (pc.isDead()) {
			situation = DEAD_COLON + mostSeriousProblem.printDeadStatus();
		}
    	else if (mostSeriousProblem != null) {
			situation = SICK_COLON + mostSeriousProblem.printStatus();
		}
		return situation;
	}
	
}
