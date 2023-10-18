/*
 * Mars Simulation Project
 * ClockPulse.java
 * @date 2022-06-26
 * @author Barry Evans
 */
package com.mars_sim.core.time;

public class ClockPulse {
	
	private boolean newSol;
	private boolean newHalfSol;
	private boolean newIntMillisol;

	private int lastIntMillisol;
	private int lastSol;

	private long id;
	
	/** The sols passed since last pulse. */
	private double elapsed;
	
	private MarsTime marsTime;
	private MasterClock master;

	
	/**
	 * Creates a pulse defining a step forward in the simulation.
	 * 
	 * @param id 			Unique pulse ID. Sequential.
	 * @param elapsed 		This must be a final & positive number.
	 * @param marsTime
	 * @param master
	 * @param newSol 		Has the pulse just crossed into the new sol ? 
	 * @param isNewHalfSol 	Has the pulse just crossed the half sol ?
	 * @param newMSol 		Does this pulse start a new msol (an integer millisol) ?
	 */
	public ClockPulse(long id, double elapsed, 
					MarsTime marsTime, MasterClock master, 
					boolean newSol, boolean newHalfSol, boolean newMSol) {
		super();
		
		if ((elapsed <= 0) || !Double.isFinite(elapsed)) {
			throw new IllegalArgumentException("Elapsed time must be positive : " + elapsed);
		}
		
		this.id = id;
		this.elapsed = elapsed;
		this.marsTime = marsTime;
		this.master = master;
		this.newSol = newSol;
		this.newHalfSol = newHalfSol;
		this.newIntMillisol = newMSol;
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
		return newSol;
	}
	
	/**
	 * Is this a new half sol ?
	 * 
	 * @return
	 */
	public boolean isNewHalfSol() {
		return newHalfSol;
	}
	
	/**
	 * Is this a new integer millisol ?
	 * 
	 * @return
	 */
	public boolean isNewMSol() {
		return newIntMillisol;
	}

	/**
	 * Creates a new pulse based on this one but add extra elapsed time.
	 * Note: This does not change any of the original flags; only the elapses time.
	 * 
	 * @param msolsSkipped 
	 * @return
	 */
	public ClockPulse addElapsed(double msolsSkipped) {
		double actualElapsed = msolsSkipped + elapsed;

		// Identify if it's a new Sol
		int currentSol = marsTime.getMissionSol();
		// This pulse cross a day or the total elapsed since the last pulse cross the sol boundary
		boolean isNewSol = newSol || (actualElapsed > marsTime.getMillisol())
				|| (lastSol >= 0 && (lastSol != currentSol));

		// Identify if it's half a sol
		boolean isNewHalfSol = isNewSol || (lastSol <= 500 && currentSol > 500);	
		
		// Update the lastSol
		lastSol = currentSol;	
		
		int thisIntMillisol = marsTime.getMillisolInt();
		// Checks if this pulse starts a new integer millisol
		boolean isNewIntMillisol = lastIntMillisol != thisIntMillisol; 
		
		if (isNewIntMillisol) {
			lastIntMillisol = thisIntMillisol;
		}
		MarsTime newMars = marsTime.addTime(msolsSkipped);

		return new ClockPulse(id, actualElapsed, newMars, master, isNewSol, isNewHalfSol, isNewIntMillisol);
	}
}
