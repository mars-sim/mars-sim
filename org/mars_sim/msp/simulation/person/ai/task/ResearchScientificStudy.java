/**
 * Mars Simulation Project
 * ResearchScientificStudy.java
 * @version 2.87 2009-11-11
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.task;

import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.science.Science;

/**
 * Interface for tasks the add research credit to scientific studies.
 */
public interface ResearchScientificStudy {

    /**
     * Gets the scientific field that is being researched for the study.
     * @return scientific field.
     */
    public Science getResearchScience();
    
    /**
     * Gets the researcher who is being assisted.
     * @return researcher.
     */
    public Person getResearcher();
    
    /**
     * Checks if there is a research assistant.
     * @return research assistant.
     */
    public boolean hasResearchAssistant();
    
    /**
     * Gets the research assistant.
     * @return research assistant or null if none.
     */
    public Person getResearchAssistant();
    
    /**
     * Sets the research assistant.
     */
    public void setResearchAssistant(Person researchAssistant);
}