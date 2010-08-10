/**
 * Mars Simulation Project
 * ResupplyConfig.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.PartPackageConfig;




/**
 * Provides configuration information about settlement resupply missions.
 * Uses a DOM document to get the information. 
 */
public class ResupplyConfig implements Serializable {

	// Element names
	private static final String RESUPPLY = "resupply";
	private static final String NAME = "name";
	private static final String BUILDING = "building";
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
	 * @param resupplyDoc DOM document for resupply configuration.
	 * @param partPackageConfig the part package configuration.
	 * @throws Exception if error parsing XML.
	 */
	public ResupplyConfig(Document resupplyDoc, PartPackageConfig partPackageConfig) 
			throws Exception {
		resupplyTemplates = new ArrayList<ResupplyTemplate>();
		loadResupplyTemplates(resupplyDoc, partPackageConfig);
	}
	
	/**
	 * Loads teh resupply templates.
	 * @param resupplyDoc DOM document for resupply configuration.
	 * @param partPackageConfig th epart package configuration.
	 * @throws Exception if error parsing XML.
	 */
    @SuppressWarnings("unchecked")
	private void loadResupplyTemplates(Document resupplyDoc, PartPackageConfig partPackageConfig) 
			throws Exception {
		
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
				int buildingNumber = Integer.parseInt(buildingElement.getAttributeValue(NUMBER));
				if (template.buildings.containsKey(buildingType)) 
					buildingNumber += template.buildings.get(buildingType);
				template.buildings.put(buildingType, buildingNumber);
			}
			
			// Load vehicles
			List<Element> vehicleNodes = resupplyElement.getChildren(VEHICLE);
			for (Element vehicleElement : vehicleNodes) {
				String vehicleType = vehicleElement.getAttributeValue(TYPE);
				int vehicleNumber = Integer.parseInt(vehicleElement.getAttributeValue(NUMBER));
				if (template.vehicles.containsKey(vehicleType)) 
					vehicleNumber += template.vehicles.get(vehicleType);
				template.vehicles.put(vehicleType, vehicleNumber);
			}
			
			// Load equipment
			List<Element> equipmentNodes = resupplyElement.getChildren(EQUIPMENT);
			for (Element equipmentElement : equipmentNodes) {
				String equipmentType = equipmentElement.getAttributeValue(TYPE);
				int equipmentNumber = Integer.parseInt(equipmentElement.getAttributeValue(NUMBER));
				if (template.equipment.containsKey(equipmentType)) 
					equipmentNumber += template.equipment.get(equipmentType);
				template.equipment.put(equipmentType, equipmentNumber);
			}
			
			// Load people
			List<Element> personNodes = resupplyElement.getChildren(PERSON);
			for (Element personElement : personNodes) {
				int personNumber = Integer.parseInt(personElement.getAttributeValue(NUMBER));
				template.people += personNumber;
			}
			
			// Load resources
		    List<Element> resourceNodes = resupplyElement.getChildren(RESOURCE);
			for (Element resourceElement : resourceNodes) {
				String resourceType = resourceElement.getAttributeValue(TYPE);
				AmountResource resource = AmountResource.findAmountResource(resourceType);
				double resourceAmount = Double.parseDouble(resourceElement.getAttributeValue(AMOUNT));
				if (template.resources.containsKey(resource)) 
					resourceAmount += template.resources.get(resource);
				template.resources.put(resource, resourceAmount);
			}
			
			// Load parts
			List<Element> partNodes = resupplyElement.getChildren(PART);
			for (Element partElement : partNodes) {
				String partType = partElement.getAttributeValue(TYPE);
				Part part = (Part) Part.findItemResource(partType);
				int partNumber = Integer.parseInt(partElement.getAttributeValue(NUMBER));
				if (template.parts.containsKey(part)) partNumber += template.parts.get(part);
				template.parts.put(part, partNumber);
			}
			
			// Load part packages
			List<Element> partPackageNodes = resupplyElement.getChildren(PART_PACKAGE);
			
			for (Element partPackageElement : partPackageNodes) {
				String packageName = partPackageElement.getAttributeValue(NAME);
				int packageNumber = Integer.parseInt(partPackageElement.getAttributeValue(NUMBER));
				if (packageNumber > 0) {
					for (int z = 0; z < packageNumber; z++) {
						Map<Part, Integer> partPackage = partPackageConfig.getPartsInPackage(packageName);
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
			if (template.name.equals(resupplyName)) result = template; 
		}
		
		if (result == null) throw new IllegalArgumentException("resupplyName: " 
				+ resupplyName + " not found.");
		
		return result;
	}
	
	/**
	 * Gets a list of building types in the resupply mission.
	 * @param resupplyName name of the resupply mission.
	 * @return list of building types as strings.
	 */
	public List<String> getResupplyBuildingTypes(String resupplyName) {
		ResupplyTemplate foundTemplate = getResupplyTemplate(resupplyName);
		List<String> result = new ArrayList<String>();
		Iterator<String> j = foundTemplate.buildings.keySet().iterator();
		while (j.hasNext()) {
			String buildingType = j.next();
			int buildingNumber = foundTemplate.buildings.get(buildingType);
			for (int x = 0; x < buildingNumber; x++) result.add(buildingType);
		}
		return result;
	}
	
	/**
	 * Gets a list of vehicle types in the resupply mission.
	 * @param resupplyName name of the resupply mission.
	 * @return list of vehicle types as strings.
	 */
	public List<String> getResupplyVehicleTypes(String resupplyName) throws Exception {
		ResupplyTemplate foundTemplate = getResupplyTemplate(resupplyName);
		List<String> result = new ArrayList<String>();
		Iterator<String> j = foundTemplate.vehicles.keySet().iterator();
		while (j.hasNext()) {
			String vehicleType = j.next();
			int vehicleNumber = foundTemplate.vehicles.get(vehicleType);
			for (int x = 0; x < vehicleNumber; x++) result.add(vehicleType);
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
	 * Private inner class for resupply template.
	 */
	private class ResupplyTemplate implements Serializable {
		private String name;
		private Map<String, Integer> buildings;
		private Map<String, Integer> vehicles;
		private Map<String, Integer> equipment;
		private int people;
		private Map<AmountResource, Double> resources;
		private Map<Part, Integer> parts;
		
		private ResupplyTemplate() {
			buildings = new HashMap<String, Integer>();
			vehicles = new HashMap<String, Integer>();
			equipment = new HashMap<String, Integer>();
			resources = new HashMap<AmountResource, Double>();
			parts = new HashMap<Part, Integer>();
		}
	}
}