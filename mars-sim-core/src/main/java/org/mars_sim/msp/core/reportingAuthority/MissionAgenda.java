/**
 * Mars Simulation Project
 * MissionAgenda.java
 * @version 3.1.1 2020-07-22
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
