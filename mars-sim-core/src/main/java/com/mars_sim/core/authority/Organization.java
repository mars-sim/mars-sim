/*
 * Mars Simulation Project
 * Organization.java
 * @date 2023-10-08
 * @author Manny Kung
 */

package com.mars_sim.core.authority;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;

public abstract class Organization implements Serializable, Temporal {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(Organization.class.getName());

	private String acronym;
	
	private String fullName;
	
	private Set<Nation> memberNations = new HashSet<>();
	
	private Nation leadNation;
	
	/**
	 * Constructor.
	 * 
	 * @param acronym
	 * @param fullName
	 */
	Organization(String acronym, String fullName) {
		this.acronym = acronym;
		this.fullName = fullName;
	}
	
	public String getAcronym() {
		return acronym;
	}
	
	public String getFullName() {
		return fullName;
	}
	
	
	public Set<Nation> getMemberNations() {
		return memberNations;
	}
	
	public Nation getLeadNation() {
		return leadNation;
	}
	
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		// Will add logics here in future
		return true;
	}



}
