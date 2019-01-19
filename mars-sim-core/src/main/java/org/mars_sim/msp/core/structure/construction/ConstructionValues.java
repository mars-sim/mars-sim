/**
 * Mars Simulation Project
 * ConstructionValues.java
 * @version 3.1.0 2017-09-11
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.construction;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.structure.goods.GoodsUtil;
import org.mars_sim.msp.core.time.MarsClock;

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
    private MarsClock settlementConstructionValueCacheTime;
    private Map<ConstructionStageInfoSkillKey, Double> stageInfoValueCache;
    private MarsClock stageInfoValueCacheTime;
    private Map<ConstructionStageInfoSkillKey, Double> allStageInfoValueCache;
    private MarsClock allStageInfoValueCacheTime;

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

        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
        if ((settlementConstructionValueCacheTime == null) || 
                (MarsClock.getTimeDiff(currentTime, settlementConstructionValueCacheTime) > 1000D)) {
            if (settlementConstructionValueCache == null) 
                settlementConstructionValueCache = new HashMap<Integer, Double>();
            settlementConstructionValueCache.clear();
            settlementConstructionValueCacheTime = (MarsClock) currentTime.clone();
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

        Map<ConstructionStageInfo, Double> result = new HashMap<ConstructionStageInfo, Double>();

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

        Map<ConstructionStageInfo, Double> result = new HashMap<ConstructionStageInfo, Double>();

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

        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
        if ((allStageInfoValueCacheTime == null) || 
                (MarsClock.getTimeDiff(currentTime, allStageInfoValueCacheTime) > 1000D)) {
            if (allStageInfoValueCache == null) {
                allStageInfoValueCache = new HashMap<ConstructionStageInfoSkillKey, Double>();
            }
            allStageInfoValueCache.clear();

            Iterator<ConstructionStageInfo> i = ConstructionUtil.getAllConstructionStageInfoList()
                    .iterator();
            while (i.hasNext()) {
                ConstructionStageInfo stageInfo = i.next();
                allStageInfoValueCache.put(new ConstructionStageInfoSkillKey(stageInfo, constructionSkill), 
                        getConstructionStageValue(stageInfo, constructionSkill));
            }

            allStageInfoValueCacheTime = (MarsClock) currentTime.clone();

            // Display building construction values report to System.out for testing purposes.
//            displayAllBuildingConstructionValues();
        }
        
        // Create result map with just construction stage infos and their values.
        Map<ConstructionStageInfo, Double> result = new HashMap<ConstructionStageInfo, Double>(allStageInfoValueCache.size());
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

        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
        if ((stageInfoValueCacheTime == null) || 
                (MarsClock.getTimeDiff(currentTime, stageInfoValueCacheTime) > 1000D)) {
            if (stageInfoValueCache == null) {
                stageInfoValueCache = new HashMap<ConstructionStageInfoSkillKey, Double>();
            }
            stageInfoValueCache.clear();
            stageInfoValueCacheTime = (MarsClock) currentTime.clone();
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
            Good resourceGood = GoodsUtil.getResourceGood(resource);
            double amount = resources.get(resource);
            double value = manager.getGoodValuePerItem(resourceGood) * amount;
            cost += value;
        }

        // Add value of construction parts.
        Map<Integer, Integer> parts = stageInfo.getParts();
        Iterator<Integer> k = parts.keySet().iterator();
        while (k.hasNext()) {
        	Integer part = k.next();
            Good partGood = GoodsUtil.getResourceGood(part);
            int number = parts.get(part);
            double value = manager.getGoodValuePerItem(partGood) * number;
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
     * Display construction values report to System.out for testing purposes.
     */
    private void displayAllBuildingConstructionValues() {

        System.out.println("\n" + settlement.getName() + " constructable building profits:");
        DecimalFormat formatter = new DecimalFormat("0.0");
        
        int constructionSkill = ConstructionUtil.getBestConstructionSkillAtSettlement(settlement);
        
        Iterator<ConstructionStageInfo> i = ConstructionUtil.getAllConstructionStageInfoList()
                .iterator();
        while (i.hasNext()) {
            ConstructionStageInfo stageInfo = i.next();
            if (ConstructionStageInfo.BUILDING.equals(stageInfo.getType()) && isLocallyConstructable(stageInfo)) {
                double value = getConstructionStageValue(stageInfo, constructionSkill);
                double cost = getConstructionStageCost(stageInfo);
                ConstructionStageInfo buildingStage = stageInfo;
                ConstructionStageInfo frameStage = ConstructionUtil.getPrerequisiteStage(buildingStage);
                if ((frameStage != null) && frameStage.isConstructable()) {
                    cost += getConstructionStageCost(frameStage);
                    ConstructionStageInfo foundationStage = ConstructionUtil.getPrerequisiteStage(frameStage);
                    if ((foundationStage != null) && foundationStage.isConstructable()) {
                        cost += getConstructionStageCost(foundationStage);
                    }
                }
                double profit = value - cost;

                boolean canConstruct = false;
                if ((frameStage != null) && frameStage.isConstructable()) {
                    ConstructionStageInfo foundationStage = ConstructionUtil.getPrerequisiteStage(frameStage);
                    if ((foundationStage != null) && foundationStage.isConstructable()) {
                        canConstruct = true;
                    }
                }

                StringBuffer buff = new StringBuffer();
                buff.append(stageInfo.getName());
                buff.append(" = ");
                buff.append("value: ");
                buff.append(formatter.format(value));
                buff.append(" cost: ");
                buff.append(formatter.format(cost));
                buff.append(" profit: ");
                buff.append(formatter.format(profit));
                buff.append(" can construct: ");
                buff.append(canConstruct);
                System.out.println(buff.toString());
            }
        }
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
                        Iterator<Building> i = settlement.getBuildingManager().getACopyOfBuildings().iterator();
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
     * Clears the value caches.
     */
    public void clearCache() {
        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();

        if (settlementConstructionValueCache == null) {
            settlementConstructionValueCache = new HashMap<Integer, Double>();
        }
        settlementConstructionValueCache.clear();
        settlementConstructionValueCacheTime = (MarsClock) currentTime.clone();

        if (stageInfoValueCache == null) {
            stageInfoValueCache = new HashMap<ConstructionStageInfoSkillKey, Double>();
        }
        stageInfoValueCache.clear();
        stageInfoValueCacheTime = (MarsClock) currentTime.clone();

        if (allStageInfoValueCache == null) {
            allStageInfoValueCache = new HashMap<ConstructionStageInfoSkillKey, Double>();
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
    }
}