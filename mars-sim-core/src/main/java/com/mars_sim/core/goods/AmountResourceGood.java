/*
 * Mars Simulation Project
 * AmountResourceGood.java
 * @date 2025-07-26
 * @author Barry Evans
 */
package com.mars_sim.core.goods;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.building.construction.ConstructionStage;
import com.mars_sim.core.building.construction.ConstructionStageInfo;
import com.mars_sim.core.building.construction.ConstructionUtil;
import com.mars_sim.core.building.construction.ConstructionValues;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.LivingAccommodation;
import com.mars_sim.core.building.function.cooking.Cooking;
import com.mars_sim.core.building.function.cooking.DishRecipe;
import com.mars_sim.core.building.function.cooking.MealConfig;
import com.mars_sim.core.building.function.farming.Crop;
import com.mars_sim.core.building.function.farming.Farming;
import com.mars_sim.core.food.FoodProductionProcess;
import com.mars_sim.core.food.FoodProductionProcessInfo;
import com.mars_sim.core.food.FoodProductionUtil;
import com.mars_sim.core.goods.GoodsManager.CommerceType;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.manufacture.ManufactureUtil;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.resourceprocess.ResourceProcess;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * This represents how a Amount Resource can be traded.
 */
class AmountResourceGood extends Good {
	
	private static final long serialVersionUID = 1L;

	private static final String NACO3 = "Sodium Carbonate";
	private static final String IRON_POWDER = "Iron Powder";
	
	private static final double INITIAL_AMOUNT_DEMAND = 0;
	private static final double INITIAL_AMOUNT_SUPPLY = 0;

	private static final double WASTE_WATER_VALUE_MODIFIER = 1.5;
	private static final double GREY_WATER_VALUE_MODIFIER = 1;
	private static final double BLACK_WATER_VALUE_MODIFIER = .5;
	private static final double USEFUL_WASTE_VALUE_MODIFIER = 1.05D;

	// Cost modifiers
	private static final double CH4_COST = 0.3;
	private static final double METHANOL_COST = 0.4;
	private static final double H2_COST = 1;
	private static final double CO_COST = 0.05;
    private static final double CO2_COST = 0.0000005;
	private static final double CL_COST = 0.25;
	private static final double ICE_COST = 0.5;
	private static final double FOOD_COST = 0.1;
	private static final double DERIVED_COST = .07;
	private static final double SOY_COST = 0.05;
	private static final double CROP_COST = 5;
	private static final double ANIMAL_COST = 10;
	private static final double ORGANISM_COST = 10;
	private static final double CHEMICAL_COST = 0.01;
	private static final double MEDICAL_COST = 0.01;
	private static final double WASTE_COST = 0.0001;
	private static final double OIL_COST = 0.01;
	private static final double ROCK_COST = 5;
	private static final double REGOLITH_COST = 0.02;
	private static final double ORE_COST = 0.3;
	private static final double MINERAL_COST = 0.3;
	private static final double ELEMENT_COST = 0.5;
	private static final double LIFE_SUPPORT_COST = 0.5;


	// modifiers
    private static final double ICE_VALUE_MODIFIER = 0.1;
	private static final double WATER_VALUE_MODIFIER = 0.2;
	private static final double BRINE_WATER_VALUE_MODIFIER  = 0.04;
	
	private static final double SOIL_VALUE_MODIFIER = 0.05;
	private static final double SAND_VALUE_MODIFIER = 0.03;
	private static final double ORES_VALUE_MODIFIER = 0.05;
	
	private static final double CONCRETE_VALUE_MODIFIER = 0.7;
	private static final double CEMENT_VALUE_MODIFIER = 2;
	private static final double MINERAL_VALUE_MODIFIER = 0.02;
	private static final double ROCK_VALUE_MODIFIER = 0.02;
	private static final double METEORITE_VALUE_MODIFIER = 100;
	
	private static final double ROCK_SALT_VALUE_MODIFIER = 1;
	private static final double EPSOM_SALT_VALUE_MODIFIER = 0.1;
	
	private static final double FOOD_VALUE_MODIFIER = 0.1;
	
	private static final double OXYGEN_VALUE_MODIFIER = 10;
	private static final double METHANE_VALUE_MODIFIER = 0.07;
	private static final double HYDROGEN_VALUE_MODIFIER = 0.005;
	private static final double METHANOL_VALUE_MODIFIER = 0.05;
	
	private static final double CO2_VALUE_MODIFIER = 0.0075;

	private static final double LIFE_SUPPORT_FACTOR = .005;
	private static final double VEHICLE_FUEL_FACTOR = 1;
	private static final double FARMING_FACTOR = .1;

	private static final double LEAVES_VALUE_MODIFIER = .5;

	private static final double DESSERT_FACTOR = .1;
	
	private static final double TISSUE_CULTURE_VALUE = 0.5;
	
	private static final double REGOLITH_TYPE_VALUE_MODIFIER = 2;
	private static final double REGOLITH_VALUE_MODIFIER = 2.0;
	private static final double REGOLITH_VALUE_MODIFIER_1 = 10;
	private static final double REGOLITH_VALUE_MODIFIER_2 = 10;
		
	// flatten multipliers
	private static final double ORE_FLATTENING_FACTOR = 1.1;
	private static final double MINERAL_FLATTENING_FACTOR = 1.1;
	private static final double ROCK_FLATTENING_FACTOR = 1;
	private static final double REGOLITH_FLATTENING_FACTOR = 2;
	private static final double SAND_FLATTENING_FACTOR = 1;
	
	private static final double OLIVINE_FLATTENING_FACTOR = 0.5;
	private static final double KAMACITE_FLATTENING_FACTOR = 0.2;
	
	private static final double CHEMICAL_FLATTENING_FACTOR = 3;
	private static final double COMPOUND_FLATTENING_FACTOR = 2;
	private static final double ELEMENT_FLATTENING_FACTOR = 4;

	private static final double GEMSTONE_FLATTENING_FACTOR = 3;

	private static final double WASTE_FLATTENING_FACTOR = 0.15;
	
	private static final double UTILITY_FLATTENING_FACTOR = 10;
	private static final double INSTRUMENT_FLATTENING_FACTOR = 5;

	private static final double INSECT_FLATTENING_FACTOR = 5;
	private static final double ORGANISM_FLATTENING_FACTOR = 2;
	private static final double SOYBASED_FLATTENING_FACTOR = 0.5;
	private static final double ANIMAL_FLATTENING_FACTOR = 2;
	private static final double CROP_FLATTENING_FACTOR = 2;
	private static final double DERIVED_FLATTENING_FACTOR = 2;
	private static final double TISSUE_FLATTENING_FACTOR = 4;
	
