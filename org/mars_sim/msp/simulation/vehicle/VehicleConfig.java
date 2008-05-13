/**
 * Mars Simulation Project
 * VehicleConfig.java
 * @version 2.84 2008-05-13
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.vehicle;

import java.io.Serializable;
import java.util.*;
import org.w3c.dom.*;

/**
 * Provides configuration information about vehicle units.
 * Uses a DOM document to get the information. 
 */
public class VehicleConfig implements Serializable {

	// Element names
	private static final String VEHICLE = "vehicle";
	private static final String TYPE = "type";
	private static final String FUEL_EFFICIENCY = "fuel-efficiency";
	private static final String BASE_SPEED = "base-speed";
	private static final String EMPTY_MASS = "empty-mass";
	private static final String CREW_SIZE = "crew-size";
	private static final String CARGO = "cargo";
	private static final String TOTAL_CAPACITY = "total-capacity";
	private static final String CAPACITY = "capacity";
	private static final String RESOURCE = "resource";
	private static final String VALUE = "value";
	private static final String SICKBAY = "sickbay";
	private static final String LAB = "lab";
	private static final String TECH_LEVEL = "tech-level";
	private static final String BEDS = "beds";
	private static final String TECH_SPECIALITY = "tech-speciality";
	private static final String ROVER_NAME_LIST = "rover-name-list";
	private static final String ROVER_NAME = "rover-name";

	private Document vehicleDoc;
	private List<String> roverNames;
	
	/**
	 * Constructor
	 * @param vehicleDoc DOM document with vehicle configuration.
	 */
	public VehicleConfig(Document vehicleDoc) {
		this.vehicleDoc = vehicleDoc;
	}
	
	/**
	 * Returns a set of all vehicle types.
	 * @return set of vehicle types as strings.
	 * @throws Exception if error retrieving vehicle types.
	 */
	public Set<String> getVehicleTypes() throws Exception {
		Element root = vehicleDoc.getDocumentElement();
		NodeList vehicleNodes = root.getElementsByTagName(VEHICLE);
		Set<String> types = new HashSet<String>(vehicleNodes.getLength());
		for (int x=0; x < vehicleNodes.getLength(); x++) {
			Element vehicleElement = (Element) vehicleNodes.item(x);
			types.add(vehicleElement.getAttribute(TYPE));
		}
		return types;
	}
	
	/**
	 * Gets a vehicle DOM element for a particular vehicle type.
	 * @param vehicleType the vehicle type
	 * @return vehicle element
	 * @throws Exception if vehicle type could not be found.
	 */
	private Element getVehicleElement(String vehicleType) throws Exception {
		Element result = null;
		
		Element root = vehicleDoc.getDocumentElement();
		NodeList vehicleNodes = root.getElementsByTagName(VEHICLE);
		for (int x=0; x < vehicleNodes.getLength(); x++) {
			Element vehicleElement = (Element) vehicleNodes.item(x);
			String type = vehicleElement.getAttribute(TYPE);
			if (vehicleType.equalsIgnoreCase(type)) result = vehicleElement;
		}
		
		if (result == null) throw new Exception("Vehicle type: " + vehicleType + 
			" could not be found in vehicles.xml.");
		
		return result;
	}
	
