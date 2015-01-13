/**
 * Mars Simulation Project
 * GoodsManager.java
 * @version 3.07 2015-01-10
 * @author Scott Davis
 * 
 */
package org.mars_sim.msp.core.structure.goods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.InventoryException;
import org.mars_sim.msp.core.LifeSupport;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.ContainerUtil;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.SpecimenContainer;
import org.mars_sim.msp.core.foodProduction.FoodProductionProcess;
import org.mars_sim.msp.core.foodProduction.FoodProductionProcessInfo;
import org.mars_sim.msp.core.foodProduction.FoodProductionProcessItem;
import org.mars_sim.msp.core.foodProduction.FoodProductionUtil;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.manufacture.ManufactureProcess;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.manufacture.ManufactureProcessItem;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.job.Architect;
import org.mars_sim.msp.core.person.ai.job.Areologist;
import org.mars_sim.msp.core.person.ai.job.Biologist;
import org.mars_sim.msp.core.person.ai.job.Driver;
import org.mars_sim.msp.core.person.ai.job.Trader;
import org.mars_sim.msp.core.person.ai.mission.CollectIce;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.Phase;
import org.mars_sim.msp.core.resource.Type;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Crop;
import org.mars_sim.msp.core.structure.building.function.CropConfig;
import org.mars_sim.msp.core.structure.building.function.CropType;
import org.mars_sim.msp.core.structure.building.function.Farming;
import org.mars_sim.msp.core.structure.building.function.FoodProduction;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.structure.building.function.Manufacture;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.core.structure.building.function.cooking.HotMeal;
import org.mars_sim.msp.core.structure.building.function.cooking.Ingredient;
import org.mars_sim.msp.core.structure.building.function.cooking.MealConfig;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.construction.ConstructionUtil;
import org.mars_sim.msp.core.structure.construction.ConstructionValues;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleConfig;
import org.mars_sim.msp.core.vehicle.VehicleConfig.VehicleDescription;

/**
 * A manager for goods values at a settlement.
 */
