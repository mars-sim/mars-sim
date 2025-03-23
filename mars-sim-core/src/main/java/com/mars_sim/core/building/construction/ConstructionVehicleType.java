/*
 * Mars Simulation Project
 * ConstructionVehicleType.java
 * @date 2021-12-15
 * @author Scott Davis
 */
package com.mars_sim.core.building.construction;

import java.io.Serializable;
import java.util.List;

import com.mars_sim.core.vehicle.VehicleType;

/**
 * Construction vehicle information.
 */
public class ConstructionVehicleType
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private VehicleType vehicleType;
	private List<Integer> attachmentParts;

	/**
	 * Constructor.
	 * @param vehicleType the vehicle type.
	 * @param attachmentParts list of attachment parts.
	 */
	public ConstructionVehicleType(
		VehicleType vehicleType, List<Integer> attachmentParts
	) {

		this.vehicleType = vehicleType;
		this.attachmentParts = attachmentParts;
	}

	/**
	 * Gets the attachment parts.
	 * @return list of parts.
	 */
	public List<Integer> getAttachmentParts() {
		return attachmentParts;
	}

	/**
	 * Gets the vehicle type.
	 * @return vehicle type.
	 */
	public VehicleType getVehicleType() {
		return vehicleType;
	}

	@Override
	public String toString() {
		return vehicleType.name();
	}
}
