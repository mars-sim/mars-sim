/*
 * Mars Simulation Project
 * SettlementTemplateConfig.java
 * @date 2025-09-30
 * @author Barry Evans
 */

package com.mars_sim.core.structure;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.authority.AuthorityFactory;
import com.mars_sim.core.building.BuildingPackageConfig;
import com.mars_sim.core.building.BuildingTemplate;
import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.configuration.UserConfigurableConfig;
import com.mars_sim.core.interplanetary.transport.resupply.ResupplyConfig;
import com.mars_sim.core.interplanetary.transport.resupply.ResupplyManifest;
import com.mars_sim.core.interplanetary.transport.resupply.ResupplySchedule;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.ai.shift.ShiftPattern;
import com.mars_sim.core.resource.*;
import com.mars_sim.core.robot.RobotTemplate;
import com.mars_sim.core.robot.RobotType;

import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Configuration loader that creates SettlementTemplates
 */
public class SettlementTemplateConfig extends UserConfigurableConfig<SettlementTemplate> {

    private static final String BUILDING_PACKAGE = "building-package";
    private static final String BUILDING = "building";
    private static final String CONNECTOR = "connector";
    private static final String STANDALONE = "standalone";

    private static final String SETTLEMENT_TEMPLATE_LIST = "settlement-template-list";
    private static final String TEMPLATE = "template";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String DEFAULT_POPULATION = "default-population";
    private static final String DEFAULT_NUM_ROBOTS = "number-of-robots";
    private static final String OBJECTIVE = "objective";

    private static final String ID = "id";
    private static final String HATCH_FACE = "hatch-facing";
    private static final String ZONE = "zone";
    private static final String TYPE = "type";
    private static final String AMOUNT = "amount";
    private static final String CONNECTION_LIST = "connection-list";
    private static final String CONNECTION = "connection";
    private static final String NUMBER = "number";
    private static final String VEHICLE = "vehicle";
    private static final String EQUIPMENT = "equipment";
    private static final String BIN = "bin";
    private static final String SPONSOR = "sponsor";
    private static final String RESUPPLY = "resupply";
    private static final String RESUPPLY_MISSION = "resupply-mission";
    private static final String PART = "part";
    private static final String PART_PACKAGE = "part-package";
    private static final String RESOURCE = "resource";


    private static final String SHIFT_PATTERN = "shift-pattern";
    private static final String MODEL = "model";
    private static final String MANIFEST_NAME = "manifest-name";
    private static final String SCHEDULE = "schedule";
    private static final String ACTIVITY_SCHEDULE = "activity-schedule";

    private static final String EVA = "EVA";
    private static final String ROBOT = "robot";

    private final PartPackageConfig partPackageConfig;
    private final BuildingPackageConfig buildingPackageConfig;
    private final ResupplyConfig resupplyConfig;
    private final AuthorityFactory authorityConfig;
    private final SettlementConfig settlementConfig;

    /**
     * Constructor.
     *
     * @param settlementDoc     DOM document with settlement configuration.
     * @param resupplyConfig 
     * @param buildingPackageConfig 
     * @param partPackageConfig
     * @param config          simulation configuration.
     */
    public SettlementTemplateConfig(Document settlementDoc, PartPackageConfig partPackageConfig,
                                            BuildingPackageConfig buildingPackageConfig,
                                            ResupplyConfig resupplyConfig, SimulationConfig config) {
        super("settlement");
        this.partPackageConfig = partPackageConfig;
        this.buildingPackageConfig = buildingPackageConfig;
        this.resupplyConfig = resupplyConfig;
        this.settlementConfig = config.getSettlementConfiguration();
        this.authorityConfig = config.getReportingAuthorityFactory();
        setXSDName("settlement.xsd", config);

        loadDefaults(loadSettlementTemplates(settlementDoc));

        loadUserDefined();
    }

    /**
     * Get. the manifest that are used in the Settlement templates.
     * 
     * @return
     */
    public List<ResupplyManifest> getSupplyManifests() {
        return resupplyConfig.getAll();
    }

    /**
     * Loads the settlement templates from the XML document.
     *
     * @param settlementDoc     DOM document with settlement configuration.
     * @throws Exception if error reading XML document.
     */
    private String[] loadSettlementTemplates(Document settlementDoc) {
        Element root = settlementDoc.getRootElement();
        Element templateList = root.getChild(SETTLEMENT_TEMPLATE_LIST);
        List<Element> templateNodes = templateList.getChildren(TEMPLATE);
        List<String> names = new ArrayList<>();

        for (Element templateElement : templateNodes) {
            names.add(templateElement.getAttributeValue(NAME));
        }
        return names.toArray(new String[0]);
    }

