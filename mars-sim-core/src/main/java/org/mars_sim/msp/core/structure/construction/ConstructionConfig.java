/**
 * Mars Simulation Project
 * ConstructionConfig.java
 * @version 3.1.0 2017-09-11
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.construction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;


/**
 * Parses construction configuration file.
 */
public class ConstructionConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	    
	private static Logger logger = Logger.getLogger(ConstructionConfig.class.getName());

    // Element names
    private static final String NAME = "name";
    private static final String WIDTH = "width";
    private static final String LENGTH = "length";
    private static final String BASE_LEVEL = "base-level";
    private static final String CONSTRUCTABLE = "constructable";
    private static final String SALVAGABLE = "salvagable";
    private static final String WORK_TIME = "work-time";
    private static final String SKILL_REQUIRED = "skill-required";
    private static final String PART = "part";
    private static final String NUMBER = "number";
    private static final String RESOURCE = "resource";
    private static final String AMOUNT = "amount";
    private static final String VEHICLE = "vehicle";
    private static final String TYPE = "type";
    private static final String ATTACHMENT_PART = "attachment-part";
    
    // Data members
    private Document constructionDoc;
    private List<ConstructionStageInfo> foundationStageInfoList;
    private List<ConstructionStageInfo> frameStageInfoList;
    private List<ConstructionStageInfo> buildingStageInfoList;
    
    /**
     * Constructor
     * @param constructionDoc DOM document with construction configuration
     */
    public ConstructionConfig(Document constructionDoc) {
        this.constructionDoc = constructionDoc; 
    }
    
    /**
     * Gets a list of construction stage infos.
     * @param stageType the type of stage.
     * @return list of construction stage infos.
     * @throws Exception if error parsing list.
     */
    public List<ConstructionStageInfo> getConstructionStageInfoList(String stageType) {
        
        List<ConstructionStageInfo> stageInfo = null;
        
        if (ConstructionStageInfo.FOUNDATION.equals(stageType)) {
            if (foundationStageInfoList == null)
                createConstructionStageInfoList(ConstructionStageInfo.FOUNDATION);
            stageInfo = foundationStageInfoList;
        }
        else if (ConstructionStageInfo.FRAME.equals(stageType)) {
            if (frameStageInfoList == null)
                createConstructionStageInfoList(ConstructionStageInfo.FRAME);
            stageInfo = frameStageInfoList;
        }
        else if (ConstructionStageInfo.BUILDING.equals(stageType)) {
            if (buildingStageInfoList == null)
                createConstructionStageInfoList(ConstructionStageInfo.BUILDING);
            stageInfo = buildingStageInfoList;
        }
        else throw new IllegalStateException("stageType: " + stageType + " is invalid.");
        
        stageInfo = new ArrayList<ConstructionStageInfo>(stageInfo);
        Collections.sort(stageInfo, new Comparator<ConstructionStageInfo>() {
            @Override
            public int compare(ConstructionStageInfo c2, ConstructionStageInfo c1) {
                return c2.getName().compareTo(c1.getName());
            }
        });		
        
        return stageInfo;//new ArrayList<ConstructionStageInfo>(stageInfo);
    }
    
    /**
     * Creates a stage info list.
     * @param stageType the stage type.
     * @return list of construction stage infos.
     * @throws Exception if error parsing XML file.
     */
    @SuppressWarnings("unchecked")
    private List<ConstructionStageInfo> createConstructionStageInfoList(String stageType) {
        
        List<ConstructionStageInfo> stageInfoList = null;
        if (ConstructionStageInfo.FOUNDATION.equals(stageType)) {
            foundationStageInfoList = new ArrayList<ConstructionStageInfo>();
            stageInfoList = foundationStageInfoList;
        }
        else if (ConstructionStageInfo.FRAME.equals(stageType)) {
            frameStageInfoList = new ArrayList<ConstructionStageInfo>();
            stageInfoList = frameStageInfoList;
        }
        else if (ConstructionStageInfo.BUILDING.equals(stageType)) {
            buildingStageInfoList = new ArrayList<ConstructionStageInfo>();
            stageInfoList = buildingStageInfoList;
        }
        else throw new IllegalStateException("stageType: " + stageType + " not valid.");
            
        Element root = constructionDoc.getRootElement();
        Element stageInfoListElement = root.getChild(stageType + "-list");
        List<Element> stageInfoNodes = stageInfoListElement.getChildren(stageType);
        
        for (Element stageInfoElement : stageInfoNodes) {
            String name = "";
                
            try {
                    
                // Get name
                name = stageInfoElement.getAttributeValue(NAME);
                
                if (stageInfoList == buildingStageInfoList) {
	                boolean invalid_name = true;
	                
	                Set<String> types = SimulationConfig.instance().getBuildingConfiguration().getBuildingTypes();
	                
	                for (String s : types) {
	                	if (s.toLowerCase().equals(name.toLowerCase())) {
	                		invalid_name = false;
	                		break;
	                	}
	                }
	                if (invalid_name)
	                	throw new IllegalStateException("ConstructionConfig : '" + name +
	                			"' in constructions.xml does not match to any building types in buildings.xml.");
                }
                
                String widthStr = stageInfoElement.getAttributeValue(WIDTH);
                double width = Double.parseDouble(widthStr);
                
                String lengthStr = stageInfoElement.getAttributeValue(LENGTH);
                double length = Double.parseDouble(lengthStr);
                
                boolean unsetDimensions = false;
                if ((width == -1D) || (length == -1D)) {
                    unsetDimensions = true;
                }
                
                String baseLevelStr = stageInfoElement.getAttributeValue(BASE_LEVEL);
                int baseLevel = Integer.parseInt(baseLevelStr);
                
                // Get constructable.
                // Note should be false if constructable attribute doesn't exist.
                boolean constructable = Boolean.parseBoolean(stageInfoElement.getAttributeValue(CONSTRUCTABLE));
                
                // Get salvagable.
                // Note should be false if salvagable attribute doesn't exist.
                boolean salvagable = Boolean.parseBoolean(stageInfoElement.getAttributeValue(SALVAGABLE));
                    
                double workTime = Double.parseDouble(stageInfoElement.getAttributeValue(WORK_TIME));
                // convert work time from Sols to millisols.
                workTime *= 1000D;
                    
                int skillRequired = Integer.parseInt(stageInfoElement.getAttributeValue(SKILL_REQUIRED));
                    
                String prerequisiteStage = null;
                String prerequisiteStageType = null;
                if (ConstructionStageInfo.FRAME.equals(stageType)) 
                    prerequisiteStageType = ConstructionStageInfo.FOUNDATION;
                else if (ConstructionStageInfo.BUILDING.equals(stageType)) 
                    prerequisiteStageType = ConstructionStageInfo.FRAME;
                if (prerequisiteStageType != null) 
                    prerequisiteStage = stageInfoElement.getAttributeValue(prerequisiteStageType);
                    
                List<Element> partList = stageInfoElement.getChildren(PART);
                
                Map<Integer, Integer> parts = new HashMap<>(partList.size());
                for (Element partElement : partList) {
                    String partName = partElement.getAttributeValue(NAME);
                    int partNum = Integer.parseInt(partElement.getAttributeValue(NUMBER));
                    Part part = (Part) ItemResourceUtil.findItemResource(partName);
                    
    				if (part == null)
    					logger.severe(partName + " shows up in constructions.xml but doesn't exist in parts.xml.");
    				else
                        parts.put(ItemResourceUtil.findIDbyItemResourceName(partName), partNum);  

                }
                    
                List<Element> resourceList = stageInfoElement.getChildren(RESOURCE);
                Map<Integer, Double> resources = 
                    new HashMap<>(resourceList.size());
                for (Element resourceElement : resourceList) {
                    String resourceName = resourceElement.getAttributeValue(NAME);
                    double resourceAmount = Double.parseDouble(resourceElement.getAttributeValue(AMOUNT));
                    AmountResource resource = ResourceUtil.findAmountResource(resourceName);
       				if (resource == null)
    					logger.severe(resourceName + " shows up in constructions.xml but doesn't exist in resources.xml.");
    				else
    					resources.put(ResourceUtil.findIDbyAmountResourceName(resourceName), resourceAmount);
                }
                    
                List<Element> vehicleList = stageInfoElement.getChildren(VEHICLE);
                List<ConstructionVehicleType> vehicles = 
                    new ArrayList<ConstructionVehicleType>(vehicleList.size());
                
                for (Element vehicleElement : vehicleList) {
                    String vehicleType = vehicleElement.getAttributeValue(TYPE);
                        
                    Class<? extends Vehicle> vehicleClass = null;
                    if (vehicleType.toLowerCase().indexOf("rover") > -1) vehicleClass = Rover.class;
                    else if (vehicleType.equalsIgnoreCase("light utility vehicle")) 
                        vehicleClass = LightUtilityVehicle.class;
                    else throw new IllegalStateException("Unknown vehicle type: " + vehicleType);
                        
                    List<Element> attachmentPartList = vehicleElement.getChildren(ATTACHMENT_PART);
                    List<Integer> attachmentParts = new ArrayList<>(attachmentPartList.size());
                    for (Element attachmentPartElement : attachmentPartList) {
                        String partName = attachmentPartElement.getAttributeValue(NAME);
                        //Part attachmentPart = (Part) ItemResource.findItemResource(partName);
                        attachmentParts.add(ItemResourceUtil.findIDbyItemResourceName(partName));
                    }
                        
                    vehicles.add(new ConstructionVehicleType(vehicleType, vehicleClass, attachmentParts));
                }
                    
                ConstructionStageInfo stageInfo = new ConstructionStageInfo(name, stageType, width, length, 
                        unsetDimensions, baseLevel, constructable, salvagable, workTime, skillRequired, 
                        prerequisiteStage, parts, resources, vehicles);
                stageInfoList.add(stageInfo);
            }
            catch (Exception e) {
                throw new IllegalStateException("Error reading construction stage " + name + ": " + e.getMessage());
            }
        }
        
        return stageInfoList;
    }
    
    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        constructionDoc = null;

        if(foundationStageInfoList != null){

            Iterator<ConstructionStageInfo> i = foundationStageInfoList.iterator();
            while (i.hasNext()) {
                i.next().destroy();
            }
        }

        if(frameStageInfoList != null){

            Iterator<ConstructionStageInfo> j = frameStageInfoList.iterator();
            while (j.hasNext()) {
                j.next().destroy();
            }
        }

        if(buildingStageInfoList != null){

            Iterator<ConstructionStageInfo> k = buildingStageInfoList.iterator();
            while (k.hasNext()) {
                k.next().destroy();
            }
        }
    }
}