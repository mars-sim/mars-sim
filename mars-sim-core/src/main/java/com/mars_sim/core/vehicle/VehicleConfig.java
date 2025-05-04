/*
 * Mars Simulation Project
 * VehicleConfig.java
 * @date 2023-06-05
 * @author Barry Evans
 */
package com.mars_sim.core.vehicle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;

import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.manufacture.ManufactureConfig;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.science.ScienceType;

/**
 * Provides configuration information about vehicle units. Uses a DOM document
 * to get the information.
 */
public class VehicleConfig {
	
	// Element names
	private static final String VEHICLE = "vehicle";
	private static final String NAME = "name";
	private static final String MODEL = "model";
	private static final String TYPE = "type";
	private static final String FUEL = "fuel";
	private static final String BASE_IMAGE = "base-image";
	private static final String WIDTH = "width";
	private static final String LENGTH = "length";
	private static final String DESCRIPTION = "description";
	private static final String POWER_SOURCE = "power-source";	
	private static final String BATTERY_MODULE = "battery-module";
	private static final String ENERGY_PER_MODULE = "energy-per-module";
	private static final String FUEL_CELL_STACK = "fuel-cell-stack";
	private static final String DRIVETRAIN_EFFICIENCY = "drivetrain-efficiency";
	private static final String BASE_SPEED = "base-speed";
	private static final String BASE_POWER = "base-power";
	private static final String EMPTY_MASS = "empty-mass";
	private static final String CREW_SIZE = "crew-size";
	private static final String CARGO = "cargo";
	private static final String TOTAL_CAPACITY = "total-capacity";
	private static final String CAPACITY = "capacity";
	private static final String SICKBAY = "sickbay";
	private static final String LAB = "lab";
	private static final String TECH_LEVEL = "tech-level";
	private static final String CAP_LEVEL = "capacity";
	private static final String BEDS = "beds";
	private static final String TECH_SPECIALTY = "tech-specialty";
	private static final String PART_ATTACHMENT = "part-attachment";
	private static final String NUMBER_SLOTS = "number-slots";
	private static final String PART = "part";
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
	private static final String AMOUNT = "amount";

	private Map<String, VehicleSpec> vehicleSpecMap;
	
	/**
	 * Constructor.
	 * 
	 * @param vehicleDoc {@link Document} DOM document with vehicle configuration.
	 * @param manuCon Use to calculate vehcile construction details
	 */
	public VehicleConfig(Document vehicleDoc, ManufactureConfig manuConfig) {
		loadVehicleSpecs(vehicleDoc, manuConfig);
	}

	/**
	 * Parses only once. Stores resulting data for later use.
	 * 
	 * @param vehicleDoc
	 * @param manuConfig 
	 */
	private synchronized void loadVehicleSpecs(Document vehicleDoc, ManufactureConfig manuConfig) {
		if (vehicleSpecMap != null) {
			// just in case if another thread is being created
			return;
		}
		
		// Build the global list in a temp to avoid access before it is built
		Map<String, VehicleSpec> newMap = new HashMap<>();
		
		Element root = vehicleDoc.getRootElement();
		List<Element> vehicleNodes = root.getChildren(VEHICLE);
		for (Element vehicleElement : vehicleNodes) {
			var vSpec = parseVehicleSpec(vehicleElement, manuConfig);

			// Keep results for later use
			newMap.put(vSpec.getName().toLowerCase(), vSpec);
		}
		
		vehicleSpecMap = Collections.unmodifiableMap(newMap);
	}

