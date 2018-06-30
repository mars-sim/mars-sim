/**
 * Mars Simulation Project
 * Lab.java
 * @version 3.1.0 2017-11-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure;

import java.io.Serializable;

import org.mars_sim.msp.core.science.ScienceType;

/**
 * The Lab interface represents a unit that can perform the function
 * of a research laboratory.
 */
public interface Lab extends Serializable {

	/** 
	 * Gets the laboratory size.
	 * This is the number of researchers supportable at any given time. 
	 * @return the size of the laboratory (in researchers). 
	 */
	public int getLaboratorySize(); 

	/** 
	 * Gets the technology level of laboratory.
	 * (units defined later) 
	 * @return the technology level of the laboratory 
	 * (units defined later)
	 */
	public int getTechnologyLevel();

    /** 
     * Gets the lab's science specialties .
     * @return the lab's science specialties
     */
    public ScienceType[] getTechSpecialties();

    /**
     * Checks to see if the laboratory has a given tech specialty.
     * @return true if lab has tech specialty
     */
    public boolean hasSpecialty(ScienceType specialty);
    
    /**
     * Gets the number of people currently researching in the laboratory.
     * @return number of researchers
     */
    public int getResearcherNum(); 

	/**
	 * Adds a researcher to the laboratory.
	 * @return 
	 * @throws Exception if person cannot be added.
	 */
	public boolean addResearcher();

	/**
	 * Removes a researcher from the laboratory.
	 * @throws Exception if person cannot be removed.
	 */
	public void removeResearcher();

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy();
}
