/*
 * Mars Simulation Project
 * Research.java
 * @date 2023-08-11
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.mars_sim.msp.core.data.SolSingleMetricDataLogger;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Lab;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.FunctionSpec;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;
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

	/** The maximum number of sols for storing stats. */
	public static final int MAX_NUM_SOLS = 100;
	
	private static final int NUM_INSPECTIONS = 3;
	
    /** Number of researchers supported at any given time. */
    private int researcherCapacity;
    /** How advanced the laboratory is. */
    private int technologyLevel;
    /** The number of people currently doing research in laboratory. */
    private int researcherNum = 0;
    
    /** The usage history in millisols per researcher. */
    private SolSingleMetricDataLogger history = new SolSingleMetricDataLogger(MAX_NUM_SOLS);

    /** What fields of science the laboratory specialize in. */
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
     * The incubator in which crop tissue grows
     */
	private Map<String, Double> tissueIncubator;
	
    /**
     * Constructor.
     * 
     * @param building the building this function is for.
     */
    public Research(Building building, FunctionSpec spec) {
        // Use Function constructor
        super(FunctionType.RESEARCH, spec, building);

        setupTissueCultures();
        
        technologyLevel = spec.getTechLevel();
        researcherCapacity = spec.getCapacity();
        researchSpecialties = buildingConfig.getResearchSpecialties(building.getBuildingType());
        researchQualityMap = new HashMap<>();
		
        // Initialize the research quality map
        for (ScienceType scienceType : researchSpecialties) {
        	researchQualityMap.put(scienceType, technologyLevel * 1.0);
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

            for (Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.RESEARCH)) {
                if (!newBuilding && building.getBuildingType().equalsIgnoreCase(type) && !removedBuilding) {
                    removedBuilding = true;
                }
                else {
                    Research researchFunction = building.getResearch();
                    int techLevel = researchFunction.technologyLevel;
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
        return technologyLevel;
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
    public boolean checkAvailability() {
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
		if (!valid) {
			return false;
		}
		
		if (researcherNum > 0)
			recordUsage(researcherNum * pulse.getElapsed());
		
		if (pulse.isNewSol()) {
            tissueCultureInspection.replaceAll((s, v) -> 0);
		}
		
		if (pulse.isNewMSol()) {
			Map<String, Double> newMap = new HashMap<>();
			Iterator<Map.Entry<String, Double>> i = tissueIncubator.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry<String, Double> entry = i.next();
				String key = entry.getKey();
				double amount = entry.getValue();
				if (amount > 0) {
					i.remove();
					
					double time = pulse.getMarsTime().getMillisol();
					double delta = obtainGrow(time, key);
					
					// Increase the amount by millisols / 1000.0 
					newMap.put(key, amount * (1 + delta)/1000.0);
				}
			}
			tissueIncubator.putAll(newMap);
		}
		
		return valid;
    }

	/**
	 * Returns both the cumulative total and the daily average.
	 * 
	 * @return
	 */
	public double[] getTotCumulativeDailyAverage() {
		return history.getTotCumulativeDailyAverage();
	}
    
	/**
	 * Records the usage.
	 * 
	 * @param time in millisols
	 */
	public void recordUsage(double time) {
		history.increaseDataPoint(time);
	}
	
	/**
	 * Returns the usage.
	 * 
	 * @return
	 */
	public Map<Integer, Double> getHistory() {
		return history.getHistory();
	}

	/**
	 * Gets the usage history.
	 * 
	 * @return
	 */
	public double getUsage(int solCache) {
		double time = 0;
		if (getHistory().containsKey(solCache)) {
			time = getHistory().get(solCache);
		}
		return time;
	}
	
    /**
     * Obtains the grow factor.
     * 
     * @param time
     * @param key
     * @return
     */
    private double obtainGrow(double time, String key) {
		// Grow the tissue a little bit in each millisol
		double delta = 0;
		
		// Check if the tissue culture has been well taken care of
		if (tissueCultureInspection.containsKey(key)) {
			int num = tissueCultureInspection.get(key);
			// Incur penalty if having less than NUM_INSPECTIONS
			delta = (num - NUM_INSPECTIONS) * time;
			delta = Math.min(0, delta);
		}
		
		return delta;
    }
    
    /**
     * Harvests and extract a crop tissue.
     * 
     * @param worker
     */
    public boolean harvestTissue(Worker worker) {
    	Iterator<Map.Entry<String, Double>> i = tissueIncubator.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<String, Double> entry = i.next();
			String cropName = entry.getKey();
			double amount = entry.getValue();
			if (amount > 0.5) {
				String tissueName = cropName + Farming.TISSUE;
				int tissueID = ResourceUtil.findIDbyAmountResourceName(tissueName);
				building.getFarming().store(Farming.LOW_AMOUNT_TISSUE_CULTURE, tissueID, "Farming::growCropTissue");
				logger.log(building, worker, Level.INFO, 10_000,  
						"Harvested 0.5 kg " + tissueName + " in Botany lab.");
				return true;
			}
		}
		logger.log(building, worker, Level.INFO, 10_000,  
				"Not ready to harvest/extract any crop tissues yet in Botany lab.");
		return false;
    }
    
	public void addToIncubator(String c, double amountToAdd) {
		if (tissueIncubator.containsKey(c)) {
			double amount = tissueIncubator.get(c);
			tissueIncubator.put(c, amount + amountToAdd);
		}
	}
    
    private void setupTissueCultures() {
       	tissueCultureInspection = new HashMap<>();
       	tissueCultureAmount = new HashMap<>();
       	tissueIncubator = new HashMap<>();
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
        result += technologyLevel * 10D;
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
