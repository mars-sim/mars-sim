/*
 * Mars Simulation Project
 * Job.java
 * @date 2021-09-27
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.job;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * The Job class represents a person's job.
 */
public abstract class Job {



	private static final String JOB_STR = "job.";
	private static final String MALE_STR = "male.";
	private static final String FEMALE_STR = "female.";
	private static final String UNKNOWN = "unknown.";


	private Map<RoleType, Double> jobProspects;
	private JobType jobType;

	protected static MissionManager missionManager = Simulation.instance().getMissionManager();
	protected static UnitManager unitManager = Simulation.instance().getUnitManager();
	
	/**
	 * Constructor.
	 * @param jobProspects 
	 * 
	 * @param name the name of the job.
	 */
	protected Job(JobType jobType, Map<RoleType, Double> jobProspects) {
		this.jobType = jobType;
		this.jobProspects = jobProspects;
	}

	/**
	 * Gets the job's internationalized name for display in user interface. This
	 * uses directly the name of the class that extends {@link Job}, so take care
	 * not to rename those, or if you do then remember to change the keys in
	 * <code>messages.properties</code> accordingly.
	 * 
	 * @param gender {@link GenderType}
	 * @return name
	 */
	public String getName(GenderType gender) {
		StringBuffer key = new StringBuffer().append(JOB_STR); // $NON-NLS-1$
		switch (gender) {
		case MALE:
			key.append(MALE_STR);
			break; // $NON-NLS-1$
		case FEMALE:
			key.append(FEMALE_STR);
			break; // $NON-NLS-1$
		default:
			key.append(UNKNOWN);
			break; // $NON-NLS-1$
		}
		key.append(this.getClass().getSimpleName());
		return Msg.getString(key.toString()); // $NON-NLS-1$
	};

	public JobType getType() {
		return jobType;
	}
	
	/**
	 * Gets a person/robot's capability to perform this job.
	 * 
	 * @param person/robot the person/robot to check.
	 * @return capability (min 0.0).
	 */
	public abstract double getCapability(Person person);

	/**
	 * Gets the base settlement need for this job.
	 * 
	 * @param settlement the settlement in need.
	 * @return the base need >= 0
	 */
	public abstract double getSettlementNeed(Settlement settlement);

	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param u {@link UnitManager}
	 * @param m {@link MissionManager}
	 */
	public static void initializeInstances(UnitManager u, MissionManager m) {
		unitManager = u;
		missionManager = m;
	}

	/**
	 * Build a Map to cover the Specialist RoleTypes.
	 * @return
	 */
	protected static Map<RoleType, Double> buildRoleMap(
		  double agr, double com, double eng, double mis, double log, double res, double saf, double sci) {
		
		Map<RoleType, Double> m = new EnumMap<>(RoleType.class);
		m.put(RoleType.AGRICULTURE_SPECIALIST, agr);
		m.put(RoleType.COMPUTING_SPECIALIST, com);
		m.put(RoleType.ENGINEERING_SPECIALIST, eng);
		m.put(RoleType.MISSION_SPECIALIST, mis);
		m.put(RoleType.LOGISTIC_SPECIALIST, log);
		m.put(RoleType.RESOURCE_SPECIALIST, res);
		m.put(RoleType.SAFETY_SPECIALIST, saf);
		m.put(RoleType.SCIENCE_SPECIALIST, sci);	

		return Collections.unmodifiableMap(m);
	}
	
	public Map<RoleType,Double> getRoleProspects() {
		return jobProspects;
	}

}
