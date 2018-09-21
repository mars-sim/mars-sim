package org.mars_sim.msp.restws.model;

public class RobotDetails extends RobotSummary {
	private String personalityType;
	private String taskPhase;
	
	public String getPersonalityType() {
		return personalityType;
	}
	public void setPersonalityType(String personalityType) {
		this.personalityType = personalityType;
	}
	public String getTaskPhase() {
		return taskPhase;
	}
	public void setTaskPhase(String taskPhase) {
		this.taskPhase = taskPhase;
	}
}
