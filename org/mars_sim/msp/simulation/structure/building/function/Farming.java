/**
 * Mars Simulation Project
 * Farming.java
 * @version 2.78 2004-11-20
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;
import java.util.*;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.PersonIterator;
import org.mars_sim.msp.simulation.person.ai.task.*;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
 
/**
 * The Farming class is a building function for greenhouse farming.
 */
public class Farming extends Function implements Serializable {
        
	// Unit update events
	public static final String CROP_EVENT = "crop event";
	
    public static final String NAME = "Farming";
    public static final double HARVEST_MULTIPLIER = 10D;
    public static final double BASE_CROP_GROWING_TIME = 90000D;
    
    private int cropNum;
    private double powerGrowingCrop;
    private double powerSustainingCrop;
    private double growingArea;
    private double maxHarvest;
    private List crops;
    
    /**
     * Constructor
     * @param building the building the function is for.
     * @throws BuildingException if error in constructing function.
     */
    public Farming(Building building) throws BuildingException {
    	// Use Function constructor.
    	super(NAME, building);
    	
		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		
		try {
			cropNum = config.getCropNum(building.getName());
			powerGrowingCrop = config.getPowerForGrowingCrop(building.getName());
			powerSustainingCrop = config.getPowerForSustainingCrop(building.getName());
			growingArea = config.getCropGrowingArea(building.getName());
		}
		catch (Exception e) {
			throw new BuildingException("Farming.constructor: " + e.getMessage());
		}
		
		// Determine maximum harvest.
		maxHarvest = growingArea * HARVEST_MULTIPLIER;
		
		// Create initial crops.
		crops = new ArrayList();
		try {
			Settlement settlement = building.getBuildingManager().getSettlement();
			CropConfig cropConfig = SimulationConfig.instance().getCropConfiguration();
			for (int x=0; x < cropNum; x++) {
				Crop crop = new Crop(Crop.getRandomCropType(cropConfig), 
						(maxHarvest / (double) cropNum), this, settlement, false);
				crops.add(crop);
				building.getBuildingManager().getSettlement().fireUnitUpdate(CROP_EVENT, crop);
			}
		}
		catch (Exception e) {
			throw new BuildingException("Crops could not be loaded for greenhouse: " + e.getMessage());  
		}
    }
    
    /**
     * Gets the farm's current crops.
     * @return collection of crops
     */
    public List getCrops() {
    	return crops;
    }
    
    /**
     * Checks if farm currently requires work.
     * @return true if farm requires work
     */
    public boolean requiresWork() {
		boolean result = false;
		Iterator i = crops.iterator();
		while (i.hasNext()) {
			Crop crop = (Crop) i.next();
			if (crop.requiresWork()) result = true;
		}
		return result;
    }
    
    /**
     * Adds work time to the crops current phase.
     * @param workTime - Work time to be added (millisols)
     * @return workTime remaining after working on crop (millisols)
     * @throws Exception if error adding work.
     */
    public double addWork(double workTime) throws Exception {
		double workTimeRemaining = workTime;
		int needyCrops = 0;
		// Scott - I used the comparison criteria 00001D rather than 0D
		// because sometimes math anomolies result in workTimeRemaining
		// becoming very small double values and an endless loop occurs.
		while (((needyCrops = getNeedyCrops()) > 0) && (workTimeRemaining > 00001D)) {
			double maxCropTime = workTimeRemaining / (double) needyCrops;
			Iterator i = crops.iterator();
			while (i.hasNext()) {
				Crop crop = (Crop) i.next();
				workTimeRemaining -= (maxCropTime - crop.addWork(maxCropTime));
			}
		}
 
		return workTimeRemaining;
    }
    
	/**
	 * Gets the number of crops that currently need work.
	 * @return number of crops requiring work
	 */
	private int getNeedyCrops() {
		int result = 0;
		Iterator i = crops.iterator();
		while (i.hasNext()) {
			Crop crop = (Crop) i.next();
			if (crop.requiresWork()) result++;
		}
		return result;
	}
    
