/**
 * Mars Simulation Project
 * VehicleConfig.java
 * @version 3.02 2011-11-26
 * @author Scott Davis
 */
package org.mars_sim.msp.core.vehicle;

import org.jdom.Document;
import org.jdom.Element;
import org.mars_sim.msp.core.resource.Part;

import java.io.Serializable;
import java.util.*;


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
	private static final String PART_ATTACHMENT = "part-attachment";
	private static final String NUMBER_SLOTS = "number-slots";
	private static final String PART = "part";
	private static final String NAME = "name";
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
    @SuppressWarnings("unchecked")
	public Set<String> getVehicleTypes() {
		Element root = vehicleDoc.getRootElement();
		List<Element> vehicleNodes = root.getChildren(VEHICLE);
		Set<String> types = new HashSet<String>(vehicleNodes.size());
		for (Element vehicleElement : vehicleNodes) {
			types.add(vehicleElement.getAttributeValue(TYPE));
		}
		return types;
	}
	
	/**
	 * Gets a vehicle DOM element for a particular vehicle type.
	 * @param vehicleType the vehicle type
	 * @return vehicle element
	 * @throws Exception if vehicle type could not be found.
	 */
    @SuppressWarnings("unchecked")
	private Element getVehicleElement(String vehicleType) {
		Element result = null;
		
		Element root = vehicleDoc.getRootElement();
		List<Element> vehicleNodes = root.getChildren(VEHICLE);
		
		for (Element vehicleElement : vehicleNodes) {
			String type = vehicleElement.getAttributeValue(TYPE);
			if (vehicleType.equalsIgnoreCase(type))
				result = vehicleElement;
		}
		
		if (result == null) throw new IllegalStateException("Vehicle type: " + vehicleType +
			" could not be found in vehicles.xml.");
		
		return result;
	}
	
	/**
	 * Gets the vehicle's fuel efficiency.
	 * @param vehicleType the vehicle type
	 * @return fuel efficiency in km/kg.
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public double getFuelEfficiency(String vehicleType) {
		Element vehicleElement = getVehicleElement(vehicleType);
		Element fuelEfficiencyElement = vehicleElement.getChild(FUEL_EFFICIENCY);
		return Double.parseDouble(fuelEfficiencyElement.getAttributeValue(VALUE));
	}
	
	/**
	 * Gets the vehicle's base speed.
	 * @param vehicleType the vehicle type
	 * @return base speed in km/hr.
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public double getBaseSpeed(String vehicleType) {
		Element vehicleElement = getVehicleElement(vehicleType);
		Element baseSpeedElement = vehicleElement.getChild(BASE_SPEED);
		return Double.parseDouble(baseSpeedElement.getAttributeValue(VALUE));
	}
	
	/**
	 * Gets the vehicle's mass when empty.
	 * @param vehicleType the vehicle type
	 * @return empty mass in kg.
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public double getEmptyMass(String vehicleType) {
		Element vehicleElement = getVehicleElement(vehicleType);
		Element emptyMassElement = vehicleElement.getChild(EMPTY_MASS);
		return Double.parseDouble(emptyMassElement.getAttributeValue(VALUE));
	}
	
	/**
	 * Gets the vehicle's maximum crew size.
	 * @param vehicleType the vehicle type
	 * @return crew size
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public int getCrewSize(String vehicleType) {
		Element vehicleElement = getVehicleElement(vehicleType);
		Element crewSizeElement = vehicleElement.getChild(CREW_SIZE);
		return Integer.parseInt(crewSizeElement.getAttributeValue(VALUE));
	}
	
	/**
	 * Gets the vehicle's total cargo capacity.
	 * @param vehicleType the vehicle type
	 * @return total cargo capacity
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public double getTotalCapacity(String vehicleType) {
		Element vehicleElement = getVehicleElement(vehicleType);
		Element cargoElement = vehicleElement.getChild(CARGO);
		return Double.parseDouble(cargoElement.getAttributeValue(TOTAL_CAPACITY));
	}
	
	/**
	 * Gets the vehicle's capacity for a resource.
	 * @param vehicleType the vehicle type
	 * @param resource the resource
	 * @return vehicle capacity for resource
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
    @SuppressWarnings("unchecked")
	public double getCargoCapacity(String vehicleType, String resource) {
		
		double resourceCapacity = 0D;
		
		Element vehicleElement = getVehicleElement(vehicleType);
		Element cargoElement = vehicleElement.getChild(CARGO);
		List<Element> capacityList = cargoElement.getChildren(CAPACITY);
		for (Element capacityElement : capacityList) {
			if (resource.toLowerCase().equals(capacityElement.getAttributeValue(RESOURCE).toLowerCase())) 
				resourceCapacity = Double.parseDouble(capacityElement.getAttributeValue(VALUE));
		}
		
		return resourceCapacity;
	}
	
	/**
	 * Checks if the vehicle has a sickbay.
	 * @param vehicleType the vehicle type
	 * @return true if sickbay
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
    @SuppressWarnings("unchecked")
	public boolean hasSickbay(String vehicleType) {
		Element vehicleElement = getVehicleElement(vehicleType);
		List<Element> sickbayNodes = vehicleElement.getChildren(SICKBAY);
		return (sickbayNodes.size() > 0);
	}
	
	/**
	 * Gets the vehicle's sickbay tech level.
	 * @param vehicleType the vehicle type
	 * @return tech level or -1 if no sickbay.
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public int getSickbayTechLevel(String vehicleType) {
		int sickbayTechLevel = -1;	
		Element vehicleElement = getVehicleElement(vehicleType);
	    Element sickbayElement = vehicleElement.getChild(SICKBAY);
	    
	    if(sickbayElement != null)
		sickbayTechLevel = Integer.parseInt(sickbayElement.getAttributeValue(TECH_LEVEL));
	
		return sickbayTechLevel;
	}
	
	/**
	 * Gets the vehicle's sickbay bed number.
	 * @param vehicleType the vehicle type
	 * @return number of sickbay beds or -1 if no sickbay.
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public int getSickbayBeds(String vehicleType) {
		int sickbayBeds = -1;
		
		Element vehicleElement = getVehicleElement(vehicleType);
	    Element sickbayElement = vehicleElement.getChild(SICKBAY);
	    
	    if(sickbayElement != null)
		sickbayBeds = Integer.parseInt(sickbayElement.getAttributeValue(BEDS));
		
		return sickbayBeds;
	}
	
	/**
	 * Checks if the vehicle has a lab.
	 * @param vehicleType the vehicle type
	 * @return true if lab
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
    @SuppressWarnings("unchecked")
	public boolean hasLab(String vehicleType) {	
		Element vehicleElement = getVehicleElement(vehicleType);
		List<Element> labNodes = vehicleElement.getChildren(LAB);
		return (labNodes.size() > 0);
	}	
	
	/**
	 * Gets the vehicle's lab tech level.
	 * @param vehicleType the vehicle type
	 * @return lab tech level or -1 if no lab.
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public int getLabTechLevel(String vehicleType) {
		int labTechLevel = -1;
		
		Element vehicleElement = getVehicleElement(vehicleType);
		Element labElement = vehicleElement.getChild(LAB);
		
		if(labElement != null)
		labTechLevel = Integer.parseInt(labElement.getAttributeValue(TECH_LEVEL));
		
		
		return labTechLevel;
	}
	
	/**
	 * Gets a list of the vehicle's lab tech specialities.
	 * @param vehicleType the vehicle type
	 * @return list of lab tech speciality strings.
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
    @SuppressWarnings("unchecked")
	public List<String> getLabTechSpecialities(String vehicleType) {
		List<String> specialities = new ArrayList<String>();
		
		Element vehicleElement = getVehicleElement(vehicleType);
		
		Element labElement = vehicleElement.getChild(LAB);
			
		if(labElement != null) {
			List<Element> techSpecialityNodes = labElement.getChildren(TECH_SPECIALITY);
			for (Element techSpecialityElement : techSpecialityNodes) {
				specialities.add(techSpecialityElement.getAttributeValue(VALUE));
			}
		}

		
		return specialities;
	}
	
	/**
	 * Checks if a vehicle type has the ability to attach parts.
	 * @param vehicleType the vehicle type
	 * @return true if can attach parts.
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
    @SuppressWarnings("unchecked")
	public boolean hasPartAttachments(String vehicleType) {
		Element vehicleElement = getVehicleElement(vehicleType);
		List<Element> partAttachmentNodes = vehicleElement.getChildren(PART_ATTACHMENT);
		return (partAttachmentNodes.size() > 0);
	}
	
	/**
	 * Gets the number of part attachment slots for a vehicle.
	 * @param vehicleType the vehicle type.
	 * @return number of part attachment slots.
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public int getPartAttachmentSlotNumber(String vehicleType) {
		int result = 0;
		
		Element vehicleElement = getVehicleElement(vehicleType);
			Element partAttachmentElement = vehicleElement.getChild(PART_ATTACHMENT);
			
			if(partAttachmentElement != null)
			result = Integer.parseInt(partAttachmentElement.getAttributeValue(NUMBER_SLOTS));
		
		return result;
	}
	
	/**
	 * Gets all of the parts that can be attached to a vehicle.
	 * @param vehicleType the vehicle type
	 * @return collection of parts that are attachable.
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
    @SuppressWarnings("unchecked")
	public Collection<Part> getAttachableParts(String vehicleType) {
		Collection<Part> result = new ArrayList<Part>();
		
		Element vehicleElement = getVehicleElement(vehicleType);

	    Element partAttachmentElement = vehicleElement.getChild(PART_ATTACHMENT);
			
		if(partAttachmentElement != null) {
			List<Element> partNodes = partAttachmentElement.getChildren(PART);
			
			for (Element partElement : partNodes) {
				String partName = partElement.getAttributeValue(NAME);
				Part part = (Part) Part.findItemResource(partName);
				result.add(part);
			}
		}
	
		
		return result;
	}
	
	/**
	 * Gets a list of rover names.
	 * @return list of rover names as strings.
	 * @throws Exception if XML parsing error.
	 */
    @SuppressWarnings("unchecked")
	public List<String> getRoverNameList() {
		
		if (roverNames == null) {
			roverNames = new ArrayList<String>();
			
			Element root = vehicleDoc.getRootElement();
			Element vehicleNameListElement = root.getChild(ROVER_NAME_LIST);
			List<Element> vehicleNameNodes = vehicleNameListElement.getChildren(ROVER_NAME);
			
			for (Element vehicleNameElement : vehicleNameNodes) {
				roverNames.add(vehicleNameElement.getAttributeValue(VALUE));
			}
		}
		
		return roverNames;
	}
    
    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        vehicleDoc = null;
        if(roverNames != null){

            roverNames.clear();
            roverNames = null;
        }
    }
}