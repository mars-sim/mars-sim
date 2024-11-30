/*
 * Mars Simulation Project
 * ConstructionConfig.java
 * @date 2022-08-09
 * @author Scott Davis
 */

package com.mars_sim.core.structure.construction;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;

import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.structure.construction.ConstructionStageInfo.Stage;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;


/**
 * Parses construction configuration file.
 */
public class ConstructionConfig {

    // Element names
    private static final String NAME = "name";
    private static final String WIDTH = "width";
    private static final String LENGTH = "length";
	private static final String N_S_ALIGNMENT = "north-south-alignment";    
    private static final String BASE_LEVEL = "base-level";
    private static final String CONSTRUCTABLE = "constructable";
    private static final String SALVAGABLE = "salvagable";
    private static final String WORK_TIME = "work-time";
    private static final String SKILL_REQUIRED = "skill-required";
    private static final String PART = "part";
    private static final String RESOURCE = "resource";
    private static final String VEHICLE = "vehicle";
    private static final String TYPE = "type";
    private static final String ATTACHMENT_PART = "attachment-part";

    // Data members
    private Map<Stage,List<ConstructionStageInfo>> stageInfos = new EnumMap<>(Stage.class);
    private List<ConstructionStageInfo> allConstructionStageInfoList;
	
    private List<Integer> constructionParts;
    private List<Integer> constructionResources;
    
    /**
     * Constructor.
     * 
     * @param constructionDoc DOM document with construction configuration
     */
    public ConstructionConfig(Document constructionDoc) {
        Map<String, ConstructionStageInfo> loaded = new HashMap<>();
        for(var e : Stage.values()) {
            stageInfos.put(e, createConstructionStageInfoList(constructionDoc, e, loaded));
        }
        allConstructionStageInfoList = new ArrayList<>(loaded.values());
    }

    /**
     * Gets a list of construction stage infos.
     *
     * @param stageType the type of stage.
     * @return list of construction stage infos.
     * @throws Exception if error parsing list.
     */
    public List<ConstructionStageInfo> getConstructionStageInfoList(Stage stageType) {
        return stageInfos.get(stageType);
    }

    /**
     * Find a stage info by it's name
     * @param name
     * @return
     */
    public ConstructionStageInfo getConstructionStageInfoByName(String name) {
        return allConstructionStageInfoList.stream()
                    .filter(s -> name.equals(s.getName()))
                    .findAny().orElseGet(null);
    }
	
    /**
     * Get all stages which follow the specified Stage. 
     * @param start
     * @return
     */
    public List<ConstructionStageInfo> getPotentialNextStages(ConstructionStageInfo start) {
        return allConstructionStageInfoList.stream()
                    .filter(s -> start.equals(s.getPrerequisiteStage()))
                    .toList();
    }

