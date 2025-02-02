/*
 * Mars Simulation Project
 * RepairPart.java
 * @date 2021-11-16
 * @author Barry Evans
 */

package com.mars_sim.core.malfunction;

import java.io.Serializable;

/**
 * Private inner class for repair part information.
 */
public class RepairPart implements Serializable {

	private static final long serialVersionUID = 1L;

	// Data members
	private String name;
	private int partID;
	private int number;
	private double repairProbability;

	/**
	 * Constructor.
	 *
	 * @param name        the name of the part.
	 * @param partID      the ID of the part.
	 * @param number      the maximum number of parts.
	 * @param repairProbability the probability of the part being needed.
	 */
	RepairPart(String name, int partID, int number, double repairProbability) {
		this.partID = partID;
		this.name = name;
		this.number = number;
		this.repairProbability = repairProbability;
	}

	public String getName() {
		return name;
	}

	public int getPartID() {
		return partID;
	}

	public int getNumber() {
		return number;
	}

	public double getRepairProbability() {
		return repairProbability;
	}
	
	protected void setRepairProbability(double value) {
		repairProbability = value;
	}
}
