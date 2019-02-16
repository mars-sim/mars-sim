/**
 * Mars Simulation Project
 * ResupplyConfig.java
 * @version 3.1.0 2017-02-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport.resupply;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.PartPackageConfig;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.BuildingTemplate;

/**
 * Provides configuration information about settlement resupply missions. Uses a
 * DOM document to get the information.
 */
public class ResupplyConfig implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    // Element names
    private static final String RESUPPLY = "resupply";
    private static final String NAME = "name";
    private static final String BUILDING = "building";
    private static final String WIDTH = "width";
    private static final String LENGTH = "length";
    private static final String X_LOCATION = "x-location";
    private static final String Y_LOCATION = "y-location";
    private static final String FACING = "facing";
    private static final String VEHICLE = "vehicle";
    private static final String EQUIPMENT = "equipment";
    private static final String PERSON = "person";
    private static final String RESOURCE = "resource";
    private static final String PART = "part";
    private static final String PART_PACKAGE = "part-package";
    private static final String TYPE = "type";
    private static final String NUMBER = "number";
    private static final String AMOUNT = "amount";

    // Data members
    Collection<ResupplyTemplate> resupplyTemplates;

    /**
     * Constructor
     *
     * @param resupplyDoc DOM document for resupply configuration.
     * @param partPackageConfig the part package configuration.
     */
    public ResupplyConfig(Document resupplyDoc,
            PartPackageConfig partPackageConfig) {

    	// Initialize amountResourceConfig in this constructor
    	ResourceUtil.getInstance().initializeNewSim();

        resupplyTemplates = new ArrayList<ResupplyTemplate>();
        loadResupplyTemplates(resupplyDoc, partPackageConfig);
    }

    /**
     * Loads the resupply templates.
     * @param resupplyDoc DOM document for resupply configuration.
     * @param partPackageConfig the part package configuration.
     */
    private void loadResupplyTemplates(Document resupplyDoc,
            PartPackageConfig partPackageConfig) {

        Element root = resupplyDoc.getRootElement();
        List<Element> resupplyNodes = root.getChildren(RESUPPLY);
        for (Element resupplyElement : resupplyNodes) {
            ResupplyTemplate template = new ResupplyTemplate();
            resupplyTemplates.add(template);

            template.name = resupplyElement.getAttributeValue(NAME);

            // Load buildings
            List<Element> buildingNodes = resupplyElement.getChildren(BUILDING);
            for (Element buildingElement : buildingNodes) {
                String buildingType = buildingElement.getAttributeValue(TYPE);
                double width = -1D;
                if (buildingElement.getAttribute(WIDTH) != null) {
                    width = Double.parseDouble(buildingElement
                            .getAttributeValue(WIDTH));
                }

                // Determine optional length attribute value. "-1" if it doesn't
                // exist.
                double length = -1D;
                if (buildingElement.getAttribute(LENGTH) != null) {
                    length = Double.parseDouble(buildingElement
                            .getAttributeValue(LENGTH));
                }

                double xLoc = Double.parseDouble(buildingElement
                        .getAttributeValue(X_LOCATION));
                double yLoc = Double.parseDouble(buildingElement
                        .getAttributeValue(Y_LOCATION));
                double facing = Double.parseDouble(buildingElement
                        .getAttributeValue(FACING));

                String scenario = "A";
                //if (NAME.toLowerCase().equals("Mars Direct Base Resupply 3".toLowerCase()))
                //	scenario = "A";
                // TODO: need to rework how "scenario" and "scenarioID" are applied

                // 2014-10-28 Added buildingType (at the buildingNickName position)
                template.buildings.add(new BuildingTemplate(template.name, 0, scenario, buildingType,
                        buildingType, width, length, xLoc, yLoc, facing));

            }

            // Load vehicles
            List<Element> vehicleNodes = resupplyElement.getChildren(VEHICLE);
            for (Element vehicleElement : vehicleNodes) {
                String vehicleType = vehicleElement.getAttributeValue(TYPE);
                int vehicleNumber = Integer.parseInt(vehicleElement
                        .getAttributeValue(NUMBER));
                if (template.vehicles.containsKey(vehicleType))
                    vehicleNumber += template.vehicles.get(vehicleType);
                template.vehicles.put(vehicleType, vehicleNumber);
            }

            // Load equipment
            List<Element> equipmentNodes = resupplyElement
                    .getChildren(EQUIPMENT);
            for (Element equipmentElement : equipmentNodes) {
                String equipmentType = equipmentElement.getAttributeValue(TYPE);
                int equipmentNumber = Integer.parseInt(equipmentElement
                        .getAttributeValue(NUMBER));
                if (template.equipment.containsKey(equipmentType))
                    equipmentNumber += template.equipment.get(equipmentType);
                template.equipment.put(equipmentType, equipmentNumber);
            }

            // Load people
            List<Element> personNodes = resupplyElement.getChildren(PERSON);
            for (Element personElement : personNodes) {
                int personNumber = Integer.parseInt(personElement
                        .getAttributeValue(NUMBER));
                template.people += personNumber;
            }

            // Load resources
            List<Element> resourceNodes = resupplyElement.getChildren(RESOURCE);
            for (Element resourceElement : resourceNodes) {
                String resourceName = resourceElement.getAttributeValue(NAME);
                //System.out.println("resourceName is " + resourceName);
                AmountResource resource = ResourceUtil.findAmountResource(resourceName);
                double resourceAmount = Double.parseDouble(resourceElement
                        .getAttributeValue(AMOUNT));
                if (template.resources.containsKey(resource))
                    resourceAmount += template.resources.get(resource);
                template.resources.put(resource, resourceAmount);
            }

            // Load parts
            List<Element> partNodes = resupplyElement.getChildren(PART);
            for (Element partElement : partNodes) {
                String partType = partElement.getAttributeValue(TYPE);
                Part part = (Part) (ItemResourceUtil.findItemResource(partType));
                int partNumber = Integer.parseInt(partElement
                        .getAttributeValue(NUMBER));
                if (template.parts.containsKey(part))
                    partNumber += template.parts.get(part);
                template.parts.put(part, partNumber);
            }

            // Load part packages
            List<Element> partPackageNodes = resupplyElement
                    .getChildren(PART_PACKAGE);

            for (Element partPackageElement : partPackageNodes) {
                String packageName = partPackageElement.getAttributeValue(NAME);
                int packageNumber = Integer.parseInt(partPackageElement
                        .getAttributeValue(NUMBER));
                if (packageNumber > 0) {
                    for (int z = 0; z < packageNumber; z++) {
                        Map<Part, Integer> partPackage = partPackageConfig
                                .getPartsInPackage(packageName);
                        Iterator<Part> i = partPackage.keySet().iterator();
                        while (i.hasNext()) {
                            Part part = i.next();
                            int partNumber = partPackage.get(part);
                            if (template.parts.containsKey(part))
                                partNumber += template.parts.get(part);
                            template.parts.put(part, partNumber);
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the resupply template for a resupply mission name.
     * @param resupplyName the resupply mission name.
     * @return the resupply template.
     */
    private ResupplyTemplate getResupplyTemplate(String resupplyName) {

        ResupplyTemplate result = null;

        Iterator<ResupplyTemplate> i = resupplyTemplates.iterator();
        while (i.hasNext()) {
            ResupplyTemplate template = i.next();
            if (template.name.equals(resupplyName)) {
                result = template;
            }
        }

        if (result == null)
            throw new IllegalArgumentException("resupplyName: " + resupplyName
                    + " not found.");

        return result;
    }

    /**
     * Gets a list of all building templates in the resupply mission.
     * @param resupplyName the resupply mission name.
     * @return list of building templates.
     */
    public List<BuildingTemplate> getResupplyBuildings(String resupplyName) {

        List<BuildingTemplate> result = new ArrayList<BuildingTemplate>();
        ResupplyTemplate foundTemplate = getResupplyTemplate(resupplyName);
        if (foundTemplate != null) {
            result = new ArrayList<BuildingTemplate>(foundTemplate.buildings);
        }
        return result;
    }

    /**
     * Gets a list of vehicle types in the resupply mission.
     * @param resupplyName name of the resupply mission.
     * @return list of vehicle types as strings.
     */
    public List<String> getResupplyVehicleTypes(String resupplyName) {

        ResupplyTemplate foundTemplate = getResupplyTemplate(resupplyName);
        List<String> result = new ArrayList<String>();
        Iterator<String> j = foundTemplate.vehicles.keySet().iterator();
        while (j.hasNext()) {
            String vehicleType = j.next();
            int vehicleNumber = foundTemplate.vehicles.get(vehicleType);
            for (int x = 0; x < vehicleNumber; x++)
                result.add(vehicleType);
        }
        return result;
    }

    /**
     * Gets the equipment types in a resupply mission.
     * @param resupplyName the name of the resupply mission.
     * @return map of equipment types and number.
     */
    public Map<String, Integer> getResupplyEquipment(String resupplyName) {
        ResupplyTemplate foundTemplate = getResupplyTemplate(resupplyName);
        return new HashMap<String, Integer>(foundTemplate.equipment);
    }

    /**
     * Gets the number of immigrants in a resupply mission.
     * @param resupplyName name of the resupply mission.
     * @return number of immigrants
     */
    public int getNumberOfResupplyImmigrants(String resupplyName) {
        ResupplyTemplate foundTemplate = getResupplyTemplate(resupplyName);
        return foundTemplate.people;
    }

    /**
     * Gets a map of parts and their number in a resupply mission.
     * @param resupplyName the name of the resupply mission.
     * @return map of parts and their numbers.
     */
    public Map<Part, Integer> getResupplyParts(String resupplyName) {
        ResupplyTemplate foundTemplate = getResupplyTemplate(resupplyName);
        return new HashMap<Part, Integer>(foundTemplate.parts);
    }

    /**
     * Gets a map of resources and their amounts in a resupply mission.
     * @param resupplyName the name of the resupply mission.
     * @return map of resources and their amounts (Double).
     */
    public Map<AmountResource, Double> getResupplyResources(String resupplyName) {
        ResupplyTemplate foundTemplate = getResupplyTemplate(resupplyName);
        return new HashMap<AmountResource, Double>(foundTemplate.resources);
    }

    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {

        Iterator<ResupplyTemplate> i = resupplyTemplates.iterator();
        while (i.hasNext()) {
            ResupplyTemplate template = i.next();
            template.name = null;
            template.buildings.clear();
            template.buildings = null;
            template.vehicles.clear();
            template.vehicles = null;
            template.equipment.clear();
            template.equipment = null;
            template.resources.clear();
            template.resources = null;
            template.parts.clear();
            template.parts = null;
        }
        resupplyTemplates.clear();
        resupplyTemplates = null;
    }

    /**
     * Private inner class for resupply template.
     */
    private static class ResupplyTemplate implements Serializable {

        /** default serial id. */
        private static final long serialVersionUID = 1L;

        private String name;
        private List<BuildingTemplate> buildings;
        private Map<String, Integer> vehicles;
        private Map<String, Integer> equipment;
        private int people;
        private Map<AmountResource, Double> resources;
        private Map<Part, Integer> parts;

        private ResupplyTemplate() {
            buildings = new ArrayList<BuildingTemplate>();
            vehicles = new HashMap<String, Integer>();
            equipment = new HashMap<String, Integer>();
            resources = new HashMap<AmountResource, Double>();
            parts = new HashMap<Part, Integer>();
        }
    }
}