/*
 * Mars Simulation Project
 * ResearchProject.java
 * @date 2023-12-13
 * @author Manny Kung
 */
package com.mars_sim.core.moon.project;

import java.io.Serializable;
import java.util.List;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.science.ScienceType;

public class DevelopmentProject implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(DevelopmentProject.class.getName());

	private double developmentValue;
	
	private double researchValue;
	
	private String name;
	
	private ScienceType science;
	
	/** Major topics covered by this research. */
	private List<String> topics;

	DevelopmentProject(String name, ScienceType science) {
		this.name = name;
		this.science = science;
	}
	
	public void addTopic() {
		topics.add(SimulationConfig.instance().getScienceConfig().getATopic(science));
	}
	
	public void addDevelopmentValue(double value) {
		developmentValue += value;
	}
	
	public void addResearchValue(double value) {
		researchValue += value;
	}
}
