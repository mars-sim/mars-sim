/*
 * Mars Simulation Project
 * EngineeringProject.java
 * @date 2023-12-15
 * @author Manny Kung
 */
package com.mars_sim.core.moon.project;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.tools.util.RandomUtil;

public class EngineeringProject implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(EngineeringProject.class.getName());

	private final int maxNumParticipants = RandomUtil.getRandomInt(3, 10);
	
	private double developmentValue;
	
	private double researchValue;
	
	private String name;
	
	private ScienceType science;
	/** The lead engineer. */
	private ColonistEngineer lead;
	
	/** Major topics covered by this engineering project. */
	private List<String> topics;
	/** A set of participating engineer. */
	private Set<ColonistEngineer> participants = new HashSet<>();
	
	EngineeringProject(ColonistEngineer lead, String name, ScienceType science) {
		this.lead = lead;
		this.name = name;
		this.science = science;
	}
	
	public void addParticipant(ColonistEngineer participant) {
		participants.add(participant);
	}
	
	public ColonistEngineer getLead() {
		return lead;
	}
	
	public Set<ColonistEngineer> getParticipants() {
		return participants;
	}
	
	public boolean canAddParticipants() {
		return maxNumParticipants <= getNumParticipants();
	}
	
	public int getNumParticipants() {
		return participants.size();
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
	
	public double getDevelopmentValue() {
		return developmentValue;
	}
	
	public double getResearchValue() {
		return researchValue;
	}
	
	public double getAverageDevelopmentActiveness() {
		int num = 1;
		double sum = lead.getActiveness() * 2;
		for (ColonistEngineer r: participants) {
			num++;
			sum += r.getActiveness();
		}
		return sum / num;
	}
}
