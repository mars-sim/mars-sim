/*
 * Mars Simulation Project
 * ResupplyConfig.java
 * @date 2023-07-30
 * @author Scott Davis
 */
package com.mars_sim.core.interplanetary.transport.resupply;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.resource.PartPackageConfig;
import com.mars_sim.core.structure.SettlementTemplateConfig;
import com.mars_sim.core.structure.building.BuildingTemplate;

/**
 * Provides configuration information about settlement resupply missions. Uses a
 * DOM document to get the information.
 */
public class ResupplyConfig {

    // Element names
    private static final String RESUPPLY = "resupply";
    private static final String NAME = "name";
    private static final String ZONE = "zone";
    private static final String BUILDING = "building";
    private static final String PERSON = "person";
    private static final String TYPE = "type";

    // Data members
    private List<ResupplyManifest> resupplyTemplates;

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
	
	                buildings.add(new BuildingTemplate("", zone, buildingType,
	                        buildingType, bounds));	
	            }

				// Load tje supplies
				var supplies = SettlementTemplateConfig.parseSupplies(name, resupplyElement, buildings,
												partPackageConfig);
                
				// Build the 
                ResupplyManifest template = new ResupplyManifest(name, people, supplies);
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
    public ResupplyManifest getSupplyManifest(String resupplyName) {
        for(ResupplyManifest template : resupplyTemplates) {
            if (template.getName().equals(resupplyName)) {
                return template;
            }
        }

        throw new IllegalArgumentException("resupplyName: " + resupplyName
                    + " not found.");
    }

    /**
     * Get all declared supply manifests
     * @return
     */
    public List<ResupplyManifest> getAll() {
        return resupplyTemplates;
    }
}