	/**
	 * Gets the vehicle's fuel efficiency.
	 * @param vehicleType the vehicle type
	 * @return fuel efficiency in km/kg.
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public double getFuelEfficiency(String vehicleType) throws Exception {
		Element vehicleElement = getVehicleElement(vehicleType);
		Element fuelEfficiencyElement = (Element) vehicleElement.getElementsByTagName(FUEL_EFFICIENCY).item(0);
		return Double.parseDouble(fuelEfficiencyElement.getAttribute(VALUE));
	}
	
	/**
	 * Gets the vehicle's base speed.
	 * @param vehicleType the vehicle type
	 * @return base speed in km/hr.
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public double getBaseSpeed(String vehicleType) throws Exception {
		Element vehicleElement = getVehicleElement(vehicleType);
		Element baseSpeedElement = (Element) vehicleElement.getElementsByTagName(BASE_SPEED).item(0);
		return Double.parseDouble(baseSpeedElement.getAttribute(VALUE));
	}
	
	/**
	 * Gets the vehicle's mass when empty.
	 * @param vehicleType the vehicle type
	 * @return empty mass in kg.
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public double getEmptyMass(String vehicleType) throws Exception {
		Element vehicleElement = getVehicleElement(vehicleType);
		Element emptyMassElement = (Element) vehicleElement.getElementsByTagName(EMPTY_MASS).item(0);
		return Double.parseDouble(emptyMassElement.getAttribute(VALUE));
	}
	
	/**
	 * Gets the vehicle's maximum crew size.
	 * @param vehicleType the vehicle type
	 * @return crew size
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public int getCrewSize(String vehicleType) throws Exception {
		Element vehicleElement = getVehicleElement(vehicleType);
		Element crewSizeElement = (Element) vehicleElement.getElementsByTagName(CREW_SIZE).item(0);
		return Integer.parseInt(crewSizeElement.getAttribute(VALUE));
	}
	
	/**
	 * Gets the vehicle's total cargo capacity.
	 * @param vehicleType the vehicle type
	 * @return total cargo capacity
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public double getTotalCapacity(String vehicleType) throws Exception {
		Element vehicleElement = getVehicleElement(vehicleType);
		Element cargoElement = (Element) vehicleElement.getElementsByTagName(CARGO).item(0);
		return Double.parseDouble(cargoElement.getAttribute(TOTAL_CAPACITY));
	}
	
	/**
	 * Gets the vehicle's capacity for a resource.
	 * @param vehicleType the vehicle type
	 * @param resource the resource
	 * @return vehicle capacity for resource
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public double getCargoCapacity(String vehicleType, String resource) throws Exception {
		
		double resourceCapacity = 0D;
		
		Element vehicleElement = getVehicleElement(vehicleType);
		Element cargoElement = (Element) vehicleElement.getElementsByTagName(CARGO).item(0);
		NodeList capacityList = cargoElement.getElementsByTagName(CAPACITY);
		for (int x=0; x < capacityList.getLength(); x++) {
			Element capacityElement = (Element) capacityList.item(x);
			if (resource.toLowerCase().equals(capacityElement.getAttribute(RESOURCE).toLowerCase())) 
				resourceCapacity = Double.parseDouble(capacityElement.getAttribute(VALUE));
		}
		
		return resourceCapacity;
	}
	
	/**
	 * Checks if the vehicle has a sickbay.
	 * @param vehicleType the vehicle type
	 * @return true if sickbay
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public boolean hasSickbay(String vehicleType) throws Exception {
		boolean result = false;
		
		Element vehicleElement = getVehicleElement(vehicleType);
		NodeList sickbayNodes = vehicleElement.getElementsByTagName(SICKBAY);
		if (sickbayNodes.getLength() > 0) result = true;
		
		return result;
	}
	
	/**
	 * Gets the vehicle's sickbay tech level.
	 * @param vehicleType the vehicle type
	 * @return tech level or -1 if no sickbay.
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public int getSickbayTechLevel(String vehicleType) throws Exception {
		int sickbayTechLevel = -1;
		
		Element vehicleElement = getVehicleElement(vehicleType);
		try {
			Element sickbayElement = (Element) vehicleElement.getElementsByTagName(SICKBAY).item(0);
			sickbayTechLevel = Integer.parseInt(sickbayElement.getAttribute(TECH_LEVEL));
		}
		catch (NullPointerException e) {}
		
		return sickbayTechLevel;
	}
	
	/**
	 * Gets the vehicle's sickbay bed number.
	 * @param vehicleType the vehicle type
	 * @return number of sickbay beds or -1 if no sickbay.
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public int getSickbayBeds(String vehicleType) throws Exception {
		int sickbayBeds = -1;
		
		Element vehicleElement = getVehicleElement(vehicleType);
		try {
			Element sickbayElement = (Element) vehicleElement.getElementsByTagName(SICKBAY).item(0);
			sickbayBeds = Integer.parseInt(sickbayElement.getAttribute(BEDS));
		}
		catch (NullPointerException e) {}
		
		return sickbayBeds;
	}
	
	/**
	 * Checks if the vehicle has a lab.
	 * @param vehicleType the vehicle type
	 * @return true if lab
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public boolean hasLab(String vehicleType) throws Exception {
		boolean result = false;
		
		Element vehicleElement = getVehicleElement(vehicleType);
		NodeList labNodes = vehicleElement.getElementsByTagName(LAB);
		if (labNodes.getLength() > 0) result = true;
		
		return result;
	}	
	
	/**
	 * Gets the vehicle's lab tech level.
	 * @param vehicleType the vehicle type
	 * @return lab tech level or -1 if no lab.
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public int getLabTechLevel(String vehicleType) throws Exception {
		int labTechLevel = -1;
		
		Element vehicleElement = getVehicleElement(vehicleType);
		try {
			Element labElement = (Element) vehicleElement.getElementsByTagName(LAB).item(0);
			labTechLevel = Integer.parseInt(labElement.getAttribute(TECH_LEVEL));
		}
		catch (NullPointerException e) {}
		
		return labTechLevel;
	}
	
	/**
	 * Gets a list of the vehicle's lab tech specialities.
	 * @param vehicleType the vehicle type
	 * @return list of lab tech speciality strings.
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public List<String> getLabTechSpecialities(String vehicleType) throws Exception {
		List<String> specialities = new ArrayList<String>();
		
		Element vehicleElement = getVehicleElement(vehicleType);
		try {
			Element labElement = (Element) vehicleElement.getElementsByTagName(LAB).item(0);
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
	public List<String> getRoverNameList() throws Exception {
		
		if (roverNames == null) {
			roverNames = new ArrayList<String>();
			
			Element root = vehicleDoc.getDocumentElement();
			Element vehicleNameListElement = (Element) root.getElementsByTagName(ROVER_NAME_LIST).item(0);
			NodeList vehicleNameNodes = vehicleNameListElement.getElementsByTagName(ROVER_NAME);
			for (int x=0; x < vehicleNameNodes.getLength(); x++) {
				Element vehicleNameElement = (Element) vehicleNameNodes.item(x);
				roverNames.add(vehicleNameElement.getAttribute(VALUE));
			}
		}
		
		return roverNames;
	}
}