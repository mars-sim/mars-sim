/*
 * Mars Simulation Project
 * DevelopmentProject.java
 * @date 2023-12-15
 * @author Manny Kung
 */
package com.mars_sim.core.moon.project;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.moon.Colonist;
import com.mars_sim.core.science.ScienceType;

public class DevelopmentProject extends LunarProject {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(DevelopmentProject.class.getName());

	/**
	 * Constructor.
	 * 
	 * @param lead
	 * @param name
	 * @param science
	 */
	DevelopmentProject(ColonySpecialist lead, String name, ScienceType science) {
		super(lead, name, science);
	}
	
	public void addDevelopmentValue(double value) {
		addValue(value);
	}
	
	public double getDevelopmentValue() {
		return getValue();
	}
	
	public double getAverageDevelopmentActiveness() {
		int num = 1;
		double sum = ((ColonySpecialist)getLead()).getActiveness() * 2;
		for (Colonist c: getParticipants()) {
			num++;
			sum += ((ColonySpecialist)c).getActiveness();
		}
		return sum / num;
	}
}
