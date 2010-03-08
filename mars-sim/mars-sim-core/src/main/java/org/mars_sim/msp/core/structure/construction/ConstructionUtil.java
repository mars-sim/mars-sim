/**
 * Mars Simulation Project
 * ConstructionUtil.java
 * @version 2.85 2008-08-17
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.construction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.SimulationConfig;

/**
 * Utility class for construction.
 */
public class ConstructionUtil {
    
    /**
     * Private constructor.
     */
    private ConstructionUtil() {
    }
    
    /**
     * Gets a list of all construction stage info of a given type.
     * @param stageType the type of stage.
     * @return list of construction stage info.
     * @throws Exception if error getting list.
     */
    public final static List<ConstructionStageInfo> getConstructionStageInfoList(String stageType) 
            throws Exception {
        return getConstructionStageInfoList(stageType, Integer.MAX_VALUE);
    }
    
    /**
     * Gets a list of all construction stage info of a given type.
     * @param stageType the type of stage.
     * @param constructionSkill the architect's construction skill.
     * @return list of construction stage info.
     * @throws Exception if error getting list.
     */
    public final static List<ConstructionStageInfo> getConstructionStageInfoList(String stageType, 
            int constructionSkill) throws Exception {
        ConstructionConfig config = SimulationConfig.instance().getConstructionConfiguration();
        List<ConstructionStageInfo> result = 
            new ArrayList<ConstructionStageInfo>(config.getConstructionStageInfoList(stageType));
        Iterator<ConstructionStageInfo> i = result.iterator();
        while (i.hasNext()) {
            if (i.next().getArchitectConstructionSkill() > constructionSkill) i.remove();
        }
        return result;
    }
    
    /**
     * Gets a list of all foundation construction stage info.
     * @return list of construction stage info.
     * @throws Exception if error getting list.
     */
    public final static List<ConstructionStageInfo> getFoundationConstructionStageInfoList() 
            throws Exception { 
        return getFoundationConstructionStageInfoList(Integer.MAX_VALUE);
    }
    
    /**
     * Gets a list of all foundation construction stage info.
     * @param constructionSkill the architect's construction skill.
     * @return list of construction stage info.
     * @throws Exception if error getting list.
     */
    public final static List<ConstructionStageInfo> getFoundationConstructionStageInfoList(
            int constructionSkill) throws Exception { 
        return getConstructionStageInfoList(ConstructionStageInfo.FOUNDATION, constructionSkill);
    }
    
    /**
     * Gets a list of all frame construction stage info.
     * @return list of construction stage info.
     * @throws Exception if error getting list.
     */
    public final static List<ConstructionStageInfo> getFrameConstructionStageInfoList() 
            throws Exception {
        return getFrameConstructionStageInfoList(Integer.MAX_VALUE);
    }
    
    /**
     * Gets a list of all frame construction stage info.
     * @param constructionSkill the architect's construction skill.
     * @return list of construction stage info.
     * @throws Exception if error getting list.
     */
    public final static List<ConstructionStageInfo> getFrameConstructionStageInfoList(
            int constructionSkill) throws Exception {
        return getConstructionStageInfoList(ConstructionStageInfo.FRAME, constructionSkill);
    }
    
    /**
     * Gets a list of all building construction stage info.
     * @return list of construction stage info.
     * @throws Exception if error getting list.
     */
    public final static List<ConstructionStageInfo> getBuildingConstructionStageInfoList() 
            throws Exception {
        return getBuildingConstructionStageInfoList(Integer.MAX_VALUE);
    }
    
    /**
     * Gets a list of all building construction stage info.
     * @param constructionSkill the architect's construction skill.
     * @return list of construction stage info.
     * @throws Exception if error getting list.
     */
    public final static List<ConstructionStageInfo> getBuildingConstructionStageInfoList(
            int constructionSkill) throws Exception {
        return getConstructionStageInfoList(ConstructionStageInfo.BUILDING, constructionSkill);
    }
    
