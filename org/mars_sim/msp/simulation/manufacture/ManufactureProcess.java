/**
 * Mars Simulation Project
 * ManufactureProcess.java
 * @version 2.83 2008-01-19
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.manufacture;

import java.io.Serializable;

/**
 * A manufacturing process.
 */
public class ManufactureProcess implements Serializable {

	// Data members.
	private ManufactureProcessInfo info;
	private double workTimeRemaining;
	private double processTimeRemaining;
	
	/**
	 * Constructor
	 * @param info information about the process.
	 */
	public ManufactureProcess(ManufactureProcessInfo info) {
		this.info = info;
		workTimeRemaining = info.getWorkTimeRequired();
		processTimeRemaining = info.getProcessTimeRequired();
	}
	
	/**
	 * Gets the information about the process.
	 * @return process information
	 */
	public ManufactureProcessInfo getInfo() {
		return info;
	}
	
	/**
	 * Gets the remaining work time.
	 * @return work time (millisols)
	 */
	public double getWorkTimeRemaining() {
		return workTimeRemaining;
	}
	
	/**
	 * Adds work time to the process.
	 * @param workTime work time (millisols)
	 */
	public void addWorkTime(double workTime) {
		workTimeRemaining -= workTime;
		if (workTimeRemaining < 0D) workTimeRemaining = 0D;
	}
	
	/**
	 * Gets the remaining process time.
	 * @return process time (millisols)
	 */
	public double getProcessTimeRemaining() {
		return processTimeRemaining;
	}
	
	/**
	 * Adds process time to the process.
	 * @param processTime process time (millisols)
	 */
	public void addProcessTime(double processTime) {
		processTimeRemaining -= processTime;
		if (processTimeRemaining < 0D) processTimeRemaining = 0D;
	}
}