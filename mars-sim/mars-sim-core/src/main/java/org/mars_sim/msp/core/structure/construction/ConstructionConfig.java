/**
 * Mars Simulation Project
 * ConstructionConfig.java
 * @version 3.00 2010-08-18
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.construction;

import org.jdom.Document;
import org.jdom.Element;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Parses construction configuration file.
 */
public class ConstructionConfig implements Serializable {

    // Element names
    private static final String NAME = "name";
    private static final String WIDTH = "width";
    private static final String LENGTH = "length";
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
    List<ConstructionStageInfo> getConstructionStageInfoList(String stageType) {
        
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
        
        return new ArrayList<ConstructionStageInfo>(stageInfo);
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
                    
                // Get name.
                name = stageInfoElement.getAttributeValue(NAME);
                
                double width = 0D;
                String widthStr = stageInfoElement.getAttributeValue(WIDTH);
                if (!widthStr.equals("*")) width = Double.parseDouble(widthStr);
                
                double length = 0D;
                String lengthStr = stageInfoElement.getAttributeValue(LENGTH);
                if (!lengthStr.equals("*")) length = Double.parseDouble(lengthStr);
                
                boolean unsetDimensions = false;
                if (widthStr.equals("*") || lengthStr.equals("*")) unsetDimensions = true;
                
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
                
                Map<Part, Integer> parts = new HashMap<Part, Integer>(partList.size());
                for (Element partElement : partList) {
                    String partName = partElement.getAttributeValue(NAME);
                    int partNum = Integer.parseInt(partElement.getAttributeValue(NUMBER));
                    Part part = (Part) ItemResource.findItemResource(partName);
                    parts.put(part, partNum);
                }
                    
                List<Element> resourceList = stageInfoElement.getChildren(RESOURCE);
                Map<AmountResource, Double> resources = 
                    new HashMap<AmountResource, Double>(resourceList.size());
                for (Element resourceElement : resourceList) {
                    String resourceName = resourceElement.getAttributeValue(NAME);
                    double resourceAmount = Double.parseDouble(resourceElement.getAttributeValue(AMOUNT));
                    AmountResource resource = AmountResource.findAmountResource(resourceName);
                    resources.put(resource, resourceAmount);
                }
                    
                List<Element> vehicleList = stageInfoElement.getChildren(VEHICLE);
                List<ConstructionVehicleType> vehicles = 
                    new ArrayList<ConstructionVehicleType>(vehicleList.size());
                
                for (Element vehicleElement : vehicleList) {
                    String vehicleType = vehicleElement.getAttributeValue(TYPE);
                        
                    Class vehicleClass = null;
                    if (vehicleType.toLowerCase().indexOf("rover") > -1) vehicleClass = Rover.class;
                    else if (vehicleType.equalsIgnoreCase("light utility vehicle")) 
                        vehicleClass = LightUtilityVehicle.class;
                    else throw new IllegalStateException("Unknown vehicle type: " + vehicleType);
                        
                    List<Element> attachmentPartList = vehicleElement.getChildren(ATTACHMENT_PART);
                    List<Part> attachmentParts = new ArrayList<Part>(attachmentPartList.size());
                    for (Element attachmentPartElement : attachmentPartList) {
                        String partName = attachmentPartElement.getAttributeValue(NAME);
                        Part attachmentPart = (Part) ItemResource.findItemResource(partName);
                        attachmentParts.add(attachmentPart);
                    }
                        
                    vehicles.add(new ConstructionVehicleType(vehicleType, vehicleClass, attachmentParts));
                }
                    
                ConstructionStageInfo stageInfo = new ConstructionStageInfo(name, stageType, width, length, 
                        unsetDimensions, constructable, salvagable, workTime, skillRequired, prerequisiteStage, 
                        parts, resources, vehicles);
                stageInfoList.add(stageInfo);
            }
            catch (Exception e) {
                throw new IllegalStateException("Error reading construction stage " + name + ": " + e.getMessage());
            }
        }
        
        return stageInfoList;
    }
}