	private static final double METHANOL_FLATTENING_FACTOR = 0.9;
	private static final double METHANE_FLATTENING_FACTOR = 1.3;
	private static final double HYDROGEN_FLATTENING_FACTOR = 0.85;
	private static final double OXYGEN_FLATTENING_FACTOR = .5;	
	
	private static final double ACETYLENE_FLATTENING_FACTOR = 0.025;
	private static final double CO_FLATTENING_FACTOR = 0.09;
	private static final double CO2_FLATTENING_FACTOR = 0.06;
	

	private static final double ICE_FLATTENING_FACTOR = 0.05;

	private static final double NACO3_FLATTENING_FACTOR = 0.5;
	private static final double IRON_POWDER_FLATTENING_FACTOR = 0.005;
	
	private static final double WATER_FLATTENING_FACTOR = 1.2;
	
	private static final double COOKED_MEAL_INPUT_FACTOR = 0.05;
	
	private static final double MANUFACTURING_INPUT_FACTOR = 2D;
	private static final double FOOD_PRODUCTION_INPUT_FACTOR = 0.1;
	private static final double CONSTRUCTION_SITE_REQUIRED_RESOURCE_FACTOR = 1000D;
	private static final double CONSTRUCTING_INPUT_FACTOR = 2D;

	private static final double MAX_RESOURCE_PROCESSING_DEMAND = 1500; 
	private static final double MAX_MANUFACTURING_DEMAND = 1500;
	private static final double MAX_FOOD_PRODUCTION_DEMAND = 1500;
	/** The factor due to the population. */
	private double popFactor;
	/** The fixed flatten demand for this resource. */
	private double flattenDemand;
	/** The projected demand of each refresh cycle. */
	private double projectedDemand;
	/** The trade demand of each refresh cycle. */
	private double tradeDemand;
	/** The ingredient demand of each refresh cycle. */
	private double ingredientDemand;
	
	private double costModifier = -1;
		
	private AmountResource resource;
	
	private static SimulationConfig simulationConfig = SimulationConfig.instance();
	
    AmountResourceGood(AmountResource ar) {
        super(ar.getName(), ar.getID());
		this.resource = ar;

		// Calculate fixed values
		flattenDemand = calculateFlattenDemand(ar);
		costModifier = calculateCostModifier(ar);
		ingredientDemand = calculateIngredientDemand(ar, simulationConfig.getMealConfiguration());
    }

	/**
	 * Flattens the flatten demand based on selected resource.
	 * 
	 * @param resource
	 * @return
	 */
	private static double calculateFlattenDemand(AmountResource ar) {

		// WARNING: do NOT miss adding the break keyword at the end of each case
		// or else the flatten amount would be invalid
		
		double mod = 1;
		switch(ar.getGoodType()) {
		
		case ANIMAL:
			mod = ANIMAL_FLATTENING_FACTOR;
			
			break;	

		case CHEMICAL:
			mod = CHEMICAL_FLATTENING_FACTOR;	
			
			break;
			
		case COMPOUND:
			mod = COMPOUND_FLATTENING_FACTOR;
	
			mod *= switch(ar.getID()) {
				case ResourceUtil.ACETYLENE_ID -> ACETYLENE_FLATTENING_FACTOR;
				case ResourceUtil.SAND_ID -> SAND_FLATTENING_FACTOR;
				case ResourceUtil.ICE_ID -> ICE_FLATTENING_FACTOR;
				case ResourceUtil.CO_ID -> CO_FLATTENING_FACTOR;
				case ResourceUtil.CO2_ID -> CO2_FLATTENING_FACTOR;
				case ResourceUtil.METHANE_ID -> METHANE_FLATTENING_FACTOR;
				case ResourceUtil.METHANOL_ID -> METHANOL_FLATTENING_FACTOR;
				case ResourceUtil.WATER_ID -> WATER_FLATTENING_FACTOR;
				default -> 1D;
			};
			
			// Special case for NACO3
			if (ar.getName().equalsIgnoreCase(NACO3)) 
				mod *= NACO3_FLATTENING_FACTOR;
			
			break;
	
		case CROP:
			mod = CROP_FLATTENING_FACTOR;
			
			break;
					
		case DERIVED:
			mod = DERIVED_FLATTENING_FACTOR;
			
			break;
					
		case ELEMENT:
			mod = ELEMENT_FLATTENING_FACTOR;
			
			if (ar.getID() == ResourceUtil.HYDROGEN_ID)
				mod *= HYDROGEN_FLATTENING_FACTOR;
			
			else if (ar.getID() == ResourceUtil.OXYGEN_ID)
				mod *= OXYGEN_FLATTENING_FACTOR;
					
			if (ar.getName().equalsIgnoreCase(IRON_POWDER))
				mod *= IRON_POWDER_FLATTENING_FACTOR;
			
			break;
			
		case GEMSTONE:
			mod = GEMSTONE_FLATTENING_FACTOR;
			
			break;
					
		case INSECT:
			mod = INSECT_FLATTENING_FACTOR;
			
			break;
			
		case INSTRUMENT:
			mod = INSTRUMENT_FLATTENING_FACTOR;

			break;
			
		case MINERAL:
			mod = MINERAL_FLATTENING_FACTOR;
			
			if (ar.getID() == ResourceUtil.OLIVINE_ID)
				mod *= OLIVINE_FLATTENING_FACTOR;
			else if (ar.getID() == ResourceUtil.KAMACITE_ID)
				mod *= KAMACITE_FLATTENING_FACTOR;
			
			break;
			
		case ORE:
			mod = ORE_FLATTENING_FACTOR;
			
			break;

		case ORGANISM:
			mod = ORGANISM_FLATTENING_FACTOR;
			
			break;

		case REGOLITH:
			mod = REGOLITH_FLATTENING_FACTOR;
			
			break;
				
		case ROCK:
			mod = ROCK_FLATTENING_FACTOR;
			
			break;
			
		case SOY_BASED:
			mod = SOYBASED_FLATTENING_FACTOR;
			
			break;
			
		case TISSUE:
			mod = TISSUE_FLATTENING_FACTOR;
			
			break;
	
		case UTILITY:
			mod = UTILITY_FLATTENING_FACTOR;
			
			break;
	
		case WASTE:
			mod = WASTE_FLATTENING_FACTOR;
			
			break;

		default:
			break;
		}

		return mod;
	}

