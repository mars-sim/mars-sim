/**
 * Mars Simulation Project
 * ConstructionValues.java
 * @version 2.85 2008-08-23
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
    private double settlementConstructionValueCache;
    private MarsClock settlementConstructionValueCacheTime;
    
    /**
     * Constructor
     * @param settlement the settlement.
     */
    ConstructionValues(Settlement settlement) {
        this.settlement = settlement;
    }
    
    /**
     * Gets the overall value for construction at the settlement.
     * @return value (VP)
     * @throws Exception if error determining value.
     */
    public double getSettlementConstructionValue() throws Exception {
        return getSettlementConstructionValue(Integer.MAX_VALUE);
    }
    
    /**
     * Gets the overall value for construction at the settlement.
     * @param constructionSkill the architect's construction skill.
     * @return value (VP)
     * @throws Exception if error determining value.
     */
    public double getSettlementConstructionValue(int constructionSkill) 
            throws Exception {
        
        MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
        if ((settlementConstructionValueCacheTime == null) || 
                (MarsClock.getTimeDiff(settlementConstructionValueCacheTime, currentTime) > 1000D)) {
            settlementConstructionValueCache = 0D;
            
            double existingSitesValue = getAllConstructionSitesValue(constructionSkill);
            if (existingSitesValue > settlementConstructionValueCache) 
                settlementConstructionValueCache = existingSitesValue;
            
            double newSiteValue = getNewConstructionSiteValue(constructionSkill);
            if (newSiteValue > settlementConstructionValueCache) 
                settlementConstructionValueCache = newSiteValue;
            
            settlementConstructionValueCacheTime = (MarsClock) currentTime.clone();
        }
        
        return settlementConstructionValueCache;
    }
    
    /**
     * Gets the overall value of all existing construction sites at a settlement
     * that can be worked on with a given construction skill.
     * @param constructionSkill the architect's construction skill.
     * @return value (VP)
     * @throws Exception if error determining value.
     */
    public double getAllConstructionSitesValue(int constructionSkill) 
            throws Exception {
        
        double result = 0D;
        
        ConstructionManager manager = settlement.getConstructionManager();
        Iterator<ConstructionSite> i = manager.getConstructionSitesNeedingMission().iterator();
        while (i.hasNext()) {
            double value = getConstructionSiteValue(i.next(), constructionSkill);
            if (value > result) result = value;
        }
        
        return result;
    }
    
    /**
     * Gets the value of an existing construction site at a settlement.
     * @param site the construction site.
     * @return value (VP)
     * @throws Exception if error determining value.
     */
    public double getConstructionSiteValue(ConstructionSite site) throws Exception {
        return getConstructionSiteValue(site, Integer.MAX_VALUE);
    }
    
    /**
     * Gets the value of an existing construction site at a settlement.
     * @param site the construction site.
     * @param constructionSkill the architect's construction skill.
     * @return value (VP)
     * @throws Exception if error determining value.
     */
    public double getConstructionSiteValue(ConstructionSite site, int constructionSkill) 
            throws Exception {
        
        double result = 0D;
        
        if (!site.isUndergoingConstruction()) {
            if (site.hasUnfinishedStage()) {
                ConstructionStage stage = site.getCurrentConstructionStage();
                if (stage.getInfo().getArchitectConstructionSkill() <= constructionSkill)
                    result = getConstructionStageValue(stage.getInfo());
            }
            else {
                if (site.getNextStageType() != null) {
                    Map<ConstructionStageInfo, Double> stageValues = getConstructionStageValues(
                            site.getNextStageType(), constructionSkill);
                    Iterator<ConstructionStageInfo> i = stageValues.keySet().iterator();
                    while (i.hasNext()) {
                        double value = stageValues.get(i.next());
                        if (value > result) result = value;
                    }
                }
                else {
                    String buildingName = site.getBuildingName();
                    if (buildingName != null) 
                        result = getBuildingConstructionValue(site.getBuildingName());
                }
            }
        }
        
        return result;
    }
    
    /**
     * Gets the value of creating a new construction site at a settlement.
     * @param constructionSkill the architect's construction skill.
     * @return value (VP)
     * @throws Exception if error determining value.
     */
    public double getNewConstructionSiteValue(int constructionSkill) 
            throws Exception {
        
        double result = 0D;
        Map<ConstructionStageInfo, Double> stageValues = getConstructionStageValues(
                ConstructionStageInfo.FOUNDATION, constructionSkill);
        Iterator<ConstructionStageInfo> i = stageValues.keySet().iterator();
        while (i.hasNext()) {
            double value = stageValues.get(i.next());
            if (value > result) result = value;
        }
        
        return result;
    }
    
    /**
     * Gets a map of construction stage infos and their values for a particular 
     * construction site.
     * @param site the construction site.
     * @param constructionSkill the architect's construction skill.
     * @return map of construction stage infos and their values (VP).
     * @throws Exception if error determining value.
     */
    public Map<ConstructionStageInfo, Double> getNewConstructionStageValues(
            ConstructionSite site, int constructionSkill) throws Exception {
        
        Map<ConstructionStageInfo, Double> result = new HashMap<ConstructionStageInfo, Double>();
        
        String nextStageType = site.getNextStageType();
        if (nextStageType == null) 
            result = getConstructionStageValues(nextStageType, constructionSkill);
        
        return result;
    }
    
    /**
     * Gets a map of construction stage infos and their values for a given stage type.
     * @param stageType the construction stage type.
     * @param constructionSkill the architect's construction skill.
     * @return map of construction stage infos and their values (VP).
     * @throws Exception if error determining value.
     */
    public Map<ConstructionStageInfo, Double> getConstructionStageValues(String stageType, 
            int constructionSkill) throws Exception {
        
        Map<ConstructionStageInfo, Double> result = new HashMap<ConstructionStageInfo, Double>();
        
        List<ConstructionStageInfo> nextStages = ConstructionUtil.getConstructionStageInfoList(
                stageType, constructionSkill);
        Iterator<ConstructionStageInfo> i = nextStages.iterator();
        while (i.hasNext()) {
            ConstructionStageInfo stageInfo = i.next();
            double value = getConstructionStageValue(stageInfo);
            result.put(stageInfo, value);
        }
        
        return result;
    }
    
    /**
     * Gets the value of a construction stage.
     * @param stageInfo the construction stage info.
     * @return value (VP).
     * @throws Exception if error getting value.
     */
    private double getConstructionStageValue(ConstructionStageInfo stageInfo) throws Exception {
        
        double result = 0D;
        
        if (ConstructionStageInfo.BUILDING.equals(stageInfo.getType())) {
            result = getBuildingConstructionValue(stageInfo.getName()) / 2D;
        }
        else {
            Iterator<ConstructionStageInfo> i = 
                ConstructionUtil.getNextPossibleStages(stageInfo).iterator();
            while (i.hasNext()) {
                double stageValue = getConstructionStageValue(i.next()) / 2D;
                if (stageValue > result) result = stageValue;
            }
        }
        
        GoodsManager manager = settlement.getGoodsManager();
        
        // Subtract value of construction resources.
        Map<AmountResource, Double> resources = stageInfo.getResources();
        Iterator<AmountResource> j = resources.keySet().iterator();
        while (j.hasNext()) {
            AmountResource resource = j.next();
            Good resourceGood = GoodsUtil.getResourceGood(resource);
            double amount = resources.get(resource);
            double value = manager.getGoodValuePerMass(resourceGood) * amount;
            result -= value;
        }
        
        // Subtract value of construction parts.
        Map<Part, Integer> parts = stageInfo.getParts();
        Iterator<Part> k = parts.keySet().iterator();
        while (k.hasNext()) {
            Part part = k.next();
            Good partGood = GoodsUtil.getResourceGood(part);
            int number = parts.get(part);
            double value = manager.getGoodValuePerItem(partGood) * number;
            result -= value;
        }
        
        if (result < 0D) result = 0D;
        
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
}