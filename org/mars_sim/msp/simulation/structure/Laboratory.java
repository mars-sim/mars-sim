/**
 * Mars Simulation Project
 * Laboratory.java
 * @version 2.74 2002-04-21
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure;

import org.mars_sim.msp.simulation.*;
import java.io.Serializable;
import java.util.*;

/** 
 * The Laboratory class represents the research laboratories
 * in a settlement. A settlement may or may not have laboratories.
 */
public class Laboratory extends Facility implements Lab, Serializable {

    public final static String NAME = "Research Laboratories";
	
    // Data members
    private int laboratorySize; // Number of researchers supportable at any given time. 
    private int technologyLevel; // How advanced the laboratories are (units defined later)
    private Collection techSpecialities; // What fields of science the laboratories specialize in.
    private int researcherNum; // The number of people currently doing research in laboratory.

    /** 
     * Constructor for random creation. 
     * @param manager the laboratory's facility manager
     */
    Laboratory(FacilityManager manager) {

        // Use Facility's constructor.
        super(manager, NAME);

        // Add scope string to malfunction manager.
	malfunctionManager.addScopeString("Laboratory");
	
        // Initialize random laboratorySize from 1 to 5.
        laboratorySize = 1 + RandomUtil.getRandomInt(4);

        // Initialize random technologyLevel from 1 to 5.
        technologyLevel = 1 + RandomUtil.getRandomInt(4);

        // Initialize techSpecialities from 1 to 5 technologies.
        techSpecialities = new ArrayList();
	
	// All laboratories specialize in Areology.
	techSpecialities.add("Areology");

        String[] techs = { "Medical Research", "Botany", "Physics", "Material Science" };

        while (techSpecialities.size() == 1) {
            for (int x = 0; x < techs.length; x ++) {
                if (RandomUtil.lessThanRandPercent(25)) {
                    techSpecialities.add(techs[x]);
                }
            }
        }
    }

    /** 
     * Constructor for set values (used later when facilities can be built or upgraded.) 
     * @param manager the laboratory's facility manager
     * @param size the size of the collective laboratories (units defined later)
     * @param techlevel how advanced the laboratories are (units defined later)
     * @param techFocus the names of the technologies the labs are focused on
     */
    Laboratory(FacilityManager manager, int size, int techLevel, String[] techFocus) {

        // Use Facility's constructor.
        super(manager, "Research Laboratories");

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
     * Gets the technology level of the settlement's laboratories
     * (units defined later) 
     * @return the technology level of the settlement's laboratories 
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

    /**
     * Time passing for laboratory.
     * @param time the amount of time passing (millisols)
     */
    public void timePassing(double time) {
        if (researcherNum > 0) malfunctionManager.activeTimePassing(time);
    }
}
