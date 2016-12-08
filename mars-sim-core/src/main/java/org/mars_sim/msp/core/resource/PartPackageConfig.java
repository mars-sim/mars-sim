/**
 * Mars Simulation Project
 * PartPackageConfig.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */
package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;


/**
 * Provides configuration information about part packages.
 * Uses a JDOM document to get the information. 
 */
public class PartPackageConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

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
	public PartPackageConfig(Document partPackageDoc) {
    	// 2016-12-07 Call to just initialize PartConfig in this constructor
       	new ItemResource();
       	
		partPackages = new ArrayList<PartPackage>();
		loadPartPackages(partPackageDoc);
	}
	
	/**
	 * Loads the part packages for the simulation.
	 * @param partPackageDoc the part package XML document.
	 * @throws Exception if error reading XML document.
	 */
    @SuppressWarnings("unchecked")
	private void loadPartPackages(Document partPackageDoc) {
		
		Element root = partPackageDoc.getRootElement();
		List<Element> partPackageNodes = root.getChildren(PART_PACKAGE);
		for (Element partPackageElement : partPackageNodes) {
			PartPackage partPackage = new PartPackage();
			
			partPackage.name = partPackageElement.getAttributeValue(NAME);
			
			List<Element> partNodes = partPackageElement.getChildren(PART);
			for (Element partElement : partNodes) {
				String partType = partElement.getAttributeValue(TYPE);
				//System.out.println("partPackage is " + partPackage.name + "     partType is " + partType);
				Part part = (Part) ItemResource.findItemResource(partType);
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
	public Map<Part, Integer> getPartsInPackage(String name) {
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
		else throw new IllegalStateException("name: " + name + " does not match any part packages.");
		
		return result;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
	    Iterator<PartPackage> i = partPackages.iterator();
	    while (i.hasNext()) {
	        i.next().parts.clear();
	    }
	    partPackages.clear();
	}
	
	/**
	 * Private inner class for storing part packages.
	 */
	private static class PartPackage implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members
		private String name;
		private Map<Part, Integer> parts;
		
		private PartPackage() {
			parts = new HashMap<Part, Integer>();
		}
	}
}