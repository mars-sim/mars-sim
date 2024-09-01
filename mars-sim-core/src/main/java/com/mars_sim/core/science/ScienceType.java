/*
 * Mars Simulation Project
 * ScienceType.java
 * @date 2021-09-27
 * @author stpa
 */

package com.mars_sim.core.science;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.Job;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * Science field names and researcher job descriptions.
 */
public enum ScienceType {
	// the actual enum value is followed by data associated to the value.

	/** The study of the evolution of the planet Mars. */
	AREOLOGY(
			Msg.getString("ScienceType.areology"), //$NON-NLS-1$
			"ARE", SkillType.AREOLOGY, JobType.AREOLOGIST),

	/** Keeping track of heavenly bodies. */
	ASTRONOMY(Msg.getString("ScienceType.astronomy"), //$NON-NLS-1$
			"AST", SkillType.ASTRONOMY, JobType.ASTRONOMER),

	/** Concerned with the processes of life from micro to macro scale. */
	BIOLOGY(Msg.getString("ScienceType.biology"), //$NON-NLS-1$
			"BIO", SkillType.BIOLOGY, JobType.BIOLOGIST),

	/** How to grow plants. */
	BOTANY(Msg.getString("ScienceType.botany"), //$NON-NLS-1$
			"BOT", SkillType.BOTANY, JobType.BOTANIST),

	/** How to mix elements and compounds. */
	CHEMISTRY(Msg.getString("ScienceType.chemistry"), //$NON-NLS-1$
			"CHE", SkillType.CHEMISTRY, JobType.CHEMIST),

	/** Provides fundamental computing skill. */
	COMPUTING (Msg.getString("ScienceType.computing"), //$NON-NLS-1$
			"COM", SkillType.COMPUTING, JobType.COMPUTER_SCIENTIST),

	/** How to make stuff. */
	ENGINEERING(Msg.getString("ScienceType.engineering"), //$NON-NLS-1$
			"ENG", SkillType.MATERIALS_SCIENCE, JobType.ENGINEER),

	/** Provides fundamental basics for all sciences. */
	MATHEMATICS(Msg.getString("ScienceType.mathematics"), //$NON-NLS-1$
			"MAT", SkillType.MATHEMATICS, JobType.MATHEMATICIAN),

	/** How to tell sick from healthy. */
	MEDICINE(Msg.getString("ScienceType.medicine"), //$NON-NLS-1$
			"MED", SkillType.MEDICINE, JobType.DOCTOR),

	/** Weather forecasting, climate modeling. */
	METEOROLOGY(Msg.getString("ScienceType.meteorology"), //$NON-NLS-1$
			"MET", SkillType.METEOROLOGY, JobType.METEOROLOGIST),

	/** Laws of nature. Study of forces and mechanics. */
	PHYSICS(Msg.getString("ScienceType.physics"), //$NON-NLS-1$
			"PHY", SkillType.PHYSICS, JobType.PHYSICIST),

	/** The Study of the mind and behavior.  */
	PSYCHOLOGY(Msg.getString("ScienceType.psychology"), //$NON-NLS-1$
			"PSY", SkillType.PSYCHOLOGY, JobType.PSYCHOLOGIST);


	/** A list of science subjects for research projects. */
	private static List<ScienceType> sciencesSubjects = new ArrayList<>();

	/** A list of engineering subjects for development projects. */
	private static List<ScienceType> engineeringSubjects = new ArrayList<>();

	
	/** Maps for keeping track of collaborative sciences. */
	private static Map<ScienceType, Science> collabSciences = new HashMap<>();
	 
	private String name;
	private String code;
	private JobType job;
	private SkillType skill;

	/** 
	 * Initializes engineering subjects.
	 */
	static  {
		engineeringSubjects.add(AREOLOGY);
		engineeringSubjects.add(COMPUTING);
		engineeringSubjects.add(ENGINEERING);
	}
		
