/**
 * Mars Simulation Project
 * SalvageValues.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.core.building.construction;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.mars_sim.core.UnitManager;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.LifeSupport;
import com.mars_sim.core.building.function.LivingAccommodation;
import com.mars_sim.core.building.function.RoboticStation;
import com.mars_sim.core.building.function.VehicleGarage;
import com.mars_sim.core.goods.GoodsManager;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.TaskManager;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.ai.task.BotTaskManager;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MasterClock;

/**
 * Calculates values for salvaging buildings at a settlement.
 */
public class SalvageValues
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Static members
	/** Value for salvaging a foundation. */
	private static final double FOUNDATION_SALVAGE_VALUE = 10D;

	// Data members
	private Settlement settlement;
	private Map<Integer, Double> settlementSalvageValueCache;
	private MarsTime settlementSalvageValueCacheTime;

	private static UnitManager unitManager;
	
	private static MasterClock clock;

	/**
	 * Constructor.
	 * @param settlement the settlement
	 */
	public SalvageValues(Settlement settlement) {
		this.settlement = settlement;
	}
	
	/**
	 * Clears the salvage value cache.
	 */
	public void clearCache() {
	    if (settlementSalvageValueCache != null) {
	        settlementSalvageValueCache.clear();
	        settlementSalvageValueCacheTime = null;
	    }
	}

	/**
	 * Gets the overall profit for construction salvage at the settlement.
	 * @return profit (VP)
	 * @throws Exception if error determining profit.
	 */
	public double getSettlementSalvageProfit() {
		return getSettlementSalvageProfit(Integer.MAX_VALUE);
	}

	/**
	 * Gets the overall profit for construction salvage at the settlement.
	 * @param constructionSkill the architect's construction skill.
	 * @return profit (VP)
	 * @throws Exception if error determining profit.
	 */
	public double getSettlementSalvageProfit(int constructionSkill) {
		MarsTime now = clock.getMarsTime();
		if ((settlementSalvageValueCacheTime == null) || 
				(now.getTimeDiff(settlementSalvageValueCacheTime) > 1000D)) {
			if (settlementSalvageValueCache == null) 
				settlementSalvageValueCache = new HashMap<>();
			settlementSalvageValueCache.clear();
			settlementSalvageValueCacheTime = now;
		}

		if (!settlementSalvageValueCache.containsKey(constructionSkill)) {
			double profit = 0D;

			double existingSitesProfit = getAllSalvageSitesProfit(constructionSkill);
			if (existingSitesProfit > profit) profit = existingSitesProfit;

			double newSiteProfit = getNewSalvageSiteProfit(constructionSkill);
			if (newSiteProfit > profit) profit = newSiteProfit;

			settlementSalvageValueCache.put(constructionSkill, profit);
		}

		return settlementSalvageValueCache.get(constructionSkill);
	}

	/**
	 * Gets the overall salvage profit of all existing construction sites at a settlement
	 * that can be worked on with a given construction skill.
	 * @param constructionSkill the architect's construction skill.
	 * @return profit (VP)
	 * @throws Exception if error determining profit.
	 */
	public double getAllSalvageSitesProfit(int constructionSkill) {

		double result = 0D;

		ConstructionManager manager = settlement.getConstructionManager();
		Iterator<ConstructionSite> i = manager.getConstructionSitesNeedingMission(false).iterator();
		while (i.hasNext()) {
			double profit = getSalvageSiteProfit(i.next(), constructionSkill);
			if (profit > result) result = profit;
		}

		return result;
	}

	/**
	 * Gets the salvage profit of an existing construction site at a settlement.
	 * @param site the construction site.
	 * @return profit (VP)
	 * @throws Exception if error determining profit.
	 */
	public double getSalvageSiteProfit(ConstructionSite site) {
		return getSalvageSiteProfit(site, Integer.MAX_VALUE);
	}

	/**
	 * Gets the salvage profit of an existing construction site at a settlement.
	 * @param site the construction site.
	 * @param constructionSkill the architect's construction skill.
	 * @return profit (VP)
	 * @throws Exception if error determining profit.
	 */
	public double getSalvageSiteProfit(ConstructionSite site, int constructionSkill) {

		double result = 0D;

		if (site.isConstruction()) {
			ConstructionStage stage = site.getCurrentConstructionStage();
			ConstructionStageInfo prerequisiteStage = stage.getInfo().getPrerequisiteStage();

			if (site.hasUnfinishedStage()) {
				if (stage.getInfo().getArchitectConstructionSkill() <= constructionSkill) {
					if (prerequisiteStage != null) result = getSalvageStageValue(prerequisiteStage);
					else {
						// If stage is foundation and has no prerequisite stage, use foundation salvage value.
						result = FOUNDATION_SALVAGE_VALUE;
					}

					// Determine estimated salvaged parts value.
					result += getEstimatedSalvagedPartsValue(stage.getInfo(), constructionSkill, 1D);
				}
			}
			else {
				double currentValue = getSalvageStageValue(stage.getInfo());

				double preValue = 0D;
				if (prerequisiteStage != null) preValue = getSalvageStageValue(prerequisiteStage);
				else {
					// If stage is foundation and has no prerequisite stage, use foundation salvage value.
					preValue = FOUNDATION_SALVAGE_VALUE;
				}

				// Determine estimated construction parts returned.
				double partsValue = getEstimatedSalvagedPartsValue(stage.getInfo(), constructionSkill, 1D);

				result = preValue - currentValue + partsValue;
			}
		}

		return result;
	}

	/**
	 * Gets the profit of creating a new salvage construction site at a settlement.
	 * @param constructionSkill the architect's construction skill.
	 * @return profit (VP)
	 * @throws Exception if error determining profit.
	 */
	public double getNewSalvageSiteProfit(int constructionSkill) {

		double result = 0D;


		Iterator<Building> i = settlement.getBuildingManager().getBuildingSet().iterator();
		while (i.hasNext()) {
			Building building = i.next();
			double salvageProfit = getNewBuildingSalvageProfit(building, constructionSkill);
			if (salvageProfit > result) {
			    result = salvageProfit;
			}
		}

		return result;
	}

	/**
	 * Gets the profit from salvaging a given existing building.
	 * @param building the building.
	 * @param constructionSkill the architect's construction skill.
	 * @return salvage profit (VP).
	 * @throws Exception if error determining salvage profit.
	 */
	public double getNewBuildingSalvageProfit(Building building, int constructionSkill) {
		// Check for life support at settlement.
		LifeSupport lifeSupport = building.getFunction(FunctionType.LIFE_SUPPORT);
		if (lifeSupport != null) {
			// See if the building currently have any people in it.
		    if (lifeSupport.getOccupantNumber() > 0 && settlement.getBuildingManager().getBuildings(FunctionType.LIFE_SUPPORT).size() == 1) {
		    	// If there are people in it and that's the only one life support building, for sure do NOT salvage that building
		        return 0D;
		    }
		}

		// Check if building has needed living accommodation for settlement population.
		if (building.hasFunction(FunctionType.LIVING_ACCOMMODATION)) {
			int popSize = settlement.getNumCitizens();
			int popCapacity = settlement.getPopulationCapacity();
			LivingAccommodation livingaccommodation = building.getLivingAccommodation();
			int buildingPopCapacity = livingaccommodation.getBedCap();
			if ((popCapacity - buildingPopCapacity) < popSize) {
				return 0D;
			}
		}

		// Check that building doesn't have only airlock at settlement.
		if (building.hasFunction(FunctionType.EVA)
			&& settlement.getAirlockNum() == 1) {
			return 0D;
		}

		// Check that the building isn't on any person's walking path.
		Iterator<Person> i = unitManager.getPeople().iterator();
		while (i.hasNext()) {
		    Person person = i.next();
		    TaskManager taskManager = person.getMind().getTaskManager();
		    if (taskManager.isWalkingThroughBuilding(building)) {
		    	return 0D;
		    }
		}
		
		// Check that the building doesn't currently have any robots in it.
		if (building.hasFunction(FunctionType.ROBOTIC_STATION)) {
            RoboticStation roboticStation = building.getRoboticStation();
            if (roboticStation.getRobotOccupantNumber() > 0) {
            	return 0D;
            }
        } 
		
		// Check that the building isn't on any robot's walking path.
		Iterator<Robot> j = unitManager.getRobots().iterator();
        while (j.hasNext()) {
            Robot robot = j.next();
            BotTaskManager taskManager = robot.getBotMind().getBotTaskManager();
            if (taskManager.isWalkingThroughBuilding(building)) {
            	return 0D;
            }
        }
		
		// Check that the building doesn't currently have any vehicles in it.
		if (building.hasFunction(FunctionType.VEHICLE_MAINTENANCE)) {
		    VehicleGarage vehicleMaint = building.getVehicleParking();
		    if (vehicleMaint.getCurrentRoverNumber() > 0) {
		        return 0D;
		    }
		}

		// Get current value of building.
		BuildingManager buildingManager = settlement.getBuildingManager();
		double buildingValue = buildingManager.getBuildingValue(building);

		// Get value of prerequisite frame stage.
		double frameStageValue = 0D;
		ConstructionStageInfo buildingStage = ConstructionUtil.getConstructionStageInfo(building.getBuildingType());
		if ((buildingStage != null) && buildingStage.isSalvagable() && 
				(constructionSkill >= buildingStage.getArchitectConstructionSkill())) {
			ConstructionStageInfo frameStage = buildingStage.getPrerequisiteStage();
			frameStageValue = getSalvageStageValue(frameStage);

			// Determine value of estimated salvaged parts.
			double buildingWear = building.getMalfunctionManager().getWearCondition();
			double partsValue = getEstimatedSalvagedPartsValue(buildingStage, constructionSkill, (buildingWear / 100D));

			double salvageProfit = frameStageValue - buildingValue + partsValue;
			return salvageProfit;
		}
		
		return 0D;
	}

	/**
	 * Gets the value of a construction stage info.
	 * @param stageInfo the construction stage info.
	 * @return value of the stage (VP).
	 * @throws Exception if error determining value.
	 */
	private double getSalvageStageValue(ConstructionStageInfo stageInfo) {
	    	    
		// Use construction stage value.
		double value = 0D;
		for (var r : stageInfo.getResources().entrySet()) {
			value += r.getValue();
		}

		return value;
	}

	/**
	 * Gets the estimated value of salvaged parts.
	 * @param stageInfo the stage info.
	 * @param constructionSkill the architect's construction skill.
	 * @param wearCondition the wear factor for the stage (0.0 - 1.0).
	 * @return value of salvaged parts.
	 * @throws Exception if error determining value.
	 */
	private double getEstimatedSalvagedPartsValue(
		ConstructionStageInfo stageInfo,
		int constructionSkill, 
		double wearConditionModifier
	) {
		double result = 0D;

		GoodsManager goodsManager = settlement.getGoodsManager();

		Iterator<Integer> i = stageInfo.getParts().keySet().iterator();
		while (i.hasNext()) {
			Integer part = i.next();
			int number = stageInfo.getParts().get(part);
			double partValue = goodsManager.getGoodValuePoint(part);
			result += number * partValue;
		}

		// Modify total parts good value by wear condition and salvager skill.
		double valueModifier = .25D + (wearConditionModifier * .25D) + ((double) constructionSkill * .05D);
		result *= valueModifier;

		return result;
	}

	/**
	 * Loads instances.
	 * 
	 * @param u {@link UnitManager}
	 */
	public static void initializeInstances(UnitManager u, MasterClock masterClock) {
		unitManager = u;
		clock = masterClock;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		settlement = null;
		if(settlementSalvageValueCache != null){
			settlementSalvageValueCache.clear();
			settlementSalvageValueCache = null;
			settlementSalvageValueCacheTime = null;
		}
	}
}
