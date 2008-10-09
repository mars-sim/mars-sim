/**
 * Mars Simulation Project
 * ConstructionValues.java
 * @version 2.85 2008-08-31
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure.construction;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.Part;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.goods.Good;
import org.mars_sim.msp.simulation.structure.goods.GoodsManager;
import org.mars_sim.msp.simulation.structure.goods.GoodsUtil;
import org.mars_sim.msp.simulation.time.MarsClock;

/**
 * Provides value information for construction.
 */
public class ConstructionValues implements Serializable {

    // Data members
    private Settlement settlement;
    private Map<Integer, Double> settlementConstructionValueCache;
    private MarsClock settlementConstructionValueCacheTime;
    private Map<ConstructionStageInfo, Double> stageInfoValueCache;
    private MarsClock stageInfoValueCacheTime;
    private Map<ConstructionStageInfo, Double> allStageInfoValueCache;
    private MarsClock allStageInfoValueCacheTime;
    
    /**
     * Constructor
     * @param settlement the settlement.
     */
    ConstructionValues(Settlement settlement) {
        this.settlement = settlement;
    }
    
    /**
     * Gets the overall profit for construction at the settlement.
     * @return profit (VP)
     * @throws Exception if error determining profit.
     */
    public double getSettlementConstructionProfit() throws Exception {
        return getSettlementConstructionProfit(Integer.MAX_VALUE);
    }
    
    /**
     * Gets the overall profit for construction at the settlement.
     * @param constructionSkill the architect's construction skill.
     * @return profit (VP)
     * @throws Exception if error determining profit.
     */
    public double getSettlementConstructionProfit(int constructionSkill) 
            throws Exception {
        
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
            if (existingSitesProfit > profit) profit = existingSitesProfit;
            
            double newSiteProfit = getNewConstructionSiteProfit(constructionSkill);
            if (newSiteProfit > profit) profit = newSiteProfit;
            
            settlementConstructionValueCache.put(constructionSkill, profit);
            System.out.println(settlement.getName() + " construction profit: " + profit + 
            		" for skill: " + constructionSkill);
        }
        
