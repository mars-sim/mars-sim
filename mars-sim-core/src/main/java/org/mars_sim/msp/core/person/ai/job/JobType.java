/**
 * Mars Simulation Project
 * JobType.java
 * @version 3.1.0 2017-10-23
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mars_sim.msp.core.Msg;

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
	REPORTER			(Msg.getString("JobType.Reporter")), //$NON-NLS-1$
	
	TECHNICIAN			(Msg.getString("JobType.Technician")), //$NON-NLS-1$
	TRADER				(Msg.getString("JobType.Trader")) //$NON-NLS-1$
	;

	public static JobType[] JOB_TYPES = new JobType[]{
			ARCHITECT,
			AREOLOGIST,
			ASTRONOMER,
			BIOLOGIST,
			BOTANIST,
			
			CHEF,
			CHEMIST,
			DOCTOR,
			ENGINEER,
			MATHEMATICIAN,
			
			METEOROLOGIST,
			PHYSICIST,
			PILOT,
			POLITICIAN,
			REPORTER,
			
			TECHNICIAN,
			TRADER	
			};

	public static int numJobTypes = JOB_TYPES.length;
	
	private String name;
	
	private static Set<JobType> jobSet = new HashSet<JobType>(numJobTypes);
	
	private static List<String> jobList = new ArrayList<String>(numJobTypes);

	private static List<String> editedJobList = new ArrayList<String>(numJobTypes);

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
	
	public static JobType getJobType(String name) {
		if (name != null) {
	    	for (JobType ra : JobType.values()) {
	    		if (name.equalsIgnoreCase(ra.name)) {
	    			return ra;
	    		}
	    	}
		}
		
		return null;
	}

	public static Set<JobType> getSet() {
		if (jobSet.isEmpty()) {
			for (JobType ra : JobType.values()) {
				jobSet.add(ra);
			}
		}
		return jobSet;
	}

	public static List<String> getList() {
		if (jobList.isEmpty()) {
			for (JobType ra : JobType.values()) {
				jobList.add(ra.toString());
			}
		}
		return jobList;
	}

	public static List<String> getEditedList() {
		editedJobList = JobType.getList();
		editedJobList.remove(POLITICIAN.getName());
		Collections.sort(editedJobList);
		return editedJobList;
	}
		
	public static String getEditedJobString(int num) {
		if (editedJobList.isEmpty())
			getEditedList();
		return editedJobList.get(num);
	}
	
	public static int getJobNum(String s) {
		if (editedJobList.isEmpty())
			getEditedList();
		for (int i=0; i<editedJobList.size(); i++) {
			if (editedJobList.get(i).equalsIgnoreCase(s))
				return i;
		}
		return -1;
	}
	
	/**
	 * gives back a list of all valid values for the JobType enum.
	 */
	public static List<JobType> valuesList() {
		return Arrays.asList(JobType.values());
		// Arrays.asList() returns an ArrayList which is a private static class inside Arrays. 
		// It is not an java.util.ArrayList class.
		// Could possibly reconfigure this method as follows: 
		// public ArrayList<JobType> valuesList() {
		// return new ArrayList<JobType>(Arrays.asList(JobType.values())); }
	}
}
