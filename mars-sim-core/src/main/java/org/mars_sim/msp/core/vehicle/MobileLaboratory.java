/*
 * Mars Simulation Project
 * MobileLaboratory.java
 * @date 2023-08-11
 * @author Barry Evans
 */

package org.mars_sim.msp.core.vehicle;

import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Lab;

/**
 * The MobileLaboratory class represents the research laboratory in a vehicle.
 */
public class MobileLaboratory implements Lab {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    // Data members
    /** Number of researchers supported at any given time. */
    private int researcherCapacity;
    /** How advanced the laboratory is. */
    private int technologyLevel;
    /** What fields of science the laboratory specialize in. */
    private List<ScienceType> researchSpecialties;
    /** The number of people currently doing research in laboratory. */
    private int researcherNum;

    /**
     * Constructor for parameter values.
     *
     * @param researcherCapacity number of researchers the lab can support at any given time.
     * @param techlevel how advanced the laboratories are (units defined later)
     * @param techFocus the names of the technologies the labs are focused on
     */
    MobileLaboratory(int size, int techLevel, List<ScienceType> techSpecialties) {

        // Initialize data members.
        this.researcherCapacity = size;
        this.technologyLevel = techLevel;
        this.researchSpecialties = techSpecialties;
    }

    /**
     * Gets the laboratory size.
     * This is the number of researchers supportable at any given time.
     *
     * @return the size of the laboratory (in researchers).
     */
    public int getLaboratorySize() {
        return researcherCapacity;
    }

    /**
     * Gets the technology level of the laboratory
     * (units defined later)
     *
     * @return the technology level laboratory
     * (units defined later)
     */
    public int getTechnologyLevel() {
        return technologyLevel;
    }

    /**
     * Gets the lab's science specialties as an array.
     *
     * @return the lab's science specialties as an array.
     */
    public ScienceType[] getTechSpecialties() {
        return researchSpecialties.toArray(new ScienceType[] {});
    }

    /**
     * Checks to see if the laboratory has a given tech specialty.
     *
     * @return true if lab has tech specialty
     */
    public boolean hasSpecialty(ScienceType specialty) {
        boolean result = false;
        Iterator<ScienceType> i = researchSpecialties.iterator();
        while (i.hasNext()) {
            if (specialty == i.next()) result = true;
        }

        return result;
    }

    /**
     * Gets the number of people currently researching in laboratory.
     *
     * @return number of researchers
     */
    public int getResearcherNum() {
        return researcherNum;
    }

    /**
     * Adds a researcher to the laboratory.
     *
     * @return true if the person can be added.
     */
    public boolean addResearcher() {

        if (researcherNum > researcherCapacity) {
            researcherNum = researcherCapacity;
            return false;
            //throw new IllegalStateException("Lab already full of researchers.");
        }
        else {
            researcherNum ++;
            return true;
        }
    }

    /**
     * Removes a researcher from the laboratory.
     *
     * @throws Exception if person cannot be removed.
     */
    public void removeResearcher() {
    	researcherNum --;
        if (researcherNum < 0) {
        	researcherNum = 0;
        	// Seems too harsh throwing an exception
            //throw new IllegalStateException("Lab is already empty of researchers.");
        }
    }

    @Override
    public void destroy() {
        researchSpecialties.clear();
        researchSpecialties = null;
    }
}
