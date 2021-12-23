/*
 * Mars Simulation Project
 * ClockPulse.java
 * @date 2021-12-17
 * @author Barry Evans
 */
package org.mars_sim.msp.core.time;

public class ClockPulse {
	/**
	 * The sols passed since last pulse
	 */
	private double elapsed;
	
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
		this.elapsed = elapsed;
		this.marsTime = marsTime;
		this.earthTime = earthTime;
		this.master = master;
		this.newSol = newSol;
		this.newMSol = newMSol;
	}

	public long getId() {
		return id;
	}
	
	/**
	 * The elapsed real time since the last pulse.
	 * @return
	 */
	public double getElapsed() {
		return elapsed;
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

	/**
	 * Create a new pulse based on this one but add extra msol elapsed time.
	 * This does not change any of the original flags; only the elapses time.
	 * @param msolsSkipped 
	 * @return
	 */
	public ClockPulse addElapsed(double msolsSkipped) {
		double actualElapsed = msolsSkipped + elapsed;
		
		// This pulse cross a day or the total elapsed since the last pulse cross the sol boundary
		boolean actualNewSol = newSol || (actualElapsed > marsTime.getMillisol());
		
		return new ClockPulse(id, actualElapsed, marsTime, earthTime, master, actualNewSol, newMSol);
	}
}