	private static VehicleSpec parseVehicleSpec(Element vehicleElement, ManufactureConfig manuConfig) {
		String name = vehicleElement.getAttributeValue(NAME);
		String model = vehicleElement.getAttributeValue(MODEL);
		VehicleType type = ConfigHelper.getEnum(VehicleType.class, vehicleElement.getAttributeValue(TYPE));

		String baseImage = vehicleElement.getAttributeValue(BASE_IMAGE);
		if (baseImage == null) {
			baseImage = type.name().toLowerCase().replace(" ", "_");
		}

		// vehicle description
		double width = ConfigHelper.getAttributeDouble(vehicleElement, WIDTH);
		double length = ConfigHelper.getAttributeDouble(vehicleElement, LENGTH);
		String description = "No Description is Available.";
		if (!vehicleElement.getChildren(DESCRIPTION).isEmpty()) {
			description = vehicleElement.getChildText(DESCRIPTION);
		}
		
		Element powerSourceElement = vehicleElement.getChild(POWER_SOURCE);
		String powerSourceType = powerSourceElement.getAttributeValue(TYPE);
		String fuelTypeStr = powerSourceElement.getAttributeValue(FUEL);
		double powerValue = ConfigHelper.getAttributeDouble(powerSourceElement, VALUE);
		
		int battery = ConfigHelper.getAttributeInt(vehicleElement.getChild(BATTERY_MODULE),
													NUMBER);
		double energyPerModule = ConfigHelper.getAttributeDouble(vehicleElement.getChild(ENERGY_PER_MODULE),
												VALUE);
		int fuelCell = ConfigHelper.getAttributeInt(vehicleElement.getChild(FUEL_CELL_STACK), NUMBER);
		
		double drivetrainEff = ConfigHelper.getAttributeDouble(vehicleElement.getChild(DRIVETRAIN_EFFICIENCY),
												VALUE);
		double baseSpeed = ConfigHelper.getAttributeDouble(vehicleElement.getChild(BASE_SPEED), VALUE);
		double basePower = ConfigHelper.getAttributeDouble(vehicleElement.getChild(BASE_POWER), VALUE);
		double emptyMass = ConfigHelper.getAttributeDouble(vehicleElement.getChild(EMPTY_MASS), VALUE);
		
		int crewSize = ConfigHelper.getAttributeInt(vehicleElement.getChild(CREW_SIZE), VALUE);

		VehicleSpec vSpec = new VehicleSpec(name, type, model, description, baseImage, 
				powerSourceType, fuelTypeStr, powerValue,
				battery, energyPerModule, fuelCell, 
				drivetrainEff, baseSpeed, basePower, emptyMass, 
				crewSize);
		
		vSpec.setWidth(width);
		vSpec.setLength(length);
		
		// Ground vehicle terrain handling ability
		if (vehicleElement.getChild(TERRAIN_HANDLING) != null) {
			vSpec.setTerrainHandling(ConfigHelper.getAttributeDouble(
							vehicleElement.getChild(TERRAIN_HANDLING), VALUE));
		}

		// cargo capacities
		Element cargoElement = vehicleElement.getChild(CARGO);
		if (cargoElement != null) {
			Map<Integer, Double> cargoCapacityMap = ConfigHelper.parseDoubleList("Vehicle spec " + name,
															cargoElement.getChildren(CAPACITY),	
															TYPE, k -> ResourceUtil.findAmountResource(k).getID(),
							            					AMOUNT);

			double totalCapacity = ConfigHelper.getAttributeDouble(cargoElement, TOTAL_CAPACITY);
			vSpec.setCargoCapacity(totalCapacity, cargoCapacityMap);
		}

		// Use the cargo capacity for performance analysis
		vSpec.calculateDetails(manuConfig);
		
		// sickbay
		Element sickbayElement = vehicleElement.getChild(SICKBAY);
		if (sickbayElement != null) {
			int sickbayTechLevel = ConfigHelper.getAttributeInt(sickbayElement, TECH_LEVEL);
			int sickbayBeds = ConfigHelper.getAttributeInt(sickbayElement, BEDS);
			vSpec.setSickBay(sickbayTechLevel, sickbayBeds);
		}

		// labs
		Element labElement = vehicleElement.getChild(LAB);
		if (labElement != null) {
			List<ScienceType> labTechSpecialties = new ArrayList<>();
			int labTechLevel = ConfigHelper.getAttributeInt(labElement, TECH_LEVEL);
			int labCapacity = ConfigHelper.getAttributeInt(labElement, CAP_LEVEL);
			for (Element tech : labElement.getChildren(TECH_SPECIALTY)) {
				String scienceName = tech.getAttributeValue(VALUE);
				labTechSpecialties.add(ConfigHelper.getEnum(ScienceType.class, scienceName));
			}
			
			vSpec.setLabSpec(labTechLevel, labCapacity, labTechSpecialties);
		}

		// attachments
		if (!vehicleElement.getChildren(PART_ATTACHMENT).isEmpty()) {
			List<Part> attachableParts = new ArrayList<>();
			Element attachmentElement = vehicleElement.getChild(PART_ATTACHMENT);
			int attachmentSlots = ConfigHelper.getAttributeInt(attachmentElement, NUMBER_SLOTS);
			for (Element part : attachmentElement.getChildren(PART)) {
				attachableParts.add((Part) ItemResourceUtil
						.findItemResource(((part.getAttributeValue(NAME)).toLowerCase())));
			}
			vSpec.setAttachments(attachmentSlots, attachableParts);
		}

		// airlock locations (optional).
		Element airlockElement = vehicleElement.getChild(AIRLOCK);
		if (airlockElement != null) {
			LocalPosition airlockLoc = ConfigHelper.parseLocalPosition(airlockElement);
			LocalPosition airlockInteriorLoc = ConfigHelper.parseLocalPosition(airlockElement.getChild(INTERIOR_LOCATION));
			LocalPosition airlockExteriorLoc = ConfigHelper.parseLocalPosition(airlockElement.getChild(EXTERIOR_LOCATION));
			
			vSpec.setAirlock(airlockLoc, airlockInteriorLoc, airlockExteriorLoc);
		}

		// Activity spots.
		Element activityElement = vehicleElement.getChild(ACTIVITY);
		if (activityElement != null) {

			// Initialize activity spot lists.
			List<LocalPosition> operatorActivitySpots = new ArrayList<>();
			List<LocalPosition> passengerActivitySpots = new ArrayList<>();
			List<LocalPosition> sickBayActivitySpots = new ArrayList<>();
			List<LocalPosition> labActivitySpots = new ArrayList<>();

			for (var activitySpotElement : activityElement.getChildren(ACTIVITY_SPOT)) {
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
			
			vSpec.setActivitySpots(operatorActivitySpots, passengerActivitySpots, sickBayActivitySpots, labActivitySpots);
		}
		return vSpec;
	}

	/**
	 * Returns a set of all vehicle types.
	 * 
	 * @return set of vehicle types as strings.
	 * @throws Exception if error retrieving vehicle types.
	 */
	public Collection<VehicleSpec> getVehicleSpecs() {
		return Collections.unmodifiableCollection(vehicleSpecMap.values());
	}

	/**
	 * Gets the vehicle description class.
	 * 
	 * @param vehicleType
	 * @return {@link VehicleSpec}
	 */
	public VehicleSpec getVehicleSpec(String vehicleType) {
		return vehicleSpecMap.get(vehicleType.toLowerCase());
	}
	
	/**
	 * Prepares object for garbage collection. or simulation reboot.
	 */
	public void destroy() {
		if (vehicleSpecMap != null) {
			vehicleSpecMap = null;
		}
	}
}
