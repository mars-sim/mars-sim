package org.mars_sim.msp.javafx.configEditor;

/**
 * A class representing a settlement configuration.
 */
public class SettlementInfo {
	String playerName;
	String name;
	String template;
	String population;
	String numOfRobots;
	String latitude;
	String longitude;
	boolean hasMaxMSD;
	String maxMSD;
	String editMSD = "Edit";
	
	public String getName() {
		return name;
	}
}