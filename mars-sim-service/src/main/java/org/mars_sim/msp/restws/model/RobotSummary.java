package org.mars_sim.msp.restws.model;

/**
 * Summary info of a Robot
 * @author barry
 */

public class RobotSummary extends EntityReference {
	
	private String task;
	private String type;
	
	public String getTask() {
		return task;
	}
	public void setTask(String task) {
		this.task = task;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

}
