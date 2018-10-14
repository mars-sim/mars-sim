/**
 * Mars Simulation Project
 * Complaint.java
 * @version 3.1.0 2017-03-09
 * @author Barry Evans
 */

package org.mars_sim.msp.core.person.health;

import java.io.Serializable;

/**
 * This class represents the definition of a specific Medical Complaint that can
 * effect a Person. The Complaint once effecting a Person can either result in
 * the Person entering a recovery period or developing a more serious complaint
 * or possibly death.
 */
public class Complaint implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * The maximum probability rating. This allows the complaint to be specified to
	 * 1/10th of a percentage.
	 */
	public final static double MAXPROBABILITY = 10000D;

	private int seriousness; // Seriousness of this illness
	private double degradePeriod; // Time before complaint degrades
	private double recoveryPeriod; // Time before Person recovers
	private double probability; // Probability of occurring
	private double performanceFactor; // Factor effecting Person performance
	private boolean bedRestRecovery; // Does complaint require bed rest during recovery?

	/** The complaint type of this illness. */
	private ComplaintType type;
	/** Treatment needed for recoveryNext phase of this illness. */
	private Treatment recoveryTreatment;
	/** Next phase of this illness. */
	private Complaint nextPhase;
	/** Temporary next phase . */
	private ComplaintType nextPhaseType;

	/**
	 * Constructor 1 : Create a Medical Complaint instance.
	 *
	 * @param type ComplaintYype.
	 */
	public Complaint(ComplaintType type) {
		this.type = type;
	}

	/**
	 * Constructor 2 : Create an environmental Medical Complaint instance
	 *
	 * @param type              ComplaintYype.
	 * @param seriousness       How serious is this complaint.
	 * @param degrade           The time it takes before this complaint advances, if
	 *                          this value is zero, then the Person can shelf heel
	 *                          themselves. This value is in sols.
	 * @param recovery          The time is takes for a Person to recover. If this
	 *                          value is zero it means the complaint results in
	 *                          death unless treated. This value is in sols.
	 * @param probability       The probability of this illness occurring, this can
	 *                          be between 0 and MAXPROBABILITY.
	 * @param performance       The percentage that a Persons performance is
	 *                          decreased.
	 * @param bedRestRecovery   True if bed rest is required during recovery.
	 * @param recoveryTreatment Any treatment that is needed for recovery.
	 * @param next              The complaint that this degrades into unless
	 *                          checked.
	 */
	Complaint(ComplaintType type, int seriousness, double degrade, double recovery, double probability,
			double performance, boolean bedRestRecovery, Treatment recoveryTreatment, Complaint next) {
		// this.name = name;
		this.type = type;
		this.seriousness = seriousness;
		this.degradePeriod = degrade;
		this.recoveryPeriod = recovery;
		this.performanceFactor = (performance / 100D);
		this.bedRestRecovery = bedRestRecovery;
		this.nextPhase = next;
		this.nextPhaseType = null;// "";
		if (next != null)
			this.nextPhaseType = next.type;// name;
		this.probability = probability;
		this.recoveryTreatment = recoveryTreatment;
	}

	/**
	 * Constructor 3 : create a Medical Complaint instance from medical.xml
	 * 
	 * @param type              ComplaintYype
	 * @param seriousness       seriousness of complaint
	 * @param degrade           degrade time until next complaint
	 * @param recovery          recovery time
	 * @param probability       probability of complaint
	 * @param recoveryTreatment treatment for recovery
	 * @param nextStr           next complaint name
	 * @param performance       performance factor
	 * @param bedRestRecovery   True if bed rest is required during recovery.
	 */
	Complaint(ComplaintType type, int seriousness, double degrade, double recovery, double probability,
			Treatment recoveryTreatment, ComplaintType nextStr, double performance, boolean bedRestRecovery) {
		this.type = type;
		this.seriousness = seriousness;
		this.degradePeriod = degrade;
		this.recoveryPeriod = recovery;
		this.performanceFactor = (performance / 100D);
		this.probability = probability;
		this.bedRestRecovery = bedRestRecovery;
		this.recoveryTreatment = recoveryTreatment;
		this.nextPhaseType = nextStr;
	}

	/**
	 * Sets the next complaint this complaint degrades to.
	 * 
	 * @param nextComplaint the next complaint
	 */
	void setNextComplaint(Complaint nextComplaint) {
		this.nextPhase = nextComplaint;
	}

	/**
	 * Get the degrade period.
	 * 
	 * @return Double value representing a duration.
	 */
	public double getDegradePeriod() {
		return degradePeriod;
	}

//	/**
//	 * Get the name of complaint.
//	 * 
//	 * @return Complaint name.
//	 */
	// public String getName() {
	// return name;
	// }

	/**
	 * Get the type of complaint.
	 * 
	 * @return Complaint type.
	 */
	public ComplaintType getType() {
		return type;
	}

	/**
	 * Get the next complaint that this complaint developers into.
	 * 
	 * @return The next complaint, if null then death results.
	 */
	public Complaint getNextPhase() {
		return nextPhase;
	}

	/**
	 * Gets the next complaintType.
	 * 
	 * @return complaint type
	 */
	ComplaintType getNextPhaseStr() {
		return nextPhaseType;
	}

	/**
	 * Get the performance factor that effect Person with the complaint.
	 * 
	 * @return The value is between 0 -> 1.
	 */
	public double getPerformanceFactor() {
		return performanceFactor;
	}

	/**
	 * Get the probability of this complaint.
	 * 
	 * @return Probability from 0 to 100.
	 */
	public double getProbability() {
		return probability;
	}

	/**
	 * Get the treatment required for recovery to start.
	 * 
	 * @return recovery treatment.
	 */
	public Treatment getRecoveryTreatment() {
		return recoveryTreatment;
	}

	/**
	 * Get the recover period.
	 * 
	 * @return Double value representing a duration.
	 */
	public double getRecoveryPeriod() {
		return recoveryPeriod;
	}

	/**
	 * Checks if recovery requires bed rest.
	 * 
	 * @return true if bed rest required.
	 */
	public boolean requiresBedRestRecovery() {
		return bedRestRecovery;
	}

	/**
	 * Get the seriousness of this complaint.
	 * 
	 * @return Seriousness rating.
	 */
	public int getSeriousness() {
		return seriousness;
	}

	/**
	 * Get a string representation.
	 * 
	 * @return The String name of the ComplaintType.
	 */
	public String toString() {
		return type.getName();
	}

	public void destroy() {
		type = null;
		recoveryTreatment = null;
		nextPhase = null;
		nextPhaseType = null;
	}

}