    /**
     * Gets the flattened demand of this amount resource.
     * 
     * @return
     */
	@Override
    public double getFlattenDemand() {
    	return flattenDemand;
    }
	
    /**
     * Gets the projected demand of this resource.
     * 
     * @return
     */
	@Override
    public double getProjectedDemand() {
    	return projectedDemand;
    }
	
    /**
     * Gets the trade demand of this resource.
     * 
     * @return
     */
	@Override
    public double getTradeDemand() {
    	return tradeDemand;
    }
    
    @Override
    public GoodCategory getCategory() {
        return GoodCategory.AMOUNT_RESOURCE;
    }

    @Override
    public double getMassPerItem() {
        return 1D;
    }

    @Override
    public GoodType getGoodType() {
        return resource.getGoodType();
    }

    @Override
    protected double computeCostModifier() {
		return costModifier;
	}

	/**
	 * Calculates the cost modifier from a resource.
	 * 
     * @param ar
     * @return
     */
	private static double calculateCostModifier(AmountResource ar) {
        boolean edible = ar.isEdible();
        boolean lifeSupport = ar.isLifeSupport();
        GoodType type = ar.getGoodType();
        double result = 0D;

        if (lifeSupport)
            result += LIFE_SUPPORT_COST;
        
        else if (edible) {
            result += switch (type) {
				case GoodType.DERIVED -> DERIVED_COST;
				case GoodType.SOY_BASED -> SOY_COST;
				case GoodType.ANIMAL -> ANIMAL_COST;
				case GoodType.ORGANISM -> ORGANISM_COST;
				default -> FOOD_COST;
			};
        }
        else {
			result += switch (type) {
					case GoodType.CROP -> CROP_COST;
					case GoodType.WASTE -> WASTE_COST;
					case GoodType.MEDICAL -> MEDICAL_COST;
					case GoodType.OIL -> OIL_COST;
					case GoodType.ROCK -> ROCK_COST;
					case GoodType.REGOLITH -> REGOLITH_COST;
					case GoodType.ORE -> ORE_COST;
					case GoodType.MINERAL -> MINERAL_COST;
					case GoodType.ELEMENT -> ELEMENT_COST;
					case GoodType.CHEMICAL -> CHEMICAL_COST;
					default -> switch(ar.getID()) {
								case ResourceUtil.METHANE_ID -> CH4_COST;
								case ResourceUtil.METHANOL_ID -> METHANOL_COST;
								case ResourceUtil.HYDROGEN_ID -> H2_COST;
								case ResourceUtil.CHLORINE_ID -> CL_COST;
								case ResourceUtil.CO2_ID -> CO2_COST;
								case ResourceUtil.CO_ID -> CO_COST;
								case ResourceUtil.ICE_ID -> ICE_COST;
								default -> 0D;
						};
				};
		}

        return result;
    }

    /**
	 * Gets the amount of the good being produced at the settlement by ongoing food
	 * production.
	 *
	 * @param settlement the good.
	 * @return amount (kg for amount resources, number for parts, equipment, and
	 *         vehicles).
	 */
	private double getFoodProductionOutput(Settlement settlement) {
		double result = 0D;

		// Get the amount of the resource that will be produced by ongoing food
		// production processes.
		for (Building b : settlement.getBuildingManager().getBuildingSet(FunctionType.FOOD_PRODUCTION)) {
			// Go through each ongoing food production process.
			for(FoodProductionProcess process : b.getFoodProduction().getProcesses()) {
				for(ProcessItem item : process.getInfo().getOutputList()) {
					if (item.getName().equalsIgnoreCase(getName())) {
						result += item.getAmount();
					}
				}
			}
		}

		return result;
	}


    @Override
    public double getNumberForSettlement(Settlement settlement) {
        double amount = 0D;

		// Get amount of resource in settlement storage.
		amount += settlement.getSpecificAmountResourceStored(getID());
        
        // Get amount of resource out on mission vehicles.
        amount += getVehiclesOnMissions(settlement)
                        .map(v -> v.getSpecificAmountResourceStored(getID()))
                        .collect(Collectors.summingDouble(f -> f));

		// Get amount of resource carried by people on EVA.
		amount += getPersonOnEVA(settlement)
                    .map(p -> p.getSpecificAmountResourceStored(getID()))
                    .collect(Collectors.summingDouble(f -> f));

		// Get the amount of the resource that will be produced by ongoing manufacturing
		// processes.
		amount += getManufacturingProcessOutput(settlement);

		// Get the amount of the resource that will be produced by ongoing food
		// production processes.
		amount += getFoodProductionOutput(settlement);

		return amount;
    }

    @Override
    double calculatePrice(Settlement settlement, double value) {
		double totalMass = Math.round(settlement.getSpecificAmountResourceStored(getID()) * 100.0)/100.0;
		double factor = 1.5 / (.5 + Math.log(totalMass + 1));
	    return getCostOutput() * (1 + 2 * factor * Math.log(value + 1));
    }

    @Override
    double getDefaultDemandValue() {
        return INITIAL_AMOUNT_DEMAND;
    }

    @Override
    double getDefaultSupplyValue() {
        return INITIAL_AMOUNT_SUPPLY;
    }

