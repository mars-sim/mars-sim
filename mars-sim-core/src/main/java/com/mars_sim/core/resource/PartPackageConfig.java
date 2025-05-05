/*
 * Mars Simulation Project
 * PartPackageConfig.java
 * @date 2023-10-20
 * @author Scott Davis
 */
package com.mars_sim.core.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;

import com.mars_sim.core.configuration.ConfigHelper;

/**
 * Provides configuration information about part packages. Uses a JDOM document
 * to get the information.
 */
public class PartPackageConfig  {

	// Element names
	private static final String PART_PACKAGE = "part-package";
	private static final String PART = "part";
	private static final String NAME = "name";
	private static final String TYPE = "type";
	private static final String NUMBER = "number";

	// Data members
	private Collection<PartPackage> partPackages;

	/**
	 * Constructor.
	 * 
	 * @param partPackageDoc the part package XML document.
	 * @throws Exception if error reading XML document
	 */
	public PartPackageConfig(Document partPackageDoc, PartConfig partsConfig) {
		loadPartPackages(partPackageDoc, partsConfig);
	}

	/**
	 * Loads the part packages for the simulation.
	 * 
	 * @param partPackageDoc the part package XML document.
	 * @throws Exception if error reading XML document.
	 */
	private synchronized void loadPartPackages(Document partPackageDoc, PartConfig partsConfig) {
		if (partPackages != null) {
			// just in case if another thread is being created
			return;
		}
		
		List<PartPackage> newList = new ArrayList<>();
		
		Element root = partPackageDoc.getRootElement();
		List<Element> partPackageNodes = root.getChildren(PART_PACKAGE);
		for (Element partPackageElement : partPackageNodes) {

			var name = partPackageElement.getAttributeValue(NAME);

			List<Element> partNodes = partPackageElement.getChildren(PART);
			Map<Part,Integer> parts = ConfigHelper.parseIntList("Parts package " + name, partNodes,
						TYPE, partsConfig::getPartByName, NUMBER);

			// Add partPackage to newList.
			newList.add(new PartPackage(name, Collections.unmodifiableMap(parts)));
		}
		
		// Assign the newList now built
		partPackages = Collections.unmodifiableList(newList);
	}

	/**
	 * Gets the parts stored in a given part package.
	 * 
	 * @param name the part package name.
	 * @return parts and their numbers.
	 * @throws Exception if part package name does not match any packages.
	 */
	public Map<Part, Integer> getPartsInPackage(String name) {
		Map<Part, Integer> result = null;

		PartPackage foundPartPackage = null;
		for (PartPackage partPackage : partPackages) {
			if (partPackage.name.equals(name)) {
				foundPartPackage = partPackage;
				break;
			}
		}

		if (foundPartPackage != null)
			result = foundPartPackage.parts;
		else
			throw new IllegalStateException("name: " + name + " does not match any part packages.");

		return result;
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		Iterator<PartPackage> i = partPackages.iterator();
		while (i.hasNext()) {
			i.next().parts.clear();
		}
		partPackages = null;
	}

	/**
	 * Private inner class for storing part packages.
	 */
	private static record PartPackage(String name, Map<Part,Integer> parts) implements Serializable {}
}
