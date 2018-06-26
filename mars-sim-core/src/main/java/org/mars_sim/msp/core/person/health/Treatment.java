/**
 * Mars Simulation Project
 * Treatment.java
 * @version 3.1.0 2017-09-20
 * @author Barry Evans
 */
package org.mars_sim.msp.core.person.health;

import java.io.Serializable;

/**
 * This class represents a Medical treatment that can be applied to
 * a Person to cure a complaint.
 */
public class Treatment
implements Serializable, Comparable<Treatment> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private String name;
	/** Optimal MEDICAL skill. */
	private int requiredSkill;
	/** Required MedicalAid level. */
	private int facilityLevel;
	/** Length of treatment. */
	private double duration;
	/** Can perform the treatment on oneself or not. */
	private boolean selfAdmin;

	/**
	 * Constructor.
	 * @param name The unique name.
	 * @param skill Required Medical skill.
	 * @param duration The duration of treatment in millisols.
	 * @param selfAdmin Can the treatment be self-administered.
	 * @param facilityLevel Required medical aid tech level.
	 */
	public Treatment(String name, int skill, double duration,
			boolean selfAdmin, int facilityLevel) {
		this.name = name;
		this.requiredSkill = skill;
		this.selfAdmin = selfAdmin;
		this.facilityLevel = facilityLevel;
		if (duration < 0D) {
			// Negative duration means, the treatment takes as long as recovery
			duration = -1D;
		}
		else {
			this.duration = duration;
		}
	}

	/**
	 * Compare this object with another.
	 */
	@Override
	public int compareTo(Treatment otherTreatment) {
		return name.compareTo((otherTreatment).name);
	}

	/**
	 * Compare this object with another object.
	 * @param other Object to compare.
	 * @return DO they match or not.
	 */
	@Override
	public boolean equals(Object other) {
		boolean match = false;
		if (other instanceof Treatment) {
			match = name.equals(((Treatment)other).name);
		}
		return match;
	}

	/**
	 * Get the time required to perform this treatment by a Person with
	 * the appropriate skill rating.
	 * @param skill The skill rating that will apply the treatment.
	 * @return Adjusted treatment time according to skill.
	 */
	public double getAdjustedDuration(int skill) {
		double result = duration;
		if ((result > 0D) && (skill < requiredSkill)) {
			// Increase the time by the percentage skill lacking
			result = duration * (1 + ((requiredSkill - skill)/requiredSkill));
		}
		return result;
	}

	/**
	 * Return the theoretical duration of this treatment.
	 * @return The duration to apply this Treatment.
	 */
	public double getDuration() {
		return duration;
	}

	/**
	 * Get the required facility level.
	 */
	public int getFacilityLevel() {
		return facilityLevel;
	}

	/**
	 * Return the name of the treatment.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the Medical skill required for this treatment.
	 */
	public int getSkill() {
		return requiredSkill;
	}

	/**
	 * Can the treatment be self administered.
	 */
	public boolean getSelfAdminister() {
		return selfAdmin;
	}

	/**
	 * Hash code value for this object.
	 * @return hash code.
	 */
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * Return string representation.
	 * @return The treatment name.
	 */
	public String toString() {
		return name;
	}
}
