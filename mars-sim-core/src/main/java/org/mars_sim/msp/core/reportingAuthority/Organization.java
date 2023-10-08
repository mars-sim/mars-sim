/*
 * Mars Simulation Project
 * Organization.java
 * @date 2023-10-08
 * @author Manny Kung
 */

package org.mars_sim.msp.core.reportingAuthority;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;

public abstract class Organization implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(Organization.class.getName());

	private String name;
	
	private Set<Nation> memberNations = new HashSet<>();
	
	private Nation leadNation;
	
	Organization(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Set<Nation> getMemberNations() {
		return memberNations;
	}
	
	public Nation getLeadNation() {
		return leadNation;
	}
	
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		// TODO Auto-generated method stub
		return false;
	}



}
