package org.mars_sim.msp.restws.model;

/**
 * Summary DTO of a Vehicle
 */
public class VehicleSummary extends EntityReference{

	private String status;
	private String vehicleType;
	

	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getVehicleType() {
		return vehicleType;
	}
	public void setVehicleType(String vehicleType) {
		this.vehicleType = vehicleType;
	}

}
