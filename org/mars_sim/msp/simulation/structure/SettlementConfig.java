/**
 * Mars Simulation Project
 * SettlementConfig.java
 * @version 2.76 2004-08-01
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure;

import java.util.*;
import org.w3c.dom.*;

/**
 * Provides configuration information about settlements.
 * Uses a DOM document to get the information. 
 */
public class SettlementConfig {
	
	// Element names
	private static final String SETTLEMENT_TEMPLATE_LIST = 	"settlement-template-list";
	private static final String TEMPLATE = "template";
	private static final String NAME = "name";
	private static final String BUILDING = "building";
	private static final String TYPE = "type";
	private static final String NUMBER = "number";
	private static final String VEHICLE = "vehicle";
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
	private static final String PERSON = "person";
	private static final String RESOURCE = "resource";
	private static final String AMOUNT = "amount";
	private static final String RESUPPLY_LIST = "resupply-list";
	
	// Random value indicator.
	public static final String RANDOM = "random";
	
	private Document settlementDoc;
	
	/**
	 * Constructor
	 * @param buildingDoc DOM document with building configuration
	 */
	public SettlementConfig(Document settlementDoc) {
		this.settlementDoc = settlementDoc;	
	}
	
	/**
	 * Gets a settlement template element by name.
	 * @param name the name of the settlement template.
	 * @return settlement template element
	 * @throws Exception if settlement template does not exist for the name.
	 */
	private Element getSettlementTemplateElement(String name) throws Exception {
		Element result = null;
		
		Element root = settlementDoc.getDocumentElement();
		Element templateList = (Element) root.getElementsByTagName(SETTLEMENT_TEMPLATE_LIST).item(0);
		NodeList templateNodes = templateList.getElementsByTagName(TEMPLATE);
		for (int x=0; x < templateNodes.getLength(); x++) {
			Element templateElement = (Element) templateNodes.item(x);
			String templateName = templateElement.getAttribute(NAME);
			if (templateName.equals(name)) result = templateElement;
		}
		
		if (result == null) throw new Exception("Template name: " + name + 
			" could not be found in settlements.xml.");
		
		return result;
	}
	
	/**
	 * Gets the building types in a settlement template.
	 * If there are multiple buildings of the same type, they are separately listed.
	 * @param templateName the name of the settlement template.
	 * @return list of building types as strings.
	 * @throws Exception if there isn't a settlement template with this name or
	 * if there is an XML parsing error.
	 */
	public List getTemplateBuildingTypes(String templateName) throws Exception {
		List result = new ArrayList();
		
		Element templateElement = getSettlementTemplateElement(templateName);
		NodeList buildingNodes = templateElement.getElementsByTagName(BUILDING);
		for (int x=0; x < buildingNodes.getLength(); x++) {
			Element buildingElement = (Element) buildingNodes.item(x);
			String type = buildingElement.getAttribute(TYPE);
			int number = Integer.parseInt(buildingElement.getAttribute(NUMBER));
			for (int y=0; y < number; y++) result.add(type); 
		}
		
		return result;
	}
	
	/**
	 * Gets the vehicle types in a settlement template.
	 * If there are multiple vehicles of the same type, they are separately listed.
	 * @param templateName the name of the settlement template.
	 * @return list of vehicle types as strings.
	 * @throws Exception if there isn't a settlement template with this name or
	 * if there is an XML parsing error.
	 */
	public List getTemplateVehicleTypes(String templateName) throws Exception {
		List result = new ArrayList();
		
		Element templateElement = getSettlementTemplateElement(templateName);
		NodeList vehicleNodes = templateElement.getElementsByTagName(VEHICLE);
		for (int x=0; x < vehicleNodes.getLength(); x++) {
			Element vehicleElement = (Element) vehicleNodes.item(x);
			String type = vehicleElement.getAttribute(TYPE);
			int number = Integer.parseInt(vehicleElement.getAttribute(NUMBER));
			for (int y=0; y < number; y++) result.add(type); 
		}
		
		return result;
	}
	
	/**
	 * Gets the number of resupplies for a settlement template.
	 * @param templateName the name of the settlement template.
	 * @return number of resupplies
	 * @throws Exception if XML parsing error.
	 */
	public int getNumberOfTemplateResupplies(String templateName) throws Exception {
		int result = 0;
		Element templateElement = getSettlementTemplateElement(templateName);
		Element resupplyList = (Element) templateElement.getElementsByTagName(RESUPPLY).item(0);
		if (resupplyList != null) {
			NodeList resupplyNodes = resupplyList.getElementsByTagName(RESUPPLY_MISSION);
			result = resupplyNodes.getLength();
		}
		return result;
	}
	
