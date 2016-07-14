package org.mars_sim.msp.restws.model;

public class SimulationDetails {

	private String uptime;
	private String marsTime;
	private boolean paused;
	
	public SimulationDetails(String uptime, String marsTime, boolean paused) {
		super();
		this.uptime = uptime;
		this.marsTime = marsTime;
		this.paused = paused;
	}
	
	public String getUptime() {
		return uptime;
	}
	
	public String getMarsTime() {
		return marsTime;
	}
	
	public boolean getPaused() {
		return paused;
	}
	
}
