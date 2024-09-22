/*
 * Mars Simulation Project
 * Good.java
 * @date 2024-06-29
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
import com.mars_sim.core.Unit;
import com.mars_sim.core.food.FoodProductionProcessInfo;
import com.mars_sim.core.food.FoodProductionUtil;
import com.mars_sim.core.manufacture.ManufactureProcess;
import com.mars_sim.core.manufacture.ManufactureProcessInfo;
import com.mars_sim.core.manufacture.ManufactureUtil;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.person.ai.mission.MissionManager;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.process.ProcessItem;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.ItemType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.farming.CropConfig;
import com.mars_sim.core.structure.construction.ConstructionStageInfo;
import com.mars_sim.core.structure.construction.ConstructionUtil;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleConfig;

/**
 * A meta class describing an economic good in the simulation.
 */
public abstract class Good implements Serializable, Comparable<Good> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
//	private static final int LOWEST_VALUE_TO_DEFLATE = 1000;
//	private static final int HIGHEST_VALUE_TO_INFLATE = 1;
	static final int HIGHEST_PROJECTED_VALUE = 20_000;
	
	private static final double LABOR_FACTOR = 150D ;
	private static final double PROCESS_TIME_FACTOR = 500D;
	private static final double POWER_FACTOR = 1D;
	private static final double SKILL_FACTOR = 1D;
	private static final double TECH_FACTOR = 2D;

	protected static MissionManager missionManager;
    protected static VehicleConfig vehicleConfig;
	protected static PersonConfig personConfig;
	protected static CropConfig cropConfig;


	// Data members
	private String name;

	private int id;
	private int count0Out;
	private int count1Out;

	private double laborTime;
	private double power;
	private double processTime;
	private double skill;
	private double tech;

	private double costModifier = -1;
	/** The adjusted cost output for this good. */
	private double adjustedCostOutput = -1;

	private static List<ManufactureProcessInfo> manufactureProcessInfos;
	private static List<FoodProductionProcessInfo> foodProductionProcessInfos;

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
		return 1;
	}
    
    /**
     * Gets the projected demand of this resource.
     * Note: to be overridden in subclass.
     * 
     * @return
     */
    public double getProjectedDemand() {
    	return 1;
    }
	
    /**
     * Gets the trade demand of this resource.
     * Note: to be overridden in subclass.
     * 
     * @return
     */
    public double getTradeDemand() {
    	return 0;
    }
	
    /**
     * Gets the repair demand of this resource.
     * 
     * @return
     */
    public double getRepairDemand() {
    	return 0;
    }
	
	/**
	 * Calculates the cost of each good.
	 */
	public void computeCost() {
		manufactureProcessInfos = ManufactureUtil.getManufactureProcessesWithGivenOutput(name);
		foodProductionProcessInfos = FoodProductionUtil.getFoodProductionProcessesWithGivenOutput(name);

		// Compute the base output cost
		computeBaseOutputCost();
		// Compute the adjusted output cost
		computeAdjustedOutputCost();
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
	 *
	 * @return category.
	 */
	public abstract GoodCategory getCategory();

	public double getlaborTime() {
		return laborTime;
	}

	public double getPower() {
		return power;
	}

	public double getProcessTime() {
		return processTime;
	}

	public double getSkill() {
		return skill;
	}

	public double getTech() {
		return tech;
	}

	public double getModifier() {
		return costModifier;
	}

	public double getCount0() {
		return count0Out;
	}

	public double getCount1() {
		return count1Out;
	}

	/**
	 * Gets the cost of output.
	 */
	public double getCostOutput() {
		if (adjustedCostOutput == -1)
			computeAdjustedOutputCost();
		return adjustedCostOutput;
	}

	/**
	 * Calculates the modified cost of output.
	 */
	public void computeAdjustedOutputCost() {
		// First compute the modifier
		if (costModifier == -1) {
			costModifier = computeCostModifier();
			// Then compute the total cost
			adjustedCostOutput = (0.01 + costModifier) * (
					1 + getlaborTime() / LABOR_FACTOR
					+ getProcessTime() / PROCESS_TIME_FACTOR
					+ getPower() / POWER_FACTOR
					+ getSkill() / SKILL_FACTOR
					+ getTech() / TECH_FACTOR);
		}
	}

	/**
	 * Computes the cost modifier for calculating output cost.
	 * 
	 * @return
	 */
	protected abstract double computeCostModifier();
	
//	/**
//	 * Adjusts the market value of this good.
//	 */
//	public synchronized void adjustMarketGoodValue() {
//		// Deflate the value by 5%
//		if (marketGoodValue > LOWEST_VALUE_TO_DEFLATE)
//			marketGoodValue = .95 * marketGoodValue;
//
//		// Inflate the value by 5%
//		else if (marketGoodValue < HIGHEST_VALUE_TO_INFLATE)
//			marketGoodValue = 1.05 * marketGoodValue;
//	}

//	/**
//	 * Adjusts the market demand of this good.
//	 */
//	public synchronized void adjustMarketDemand() {
//		// Deflate the demand by 5%
//		if (marketDemand > LOWEST_VALUE_TO_DEFLATE)
//			marketDemand = .95 * marketDemand;
//
//		// Inflate the value by 5%
//		else if (marketDemand < HIGHEST_VALUE_TO_INFLATE)
//			marketDemand = 1.05 * marketDemand;
//	}
	
	/**
	 * Computes the base cost of each good from manufacturing and food production
	 */
	public void computeBaseOutputCost() {
		double labor0Out = 0;
		double power0Out = 0;
		double process0Out = 0;
		double skill0Out = 0;
		double tech0Out = 0;

		if (manufactureProcessInfos != null || !manufactureProcessInfos.isEmpty()) {

			double goodAmount0Out = 0;
			double otherAmount0Out = 0;
			double goodWeight0Out = 1;
			double otherWeight0Out = 1;
			int numProcesses = manufactureProcessInfos.size();

			for (ManufactureProcessInfo i: manufactureProcessInfos) {

				var items = i.getOutputItemsByName(name);

				for (var j: items) {
					String goodName = j.getName();
					if (goodName.equalsIgnoreCase(name)) {
						goodAmount0Out += j.getAmount();

						if (ItemType.PART == j.getType())
							goodWeight0Out += ItemResourceUtil.findItemResource(name).getMassPerItem();
					}
					else {
						otherAmount0Out += j.getAmount();

						if (ItemType.PART == j.getType())
							otherWeight0Out += ItemResourceUtil.findItemResource(name).getMassPerItem();
					}
				}

				labor0Out 	 += i.getWorkTimeRequired();
				power0Out 	 += i.getPowerRequired();
				process0Out += i.getProcessTimeRequired();
				skill0Out 	 += i.getSkillLevelRequired();
				tech0Out 	 += i.getTechLevelRequired();
				count0Out++;

				if (count0Out != 0) {
					double fraction = 1 / (goodAmount0Out * goodWeight0Out + otherAmount0Out * otherWeight0Out);
					labor0Out 	 = labor0Out * fraction;
					power0Out 	 = power0Out * fraction;
					process0Out = process0Out * fraction;
					skill0Out	 = skill0Out * fraction;
					tech0Out 	 = tech0Out * fraction;
				}
			}

			if (numProcesses != 0) {
				labor0Out 	 = labor0Out / numProcesses;
				power0Out 	 = power0Out / numProcesses;
				process0Out = process0Out / numProcesses;
				skill0Out	 = skill0Out / numProcesses;
				tech0Out 	 = tech0Out / numProcesses;
			}
		}

		double labor1Out = 0;
		double power1Out = 0;
		double process1Out = 0;
		double skill1Out = 0;
		double tech1Out = 0;

		if (foodProductionProcessInfos != null || !foodProductionProcessInfos.isEmpty()) {

			double goodAmount1Out = 0;
			double otherAmount1Out = 0;
			double goodWeight1Out = 1;
			double otherWeight1Out = 1;
			int numProcesses = foodProductionProcessInfos.size();

			for (FoodProductionProcessInfo i: foodProductionProcessInfos) {
				List<ProcessItem> items = i.getOutputItemsByName(name);
				for (ProcessItem j: items) {
					String goodName = j.getName();
					if (goodName.equalsIgnoreCase(name)) {
						goodAmount1Out += j.getAmount();

						if (ItemType.PART == j.getType())
							goodWeight1Out += ItemResourceUtil.findItemResource(name).getMassPerItem();
					}
					else {
						otherAmount1Out += j.getAmount();

						if (ItemType.PART == j.getType())
							otherWeight1Out += ItemResourceUtil.findItemResource(name).getMassPerItem();
					}
				}

				labor1Out 	 += i.getWorkTimeRequired();
				power1Out 	 += i.getPowerRequired();
				process1Out += i.getProcessTimeRequired();
				skill1Out 	 += i.getSkillLevelRequired();
				tech1Out 	 += i.getTechLevelRequired();
				count1Out++;
			}

			if (count1Out != 0) {
				double fraction = 1 / (goodAmount1Out * goodWeight1Out + otherAmount1Out * otherWeight1Out);
				labor1Out 	 = labor1Out * fraction;
				power1Out 	 = power1Out * fraction;
				process1Out = process1Out * fraction;
				skill1Out	 = skill1Out * fraction;
				tech1Out 	 = tech1Out * fraction;
			}

			if (numProcesses != 0) {
				labor1Out 	 = labor1Out / numProcesses;
				power1Out 	 = power1Out / numProcesses;
				process1Out = process1Out / numProcesses;
				skill1Out	 = skill1Out / numProcesses;
				tech1Out 	 = tech1Out / numProcesses;
			}
		}

		if (labor0Out == 0)
			laborTime = labor1Out;
		else if (labor1Out == 0)
			laborTime = labor0Out;
		else
			laborTime = (labor0Out + labor1Out)/2D;

		if (power0Out == 0)
			power = power1Out;
		else if (power1Out == 0)
			power = power0Out;
		else
			power = (power0Out + power1Out)/2D;

		if (process0Out == 0)
			processTime = process1Out;
		else if (process1Out == 0)
			processTime = process0Out;
		else
			processTime = (process0Out + process1Out)/2D;

		if (skill0Out == 0)
			skill = skill1Out;
		else if (skill1Out == 0)
			skill = skill0Out;
		else
			skill = (skill0Out + skill1Out)/2D;

		if (tech0Out == 0)
			tech = tech1Out;
		else if (tech1Out == 0)
			tech = tech0Out;
		else
			tech = (tech0Out + tech1Out)/2D;

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
			for(ManufactureProcess process : b.getManufacture().getProcesses()) {
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
			ConstructionStageInfo frameStage = ConstructionUtil.getPrerequisiteStage(buildingStage);
			if (frameStage != null) {
				ConstructionStageInfo foundationStage = ConstructionUtil.getPrerequisiteStage(frameStage);
				if (foundationStage != null) {
					if (frameStage.isConstructable() && foundationStage.isConstructable()) {
						return true;
					} else {
						// Check if any existing buildings have same frame stage and can be refit or
						// refurbished
						// into new building.
						for(var b : settlement.getBuildingManager().getBuildingSet()) {
							ConstructionStageInfo tempBuildingStage = ConstructionUtil
									.getConstructionStageInfo(b.getBuildingType());
							if (tempBuildingStage != null) {
								ConstructionStageInfo tempFrameStage = ConstructionUtil
										.getPrerequisiteStage(tempBuildingStage);
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
		ConstructionStageInfo preStage1 = ConstructionUtil.getPrerequisiteStage(stage);
		if (preStage1 != null) {
			for(var e : preStage1.getResources().entrySet()) {
				Integer resource = e.getKey();
				double amount = e.getValue();
				if (result.containsKey(resource)) {
					double totalAmount = result.get(resource) + amount;
					result.put(resource, totalAmount);
				} else {
					result.put(resource, amount);
				}
			}

			// Add all resources required to build second prestage, if any.
			ConstructionStageInfo preStage2 = ConstructionUtil.getPrerequisiteStage(preStage1);
			if (preStage2 != null) {
				for(var e : preStage2.getResources().entrySet()) {
					Integer resource = e.getKey();
					double amount = e.getValue();
					if (result.containsKey(resource)) {
						double totalAmount = result.get(resource) + amount;
						result.put(resource, totalAmount);
					} else {
						result.put(resource, amount);
					}
				}
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
		ConstructionStageInfo preStage1 = ConstructionUtil.getPrerequisiteStage(stage);
		if (preStage1 != null) {
			for(var e : preStage1.getParts().entrySet()) {
				Integer part = e.getKey();
				int number = e.getValue();
				if (result.containsKey(part)) {
					int totalNumber = result.get(part) + number;
					result.put(part, totalNumber);
				} else {
					result.put(part, number);
				}
			}

			// Add parts from second pre-stage, if any.
			ConstructionStageInfo preStage2 = ConstructionUtil.getPrerequisiteStage(preStage1);
			if (preStage2 != null) {
				for(var e : preStage2.getParts().entrySet()) {
					Integer part = e.getKey();
					int number = e.getValue();
					if (result.containsKey(part)) {
						int totalNumber = result.get(part) + number;
						result.put(part, totalNumber);
					} else {
						result.put(part, number);
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets a string representation of the good.
	 *
	 * @return string.
	 */
	public String toString() {
		return name;
	}

	/**
	 * Gets the hash code value.
	 *
	 * @return hash code
	 */
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
	public int compareTo(Good o) {
		return name.compareTo(o.name);
	}

	/**
	 * Is this object the same as another object ?
	 */
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
			              .filter(Unit::isOutside);
    }

	/**
	 * Gets the price for this Good at a settlement with a specific Value Point.
	 * 
	 * @param settlement Get the price at
	 * @param value Value Point for the good
	 */
    abstract double getPrice(Settlement settlement, double value);

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
	abstract void refreshSupplyDemandValue(GoodsManager owner);

	/**
	 * Initialises the configs.
	 * 
	 * @param sc
	 * @param m
	 */
	static void initializeInstances(SimulationConfig sc, MissionManager m) {
		missionManager = m;

		vehicleConfig = sc.getVehicleConfiguration();
		personConfig = sc.getPersonConfig();
		cropConfig = sc.getCropConfiguration();
	}
}