    @Override
    void refreshSupplyDemandScore(GoodsManager owner) {
        int id = getID();
        
		double previousDemand = owner.getDemandScore(this);

        Settlement settlement = owner.getSettlement();
		// Note: The population should only minimally impact the demand value
		// pop should never be linearly proportional to demand
        popFactor = Math.log(Math.sqrt(settlement.getNumCitizens())) * 5;
        
		double totalDemand = 0;
		double totalSupply = 0;	

		// Calculate projected demand
		double newDemand = 
			// Tune ice demand.
			computeIceProjectedDemand(owner)
			// Tune regolith projected demand.
			+ computeRegolithProjectedDemand(owner)
			// Tune life support demand if applicable.
			+ getLifeSupportDemand(owner, settlement)
			// Tune potable water usage demand if applicable.
			+ getPotableWaterUsageDemand(owner, settlement)
			// Tune toiletry usage demand if applicable.
			+ getToiletryUsageDemand(settlement)
			// Tune vehicle demand if applicable.
			+ getVehicleFuelDemand(owner, settlement)
			// Tune farming demand.
			+ getFarmingDemand(owner, settlement)
			// Tune the tissue demand due to its crop
			+ computeTissueDemandDueToCrop(owner)
			// Tune resource processing demand.
			+ getResourceProcessingDemand(owner, settlement)
			// Tune manufacturing demand.
			+ getResourceManufacturingDemand(owner, settlement)
			// Tune food production related demand.
			+ getResourceFoodProductionDemand(owner, settlement)
			// Tune demand for the ingredients in all meals.
			+ getAvailableMealDemand(settlement)
			// Tune construction demand.
			+ getResourceConstructionDemand(settlement)
			// Tune construction site demand.
			+ getResourceConstructionSiteDemand(settlement)
			// Adjust the demand on minerals and ores.
			+ getMineralDemand(owner, settlement);

		newDemand = Math.min(HIGHEST_PROJECTED_VALUE, newDemand);
	
		this.projectedDemand = newDemand;
		
		double projected = newDemand
			// Flatten certain types of demand.
			* flattenDemand
			// Adjust the demand on various waste products with the disposal cost.
			* modifyWasteResource();
		
		// Add trade value. Cache is always false if this method is called
		this.tradeDemand = owner.determineTradeDemand(this);
		
		if (previousDemand == 0) {
			// At the start of the sim
			totalDemand = (
					.5 * projected 
					+ .5 * tradeDemand);

		}
		else {
			// Intentionally loses a tiny percentage (e.g. 0.0008) of its value
			// in order to counter the tendency for all goods to increase 
			// in value over time. 
			
			// Warning: a lot of Goods could easily will hit 10,000 demand
			// if not careful.
			
			// Allows only very small fluctuations of demand as possible

			totalDemand = (
					  .9986 * previousDemand 
					+ .00012 * projected 
					+ .00005 * tradeDemand); 
		}
		
		// Save the goods demand
		owner.setDemandScore(this, totalDemand);
		
		// Calculate total supply
		totalSupply = getAverageAmountSupply(settlement.getSpecificAmountResourceStored(id));

		// Store the average supply
		owner.setSupplyValue(this, totalSupply);
    }

    
    /**
	 * Gets the total supply for the amount resource.
	 *
	 * @param resource`
	 * @param supplyStored
	 * @param solElapsed
	 * @return
	 */
	private static double getAverageAmountSupply(double supplyStored) {
		return Math.sqrt(1 + supplyStored);
	}


	/**
	 * Gets the demand for a resource from all automated resource processes at a
	 * settlement.
	 *
	 * @param resource the amount resource.
	 * @return demand (kg)
	 */
	private double getResourceProcessingDemand(GoodsManager owner, Settlement settlement) {
		double demand = 0D;

		// Get all resource processes at settlement.
		for(ResourceProcess i : getResourceProcesses(settlement)) {
			double processDemand = getResourceProcessDemand(owner, i);
			demand += processDemand;
		}

		return Math.min(MAX_RESOURCE_PROCESSING_DEMAND, demand / 20);
	}

	/**
	 * Gets the demand for a resource from an automated resource process.
	 *
	 * @param process  the resource process.
	 * @param resource the amount resource.
	 * @return demand (kg)
	 */
	private double getResourceProcessDemand(GoodsManager owner, ResourceProcess process) {

		int resourceID = getID();

		Set<Integer> inputResources = process.getInputResources();
		Set<Integer> outputResources = process.getOutputResources();

		if (inputResources.contains(resourceID) && !process.isAmbientInputResource(resourceID)) {
			double outputValue = 0D;
			for (Integer output : outputResources) {
				double singleOutputRate = process.getBaseSingleOutputRate(output);
				if (!process.isWasteOutputResource(resourceID)) {
					outputValue += (owner.getDemandScore(GoodsUtil.getGood(output)) 
							* singleOutputRate);
				}
			}

			double singleInputRate = process.getBaseSingleInputRate(resourceID);

			return outputValue / singleInputRate / 3; 
		}

		return 0;
	}

	/**
	 * Gets all resource processes at settlement.
	 *
	 * @return list of resource processes.
	 */
	private static List<ResourceProcess> getResourceProcesses(Settlement settlement) {
		List<ResourceProcess> processes = new ArrayList<>();
		for(Building building : settlement.getBuildingManager().getBuildingSet()) {
			if (building.hasFunction(FunctionType.RESOURCE_PROCESSING)) {
				processes.addAll(building.getResourceProcessing().getProcesses());
			}
		}
		return processes;
	}

	/**
	 * Gets the demand for an amount resource as an input in the settlement's
	 * manufacturing processes.
	 *
	 * @return demand (kg)
	 */
	private double getResourceManufacturingDemand(GoodsManager owner, Settlement settlement) {
		double demand = 0D;

		// Get highest manufacturing tech level in settlement.
		int techLevel = ManufactureUtil.getHighestManufacturingTechLevel(settlement);
		if (techLevel >= 0) {
			for (ManufactureProcessInfo i : ManufactureUtil.getManufactureProcessesForTechLevel(techLevel)) {
				double manufacturingDemand = getResourceManufacturingProcessDemand(owner, settlement, i);
				demand += manufacturingDemand / 1000D;
			}
		}

		return Math.min(MAX_MANUFACTURING_DEMAND, demand / 200);
	}

	/**
	 * Gets the demand for an amount resource as an input in the settlement's Food
	 * Production processes.
	 *
	 * @return demand (kg)
	 */
	private double getResourceFoodProductionDemand(GoodsManager owner, Settlement settlement) {
		double demand = 0D;

		// Get highest Food Production tech level in settlement.
		if (FoodProductionUtil.doesSettlementHaveFoodProduction(settlement)) {
			int techLevel = FoodProductionUtil.getHighestFoodProductionTechLevel(settlement);
			for(FoodProductionProcessInfo i : FoodProductionUtil.getProcessesForTechSkillLevel(techLevel)) {
				double foodProductionDemand = getResourceFoodProductionProcessDemand(owner, settlement, i);
				demand += foodProductionDemand;
			}
		}

		return Math.min(MAX_FOOD_PRODUCTION_DEMAND, demand / 2);
	}

