/*
 * Mars Simulation Project
 * Good.java
 * @date 2025-08-25
 * @author Scott Davis
 */
package com.mars_sim.core.goods;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.config.BuildingConfig;
import com.mars_sim.core.building.construction.ConstructionStageInfo;
import com.mars_sim.core.building.construction.ConstructionUtil;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.farming.CropConfig;
import com.mars_sim.core.food.FoodProductionUtil;
import com.mars_sim.core.manufacture.ManufactureUtil;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.person.ai.mission.MissionManager;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.process.ProcessInfo;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleConfig;

/**
 * A meta class describing an economic good in the simulation.
 */
public abstract class Good implements Serializable, Comparable<Good> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	static final double LOWEST_PROJECTED_VALUE = 0.01;
	static final int HIGHEST_PROJECTED_VALUE = 10_000;
	
	private static final double LABOR_FACTOR = 150D ;
	private static final double PROCESS_TIME_FACTOR = 500D;
	private static final double POWER_FACTOR = 1D;
	private static final double SKILL_FACTOR = 1D;
	private static final double TECH_FACTOR = 2D;

	protected static MissionManager missionManager;
    protected static VehicleConfig vehicleConfig;
	protected static PersonConfig personConfig;
	protected static CropConfig cropConfig;
	protected static BuildingConfig buildingConfig;
	protected static UnitManager unitManager;

	// Data members
	private String name;

	private int id;

	private double baseCost = -1;


	private double costModifier = -1;
	/** The cost for this good. */
	private double cost = -1;
	/** The price for this good. */
	private double price = -1;

	/**
	 * Constructor with object.
	 *
	 * @param name     the name of the good.
	 * @param id   the good's id.
	 */
	protected Good(String name, int id) {
		this.name = name;
		this.id = id;
	}

	/**
     * Gets the flattened demand of this resource.
     * Note: to be overridden in subclass.
     * 
     * @return
     */
    public double getFlattenDemand() {
		return 1.0;
	}
    
    /**
     * Gets the projected demand of this resource.
     * Note: to be overridden in subclass.
     * 
     * @return
     */
    public double getProjectedDemand() {
    	return 1.0;
    }
	
    /**
     * Gets the trade demand of this resource.
     * Note: to be overridden in subclass.
     * 
     * @return
     */
    public double getTradeDemand() {
    	return 0.0;
    }
	
    /**
     * Gets the repair demand of this resource.
     * 
     * @return
     */
    public double getRepairDemand() {
    	return 0.0;
    }

	/**
	 * Gets the good's name.
	 *
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the good's id.
	 * 
	 * @return
	 */
	public int getID() {
		return id;
	}

	/**
	 * Gets the good's category enum.
	 
	 * @return category.
	 */
	public abstract GoodCategory getCategory();


	public double getModifier() {
		return costModifier;
	}

	/**
	 * Calculates the two costs of each good.
	 */
	public void computeAllCosts() {

		// Compute the base output cost
		if (baseCost== -1) {
			computeBaseOutputCost();
		}

		// Compute the adjusted output cost
		computeAdjustedCost();
	}
	
	/**
	 * Gets the cost of output.
	 */
	public double getCostOutput() {
		if (cost == -1)
			computeAdjustedCost();
		return cost;
	}

	/**
	 * Calculates the modified cost of output.
	 */
	public double computeAdjustedCost() {
		// First compute the modifier
		if (costModifier == -1) {
			costModifier = computeCostModifier();
		}
		
		// Then compute the total cost
		cost = (0.01 + costModifier) * baseCost;
		
		return cost;
	}

	public double getPrice() {
		return price;
	}
	
	public void setPrice(double value) {
		price = value;
	}
	
	/**
	 * Computes the cost modifier for calculating output cost.
	 * 
	 * @return
	 */
	protected abstract double computeCostModifier();
	
	private record OutputCosts(double labor, double power, double process, double skill, double tech) {}

	/**
	 * Calculates the output costs for a list of processes.
	 * 
	 * @param processInfos
	 * @return
	 */
	private OutputCosts computeOutputCosts(List<? extends ProcessInfo> processInfos) {

		if (processInfos.isEmpty()) {
			return new OutputCosts(0, 0, 0, 0, 0);
		}

		double labor0Out = 0;
		double power0Out = 0;
		double process0Out = 0;
		double skill0Out = 0;
		double tech0Out = 0;

		for (ProcessInfo i: processInfos) {
			double goodsProduced = 0;
			for (var j: i.getOutputItemsByName(name)) {
				goodsProduced += j.getAmount();
			}

			if (goodsProduced > 0) {
				// The consumption figures are balanced to the amount produced per unit
				labor0Out 	 += i.getWorkTimeRequired()/goodsProduced;
				power0Out 	 += i.getPowerRequired()/goodsProduced;
				process0Out += i.getProcessTimeRequired()/goodsProduced;

				skill0Out 	 += i.getSkillLevelRequired();
				tech0Out 	 += i.getTechLevelRequired();
			}
		}	

		var numProcesses = processInfos.size();
		return new OutputCosts(labor0Out / numProcesses, power0Out / numProcesses, process0Out / numProcesses,
						skill0Out / numProcesses, tech0Out / numProcesses);
	}

	/**
	 * Computes the base cost of each good from manufacturing and food production. THis is based on the process definitions
	 * and never changes once created.
	 */
	private void computeBaseOutputCost() {

		var manuResults = computeOutputCosts(ManufactureUtil.getManufactureProcessesWithGivenOutput(name));
		var foodResults = computeOutputCosts(FoodProductionUtil.getFoodProductionProcessesWithGivenOutput(name));

		var laborTime = getCombinedValue(manuResults.labor, foodResults.labor);
		var power = getCombinedValue(manuResults.power, foodResults.power);
		var processTime = getCombinedValue(manuResults.process, foodResults.process);
		var skill = getCombinedValue(manuResults.skill, foodResults.skill);
		var tech = getCombinedValue(manuResults.tech, foodResults.tech);

		baseCost = 
			1 + laborTime / LABOR_FACTOR
			+ processTime / PROCESS_TIME_FACTOR
			+ power / POWER_FACTOR
			+ skill / SKILL_FACTOR
			+ tech / TECH_FACTOR;
	}

	/**
	 * Combine 2 values. If either is zero then the otehr is returned. If both are
	 * non-zero then the average is returned.
	 * @param value0
	 * @param value1
	 * @return
	 */
	private static double getCombinedValue(double value0, double value1) {
		if (value0 == 0)
			return value1;
		else if (value1 == 0)
			return value0;
		else
			return (value0 + value1)/2D;
	}

	/**
	 * Gets the amount of this good being produced at the settlement by ongoing
	 * manufacturing processes.
	 *
	 * @param settlement Place producing the Good
	 * @return amount (kg for amount resources, number for parts, equipment, and
	 *         vehicles).
	 */
	protected double getManufacturingProcessOutput(Settlement settlement) {

		double result = 0D;

		for(Building b : settlement.getBuildingManager().getBuildingSet(FunctionType.MANUFACTURE)) {
			// Go through each ongoing active manufacturing process.
			for(var process : b.getManufacture().getProcesses()) {
				for(var item : process.getInfo().getOutputItemsByName(name)) {
					result += item.getAmount();
				}
			}
		}

		return result;
	}

	
	/**
	 * Checks if a building construction stage can be constructed at the local
	 * settlement.
	 *
	 * @param buildingStage the building construction stage info.
	 * @return true if building can be constructed.
	 */
	protected static boolean isLocallyConstructable(Settlement settlement, ConstructionStageInfo buildingStage) {

		if (buildingStage.isConstructable()) {
			ConstructionStageInfo frameStage = buildingStage.getPrerequisiteStage();
			if (frameStage != null) {
				ConstructionStageInfo foundationStage = frameStage.getPrerequisiteStage();
				if (foundationStage != null) {
					if (frameStage.isConstructable() && foundationStage.isConstructable()) {
						return true;
					} else {
						// Check if any existing buildings have same frame stage and can be refit or
						// refurbished
						// into new building.
						for (String type: buildingConfig.getBuildingTypes()) {
							ConstructionStageInfo tempBuildingStage = ConstructionUtil
									.getConstructionStageInfo(type);
							if (tempBuildingStage != null) {
								ConstructionStageInfo tempFrameStage = tempBuildingStage.getPrerequisiteStage();
								if (frameStage.equals(tempFrameStage)) {
									return true;
								}
							}
						}
					}
				}
			}
		}

		return false;
	}



	/**
	 * Gets all resource amounts required to build a stage including all pre-stages.
	 *
	 * @param stage the stage.
	 * @return map of resources and their amounts (kg).
	 */
	protected Map<Integer, Double> getAllPrerequisiteConstructionResources(ConstructionStageInfo stage) {

		// Start with all resources required to build stage.
		Map<Integer, Double> result = new HashMap<>(stage.getResources());

		// Add all resources required to build first prestage, if any.
		ConstructionStageInfo preStage1 = stage.getPrerequisiteStage();
		if (preStage1 != null) {
			preStage1.getResources().forEach((k,v) -> result.merge(k, v, Double::sum));

			// Add all resources required to build second prestage, if any.
			ConstructionStageInfo preStage2 = preStage1.getPrerequisiteStage();
			if (preStage2 != null) {
				preStage2.getResources().forEach((k,v) -> result.merge(k, v, Double::sum));
			}
		}

		return result;
	}

	/**
	 * Gets a map of all parts required to build a stage including all pre-stages.
	 *
	 * @param stage the stage.
	 * @return map of parts and their numbers.
	 */
	protected static Map<Integer, Integer> getAllPrerequisiteConstructionParts(ConstructionStageInfo stage) {

		// Start with all parts required to build stage.
		Map<Integer, Integer> result = new HashMap<>(stage.getParts());

		// Add parts from first prestage, if any.
		ConstructionStageInfo preStage1 = stage.getPrerequisiteStage();
		if (preStage1 != null) {
			preStage1.getParts().forEach((k,v) -> result.merge(k, v, Integer::sum));

			// Add parts from second pre-stage, if any.
			ConstructionStageInfo preStage2 = preStage1.getPrerequisiteStage();
			if (preStage2 != null) {
				preStage2.getParts().forEach((k,v) -> result.merge(k, v, Integer::sum));
			}
		}

		return result;
	}

	/**
	 * Gets a string representation of the good.
	 *
	 * @return string.
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * Gets the hash code value.
	 *
	 * @return hash code
	 */
	@Override
	public int hashCode() {
		return id % 64;
	}

	/**
	 * Compares this object with the specified object for order.
	 *
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	@Override
	public int compareTo(Good o) {
		return name.compareTo(o.name);
	}

	/**
	 * Is this object the same as another object ?
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Good g = (Good) obj;
		return this.id == g.getID();
	}
    
	/**
     * Gets the mass per item for a good.
     *
     * @return mass (kg) per item (or 1kg for amount resources).
     * @throws Exception if error getting mass per item.
     */
    public abstract double getMassPerItem();

	/**
     * Gets the good type
     * 
     * @return
     */
	public abstract GoodType getGoodType();

	/**
	 * Gets the number of this good being in use or being produced at this moment at
	 * the settlement.
	 *
	 * @param settlement Settlement to check
	 * @return the number of the good (or amount (kg) if amount resource good).
	 */
	public abstract double getNumberForSettlement(Settlement settlement);

	/**
     * Gets a stream of all Vehicles on Missions from the target Settlement.
     * 
     * @param settlement Settlement being checked
     */
    protected Stream<Vehicle> getVehiclesOnMissions(Settlement settlement) {
        return missionManager.getMissionsForSettlement(settlement).stream()
                .filter(VehicleMission.class::isInstance)
                .map(vm -> ((VehicleMission) vm).getVehicle())
                .filter(Objects::nonNull);
    }
    
    /**
     * Gets a stream of Person on EVA at the Settlement.
     * 
     * @param settlement
     * @return
     */
    protected Stream<Person> getPersonOnEVA(Settlement settlement) {
        return  settlement.getAllAssociatedPeople().stream()
			              .filter(Person::isOutside);
    }

	/**
	 * Calculates the price for this Good at a settlement with a specific Value Point.
	 * 
	 * @param settlement Get the price at
	 * @param value Value Point for the good
	 */
    abstract double calculatePrice(Settlement settlement, double value);

	/**
	 * Gets the default initial demand value for this Good.
	 */
    abstract double getDefaultDemandValue();

	/**
	 * Gets the default initial supply value for this Good.
	 */
    abstract double getDefaultSupplyValue();

	/**
	 * Refreshes the Supply and Demand values associated with this Good for a specific Settlement.
	 * 
	 * @param owner Owner of the Supply/Demand values.
	 */
	abstract void refreshSupplyDemandScore(GoodsManager owner);

	/**
	 * Initialises the configs.
	 * 
	 * @param sc
	 * @param m
	 */
	static void initializeInstances(SimulationConfig sc, MissionManager m, UnitManager u) {
		missionManager = m;
		unitManager = u;
		vehicleConfig = sc.getVehicleConfiguration();
		personConfig = sc.getPersonConfig();
		cropConfig = sc.getCropConfiguration();
		buildingConfig = sc.getBuildingConfiguration();
	}
}
