/**
 * Mars Simulation Project
 * MobileLaboratory.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core.vehicle;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Lab;
import org.mars_sim.msp.core.science.ScienceType;

/** 
 * The MobileLaboratory class represents the research laboratory in a vehicle.
 */
public class MobileLaboratory
implements Lab, Serializable {

    /** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
    /** Number of researchers supportable at any given time. */
	private int laboratorySize; 
    /** How advanced the laboratories are (units defined later). */
	private int technologyLevel;
    /** What fields of science the laboratories specialize in. */
	private List<ScienceType> techSpecialities;
    /** The number of people currently doing research in laboratory. */
	private int researcherNum;

    /** 
     * Constructor for parameter values. 
     * @param laboratorySize number of researchers the lab can support at any given time. 
     * @param techlevel how advanced the laboratories are (units defined later)
     * @param techFocus the names of the technologies the labs are focused on
     */
    MobileLaboratory(int size, int techLevel, List<ScienceType> techSpecialities) {

        // Initialize data members.
        this.laboratorySize = size;
        this.technologyLevel = techLevel;
        this.techSpecialities = techSpecialities;
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
     * Gets the lab's science specialities as an array.
     * @return the lab's science specialities as an array.
     */
    public ScienceType[] getTechSpecialities() {
        return techSpecialities.toArray(new ScienceType[] {});
    }

    /**
     * Checks to see if the laboratory has a given tech speciality.
     * @return true if lab has tech speciality
     */
    public boolean hasSpeciality(ScienceType speciality) {
        boolean result = false;
        Iterator<ScienceType> i = techSpecialities.iterator();
        while (!result && i.hasNext()) {
            if (speciality == i.next()) result = true;
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
     * @throws Exception if person cannot be added.
     */
    public void addResearcher() {
    	researcherNum ++;
        if (researcherNum > laboratorySize) {
        	 researcherNum = laboratorySize;
            throw new IllegalStateException("Lab already full of researchers.");
        }
    }

    /**
     * Removes a researcher from the laboratory.
     * @throws Exception if person cannot be removed.
     */
    public void removeResearcher() {
    	researcherNum --;
        if (researcherNum < 0) { 
        	researcherNum = 0;
            throw new IllegalStateException("Lab is already empty of researchers.");
        }
    }

    @Override
    public void destroy() {
        techSpecialities.clear();
        techSpecialities = null;
    }
}