	/**
	 * Gets the demand for an input amount resource in a manufacturing process.
	 *
	 * @param process  the manufacturing process.
	 * @return demand (kg)
	 */
	private double getResourceManufacturingProcessDemand(GoodsManager owner, Settlement settlement, ManufactureProcessInfo process) {
		double demand = 0D;
		String r = resource.getName().toLowerCase();

		ProcessItem resourceInput = null;
		for(var item : process.getInputList()) {
			if ((ItemType.AMOUNT_RESOURCE == item.getType()) && r.equals(item.getName())) {
				resourceInput = item;
				break;
			}
		}

		if (resourceInput != null) {
			double outputsValue = process.getOutputList().stream()
					.mapToDouble(j -> ManufactureUtil.getManufactureProcessItemValue(j, settlement, true))
					.sum();

			double totalItems = process.getInputList().stream()
					.mapToDouble(k -> k.getAmount())
					.sum();

			// Determine value of required process power.
			double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsTime.HOURS_PER_MILLISOL;
			double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();

			// Obtain the value of this resource
			double totalInputsValue = (outputsValue - powerValue);

			CommerceType cType = toCommerceType(resource, settlement.getObjective());		
			if (cType != null) {
				totalInputsValue *= owner.getCommerceFactor(cType);
			}
			// Modify by other factors
			totalInputsValue *= MANUFACTURING_INPUT_FACTOR;

			if (totalItems > 0) {
				demand = (1D / totalItems) * totalInputsValue;
			}
		}

		return demand;
	}

	/**
	 * Converts an amount resource in the context of an Objective to a commerce type.
	 * @param resource
	 * @param objective
	 * @return
	 */
	private static CommerceType toCommerceType(AmountResource resource, ObjectiveType objective) {
		GoodType type = resource.getGoodType();
		
		switch(objective) {
			case BUILDERS_HAVEN: { 
				if (GoodType.REGOLITH == type
				|| GoodType.MINERAL == type
				|| GoodType.ORE == type
				|| GoodType.UTILITY == type) {
					return CommerceType.BUILDING;
				}
			} break;

			case CROP_FARM: {
				if (GoodType.CROP == type
				|| GoodType.DERIVED == type
				|| GoodType.SOY_BASED == type) {
					return CommerceType.CROP;
				}
			} break;

			case MANUFACTURING_DEPOT: 
				return CommerceType.MANUFACTURING;

			case RESEARCH_CAMPUS: { 
				if (GoodType.MEDICAL == type
				|| GoodType.ORGANISM == type
				|| GoodType.CHEMICAL == type
				|| GoodType.ROCK == type) {
					return CommerceType.RESEARCH;
				}
			} break;

			case TRADE_CENTER:
				return CommerceType.TRADE;

			case TRANSPORTATION_HUB: {
				if (resource.getID() == ResourceUtil.METHANOL_ID) {
					return CommerceType.TRANSPORT;
				}
			} break;

			default:
				break;
		}
		return null;
	}

	/**
	 * Gets the demand for an input amount resource in a Food Production process.
	 *
	 * @param process  the Food Production process.
	 * @return demand (kg)
	 */
	private double getResourceFoodProductionProcessDemand(GoodsManager owner, Settlement settlement, FoodProductionProcessInfo process) {
		double demand = 0D;
		String name = resource.getName();

		ProcessItem resourceInput = null;
		for(ProcessItem i : process.getInputList()) {
			if ((ItemType.AMOUNT_RESOURCE == i.getType())
					&& name.equalsIgnoreCase(i.getName())) {
				resourceInput = i;
				break;
			}
		}

		if (resourceInput != null) {
			double outputsValue = 0D;
			for(ProcessItem j : process.getOutputList()) {
				outputsValue += FoodProductionUtil.getProcessItemValue(j, settlement, true);
			}

			double totalItems = 0D;
			for(ProcessItem k : process.getInputList()) {
				totalItems += k.getAmount();
			}

			// Determine value of required process power.
			double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsTime.HOURS_PER_MILLISOL;
			double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();

			double totalInputsValue = (outputsValue - powerValue) * owner.getCommerceFactor(CommerceType.TRADE)
											* owner.getCommerceFactor(CommerceType.CROP)
											* FOOD_PRODUCTION_INPUT_FACTOR;

			if (totalItems > 0D) {
				demand = (1D / totalItems) * totalInputsValue;
			}
		}

		return demand;
	}

	/**
	 * Gets the resource demand for all available meals.
	 *
	 * @param resource the amount resource.
	 * @return demand (kg)
	 */
	private double getAvailableMealDemand(Settlement settlement) {

		if (!resource.isEdible())
			return 0;
		
		double demand = 0D;
		int id = getID();
		
		if (id == ResourceUtil.TABLE_SALT_ID) {
			// Assuming a person takes 3 meals per sol
			return MarsTime.AVERAGE_SOLS_PER_ORBIT_NON_LEAPYEAR * 3 * Cooking.AMOUNT_OF_SALT_PER_MEAL; 
		}

		else {
			if (ResourceUtil.getOilResources().contains(id)) {
				// Assuming a person takes 3 meals per sol
				return MarsTime.SOLS_PER_ORBIT_NON_LEAPYEAR * 3 * Cooking.AMOUNT_OF_OIL_PER_MEAL;
			}

			// Determine total demand for cooked meal mass for the settlement.
			double cookedMealDemandSol = personConfig.getFoodConsumptionRate();
			double cookedMealDemandOrbit = cookedMealDemandSol * MarsTime.SOLS_PER_ORBIT_NON_LEAPYEAR;
			double cookedMealDemand = popFactor * cookedMealDemandOrbit;
			var meals = simulationConfig.getMealConfiguration().getDishList();
			int numMeals = meals.size();
			double factor = cookedMealDemand / numMeals * COOKED_MEAL_INPUT_FACTOR;
			
			// Determine demand for the resource as an ingredient for each cooked meal
			// recipe.
			demand += ingredientDemand * factor;

		}

		return demand;
	}

	/**
	 * Get the base demand for this resource as an ingredient in all meals.
	 * @param mConfig
	 * @return
	 */
	private static double calculateIngredientDemand(AmountResource ar, MealConfig mConfig) {
		
		if (!ar.isEdible())
			return 0;
		
		int id = ar.getID();
		double demand = 0D;
			for (DishRecipe meal : mConfig.getDishList()) {
				demand += meal.getIngredientList().stream()
					.filter(ingredient -> id == ingredient.getAmountResourceID())
					.mapToDouble(i -> i.getProportion())
					.sum();
			}
		return demand;
	}