public class GoodsManager
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 12L;

    /** Initialized logger. */
    private static Logger logger = Logger.getLogger(GoodsManager.class.getName());

    // TODO Mission types should be an enum.
    private static final String TRAVEL_TO_SETTLEMENT_MISSION = "travel to settlement";
    private static final String EXPLORATION_MISSION = "exploration";
    private static final String COLLECT_ICE_MISSION = "collect ice";
    private static final String RESCUE_SALVAGE_MISSION = "rescue/salvage mission";
    private static final String TRADE_MISSION = "trade";
    private static final String COLLECT_REGOLITH_MISSION = "collect regolith";
    private static final String MINING_MISSION = "mining";
    private static final String CONSTRUCT_BUILDING_MISSION = "construct building";
    private static final String AREOLOGY_STUDY_FIELD_MISSION = "areology field study";
    private static final String BIOLOGY_STUDY_FIELD_MISSION = "biology field study";
    private static final String SALVAGE_BUILDING_MISSION = "salvage building";
    private static final String EMERGENCY_SUPPLY_MISSION = "deliver emergency supplies";

    // Number modifiers for outstanding repair and maintenance parts.
    private static final int OUTSTANDING_REPAIR_PART_MODIFIER = 100;
    private static final int OUTSTANDING_MAINT_PART_MODIFIER = 10;

    // Value multiplier factors for certain goods.
    private static final double EVA_SUIT_FACTOR = 100D;
    private static final double VEHICLE_FACTOR = 10000D;
    private static final double LIFE_SUPPORT_FACTOR = 4D;
    private static final double VEHICLE_FUEL_FACTOR = 10D;
    private static final double RESOURCE_PROCESSING_INPUT_FACTOR = .5D;
    private static final double MANUFACTURING_INPUT_FACTOR = .5D;
    private static final double CONSTRUCTING_INPUT_FACTOR = .5D;
    private static final double COOKED_MEAL_INPUT_FACTOR = .5D;
    private static final double DESSERT_FACTOR = 1D;
    // 2014-12-04 Added FOOD_PRODUCTION_INPUT_FACTOR
    private static final double FOOD_PRODUCTION_INPUT_FACTOR = .6D;
	// 2015-01-10 Added FARMING_FACTOR
    private static final double FARMING_FACTOR = .01D;
    //  SERVING_FRACTION was used in PreparingDessert.java
    public final double FRACTION = PreparingDessert.DESSERT_SERVING_FRACTION;
    
    // Data members
    private Settlement settlement;
    private Map<Good, Double> goodsValues;
    private Map<Good, Double> goodsDemandCache;
    private Map<Good, Double> goodsTradeCache;
    private Map<AmountResource, Double> resourceProcessingCache;
    private Map<String, Double> vehicleBuyValueCache;
    private Map<String, Double> vehicleSellValueCache;
    private Map<Part, Double> partsDemandCache;
    private boolean initialized = false;

    private Inventory inv;
	//private int solCache = 1;
	
    /**
     * Constructor.
     * @param settlement the settlement this manager is for.
     * @throws Exception if errors constructing instance.
     */
    public GoodsManager(Settlement settlement) {
        this.settlement = settlement;
        inv = settlement.getInventory();
        populateGoodsValues();
    }

    /**
     * Checks if goods manager has been initialized.
     * @return initialized.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Populates the goods cache maps with empty values.
     */
    private void populateGoodsValues() {
        List<Good> goods = GoodsUtil.getGoodsList();
        goodsValues = new TreeMap<Good, Double>();
        goodsDemandCache = new TreeMap<Good, Double>();
        goodsTradeCache = new TreeMap<Good, Double>();

        Iterator<Good> i = goods.iterator();
        while (i.hasNext()) {
            Good good = i.next();
            goodsValues.put(good, 0D);
            goodsDemandCache.put(good, 0D);
            goodsTradeCache.put(good, 0D);
        }

        // Create and populate resource processing cache with all amount resources.
        Set<AmountResource> amountResources = AmountResource.getAmountResources();
        resourceProcessingCache = new HashMap<AmountResource, Double>(amountResources.size());
        Iterator<AmountResource> j = amountResources.iterator();
        while (j.hasNext()) resourceProcessingCache.put(j.next(), 0D);

        // Create parts demand cache.
        partsDemandCache = new HashMap<Part, Double>(ItemResource.getItemResources().size());

        // Create vehicle caches.
        vehicleBuyValueCache = new HashMap<String, Double>();
        vehicleSellValueCache = new HashMap<String, Double>();
    }

    /**
     * Gets the value per item of a good.
     * @param good the good to check.
     * @return value (VP)
     * @throws Exception if error getting value.
     */
    public double getGoodValuePerItem(Good good) {
        try {
            if (goodsValues.containsKey(good)) return goodsValues.get(good);
            else throw new IllegalArgumentException("Good: " + good + " not valid.");
        } catch (Exception e) {
            logger.log(Level.SEVERE,e.getMessage());
            return 0;
        }
    }

    public double getGoodValuePerItem(Good good, double supply) {
        if (goodsValues.containsKey(good)) return determineGoodValue(good, supply, true);
        else throw new IllegalArgumentException("Good: " + good + " not valid.");
    }

    /**
     * Time passing
     * @param time the amount of time passing (millisols).
     * @throws Exception if error during time.
     */
    public void timePassing(double time) {
        updateGoodsValues();
    }

    /**
     * Updates the values for all the goods at the settlement.
     * @throws Exception if error updating goods values.
     */
    public void updateGoodsValues() {
        // Clear parts demand cache.
        partsDemandCache.clear();

        // Clear vehicle caches.
        vehicleBuyValueCache.clear();
        vehicleSellValueCache.clear();

        Iterator<Good> i = goodsValues.keySet().iterator();
        while (i.hasNext()) updateGoodValue(i.next(), true);
        settlement.fireUnitUpdate(UnitEventType.GOODS_VALUE_EVENT);

        initialized = true;
    }

    /**
     * Updates the value of a good at the settlement.
     * @param good the good to update.
     * @param collectiveUpdate true if this update is part of a collective good value update.
     * @throws Exception if error updating good value.
     */
    public void updateGoodValue(Good good, boolean collectiveUpdate) {
        if (good != null) {
            goodsValues.put(good, determineGoodValue(good, getNumberOfGoodForSettlement(good), false));
            if (!collectiveUpdate) settlement.fireUnitUpdate(UnitEventType.GOODS_VALUE_EVENT, good);
        }
        else throw new IllegalArgumentException("Good is null.");
    }

    /**
     * Determines the value of a good.
     * @param good the good to check.
     * @param supply the current supply (# of items) of the good.
     * @param useCache use demand and trade caches to determine value?
     * @return value of good.
     * @throws Exception if problem determining good value.
     */
    private double determineGoodValue(Good good, double supply, boolean useCache) {
        if (good != null) {
            double value = 0D;

            // Determine all amount resource good values.
            if (GoodType.AMOUNT_RESOURCE == good.getCategory()) 
                value = determineAmountResourceGoodValue(good, supply, useCache);

            // Determine all item resource values.
            if (GoodType.ITEM_RESOURCE == good.getCategory())
                value = determineItemResourceGoodValue(good, supply, useCache);

            // Determine all equipment values.
            if (GoodType.EQUIPMENT == good.getCategory())
                value = determineEquipmentGoodValue(good, supply, useCache);

            // Determine all vehicle values.
            if (GoodType.VEHICLE == good.getCategory())
                value = determineVehicleGoodValue(good, supply, useCache);

            return value;
        }
        else throw new IllegalArgumentException("Good is null.");
    }

    /**
     * Determines the value of an amount resource.
     * @param resourceGood the amount resource good.
     * @param supply the current supply (kg) of the good.
     * @param useCache use the cache to determine value.
     * @return value (value points / kg)
     * @throws Exception if error determining resource value.
     */
    private double determineAmountResourceGoodValue(Good resourceGood, double supply, boolean useCache) {
    	//System.out.println( "entering determineAmountResourceGoodValue() ");
    	double value = 0D;
        double demand = 0D;
        double totalDemand = 0D;
        //double projectedDemand = 0D;
        
        supply++;

        AmountResource resource = (AmountResource) resourceGood.getObject();
                
        if (useCache) {
            if (goodsDemandCache.containsKey(resourceGood)) demand = goodsDemandCache.get(resourceGood);
            else throw new IllegalArgumentException("Good: " + resourceGood + " not valid.");
        }
        else {
          
            // Add life support demand if applicable.
            demand += getLifeSupportDemand(resource);

            // Add potable water usage demand if applicable.
            demand += getPotableWaterUsageDemand(resource);

            // Add vehicle demand if applicable.
            demand += getVehicleDemand(resource);

            // Add farming demand.
            demand += getFarmingDemand(resource);

            // Add manufacturing demand.
            demand += getResourceManufacturingDemand(resource);
 
            //2014-11-25 Add Food Production demand.
            demand += getResourceFoodProductionDemand(resource);
            
            // Add demand for the resource as a cooked meal ingredient.
            demand += getResourceCookedMealIngredientDemand(resource);
            
            // Add dessert food demand.
            demand += getResourceDessertDemand(resource);

            // Add construction demand.
            demand += getResourceConstructionDemand(resource);

            // 2015-01-10 Called getRealTimeDemand()
            totalDemand = getRealTimeDemand(resource, demand);
            
            // Add trade value.
            double tradeDemand = determineTradeDemand(resourceGood, useCache);
            tradeDemand = Math.round(tradeDemand* 100.0) / 100.0;
 
            //System.out.println( resource.getName() 
            //+ " : supply is " + Math.round(supply* 100.0) / 100.0
            //+ "  projectedDemand is " + Math.round(demand* 100.0) / 100.0     
            //+ "  tradeDemand is " + Math.round(tradeDemand* 100.0) / 100.0);

            if (tradeDemand > totalDemand) {
            	totalDemand = tradeDemand;
            }

            goodsDemandCache.put(resourceGood, demand);
        }
        
        value = totalDemand / supply;

        // Use resource processing value if higher. 
        // Manny: why using higher values?
        double resourceProcessingValue = getResourceProcessingValue(resource, useCache);
        if (resourceProcessingValue > value) value = resourceProcessingValue;

        return value;
    }

    
    // 2015-01-10 Created getRealTimeDemand()
    public double getRealTimeDemand(AmountResource resource, double demand) {
    
    	double projectedDemand = demand;
        // s = successful
        // u = unsuccessful
        // t = total
        // note:  uRequest = tRequest - sRequest; 
        double uDemand = 0;
        int uRequest = 0;
        
        double sDemand = inv.getDemandRealUsage(resource.getName());
        sDemand = Math.round(sDemand * 100.0) / 100.0;
        int tRequest = inv.getDemandTotalRequest(resource.getName());
        int sRequest = inv.getDemandSuccessfulRequest(resource.getName());
        
        if (tRequest == 0 || sRequest == 0) 
        	uDemand = 0;
        if (tRequest < sRequest ) 
        	uDemand = 0;
        else {
        	uRequest = tRequest - sRequest; 
        	// unsuccessful demand usage approximately equals average successful demand * # of unsuccessful request
        	uDemand = Math.round(sDemand / sRequest * uRequest * 100.0) / 100.0;
        }

        // Get the average demand per orbit 
        // total average demand = (projected demand + real demand usage + unsuccessful demand usage) / 3 
        double totalDemand = ( 
        		projectedDemand +
        		sDemand * MarsClock.SOLS_IN_ORBIT_NON_LEAPYEAR +
        		uDemand * MarsClock.SOLS_IN_ORBIT_NON_LEAPYEAR
        		) / 3.0;
        
        totalDemand = Math.round(totalDemand* 100.0) / 100.0;

        //System.out.println( resource.getName() 
        //+ " : sDemand  is " + sDemand * MarsClock.SOLS_IN_ORBIT_NON_LEAPYEAR
        //+ "  tRequest is " + tRequest
        //+ "  sRequest is " + sRequest
        //+ "  uDemand is " + uDemand* MarsClock.SOLS_IN_ORBIT_NON_LEAPYEAR
        //+ "  totalAveDemand is " + totalDemand);
	
    	// TODO: should keep only the demand data of the last 5 days
        // Should the realtime demand data be cumulative ?
        
    	//inv.clearDemandTotalRequestMap();
    	//inv.clearDemandRealUsageMap();
    	//inv.clearDemandSuccessfulRequestMap();
    	
    	return totalDemand;
    }
    
    /**
     * Gets the life support demand for an amount resource.
     * @param resource the resource to check.
     * @return demand (kg)
     * @throws Exception if error getting life support demand.
     */
    private double getLifeSupportDemand(AmountResource resource) {
		
        if (resource.isLifeSupport()) {
            double amountNeededSol = 0D;
            PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
            AmountResource oxygen = AmountResource.findAmountResource(LifeSupport.OXYGEN);
            if (resource.equals(oxygen)) 
                amountNeededSol = config.getOxygenConsumptionRate();
            AmountResource water = AmountResource.findAmountResource(LifeSupport.WATER);
            if (resource.equals(water)) 
                amountNeededSol = config.getWaterConsumptionRate();
            AmountResource food = AmountResource.findAmountResource(LifeSupport.FOOD);
            if (resource.equals(food)) {
                amountNeededSol = config.getFoodConsumptionRate();
            }
   			
            double amountNeededOrbit = amountNeededSol * MarsClock.SOLS_IN_ORBIT_NON_LEAPYEAR;
            int numPeople = settlement.getAllAssociatedPeople().size();
            return numPeople * amountNeededOrbit * LIFE_SUPPORT_FACTOR;
        }
        else return 0D;
    }

    /**
     * Gets the potable water usage demand for an amount resource.
     * @param resource the resource to check.
     * @return demand (kg)
     * @throws Exception if error getting potable water usage demand.
     */
    private double getPotableWaterUsageDemand(AmountResource resource) {
        AmountResource water = AmountResource.findAmountResource(LifeSupport.WATER);
        if (resource.equals(water)) {
            double amountNeededSol = LivingAccommodations.WASH_WATER_USAGE_PERSON_SOL;
            double amountNeededOrbit = amountNeededSol * MarsClock.SOLS_IN_ORBIT_NON_LEAPYEAR;
            int numPeople = settlement.getCurrentPopulationNum();
            return numPeople * amountNeededOrbit * LIFE_SUPPORT_FACTOR;
        }
        else return 0D;
    }

    /**
     * Gets vehicle demand for an amount resource.
     * @param resource the resource to check.
     * @return demand (kg) for the resource.
     * @throws Exception if error getting resource demand.
     */
    private double getVehicleDemand(AmountResource resource) {
        double demand = 0D;
        AmountResource methane = AmountResource.findAmountResource("methane");
        if (resource.isLifeSupport() || resource.equals(methane)) {
            Iterator<Vehicle> i = getAssociatedVehicles().iterator();
            while (i.hasNext()) {
                double fuelDemand = i.next().getInventory().getAmountResourceCapacity(resource, false);
                demand += fuelDemand * VEHICLE_FUEL_FACTOR;
            }
        }
        return demand;
    }

    /**
     * Gets all vehicles associated with the settlement.
     * @return collection of vehicles.
     */
    private Collection<Vehicle> getAssociatedVehicles() {
        // Start with parked vehicles at settlement.
        Collection<Vehicle> vehicles = settlement.getParkedVehicles();

        // Add associated vehicles out on missions.
        Iterator<Mission> i = Simulation.instance().getMissionManager().getMissionsForSettlement(settlement).iterator();
        while (i.hasNext()) {
            Mission mission = i.next();
            if (mission instanceof VehicleMission) {
                Vehicle vehicle = ((VehicleMission) mission).getVehicle();
                if ((vehicle != null) && !vehicles.contains(vehicle)) vehicles.add(vehicle);
            }
        }

        return vehicles;
    }

  
    // 2014-11-30 Created getValueList()    		
    public List<Double> getValueList(List<AmountResource> foodARList) {
    	
    	double cropValue = 0;
    	List<Double> cropValueList = new ArrayList<Double>();
    	AmountResource ar = null;
    	
		Iterator<AmountResource> i = foodARList.iterator();
		while (i.hasNext()) 
		{
			ar = i.next();
			cropValue = getGoodValuePerItem(GoodsUtil.getResourceGood(ar));
			cropValueList.add(cropValue);	
		}
    	return cropValueList;
    }
    
    // 2015-01-10 Revised getCropARList()
    public List<AmountResource> getCropARList() {
    	List<AmountResource> cropARList = new ArrayList<AmountResource>();
    	
    	CropConfig config = SimulationConfig.instance().getCropConfiguration();
		List<CropType> cropTypeList = config.getCropList();
		//2014-12-12 Enabled Collections.sorts by implementing Comparable<CropType> 
		Collections.sort(cropTypeList);
		List<CropType> cropCache = new ArrayList<CropType>(cropTypeList);
		Iterator<CropType> i = cropCache.iterator();
		while (i.hasNext()) {
			CropType c = i.next();
			String cropName = c.getName();
			AmountResource ar = AmountResource.findAmountResource(cropName);
			cropARList.add(ar);
		}

    	return cropARList;
    }
    
    // 2014-11-30 Created getTotalDemand()
    public double getTotalDemand(List<Double> cropValueList, Farming farm, double amountNeeded) {
    	double demand = 0;

    	Iterator<Double> i = cropValueList.iterator();
    	
    	while (i.hasNext()) {	
    		double cropValue = i.next();
        	demand += (farm.getEstimatedHarvestPerOrbit() * cropValue) / amountNeeded;
    	}
    
    	return demand;
    }
    
    /**
     * Gets the farming demand for the resource.
     * @param resource the resource to check.
     * @return demand (kg) for the resource.
     * @throws Exception if error determining demand.
     */
    // 2014-10-15 mkung: added 5 new food groups to enable them to be traded
    // 2014-11-30 Rewrote getFarmingDemand() for a large list of edible food
    private double getFarmingDemand(AmountResource resource) {
        double demand = 0D;
        AmountResource wasteWater = AmountResource.findAmountResource("waste water");
        AmountResource carbonDioxide = AmountResource.findAmountResource("carbon dioxide");

        // 2015-01-10 Revised getCropARList()
        List<AmountResource> cropARList = getCropARList();
              
        if (resource.equals(wasteWater) || resource.equals(carbonDioxide)) {
            // 2014-11-30 Created getValueList()
            List<Double> cropValueList = getValueList(cropARList);
             
            Iterator<Building> i = settlement.getBuildingManager().getBuildings(BuildingFunction.FARMING).iterator();
            while (i.hasNext()) {
                Building building = i.next();
                Farming farm = (Farming) building.getFunction(BuildingFunction.FARMING);

                double amountNeeded = 0D;
                if (resource.equals(wasteWater)) 
                    amountNeeded = Crop.WASTE_WATER_NEEDED;
                else if (resource.equals(carbonDioxide))
                    amountNeeded = Crop.CARBON_DIOXIDE_NEEDED;

                // 2014-11-30 Created getTotalDemand()
                demand += getTotalDemand(cropValueList, farm, amountNeeded);
            }
        }
    	// 2015-01-10 Added FARMING_FACTOR
        demand = demand * FARMING_FACTOR;
        
        return demand;
    }

    /**
     * Gets the value of a resource from all resource processes.
     * @param resource the amount resource.
     * @param useProcessingCache use processing cache to determine value.
     * @return value (value points / kg)
     * @throws Exception if error getting value.
     */
    private double getResourceProcessingValue(AmountResource resource, 
            boolean useProcessingCache) {
        double value = 0D;

        if (useProcessingCache) {
            if (resourceProcessingCache.containsKey(resource)) value = resourceProcessingCache.get(resource);
            else throw new IllegalArgumentException("Amount Resource: " + resource + " not valid.");
        }
        else {
            // Get all resource processes at settlement.
            Iterator<ResourceProcess> i = getResourceProcesses().iterator();
            while (i.hasNext()) {
                ResourceProcess process = i.next();
                double processValue = getResourceProcessValue(process, resource);
                if (processValue > value) value = processValue;
            }
            resourceProcessingCache.put(resource, value);
        }

        return value;
    }

    /**
     * Gets the value of a resource from a resource process.
     * @param process the resource process.
     * @param resource the amount resource.
     * @return value (value points / kg)
     * @throws exception if error getting good value.
     */
    private double getResourceProcessValue(ResourceProcess process, AmountResource resource) {
        double value = 0D;

        Set<AmountResource> inputResources = process.getInputResources();
        Set<AmountResource> outputResources = process.getOutputResources();

        if (inputResources.contains(resource) && !process.isAmbientInputResource(resource)) {
            double outputValue = 0D;
            Iterator<AmountResource> i = outputResources.iterator();
            while (i.hasNext()) {
                AmountResource output = i.next();
                double outputRate = process.getMaxOutputResourceRate(output); 
                if (!process.isWasteOutputResource(resource)) {
                    outputValue += (getGoodValuePerItem(GoodsUtil.getResourceGood(output)) * outputRate);
                }
            }

            double totalInputRate = 0D;
            Iterator<AmountResource> j = process.getInputResources().iterator();
            while (j.hasNext()) {
                AmountResource inputResource = j.next();
                if (!process.isAmbientInputResource(inputResource)) {
                    totalInputRate += process.getMaxInputResourceRate(inputResource);
                }
            }

            double totalInputsValue = outputValue * RESOURCE_PROCESSING_INPUT_FACTOR;

            value = (1D / totalInputRate) * totalInputsValue;
        }

        return value;
    }

    /**
     * Get all resource processes at settlement.
     * @return list of resource processes.
     * @throws BuildingException if error getting processes.
     */
    private List<ResourceProcess> getResourceProcesses() {
        List<ResourceProcess> processes = new ArrayList<ResourceProcess>(0);
        Iterator<Building> i = settlement.getBuildingManager().getBuildings().iterator();
        while (i.hasNext()) {
            Building building = i.next();
            if (building.hasFunction(BuildingFunction.RESOURCE_PROCESSING)) {
                ResourceProcessing processing = (ResourceProcessing) building.getFunction(
                        BuildingFunction.RESOURCE_PROCESSING);
                processes.addAll(processing.getProcesses());
            }
        }
        return processes;
    }

    /**
     * Gets the demand for an amount resource as an input in the settlement's manufacturing processes.
     * @param resource the amount resource.
     * @return demand (kg)
     * @throws Exception if error determining demand for resource.
     */
    private double getResourceManufacturingDemand(AmountResource resource) {
        double demand = 0D;

        // Get highest manufacturing tech level in settlement.
        if (ManufactureUtil.doesSettlementHaveManufacturing(settlement)) {
            int techLevel = ManufactureUtil.getHighestManufacturingTechLevel(settlement);
            Iterator<ManufactureProcessInfo> i = ManufactureUtil.getManufactureProcessesForTechLevel(
                    techLevel).iterator();
            while (i.hasNext()) {
                double manufacturingDemand = getResourceManufacturingProcessDemand(resource, i.next());
                demand += manufacturingDemand;
            }
        }

        return demand;
    }

    /**
     * Gets the demand for an amount resource as an input in the settlement's Food Production processes.
     * @param resource the amount resource.
     * @return demand (kg)
     * @throws Exception if error determining demand for resource.
     */
    //2014-12-04 Modified getResourceFoodProductionDemand
    private double getResourceFoodProductionDemand(AmountResource resource) {
        double demand = 0D;

        // Get highest Food Production tech level in settlement.
        if (FoodProductionUtil.doesSettlementHaveFoodProduction(settlement)) {
            int techLevel = FoodProductionUtil.getHighestFoodProductionTechLevel(settlement);
            Iterator<FoodProductionProcessInfo> i = FoodProductionUtil.getFoodProductionProcessesForTechLevel(
                    techLevel).iterator();
            while (i.hasNext()) {
                double FoodProductionDemand = getResourceFoodProductionProcessDemand(resource, i.next());
                demand += FoodProductionDemand;
            }
        }

        return demand;
    }
  
    /**
     * Gets the demand for an input amount resource in a manufacturing process.
     * @param resource the amount resource.
     * @param process the manufacturing process.
     * @return demand (kg)
     * @throws Exception if error determining resource value.
     */
    private double getResourceManufacturingProcessDemand(AmountResource resource,
            ManufactureProcessInfo process) {
        double demand = 0D;

        ManufactureProcessItem resourceInput = null;
        Iterator<ManufactureProcessItem> i = process.getInputList().iterator();
        while ((resourceInput == null) && i.hasNext()) {
            ManufactureProcessItem item = i.next();
            if (
                    Type.AMOUNT_RESOURCE.equals(item.getType()) && 
                    resource.getName().equalsIgnoreCase(item.getName())
                    ) {
                resourceInput = item;
                break;
            }
        }

        if (resourceInput != null) {
            double outputsValue = 0D;
            Iterator<ManufactureProcessItem> j = process.getOutputList().iterator();
            while (j.hasNext()) {
                outputsValue += ManufactureUtil.getManufactureProcessItemValue(j.next(), settlement, true);
            }

            double totalItems = 0D;
            Iterator<ManufactureProcessItem> k = process.getInputList().iterator();
            while (k.hasNext()) {
                totalItems += k.next().getAmount();
            }

            double totalInputsValue = outputsValue * MANUFACTURING_INPUT_FACTOR;

            demand = (1D / totalItems) * totalInputsValue;
        }

        return demand;
    }


    /**
     * Gets the demand for an input amount resource in a Food Production process.
     * @param resource the amount resource.
     * @param process the Food Production process.
     * @return demand (kg)
     * @throws Exception if error determining resource value.
     */
    // 2014-12-04 Added getResourceFoodProductionProcessDemand()
    private double getResourceFoodProductionProcessDemand(AmountResource resource,
            FoodProductionProcessInfo process) {
        double demand = 0D;

        FoodProductionProcessItem resourceInput = null;
        Iterator<FoodProductionProcessItem> i = process.getInputList().iterator();
        while ((resourceInput == null) && i.hasNext()) {
            FoodProductionProcessItem item = i.next();
            if (
                    Type.AMOUNT_RESOURCE.equals(item.getType()) && 
                    resource.getName().equalsIgnoreCase(item.getName())
                    ) {
                resourceInput = item;
                break;
            }
        }

        if (resourceInput != null) {
            double outputsValue = 0D;
            Iterator<FoodProductionProcessItem> j = process.getOutputList().iterator();
            while (j.hasNext()) {
                outputsValue += FoodProductionUtil.getFoodProductionProcessItemValue(j.next(), settlement, true);
            }

            double totalItems = 0D;
            Iterator<FoodProductionProcessItem> k = process.getInputList().iterator();
            while (k.hasNext()) {
                totalItems += k.next().getAmount();
            }

            double totalInputsValue = outputsValue * FOOD_PRODUCTION_INPUT_FACTOR;

            demand = (1D / totalItems) * totalInputsValue;
        }

        return demand;
    }
    
    /**
     * Gets the demand for a resource as a cooked meal ingredient.
     * @param resource the amount resource.
     * @return demand (kg)
     */
    private double getResourceCookedMealIngredientDemand(AmountResource resource) {
        double demand = 0D;
        
        // Determine total demand for cooked meal mass for the settlement.
        PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
        double cookedMealDemandSol = personConfig.getFoodConsumptionRate();
        double cookedMealDemandOrbit = cookedMealDemandSol * MarsClock.SOLS_IN_ORBIT_NON_LEAPYEAR;
        int numPeople = settlement.getAllAssociatedPeople().size();
        double cookedMealDemand = numPeople * cookedMealDemandOrbit;
        
        // Determine demand for the resource as an ingredient for each cooked meal recipe.
        MealConfig mealConfig = SimulationConfig.instance().getMealConfiguration();
        Iterator<HotMeal> i = mealConfig.getMealList().iterator();
        while (i.hasNext()) {
            HotMeal meal = i.next();
            Iterator<Ingredient> j = meal.getIngredientList().iterator();
            while (j.hasNext()) {
                Ingredient ingredient = j.next();
                if (ingredient.getName().equalsIgnoreCase(resource.getName())) {
                    demand += ingredient.getProportion() * cookedMealDemand * COOKED_MEAL_INPUT_FACTOR;
                }
            }
        }
        
        return demand;
    }
    
    /**
     * Gets the demand for a food dessert item.
     * @param resource the amount resource.
     * @return demand (kg)
     */
    private double getResourceDessertDemand(AmountResource resource) {
        double demand = 0D;
        // 2015-01-03 Added more food dessert item
        String [] dessert = { 	"Soymilk",
        						"Sugarcane Juice",
        						"Strawberry",
        						"Granola Bar",
        						"Blueberry Muffin", 
        						"Cranberry Juice"  };
        
        
        String dessertName = resource.getName();
        boolean hasDessert = false;
        
        for(String n : dessert) {
        	if (n.equalsIgnoreCase(dessertName)) {
        		hasDessert = true;
        	}
        }	
        
        if (hasDessert) {
        	
            PersonConfig config = SimulationConfig.instance().getPersonConfiguration();      

            // see PrepareDessert.java for the number of dessert served per sol
            //final double NUM_OF_DESSERT_PER_SOL = 3D;
            
            // Note: getFoodConsumptionRate has already been used by meal
            double amountNeededSol = config.getFoodConsumptionRate() * FRACTION / dessert.length;
            double amountNeededOrbit = amountNeededSol * MarsClock.SOLS_IN_ORBIT_NON_LEAPYEAR;
            int numPeople = settlement.getAllAssociatedPeople().size();
            return numPeople * amountNeededOrbit * DESSERT_FACTOR;
        }
        
        return demand;
    }

    /**
     * Gets the demand for an amount resource as an input in building construction.
     * @param resource the amount resource.
     * @return demand (kg)
     * @throws Exception if error determining demand for resource.
     */
    private double getResourceConstructionDemand(AmountResource resource) {
        double demand = 0D;

        ConstructionValues values = settlement.getConstructionManager().getConstructionValues();
        Map<ConstructionStageInfo, Double> stageValues = values.getAllConstructionStageValues();
        Iterator<ConstructionStageInfo> i = stageValues.keySet().iterator();
        while (i.hasNext()) {
            ConstructionStageInfo stage = i.next();
            double stageValue = stageValues.get(stage);
            if (stageValue > 0D && ConstructionStageInfo.BUILDING.equals(stage.getType()) 
                    && isLocallyConstructable(stage)) {
                double constructionDemand = getResourceConstructionStageDemand(resource, stage, stageValue);
                if (constructionDemand > 0D) {
                    demand += constructionDemand;
                }
            }
        }

        return demand;
    }

    /**
     * Checks if a building construction stage can be constructed at the local settlement.
     * @param buildingStage the building construction stage info.
     * @return true if building can be constructed.
     */
    private boolean isLocallyConstructable(ConstructionStageInfo buildingStage) {
        boolean result = false;

        if (buildingStage.isConstructable()) {
            ConstructionStageInfo frameStage = ConstructionUtil.getPrerequisiteStage(buildingStage);
            if (frameStage != null) {
                ConstructionStageInfo foundationStage = ConstructionUtil.getPrerequisiteStage(frameStage);
                if (foundationStage != null) {
                    if (frameStage.isConstructable() && foundationStage.isConstructable()) {
                        result = true;
                    }
                    else {
                        // Check if any existing buildings have same frame stage and can be refit or refurbished 
                        // into new building.
                        Iterator<Building> i = settlement.getBuildingManager().getBuildings().iterator();
                        while (i.hasNext()) {
                            ConstructionStageInfo tempBuildingStage = ConstructionUtil.getConstructionStageInfo(
                                    i.next().getBuildingType());
                            if (tempBuildingStage != null) {
                                ConstructionStageInfo tempFrameStage = ConstructionUtil.getPrerequisiteStage(
                                        tempBuildingStage);
                                if (frameStage.equals(tempFrameStage)) {
                                    result = true;
                                }
                            }
                        }
                    }
                }
            } 
        }

        return result;
    }

    /**
     * Gets the demand for an amount resources as an input for a particular building construction stage.
     * @param resource the amount resource.
     * @param stage the building construction stage.
     * @param stageValue the building construction stage value (VP).
     * @return demand (kg)
     * @throws Exception if error determining demand for resource.
     */
    private double getResourceConstructionStageDemand(AmountResource resource, ConstructionStageInfo stage, 
            double stageValue) {
        double demand = 0D;

        Map<AmountResource, Double> resources = getAllPrerequisiteConstructionResources(stage);
        Map<Part, Integer> parts = getAllPrerequisiteConstructionParts(stage);
        double resourceAmount = getPrerequisiteConstructionResourceAmount(resource, stage);

        if (resourceAmount > 0D) {
            double totalItems = 0D;

            Iterator<AmountResource> i = resources.keySet().iterator();
            while (i.hasNext()) {
                totalItems += resources.get(i.next());
            }

            Iterator<Part> j = parts.keySet().iterator();
            while (j.hasNext()) {
                totalItems += parts.get(j.next());
            }

            double totalInputsValue = stageValue * CONSTRUCTING_INPUT_FACTOR;

//            demand = (1D / totalItems) * totalInputsValue;
            demand = (resourceAmount / totalItems) * totalInputsValue;
        }

        return demand;
    }

    private Map<AmountResource, Double> getAllPrerequisiteConstructionResources(ConstructionStageInfo stage) {
        Map<AmountResource, Double> result = new HashMap<AmountResource, Double>(stage.getResources());

        ConstructionStageInfo preStage1 = ConstructionUtil.getPrerequisiteStage(stage);
//        if ((preStage1 != null) && preStage1.isConstructable()) {
        // TODO
        if ((preStage1 != null)) {
            Iterator<AmountResource> i = preStage1.getResources().keySet().iterator();
            while (i.hasNext()) {
                AmountResource resource = i.next();
                double amount = preStage1.getResources().get(resource);
                if (result.containsKey(resource)) {
                    double totalAmount = result.get(resource) + amount;
                    result.put(resource, totalAmount);
                }
                else {
                    result.put(resource, amount);
                }
            }

            ConstructionStageInfo preStage2 = ConstructionUtil.getPrerequisiteStage(preStage1);
//            if ((preStage2 != null) && preStage2.isConstructable()) {
            // TODO
            if ((preStage2 != null)) {
                Iterator<AmountResource> j = preStage2.getResources().keySet().iterator();
                while (j.hasNext()) {
                    AmountResource resource = j.next();
                    double amount = preStage2.getResources().get(resource);
                    if (result.containsKey(resource)) {
                        double totalAmount = result.get(resource) + amount;
                        result.put(resource, totalAmount);
                    }
                    else {
                        result.put(resource, amount);
                    }
                }
            }
        }

        return result;
    }
    
    private double getPrerequisiteConstructionResourceAmount(AmountResource resource, ConstructionStageInfo stage) {
        
        double result = 0D;
        
        Map<AmountResource, Double> stageResources = stage.getResources();
        if (stageResources.containsKey(resource)) {
            result += stageResources.get(resource);
        }
        
        ConstructionStageInfo preStage1 = ConstructionUtil.getPrerequisiteStage(stage);
        if ((preStage1 != null) && preStage1.isConstructable()) {
            Map<AmountResource, Double> preStage1Resources = preStage1.getResources();
            if (preStage1Resources.containsKey(resource)) {
                result += preStage1Resources.get(resource);
            }
            
            ConstructionStageInfo preStage2 = ConstructionUtil.getPrerequisiteStage(preStage1);
            if ((preStage2 != null) && preStage2.isConstructable()) {
                Map<AmountResource, Double> preStage2Resources = preStage2.getResources();
                if (preStage2Resources.containsKey(resource)) {
                    result += preStage2Resources.get(resource);
                }
            }
        }
        
        return result;
    }

    private Map<Part, Integer> getAllPrerequisiteConstructionParts(ConstructionStageInfo stage) {
        Map<Part, Integer> result = new HashMap<Part, Integer>(stage.getParts());

        ConstructionStageInfo preStage1 = ConstructionUtil.getPrerequisiteStage(stage);
//        if ((preStage1 != null) && preStage1.isConstructable()) {
        // TODO
        if ((preStage1 != null)) {
            Iterator<Part> i = preStage1.getParts().keySet().iterator();
            while (i.hasNext()) {
                Part part = i.next();
                int number = preStage1.getParts().get(part);
                if (result.containsKey(part)) {
                    int totalNumber = result.get(part) + number;
                    result.put(part, totalNumber);
                }
                else {
                    result.put(part, number);
                }
            }

            ConstructionStageInfo preStage2 = ConstructionUtil.getPrerequisiteStage(preStage1);
//            if ((preStage2 != null) && preStage2.isConstructable()) {
            // TODO
            if ((preStage2 != null)) {
                Iterator<Part> j = preStage2.getParts().keySet().iterator();
                while (j.hasNext()) {
                    Part part = j.next();
                    int number = preStage2.getParts().get(part);
                    if (result.containsKey(part)) {
                        int totalNumber = result.get(part) + number;
                        result.put(part, totalNumber);
                    }
                    else {
                        result.put(part, number);
                    }
                }
            }
        }

        return result;
    }
    
    private int getPrerequisiteConstructionPartNum(Part part, ConstructionStageInfo stage) {
        
        int result = 0;
        
        Map<Part, Integer> stageParts = stage.getParts();
        if (stageParts.containsKey(part)) {
            result += stageParts.get(part);
        }
        
        ConstructionStageInfo preStage1 = ConstructionUtil.getPrerequisiteStage(stage);
        if ((preStage1 != null) && preStage1.isConstructable()) {
            Map<Part, Integer> preStage1Parts = preStage1.getParts();
            if (preStage1Parts.containsKey(part)) {
                result += preStage1Parts.get(part);
            }
            
            ConstructionStageInfo preStage2 = ConstructionUtil.getPrerequisiteStage(preStage1);
            if ((preStage2 != null) && preStage2.isConstructable()) {
                Map<Part, Integer> preStage2Parts = preStage2.getParts();
                if (preStage2Parts.containsKey(part)) {
                    result += preStage2Parts.get(part);
                }
            }
        }
        
        return result;
    }

    /**
     * Gets the number of a good at the settlement.
     * @param good the good to check.
     * @return the number of the good (or amount (kg) if amount resource good).
     * @throws InventoryException if error determining the number of the good.
     */
    public double getNumberOfGoodForSettlement(Good good) {
        if (good != null) {
            double result = 0D;

            if (GoodType.AMOUNT_RESOURCE == good.getCategory()) 
                result = getAmountOfResourceForSettlement((AmountResource) good.getObject());
            else if (GoodType.ITEM_RESOURCE == good.getCategory())
                result = getNumberOfResourceForSettlement((ItemResource) good.getObject());
            else if (GoodType.EQUIPMENT == good.getCategory())
                result = getNumberOfEquipmentForSettlement(good.getClassType());
            else if (GoodType.VEHICLE == good.getCategory())
                result = getNumberOfVehiclesForSettlement(good.getName());

            return result;
        }
        else throw new IllegalArgumentException("Good is null.");
    }

    /**
     * Gets the amount of an amount resource for a settlement.
     * @param resource the resource to check.
     * @return amount (kg) of resource for the settlement.
     * @throws InventoryException if error getting the amount of the resource.
     */
    private double getAmountOfResourceForSettlement(AmountResource resource) {
        double amount = 0D;

        // Get amount of resource in settlement storage.
        amount += settlement.getInventory().getAmountResourceStored(resource, false);

        // Get amount of resource out on mission vehicles.
        Iterator<Mission> i = Simulation.instance().getMissionManager().getMissionsForSettlement(settlement).iterator();
        while (i.hasNext()) {
            Mission mission = i.next();
            if (mission instanceof VehicleMission) {
                Vehicle vehicle = ((VehicleMission) mission).getVehicle();
                if ((vehicle != null) && !settlement.equals(vehicle.getSettlement())) 
                    amount += vehicle.getInventory().getAmountResourceStored(resource, false);
            }
        }

        // Get amount of resource carried by people on EVA.
        Iterator<Person> j = settlement.getAllAssociatedPeople().iterator();
        while (j.hasNext()) {
            Person person = j.next();
            if (person.getLocationSituation() == LocationSituation.OUTSIDE) 
                amount += person.getInventory().getAmountResourceStored(resource, false);
        }
        
        // Get the amount of the resource that will be produced by ongoing manufacturing processes.
        Good amountResourceGood = GoodsUtil.getResourceGood(resource);
        amount += getManufacturingProcessOutput(amountResourceGood);
        
        // Get the amount of the resource that will be produced by ongoing food production processes.
        Iterator<Building> p = settlement.getBuildingManager().getBuildings(BuildingFunction.FOOD_PRODUCTION).iterator();
        while (p.hasNext()) {
            Building building = p.next();
            FoodProduction kitchen = (FoodProduction) building.getFunction(BuildingFunction.FOOD_PRODUCTION);
            
            // Go through each ongoing food production process.
            Iterator<FoodProductionProcess> q = kitchen.getProcesses().iterator();
            while (q.hasNext()) {
                FoodProductionProcess process = q.next();
                Iterator<FoodProductionProcessItem> r = process.getInfo().getOutputList().iterator();
                while (r.hasNext()) {
                    FoodProductionProcessItem item = r.next();
                    if (item.getName().equalsIgnoreCase(resource.getName())) {
                        amount += item.getAmount();
                    }
                }
            }
        }

        return amount;
    }
    
    /**
     * Gets the amount of the good being produced at the settlement by ongoing manufacturing processes. 
     * @param good the good.
     * @return amount (kg for amount resources, number for parts, equipment, and vehicles).
     */
    private double getManufacturingProcessOutput(Good good) {
        
        double result = 0D;
        
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(BuildingFunction.MANUFACTURE).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            Manufacture workshop = (Manufacture) building.getFunction(BuildingFunction.MANUFACTURE);
            
            // Go through each ongoing manufacturing process.
            Iterator<ManufactureProcess> j = workshop.getProcesses().iterator();
            while (j.hasNext()) {
                ManufactureProcess process = j.next();
                Iterator<ManufactureProcessItem> k = process.getInfo().getOutputList().iterator();
                while (k.hasNext()) {
                    ManufactureProcessItem item = k.next();
                    if (item.getName().equalsIgnoreCase(good.getName())) {
                        result += item.getAmount();
                    }
                }
            }
        }
        
        return result;
    }

    /**
     * Determines the value of an item resource.
     * @param resourceGood the resource good to check.
     * @param supply the current supply (# items) of the good.
     * @param useCache use the cache to determine value.
     * @return value (Value Points / item)
     * @throws Exception if error determining value.
     */
    //2014-12-04 Added getPartFoodProductionDemand()
    private double determineItemResourceGoodValue(Good resourceGood, double supply, boolean useCache) 
    {
        double value = 0D;
        ItemResource resource = (ItemResource) resourceGood.getObject();
        double demand = 0D;

        if (useCache) {
            if (goodsDemandCache.containsKey(resourceGood)) demand = goodsDemandCache.get(resourceGood);
            else throw new IllegalArgumentException("Good: " + resourceGood + " not valid.");

            // Clear parts demand cache so it will be calculated next time.
            partsDemandCache.clear();
        }
        else {
            // Get demand for part.
            if (resource instanceof Part) {

                Part part = (Part) resource;
                if (partsDemandCache.size() == 0) determinePartsDemand();
                if (partsDemandCache.containsKey(part)) demand = partsDemandCache.get(part);

                // Add manufacturing demand.
                demand += getPartManufacturingDemand(part);

                //2014-12-04 getPartFoodProductionDemand()
                demand += getPartFoodProductionDemand(part);
                
                // Add construction demand.
                demand += getPartConstructionDemand(part);
            }

            // Add trade demand.
            double tradeDemand = determineTradeDemand(resourceGood, useCache);
            if (tradeDemand > demand) {
                demand = tradeDemand;
            }

            goodsDemandCache.put(resourceGood, demand);
        }

        value = demand / (supply + 1D);

        return value;
    }

    /**
     * Determines the number demand for all parts at the settlement.
     * @return map of parts and their demand.
     * @throws Exception if error determining the parts demand.
     */
    private void determinePartsDemand() {
        Map<Part, Double> partsProbDemand = new HashMap<Part, Double>(ItemResource.getItemResources().size());

        // Get all malfunctionables associated with settlement.
        Iterator<Malfunctionable> i = MalfunctionFactory.getAssociatedMalfunctionables(settlement).iterator();
        while (i.hasNext()) {
            Malfunctionable entity = i.next();

            // Determine wear condition modifier.
            double wearModifier = (entity.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;

            // Estimate repair parts needed per orbit for entity.
            sumPartsDemand(partsProbDemand, getEstimatedOrbitRepairParts(entity), wearModifier);

            // Add outstanding repair parts required.
            sumPartsDemand(partsProbDemand, getOutstandingRepairParts(entity), wearModifier);

            // Estimate maintenance parts needed per orbit for entity.
            sumPartsDemand(partsProbDemand, getEstimatedOrbitMaintenanceParts(entity), wearModifier);

            // Add outstanding maintenance parts required.
            sumPartsDemand(partsProbDemand, getOutstandingMaintenanceParts(entity), wearModifier);
        }

        // Add demand for vehicle attachment parts.
        sumPartsDemand(partsProbDemand, getVehicleAttachmentParts(), 1D);

        // Store in parts demand cache.
        Iterator<Part> j = partsProbDemand.keySet().iterator();
        while (j.hasNext()) {
            Part part = j.next();
            partsDemandCache.put(part, partsProbDemand.get(part));
        }
    }

    /**
     * Sums the additional parts number map into a total parts number map.
     * @param totalPartsDemand the total parts number.
     * @param additionalPartsDemand the additional parts number.
     * @param multiplier the multiplier for the additional parts number.
     */
    private void sumPartsDemand(Map<Part, Double> totalPartsDemand, Map<Part, Number> additionalPartsDemand, 
            double multiplier) {
        Iterator<Part> i = additionalPartsDemand.keySet().iterator();
        while (i.hasNext()) {
            Part part = i.next();
            double number = additionalPartsDemand.get(part).doubleValue() * multiplier;
            if (totalPartsDemand.containsKey(part)) number += totalPartsDemand.get(part);
            totalPartsDemand.put(part, number);
        }
    }

    private Map<Part, Number> getEstimatedOrbitRepairParts(Malfunctionable entity) {
        Map<Part, Number> result = new HashMap<Part, Number>();

        MalfunctionManager manager = entity.getMalfunctionManager();

        // Estimate number of malfunctions for entity per orbit.
        double orbitMalfunctions = manager.getEstimatedNumberOfMalfunctionsPerOrbit();

        // Estimate parts needed per malfunction.
        Map<Part, Double> partsPerMalfunction = manager.getRepairPartProbabilities();

        // Multiply parts needed by malfunctions per orbit.
        Iterator<Part> i = partsPerMalfunction.keySet().iterator();
        while (i.hasNext()) {
            Part part = i.next();
            result.put(part, partsPerMalfunction.get(part) * orbitMalfunctions);
        }

        return result;
    }

    private Map<Part, Number> getOutstandingRepairParts(Malfunctionable entity) {
        Map<Part, Number> result = new HashMap<Part, Number>(0);

        Iterator<Malfunction> i = entity.getMalfunctionManager().getMalfunctions().iterator();
        while (i.hasNext()) {
            Malfunction malfunction = i.next();
            Map<Part, Integer> repairParts = malfunction.getRepairParts();
            Iterator<Part> j = repairParts.keySet().iterator();
            while (j.hasNext()) {
                Part part = j.next();
                int number = repairParts.get(part) * OUTSTANDING_REPAIR_PART_MODIFIER;
                if (result.containsKey(part)) number += result.get(part).intValue();
                result.put(part, number);
            }
        }

        return result;
    }

    private Map<Part, Number> getEstimatedOrbitMaintenanceParts(Malfunctionable entity) {
        Map<Part, Number> result = new HashMap<Part, Number>();

        MalfunctionManager manager = entity.getMalfunctionManager();

        // Estimate number of maintenances for entity per orbit.
        double orbitMaintenances = manager.getEstimatedNumberOfMaintenancesPerOrbit();

        // Estimate parts needed per maintenance.
        Map<Part, Double> partsPerMaintenance = manager.getMaintenancePartProbabilities();

        // Multiply parts needed by maintenances per orbit.
        Iterator<Part> i = partsPerMaintenance.keySet().iterator();
        while (i.hasNext()) {
            Part part = i.next();
            result.put(part, partsPerMaintenance.get(part) * orbitMaintenances);
        }

        return result;
    }

    private Map<Part, Number> getOutstandingMaintenanceParts(Malfunctionable entity) {
        Map<Part, Number> result = new HashMap<Part, Number>();

        Map<Part, Integer> maintParts = entity.getMalfunctionManager().getMaintenanceParts();
        Iterator<Part> i = maintParts.keySet().iterator();
        while (i.hasNext()) {
            Part part = i.next();
            int number = maintParts.get(part) * OUTSTANDING_MAINT_PART_MODIFIER;
            result.put(part, number);
        }

        return result;
    }

    /**
     * Gets the part demand for vehicle attachments.
     * @return map of parts and demand number.
     * @throws Exception if error getting parts.
     */
    private Map<Part, Number> getVehicleAttachmentParts() {
        Map<Part, Number> result = new HashMap<Part, Number>();

        VehicleConfig config = SimulationConfig.instance().getVehicleConfiguration();
        Iterator<Vehicle> i = settlement.getAllAssociatedVehicles().iterator();
        while (i.hasNext()) {
            String type = i.next().getDescription().toLowerCase();
            if (config.hasPartAttachments(type)) {
                Iterator<Part> j = config.getAttachableParts(type).iterator();
                while (j.hasNext()) {
                    Part part = j.next();
                    int demand = 1;
                    if (result.containsKey(part)) demand += result.get(part).intValue();
                    result.put(part, demand);
                }
            }
        }

        return result;
    }

    /**
     * Gets the manufacturing demand for a part.
     * @param part the part.
     * @return demand (# of parts)
     * @throws Exception if error getting part manufacturing demand.
     */
    private double getPartManufacturingDemand(Part part) {
        double demand = 0D;

        // Get highest manufacturing tech level in settlement.
        if (ManufactureUtil.doesSettlementHaveManufacturing(settlement)) {
            int techLevel = ManufactureUtil.getHighestManufacturingTechLevel(settlement);
            Iterator<ManufactureProcessInfo> i = ManufactureUtil.getManufactureProcessesForTechLevel(
                    techLevel).iterator();
            while (i.hasNext()) {
                double manufacturingDemand = getPartManufacturingProcessDemand(part, i.next());
                demand += manufacturingDemand;
            }
        }

        return demand;
    }

    /**
     * Gets the demand of an input part in a manufacturing process. 
     * @param part the input part.
     * @param process the manufacturing process.
     * @return demand (# of parts)
     * @throws Exception if error determining manufacturing demand.
     */
    private double getPartManufacturingProcessDemand(Part part, 
            ManufactureProcessInfo process) {
        double demand = 0D;
        double totalInputNum = 0D;

        ManufactureProcessItem partInput = null;
        Iterator<ManufactureProcessItem> i = process.getInputList().iterator();
        while (i.hasNext()) {
            ManufactureProcessItem item = i.next();
            if (
                    Type.PART.equals(item.getType()) && 
                    part.getName().equalsIgnoreCase(item.getName())
                    ) {
                partInput = item;
            }
            totalInputNum += item.getAmount();
        }

        if (partInput != null) {

            double outputsValue = 0D;
            Iterator<ManufactureProcessItem> j = process.getOutputList().iterator();
            while (j.hasNext()) {
                ManufactureProcessItem item = j.next();
                outputsValue += ManufactureUtil.getManufactureProcessItemValue(item, settlement, true);
            }

            double totalInputsValue = outputsValue * MANUFACTURING_INPUT_FACTOR;
            double partNum = partInput.getAmount();

            demand = totalInputsValue * (partNum / totalInputNum);
        }

        return demand;
    }

    

    /**
     * Gets the Food Production demand for a part.
     * @param part the part.
     * @return demand (# of parts)
     * @throws Exception if error getting part Food Production demand.
     */
    // 2014-12-04 Added getPartFoodProductionDemand()
    private double getPartFoodProductionDemand(Part part) {
        double demand = 0D;

        // Get highest Food Production tech level in settlement.
        if (FoodProductionUtil.doesSettlementHaveFoodProduction(settlement)) {
            int techLevel = FoodProductionUtil.getHighestFoodProductionTechLevel(settlement);
            Iterator<FoodProductionProcessInfo> i = FoodProductionUtil.getFoodProductionProcessesForTechLevel(
                    techLevel).iterator();
            while (i.hasNext()) {
                double FoodProductionDemand = getPartFoodProductionProcessDemand(part, i.next());
                demand += FoodProductionDemand;
            }
        }

        return demand;
    }

    /**
     * Gets the demand of an input part in a Food Production process. 
     * @param part the input part.
     * @param process the Food Production process.
     * @return demand (# of parts)
     * @throws Exception if error determining Food Production demand.
     */
    // 2014-12-04 Added getPartFoodProductionProcessDemand()
    private double getPartFoodProductionProcessDemand(Part part, 
    		FoodProductionProcessInfo process) {
        double demand = 0D;
        double totalInputNum = 0D;

        FoodProductionProcessItem partInput = null;
        Iterator<FoodProductionProcessItem> i = process.getInputList().iterator();
        while (i.hasNext()) {
        	FoodProductionProcessItem item = i.next();
            if (
                    Type.PART.equals(item.getType()) && 
                    part.getName().equalsIgnoreCase(item.getName())
                    ) {
                partInput = item;
            }
            totalInputNum += item.getAmount();
        }

        if (partInput != null) {

            double outputsValue = 0D;
            Iterator<FoodProductionProcessItem> j = process.getOutputList().iterator();
            while (j.hasNext()) {
            	FoodProductionProcessItem item = j.next();
                outputsValue += FoodProductionUtil.getFoodProductionProcessItemValue(item, settlement, true);
            }

            double totalInputsValue = outputsValue * FOOD_PRODUCTION_INPUT_FACTOR;
            double partNum = partInput.getAmount();

            demand = totalInputsValue * (partNum / totalInputNum);
        }

        return demand;
    }
    
    /**
     * Gets the construction demand for a part.
     * @param part the part.
     * @return demand
     * @throws Exception if error getting part construction demand.
     */
    private double getPartConstructionDemand(Part part) {
        double demand = 0D;

        ConstructionValues values = settlement.getConstructionManager().getConstructionValues();
        Map<ConstructionStageInfo, Double> stageValues = values.getAllConstructionStageValues();
        Iterator<ConstructionStageInfo> i = stageValues.keySet().iterator();
        while (i.hasNext()) {
            ConstructionStageInfo stage = i.next();
            double stageValue = stageValues.get(stage);
            if (stageValue > 0D && ConstructionStageInfo.BUILDING.equals(stage.getType()) && 
                    isLocallyConstructable(stage)) {
                double constructionStageDemand = getPartConstructionStageDemand(part, stage, stageValue);
                if (constructionStageDemand > 0D) {
                    demand += constructionStageDemand;
                }
            }
        }

        return demand;
    }

    /**
     * Gets the demand for a part as an input for a particular building construction stage.
     * @param part the part.
     * @param stage the building construction stage.
     * @param stageValue the building construction stage value (VP).
     * @return demand
     * @throws Exception if error determining demand for part.
     */
    private double getPartConstructionStageDemand(Part part, ConstructionStageInfo stage, 
            double stageValue) {
        double demand = 0D;

        Map<AmountResource, Double> resources = getAllPrerequisiteConstructionResources(stage);
        Map<Part, Integer> parts = getAllPrerequisiteConstructionParts(stage);
        int partNumber = getPrerequisiteConstructionPartNum(part, stage);

        if (partNumber > 0) {
            double totalNumber = 0D;

            Iterator<AmountResource> i = resources.keySet().iterator();
            while (i.hasNext()) totalNumber += resources.get(i.next());

            Iterator<Part> j = parts.keySet().iterator();
            while (j.hasNext()) totalNumber += parts.get(j.next());

            double totalInputsValue = stageValue * CONSTRUCTING_INPUT_FACTOR;

            demand = totalInputsValue * (partNumber / totalNumber);
        }

        return demand;
    }

    /**
     * Gets the number of an item resource for a settlement.
     * @param resource the resource to check.
     * @return number of resource for the settlement.
     * @throws InventoryException if error getting the number of the resource.
     */
    private double getNumberOfResourceForSettlement(ItemResource resource) {
        double number = 0D;

        // Get number of resources in settlement storage.
        number += settlement.getInventory().getItemResourceNum(resource);

        // Get number of resources out on mission vehicles.
        Iterator<Mission> i = Simulation.instance().getMissionManager().getMissionsForSettlement(settlement).iterator();
        while (i.hasNext()) {
            Mission mission = i.next();
            if (mission instanceof VehicleMission) {
                Vehicle vehicle = ((VehicleMission) mission).getVehicle();
                if ((vehicle != null) && !settlement.equals(vehicle.getSettlement())) 
                    number += vehicle.getInventory().getItemResourceNum(resource);
            }
        }

        // Get number of resources carried by people on EVA.
        Iterator<Person> j = settlement.getAllAssociatedPeople().iterator();
        while (j.hasNext()) {
            Person person = j.next();
            if (person.getLocationSituation() == LocationSituation.OUTSIDE) 
                number += person.getInventory().getItemResourceNum(resource);
        }
        
        // Get the number of resources that will be produced by ongoing manufacturing processes.
        Good amountResourceGood = GoodsUtil.getResourceGood(resource);
        number += getManufacturingProcessOutput(amountResourceGood);

        return number;
    }

    /**
     * Determines the value of an equipment.
     * @param equipmentGood the equipment good to check.
     * @param supply the current supply (# of items) of the good.
     * @param useCache use the cache to determine value.
     * @return the value (value points) 
     * @throws Exception if error determining value.
     */
    private double determineEquipmentGoodValue(Good equipmentGood, double supply, boolean useCache) {
        double value = 0D;
        double demand = 0D;

        if (useCache) {
            if (goodsDemandCache.containsKey(equipmentGood)) demand = goodsDemandCache.get(equipmentGood);
            else throw new IllegalArgumentException("Good: " + equipmentGood + " not valid.");
        }
        else {
            // Determine demand amount.
            demand = determineEquipmentDemand(equipmentGood.getClassType());

            // Add trade demand.
//            demand += determineTradeDemand(equipmentGood, useCache);
            double tradeDemand = determineTradeDemand(equipmentGood, useCache);
            if (tradeDemand > demand) {
                demand = tradeDemand;
            }

            goodsDemandCache.put(equipmentGood, demand);
        }

        value = demand / (supply + 1D);

        return value;
    }

    /**
     * Determines the demand for a type of equipment.
     * @param equipmentClass the equipment class.
     * @return demand (# of equipment).
     * @throws Exception if error getting demand.
     */
    private double determineEquipmentDemand(Class<? extends Equipment> equipmentClass) {
        double numDemand = 0D;

        // Determine number of EVA suits that are needed
        if (EVASuit.class.equals(equipmentClass)) {
            numDemand += 2D * settlement.getAllAssociatedPeople().size() * EVA_SUIT_FACTOR;
        }

        // Determine the number of containers that are needed.
        if (Container.class.isAssignableFrom(equipmentClass) && 
                !SpecimenContainer.class.equals(equipmentClass)) {

            Phase containerPhase = ContainerUtil.getContainerPhase((Class<? extends 
                    Container>) equipmentClass);
            double containerCapacity = ContainerUtil.getContainerCapacity((Class<? 
                    extends Container>) equipmentClass);

            double totalPhaseOverfill = 0D;
            Iterator<AmountResource> i = AmountResource.getAmountResources().iterator();
            while (i.hasNext()) {
                AmountResource resource = i.next();
                if (resource.getPhase() == containerPhase) {
                    double settlementCapacity = settlement.getInventory().
                            getAmountResourceCapacityNoContainers(resource);
                    Good resourceGood = GoodsUtil.getResourceGood(resource);
                    double resourceDemand = 0D;
                    if (goodsDemandCache.containsKey(resourceGood)) {
                        resourceDemand = goodsDemandCache.get(resourceGood);
                    }
                    if (resourceDemand > settlementCapacity) {
                        double resourceOverfill = resourceDemand - settlementCapacity;
                        totalPhaseOverfill += resourceOverfill;
                    }
                }
            }

            numDemand = totalPhaseOverfill * containerCapacity / 10000D;
        }

        int areologistNum = getAreologistNum();

        // Determine number of bags that are needed.
        if (Bag.class.equals(equipmentClass)) {
            AmountResource ice = AmountResource.findAmountResource("ice");
            double iceValue = getGoodValuePerItem(GoodsUtil.getResourceGood(ice));
            AmountResource regolith = AmountResource.findAmountResource("regolith");
            double regolithValue = getGoodValuePerItem(GoodsUtil.getResourceGood(regolith));
            numDemand += CollectIce.REQUIRED_BAGS * areologistNum * iceValue;
            numDemand += CollectRegolith.REQUIRED_BAGS * areologistNum * regolithValue;
        }

        // Determine number of specimen containers that are needed.
        if (SpecimenContainer.class.equals(equipmentClass)) {
            numDemand +=  Exploration.REQUIRED_SPECIMEN_CONTAINERS * areologistNum;
        }

        return numDemand;
    }

    /**
     * Gets all non-empty containers of a given type associated with this settlement.
     * @param equipmentClass the equipment type.
     * @return number of non-empty containers.
     * @throws Exception if error determining containers.
     */
    private int getNonEmptyContainers(Class<? extends Equipment> equipmentClass) {
        int result = 0;

        Inventory inv = settlement.getInventory();
        Collection<Unit> equipmentList = inv.findAllUnitsOfClass(equipmentClass);
        MissionManager missionManager = Simulation.instance().getMissionManager();
        Iterator<Mission> i = missionManager.getMissionsForSettlement(settlement).iterator();
        while (i.hasNext()) {
            Mission mission = i.next();
            if (mission instanceof VehicleMission) {
                Vehicle vehicle = ((VehicleMission) mission).getVehicle();
                if ((vehicle != null) && (vehicle.getSettlement() == null)) {
                    Inventory vehicleInv = vehicle.getInventory();
                    Iterator <Unit> j = vehicleInv.findAllUnitsOfClass(equipmentClass).iterator();
                    while (j.hasNext()) {
                        Unit equipment = j.next();
                        if (!equipmentList.contains(equipment)) equipmentList.add(equipment);
                    }
                }
            }
        }

        Iterator<Unit> k = equipmentList.iterator();
        while (k.hasNext()) {
            if (k.next().getInventory().getAllAmountResourcesStored(false).size() > 0D) result++;
        }

        return result;
    }

    /**
     * Gets the number of areologists associated with the settlement.
     * @return number of areologists.
     */
    private int getAreologistNum() {
        int result = 0;
        Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
        while (i.hasNext()) {
            if (i.next().getMind().getJob() instanceof Areologist) result ++;
        }
        return result;
    }

    /**
     * Gets the number of biologists associated with the settlement.
     * @return number of biologists.
     */
    private int getBiologistNum() {
        int result = 0;
        Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
        while (i.hasNext()) {
            if (i.next().getMind().getJob() instanceof Biologist) result ++;
        }
        return result;
    }

    /**
     * Gets the number of architect associated with the settlement.
     * @return number of architects.
     */
    private int getArchitectNum() {
        int result = 0;
        Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
        while (i.hasNext()) {
            if (i.next().getMind().getJob() instanceof Architect) result ++;
        }
        return result;
    }

    /**
     * Gets the number of drivers associated with the settlement.
     * @return number of drivers.
     */
    private int getDriverNum() {
        int result = 0;
        Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
        while (i.hasNext()) {
            if (i.next().getMind().getJob() instanceof Driver) result ++;
        }
        return result;
    }

    /**
     * Gets the number of traders associated with the settlement.
     * @return number of traders.
     */
    private int getTraderNum() {
        int result = 0;
        Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
        while (i.hasNext()) {
            if (i.next().getMind().getJob() instanceof Trader) result ++;
        }
        return result;
    }

    /**
     * Gets the number of equipment for a settlement.
     * @param equipmentClass the equipmentType to check.
     * @return number of equipment for the settlement.
     * @throws InventoryException if error getting the number of the equipment.
     */
    private double getNumberOfEquipmentForSettlement(
            Class<? extends Equipment> equipmentClass) {
        double number = 0D;

        // Get number of the equipment in settlement storage.
        number += settlement.getInventory().findNumEmptyUnitsOfClass(equipmentClass, false);

        // Get number of equipment out on mission vehicles.
        Iterator<Mission> i = Simulation.instance().getMissionManager().getMissionsForSettlement(settlement).iterator();
        while (i.hasNext()) {
            Mission mission = i.next();
            if (mission instanceof VehicleMission) {
                Vehicle vehicle = ((VehicleMission) mission).getVehicle();
                if ((vehicle != null) && !settlement.equals(vehicle.getSettlement())) 
                    number += vehicle.getInventory().findNumEmptyUnitsOfClass(equipmentClass, false);
            }
        }

        // Get number of equipment carried by people on EVA.
        Iterator<Person> j = settlement.getAllAssociatedPeople().iterator();
        while (j.hasNext()) {
            Person person = j.next();
            if (person.getLocationSituation() == LocationSituation.OUTSIDE) 
                number += person.getInventory().findNumEmptyUnitsOfClass(equipmentClass, false);
        }
        
        // Get the number of equipment that will be produced by ongoing manufacturing processes.
        Good equipmentGood = GoodsUtil.getEquipmentGood(equipmentClass);
        number += getManufacturingProcessOutput(equipmentGood);

        return number;
    }

    /**
     * Determines the value of a vehicle good.
     * @param vehicleGood the vehicle good.
     * @param supply the current supply (# of vehicles) of the good.
     * @param useCache use the cache to determine value.
     * @return the value (value points).
     * @throws Exception if error determining vehicle value.
     */
    private double determineVehicleGoodValue(Good vehicleGood, double supply, boolean useCache) {
        double value = 0D;

        String vehicleType = vehicleGood.getName();

        boolean buy = false;
        double currentNum = getNumberOfVehiclesForSettlement(vehicleType);
        if (supply == currentNum) {
            buy = true;
        }

        if (vehicleBuyValueCache == null) {
            vehicleBuyValueCache = new HashMap<String, Double>();
        }
        if (vehicleSellValueCache == null) {
            vehicleSellValueCache = new HashMap<String, Double>();
        }

        if (useCache) {
            if (buy) {
                if (vehicleBuyValueCache.containsKey(vehicleType)) {
                    value = vehicleBuyValueCache.get(vehicleType);
                }
                else {
                    value = determineVehicleGoodValue(vehicleGood, supply, false);
                }
            }
            else {
                if (vehicleSellValueCache.containsKey(vehicleType)) {
                    value = vehicleSellValueCache.get(vehicleType);
                }
                else {
                    value = determineVehicleGoodValue(vehicleGood, supply, false);
                }
            }
        }
        else {
            if (vehicleType.equalsIgnoreCase("light utility vehicle")) { 
                value = determineLUVValue(buy);
            }
            else {
                double travelToSettlementMissionValue = determineMissionVehicleValue(TRAVEL_TO_SETTLEMENT_MISSION, 
                        vehicleType, buy);
                if (travelToSettlementMissionValue > value) {
                    value = travelToSettlementMissionValue;
                }

                double explorationMissionValue = determineMissionVehicleValue(EXPLORATION_MISSION, vehicleType, buy);
                if (explorationMissionValue > value) {
                    value = explorationMissionValue;
                }

                double collectIceMissionValue = determineMissionVehicleValue(COLLECT_ICE_MISSION, vehicleType, buy);
                if (collectIceMissionValue > value) {
                    value = collectIceMissionValue;
                }

                double rescueMissionValue = determineMissionVehicleValue(RESCUE_SALVAGE_MISSION, vehicleType, buy);
                if (rescueMissionValue > value) {
                    value = rescueMissionValue;
                }

                double tradeMissionValue = determineMissionVehicleValue(TRADE_MISSION, vehicleType, buy);
                if (tradeMissionValue > value) {
                    value = tradeMissionValue;
                }

                double collectRegolithMissionValue = determineMissionVehicleValue(COLLECT_REGOLITH_MISSION, 
                        vehicleType, buy);
                if (collectRegolithMissionValue > value){
                    value = collectRegolithMissionValue;
                }

                double miningMissionValue = determineMissionVehicleValue(MINING_MISSION, vehicleType, buy);
                if (miningMissionValue > value) {
                    value = miningMissionValue;
                }

                double constructionMissionValue = determineMissionVehicleValue(CONSTRUCT_BUILDING_MISSION, vehicleType, buy);
                if (constructionMissionValue > value) {
                    value = constructionMissionValue;
                }
                
                double salvageMissionValue = determineMissionVehicleValue(SALVAGE_BUILDING_MISSION, vehicleType, buy);
                if (salvageMissionValue > value) {
                    value = salvageMissionValue;
                }

                double areologyFieldMissionValue = determineMissionVehicleValue(AREOLOGY_STUDY_FIELD_MISSION, vehicleType, buy);
                if (areologyFieldMissionValue > value) {
                    value = areologyFieldMissionValue;
                }

                double biologyFieldMissionValue = determineMissionVehicleValue(BIOLOGY_STUDY_FIELD_MISSION, vehicleType, buy);
                if (biologyFieldMissionValue > value) {
                    value = biologyFieldMissionValue;
                }
                
                double emergencySupplyMissionValue = determineMissionVehicleValue(EMERGENCY_SUPPLY_MISSION, vehicleType, buy);
                if (emergencySupplyMissionValue > value) {
                    value = emergencySupplyMissionValue;
                }
            }

            // Multiply by vehicle factor.
            value *= VEHICLE_FACTOR;

            double tradeValue = determineTradeVehicleValue(vehicleGood, useCache);
            if (tradeValue > value) {
                value = tradeValue;
            }

            if (buy) {
                vehicleBuyValueCache.put(vehicleType, value);
            }
            else {
                vehicleSellValueCache.put(vehicleType, value);
            }
        }

        return value;
    }

    private double determineTradeVehicleValue(Good vehicleGood, boolean useCache) {
        double tradeDemand = determineTradeDemand(vehicleGood, useCache);
        double supply = getNumberOfVehiclesForSettlement(vehicleGood.getName());
        return tradeDemand / (supply + 1D);
    }

    /**
     * Determine the value of a light utility vehicle.
     * @param buy true if vehicles can be bought.
     * @return value (VP)
     */
    private double determineLUVValue(boolean buy) {

        double demand = 0D;

        // Add demand for mining missions.
        demand += getAreologistNum();

        // Add demand for construction missions.
        demand += getArchitectNum();

        double supply = getNumberOfVehiclesForSettlement("light utility vehicle");
        if (!buy) supply--;
        if (supply < 0D) supply = 0D;

        return demand / (supply + 1D);
    }

    private double determineMissionVehicleValue(String missionType, String vehicleType, boolean buy) {

        double demand = determineMissionVehicleDemand(missionType);

        double currentCapacity = 0D;
        boolean soldFlag = false;
        Iterator<Vehicle> i = settlement.getAllAssociatedVehicles().iterator();
        while (i.hasNext()) {
            String type = i.next().getDescription().toLowerCase();
            if (!buy && !soldFlag && (type.equalsIgnoreCase(vehicleType))) soldFlag = true;
            else currentCapacity += determineMissionVehicleCapacity(missionType, type);
        }

        double vehicleCapacity = determineMissionVehicleCapacity(missionType, vehicleType);

        double baseValue = (demand / (currentCapacity + 1D)) * vehicleCapacity;

        return baseValue;
    }

    private double determineMissionVehicleDemand(String missionType) {
        double demand = 0D;

        if (TRAVEL_TO_SETTLEMENT_MISSION.equals(missionType)) {
            demand = getDriverNum();
            demand *= ((double) settlement.getAllAssociatedPeople().size() / 
                    (double) settlement.getPopulationCapacity());
        }
        else if (EXPLORATION_MISSION.equals(missionType)) {
            demand = getAreologistNum();
        }
        else if (COLLECT_ICE_MISSION.equals(missionType)) {
            AmountResource ice = AmountResource.findAmountResource("ice");
            demand = getGoodValuePerItem(GoodsUtil.getResourceGood(ice));
            if (demand > 10D) demand = 10D;
        }
        else if (RESCUE_SALVAGE_MISSION.equals(missionType)) {
            demand = getDriverNum();
        }
        else if (TRADE_MISSION.equals(missionType)) {
            demand = getTraderNum();
        }
        else if (COLLECT_REGOLITH_MISSION.equals(missionType)) {
            AmountResource regolith = AmountResource.findAmountResource("regolith");
            demand = getGoodValuePerItem(GoodsUtil.getResourceGood(regolith));
            if (demand > 10D) demand = 10D;
        }
        else if (MINING_MISSION.equals(missionType)) {
            demand = getAreologistNum();
        }
        else if (CONSTRUCT_BUILDING_MISSION.equals(missionType)) {
            // No demand for rover vehicles.
        }
        else if (SALVAGE_BUILDING_MISSION.equals(missionType)) {
            // No demand for rover vehicles.
        }
        else if (AREOLOGY_STUDY_FIELD_MISSION.equals(missionType)) {
            demand = getAreologistNum();
        }
        else if (BIOLOGY_STUDY_FIELD_MISSION.equals(missionType)) {
            demand = getBiologistNum();
        }
        else if (EMERGENCY_SUPPLY_MISSION.equals(missionType)) {
            demand = Simulation.instance().getUnitManager().getSettlementNum() - 1D;
            if (demand < 0D) {
                demand = 0D;
            }
        }

        return demand;
    }

    private double determineMissionVehicleCapacity(String missionType, String vehicleType) {
        double capacity = 0D;

        VehicleConfig config = SimulationConfig.instance().getVehicleConfiguration();
        VehicleDescription v = config.getVehicleDescription(vehicleType);
        int crewCapacity = v.getCrewSize();

        if (TRAVEL_TO_SETTLEMENT_MISSION.equals(missionType)) {
            if (crewCapacity >= 2) capacity = 1D;
            capacity *= crewCapacity / 8D;

            double range = getVehicleRange(v);
            capacity *= range / 2000D;
        }
        else if (EXPLORATION_MISSION.equals(missionType)) {
            if (crewCapacity >= 2) capacity = 1D;

            double cargoCapacity = v.getTotalCapacity();
            if (cargoCapacity < 500D) capacity = 0D;

            boolean hasAreologyLab = false;
            if (v.hasLab()) {
                if (v.getLabTechSpecialties().contains("Areology")) hasAreologyLab = true;
            }
            if (!hasAreologyLab) capacity /= 2D;

            double range = getVehicleRange(v);
            if (range == 0D) capacity = 0D;
        }
        else if (COLLECT_ICE_MISSION.equals(missionType)) {
            if (crewCapacity >= 2) capacity = 1D;

            double cargoCapacity = v.getTotalCapacity();
            if (cargoCapacity < 1250D) capacity = 0D;

            double range = getVehicleRange(v);
            if (range == 0D) capacity = 0D;
        }
        else if (RESCUE_SALVAGE_MISSION.equals(missionType)) {
            if (crewCapacity >= 2) capacity = 1D;

            double range = getVehicleRange(v);
            capacity *= range / 2000D;
        }
        else if (TRADE_MISSION.equals(missionType)) {
            if (crewCapacity >= 2) capacity = 1D;

            double cargoCapacity = v.getTotalCapacity();
            capacity *= cargoCapacity / 10000D;

            double range = getVehicleRange(v);
            capacity *= range / 2000D;
        }
        else if (COLLECT_REGOLITH_MISSION.equals(missionType)) {
            if (crewCapacity >= 2) capacity = 1D;

            double cargoCapacity = v.getTotalCapacity();
            if (cargoCapacity < 1250D) capacity = 0D;

            double range = getVehicleRange(v);
            if (range == 0D) capacity = 0D;
        }
        else if (MINING_MISSION.equals(missionType)) {
            if (crewCapacity >= 2) capacity = 1D;

            double cargoCapacity = v.getTotalCapacity();
            if (cargoCapacity < 1000D) capacity = 0D;

            double range = getVehicleRange(v);
            if (range == 0D) capacity = 0D;
        }
        else if (CONSTRUCT_BUILDING_MISSION.equals(missionType)) {
            // No rover vehicles needed.
        }
        else if (SALVAGE_BUILDING_MISSION.equals(missionType)) {
            // No rover vehicles needed.
        }
        else if (AREOLOGY_STUDY_FIELD_MISSION.equals(missionType)) {
            if (crewCapacity >= 2) capacity = 1D;

            if (v.hasLab()) {
                if (v.getLabTechSpecialties().contains("Areology")) {
                    capacity += v.getLabTechLevel();
                }
                else {
                    capacity /= 2D;
                }
            }

            double range = getVehicleRange(v);
            if (range == 0D) capacity = 0D;
        }
        else if (BIOLOGY_STUDY_FIELD_MISSION.equals(missionType)) {
            if (crewCapacity >= 2) capacity = 1D;

            if (v.hasLab()) {
                if (v.getLabTechSpecialties().contains("Biology")) {
                    capacity += v.getLabTechLevel();
                }
                else {
                    capacity /= 2D;
                }
            }

            double range = getVehicleRange(v);
            if (range == 0D) capacity = 0D;
        }
        else if (EMERGENCY_SUPPLY_MISSION.equals(missionType)) {
            if (crewCapacity >= 2) capacity = 1D;

            double cargoCapacity = v.getTotalCapacity();
            capacity *= cargoCapacity / 10000D;

            double range = getVehicleRange(v);
            capacity *= range / 2000D;
        }

        return capacity;
    }

    /**
     * Gets the range of the vehicle type.
     * @param v {@link VehicleDescription}.
     * @return range (km)
     * @throws Exception if error determining range.
     */
    private double getVehicleRange(VehicleDescription v) {
        double range = 0D;

        double fuelCapacity = v.getCargoCapacity("methane");
        double fuelEfficiency = v.getFuelEff();
        range = fuelCapacity * fuelEfficiency / 1.5D;

        double baseSpeed = v.getBaseSpeed();
        double distancePerSol = baseSpeed / 2D / 60D / 60D / MarsClock.convertSecondsToMillisols(1D) * 1000D;

        PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
        int crewSize = v.getCrewSize();

        // Check food capacity as range limit.
        double foodConsumptionRate = personConfig.getFoodConsumptionRate();
        double foodCapacity = v.getCargoCapacity(LifeSupport.FOOD);
        double foodSols = foodCapacity / (foodConsumptionRate * crewSize);
        double foodRange = distancePerSol * foodSols / 3D;
        if (foodRange < range) range = foodRange;

        // Check water capacity as range limit.
        double waterConsumptionRate = personConfig.getWaterConsumptionRate();
        double waterCapacity = v.getCargoCapacity(LifeSupport.WATER);
        double waterSols = waterCapacity / (waterConsumptionRate * crewSize);
        double waterRange = distancePerSol * waterSols / 3D;
        if (waterRange < range) range = waterRange;

        // Check oxygen capacity as range limit.
        double oxygenConsumptionRate = personConfig.getOxygenConsumptionRate();
        double oxygenCapacity = v.getCargoCapacity(LifeSupport.OXYGEN);
        double oxygenSols = oxygenCapacity / (oxygenConsumptionRate * crewSize);
        double oxygenRange = distancePerSol * oxygenSols / 3D;
        if (oxygenRange < range) range = oxygenRange;

        return range;
    }

    /**
     * Gets the number of the vehicle for the settlement.
     * @param vehicleType the vehicle type.
     * @return the number of vehicles.
     * @throws InventoryException if error getting the amount.
     */
    private double getNumberOfVehiclesForSettlement(String vehicleType) {
        double number = 0D;

        Iterator<Vehicle> i = settlement.getAllAssociatedVehicles().iterator();
        while (i.hasNext()) {
            Vehicle vehicle = i.next();
            if (vehicleType.equalsIgnoreCase(vehicle.getDescription())) number += 1D;
        }
        
        // Get the number of vehicles that will be produced by ongoing manufacturing processes.
        Good vehicleGood = GoodsUtil.getVehicleGood(vehicleType);
        number += getManufacturingProcessOutput(vehicleGood);

        return number;
    }

    /**
     * Determines the trade demand for a good at a settlement.
     * @param good the good.
     * @param useTradeCache use the goods trade cache to determine trade demand?
     * @return the trade demand.
     * @throws Exception if error determining trade demand.
     */
    private double determineTradeDemand(Good good, boolean useTradeCache) {
        if (useTradeCache) {
            if (goodsTradeCache.containsKey(good)) return goodsTradeCache.get(good);
            else throw new IllegalArgumentException("good: " + good + " not valid.");
        }
        else {
            double bestTradeValue = 0D;

            Iterator<Settlement> i = Simulation.instance().getUnitManager().getSettlements().iterator();
            while (i.hasNext()) {
                Settlement tempSettlement = i.next();
                if (tempSettlement != settlement) {
                    double baseValue = tempSettlement.getGoodsManager().getGoodValuePerItem(good);
                    double distance = settlement.getCoordinates().getDistance(tempSettlement.getCoordinates());
                    double tradeValue = baseValue / (1D + (distance / 1000D));
                    if (tradeValue > bestTradeValue) bestTradeValue = tradeValue;
                }
            }
            goodsTradeCache.put(good, bestTradeValue);
            return bestTradeValue;
        }
    }

    /**
     * Prepare the goods manager for a vehicle load calculation.
     */
    public void prepareForLoadCalculation() {
        // Clear vehicle buy and sell value caches.
        vehicleBuyValueCache.clear();
        vehicleSellValueCache.clear();
    }

    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        settlement = null;
        goodsValues.clear();
        goodsValues = null;
        goodsDemandCache.clear();
        goodsDemandCache = null;
        goodsTradeCache.clear();
        goodsTradeCache = null;
        resourceProcessingCache.clear();
        resourceProcessingCache = null;

        if (vehicleBuyValueCache != null){
            vehicleBuyValueCache.clear();
            vehicleBuyValueCache = null;
        }

        if (vehicleSellValueCache != null){
            vehicleSellValueCache.clear();
            vehicleSellValueCache = null;
        }

        if (partsDemandCache != null){

            partsDemandCache.clear();
            partsDemandCache = null;
        }

        // Destroy goods list in GoodsUtil.
        GoodsUtil.destroyGoodsList();
    }
}