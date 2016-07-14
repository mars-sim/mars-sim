package org.mars_sim.msp.restws.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * This is a DTO for the Mission entity. However it provides a superset of all properties of the different Mission subtypes.
 * Therefore instances of this DTO will always be sparsely populated because not all properties are available in all Mission subclasses.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MissionDetails extends MissionSummary {
	private String phaseDescription;
	private EntityReference associatedSettlement;
	
	// Used in Trade mission
	private EntityReference destinationSettlement;
	private double profit;
	
	// Used in the Missions doing research
	private ScientificStudyDTO scientificStudy;
	
	// Used in Rescue Salvage vehicle
	private EntityReference destinationVehicle;
	
	// Available from Vehicle Mission
	private EntityReference vehicle;
	private double totalDistanceTravelled;
	private double totalRemainingDistance;
	private String legETA;
	
	public String getPhaseDescription() {
		return phaseDescription;
	}
	public void setPhaseDescription(String phaseDescription) {
		this.phaseDescription = phaseDescription;
	}
	public EntityReference getAssociatedSettlement() {
		return associatedSettlement;
	}
	public void setAssociatedSettlement(EntityReference associatedSettlement) {
		this.associatedSettlement = associatedSettlement;
	}
	public EntityReference getVehicle() {
		return vehicle;
	}
	public void setVehicle(EntityReference vehicle) {
		this.vehicle = vehicle;
	}
	public String getLegETA() {
		return legETA;
	}
	public void setLegETA(String legETA) {
		this.legETA = legETA;
	}
	public double getTotalDistanceTravelled() {
		return totalDistanceTravelled;
	}
	public void setTotalDistanceTravelled(double totalDistanceTravelled) {
		this.totalDistanceTravelled = totalDistanceTravelled;
	}
	public double getTotalRemainingDistance() {
		return totalRemainingDistance;
	}
	public void setTotalRemainingDistance(double totalRemainingDistance) {
		this.totalRemainingDistance = totalRemainingDistance;
	}
	public EntityReference getDestinationSettlement() {
		return destinationSettlement;
	}
	public void setDestinationSettlement(EntityReference destinationSettlement) {
		this.destinationSettlement = destinationSettlement;
	}
	public double isProfit() {
		return profit;
	}
	public void setProfit(double profit) {
		this.profit = profit;
	}
	public EntityReference getDestinationVehicle() {
		return destinationVehicle;
	}
	public void setDestinationVehicle(EntityReference destinationVehicle) {
		this.destinationVehicle = destinationVehicle;
	}
	public ScientificStudyDTO getScientificStudy() {
		return scientificStudy;
	}
	public void setScientificStudy(ScientificStudyDTO scientificStudy) {
		this.scientificStudy = scientificStudy;
	}
}
