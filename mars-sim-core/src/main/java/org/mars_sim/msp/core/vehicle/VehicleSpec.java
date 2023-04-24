/*
 * Mars Simulation Project
 * VehicleSpec.java
 * @date 2023-04-17
 * @author Barry Evans
 */
package org.mars_sim.msp.core.vehicle;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.science.ScienceType;

/** 
 * The Specification of a Vehicle loaded from the external configuration.
 */
public class VehicleSpec implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 12L;

	// Data members
	private boolean hasLab = false;
	private boolean hasPartAttachments = false;
	private boolean hasSickbay = false;

	private int crewSize;
	private int sickbayTechLevel = -1;
	private int sickbayBeds = 0;
	private int labTechLevel = -1;
	private int attachmentSlots;
	private int batteryModule;	
	private int fuelCellStack;
	
	private double width, length;
	private double drivetrainEff;
	private double baseSpeed;
	private double averagePower;
	private double emptyMass;
	private double totalCapacity = 0D;

	private String name;
	private VehicleType type;
	private String description;
	
	private Map<Integer, Double> cargoCapacityMap;
	private List<ScienceType> labTechSpecialties = null;
	private List<Part> attachableParts = null;
	private List<LocalPosition> operatorActivitySpots;
	private List<LocalPosition> passengerActivitySpots;
	private List<LocalPosition> sickBayActivitySpots;
	private List<LocalPosition> labActivitySpots;
	
	private LocalPosition airlockLoc;
	private LocalPosition airlockInteriorLoc;
	private LocalPosition airlockExteriorLoc;

	private double terrainHandling;

	private String baseImage;


	public VehicleSpec(String name, VehicleType type, String description, String baseImage,
			int batteryModule, int fuelCellStack,
			double drivetrainEff, 
			double baseSpeed, double averagePower,
			double emptyMass, int crewSize) {
		this.name = name;
		this.type = type;
		this.description = description;
		this.batteryModule = batteryModule;	
		this.fuelCellStack = fuelCellStack;
		this.drivetrainEff = drivetrainEff;
		this.baseSpeed = baseSpeed;
		this.averagePower = averagePower;
		this.emptyMass = emptyMass;
		this.crewSize = crewSize;
		this.baseImage = baseImage;
	}

	public final void setWidth(double width) {
		this.width = width;
	}
	
	public final void setLength(double length) {
		this.length = length;
	}
	
	/**
	 * get <code>0.0d</code> or capacity for given cargo.
	 * 
	 * @param cargo {@link String}
	 * @return {@link Double}
	 */
	public final double getCargoCapacity(int resourceId) {
		if (cargoCapacityMap != null) {
			return cargoCapacityMap.getOrDefault(resourceId, 0D);
		}
		
		return 0D;
	}

	/**
	 * Get the name of the vehicle specification
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get the type of the vehicle specification
	 * @return
	 */
	public VehicleType getType() {
		return type;
	}

	/** @return the description */
	public final String getDescription() {
		return description;
	}

	/** @return the width */
	public final double getWidth() {
		return width;
	}

	/** @return the length */
	public final double getLength() {
		return length;
	}

	/** @return the batteryModule */
	public int getBatteryModule() {
		return batteryModule;
	}
	
	/** @return the fuelCellStack */
	public int getFuelCellStack() {
		return fuelCellStack;
	}
	
	/** @return the driveTrainEff */
	public final double getDriveTrainEff() {
		return drivetrainEff;
	}

	/** @return the baseSpeed */
	public final double getBaseSpeed() {
		return baseSpeed;
	}
	
	/** @return the averagePower */
	public final double getAveragePower() {
		return averagePower;
	}

	/** @return the emptyMass */
	public final double getEmptyMass() {
		return emptyMass;
	}

	/** @return the crewSize */
	public final int getCrewSize() {
		return crewSize;
	}

	/** @return the totalCapacity */
	public final double getTotalCapacity() {
		return totalCapacity;
	}

	/** @return the cargoCapacity */
	public final Map<Integer, Double> getCargoCapacityMap() {
		return cargoCapacityMap;
	}

	/** @return the hasSickbay */
	public final boolean hasSickbay() {
		return hasSickbay;
	}

	/** @return the hasLab */
	public final boolean hasLab() {
		return hasLab;
	}

	/** @return the hasPartAttachments */
	public final boolean hasPartAttachments() {
		return hasPartAttachments;
	}

	/** @return the sickbayTechLevel */
	public final int getSickbayTechLevel() {
		return sickbayTechLevel;
	}

	/** @return the sickbayBeds */
	public final int getSickbayBeds() {
		return sickbayBeds;
	}

	/** @return the labTechLevel */
	public final int getLabTechLevel() {
		return labTechLevel;
	}

	/** @return the attachmentSlots */
	public final int getAttachmentSlots() {
		return attachmentSlots;
	}

	/** @return the labTechSpecialties */
	public final List<ScienceType> getLabTechSpecialties() {
		return labTechSpecialties;
	}

	/** @return the attachableParts */
	public final List<Part> getAttachableParts() {
		return attachableParts;
	}

	/** @return the airlockLoc */
	public final LocalPosition getAirlockLoc() {
		return airlockLoc;
	}

	/** @return the airlockInteriorLoc */
	public final LocalPosition getAirlockInteriorLoc() {
		return airlockInteriorLoc;
	}

	/** @return the airlockExteriorXLoc */
	public final LocalPosition getAirlockExteriorLoc() {
		return airlockExteriorLoc;
	}

	/** @return the operator activity spots. */
	public final List<LocalPosition> getOperatorActivitySpots() {
		return operatorActivitySpots;
	}

	/** @return the passenger activity spots. */
	public final List<LocalPosition> getPassengerActivitySpots() {
		return passengerActivitySpots;
	}

	/** @return the sick bay activity spots. */
	public final List<LocalPosition> getSickBayActivitySpots() {
		return sickBayActivitySpots;
	}

	/** @return the lab activity spots. */
	public final List<LocalPosition> getLabActivitySpots() {
		return labActivitySpots;
	}

	void setSickBay(int sickbayTechLevel2, int sickbayBeds2) {
		this.hasSickbay = true;
		this.sickbayBeds = sickbayBeds2;
		this.sickbayTechLevel = sickbayTechLevel2;
	}

	void setLabSpec(int labTechLevel2, List<ScienceType> labTechSpecialties2) {
		this.hasLab = true;
		this.labTechLevel = labTechLevel2;
		this.labTechSpecialties = Collections.unmodifiableList(labTechSpecialties2);
	}

	void setAttachments(int attachmentSlots2, List<Part> attachableParts2) {
		this.hasPartAttachments = true;
		this.attachmentSlots = attachmentSlots2;
		this.attachableParts = Collections.unmodifiableList(attachableParts2);
	}

	void setActivitySpots(List<LocalPosition> operatorActivitySpots2, List<LocalPosition> passengerActivitySpots2,
			List<LocalPosition> sickBayActivitySpots2, List<LocalPosition> labActivitySpots2) {
		this.operatorActivitySpots = Collections.unmodifiableList(operatorActivitySpots2);
		this.passengerActivitySpots = Collections.unmodifiableList(passengerActivitySpots2);
		this.sickBayActivitySpots = Collections.unmodifiableList(sickBayActivitySpots2);
		this.labActivitySpots = Collections.unmodifiableList(labActivitySpots2);	
	}

	void setAirlock(LocalPosition airlockLoc, LocalPosition airlockInteriorLoc,
			LocalPosition airlockExteriorLoc) {
		this.airlockLoc = airlockLoc;
		this.airlockInteriorLoc = airlockInteriorLoc;
		this.airlockExteriorLoc = airlockExteriorLoc;
	}

	void setCargoCapacity(double totalCapacity2, Map<Integer, Double> cargoCapacityMap2) {
		this.totalCapacity = totalCapacity2;
		this.cargoCapacityMap = cargoCapacityMap2;
	}

	void setTerrainHandling(double terrainHandling) {
		this.terrainHandling = terrainHandling;
	}
	
	public int getPartAttachmentSlotNumber() {
		return attachmentSlots;
	}

	public double getTerrainHandling() {
		return terrainHandling;
	}

    public String getBaseImage() {
        return baseImage;
    }
}