	/**
	 * Gets the name of a settlement resupply for a particular settlement template.
	 * @param templateName the name of the settlement template.
	 * @param index the index of the resupply mission.
	 * @return name of the resupply mission.
	 * @throws Exception if XML parsing error.
	 */
	public String getTemplateResupplyName(String templateName, int index) throws Exception {
		Element templateElement = getSettlementTemplateElement(templateName);
		Element resupplyList = (Element) templateElement.getElementsByTagName(RESUPPLY).item(0);
		Element resupplyElement = (Element) resupplyList.getElementsByTagName(RESUPPLY_MISSION).item(index);
		return resupplyElement.getAttribute(NAME);
	}
	
	/**
	 * Gets the arrival time of a settlement resupply for a particular settlement template.
	 * @param templateName the name of the settlement template.
	 * @param index then index of the resupply mission.
	 * @return arrival time for the resupply mission (in Sols from when simulation starts).
	 * @throws Exception if XML parsing error.
	 */
	public double getTemplateResupplyArrivalTime(String templateName, int index) throws Exception {
		Element templateElement = getSettlementTemplateElement(templateName);
		Element resupplyList = (Element) templateElement.getElementsByTagName(RESUPPLY).item(0);
		Element resupplyElement = (Element) resupplyList.getElementsByTagName(RESUPPLY_MISSION).item(index);
		return Double.parseDouble(resupplyElement.getAttribute(ARRIVAL_TIME));
	}
	
	/**
	 * Gets a resupply element with a given name.
	 * @param name the name of the resupply element.
	 * @return resupply element.
	 * @throws Exception if resupply element could not be found.
	 */
	private Element getResupplyElement(String name) throws Exception {
		Element result = null;
		
		Element root = settlementDoc.getDocumentElement();
		Element resupplyList = (Element) root.getElementsByTagName(RESUPPLY_LIST).item(0);
		NodeList resupplyNodes = resupplyList.getElementsByTagName(RESUPPLY);
		for (int x=0; x < resupplyNodes.getLength(); x++) {
			Element resupplyElement = (Element) resupplyNodes.item(x);
			String resupplyName = resupplyElement.getAttribute(NAME);
			if (resupplyName.equals(name)) result = resupplyElement;
		}
		
		if (result == null) throw new Exception("Resupply name: " + name + 
			" could not be found in settlements.xml.");
		
		return result;
	}
	
	/**
	 * Gets a list of building types in the resupply mission.
	 * @param resupplyName name of the resupply mission.
	 * @return list of building types as strings.
	 * @throws Exception if XML parsing exception.
	 */
	public List getResupplyBuildingTypes(String resupplyName) throws Exception {
		List result = new ArrayList();
		
		Element resupplyElement = getResupplyElement(resupplyName);
		NodeList buildingNodes = resupplyElement.getElementsByTagName(BUILDING);
		for (int x = 0; x < buildingNodes.getLength(); x++) {
			Element buildingElement = (Element) buildingNodes.item(x);
			String type = buildingElement.getAttribute(TYPE);
			int number = Integer.parseInt(buildingElement.getAttribute(NUMBER));
			for (int y=0; y < number; y++) result.add(type); 
		}
		
		return result;
	}
	
	/**
	 * Gets a list of vehicle types in the resupply mission.
	 * @param resupplyName name of the resupply mission.
	 * @return list of vehicle types as strings.
	 * @throws Exception if XML parsing exception.
	 */
	public List getResupplyVehicleTypes(String resupplyName) throws Exception {
		List result = new ArrayList();
		
		Element resupplyElement = getResupplyElement(resupplyName);
		NodeList vehicleNodes = resupplyElement.getElementsByTagName(VEHICLE);
		for (int x = 0; x < vehicleNodes.getLength(); x++) {
			Element vehicleElement = (Element) vehicleNodes.item(x);
			String type = vehicleElement.getAttribute(TYPE);
			int number = Integer.parseInt(vehicleElement.getAttribute(NUMBER));
			for (int y=0; y < number; y++) result.add(type); 
		}
		
		return result;		
	}
	
	/**
	 * Gets the number of immigrants in a resupply mission.
	 * @param resupplyName name of the resupply mission.
	 * @return number of immigrants
	 * @throws Exception if XML parsing exception.
	 */
	public int getNumberOfResupplyImmigrants(String resupplyName) throws Exception {
		Element resupplyElement = getResupplyElement(resupplyName);
		Element personElement = (Element) resupplyElement.getElementsByTagName(PERSON).item(0);
		int number = Integer.parseInt(personElement.getAttribute(NUMBER));
		return number;
	}
	