    /**
     * Parses the building or connector list.
     *
     * @param templateElement
     * @param settlementTemplate
     * @param settlementTemplateName
     * @param existingStreetIDs
     */
    private void parseBuildingORConnectorList(Element templateElement,
                                              List<BuildingTemplate> buildings,
                                              String elementName,
                                              String settlementTemplateName,
                                              Set<String> existingStreetIDs,
                                              Map<String, Integer> buildingTypeNumMap) {

        List<Element> buildingNodes = templateElement.getChildren(elementName);
        for (Element buildingElement : buildingNodes) {

            BoundedObject bounds = ConfigHelper.parseBoundedObject(buildingElement);

            // Track the id
            String id = "";

            if (buildingElement.getAttribute(ID) != null) {
                id = buildingElement.getAttributeValue(ID);
            }

            if (existingStreetIDs.contains(id)) {
                throw new IllegalStateException(
                        "Error in SettlementConfig: the id " + id + " in settlement template "
                                + settlementTemplateName + " is not unique.");
            } else if (!id.equalsIgnoreCase("")) {
                existingStreetIDs.add(id);
            }

            // Assume the zone as 0
            int zone = ConfigHelper.getOptionalAttributeInt(buildingElement, ZONE, 0);

            // Get the building type
            String buildingType = buildingElement.getAttributeValue(TYPE);

            int last = getNextBuildingTypeID(buildingType, buildingTypeNumMap);

            // e.g. Lander Hab 1, Lander Hab 2
            String uniqueName = buildingType + " " + last;

            BuildingTemplate buildingTemplate = new BuildingTemplate(id, zone,
                    buildingType, uniqueName, bounds);

            // Need to check for collision with previous building templates
            for (BuildingTemplate t: buildings) {
                BoundedObject o0 = buildingTemplate.getBounds();
                BoundedObject o1 = t.getBounds();
                if (BoundedObject.isCollided(o0, o1)) {
                    throw new IllegalStateException(uniqueName + " collided with " + t.getBuildingName()
                            + " in settlement template: " + settlementTemplateName + ".");
                }
            }

            buildings.add(buildingTemplate);

            // Create building connection templates.
            Element connectionListElement = buildingElement.getChild(CONNECTION_LIST);
            if (connectionListElement != null) {
                List<Element> connectionNodes = connectionListElement.getChildren(CONNECTION);
                for (Element connectionElement : connectionNodes) {
                    String connectionID = connectionElement.getAttributeValue(ID);

                    if (buildingType.equalsIgnoreCase(EVA)) {
                        buildingTemplate.addEVAAttachedBuildingID(connectionID);
                    }

                    // Check that connection ID is not the same as the building ID.
                    if (connectionID.equalsIgnoreCase(id)) {
                        throw new IllegalStateException(
                                "Connection ID cannot be the same as id for this building/connector "
                                        + buildingType
                                        + " in settlement template: " + settlementTemplateName + ".");
                    }

                    String hatchFace = connectionElement.getAttributeValue(HATCH_FACE);

                    if (hatchFace == null) {
                        LocalPosition connectionLoc = ConfigHelper.parseLocalPosition(connectionElement);
                        buildingTemplate.addBuildingConnection(connectionID, connectionLoc);
                    }
                    else {
                        buildingTemplate.addBuildingConnection(connectionID, hatchFace);
                    }
                }
            }
        }
    }

    /**
     * Gets an available building type suffix ID for a new building.
     *
     * @param buildingType
     * @return type ID (starting from 1, not zero)
     */
    private int getNextBuildingTypeID(String buildingType, Map<String, Integer> buildingTypeIDMap) {
        int last = 1;
        if (buildingTypeIDMap.containsKey(buildingType)) {
            last = buildingTypeIDMap.get(buildingType);
            buildingTypeIDMap.put(buildingType, ++last);
        } else {
            buildingTypeIDMap.put(buildingType, last);
        }
        return last;
    }


