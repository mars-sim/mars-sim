/**
 * Mars Simulation Project
 * PartPackageConfig.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;


/**
 * Provides configuration information about part packages.
 * Uses a JDOM document to get the information. 
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
    @SuppressWarnings("unchecked")
	private void loadPartPackages(Document partPackageDoc) throws Exception {
		
		Element root = partPackageDoc.getRootElement();
		List<Element> partPackageNodes = root.getChildren(PART_PACKAGE);
		for (Element partPackageElement : partPackageNodes) {
			PartPackage partPackage = new PartPackage();
			
			partPackage.name = partPackageElement.getAttributeValue(NAME);
			
			List<Element> partNodes = partPackageElement.getChildren(PART);
			for (Element partElement : partNodes) {
				String partType = partElement.getAttributeValue(TYPE);
				Part part = (Part) Part.findItemResource(partType);
				int partNumber = Integer.parseInt(partElement.getAttributeValue(NUMBER));
				partPackage.parts.put(part, partNumber);
			}
			
			partPackages.add(partPackage);
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
		for(PartPackage partPackage : partPackages) {
	            if (partPackage.name.equals(name)) {
	                foundPartPackage = partPackage;
	                break;
	            }
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