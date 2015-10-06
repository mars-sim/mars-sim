/**
 * Mars Simulation Project
 * MissionAgenda.java
 * @version 3.08 2015-10-05
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

public interface MissionAgenda {

	public void reportFindings();

	public void gatherSamples();

	public String getObjectiveName();

}
