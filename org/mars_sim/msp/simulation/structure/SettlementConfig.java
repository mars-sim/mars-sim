/**
 * Mars Simulation Project
 * SettlementConfig.java
 * @version 2.75 2004-04-08
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