    /**
     * Gets a list of all construction stage info available.
     * @return list of construction stage info.
     * @throws Exception if error getting list.
     */
    public final static List<ConstructionStageInfo> getAllConstructionStageInfoList() 
            throws Exception {
        
        ConstructionConfig config = SimulationConfig.instance().getConstructionConfiguration();
        List<ConstructionStageInfo> foundations = config.getConstructionStageInfoList(
                ConstructionStageInfo.FOUNDATION);
        List<ConstructionStageInfo> frames = config.getConstructionStageInfoList(
                ConstructionStageInfo.FRAME);
        List<ConstructionStageInfo> buildings = config.getConstructionStageInfoList(
                ConstructionStageInfo.BUILDING);
        
        int resultSize = foundations.size() + frames.size() + buildings.size();
        List<ConstructionStageInfo> result = new ArrayList<ConstructionStageInfo>(resultSize);
        result.addAll(foundations);
        result.addAll(frames);
        result.addAll(buildings);
        
        return result;
    }
    
    /**
     * Gets a list of names of buildings that are constructable from a given construction stage info.
     * @param stageInfo the construction stage info.
     * @return list of building names.
     * @throws Exception if error getting list.
     */
    public final static List<String> getConstructableBuildingNames(ConstructionStageInfo stageInfo) 
            throws Exception {
        
        List<String> result = new ArrayList<String>();
        
        if (ConstructionStageInfo.FOUNDATION.equals(stageInfo.getType())) {
            Iterator<ConstructionStageInfo> i = getNextPossibleStages(stageInfo).iterator();
            while (i.hasNext()) result.addAll(getConstructableBuildingNames(i.next()));
        }
        else if (ConstructionStageInfo.FRAME.equals(stageInfo.getType())) {
            Iterator<ConstructionStageInfo> i = getNextPossibleStages(stageInfo).iterator();
            while (i.hasNext()) result.add(i.next().getName());
        }
        else if (ConstructionStageInfo.BUILDING.equals(stageInfo.getType())) {
            result.add(stageInfo.getName());
        }
        else throw new Exception("Unknown stage type: " + stageInfo.getType());
        
        return result;
    }
    
    /**
     * Gets a list of the next possible construction stages from a given construction stage info.
     * @param stageInfo the construction stage info.
     * @return list of construction stage info.
     * @throws Exception if error getting list.
     */
    public final static List<ConstructionStageInfo> getNextPossibleStages(ConstructionStageInfo stageInfo) 
            throws Exception {
        
        List<ConstructionStageInfo> result = new ArrayList<ConstructionStageInfo>();
        
        String nextStageName = null;
        if (ConstructionStageInfo.FOUNDATION.equals(stageInfo.getType())) 
            nextStageName = ConstructionStageInfo.FRAME;
        else if (ConstructionStageInfo.FRAME.equals(stageInfo.getType())) 
            nextStageName = ConstructionStageInfo.BUILDING;

        if (nextStageName != null) {
            ConstructionConfig config = SimulationConfig.instance().getConstructionConfiguration();
            Iterator<ConstructionStageInfo> i = config.getConstructionStageInfoList(nextStageName).iterator();
            while (i.hasNext()) {
                ConstructionStageInfo buildingStage = i.next();
                if (stageInfo.getName().equals(buildingStage.getPrerequisiteStage())) {
                    result.add(buildingStage);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Gets the prerequisite construction stage info for a given stage info.
     * @param stageInfo the construction stage info.
     * @return the prerequisite stage info or null if none.
     * @throws Exception if error finding prerequisite stage info.
     */
    public final static ConstructionStageInfo getPrerequisiteStage(ConstructionStageInfo stageInfo) 
            throws Exception {
        ConstructionStageInfo result = null;
        
        String prerequisiteStageName = stageInfo.getPrerequisiteStage();
        if (prerequisiteStageName != null) {
            Iterator<ConstructionStageInfo> i = getAllConstructionStageInfoList().iterator();
            while (i.hasNext()) {
                ConstructionStageInfo info = i.next();
                if (info.getName().equals(prerequisiteStageName)) result = info;
            }
        }
        
        return result;
    }
}