	/** 
	 * Initializes science subjects.
	 */
	static  {
		sciencesSubjects.add(AREOLOGY);
		sciencesSubjects.add(ASTRONOMY);
		sciencesSubjects.add(BIOLOGY);
		sciencesSubjects.add(BOTANY);
		sciencesSubjects.add(CHEMISTRY);
		sciencesSubjects.add(COMPUTING);
		sciencesSubjects.add(MATHEMATICS);
		sciencesSubjects.add(MEDICINE);
		sciencesSubjects.add(METEOROLOGY);
		sciencesSubjects.add(PHYSICS);
		sciencesSubjects.add(PSYCHOLOGY);
	}
	
	/** 
	 * Initializes collaborative sciences.
	 */
	static  {
		// Load available sciences in list.

		for (ScienceType scienceType : ScienceType.values()) {
			collabSciences.put(scienceType, new Science(scienceType));
		}

		// Configure collaborative sciences.
		Science areology = collabSciences.get(ScienceType.AREOLOGY);
		Science astronomy = collabSciences.get(ScienceType.ASTRONOMY);
		Science biology = collabSciences.get(ScienceType.BIOLOGY);
		Science botany = collabSciences.get(ScienceType.BOTANY);
		Science chemistry = collabSciences.get(ScienceType.CHEMISTRY);
		Science computing = collabSciences.get(ScienceType.COMPUTING);
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
		computing.setCollaborativeSciences(new Science[]   { astronomy, engineering, physics, mathematics, medicine, meteorology });
		mathematics.setCollaborativeSciences(new Science[] { astronomy, engineering, physics });
		medicine.setCollaborativeSciences(new Science[]    { biology, botany, chemistry, mathematics });
		meteorology.setCollaborativeSciences(new Science[] { chemistry, mathematics, physics });
		physics.setCollaborativeSciences(new Science[]     { astronomy, mathematics, engineering });
		psychology.setCollaborativeSciences(new Science[]  { biology, chemistry, medicine });
	}

	/** 
	 * Hidden constructor. 
	 */
	private ScienceType(String name, String code, SkillType skill, JobType job) {
		this.name = name;
		this.code = code;
		this.job = job;
		this.skill = skill;
	}

	/**
	 * Gets the internationalized name of this field of science.
	 * 
	 * @return 
	 */
	public final String getName() {
		return this.name;
	}

	public String getCode() {
		return code;
	}

	public JobType getJobType() {
		return job;
	}

	public final SkillType getSkill() {
		return this.skill;
	}

	/**
	 * Gives back the {@link ScienceType} associated with the given job or
	 * <code>null</code>.
	 *
	 * @param job {@link Job}
	 * @return {@link ScienceType}
	 */
	public static ScienceType getJobScience(JobType job) {
		if (job != null) {
			for(Science science : collabSciences.values()) {
				List<JobType> jobs = science.getJobs();
				if (jobs.contains(job))
					return science.getType();
			}
		}
		return null;
	}

	/**
	 * Checks if a science is collaborative to a primary science.
	 *
	 * @param sciencePrimary   {@link ScienceType}
	 * @param scienceSecondary {@link ScienceType}
	 * @return {@link Boolean}
	 */
	public static boolean isCollaborativeScience(ScienceType sciencePrimary, ScienceType scienceSecondary) {
		return collabSciences.get(sciencePrimary).getCollaborativeSciences().contains(scienceSecondary);
	}
	
	/**
	 * Gets a random science type enum.
	 * 
	 * @return
	 */
	public static ScienceType getRandomScienceType() {
		List<ScienceType> list = sciencesSubjects; //Arrays.asList(ScienceType.values());		
		int rand = RandomUtil.getRandomInt(list.size() - 1);
		return list.get(rand);
	}
	
	/**
	 * Gets a random science type enum.
	 * 
	 * @return
	 */
	public static ScienceType getRandomEngineeringSubject() {
		List<ScienceType> list = engineeringSubjects;		
		int rand = RandomUtil.getRandomInt(list.size() - 1);
		return list.get(rand);
	}
	
}
