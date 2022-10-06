/*
 * Mars Simulation Project
 * ResearcherInterface.java
 * @date 2022-10-05
 * @author Manny Kung
 */

package org.mars_sim.msp.core.science;

import java.util.Set;

public interface ResearcherInterface {

	/**
	 * Set the study that this Person is the lead on.
	 * 
	 * @param scientificStudy
	 */
	public void setStudy(ScientificStudy scientificStudy);
	
	/**
	 * Gets the scientific study instance.		
	 */
	public ScientificStudy getStudy();

	/**
	 * Gets the collaborative study sets.
	 */
	public Set<ScientificStudy> getCollabStudies();
	
	/**
	 * Adds the collaborative study.
	 * 
	 * @param study
	 */
	public void addCollabStudy(ScientificStudy study);

	/**
	 * Removes the collaborative study.
	 * 
	 * @param study
	 */
	public void removeCollabStudy(ScientificStudy study);

	/**
	 * Gets the person's achievement credit for a given scientific field.
	 *
	 * @param science the scientific field.
	 * @return achievement credit.
	 */
	public double getScientificAchievement(ScienceType science);

	/**
	 * Gets the person's total scientific achievement credit.
	 *
	 * @return achievement credit.
	 */
	public double getTotalScientificAchievement();

	/**
	 * Adds achievement credit to the person in a scientific field.
	 *
	 * @param achievementCredit the achievement credit.
	 * @param science           the scientific field.
	 */
	public void addScientificAchievement(double achievementCredit, ScienceType science);
	
}
