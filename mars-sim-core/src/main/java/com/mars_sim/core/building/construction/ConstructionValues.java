/*
 * Mars Simulation Project
 * ConstructionValues.java
 * @date 2021-12-15
 * @author Scott Davis
 */
package com.mars_sim.core.building.construction;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.goods.GoodsManager;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;

/**
 * Provides value information for construction.
 */
public class ConstructionValues
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Value modifier for lower stages. */
    private static final double LOWER_STAGE_VALUE_MODIFIER = .5D;

    private static ConstructionConfig config = SimulationConfig.instance().getConstructionConfiguration();

    // Data members
    private Settlement settlement;
    private Map<ConstructionStageInfoSkillKey, Double> stageInfoValueCache;
    private MarsTime stageInfoValueCacheTime;
    private Map<ConstructionStageInfoSkillKey, Double> allStageInfoValueCache;
    private MarsTime allStageInfoValueCacheTime;

    /**
     * Constructor.
     * @param settlement the settlement.
     */
    public ConstructionValues(Settlement settlement) {
        this.settlement = settlement;
    }


    /**
     * Gets a map of all construction stage infos and their values.
     * 
     * @param constructionSkill the construction skill of the person.
     * @return map of construction stage infos and their values (VP).
     */
    public Map<ConstructionStageInfo, Double> getAllConstructionStageValues(int constructionSkill) {

        MarsTime currentTime = Simulation.instance().getMasterClock().getMarsTime();
        if ((allStageInfoValueCacheTime == null) || 
                (currentTime.getTimeDiff(allStageInfoValueCacheTime) > 1000D)) {
            if (allStageInfoValueCache == null) {
                allStageInfoValueCache = new HashMap<>();
            }
            allStageInfoValueCache.clear();

            Iterator<ConstructionStageInfo> i = config.getAllConstructionStageInfoList()
                    .iterator();
            while (i.hasNext()) {
                ConstructionStageInfo stageInfo = i.next();
                allStageInfoValueCache.put(new ConstructionStageInfoSkillKey(stageInfo, constructionSkill), 
                        getConstructionStageValue(stageInfo, constructionSkill));
            }

            allStageInfoValueCacheTime = currentTime;
        }
        
        // Create result map with just construction stage infos and their values.
        Map<ConstructionStageInfo, Double> result = new HashMap<>();
        Iterator<ConstructionStageInfoSkillKey> j = allStageInfoValueCache.keySet().iterator();
        while (j.hasNext()) {
            ConstructionStageInfoSkillKey key = j.next();
            double value = allStageInfoValueCache.get(key);
            result.put(key.stageInfo, value);
        }

        return result;
    }

    /**
     * Gets the value of a construction stage.
     * 
     * @param stageInfo the construction stage info.
     * @param constructionSkill the person's construction skill.
     * @return value (VP).
     */
    public double getConstructionStageValue(ConstructionStageInfo stageInfo, int constructionSkill) {

        MarsTime currentTime = Simulation.instance().getMasterClock().getMarsTime();
        if ((stageInfoValueCacheTime == null) || 
                (currentTime.getTimeDiff(stageInfoValueCacheTime) > 1000D)) {
            if (stageInfoValueCache == null) {
                stageInfoValueCache = new HashMap<>();
            }
            stageInfoValueCache.clear();
            stageInfoValueCacheTime = currentTime;
        }

        ConstructionStageInfoSkillKey key = new ConstructionStageInfoSkillKey(stageInfo, constructionSkill);
        if (!stageInfoValueCache.containsKey(key)) {
            double result = 0D;

            if (constructionSkill >= stageInfo.getArchitectConstructionSkill()) {
                if (ConstructionStageInfo.Stage.BUILDING.equals(stageInfo.getType())) {
                    result = getBuildingConstructionValue(stageInfo.getName());
                }
                else {
                    Iterator<ConstructionStageInfo> i = 
                                    config.getPotentialNextStages(stageInfo).iterator();
                    while (i.hasNext()) {
                        ConstructionStageInfo nextStageInfo = i.next();
                        boolean constructable = nextStageInfo.isConstructable();
                        if (constructable) {
                            double stageValue = getConstructionStageProfit(nextStageInfo, constructionSkill) * 
                                    LOWER_STAGE_VALUE_MODIFIER;
                            if (stageValue > result) {
                                result = stageValue;
                            }
                        }
                    }
                }
            }
            stageInfoValueCache.put(key, result);
        }

        return stageInfoValueCache.get(key);
    }

    /**
     * Gets the cost of a construction stage.
     * 
     * @param stageInfo the construction stage info.
     * @return cost (VP)
     */
    private double getConstructionStageCost(ConstructionStageInfo stageInfo) {
        double cost = 0D;

        GoodsManager manager = settlement.getGoodsManager();

        // Add value of construction resources.
        Map<Integer, Double> resources = stageInfo.getResources();
        Iterator<Integer> j = resources.keySet().iterator();
        while (j.hasNext()) {
        	Integer resource = j.next();
            double amount = resources.get(resource);
            double value = manager.getGoodValuePoint(resource) * amount;
            cost += value;
        }

        // Add value of construction parts.
        Map<Integer, Integer> parts = stageInfo.getParts();
        Iterator<Integer> k = parts.keySet().iterator();
        while (k.hasNext()) {
        	Integer part = k.next();
            int number = parts.get(part);
            double value = manager.getGoodValuePoint(part) * number;
            cost += value;
        }

        return cost;
    }

    /**
     * Gets the profit for a construction stage.
     * 
     * @param stageInfo the construction stage info.
     * @param constructionSkill the person's construction skill.
     * @return profit (VP)
     */
    private double getConstructionStageProfit(ConstructionStageInfo stageInfo, int constructionSkill) {
        return getConstructionStageValue(stageInfo, constructionSkill) - getConstructionStageCost(stageInfo);
    }

    /**
     * Gets the value of constructing a building.
     * 
     * @param buildingName the building's name.
     * @return value (VP)
     */
    private double getBuildingConstructionValue(String buildingName) {
        return settlement.getBuildingManager().getBuildingValue(buildingName, true);
    }
  
    /**
     * Clears the value caches.
     */
    public void clearCache() {
        MarsTime currentTime = Simulation.instance().getMasterClock().getMarsTime();

        if (stageInfoValueCache == null) {
            stageInfoValueCache = new HashMap<>();
        }
        stageInfoValueCache.clear();
        stageInfoValueCacheTime = currentTime;

        if (allStageInfoValueCache == null) {
            allStageInfoValueCache = new HashMap<>();
        }
        allStageInfoValueCache.clear();
    }
	
    /**
     * Prepares object for garbage collection.
     */
    public void destroy() {
        settlement = null;

        if(stageInfoValueCache != null){

            stageInfoValueCache.clear();
            stageInfoValueCache = null;
            stageInfoValueCacheTime = null;
        }
        
        if(allStageInfoValueCache != null){
            allStageInfoValueCache.clear();
            allStageInfoValueCache = null;
            allStageInfoValueCacheTime = null;
        }
    }
    
    /**
     * Inner class for a construction stage info and skill combination map key value.
     */
    private record ConstructionStageInfoSkillKey(ConstructionStageInfo stageInfo,
                                        int skill) implements Serializable {}
}
