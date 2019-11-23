/**
 * Mars Simulation Project
 * MissionAgenda.java
 * @version 3.1.0 2017-01-14
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

public interface MissionAgenda {

	public void reportFindings();

	public void gatherSamples();

	public int[][] getMissionModifiers();
	
//	public void setMissionDirectives();
	
	public String getObjectiveName();

	String[] getPhases();

}
