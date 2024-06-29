/*
 * Mars Simulation Project
 * Complaint.java
 * @date 2022-07-29
 * @author Barry Evans
 */

package com.mars_sim.core.person.health;

import java.io.Serializable;

import com.mars_sim.core.data.Range;
import com.mars_sim.core.person.ai.task.util.ExperienceImpact.PhysicalEffort;

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
	public static final double MAXPROBABILITY = 10000D;

	private int seriousness; // Seriousness of this illness
	private double degradePeriod; // Time before complaint degrades
	private Range recoveryPeriod; // Time before Person recovers
	private double probability; // Probability of occurring
	private double performanceFactor; // Factor effecting Person performance
	private boolean bedRestRecovery; // Does complaint require bed rest during recovery?

	/** The complaint type of this illness. */
	private ComplaintType type;
	/** Treatment needed for recoveryNext phase of this illness. */
	private Treatment recoveryTreatment;
	/** Next phase of this illness. */
	private Complaint nextPhase;

	private boolean environmental;

	private PhysicalEffort effortInfluence;

	/**
	 * Constructor 3 : create a Medical Complaint instance from medical.xml
	 * 
	 * @param type              ComplaintYype
	 * @param seriousness       seriousness of complaint
	 * @param degrade           degrade time until next complaint
	 * @param recovery          recovery time
	 * @param probability       probability of complaint
	 * @param recoveryTreatment treatment for recovery
	 * @param degradeComplaint  next complaint
	 * @param performance       performance factor
	 * @param bedRestRecovery   True if bed rest is required during recovery.
	 * @param environmental     Is this trigger by an environmental change
	 */
	Complaint(ComplaintType type, int seriousness, double degrade, Range recovery, double probability,
			Treatment recoveryTreatment, Complaint degradeComplaint, double performance, boolean bedRestRecovery,
				boolean environmental, PhysicalEffort effort) {
		this.type = type;
		this.seriousness = seriousness;
		this.degradePeriod = degrade;
		this.recoveryPeriod = recovery;
		this.performanceFactor = (performance / 100D);
		this.probability = probability;
		this.bedRestRecovery = bedRestRecovery;
		this.recoveryTreatment = recoveryTreatment;
		this.nextPhase = degradeComplaint;
		this.environmental = environmental;
		this.effortInfluence = effort;
	}

	/**
	 * Get the degrade period.
	 * 
	 * @return Double value representing a duration.
	 */
	public double getDegradePeriod() {
		return degradePeriod;
	}

	/**
	 * Get the name of this Complaint
	 * @return
	 */
	public String getName() {
		return type.getName();
	}
	
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
	 * Get the a specific recovery period; this is a random value within the range
	 * 
	 * @return Double value representing a duration.
	 */
	public double getRecoveryPeriod() {
		return recoveryPeriod.getRandomValue() * 1000D;
	}

	/**
	 * Get the range of the recovery period
	 * @return
	 */
	public Range getRecoveryRange() {
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
	 * Is this complaint triggered by environmental changes
	 * @return
	 */
	public boolean isEnvironmental() {
		return environmental;
	}

	/**
	 * Get a string representation.
	 * 
	 * @return The String name of the ComplaintType.
	 */
	public String toString() {
		return type.getName();
	}

	/**
	 * Is this Complaint influenced by a level of effort?
	 * @return
	 */
    public PhysicalEffort getEffortInfluence() {
		return effortInfluence;
    }
}
