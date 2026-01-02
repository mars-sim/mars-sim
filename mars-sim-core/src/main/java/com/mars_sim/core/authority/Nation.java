/*
 * Mars Simulation Project
 * Nation.java
 * @date 2023-10-08
 * @author Manny Kung
 */

package com.mars_sim.core.authority;

import java.io.Serializable;
import com.mars_sim.core.logging.SimLogger;

/**
 * The Nation class represents the runtime object of a country. 
 * Note: NationSpec stores the vital pre-configured data regarding each country in 
 *       its xml file e.g. For Germany, the xml is "country_germany.xml".
 */
public class Nation implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(Nation.class.getName());
	
	private String name;
	
	public Nation(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