	/**
	 * Gets the demand for a resource from construction sites.
	 *
	 * @return demand (kg)
	 */
	private double getResourceConstructionSiteDemand(Settlement settlement) {
		double base = 0D;
		int resourceID = getID();

		// Note: Need to filter the construction resources first here
		
		// Add demand for resource required as remaining construction material on
		// construction sites.
		for (ConstructionSite site : settlement.getConstructionManager().getConstructionSites()) {
			if (site.hasUnfinishedStage() && !site.getCurrentConstructionStage().isSalvaging()) {
				ConstructionStage stage = site.getCurrentConstructionStage();
				if (stage.getMissingResources().containsKey(resourceID)) {
					double requiredAmount = stage.getMissingResources().get(resourceID);
					base += requiredAmount * CONSTRUCTION_SITE_REQUIRED_RESOURCE_FACTOR;
				}
			}
		}

		return Math.min(GoodsManager.MAX_DEMAND, base / 100);
	}

	/**
	 * Gets the demand for an amount resource as an input in building construction.
	 *
	 * @return demand (kg)
	 */
	private double getResourceConstructionDemand(Settlement settlement) {
		double base = 0D;

		ConstructionValues values = settlement.getConstructionManager().getConstructionValues();
		int bestConstructionSkill = ConstructionUtil.getBestConstructionSkillAtSettlement(settlement);
		for(Entry<ConstructionStageInfo, Double> stageDetail : values.getAllConstructionStageValues(bestConstructionSkill).entrySet()) {
			ConstructionStageInfo stage = stageDetail.getKey();
			double stageValue = stageDetail.getValue();
			if (stageValue > 0D && ConstructionStageInfo.Stage.BUILDING.equals(stage.getType())
					&& isLocallyConstructable(settlement, stage)) {
				double constructionDemand = getResourceConstructionStageDemand(stage, stageValue);
				if (constructionDemand > 0D) {
					base += constructionDemand;
				}
			}
		}

		return Math.min(GoodsManager.MAX_DEMAND, base / 100);
	}

	/**
	 * Gets the demand for an amount resources as an input for a particular building
	 * construction stage.
	 *
	 * @param stage      the building construction stage.
	 * @param stageValue the building construction stage value (VP).
	 * @return demand (kg)
	 */
	private double getResourceConstructionStageDemand(ConstructionStageInfo stage,
			double stageValue) {

		double demand = 0D;
		double resourceAmount = getPrerequisiteConstructionResourceAmount(stage);

		if (resourceAmount > 0D) {
			Map<Integer, Double> resources = getAllPrerequisiteConstructionResources(stage);
			Map<Integer, Integer> parts = getAllPrerequisiteConstructionParts(stage);

			double totalItems = 0D;

			Iterator<Integer> i = resources.keySet().iterator();
			while (i.hasNext()) {
				totalItems += resources.get(i.next());
			}

			Iterator<Integer> j = parts.keySet().iterator();
			while (j.hasNext()) {
				totalItems += parts.get(j.next());
			}

			if (totalItems > 0 ) {
				double totalInputsValue = stageValue * CONSTRUCTING_INPUT_FACTOR;
				demand = (1D / totalItems) * totalInputsValue;
			}
		}

		return demand;
	}

	/**
	 * Gets the total amount of a given resource required to build a stage.
	 *
	 * @param resource the resource.
	 * @param stage    the stage.
	 * @return total amount (kg) of the resource.
	 */
	private double getPrerequisiteConstructionResourceAmount(ConstructionStageInfo stage) {

		int resourceID = getID();

		// Add resource amount needed for stage.
		double result = stage.getResources().getOrDefault(resourceID, 0D);

		// Add resource amount needed for first prestage, if any.
		ConstructionStageInfo preStage1 = stage.getPrerequisiteStage();
		if ((preStage1 != null) && preStage1.isConstructable()) {
			result += preStage1.getResources().getOrDefault(resourceID, 0D);

			// Add resource amount needed for second prestage, if any.
			ConstructionStageInfo preStage2 = preStage1.getPrerequisiteStage();
			if ((preStage2 != null) && preStage2.isConstructable()) {
				result += preStage2.getResources().getOrDefault(resourceID, 0D);
			}
		}

		return result;
	}


	/**
	 * Gets the farming demand for the resource.
	 *
	 * @return demand (kg) for the resource.
	 */
	private double getFarmingDemand(GoodsManager owner, Settlement settlement) {
		double demand = 0D;

		// Determine demand for resource at each farming building at settlement.
		for(Building b : settlement.getBuildingManager().getBuildingSet(FunctionType.FARMING)) {
			Farming farm = b.getFarming();
			demand += getFarmingResourceDemand(farm) * owner.getCommerceFactor(CommerceType.CROP) * FARMING_FACTOR;
		}

		return demand;
	}

	/**
	 * Gets the individual greenhouse resource demand
	 *
	 * @param farm
	 * @return
	 */
	private double getFarmingResourceDemand(Farming farm) {
		
		double averageGrowingCyclesPerOrbit = farm.getAverageGrowingCyclesPerOrbit();
		double totalCropArea = farm.getGrowingArea();
		int solsInOrbit = MarsTime.SOLS_PER_ORBIT_NON_LEAPYEAR;
		double factor = totalCropArea * averageGrowingCyclesPerOrbit / solsInOrbit;

		return switch(getID()) {
			// Average water consumption rate of crops per orbit using total growing area.
			case ResourceUtil.WATER_ID -> cropConfig.getWaterConsumptionRate() * factor;
			// Average co2 consumption rate of crops per orbit using total growing area.
			case ResourceUtil.CO2_ID -> cropConfig.getCarbonDioxideConsumptionRate() * factor * CO2_VALUE_MODIFIER;
			// Average oxygen consumption rate of crops per orbit using total growing area.
			case ResourceUtil.OXYGEN_ID -> cropConfig.getOxygenConsumptionRate() * factor * OXYGEN_VALUE_MODIFIER;
			// Estimate soil needed for average number of crop plantings for total growing area
			case ResourceUtil.SOIL_ID -> Crop.NEW_SOIL_NEEDED_PER_SQM * factor;
			// Estimate fertilizer needed for average number of crop plantings for total growing area.
			// Estimate fertilizer needed when grey water not available.
			case ResourceUtil.FERTILIZER_ID -> (Crop.FERTILIZER_NEEDED_IN_SOIL_PER_SQM * Crop.FERTILIZER_NEEDED_WATERING * solsInOrbit) * factor * 5;
			// NOTE: how to properly get rid of grey water? it should NOT be considered an
			// economically vital resource
			// Average grey water consumption rate of crops per orbit using total growing
			// area.
			case ResourceUtil.GREY_WATER_ID -> WASTE_WATER_VALUE_MODIFIER;

			default -> 0D;
		};
	}

