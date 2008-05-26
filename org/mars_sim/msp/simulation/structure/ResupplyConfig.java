/**
 * Mars Simulation Project
 * ResupplyConfig.java
 * @version 2.84 2008-05-25
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.resource.Part;
import org.mars_sim.msp.simulation.resource.PartPackageConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
	private void loadResupplyTemplates(Document resupplyDoc, PartPackageConfig partPackageConfig) 
			throws Exception {
		
		Element root = resupplyDoc.getDocumentElement();
		NodeList resupplyNodes = root.getElementsByTagName(RESUPPLY);
		for (int x = 0; x < resupplyNodes.getLength(); x++) {
			ResupplyTemplate template = new ResupplyTemplate();
			resupplyTemplates.add(template);
			
			Element resupplyElement = (Element) resupplyNodes.item(x);
			template.name = resupplyElement.getAttribute(NAME);
			
			// Load buildings
			NodeList buildingNodes = resupplyElement.getElementsByTagName(BUILDING);
			for (int y = 0; y < buildingNodes.getLength(); y++) {
				Element buildingElement = (Element) buildingNodes.item(y);
				String buildingType = buildingElement.getAttribute(TYPE);
				int buildingNumber = Integer.parseInt(buildingElement.getAttribute(NUMBER));
				if (template.buildings.containsKey(buildingType)) 
					buildingNumber += template.buildings.get(buildingType);
				template.buildings.put(buildingType, buildingNumber);
			}
			
			// Load vehicles
			NodeList vehicleNodes = resupplyElement.getElementsByTagName(VEHICLE);
			for (int y = 0; y < vehicleNodes.getLength(); y++) {
				Element vehicleElement = (Element) vehicleNodes.item(y);
				String vehicleType = vehicleElement.getAttribute(TYPE);
				int vehicleNumber = Integer.parseInt(vehicleElement.getAttribute(NUMBER));
				if (template.vehicles.containsKey(vehicleType)) 
					vehicleNumber += template.vehicles.get(vehicleType);
				template.vehicles.put(vehicleType, vehicleNumber);
			}
			
			// Load equipment
			NodeList equipmentNodes = resupplyElement.getElementsByTagName(EQUIPMENT);
			for (int y = 0; y < equipmentNodes.getLength(); y++) {
				Element equipmentElement = (Element) equipmentNodes.item(y);
				String equipmentType = equipmentElement.getAttribute(TYPE);
				int equipmentNumber = Integer.parseInt(equipmentElement.getAttribute(NUMBER));
				if (template.equipment.containsKey(equipmentType)) 
					equipmentNumber += template.equipment.get(equipmentType);
				template.equipment.put(equipmentType, equipmentNumber);
			}
			
			// Load people
			NodeList personNodes = resupplyElement.getElementsByTagName(PERSON);
			for (int y = 0; y < personNodes.getLength(); y++) {
				Element personElement = (Element) personNodes.item(y);
				int personNumber = Integer.parseInt(personElement.getAttribute(NUMBER));
				template.people += personNumber;
			}
			
			// Load resources
			NodeList resourceNodes = resupplyElement.getElementsByTagName(RESOURCE);
			for (int y = 0; y < resourceNodes.getLength(); y++) {
				Element resourceElement = (Element) resourceNodes.item(y);
				String resourceType = resourceElement.getAttribute(TYPE);
				AmountResource resource = AmountResource.findAmountResource(resourceType);
				double resourceAmount = Double.parseDouble(resourceElement.getAttribute(AMOUNT));
				if (template.resources.containsKey(resource)) 
					resourceAmount += template.resources.get(resource);
				template.resources.put(resource, resourceAmount);
			}
			
			// Load parts
			NodeList partNodes = resupplyElement.getElementsByTagName(PART);
			for (int y = 0; y < partNodes.getLength(); y++) {
				Element partElement = (Element) partNodes.item(y);
				String partType = partElement.getAttribute(TYPE);
				Part part = (Part) Part.findItemResource(partType);
				int partNumber = Integer.parseInt(partElement.getAttribute(NUMBER));
				if (template.parts.containsKey(part)) partNumber += template.parts.get(part);
				template.parts.put(part, partNumber);
			}
			
			// Load part packages
			NodeList partPackageNodes = resupplyElement.getElementsByTagName(PART_PACKAGE);
			for (int y = 0; y < partPackageNodes.getLength(); y++) {
				Element partPackageElement = (Element) partPackageNodes.item(y);
				String packageName = partPackageElement.getAttribute(NAME);
				int packageNumber = Integer.parseInt(partPackageElement.getAttribute(NUMBER));
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