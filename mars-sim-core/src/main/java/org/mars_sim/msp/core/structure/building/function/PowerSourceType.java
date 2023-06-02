/*
 * Mars Simulation Project
 * PowerSourceType.java
 * @date 2023-06-02
 * @author stpa
 */

package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Msg;

public enum PowerSourceType {

	AREOTHERMAL_POWER 			(Msg.getString("PowerSourceType.areothermalPower")), //$NON-NLS-1$ 
	FISSION_POWER 				(Msg.getString("PowerSourceType.fissionPower")), //$NON-NLS-1$ 
	FUEL_POWER 					(Msg.getString("PowerSourceType.fuelPower")), //$NON-NLS-1$ 
	SOLAR_POWER 				(Msg.getString("PowerSourceType.solarPower")), //$NON-NLS-1$ 
	SOLAR_THERMAL 				(Msg.getString("PowerSourceType.solarThermalPower")), //$NON-NLS-1$ 
	THERMIONIC_NUCLEAR_POWER 	(Msg.getString("PowerSourceType.thermionicNuclearPower")), //$NON-NLS-1$ 	
	WIND_POWER 					(Msg.getString("PowerSourceType.windPower")); //$NON-NLS-1$

	private String name;

	/** 
	 * Constructor. 
	 */
	private PowerSourceType(String name) {
		this.name = name;
	}
	
	public final String getName() {
		return this.name;
	}

	@Override
	public final String toString() {
		return getName();
	}
	
	public static PowerSourceType getType(String name) {
		if (name != null) {
	    	for (PowerSourceType pst : PowerSourceType.values()) {
	    		if (name.equalsIgnoreCase(pst.name)) {
	    			return pst;
	    		}
	    	}
		}
		
		return null;
	}
}
