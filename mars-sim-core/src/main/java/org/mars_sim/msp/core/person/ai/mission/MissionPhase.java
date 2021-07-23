/**
 * Mars Simulation Project
 * MissionPhase.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.mission;

import java.io.Serializable;

/**
 * A phase of a mission.
 */
public final class MissionPhase implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// The phase name.
	private String name;

	/**
	 * Constructor
	 * 
	 * @param the phase name.
	 */
	public MissionPhase(String name) {
		this.name = name;
	}

	/**
	 * Gets the phase name.
	 * 
	 * @return phase name string.
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = (obj != null) && (obj instanceof MissionPhase) && obj.toString().equals(toString());
        return result;
	}
}
