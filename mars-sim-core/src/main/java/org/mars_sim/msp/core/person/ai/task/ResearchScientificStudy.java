/**
 * Mars Simulation Project
 * ResearchScientificStudy.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.science.ScienceType;

/**
 * Interface for tasks the add research credit to scientific studies.
 */
public interface ResearchScientificStudy extends Serializable {

	/**
	 * Gets the scientific field that is being researched for the study.
	 * 
	 * @return scientific field.
	 */
	public ScienceType getResearchScience();

	/**
	 * Gets the researcher who is being assisted.
	 * 
	 * @return researcher.
	 */
	public Person getResearcher();

	/**
	 * Checks if there is a research assistant.
	 * 
	 * @return research assistant.
	 */
	public boolean hasResearchAssistant();

	/**
	 * Gets the research assistant.
	 * 
	 * @return research assistant or null if none.
	 */
	public Person getResearchAssistant();

	/**
	 * Sets the research assistant.
	 */
	public void setResearchAssistant(Person researchAssistant);
}