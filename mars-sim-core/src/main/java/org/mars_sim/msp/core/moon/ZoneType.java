/*
 * Mars Simulation Project
 * ZoneType.java
 * @date 2023-10-05
 * @author Manny Kung
 */

package org.mars_sim.msp.core.moon;

public enum ZoneType {

	BUSINESS			("Business"),
	COMMAND_CONTROL		("Command Control"),
	COMMUNICATION		("Communication"),
	CONSTRUCTION		("Construction"),
	EDUCATION			("Education"),
	ENGINEERING			("Engineering"),
	INDUSTRIAL			("Industrial"),
	LIFE_SUPPORT		("Life Support"),
	OPERATION			("Operation"),
	RECREATION			("Recreation"),
	RESEARCH			("Research"),
	RESOURCE	("Resource Extraction"),	
	TRANSPORTATION		("Transportation"),
	;

	private String name;

	/** hidden constructor. */
	private ZoneType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}
