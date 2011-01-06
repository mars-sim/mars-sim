/**
 * Mars Simulation Project
 * SettlementConfig.java
 * @version 3.00 2010-08-25
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure;

import org.jdom.Document;
import org.jdom.Element;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.PartPackageConfig;

import java.io.Serializable;
import java.util.*;


/**
 * Provides configuration information about settlements.
 * Uses a DOM document to get the information. 
 */
public class SettlementConfig implements Serializable {
	
	// Element names
	private static final String SETTLEMENT_TEMPLATE_LIST = 	"settlement-template-list";
	private static final String TEMPLATE = "template";
	private static final String NAME = "name";
	private static final String BUILDING = "building";
	private static final String TYPE = "type";
	private static final String X_LOCATION = "x-location";
	private static final String Y_LOCATION = "y-location";
	private static final String FACING = "facing";
	private static final String NUMBER = "number";
	private static final String VEHICLE = "vehicle";
	private static final String EQUIPMENT = "equipment";
	private static final String INITIAL_SETTLEMENT_LIST = "initial-settlement-list";
	private static final String SETTLEMENT = "settlement";
	private static final String LOCATION = "location";
	private static final String LONGITUDE = "longitude";
	private static final String LATITUDE = "latitude";
	private static final String SETTLEMENT_NAME_LIST = "settlement-name-list";
	private static final String SETTLEMENT_NAME = "settlement-name";
	private static final String VALUE = "value";
	private static final String RESUPPLY = "resupply";
	private static final String RESUPPLY_MISSION = "resupply-mission";
	private static final String ARRIVAL_TIME = "arrival-time";
	private static final String RESOURCE = "resource";
	private static final String AMOUNT = "amount";
	private static final String PART = "part";
	private static final String PART_PACKAGE = "part-package";
	
	// Random value indicator.
	public static final String RANDOM = "random";
	
	// Data members
	private Collection<SettlementTemplate> settlementTemplates;
	private List<InitialSettlement> initialSettlements;
	private List<String> settlementNames;
	
	/**
	 * Constructor
	 * @param settlementDoc DOM document with settlement configuration.
	 * @param partPackageConfig the part package configuration.
	 * @throws Exception if error reading XML document.
	 */
	public SettlementConfig(Document settlementDoc, PartPackageConfig partPackageConfig) 
			{
		settlementTemplates = new ArrayList<SettlementTemplate>();
		initialSettlements = new ArrayList<InitialSettlement>();
		settlementNames = new ArrayList<String>();
		
		loadSettlementTemplates(settlementDoc, partPackageConfig);	
		loadInitialSettlements(settlementDoc);
		loadSettlementNames(settlementDoc);
	}
	
