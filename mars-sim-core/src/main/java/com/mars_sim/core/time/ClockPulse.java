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
	/** The last sol on the last fireEvent. Need to set to -1. */
	private int lastSol = -1;
	/** The last millisol integer on the last fireEvent. Need to set to -1. */
	private int lastIntMillisol = -1;
	/** The last millisol from the last pulse. */
	private double lastMillisol;
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

		return fireClockPulse(actualElapsed);
	}
	
	/**
	 * Fires a clock pulse.
	 * 
	 * @param actualElapsed
	 * @return
	 */
	private ClockPulse fireClockPulse(double actualElapsed) {
		////////////////////////////////////////////////////////////////////////////////////		
		// NOTE: Any changes made below may need to be brought to MasterClock's fireClockPulse()'s Part 0 to Part 3
		////////////////////////////////////////////////////////////////////////////////////
		
		////////////////////////////////////////////////////////////////////////////////////
		// Part 0: Retrieve values
		////////////////////////////////////////////////////////////////////////////////////
		
		// Get the current millisol integer
		int currentIntMillisol = marsTime.getMillisolInt();
		// Get the current millisol
		double currentMillisol = marsTime.getMillisol();
		// Get the current sol
		int currentSol = marsTime.getMissionSol();
		
		////////////////////////////////////////////////////////////////////////////////////
		// Part 1: Update isNewSol and isNewHalfSol
		////////////////////////////////////////////////////////////////////////////////////

		// Identify if this pulse crosses a sol
		boolean isNewSol = (lastSol != currentSol);
		boolean isNewHalfSol = false;
		
		// Updates lastSol
		if (isNewSol) {
			this.lastSol = currentSol;
			isNewHalfSol = true;
		}
		else {
			// Identify if it just passes half a sol
			isNewHalfSol = lastMillisol < 500 && currentMillisol >= 500;
		}

		////////////////////////////////////////////////////////////////////////////////////
		// Part 2: Update isNewIntMillisol and isNewHalfMillisol
		////////////////////////////////////////////////////////////////////////////////////

		// Checks if this pulse starts a new integer millisol
		boolean isNewIntMillisol = (lastIntMillisol != currentIntMillisol);
		boolean isNewHalfMillisol = false;
		
		// Updates lastSol
		if (isNewIntMillisol) {
			this.lastIntMillisol = currentIntMillisol;
			isNewHalfMillisol = true;
		}
		else {
			// Find the decimal part of the past millisol and current millisol
			int intPartLast = (int)lastMillisol;
			double decimalPartLast = lastMillisol - intPartLast;
			int intPartCurrent = (int)currentMillisol;
			double decimalPartCurrent = currentMillisol - intPartCurrent;
			
			// Identify if it just passes half a millisol
			isNewHalfMillisol = decimalPartLast < .5 && decimalPartCurrent >= .5;
		}
	
		////////////////////////////////////////////////////////////////////////////////////
		// Part 3: Update lastMillisol
		////////////////////////////////////////////////////////////////////////////////////

		// Update the lastMillisol
		this.lastMillisol = currentMillisol;
		
		////////////////////////////////////////////////////////////////////////////////////
		// Part 4: Update the boolean values
		////////////////////////////////////////////////////////////////////////////////////
		
		this.isNewSol = isNewSol;
		this.isNewHalfSol = isNewHalfSol;
		this.isNewIntMillisol = isNewIntMillisol;
		this.isNewHalfMillisol = isNewHalfMillisol;
		
		////////////////////////////////////////////////////////////////////////////////////
		// Part 5: Create a clock pulse
		////////////////////////////////////////////////////////////////////////////////////
		
		return new ClockPulse(id, actualElapsed, marsTime, master, 
				isNewSol, isNewHalfSol, isNewIntMillisol, isNewHalfMillisol);
	}

	public void destroy() {
		marsTime = null;
		master = null;
	}
}
