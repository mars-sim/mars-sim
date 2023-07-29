/*
 * Mars Simulation Project
 * ComputingTask.java
 * @date 2023-07-28
 * @author Manny Kung
 */
package org.mars_sim.msp.core.computing;

import java.io.Serializable;

public class ComputingTask implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private double computingPower;
	private int sol;
	private int duration;
	private int startTime;
	
	public ComputingTask(double computingPower, int sol, int startTime, int duration) {	
		this.computingPower = computingPower;
		this.sol = sol;
		this.startTime = startTime;	
		this.duration = duration;	
	}
	
	public double getComputingPower() {
		return computingPower;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public int getStartTime() {
		return startTime;
	}
	
	public int getSol() {
		return sol;
	}
}
