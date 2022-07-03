/*
 * Mars Simulation Project
 * AmountResourceGood.java
 * @date 2022-06-26
 * @author Barry Evans
 */
package org.mars_sim.msp.core.goods;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.food.FoodProductionProcess;
import org.mars_sim.msp.core.food.FoodProductionProcessInfo;
import org.mars_sim.msp.core.food.FoodProductionProcessItem;
import org.mars_sim.msp.core.food.FoodProductionUtil;
import org.mars_sim.msp.core.manufacture.ManufactureProcessInfo;
import org.mars_sim.msp.core.manufacture.ManufactureProcessItem;
import org.mars_sim.msp.core.manufacture.ManufactureUtil;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemType;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.structure.building.function.ResourceProcess;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.building.function.cooking.HotMeal;
import org.mars_sim.msp.core.structure.building.function.cooking.Ingredient;
import org.mars_sim.msp.core.structure.building.function.cooking.MealConfig;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.structure.building.function.farming.Crop;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.construction.ConstructionUtil;
import org.mars_sim.msp.core.structure.construction.ConstructionValues;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * This represents how a Amount Resource can be traded.
 */
class AmountResourceGood extends Good {
	
	private static final long serialVersionUID = 1L;
	
	// TODO, move these to the AmountResource class via XML config
	private static final double INITIAL_AMOUNT_DEMAND = 0;
	private static final double INITIAL_AMOUNT_SUPPLY = 0;

    private static final double CO2_VALUE = 0.0001;
	private static final double CL_VALUE = 0.01;
	private static final double ICE_VALUE = 1.5;
	private static final double FOOD_VALUE = 0.1;
	private static final double DERIVED_VALUE = .07;
	private static final double SOY_VALUE = .05;
	private static final int CROP_VALUE = 3;
	private static final double WASTE_WATER_VALUE = .05D;
	private static final double USEFUL_WASTE_VALUE = 1.05D;
	private static final double ANIMAL_VALUE = .1;
	private static final double CHEMICAL_VALUE = 0.01;
	private static final double MEDICAL_VALUE = 0.001;
	private static final double WASTE_VALUE = 0.0001;
	private static final double OIL_VALUE = 0.001;
	private static final double ROCK_VALUE = 0.005;
	private static final double REGOLITH_VALUE = .02;
	private static final double ORE_VALUE = 0.03;
	private static final double MINERAL_VALUE = 0.1;
	private static final double STANDARD_AMOUNT_VALUE = 0.3;
	private static final double ELEMENT_VALUE = 0.5;
	private static final double LIFE_SUPPORT_VALUE = 1;

    private static final double ICE_VALUE_MODIFIER = 5D;
	private static final double WATER_VALUE_MODIFIER = 1D;
	private static final double SOIL_VALUE_MODIFIER = .5;
	private static final double REGOLITH_VALUE_MODIFIER = 25D;
	private static final double SAND_VALUE_MODIFIER = 5D;
	private static final double CONCRETE_VALUE_MODIFIER = .5D;
	private static final double ROCK_MODIFIER = 0.99D;
	private static final double METEORITE_MODIFIER = 1.05;
	private static final double SALT_VALUE_MODIFIER = .2;
	private static final double OXYGEN_VALUE_MODIFIER = .02D;
	private static final double METHANE_VALUE_MODIFIER = .5D;
	private static final double LIFE_SUPPORT_FACTOR = .005;
	private static final double FOOD_VALUE_MODIFIER = .1;

	private static final double VEHICLE_FUEL_FACTOR = .5;
	private static final double FARMING_FACTOR = .1;
	private static final double TISSUE_CULTURE_FACTOR = .75;
	private static final double LEAVES_FACTOR = .5;
	private static final double CROP_FACTOR = .1;
	private static final double DESSERT_FACTOR = .1;

	private static final double REGOLITH_DEMAND_FACTOR = 30;
	private static final double CHEMICAL_DEMAND_FACTOR = .01;
	private static final double COMPOUND_DEMAND_FACTOR = .01;
	private static final double ELEMENT_DEMAND_FACTOR = .1;

	private static final double COOKED_MEAL_INPUT_FACTOR = .5;
	private static final double MANUFACTURING_INPUT_FACTOR = 2D;
	private static final double FOOD_PRODUCTION_INPUT_FACTOR = .1;
	private static final double RESOURCE_PROCESSING_INPUT_FACTOR = .5;
	private static final double CONSTRUCTION_SITE_REQUIRED_RESOURCE_FACTOR = 100D;
	private static final double CONSTRUCTING_INPUT_FACTOR = 2D;


	private static final double LIFE_SUPPORT_MAX = 100;

	private static final int METEORITE_ID = ResourceUtil.findIDbyAmountResourceName("meteorite");

	private double flattenDemand;

	private double costModifier;

