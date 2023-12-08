/*
 * Mars Simulation Project
 * BuildingPackageConfig.java
 * @date 2023-10-20
 * @author Scott Davis
 */
package com.mars_sim.core.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.structure.building.BuildingTemplate;
import com.mars_sim.mapdata.location.BoundedObject;

/**
 * Provides configuration information about the building packages. 
 * Uses a JDOM document to get the information.
 */
public class BuildingPackageConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// May add back private static final Logger logger = Logger.getLogger(BuildingPackageConfig.class.getName())

	// Element names
	private final String BUILDING_PACKAGE = "building-package";
	private final String BUILDING = "building";
	private final String NAME = "name";
	private final String TYPE = "type";

	// Data members
	private Collection<BuildingPackage> buildingPackages;

	/**
	 * Constructor.
	 * 
	 * @param buildingPackageDoc the part package XML document.
	 * @throws Exception if error reading XML document
	 */
	public BuildingPackageConfig(Document doc) {
		loadPartPackages(doc);
	}

	/**
	 * Loads the building packages for the simulation.
	 * 
	 * @param buildingPackageDoc the building package XML document.
	 * @throws Exception if error reading XML document.
	 */
	private synchronized void loadPartPackages(Document doc) {
		if (buildingPackages != null) {
			// just in case if another thread is being created
			return;
		}
		
		List<BuildingPackage> newList = new ArrayList<>();
		
		Element root = doc.getRootElement();
		List<Element> packageNodes = root.getChildren(BUILDING_PACKAGE);
			
		for (Element packageElement : packageNodes) {
			BuildingPackage buildingPackage = new BuildingPackage();

			buildingPackage.name = packageElement.getAttributeValue(NAME);

			List<Element> buildingNodes = packageElement.getChildren(BUILDING);

            // Load buildings as building templates
            for (Element buildingElement : buildingNodes) {
                String buildingType = buildingElement.getAttributeValue(TYPE);
                BoundedObject bounds = ConfigHelper.parseBoundedObject(buildingElement);
 
                buildingPackage.addTemplate(new BuildingTemplate(-1, 1, buildingType,
                        buildingType, bounds));
			}
			// Add buildingPackage to newList.
			newList.add(buildingPackage);
		}
		
		// Assign the newList to buildingPackages
		buildingPackages = Collections.unmodifiableList(newList);
	}

	/**
	 * Gets the buildings stored in a given part package.
	 * 
	 * @param name the building package name.
	 * @return buildings and their numbers.
	 * @throws Exception if building package name does not match any packages.
	 */
	public List<BuildingTemplate> getBuildingsInPackage(String name) {
		List<BuildingTemplate> result = null;

		BuildingPackage foundPackage = null;
		for (BuildingPackage pk: buildingPackages) {
			if (pk.name.equals(name)) {
				foundPackage = pk;
				break;
			}
		}

		if (foundPackage != null)
			result = new ArrayList<>(foundPackage.buildings);
		else
			throw new IllegalStateException("name: " + name + " does not match any building packages.");

		return result;
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		Iterator<BuildingPackage> i = buildingPackages.iterator();
		while (i.hasNext()) {
			i.next().buildings.clear();
		}
		buildingPackages = null;
	}

	/**
	 * Private inner class for storing building packages.
	 */
	private static class BuildingPackage implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members
		private String name;
		private List<BuildingTemplate> buildings;

		private BuildingPackage() {
			buildings = new ArrayList<>();
		}
		
		public void addTemplate(BuildingTemplate buildingTemplate) {
			buildings.add(buildingTemplate);
		}
	}
}