	/**
	 * Computes the tissue demand.
	 * 
	 * @param owner
	 * @return
	 */
	private double computeTissueDemandDueToCrop(GoodsManager owner) {

		AmountResource ar = resource;
		if (ar.getGoodType() == GoodType.TISSUE) {		
			String cropName = ar.getName().replace(" tissue", "");
			// Convert the tissue name to its crop's name
			return owner.getDemandScore(GoodsUtil.getGood(cropName)) * TISSUE_CULTURE_VALUE;
		}
		else
			return 0;
	}
	
	/**
	 * Gets the life support demand for an amount resource.
	 *
	 * @param resource the resource to check.
	 * @return demand (kg)
	 */
	private double getLifeSupportDemand(GoodsManager owner, Settlement settlement) {
		int resourceID = resource.getID();
		if (resource.isLifeSupport()) {
			double amountNeededSol = switch(resourceID) {
				case ResourceUtil.OXYGEN_ID -> personConfig.getNominalO2ConsumptionRate() * OXYGEN_VALUE_MODIFIER;
				case ResourceUtil.WATER_ID -> personConfig.getWaterConsumptionRate() *  WATER_VALUE_MODIFIER;
				case ResourceUtil.FOOD_ID -> personConfig.getFoodConsumptionRate() * FOOD_VALUE_MODIFIER;
				case ResourceUtil.METHANE_ID -> personConfig.getWaterConsumptionRate() * METHANE_VALUE_MODIFIER;
				case ResourceUtil.CO2_ID -> CO2_VALUE_MODIFIER;
				case ResourceUtil.HYDROGEN_ID -> personConfig.getWaterConsumptionRate() * HYDROGEN_VALUE_MODIFIER;
				default -> 0D;
			};
			
			return popFactor * amountNeededSol * owner.getCommerceFactor(CommerceType.TRADE)  
					* LIFE_SUPPORT_FACTOR;
		}
		else
			return 0;
	}

	/**
	 * Gets mineral demand.
	 *
	 * @return
	 */
	private double getMineralDemand(GoodsManager owner, Settlement settlement) {
		double base = .25;
		int resourceID = getID();
		switch(resourceID) {
        	case ResourceUtil.ROCK_SALT_ID:
				return base * ROCK_SALT_VALUE_MODIFIER;
			case ResourceUtil.EPSOM_SALT_ID:
				return base * EPSOM_SALT_VALUE_MODIFIER;
			case ResourceUtil.SOIL_ID:
				return base * settlement.getTotalCropArea() * SOIL_VALUE_MODIFIER;
			case ResourceUtil.CEMENT_ID: {
				double cementDemand = owner.getDemandScoreWithID(ResourceUtil.CEMENT_ID);
				double concreteDemand = owner.getDemandScoreWithID(ResourceUtil.CONCRETE_ID);
				double regolithDemand = owner.getDemandScoreWithID(ResourceUtil.REGOLITH_ID);
				double sandDemand = owner.getDemandScoreWithID(ResourceUtil.SAND_ID);
				return base * (.5 * cementDemand + .2 * regolithDemand + .2 * sandDemand + .1 * concreteDemand) 
						/ (1 + cementDemand) * CEMENT_VALUE_MODIFIER;
			}
			case ResourceUtil.CONCRETE_ID:{
				double concreteDemand = owner.getDemandScoreWithID(ResourceUtil.CONCRETE_ID);
				double regolithDemand = owner.getDemandScoreWithID(ResourceUtil.REGOLITH_ID);
				double sandDemand = owner.getDemandScoreWithID(ResourceUtil.SAND_ID);
				// the demand for sand is dragged up or down by that of regolith
				// loses 5% by default
				return base * (.5 * concreteDemand + .55 * regolithDemand + .25 * sandDemand) 
							/ (1 + concreteDemand) * CONCRETE_VALUE_MODIFIER;
			}
			case ResourceUtil.SAND_ID: {
				double regolithDemand = owner.getDemandScoreWithID(ResourceUtil.REGOLITH_ID);
				double sandDemand = owner.getDemandScoreWithID(ResourceUtil.SAND_ID);
				// the demand for sand is dragged up or down by that of regolith
				// loses 10% by default
				return base * (.2 * regolithDemand + .7 * sandDemand) 
							/ (1 + sandDemand) * SAND_VALUE_MODIFIER;
			}
		}
        
		double regolithDemand = owner.getDemandScoreWithID(ResourceUtil.REGOLITH_ID);
		double sandDemand = owner.getDemandScoreWithID(ResourceUtil.SAND_ID);

		for (int id : ResourceUtil.ROCK_IDS) {
			if (resourceID == id) {
				double rockDemand = owner.getDemandScoreWithID(id);
				return base * (.2 * regolithDemand + .9 * rockDemand) 
						/ (1 + rockDemand) * ROCK_VALUE_MODIFIER;
			}
		}

		for (int id : ResourceUtil.MINERAL_CONC_IDs) {
			if (resourceID == id) {
				double mineralDemand = owner.getDemandScoreWithID(id);
				return base * (.2 * regolithDemand + .9 * mineralDemand) 
						/ (1 + mineralDemand) * MINERAL_VALUE_MODIFIER;
			}
		}

		for (int id : ResourceUtil.ORE_DEPOSIT_IDS) {
			if (resourceID == id) {
				double oreDemand = owner.getDemandScoreWithID(id);
				// loses 10% by default
				return base * (.3 * regolithDemand + .6 * oreDemand) 
						/ (1 + oreDemand) * ORES_VALUE_MODIFIER;
			}
		}

		if (resourceID == ResourceUtil.REGOLITH_ID) {
			return base * regolithDemand * REGOLITH_VALUE_MODIFIER;
		}
		
		else if (resourceID == ResourceUtil.REGOLITHB_ID 
				|| resourceID == ResourceUtil.REGOLITHC_ID) {
			return base * regolithDemand * REGOLITH_VALUE_MODIFIER_1;
		}
		
		else if (resourceID == ResourceUtil.REGOLITHD_ID) {
			return base * regolithDemand * REGOLITH_VALUE_MODIFIER_2;
		}
		
		// Checks if this resource is a ROCK type
		GoodType type = getGoodType();
		if (type != null && type == GoodType.ROCK) {
			double rockDemand = owner.getDemandScoreWithID(resourceID);

			if (resourceID == ResourceUtil.METEORITE_ID)
				return base * (.4 * regolithDemand + .5 * rockDemand) 
						/ (1 + rockDemand) * METEORITE_VALUE_MODIFIER;
			else
				return base * (.2 * sandDemand + .7 * rockDemand) 
						/ (1 + rockDemand) * ROCK_VALUE_MODIFIER;
		}

		return base;
	}

