/**
 * Mars Simulation Project
 * Research.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Lab;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
 
/**
 * The Research class is a building function for research.
 */
public class Research extends Function implements Lab, Serializable {

	public static final String NAME = "Research";

	private int techLevel;
	private int researcherCapacity;
	private List<String> researchSpecialities;
	private int researcherNum;
	
	/**
	 * Constructor
	 * @param building the building this function is for.
	 * @throws BuildingException if function could not be constructed.
	 */
	public Research(Building building) {
		// Use Function constructor
		super(NAME, building);
		
		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
			
//		try {
			techLevel = config.getResearchTechLevel(building.getName());
			researcherCapacity = config.getResearchCapacity(building.getName());
			researchSpecialities = config.getResearchSpecialities(building.getName());
//		}
//		catch (Exception e) {
//			throw new BuildingException("Research.constructor: " + e.getMessage());
//		}
	}
    
    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     * @throws Exception if error getting function value.
     */
    public static double getFunctionValue(String buildingName, boolean newBuilding,
            Settlement settlement) {
        
        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
        List<String> specialities = config.getResearchSpecialities(buildingName);
        
        double researchDemand = 0D;
        Iterator<String> i = specialities.iterator();
        while (i.hasNext()) {
            String speciality = i.next();
            Iterator<Person> j = settlement.getAllAssociatedPeople().iterator();
            while (j.hasNext()) 
                researchDemand += j.next().getMind().getSkillManager().getSkillLevel(speciality);
        }
        
        double researchSupply = 0D;
        boolean removedBuilding = false;
        Iterator<Building> k = settlement.getBuildingManager().getBuildings(NAME).iterator();
        while (k.hasNext()) {
            Building building = k.next();
            if (!newBuilding && building.getName().equalsIgnoreCase(buildingName) && !removedBuilding) {
                removedBuilding = true;
            }
            else {
                Research researchFunction = (Research) building.getFunction(NAME);
                int techLevel = researchFunction.techLevel;
                int labSize = researchFunction.researcherCapacity;
                double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
                for (int x = 0; x < researchFunction.getTechSpecialities().length; x++) {
                    String speciality = researchFunction.getTechSpecialities()[x];
                    if (specialities.contains(speciality)) researchSupply += techLevel * labSize * wearModifier;
                }
            }
        }
        
        double existingResearchValue = researchDemand / (researchSupply + 1D);
        
        int techLevel = config.getResearchTechLevel(buildingName);
        int labSize = config.getResearchCapacity(buildingName);
        double buildingResearchSupply = specialities.size() * techLevel * labSize;
        
        return buildingResearchSupply * existingResearchValue;
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
			result[x] = researchSpecialities.get(x);
		return result;
	}
	
	/**
	 * Checks to see if the laboratory has a given tech speciality.
	 * @return true if lab has tech speciality
	 */
	public boolean hasSpeciality(String speciality) {
		boolean result = false;
		Iterator<String> i = researchSpecialities.iterator();
		while (i.hasNext()) {
			if (i.next().equalsIgnoreCase(speciality)) result = true;
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
	public void addResearcher() {
		researcherNum ++;
		if (researcherNum > researcherCapacity) {
			researcherNum = researcherCapacity;
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
	
	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) {}
	
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