/**
 * Mars Simulation Project
 * StandardResearch.java
 * @version 2.75 2003-04-16
 * @author Scott Davis
 */
 
package org.mars_sim.msp.simulation.structure.building.function.impl;
 
import java.io.Serializable;
import org.mars_sim.msp.simulation.person.PersonIterator;
import org.mars_sim.msp.simulation.person.ai.StudyRockSamples;
import org.mars_sim.msp.simulation.structure.building.InhabitableBuilding;
import org.mars_sim.msp.simulation.structure.building.function.Research;
 
/**
 * Standard implementation of the Research building function.
 */
public class StandardResearch implements Research, Serializable {
    
    private InhabitableBuilding building;
    private int techLevel;
    private String[] specialities;
    private int labSize;
    
    /**
     * Constructor
     *
     * @param building the building this is implemented for.
     * @param techLevel the tech level of the lab.
     * @param specialities the scientific specialities of the lab.
     * @param the number of researchers the lab can hold.
     */
    public StandardResearch(InhabitableBuilding building, int techLevel, String[] specialities, int labSize) {
        this.building = building;
        this.techLevel = techLevel;
        this.specialities = specialities;
        this.labSize = labSize;
    }
    
    /** 
     * Gets the laboratory size.
     * This is the number of researchers supportable at any given time. 
     * @return the size of the laboratory (in researchers). 
     */
    public int getLaboratorySize() {
        return labSize;
    }

    /** 
     * Gets the technology level of laboratory
     * (units defined later) 
     * @return the technology level of the laboratory 
     * (units defined later)
     */
    public int getTechnologyLevel() {
        return techLevel;
    }

    /** 
     * Gets the lab's science specialities as an array of Strings 
     * @return the lab's science specialities as an array of Strings
     */
    public String[] getTechSpecialities() {
        return specialities;
    }

    /**
     * Checks to see if the laboratory has a given tech speciality.
     * @return true if lab has tech speciality
     */
    public boolean hasSpeciality(String speciality) {
        boolean result = false;
        for (int x = 0; x < specialities.length; x++) {
            if (speciality.equals(specialities[x])) result = true;
        }
        
        return result;
    }
    
    /**
     * Gets the number of people currently researching in the laboratory.
     * @return number of researchers
     */
    public int getResearcherNum() {
        int researcherNum = 0;
        PersonIterator i = building.getOccupants().iterator();
        while (i.hasNext()) {
            if (i.next().getMind().getTaskManager().getTask() instanceof StudyRockSamples) researcherNum++;
        }
        return researcherNum;
    }

    /**
     * Adds a researcher to the laboratory.
     * @throws Exception if person cannot be added.
     */
    public void addResearcher() throws Exception {
        if (getResearcherNum() == getLaboratorySize()) 
            throw new Exception("Lab already full of researchers.");
    }

    /**
     * Removes a researcher from the laboratory.
     * @throws Exception if person cannot be removed.
     */
    public void removeResearcher() throws Exception {
        if (getResearcherNum() == 0) 
            throw new Exception("Lab is already empty of researchers.");
    } 
}
