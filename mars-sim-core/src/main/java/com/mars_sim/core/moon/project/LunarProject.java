/*
 * Mars Simulation Project
 * LunarProject.java
 * @date 2024-02-15
 * @author Manny Kung
 */
package com.mars_sim.core.moon.project;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.moon.Colonist;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.tools.util.RandomUtil;

public class LunarProject {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(LunarProject.class.getName());

	private final int maxNumParticipants = RandomUtil.getRandomInt(3, 10);
	
	private double value;
	
	private String name;
	
	private ScienceType science;
	/** The lead colonist. */
	private Colonist lead;
	
	/** Major topics covered by this project. */
	private List<String> topics;
	/** A set of participating colonists. */
	private Set<Colonist> participants = new HashSet<>();
	
	/**
	 * Constructor.
	 * 
	 * @param lead
	 * @param name
	 * @param science
	 */
	LunarProject(Colonist lead, String name, ScienceType science) {
		this.lead = lead;
		this.name = name;
		this.science = science;
	}

	public void addParticipant(Colonist participant) {
		participants.add(participant);
	}
	
	public Colonist getLead() {
		return lead;
	}
	
	public Set<Colonist> getParticipants() {
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
	
	public void addValue(double value) {
		this.value += value;
	}
	
	public double getValue() {
		return this.value;
	}
}
