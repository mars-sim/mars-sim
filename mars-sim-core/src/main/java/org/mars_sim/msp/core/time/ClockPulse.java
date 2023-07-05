/*
 * Mars Simulation Project
 * ClockPulse.java
 * @date 2022-06-26
 * @author Barry Evans
 */
package org.mars_sim.msp.core.time;

public class ClockPulse {
	/**
	 * The sols passed since last pulse.
	 */
	private double elapsed;
	private int lastIntMillisol;
	private MarsTime marsTime;
	private MasterClock master;
	private boolean isNewSol;
	private boolean isNewIntMillisol;
	private long id;
	
	/**
	 * Creates a pulse defining a step forward in the simulation.
	 * 
	 * @param id Unique pulse ID. Sequential.
	 * @param elapsed This must be a final & positive number.
	 * @param marsTime
	 * @param master
	 * @param newSol Has a new Mars day started with this pulse?
	 * @param newMSol Does this pulse start a new msol (an integer millisol) ?
	 */
	public ClockPulse(long id, double elapsed, 
					MarsTime marsTime, MasterClock master, 
					boolean newSol, boolean newMSol) {
		super();
		
		if ((elapsed <= 0) || !Double.isFinite(elapsed)) {
			throw new IllegalArgumentException("Elapsed time must be positive : " + elapsed);
		}
		
		this.id = id;
		this.elapsed = elapsed;
		this.marsTime = marsTime;
		this.master = master;
		this.isNewSol = newSol;
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

		int thisIntMillisol = marsTime.getMillisolInt();
		// Checks if this pulse starts a new integer millisol
		boolean isNewIntMillisol = lastIntMillisol != thisIntMillisol; 
		
		if (isNewIntMillisol) {
			lastIntMillisol = thisIntMillisol;
		}
		MarsTime newMars = marsTime.addTime(msolsSkipped);

		return new ClockPulse(id, actualElapsed, newMars, master, actualNewSol, isNewIntMillisol);
	}
}
