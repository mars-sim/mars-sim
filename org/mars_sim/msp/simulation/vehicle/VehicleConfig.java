/**
 * Mars Simulation Project
 * VehicleConfig.java
 * @version 2.75 2004-03-23
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.vehicle;

import java.util.*;
import org.w3c.dom.*;

/**
 * Provides configuration information about vehicle units.
 * Uses a DOM document to get the information. 
 */
public class VehicleConfig {

	// Element names
	private static final String ROVER = "rover";
	private static final String TYPE = "type";
	private static final String RANGE = "range";
	private static final String FUEL_EFFICIENCY = "fuel-efficiency";
	private static final String BASE_SPEED = "base-speed";
	private static final String EMPTY_MASS = "empty-mass";
	private static final String CREW_SIZE = "crew-size";
	private static final String CARGO = "cargo";
	private static final String TOTAL_CAPACITY = "total-capacity";
	private static final String CAPACITY = "capacity";
	private static final String RESOURCE = "resource";
	private static final String VALUE = "value";
	private static final String EVA_SUITS = "eva-suits";
	private static final String SICKBAY = "sickbay";
	private static final String LAB = "lab";
	private static final String TECH_LEVEL = "tech-level";
	private static final String BEDS = "beds";
	private static final String TECH_SPECIALITY = "tech-speciality";
	private static final String ROVER_NAME_LIST = "rover-name-list";
	private static final String ROVER_NAME = "rover-name";

	private Document roverDoc;
	private List roverNames;
	
	/**
	 * Constructor
	 * @param roverDoc DOM document with rover configuration.
	 */
	public VehicleConfig(Document roverDoc) {
		this.roverDoc = roverDoc;
	}
	
	/**
	 * Gets a rover DOM element for a particular rover type.
	 * @param roverType the rover type
	 * @return rover element
	 * @throws Exception if rover type could not be found.
	 */
	private Element getRoverElement(String roverType) throws Exception {
		Element result = null;
		
		Element root = roverDoc.getDocumentElement();
		NodeList roverNodes = root.getElementsByTagName(ROVER);
		for (int x=0; x < roverNodes.getLength(); x++) {
			Element roverElement = (Element) roverNodes.item(x);
			String type = roverElement.getAttribute(TYPE);
			if (roverType.equals(type)) result = roverElement;
		}
		
		if (result == null) throw new Exception("Rover type: " + roverType + 
			" could not be found in vehicles.xml.");
		
		return result;
	}
	
	/**
	 * Gets the rover's range
	 * @param roverType the rover type
	 * @return range in km.
	 * @throws Exception if rover type could not be found or XML parsing error.
	 */
	public double getRange(String roverType) throws Exception {	
		Element roverElement = getRoverElement(roverType);
		Element rangeElement = (Element) roverElement.getElementsByTagName(RANGE).item(0);
		return Double.parseDouble(rangeElement.getAttribute(VALUE));
	}
	
	/**
	 * Gets the rover's fuel efficiency.
	 * @param roverType the rover type
	 * @return fuel efficiency in km/kg.
	 * @throws Exception if rover type could not be found or XML parsing error.
	 */
	public double getFuelEfficiency(String roverType) throws Exception {
		Element roverElement = getRoverElement(roverType);
		Element fuelEfficiencyElement = (Element) roverElement.getElementsByTagName(FUEL_EFFICIENCY).item(0);
		return Double.parseDouble(fuelEfficiencyElement.getAttribute(VALUE));
	}
	
	/**
	 * Gets the rover's base speed.
	 * @param roverType the rover type
	 * @return base speed in km/hr.
	 * @throws Exception if rover type could not be found or XML parsing error.
	 */
	public double getBaseSpeed(String roverType) throws Exception {
		Element roverElement = getRoverElement(roverType);
		Element baseSpeedElement = (Element) roverElement.getElementsByTagName(BASE_SPEED).item(0);
		return Double.parseDouble(baseSpeedElement.getAttribute(VALUE));
	}
	
	/**
	 * Gets the rover's mass when empty.
	 * @param roverType the rover type
	 * @return empty mass in kg.
	 * @throws Exception if rover type could not be found or XML parsing error.
	 */
	public double getEmptyMass(String roverType) throws Exception {
		Element roverElement = getRoverElement(roverType);
		Element emptyMassElement = (Element) roverElement.getElementsByTagName(EMPTY_MASS).item(0);
		return Double.parseDouble(emptyMassElement.getAttribute(VALUE));
	}
	
	/**
	 * Gets the rover's maximum crew size.
	 * @param roverType the rover type
	 * @return crew size
	 * @throws Exception if rover type could not be found or XML parsing error.
	 */
	public int getCrewSize(String roverType) throws Exception {
		Element roverElement = getRoverElement(roverType);
		Element crewSizeElement = (Element) roverElement.getElementsByTagName(CREW_SIZE).item(0);
		return Integer.parseInt(crewSizeElement.getAttribute(VALUE));
	}
	
	/**
	 * Gets the rover's total cargo capacity.
	 * @param roverType the rover type
	 * @return total cargo capacity
	 * @throws Exception if rover type could not be found or XML parsing error.
	 */
	public double getTotalCapacity(String roverType) throws Exception {
		Element roverElement = getRoverElement(roverType);
		Element cargoElement = (Element) roverElement.getElementsByTagName(CARGO).item(0);
		return Double.parseDouble(cargoElement.getAttribute(TOTAL_CAPACITY));
	}
	