	/**
	 * Load the settlement templates from the XML document.
	 * @param settlementDoc DOM document with settlement configuration.
	 * @param partPackageConfig the part package configuration.
	 * @throws Exception if error reading XML document.
	 */
    @SuppressWarnings("unchecked")
	private void loadSettlementTemplates(Document settlementDoc, PartPackageConfig partPackageConfig) 
			{
		
		Element root = settlementDoc.getRootElement();
		Element templateList = root.getChild(SETTLEMENT_TEMPLATE_LIST);
		List<Element> templateNodes = templateList.getChildren(TEMPLATE);
		for (Element templateElement : templateNodes) {
		    String name = templateElement.getAttributeValue(NAME);
		    
			SettlementTemplate template = new SettlementTemplate(name);
			settlementTemplates.add(template);
			
			// Load buildings
			List<Element> buildingNodes = templateElement.getChildren(BUILDING);
			for (Element buildingElement : buildingNodes) {
				String buildingType = buildingElement.getAttributeValue(TYPE);
				double xLoc = Double.parseDouble(buildingElement.getAttributeValue(X_LOCATION));
				double yLoc = Double.parseDouble(buildingElement.getAttributeValue(Y_LOCATION));
				double facing = Double.parseDouble(buildingElement.getAttributeValue(FACING));
				template.addBuildingTemplate(new BuildingTemplate(buildingType, xLoc, yLoc, facing));
			}
			
			// Load vehicles
			List<Element> vehicleNodes = templateElement.getChildren(VEHICLE);
			for (Element vehicleElement : vehicleNodes) {
				String vehicleType = vehicleElement.getAttributeValue(TYPE);
				int vehicleNumber = Integer.parseInt(vehicleElement.getAttributeValue(NUMBER));
				template.addVehicles(vehicleType, vehicleNumber);
			}
			
			// Load equipment
			List<Element> equipmentNodes = templateElement.getChildren(EQUIPMENT);
			for (Element equipmentElement : equipmentNodes) {
				String equipmentType = equipmentElement.getAttributeValue(TYPE);
				int equipmentNumber = Integer.parseInt(equipmentElement.getAttributeValue(NUMBER));
				template.addEquipment(equipmentType, equipmentNumber);
			}
			
			// Load resources
			List<Element> resourceNodes = templateElement.getChildren(RESOURCE);
			for (Element resourceElement : resourceNodes) {
				String resourceType = resourceElement.getAttributeValue(TYPE);
				AmountResource resource = AmountResource.findAmountResource(resourceType);
				double resourceAmount = Double.parseDouble(resourceElement.getAttributeValue(AMOUNT));
				template.addAmountResource(resource, resourceAmount);
			}
			
			// Load parts
			List<Element> partNodes = templateElement.getChildren(PART);
			for (Element partElement : partNodes) {
				String partType = partElement.getAttributeValue(TYPE);
				Part part = (Part) Part.findItemResource(partType);
				int partNumber = Integer.parseInt(partElement.getAttributeValue(NUMBER));
				template.addPart(part, partNumber);
			}
			
			// Load part packages
			List<Element> partPackageNodes = templateElement.getChildren(PART_PACKAGE);
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
							template.addPart(part, partNumber);
						}
					}
				}
			}
			
			// Load resupplies
			Element resupplyList = templateElement.getChild(RESUPPLY);
			if (resupplyList != null) {
				List<Element> resupplyNodes = resupplyList.getChildren(RESUPPLY_MISSION);
				for (Element resupplyMissionElement: resupplyNodes) {
				    String resupplyName = resupplyMissionElement.getAttributeValue(NAME);
                    double arrivalTime = 
                        Double.parseDouble(resupplyMissionElement.getAttributeValue(ARRIVAL_TIME));
				    ResupplyMissionTemplate resupplyMissionTemplate = 
				        new ResupplyMissionTemplate(resupplyName, arrivalTime);
				    template.addResupplyMissionTemplate(resupplyMissionTemplate);
				}
			}
		}
	}
	
	/**
	 * Load initial settlements.
	 * @param settlementDoc DOM document with settlement configuration.
	 * @throws Exception if XML error.
	 */
    @SuppressWarnings("unchecked")
	private void loadInitialSettlements(Document settlementDoc) {
		Element root = settlementDoc.getRootElement();
		Element initialSettlementList = root.getChild(INITIAL_SETTLEMENT_LIST);
		List<Element> settlementNodes = initialSettlementList.getChildren(SETTLEMENT);
		for (Element settlementElement : settlementNodes) {
			InitialSettlement initialSettlement = new InitialSettlement();
			
			String settlementName = settlementElement.getAttributeValue(NAME);
			if (settlementName.equals(RANDOM)) initialSettlement.randomName = true;
			else initialSettlement.name = settlementName;
			
			initialSettlement.template = settlementElement.getAttributeValue(TEMPLATE);
			
			List<Element> locationNodes = settlementElement.getChildren(LOCATION);
			if (locationNodes.size() > 0) {
				Element locationElement = locationNodes.get(0);
				
				String longitudeString = locationElement.getAttributeValue(LONGITUDE);
				if (longitudeString.equals(RANDOM)) initialSettlement.randomLongitude = true;
				else initialSettlement.longitude = longitudeString;
				
				String latitudeString = locationElement.getAttributeValue(LATITUDE);
				if (latitudeString.equals(RANDOM)) initialSettlement.randomLatitude = true;
				else initialSettlement.latitude = latitudeString;
			}
			else {
				initialSettlement.randomLongitude = true;
				initialSettlement.randomLatitude = true;
			}
			
			initialSettlements.add(initialSettlement);
		}
	}
	
	/**
	 * Load settlement names.
	 * @param settlementDoc DOM document with settlement configuration.
	 * @throws Exception if XML error.
	 */
    @SuppressWarnings("unchecked")
	private void loadSettlementNames(Document settlementDoc) {
		Element root = settlementDoc.getRootElement();
		Element settlementNameList = root.getChild(SETTLEMENT_NAME_LIST);
		List<Element> settlementNameNodes = settlementNameList.getChildren(SETTLEMENT_NAME);
		for (Element settlementNameElement : settlementNameNodes) {
			settlementNames.add(settlementNameElement.getAttributeValue(VALUE));
		}
	}
	
	/**
	 * Gets the settlement template that matches a template name.
	 * @param templateName the template name.
	 * @return settlement template
	 */
	public SettlementTemplate getSettlementTemplate(String templateName) {
		SettlementTemplate result = null;
		
		Iterator<SettlementTemplate> i = settlementTemplates.iterator();
		while (i.hasNext()) {
			SettlementTemplate template = i.next();
			if (template.getTemplateName().equals(templateName)) result = template; 
		}
		
		if (result == null) throw new IllegalArgumentException("templateName: " 
				+ templateName + " not found.");
		
		return result;
	}
	
	/**
	 * Gets the number of initial settlements.
	 * @return number of settlements.
	 */
	public int getNumberOfInitialSettlements() {
		return initialSettlements.size();
	}
	
	/**
	 * Gets the name of an initial settlement 
	 * or 'random' if the name is to chosen randomly from the settlement name list.
	 * @param index the index of the initial settlement.
	 * @return settlement name
	 */
	public String getInitialSettlementName(int index) {
		if ((index >= 0) && (index < initialSettlements.size())) {
			InitialSettlement settlement = initialSettlements.get(index);
			if (settlement.randomName) return RANDOM;
			else return settlement.name;
		}
		else throw new IllegalArgumentException("index: " + index + "is out of bounds");
	}
	
	/**
	 * Gets the template used by an initial settlement.
	 * @param index the index of the initial settlement.
	 * @return settlement template name.
	 */
	public String getInitialSettlementTemplate(int index) {
		if ((index >= 0) && (index < initialSettlements.size()))
			return initialSettlements.get(index).template;
		else throw new IllegalArgumentException("index: " + index + "is out of bounds");
	}
	
	/**
	 * Gets the longitude of an initial settlement, 
	 * or 'random' if the longitude is to be randomly determined.
	 * @param index the index of the initial settlement.
	 * @return longitude of the settlement as a string. Example: '0.0 W'
	 */
	public String getInitialSettlementLongitude(int index) {
		if ((index >= 0) && (index < initialSettlements.size())) {
			InitialSettlement settlement = initialSettlements.get(index);
			if (settlement.randomLongitude) return RANDOM;
			else return settlement.longitude;
		}
		else throw new IllegalArgumentException("index: " + index + "is out of bounds");
	}
	
	/**
	 * Gets the latitude of an initial settlement, 
	 * or 'random' if the longitude is to be randomly determined.
	 * @param index the index of the initial settlement.
	 * @return latitude of the settlement as a string. Example: '0.0 N'
	 */
	public String getInitialSettlementLatitude(int index) {
		if ((index >= 0) && (index < initialSettlements.size())) {
			InitialSettlement settlement = initialSettlements.get(index);
			if (settlement.randomLatitude) return RANDOM;
			else return settlement.latitude;
		}
		else throw new IllegalArgumentException("index: " + index + "is out of bounds");
	}
	
	/**
	 * Gets a list of possible settlement names.
	 * @return list of settlement names as strings
	 */
	public List<String> getSettlementNameList() {
		return new ArrayList<String>(settlementNames);
	}
	
	/**
	 * Private inner class for holding a initial settlement info.
	 */
	private static class InitialSettlement implements Serializable {
		private String name;
		private boolean randomName = false;
		private String template;
		private String longitude;
		private boolean randomLongitude = false;
		private String latitude;
		private boolean randomLatitude = false;
	}
}