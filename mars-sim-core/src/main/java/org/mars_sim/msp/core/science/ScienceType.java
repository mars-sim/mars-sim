/**
 * Mars Simulation Project
 * ScienceType.java
 * @version 3.1.2 2020-09-02
 * @author stpa
 */

package org.mars_sim.msp.core.science;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Areologist;
import org.mars_sim.msp.core.person.ai.job.Astronomer;
import org.mars_sim.msp.core.person.ai.job.Biologist;
import org.mars_sim.msp.core.person.ai.job.Botanist;
import org.mars_sim.msp.core.person.ai.job.Chemist;
import org.mars_sim.msp.core.person.ai.job.Engineer;
import org.mars_sim.msp.core.person.ai.job.Doctor;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.Mathematician;
import org.mars_sim.msp.core.person.ai.job.Meteorologist;
import org.mars_sim.msp.core.person.ai.job.Physicist;
import org.mars_sim.msp.core.person.ai.job.Psychologist;

/**
 * Science field names and researcher job descriptions.
 */
public enum ScienceType {
	// the actual enum value is followed by data associated to the value.
	
	/** 1. Environmental science of mars. */
	AREOLOGY( 
			Msg.getString("ScienceType.areology"), //$NON-NLS-1$
			SkillType.AREOLOGY, Areologist.class),

	/** 2. Keeping track of heavenly bodies. */
	ASTRONOMY(Msg.getString("ScienceType.astronomy"), //$NON-NLS-1$
			SkillType.ASTRONOMY, Astronomer.class),

	/** 3. Concerned with the processes of life from micro to macro scale. */
	BIOLOGY(Msg.getString("ScienceType.biology"), //$NON-NLS-1$
			SkillType.BIOLOGY, Biologist.class),

	/** 4. How to grow plants. */
	BOTANY(Msg.getString("ScienceType.botany"), //$NON-NLS-1$
			SkillType.BOTANY, Botanist.class),

	/** 5. How to mix elements and compounds. */
	CHEMISTRY(Msg.getString("ScienceType.chemistry"), //$NON-NLS-1$
			SkillType.CHEMISTRY, Chemist.class),

	/** 6. How to make stuff. */
	ENGINEERING(Msg.getString("ScienceType.engineering"), //$NON-NLS-1$
			SkillType.MATERIALS_SCIENCE, Engineer.class),

	/** 7. Provides fundamental basics for all sciences. */
	MATHEMATICS(Msg.getString("ScienceType.mathematics"), //$NON-NLS-1$
			SkillType.MATHEMATICS, Mathematician.class),

	/** 8. How to tell sick from healthy. */
	MEDICINE(Msg.getString("ScienceType.medicine"), //$NON-NLS-1$
			SkillType.MEDICINE, Doctor.class),

	/** 9. Weather forecasting, climate modeling. */
	METEOROLOGY(Msg.getString("ScienceType.meteorology"), //$NON-NLS-1$
			SkillType.METEOROLOGY, Meteorologist.class),
	
	/** 10. Laws of nature. Study of forces and mechanics. */
	PHYSICS(Msg.getString("ScienceType.physics"), //$NON-NLS-1$
			SkillType.PHYSICS, Physicist.class),

	/** 11. The Study of the mind and behavior.  */
	PSYCHOLOGY(Msg.getString("ScienceType.psychology"), //$NON-NLS-1$
			SkillType.PSYCHOLOGY, Psychologist.class);
	
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
		collabSciences = new ConcurrentHashMap<ScienceType, Science>();
		for (ScienceType scienceType : ScienceType.values()) {
			collabSciences.put(scienceType, new Science(scienceType));
		}
		// Configure collaborative sciences.
		Science areology = collabSciences.get(ScienceType.AREOLOGY);
		Science astronomy = collabSciences.get(ScienceType.ASTRONOMY);
		Science biology = collabSciences.get(ScienceType.BIOLOGY);
		Science botany = collabSciences.get(ScienceType.BOTANY);
		Science chemistry = collabSciences.get(ScienceType.CHEMISTRY);
		Science engineering = collabSciences.get(ScienceType.ENGINEERING);
		Science mathematics = collabSciences.get(ScienceType.MATHEMATICS);
		Science medicine = collabSciences.get(ScienceType.MEDICINE);
		Science meteorology = collabSciences.get(ScienceType.METEOROLOGY);
		Science physics = collabSciences.get(ScienceType.PHYSICS);
		Science psychology = collabSciences.get(ScienceType.PSYCHOLOGY);
		
		areology.setCollaborativeSciences(new Science[]    { biology, chemistry, physics, meteorology });
		astronomy.setCollaborativeSciences(new Science[]   { biology, chemistry, mathematics, physics });
		biology.setCollaborativeSciences(new Science[]     { botany, chemistry, mathematics, medicine });
		botany.setCollaborativeSciences(new Science[]      { biology, chemistry, medicine });
		chemistry.setCollaborativeSciences(new Science[]   { biology, mathematics, medicine });
		mathematics.setCollaborativeSciences(new Science[] { astronomy, engineering, physics });
		medicine.setCollaborativeSciences(new Science[]    { biology, botany, chemistry, mathematics });
		meteorology.setCollaborativeSciences(new Science[] { chemistry, mathematics, physics });
		physics.setCollaborativeSciences(new Science[]     { astronomy, mathematics, engineering });
		psychology.setCollaborativeSciences(new Science[]  { biology, chemistry, medicine });
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
