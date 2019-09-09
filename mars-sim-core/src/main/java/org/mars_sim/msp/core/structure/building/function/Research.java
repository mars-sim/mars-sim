/**
 * Mars Simulation Project
 * Research.java
 * @version 3.1.0 2017-09-18
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The Research class is a building function for research.
 */
public class Research
extends Function
implements Lab, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
	private static transient Logger logger = Logger.getLogger(Research.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());
	
    private static final FunctionType FUNCTION = FunctionType.RESEARCH;

	private static final int NUM_INSPECTIONS = 2;

    private int techLevel;
    private int researcherCapacity = 0;
    private int researcherNum = 0;
    private int solCache;
    
    private List<ScienceType> researchSpecialties;

    /** This map is the log book for tallying the # of daily inspections on the tissue cultures that this lab maintains */
    private Map<String, Integer> tissueCultureMap;
    //private List<String> tissueCultureList;
    
    private Building building;
    

    /**
     * Constructor.
     * @param building the building this function is for.
     */
    public Research(Building building) {
        // Use Function constructor
        super(FUNCTION, building);

        setupTissueCultures();
        
        String type = building.getBuildingType();
        techLevel = buildingConfig.getResearchTechLevel(type);
        researcherCapacity = buildingConfig.getResearchCapacity(type);
        researchSpecialties = buildingConfig.getResearchSpecialties(type);

        // Load activity spots
        loadActivitySpots(buildingConfig.getResearchActivitySpots(type));
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

        List<ScienceType> specialties = buildingConfig.getResearchSpecialties(buildingName);

        for (ScienceType specialty : specialties) {
            double researchDemand = 0D;
            Iterator<Person> j = settlement.getAllAssociatedPeople().iterator();
            while (j.hasNext())
                researchDemand += j.next().getSkillManager().getSkillLevel(specialty.getSkill());

            double researchSupply = 0D;
            boolean removedBuilding = false;

            List<Building> b_list = settlement.getBuildingManager().getBuildings(FUNCTION);
            for (Building building : b_list) {
                if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
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

            int techLevel = buildingConfig.getResearchTechLevel(buildingName);
            int labSize = buildingConfig.getResearchCapacity(buildingName);
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
    	//System.out.println("lab : " + researcherNum + " of " + researcherCapacity);
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
            Settlement s = building.getSettlement();
			LogConsolidated.log(Level.SEVERE, 5000, sourceName,
					"[" + s + "] "
					+ building + "'s lab has no researchers.");
//            throw new IllegalStateException("Lab is already empty of researchers.");
        }
    }
	
    /**
     * Time passing for the building.
     * @param time amount of time passing (in millisols)
     * @throws BuildingException if error occurs.
     */
    public void timePassing(double time) {
    	
	    // check for the passing of each day
	    int solElapsed = marsClock.getMissionSol();
	    if (solCache != solElapsed) {
			solCache = solElapsed;
			
			for (String s : tissueCultureMap.keySet()) {
				tissueCultureMap.put(s, 0);
			}

		}
    	
    }

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

    public void setupTissueCultures() {
       	tissueCultureMap = new HashMap<>();

//        Set<AmountResource> tissues = SimulationConfig.instance().getResourceConfiguration().getTissueCultures();
//        for (AmountResource ar : tissues) {
//        	String s = ar.getName();
//        	tissueCultureMap.put(s, 0);
//        }	
        
    }
    
	public List<String> getUncheckedTissues() {
		List<String> batch = new ArrayList<>();
		for (String s : tissueCultureMap.keySet()) {
			if (tissueCultureMap.get(s) < NUM_INSPECTIONS)
				batch.add(s);
		}
		return batch;
	}
	
    public void markChecked(String s) {
    	tissueCultureMap.put(s, tissueCultureMap.get(s) + 1);
    }
    
    
    /**
     * Checks if the lab has tissue culture in stock
     * 
     * @param tissueName
     * @return true if the lab has it
     */
    public boolean hasTissueCulture(String tissueName) {
    	if (!tissueCultureMap.containsKey(tissueName)) {
    		tissueCultureMap.put(tissueName, 0);
    		return false;
    	}
    	return true;
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
	public double getFullHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	   @Override
	    public void destroy() {
	        super.destroy();
	        researchSpecialties.clear();
	        researchSpecialties = null;
	    }
}