        return settlementConstructionValueCache.get(constructionSkill);
    }
    
    /**
     * Gets the overall profit of all existing construction sites at a settlement
     * that can be worked on with a given construction skill.
     * @param constructionSkill the architect's construction skill.
     * @return profit (VP)
     * @throws Exception if error determining profit.
     */
    public double getAllConstructionSitesProfit(int constructionSkill) 
            throws Exception {
        
        double result = 0D;
        
        ConstructionManager manager = settlement.getConstructionManager();
        Iterator<ConstructionSite> i = manager.getConstructionSitesNeedingMission().iterator();
        while (i.hasNext()) {
            double profit = getConstructionSiteProfit(i.next(), constructionSkill);
            if (profit > result) result = profit;
        }
        
        return result;
    }
    
    /**
     * Gets the profit of an existing construction site at a settlement.
     * @param site the construction site.
     * @return profit (VP)
     * @throws Exception if error determining profit.
     */
    public double getConstructionSiteProfit(ConstructionSite site) throws Exception {
        return getConstructionSiteProfit(site, Integer.MAX_VALUE);
    }
    
    /**
     * Gets the profit of an existing construction site at a settlement.
     * @param site the construction site.
     * @param constructionSkill the architect's construction skill.
     * @return profit (VP)
     * @throws Exception if error determining profit.
     */
    public double getConstructionSiteProfit(ConstructionSite site, int constructionSkill) 
            throws Exception {
        
        double result = 0D;
        
        if (!site.isUndergoingConstruction()) {
            if (site.hasUnfinishedStage()) {
                ConstructionStage stage = site.getCurrentConstructionStage();
                if (stage.getInfo().getArchitectConstructionSkill() <= constructionSkill)
                    result = getConstructionStageValue(stage.getInfo());
            }
            else {
                List<ConstructionStageInfo> nextStageInfos = null;
                
                ConstructionStage lastStage = site.getCurrentConstructionStage();
                if (lastStage != null) nextStageInfos = ConstructionUtil.getNextPossibleStages(lastStage.getInfo());
                else nextStageInfos = ConstructionUtil.getConstructionStageInfoList(
                        ConstructionStageInfo.FOUNDATION, constructionSkill);
                
                if (nextStageInfos != null) {
                    Iterator<ConstructionStageInfo> i = nextStageInfos.iterator();
                    while (i.hasNext()) {
                        ConstructionStageInfo stageInfo = i.next();
                        double profit = getConstructionStageProfit(stageInfo, true);
                        if (profit > result) result = profit;
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
     * @throws Exception if error determining profit.
     */
    public double getNewConstructionSiteProfit(int constructionSkill) 
            throws Exception {
        
        double result = 0D;
        Map<ConstructionStageInfo, Double> stageProfits = getConstructionStageProfit(
                ConstructionStageInfo.FOUNDATION, constructionSkill, true);
        Iterator<ConstructionStageInfo> i = stageProfits.keySet().iterator();
        while (i.hasNext()) {
            ConstructionStageInfo foundationStage = i.next();
            double profit = stageProfits.get(foundationStage);
            
            // Divide by number of existing construction sites (x 10) with this foundation.
            int numSites = 0;
            ConstructionManager manager = settlement.getConstructionManager();
            Iterator<ConstructionSite> j = manager.getConstructionSites().iterator();
            while (j.hasNext()) {
                if (j.next().hasStage(foundationStage)) numSites++;
            }
            profit/= ((numSites * 10D) + 1D);
            
            if (profit > result) result = profit;
        }
        
        return result;
    }
    
    /**
     * Gets a map of construction stage infos and their profits for a particular 
     * construction site.
     * @param site the construction site.
     * @param constructionSkill the architect's construction skill.
     * @return map of construction stage infos and their profits (VP).
     * @throws Exception if error determining profit.
     */
    public Map<ConstructionStageInfo, Double> getNewConstructionStageProfits(
            ConstructionSite site, int constructionSkill) throws Exception {
        
        Map<ConstructionStageInfo, Double> result = new HashMap<ConstructionStageInfo, Double>();
        
        ConstructionStage lastStage = site.getCurrentConstructionStage();
        if (lastStage != null) {
            ConstructionStageInfo lastStageInfo = lastStage.getInfo();
            Iterator<ConstructionStageInfo> i = 
                ConstructionUtil.getNextPossibleStages(lastStageInfo).iterator();
            while (i.hasNext()) {
                ConstructionStageInfo stageInfo = i.next();
                double profit = getConstructionStageProfit(stageInfo, true);
                result.put(stageInfo, profit);
            }
        }
        else {
            result = getConstructionStageProfit(ConstructionStageInfo.FOUNDATION, 
                    constructionSkill, true);
        }
        
        return result;
    }
    
    /**
     * Gets a map of construction stage infos and their profits for a given stage type.
     * @param stageType the construction stage type.
     * @param constructionSkill the architect's construction skill.
     * @param checkMaterials should check if settlement has enough construction materials?
     * @return map of construction stage infos and their profits (VP).
     * @throws Exception if error determining profit.
     */
    public Map<ConstructionStageInfo, Double> getConstructionStageProfit(String stageType, 
            int constructionSkill, boolean checkMaterials) throws Exception {
        
        Map<ConstructionStageInfo, Double> result = new HashMap<ConstructionStageInfo, Double>();
        
        List<ConstructionStageInfo> nextStages = ConstructionUtil.getConstructionStageInfoList(
                stageType, constructionSkill);
        Iterator<ConstructionStageInfo> i = nextStages.iterator();
        while (i.hasNext()) {
            ConstructionStageInfo stageInfo = i.next();
            double profit = getConstructionStageProfit(stageInfo, checkMaterials);
            result.put(stageInfo, profit);
        }
        
        return result;
    }
    
    /**
     * Gets a map of all construction stage infos and their values.
     * @param stageType the construction stage type.
     * @return map of construction stage infos and their values (VP).
     * @throws Exception if error determining value.
     */
    public Map<ConstructionStageInfo, Double> getAllConstructionStageValues() throws Exception {
        
        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
        if ((allStageInfoValueCacheTime == null) || 
                (MarsClock.getTimeDiff(currentTime, allStageInfoValueCacheTime) > 1000D)) {
            if (allStageInfoValueCache == null) 
                allStageInfoValueCache = new HashMap<ConstructionStageInfo, Double>();
            allStageInfoValueCache.clear();
            
            Iterator<ConstructionStageInfo> i = ConstructionUtil.getAllConstructionStageInfoList()
                    .iterator();
            while (i.hasNext()) {
                ConstructionStageInfo stageInfo = i.next();
                allStageInfoValueCache.put(stageInfo, getConstructionStageValue(stageInfo));
            }
            
            allStageInfoValueCacheTime = (MarsClock) currentTime.clone();
        }
        
        return allStageInfoValueCache;
    }
    
    /**
     * Gets the value of a construction stage.
     * @param stageInfo the construction stage info.
     * @return value (VP).
     * @throws Exception if error getting value.
     */
    private double getConstructionStageValue(ConstructionStageInfo stageInfo) throws Exception {
        
        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
        if ((stageInfoValueCacheTime == null) || 
                (MarsClock.getTimeDiff(currentTime, stageInfoValueCacheTime) > 1000D)) {
            if (stageInfoValueCache == null) 
                stageInfoValueCache = new HashMap<ConstructionStageInfo, Double>();
            stageInfoValueCache.clear();
            stageInfoValueCacheTime = (MarsClock) currentTime.clone();
        }
        
        if (!stageInfoValueCache.containsKey(stageInfo)) {
            double result = 0D;
        
            if (ConstructionStageInfo.BUILDING.equals(stageInfo.getType())) {
                result = getBuildingConstructionValue(stageInfo.getName());
            }
            else {
                Iterator<ConstructionStageInfo> i = 
                        ConstructionUtil.getNextPossibleStages(stageInfo).iterator();
                while (i.hasNext()) {
                    double stageValue = getConstructionStageProfit(i.next(), true) / 2D;
                    if (stageValue > result) result = stageValue;
                }
            }
        
            stageInfoValueCache.put(stageInfo, result);
        }
        
        return stageInfoValueCache.get(stageInfo);
    }
    
    /**
     * Gets the cost of a construction stage.
     * @param stageInfo the construction stage info.
     * @return cost (VP)
     * @throws Exception if error determining the cost.
     */
    private double getConstructionStageCost(ConstructionStageInfo stageInfo) throws Exception {
        double cost = 0D;
        
        GoodsManager manager = settlement.getGoodsManager();
        
        // Add value of construction resources.
        Map<AmountResource, Double> resources = stageInfo.getResources();
        Iterator<AmountResource> j = resources.keySet().iterator();
        while (j.hasNext()) {
            AmountResource resource = j.next();
            Good resourceGood = GoodsUtil.getResourceGood(resource);
            double amount = resources.get(resource);
            double value = manager.getGoodValuePerMass(resourceGood) * amount;
            cost += value;
        }
    
        // Add value of construction parts.
        Map<Part, Integer> parts = stageInfo.getParts();
        Iterator<Part> k = parts.keySet().iterator();
        while (k.hasNext()) {
            Part part = k.next();
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
     * @param checkMaterials check availability of construction materials?
     * @return profit (VP)
     * @throws Exception if error determining profit.
     */
    private double getConstructionStageProfit(ConstructionStageInfo stageInfo, boolean checkMaterials) 
            throws Exception {
        double result = getConstructionStageValue(stageInfo) - getConstructionStageCost(stageInfo);
        if (checkMaterials && !hasConstructionMaterials(stageInfo)) result = 0D;
        
        return result;
    }
    
    /**
     * Gets the value of constructing a building.
     * @param buildingName the building's name.
     * @return value (VP)
     * @throws Exception if error getting construction value.
     */
    private double getBuildingConstructionValue(String buildingName) throws Exception {
        return settlement.getBuildingManager().getBuildingValue(buildingName, true);
    }
    
    // TODO: add comments
    private boolean hasConstructionMaterials(ConstructionStageInfo stageInfo) throws Exception {
        boolean result = true;
        
        // Check resources.
        Map<AmountResource, Double> resources = stageInfo.getResources();
        Iterator<AmountResource> j = resources.keySet().iterator();
        while (j.hasNext()) {
            AmountResource resource = j.next();
            double amount = resources.get(resource);
            double stored = settlement.getInventory().getAmountResourceStored(resource);
            if (stored < amount) result = false;
        }
    
        // Check parts.
        Map<Part, Integer> parts = stageInfo.getParts();
        Iterator<Part> k = parts.keySet().iterator();
        while (k.hasNext()) {
            Part part = k.next();
            int number = parts.get(part);
            int stored = settlement.getInventory().getItemResourceNum(part);
            if (stored < number) result = false;
        }
        
        return result;
    }
    
    /**
     * Clears the value caches.
     */
    public void clearCache() {
        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
        
        if (settlementConstructionValueCache == null) 
            settlementConstructionValueCache = new HashMap<Integer, Double>();
        settlementConstructionValueCache.clear();
        settlementConstructionValueCacheTime = (MarsClock) currentTime.clone();
        
        if (stageInfoValueCache == null) 
            stageInfoValueCache = new HashMap<ConstructionStageInfo, Double>();
        stageInfoValueCache.clear();
        stageInfoValueCacheTime = (MarsClock) currentTime.clone();
        
        if (allStageInfoValueCache == null) 
            allStageInfoValueCache = new HashMap<ConstructionStageInfo, Double>();
        allStageInfoValueCache.clear();
    }
}