    /**
     * Adds harvested food to the farm.
     * @param harvest harvested food to add (kg.)
     */
    public void addHarvest(double harvest) {
    	try {
    		Inventory inv = getBuilding().getInventory();
    		double remainingCapacity = inv.getAmountResourceRemainingCapacity(AmountResource.FOOD);
    		if (remainingCapacity < harvest) harvest = remainingCapacity;
    		inv.storeAmountResource(AmountResource.FOOD, harvest);
    	}
    	catch (Exception e) {}
    }
    
    /**
     * Gets the number of farmers currently working at the farm.
     * @return number of farmers
     */
    public int getFarmerNum() {
		int result = 0;
        
		if (getBuilding().hasFunction(LifeSupport.NAME)) {
			try {
				LifeSupport lifeSupport = (LifeSupport) getBuilding().getFunction(LifeSupport.NAME);
				PersonIterator i = lifeSupport.getOccupants().iterator();
				while (i.hasNext()) {
					Task task = i.next().getMind().getTaskManager().getTask();
					if (task instanceof TendGreenhouse) result++;
				}
			}
			catch (Exception e) {}
		}
        
		return result;
    }
    
	/**
	 * Time passing for the building.
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) throws BuildingException {
        
		// Determine resource processing production level.
		double productionLevel = 0D;
		if (getBuilding().getPowerMode().equals(Building.FULL_POWER)) productionLevel = 1D;
		else if (getBuilding().getPowerMode().equals(Building.POWER_DOWN)) productionLevel = .5D;
        
		// Add time to each crop.
		Iterator i = crops.iterator();
		int newCrops = 0;
		try {
			while (i.hasNext()) {
				Crop crop = (Crop) i.next();
				crop.timePassing(time * productionLevel);
            
				// Remove old crops.
				if (crop.getPhase().equals(Crop.FINISHED)) {
					i.remove();
					newCrops++;
				}
			}
        }
        catch (Exception e) {
        	throw new BuildingException("Farming.timePassing(): Problem with crops");
        }
        
		// Add any new crops.
		try {
			Settlement settlement = getBuilding().getBuildingManager().getSettlement();
			CropConfig cropConfig = SimulationConfig.instance().getCropConfiguration();
			for (int x=0; x < newCrops; x++) {
				Crop crop = new Crop(Crop.getRandomCropType(cropConfig), 
						(maxHarvest / (double) cropNum), this, settlement, true);
				crops.add(crop);
				getBuilding().getBuildingManager().getSettlement().fireUnitUpdate(CROP_EVENT, crop);
			}
		}
		catch (Exception e) {
			throw new BuildingException("Farming could not add new crop: " + e.getMessage());    
		}
	}
	
	/**
	 * Gets the amount of power required when function is at full power.
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {

		// Power (kW) required for normal operations.
		double powerRequired = 0D;
        
		Iterator i = crops.iterator();
		while (i.hasNext()) {
			Crop crop = (Crop) i.next();
			if (crop.getPhase().equals(Crop.GROWING))
				powerRequired += (crop.getMaxHarvest() * powerGrowingCrop);
		}
        
		return powerRequired;
	}
	
	/**
	 * Gets the amount of power required when function is at power down level.
	 * @return power (kW)
	 */
	public double getPowerDownPowerRequired() {
        
		// Get power required for occupant life support.
		double powerRequired = 0D;
        
		// Add power required to sustain growing or harvest-ready crops.
		Iterator i = crops.iterator();
		while (i.hasNext()) {
			Crop crop = (Crop) i.next();
			if (crop.getPhase().equals(Crop.GROWING) || crop.getPhase().equals(Crop.HARVESTING))
				powerRequired += (crop.getMaxHarvest() * powerSustainingCrop);
		}
        
		return powerRequired;
	}
	
	/**
	 * Gets the total growing area for all crops.
	 * @return growing area in square meters
	 */
	public double getGrowingArea() {
		return growingArea;
	}
}