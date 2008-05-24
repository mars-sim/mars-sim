/**
 * Mars Simulation Project
 * PartPackageConfig.java
 * @version 2.84 2008-05-24
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Provides configuration information about part packages.
 * Uses a DOM document to get the information. 
 */
public class PartPackageConfig implements Serializable {

	// Element names
	private static final String PART_PACKAGE = "part-package";
	private static final String PART = "part";
	private static final String NAME = "name";
	private static final String TYPE = "type";
	private static final String NUMBER = "number";
	
	// Data members
	private Collection<PartPackage> partPackages;
	
	/**
	 * Constructor
	 * @param partPackageDoc the part package XML document.
	 * @throws Exception if error reading XML document
	 */
	public PartPackageConfig(Document partPackageDoc) throws Exception {
		partPackages = new ArrayList<PartPackage>();
		loadPartPackages(partPackageDoc);
	}
	
	/**
	 * Loads the part packages for the simulation.
	 * @param partPackageDoc the part package XML document.
	 * @throws Exception if error reading XML document.
	 */
	private void loadPartPackages(Document partPackageDoc) throws Exception {
		
		Element root = partPackageDoc.getDocumentElement();
		NodeList partPackageNodes = root.getElementsByTagName(PART_PACKAGE);
		for (int x = 0; x < partPackageNodes.getLength(); x++) {
			PartPackage partPackage = new PartPackage();
			partPackages.add(partPackage);
			
			Element partPackageElement = (Element) partPackageNodes.item(x);
			partPackage.name = partPackageElement.getAttribute(NAME);
			
			NodeList partNodes = partPackageElement.getElementsByTagName(PART);
			for (int y = 0; y < partNodes.getLength(); y++) {
				Element partElement = (Element) partNodes.item(y);
				String partType = partElement.getAttribute(TYPE);
				Part part = (Part) Part.findItemResource(partType);
				int partNumber = Integer.parseInt(partElement.getAttribute(NUMBER));
				partPackage.parts.put(part, partNumber);
			}
		}
	}
	
	/**
	 * Gets the parts stored in a given part package.
	 * @param name the part package name.
	 * @return parts and their numbers.
	 * @throws Exception if part package name does not match any packages.
	 */
	public Map<Part, Integer> getPartsInPackage(String name) throws Exception {
		Map<Part, Integer> result = null;
		
		PartPackage foundPartPackage = null;
		Iterator<PartPackage> i = partPackages.iterator();
		while (i.hasNext()) {
			PartPackage partPackage = i.next();
			if (partPackage.name.equals(name)) foundPartPackage = partPackage;
		}
		
		if (foundPartPackage != null) 
			result = new HashMap<Part, Integer>(foundPartPackage.parts);
		else throw new Exception("name: " + name + " does not match any part packages.");
		
		return result;
	}
	
	/**
	 * Private inner class for storing part packages.
	 */
	private class PartPackage implements Serializable {
		
		// Data members
		private String name;
		private Map<Part, Integer> parts;
		
		private PartPackage() {
			parts = new HashMap<Part, Integer>();
		}
	}
}