    /**
     * Parses the item XML.
     * 
     * @param doc
     * @param predefined
     */
    @Override
    protected SettlementTemplate parseItemXML(Document doc, boolean predefined) {
        Element templateElement = doc.getRootElement();

        String settlementTemplateName = templateElement.getAttributeValue(NAME);
        String description = templateElement.getAttributeValue(DESCRIPTION);
        String sponsorName = templateElement.getAttributeValue(SPONSOR);
        Authority sponsor = authorityConfig.getItem(sponsorName);
        if (sponsor == null) {
            throw new IllegalArgumentException("Unknown sponsor:" + sponsorName
                                + " in Settlement " + settlementTemplateName);
        }


        // Obtains the default population
        int defaultPopulation = Integer.parseInt(templateElement.getAttributeValue(DEFAULT_POPULATION));
        // Obtains the default numbers of robots
        int defaultNumOfRobots = Integer.parseInt(templateElement.getAttributeValue(DEFAULT_NUM_ROBOTS));

        // Look up the shift pattern
        ShiftPattern pattern = null;
        String shiftPattern = templateElement.getAttributeValue(SHIFT_PATTERN);
        if (shiftPattern == null) {
            pattern = settlementConfig.getShiftByPopulation(defaultPopulation);
        }
        else {
            pattern = settlementConfig.getShiftByName(shiftPattern);
        }

        GroupActivitySchedule activitySchedule = null;
        String scheduleName = templateElement.getAttributeValue(ACTIVITY_SCHEDULE);
        if (scheduleName != null) {
            activitySchedule = settlementConfig.getActivityByName(scheduleName);
        }
        else {
            activitySchedule = settlementConfig.getActivityByPopulation(defaultPopulation);
        }

 
        List<BuildingTemplate> buildingTemplates = new ArrayList<>();
        Set<String> existingBuildingIDs = new HashSet<>();
        Map<String, Integer> buildingTypeNumMap = new HashMap<>();

        // Process a list of buildings
        parseBuildingORConnectorList(templateElement, buildingTemplates,
                BUILDING,
                settlementTemplateName,
                existingBuildingIDs,
                buildingTypeNumMap);

        // Process a list of connectors
        Set<String> existingConnectorIDs = new HashSet<>();
        parseBuildingORConnectorList(templateElement, buildingTemplates,
                CONNECTOR,
                settlementTemplateName,
                existingConnectorIDs,
                buildingTypeNumMap);

        // Process a list of standalone buildings
        parseBuildingORConnectorList(templateElement, buildingTemplates,
                STANDALONE,
                settlementTemplateName,
                existingBuildingIDs,
                buildingTypeNumMap);

       // Load building packages
       List<Element> buildingPackageNodes = templateElement.getChildren(BUILDING_PACKAGE);
       for (Element buildingPackageElement : buildingPackageNodes) {
           String packageName = buildingPackageElement.getAttributeValue(NAME);

           List<BuildingTemplate> buildingPackages = buildingPackageConfig.getBuildingsInPackage(packageName);

           for (BuildingTemplate buildingTemplate: buildingPackages) {

               // Get the building type
               String buildingType = buildingTemplate.getBuildingType();

               int last = getNextBuildingTypeID(buildingType, buildingTypeNumMap);

               String uniqueName = buildingType + " " + last;

               // Overwrite with a new building nick name
               buildingTemplates.add(new BuildingTemplate(uniqueName, buildingTemplate));
           }
       }

        // Check that building connections point to valid building ID's.
        for (BuildingTemplate buildingTemplate : buildingTemplates) {

            List<BuildingTemplate.BuildingConnectionTemplate> connectionTemplates = buildingTemplate
                    .getBuildingConnectionTemplates();

            for (BuildingTemplate.BuildingConnectionTemplate connectionTemplate : connectionTemplates) {

                if (!existingBuildingIDs.contains(connectionTemplate.getID())
                        && !existingConnectorIDs.contains(connectionTemplate.getID())) {
                    throw new IllegalStateException("XML issues with settlement template: "
                            + settlementTemplateName
                            + " in " + buildingTemplate.getBuildingName()
                            + " at connection id " + connectionTemplate.getID()
                            + ". existingBuildingIDs: " + existingBuildingIDs
                            + ". existingConnectorIDs: " + existingConnectorIDs
                    );
                }
            }
        }

        // Get supplies
        var supplies = parseSupplies("Settlement template " + settlementTemplateName, templateElement,
                            buildingTemplates, partPackageConfig);

        // Add templateID
        SettlementTemplate settlementTemplate = new SettlementTemplate(
                settlementTemplateName,
                description,
                predefined,
                sponsor,
                pattern,
                activitySchedule,
                defaultPopulation,
                defaultNumOfRobots,
                supplies);

        // Check the objective
        String objectiveText = templateElement.getAttributeValue(OBJECTIVE);
        if (objectiveText != null) {
            var oType = ConfigHelper.getEnum(ObjectiveType.class, objectiveText);
            settlementTemplate.setObjective(oType);
        }

        // Load robots
        List<Element> robotNodes = templateElement.getChildren(ROBOT);
        for (Element robotElement : robotNodes) {
            RobotType rType = ConfigHelper.getEnum(RobotType.class,
                    robotElement.getAttributeValue(TYPE));
            String name = robotElement.getAttributeValue(NAME);
            String model = robotElement.getAttributeValue(MODEL);
            settlementTemplate.addRobot(new RobotTemplate(name, rType, model));
        }

        // Load resupplies
        Element resupplyList = templateElement.getChild(RESUPPLY);
        if (resupplyList != null) {
            List<Element> resupplyNodes = resupplyList.getChildren(RESUPPLY_MISSION);
            for (Element resupplyMissionElement : resupplyNodes) {
                String resupplyName = resupplyMissionElement.getAttributeValue(NAME);
                String manifestName = resupplyMissionElement.getAttributeValue(MANIFEST_NAME);
                ResupplyManifest manifest = resupplyConfig.getSupplyManifest(manifestName);

                var schedule = ConfigHelper.parseEventCalendar(resupplyMissionElement.getChild(SCHEDULE));
                ResupplySchedule resupplyMissionTemplate = new ResupplySchedule(resupplyName, schedule, manifest);
                settlementTemplate.addResupplyMissionTemplate(resupplyMissionTemplate);
            }
        }

        return settlementTemplate;
    }

