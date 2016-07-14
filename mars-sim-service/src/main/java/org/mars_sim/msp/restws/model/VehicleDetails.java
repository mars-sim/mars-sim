package org.mars_sim.msp.restws.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleDetails  extends VehicleSummary {
	private double speed;
	private double distanceLastMaintenance;
	private double fuelEfficiency;
	private EntityReference towingVehicle;
	private EntityReference settlement;
	private int numPersons;
	private int numItems;
	private int numResources;
	
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	public double getDistanceLastMaintenance() {
		return distanceLastMaintenance;
	}
	public void setDistanceLastMaintenance(double distanceLastMaintenance) {
		this.distanceLastMaintenance = distanceLastMaintenance;
	}
	public double getFuelEfficiency() {
		return fuelEfficiency;
	}
	public void setFuelEfficiency(double fuelEfficiency) {
		this.fuelEfficiency = fuelEfficiency;
	}
	public EntityReference getTowingVehicle() {
		return towingVehicle;
	}
	public void setTowingVehicle(EntityReference towingVehicle) {
		this.towingVehicle = towingVehicle;
	}
	public EntityReference getSettlement() {
		return settlement;
	}
	public void setSettlement(EntityReference settlement) {
		this.settlement = settlement;
	}
	public int getNumPersons() {
		return numPersons;
	}
	public void setNumPersons(int numPersons) {
		this.numPersons = numPersons;
	}
	public int getNumItems() {
		return numItems;
	}
	public void setNumItems(int numItems) {
		this.numItems = numItems;
	}
	public int getNumResources() {
		return numResources;
	}
	public void setNumResources(int numResources) {
		this.numResources = numResources;
	}
}
