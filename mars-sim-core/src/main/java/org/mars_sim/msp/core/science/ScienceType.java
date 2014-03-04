package org.mars_sim.msp.core.science;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobManager;

/**
 * science field names and researcher job descriptions.
 * @author stpa
 * 2014-03-03
 */
public enum ScienceType {

	/** environmental science of mars. */
	AREOLOGY ( // the actual enum value is followed by data associated to the value.
		Msg.getString("ScienceType.areology.science"), //$NON-NLS-1$
		Msg.getString("ScienceType.areology.male"), //$NON-NLS-1$
		Msg.getString("ScienceType.areology.female"), //$NON-NLS-1$
		SkillType.AREOLOGY
	),

	/** keeping track of heavenly bodies. */
	ASTRONOMY (
		Msg.getString("ScienceType.astronomy.science"), //$NON-NLS-1$
		Msg.getString("ScienceType.astronomy.male"), //$NON-NLS-1$
		Msg.getString("ScienceType.astronomy.female"), //$NON-NLS-1$
		SkillType.ASTRONOMY
	),

	/** concerned with the processes of life from micro to macro scale. */
	BIOLOGY (
		Msg.getString("ScienceType.biology.science"), //$NON-NLS-1$
		Msg.getString("ScienceType.biology.male"), //$NON-NLS-1$
		Msg.getString("ScienceType.biology.female"), //$NON-NLS-1$
		SkillType.BIOLOGY
	),

	/** plants and how to grow them. */
	BOTANY (
		Msg.getString("ScienceType.botany.science"), //$NON-NLS-1$
		Msg.getString("ScienceType.botany.male"), //$NON-NLS-1$
		Msg.getString("ScienceType.botany.female"), //$NON-NLS-1$
		SkillType.BOTANY
	),

	/** stuff and how to make it. */
	CHEMISTRY (
		Msg.getString("ScienceType.chemistry.science"), //$NON-NLS-1$
		Msg.getString("ScienceType.chemistry.male"), //$NON-NLS-1$
		Msg.getString("ScienceType.chemistry.female"), //$NON-NLS-1$
		SkillType.CHEMISTRY
	),

	/** provides fundamental basics for all sciences. */
	MATHEMATICS (
		Msg.getString("ScienceType.mathematics.science"), //$NON-NLS-1$
		Msg.getString("ScienceType.mathematics.male"), //$NON-NLS-1$
		Msg.getString("ScienceType.mathematics.female"), //$NON-NLS-1$
		SkillType.MATHEMATICS
	),

	/** how to tell sick from healthy. */
	MEDICINE (
		Msg.getString("ScienceType.medicine.science"), //$NON-NLS-1$
		Msg.getString("ScienceType.medicine.male"), //$NON-NLS-1$
		Msg.getString("ScienceType.medicine.female"), //$NON-NLS-1$
		SkillType.MEDICINE
	),

	/** weather forecasting, climate modelling. */
	METEOROLOGY (
		Msg.getString("ScienceType.meteorology.science"), //$NON-NLS-1$
		Msg.getString("ScienceType.meteorology.male"), //$NON-NLS-1$
		Msg.getString("ScienceType.meteorology.female"), //$NON-NLS-1$
		SkillType.METEOROLOGY
	),

	PHYSICS (
		Msg.getString("ScienceType.physics.science"), //$NON-NLS-1$
		Msg.getString("ScienceType.physics.male"), //$NON-NLS-1$
		Msg.getString("ScienceType.physics.female"), //$NON-NLS-1$
		SkillType.PHYSICS
	);

	/** used to keep track of collaborative sciences. */
	private static Map<ScienceType,Science> collabSciences;

	private String name;
	private String jobMale;
	private String jobFemale;
	private SkillType skill;

	/** hidden constructor. */
	private ScienceType(String name, String jobMale, String jobFemale, SkillType skill) {
		this.name = name;
		this.jobFemale = jobFemale;
		this.jobMale = jobMale;
		this.skill = skill;
	}

	/**
	 * @return the internationalized name of this field of science.
	 */
	public final String getName() {
		return this.name;
	}

	/**
	 * @return the internationalized job description of singular male scientists in this field.
	 */
	public final String getJobMale() {
		return this.jobMale;
	}

	/**
	 * @return the internationalized job description of singular female scientists in this field.
	 */
	public final String getJobFemale() {
		return this.jobFemale;
	}

	public final SkillType getSkill() {
		return this.skill;
	}

	/** initialize collaborative sciences. */
	static {
		// Load available sciences in list.
		collabSciences = new HashMap<ScienceType,Science>();
		for (ScienceType science : ScienceType.values()) {
			collabSciences.put(
				science,
				new Science(science,JobManager.getJob(science.getName()))
			);
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

		areology.setCollaborativeSciences(new Science[] { biology, chemistry, mathematics, meteorology });
		astronomy.setCollaborativeSciences(new Science[] { biology, chemistry, mathematics, physics });
		biology.setCollaborativeSciences(new Science[] { botany, chemistry, mathematics });
		botany.setCollaborativeSciences(new Science[] { biology, chemistry, mathematics });
		chemistry.setCollaborativeSciences(new Science[] { mathematics });
		mathematics.setCollaborativeSciences(new Science[] {});
		medicine.setCollaborativeSciences(new Science[] { biology, botany, chemistry, mathematics });
		meteorology.setCollaborativeSciences(new Science[] { chemistry, mathematics, physics });
		physics.setCollaborativeSciences(new Science[] { astronomy, mathematics });
	}

	/** gives back the {@link ScienceType} associated with the given job or <code>null</code>. */
	public static ScienceType getJobScience(Job job) {
		ScienceType result = null;
		for (Science science : collabSciences.values()) {
			if (science.getJobs().contains(science)) result = science.getType();
		}
		return result;
	}

	/** <code>true</code> if a scientist is needed for the job. */
	public static boolean isScienceJob(Job job) {
		return getJobScience(job) != null;
	}

	/**
	 * Checks if a science is collaborative to a primary science.
	 * @param sciencePrimary {@link ScienceType}
	 * @param scienceSecondary {@link ScienceType}
	 * @return {@link Boolean}
	 */
	public static boolean isCollaborativeScience(ScienceType sciencePrimary, ScienceType scienceSecondary) {
		return collabSciences
		.get(sciencePrimary)
		.getCollaborativeSciences()
		.contains(scienceSecondary);
	}

	/**
	 * gives back a list of all valid values for the ScienceType enum.
	 */
	public static List<ScienceType> valuesList() {
		return Arrays.asList(ScienceType.values());
	}
}
