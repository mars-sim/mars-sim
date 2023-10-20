/*
 * Mars Simulation Project
 * Researcher.java
 * @date 2022-10-05
 * @author Manny Kung
 */

package com.mars_sim.core.science;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mars_sim.core.moon.Colonist;

public class Researcher extends Colonist implements ResearcherInterface {
	/** default logger. */
	// May add back private static final SimLogger logger = SimLogger.getLogger(Researcher.class.getName())
	
	/** The person's current scientific study. */
	private ScientificStudy study;

	/** The person's achievement in scientific fields. */
	private Map<ScienceType, Double> scientificAchievement = new ConcurrentHashMap<>();
	/** The person's list of collaborative scientific studies. */
	private Set<ScientificStudy> collabStudies;
	
	public Researcher(String name, int colonyId) {
		super(name, colonyId);
	}
	
	/**
	 * Sets the study that this researcher is the lead on.
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
	 * Gets the researcher's achievement credit for a given scientific field.
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
	 * Gets the researcher's total scientific achievement credit.
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
	 * Adds achievement credit to the researcher in a scientific field.
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

}
