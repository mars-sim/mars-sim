/*
 * Mars Simulation Project
 * Part.java
 * @date 2021-11-16
 * @author Scott Davis
 */
package org.mars_sim.msp.core.resource;

import java.util.Set;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.goods.GoodType;

/**
 * The Part class represents a type of unit resource that is used for
 * maintenance and repairs.
 */
public class Part extends ItemResource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** The assumed # of years for calculating MTBF. */
	public static final int NUM_YEARS = 3;
	/** The maximum possible mean time between failure rate on Mars (Note that Mars has 669 sols in a year). */
	public static final double MAX_MTBF = 669D * NUM_YEARS;
	/** The maximum possible reliability percentage. */
	public static final double MAX_RELIABILITY = 99.999;

	// Number of failures
	private int numFailures = 0;

	private double mtbf = MAX_MTBF;

	private double percentReliability = MAX_RELIABILITY;

	/**
	 * Constructor.
	 *
	 * @param name        the name of the part.
	 * @param id          the id# of the part
	 * @param description {@link String}
	 * @param mass        the mass of the part (kg)
	 * @param the         sol when this part is put to use
	 */
	public Part(String name, int id, String description, GoodType type, double mass, int solsUsed) {
		// Use ItemResource constructor.
		super(name, id, description, type, mass, solsUsed);
	}

	/**
	 * Gets a set of all parts.
	 *
	 * @return set of parts.
	 */
	public static Set<Part> getParts() {
		return ItemResourceUtil.getItemResources();
	}

	/**
	 * Compute the reliability
	 */
	public void computeReliability(int missionSol) {

		int numSols = missionSol - getStartSol();

		if (numFailures == 0)
			mtbf = MAX_MTBF;
		else {
			numSols = Math.max(1, numSols);
			mtbf = computeMTBF(numSols);
//			System.out.println(getName() + "'s MTBF: " + mtbf);
		}

		if (mtbf == 0) {
			percentReliability = MAX_RELIABILITY;
		}
		else {
			percentReliability = Math.min(MAX_RELIABILITY, Math.exp(-numSols / mtbf) * 100);
		}
	}

	/**
	 * Computes the MTBF
	 *
	 * @param numSols
	 * @return
	 */
	private double computeMTBF(double numSols) {
		// Obtain the total # of this part in used from all settlements
		int numItem = CollectionUtils.getTotalNumPart(getID());

		// Take the average between the factory mtbf and the field measured mtbf
		return (numItem * numSols / numFailures + MAX_MTBF) / 2D;
	}

	public double getReliability() {
		return percentReliability;
	}

	public double getMTBF() {
		return mtbf;
	}

	public void setFailure(int num, int missionSol) {
		numFailures += num;
		// Recompute the reliability for this part
		computeReliability(missionSol);
	}

	public int getFailure() {
		return numFailures;
	}

}
