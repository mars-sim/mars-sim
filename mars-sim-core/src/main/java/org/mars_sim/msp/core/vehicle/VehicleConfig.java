/**
 * Mars Simulation Project
 * VehicleConfig.java
 * @version 3.06 2014-01-29
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

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	// Element names
	private static final String VEHICLE = "vehicle";
	private static final String TYPE = "type";
	private static final String WIDTH = "width";
	private static final String LENGTH = "length";
	private static final String DESCRIPTION = "description";
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
	private Map<String,VehicleDescription> map = null;

	private class VehicleDescription implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		String description;
		double width,length;
		double fuelEff,baseSpeed,emptyMass;
		int crewSize;
		double totalCapacity;
		Map<String,Double> cargoCapacity;
		boolean hasSickbay,hasLab,hasAttachments;
		int sickbayLevel,sickbayBeds;
		int labLevel,attachmentSlots;
		List<String> labSpecialties;
		List<Part> attachments;
	}

	/**
	 * Constructor.
	 * @param vehicleDoc {@link Document} DOM document with vehicle configuration.
	 */
	public VehicleConfig(Document vehicleDoc) {
		this.vehicleDoc = vehicleDoc;
	}

	@SuppressWarnings("unchecked")
	private void parse() {
		// only parse when neccessary
		if (map == null) {
			map = new HashMap<String,VehicleDescription>();
			Element root = vehicleDoc.getRootElement();
			List<Element> vehicleNodes = root.getChildren(VEHICLE);
			for (Element vehicleElement : vehicleNodes) {
				String type = vehicleElement.getAttributeValue(TYPE).toLowerCase();
				// vehicle description
				VehicleDescription v = new VehicleDescription();
				v.width = Double.parseDouble(vehicleElement.getAttributeValue(WIDTH));
				v.length = Double.parseDouble(vehicleElement.getAttributeValue(LENGTH));
				v.description = "no description available.";
				if (vehicleElement.getChildren(DESCRIPTION).size() > 0) {
					vehicleElement.getChildText(DESCRIPTION);
				}
				v.fuelEff = Double.parseDouble(vehicleElement.getChild(FUEL_EFFICIENCY).getAttributeValue(VALUE));
				v.baseSpeed = Double.parseDouble(vehicleElement.getChild(BASE_SPEED).getAttributeValue(VALUE));
				v.emptyMass = Double.parseDouble(vehicleElement.getChild(EMPTY_MASS).getAttributeValue(VALUE));
				v.crewSize = Integer.parseInt(vehicleElement.getChild(CREW_SIZE).getAttributeValue(VALUE));
				// cargo capacities
				Element cargoElement = vehicleElement.getChild(CARGO);
				v.cargoCapacity = new HashMap<String,Double>();
				if (cargoElement != null) {
					double resourceCapacity = 0D;
					List<Element> capacityList = cargoElement.getChildren(CAPACITY);
					for (Element capacityElement : capacityList) {
						resourceCapacity = Double.parseDouble(capacityElement.getAttributeValue(VALUE));
						v.cargoCapacity.put(
							capacityElement.getAttributeValue(RESOURCE).toLowerCase(),
							resourceCapacity
						);
					}
					v.totalCapacity = Double.parseDouble(cargoElement.getAttributeValue(TOTAL_CAPACITY));
				} else v.totalCapacity = 0d;
				// sickbay
				v.sickbayBeds = 0;
				v.sickbayLevel = -1;
				v.hasSickbay = (vehicleElement.getChildren(SICKBAY).size() > 0);
				if (v.hasSickbay) {
					Element sickbayElement = vehicleElement.getChild(SICKBAY);
					if (sickbayElement != null) {
						v.sickbayLevel = Integer.parseInt(sickbayElement.getAttributeValue(TECH_LEVEL));
						v.sickbayBeds = Integer.parseInt(sickbayElement.getAttributeValue(BEDS));
					}
				};
				// labs
				v.labLevel = -1;
				v.labSpecialties = new ArrayList<String>();
				v.hasLab = (vehicleElement.getChildren(LAB).size() > 0);
				if (v.hasLab) {
					Element labElement = vehicleElement.getChild(LAB);
					if (labElement != null) {
						v.labLevel = Integer.parseInt(labElement.getAttributeValue(TECH_LEVEL));
						for (Object tech : labElement.getChildren(TECH_SPECIALITY)) {
							v.labSpecialties.add(
								(((Element) tech).getAttributeValue(VALUE)).toLowerCase()
							);
						}
					}
				}
				// attachments
				v.attachments = new ArrayList<Part>();
				v.attachmentSlots = 0;
				v.hasAttachments = (vehicleElement.getChildren(PART_ATTACHMENT).size() > 0);
				if (v.hasAttachments) {
					Element attachmentElement = vehicleElement.getChild(PART_ATTACHMENT);
					v.attachmentSlots = Integer.parseInt(attachmentElement.getAttributeValue(NUMBER_SLOTS));
					for (Object part : attachmentElement.getChildren(PART)) {
						v.attachments.add(
							(Part) Part.findItemResource(
								(((Element) part).getAttributeValue(NAME)).toLowerCase()
							)
						);
					}
				}
				// TODO
				// and keep results for later use
				map.put(type,v);
			}
		}
	}

	/**
	 * Returns a set of all vehicle types.
	 * @return set of vehicle types as strings.
	 * @throws Exception if error retrieving vehicle types.
	 */
	public Set<String> getVehicleTypes() {
		parse();
		return map.keySet();
	}

	/**
	 * Gets the vehicle's width.
	 * @param vehicleType the vehicle type.
	 * @return width (meters).
	 */
	public double getWidth(String vehicleType) {
		parse();
		return map.get(vehicleType).width;
	}

	/**
	 * Gets the vehicle's length.
	 * @param vehicleType the vehicle type.
	 * @return length (meters).
	 */
	public double getLength(String vehicleType) {
		parse();
		return map.get(vehicleType).length;
	}

	/**
	 * Gets the vehicle's fuel efficiency.
	 * @param vehicleType the vehicle type
	 * @return fuel efficiency in km/kg.
	 * @throws Exception if vehicle type could not be found or XML parsing error.
	 */
	public double getFuelEfficiency(String vehicleType) {
		parse();
		return map.get(vehicleType).fuelEff;
	}

	/**
	 * Gets the vehicle's base speed.
	 * @param vehicleType the vehicle type
	 * @return base speed in km/hr.
	 */
	public double getBaseSpeed(String vehicleType) {
		parse();
		return map.get(vehicleType).baseSpeed;
	}

	/**
	 * Gets the vehicle's mass when empty.
	 * @param vehicleType the vehicle type
	 * @return empty mass in kg.
	 */
	public double getEmptyMass(String vehicleType) {
		parse();
		return map.get(vehicleType).emptyMass;
	}

	/**
	 * Gets the vehicle's maximum crew size.
	 * @param vehicleType the vehicle type
	 * @return crew size
	 */
	public int getCrewSize(String vehicleType) {
		parse();
		return map.get(vehicleType).crewSize;
	}
	
	/**
	 * Gets the vehicle's total cargo capacity.
	 * @param vehicleType the vehicle type
	 * @return total cargo capacity
	 */
	public double getTotalCapacity(String vehicleType) {
		parse();
		return map.get(vehicleType).totalCapacity;
	}
	
	/**
	 * Gets the vehicle's capacity for a resource.
	 * @param vehicleType the vehicle type
	 * @param resource the resource
	 * @return vehicle capacity for resource might be <code>null</code>
	 */
	public Double getCargoCapacity(String vehicleType, String resource) {
		parse();
		Double value = map.get(vehicleType).cargoCapacity.get(resource);
		if (value == null) return 0d;
		return value;
	}
	
	/**
	 * Checks if the vehicle has a sickbay.
	 * @param vehicleType the vehicle type
	 * @return true if sickbay
	 */
	public boolean hasSickbay(String vehicleType) {
		parse();
		return map.get(vehicleType).hasSickbay;
	}
	
	/**
	 * Gets the vehicle's sickbay tech level.
	 * @param vehicleType the vehicle type
	 * @return tech level or -1 if no sickbay.
	 */
	public int getSickbayTechLevel(String vehicleType) {
		parse();
		return map.get(vehicleType).sickbayLevel;
	}
	
	/**
	 * Gets the vehicle's sickbay bed number.
	 * @param vehicleType the vehicle type
	 * @return number of sickbay beds or -1 if no sickbay.
	 */
	public int getSickbayBeds(String vehicleType) {
		parse();
		return map.get(vehicleType).sickbayBeds;
	}
	
	/**
	 * Checks if the vehicle has a lab.
	 * @param vehicleType the vehicle type
	 * @return true if lab
	 */
	public boolean hasLab(String vehicleType) {	
		parse();
		return map.get(vehicleType).hasLab;
	}	
	
	/**
	 * Gets the vehicle's lab tech level.
	 * @param vehicleType the vehicle type
	 * @return lab tech level or -1 if no lab.
	 */
	public int getLabTechLevel(String vehicleType) {
		parse();
		return map.get(vehicleType).labLevel;
	}
	
	/**
	 * Gets a list of the vehicle's lab tech specialities.
	 * @param vehicleType the vehicle type
	 * @return list of lab tech speciality strings.
	 */
	public List<String> getLabTechSpecialities(String vehicleType) {
		parse();
		return map.get(vehicleType).labSpecialties;
	}
	
	/**
	 * Checks if a vehicle type has the ability to attach parts.
	 * @param vehicleType the vehicle type
	 * @return true if can attach parts.
	 */
	public boolean hasPartAttachments(String vehicleType) {
		parse();
		return map.get(vehicleType).hasAttachments;
	}
	
	/**
	 * Gets the number of part attachment slots for a vehicle.
	 * @param vehicleType the vehicle type.
	 * @return number of part attachment slots.
	 */
	public int getPartAttachmentSlotNumber(String vehicleType) {
		parse();
		return map.get(vehicleType).attachmentSlots;
	}
	
	/**
	 * Gets all of the parts that can be attached to a vehicle.
	 * @param vehicleType the vehicle type
	 * @return collection of parts that are attachable.
	 */
	public Collection<Part> getAttachableParts(String vehicleType) {
		parse();
		return map.get(vehicleType).attachments;
	}

	public String getDescription(String vehicleType) {
		parse();
		return map.get(vehicleType).description;
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