/*
 * Mars Simulation Project
 * VehicleConfig.java
 * @date 2023-04-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.vehicle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.configuration.ConfigHelper;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.science.ScienceType;

/**
 * Provides configuration information about vehicle units. Uses a DOM document
 * to get the information.
 */
public class VehicleConfig {


	private static final Logger logger = Logger.getLogger(VehicleConfig.class.getName());
	
	// Element names
	private static final String VEHICLE = "vehicle";
	private static final String TYPE = "type";
	private static final String WIDTH = "width";
	private static final String LENGTH = "length";
	private static final String DESCRIPTION = "description";
	private static final String BATTERY_MODULE = "battery-module";
	private static final String FUEL_CELL_STACK = "fuel-cell-stack";
	private static final String DRIVETRAIN_EFFICIENCY = "drivetrain-efficiency";
	private static final String BASE_SPEED = "base-speed";
	private static final String AVERAGE_POWER = "average-power";
	private static final String EMPTY_MASS = "empty-mass";
	private static final String CREW_SIZE = "crew-size";
	private static final String CARGO = "cargo";
	private static final String TOTAL_CAPACITY = "total-capacity";
	private static final String CAPACITY = "capacity";
	private static final String RESOURCE = "resource";
	private static final String SICKBAY = "sickbay";
	private static final String LAB = "lab";
	private static final String TECH_LEVEL = "tech-level";
	private static final String BEDS = "beds";
	private static final String TECH_SPECIALTY = "tech-specialty";
	private static final String PART_ATTACHMENT = "part-attachment";
	private static final String NUMBER_SLOTS = "number-slots";
	private static final String PART = "part";
	private static final String NAME = "name";
	private static final String AIRLOCK = "airlock";
	private static final String INTERIOR_LOCATION = "interior";
	private static final String EXTERIOR_LOCATION = "exterior";
	private static final String ACTIVITY = "activity";
	private static final String ACTIVITY_SPOT = "activity-spot";
	private static final String OPERATOR_TYPE = "operator";
	private static final String PASSENGER_TYPE = "passenger";
	private static final String SICKBAY_TYPE = "sickbay";
	private static final String LAB_TYPE = "lab";
	private static final String TERRAIN_HANDLING = "terrain-handling";
	
	private static final String VALUE = "value";
	private static final String NUMBER = "number";

	private Map<String, VehicleSpec> map;
	
	/**
	 * Constructor.
	 * 
	 * @param vehicleDoc {@link Document} DOM document with vehicle configuration.
	 */
	public VehicleConfig(Document vehicleDoc) {
		loadVehicleSpecs(vehicleDoc);
	}

