/*
 * Mars Simulation Project
 * Part.java
 * @date 2023-05-17
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

	/** Assume a standard 1-orbit of guarantee period between failures. */
	public static final int NUM_ORBITS = 1;
	/** The maximum possible mean time between failure rate on Mars [in sols]. */
	public static final double MAX_MTBF = 669D * NUM_ORBITS;
	/** The maximum possible reliability percentage. */
	public static final double MAX_RELIABILITY = 99.99;

	// The cumulative number of failures
	private int cumFailures = 0;
	// The current MTBF [in sols]
	private double mtbf = MAX_MTBF;
	// The failure rate
	private double failureRate = 1.0 / mtbf;
	// The current percent reliability
	private double percentReliability = MAX_RELIABILITY;

	
	/**
	 * Constructor.
	 *
	 * @param name        the name of the part.
	 * @param id          the id# of the part
	 * @param description {@link String}
	 * @param mass        the mass of the part (kg)
	 * @param startSol    the start sol when this part is put to use
	 */
	public Part(String name, int id, String description, GoodType type, double mass, int startSol) {
		// Use ItemResource constructor.
		super(name, id, description, type, mass, startSol);
		
		// Typical order:
		// 1. update numFailure
		// 2. update MTBF and failure rate
		// 3. update reliability
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
	 * Computes the percent reliability.
	 * 
	 * @param solsInUse
	 * @return 
	 */
	public double computeReliability(double solsInUse) {

		if (mtbf == 0) {
			return percentReliability;
		}
		else {
			percentReliability = .9 * percentReliability + .1 * (1 - Math.exp(-solsInUse / mtbf * 100));
		}
		
		return percentReliability;
	}

	/**
	 * Computes the MTBF.
	 * 
	 * @param solsInUse
	 * @param numFailed
	 * @return 
	 */
	public double computeMTBF(double solsInUse, int numFailed) {
		
		if (cumFailures == 0)
			mtbf = MAX_MTBF;
		else {
			mtbf = updateMTBF(solsInUse, numFailed);
		}
		
		return mtbf;
	}
	
	/**
	 * Computes the MTBF.
	 *
	 * @param solsInUse
	 * @param numFailed
	 * @return
	 */
	private double updateMTBF(double solsInUse, int numFailed) {
		// Obtain the total # of this part in used from all settlements
		int numItem = CollectionUtils.getTotalNumPart(getID());

		// Take the average between the field measured mtbf and factory/max mtbf 
		return Math.min(MAX_MTBF, (0.25 * Math.min(numItem / numFailed, 10) * solsInUse / cumFailures + 0.75 * MAX_MTBF));
	}

	/**
	 * Computes the failure rate.
	 * 
	 * @return
	 */
	public double computeFailureRate(double solsInUse) {
		double oneOverMTBF = 1.0 / mtbf;
		failureRate = oneOverMTBF * Math.exp(-oneOverMTBF * solsInUse);
		return failureRate;
	}

	/**
	 * Returns the failure rate.
	 * 
	 * @return
	 */
	public double getFailureRate() {
		return failureRate;
	}
	
	/**
	 * Returns the percent reliability.
	 * 
	 * @return
	 */
	public double getReliability() {
		return percentReliability;
	}

	/**
	 * Returns the MTBF.
	 * 
	 * @return
	 */
	public double getMTBF() {
		return mtbf;
	}
	
	/**
	 * Records the cumulative number of failures.
	 * Note: should call computeMTBF and then computeReliability after this.
	 * 
	 * @param num
	 */
	public void recordCumFailure(int num) {
		cumFailures += num;
	}

	/**
	 * Returns the cumulative number of failures.
	 * 
	 * @return
	 */
	public int getCumFailure() {
		return cumFailures;
	}

}
