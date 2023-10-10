/*
 * Mars Simulation Project
 * ClockPulse.java
 * @date 2022-06-26
 * @author Barry Evans
 */
package org.mars_sim.msp.core.time;

public class ClockPulse {
	
	private boolean isNewSol;
	private boolean isNewHalfSol;
	private boolean isNewIntMillisol;

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
		this.isNewSol = newSol;
		this.isNewHalfSol = newHalfSol;
		this.isNewIntMillisol = newMSol;
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
	 * Is this a new integer millisol ?
	 * 
	 * @return
	 */
	public boolean isNewMSol() {
		return isNewIntMillisol;
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
		// This pulse cross a day or the total elapsed since the last pulse cross the sol boundary
		boolean actualNewSol = isNewSol || (actualElapsed > marsTime.getMillisol());

		// Identify if it's a new Sol
		int currentSol = marsTime.getMissionSol();
		boolean isNewSol = ((lastSol >= 0) && (lastSol != currentSol));

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

		return new ClockPulse(id, actualElapsed, newMars, master, actualNewSol, isNewHalfSol, isNewIntMillisol);
	}
}
