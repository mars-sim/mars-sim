/**
 * Mars Simulation Project
 * SickBay.java
 * @version 3.1.0 2017-09-07
 * @author Scott Davis
 */

package org.mars_sim.msp.core.vehicle;

import java.io.Serializable;

import org.mars_sim.msp.core.person.health.MedicalStation;

/**
 * The SickBay class is a medical station for a vehicle.
 */
public class SickBay
extends MedicalStation
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** The vehicle this sickbay is in. */
	private Vehicle vehicle;

	/**
	 * Constructor.
	 * @param vehicle The vehicle the sickbay is in.
	 * @param treatmentLevel The treatment level of the medical station.
	 * @param sickBedNum Number of sickbeds. 
	 */
	public SickBay(Vehicle vehicle, int treatmentLevel, int sickBedNum) {
		// Use MedicalStation constructor
		super(treatmentLevel, sickBedNum);

		setVehicle(vehicle);
		this.vehicle = vehicle;
	}

	/**
	 * Gets the vehicle this sickbay is in.
	 * @return vehicle
	 */
	public Vehicle getVehicle() {
		return vehicle;
	}

	@Override
	public void destroy() {
		super.destroy();
		vehicle = null;
	}
}