    /**
     * Creates a stage info list.
     *
     * @param constructionDoc
     * @param stage the stage type.
     * @param loadedStage Stages already loaded keyed by name
     * @return list of construction stage infos.
     * @throws Exception if error parsing XML file.
     */
	private List<ConstructionStageInfo> createConstructionStageInfoList(Document constructionDoc,
                        Stage stage, Map<String,ConstructionStageInfo> loadedStages) {

		List<ConstructionStageInfo> stageInfoList = new ArrayList<>();
		
        String stageType = stage.name().toLowerCase();
        Element stageInfoListElement = constructionDoc.getRootElement().getChild(stageType + "-list");
        List<Element> stageInfoNodes = stageInfoListElement.getChildren(stageType);

        for (Element stageInfoElement : stageInfoNodes) {
            String name = stageInfoElement.getAttributeValue(NAME);
            int baseLevel = ConfigHelper.getAttributeInt(stageInfoElement, BASE_LEVEL);
            int skillRequired = ConfigHelper.getAttributeInt(stageInfoElement, SKILL_REQUIRED);
            String alignment = stageInfoElement.getAttributeValue(N_S_ALIGNMENT);

            double width = ConfigHelper.getAttributeDouble(stageInfoElement, WIDTH);
            double length = ConfigHelper.getAttributeDouble(stageInfoElement, LENGTH);	
            boolean unsetDimensions = (width == -1D) || (length == -1D);

            // Get constructable.
            // Note should be false if constructable attribute doesn't exist.
            boolean constructable = ConfigHelper.getAttributeBool(stageInfoElement, CONSTRUCTABLE);

            // Get salvagable.
            // Note should be false if salvagable attribute doesn't exist.
            boolean salvagable = ConfigHelper.getAttributeBool(stageInfoElement, SALVAGABLE);

            // convert work time from sols to millisols.
            double workTime = ConfigHelper.getAttributeDouble(stageInfoElement, WORK_TIME) * 1000D;

            // Find any prestage that must be earlier that this one
            ConstructionStageInfo preStage = null;
            for(Stage s : Stage.values()) {
                var preStageName = stageInfoElement.getAttributeValue(s.name().toLowerCase());
                if (preStageName != null) {
                    // Check correct sequence
                    if (s.ordinal() >= stage.ordinal()) {
                        throw new IllegalStateException("Construction stage " + name
                                            + " references a prestage from a later stage type.");
                    }
                    preStage = loadedStages.get(preStageName);
                    break;
                }
            }

            String context = "Construction " + name;
            List<Element> partList = stageInfoElement.getChildren(PART);
            Map<Integer, Integer> parts = ConfigHelper.parsePartListById(context, partList);

            List<Element> resourceList = stageInfoElement.getChildren(RESOURCE);
            Map<Integer, Double> resources = ConfigHelper.parseResourceListById(context,
                                        resourceList);

            List<Element> vehicleList = stageInfoElement.getChildren(VEHICLE);
            List<ConstructionVehicleType> vehicles = new ArrayList<>(vehicleList.size());
            for (Element vehicleElement : vehicleList) {
                String vehicleType = vehicleElement.getAttributeValue(TYPE);

                Class<? extends Vehicle> vehicleClass = null;
                if (vehicleType.toLowerCase().contains("rover")) vehicleClass = Rover.class;
                else if (vehicleType.equalsIgnoreCase(LightUtilityVehicle.NAME))
                    vehicleClass = LightUtilityVehicle.class;
                else throw new IllegalStateException("Unknown vehicle type: " + vehicleType);

                List<Element> attachmentPartList = vehicleElement.getChildren(ATTACHMENT_PART);
                List<Integer> attachmentParts = new ArrayList<>(attachmentPartList.size());
                for (Element attachmentPartElement : attachmentPartList) {
                    String partName = attachmentPartElement.getAttributeValue(TYPE);
                    attachmentParts.add(ItemResourceUtil.findIDbyItemResourceName(partName));
                }

                vehicles.add(new ConstructionVehicleType(vehicleType, vehicleClass, attachmentParts));
            }

            ConstructionStageInfo stageInfo = new ConstructionStageInfo(name, stage, width, length,
                    alignment, unsetDimensions, baseLevel, constructable, salvagable, workTime, skillRequired,
                    preStage, parts, resources, vehicles);
            stageInfoList.add(stageInfo);

            if (loadedStages.containsKey(name)) {
                throw new IllegalStateException("Construction stage name is not unique:" + name);
            }
            loadedStages.put(name, stageInfo);
        }

        return stageInfoList;
    }

	/**
	 * Gets a list of all construction stage info available.
	 * 
	 * @return list of construction stage info.
	 * @throws Exception if error getting list.
	 */
	public List<ConstructionStageInfo> getAllConstructionStageInfoList() {
        return allConstructionStageInfoList;
	}


	/**
	 * Determines all resources needed for construction projects.
	 *
	 * @return
	 */
	public List<Integer> determineConstructionResources() {
		
		if (constructionResources == null) {
			List<Integer> resources = new ArrayList<>();

			for(ConstructionStageInfo info : allConstructionStageInfoList) {
				if (info.isConstructable()) {
					for(Integer resource : info.getResources().keySet()) {
						if (!resources.contains(resource)) {
							resources.add(resource);
						}
					}
				}
			}
			
			constructionResources = resources;
		}
		
		return constructionResources;
	}

	/**
	 * Determines all parts needed for construction projects.
	 * 
	 * @return
	 */
	public List<Integer> determineConstructionParts() {
		
		if (constructionParts == null) {
			
			List<Integer> parts = new ArrayList<>();
	
			for(ConstructionStageInfo info : allConstructionStageInfoList) {
				if (info.isConstructable()) {
					for(Integer part : info.getParts().keySet()) {
						if (!parts.contains(part)) {
							parts.add(part);
						}
					}
				}
			}
			
			constructionParts = parts;
		}
		
		return constructionParts;
	}
}