    AmountResourceGood(AmountResource ar) {
        super(ar.getName(), ar.getID());

		// Calcualted fixed values
		flattenDemand = calculateFlattenAmountDemand(ar);
		costModifier = calculateCostModifier(ar);
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
        return getAmountResource().getGoodType();

    }

    private AmountResource getAmountResource() {
        return ResourceUtil.findAmountResource(getID());
    }

    @Override
    protected double computeCostModifier() {
		return costModifier;
	}

	/**
	 * Calculate the cost modifier from a resource
	 */
	private static double calculateCostModifier(AmountResource ar) {
        boolean edible = ar.isEdible();
        boolean lifeSupport = ar.isLifeSupport();
        GoodType type = ar.getGoodType();
        double result = 0D;

        if (lifeSupport)
            result += LIFE_SUPPORT_VALUE;
        
        else if (edible) {
            if (type == GoodType.DERIVED)
                result += DERIVED_VALUE;
            else if (type == GoodType.SOY_BASED)
                result += SOY_VALUE;
            else if (type == GoodType.ANIMAL)
                result += ANIMAL_VALUE;
            else
                result += FOOD_VALUE;
        }
        
        else if (type == GoodType.WASTE)
            result += WASTE_VALUE ;
        
        else if (ar.getName().equalsIgnoreCase("chlorine"))
            result += CL_VALUE;
        else if (ar.getName().equalsIgnoreCase("carbon dioxide"))
            result += CO2_VALUE;
        else if (ar.getName().equalsIgnoreCase("ice"))
            result += ICE_VALUE;


		// TODO Should be a Map GoodType -> double VALUE
        else if (type == GoodType.MEDICAL)
            result += MEDICAL_VALUE;
        else if (type == GoodType.OIL)
            result += OIL_VALUE;
        else if (type == GoodType.CROP)
            result += CROP_VALUE;
        else if (type == GoodType.ROCK)
            result += ROCK_VALUE;
        else if (type == GoodType.REGOLITH)
            result += REGOLITH_VALUE;
        else if (type == GoodType.ORE)
            result += ORE_VALUE;
        else if (type == GoodType.MINERAL)
            result += MINERAL_VALUE;
        else if (type == GoodType.ELEMENT)
            result += ELEMENT_VALUE;
        else if (type == GoodType.CHEMICAL)
            result += CHEMICAL_VALUE;
        else
            result += STANDARD_AMOUNT_VALUE ;

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
		for(Building b : settlement.getBuildingManager().getBuildings(FunctionType.FOOD_PRODUCTION)) {
			// Go through each ongoing food production process.
			for(FoodProductionProcess process : b.getFoodProduction().getProcesses()) {
				for(FoodProductionProcessItem item : process.getInfo().getOutputList()) {
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
		amount += settlement.getAmountResourceStored(getID());
        
        // Get amount of resource out on mission vehicles.
        amount += getVehiclesOnMissions(settlement)
                        .map(v -> v.getAmountResourceStored(getID()))
                        .collect(Collectors.summingDouble(f -> f));

		// Get amount of resource carried by people on EVA.
		amount += getPersonOnEVA(settlement)
                    .map(p -> p.getAmountResourceStored(getID()))
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
    double getPrice(Settlement settlement, double value) {
		double totalMass = Math.round(settlement.getAmountResourceStored(getID()) * 100.0)/100.0;
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
    void refreshSupplyDemandValue(GoodsManager owner) {
        int id = getID();
		double previousDemand = owner.getDemandValue(this);

        Settlement settlement = owner.getSettlement();
		double average = 0;
		double trade = 0;
		double totalDemand = 0;
		
		double totalSupply = 0;

		// Needed for loading a saved sim
		int solElapsed = marsClock.getMissionSol();
		// Compact and/or clear supply and demand maps every x days
		int numSol = solElapsed % Settlement.SUPPLY_DEMAND_REFRESH + 1;

		// Calculate the average demand
		average = capLifeSupportAmountDemand(getAverageAmountDemand(owner, numSol));		

		// Calculate projected demand
		double projected = 
			// Tune ice demand.
			computeIceProjectedDemand(owner, settlement)
			// Tune regolith projected demand.
			+ computeRegolithProjectedDemand(owner, settlement)
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
			// Tune the crop demand
			+ getCropDemand(owner, settlement)
			// Tune resource processing demand.
			+ getResourceProcessingDemand(owner, settlement)
			// Tune manufacturing demand.
			+ getResourceManufacturingDemand(owner, settlement)
			// Tune food production related demand.
			+ getResourceFoodProductionDemand(owner, settlement)
			// Tune demand for the ingredients in a cooked meal.
			+ getResourceCookedMealIngredientDemand(settlement)
			// Tune dessert demand.
			+ getResourceDessertDemand(settlement)
			// Tune construction demand.
			+ getResourceConstructionDemand(settlement)
			// Tune construction site demand.
			+ getResourceConstructionSiteDemand(settlement)
			// Adjust the demand on various waste products with the disposal cost.
			+ getWasteDisposalSinkCost()
			// Adjust the demand on minerals and ores.
			+ getMineralDemand(owner, settlement)
			// Flatten certain types of demand.
			* flattenDemand;
			
		// Add trade value. Cache is always false if this method is called
		trade = owner.determineTradeDemand(this);
		
		if (previousDemand == 0) {
			// At the start of the sim
			totalDemand = (.5 * average + .1 * projected + .01 * trade);
		}
		else {
			// Intentionally loses .01% 
			// Allows only very small fluctuations of demand as possible
			totalDemand = (.9998 * previousDemand + .00005 * projected + .00005 * trade);
		}

		// Save the goods demand
		owner.setDemandValue(this, totalDemand);
		
		// Calculate total supply
		totalSupply = getAverageAmountSupply(settlement.getAmountResourceStored(id), numSol);

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
	private static double getAverageAmountSupply(double supplyStored, int solElapsed) {
		double aveSupply = 0.5 + Math.log((1 + 5 * supplyStored) / solElapsed);

		if (aveSupply < 0.5)
			aveSupply = 0.5;

		return aveSupply;
	}

	/**
	 * Gets the new demand.
	 *
	 * @param resource
	 * @param projectedDemand
	 * @param solElapsed
	 * @return
	 */
	private double getAverageAmountDemand(GoodsManager owner, int solElapsed) {
		return Math.min(10, owner.getDemandValue(this) / solElapsed);
	}
	

	/**
	 * Flattens the amount demand on certain selected resources or types of resources.
	 * 
	 * @param resource
	 * @return
	 */
	private static double calculateFlattenAmountDemand(AmountResource ar) {
		double demand = 0;
		String name = ar.getName();
		
		if (name.contains("polyester")
				|| name.contains("styrene")
				|| name.contains("polyethylene"))
			demand = CHEMICAL_DEMAND_FACTOR;
		
		else {
			switch(ar.getGoodType()) {
				case REGOLITH:
				case ORE:
				case MINERAL:
					demand = REGOLITH_DEMAND_FACTOR;
					break;

				case ROCK:
					demand = 2;
					break;
		
				case CHEMICAL:
					demand = CHEMICAL_DEMAND_FACTOR;
					break;

				case ELEMENT:
					demand = ELEMENT_DEMAND_FACTOR;
					break;

				case COMPOUND:
					demand = COMPOUND_DEMAND_FACTOR;
					break;

				case WASTE:
					demand = WASTE_VALUE;
					break;

				default:
					demand = 1;
			}
		}

		return demand;
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
			double processDemand = getResourceProcessDemand(owner, settlement, i);
			demand += processDemand;
		}

		return Math.min(1000, demand);
	}

	/**
	 * Gets the demand for a resource from an automated resource process.
	 *
	 * @param process  the resource process.
	 * @param resource the amount resource.
	 * @return demand (kg)
	 */
	private double getResourceProcessDemand(GoodsManager owner, Settlement settlement, ResourceProcess process) {
		double demand = 0D;
		int resource = getID();

		Set<Integer> inputResources = process.getInputResources();
		Set<Integer> outputResources = process.getOutputResources();

		if (inputResources.contains(resource) && !process.isAmbientInputResource(resource)) {
			double outputValue = 0D;
			for(Integer output : outputResources) {
				double outputRate = process.getMaxOutputResourceRate(output);
				if (!process.isWasteOutputResource(resource)) {
					outputValue += (owner.getDemandValue(GoodsUtil.getGood(output)) * outputRate);
				}
			}

			double resourceInputRate = process.getMaxInputResourceRate(resource);

			// Determine value of required process power.
			double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsClock.HOURS_PER_MILLISOL;
			double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();

			double totalInputsValue = (outputValue - powerValue) * RESOURCE_PROCESSING_INPUT_FACTOR;

			if (totalInputsValue > 0D) {
				double demandMillisol = resourceInputRate;
				double demandSol = demandMillisol * 1000D;
				demand = demandSol;
			}
		}

		return demand;
	}

	/**
	 * Get all resource processes at settlement.
	 *
	 * @return list of resource processes.
	 */
	private static List<ResourceProcess> getResourceProcesses(Settlement settlement) {
		List<ResourceProcess> processes = new ArrayList<>();
		for(Building building : settlement.getBuildingManager().getBuildings()) {
			if (building.hasFunction(FunctionType.RESOURCE_PROCESSING)) {
				ResourceProcessing processing = building.getResourceProcessing();
				processes.addAll(processing.getProcesses());
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
		if (ManufactureUtil.doesSettlementHaveManufacturing(settlement)) {
			int techLevel = ManufactureUtil.getHighestManufacturingTechLevel(settlement);
			for(ManufactureProcessInfo i : ManufactureUtil.getManufactureProcessesForTechLevel(techLevel)) {
				double manufacturingDemand = getResourceManufacturingProcessDemand(owner, settlement, i);
				demand += manufacturingDemand / 100D;
			}
		}

		return Math.min(1000, demand);
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
			for(FoodProductionProcessInfo i : FoodProductionUtil.getFoodProductionProcessesForTechLevel(techLevel)) {
				double foodProductionDemand = getResourceFoodProductionProcessDemand(owner, settlement, i);
				demand += foodProductionDemand;
			}
		}

		return Math.min(1000, demand);
	}

	/**
	 * Gets the demand for an input amount resource in a manufacturing process.
	 *
	 * @param process  the manufacturing process.
	 * @return demand (kg)
	 */
	private double getResourceManufacturingProcessDemand(GoodsManager owner, Settlement settlement, ManufactureProcessInfo process) {
		double demand = 0D;
		String r = getAmountResource().getName().toLowerCase();

		ManufactureProcessItem resourceInput = null;
		for(ManufactureProcessItem item : process.getInputList()) {
			if ((ItemType.AMOUNT_RESOURCE == item.getType()) && r.equals(item.getName())) {
				resourceInput = item;
				break;
			}
		}

		if (resourceInput != null) {
			double outputsValue = 0D;
			for(ManufactureProcessItem j : process.getOutputList()) {
				outputsValue += ManufactureUtil.getManufactureProcessItemValue(j, settlement, true);
			}

			double totalItems = 0D;
			for(ManufactureProcessItem k : process.getInputList()) {
				totalItems += k.getAmount();
			}

			// Determine value of required process power.
			double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsClock.HOURS_PER_MILLISOL;
			double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();

			// Obtain the value of this resource
			double totalInputsValue = (outputsValue - powerValue);

			int resource = getID();
			GoodType type = getGoodType();
			switch(settlement.getObjective()) {
				case BUILDERS_HAVEN: { 
					if (GoodType.REGOLITH == type
					|| GoodType.MINERAL == type
					|| GoodType.ORE == type
					|| GoodType.UTILITY == type) {
						totalInputsValue *= owner.getBuildersFactor();
					}
				} break;

				case CROP_FARM: {
					if (GoodType.CROP == type
					|| GoodType.DERIVED == type
					|| GoodType.SOY_BASED == type) {
						totalInputsValue *= owner.getCropFarmFactor();
					}
				} break;

				case MANUFACTURING_DEPOT: 
					totalInputsValue *= owner.getManufacturingFactor();
				break;

				case RESEARCH_CAMPUS: { 
					if (GoodType.MEDICAL == type
					|| GoodType.ORGANISM == type
					|| GoodType.CHEMICAL == type
					|| GoodType.ROCK == type) {
						totalInputsValue *= owner.getResearchFactor();
					}
				} break;

				case TRADE_CENTER:
						totalInputsValue *= owner.getTradeFactor();
				break;

				case TRANSPORTATION_HUB: {
					if (resource == ResourceUtil.methaneID
					|| resource == ResourceUtil.methanolID
					|| resource == ResourceUtil.hydrogenID) {
						totalInputsValue *= owner.getTransportationFactor();
					}
				} break;

				default:
					break;
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
	 * Gets the demand for an input amount resource in a Food Production process.
	 *
	 * @param process  the Food Production process.
	 * @return demand (kg)
	 */
	private double getResourceFoodProductionProcessDemand(GoodsManager owner, Settlement settlement, FoodProductionProcessInfo process) {
		double demand = 0D;
		String name = getAmountResource().getName();

		FoodProductionProcessItem resourceInput = null;
		for(FoodProductionProcessItem i : process.getInputList()) {
			if ((ItemType.AMOUNT_RESOURCE == i.getType())
					&& name.equalsIgnoreCase(i.getName())) {
				resourceInput = i;
				break;
			}
		}

		if (resourceInput != null) {
			double outputsValue = 0D;
			for(FoodProductionProcessItem j : process.getOutputList()) {
				outputsValue += FoodProductionUtil.getFoodProductionProcessItemValue(j, settlement, true);
			}

			double totalItems = 0D;
			for(FoodProductionProcessItem k : process.getInputList()) {
				totalItems += k.getAmount();
			}

			// Determine value of required process power.
			double powerHrsRequiredPerMillisol = process.getPowerRequired() * MarsClock.HOURS_PER_MILLISOL;
			double powerValue = powerHrsRequiredPerMillisol * settlement.getPowerGrid().getPowerValue();

			double totalInputsValue = (outputsValue - powerValue) * owner.getTradeFactor() * owner.getCropFarmFactor()
					* FOOD_PRODUCTION_INPUT_FACTOR;

			if (totalItems > 0D) {
				demand = (1D / totalItems) * totalInputsValue;
			}
		}

		return demand;
	}

	/**
	 * Gets the demand for a resource as a cooked meal ingredient.
	 *
	 * @param resource the amount resource.
	 * @return demand (kg)
	 */
	private double getResourceCookedMealIngredientDemand(Settlement settlement) {
		double demand = 0D;

		if (getAmountResource().isEdible()) {
			int id = getID();
			if (id == ResourceUtil.tableSaltID) {
				// Assuming a person takes 3 meals per sol
				return MarsClock.AVERAGE_SOLS_PER_ORBIT_NON_LEAPYEAR * 3 * Cooking.AMOUNT_OF_SALT_PER_MEAL;
			}

			else {
				for (int oilID : Cooking.getOilMenu()) {
					if (id == oilID) {
						// Assuming a person takes 3 meals per sol
						return MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR * 3 * Cooking.AMOUNT_OF_OIL_PER_MEAL;
					}
				}

				// Determine total demand for cooked meal mass for the settlement.
				double cookedMealDemandSol = personConfig.getFoodConsumptionRate();
				double cookedMealDemandOrbit = cookedMealDemandSol * MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR;
				int numPeople = settlement.getNumCitizens();
				double cookedMealDemand = numPeople * cookedMealDemandOrbit;

				// Determine demand for the resource as an ingredient for each cooked meal
				// recipe.
				int numMeals = MealConfig.getDishList().size();
				for(HotMeal meal : MealConfig.getDishList()) {
					for(Ingredient ingredient : meal.getIngredientList()) {
						if (id == ingredient.getAmountResourceID()) {
							demand += ingredient.getProportion() * cookedMealDemand / numMeals
									* COOKED_MEAL_INPUT_FACTOR;
						}
					}
				}
			}
		}

		return demand;
	}

	/**
	 * Gets the demand for a food dessert item.
	 *
	 * @param resource the amount resource.
	 * @return demand (kg)
	 */
	private double getResourceDessertDemand(Settlement settlement) {
		double demand = 0D;

		AmountResource[] dessert = PreparingDessert.getArrayOfDessertsAR();
		boolean hasDessert = false;

		if (dessert[0] != null) {
			for (AmountResource ar : dessert) {
				if (ar.getID() == getID()) {
					hasDessert = true;
					break;
				}
			}

			if (hasDessert) {
				double amountNeededSol = personConfig.getDessertConsumptionRate() / dessert.length;
				int numPeople = settlement.getNumCitizens();
				demand = 5 * Math.log(1 + numPeople) * amountNeededSol * DESSERT_FACTOR;
			}
		}

		return demand;
	}

	/**
	 * Gets the demand for a resource from construction sites.
	 *
	 * @return demand (kg)
	 */
	private double getResourceConstructionSiteDemand(Settlement settlement) {
		double demand = 0D;
		int resource = getID();

		// Add demand for resource required as remaining construction material on
		// construction sites.
		for(ConstructionSite site : settlement.getConstructionManager().getConstructionSites()) {
			if (site.hasUnfinishedStage() && !site.getCurrentConstructionStage().isSalvaging()) {
				ConstructionStage stage = site.getCurrentConstructionStage();
				if (stage.getRemainingResources().containsKey(resource)) {
					double requiredAmount = stage.getRemainingResources().get(resource);
					demand += requiredAmount * CONSTRUCTION_SITE_REQUIRED_RESOURCE_FACTOR;
				}
			}
		}

		return demand;
	}

	/**
	 * Gets the demand for an amount resource as an input in building construction.
	 *
	 * @return demand (kg)
	 */
	private double getResourceConstructionDemand(Settlement settlement) {
		double demand = 0D;

		ConstructionValues values = settlement.getConstructionManager().getConstructionValues();
		int bestConstructionSkill = ConstructionUtil.getBestConstructionSkillAtSettlement(settlement);
		for(Entry<ConstructionStageInfo, Double> stageDetail : values.getAllConstructionStageValues(bestConstructionSkill).entrySet()) {
			ConstructionStageInfo stage = stageDetail.getKey();
			double stageValue = stageDetail.getValue();
			if (stageValue > 0D && ConstructionStageInfo.BUILDING.equals(stage.getType())
					&& isLocallyConstructable(settlement, stage)) {
				double constructionDemand = getResourceConstructionStageDemand(stage, stageValue);
				if (constructionDemand > 0D) {
					demand += constructionDemand;
				}
			}
		}

		return demand;
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

		// TODO Not sure this makes sense
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

		int resource = getID();

		// Add resource amount needed for stage.
		double result = stage.getResources().getOrDefault(resource, 0D);

		// Add resource amount needed for first prestage, if any.
		ConstructionStageInfo preStage1 = ConstructionUtil.getPrerequisiteStage(stage);
		if ((preStage1 != null) && preStage1.isConstructable()) {
			result += preStage1.getResources().getOrDefault(resource, 0D);

			// Add resource amount needed for second prestage, if any.
			ConstructionStageInfo preStage2 = ConstructionUtil.getPrerequisiteStage(preStage1);
			if ((preStage2 != null) && preStage2.isConstructable()) {
				result += preStage2.getResources().getOrDefault(resource, 0D);
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
		for(Building b : settlement.getBuildingManager().getBuildings(FunctionType.FARMING)) {
			Farming farm = b.getFarming();
			demand += getFarmingResourceDemand(farm) * owner.getCropFarmFactor() * FARMING_FACTOR;
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
		double demand = 0;
		int resource = getID();
		
		double averageGrowingCyclesPerOrbit = farm.getAverageGrowingCyclesPerOrbit();
		double totalCropArea = farm.getGrowingArea();
		int solsInOrbit = MarsClock.SOLS_PER_ORBIT_NON_LEAPYEAR;
		double factor = totalCropArea * averageGrowingCyclesPerOrbit / solsInOrbit;

		if (resource == ResourceUtil.waterID) {
			// Average water consumption rate of crops per orbit using total growing area.
			demand = cropConfig.getWaterConsumptionRate() * factor;
		} else if (resource == ResourceUtil.co2ID) {
			// Average co2 consumption rate of crops per orbit using total growing area.
			demand = cropConfig.getCarbonDioxideConsumptionRate() * factor;
		} else if (resource == ResourceUtil.oxygenID) {
			// Average oxygen consumption rate of crops per orbit using total growing area.
			demand = cropConfig.getOxygenConsumptionRate() * factor;
		} else if (resource == ResourceUtil.soilID) {
			// Estimate soil needed for average number of crop plantings for total growing
			// area.
			demand = Crop.NEW_SOIL_NEEDED_PER_SQM * factor;
		} else if (resource == ResourceUtil.fertilizerID) {
			// Estimate fertilizer needed for average number of crop plantings for total
			// growing area.
			// Estimate fertilizer needed when grey water not available.
			demand = (Crop.FERTILIZER_NEEDED_IN_SOIL_PER_SQM + Crop.FERTILIZER_NEEDED_WATERING) * factor;
		} else if (resource == ResourceUtil.greyWaterID) {
			// NOTE: how to properly get rid of grey water? it should NOT be considered an
			// economically vital resource
			// Average grey water consumption rate of crops per orbit using total growing
			// area.
			// demand = cropConfig.getWaterConsumptionRate() * totalCropArea * solsInOrbit;
			demand = WASTE_WATER_VALUE;
		}

		return demand;
	}

	/**
	 * Gets the farming demand for the resource.
	 *
	 * @return demand (kg) for the resource.
	 */
	private double getCropDemand(GoodsManager owner, Settlement settlement) {
		double demand = 0D;

		if (settlement.getNumCitizens() == 0)
			return demand;

		HotMeal mainMeal = null;
		HotMeal sideMeal = null;

		// TODO Why does it scan all People but only take a single meal ?
		Iterator<Person> i = settlement.getAllAssociatedPeople().iterator();
		while (i.hasNext()) {
			Person p = i.next();
			String mainDish = p.getFavorite().getFavoriteMainDish();
			String sideDish = p.getFavorite().getFavoriteSideDish();
			mainMeal = getHotMeal(MealConfig.getMainDishList(), mainDish);
			sideMeal = getHotMeal(MealConfig.getSideDishList(), sideDish);
		}

		demand +=  getIngredientDemand(owner, getID(), mainMeal.getIngredientList());

		demand +=  getIngredientDemand(owner, getID(), sideMeal.getIngredientList());

		return demand;
	}

	private static double getIngredientDemand(GoodsManager owner, int resource, List<Ingredient> ingredients) {
		double demand = 0D;

		for(Ingredient it : ingredients) {
			AmountResource ar = ResourceUtil.findAmountResource(it.getAmountResourceID());
			if (ar.getGoodType() == GoodType.CROP) {
				String tissueName = it.getName() + Farming.TISSUE;

				if (it.getAmountResourceID() == resource) {
					// Tune demand with various factors
					demand += CROP_FACTOR *  owner.getCropFarmFactor();
				}

				else if (ResourceUtil.findIDbyAmountResourceName(tissueName.toLowerCase()) == resource) {
					// Tune demand with various factors
					demand += TISSUE_CULTURE_FACTOR *  owner.getCropFarmFactor();
				}
			}
		}
		return demand;
	}

	
	/**
	 * Gets an instance of the hot meal.
	 * 
	 * @param dishList
	 * @param dish
	 * @return
	 */
	private static HotMeal getHotMeal(List<HotMeal> dishList, String dish) {
		for(HotMeal hm : dishList) {
			if (hm.getMealName().equals(dish))
				return hm;
		}
		return null;
	}

	/**
	 * Gets the life support demand for an amount resource.
	 *
	 * @param resource the resource to check.
	 * @return demand (kg)
	 */
	private double getLifeSupportDemand(GoodsManager owner, Settlement settlement) {
		int resource = getID();
		if (ResourceUtil.isLifeSupport(resource)) {
			double amountNeededSol = 0;
			int numPeople = settlement.getNumCitizens();

			if (resource == ResourceUtil.oxygenID) {
				amountNeededSol = personConfig.getNominalO2ConsumptionRate() * OXYGEN_VALUE_MODIFIER;
			} else if (resource == ResourceUtil.waterID) {
				amountNeededSol = personConfig.getWaterConsumptionRate() *  WATER_VALUE_MODIFIER;
			} else if (resource == ResourceUtil.foodID) {
				amountNeededSol = personConfig.getFoodConsumptionRate() * FOOD_VALUE_MODIFIER;
			}

			return numPeople * amountNeededSol * owner.getTradeFactor() * LIFE_SUPPORT_FACTOR;

		} else
			return 0;
	}


    /**
	 * Limits the demand of life support resources.
	 * 
	 * @param demand
	 * @return
	 */
	private double capLifeSupportAmountDemand(double demand) {
        int resource = getID();

        // TODO can be calcualted at constructor time
		if (resource == ResourceUtil.foodID
				|| resource == ResourceUtil.oxygenID || resource == ResourceUtil.waterID 
				|| resource == ResourceUtil.hydrogenID || resource == ResourceUtil.methaneID)
			// Cap the resource at less than LIFE_SUPPORT_MAX
			return Math.min(LIFE_SUPPORT_MAX, demand);
		return demand;
	}

	/**
	 * Gets a particular mineral demand.
	 *
	 * @return
	 */
	private double getMineralDemand(GoodsManager owner, Settlement settlement) {
		double demand = 1;
		int resource= getID();

        if (resource == ResourceUtil.rockSaltID)
			return demand * SALT_VALUE_MODIFIER;
		else if (resource == ResourceUtil.epsomSaltID)
			return demand * SALT_VALUE_MODIFIER;
		else if (resource == ResourceUtil.soilID)
			return demand * settlement.getCropsNeedingTending() * SAND_VALUE_MODIFIER;
		else if (resource == ResourceUtil.concreteID) {
			double regolithVP = 1 + owner.getGoodValuePerItem(ResourceUtil.regolithID);
			double sandVP = 1 + owner.getGoodValuePerItem(ResourceUtil.sandID);
			// the demand for sand is dragged up or down by that of regolith
			// loses 5% by default
			return demand * (.75 * regolithVP + .2 * sandVP) / sandVP * CONCRETE_VALUE_MODIFIER;
		}
		else if (resource == ResourceUtil.sandID) {
			double regolithVP = 1 + owner.getGoodValuePerItem(ResourceUtil.regolithID);
			double sandVP = 1 + owner.getGoodValuePerItem(ResourceUtil.sandID);
			// the demand for sand is dragged up or down by that of regolith
			// loses 10% by default
			return demand * (.2 * regolithVP + .7 * sandVP) / sandVP * SAND_VALUE_MODIFIER;
		}
        else {
			double regolithVP = owner.getGoodValuePerItem(ResourceUtil.regolithID);
			double sandVP = 1 + owner.getGoodValuePerItem(ResourceUtil.sandID);

			for (int id : ResourceUtil.rockIDs) {
				if (resource == id) {
					double vp = owner.getGoodValuePerItem(id);
					return demand * (.2 * regolithVP + .9 * vp) / vp * ROCK_VALUE;
				}
			}

			for (int id : ResourceUtil.mineralConcIDs) {
				if (resource == id) {
					double vp = owner.getGoodValuePerItem(id);
					return demand * (.2 * regolithVP + .9 * vp) / vp * MINERAL_VALUE;
				}
			}

			for (int id : ResourceUtil.oreDepositIDs) {
				if (resource == id) {
					double vp = owner.getGoodValuePerItem(id);
					// loses 10% by default
					return demand * (.3 * regolithVP + .6 * vp) / vp * ORE_VALUE;
				}
			}

			if (resource == ResourceUtil.regolithBID || resource == ResourceUtil.regolithCID
					|| resource == ResourceUtil.regolithDID) {
				double vp = owner.getGoodValuePerItem(resource);
				return demand * (.3 * regolithVP + .6 * vp) / vp;
			}

			// Checks if this resource is a ROCK type
			GoodType type = getGoodType();
			if (type != null && type == GoodType.ROCK) {
				double vp = owner.getGoodValuePerItem(resource);

				if (resource == METEORITE_ID)
					return demand * (.4 * regolithVP + .5 * vp) / vp * METEORITE_MODIFIER;
				else
					return demand * (.2 * sandVP + .7 * vp) / vp * ROCK_MODIFIER;
			}

		}

		return demand;
	}

	/**
	 * Adjusts the sink cost for various waste resources.
	 *
	 * TODO This method doesn;t make sense; it will always return zero unless WASTE type
	 * @return demand (kg)
	 */
	private double getWasteDisposalSinkCost() {
		int resource = getID();
		double demand = 0;
		if (resource == ResourceUtil.greyWaterID || resource == ResourceUtil.blackWaterID) {
			return demand * WASTE_WATER_VALUE;
		}

		if (resource == ResourceUtil.leavesID) {
			return demand * LEAVES_FACTOR;
		}

		if (resource == ResourceUtil.soilID) {
			return demand / 2D;
		}

		if (resource == ResourceUtil.coID) {
			return demand / 2D;
		}

		if (resource == ResourceUtil.foodWasteID) {
			return demand / 2D;
		}

		if (resource == ResourceUtil.cropWasteID) {
			return demand / 2D;
		}

		if (resource == ResourceUtil.compostID) {
			return demand / 2D * USEFUL_WASTE_VALUE;
		}

		if (resource == ResourceUtil.co2ID) {
			return demand / 2D;
		}

		else {
			if (getGoodType() == GoodType.WASTE) {
				return WASTE_VALUE;
			}

		}

		return demand;
	}

	/**
	 * Gets the potable water usage demand for an amount resource.
	 *
	 * @param resource the resource to check.
	 * @return demand (kg)
	 */
	private double getPotableWaterUsageDemand(GoodsManager owner, Settlement settlement) {
		double demand = 0;
		if (getID() == ResourceUtil.waterID) {
			// Add the awareness of the water ration level in adjusting the water demand
			double waterRationLevel = settlement.getWaterRationLevel();
			double amountNeededSol = personConfig.getWaterUsageRate();
			int numPeople = settlement.getNumCitizens();
			demand = numPeople * amountNeededSol *  WATER_VALUE_MODIFIER * owner.getTradeFactor() * (1 + waterRationLevel);
		}

		return demand;
	}

	/**
	 * Computes the ice demand.
	 *
	 * @param owner Owner of Supply Demand tables
	 * @return demand (kg)
	 */
	private double computeIceProjectedDemand(GoodsManager owner, Settlement settlement) {
        int resource = getID();
		if (resource == ResourceUtil.iceID) {
			double ice = 1 + owner.getGoodValuePerItem(resource);
			double water = 1 + owner.getGoodValuePerItem(ResourceUtil.waterID);
			// Use the water's VP and existing iceSupply to compute the ice demand
			return Math.max(1, settlement.getNumCitizens()) * (.8 * water + .2 * ice) / ice
					* ICE_VALUE_MODIFIER * settlement.getWaterRationLevel();
		}

		return 0;
	}

	/**
	 * Computes the regolith demand.
	 *
	 * @return demand (kg)
	 */
	private double computeRegolithProjectedDemand(GoodsManager owner, Settlement settlement) {
		double demand = 0;
		int resource = getID();
		if ((resource == ResourceUtil.regolithID)
			|| (resource == ResourceUtil.regolithBID)
			|| (resource ==  ResourceUtil.regolithCID)
			|| (resource ==  ResourceUtil.regolithDID)) {
			double sand = 1 + owner.getGoodValuePerItem(ResourceUtil.sandID);
			double concrete = 1 + owner.getGoodValuePerItem(ResourceUtil.concreteID);
			double cement = 1 + owner.getGoodValuePerItem(ResourceUtil.cementID);
			double regolith = 1 + owner.getGoodValuePerItem(getID());
			// Limit the minimum value of regolith projected demand
			demand = 1 + Math.min(48, settlement.getNumCitizens()) *
					(.1 * cement + .2 * concrete + .5 * regolith + .2 * sand) / regolith
					* REGOLITH_VALUE_MODIFIER;
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
		if (getID() == ResourceUtil.toiletTissueID) {
			double amountNeededSol = LivingAccommodations.TOILET_WASTE_PERSON_SOL;
			int numPeople = settlement.getIndoorPeopleCount();
			return numPeople * amountNeededSol;
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
		if (getID() == ResourceUtil.methaneID) {
			for(Vehicle v: settlement.getAllAssociatedVehicles()) {
				double fuelDemand = v.getAmountResourceCapacity(getID());
				demand += fuelDemand * owner.getTransportationFactor() * VEHICLE_FUEL_FACTOR;
			}
		}

		return demand;
	}

}