	/**
	 * Gets the rover's capacity for a resource.
	 * @param roverType the rover type
	 * @param resource the resource
	 * @return rover capacity for resource
	 * @throws Exception if rover type could not be found or XML parsing error.
	 */
	public double getCargoCapacity(String roverType, String resource) throws Exception {
		
		double resourceCapacity = 0D;
		
		Element roverElement = getRoverElement(roverType);
		Element cargoElement = (Element) roverElement.getElementsByTagName(CARGO).item(0);
		NodeList capacityList = cargoElement.getElementsByTagName(CAPACITY);
		for (int x=0; x < capacityList.getLength(); x++) {
			Element capacityElement = (Element) capacityList.item(x);
			if (resource.toLowerCase().equals(capacityElement.getAttribute(RESOURCE).toLowerCase())) 
				resourceCapacity = Double.parseDouble(capacityElement.getAttribute(VALUE));
		}
		
		return resourceCapacity;
	}
	
	/**
	 * Gets the number of EVA suits in the rover.
	 * @param roverType the rover type
	 * @return the number of EVA suits
	 * @throws Exception if rover type could not be found or XML parsing error.
	 */
	public int getEvaSuits(String roverType) throws Exception {
		Element roverElement = getRoverElement(roverType);
		Element evaSuitsElement = (Element) roverElement.getElementsByTagName(EVA_SUITS).item(0);
		return Integer.parseInt(evaSuitsElement.getAttribute(VALUE));
	}
	
	/**
	 * Gets the rover's sickbay tech level.
	 * @param roverType the rover type
	 * @return tech level or -1 if no sickbay.
	 * @throws Exception if rover type could not be found or XML parsing error.
	 */
	public int getSickbayTechLevel(String roverType) throws Exception {
		int sickbayTechLevel = -1;
		
		Element roverElement = getRoverElement(roverType);
		try {
			Element sickbayElement = (Element) roverElement.getElementsByTagName(SICKBAY).item(0);
			sickbayTechLevel = Integer.parseInt(sickbayElement.getAttribute(TECH_LEVEL));
		}
		catch (NullPointerException e) {}
		
		return sickbayTechLevel;
	}
	
	/**
	 * Gets the rover's sickbay bed number.
	 * @param roverType the rover type
	 * @return number of sickbay beds or -1 if no sickbay.
	 * @throws Exception if rover type could not be found or XML parsing error.
	 */
	public int getSickbayBeds(String roverType) throws Exception {
		int sickbayBeds = -1;
		
		Element roverElement = getRoverElement(roverType);
		try {
			Element sickbayElement = (Element) roverElement.getElementsByTagName(SICKBAY).item(0);
			sickbayBeds = Integer.parseInt(sickbayElement.getAttribute(BEDS));
		}
		catch (NullPointerException e) {}
		
		return sickbayBeds;
	}
	
	/**
	 * Gets the rover's lab tech level.
	 * @param roverType the rover type
	 * @return lab tech level or -1 if no lab.
	 * @throws Exception if rover type could not be found or XML parsing error.
	 */
	public int getLabTechLevel(String roverType) throws Exception {
		int labTechLevel = -1;
		
		Element roverElement = getRoverElement(roverType);
		try {
			Element labElement = (Element) roverElement.getElementsByTagName(LAB).item(0);
			labTechLevel = Integer.parseInt(labElement.getAttribute(TECH_LEVEL));
		}
		catch (NullPointerException e) {}
		
		return labTechLevel;
	}
	
	/**
	 * Gets a list of the rover's lab tech specialities.
	 * @param roverType the rover type
	 * @return list of lab tech speciality strings.
	 * @throws Exception if rover type could not be found or XML parsing error.
	 */
	public List getLabTechSpecialities(String roverType) throws Exception {
		List specialities = new ArrayList();
		
		Element roverElement = getRoverElement(roverType);
		try {
			Element labElement = (Element) roverElement.getElementsByTagName(LAB).item(0);
			NodeList techSpecialityNodes = labElement.getElementsByTagName(TECH_SPECIALITY);
			for (int x=0; x < techSpecialityNodes.getLength(); x++) {
				Element techSpecialityElement = (Element) techSpecialityNodes.item(x);
				specialities.add(techSpecialityElement.getAttribute(VALUE));
			}
		}
		catch (NullPointerException e) {}
		
		return specialities;
	}
	
	/**
	 * Gets a list of rover names.
	 * @return list of rover names as strings.
	 * @throws Exception if XML parsing error.
	 */
	public List getRoverNameList() throws Exception {
		
		if (roverNames == null) {
			roverNames = new ArrayList();
			
			Element root = roverDoc.getDocumentElement();
			Element roverNameListElement = (Element) root.getElementsByTagName(ROVER_NAME_LIST).item(0);
			NodeList roverNameNodes = root.getElementsByTagName(ROVER_NAME);
			for (int x=0; x < roverNameNodes.getLength(); x++) {
				Element roverNameElement = (Element) roverNameNodes.item(x);
				roverNames.add(roverNameElement.getAttribute(VALUE));
			}
		}
		
		return roverNames;
	}
}