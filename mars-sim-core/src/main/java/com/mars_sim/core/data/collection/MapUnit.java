/*
 * Mars Simulation Project
 * MapUnit.java
 * @date 2026-08-31
 * @author Manny Kung
 */
package com.mars_sim.core.data.collection;

import java.io.Serializable;

public interface MapUnit extends Serializable {

	/**
	 * Gets the type.
	 * 
	 * @return name
	 */
	public String getType();
	
	/**
	 * Gets the name.
	 * 
	 * @return name
	 */
	public String getName();
}
