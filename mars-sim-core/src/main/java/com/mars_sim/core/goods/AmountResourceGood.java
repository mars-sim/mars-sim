/*
 * Mars Simulation Project
 * AmountResourceGood.java
 * @date 2025-10-02
 * @author Barry Evans
 */
package com.mars_sim.core.goods;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.building.construction.ConstructionStageInfo;
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
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.manufacture.ManufactureUtil;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.resourceprocess.ResourceProcess;
import com.mars_sim.core.structure.ObjectiveType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.MathUtils;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * This represents how a Amount Resource can be traded.
 */
class AmountResourceGood extends Good {
	
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(AmountResourceGood.class.getName());
	
	private static final double INITIAL_AMOUNT_DEMAND = 0.1;
	private static final double INITIAL_AMOUNT_SUPPLY = 0;

	private static final double WASTE_WATER_VALUE_MODIFIER = 1.5;
	private static final double GREY_WATER_VALUE_MODIFIER = 1;
	private static final double BLACK_WATER_VALUE_MODIFIER = .5;
	private static final double USEFUL_WASTE_VALUE_MODIFIER = 1.05D;

	// Cost modifiers
	private static final double METHANE_COST = 0.3;
	private static final double METHANOL_COST = 0.4;
	private static final double H2_COST = 1;
	private static final double CO_COST = 0.05;
    private static final double CO2_COST = 0.0000005;
	private static final double CL_COST = 0.25;
	private static final double ICE_COST = 0.5;
	private static final double FOOD_COST = 0.3;
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
	
	// Water related flattening factors
	private static final double ICE_FLATTENING_FACTOR = 1.25;
	private static final double WATER_FLATTENING_FACTOR = 1.5;
	
	// Gases flattening factors
	private static final double METHANOL_FLATTENING_FACTOR = 1.1;
	private static final double METHANE_FLATTENING_FACTOR = 1.5;
	private static final double HYDROGEN_FLATTENING_FACTOR = 0.7;
	private static final double OXYGEN_FLATTENING_FACTOR = 0.7;	
	

	// Note1: must keep Carbon Monoxide demand high enough so as to start 
	// 5 important Resource Processes that produce other resources (alongside with Carbon Monoxide)
	// Note2: high CO demand can be counter productive for manufacturing processes that uses it as input 
	private static final double CO_FLATTENING_FACTOR = 0.5;
	private static final double CO2_FLATTENING_FACTOR = 1.05;
		
	private static final double ORE_FLATTENING_FACTOR = 1.0;
	private static final double MINERAL_FLATTENING_FACTOR = 0.9;
	private static final double ROCK_FLATTENING_FACTOR = 1D;
	private static final double REGOLITH_FLATTENING_FACTOR = 0.75D;
	private static final double SAND_FLATTENING_FACTOR = 1D;
	
	private static final double ROCK_SALT_FLATTENING_FACTOR = 0.2;
	
	private static final int MYLAR_FLATTENING_FACTOR = 3;
	
	// Minerals
	private static final double OLIVINE_FLATTENING_FACTOR = 0.75;
	private static final double KAMACITE_FLATTENING_FACTOR = 0.75;
	
	private static final double SILICON_FLATTENING_FACTOR = .5;
	
	// Chemicals
	private static final double ETHYLENE_FLATTENING_FACTOR = 0.75;
	private static final double ACETYLENE_FLATTENING_FACTOR = 0.5;
	private static final double POLYESTER_FIBER_FLATTENING_FACTOR = 2.0;
	private static final int POLYURETHANE_FLATTENING_FACTOR = 3;
	private static final double NA2CO3_FLATTENING_FACTOR = 2.0;
	
	// Metals
	private static final double IRON_POWDER_FLATTENING_FACTOR = 0.5;
	
	// types
	private static final double CHEMICAL_FLATTENING_FACTOR = 1.1;
	private static final double COMPOUND_FLATTENING_FACTOR = 1.5;
	private static final int CONSTRUCTION_FLATTENING_FACTOR = 1;
	private static final int ELEMENT_FLATTENING_FACTOR = 2;

	private static final int GEMSTONE_FLATTENING_FACTOR = 3;

	private static final double WASTE_FLATTENING_FACTOR = 0.15;
	
	private static final int UTILITY_FLATTENING_FACTOR = 4;

	private static final int INSECT_FLATTENING_FACTOR = 5;
	private static final int ORGANISM_FLATTENING_FACTOR = 2;
	private static final double SOYBASED_FLATTENING_FACTOR = 0.5;
	private static final int ANIMAL_FLATTENING_FACTOR = 2;
	private static final double CROP_FLATTENING_FACTOR = 1.25;
	private static final double DERIVED_FLATTENING_FACTOR = 1.25;
	private static final double TISSUE_FLATTENING_FACTOR = 0.95;

	private static final int PLASTIC_RELATED_FLATTENING_FACTOR = 3;
	private static final double ETHYLENE_GLYCOL_FLATTENING_FACTOR = 1.5;
	
	private static final double LEAVES_VALUE_MODIFIER = 1.5;
	
	// Future: Need to avoid making all regolith types max out at 10k proj demand.
	private static final double REGOLITH_VALUE_MODIFIER = 0.02;
	
	// Demand Modifiers
    private static final double ICE_VALUE_MODIFIER = 1.05;
	private static final double WATER_VALUE_MODIFIER = 1.25;
	private static final double BRINE_WATER_VALUE_MODIFIER  = 0.75;
	
	private static final double SOIL_VALUE_MODIFIER = 0.05;
	private static final double SAND_VALUE_MODIFIER = 2.5;
	private static final double ORES_VALUE_MODIFIER = 0.9;
	
	private static final double MINERAL_VALUE_MODIFIER = 0.9;
	private static final double ROCK_VALUE_MODIFIER = 0.2;
	private static final double METEORITE_VALUE_MODIFIER = 100;
	
