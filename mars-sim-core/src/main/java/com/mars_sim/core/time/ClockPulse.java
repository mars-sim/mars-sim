/*
 * Mars Simulation Project
 * ClockPulse.java
 * @date 2024-08-15
 * @author Barry Evans
 */
package com.mars_sim.core.time;

public class ClockPulse {

	// is it a new sol ?
	private boolean isNewSol = false;
	// is it a new half sol ?
	private boolean isNewHalfSol = false;
	// is it a new half msol ?	
	private boolean isNewHalfMillisol = false;
	// is it a new integer msol ?
	private boolean isNewIntMillisol = false;

	private long id;

	/** The sols passed since last pulse. */
	private double elapsed;

	private MarsTime marsTime;
	private MasterClock master;

	/**
	 * Creates a pulse defining a step forward in the simulation.
	 * 
	 * @param id           Unique pulse ID. Sequential.
	 * @param elapsed      This must be a final & positive number.
	 * @param marsTime
	 * @param master
	 * @param newSol       Has the pulse just crossed into the new sol ?
	 * @param isNewHalfSol Has the pulse just crossed the half sol ?
	 * @param newMSol      Does this pulse start a new msol (an integer millisol) ?
	 */
	public ClockPulse(long id, double elapsed, MarsTime marsTime, MasterClock master, boolean newSol,
			boolean newHalfSol, boolean newIntMillisol, boolean newHalfMillisol) {
		super();

		if ((elapsed <= 0) || !Double.isFinite(elapsed)) {
			throw new IllegalArgumentException("Elapsed time must be positive : " + elapsed);
		}

		this.id = id;
		this.elapsed = elapsed;
		this.marsTime = marsTime;
		this.master = master;
		this.isNewSol = newSol;
		this.isNewHalfSol = newHalfSol;
		this.isNewIntMillisol = newIntMillisol;
		this.isNewHalfMillisol = newHalfMillisol;
	}

	public long getId() {
		return id;
	}

	/**
	 * Gets the elapsed real time since the last pulse.
	 * 
	 * @return
	 */
	public double getElapsed() {
		return elapsed;
	}

	/**
	 * Gets MarsClock instance.
	 * 
	 * @return
	 */
	public MarsTime getMarsTime() {
		return marsTime;
	}

	/**
	 * Gets MasterClock instance.
	 * 
	 * @return
	 */
	public MasterClock getMasterClock() {
		return master;
	}
	
	/**
	 * Is this a new sol ?
	 * 
	 * @return
	 */
	public boolean isNewSol() {
		return isNewSol;
	}

	/**
	 * Is this a new half sol ?
	 * 
	 * @return
	 */
	public boolean isNewHalfSol() {
		return isNewHalfSol;
	}

	/**
	 * Is this a new half integer millisol ?
	 * 
	 * @return
	 */
	public boolean isNewHalfMillisol() {
		return isNewHalfMillisol;
	}

	/**
	 * Is this a new integer millisol ?
	 * 
	 * @return
	 */
	public boolean isNewIntMillisol() {
		return isNewIntMillisol;
	}
	
	/**
	 * Creates a new pulse based on this one but add extra elapsed time. Note: This
	 * does not change any of the original flags; only the elapses time.
	 * 
	 * @param msolsSkipped
	 * @return
	 */
	public ClockPulse addElapsed(double msolsSkipped) {
		// NOTE: See MasterClock's fireClockPulse() for deriving a set of new params
		
		// Get the actual elapsed millisols
		double actualElapsed = msolsSkipped + elapsed;
		// Add the skipped millisols
		this.marsTime = marsTime.addTime(msolsSkipped);

		return new ClockPulse(id, actualElapsed, marsTime, master, isNewSol, isNewHalfSol, isNewIntMillisol,
				isNewHalfMillisol);
	}

	public void destroy() {
		marsTime = null;
		master = null;
	}
}
