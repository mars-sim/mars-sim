/**
 * Mars Simulation Project
 * LaboratoryFacility.java
 * @version 2.74 2002-01-13
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure;

import org.mars_sim.msp.simulation.*;
import java.io.Serializable;
import java.util.Vector;

/** The LaboratoryFacility class represents the research laboratories
 *  in a settlement. A settlement may or may not have laboratories.
 */
public class LaboratoryFacility extends Facility implements Serializable {

    // Data members
    private int laboratorySize; // Size of collective laboratories (units defined later)
    private int technologyLevel; // How advanced the laboratories are (units defined later)
    private Vector techSpecialities; // What fields of science the laboratories specialize in.

    /** Constructor for random creation. 
     *  @param manager the laboratory's facility manager
     */
    LaboratoryFacility(FacilityManager manager) {

        // Use Facility's constructor.
        super(manager, "Research Laboratories");

        // Initialize random laboratorySize from 1 to 5.
        laboratorySize = 1 + RandomUtil.getRandomInt(4);

        // Initialize random technologyLevel from 1 to 5.
        technologyLevel = 1 + RandomUtil.getRandomInt(4);

        // Initialize techSpecialities from 1 to 5 technologies.
        techSpecialities = new Vector();
        String[] techs = { "Medical Research", "Areology", "Botany", "Physics", "Material Science" };

        while (techSpecialities.size() == 0) {
            for (int x = 0; x < techs.length; x ++) {
                if (RandomUtil.lessThanRandPercent(20)) {
                    techSpecialities.addElement(techs[x]);
                }
            }
        }
    }

    /** Constructor for set values (used later when facilities can be built or upgraded.) 
     *  @param manager the laboratory's facility manager
     *  @param size the size of the collective laboratories (units defined later)
     *  @param techlevel how advanced the laboratories are (units defined later)
     *  @param techFocus the names of the technologies the labs are focused on
     */
    LaboratoryFacility(FacilityManager manager, int size, int techLevel, String[] techFocus) {

        // Use Facility's constructor.
        super(manager, "Research Laboratories");

        // Initialize data members.
        laboratorySize = size;
        technologyLevel = techLevel;

        techSpecialities = new Vector();
        for (int x = 0; x < techFocus.length; x++) {
            techSpecialities.addElement(techFocus[x]);
        }
    }

    /** Returns the size of the settlement's laboratories (units defined later) 
     *  @return the size of the collective laboratories (units defined later)
     */
    public int getLaboratorySize() {
        return laboratorySize;
    }

    /** Returns the technology level of the settlement's laboratories
      *  (units defined later) 
      *  @return the technology level of the settlement's laboratories 
      *  (units defined later)
      */
    public int getTechnologyLevel() {
        return technologyLevel;
    }

    /** Returns the lab's science specialities as an array of Strings 
     *  @return the lab's science specialities as an array of Strings
     */
    public String[] getTechSpecialities() {
        String[] result = new String[techSpecialities.size()];
        for (int x = 0; x < techSpecialities.size(); x++) 
            result[x] = (String) techSpecialities.elementAt(x);
        
        return result;
    }
}

