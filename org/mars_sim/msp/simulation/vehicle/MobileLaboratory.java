/**
 * Mars Simulation Project
 * MobileLaboratory.java
 * @version 2.74 2002-03-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.vehicle;

import org.mars_sim.msp.simulation.*;
import java.io.Serializable;
import java.util.*;

/** 
 * The MobileLaboratory class represents the research laboratory in a vehicle.
 */
public class MobileLaboratory implements Lab, Serializable {

    // Data members
    private int laboratorySize; // Number of researchers supportable at any given time. 
    private int technologyLevel; // How advanced the laboratories are (units defined later)
    private Collection techSpecialities; // What fields of science the laboratories specialize in.
    private int researcherNum; // The number of people currently doing research in laboratory.

    /** 
     * Constructor for parameter values. 
     * @param laboratorySize number of researchers the lab can support at any given time. 
     * @param techlevel how advanced the laboratories are (units defined later)
     * @param techFocus the names of the technologies the labs are focused on
     */
    MobileLaboratory(int size, int techLevel, String[] techFocus) {

        // Initialize data members.
        laboratorySize = size;
        technologyLevel = techLevel;

        techSpecialities = new ArrayList();
        for (int x = 0; x < techFocus.length; x++) {
            techSpecialities.add(techFocus[x]);
        }
    }

    /** 
     * Gets the laboratory size.
     * This is the number of researchers supportable at any given time. 
     * @return the size of the laboratory (in researchers). 
     */
    public int getLaboratorySize() {
        return laboratorySize;
    }

    /** 
     * Gets the technology level of the laboratory
     * (units defined later) 
     * @return the technology level laboratory 
     * (units defined later)
     */
    public int getTechnologyLevel() {
        return technologyLevel;
    }

    /** 
     * Gets the lab's science specialities as an array of Strings 
     * @return the lab's science specialities as an array of Strings
     */
    public String[] getTechSpecialities() {
        String[] result = new String[techSpecialities.size()];
	Iterator i = techSpecialities.iterator();
	int count = 0;
	while (i.hasNext()) {
            result[count] = (String) i.next();
	    count ++;
	}
        
        return result;
    }

    /**
     * Checks to see if the laboratory has a given tech speciality.
     * @return true if lab has tech speciality
     */
    public boolean hasSpeciality(String speciality) {
        boolean result = false;
	Iterator i = techSpecialities.iterator();
	while (i.hasNext()) {
	    if (speciality.equals((String) i.next())) result = true;
        }

	return result;
    }

    /**
     * Gets the number of people currently researching in laboratory.
     * @return number of researchers
     */
    public int getResearcherNum() {
        return researcherNum;
    }

    /**
     * Adds a researcher to the laboratory.
     */
    public void addResearcher() {
        researcherNum++;
	if (researcherNum > laboratorySize) researcherNum = laboratorySize;
    }

    /**
     * Removes a researcher from the laboratory.
     */
    public void removeResearcher() {
        researcherNum--;
	if (researcherNum < 0) researcherNum = 0;
    }
}
