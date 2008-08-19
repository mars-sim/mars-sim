/**
 * Mars Simulation Project
 * Research.java
 * @version 2.85 2008-08-18
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
 
/**
 * The Resource class is a building function for research.
 */
public class Research extends Function implements Lab, Serializable {

	public static final String NAME = "Research";

	private int techLevel;
	private int researcherCapacity;
	private List researchSpecialities;
	private int researcherNum;
	
	/**
	 * Constructor
	 * @param building the building this function is for.
	 * @throws BuildingException if function could not be constructed.
	 */
	public Research(Building building) throws BuildingException {
		// Use Function constructor
		super(NAME, building);
		
		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
			
		try {
			techLevel = config.getResearchTechLevel(building.getName());
			researcherCapacity = config.getResearchCapacity(building.getName());
			researchSpecialities = config.getResearchSpecialities(building.getName());
		}
		catch (Exception e) {
			throw new BuildingException("Research.constructor: " + e.getMessage());
		}
	}
    
    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     */
    public static final double getFunctionValue(String buildingName, boolean newBuilding, 
            Settlement settlement) {
        // TODO: Implement later as needed.
        return 0D;
    }
	
	/**
	 * Gets the research tech level of this building.
	 * @return tech level
	 */
	public int getTechnologyLevel() {
		return techLevel;
	}
	
	/**
	 * Gets the number of researchers who can use the laboratory at once.
	 * @return capacity
	 */
	public int getLaboratorySize() {
		return researcherCapacity;
	}
	
	/**
	 * Gets an array of the building's research tech specialities.
	 * @return array of specialities as strings.
	 */
	public String[] getTechSpecialities() {
		String[] result = new String[researchSpecialities.size()];
		for (int x=0; x < researchSpecialities.size(); x++)
			result[x] = (String) researchSpecialities.get(x);
		return result;
	}
	
	/**
	 * Checks to see if the laboratory has a given tech speciality.
	 * @return true if lab has tech speciality
	 */
	public boolean hasSpeciality(String speciality) {
		boolean result = false;
		Iterator i = researchSpecialities.iterator();
		while (i.hasNext()) {
			if (((String) i.next()).equalsIgnoreCase(speciality)) result = true;
		}
		return result;
	}
	
	/**
	 * Gets the number of people currently researching in the laboratory.
	 * @return number of researchers
	 */
	public int getResearcherNum() {
		return researcherNum;
	}

	/**
	 * Adds a researcher to the laboratory.
	 * @throws Exception if person cannot be added.
	 */
	public void addResearcher() throws Exception {
		researcherNum ++;
		if (researcherNum > researcherCapacity) {
			researcherNum = researcherCapacity;
			throw new Exception("Lab already full of researchers.");
		}
	}

	/**
	 * Removes a researcher from the laboratory.
	 * @throws Exception if person cannot be removed.
	 */
	public void removeResearcher() throws Exception {
		researcherNum --;
		if (getResearcherNum() < 0) {
			researcherNum = 0; 
			throw new Exception("Lab is already empty of researchers.");
		}
	}
	
	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) throws BuildingException {}
	
	/**
	 * Gets the amount of power required when function is at full power.
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return 0D;
	}
	
	/**
	 * Gets the amount of power required when function is at power down level.
	 * @return power (kW)
	 */
	public double getPowerDownPowerRequired() {
		return 0D;
	}
}