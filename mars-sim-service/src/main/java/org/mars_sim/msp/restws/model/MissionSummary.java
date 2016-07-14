package org.mars_sim.msp.restws.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MissionSummary extends EntityReference{

	private int numPersons;
	private String phase;
	private String type;

	public int getNumPersons() {
		return numPersons;
	}
	public void setNumPersons(int numPeople) {
		this.numPersons = numPeople;
	}
	public String getPhase() {
		return phase;
	}
	public void setPhase(String phase) {
		this.phase = phase;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