	private static final double CONCRETE_VALUE_MODIFIER = 1.0;
	private static final double CEMENT_VALUE_MODIFIER = 0.45;
	
	private static final double ROCK_SALT_VALUE_MODIFIER = 1.5;
	private static final double EPSOM_SALT_VALUE_MODIFIER = .05;
	
	private static final double FOOD_VALUE_MODIFIER = 1.2;
	
	private static final double OXYGEN_VALUE_MODIFIER = 3.5;
	private static final double METHANE_VALUE_MODIFIER = 2.5;
	private static final double HYDROGEN_VALUE_MODIFIER = 1.4;
	private static final double METHANOL_VALUE_MODIFIER = 0.9;
	
	// Chemicals
	private static final int CLEANING_AGENT_MODIFIER = 1;
	private static final int ETHYLENE_MODIFIER = 10;
	private static final int POLYETHYLENE_MODIFIER = 1;
	private static final int STYRENE_MODIFIER = 7;
	private static final int PROPYLENE_MODIFIER = 3;
	private static final int ETHANE_MODIFIER = 2;
	private static final int H2O2_MODIFIER = 4;
	private static final int EPOXY_RESIN_MODIFIER = 10;
	private static final int POLYCARBONATE_RESIN_MODIFIER = 10;
	private static final int POLYESTER_RESIN_MODIFIER = 3;
	private static final int ACETYLENE_MODIFIER = 3;
	private static final int FIBER_MODIFIER = 2;
	private static final int LIME_MODIFIER = 6;
	
	// gases
	private static final double CO2_VALUE_MODIFIER = 0.8;

	// metal
	private static final int IRON_OXIDE_MODIFIER = 8;
	private static final double IRON_POWDER_MODIFIER = 1.2;

	// Future: Need to avoid making all tissue culture max out at 10k proj demand.
	private static final double TISSUE_CULTURE_VALUE = 0.05;
	
	private static final double LIFE_SUPPORT_FACTOR = .005;
	private static final double VEHICLE_FUEL_FACTOR = 1;
	private static final double FARMING_FACTOR = .1;
	
	private static final double COOKED_MEAL_INPUT_FACTOR = 0.5;
	private static final double MANUFACTURING_INPUT_FACTOR = 2D;
	private static final double FOOD_PRODUCTION_INPUT_FACTOR = 1.2;
	private static final int CONSTRUCTION_SITE_REQUIRED_RESOURCE_FACTOR = 2;

	private static final int MAX_RESOURCE_PROCESSING_DEMAND = 500; 
	private static final int MAX_MANUFACTURING_DEMAND = 500;
	private static final int MAX_FOOD_PRODUCTION_DEMAND = 500;
	
	private static final double REGOLITH_LOWEST_DEMAND = 0.05;
	private static final double REGOLITH_BASE_DEMAND = 40;
	
	// Base factors
	private static final double BASE_MINERAL_ORE = 5;
	private static final double BASE_CHEMICAL_DEMAND = 5;
	private static final double BASE_METAL_DEMAND = 5;

	// Multipliers
	private static final double MANUFACTURING_DEMAND_MULTIPLIER = 0.5;
	
	private static final String DIMETHYL_TER = "Dimethyl terephthalate";
	private static final String SILICONE_E  = "Silicone elastomer";
	private static final String THERMOPLASTIC_EL = "Thermoplastic elastomer";
	
//	private static final String ETHYLENE_GLYCOL = "Ethylene glycol";
	
	/** The fixed flatten demand for this resource. */
	private double flattenDemand;
	/** The ingredient demand of each refresh cycle. */
	private double ingredientDemand;
	/** The constant manufacturing demand for each refresh cycle. */
	private double constantManufacturingInputNeed = -1D;
	/** The constant food production demand for each refresh cycle. */
	private double constantFoodProductionInputNeed = -1D;
	/** The constant resource process demand for each refresh cycle. */
	private double constantResourceProcessInputNeed = -1D;
	/** The constant life support demand for each refresh cycle. */
	private double constantLifeSupportNeed = -1D;
	/** The constant meal ingredient demand for each refresh cycle. */
	private double constantMealIngredientNeed = -1D;
//	/** The constant crop tissue need for each refresh cycle. */
//	private double constantCropTissueNeed = -1D;
	/** The constant farming need for each refresh cycle. */
	private double constantFarmingNeed = -1D;
	
	private double costModifier = -1D;
	
	private AmountResource resource;
	
	private static SimulationConfig simulationConfig = SimulationConfig.instance();
	
