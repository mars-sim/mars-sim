/**
 * Mars Simulation Project
 * PhaseType.java
 * @version 3.1.0 2017-09-16
 * @author Scott Davis
 */

package org.mars_sim.msp.core.resource;

import org.mars_sim.msp.core.Msg;


/**
 * The phase type of an amount resource.
 */
public enum PhaseType {

	GAS (Msg.getString("PhaseType.gas")), //$NON-NLS-1$
	LIQUID (Msg.getString("PhaseType.liquid")), //$NON-NLS-1$
	SOLID (Msg.getString("PhaseType.solid")); //$NON-NLS-1$

	private String name;

	/**
	 * Private constructor.
	 * @param name the name of the phase.
	 */
	private PhaseType(String name) {
		this.name = name;
	}

	/**
	 * Gets the internationalized name of the phase for display in user interface.
	 * @return name
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
	
	public static PhaseType fromString(String name) {
		if (name != null) {
	    	for (PhaseType pt : PhaseType.values()) {
	    		if (name.equalsIgnoreCase(pt.name)) {
	    			return pt;
	    		}
	    	}
		}
		
		return null;
	}
}