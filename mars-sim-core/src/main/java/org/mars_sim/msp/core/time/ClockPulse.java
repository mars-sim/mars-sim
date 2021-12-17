/*
 * Mars Simulation Project
 * ClockPulse.java
 * @date 2021-12-17
 * @author Barry Evans
 */
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

	/**
	 * Has this pulse crossed into a new millisol integer
	 */
	private boolean newMSol;
	
	/**
	 * Time back on Earth
	 */
	private EarthClock earthTime;

	/**
	 * Pulse id
	 */
	private long id;
	
	/**
	 * Create a pulse defining a step forward in the simulation.
	 * @param sim Context of the simulation being advanced
	 * @param id Unique pulse ID. Sequential.
	 * @param elapsed This must be a final & positive number.
	 * @param marsTime
	 * @param earthTime
	 * @param master
	 * @param newSol Has a new Mars day started with this pulse?
	 */
	ClockPulse(long id, double elapsed, MarsClock marsTime, EarthClock earthTime, MasterClock master, 
			boolean newSol, boolean newMSol) {
		super();
		
		if ((elapsed <= 0) || !Double.isFinite(elapsed)) {
			throw new IllegalArgumentException("Elapsed time must be positive : " + elapsed);
		}
		
		this.id = id;
		this.time = elapsed;
		this.marsTime = marsTime;
		this.earthTime = earthTime;
		this.master = master;
		this.newSol = newSol;
		this.newMSol = newMSol;
	}

	public long getId() {
		return id;
	}
	
	public double getElapsed() {
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
	
	public boolean isNewMSol() {
		return newMSol;
	}
	
	public EarthClock getEarthTime() {
		return earthTime;
	}
}
