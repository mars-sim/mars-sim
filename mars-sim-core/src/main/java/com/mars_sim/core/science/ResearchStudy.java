/*
 * Mars Simulation Project
 * ResearchStudy.java
 * @date 2024-08-12
 * @author Manny Kung
 */

package com.mars_sim.core.science;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mars_sim.core.time.ClockPulse;

public class ResearchStudy implements Researcher, Serializable {
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** The person's achievement in scientific fields. */
	private Map<ScienceType, Double> scientificAchievement = new ConcurrentHashMap<>();

	/** The person's list of collaborative scientific studies. */
	private Set<ScientificStudy> collabStudies;
	
	/** The person's current scientific study. */
	private ScientificStudy study;
	
	public ResearchStudy() {
		
		// Create a set of collaborative studies
		collabStudies = new HashSet<>();
	}
	
	/**
	 * Person can take action with time passing.
	 *
	 * @param pulse amount of time passing (in millisols).
	 */
	public void timePassing(ClockPulse pulse) {
		// Primary researcher; my responsibility to update Study
		if (study != null) {
			study.timePassing(pulse);
		}
	}
	
	public void terminateStudy() {
		if (study != null) {
			study.setCompleted(StudyStatus.CANCELLED, "Primary researcher was dead.");
			study = null;
		}
	}
	
	/**
	 * Set the study that this Person is the lead on.
	 * 
	 * @param scientificStudy
	 */
	@Override
	public void setStudy(ScientificStudy scientificStudy) {
		this.study = scientificStudy;
	}

	
	/**
	 * Gets the scientific study instance.		
	 */
	@Override
	public ScientificStudy getStudy() {
		return study;
	}

	/**
	 * Gets the collaborative study sets.
	 */
	@Override
	public Set<ScientificStudy> getCollabStudies() {
		return collabStudies;
	}
	
	/**
	 * Adds the collaborative study.
	 * 
	 * @param study
	 */
	@Override
	public void addCollabStudy(ScientificStudy study) {
		this.collabStudies.add(study);
	}

	/**
	 * Removes the collaborative study.
	 * 
	 * @param study
	 */
	@Override
	public void removeCollabStudy(ScientificStudy study) {
		this.collabStudies.remove(study);
	}

	/**
	 * Gets the person's achievement credit for a given scientific field.
	 *
	 * @param science the scientific field.
	 * @return achievement credit.
	 */
	@Override
	public double getScientificAchievement(ScienceType science) {
		double result = 0D;
		if (science == null)
			return result;
		if (scientificAchievement.containsKey(science)) {
			result = scientificAchievement.get(science);
		}
		return result;
	}

	/**
	 * Gets the person's total scientific achievement credit.
	 *
	 * @return achievement credit.
	 */
	@Override
	public double getTotalScientificAchievement() {
		double result = 0d;
		for (double value : scientificAchievement.values()) {
			result += value;
		}
		return result;
	}

	/**
	 * Adds achievement credit to the person in a scientific field.
	 *
	 * @param achievementCredit the achievement credit.
	 * @param science           the scientific field.
	 */
	@Override
	public void addScientificAchievement(double achievementCredit, ScienceType science) {
		if (scientificAchievement.containsKey(science)) {
			achievementCredit += scientificAchievement.get(science);
		}
		scientificAchievement.put(science, achievementCredit);
	}

	public void destroy() {
		collabStudies.clear();
		collabStudies = null;
		study = null;
		scientificAchievement.clear();
		scientificAchievement = null;
	}
}
