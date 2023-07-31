/*
 * Mars Simulation Project
 * ResupplyConfig.java
 * @date 2023-07-30
 * @author Scott Davis
 */
package org.mars_sim.msp.core.interplanetary.transport.resupply;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars.sim.mapdata.location.BoundedObject;
import org.mars_sim.msp.core.configuration.ConfigHelper;
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
    private static final String ZONE = "zone";
    private static final String BUILDING = "building";
    private static final String VEHICLE = "vehicle";
    private static final String EQUIPMENT = "equipment";
    private static final String BIN = "bin";
    private static final String PERSON = "person";
    private static final String RESOURCE = "resource";
    private static final String PART = "part";
    private static final String PART_PACKAGE = "part-package";
    private static final String TYPE = "type";
    private static final String NUMBER = "number";
    private static final String AMOUNT = "amount";

    // Data members
    private Collection<SupplyManifest> resupplyTemplates;

    /**
     * Constructor.
     *
     * @param resupplyDoc DOM document for resupply configuration.
     * @param partPackageConfig the part package configuration.
     */
    public ResupplyConfig(Document resupplyDoc,
            PartPackageConfig partPackageConfig) {

        resupplyTemplates = new ArrayList<>();
        loadResupplyTemplates(resupplyDoc, partPackageConfig);
    }

    /**
     * Loads the resupply templates.
     * 
     * @param resupplyDoc DOM document for resupply configuration.
     * @param partPackageConfig the part package configuration.
     */
    private void loadResupplyTemplates(Document resupplyDoc,
            PartPackageConfig partPackageConfig) {
    	
    	if (resupplyTemplates.isEmpty()) {
	        Element root = resupplyDoc.getRootElement();
	        List<Element> resupplyNodes = root.getChildren(RESUPPLY);
	        for (Element resupplyElement : resupplyNodes) {
	
	            String name = resupplyElement.getAttributeValue(NAME);
                int people = ConfigHelper.getOptionalAttributeInt(resupplyElement, PERSON, 0);
                int zone = ConfigHelper.getOptionalAttributeInt(resupplyElement, ZONE, 0);
	            	
	            // Load buildings
                List<BuildingTemplate> buildings = new ArrayList<>();
	            List<Element> buildingNodes = resupplyElement.getChildren(BUILDING);
	            for (Element buildingElement : buildingNodes) {
	                String buildingType = buildingElement.getAttributeValue(TYPE);
	                BoundedObject bounds = ConfigHelper.parseBoundedObject(buildingElement);
	
	                buildings.add(new BuildingTemplate(0, zone, buildingType,
	                        buildingType, bounds));
	
	            }
	
	            // Load vehicles
                Map<String, Integer> vehicles = new HashMap<>();
	            List<Element> vehicleNodes = resupplyElement.getChildren(VEHICLE);
	            for (Element vehicleElement : vehicleNodes) {
	                String vehicleType = vehicleElement.getAttributeValue(TYPE);
	                int vehicleNumber = Integer.parseInt(vehicleElement
	                        .getAttributeValue(NUMBER));
	                if (vehicles.containsKey(vehicleType))
	                    vehicleNumber += vehicles.get(vehicleType);
	                vehicles.put(vehicleType, vehicleNumber);
	            }
	
	            // Load equipment
                Map<String, Integer> equipment = new HashMap<>();
	            List<Element> equipmentNodes = resupplyElement
	                    .getChildren(EQUIPMENT);
	            for (Element equipmentElement : equipmentNodes) {
	                String equipmentType = equipmentElement.getAttributeValue(TYPE);
	                int equipmentNumber = Integer.parseInt(equipmentElement
	                        .getAttributeValue(NUMBER));
	                if (equipment.containsKey(equipmentType))
	                    equipmentNumber += equipment.get(equipmentType);
	                equipment.put(equipmentType, equipmentNumber);
	            }
	            
	            // Load bins
                Map<String, Integer> bin = new HashMap<>();
	            List<Element> binNodes = resupplyElement
	                    .getChildren(BIN);
	            for (Element binElement : binNodes) {
	                String binType = binElement.getAttributeValue(TYPE);
	                int binNumber = Integer.parseInt(binElement
	                        .getAttributeValue(NUMBER));
	                if (bin.containsKey(binType))
	                	binNumber += bin.get(binType);
	                bin.put(binType, binNumber);
	            }
	            

	            // Load resources
                Map<AmountResource, Double> resources = new HashMap<>();
	            List<Element> resourceNodes = resupplyElement.getChildren(RESOURCE);
	            for (Element resourceElement : resourceNodes) {
	                String resourceName = resourceElement.getAttributeValue(NAME);

	                AmountResource resource = ResourceUtil.findAmountResource(resourceName);
	                if (resource == null) {
	                	throw new IllegalStateException(
								"ResupplyConfig detected a null resource entry in resupply.xml. resourceName: " + resourceName);
	                }
	                
	                double resourceAmount = Double.parseDouble(resourceElement
	                        .getAttributeValue(AMOUNT));
	                
	                if (resources.containsKey(resource))
	                    resourceAmount += resources.get(resource);
	                
	                resources.put(resource, resourceAmount);
	            }
	
	            // Load parts
                Map<Part,Integer> parts = new HashMap<>();
	            List<Element> partNodes = resupplyElement.getChildren(PART);
	            for (Element partElement : partNodes) {
	                String partType = partElement.getAttributeValue(TYPE);
	                Part part = (Part) (ItemResourceUtil.findItemResource(partType));
	                int partNumber = Integer.parseInt(partElement
	                        .getAttributeValue(NUMBER));
	                if (parts.containsKey(part))
	                    partNumber += parts.get(part);
	                parts.put(part, partNumber);
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
	                            if (parts.containsKey(part))
	                                partNumber += parts.get(part);
	                            parts.put(part, partNumber);
	                        }
	                    }
	                }
	            }

                // Build the 
                SupplyManifest template = new SupplyManifest(name, people,
                                            Collections.unmodifiableList(buildings),
                                            Collections.unmodifiableMap(vehicles),
                                            Collections.unmodifiableMap(equipment),
                                            Collections.unmodifiableMap(bin),
                                            Collections.unmodifiableMap(resources),
                                            Collections.unmodifiableMap(parts) );
	            resupplyTemplates.add(template);
	        }
    	}
    }

    /**
     * Gets the resupply template for a resupply mission name.
     * 
     * @param resupplyName the resupply mission name.
     * @return the resupply template.
     */
    public SupplyManifest getSupplyManifest(String resupplyName) {

        Iterator<SupplyManifest> i = resupplyTemplates.iterator();
        while (i.hasNext()) {
            SupplyManifest template = i.next();
            if (template.name.equals(resupplyName)) {
                return template;
            }
        }

        throw new IllegalArgumentException("resupplyName: " + resupplyName
                    + " not found.");
    }

    /**
     * Prepares object for garbage collection.
     */
    public void destroy() {

        resupplyTemplates.clear();
        resupplyTemplates = null;
    }

    /**
     * Definition of a Supply Manifest used in a resupply mission.
     */
    public static record SupplyManifest(String name, int people, List<BuildingTemplate> buildings,
                                            Map<String, Integer> vehicles,
                                            Map<String, Integer> equipment,
                                            Map<String, Integer> bins,
                                            Map<AmountResource, Double> resources,
                                            Map<Part, Integer> parts)
                        implements Serializable {};
}
