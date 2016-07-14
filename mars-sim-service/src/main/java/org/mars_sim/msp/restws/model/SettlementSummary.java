package org.mars_sim.msp.restws.model;

public class SettlementSummary extends EntityReference {

	private int numPersons;
	private int numParkedVehicles;
	private int numBuildings;
	
	public int getNumPersons() {
		return numPersons;
	}
	public void setNumPersons(int numPersons) {
		this.numPersons = numPersons;
	}
	public int getNumParkedVehicles() {
		return numParkedVehicles;
	}
	public void setNumParkedVehicles(int numParkedVehicles) {
		this.numParkedVehicles = numParkedVehicles;
	}
	public int getNumBuildings() {
		return numBuildings;
	}
	public void setNumBuildings(int numBuildings) {
		this.numBuildings = numBuildings;
	}
	
}
