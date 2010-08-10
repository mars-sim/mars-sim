/**
 * Mars Simulation Project
 * Lab.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.core;

/** 
 * The Lab interface represents a unit that can perform the function
 * of a research laboratory.
 */
public interface Lab {

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
     * Gets the lab's science specialities as an array of Strings. 
     * @return the lab's science specialities as an array of Strings
     */
    public String[] getTechSpecialities(); 

    /**
     * Checks to see if the laboratory has a given tech speciality.
     * @return true if lab has tech speciality
     */
    public boolean hasSpeciality(String speciality);
    
    /**
     * Gets the number of people currently researching in the laboratory.
     * @return number of researchers
     */
    public int getResearcherNum(); 

    /**
     * Adds a researcher to the laboratory.
     * @throws Exception if person cannot be added.
     */
    public void addResearcher() throws Exception; 

    /**
     * Removes a researcher from the laboratory.
     * @throws Exception if person cannot be removed.
     */
    public void removeResearcher() throws Exception; 
}