    /**
     * Parses an XML to create a Settlement Supply instance. The buildings are created externally.
     * 
     * @param context The conext this is being parsed
     * @param supplyElement The XML element
     * @param newBuildings The list of building templates
     * @param partPackageConfig2 
     * @return
     */
    public static SettlementSupplies parseSupplies(String context, Element supplyElement,
                                            List<BuildingTemplate> newBuildings,
                                            PartPackageConfig packageConfig) {

        // Load equipment
        Map<String, Integer> newEquipment = ConfigHelper.parseIntList(context, supplyElement.getChildren(EQUIPMENT),
                            TYPE, s -> s, NUMBER);

        // Load bins
        Map<String, Integer> newBins = ConfigHelper.parseIntList(context, supplyElement.getChildren(BIN),
                            TYPE, s -> s, NUMBER);

        // Load resources
        Map<AmountResource, Double> newResources = ConfigHelper.parseDoubleList(context, supplyElement.getChildren(RESOURCE),
                                    TYPE, ResourceUtil::findAmountResource,
                                    AMOUNT);

        // Load vehicles
        Map<String, Integer> newVehicles = ConfigHelper.parseIntList(context, supplyElement.getChildren(VEHICLE),
                                                    TYPE, s -> s, NUMBER);

        // Load parts
        Map<Part, Integer> newParts = ConfigHelper.parseIntList(context, supplyElement.getChildren(PART),
                                            TYPE, s -> (Part) ItemResourceUtil.findItemResource(s),
                                            NUMBER);

        // Load part packages                      
        List<Element> partPackageNodes = supplyElement.getChildren(PART_PACKAGE);
        for (Element partPackageElement : partPackageNodes) {
            String packageName = partPackageElement.getAttributeValue(NAME);
            int packageNumber = ConfigHelper.getAttributeInt(partPackageElement, NUMBER);
            if (packageNumber > 0) {
                Map<Part, Integer> partPackage = packageConfig.getPartsInPackage(packageName);
                for (var pp : partPackage.entrySet()) {
                    int partNumber = pp.getValue();
                    newParts.merge(pp.getKey(), partNumber * packageNumber, (v1, v2) -> v1 + v2);
                }
            }
        }
        
        return new SettlementSuppliesImpl(newBuildings, newVehicles, newEquipment, newBins,
                                    newResources, newParts);
    }

    /**
     * It is not possible to create new SettlementTemplates via the application.
     */
    @Override
    protected Document createItemDoc(SettlementTemplate item) {
        throw new UnsupportedOperationException("Saving Settlement templates is not supported.");
    }
}
