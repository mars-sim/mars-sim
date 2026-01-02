/*
 * Mars Simulation Project
 * ResearchProject.java
 * @date 2023-12-13
 * @author Manny Kung
 */
package com.mars_sim.core.moon.project;


import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.moon.Colonist;
import com.mars_sim.core.science.ScienceType;

public class ResearchProject extends LunarProject{

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(ResearchProject.class.getName());
	
	/**
	 * Constructor.
	 * 
	 * @param lead
	 * @param name
	 * @param science
	 */
	ResearchProject(Colonist lead, String name, ScienceType science) {
		super(lead, name, science);
	}
	
	public void addResearchValue(double value) {
		addValue(value);
	}
	
	public double getResearchValue() {
		return getValue();
	}
	
	public double getAverageResearchActiveness() {
		int num = 1;
		double sum = ((ColonyResearcher)getLead()).getActiveness() * 2;
		for (Colonist c: getParticipants()) {
			num++;
			sum += ((ColonyResearcher)c).getActiveness();
		}
		return sum / num;
	}
}
