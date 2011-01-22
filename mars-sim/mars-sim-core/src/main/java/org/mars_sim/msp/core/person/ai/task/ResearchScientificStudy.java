/**
 * Mars Simulation Project
 * ResearchScientificStudy.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.science.Science;

import java.io.Serializable;

/**
 * Interface for tasks the add research credit to scientific studies.
 */
public interface ResearchScientificStudy extends Serializable{

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