	/**
	 * Gets a map of resources and their amounts in a resupply mission.
	 * @param resupplyName the name of the resupply mission.
	 * @return map of resource types (String) and their amounts (Double).
	 * @throws Exception if XML parsing exception
	 */
	public Map getResupplyResources(String resupplyName) throws Exception {
		Map result = new HashMap();
		
		Element resupplyElement = getResupplyElement(resupplyName);
		NodeList resourceNodes = resupplyElement.getElementsByTagName(RESOURCE);
		for (int x = 0; x < resourceNodes.getLength(); x++) {
			Element resourceElement = (Element) resourceNodes.item(x);
			String type = resourceElement.getAttribute(TYPE);
			Double amount = new Double(resourceElement.getAttribute(AMOUNT));
			result.put(type, amount);
		}
		
		return result;
	}
	
	/**
	 * Gets the number of initial settlements.
	 * @return number of settlements.
	 * @throws Exception if XML parsing error.
	 */
	public int getNumberOfInitialSettlements() throws Exception {
		Element root = settlementDoc.getDocumentElement();
		Element initialSettlementList = (Element) root.getElementsByTagName(INITIAL_SETTLEMENT_LIST).item(0);
		NodeList settlementNodes = initialSettlementList.getElementsByTagName(SETTLEMENT);
		return settlementNodes.getLength();
	}
	
	/**
	 * Gets the name of an initial settlement 
	 * or 'random' if the name is to chosen randomly from the settlement name list.
	 * @param index the index of the initial settlement.
	 * @return settlement name
	 * @throws Exception if index is out of range of the initial settlement list
	 * or if there is an XML parsing error.
	 */
	public String getInitialSettlementName(int index) throws Exception {
		Element root = settlementDoc.getDocumentElement();
		Element initialSettlementList = (Element) root.getElementsByTagName(INITIAL_SETTLEMENT_LIST).item(0);
		Element settlementElement = (Element) initialSettlementList.getElementsByTagName(SETTLEMENT).item(index);
		return settlementElement.getAttribute(NAME);
	}
	
	/**
	 * Gets the template used by an initial settlement.
	 * @param index the index of the initial settlement.
	 * @return settlement template name.
	 * @throws Exception if index is out of range of the initial settlement list
	 * or if there is an XML parsing error.
	 */
	public String getInitialSettlementTemplate(int index) throws Exception {
		Element root = settlementDoc.getDocumentElement();
		Element initialSettlementList = (Element) root.getElementsByTagName(INITIAL_SETTLEMENT_LIST).item(0);
		Element settlementElement = (Element) initialSettlementList.getElementsByTagName(SETTLEMENT).item(index);
		return settlementElement.getAttribute(TEMPLATE);
	}
	
	/**
	 * Gets the longitude of an initial settlement, 
	 * or 'random' if the longitude is to be randomly determined.
	 * @param index the index of the initial settlement.
	 * @return longitude of the settlement as a string. Example: '0.0 W'
	 * @throws Exception if index is out of range of the initial settlement list
	 * or if there is an XML parsing error.
	 */
	public String getInitialSettlementLongitude(int index) throws Exception {
		Element root = settlementDoc.getDocumentElement();
		Element initialSettlementList = (Element) root.getElementsByTagName(INITIAL_SETTLEMENT_LIST).item(0);
		Element settlementElement = (Element) initialSettlementList.getElementsByTagName(SETTLEMENT).item(index);
		Element locationElement = (Element) settlementElement.getElementsByTagName(LOCATION).item(0);
		return locationElement.getAttribute(LONGITUDE);
	}
	
	/**
	 * Gets the latitude of an initial settlement, 
	 * or 'random' if the longitude is to be randomly determined.
	 * @param index the index of the initial settlement.
	 * @return latitude of the settlement as a string. Example: '0.0 N'
	 * @throws Exception if index is out of range of the initial settlement list
	 * or if there is an XML parsing error.
	 */
	public String getInitialSettlementLatitude(int index) throws Exception {
		Element root = settlementDoc.getDocumentElement();
		Element initialSettlementList = (Element) root.getElementsByTagName(INITIAL_SETTLEMENT_LIST).item(0);
		Element settlementElement = (Element) initialSettlementList.getElementsByTagName(SETTLEMENT).item(index);
		Element locationElement = (Element) settlementElement.getElementsByTagName(LOCATION).item(0);
		return locationElement.getAttribute(LATITUDE);
	}
	
	/**
	 * Gets a list of possible settlement names.
	 * @return list of settlement names as strings
	 * @throws Exception if XML parsing error.
	 */
	public List getSettlementNameList() throws Exception {
		List result = new ArrayList();
		Element root = settlementDoc.getDocumentElement();
		Element settlementNameList = (Element) root.getElementsByTagName(SETTLEMENT_NAME_LIST).item(0);
		NodeList settlementNameNodes = settlementNameList.getElementsByTagName(SETTLEMENT_NAME);
		for (int x=0; x < settlementNameNodes.getLength(); x++) {
			Element settlementNameElement = (Element) settlementNameNodes.item(x);
			result.add(settlementNameElement.getAttribute(VALUE));
		}
		
		return result;
	}
}