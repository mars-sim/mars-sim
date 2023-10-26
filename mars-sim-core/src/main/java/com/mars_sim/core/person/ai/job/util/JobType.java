/*
 * Mars Simulation Project
 * JobType.java
 * @Date 2021-09-27
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.job.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.mars_sim.tools.Msg;

public enum JobType {

	ARCHITECT			(Msg.getString("JobType.Architect")), //$NON-NLS-1$
	AREOLOGIST			(Msg.getString("JobType.Areologist")), //$NON-NLS-1$
	ASTRONOMER			(Msg.getString("JobType.Astronomer")), //$NON-NLS-1$
	BIOLOGIST			(Msg.getString("JobType.Biologist")), //$NON-NLS-1$
	BOTANIST			(Msg.getString("JobType.Botanist")), //$NON-NLS-1$t

	CHEF				(Msg.getString("JobType.Chef")), //$NON-NLS-1$
	CHEMIST				(Msg.getString("JobType.Chemist")), //$NON-NLS-1$
	COMPUTER_SCIENTIST  (Msg.getString("JobType.ComputerScientist")),  //$NON-NLS-1$
	DOCTOR				(Msg.getString("JobType.Doctor")), //$NON-NLS-1$
	ENGINEER			(Msg.getString("JobType.Engineer")), //$NON-NLS-1$

	MATHEMATICIAN		(Msg.getString("JobType.Mathematician")), //$NON-NLS-1$
	METEOROLOGIST		(Msg.getString("JobType.Meteorologist")), //$NON-NLS-1$
	PHYSICIST			(Msg.getString("JobType.Physicist")), //$NON-NLS-1$
	PILOT				(Msg.getString("JobType.Pilot")), //$NON-NLS-1$
	POLITICIAN			(Msg.getString("JobType.Politician")), //$NON-NLS-1$

	PSYCHOLOGIST		(Msg.getString("JobType.Psychologist")), //$NON-NLS-1$
	REPORTER			(Msg.getString("JobType.Reporter")), //$NON-NLS-1$
	TECHNICIAN			(Msg.getString("JobType.Technician")), //$NON-NLS-1$
	TRADER				(Msg.getString("JobType.Trader")), //$NON-NLS-1$
	;

	// Interals allows to be built is static initiaiser method
	private static final Set<JobType> INTERNAL_ACA = new HashSet<>();
	private static final Set<JobType> INTERNAL_INT = new HashSet<>();

	/**
	 * Those having an academic background. This is a combination of
	 * INTELLECTUALS and others
	 */
	public static final Set<JobType> ACADEMICS = Collections.unmodifiableSet(INTERNAL_ACA);

	/**
	 * Those having an intellectuals background. This is a combination of
	 * SCIENTISTS & MEDICS.
	 */
	public static final Set<JobType> INTELLECTUALS = Collections.unmodifiableSet(INTERNAL_INT);

	/**
	 * Those who are loaders.
	 */
	public static final Set<JobType> LOADERS =
				Set.of(JobType.ENGINEER,
						JobType.PILOT,
						JobType.TECHNICIAN,
						JobType.TRADER);
	/**
	 * Those having an medical background.
	 */
	public static final Set<JobType> MEDICS =
				Set.of(JobType.DOCTOR,
						JobType.PSYCHOLOGIST);
	/**
	 * Those having an mechanics background.
	 */
	public static final Set<JobType> MECHANICS =
				Set.of(JobType.ENGINEER,
						JobType.PILOT,
						JobType.TECHNICIAN);
	/**
	 * Those in need of using a lab.
	 */
	public static final Set<JobType> SCIENTISTS =
				Set.of(JobType.AREOLOGIST,
						JobType.BIOLOGIST,
						JobType.BOTANIST,
						JobType.CHEMIST,
						JobType.COMPUTER_SCIENTIST,
						JobType.PHYSICIST
						);

	static {
		INTERNAL_INT.addAll(SCIENTISTS);
		INTERNAL_INT.addAll(MEDICS);

		INTERNAL_ACA.addAll(INTERNAL_INT);
		INTERNAL_ACA.add(JobType.ASTRONOMER);
		INTERNAL_ACA.add(JobType.ENGINEER);
		INTERNAL_ACA.add(JobType.MATHEMATICIAN);
		INTERNAL_ACA.add(JobType.METEOROLOGIST);
	}

	private String name;

	/** hidden constructor. */
	private JobType(String name) {
		this.name = name;
	}

	public final String getName() {
		return this.name;
	}

	@Override
	public final String toString() {
		return getName();
	}

	public static JobType getJobTypeByName(String name) {
		if (name != null) {
	    	for (JobType ra : JobType.values()) {
	    		if (name.equalsIgnoreCase(ra.name)) {
	    			return ra;
	    		}
	    	}
		}
		return null;
	}
}
