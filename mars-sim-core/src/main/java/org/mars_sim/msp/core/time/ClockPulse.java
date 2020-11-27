package org.mars_sim.msp.core.time;

public class ClockPulse {
	/**
	 * The real time passed since last pulse
	 */
	private double time;
	
	/**
	 * Updated Mars time for this simulation
	 */
	private MarsClock marsTime;

	private MasterClock master;
	

	public ClockPulse(double time, MarsClock marsTime, MasterClock master) {
		super();
		this.time = time;
		this.marsTime = marsTime;
		this.master = master;
	}

	public double getTime() {
		return time;
	}

	public MarsClock getMarsTime() {
		return marsTime;
	}

	public MasterClock getMasterClock() {
		return master;
	}
	
	
	
}
