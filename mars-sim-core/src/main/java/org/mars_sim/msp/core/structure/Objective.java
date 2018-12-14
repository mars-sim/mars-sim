/**
 * Mars Simulation Project
 * Objective.java
 * @version 3.1.0 2017-11-04
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure;

public interface Objective {

	public void setObjective(ObjectiveType objectiveType, int level);

	public ObjectiveType getObjective();
	
}
