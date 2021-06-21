/**
 * Mars Simulation Project
 * Objective.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure;

public interface Objective {

	public void setObjective(ObjectiveType objectiveType, int level);

	public ObjectiveType getObjective();
	
}
