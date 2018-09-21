package org.mars_sim.msp.restws.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * This DTO represents a Building entity. The entity has many properties and a deep entity graph.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BuildingDetails {
	private int id;
	private String name;
	private String buildingType;
	private String powerMode;
	private String heatMode;
	private double temperature;
	private List<String> functions;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBuildingType() {
		return buildingType;
	}
	public void setBuildingType(String buildingType) {
		this.buildingType = buildingType;
	}
	public String getPowerMode() {
		return powerMode;
	}
	public void setPowerMode(String powerMode) {
		this.powerMode = powerMode;
	}
	public String getHeatMode() {
		return heatMode;
	}
	public void setHeatMode(String heatMode) {
		this.heatMode = heatMode;
	}
	public double getTemperature() {
		return temperature;
	}
	public void setTemperature(double currentTemperature) {
		this.temperature = currentTemperature;
	}
/*
	public List<String> getFunctions() {
		return functions;
	}
	public void setFunctions(List<String> functions) {
		this.functions = functions;
	}
*/
}
