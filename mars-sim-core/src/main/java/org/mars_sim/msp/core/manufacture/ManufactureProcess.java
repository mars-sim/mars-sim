/**
 * Mars Simulation Project
 * ManufactureProcess.java
 * @version 3.1.0 2019-02-19
 * @author Scott Davis
 */

package org.mars_sim.msp.core.manufacture;

import java.io.Serializable;

import org.mars_sim.msp.core.structure.building.function.Manufacture;

/**
 * A manufacturing process.
 */
public class ManufactureProcess implements Serializable {

	private static final long serialVersionUID = 1L;

	// Data members.
	private double totalWorkTimeReq;
	private double workTimeRemaining;
	private double processTimeRemaining;
	
	private Manufacture workshop;
	private ManufactureProcessInfo info;
	


	/**
	 * Constructor
	 * 
	 * @param info     information about the process.
	 * @param workshop the manufacturing workshop where the process is taking place.
	 */
	public ManufactureProcess(ManufactureProcessInfo info, Manufacture workshop) {
		this.info = info;
		this.workshop = workshop;
		workTimeRemaining = info.getWorkTimeRequired();
		processTimeRemaining = info.getProcessTimeRequired();
		
		totalWorkTimeReq = workTimeRemaining;
	}

	/**
	 * Gets the information about the process.
	 * 
	 * @return process information
	 */
	public ManufactureProcessInfo getInfo() {
		return info;
	}

	/**
	 * Gets the remaining work time.
	 * 
	 * @return work time (millisols)
	 */
	public double getWorkTimeRemaining() {
		return workTimeRemaining;
	}

	/**
	 * Adds work time to the process.
	 * 
	 * @param workTime work time (millisols)
	 */
	public void addWorkTime(double workTime) {
		workTimeRemaining -= workTime;
		if (workTimeRemaining < 0D)
			workTimeRemaining = 0D;
	}

	/**
	 * Gets the remaining process time.
	 * 
	 * @return process time (millisols)
	 */
	public double getProcessTimeRemaining() {
		return processTimeRemaining;
	}

	/**
	 * Adds process time to the process.
	 * 
	 * @param processTime process time (millisols)
	 */
	public void addProcessTime(double processTime) {
		processTimeRemaining -= processTime;
		if (processTimeRemaining < 0D)
			processTimeRemaining = 0D;
	}
	
	@Override
	public String toString() {
		return info.getName();
	}

	/**
	 * Gets the manufacture building function.
	 * 
	 * @return manufacture building function.
	 */
	public Manufacture getWorkshop() {
		return workshop;
	}

	/**
	 * Compares this object with the specified object for order.
	 * 
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	public int compareTo(ManufactureProcess p) {
		return info.getName().compareToIgnoreCase(p.info.getName());
	}

	/**
	 * Gets the total work time required
	 * 
	 * @return
	 */
	public double getTotalWorkTime() {
		return totalWorkTimeReq;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		workshop = null;
		info.destroy();
		info = null;
	}

}