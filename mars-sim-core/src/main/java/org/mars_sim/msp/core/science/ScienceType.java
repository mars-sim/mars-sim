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
import org.mars_sim.msp.core.person.ai.job.JobType;

/**
 * Science field names and researcher job descriptions.
 */
public enum ScienceType {
	// the actual enum value is followed by data associated to the value.
	
	/** 1. Environmental science of mars. */
	AREOLOGY( 
			Msg.getString("ScienceType.areology"), //$NON-NLS-1$
			SkillType.AREOLOGY, JobType.AREOLOGIST),

	/** 2. Keeping track of heavenly bodies. */
	ASTRONOMY(Msg.getString("ScienceType.astronomy"), //$NON-NLS-1$
			SkillType.ASTRONOMY, JobType.ASTRONOMER),

	/** 3. Concerned with the processes of life from micro to macro scale. */
	BIOLOGY(Msg.getString("ScienceType.biology"), //$NON-NLS-1$
			SkillType.BIOLOGY, JobType.BIOLOGIST),

	/** 4. How to grow plants. */
	BOTANY(Msg.getString("ScienceType.botany"), //$NON-NLS-1$
			SkillType.BOTANY, JobType.BOTANIST),

	/** 5. How to mix elements and compounds. */
	CHEMISTRY(Msg.getString("ScienceType.chemistry"), //$NON-NLS-1$
			SkillType.CHEMISTRY, JobType.CHEMIST),

	/** 6. How to make stuff. */
	ENGINEERING(Msg.getString("ScienceType.engineering"), //$NON-NLS-1$
			SkillType.MATERIALS_SCIENCE, JobType.ENGINEER),

	/** 7. Provides fundamental basics for all sciences. */
	MATHEMATICS(Msg.getString("ScienceType.mathematics"), //$NON-NLS-1$
			SkillType.MATHEMATICS, JobType.MATHEMATICIAN),

	/** 8. How to tell sick from healthy. */
	MEDICINE(Msg.getString("ScienceType.medicine"), //$NON-NLS-1$
			SkillType.MEDICINE, JobType.DOCTOR),

	/** 9. Weather forecasting, climate modeling. */
	METEOROLOGY(Msg.getString("ScienceType.meteorology"), //$NON-NLS-1$
			SkillType.METEOROLOGY, JobType.METEOROLOGIST),
	
	/** 10. Laws of nature. Study of forces and mechanics. */
	PHYSICS(Msg.getString("ScienceType.physics"), //$NON-NLS-1$
			SkillType.PHYSICS, JobType.PHYSICIST),

	/** 11. The Study of the mind and behavior.  */
	PSYCHOLOGY(Msg.getString("ScienceType.psychology"), //$NON-NLS-1$
			SkillType.PSYCHOLOGY, JobType.PSYCHOLOGIST);
	
	/** used to keep track of collaborative sciences. */
	private static Map<ScienceType, Science> collabSciences;

	private String name;
	private JobType job;
	private SkillType skill;

	/** hidden constructor. */
	private ScienceType(String name, SkillType skill, JobType job) {
		this.name = name;
		this.job = job;
		this.skill = skill;
	}

	/**
	 * @return the internationalized name of this field of science.
	 */
	public final String getName() {
		return this.name;
	}

	public JobType getJobType() {
		return job;
	}

	public final SkillType getSkill() {
		return this.skill;
	}

	/** Initializes collaborative sciences. */
	private static void initSciences() {
		// Load available sciences in list.
		collabSciences = new ConcurrentHashMap<>();
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
	public static ScienceType getJobScience(JobType job) {
		ScienceType result = null;
		if (job != null) {
			if (collabSciences == null)
				initSciences();
			Iterator<Science> i = collabSciences.values().iterator();
			while (result == null && i.hasNext()) {
				Science science = i.next();
				List<JobType> jobs = science.getJobs();
				if (jobs.contains(job))
					result = science.getType();
			}
		}
		return result;
	}

	/** <code>true</code> if a scientist is needed for the job. */
	public static boolean isScienceJob(JobType job) {
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
