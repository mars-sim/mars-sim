/*
 * Mars Simulation Project
 * ScienceType.java
 * @date 2025-08-22
 * @author stpa
 */

package com.mars_sim.core.science;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

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
	AREOLOGY("ARE", SkillType.AREOLOGY, JobType.AREOLOGIST),

	/** Concerned with the processes of life from micro to macro scale. */
	ASTROBIOLOGY("BIO", SkillType.ASTROBIOLOGY, JobType.ASTROBIOLOGIST),
	
	/** Keeping track of heavenly bodies. */
	ASTRONOMY("AST", SkillType.ASTRONOMY, JobType.ASTRONOMER),

	/** How to grow plants. */
	BOTANY("BOT", SkillType.BOTANY, JobType.BOTANIST),

	/** How to mix elements and compounds. */
	CHEMISTRY("CHE", SkillType.CHEMISTRY, JobType.CHEMIST),

	/** Provides fundamental computing skill. */
	COMPUTING ("COM", SkillType.COMPUTING, JobType.COMPUTER_SCIENTIST),

	/** How to make stuff. */
	ENGINEERING("ENG", SkillType.MATERIALS_SCIENCE, JobType.ENGINEER),

	/** Provides fundamental basics for all sciences. */
	MATHEMATICS("MAT", SkillType.MATHEMATICS, JobType.MATHEMATICIAN),

	/** Understanding, diagnosing, managing disease and ailments. */
	MEDICINE("MED", SkillType.MEDICINE, JobType.DOCTOR),

	/** Weather forecasting, climate modeling. */
	METEOROLOGY("MET", SkillType.METEOROLOGY, JobType.METEOROLOGIST),

	/** Laws of nature. Study of forces and mechanics. */
	PHYSICS("PHY", SkillType.PHYSICS, JobType.PHYSICIST),

	/** The Study of the politics.  */
	POLITICS("POL", SkillType.MANAGEMENT, JobType.POLITICIAN),
	
	/** The Study of the mind and behavior.  */
	PSYCHOLOGY("PSY", SkillType.PSYCHOLOGY, JobType.PSYCHOLOGIST),

	/** The Study of the social aspects of human beings.  */
	SOCIOLOGY("SOC", SkillType.ORGANISATION, JobType.SOCIOLOGIST);
	
	

	/** A list of science subjects for research projects. */
	private static List<ScienceType> sciencesSubjects = new ArrayList<>();

	/** A list of engineering subjects for development projects. */
	private static List<ScienceType> engineeringSubjects = new ArrayList<>();

	
	/** A map for keeping track of collaborative sciences. */
	private static EnumMap<ScienceType, Science> collabSciences = new EnumMap<>(ScienceType.class);
	 
	/** A map for matching a job type to science type */
	private static EnumMap<JobType, ScienceType> matchJobToScience = new EnumMap<>(JobType.class);
	 
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
		engineeringSubjects.add(MATHEMATICS);
		engineeringSubjects.add(PHYSICS);
	}
		
	/** 
	 * Initializes science subjects.
	 */
	static  {
		sciencesSubjects.add(AREOLOGY);
		sciencesSubjects.add(ASTRONOMY);
		sciencesSubjects.add(ASTROBIOLOGY);
		sciencesSubjects.add(BOTANY);
		sciencesSubjects.add(CHEMISTRY);
		sciencesSubjects.add(COMPUTING);
		sciencesSubjects.add(MATHEMATICS);
		sciencesSubjects.add(MEDICINE);
		sciencesSubjects.add(METEOROLOGY);
		sciencesSubjects.add(PHYSICS);
		sciencesSubjects.add(POLITICS);
		sciencesSubjects.add(PSYCHOLOGY);
		sciencesSubjects.add(SOCIOLOGY);
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
		Science astrobiology = collabSciences.get(ScienceType.ASTROBIOLOGY);
		Science botany = collabSciences.get(ScienceType.BOTANY);
		Science chemistry = collabSciences.get(ScienceType.CHEMISTRY);
		Science computing = collabSciences.get(ScienceType.COMPUTING);
		Science engineering = collabSciences.get(ScienceType.ENGINEERING);
		Science mathematics = collabSciences.get(ScienceType.MATHEMATICS);
		Science medicine = collabSciences.get(ScienceType.MEDICINE);
		Science meteorology = collabSciences.get(ScienceType.METEOROLOGY);
		Science physics = collabSciences.get(ScienceType.PHYSICS);
		Science psychology = collabSciences.get(ScienceType.PSYCHOLOGY);
		Science politics = collabSciences.get(ScienceType.POLITICS);
		Science sociology = collabSciences.get(ScienceType.SOCIOLOGY);
		
		areology.setCollaborativeSciences(new Science[]    { astrobiology, chemistry, physics, meteorology });
		astronomy.setCollaborativeSciences(new Science[]   { astrobiology, chemistry, mathematics, physics, computing});
		astrobiology.setCollaborativeSciences(new Science[]    { botany, chemistry, mathematics, medicine, astronomy });
		botany.setCollaborativeSciences(new Science[]      { astrobiology, chemistry, medicine });
		chemistry.setCollaborativeSciences(new Science[]   { astrobiology, mathematics, medicine, astronomy});
		computing.setCollaborativeSciences(new Science[]   { astronomy, engineering, physics, mathematics, medicine, meteorology });
		mathematics.setCollaborativeSciences(new Science[] { astronomy, engineering, physics, computing });
		medicine.setCollaborativeSciences(new Science[]    { astrobiology, botany, chemistry, mathematics });
		meteorology.setCollaborativeSciences(new Science[] { astronomy, chemistry, mathematics, physics });
		physics.setCollaborativeSciences(new Science[]     { astronomy, mathematics, engineering, computing});
		psychology.setCollaborativeSciences(new Science[]  { psychology, chemistry, medicine, sociology, politics});
		politics.setCollaborativeSciences(new Science[]  { psychology, sociology});
		sociology.setCollaborativeSciences(new Science[]  { psychology, politics});
	}

	static {
		
		for (ScienceType scienceType : ScienceType.values()) {
			matchJobToScience.put(scienceType.getJobType(), scienceType);
		}
	}
	
	/** 
	 * Hidden constructor. 
	 */
	private ScienceType(String code, SkillType skill, JobType job) {
        this.name = Msg.getStringOptional("ScienceType", name());
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
			return matchJobToScience.get(job);
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
		return RandomUtil.getRandomElement(sciencesSubjects);
	}
	
	/**
	 * Gets a random science type enum.
	 * 
	 * @return
	 */
	public static ScienceType getRandomEngineeringSubject() {
		return RandomUtil.getRandomElement(engineeringSubjects);
	}
	
}
