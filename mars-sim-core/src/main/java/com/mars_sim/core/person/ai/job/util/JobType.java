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

import com.mars_sim.core.Named;
import com.mars_sim.core.tool.Msg;

public enum JobType implements Named{

	ARCHITECT, AREOLOGIST, ASTRONOMER, ASTROBIOLOGIST, BOTANIST,
	CHEF, CHEMIST, COMPUTER_SCIENTIST, DOCTOR, ENGINEER, MATHEMATICIAN,
	METEOROLOGIST, PHYSICIST, PILOT, POLITICIAN, PSYCHOLOGIST,
	REPORTER, SOCIOLOGIST, TECHNICIAN, TRADER;

	// Internals allows to be built is static initialiser method
	// Academic subjects
	private static final Set<JobType> INTERNAL_ACA = new HashSet<>();
	// Intellectual subjects
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
						JobType.ASTROBIOLOGIST,
						JobType.BOTANIST,
						JobType.CHEMIST,
						JobType.COMPUTER_SCIENTIST,
						JobType.PHYSICIST,
						JobType.SOCIOLOGIST
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
	private JobType() {
        this.name = Msg.getStringOptional("JobType", name());
	}

	public final String getName() {
		return this.name;
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
