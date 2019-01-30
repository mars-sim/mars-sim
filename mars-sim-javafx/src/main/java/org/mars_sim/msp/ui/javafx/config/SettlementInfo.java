/**
 * Mars Simulation Project
 * SettlementInfo.java
 * @version 3.08 2016-07-18
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.javafx.config;

/**
 * A class representing a settlement configuration.
 */
public class SettlementInfo {
	String playerName;
	String name;
	String template;
	String population;
	String numOfRobots;
	String sponsor = "Mars Society (MS)";
	
	String latitude;
	String longitude;
	
	boolean hasMaxMSD;
	String maxMSD;
	String editMSD = "Edit";
	
	public String getName() {
		return name;
	}
}