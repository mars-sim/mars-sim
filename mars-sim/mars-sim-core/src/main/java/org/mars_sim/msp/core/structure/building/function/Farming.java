/**
 * Mars Simulation Project
 * Farming.java
 * @version 3.07 2014-11-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.ai.task.TendGreenhouse;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The Farming class is a building function for greenhouse farming.
 */
// 2014-11-06 Added if clause to account for soybean harvest
// 2014-10-15 Fixed the crash by checking if there is any food available
// 	Added new method checkAmountOfFood() for CookMeal.java to call ahead of time to 
//  see if new crop harvest comes in.
// 2014-10-14 Implemented new way of calculating amount of crop harvest in kg, 
// Crop Yield or Edible Biomass, based on NASA Advanced Life Support Baseline Values and Assumptions CR-2004-208941 
public class Farming
extends Function
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    /** default logger. */
	//private static Logger logger = Logger.getLogger(Farming.class.getName());

    private static final BuildingFunction FUNCTION = BuildingFunction.FARMING;

    // HARVEST_MULTIPLIER has been the conversion parameter set for all crops 
    //public static final double HARVEST_MULTIPLIER = 10D;

    private int cropNum;
    private double powerGrowingCrop;
    private double powerSustainingCrop;
    private double growingArea;
    private double maxHarvestinKg;
    private List<Crop> crops;

    /**
     * Constructor.
     * @param building the building the function is for.
     * @throws BuildingException if error in constructing function.
     */
    public Farming(Building building) {
        // Use Function constructor.
        super(FUNCTION, building);

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

        cropNum = config.getCropNum(building.getName());
        powerGrowingCrop = config.getPowerForGrowingCrop(building.getName());
        powerSustainingCrop = config.getPowerForSustainingCrop(building.getName());
        growingArea = config.getCropGrowingArea(building.getName());

        // Load activity spots
        loadActivitySpots(config.getFarmingActivitySpots(building.getName()));

        // [DEPRECATED by mkung]
        // Determine maximum harvest 
        //maxHarvest = growingArea * HARVEST_MULTIPLIER;

        
        // Create initial crops.
        crops = new ArrayList<Crop>();
        Settlement settlement = building.getBuildingManager().getSettlement();
        
        for (int x=0; x < cropNum; x++) {
        	// 2014-10-14 Implemented new way of calculating amount of food in kg, accounting for the Edible Biomass of a crop 
        	CropType cropType = Crop.getRandomCropType();
        	// edibleBiomass is in  [ gram / m^2 / day ]
        	double edibleBiomass = cropType.getEdibleBiomass();
        	// growing-time is in millisol vs. growingDay is in sol
        	double growingDay = cropType.getGrowingTime() / 1000 ;
        	// maxHarvest is in kg
        	maxHarvestinKg = edibleBiomass * growingDay * (growingArea / (double) cropNum)/1000;
        	      //logger.info("constructor :  max possible Harvest is set to "+ Math.round(maxHarvestinKg) + " kg");
            Crop crop = new Crop(cropType, maxHarvestinKg, this, settlement, false);
            crops.add(crop);
            building.getBuildingManager().getSettlement().fireUnitUpdate(UnitEventType.CROP_EVENT, crop);       
        }
        
    }

    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     * @throws Exception if error getting function value.
     * Called by BuildingManager.java getBUildingValue() 
     */
    public static double getFunctionValue(String buildingName, boolean newBuilding,
            Settlement settlement) {

        // Demand is value of estimated food needed by population per orbit.
        double foodPerSol = SimulationConfig.instance().getPersonConfiguration().getFoodConsumptionRate();
        int solsInOrbit = MarsClock.SOLS_IN_ORBIT_NON_LEAPYEAR;
        double foodPerOrbit = foodPerSol * solsInOrbit;
        double demand = foodPerOrbit * settlement.getAllAssociatedPeople().size();

        // Supply is total estimate harvest per orbit.
        double supply = 0D;
        boolean removedBuilding = false;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            if (!newBuilding && building.getName().equalsIgnoreCase(buildingName) && !removedBuilding) {
                removedBuilding = true;
            }
            else {
                Farming farmingFunction = (Farming) building.getFunction(FUNCTION);
                double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
                supply += farmingFunction.getEstimatedHarvestPerOrbit() * wearModifier;
            }
        }

        // TODO: investigating if other food group besides food should be added as well
        return  addSupply(supply, demand, settlement, buildingName);        
    }

    public static double addSupply(double supply, double demand, Settlement settlement, String buildingName) {
        AmountResource food = AmountResource.findAmountResource(org.mars_sim.msp.core.LifeSupport.FOOD);
        supply += settlement.getInventory().getAmountResourceStored(food, false);

        double growingAreaValue = demand / (supply + 1D);

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
        double growingArea = config.getCropGrowingArea(buildingName);       
        //logger.info("addSupply() : growingArea is " + growingArea);
        //logger.info("addSupply() : growingAreaValue is " + growingAreaValue);
        
        return growingArea * growingAreaValue;
    }

    
    /**
     * Gets the farm's current crops.
     * @return collection of crops
     */
    public List<Crop> getCrops() {
        return crops;
    }

    /**
     * Checks if farm currently requires work.
     * @return true if farm requires work
     */
    public boolean requiresWork() {
        boolean result = false;
        Iterator<Crop> i = crops.iterator();
        while (i.hasNext()) {
            if (i.next().requiresWork()) result = true;
        }
        return result;
    }

    /**
     * Adds work time to the crops current phase.
     * @param workTime - Work time to be added (millisols)
     * @return workTime remaining after working on crop (millisols)
     * @throws Exception if error adding work.
     */
    public double addWork(double workTime) {
        double workTimeRemaining = workTime;
        Crop needyCrop = null;
        // Scott - I used the comparison criteria 00001D rather than 0D
        // because sometimes math anomalies result in workTimeRemaining
        // becoming very small double values and an endless loop occurs.
        while (((needyCrop = getNeedyCrop()) != null) && (workTimeRemaining > 00001D)) {
            workTimeRemaining = needyCrop.addWork(workTimeRemaining);
        }

        return workTimeRemaining;
    }

    /**
     * Gets a crop that needs planting, tending, or harvesting.
     * @return crop or null if none found.
     */
    private Crop getNeedyCrop() {
        Crop result = null;

        List<Crop> needyCrops = new ArrayList<Crop>(crops.size());
        Iterator<Crop> i = crops.iterator();
        while (i.hasNext()) {
            Crop crop = i.next();
            if (crop.requiresWork()) {
                needyCrops.add(crop);
            }
        }

        if (needyCrops.size() > 0) {
            result = needyCrops.get(RandomUtil.getRandomInt(needyCrops.size() - 1));
        }

        return result;
    }

    /**
     * Adds the crop harvest to the farm.
     * @param harvest: harvested food to add (kg.)
     * @param cropCategory
     
     */
    // 2014-10-14 Added String cropCategory to the param list,
    // Note: this method was called by Crop.java's addWork()
    public void addHarvest(double harvestAmount, String cropName, String cropCategory) {

    	try {
    		Inventory inv = getBuilding().getInventory();
            AmountResource harvestCropCategory = AmountResource.findAmountResource(cropCategory);      
            double remainingCapacity = inv.getAmountResourceRemainingCapacity(harvestCropCategory, false, false);
                 //logger.info("addHarvest() : remaining Capacity is " + Math.round(remainingCapacity));
            /*// look up on the following three attributes. Sanity check only.
            double amountResourceStored = inv.getAmountResourceStored(harvestCrop, false);
                 // logger.info("addHarvest() : amountResourceStored is " + amountResourceStored);             
            double amountResourceCapacityNoContainers = inv.getAmountResourceCapacityNoContainers(harvestCrop);
                 // logger.info("addHarvest() : amountResourceCapacityNoContainers is " + amountResourceCapacityNoContainers);               
            double amountResourceCapacity = inv.getAmountResourceCapacity(harvestCrop, false);
                 // logger.info("addHarvest() : amountResourceCapacity is " + amountResourceCapacity);                
            */
            
            if (remainingCapacity < harvestAmount) {
                // if the remaining capacity is smaller than the harvested amount, set remaining capacity to full
            	harvestAmount = remainingCapacity;
                 	//logger.info("addHarvest() : storage is full!");
                }
                // add the harvest to the remaining capacity
            // 2014-11-06 changed the last param from false to true
            inv.storeAmountResource(harvestCropCategory, harvestAmount, true);
            // logger.info("addHarvest() : just added a harvest in " + harvestCropCategory + " to storage");
            // 2014-11-06 Added if clause to account for soybean harvest
            // note that crop name is Soybean (without 's')
            if (cropName == "Soybean") {
               AmountResource soybeansAR = AmountResource.findAmountResource("Soybeans");
        	   inv.storeAmountResource(soybeansAR, harvestAmount, true);
           }
        }  catch (Exception e) {}
    }

    /**
     * Gets the number of farmers currently working at the farm.
     * @return number of farmers
     */
    public int getFarmerNum() {
        int result = 0;

        if (getBuilding().hasFunction(BuildingFunction.LIFE_SUPPORT)) {
            try {
                LifeSupport lifeSupport = (LifeSupport) getBuilding().getFunction(BuildingFunction.LIFE_SUPPORT);
                Iterator<Person> i = lifeSupport.getOccupants().iterator();
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
    public void timePassing(double time) {

        // Determine resource processing production level.
        double productionLevel = 0D;
        if (getBuilding().getPowerMode() == PowerMode.FULL_POWER) productionLevel = 1D;
        else if (getBuilding().getPowerMode() ==  PowerMode.POWER_DOWN) productionLevel = .5D;

        // Add time to each crop.
        Iterator<Crop> i = crops.iterator();
        int newCrops = 0;
        while (i.hasNext()) {
            Crop crop = i.next();
            crop.timePassing(time * productionLevel);

            // Remove old crops.
            if (crop.getPhase().equals(Crop.FINISHED)) {
                i.remove();
                newCrops++;
            }
        }

        // Add any new crops.
        Settlement settlement = getBuilding().getBuildingManager().getSettlement();
        for (int x=0; x < newCrops; x++) {
        	
        	CropType cropType = Crop.getRandomCropType();
        	double edibleBiomassPerDay = cropType.getEdibleBiomass();
        	double growingDay = cropType.getGrowingTime() / 1000 ;
        	maxHarvestinKg = edibleBiomassPerDay * growingDay * (growingArea / (double) cropNum) /1000;
        	// logger.info("timePassing : seeding a new crop with maxHarvest "+ Math.round(maxHarvestinKg) + " kg");          
        	// Note: the last param of Crop must be set to TRUE
        	Crop crop = new Crop(cropType, maxHarvestinKg, this, settlement, true);
            //Crop crop = new Crop(Crop.getRandomCropType(), (maxHarvest / (double) cropNum), this, settlement, true);
            crops.add(crop);
            getBuilding().getBuildingManager().getSettlement().fireUnitUpdate(UnitEventType.CROP_EVENT, crop);
        }
    }

    /**
     * Gets the amount of power required when function is at full power.
     * @return power (kW)
     */
    public double getFullPowerRequired() {
        // Power (kW) required for normal operations.
        double powerRequired = 0D;

        Iterator<Crop> i = crops.iterator();
        while (i.hasNext()) {
            Crop crop = i.next();
            if (crop.getPhase().equals(Crop.GROWING))
                powerRequired += (crop.getMaxHarvest() * powerGrowingCrop);
        }

        return powerRequired;
    }

    /**
     * Gets the amount of power required when function is at power down level.
     * @return power (kW)
     */
    public double getPoweredDownPowerRequired() {

        // Get power required for occupant life support.
        double powerRequired = 0D;

        // Add power required to sustain growing or harvest-ready crops.
        Iterator<Crop> i = crops.iterator();
        while (i.hasNext()) {
            Crop crop = i.next();
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

    /**
     * Gets the estimated maximum harvest for one orbit.
     * @return max harvest (kg)
     * @throws Exception if error determining harvest.
     */
    public double getEstimatedHarvestPerOrbit() {
        double aveGrowingTime = Crop.getAverageCropGrowingTime();
        int solsInOrbit = MarsClock.SOLS_IN_ORBIT_NON_LEAPYEAR;
        double aveGrowingCyclesPerOrbit = solsInOrbit * 1000D / aveGrowingTime;
        return maxHarvestinKg * aveGrowingCyclesPerOrbit;
    }

    @Override
    public double getMaintenanceTime() {
        return growingArea * 5D;
    }

    @Override
    public void destroy() {
        super.destroy();

        Iterator<Crop> i = crops.iterator();
        while (i.hasNext()) {
            i.next().destroy();
        }
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