    AmountResourceGood(AmountResource ar) {
        super(ar.getName(), ar.getID());
		this.resource = ar;
		
		double multiplier = ar.getDemandMultiplier();

		// Calculate fixed values
		flattenDemand = calculateFlattenDemand(ar) * multiplier;
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
		int id = ar.getID();
		String name = ar.getName();
		switch(ar.getGoodType()) {
		
		case ANIMAL:
			mod = ANIMAL_FLATTENING_FACTOR;
			
			break;	

		case CHEMICAL:
			mod = CHEMICAL_FLATTENING_FACTOR;	
			
			if (name.equalsIgnoreCase(DIMETHYL_TER)
				|| name.equalsIgnoreCase(SILICONE_E)
				|| name.equalsIgnoreCase(THERMOPLASTIC_EL)	
					) {
				mod *= PLASTIC_RELATED_FLATTENING_FACTOR;
			}
			else if (id == ResourceUtil.ETHYLENE_GLYCOL_ID) {
				mod *= ETHYLENE_GLYCOL_FLATTENING_FACTOR;
			}
			
			mod *= switch(id) {
				case ResourceUtil.ETHYLENE_ID ->  ETHYLENE_FLATTENING_FACTOR;
				case ResourceUtil.ROCK_SALT_ID -> ROCK_SALT_FLATTENING_FACTOR;
				case ResourceUtil.POLYESTER_FIBER_ID -> POLYESTER_FIBER_FLATTENING_FACTOR;
				case ResourceUtil.POLYURETHANE_ID -> POLYURETHANE_FLATTENING_FACTOR;
				case ResourceUtil.TEREPHTHALIC_ACID_ID -> PLASTIC_RELATED_FLATTENING_FACTOR;
				default -> 1D;
			};
	
			break;
			
		case COMPOUND:
			mod = COMPOUND_FLATTENING_FACTOR;
	
			mod *= switch(id) {
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
			
			// Special case for NA2CO3
			if (id == ResourceUtil.NA2CO3_ID) 
				mod *= NA2CO3_FLATTENING_FACTOR;
			
			break;
	
		case CONSTRUCTION:
			mod = CONSTRUCTION_FLATTENING_FACTOR;
			
			break;
			
		case CROP:
			mod = CROP_FLATTENING_FACTOR;
			
			break;
					
		case DERIVED:
			mod = DERIVED_FLATTENING_FACTOR;
			
			break;
					
		case ELEMENT:
			mod = ELEMENT_FLATTENING_FACTOR;
			
			if (id == ResourceUtil.HYDROGEN_ID)
				mod *= HYDROGEN_FLATTENING_FACTOR;
			
			else if (id == ResourceUtil.OXYGEN_ID)
				mod *= OXYGEN_FLATTENING_FACTOR;
					
			else if (id == ResourceUtil.IRON_POWDER_ID)
				mod *= IRON_POWDER_FLATTENING_FACTOR;
			
			else if (id == ResourceUtil.SILICON_ID)
				mod *= SILICON_FLATTENING_FACTOR;
					
			break;
			
		case GEMSTONE:
			mod = GEMSTONE_FLATTENING_FACTOR;
			
			break;
					
		case INSECT:
			mod = INSECT_FLATTENING_FACTOR;
			
			break;

		case MINERAL:
			mod = MINERAL_FLATTENING_FACTOR;
			
			if (id == ResourceUtil.OLIVINE_ID)
				mod *= OLIVINE_FLATTENING_FACTOR;
			else if (id == ResourceUtil.KAMACITE_ID)
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
			
			if (id == ResourceUtil.MYLAR_ID
					|| id == ResourceUtil.AL_MYLAR_ID)
				mod *= MYLAR_FLATTENING_FACTOR;
			
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
								case ResourceUtil.METHANE_ID -> METHANE_COST;
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
	 * Gets the ongoing amount of the good being produced at the settlement by food
	 * production.
	 *
	 * @param settlement the good.
	 * @return amount (kg for amount resources, number for parts, equipment, and
	 *         vehicles).
	 */
	private double getFoodProductionOngoingOutput(Settlement settlement) {
		double result = 0D;

		// Get the amount of the resource that will be produced by ongoing food
		// production processes.
		for (Building b : settlement.getBuildingManager().getBuildingSet(FunctionType.FOOD_PRODUCTION)) {
			// Go through each ongoing food production process.
			for (FoodProductionProcess process : b.getFoodProduction().getProcesses()) {
				for (ProcessItem item : process.getInfo().getOutputList()) {
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
		var rh = settlement.getEquipmentInventory();

		// Get amount of resource in settlement storage.
		amount += rh.getSpecificAmountResourceStored(getID());
        
        // Get amount of resource out on mission vehicles.
        amount += getVehiclesOnMissions(settlement)
                        .map(v -> v.getEquipmentInventory().getSpecificAmountResourceStored(getID()))
                        .collect(Collectors.summingDouble(f -> f));

		// Get amount of resource carried by people on EVA.
		amount += getPersonOnEVA(settlement)
                    .map(p -> p.getEquipmentInventory().getSpecificAmountResourceStored(getID()))
                    .collect(Collectors.summingDouble(f -> f));

		// Get the amount of the resource that will be produced by ongoing manufacturing
		// processes.
		amount += getManufacturingProcessOngoingOutput(settlement);

		// Get the amount of the resource that will be produced by ongoing food
		// production processes.
		amount += getFoodProductionOngoingOutput(settlement);

		return amount;
    }

    @Override
    double calculatePrice(Settlement settlement, double value) {
		double supply = settlement.getGoodsManager().getSupplyScore(getID());
		double factor = 1.5 / (2 + Math.log(supply));
	    double price = getCostOutput() * (1 + 2 * factor * Math.log(value + 1));
	    setPrice(price);
	    return price;
    }

    @Override
    double getDefaultDemandValue() {
        return INITIAL_AMOUNT_DEMAND;
    }

    @Override
    double getDefaultSupplyValue() {
        return INITIAL_AMOUNT_SUPPLY;
    }

    /**
     * Calculates the constant manufacturing need.
     * @Note: if a new building is being put in place, must call this method again
     * to update constantManufacturingInputNeed
     * 
     * @param owner
     * @param settlement
     */
    private void calculateConstantManufacturingDemand(GoodsManager owner, Settlement settlement) {
    	constantManufacturingInputNeed = getConstantManufacturingDemand(owner, settlement);	
    }
    
    /**
     * Calculates the constant food production need.
     * @Note: if a new building is being put in place, must call this method again
     * to update constantFoodProductionInputNeed
     * 
     * @param owner
     * @param settlement
     */
    private void calculateFoodProductionInputNeed(GoodsManager owner, Settlement settlement) {
    	constantFoodProductionInputNeed = getConstantFoodProductionInputNeed(owner, settlement);	
    }
    
    /**
     * Calculates the constant resource process need.
     * @Note: if a new building is being put in place, must call this method again
     * to update constantResourceProcessInputNeed
     * 
     * @param owner
     * @param settlement
     */
    private void calculateResourceProcessInputNeed(GoodsManager owner, Settlement settlement) {
    	constantResourceProcessInputNeed = getConstantResourceProcessInputOutputNeed(owner, settlement);	
    }
    
    /**
     * Calculates the constant life support need.
     * @Note: if a new building is being put in place, must call this method again
     * to update constantLifeSupportNeed
     * 
     * @param owner
     * @param settlement
     */
    private void calculateLifeSupportNeed(GoodsManager owner, Settlement settlement) {
    	constantLifeSupportNeed = getConstantLifeSupportNeed(owner, settlement);	
    }
    
    /**
     * Calculates the constant meal ingredient need.
     * @Note: if a new building is being put in place, must call this method again
     * to update constantMealIngredientNeed
     * 
     * @param owner
     * @param settlement
     */
    private void calculateMealIngredientNeed(Settlement settlement) {
    	constantMealIngredientNeed = getConstantMealIngredientNeed(settlement);	
    }
    
    /**
     * Calculates the constant farming need.
     * @Note: if a new building is being put in place, must call this method again
     * to update constantFarmingNeed
     * 
     * @param owner
     * @param settlement
     */
    private void calculateFarmingNeed(GoodsManager owner, Settlement settlement) {
    	constantFarmingNeed = getConstantFarmingDemand(owner, settlement);	
    }

//    /**
//     * Calculates the constant crop tissue need.
//     * @Note: if a new building is being put in place, must call this method again
//     * to update constantCropTissueNeed
//     * 
//     * @param owner
//     * @param settlement
//     */
//    private void calculateCropTissueNeed(GoodsManager owner, Settlement settlement) {
//    	constantCropTissueNeed = getConstantCropTissueNeed(owner, settlement);	
//    }

    @Override
    void refreshSupplyDemandScore(GoodsManager owner) {
        int id = getID();
        
		double previousDemand = owner.getDemandScore(this);

        Settlement settlement = owner.getSettlement();
		var rh = settlement.getEquipmentInventory();
 
        if (constantManufacturingInputNeed == -1D) {
        	// At startup only
        	calculateConstantManufacturingDemand(owner, settlement);	
        	
            // Note: whenever a building with a higher tech level is added, 
        	// will need to figure out how to to flag and call this method 
        	// again in order to obtain a new demand value for each amount resource
        }
        
        if (constantFoodProductionInputNeed == -1D) {
        	// At startup only
        	calculateFoodProductionInputNeed(owner, settlement);	
        	
            // Note: whenever a building with a higher tech level is added, 
        	// will need to figure out how to to flag and call this method 
        	// again in order to obtain a new demand value for each amount resource
        }

        if (constantResourceProcessInputNeed == -1D) {
        	// At startup only
        	calculateResourceProcessInputNeed(owner, settlement);	
        	
            // Note: whenever a building with a higher tech level is added, 
        	// will need to figure out how to to flag and call this method 
        	// again in order to obtain a new demand value for each amount resource
        }
        
        if (constantLifeSupportNeed == -1D) {
        	// At startup only
        	calculateLifeSupportNeed(owner, settlement);	
        	
            // Note: whenever a building with a higher tech level is added, 
        	// will need to figure out how to to flag and call this method 
        	// again in order to obtain a new demand value for each amount resource
        }
        
        if (constantMealIngredientNeed == -1D) {
        	// At startup only
        	calculateMealIngredientNeed(settlement);	
        	
            // Note: whenever a building with a higher tech level is added, 
        	// will need to figure out how to to flag and call this method 
        	// again in order to obtain a new demand value for each amount resource
        }
        
        if (constantFarmingNeed == -1D) {
        	// At startup only
        	calculateFarmingNeed(owner, settlement);	
        	
            // Note: whenever a building with a higher tech level is added, 
        	// will need to figure out how to to flag and call this method 
        	// again in order to obtain a new demand value for each amount resource
        }
        
//        if (constantCropTissueNeed == -1D) {
//        	// At startup only
//        	calculateCropTissueNeed(owner, settlement);	
//        	
//            // Note: whenever a building with a higher tech level is added, 
//        	// will need to figure out how to to flag and call this method 
//        	// again in order to obtain a new demand value for each amount resource
//        }
        
		// Calculate total supply
		double totalSupply = owner.getAverageSupply(rh.getSpecificAmountResourceStored(id));

		// Store the average supply
		owner.setSupplyScore(this, totalSupply);
			
		// Calculate new projected demand
		double newProjDemand = 
			// The constant manufacturing need of this resource
			this.constantManufacturingInputNeed
			// The constant food production need of this resource
			+ this.constantFoodProductionInputNeed
			// The constant resource processing need of this resource
			+ this.constantResourceProcessInputNeed
			// The constant life support need of this resource
			+ this.constantLifeSupportNeed * settlement.getLogPopFactor()
			// The constant meal ingredient need of this resource
			+ this.constantMealIngredientNeed * settlement.getLogPopFactor()
			// The constant farming need of this resource
			+ this.constantFarmingNeed
			// Tune ice demand.
			+ computeIceProjectedDemand(owner)
			// Tune regolith projected demand.
			+ computeRegolithProjectedDemand(owner)
			// Tune potable water usage demand if applicable.
			+ getPotableWaterUsageDemand(owner, settlement)
			// Tune toiletry usage demand if applicable.
			+ getToiletryUsageDemand(settlement)
			// Tune vehicle demand if applicable.
			+ getVehicleFuelDemand(owner, settlement)
			// Tune the tissue demand due to its crop
			+ computeTissueDemandDueToCrop(owner)
			// The current ongoing manufacturing demand.
			+ getManufacturingOngoingProcessInput(settlement)
			// Tune construction demand.
			+ getConstructionOngoingNeed(settlement)
			// Adjust the demand on minerals and ores.
			+ getMineralOreDemand(owner, settlement)
			// Get the metal demand.
			+ getMetalDemand(owner, settlement)
			// Get the chemical demand.
			+ getChemicalDemand(owner, settlement);
				
		double projectedCache = owner.getProjectedDemandScore(this);
		
		if (newProjDemand == Double.NaN) {
			logger.severe(ResourceUtil.findAmountResourceName(id) + "newProjDemand is NaN.");
			newProjDemand = projectedCache;
		}
		
		double projected = newProjDemand 
			// Flatten certain types of demand.
			* flattenDemand
			// Adjust the demand on various waste products with the disposal cost.
			* modifyWasteResource();

//		if (projectedCache == INITIAL_AMOUNT_DEMAND) {
//			projected = projectedCache;
//		}
//		else {
//			projectedCache = .005 * projected + .995 * projectedCache;
			projected = .02 * projected + .98 * projectedCache;
//		}
		
		owner.setProjectedDemandScore(this, projected);
				
		// Add trade value. Cache is always false if this method is called
		double tradeDemand = owner.determineTradeDemand(this) / 20;

		owner.setTradeDemandScore(this, tradeDemand);
		
		double totalDemand = previousDemand;
		// Note: the ceiling uses projected, not projectedCache
		double ceiling = projected + tradeDemand;
		
		if (previousDemand == INITIAL_AMOUNT_DEMAND) {
			// At the start of the simˇ
			totalDemand = .8 * projected 
						+ .2 * tradeDemand;
		}
//		else if (totalSupply < 0.005 && previousDemand < projectedDemand) {
//			// Quickly ramp up the totalDemand
//			totalDemand = .985 * previousDemand
//						+ .010 * projectedCache
//						+ .005 * tradeDemand; 
//		}
//		else if (previousDemand < projectedDemand + tradeDemand) {
//			// Intentionally loses a tiny percentage of its value
//			// in order to counter the tendency for all goods to increase 
//			// in value over time. 
//			
//			// Warning: a lot of Goods could easily will hit 10,000 demand
//			// if not careful.
//			
//			// Allows only very small fluctuations of demand as possible
//			totalDemand = .993  * previousDemand
//						+ .005  * projectedCache
//						+ .0005 * tradeDemand; 
//		}
		
		// If less than 1, increase a small percent to gradually reach toward one 
		if (totalDemand < ceiling || totalDemand < 1) {
			// Increment projectedDemand
			totalDemand *= 1.01;
		}
		// If less than 1, decrease a small percent to gradually reach toward one 
		else if (totalDemand > ceiling) {
			// Decrement projectedDemand
			totalDemand *= 0.99;
		}
		
		// Save the goods demand
		owner.setDemandScore(this, totalDemand);
    }

	/**
	 * Gets the constant input and output for a resource from all automated resource processes at a
	 * settlement.
	 *
	 * @param resource the amount resource.
	 * @return demand
	 */
	private double getConstantResourceProcessInputOutputNeed(GoodsManager owner, Settlement settlement) {
		double demand = 0D;

		// Get all resource processes at settlement.
		for(ResourceProcess i : getResourceProcesses(settlement)) {
			double processDemand = getResourceProcessInputOutputNeed(owner, i);
			demand += processDemand;
		}
		// Avoid NaN when demand is zero by adding 0.1 before calling Math.sqrt
		return MathUtils.between(2 * Math.sqrt(demand + 0.1), 0.0, MAX_RESOURCE_PROCESSING_DEMAND);
	}

	/**
	 * Gets the input and output need for a resource from an automated resource process.
	 *
	 * @param process  the resource process.
	 * @param resource the amount resource.
	 * @return demand
	 */
	private double getResourceProcessInputOutputNeed(GoodsManager owner, ResourceProcess process) {

		int resourceID = getID();

		Set<Integer> inputResources = process.getInputResources();
		Set<Integer> outputResources = process.getOutputResources();

		if (inputResources.contains(resourceID) && !process.isAmbientInputResource(resourceID)) {
			double outputValue = 0D;
			for (Integer output : outputResources) {
				double singleOutputRate = process.getBaseSingleOutputRate(output);
				if (!process.isWasteOutputResource(resourceID)) {
					outputValue += singleOutputRate;
				}
			}

			double singleInputRate = process.getBaseSingleInputRate(resourceID);

			return outputValue / (singleInputRate + 1); 
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
		for (Building building : settlement.getBuildingManager().getBuildingSet(FunctionType.RESOURCE_PROCESSING)) {
			processes.addAll(building.getResourceProcessing().getProcesses());
		}
		return processes;
	}

	/**
	 * Gets the need for an amount resource as an input in the settlement's Food
	 * Production processes.
	 *
	 * @return demand
	 */
	private double getConstantFoodProductionInputNeed(GoodsManager owner, Settlement settlement) {
		double demand = 0D;

		// Get highest Food Production tech level in settlement.
		if (FoodProductionUtil.doesSettlementHaveFoodProduction(settlement)) {
			int techLevel = FoodProductionUtil.getHighestFoodProductionTechLevel(settlement);
			for (int i = 0; i <= techLevel; i++) {
				for (FoodProductionProcessInfo j : FoodProductionUtil.getProcessesForTechSkillLevel(techLevel)) {
					double foodProductionDemand = getFoodProductionProcessInputNeed(owner, settlement, j);
					demand += foodProductionDemand * (0.5 * i * 1.25);
				}
			}
		}
		// Avoid NaN when demand is zero by adding 0.1 before calling Math.sqrt
		return MathUtils.between(2 * Math.sqrt(demand + 0.1), 0.0, MAX_FOOD_PRODUCTION_DEMAND);
	}

	/**
	 * Gets the need for an input amount resource in a Food Production process.
	 * 
	 * @param owner
	 * @param settlement
	 * @param process the Food Production process.
	 * @return demand
	 */
	private double getFoodProductionProcessInputNeed(GoodsManager owner, Settlement settlement, FoodProductionProcessInfo process) {
		double need = 0D;
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
			for (ProcessItem j : process.getOutputList()) {
				outputsValue += FoodProductionUtil.getProcessItemValue(j, settlement, true);
			}

			double totalItems = 0D;
			for (ProcessItem k : process.getInputList()) {
				totalItems += k.getAmount();
			}

			// Determine value of required process power.
			double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsTime.HOURS_PER_MILLISOL;
			double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();

			double totalInputsValue = (outputsValue - powerValue) * owner.getCommerceFactor(CommerceType.TRADE)
											* owner.getCommerceFactor(CommerceType.CROP)
											* FOOD_PRODUCTION_INPUT_FACTOR;

			if (totalItems > 0D) {
				need = (1D / totalItems) * totalInputsValue;
			}
		}

		return need;
	}

	/**
	 * Gets the constant meal ingredient need.
	 *
	 * @param settlement
	 * @return demand
	 */
	private double getConstantMealIngredientNeed(Settlement settlement) {

		if (!resource.isEdible())
			return 0;
		
		double demand = 0D;
		int id = getID();
		
		if (id == ResourceUtil.TABLE_SALT_ID) {
			// Assuming a person takes 1 meal per sol
			return MarsTime.AVERAGE_SOLS_PER_ORBIT_NON_LEAPYEAR * Cooking.AMOUNT_OF_SALT_PER_MEAL / 10; 
		}

		else if (id == ResourceUtil.CLEANING_AGENT_ID) {
			// Assuming a person takes 3 meals per sol
			return MarsTime.AVERAGE_SOLS_PER_ORBIT_NON_LEAPYEAR * Cooking.getCleaningAgentPerSol(); 
		}
		
		else {
			if (ResourceUtil.getOilResources().contains(id)) {
				// Assuming a person takes 3 meals per sol
				return MarsTime.SOLS_PER_ORBIT_NON_LEAPYEAR * 3 * Cooking.AMOUNT_OF_OIL_PER_MEAL;
			}

			// Determine total demand for cooked meal mass for the settlement.
			double cookedMealDemandSol = personConfig.getFoodConsumptionRate();
			double cookedMealDemandOrbit = cookedMealDemandSol * MarsTime.SOLS_PER_ORBIT_NON_LEAPYEAR;
			double cookedMealDemand = cookedMealDemandOrbit;
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
	 * Gets the base demand for this resource as an ingredient in all meals.
	 * 
	 * @param ar
	 * @param mConfig
	 * @return demand
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
	 * Gets the constant demand for an amount resource as an input in the settlement's
	 * manufacturing processes.
	 *
	 * @return demand
	 */
	private double getConstantManufacturingDemand(GoodsManager owner, Settlement settlement) {
		double need = 0D;

		// Get highest manufacturing tech level in settlement.
		int techLevel = ManufactureUtil.getHighestManufacturingTechLevel(settlement);
		for (int i = 0; i <= techLevel; i++) {
			for (ManufactureProcessInfo info : ManufactureUtil.getManufactureProcessesForTechLevel(i)) {
				double manufacturingInputNeed = getConstantManufacturingProcessInputNeed(owner, settlement, info);
				need += manufacturingInputNeed * MANUFACTURING_DEMAND_MULTIPLIER * (.5 + i * 1.25);
			}
		}
		// Avoid NaN when demand is zero by adding 0.1 before calling Math.sqrt
		return MathUtils.between(1.2 * Math.sqrt(need + 0.1), 0.0, MAX_MANUFACTURING_DEMAND);
	}

	/**
	 * Gets the constant need for an input amount resource in a manufacturing process.
	 *
	 * @param process  the manufacturing process.
	 * @return demand
	 */
	private double getConstantManufacturingProcessInputNeed(GoodsManager owner, Settlement settlement, ManufactureProcessInfo process) {
		double demand = 0D;
		String r = resource.getName().toLowerCase();

		ProcessItem resourceInput = null;
		for (var item : process.getInputList()) {
			if ((ItemType.AMOUNT_RESOURCE == item.getType()) && r.equals(item.getName())) {
				resourceInput = item;
				break;
			}
		}

		if (resourceInput != null) {
			double outputsValue = process.getOutputList().stream()
					.mapToDouble(j -> ManufactureUtil.getManufactureProcessItemGoodValuePoint(j, settlement, true))
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
	 * 
	 * @param resource
	 * @param objective
	 * @return demand
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
	 * Gets the demand for an amount resource as an input in building construction.
	 * This is based on the missing resources of a current stage.
	 * 
	 * @param settlement
	 * @return
	 */
	private double getConstructionOngoingNeed(Settlement settlement) {
		double base = 0D;
		int id = getID();
		
		for (ConstructionSite s : settlement.getConstructionManager().getConstructionSites()) {
			
			if (s.isConstruction()) {
				double amountResourceNeed = s.getCurrentConstructionStage().getResourceNeeded(id) * CONSTRUCTION_SITE_REQUIRED_RESOURCE_FACTOR * 4;	
			
				double itemResourceNeed = obtainIronPartNeed(id, s);
				
				base += amountResourceNeed + itemResourceNeed;
			}
			else {
				// If construction has not started, should anticipate the need
				double amountResourceNeed = s.getCurrentConstructionStage().getResourceNeeded(id) * CONSTRUCTION_SITE_REQUIRED_RESOURCE_FACTOR * 2;	
				
				double itemResourceNeed = obtainIronPartNeed(id, s) / 2;
				
				base += amountResourceNeed + itemResourceNeed;
			}
			
			// Anticipate the need for the next stage
			ConstructionStageInfo info = s.getNextConstructionStageInfo();
			
			if (info != null) {
				double amountResourceNeed = info.getResourceRequired(id) * CONSTRUCTION_SITE_REQUIRED_RESOURCE_FACTOR;	
				
				double itemResourceNeed = obtainIronPartNeed(id, s) / 8;
				
				base += amountResourceNeed + itemResourceNeed;
			}
		}

		return MathUtils.between(base, 0.0, GoodsManager.MAX_DEMAND);
	}

	/**
	 * Obtains iron part need.
	 * 
	 * @param id
	 * @param s
	 * @return
	 */
	private double obtainIronPartNeed(int id, ConstructionSite s) {
		double missing = 0;
		
		if (id == ResourceUtil.IRON_OXIDE_ID || id == ResourceUtil.IRON_POWDER_ID) {
			double partNeed = s.getCurrentConstructionStage().getPartNeeded(ItemResourceUtil.STEEL_INGOT_ID);
			missing += (int)partNeed * ItemResourceUtil.findItemResource(ItemResourceUtil.STEEL_INGOT_ID).getMassPerItem();
			partNeed = s.getCurrentConstructionStage().getPartNeeded(ItemResourceUtil.STEEL_SHEET_ID);
			missing += (int)partNeed * ItemResourceUtil.findItemResource(ItemResourceUtil.STEEL_SHEET_ID).getMassPerItem();
			partNeed = s.getCurrentConstructionStage().getPartNeeded(ItemResourceUtil.STEEL_POST_ID);
			missing += (int)partNeed * ItemResourceUtil.findItemResource(ItemResourceUtil.STEEL_POST_ID).getMassPerItem();
			partNeed = s.getCurrentConstructionStage().getPartNeeded(ItemResourceUtil.STEEL_TRUSS_ID);
			missing += (int)partNeed * ItemResourceUtil.findItemResource(ItemResourceUtil.STEEL_TRUSS_ID).getMassPerItem();
			partNeed = s.getCurrentConstructionStage().getPartNeeded(ItemResourceUtil.STEEL_PIPE_ID);
			missing += (int)partNeed * ItemResourceUtil.findItemResource(ItemResourceUtil.STEEL_PIPE_ID).getMassPerItem();
		}
		
		return missing;
	}
	
	/**
	 * Gets the constant farming need for the resource.
	 *
	 * @param owner
	 * @param settlement
	 * @return demand for the resource.
	 */
	private double getConstantFarmingDemand(GoodsManager owner, Settlement settlement) {
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
	 * @return demand
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
			case ResourceUtil.FERTILIZER_ID -> (Crop.FERTILIZER_NEEDED_IN_SOIL_PER_SQM * Crop.FERTILIZER_NEEDED_WATERING * solsInOrbit) * factor * 15;
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
	 * @return demand
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
	 * Gets the constant life support demand for an amount resource.
	 * 
	 * @param owner
	 * @param settlement
	 * @return demand
?	 */
	private double getConstantLifeSupportNeed(GoodsManager owner, Settlement settlement) {
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
			
			return amountNeededSol * owner.getCommerceFactor(CommerceType.TRADE)  
					* LIFE_SUPPORT_FACTOR;
		}
		else
			return 0;
	}

	/**
	 * Gets mineral/ore demand.
	 * 
	 * @param owner
	 * @param settlement
	 * @return demand
	 */
	private double getMineralOreDemand(GoodsManager owner, Settlement settlement) {
		double base = BASE_MINERAL_ORE;
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
		}
        
		double regolithDemand = owner.getDemandScoreWithID(ResourceUtil.REGOLITH_ID);
		double sandDemand = owner.getDemandScoreWithID(ResourceUtil.SAND_ID);
		base = MathUtils.between(REGOLITH_BASE_DEMAND / regolithDemand, REGOLITH_LOWEST_DEMAND, REGOLITH_BASE_DEMAND);
		
		if (resourceID == ResourceUtil.SAND_ID) {
			// the demand for sand is dragged up or down by that of regolith
			// loses 10% by default
			return base * (.2 * regolithDemand + .7 * sandDemand) 
						/ (1 + sandDemand) * SAND_VALUE_MODIFIER;
		}
		
		for (int id : ResourceUtil.ROCK_IDS) {
			if (resourceID == id) {
				double rockDemand = owner.getDemandScoreWithID(id);
				return base * (.2 * regolithDemand + .8 * rockDemand) 
						 * ROCK_VALUE_MODIFIER;
			}
		}

		for (int id : ResourceUtil.MINERAL_CONC_IDs) {
			if (resourceID == id) {
				double mineralDemand = owner.getDemandScoreWithID(id);
				return base * (.3 * regolithDemand + .7 * mineralDemand) 
						 * MINERAL_VALUE_MODIFIER;
			}
		}

		for (int id : ResourceUtil.ORE_DEPOSIT_IDS) {
			if (resourceID == id) {
				double oreDemand = owner.getDemandScoreWithID(id);
				// loses 10% by default
				return base * (.4 * regolithDemand + .6 * oreDemand) 
						 * ORES_VALUE_MODIFIER;
			}
		}

		for (int id : ResourceUtil.REGOLITH_TYPES_IDS) {
			if (resourceID == id) {
				return base * regolithDemand * REGOLITH_VALUE_MODIFIER;
			}
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

		return 0;
	}

	/**
	 * Gets the metal demand.
	 * 
	 * @param owner
	 * @param settlement
	 * @return demand
	 */
	private double getMetalDemand(GoodsManager owner, Settlement settlement) {
		double base = BASE_METAL_DEMAND;
		int resourceID = getID();
		switch(resourceID) {
        	case ResourceUtil.IRON_OXIDE_ID:
				return base * IRON_OXIDE_MODIFIER;
			case ResourceUtil.IRON_POWDER_ID:
				return base * IRON_POWDER_MODIFIER;
				
		}
		
		return 0;
	}

	/**
	 * Gets the chemical demand.
	 * 
	 * @param owner
	 * @param settlement
	 * @return demand
	 */
	private double getChemicalDemand(GoodsManager owner, Settlement settlement) {
		double base = BASE_CHEMICAL_DEMAND;
		int resourceID = getID();
		switch(resourceID) {
        	case ResourceUtil.ACETYLENE_ID:
				return base * ACETYLENE_MODIFIER;
			case ResourceUtil.CLEANING_AGENT_ID:
				return base * CLEANING_AGENT_MODIFIER;
			case ResourceUtil.ETHANE_ID:
				return base * ETHANE_MODIFIER;
			case ResourceUtil.ETHYLENE_ID:
				return base * ETHYLENE_MODIFIER;
			case ResourceUtil.EPOXY_RESIN_ID:
				return base * EPOXY_RESIN_MODIFIER;
			case ResourceUtil.H2O2_ID:
				return base * H2O2_MODIFIER;
			case ResourceUtil.POLYCARBONATE_RESIN_ID:
				return base * POLYCARBONATE_RESIN_MODIFIER;
			case ResourceUtil.POLYESTER_FIBER_ID:
				return base * FIBER_MODIFIER;
			case ResourceUtil.POLYESTER_RESIN_ID:
				return base * POLYESTER_RESIN_MODIFIER;
			case ResourceUtil.POLYETHYLENE_ID:
				return base * POLYETHYLENE_MODIFIER;
			case ResourceUtil.POLYPROPYLENE_ID:
				return base * PROPYLENE_MODIFIER;
			case ResourceUtil.POLYSTYRENE_ID:
				return base * STYRENE_MODIFIER;
			case ResourceUtil.POLYURETHANE_ID:
				return base * ETHANE_MODIFIER;
			case ResourceUtil.PROPYLENE_ID:
				return base * PROPYLENE_MODIFIER;
			case ResourceUtil.STYRENE_ID:
				return base * STYRENE_MODIFIER;
			case ResourceUtil.LIME_ID:
				return base * LIME_MODIFIER;
		}
		
		return 0;
	}
	
	/**
	 * Adjusts the demand for waste resources.
	 *
	 * @return demand
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
			case ResourceUtil.TOXIC_WASTE_ID -> .1;
			case ResourceUtil.CROP_WASTE_ID -> 4 * USEFUL_WASTE_VALUE_MODIFIER;
			case ResourceUtil.COMPOST_ID -> 2 * USEFUL_WASTE_VALUE_MODIFIER;
			default -> 1D;
		};
	}

	/**
	 * Gets the potable water usage demand for an amount resource.
	 *
	 * @param resource the resource to check.
	 * @return demand
	 */
	private double getPotableWaterUsageDemand(GoodsManager owner, Settlement settlement) {
		double demand = 0;
		if (getID() == ResourceUtil.WATER_ID) {
			// Add the awareness of the water ration level in adjusting the water demand
			double waterRationLevel = settlement.getRationing().getRationingLevel();
			double amountNeededSol = personConfig.getWaterUsageRate();
			demand = amountNeededSol *  WATER_VALUE_MODIFIER 
					* owner.getCommerceFactor(CommerceType.TRADE) * Math.sqrt(1.0 + waterRationLevel);
		}

		return demand;
	}

	/**
	 * Computes ice and brine water projected demand.
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
			return  (.25 * water + .75 * ice)
					* ICE_VALUE_MODIFIER;
		}
		else if (resourceID == ResourceUtil.BRINE_WATER_ID) {
			double ice = 1 + owner.getDemandScoreWithID(resourceID);
			double water = 1 + owner.getDemandScoreWithID(ResourceUtil.WATER_ID);
			double brineWater = 1 + owner.getDemandScoreWithID(ResourceUtil.BRINE_WATER_ID);
			// Use the water's VP and existing iceSupply to compute the ice demand
			return  (.3 * water + .3 * ice + .4 * brineWater)
					* BRINE_WATER_VALUE_MODIFIER;
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
		if (resourceID == ResourceUtil.REGOLITH_ID
			|| resourceID == ResourceUtil.REGOLITHB_ID
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
					* REGOLITH_VALUE_MODIFIER;
		}

		return demand;
	}

	/**
	 * Gets the toilet tissue usage demand.
	 *
	 * @param resource the resource to check.
	 * @return demand
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
	 * @return demand for the resource.
	 */
	private double getVehicleFuelDemand(GoodsManager owner, Settlement settlement) {
		double demand = 0D;
		double transFactor = owner.getCommerceFactor(CommerceType.TRANSPORT) * VEHICLE_FUEL_FACTOR;
		int pop = settlement.getNumCitizens();

		switch(getID()) {
			case ResourceUtil.METHANOL_ID: {
				for(Vehicle v: settlement.getAllAssociatedVehicles()) {
					double fuelDemand = v.getEquipmentInventory().getSpecificCapacity(getID());
					demand += fuelDemand;
				}
				demand = demand * transFactor * METHANOL_VALUE_MODIFIER / Math.sqrt(1 + 2 * pop) * 2;
			} break;
		
			case ResourceUtil.METHANE_ID: {
				for(Vehicle v: settlement.getAllAssociatedVehicles()) {
					double fuelDemand = v.getEquipmentInventory().getSpecificCapacity(getID());
					demand += fuelDemand;
				}
				demand = demand * transFactor * METHANE_VALUE_MODIFIER / Math.sqrt(1 + 2 * pop) * 2;
			} break;

			case ResourceUtil.HYDROGEN_ID: {
				demand =  transFactor * HYDROGEN_VALUE_MODIFIER / Math.sqrt(1 + 2 * pop) * 2;
			} break;
		}

		return demand;
	}
	
	/**
	 * Is this object the same as another object ?
	 */
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	/**
	 * Gets the hash code value.
	 *
	 * @return hash code
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
