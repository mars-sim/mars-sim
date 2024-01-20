/*
 * Mars Simulation Project
 * ResearchProject.java
 * @date 2023-12-13
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

public class ResearchProject implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(ResearchProject.class.getName());

	private final int maxNumParticipants = RandomUtil.getRandomInt(3, 10);
	
	private double developmentValue;
	
	private double researchValue;
	
	private String name;
	
	private ScienceType science;
	/** The lead researcher. */
	private ColonyResearcher lead;
	
	/** Major topics covered by this research. */
	private List<String> topics;
	/** A set of participating researcher. */
	private Set<ColonyResearcher> participants = new HashSet<>();
	
	ResearchProject(ColonyResearcher lead, String name, ScienceType science) {
		this.lead = lead;
		this.name = name;
		this.science = science;
	}
	
	public void addParticipant(ColonyResearcher participant) {
		participants.add(participant);
	}
	
	public ColonyResearcher getLead() {
		return lead;
	}
	
	public Set<ColonyResearcher> getParticipants() {
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
	
	public double getAverageResearchActiveness() {
		int num = 1;
		double sum = lead.getActiveness() * 2;
		for (ColonyResearcher r: participants) {
			num++;
			sum += r.getActiveness();
		}
		return sum / num;
	}
}
