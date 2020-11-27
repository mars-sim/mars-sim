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

	/**
	 * Master clock
	 */
	private MasterClock master;
	
	/**
	 * Has this pulse crossed into a new Sol
	 */
	private boolean newSol;

	private EarthClock earthTime;

	public ClockPulse(double time, MarsClock marsTime, EarthClock earthTime, MasterClock master, boolean newSol) {
		super();
		this.time = time;
		this.marsTime = marsTime;
		this.earthTime = earthTime;
		this.master = master;
		this.newSol = newSol;
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
	
	public boolean isNewSol() {
		return newSol;
	}

	public EarthClock getEarthTime() {
		return earthTime;
	}
	
}
