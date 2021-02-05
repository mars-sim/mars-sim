package org.mars_sim.msp.core.resource;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;

/**
 * A class for holding maintenance scope for a Part
 */
public class MaintenanceScope implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Domain members
	private String scope;
	private Part part;
	private int probability;
	private int maxNumber;

	/**
	 * Constructor
	 * 
	 * @param part		  Part of the maintenance
	 * @param scope        scope of the maintenance.
	 * @param probability the probability of this part being needed for maintenance.
	 * @param maxNumber   the maximum number of this part needed for maintenance.
	 */
	public MaintenanceScope(Part part, String scope, int probability, int maxNumber) {
		if (scope == null) {
			throw new IllegalArgumentException(Msg.getString("Part.error.nameIsNull")); //$NON-NLS-1$
		}
		this.part = part;
		this.scope = scope;
		this.probability = probability;
		this.maxNumber = maxNumber;
	}

	public Part getPart() {
		return part;
	}
	
	public int getProbability() {
		return probability;
	}

	public int getMaxNumber() {
		return maxNumber;
	}

	public String getName() {
		return scope;
	}
}