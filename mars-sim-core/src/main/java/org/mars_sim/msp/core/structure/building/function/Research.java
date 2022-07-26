/*
 * Mars Simulation Project
 * Research.java
 * @date 2022-07-16
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.FunctionSpec;
import org.mars_sim.msp.core.time.ClockPulse;

/**
 * The Research class is a building function for research.
 */
public class Research
extends Function
implements Lab {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
	private static final SimLogger logger = SimLogger.getLogger(Research.class.getName());

	private static final int NUM_INSPECTIONS = 2;
	
    private int techLevel;
    private int researcherCapacity = 0;
    private int researcherNum = 0;

    private double quality = 100;
    
    private String owner;
    
    private Map<Part, Integer> partsMap; 
    
    private List<ScienceType> researchSpecialties;
    
    /** 
     * This map records the quality of the research on
     * a science subject in the form of a score. 
     * It can go up and down over time. 
     */
    private Map<ScienceType, Double> researchQualityMap;
    
    /** 
     * This map is the log book for tallying the # of daily 
     * inspections on the tissue cultures that this lab maintains.
     */
    private Map<String, Integer> tissueCultureInspection;
    
    /** 
     * The amount of tissue cultures that this lab maintains.
     */
    private Map<String, Double> tissueCultureAmount;
    
    /**
     * Constructor.
     * 
     * @param building the building this function is for.
     */
    public Research(Building building, FunctionSpec spec) {
        // Use Function constructor
        super(FunctionType.RESEARCH, spec, building);

        setupTissueCultures();
        
        techLevel = spec.getTechLevel();
        researcherCapacity = spec.getCapacity();
        researchSpecialties = buildingConfig.getResearchSpecialties(building.getBuildingType());
        researchQualityMap = new HashMap<>();
        
        // Initialize the research quality map
        for (ScienceType scienceType : researchSpecialties) {
        	researchQualityMap.put(scienceType, techLevel * 1.0);
        }
    }

    /**
     * Gets the value of the function for a named building type.
     * 
     * @param type the building type.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     */
    public static double getFunctionValue(String type, boolean newBuilding,
            Settlement settlement) {

        double result = 0D;

        for (ScienceType specialty : buildingConfig.getResearchSpecialties(type)) {
            double researchDemand = 0D;
            for(Person p : settlement.getAllAssociatedPeople()) {
                researchDemand += p.getSkillManager().getSkillLevel(specialty.getSkill());
            }

            double researchSupply = 0D;
            boolean removedBuilding = false;

            for (Building building : settlement.getBuildingManager().getBuildings(FunctionType.RESEARCH)) {
                if (!newBuilding && building.getBuildingType().equalsIgnoreCase(type) && !removedBuilding) {
                    removedBuilding = true;
                }
                else {
                    Research researchFunction = building.getResearch();
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

            FunctionSpec spec = buildingConfig.getFunctionSpec(type, FunctionType.RESEARCH);
            int techLevel = spec.getTechLevel();
            int labSize = spec.getCapacity();
            int buildingResearchSupply = techLevel * labSize;

            result += buildingResearchSupply * existingResearchValue;
        }

        return result;
    }

    /**
     * Gets the research tech level of this building.
     * 
     * @return tech level
     */
    public int getTechnologyLevel() {
        return techLevel;
    }

    /**
     * Gets the number of researchers who can use the laboratory at once.
     * 
     * @return capacity
     */
    public int getLaboratorySize() {
        return researcherCapacity;
    }

    /**
     * Gets an array of the building's research tech specialties.
     * 
     * @return array of specialties.
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
        return researchSpecialties.contains(specialty);
    }

    /**
     * Gets the number of people currently researching in the laboratory.
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
        }
        else {
            researcherNum ++;
            return true;
        }
    }

    /**
     * Checks if there is an available slot in the laboratory.
     * 
     * @throws Exception if person cannot be added.
     */
    public Boolean checkAvailability() {
        return researcherNum < researcherCapacity;
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
			logger.severe(building, "Lab has no researchers to remove.");
        }
    }
	
    /**
     * Time passing for the building.
     * 
     * @param time amount of time passing (in millisols)
     * @throws BuildingException if error occurs.
     */
    @Override
    public boolean timePassing(ClockPulse pulse) {
		boolean valid = isValid(pulse);
		if (valid) {
			if (pulse.isNewSol()) {
                tissueCultureInspection.replaceAll((s, v) -> 0);
			}
		}
		return valid;
    }

    private void setupTissueCultures() {
       	tissueCultureInspection = new HashMap<>();
       	tissueCultureAmount = new HashMap<>();
    }
    
	public List<String> getUncheckedTissues() {
		List<String> batch = new ArrayList<>();
		for (String s : tissueCultureInspection.keySet()) {
			if (tissueCultureInspection.get(s) < NUM_INSPECTIONS)
				batch.add(s);
		}
		return batch;
	}

    public void markChecked(String s) {
    	tissueCultureInspection.put(s, tissueCultureInspection.get(s) + 1);
    }
    
    
    /**
     * Checks if the lab has tissue culture in stock
     * 
     * @param tissueName
     * @return true if the lab has it
     */
    public boolean hasTissueCulture(String tissueName) {
    	if (tissueCultureAmount.containsKey(tissueName)
    		&& tissueCultureAmount.get(tissueName) > 0) {
    			return true;
    	}

    	return false;
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
}