	/**
	 * Parses only once. Stores resulting data for later use.
	 * 
	 * @param vehicleDoc
	 */
	private synchronized void loadVehicleSpecs(Document vehicleDoc) {
		if (map != null) {
			// just in case if another thread is being created
			return;
		}
		
		// Build the global list in a temp to avoid access before it is built
		Map<String, VehicleSpec> newMap = new HashMap<>();
		
		Element root = vehicleDoc.getRootElement();
		List<Element> vehicleNodes = root.getChildren(VEHICLE);
		for (Element vehicleElement : vehicleNodes) {
			String type = vehicleElement.getAttributeValue(TYPE);

			// vehicle description
			double width = Double.parseDouble(vehicleElement.getAttributeValue(WIDTH));
			double length = Double.parseDouble(vehicleElement.getAttributeValue(LENGTH));
			String description = "No Description is Available.";
			if (vehicleElement.getChildren(DESCRIPTION).size() > 0) {
				description = vehicleElement.getChildText(DESCRIPTION);
			}
			int battery = Integer.parseInt(vehicleElement.getChild(BATTERY_MODULE).getAttributeValue(NUMBER));
			int fuelCell = Integer.parseInt(vehicleElement.getChild(FUEL_CELL_STACK).getAttributeValue(NUMBER));
        	
			double drivetrainEff = Double
					.parseDouble(vehicleElement.getChild(DRIVETRAIN_EFFICIENCY).getAttributeValue(VALUE));
			double baseSpeed = Double.parseDouble(vehicleElement.getChild(BASE_SPEED).getAttributeValue(VALUE));
			double averagePower = Double.parseDouble(vehicleElement.getChild(AVERAGE_POWER).getAttributeValue(VALUE));
			double emptyMass = Double.parseDouble(vehicleElement.getChild(EMPTY_MASS).getAttributeValue(VALUE));
			
			int crewSize = Integer.parseInt(vehicleElement.getChild(CREW_SIZE).getAttributeValue(VALUE));

			VehicleSpec v = new VehicleSpec(type, description, battery, fuelCell, 
					drivetrainEff, baseSpeed, averagePower, emptyMass, crewSize);
			
			v.setWidth(width);
			v.setLength(length);
			
			// Ground vehicle details
			if (vehicleElement.getChild(TERRAIN_HANDLING) != null) {
				v.setTerrainHandling(Double.parseDouble(vehicleElement.getChild(TERRAIN_HANDLING).getAttributeValue(VALUE)));
			}

			// cargo capacities
			Element cargoElement = vehicleElement.getChild(CARGO);
			if (cargoElement != null) {
				Map<Integer, Double> cargoCapacityMap = new HashMap<>();
				double resourceCapacity = 0D;
				List<Element> capacityList = cargoElement.getChildren(CAPACITY);
				for (Element capacityElement : capacityList) {
					resourceCapacity = Double.parseDouble(capacityElement.getAttributeValue(VALUE));

					// toLowerCase() is crucial in matching resource name
					String resource = capacityElement.getAttributeValue(RESOURCE).toLowerCase();

					AmountResource ar = ResourceUtil.findAmountResource(resource);
					if (ar == null)
						logger.severe(
								resource + " shows up in vehicles.xml but doesn't exist in resources.xml.");
					else
						cargoCapacityMap.put(ar.getID(), resourceCapacity);
				}

				double totalCapacity = Double.parseDouble(cargoElement.getAttributeValue(TOTAL_CAPACITY));
				v.setCargoCapacity(totalCapacity, cargoCapacityMap);
			} 

			// sickbay
			if (!vehicleElement.getChildren(SICKBAY).isEmpty()) {
				Element sickbayElement = vehicleElement.getChild(SICKBAY);
				if (sickbayElement != null) {
					int sickbayTechLevel = Integer.parseInt(sickbayElement.getAttributeValue(TECH_LEVEL));
					int sickbayBeds = Integer.parseInt(sickbayElement.getAttributeValue(BEDS));
					v.setSickBay(sickbayTechLevel, sickbayBeds);
				}
			}

			// labs
			if (!vehicleElement.getChildren(LAB).isEmpty()) {
				Element labElement = vehicleElement.getChild(LAB);
				if (labElement != null) {
					List<ScienceType> labTechSpecialties = new ArrayList<>();
					int labTechLevel = Integer.parseInt(labElement.getAttributeValue(TECH_LEVEL));
					for (Element tech : labElement.getChildren(TECH_SPECIALTY)) {
						String scienceName = tech.getAttributeValue(VALUE);
						labTechSpecialties
								.add(ScienceType.valueOf(ConfigHelper.convertToEnumName(scienceName)));
					}
					
					v.setLabSpec(labTechLevel, labTechSpecialties);
				}
			}

			// attachments
			if (!vehicleElement.getChildren(PART_ATTACHMENT).isEmpty()) {
				List<Part> attachableParts = new ArrayList<>();
				Element attachmentElement = vehicleElement.getChild(PART_ATTACHMENT);
				int attachmentSlots = Integer.parseInt(attachmentElement.getAttributeValue(NUMBER_SLOTS));
				for (Element part : attachmentElement.getChildren(PART)) {
					attachableParts.add((Part) ItemResourceUtil
							.findItemResource(((part.getAttributeValue(NAME)).toLowerCase())));
				}
				v.setAttachments(attachmentSlots, attachableParts);
			}

			// airlock locations (optional).
			Element airlockElement = vehicleElement.getChild(AIRLOCK);
			if (airlockElement != null) {
				LocalPosition airlockLoc = ConfigHelper.parseLocalPosition(airlockElement);
				LocalPosition airlockInteriorLoc = ConfigHelper.parseLocalPosition(airlockElement.getChild(INTERIOR_LOCATION));
				LocalPosition airlockExteriorLoc = ConfigHelper.parseLocalPosition(airlockElement.getChild(EXTERIOR_LOCATION));
				
				v.setAirlock(airlockLoc, airlockInteriorLoc, airlockExteriorLoc);
			}

			// Activity spots.
			Element activityElement = vehicleElement.getChild(ACTIVITY);
			if (activityElement != null) {

				// Initialize activity spot lists.
				List<LocalPosition> operatorActivitySpots = new ArrayList<>();
				List<LocalPosition> passengerActivitySpots = new ArrayList<>();
				List<LocalPosition> sickBayActivitySpots = new ArrayList<>();
				List<LocalPosition> labActivitySpots = new ArrayList<>();

				for (Object activitySpot : activityElement.getChildren(ACTIVITY_SPOT)) {
					Element activitySpotElement = (Element) activitySpot;
					LocalPosition spot = ConfigHelper.parseLocalPosition(activitySpotElement);
					String activitySpotType = activitySpotElement.getAttributeValue(TYPE);
					if (OPERATOR_TYPE.equals(activitySpotType)) {
						operatorActivitySpots.add(spot);
					} else if (PASSENGER_TYPE.equals(activitySpotType)) {
						passengerActivitySpots.add(spot);
					} else if (SICKBAY_TYPE.equals(activitySpotType)) {
						sickBayActivitySpots.add(spot);
					} else if (LAB_TYPE.equals(activitySpotType)) {
						labActivitySpots.add(spot);
					}
				}
				
				v.setActivitySpots(operatorActivitySpots, passengerActivitySpots, sickBayActivitySpots, labActivitySpots);
			}

			// Keep results for later use
			newMap.put(type.toLowerCase(), v);
		}
		
		map = Collections.unmodifiableMap(newMap);
	}

	/**
	 * Returns a set of all vehicle types.
	 * 
	 * @return set of vehicle types as strings.
	 * @throws Exception if error retrieving vehicle types.
	 */
	public Collection<VehicleSpec> getVehicleSpecs() {
		return Collections.unmodifiableCollection(map.values());
	}

	/**
	 * Gets the vehicle description class.
	 * 
	 * @param vehicleType
	 * @return {@link VehicleSpec}
	 */
	public VehicleSpec getVehicleSpec(String vehicleType) {
		return map.get(vehicleType.toLowerCase());
	}
	
	/**
	 * Prepares object for garbage collection. or simulation reboot.
	 */
	public void destroy() {
		if (map != null) {
			map = null;
		}
	}
}