	/**
	 * Adjusts the demand for waste resources.
	 *
	 * @return demand (kg)
	 */
	private double modifyWasteResource() {
		return switch(getID()) {
			case ResourceUtil.BRINE_WATER_ID -> BRINE_WATER_VALUE_MODIFIER;
			case ResourceUtil.GREY_WATER_ID -> GREY_WATER_VALUE_MODIFIER;
			case ResourceUtil.BLACK_WATER_ID -> BLACK_WATER_VALUE_MODIFIER;
			case ResourceUtil.LEAVES_ID -> LEAVES_VALUE_MODIFIER;
			case ResourceUtil.SOIL_ID -> SOIL_VALUE_MODIFIER;
			case ResourceUtil.FOOD_WASTE_ID -> 4 * USEFUL_WASTE_VALUE_MODIFIER;
			case ResourceUtil.SOLID_WASTE_ID -> .2;
			case ResourceUtil.TOXIC_WASTE_ID -> .05;
			case ResourceUtil.CROP_WASTE_ID -> 4 * USEFUL_WASTE_VALUE_MODIFIER;
			case ResourceUtil.COMPOST_ID -> 2 * USEFUL_WASTE_VALUE_MODIFIER;
			default -> 1D;
		};
	}

	/**
	 * Gets the potable water usage demand for an amount resource.
	 *
	 * @param resource the resource to check.
	 * @return demand (kg)
	 */
	private double getPotableWaterUsageDemand(GoodsManager owner, Settlement settlement) {
		double demand = 0;
		if (getID() == ResourceUtil.WATER_ID) {
			// Add the awareness of the water ration level in adjusting the water demand
			double waterRationLevel = settlement.getWaterRationLevel();
			double amountNeededSol = personConfig.getWaterUsageRate();
			demand = popFactor * amountNeededSol *  WATER_VALUE_MODIFIER 
					* owner.getCommerceFactor(CommerceType.TRADE)  * (1 + waterRationLevel);
		}

		return demand;
	}

	/**
	 * Computes ice projected demand.
	 *
	 * @param owner
	 * @param settlement
	 * @return demand
	 */
	private double computeIceProjectedDemand(GoodsManager owner) {
        int resourceID = getID();
		if (resourceID == ResourceUtil.ICE_ID) {
			double ice = 1 + owner.getDemandScoreWithID(resourceID);
			double water = 1 + owner.getDemandScoreWithID(ResourceUtil.WATER_ID);
			// Use the water's VP and existing iceSupply to compute the ice demand
			return  (.5 * water + .5 * ice) / ice
					* ICE_VALUE_MODIFIER;
		}

		return 0;
	}

	/**
	 * Computes regolith projected demand.
	 *
	 * @param owner
	 * @param settlement
	 * @return demand
	 */
	private double computeRegolithProjectedDemand(GoodsManager owner) {
		double demand = 0;
		int resourceID = getID();
		// This averaging method make all regolith types to be placed at similar demand
		if (resourceID == ResourceUtil.REGOLITHB_ID
			|| resourceID ==  ResourceUtil.REGOLITHC_ID
			|| resourceID ==  ResourceUtil.REGOLITHD_ID) {

			double sand = owner.getDemandScoreWithID(ResourceUtil.SAND_ID);
			double concrete = owner.getDemandScoreWithID(ResourceUtil.CONCRETE_ID);
			double cement = owner.getDemandScoreWithID(ResourceUtil.CEMENT_ID);

			double targetRegolith = owner.getDemandScoreWithID(resourceID);
			double regolith = owner.getDemandScoreWithID(ResourceUtil.REGOLITH_ID);
			double regolithB = owner.getDemandScoreWithID(ResourceUtil.REGOLITHB_ID);
			double regolithC = owner.getDemandScoreWithID(ResourceUtil.REGOLITHC_ID);
			double regolithD = owner.getDemandScoreWithID(ResourceUtil.REGOLITHD_ID);
			
			double averageRegolith = (regolith + regolithB + regolithC + regolithD) / 4.0;
			
			// Limit the minimum value of regolith projected demand
			demand = (cement + concrete + 4 * targetRegolith + 2 * sand + 4 * averageRegolith) 
					* REGOLITH_TYPE_VALUE_MODIFIER;
		}

		return demand;
	}

	/**
	 * Gets the toilet tissue usage demand.
	 *
	 * @param resource the resource to check.
	 * @return demand (kg)
	 */
	private double getToiletryUsageDemand(Settlement settlement) {
		if (getID() == ResourceUtil.TOILET_TISSUE_ID) {
			double amountNeededSol = LivingAccommodation.TOILET_WASTE_PERSON_SOL;
			int numPeople = settlement.getIndoorPeopleCount();
			int pop = settlement.getNumCitizens(); 
			return numPeople * amountNeededSol / pop * 20;
		}

		return 0;
	}

	/**
	 * Gets vehicle fuel demand for an amount resource.
	 *
	 * @return demand (kg) for the resource.
	 */
	private double getVehicleFuelDemand(GoodsManager owner, Settlement settlement) {
		double demand = 0D;
		double transFactor = owner.getCommerceFactor(CommerceType.TRANSPORT) * VEHICLE_FUEL_FACTOR;
		int pop = settlement.getNumCitizens();

		switch(getID()) {
			case ResourceUtil.METHANOL_ID: {
				for(Vehicle v: settlement.getAllAssociatedVehicles()) {
					double fuelDemand = v.getSpecificCapacity(getID());
					demand += fuelDemand;
				}
				demand = demand * transFactor * METHANOL_VALUE_MODIFIER * 10 / pop;
			} break;
		
			case ResourceUtil.METHANE_ID: {
				for(Vehicle v: settlement.getAllAssociatedVehicles()) {
					double fuelDemand = v.getSpecificCapacity(getID());
					demand += fuelDemand;
				}
				demand = demand * transFactor * METHANE_VALUE_MODIFIER * 2 / pop;
			} break;

			case ResourceUtil.HYDROGEN_ID: {
				demand =  transFactor * HYDROGEN_VALUE_MODIFIER / 10;
			} break;
		}

		return demand;
	}
}
