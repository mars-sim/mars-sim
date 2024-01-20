/*
 * Mars Simulation Project
 * MissionCapability.java
 * @date 2023-05-31
 * @author Barry Evans
 */
package com.mars_sim.core.authority;

import java.io.Serializable;

import com.mars_sim.core.parameter.ParameterManager;

/**
 * This class represents a demonstration capability to develop.
 */
public class MissionCapability implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String description;
	private ParameterManager preferences;

	public MissionCapability(String description, ParameterManager preferences2) {
		super();
		this.description = description;
		this.preferences = preferences2;
	}
	
	public String getDescription() {
		return description;
	}

	public String toString() {
		return description;
	}

	/**
	 * Gets the preferences that are defined for this capability.
	 * 
	 * @return
	 */
    public ParameterManager getPreferences() {
        return preferences;
    }
}
