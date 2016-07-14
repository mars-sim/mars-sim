package org.mars_sim.msp.restws.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO of the Person details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonDetails extends PersonSummary {
	
	private String personalityType;
	
    private double fatigue;
    private double hunger;
    private double stress;
    private double performance;
    private String healthSituation;
	private int height;
	private EntityReference vehicle;
	private String birthDate;
	private double mass;
	private EntityReference settlement;
	private String taskPhase;
	private EntityReference mission;

	public String getPersonalityType() {
		return personalityType;
	}

	public void setPersonalityType(String personalityType) {
		this.personalityType = personalityType;
	}

	public double getFatigue() {
		return fatigue;
	}

	public void setFatigue(double fatigue) {
		this.fatigue = fatigue;
	}

	public double getHunger() {
		return hunger;
	}

	public void setHunger(double hunger) {
		this.hunger = hunger;
	}

	public double getStress() {
		return stress;
	}

	public void setStress(double stress) {
		this.stress = stress;
	}

	public EntityReference getVehicle() {
		return vehicle;
	}

	public void setVehicle(EntityReference vehicle) {
		this.vehicle = vehicle;
	}

	public double getPerformance() {
		return performance;
	}

	public void setPerformance(double performance) {
		this.performance = performance;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public EntityReference getSettlement() {
		return settlement;
	}

	public void setSettlement(EntityReference settlement) {
		this.settlement = settlement;
	}

	public String getTaskPhase() {
		return taskPhase;
	}

	public void setTaskPhase(String taskPhase) {
		this.taskPhase = taskPhase;
	}

	public String getHealthSituation() {
		return healthSituation;
	}

	public void setHealthSituation(String healthSituation) {
		this.healthSituation = healthSituation;
	}

	public EntityReference getMission() {
		return mission;
	}

	public void setMission(EntityReference mission) {
		this.mission = mission;
	}

}
