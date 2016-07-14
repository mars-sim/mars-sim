package org.mars_sim.msp.restws.model;

public class SettlementDetails extends SettlementSummary {
	private int populationCapacity;
	private double airPressure;
	private double temperature;
	private double totalScientificAchievement; 
	private CoordinateDTO coordinates;
	private int numResources;
	private int numItems;
	
	public int getPopulationCapacity() {
		return populationCapacity;
	}
	public void setPopulationCapacity(int populationCapacity) {
		this.populationCapacity = populationCapacity;
	}
	public double getAirPressure() {
		return airPressure;
	}
	public void setAirPressure(double airPressure) {
		this.airPressure = airPressure;
	}
	public double getTemperature() {
		return temperature;
	}
	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}
	public double getTotalScientificAchievement() {
		return totalScientificAchievement;
	}
	public void setTotalScientificAchievement(double totalScientificAchievement) {
		this.totalScientificAchievement = totalScientificAchievement;
	}
	public CoordinateDTO getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(CoordinateDTO coordinates) {
		this.coordinates = coordinates;
	}
	public int getNumResources() {
		return numResources;
	}
	public void setNumResources(int numResources) {
		this.numResources = numResources;
	}
	public int getNumItems() {
		return numItems;
	}
	public void setNumItems(int numItems) {
		this.numItems = numItems;
	}
}