/*
 * Mars Simulation Project
 * ConstructionValues.java
 * @date 2021-12-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.construction;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.goods.GoodsManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsTime;

/**
 * Provides value information for construction.
 */
public class ConstructionValues
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** Value modifier for lower stages. */
    public final static double LOWER_STAGE_VALUE_MODIFIER = .5D;

    // Data members
    private Settlement settlement;
    private Map<Integer, Double> settlementConstructionValueCache;
    private MarsTime settlementConstructionValueCacheTime;
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
     * Gets the overall profit for construction at the settlement.
     * @return profit (VP)
     */
    public double getSettlementConstructionProfit() {
        return getSettlementConstructionProfit(Integer.MAX_VALUE);
    }

    /**
     * Gets the overall profit for construction at the settlement.
     * @param constructionSkill the architect's construction skill.
     * @return profit (VP)
     */
    public double getSettlementConstructionProfit(int constructionSkill) {

        MarsTime currentTime = Simulation.instance().getMasterClock().getMarsTime();
        if ((settlementConstructionValueCacheTime == null) || 
                (currentTime.getTimeDiff(settlementConstructionValueCacheTime) > 1000D)) {
            if (settlementConstructionValueCache == null) 
                settlementConstructionValueCache = new HashMap<>();
            settlementConstructionValueCache.clear();
            settlementConstructionValueCacheTime = currentTime;
        }

        if (!settlementConstructionValueCache.containsKey(constructionSkill)) {
            double profit = 0D;

            double existingSitesProfit = getAllConstructionSitesProfit(constructionSkill);
            if (existingSitesProfit > profit) {
                profit = existingSitesProfit;
            }

            double newSiteProfit = getNewConstructionSiteProfit(constructionSkill);
            if (newSiteProfit > profit) {
                profit = newSiteProfit;
            }

            settlementConstructionValueCache.put(constructionSkill, profit);
        }

        return settlementConstructionValueCache.get(constructionSkill);
    }

    /**
     * Gets the overall profit of all existing construction sites at a settlement
     * that can be worked on with a given construction skill.
     * @param constructionSkill the architect's construction skill.
     * @return profit (VP)
     */
    public double getAllConstructionSitesProfit(int constructionSkill) {

        double result = 0D;

        ConstructionManager manager = settlement.getConstructionManager();
        Iterator<ConstructionSite> i = manager.getConstructionSitesNeedingConstructionMission().iterator();
        while (i.hasNext()) {
            double profit = getConstructionSiteProfit(i.next(), constructionSkill);
            if (profit > result) {
                result = profit;
            }
        }

        return result;
    }

    /**
     * Gets the profit of an existing construction site at a settlement.
     * @param site the construction site.
     * @return profit (VP)
     */
    public double getConstructionSiteProfit(ConstructionSite site) {
        return getConstructionSiteProfit(site, Integer.MAX_VALUE);
    }

    /**
     * Gets the profit of an existing construction site at a settlement.
     * @param site the construction site.
     * @param constructionSkill the architect's construction skill.
     * @return profit (VP)
     */
    public double getConstructionSiteProfit(ConstructionSite site, int constructionSkill) {

        double result = 0D;

        if (!site.isUndergoingConstruction()) {
            if (site.hasUnfinishedStage()) {
                
                // Value for finishing construction stage at site.
                ConstructionStage stage = site.getCurrentConstructionStage();
                boolean enoughSkill = constructionSkill >= stage.getInfo().getArchitectConstructionSkill();
                boolean workCompletable = stage.getCompletedWorkTime() < stage.getCompletableWorkTime();
                boolean availableMaterials = settlement.getConstructionManager().hasRemainingConstructionMaterials(stage);
                if (enoughSkill && (workCompletable || availableMaterials)) {
                    result = getConstructionStageValue(stage.getInfo(), constructionSkill);
                }
            }
            else {
                
                // Value for starting new construction stage at site.
                List<ConstructionStageInfo> nextStageInfos = null;

                ConstructionStage lastStage = site.getCurrentConstructionStage();
                if (lastStage != null) {
                    nextStageInfos = ConstructionUtil.getNextPossibleStages(lastStage.getInfo());
                }
                else {
                    nextStageInfos = ConstructionUtil.getConstructionStageInfoList(
                            ConstructionStageInfo.FOUNDATION, constructionSkill);
                }

                if (nextStageInfos != null) {
                    Iterator<ConstructionStageInfo> i = nextStageInfos.iterator();
                    while (i.hasNext()) {
                        ConstructionStageInfo stageInfo = i.next();
                        double profit = getConstructionStageProfit(stageInfo, constructionSkill);
                        if (profit > result) {
                            result = profit;
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Gets the profit of creating a new construction site at a settlement.
     * @param constructionSkill the architect's construction skill.
     * @return profit (VP)
     */
    public double getNewConstructionSiteProfit(int constructionSkill) {

        double result = 0D;
        Map<ConstructionStageInfo, Double> stageProfits = getConstructionStageProfit(
                ConstructionStageInfo.FOUNDATION, constructionSkill);
        Iterator<ConstructionStageInfo> i = stageProfits.keySet().iterator();
        while (i.hasNext()) {
            ConstructionStageInfo foundationStage = i.next();
            double profit = stageProfits.get(foundationStage);

            if (profit > result) {
                result = profit;
            }
        }

        return result;
    }

    /**
     * Gets a map of construction stage infos and their profits for a particular 
     * construction site.
     * @param site the construction site.
     * @param constructionSkill the architect's construction skill.
     * @return map of construction stage infos and their profits (VP).
     */
    public Map<ConstructionStageInfo, Double> getNewConstructionStageProfits(
            ConstructionSite site, int constructionSkill) {

        Map<ConstructionStageInfo, Double> result = new HashMap<>();

        ConstructionStage lastStage = site.getCurrentConstructionStage();
        if (lastStage != null) {
            ConstructionStageInfo lastStageInfo = lastStage.getInfo();
            Iterator<ConstructionStageInfo> i = 
                    ConstructionUtil.getNextPossibleStages(lastStageInfo).iterator();
            while (i.hasNext()) {
                ConstructionStageInfo stageInfo = i.next();
                double profit = getConstructionStageProfit(stageInfo, constructionSkill);
                result.put(stageInfo, profit);
            }
        }
        else {
            result = getConstructionStageProfit(ConstructionStageInfo.FOUNDATION, 
                    constructionSkill);
        }

        return result;
    }

    /**
     * Gets a map of construction stage infos and their profits for a given stage type.
     * @param stageType the construction stage type.
     * @param constructionSkill the architect's construction skill.
     * @return map of construction stage infos and their profits (VP).
     */
    public Map<ConstructionStageInfo, Double> getConstructionStageProfit(String stageType, 
            int constructionSkill) {

        Map<ConstructionStageInfo, Double> result = new HashMap<>();

        List<ConstructionStageInfo> nextStages = ConstructionUtil.getConstructionStageInfoList(
                stageType, constructionSkill);
        Iterator<ConstructionStageInfo> i = nextStages.iterator();
        while (i.hasNext()) {
            ConstructionStageInfo stageInfo = i.next();
            double profit = getConstructionStageProfit(stageInfo, constructionSkill);
            result.put(stageInfo, profit);
        }

        return result;
    }

    /**
     * Gets a map of all construction stage infos and their values.
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

            Iterator<ConstructionStageInfo> i = ConstructionUtil.getAllConstructionStageInfoList()
                    .iterator();
            while (i.hasNext()) {
                ConstructionStageInfo stageInfo = i.next();
                allStageInfoValueCache.put(new ConstructionStageInfoSkillKey(stageInfo, constructionSkill), 
                        getConstructionStageValue(stageInfo, constructionSkill));
            }

            allStageInfoValueCacheTime = currentTime;
        }
        
        // Create result map with just construction stage infos and their values.
        Map<ConstructionStageInfo, Double> result = new HashMap<>(allStageInfoValueCache.size());
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
                if (ConstructionStageInfo.BUILDING.equals(stageInfo.getType())) {
                    result = getBuildingConstructionValue(stageInfo.getName());
                }
                else {
                    Iterator<ConstructionStageInfo> i = 
                            ConstructionUtil.getNextPossibleStages(stageInfo).iterator();
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
            //System.out.println(settlement.getName() + " - " + stageInfo.getName() + ": " + (int) result);
            stageInfoValueCache.put(key, result);
        }

        return stageInfoValueCache.get(key);
    }

    /**
     * Gets the cost of a construction stage.
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
     * @param stageInfo the construction stage info.
     * @param constructionSkill the person's construction skill.
     * @return profit (VP)
     */
    private double getConstructionStageProfit(ConstructionStageInfo stageInfo, int constructionSkill) {
        return getConstructionStageValue(stageInfo, constructionSkill) - getConstructionStageCost(stageInfo);
    }

    /**
     * Gets the value of constructing a building.
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

        if (settlementConstructionValueCache == null) {
            settlementConstructionValueCache = new HashMap<>();
        }
        settlementConstructionValueCache.clear();
        settlementConstructionValueCacheTime = currentTime;

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
     * Prepare object for garbage collection.
     */
    public void destroy() {
        settlement = null;
        if(settlementConstructionValueCache != null){

            settlementConstructionValueCache.clear();
            settlementConstructionValueCache = null;
            settlementConstructionValueCacheTime = null;
        }
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
    private class ConstructionStageInfoSkillKey implements Serializable {
        
        /** default serial id. */
        private static final long serialVersionUID = 1L;
        
        // Data members.
        ConstructionStageInfo stageInfo;
        int skill;
        
        ConstructionStageInfoSkillKey(ConstructionStageInfo stageInfo, int skill) {
            this.stageInfo = stageInfo;
            this.skill = skill;
        }
        
        @Override
        public boolean equals(Object object) {
            boolean result = false;
            
            if (object instanceof ConstructionStageInfoSkillKey) {
                ConstructionStageInfoSkillKey objectKey = (ConstructionStageInfoSkillKey) object;
                if (objectKey.stageInfo.equals(stageInfo) && (objectKey.skill == skill)) {
                    result = true;
                }
            }
            
            return result;
        }
        
    	/**
    	 * Gets the hash code for this object.
    	 *
    	 * @return hash code.
    	 */
    	@Override
    	public int hashCode() {
    		return super.hashCode();
    	}

    }
}
