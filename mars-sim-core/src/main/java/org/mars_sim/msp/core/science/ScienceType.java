/**
 * Mars Simulation Project
 * ScienceType.java
 * @version 3.1.0 2017-10-23
 * @author stpa
 */
package org.mars_sim.msp.core.science;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Areologist;
import org.mars_sim.msp.core.person.ai.job.Astronomer;
import org.mars_sim.msp.core.person.ai.job.Biologist;
import org.mars_sim.msp.core.person.ai.job.Botanist;
import org.mars_sim.msp.core.person.ai.job.Chemist;
import org.mars_sim.msp.core.person.ai.job.Doctor;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.Mathematician;
import org.mars_sim.msp.core.person.ai.job.Meteorologist;
import org.mars_sim.msp.core.person.ai.job.Physicist;

/**
 * Science field names and researcher job descriptions.
 */
public enum ScienceType {

	/** environmental science of mars. */
	AREOLOGY( // the actual enum value is followed by data associated to the value.
			Msg.getString("ScienceType.areology"), //$NON-NLS-1$
			SkillType.AREOLOGY, Areologist.class),

	/** keeping track of heavenly bodies. */
	ASTRONOMY(Msg.getString("ScienceType.astronomy"), //$NON-NLS-1$
			SkillType.ASTRONOMY, Astronomer.class),

	/** concerned with the processes of life from micro to macro scale. */
	BIOLOGY(Msg.getString("ScienceType.biology"), //$NON-NLS-1$
			SkillType.BIOLOGY, Biologist.class),

	/** plants and how to grow them. */
	BOTANY(Msg.getString("ScienceType.botany"), //$NON-NLS-1$
			SkillType.BOTANY, Botanist.class),

	/** stuff and how to make it. */
	CHEMISTRY(Msg.getString("ScienceType.chemistry"), //$NON-NLS-1$
			SkillType.CHEMISTRY, Chemist.class),

	/** provides fundamental basics for all sciences. */
	MATHEMATICS(Msg.getString("ScienceType.mathematics"), //$NON-NLS-1$
			SkillType.MATHEMATICS, Mathematician.class),

	/** how to tell sick from healthy. */
	MEDICINE(Msg.getString("ScienceType.medicine"), //$NON-NLS-1$
			SkillType.MEDICINE, Doctor.class),

	/** weather forecasting, climate modelling. */
	METEOROLOGY(Msg.getString("ScienceType.meteorology"), //$NON-NLS-1$
			SkillType.METEOROLOGY, Meteorologist.class),

	PHYSICS(Msg.getString("ScienceType.physics"), //$NON-NLS-1$
			SkillType.PHYSICS, Physicist.class);

	/** used to keep track of collaborative sciences. */
	private static Map<ScienceType, Science> collabSciences;

	private String name;
	private Class<? extends Job> jobClass;
	private SkillType skill;

	/** hidden constructor. */
	private ScienceType(String name, SkillType skill, Class<? extends Job> jobClass) {
		this.name = name;
		this.jobClass = jobClass;
		this.skill = skill;
	}

	/**
	 * @return the internationalized name of this field of science.
	 */
	public final String getName() {
		return this.name;
	}

	public Class<? extends Job> getJobClass() {
		return this.jobClass;
	}

	public final SkillType getSkill() {
		return this.skill;
	}

	/** Initializes collaborative sciences. */
	private static void initSciences() {
		// Load available sciences in list.
		collabSciences = new HashMap<ScienceType, Science>();
		for (ScienceType scienceType : ScienceType.values()) {
			collabSciences.put(scienceType, new Science(scienceType));
		}
		// Configure collaborative sciences.
		Science areology = collabSciences.get(ScienceType.AREOLOGY);
		Science astronomy = collabSciences.get(ScienceType.ASTRONOMY);
		Science biology = collabSciences.get(ScienceType.BIOLOGY);
		Science botany = collabSciences.get(ScienceType.BOTANY);
		Science chemistry = collabSciences.get(ScienceType.CHEMISTRY);
		Science mathematics = collabSciences.get(ScienceType.MATHEMATICS);
		Science medicine = collabSciences.get(ScienceType.MEDICINE);
		Science meteorology = collabSciences.get(ScienceType.METEOROLOGY);
		Science physics = collabSciences.get(ScienceType.PHYSICS);

		areology.setCollaborativeSciences(new Science[] { biology, chemistry, mathematics, physics, meteorology });
		astronomy.setCollaborativeSciences(new Science[] { biology, chemistry, mathematics, physics });
		biology.setCollaborativeSciences(new Science[] { botany, chemistry, mathematics });
		botany.setCollaborativeSciences(new Science[] { biology, chemistry, mathematics });
		chemistry.setCollaborativeSciences(new Science[] { mathematics });
		mathematics.setCollaborativeSciences(new Science[] {});
		medicine.setCollaborativeSciences(new Science[] { biology, botany, chemistry, mathematics });
		meteorology.setCollaborativeSciences(new Science[] { chemistry, mathematics, physics });
		physics.setCollaborativeSciences(new Science[] { astronomy, mathematics });
	}

	/**
	 * Gives back the {@link ScienceType} associated with the given job or
	 * <code>null</code>.
	 * 
	 * @param job {@link Job}
	 * @return {@link ScienceType}
	 */
	public static ScienceType getJobScience(Job job) {
		ScienceType result = null;
		if (job != null) {
			if (collabSciences == null)
				initSciences();
			Iterator<Science> i = collabSciences.values().iterator();
			while (result == null && i.hasNext()) {
				Science science = i.next();
				List<Class<? extends Job>> jobs = science.getJobs();
				if (jobs.contains(job.getJobClass()))
					result = science.getType();
			}
//			for (Science science : collabSciences.values()) {
//				// here was some type mixup
//				if (science.getJobs().contains(science.getJobs())) result = science.getType();
//			}
		}
		return result;
	}

	/** <code>true</code> if a scientist is needed for the job. */
	public static boolean isScienceJob(Job job) {
		return getJobScience(job) != null;
	}

	/**
	 * Checks if a science is collaborative to a primary science.
	 * 
	 * @param sciencePrimary   {@link ScienceType}
	 * @param scienceSecondary {@link ScienceType}
	 * @return {@link Boolean}
	 */
	public static boolean isCollaborativeScience(ScienceType sciencePrimary, ScienceType scienceSecondary) {
		if (collabSciences == null)
			initSciences();
		return collabSciences.get(sciencePrimary).getCollaborativeSciences().contains(scienceSecondary);
	}

	/**
	 * Gives back a list of all valid values for the ScienceType enum.
	 */
	public static List<ScienceType> valuesList() {
		return Arrays.asList(ScienceType.values());
		// Arrays.asList() returns an ArrayList which is a private static class inside
		// Arrays.
		// It is not an java.util.ArrayList class.
		// Could possibly reconfigure this method as follows:
		// public ArrayList<ScienceType> valuesList() {
		// return new ArrayList<ScienceType>(Arrays.asList(ScienceType.values())); }
	}
	
	public static ScienceType getType(String name) {
		if (name != null) {
	    	for (ScienceType t : ScienceType.values()) {
	    		if (name.equalsIgnoreCase(t.name)) {
	    			return t;
	    		}
	    	}
		}
		
		return null;
	}
}
