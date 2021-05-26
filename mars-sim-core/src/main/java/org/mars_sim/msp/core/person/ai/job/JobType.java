/**
 * Mars Simulation Project
 * JobType.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.job;

import java.util.Set;

import org.mars_sim.msp.core.Msg;

import com.google.common.collect.ImmutableSet;

public enum JobType {

	ARCHITECT			(Msg.getString("JobType.Architect")), //$NON-NLS-1$
	AREOLOGIST			(Msg.getString("JobType.Areologist")), //$NON-NLS-1$
	ASTRONOMER			(Msg.getString("JobType.Astronomer")), //$NON-NLS-1$
	BIOLOGIST			(Msg.getString("JobType.Biologist")), //$NON-NLS-1$
	BOTANIST			(Msg.getString("JobType.Botanist")), //$NON-NLS-1$t
	
	CHEF				(Msg.getString("JobType.Chef")), //$NON-NLS-1$
	CHEMIST				(Msg.getString("JobType.Chemist")), //$NON-NLS-1$
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

	/**
	 * That have an academic background
	 */
	public final static Set<JobType> ACADEMICS = 
				ImmutableSet.of(JobType.AREOLOGIST,
								JobType.ASTRONOMER,
								JobType.BIOLOGIST,
								JobType.BOTANIST,
								JobType.CHEMIST,
								JobType.DOCTOR,
								JobType.MATHEMATICIAN,
								JobType.METEOROLOGIST,
								JobType.PHYSICIST,
								JobType.PSYCHOLOGIST);

	/**
	 * That have are loaders
	 */
	public final static Set<JobType> LOADERS =
				ImmutableSet.of(JobType.ENGINEER,
								JobType.PILOT,
								JobType.TECHNICIAN,
								JobType.TRADER);
	/**
	 * That have an medical background
	 */
	public final static Set<JobType> MEDICS =
				ImmutableSet.of(JobType.DOCTOR,
								JobType.PSYCHOLOGIST);
	/**
	 * That have an mechanics background
	 */
	public final static Set<JobType> MECHANIICS =
				ImmutableSet.of(JobType.ENGINEER,
								JobType.PILOT,
								JobType.TECHNICIAN);
	/**
	 * That are used Lab aware
	 */
	public final static Set<JobType> SCIENTISTS =
				ImmutableSet.of(JobType.AREOLOGIST,
								JobType.ASTRONOMER,
								JobType.BIOLOGIST,
								JobType.BOTANIST,
								JobType.CHEMIST,
								JobType.PHYSICIST);
							
					
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
		throw new IllegalArgumentException("Cannot find a Job with the name " + name);
	}
}
