/**
 * Mars Simulation Project
 * Research.java
 * @version 3.07 2014-06-19
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Lab;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;

/**
 * The Research class is a building function for research.
 */
public class Research
extends Function
implements Lab, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    private static final FunctionType FUNCTION = FunctionType.RESEARCH;

    private int techLevel;
    private int researcherCapacity;
    private List<ScienceType> researchSpecialties;
    private int researcherNum;

    /**
     * Constructor.
     * @param building the building this function is for.
     */
    public Research(Building building) {
        // Use Function constructor
        super(FUNCTION, building);

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

        techLevel = config.getResearchTechLevel(building.getBuildingType());
        researcherCapacity = config.getResearchCapacity(building.getBuildingType());
        researchSpecialties = config.getResearchSpecialties(building.getBuildingType());

        // Load activity spots
        loadActivitySpots(config.getResearchActivitySpots(building.getBuildingType()));
    }

    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     */
    public static double getFunctionValue(String buildingName, boolean newBuilding,
            Settlement settlement) {

        double result = 0D;

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
        List<ScienceType> specialties = config.getResearchSpecialties(buildingName);

        for (ScienceType specialty : specialties) {
            double researchDemand = 0D;
            Iterator<Person> j = settlement.getAllAssociatedPeople().iterator();
            while (j.hasNext())
                researchDemand += j.next().getMind().getSkillManager().getSkillLevel(specialty.getSkill());

            double researchSupply = 0D;
            boolean removedBuilding = false;
            Iterator<Building> k = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
            while (k.hasNext()) {
                Building building = k.next();
                if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
                    removedBuilding = true;
                }
                else {
                    Research researchFunction = (Research) building.getFunction(FUNCTION);
                    int techLevel = researchFunction.techLevel;
                    int labSize = researchFunction.researcherCapacity;
                    double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
                    for (int x = 0; x < researchFunction.getTechSpecialties().length; x++) {
                        ScienceType researchSpecialty = researchFunction.getTechSpecialties()[x];
                        if (specialty.equals(researchSpecialty)) {
                            researchSupply += techLevel * labSize * wearModifier;
                        }
                    }
                }
            }

            double existingResearchValue = researchDemand / (researchSupply + 1D);

            int techLevel = config.getResearchTechLevel(buildingName);
            int labSize = config.getResearchCapacity(buildingName);
            double buildingResearchSupply = techLevel * labSize;

            result += buildingResearchSupply * existingResearchValue;
        }

        return result;
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
     * Gets an array of the building's research tech specialties.
     * @return array of specialties.
     */
    public ScienceType[] getTechSpecialties() {
        return researchSpecialties.toArray(new ScienceType[] {});
    }

    /**
     * Checks to see if the laboratory has a given tech specialty.
     * @return true if lab has tech specialty
     */
    public boolean hasSpecialty(ScienceType specialty) {
        return researchSpecialties.contains(specialty);
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
     * Checks if there is an available slot in the laboratory.
     * @throws Exception if person cannot be added.
     */
    public Boolean checkAvailability() {
        if (researcherNum < researcherCapacity) {
            return true;
        }
        else
        	return false;
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
    public double getPoweredDownPowerRequired() {
        return 0D;
    }

    @Override
    public double getMaintenanceTime() {

        double result = 0D;

        // Add maintenance for tech level.
        result += techLevel * 10D;

        // Add maintenance for researcher capacity.
        result += researcherCapacity * 10D;

        return result;
    }

    @Override
    public void destroy() {
        super.destroy();
        researchSpecialties.clear();
        researchSpecialties = null;
    }

	@Override
	public double getFullHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}
}