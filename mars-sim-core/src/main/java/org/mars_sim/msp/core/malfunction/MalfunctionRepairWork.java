package org.mars_sim.msp.core.malfunction;

/**
 * The different types of repair work required for a Malfunction.
 */
public enum MalfunctionRepairWork {
	GENERAL("General"),
	EMERGENCY("Emergency"),
	EVA("EVA");
	
	private String name;
	
	private MalfunctionRepairWork(String name) {
		this.name = name;
	}	
	
	public String getName() {
		return name;
	}
}
