/**
 * Mars Simulation Project
 * LaboratoryFacility.java
 * @version 2.71 2000-09-17
 * @author Scott Davis
 */

import java.util.*;

/** The LaboratoryFacility class represents the research laboratories
 *  in a settlement. A settlement may or may not have laboratories.
 */
public class LaboratoryFacility extends Facility {

    private int laboratorySize;         // Size of collective laboratories (units defined later)
    private int technologyLevel;        // How advanced the laboratories are (units defined later)
    private Vector techSpecialities;  // What fields of science the laboratories specialize in.

    public LaboratoryFacility(FacilityManager manager) {
	
	// Use Facility's constructor.
	super(manager, "Research Laboratories");
		
	// Initialize random laboratorySize from 1 to 5.
	laboratorySize = 1 + RandomUtil.getRandomInteger(4);
		
	// Initialize random technologyLevel from 1 to 5.
	technologyLevel = 1 + RandomUtil.getRandomInteger(4);
		
	// Initialize techSpecialities from 1 to 5 technologies.
	techSpecialities = new Vector();
	String[] techs = { "Medical Research", "Areology", "Botany", "Physics", "Material Science" };
		
	while (techSpecialities.size() == 0) {
	    for (int x=0; x < techs.length; x ++) {
		if (RandomUtil.lessThanRandPercent(20)) {
		    techSpecialities.addElement(techs[x]);
		}
	    }
	}
    }
	
    /** Constructor for set values (used later when facilities can be built or upgraded.) */
    public LaboratoryFacility(FacilityManager manager, int size, int techLevel, String[] techFocus) {
	
	// Use Facility's constructor.
	super(manager, "Research Laboratories");
		
	// Initialize data members.
	laboratorySize = size;
	technologyLevel = techLevel;
		
	techSpecialities = new Vector();
	for (int x=0; x < techFocus.length; x++) {
	    techSpecialities.addElement(techFocus[x]);
	}
    }
	
    /** Returns the size of the settlement's laboratories (units defined later) */
    public int getLaboratorySize() { return laboratorySize; }
	
    /** Returns the technology level of the settlement's laboratories
     *  (units defined later) */
    public int getTechnologyLevel() { return technologyLevel; }
	
    /** Returns the lab's science specialities as an array of Strings */
    public String[] getTechSpecialities() { 
	String[] result = new String[techSpecialities.size()];
	for (int x=0; x < techSpecialities.size(); x++) {
	    result[x] = (String) techSpecialities.elementAt(x);
	}
	return result;
    }	
}	
